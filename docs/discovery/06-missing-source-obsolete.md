# 06 – Missing Source / Obsolete Report

This report cross-references every program reference against the set of source files actually
present in the repository.

- **Referenced programs** come from static `CALL '<lit>'`, CICS `XCTL`/`LINK PROGRAM(...)`, CICS
  `DEFINE PROGRAM`/`DEFINE TRANSACTION` in `app/csd/CARDDEMO.CSD`, and JCL `EXEC PGM=` — including
  IMS programs invoked via `DFSRRC00 PARM='…,<pgm>,…'` and DB2 programs invoked via `IKJEFT01`
  `RUN PROGRAM(<pgm>)`.
- **Source present** = all COBOL programs under `cbl/` (core + optional modules) and assembler
  modules under `asm/`.
- **Copybooks** are matched against every `COPY <name>` / `COPY '<name>'` across programs and
  copybooks.

## A. Missing Source

Programs that are *referenced* but have **no matching application source** in the repository.

### A.1 Application program with no source

| Program | Referenced by | Assessment |
| :--- | :--- | :--- |
| `COCRDSEC` | `DEFINE PROGRAM(COCRDSEC)` and `DEFINE TRANSACTION(CDV1)` in `CARDDEMO.CSD` | **Missing.** A CICS program + transaction (`CDV1`) are defined for it, but no `COCRDSEC` source exists under `cbl/`. It is also never targeted by any `XCTL`/`LINK`, so the transaction is effectively undeliverable. Needs the source, or the CSD entry should be retired. |

### A.2 External runtime services (expected — not application source)

These are resolved at bind/run time from Language Environment, IMS or MQ libraries. They are listed
for completeness and are **not** defects.

| Symbol | Kind | Called by |
| :--- | :--- | :--- |
| `CEE3ABD` | LE callable service (abend) | all core batch programs |
| `CEEDAYS` | LE callable service (date) | `CSUTLDTC` |
| `CBLTDLI` | IMS DL/I interface | `DBUNLDGS`, `PAUDBLOD`, `PAUDBUNL` |
| `MQOPEN`, `MQGET`, `MQPUT`, `MQPUT1`, `MQCLOSE` | WebSphere MQ API stubs | `COACCT01`, `CODATE01`, `COPAUA0C` |

> `COBDATFT` and `MVSWAIT` are `CALL`ed as subroutines and **do** exist as assembler source
> (`app/asm/COBDATFT.asm`, `app/asm/MVSWAIT.asm`), so they are not missing.

## B. Obsolete / Orphaned

Source files present in the repository that are **never referenced** by any JCL, `CALL`, CICS
transfer, or CSD definition.

### B.1 Orphaned programs

| Program | Type | Assessment |
| :--- | :--- | :--- |
| `CBTRN01C` | Batch COBOL | **Orphaned in this repo.** Validates daily transactions (reads daily-tran, customer, xref, card, account). No JCL `EXEC PGM=CBTRN01C`, no `CALL`, no CSD entry. Likely a pre-posting validation step whose driver JCL was not delivered; confirm before deleting — it may still be intended in the batch flow ahead of `POSTTRAN`/`CBTRN02C`. |

All other programs (including `COBSWAIT`, `CBSTM03B`, `CSUTLDTC`, and every optional-module program)
are reachable: `COBSWAIT` via `WAITSTEP.jcl`, `CBSTM03B` via `CALL` from `CBSTM03A`, `CSUTLDTC` via
`CALL` from `CORPT00C`/`COTRN02C`, and the IMS/DB2 programs via `DFSRRC00`/`IKJEFT01`.

### B.2 Copybooks never `COPY`d

| Copybook | Assessment |
| :--- | :--- |
| `app/cpy/UNUSED1Y.cpy` | **Unused.** Defines an `UNUSED-DATA` layout (an unused clone of the `USRSEC`/`CSUSR01Y` record). Not `COPY`d by any program or copybook. Safe candidate for removal. |

> `CSSTRPFY` and `CSUTLDWY` are **not** orphaned: they are pulled in with the quoted form
> `COPY 'CSSTRPFY'` / `COPY 'CSUTLDWY'` by the account/card online programs (and the trantype-db2
> module), which a naive unquoted scan misses.

### B.3 JCL invoking nonexistent programs

**None found.** Every `EXEC PGM=` in `app/jcl/` and the optional-module JCL resolves either to a
delivered program/assembler module or to a standard system utility (`IDCAMS`, `IEBGENER`,
`IEFBR14`, `SORT`/`ICEMAN`, `IKJEFT01`, `DFSRRC00`, `DFHCSDUP`, `FTP`, `SDSF`). The programs named
inside `DFSRRC00 PARM=` (`CBPAUP0C`, `PAUDBLOD`, `PAUDBUNL`, `DBUNLDGS`) and inside `IKJEFT01`
`RUN PROGRAM(COBTUPDT)` all have matching source.

## Summary

| Category | Count | Items |
| :--- | ---: | :--- |
| Missing application source | 1 | `COCRDSEC` |
| External runtime services (expected) | 8 | `CEE3ABD`, `CEEDAYS`, `CBLTDLI`, `MQOPEN`, `MQGET`, `MQPUT`, `MQPUT1`, `MQCLOSE` |
| Orphaned programs | 1 | `CBTRN01C` |
| Never-`COPY`d copybooks | 1 | `UNUSED1Y.cpy` |
| JCL → nonexistent program | 0 | — |
