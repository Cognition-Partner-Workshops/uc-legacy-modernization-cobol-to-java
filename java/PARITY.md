# Parity report: Java 21 port of CBACT01C / CBACT04C / CBTRN02C

Plain Java 21 + Maven (no Spring, no COBOL runtime emulation library). One CLI per
program, `java/scripts/run-parity.sh HARNESS_DIR OUT_DIR` reproduces the four
golden scenarios and runs the harness' own `compare.py`.

## Final `compare.py` report (verbatim)

```
Parity report: /home/ubuntu/parity-out vs /home/ubuntu/harness_root/harness/golden

FILE                                     STATUS     GOLDEN     CAND   DIFFS
CBACT01C/ARRYFILE.txt                    MATCH          50       50       0
CBACT01C/OUTFILE.txt                     MATCH          50       50       0
CBACT01C/VBRCFILE.txt                    MATCH         100      100       0
CBACT01C/return-code.txt                 MATCH           1        1       0
CBACT01C/stdout.txt                      MATCH         752      752       0
CBACT04C-after-CBTRN02C/ACCTFILE.txt     MATCH          50       50       0
CBACT04C-after-CBTRN02C/TRANSACT.txt     MATCH          50       50       0
CBACT04C-after-CBTRN02C/return-code.txt  MATCH           1        1       0
CBACT04C-after-CBTRN02C/stdout.txt       MATCH         302      302       0
CBACT04C/ACCTFILE.txt                    MATCH          50       50       0
CBACT04C/TRANSACT.txt                    MATCH          50       50       0
CBACT04C/return-code.txt                 MATCH           1        1       0
CBACT04C/stdout.txt                      MATCH         152      152       0
CBTRN02C/ACCTFILE.txt                    MATCH          50       50       0
CBTRN02C/DALYREJS.txt                    MATCH          38       38       0
CBTRN02C/TCATBALF.txt                    MATCH          100      100       0
CBTRN02C/TRANFILE.txt                    MATCH          262      262       0
CBTRN02C/return-code.txt                 MATCH           1        1       0
CBTRN02C/stdout.txt                      MATCH           54       54       0

SUMMARY: 19/19 files match
```

## `./mvnw -q verify`

Exit code 0, no output (quiet mode). Surefire: 21 tests, 0 failures, 0 errors
(`NumericEncodingTest` 5, `Cbact01cTest` 4, `Cbact04cTest` 5, `Cbtrn02cTest` 7).

Environment notes: JDK 21 (Temurin 21.0.7), Maven Wrapper 3.3.2 / Maven 3.9.9.
In the build sandbox `repo.maven.apache.org` answered HTTP 403, so the wrapper
distribution and dependencies were fetched through
`https://maven-central.storage-download.googleapis.com/maven2/` (`~/.m2/settings.xml`
mirror + `MVNW_REPOURL`). Nothing in the repo depends on that mirror.

## Running

```
cd java
./mvnw -q -DskipTests package
java -jar target/carddemo-batch.jar CBACT01C --dd ACCTFILE=... --dd OUTFILE=... --dd ARRYFILE=... --dd VBRCFILE=...
java -jar target/carddemo-batch.jar CBACT04C --parm 2022071800 --dd TCATBALF=... --dd XREFFILE=... --dd DISCGRP=... --dd ACCTFILE=... --dd TRANSACT=...
java -jar target/carddemo-batch.jar CBTRN02C --dd DALYTRAN=... --dd XREFFILE=... --dd ACCTFILE=... --dd TCATBALF=... --dd TRANFILE=... --dd DALYREJS=...
scripts/run-parity.sh ~/harness ~/parity-out
```

`--now` overrides the frozen timestamp (default `2022-07-18-00.00.00.000000`, the
harness' libfaketime value). The process exit status is the COBOL `RETURN-CODE`
(CBTRN02C returns 4 when any transaction was rejected). Indexed (VSAM KSDS) datasets
are fixed-record files: loaded into a sorted in-memory map keyed by the primary key
(unsigned byte order) and written back in key order when the program opened them I-O.
`XREFFILE`'s alternate index on account id is an in-memory secondary map.

## Design

* `carddemo.cobol` – byte-level toolkit: `ZonedDecimal` (DISPLAY numerics with
  trailing over-punch `{ A-I` / `} J-R`), `PackedDecimal` (COMP-3, `C`/`D`/`F`
  sign nibble), `RecordArea` (record buffer with typed field descriptors and COBOL
  store-truncation on MOVE/COMPUTE), `FixedFile` (fixed records and the GnuCOBOL
  `COB_VARSEQ_FORMAT=0` variable record framing), `KeyedFile` (KSDS semantics).
* `carddemo.records` – copybook layouts CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y,
  CVTRA05Y/CVTRA06Y.
* `carddemo.programs` – one class per COBOL program, structured after the COBOL
  paragraphs; pure static helpers hold the business rules and are what the unit tests
  exercise.

## What had to be inferred / guessed, and how it was settled

All of the following were confirmed against the golden outputs (each caused a
mismatch on the first parity run or was checked deliberately):

1. **`INITIALIZE` vs `MOVE 0` on signed DISPLAY fields.** GnuCOBOL's `INITIALIZE`
   fills a `PIC S9(10)V99` item with ASCII `0`s and *no* sign over-punch
   (`000000000000`), whereas `MOVE 0`/`COMPUTE` stores `00000000000{`. Visible in
   `ARRYFILE` slots 4-5 (initialized, never assigned) vs. `ACCTFILE` cycle fields
   after CBACT04C (`MOVE 0`). `RecordArea.initialize(Num)` reproduces this.
2. **CBACT04C never updates the last account.** The main loop is
   `PERFORM UNTIL END-OF-FILE = 'Y' ... IF END-OF-FILE = 'N' ... ELSE PERFORM
   1050-UPDATE-ACCOUNT`; the ELSE is unreachable because the loop exits as soon as
   END-OF-FILE becomes `'Y'`. Interest transactions for the last account are still
   written, but its balance/cycle counters are not. On the pristine fixtures this is
   invisible (account 50 has 0.00 interest and zero cycle counters); in the chained
   scenario it is not (golden `CBACT04C-after-CBTRN02C/ACCTFILE.txt` record 50 keeps
   `1945.87 / 1501.75 / -47.88`). The port reproduces the defect; a "fixed" port
   would fail parity on that file.
3. **CBACT01C `OUT-ACCT-CURR-CYC-DEBIT` is only assigned when the account debit is
   zero** (`IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO MOVE 2525.00 ...`, no ELSE). With a
   non-zero debit the previous record's value stays in the output area. All 50
   fixture accounts have a zero debit so the golden file only shows `2525.00`; the
   port keeps the exact COBOL semantics (sticky output field).
4. **CBTRN02C validation order / precedence.** Card XREF lookup (100) short-circuits;
   otherwise account lookup (101); otherwise *both* the credit-limit check (102) and
   the expiration check (103) run in sequence, so 103 overwrites 102. The over-limit
   test is `CREDIT-LIMIT >= CYC-CREDIT - CYC-DEBIT + AMT` (note: not against
   `CURR-BAL`), and the expiration test compares `ACCT-EXPIRAION-DATE` with the first
   10 bytes of the origin timestamp as text.
5. **CBTRN02C posting adds negative amounts to `CURR-CYC-DEBIT` and non-negative to
   `CURR-CYC-CREDIT`** (`IF DALYTRAN-AMT >= 0`) – the debit counter therefore goes
   negative (`-47.88`, over-punched `Q`).
6. **Interest arithmetic**: `(TRAN-CAT-BAL * DIS-INT-RATE) / 1200` stored without
   `ROUNDED` into `S9(09)V99` → truncation toward zero to two decimals; per-account
   total is accumulated in the same precision.
7. **Missing disclosure group** → two DISPLAY lines and a retry with group `DEFAULT`
   (same type/category); an account whose XREF/ACCT lookups fail would print
   `ACCOUNT NOT FOUND`/`INVALID KEY` diagnostics, none occur in the fixtures.
8. **DISPLAY formatting**: signed DISPLAY numerics print as digits followed by `+`/`-`
   (`000000019400+`), unsigned as plain digits; `DISPLAY` of a group item dumps raw
   bytes. Reproduced via `ZonedDecimal.displayText` and `JobContext.display(byte[]...)`.
9. **Variable-length VBRCFILE**: GnuCOBOL `COB_VARSEQ_FORMAT=0` framing (2-byte
   big-endian length + 2 NUL bytes) per README; the harness strips it on normalize.
10. **Key order**: TRANFILE is written through the KSDS map so it comes out in
    TRAN-ID order regardless of daily-transaction order; ACCTFILE/TCATBALF are
    re-emitted in primary-key order after updates. A duplicate TRAN-ID would abend
    (status 22) exactly as COBOL would; none occur.

Things that were *not* needed: the DISPLAYs of `ACCT-CREDIT-LIMIT`/`TRAN-AMT` in
`1500-B-LOOKUP-ACCT` are commented out in the source (column 7 `*`) and are absent
from the golden stdout; the "fee" routine in CBACT04C is empty.

## Harness rules not satisfied

None. All 19 manifest files match, `compare.py` and `harness/golden` were not
modified. The parity script chains the fourth scenario from *this port's* CBTRN02C
outputs (as `run.sh` chains from the COBOL's own), so a CBTRN02C regression would
surface in both scenarios.

Harness limitations noticed (not defects): the fixtures never exercise the
`ACCOUNT NOT FOUND` / duplicate TRAN-ID / non-zero `ACCT-CURR-CYC-DEBIT` paths, so
those are covered by unit tests and source reading only.

## AWS Transform inputs: what helped / what didn't

Artifacts opened: `README.md`;
`analyze_data/data_lineage_output/program_to_dsn.csv`, `jcl_to_dsn.csv`,
`dsn_to_file.csv`;
`analyze_data/data_dictionary_output/app/cpy/CVACT01Y.cpy.csv`, `CVACT03Y.cpy.csv`,
`CVTRA01Y.cpy.csv`, `CVTRA02Y.cpy.csv`, `CVTRA05Y.cpy.csv`, `CVTRA06Y.cpy.csv`;
`analyze_data/data_dictionary_output/app/cbl/CBACT01C.cbl.csv`, `CBACT04C.cbl.csv`,
`CBTRN02C.cbl.csv`;
`analyze_code/code_issues_20260924205348.csv`, `missing_20260924205348.csv`,
`assets_20260924205348.csv`, `classification_20260924205348.json`,
`dependencies_20260924205348.json`.

**Helped**

* `program_to_dsn.csv` gave in one table, per program, the DD name → SELECT name →
  01 record → copybook path → dataset type (VSAM KSDS / Non VSAM / GDG) → access mode
  (READ / UPDATE / WRITE). That fixed the DD-name surface of the CLI and told me up
  front which files are opened I-O and must be re-emitted (ACCTFILE, TCATBALF in
  CBTRN02C; ACCTFILE in CBACT04C) versus read-only, without tracing every
  `OPEN` statement. It also flagged `XREFFIL1` (the AIX path) for CBACT04C, which is
  why the alternate-key lookup exists.
* The copybook dictionaries (`CV*.cpy.csv`) list `field_position`, `data_length`,
  PIC, `decimal_positions`, `signed_indicator` and `usage_clause` per field. The
  record classes were written straight from those columns and checked once against
  the copybooks; this saved manual offset arithmetic for the 300/350/50-byte
  layouts. The program dictionaries did the same for the WORKING-STORAGE output
  records of CBACT01C (`OUT-ACCT-REC` 107, `ARR-ARRAY-REC` 110, COMP-3 positions
  91 and inside the OCCURS group) and confirmed `WS-MONTHLY-INT` / `WS-TEMP-BAL`
  are `S9(09)V99`, which is where the truncation rule comes from.
* `jcl_to_dsn.csv` confirmed READACCT/INTCALC/POSTTRAN step ↔ DD ↔ dataset and the
  `NEW,CATLG,DELETE` outputs (TRANSACT, DALYREJS), i.e. which files must be created
  empty rather than loaded.

**Didn't help / incomplete / irrelevant**

* Nothing in the output describes *behaviour*: the three non-obvious rules that
  actually cost parity iterations (unreachable ELSE in CBACT04C, `INITIALIZE` vs
  `MOVE 0` sign representation, the sticky `OUT-ACCT-CURR-CYC-DEBIT`) are only
  discoverable from the COBOL source plus the golden files. Validation order,
  reject-reason codes, the `>= 0` credit/debit split and the DEFAULT-group
  fallback likewise had to come from the source.
* `code_issues_*.csv` (93 rows) and `missing_*.csv` are almost entirely "missing
  control card" / "missing source file" for dataset assignment names (`ACCTFILE`,
  `DALYTRAN`, …) — noise for this task; no row points at a logic issue in the
  three programs. `codebase_issues_*.json`, `duplicatedIds_*.json` and the
  `generic-analysis-*.json` files were skimmed and not used.
* `dsn_to_file.csv`/`assets`/`classification`/`dependencies` confirm the obvious
  (which programs are batch vs CICS, copybook includes) and added nothing beyond
  the harness README.
* Dictionary `field_position` is 1-based (Java offsets are 0-based) and the
  `business_definition` text is auto-generated and occasionally misleading (e.g.
  `OUT-ACCT-CURR-CYC-CREDIT` is described as "USAGE COMP" although it is DISPLAY;
  `usage_clause` is correct). Field positions of the COMP-3 items are correct.
* Copybook dictionaries do not model the GnuCOBOL runtime aspects that matter for
  byte parity: over-punch sign encoding, `INITIALIZE` semantics, variable-record
  framing, `DISPLAY` formatting. Those came from `harness/README.md`, the golden
  files and GnuCOBOL behaviour knowledge.

Net: the lineage + data dictionaries replaced roughly the first pass of copybook
reading and DD mapping; all business-rule and runtime-representation work was
source-and-golden driven.
