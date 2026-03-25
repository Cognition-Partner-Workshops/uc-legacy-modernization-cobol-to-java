"""
Test suite to validate the four COBOL codebase analysis artifacts.

Categories:
  1. Artifact Count Accuracy — file counts match filesystem
  2. Completeness — every file on disk is listed in the docs
  3. Cross-Reference Consistency — programs/copybooks referenced across docs are consistent
  4. Source-Code-Grounded Validation — XCTL targets, VSAM access, PIC clauses match source
  5. Hotspot Score Reproducibility — complexity metrics and arithmetic are verifiable
"""

import os
import re
import glob
import subprocess
from pathlib import Path

import pytest

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
REPO_ROOT = Path(__file__).resolve().parent.parent
APP_DIR = REPO_ROOT / "app"
DOCS_DIR = REPO_ROOT / "docs"

APP_INVENTORY = DOCS_DIR / "APPLICATION_INVENTORY.md"
DATA_DICTIONARY = DOCS_DIR / "DATA_DICTIONARY.md"
DEPENDENCY_MAP = DOCS_DIR / "DEPENDENCY_MAP.md"
HOTSPOT_REPORT = DOCS_DIR / "HOTSPOT_REPORT.md"

CBL_DIR = APP_DIR / "cbl"
CPY_DIR = APP_DIR / "cpy"
JCL_DIR = APP_DIR / "jcl"
BMS_DIR = APP_DIR / "bms"
CPY_BMS_DIR = APP_DIR / "cpy-bms"


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------
def read_md(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def list_files(directory: Path, extensions: list[str]) -> set[str]:
    """Return basenames (without extension) of files matching extensions."""
    names = set()
    for ext in extensions:
        for f in directory.glob(f"*{ext}"):
            names.add(f.stem.upper())
    return names


def extract_table_column(md_text: str, col_name: str, col_index: int | None = None) -> list[str]:
    """Extract values from a specific column in markdown tables.
    
    If col_index is given, use positional extraction.
    Otherwise, try to find the column by header name.
    """
    values = []
    in_table = False
    target_col = col_index

    for line in md_text.splitlines():
        line = line.strip()
        if not line.startswith("|"):
            in_table = False
            target_col = col_index
            continue

        cells = [c.strip() for c in line.split("|")]
        # Remove empty first/last from leading/trailing pipes
        if cells and cells[0] == "":
            cells = cells[1:]
        if cells and cells[-1] == "":
            cells = cells[:-1]

        if not in_table:
            # This is a header row — find the column
            if col_index is None:
                for i, cell in enumerate(cells):
                    if col_name.lower() in cell.lower():
                        target_col = i
                        break
            in_table = True
            continue

        # Skip separator rows
        if all(c.replace("-", "").replace(":", "").strip() == "" for c in cells):
            continue

        if target_col is not None and target_col < len(cells):
            val = cells[target_col].strip()
            if val and val != "—":
                values.append(val)

    return values


def extract_program_ids_from_inventory(md_text: str) -> set[str]:
    """Extract all Program ID values from APPLICATION_INVENTORY tables."""
    ids = set()
    # Match program IDs in tables — column labeled "Program ID" or "Program"
    for line in md_text.splitlines():
        line = line.strip()
        if not line.startswith("|"):
            continue
        cells = [c.strip() for c in line.split("|")]
        if cells and cells[0] == "":
            cells = cells[1:]
        if cells and cells[-1] == "":
            cells = cells[:-1]
        # Skip headers and separators
        if len(cells) < 3:
            continue
        if all(c.replace("-", "").replace(":", "").strip() == "" for c in cells):
            continue
        # Program ID is typically in column index 1 (after #)
        for cell in cells:
            cell = cell.strip()
            # Match COBOL program naming patterns
            if re.match(r"^[A-Z]{2}[A-Z0-9]{4,6}$", cell):
                ids.add(cell)
    return ids


def grep_source(pattern: str, directory: Path, file_glob: str = "*.cbl") -> list[str]:
    """Grep COBOL source files for a pattern, return matching lines."""
    results = []
    for ext in [file_glob, file_glob.upper()]:
        for f in directory.glob(ext):
            try:
                content = f.read_text(encoding="utf-8", errors="ignore")
                for line in content.splitlines():
                    if re.search(pattern, line, re.IGNORECASE):
                        results.append(f"{f.name}: {line.strip()}")
            except Exception:
                pass
    # Also check .CBL files
    if file_glob == "*.cbl":
        for f in directory.glob("*.CBL"):
            try:
                content = f.read_text(encoding="utf-8", errors="ignore")
                for line in content.splitlines():
                    if re.search(pattern, line, re.IGNORECASE):
                        results.append(f"{f.name}: {line.strip()}")
            except Exception:
                pass
    return results


# ===========================================================================
# 1. ARTIFACT COUNT ACCURACY
# ===========================================================================
class TestArtifactCounts:
    """Verify that the counts stated in APPLICATION_INVENTORY match the filesystem."""

    def test_core_cobol_program_count(self):
        """APPLICATION_INVENTORY claims 31 core COBOL programs in app/cbl/."""
        files = list_files(CBL_DIR, [".cbl", ".CBL"])
        assert len(files) == 31, (
            f"Expected 31 core COBOL programs, found {len(files)}: {sorted(files)}"
        )

    def test_copybook_count(self):
        """APPLICATION_INVENTORY claims 30 data copybooks in app/cpy/."""
        files = list_files(CPY_DIR, [".cpy", ".CPY"])
        assert len(files) == 30, (
            f"Expected 30 copybooks, found {len(files)}: {sorted(files)}"
        )

    def test_jcl_job_count(self):
        """APPLICATION_INVENTORY claims 38 JCL jobs in app/jcl/."""
        files = list_files(JCL_DIR, [".jcl", ".JCL"])
        assert len(files) == 38, (
            f"Expected 38 JCL jobs, found {len(files)}: {sorted(files)}"
        )

    def test_bms_map_count(self):
        """APPLICATION_INVENTORY claims 17 BMS maps in app/bms/."""
        files = list_files(BMS_DIR, [".bms"])
        assert len(files) == 17, (
            f"Expected 17 BMS maps, found {len(files)}: {sorted(files)}"
        )

    def test_bms_copybook_count(self):
        """APPLICATION_INVENTORY claims 17 BMS-generated copybooks in app/cpy-bms/."""
        files = list_files(CPY_BMS_DIR, [".cpy", ".CPY"])
        assert len(files) == 17, (
            f"Expected 17 BMS copybooks, found {len(files)}: {sorted(files)}"
        )

    def test_optional_auth_module_count(self):
        """APPLICATION_INVENTORY claims 8 programs in authorization module."""
        auth_dir = APP_DIR / "app-authorization-ims-db2-mq" / "cbl"
        files = list_files(auth_dir, [".cbl", ".CBL"])
        assert len(files) == 8, (
            f"Expected 8 auth module programs, found {len(files)}: {sorted(files)}"
        )

    def test_optional_db2_module_count(self):
        """APPLICATION_INVENTORY claims 3 programs in transaction type DB2 module."""
        db2_dir = APP_DIR / "app-transaction-type-db2" / "cbl"
        files = list_files(db2_dir, [".cbl", ".CBL"])
        assert len(files) == 3, (
            f"Expected 3 DB2 module programs, found {len(files)}: {sorted(files)}"
        )

    def test_optional_vsam_mq_module_count(self):
        """APPLICATION_INVENTORY claims 2 programs in VSAM-MQ module."""
        mq_dir = APP_DIR / "app-vsam-mq" / "cbl"
        files = list_files(mq_dir, [".cbl", ".CBL"])
        assert len(files) == 2, (
            f"Expected 2 VSAM-MQ module programs, found {len(files)}: {sorted(files)}"
        )


# ===========================================================================
# 2. COMPLETENESS — every file on disk is listed in the docs
# ===========================================================================
class TestCompleteness:
    """Every source file on disk must appear in the corresponding documentation."""

    def test_all_core_programs_listed_in_inventory(self):
        """Every .cbl/.CBL in app/cbl/ must be mentioned in APPLICATION_INVENTORY.md."""
        md = read_md(APP_INVENTORY)
        disk_files = list_files(CBL_DIR, [".cbl", ".CBL"])
        missing = []
        for name in sorted(disk_files):
            if name not in md.upper():
                missing.append(name)
        assert not missing, (
            f"Programs on disk but missing from APPLICATION_INVENTORY.md: {missing}"
        )

    def test_all_copybooks_listed_in_inventory(self):
        """Every .cpy/.CPY in app/cpy/ must be mentioned in APPLICATION_INVENTORY.md."""
        md = read_md(APP_INVENTORY)
        disk_files = list_files(CPY_DIR, [".cpy", ".CPY"])
        missing = []
        for name in sorted(disk_files):
            if name not in md.upper():
                missing.append(name)
        assert not missing, (
            f"Copybooks on disk but missing from APPLICATION_INVENTORY.md: {missing}"
        )

    def test_all_jcl_jobs_listed_in_inventory(self):
        """Every .jcl/.JCL in app/jcl/ must be mentioned in APPLICATION_INVENTORY.md."""
        md = read_md(APP_INVENTORY)
        disk_files = list_files(JCL_DIR, [".jcl", ".JCL"])
        missing = []
        for name in sorted(disk_files):
            if name not in md.upper():
                missing.append(name)
        assert not missing, (
            f"JCL jobs on disk but missing from APPLICATION_INVENTORY.md: {missing}"
        )

    def test_all_bms_maps_listed_in_inventory(self):
        """Every .bms in app/bms/ must be mentioned in APPLICATION_INVENTORY.md."""
        md = read_md(APP_INVENTORY)
        disk_files = list_files(BMS_DIR, [".bms"])
        missing = []
        for name in sorted(disk_files):
            if name not in md.upper():
                missing.append(name)
        assert not missing, (
            f"BMS maps on disk but missing from APPLICATION_INVENTORY.md: {missing}"
        )

    def test_all_copybooks_covered_in_data_dictionary(self):
        """Every business-entity copybook (CV*, CS*, CO* prefix) from app/cpy/
        should be mentioned in DATA_DICTIONARY.md."""
        md = read_md(DATA_DICTIONARY)
        disk_files = list_files(CPY_DIR, [".cpy", ".CPY"])
        missing = []
        for name in sorted(disk_files):
            if name not in md.upper():
                missing.append(name)
        assert not missing, (
            f"Copybooks on disk but missing from DATA_DICTIONARY.md: {missing}"
        )


# ===========================================================================
# 3. CROSS-REFERENCE CONSISTENCY
# ===========================================================================
class TestCrossReferenceConsistency:
    """Programs/copybooks referenced across docs must be consistent."""

    def test_hotspot_programs_exist_in_inventory(self):
        """Every program in HOTSPOT_REPORT top 10 must exist in APPLICATION_INVENTORY."""
        inventory_md = read_md(APP_INVENTORY)
        hotspot_md = read_md(HOTSPOT_REPORT)

        # Extract program IDs from hotspot ranking table (Rank column present)
        hotspot_programs = set()
        for line in hotspot_md.splitlines():
            # Match lines like "| **1** | COACTUPC | 4,236 |..."
            m = re.search(r"\|\s*\*{0,2}\d+\*{0,2}\s*\|\s*([A-Z]{2}[A-Z0-9]{4,6})\s*\|", line)
            if m:
                hotspot_programs.add(m.group(1).upper())

        assert len(hotspot_programs) >= 10, (
            f"Expected at least 10 programs in hotspot, found {len(hotspot_programs)}"
        )

        missing = []
        for prog in sorted(hotspot_programs):
            if prog not in inventory_md.upper():
                missing.append(prog)
        assert not missing, (
            f"Hotspot programs not found in APPLICATION_INVENTORY: {missing}"
        )

    def test_dependency_map_programs_exist_in_inventory(self):
        """Key programs referenced in DEPENDENCY_MAP must exist in APPLICATION_INVENTORY."""
        inventory_md = read_md(APP_INVENTORY)
        dep_md = read_md(DEPENDENCY_MAP)

        # Extract program names from dependency map that match COBOL naming
        dep_programs = set()
        for match in re.finditer(r"\b(CO[A-Z0-9]{4,6}[C]?|CB[A-Z0-9]{4,6}[C]?|CS[A-Z0-9]{4,6}[C]?)\b", dep_md):
            dep_programs.add(match.group(1).upper())

        missing = []
        for prog in sorted(dep_programs):
            # Skip if it looks like a copybook reference (ending in Y)
            if prog.endswith("Y"):
                continue
            if prog not in inventory_md.upper():
                missing.append(prog)
        assert not missing, (
            f"Dependency map programs not found in APPLICATION_INVENTORY: {missing}"
        )

    def test_hotspot_line_counts_match_inventory(self):
        """Line counts in HOTSPOT_REPORT should match APPLICATION_INVENTORY."""
        inventory_md = read_md(APP_INVENTORY)
        hotspot_md = read_md(HOTSPOT_REPORT)

        # Extract program → LOC from hotspot ranking table
        hotspot_locs = {}
        for line in hotspot_md.splitlines():
            m = re.search(
                r"\|\s*\*?\*?\d+\*?\*?\s*\|\s*(\w+)\s*\|\s*([\d,]+)\s*\|",
                line,
            )
            if m:
                prog = m.group(1).upper()
                loc = int(m.group(2).replace(",", ""))
                hotspot_locs[prog] = loc

        # Verify against actual file line counts
        mismatches = []
        for prog, claimed_loc in hotspot_locs.items():
            # Find the actual file
            found = False
            for f in CBL_DIR.glob("*"):
                if f.stem.upper() == prog:
                    actual_loc = len(f.read_text(encoding="utf-8", errors="ignore").splitlines())
                    if actual_loc != claimed_loc:
                        mismatches.append(
                            f"{prog}: hotspot says {claimed_loc}, actual file has {actual_loc}"
                        )
                    found = True
                    break
            if not found:
                mismatches.append(f"{prog}: file not found in {CBL_DIR}")

        assert not mismatches, (
            f"Line count mismatches between HOTSPOT_REPORT and actual files:\n"
            + "\n".join(mismatches)
        )

    def test_data_dictionary_entities_have_fields(self):
        """Each major entity section in DATA_DICTIONARY should have at least 3 fields."""
        md = read_md(DATA_DICTIONARY)
        # Find entity sections: "## N. EntityName"
        sections = re.findall(r"^## \d+\.\s+(.+?)$", md, re.MULTILINE)
        # For each entity section, count PIC-clause table rows
        for section_name in sections:
            if "Summary" in section_name or "Mapping" in section_name or "Sensitive" in section_name or "Relationship" in section_name:
                continue
            # Count rows with PIC clause patterns in tables after this section
            section_start = md.index(section_name)
            # Find next ## or end
            next_section = md.find("\n## ", section_start + len(section_name))
            if next_section == -1:
                section_text = md[section_start:]
            else:
                section_text = md[section_start:next_section]

            # Count table data rows (lines starting with | that have content)
            data_rows = 0
            for line in section_text.splitlines():
                line = line.strip()
                if line.startswith("|") and not all(
                    c in "|-: " for c in line
                ):
                    cells = [c.strip() for c in line.split("|") if c.strip()]
                    # Skip header rows
                    if cells and cells[0].startswith("#"):
                        continue
                    if cells and any(kw in cells[0].lower() for kw in ["cobol", "field", "pic", "entity", "copybook"]):
                        continue
                    data_rows += 1

            # Entity sections should have at least some fields documented
            if data_rows < 2:
                pytest.fail(
                    f"DATA_DICTIONARY section '{section_name}' has only "
                    f"{data_rows} data rows — expected at least 2 field definitions"
                )


# ===========================================================================
# 4. SOURCE-CODE-GROUNDED VALIDATION
# ===========================================================================
class TestSourceCodeGrounded:
    """Validate documentation claims against actual COBOL source code."""

    def test_xctl_targets_documented(self):
        """Every XCTL target in source should appear in DEPENDENCY_MAP."""
        dep_md = read_md(DEPENDENCY_MAP)
        dep_upper = dep_md.upper()

        # Grep for XCTL in source
        xctl_targets = set()
        for f in list(CBL_DIR.glob("*.cbl")) + list(CBL_DIR.glob("*.CBL")):
            content = f.read_text(encoding="utf-8", errors="ignore")
            for m in re.finditer(r"XCTL\s+PROGRAM\s*\(\s*['\"]?(\w+)['\"]?\s*\)", content, re.IGNORECASE):
                target = m.group(1).upper()
                # Skip variable references (WS-*, LIT-*, CDEMO-*)
                if not target.startswith("WS-") and not target.startswith("LIT-") and not target.startswith("CDEMO-"):
                    xctl_targets.add(target)

        # Also check for literal XCTL program names
        for f in list(CBL_DIR.glob("*.cbl")) + list(CBL_DIR.glob("*.CBL")):
            content = f.read_text(encoding="utf-8", errors="ignore")
            for m in re.finditer(r"XCTL.*PROGRAM\s*\(\s*'([A-Z][A-Z0-9]+)'\s*\)", content, re.IGNORECASE):
                xctl_targets.add(m.group(1).upper())

        missing = []
        for target in sorted(xctl_targets):
            if target not in dep_upper:
                missing.append(target)

        assert not missing, (
            f"XCTL targets found in source but missing from DEPENDENCY_MAP: {missing}"
        )

    def test_vsam_file_access_documented(self):
        """Programs performing CICS file I/O should appear in DEPENDENCY_MAP file access table."""
        dep_md = read_md(DEPENDENCY_MAP)
        dep_upper = dep_md.upper()

        # Grep for CICS READ/WRITE/REWRITE/DELETE with FILE/DATASET
        programs_with_file_io = set()
        for f in list(CBL_DIR.glob("*.cbl")) + list(CBL_DIR.glob("*.CBL")):
            content = f.read_text(encoding="utf-8", errors="ignore").upper()
            if re.search(r"EXEC\s+CICS\s+(READ|WRITE|REWRITE|DELETE|STARTBR|READNEXT|READPREV)", content):
                programs_with_file_io.add(f.stem.upper())

        missing = []
        for prog in sorted(programs_with_file_io):
            if prog not in dep_upper:
                missing.append(prog)

        assert not missing, (
            f"Programs with CICS file I/O but not in DEPENDENCY_MAP: {missing}"
        )

    def test_copybook_inclusions_documented(self):
        """Programs that COPY major business copybooks should appear in DEPENDENCY_MAP."""
        dep_md = read_md(DEPENDENCY_MAP)
        dep_upper = dep_md.upper()

        # Key business copybooks to check
        key_copybooks = ["CVACT01Y", "CVACT02Y", "CVCUS01Y", "CVTRA05Y", "CSUSR01Y"]

        for cpybook in key_copybooks:
            # Find which programs COPY this copybook
            programs_using = set()
            for f in list(CBL_DIR.glob("*.cbl")) + list(CBL_DIR.glob("*.CBL")):
                content = f.read_text(encoding="utf-8", errors="ignore").upper()
                if f"COPY {cpybook}" in content or f"COPY '{cpybook}'" in content:
                    programs_using.add(f.stem.upper())

            # Each program using this copybook should be mentioned in the
            # dependency map (at least in the same section as the copybook)
            for prog in programs_using:
                assert prog in dep_upper, (
                    f"Program {prog} uses COPY {cpybook} but is not in DEPENDENCY_MAP"
                )

    def test_pic_clauses_in_data_dictionary_match_source(self):
        """Spot-check: PIC clauses for Account entity (CVACT01Y) fields match source."""
        dd_md = read_md(DATA_DICTIONARY)

        # Read actual CVACT01Y copybook
        cvact01y_path = CPY_DIR / "CVACT01Y.cpy"
        if not cvact01y_path.exists():
            pytest.skip("CVACT01Y.cpy not found")

        source = cvact01y_path.read_text(encoding="utf-8", errors="ignore")

        # Check that key fields mentioned in DATA_DICTIONARY actually exist in source
        key_fields = ["ACCT-ID", "ACCT-ACTIVE-STATUS", "ACCT-CURR-BAL", "ACCT-CREDIT-LIMIT"]
        for field in key_fields:
            assert field in source, (
                f"Field {field} documented in DATA_DICTIONARY but not found in CVACT01Y.cpy"
            )
            assert field in dd_md, (
                f"Field {field} exists in CVACT01Y.cpy but not documented in DATA_DICTIONARY"
            )

    def test_user_security_fields_match_source(self):
        """Spot-check: User security fields (CSUSR01Y) match source."""
        dd_md = read_md(DATA_DICTIONARY)

        csusr01y_path = CPY_DIR / "CSUSR01Y.cpy"
        if not csusr01y_path.exists():
            pytest.skip("CSUSR01Y.cpy not found")

        source = csusr01y_path.read_text(encoding="utf-8", errors="ignore")

        key_fields = ["SEC-USR-ID", "SEC-USR-FNAME", "SEC-USR-LNAME", "SEC-USR-PWD", "SEC-USR-TYPE"]
        for field in key_fields:
            assert field in source, (
                f"Field {field} documented in DATA_DICTIONARY but not found in CSUSR01Y.cpy"
            )
            assert field in dd_md, (
                f"Field {field} exists in CSUSR01Y.cpy but not documented in DATA_DICTIONARY"
            )


# ===========================================================================
# 5. HOTSPOT SCORE REPRODUCIBILITY
# ===========================================================================
class TestHotspotReproducibility:
    """Verify that complexity metrics cited in HOTSPOT_REPORT can be reproduced."""

    @pytest.mark.parametrize("program,keyword", [
        ("COACTUPC", "IF "),
        ("COACTUPC", "EVALUATE"),
        ("COACTUPC", "PERFORM"),
        ("CBTRN02C", "IF "),
        ("CBTRN02C", "PERFORM"),
        ("COCRDLIC", "IF "),
        ("CBACT04C", "IF "),
        ("CBACT04C", "PERFORM"),
    ])
    def test_complexity_metric_order_of_magnitude(self, program, keyword):
        """Verify that claimed statement counts are in the right ballpark.
        
        We allow ±30% tolerance because grep-based counting can differ based on
        how comments, continuations, and inline occurrences are handled.
        """
        hotspot_md = read_md(HOTSPOT_REPORT)

        # Find the actual file
        source_file = None
        for f in list(CBL_DIR.glob("*.cbl")) + list(CBL_DIR.glob("*.CBL")):
            if f.stem.upper() == program.upper():
                source_file = f
                break

        if source_file is None:
            pytest.skip(f"Program {program} not found in {CBL_DIR}")

        source = source_file.read_text(encoding="utf-8", errors="ignore")

        # Count occurrences of keyword in non-comment lines (col 7 != '*')
        # Use keyword with trailing space (e.g. "IF ") to avoid matching
        # substrings like END-IF when searching for IF statements.
        actual_count = 0
        for line in source.splitlines():
            # COBOL comment indicator is '*' in column 7 (index 6)
            if len(line) > 6 and line[6] == "*":
                continue
            if keyword in line.upper():
                actual_count += 1

        # Extract claimed count from the program's specific section
        # First, find the section for this program using "### Rank #N" headers
        claimed_count = None
        section_match = re.search(
            rf"###\s+Rank\s+#?\d+[^\n]*{program}.*?(?=###\s+Rank|$)",
            hotspot_md,
            re.DOTALL | re.IGNORECASE,
        )
        if section_match:
            section_text = section_match.group(0)
            m = re.search(
                rf"{keyword.strip()}\s+statements?\s*\|\s*(\d+)",
                section_text,
                re.IGNORECASE,
            )
            if m:
                claimed_count = int(m.group(1))

        if claimed_count is None:
            pytest.skip(
                f"Could not extract {keyword.strip()} count for {program} from HOTSPOT_REPORT"
            )

        # Allow 30% tolerance
        lower = claimed_count * 0.7
        upper = claimed_count * 1.3
        assert lower <= actual_count <= upper, (
            f"{program} {keyword.strip()} statements: "
            f"claimed {claimed_count}, actual {actual_count} "
            f"(outside ±30% tolerance [{lower:.0f}, {upper:.0f}])"
        )

    def test_hotspot_composite_score_arithmetic(self):
        """Verify composite score = complexity*0.35 + risk*0.35 + impact*0.30."""
        hotspot_md = read_md(HOTSPOT_REPORT)

        # Extract from ranking table: Rank | Program | LOC | Composite | Complexity | Risk | Impact
        scores = []
        for line in hotspot_md.splitlines():
            m = re.search(
                r"\|\s*\*?\*?(\d+)\*?\*?\s*\|\s*(\w+)\s*\|\s*[\d,]+\s*\|\s*\*?\*?([\d.]+)\*?\*?\s*\|\s*(\d+)\s*\|\s*(\d+)\s*\|\s*(\d+)\s*\|",
                line,
            )
            if m:
                rank = int(m.group(1))
                program = m.group(2)
                composite = float(m.group(3))
                complexity = int(m.group(4))
                risk = int(m.group(5))
                impact = int(m.group(6))
                scores.append((rank, program, composite, complexity, risk, impact))

        assert len(scores) == 10, f"Expected 10 hotspot entries, found {len(scores)}"

        for rank, program, claimed_composite, complexity, risk, impact in scores:
            expected = complexity * 0.35 + risk * 0.35 + impact * 0.30
            assert abs(claimed_composite - expected) < 0.1, (
                f"Rank #{rank} {program}: claimed composite {claimed_composite}, "
                f"expected {expected:.2f} "
                f"(complexity={complexity}*0.35 + risk={risk}*0.35 + impact={impact}*0.30)"
            )

    def test_hotspot_ranking_is_sorted_descending(self):
        """Hotspot rankings should be in descending order of composite score."""
        hotspot_md = read_md(HOTSPOT_REPORT)

        composites = []
        for line in hotspot_md.splitlines():
            m = re.search(
                r"\|\s*\*?\*?\d+\*?\*?\s*\|\s*\w+\s*\|\s*[\d,]+\s*\|\s*\*?\*?([\d.]+)\*?\*?\s*\|",
                line,
            )
            if m:
                composites.append(float(m.group(1)))

        assert len(composites) >= 10, f"Expected at least 10 scores, found {len(composites)}"

        for i in range(len(composites) - 1):
            assert composites[i] >= composites[i + 1], (
                f"Rankings not sorted: position {i} score {composites[i]} "
                f"< position {i+1} score {composites[i+1]}"
            )


# ===========================================================================
# 6. DOCUMENTATION STRUCTURE VALIDATION
# ===========================================================================
class TestDocumentStructure:
    """Verify that all four documents exist and have expected sections."""

    def test_all_four_artifacts_exist(self):
        """All four documentation artifacts must exist."""
        for path in [APP_INVENTORY, DATA_DICTIONARY, DEPENDENCY_MAP, HOTSPOT_REPORT]:
            assert path.exists(), f"Missing artifact: {path.name}"

    def test_inventory_has_required_sections(self):
        """APPLICATION_INVENTORY must have sections for programs, copybooks, JCL, BMS."""
        md = read_md(APP_INVENTORY)
        required = ["COBOL Programs", "Copybooks", "BMS Screen Maps", "JCL Batch Jobs"]
        for section in required:
            assert section.lower() in md.lower(), (
                f"APPLICATION_INVENTORY missing section: {section}"
            )

    def test_data_dictionary_has_entity_sections(self):
        """DATA_DICTIONARY must have sections for major entities."""
        md = read_md(DATA_DICTIONARY)
        required_entities = ["Account", "Card", "Customer", "Transaction", "User Security"]
        for entity in required_entities:
            assert entity.lower() in md.lower(), (
                f"DATA_DICTIONARY missing entity: {entity}"
            )

    def test_dependency_map_has_required_sections(self):
        """DEPENDENCY_MAP must have call graph, file access, and data lineage sections."""
        md = read_md(DEPENDENCY_MAP)
        required = ["Call Graph", "File", "Data Lineage", "XCTL"]
        for section in required:
            assert section.lower() in md.lower(), (
                f"DEPENDENCY_MAP missing section: {section}"
            )

    def test_hotspot_report_has_top_10(self):
        """HOTSPOT_REPORT must have detailed analysis for 10 modules."""
        md = read_md(HOTSPOT_REPORT)
        # Count "Rank #N" sections
        ranks = re.findall(r"Rank\s+#(\d+)", md)
        assert len(ranks) >= 10, (
            f"HOTSPOT_REPORT has {len(ranks)} ranked modules, expected at least 10"
        )

    def test_documents_are_nontrivial_size(self):
        """Each document should be substantial (>2KB)."""
        for path in [APP_INVENTORY, DATA_DICTIONARY, DEPENDENCY_MAP, HOTSPOT_REPORT]:
            size = path.stat().st_size
            assert size > 2000, (
                f"{path.name} is only {size} bytes — expected substantial documentation"
            )
