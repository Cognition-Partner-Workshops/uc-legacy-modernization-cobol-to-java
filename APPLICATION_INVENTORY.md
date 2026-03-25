# CardDemo Application Inventory

> **System**: AWS CardDemo -- Mainframe Credit Card Management System
> **Platform**: COBOL / CICS / VSAM / JCL / BMS (IBM z/OS)
> **Generated**: 2026-03-25

---

## Summary

| Artifact Type         | Count |
|-----------------------|-------|
| COBOL Programs (Core) | 31    |
| COBOL Programs (Optional Modules) | 13 |
| Copybooks (Core)      | 30    |
| Copybooks (Optional Modules) | 11 |
| BMS Maps (Core)       | 17    |
| BMS Maps (Optional Modules) | 4 |
| BMS-Generated Copybooks | 21 |
| JCL Jobs (Core)       | 38    |
| JCL Jobs (Optional Modules) | 8 |
| JCL Procedures        | 2     |
| Assembler Programs    | 2     |
| Scheduler Configs     | 2     |
| **Total Artifacts**   | **179** |

---

## 1. COBOL Programs -- Core (`app/cbl/`)

### 1.1 Online CICS Programs (prefix `CO*`)

| # | Program      | Lines | CICS Tran | Description                        | Classification    | BMS Map    |
|---|-------------|-------|-----------|------------------------------------|--------------------|------------|
| 1 | COSGN00C.cbl | 260  | CC00      | User sign-on / authentication       | Security           | COSGN00    |
| 2 | COMEN01C.cbl | 308  | CM00      | Main menu navigation                | Navigation         | COMEN01    |
| 3 | COADM01C.cbl | 288  | CA00      | Admin menu navigation               | Administration     | COADM01    |
| 4 | COACTVWC.cbl | 941  | CA01      | Account view (read-only)            | Account Mgmt       | COACTVW    |
| 5 | COACTUPC.cbl | 4236 | CA02      | Account update                      | Account Mgmt       | COACTUP    |
| 6 | COCRDLIC.cbl | 1459 | CC01      | Card list / browse                  | Card Mgmt          | COCRDLI    |
| 7 | COCRDSLC.cbl | 887  | CC02      | Card detail view                    | Card Mgmt          | COCRDSL    |
| 8 | COCRDUPC.cbl | 1560 | CC03      | Card update                         | Card Mgmt          | COCRDUP    |
| 9 | COTRN00C.cbl | 699  | CT00      | Transaction list / browse           | Transaction Mgmt   | COTRN00    |
| 10| COTRN01C.cbl | 330  | CT01      | Transaction detail view             | Transaction Mgmt   | COTRN01    |
| 11| COTRN02C.cbl | 783  | CT02      | Transaction add                     | Transaction Mgmt   | COTRN02    |
| 12| CORPT00C.cbl | 649  | CR00      | Transaction report request          | Reporting          | CORPT00    |
| 13| COBIL00C.cbl | 572  | CB00      | Bill payment processing             | Payments           | COBIL00    |
| 14| COUSR00C.cbl | 695  | CU00      | User list / browse (Admin)          | User Admin         | COUSR00    |
| 15| COUSR01C.cbl | 299  | CU01      | User add (Admin)                    | User Admin         | COUSR01    |
| 16| COUSR02C.cbl | 414  | CU02      | User update (Admin)                 | User Admin         | COUSR02    |
| 17| COUSR03C.cbl | 359  | CU03      | User delete (Admin)                 | User Admin         | COUSR03    |

### 1.2 Batch Programs (prefix `CB*`)

| # | Program      | Lines | Description                                   | Classification         |
|---|-------------|-------|-----------------------------------------------|------------------------|
| 18| CBACT01C.cbl | 430  | Read / dump account master file                | Data Utility           |
| 19| CBACT02C.cbl | 178  | Read / dump card master file                   | Data Utility           |
| 20| CBACT03C.cbl | 178  | Read / dump card cross-reference file           | Data Utility           |
| 21| CBACT04C.cbl | 652  | Interest calculation on accounts               | Financial Processing   |
| 22| CBCUS01C.cbl | 178  | Read / dump customer master file               | Data Utility           |
| 23| CBTRN01C.cbl | 494  | Daily transaction validation                   | Transaction Processing |
| 24| CBTRN02C.cbl | 731  | Transaction posting to master file             | Transaction Processing |
| 25| CBTRN03C.cbl | 649  | Transaction report generation                  | Reporting              |
| 26| CBSTM03A.CBL | 924  | Statement generation (main)                    | Statement Processing   |
| 27| CBSTM03B.CBL | 230  | Statement generation (subroutine)              | Statement Processing   |
| 28| CBEXPORT.cbl | 582  | Multi-entity data export to flat file          | Data Migration         |
| 29| CBIMPORT.cbl | 487  | Multi-entity data import from flat file        | Data Migration         |

### 1.3 Utility Programs

| # | Program      | Lines | Description                            | Classification |
|---|-------------|-------|----------------------------------------|----------------|
| 30| CSUTLDTC.cbl | 157  | Date conversion utility (calls CEEDAYS)| Shared Utility |
| 31| COBSWAIT.cbl | 41   | Wait / delay utility (calls MVSWAIT)   | Shared Utility |

---

## 2. COBOL Programs -- Optional Modules

### 2.1 Authorization Module -- IMS/DB2/MQ (`app/app-authorization-ims-db2-mq/cbl/`)

| # | Program      | Description                                    | Classification          |
|---|-------------|------------------------------------------------|-------------------------|
| 1 | COPAUA0C.cbl | MQ trigger for authorization requests          | MQ Integration          |
| 2 | COPAUS0C.cbl | Authorization summary screen                   | Online / IMS            |
| 3 | COPAUS1C.cbl | Authorization details screen                   | Online / IMS            |
| 4 | COPAUS2C.cbl | Fraud marking (writes to DB2)                  | Online / DB2            |
| 5 | CBPAUP0C.cbl | Batch purge of authorization records           | Batch / DB2             |
| 6 | DBUNLDGS.CBL | IMS database unload (general segments)         | Database Utility        |
| 7 | PAUDBLOD.CBL | IMS database load (authorization data)         | Database Utility        |
| 8 | PAUDBUNL.CBL | IMS database unload (authorization data)       | Database Utility        |

### 2.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cbl/`)

| # | Program      | Description                                    | Classification          |
|---|-------------|------------------------------------------------|-------------------------|
| 9 | COTRTLIC.cbl | Transaction type list / delete (DB2 cursor)    | Online / DB2            |
| 10| COTRTUPC.cbl | Transaction type add / edit (DB2)              | Online / DB2            |
| 11| COBTUPDT.cbl | Batch update of transaction types              | Batch / DB2             |

### 2.3 VSAM-MQ Module (`app/app-vsam-mq/cbl/`)

| # | Program      | Description                                    | Classification          |
|---|-------------|------------------------------------------------|-------------------------|
| 12| CODATE01.cbl | MQ request/response for system date (CDRD)     | MQ Integration          |
| 13| COACCT01.cbl | MQ request/response for account inquiry (CDRA) | MQ Integration          |

---

## 3. Copybooks -- Core (`app/cpy/`)

### 3.1 Data Record Layouts (prefix `CV*`)

| # | Copybook     | Lines | Record Size | Description                          | Business Entity      |
|---|-------------|-------|-------------|--------------------------------------|----------------------|
| 1 | CVACT01Y.cpy | 20   | 300 bytes   | Account master record                | Account              |
| 2 | CVACT02Y.cpy | 14   | 150 bytes   | Card master record                   | Card                 |
| 3 | CVACT03Y.cpy | 11   | 50 bytes    | Card-to-customer cross-reference     | Cross-Reference      |
| 4 | CVCUS01Y.cpy | 26   | 500 bytes   | Customer master record               | Customer             |
| 5 | CVCRD01Y.cpy | 46   | --          | Card navigation / commarea control   | Card (Control)       |
| 6 | CVTRA01Y.cpy | 13   | 50 bytes    | Transaction category balance record  | Tran Category Bal    |
| 7 | CVTRA02Y.cpy | 13   | 50 bytes    | Discount group / interest rate record| Discount Group       |
| 8 | CVTRA03Y.cpy | 10   | 60 bytes    | Transaction type reference           | Transaction Type     |
| 9 | CVTRA04Y.cpy | 12   | 60 bytes    | Transaction category type record     | Tran Category Type   |
| 10| CVTRA05Y.cpy | 21   | 350 bytes   | Transaction master record            | Transaction          |
| 11| CVTRA06Y.cpy | 21   | 350 bytes   | Daily transaction record             | Daily Transaction    |
| 12| CVTRA07Y.cpy | 73   | --          | Transaction report layout / headers  | Report Layout        |
| 13| CVEXPORT.cpy | 103  | 500 bytes   | Export/import composite record       | Data Exchange        |

### 3.2 Common / Infrastructure Copybooks (prefix `CS*`, `CO*`)

| # | Copybook     | Lines | Description                                     | Classification        |
|---|-------------|-------|-------------------------------------------------|-----------------------|
| 14| COCOM01Y.cpy | 47   | CICS commarea (navigation state)                | Session Management    |
| 15| COADM02Y.cpy | 62   | Admin menu option definitions                   | Menu Configuration    |
| 16| COMEN02Y.cpy | 101  | Main menu option definitions                    | Menu Configuration    |
| 17| CODATECN.cpy | 52   | Date conversion record (assembler interface)    | Date Utility          |
| 18| COTTL01Y.cpy | 27   | Application title / header literals             | UI Constants          |
| 19| CSDAT01Y.cpy | 58   | Working-storage date/time fields                | Date/Time             |
| 20| CSLKPCDY.cpy | 1318 | US phone area-code & state/ZIP lookup tables    | Validation Lookup     |
| 21| CSMSG01Y.cpy | 24   | Common user messages                            | UI Messages           |
| 22| CSMSG02Y.cpy | 35   | Abend / error handling messages                 | Error Handling        |
| 23| CSSETATY.cpy | 30   | Attribute-setting COPY REPLACING template        | UI Attribute Utility  |
| 24| CSSTRPFY.cpy | 85   | String parsing / formatting utility             | String Utility        |
| 25| CSUSR01Y.cpy | 26   | User security record (80 bytes)                 | Security              |
| 26| CSUTLDPY.cpy | 375  | Date utility parameter definitions              | Date Utility          |
| 27| CSUTLDWY.cpy | 89   | Date editing working-storage fields             | Date Utility          |
| 28| CUSTREC.cpy  | 26   | Customer record (alternate layout)              | Customer              |
| 29| COSTM01.CPY  | 38   | Statement transaction record layout             | Statement Processing  |
| 30| UNUSED1Y.cpy | 10   | Unused / placeholder record                     | Deprecated            |

---

## 4. Copybooks -- Optional Modules

### 4.1 Authorization Module (`app/app-authorization-ims-db2-mq/cpy/`)

| # | Copybook     | Description                                |
|---|-------------|--------------------------------------------|
| 1 | CCPAUERY.cpy | Authorization error response               |
| 2 | CCPAURLY.cpy | Authorization rule definition               |
| 3 | CCPAURQY.cpy | Authorization request message               |
| 4 | CIPAUDTY.cpy | Authorization detail record                 |
| 5 | CIPAUSMY.cpy | Authorization summary record                |
| 6 | IMSFUNCS.cpy | IMS function codes and constants            |
| 7 | PADFLPCB.CPY | IMS PCB masks (authorization data)          |
| 8 | PASFLPCB.CPY | IMS PCB masks (authorization summary)       |
| 9 | PAUTBPCB.CPY | IMS PCB masks (authorization table)         |

### 4.2 Transaction Type DB2 Module (`app/app-transaction-type-db2/cpy/`)

| # | Copybook     | Description                                |
|---|-------------|--------------------------------------------|
| 10| CSDB2RPY.cpy | DB2 read parameter area                    |
| 11| CSDB2RWY.cpy | DB2 rewrite parameter area                 |

---

## 5. BMS Maps -- Core (`app/bms/`)

| # | Map Set     | Lines | Screen Name      | Map Name  | Description                           |
|---|------------|-------|------------------|-----------|---------------------------------------|
| 1 | COSGN00.bms | 101  | Sign-On          | COSGN0A   | Login screen (User ID / Password)     |
| 2 | COMEN01.bms | 167  | Main Menu        | COMEN1A   | 12-option main menu                   |
| 3 | COADM01.bms | 167  | Admin Menu       | COADM1A   | 12-option admin menu                  |
| 4 | COACTVW.bms | 378  | Account View     | CACTVWA   | Read-only account detail              |
| 5 | COACTUP.bms | 512  | Account Update   | CACTUPA   | Editable account fields               |
| 6 | COCRDLI.bms | 344  | Card List        | CCRDLIA   | Scrollable card listing               |
| 7 | COCRDSL.bms | 157  | Card Detail      | CCRDSLA   | Read-only card detail                 |
| 8 | COCRDUP.bms | 172  | Card Update      | CCRDUPA   | Editable card fields                  |
| 9 | COTRN00.bms | 449  | Transaction List | COTRN0A   | Scrollable transaction listing        |
| 10| COTRN01.bms | 273  | Transaction View | COTRN1A   | Read-only transaction detail          |
| 11| COTRN02.bms | 307  | Transaction Add  | COTRN2A   | New transaction entry form            |
| 12| CORPT00.bms | 231  | Report Request   | CORPT0A   | Report criteria (monthly/yearly/custom)|
| 13| COBIL00.bms | 141  | Bill Payment     | COBIL0A   | Bill payment entry form               |
| 14| COUSR00.bms | 463  | User List        | COUSR0A   | Scrollable user listing (Admin)       |
| 15| COUSR01.bms | 164  | User Add         | COUSR1A   | New user entry form                   |
| 16| COUSR02.bms | 169  | User Update      | COUSR2A   | Editable user fields                  |
| 17| COUSR03.bms | 153  | User Delete      | COUSR3A   | User deletion confirmation            |

### BMS Maps -- Optional Modules

| # | Map Set       | Module            | Description                          |
|---|--------------|-------------------|--------------------------------------|
| 18| COPAU00.bms  | Authorization     | Authorization summary screen         |
| 19| COPAU01.bms  | Authorization     | Authorization detail screen          |
| 20| COTRTLI.bms  | Tran Type DB2     | Transaction type list screen         |
| 21| COTRTUP.bms  | Tran Type DB2     | Transaction type add/edit screen     |

---

## 6. BMS-Generated Copybooks (`app/cpy-bms/` + optional modules)

These are auto-generated COBOL copybooks that mirror each BMS map, providing the symbolic field names used by COBOL programs for SEND MAP / RECEIVE MAP.

**Core (17):** COACTUP.CPY, COACTVW.CPY, COADM01.CPY, COBIL00.CPY, COCRDLI.CPY, COCRDSL.CPY, COCRDUP.CPY, COMEN01.CPY, CORPT00.CPY, COSGN00.CPY, COTRN00.CPY, COTRN01.CPY, COTRN02.CPY, COUSR00.CPY, COUSR01.CPY, COUSR02.CPY, COUSR03.CPY

**Authorization Module (2):** COPAU00.cpy, COPAU01.cpy

**Transaction Type DB2 (2):** COTRTLI.cpy, COTRTUP.cpy

---

## 7. JCL Jobs -- Core (`app/jcl/`)

### 7.1 Data File Initialization / Refresh Jobs

| # | Job           | Lines | Purpose                                      | Key Datasets                    |
|---|--------------|-------|----------------------------------------------|---------------------------------|
| 1 | ACCTFILE.jcl  | 65   | Delete/define & load Account VSAM from PS     | ACCTDATA.VSAM.KSDS             |
| 2 | CARDFILE.jcl  | 128  | Delete/define & load Card VSAM (w/ AIX)       | CARDDATA.VSAM.KSDS             |
| 3 | CUSTFILE.jcl  | 84   | Delete/define & load Customer VSAM            | CUSTDATA.VSAM.KSDS             |
| 4 | XREFFILE.jcl  | 106  | Delete/define & load Cross-Reference VSAM     | CARDXREF.VSAM.KSDS             |
| 5 | TRANFILE.jcl  | 125  | Delete/define & load Transaction VSAM         | TRANSACT.VSAM.KSDS             |
| 6 | DUSRSECJ.jcl  | 92   | Load user security VSAM from flat file        | USRSEC.VSAM.KSDS               |
| 7 | TCATBALF.jcl  | 65   | Load transaction category balance VSAM        | TCATBALF.VSAM.KSDS             |
| 8 | TRANCATG.jcl  | 65   | Load transaction category VSAM                | TRANCATG.VSAM.KSDS             |
| 9 | TRANTYPE.jcl  | 65   | Load transaction type VSAM                    | TRANTYPE.VSAM.KSDS             |
| 10| DISCGRP.jcl   | 65   | Load discount group VSAM                      | DISCGRP.VSAM.KSDS              |
| 11| DALYREJS.jcl  | 32   | Define daily rejects GDG                      | DALYREJS                        |
| 12| REPTFILE.jcl  | 32   | Define report file                            | TRANREPT                        |

### 7.2 Batch Processing Cycle Jobs

| # | Job           | Lines | Purpose                                      | Program Executed                |
|---|--------------|-------|----------------------------------------------|---------------------------------|
| 13| CLOSEFIL.jcl  | 34   | Close CICS files before batch                 | SDSF (operator command)         |
| 14| POSTTRAN.jcl  | 45   | Post daily transactions to master             | CBTRN02C                        |
| 15| INTCALC.jcl   | 44   | Calculate interest on accounts                | CBACT04C                        |
| 16| TRANBKP.jcl   | 71   | Backup transaction master to GDG              | IDCAMS (REPRO)                  |
| 17| COMBTRAN.jcl  | 52   | Combine backup + system transactions          | SORT + IDCAMS                   |
| 18| CREASTMT.JCL  | 97   | Generate account statements (HTML + text)     | SORT + CBSTM03A                 |
| 19| TRANREPT.jcl  | 84   | Generate transaction reports                  | SORT + CBTRN03C                 |
| 20| TRANIDX.jcl   | 58   | Define/build alternate indexes                | IDCAMS                          |
| 21| OPENFIL.jcl   | 34   | Open CICS files after batch                   | SDSF (operator command)         |
| 22| WAITSTEP.jcl  | 27   | Wait/delay step between jobs                  | COBSWAIT                        |

### 7.3 Data Read / Diagnostic Jobs

| # | Job           | Lines | Purpose                                      | Program Executed                |
|---|--------------|-------|----------------------------------------------|---------------------------------|
| 23| READACCT.jcl  | 50   | Dump account master to sequential file        | CBACT01C                        |
| 24| READCARD.jcl  | 31   | Dump card master to sequential file           | CBACT02C                        |
| 25| READCUST.jcl  | 30   | Dump customer master to sequential file       | CBCUS01C                        |
| 26| READXREF.jcl  | 31   | Dump cross-reference to sequential file       | CBACT03C                        |
| 27| PRTCATBL.jcl  | 66   | Print/backup category balance file            | SORT                            |

### 7.4 Data Export / Import Jobs

| # | Job           | Lines | Purpose                                      | Program Executed                |
|---|--------------|-------|----------------------------------------------|---------------------------------|
| 28| CBEXPORT.jcl  | 72   | Export all entities to single flat file       | CBEXPORT                        |
| 29| CBIMPORT.jcl  | 68   | Import flat file into entity-specific outputs | CBIMPORT                        |

### 7.5 Infrastructure / Administration Jobs

| # | Job           | Lines | Purpose                                      |
|---|--------------|-------|----------------------------------------------|
| 30| CBADMCDJ.jcl  | 167  | CICS CSD resource definitions (DFHCSDUP)     |
| 31| DEFGDGB.jcl   | 63   | Define GDG base entries                       |
| 32| DEFGDGD.jcl   | 94   | Define GDG datasets + initial backup copies   |
| 33| DEFCUST.jcl   | 47   | Define customer VSAM cluster                  |
| 34| ESDSRRDS.jcl  | 124  | Define ESDS and RRDS VSAM clusters            |
| 35| FTPJCL.JCL    | 42   | FTP file transfer job                         |
| 36| INTRDRJ1.JCL  | 19   | Internal reader job chain (step 1)            |
| 37| INTRDRJ2.JCL  | 14   | Internal reader job chain (step 2)            |
| 38| TXT2PDF1.JCL  | 41   | Convert text statement to PDF                 |

---

## 8. JCL Procedures (`app/proc/`)

| # | Procedure     | Description                                    |
|---|--------------|------------------------------------------------|
| 1 | REPROC.prc   | Reusable REPRO procedure (IDCAMS copy)          |
| 2 | TRANREPT.prc | Transaction report procedure (wraps CBTRN03C)   |

---

## 9. Assembler Programs (`app/asm/`)

| # | Program       | Description                                    |
|---|--------------|------------------------------------------------|
| 1 | COBDATFT.asm | Date formatting routine (called by CBACT01C)    |
| 2 | MVSWAIT.asm  | Timer wait routine (called by COBSWAIT)         |

---

## 10. Scheduler Configurations (`app/scheduler/`)

| # | Config            | Description                                |
|---|------------------|--------------------------------------------|
| 1 | CardDemo.ca7     | CA-7 job scheduling definitions             |
| 2 | CardDemo.controlm| Control-M job scheduling definitions        |

---

## 11. Classification Summary

### By Business Domain

| Domain               | Online Programs | Batch Programs | Total |
|----------------------|-----------------|----------------|-------|
| Account Management   | 2               | 2              | 4     |
| Card Management      | 3               | 2              | 5     |
| Transaction Mgmt     | 3               | 3              | 6     |
| Bill Payment         | 1               | 0              | 1     |
| Reporting            | 1               | 2              | 3     |
| Statement Processing | 0               | 2              | 2     |
| User Administration  | 4               | 0              | 4     |
| Security / Sign-on   | 1               | 0              | 1     |
| Navigation / Menus   | 2               | 0              | 2     |
| Data Migration       | 0               | 2              | 2     |
| Utilities            | 0               | 2              | 2     |

### By Technology Stack

| Stack Component      | Usage                                         |
|----------------------|-----------------------------------------------|
| CICS                 | 17 online programs (SEND/RECEIVE MAP, XCTL, READ/WRITE) |
| VSAM KSDS            | Primary data store (8 VSAM clusters)          |
| VSAM ESDS/RRDS       | Alternate VSAM organizations (ESDSRRDS.jcl)   |
| VSAM Alternate Index  | Card file AIX, transaction AIX                |
| Sequential (PS)      | Flat file sources, GDG backups, reports        |
| GDG                  | Transaction backups, daily transaction files   |
| BMS 3270 Maps        | 17 terminal screens                           |
| SORT (DFSORT/SYNCSORT)| 4 JCL jobs use external sort                  |
| IDCAMS               | 20+ JCL jobs use IDCAMS for VSAM operations   |
| LE Callable Services | CEEDAYS (date conversion), CEE3ABD (abend)    |
| IMS DB (Optional)    | Authorization module -- hierarchical DB        |
| DB2 (Optional)       | Transaction type module -- relational tables   |
| MQ (Optional)        | Authorization & VSAM-MQ modules               |
