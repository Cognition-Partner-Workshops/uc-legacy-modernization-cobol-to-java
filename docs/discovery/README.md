# CardDemo – Legacy Modernization Discovery Package

This directory contains the discovery/analysis reports for the COBOL **CardDemo** application, to
support a legacy-modernization (COBOL-to-Java) effort. All reports are generated from static
analysis of the source under `app/`; the optional DB2/IMS/MQ modules
(`app/app-authorization-ims-db2-mq/`, `app/app-transaction-type-db2/`, `app/app-vsam-mq/`) are noted
separately where relevant. **No application source was modified.**

## Reports

| # | Report | Summary |
| :--- | :--- | :--- |
| 01 | [Inventory & Lines of Code](01-inventory-loc.md) | Complete artifact inventory (COBOL, copybooks, JCL, PROCs, BMS, assembler, CSD) with total/code/comment line counts, per-type subtotals and a grand total, split into Batch (`CB*`) vs Online (`CO*`). |
| 02 | [Program Call Chain](02-program-call-chain.md) | Caller→callee table for static `CALL` and CICS `XCTL`/`LINK`, dynamic (variable-target) transfers flagged `dynamic/needs review`, and a Mermaid call graph. |
| 03 | [Program-to-File CRUD](03-program-file-crud.md) | CRUD matrix (programs × files) from native COBOL and CICS file verbs, logical→physical dataset resolution via JCL DD and CSD, plus DB2/IMS/MQ detection. |
| 04 | [File-to-File Mapping](04-file-to-file-mapping.md) | VSAM base clusters → DATA/INDEX components → alternate indexes and PATHs (logical files) from `LISTCAT.txt`, cross-checked with the AIX-defining JCL. |
| 05 | [File-to-Field Mapping](05-file-field-mapping.md) | Record layout per physical file traced to its copybook, with level, field, PIC, derived length and offset. |
| 06 | [Missing Source / Obsolete](06-missing-source-obsolete.md) | Referenced-but-missing programs, orphaned source, never-`COPY`d copybooks, and JCL invoking nonexistent programs. |
| 07 | [Grouping & Sequencing](07-grouping-sequencing.md) | Functional-domain grouping and the batch execution sequence (README + `app/scheduler/`) as an ordered list and Mermaid flow. |

## Application at a Glance

- **31** COBOL programs in the core module (`app/cbl/`): 11 batch (`CB*`), 17 online (`CO*`),
  3 utility/subroutine — plus **13** programs across the three optional modules.
- **10** VSAM KSDS base clusters (`ACCTDATA`, `CARDDATA`, `CARDXREF`, `CUSTDATA`, `TRANSACT`,
  `TCATBALF`, `TRANCATG`, `TRANTYPE`, `DISCGRP`, `USRSEC`) with 3 alternate indexes.
- **38** JCL members and 2 PROCs drive the batch flow; **17** BMS maps + **17** BMS symbolic
  copybooks back the CICS screens; **2** assembler modules provide date/wait helpers.
- Online entry is transaction **`CC00`** (`COSGN00C` sign-on) → `COMEN01C` / `COADM01C` menus.

## Method & Caveats

- Line counting: COBOL comment = `*`/`/` in column 7; JCL comment = `//*`; code = non-blank,
  non-comment. See report 01 for exact rules.
- Dynamic CICS transfers (`XCTL PROGRAM(<var>)`) are resolved only as far as menu option tables
  (`COMEN02Y`, `COADM02Y`) and literal `MOVE`s allow; remaining ones are flagged for manual review.
- Logical-to-physical file resolution uses JCL DD statements for batch and CSD `DSNAME(...)` for
  CICS; datasets share the `AWS.M2.CARDDEMO` high-level qualifier.
