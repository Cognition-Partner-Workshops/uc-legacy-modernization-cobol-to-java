# CardDemo modernization backlog (COBOL/CICS/VSAM/JCL -> Java 21 / Spring Boot 3.5)

Jira-importable backlog derived from the AWS Transform mainframe analysis output
("Analyze code" + "Analyze data" runs over the CardDemo repository) plus a manual read of the
COBOL, JCL, BMS and CSD sources.

| File | Purpose |
|---|---|
| `carddemo-modernization-backlog.csv` | Jira CSV import (`Issue Type,Summary,Description,Priority,Labels,Epic Name,Parent,Story Points,Components`) |
| `carddemo-modernization-backlog.json` | Same items + `depends_on` (list of summaries) + `programs` (classified `.cbl` paths a story migrates) |
| `validate.py` | Structural checks: CSV/JSON well-formed and consistent, every Parent is an Epic, every `depends_on` resolves, no cycles, Fibonacci points, no orphan program |
| `generate.py` | Regenerates CSV/JSON from the analysis directory (`--analysis-dir aws-transform-output/`) |
| `programs.txt` | The 44 COBOL programs from the vendor classification, so `validate.py` runs without the analysis archive |

Current size: **13 epics, 73 stories, 12 tasks (85 stories+tasks), 44/44 programs covered.**

```
python3 backlog/validate.py                                   # uses programs.txt
python3 backlog/validate.py --analysis-dir aws-transform-output  # uses classification json
python3 backlog/generate.py --analysis-dir aws-transform-output  # regenerate
```

## How the backlog was derived

| Backlog field / content | Vendor artifact(s) | Notes |
|---|---|---|
| Program inventory (one story each, orphan check) | `analyze_code/classification_*.json` (`fileType == "cob"`, 44 programs incl. `app-*` sub-apps) | also 65 copybooks, 46 JCL, 21 BMS used for context |
| Copybooks, BMS maps, CICS transactions, CICS files (with EXEC CICS verbs), Db2 tables, called programs / callers, system & assembler calls | `analyze_code/dependencies_*.json` | `dependencyType` strings copied verbatim into descriptions (e.g. `Exec CICS Read dataset; Exec CICS Rewrite dataset`) |
| Metrics line, story points | `analyze_code/assets_*.csv` (Effective Lines, Cyclomatic Complexity) | Fibonacci from `eff/250 + CC/40` (+0.5 for CICS screens); thresholds 1/1.8/3.5/5/8/14 -> 1/2/3/5/8/13/21; four manual overrides (COACTUPC 21, CBTRN02C 8, CBACT04C 8, COPAUA0C 13) explained in the story notes |
| Datasets / DD names / access modes | `analyze_data/data_lineage_output/program_to_dsn.csv` | one line per DD -> DSN with `[type; access modes]` |
| "Executed by JCL", JCL step lists in data stories | `analyze_data/data_lineage_output/jcl_to_dsn.csv`, `dsn_to_file.csv` | consumers per dataset with access mode |
| Data-migration stories (one per VSAM cluster / Db2 table / IMS DB / sequential family) | `dsn_to_file.csv` + `analyze_data/data_dictionary_output/data_dictionary_cbl.csv`, `data_dictionary_cpy.csv`, `data_dictionary_ddl.csv` | data-item counts per copybook; DDL for Db2 tables/indexes |
| "Vendor code_issues" line | `analyze_code/code_issues_*.csv`, `missing_*.csv`, `codebase_issues_*.json` | missing PDS/control cards/programs, unresolved dynamic calls, unsupported utilities |
| `depends_on` | dependency graph (calls, XCTL/LINK, JCL EXEC) + lineage (dataset -> data-migration story) + platform tasks | navigation edges are ordered dispatcher-first (menu/list before option/detail); "return to menu" XCTLs are not dependencies |
| Epics, purpose text, "Notes from reading the COBOL", labels | manual: program header comments, `PROCEDURE DIVISION`, CSD files (`CARDDEMO.CSD`, `CRDDEMO2.csd`, `CRDDEMOM.csd`), Control-M/CA7 XML | the vendor run produced no decomposition or business rules |

Labels: `cobol`, `cics`, `batch`, `vsam`, `db2`, `ims`, `mq`, `gdg`, `jcl`, `asm`, `sort`, `data-migration`,
`security`, `pii`, `platform`, `testing`, `scheduler`, `cutover`, `utility`, `modernization`.
Components: Platform, Data, Online, Batch, Db2, IMS, Integration.

## Epics

1. Platform Foundation and Shared Utilities
2. Shared Data Foundation and VSAM/Db2/IMS Migration
3. Sign-on, Menus and Navigation
4. User Administration
5. Account and Customer Management
6. Card Management
7. Transaction Inquiry, Entry and Bill Payment
8. Transaction Posting and Interest Batch
9. Reporting and Statements
10. Transaction Type Reference Data (Db2 sub-application)
11. Card Pre-authorization (IMS DB / Db2 / MQ sub-application)
12. MQ Request/Reply Services (VSAM-MQ sub-application)
13. Parity Testing, Cutover and Decommission

## Migration waves (dependency-driven)

The order falls out of the `depends_on` graph: data stories have no program dependencies, the
cross-reference dataset is read by 12 programs, the menus dispatch to everything online, and the
sub-applications only consume core entities.

| Wave | Content | Rationale |
|---|---|---|
| 0 - Foundation | Epic 1 tasks (project, CICS abstractions, Spring Batch framework, assembler retirement), Epic 13 harness/fixture tasks, copybook mapping catalogue + EBCDIC conversion pipeline | every other item depends on at least one of these |
| 1 - Core reference & master data | Data stories TRANTYPE, TRANCATG, DISCGRP, USRSEC, ACCTDATA, CUSTDATA, CARDDATA, CARDXREF | leaf nodes of the lineage graph; CARDXREF is the most-read dataset (COACTVWC, COACTUPC, COCRDLIC/SLC/UPC, COTRN02C, COBIL00C, CBTRN02C, CBACT04C, CBSTM03B, CBEXPORT, CBACT03C) |
| 2 - Sign-on & admin | COSGN00C, COMEN01C, COADM01C, COUSR00C-03C, verification dump jobs | only need USRSEC; unlocks screen parity tooling early with the simplest CRUD screens |
| 3 - Online core | Account (COACTVWC, COACTUPC), Card (COCRDLIC, COCRDSLC, COCRDUPC), batch readers CBACT01C/02C/03C, CBCUS01C | read-mostly first (view/list), then updates; COACTUPC (CC 373) is the largest single item and carries most edit rules |
| 4 - Transactions | Data TRANSACT, TCATBALF, DALYTRAN; COTRN00C/01C/02C, COBIL00C, CSUTLDTC, CBTRN02C + POSTTRAN, CBTRN01C, TRANBKP/COMBTRAN | transaction entry/posting need cards+accounts+reference data; bill pay and posting both rewrite ACCTDAT so they land after the account entity is stable |
| 5 - Interest, reports, statements | CBACT04C + INTCALC, CBTRN03C + TRANREPT, CORPT00C, CBSTM03A/B + CREASTMT, TRXFL, TRANREPT/SYSTRAN/STATEMNT outputs, PRTCATBL | pure consumers of posted transactions and category balances; output-only so parity is a file diff |
| 6 - Db2 sub-application | Data DB2TRTYP, COTRTLIC, COTRTUPC, COBTUPDT, MNTTRDB2/TRANEXTR/CREADB21 | duplicates VSAM TRANTYPE/TRANCATG; decide the single system of record, then retire the VSAM copy |
| 7 - IMS/MQ pre-authorization & MQ services | MQ provisioning task, Data IMSPAUTDB, DB2AUTHFRDS, COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C, PAUDBUNL/LOD, DBUNLDGS, IMS jobs; COACCT01, CODATE01 | needs account/card/customer entities and the MQ layer; the hierarchical->relational remodel of PAUTDB is the riskiest data item so it is isolated |
| 8 - Export/import bridge, scheduler, cutover | CBEXPORT/CBIMPORT + jobs, Control-M/CA7 migration, dual-run/cutover/decommission | bridge is used during dual-run; cutover is last by definition |

## What the vendor analysis got wrong, missed, or left to inference

1. **No decomposition / business rules** - the run produced only graphs, metrics and dictionaries;
   all epic boundaries, program purposes and rule notes were derived from the COBOL headers,
   `COMEN02Y`/`COADM02Y` menu tables and the CSD files.
2. **Unresolved dynamic calls that are actually static tables** - `CDEMO-MENU-OPT-PGMNAME`
   (COMEN01C) and `CDEMO-ADMIN-OPT-PGMNAME` (COADM01C) are flagged as unresolved; the targets are
   the literal program lists in `COMEN02Y.cpy` and `COADM02Y.cpy`. Likewise the IMS `*-SSA`
   "missing dynamic call values" in PAUDBLOD/PAUDBUNL/DBUNLDGS are segment search arguments, not calls.
3. **IMS DB access is absent from the lineage** - `program_to_dsn` lists only IMS system
   libraries for CBPAUP0C and nothing for COPAUA0C/COPAUS0C/COPAUS1C even though they issue
   `EXEC DLI` against PAUTDB (DBDs DBPAUTP0/DBPAUTX0, PSBs PSBPAUTB/PSBPAUTL). Access modes for the
   GSAM outputs of DBUNLDGS are blank.
4. **Missing CICS transaction links for the sub-apps** - COPAUA0C (CP00 MQ trigger), COACCT01
   (CDRA) and CODATE01 (CDRD) have no `TRANSACTION` edge; taken from `CRDDEMO2.csd` / `CRDDEMOM.csd`.
5. **`COCRDSEC` reported as a missing program** - it is only a `DEFINE PROGRAM` in `CARDDEMO.CSD`;
   no source exists in the repo and nothing calls it. Not turned into a story.
6. **CBTRN01C has no lineage and no JCL** - no job in `app/jcl` executes it (superseded by CBTRN02C);
   its story is marked verify-or-retire.
7. **CBSTM03B shows no copybooks** - correct, but the layouts are inline; only the program-level
   data dictionary describes them.
8. **50 "missing datasets" are run-time outputs** (`*.PS`, `*.IMPORT`, `IMPORT.ERRORS`, `STATEMNT.*`,
   `TCATBALF.REPT`, GDG `(+1)` names) and `INPFILE` is a literal DSN in `MNTTRDB2.jcl`; 32 "missing
   source files" are DD names, not files. These are noise for the backlog but recorded in the stories.
9. **Environment libraries are not in the repo** - IMS/Db2/CICS PDS (`OEM.*`, `OEMA.*`, `IMS.*`),
   control card `DFSVSMDB`, `TXT2PDF` load library, `AWS.M2.CARDDEMO.LOADLIB` flagged as an
   "unsupported system utility" in 15 JCL. None of these affect Java scope.
10. **`data_dictionary_cbl.csv` covers programs, `data_dictionary_cpy.csv` copybooks** - the README
    of the archive mentions only the former; both were needed for data-item counts.
11. **Duplicate data ownership not surfaced** - TRANTYPE/TRANCATG exist as VSAM and Db2
    (`TRANEXTR` copies Db2 back to VSAM); CUSTREC vs CVCUS01Y describe the same customer record.
12. **Assembler and LE calls have no lineage** - `COBDATFT.asm` (CBACT01C), `MVSWAIT.asm`
    (COBSWAIT), `CEEDAYS` (CSUTLDTC), `CEE3ABD`; handled by a platform task.
13. **`DEFCUST.jcl`/`ESDSRRDS.jcl` define clusters no program reads** (`AWS.CUSTDATA.CLUSTER`,
    ESDS/RRDS variants of USRSEC) - noted, not migrated as data.

## Jira import

1. Jira -> Settings -> System -> External System Import -> CSV; upload `carddemo-modernization-backlog.csv`.
2. Map columns: `Issue Type` -> Issue Type, `Summary` -> Summary, `Description` -> Description,
   `Priority` -> Priority, `Labels` -> Labels (space separated, choose "Labels" field), `Epic Name` ->
   Epic Name (company-managed) or leave unmapped for team-managed, `Parent` -> Parent (team-managed)
   or Epic Link (company-managed; the importer matches on Epic Name/Summary), `Story Points` ->
   Story Points / Story point estimate, `Components` -> Component/s (create the seven components first
   or tick "create missing").
3. Epics appear first in the file so the parent lookup resolves in a single pass; descriptions are
   multi-line and quoted, so keep the default `"` quote character and `\n` line ending.
4. Dependencies are not in the CSV (Jira CSV import cannot create issue links without keys). After
   import, use `carddemo-modernization-backlog.json` `depends_on` with the Jira REST
   `issueLink` API (type "Blocks") keyed by Summary, or paste them into a "Depends on" text field.
