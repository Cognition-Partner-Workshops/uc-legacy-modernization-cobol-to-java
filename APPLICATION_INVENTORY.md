# CardDemo Application Inventory

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Total Assets:** 31 COBOL programs + 30 copybooks + 38 JCL jobs + 17 BMS maps + 13 optional-module programs

---

## 1. COBOL Programs (`app/cbl/`)

### 1.1 Online CICS Programs (User-Facing)

| # | Program | Lines | Trans ID | Function | Business Domain | VSAM Files Accessed |
|---|---------|-------|----------|----------|-----------------|---------------------|
| 1 | **COSGN00C** | 261 | CC00 | Sign-on / Authentication | Security | USRSEC |
| 2 | **COMEN01C** | 309 | CM00 | Main Menu (Regular Users) | Navigation | USRSEC |
| 3 | **COADM01C** | 288 | CA00 | Admin Menu | Navigation / Admin | USRSEC |
| 4 | **COACTVWC** | 942 | CAVW | Account View | Account Mgmt | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT |
| 5 | **COACTUPC** | 4,237 | CAUP | Account Update | Account Mgmt | ACCTDAT, CARDAIX, CXACAIX, CUSTDAT |
| 6 | **COCRDLIC** | 1,460 | CCLI | Credit Card List | Card Mgmt | CARDDAT, CARDAIX |
| 7 | **COCRDSLC** | 888 | CCDL | Credit Card Detail View | Card Mgmt | CARDDAT, CARDAIX |
| 8 | **COCRDUPC** | 1,560 | CCUP | Credit Card Update | Card Mgmt | CARDDAT, CARDAIX |
| 9 | **COTRN00C** | 699 | CT00 | Transaction List | Transactions | TRANSACT |
| 10 | **COTRN01C** | 330 | CT01 | Transaction Detail View | Transactions | TRANSACT |
| 11 | **COTRN02C** | 783 | CT02 | Transaction Add | Transactions | TRANSACT, ACCTDAT, CCXREF, CXACAIX |
| 12 | **CORPT00C** | 649 | CR00 | Transaction Report Request | Reporting | TRANSACT |
| 13 | **COBIL00C** | 572 | CB00 | Bill Payment | Billing / Payments | TRANSACT, ACCTDAT, CXACAIX |
| 14 | **COUSR00C** | 695 | CU00 | User List (Admin) | User Admin | USRSEC |
| 15 | **COUSR01C** | 299 | CU01 | User Add (Admin) | User Admin | USRSEC |
| 16 | **COUSR02C** | 414 | CU02 | User Update (Admin) | User Admin | USRSEC |
| 17 | **COUSR03C** | 359 | CU03 | User Delete (Admin) | User Admin | USRSEC |

### 1.2 Batch Programs

| # | Program | Lines | Function | Business Domain | Files Used |
|---|---------|-------|----------|-----------------|------------|
| 18 | **CBACT01C** | 430 | Read/Export Account Master | Account Mgmt | ACCTFILE (VSAM), OUTFILE, ARRYFILE, VBRCFILE |
| 19 | **CBACT02C** | 178 | Read/Display Card File | Card Mgmt | CARDFILE (VSAM) |
| 20 | **CBACT03C** | 178 | Read/Display Card Cross-Reference | Card Mgmt | XREFFILE (VSAM) |
| 21 | **CBACT04C** | 652 | Interest Calculation | Financial Processing | ACCTFILE, TCATBALF, DISCGRP, TRANFILE |
| 22 | **CBCUS01C** | 178 | Read/Display Customer File | Customer Mgmt | CUSTFILE (VSAM) |
| 23 | **CBTRN01C** | 494 | Read/Display Transaction File | Transactions | TRANSACT (VSAM) |
| 24 | **CBTRN02C** | 731 | Transaction Posting | Transactions | TRANSACT, DALYTRAN, XREFFILE, ACCTFILE |
| 25 | **CBTRN03C** | 649 | Transaction Report Generation | Reporting | TRANFILE, CARDXREF, TRANTYPE, TRTEFMT, CUSTFILE |
| 26 | **CBSTM03A** | 924 | Statement Generation (Main) | Statements | XREFFILE, CUSTFILE, ACCTFILE, STMTFILE, HTMLFILE |
| 27 | **CBSTM03B** | 230 | Statement Generation (Subroutine) | Statements | TRNXFILE, XREFFILE, CUSTFILE |
| 28 | **CBEXPORT** | 582 | Data Export to Sequential | Data Migration | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, EXPORTFL |
| 29 | **CBIMPORT** | 487 | Data Import from Sequential | Data Migration | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, IMPORTFL |
| 30 | **CSUTLDTC** | 157 | Date Utility (Validation/Conversion) | Utility | N/A (called as subroutine) |
| 31 | **COBSWAIT** | 41 | Wait Utility (Timer in centiseconds) | Utility | N/A (calls MVSWAIT ASM) |

---

## 2. Copybooks (`app/cpy/`)

### 2.1 Data Record Layouts (CV-prefix = VSAM record structures)

| # | Copybook | Lines | Entity | Description |
|---|----------|-------|--------|-------------|
| 1 | **CVACT01Y** | 20 | Account | Account master record (300 bytes) — balances, limits, status, dates |
| 2 | **CVACT02Y** | 14 | Card | Card master record (150 bytes) — card number, CVV, status, expiry |
| 3 | **CVACT03Y** | 11 | Cross-Reference | Card-to-Account-to-Customer cross-reference (50 bytes) |
| 4 | **CVCUS01Y** | 26 | Customer | Customer master record (500 bytes) — name, address, SSN, DOB, FICO |
| 5 | **CVTRA01Y** | 13 | Transaction Category Balance | Account transaction category balance |
| 6 | **CVTRA02Y** | 13 | Discount Group | Account discount/interest rate group |
| 7 | **CVTRA03Y** | 10 | Transaction Type | Transaction type code + description |
| 8 | **CVTRA04Y** | 12 | Transaction Category Type | Transaction category + type description |
| 9 | **CVTRA05Y** | 21 | Transaction (Online) | Online transaction record (350 bytes) — full transaction detail |
| 10 | **CVTRA06Y** | 21 | Transaction (Daily) | Daily transaction record — mirrors CVTRA05Y for batch |
| 11 | **CVTRA07Y** | 73 | Transaction Report | Report formatting/headers for transaction reports |
| 12 | **CUSTREC** | — | Customer (Alt) | Alternate customer record layout |
| 13 | **CVEXPORT** | 103 | Export Record | Composite export record with customer, account, card data |
| 14 | **COSTM01** | 38 | Statement Transaction | Transaction record layout for statement processing |

### 2.2 Application Control Copybooks (CO/CS-prefix)

| # | Copybook | Lines | Purpose |
|---|----------|-------|---------|
| 15 | **COCOM01Y** | — | Application COMMAREA — inter-program communication area |
| 16 | **COMEN02Y** | — | Menu option definitions (program names, labels, user types) |
| 17 | **COADM02Y** | — | Admin menu option definitions |
| 18 | **COTTL01Y** | — | Screen title constants (CardDemo title lines) |
| 19 | **CSDAT01Y** | — | Current date/time working storage fields |
| 20 | **CSMSG01Y** | — | Common user-facing messages (invalid key, thank you, etc.) |
| 21 | **CSMSG02Y** | — | Abend handling variables and messages |
| 22 | **CSUSR01Y** | — | Signed-on user security record structure |
| 23 | **CSLKPCDY** | — | Lookup code validation tables |
| 24 | **CSSETATY** | — | Screen field attribute setting (REPLACING technique) |
| 25 | **CSSTRPFY** | — | String strip/pad utility routines |
| 26 | **CSUTLDPY** | — | Date utility parameter area |
| 27 | **CSUTLDWY** | — | Date edit working storage (CCYYMMDD validation) |
| 28 | **CODATECN** | — | Date conversion constants |
| 29 | **CVCRD01Y** | 46 | Credit card work area — common card processing variables |
| 30 | **UNUSED1Y** | 10 | Unused/placeholder record layout |

---

## 3. BMS Screen Maps (`app/bms/`)

| # | Map | Screen ID | Associated Program | Screen Purpose |
|---|-----|-----------|-------------------|----------------|
| 1 | **COSGN00** | COSGN0A | COSGN00C | Sign-on screen (User ID + Password) |
| 2 | **COMEN01** | COMEN1A | COMEN01C | Main menu (Regular user options) |
| 3 | **COADM01** | COADM1A | COADM01C | Admin menu screen |
| 4 | **COACTVW** | CACTVWA | COACTVWC | Account view display |
| 5 | **COACTUP** | CACTUPA | COACTUPC | Account update form |
| 6 | **COCRDLI** | CCRDLIA | COCRDLIC | Credit card list |
| 7 | **COCRDSL** | CCRDSLA | COCRDSLC | Credit card detail view |
| 8 | **COCRDUP** | CCRDUPA | COCRDUPC | Credit card update form |
| 9 | **COTRN00** | COTRN0A | COTRN00C | Transaction list |
| 10 | **COTRN01** | COTRN1A | COTRN01C | Transaction detail view |
| 11 | **COTRN02** | COTRN2A | COTRN02C | Transaction add form |
| 12 | **CORPT00** | CORPT0A | CORPT00C | Report request screen |
| 13 | **COBIL00** | COBIL0A | COBIL00C | Bill payment screen |
| 14 | **COUSR00** | COUSR0A | COUSR00C | User list (Admin) |
| 15 | **COUSR01** | COUSR1A | COUSR01C | User add form (Admin) |
| 16 | **COUSR02** | COUSR2A | COUSR02C | User update form (Admin) |
| 17 | **COUSR03** | COUSR3A | COUSR03C | User delete confirmation (Admin) |

**BMS-Generated Copybooks** (`app/cpy-bms/`): 17 corresponding `.CPY` files mirror each BMS map above, providing symbolic field definitions for COBOL program SEND/RECEIVE MAP operations.

---

## 4. JCL Batch Jobs (`app/jcl/`)

### 4.1 Data Load / Refresh Jobs

| # | JCL Job | Programs Executed | Function |
|---|---------|------------------|----------|
| 1 | **ACCTFILE** | IDCAMS | Define and load Account master VSAM KSDS |
| 2 | **CARDFILE** | IDCAMS | Define and load Card master VSAM KSDS |
| 3 | **CUSTFILE** | IDCAMS | Define and load Customer master VSAM KSDS |
| 4 | **XREFFILE** | IDCAMS | Define and load Card cross-reference VSAM KSDS + alternate indexes |
| 5 | **TRANFILE** | SDSF, IDCAMS | Define and load Transaction master VSAM KSDS |
| 6 | **DUSRSECJ** | IDCAMS | Define and load User Security VSAM KSDS |
| 7 | **TRANTYPE** | IDCAMS | Define and load Transaction Type VSAM KSDS |
| 8 | **TRANCATG** | IDCAMS | Define and load Transaction Category VSAM KSDS |
| 9 | **TCATBALF** | IDCAMS | Define and load Transaction Category Balance VSAM KSDS |
| 10 | **DISCGRP** | IDCAMS | Define and load Discount Group VSAM KSDS |

### 4.2 CICS File Management

| # | JCL Job | Function |
|---|---------|----------|
| 11 | **CLOSEFIL** | Close CICS-managed VSAM files for batch processing |
| 12 | **OPENFIL** | Re-open CICS-managed VSAM files after batch |

### 4.3 Core Batch Processing

| # | JCL Job | Programs Executed | Function |
|---|---------|------------------|----------|
| 13 | **POSTTRAN** | CBTRN02C | Post daily transactions to account master |
| 14 | **INTCALC** | CBACT04C | Calculate and apply interest charges |
| 15 | **COMBTRAN** | SORT, IDCAMS | Combine daily transactions into master |
| 16 | **CREASTMT** | SORT, IDCAMS, CBSTM03A | Generate customer statements (text + HTML) |
| 17 | **TRANBKP** | REPROC (proc), IDCAMS | Backup transaction file to GDG |
| 18 | **TRANREPT** | REPROC, SORT, CBTRN03C | Generate daily transaction reports |
| 19 | **TRANIDX** | IDCAMS | Define/rebuild alternate indexes on transaction file |

### 4.4 Reporting and Utilities

| # | JCL Job | Programs Executed | Function |
|---|---------|------------------|----------|
| 20 | **READACCT** | CBACT01C | Read/display account master records |
| 21 | **READCARD** | CBACT02C | Read/display card master records |
| 22 | **READCUST** | CBCUS01C | Read/display customer records |
| 23 | **READXREF** | CBACT03C | Read/display cross-reference records |
| 24 | **REPTFILE** | IDCAMS | Define report output VSAM file |
| 25 | **CBEXPORT** | CBEXPORT | Export all VSAM data to sequential files |
| 26 | **CBIMPORT** | CBIMPORT | Import sequential data into VSAM files |
| 27 | **DALYREJS** | IDCAMS | Define daily rejection file |
| 28 | **DEFCUST** | IDCAMS | Define customer VSAM dataset |
| 29 | **DEFGDGB** | IDCAMS | Define GDG base for transaction backups |
| 30 | **DEFGDGD** | IDCAMS | Define GDG base for daily transactions |
| 31 | **PRTCATBL** | IDCAMS | Print catalog entries |
| 32 | **ESDSRRDS** | IDCAMS | Define ESDS/RRDS datasets |
| 33 | **WAITSTEP** | COBSWAIT | Wait step utility |
| 34 | **TXT2PDF1** | IKJEFT1B (TXT2PDF) | Convert text statements to PDF |
| 35 | **FTPJCL** | FTP | FTP file transfer |
| 36 | **INTRDRJ1** | IDCAMS, IEBGENER | Internal reader job submission (chain 1) |
| 37 | **INTRDRJ2** | IDCAMS | Internal reader job submission (chain 2) |
| 38 | **CBADMCDJ** | — | Admin batch control job |

---

## 5. Optional Extension Modules

### 5.1 Authorization Module (`app/app-authorization-ims-db2-mq/`)

IMS DB + DB2 + MQ-based card authorization processing.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 1 | **COPAUA0C** | 1,026 | CICS/MQ/IMS | Card authorization decision (MQ trigger program) |
| 2 | **COPAUS0C** | 1,032 | CICS/BMS/IMS | Authorization message summary view |
| 3 | **COPAUS1C** | 604 | CICS/BMS/IMS | Authorization message detail view |
| 4 | **COPAUS2C** | 244 | CICS/DB2/IMS | Mark authorization as fraud (DB2 update) |
| 5 | **CBPAUP0C** | 386 | Batch/IMS | Purge expired pending authorizations |
| 6 | **DBUNLDGS** | 366 | Batch/IMS | Unload IMS DB to sequential (GSAM) |
| 7 | **PAUDBLOD** | 369 | Batch/IMS | Load sequential data into IMS DB |
| 8 | **PAUDBUNL** | 317 | Batch/IMS | Unload IMS DB segments |

Additional assets: 9 copybooks, 2 BMS maps, 5 JCL jobs, IMS DBD/PSB definitions, DB2 DDL.

### 5.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/`)

DB2-based transaction type master maintenance.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 9 | **COTRTUPC** | 1,702 | CICS/DB2/BMS | Transaction type add/update (embedded SQL) |
| 10 | **COTRTLIC** | 2,098 | CICS/DB2/BMS | Transaction type list/delete (cursors, embedded SQL) |
| 11 | **COBTUPDT** | 237 | Batch/DB2 | Batch transaction type update |

Additional assets: 2 copybooks, 2 BMS maps, 3 JCL jobs, DB2 DDL/DCL.

### 5.3 VSAM-MQ Module (`app/app-vsam-mq/`)

MQ request/response services for real-time data retrieval.

| # | Program | Lines | Type | Function |
|---|---------|-------|------|----------|
| 12 | **COACCT01** | 620 | CICS/MQ | Account inquiry via MQ request/response |
| 13 | **CODATE01** | 524 | CICS/MQ | System date retrieval via MQ request/response |

---

## 6. Supporting Assets

### 6.1 Assembler Programs (`app/asm/`)

| Program | Function |
|---------|----------|
| **MVSWAIT** | Mainframe wait/sleep routine (called by COBSWAIT) |
| **COBDATFT** | Date formatting utility |

### 6.2 JCL Procedures (`app/proc/`)

| Procedure | Function |
|-----------|----------|
| **REPROC** | Reusable REPRO (file copy) procedure for VSAM-to-sequential backup |
| **TRANREPT** | Transaction report generation procedure |

### 6.3 Scheduler Configurations (`app/scheduler/`)

| File | Function |
|------|----------|
| **CardDemo.ca7** | CA-7 job scheduling definitions |
| **CardDemo.controlm** | Control-M job scheduling definitions |

### 6.4 CICS Resource Definitions (`app/csd/`)

| File | Function |
|------|----------|
| **CARDDEMO.CSD** | CICS System Definition — transaction, program, and file resource definitions |

---

## 7. Classification Summary

| Category | Count | Technology |
|----------|-------|------------|
| Online CICS Programs | 17 | COBOL / CICS / BMS / VSAM |
| Batch Programs | 14 | COBOL / JCL / VSAM / QSAM |
| Utility Programs | 2 | COBOL / ASM |
| Optional Module Programs | 13 | COBOL / IMS / DB2 / MQ |
| Copybooks (Data) | 14 | COBOL COPY |
| Copybooks (Control) | 16 | COBOL COPY |
| BMS Maps (Core) | 17 | BMS 3270 |
| BMS Maps (Optional) | 4 | BMS 3270 |
| JCL Jobs (Core) | 38 | JCL / IDCAMS / SORT |
| JCL Jobs (Optional) | 8 | JCL |
| **Grand Total** | **143** | |

### Naming Conventions

| Prefix | Meaning |
|--------|---------|
| `CO*` | Online CICS program |
| `CB*` | Batch COBOL program |
| `CS*` | Common/shared utility copybook |
| `CV*` | VSAM record layout copybook |
| `CC*` | Card-related copybook (authorization module) |
| `CI*` | IMS-related copybook |
