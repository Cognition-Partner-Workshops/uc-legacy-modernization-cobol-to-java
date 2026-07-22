# 01 – Inventory Report with Lines of Code

Source root analysed: `app/`. Optional modules (`app-authorization-ims-db2-mq/`, `app-transaction-type-db2/`, `app-vsam-mq/`) are listed separately at the end.

**Counting rules.** `Total` = every physical line. `Comment` = COBOL/copybook lines with `*` or `/` in column 7 (or a line whose first non-blank char is `*`); for JCL/PROC a line whose first non-blank characters are `//*`; for BMS/assembler/CSD a line beginning with `*`. `Code` = non-blank, non-comment lines. `Code + Comment + Blank = Total`.

## COBOL Programs (`app/cbl/`)

Programs are grouped by naming convention: **`CB*` = Batch**, **`CO*` = Online (CICS)**. Three helper programs are broken out as **Utility / subroutine** because their prefix does not reflect their execution mode: `COBSWAIT` (batch wait, run by `WAITSTEP`), `CSUTLDTC` (called date-validation subroutine) and `CBSTM03B` (statement subroutine called by `CBSTM03A`).

### Batch programs (CB*)

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `CBACT01C.cbl` | 430 | 358 | 51 |
| `CBACT02C.cbl` | 178 | 129 | 34 |
| `CBACT03C.cbl` | 178 | 130 | 33 |
| `CBACT04C.cbl` | 652 | 552 | 53 |
| `CBCUS01C.cbl` | 178 | 130 | 33 |
| `CBEXPORT.cbl` | 582 | 396 | 91 |
| `CBIMPORT.cbl` | 487 | 337 | 74 |
| `CBSTM03A.CBL` | 923 | 784 | 50 |
| `CBTRN01C.cbl` | 494 | 415 | 45 |
| `CBTRN02C.cbl` | 731 | 619 | 59 |
| `CBTRN03C.cbl` | 649 | 545 | 53 |
| **Subtotal (11 files)** | **5482** | **4395** | **576** |

### Online / CICS programs (CO*)

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COACTUPC.cbl` | 4236 | 3368 | 492 |
| `COACTVWC.cbl` | 941 | 703 | 130 |
| `COADM01C.cbl` | 288 | 189 | 58 |
| `COBIL00C.cbl` | 572 | 420 | 79 |
| `COCRDLIC.cbl` | 1459 | 1093 | 203 |
| `COCRDSLC.cbl` | 887 | 642 | 130 |
| `COCRDUPC.cbl` | 1560 | 1195 | 195 |
| `COMEN01C.cbl` | 308 | 213 | 53 |
| `CORPT00C.cbl` | 649 | 498 | 63 |
| `COSGN00C.cbl` | 260 | 172 | 49 |
| `COTRN00C.cbl` | 699 | 529 | 82 |
| `COTRN01C.cbl` | 330 | 231 | 57 |
| `COTRN02C.cbl` | 783 | 614 | 85 |
| `COUSR00C.cbl` | 695 | 531 | 80 |
| `COUSR01C.cbl` | 299 | 198 | 61 |
| `COUSR02C.cbl` | 414 | 303 | 63 |
| `COUSR03C.cbl` | 359 | 251 | 63 |
| **Subtotal (17 files)** | **14739** | **11150** | **1943** |

### Utility / subroutine programs

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `CBSTM03B.CBL` | 229 | 162 | 26 |
| `COBSWAIT.cbl` | 40 | 13 | 22 |
| `CSUTLDTC.cbl` | 157 | 114 | 29 |
| **Subtotal (3 files)** | **426** | **289** | **77** |

## Copybooks – data & COMMAREA (`app/cpy/`)

### Copybooks – data & COMMAREA

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COADM02Y.cpy` | 62 | 36 | 26 |
| `COCOM01Y.cpy` | 47 | 26 | 21 |
| `CODATECN.cpy` | 52 | 36 | 16 |
| `COMEN02Y.cpy` | 101 | 64 | 22 |
| `COSTM01.CPY` | 36 | 17 | 19 |
| `COTTL01Y.cpy` | 27 | 7 | 20 |
| `CSDAT01Y.cpy` | 58 | 39 | 19 |
| `CSLKPCDY.cpy` | 1317 | 1283 | 33 |
| `CSMSG01Y.cpy` | 24 | 5 | 19 |
| `CSMSG02Y.cpy` | 35 | 9 | 23 |
| `CSSETATY.cpy` | 30 | 10 | 20 |
| `CSSTRPFY.cpy` | 85 | 63 | 22 |
| `CSUSR01Y.cpy` | 26 | 7 | 19 |
| `CSUTLDPY.cpy` | 375 | 289 | 53 |
| `CSUTLDWY.cpy` | 89 | 82 | 6 |
| `CUSTREC.cpy` | 26 | 20 | 6 |
| `CVACT01Y.cpy` | 20 | 14 | 6 |
| `CVACT02Y.cpy` | 14 | 8 | 6 |
| `CVACT03Y.cpy` | 11 | 5 | 6 |
| `CVCRD01Y.cpy` | 46 | 34 | 11 |
| `CVCUS01Y.cpy` | 26 | 20 | 6 |
| `CVEXPORT.cpy` | 103 | 72 | 26 |
| `CVTRA01Y.cpy` | 13 | 7 | 6 |
| `CVTRA02Y.cpy` | 13 | 7 | 6 |
| `CVTRA03Y.cpy` | 10 | 4 | 6 |
| `CVTRA04Y.cpy` | 12 | 6 | 6 |
| `CVTRA05Y.cpy` | 21 | 15 | 6 |
| `CVTRA06Y.cpy` | 21 | 15 | 6 |
| `CVTRA07Y.cpy` | 73 | 57 | 6 |
| `UNUSED1Y.cpy` | 10 | 7 | 3 |
| **Subtotal (30 files)** | **2783** | **2264** | **450** |

## Copybooks – BMS symbolic maps (`app/cpy-bms/`)

### Copybooks – BMS symbolic maps

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COACTUP.CPY` | 668 | 652 | 16 |
| `COACTVW.CPY` | 464 | 448 | 16 |
| `COADM01.CPY` | 260 | 244 | 16 |
| `COBIL00.CPY` | 140 | 124 | 16 |
| `COCRDLI.CPY` | 560 | 544 | 16 |
| `COCRDSL.CPY` | 200 | 184 | 16 |
| `COCRDUP.CPY` | 224 | 208 | 16 |
| `COMEN01.CPY` | 260 | 244 | 16 |
| `CORPT00.CPY` | 224 | 208 | 16 |
| `COSGN00.CPY` | 152 | 136 | 16 |
| `COTRN00.CPY` | 728 | 712 | 16 |
| `COTRN01.CPY` | 272 | 256 | 16 |
| `COTRN02.CPY` | 272 | 256 | 16 |
| `COUSR00.CPY` | 728 | 712 | 16 |
| `COUSR01.CPY` | 164 | 148 | 16 |
| `COUSR02.CPY` | 164 | 148 | 16 |
| `COUSR03.CPY` | 152 | 136 | 16 |
| **Subtotal (17 files)** | **5632** | **5360** | **272** |

## JCL (`app/jcl/`)

### JCL

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `ACCTFILE.jcl` | 65 | 36 | 29 |
| `CARDFILE.jcl` | 128 | 82 | 46 |
| `CBADMCDJ.jcl` | 167 | 108 | 40 |
| `CBEXPORT.jcl` | 72 | 34 | 37 |
| `CBIMPORT.jcl` | 68 | 34 | 34 |
| `CLOSEFIL.jcl` | 34 | 12 | 22 |
| `COMBTRAN.jcl` | 52 | 27 | 25 |
| `CREASTMT.JCL` | 97 | 68 | 29 |
| `CUSTFILE.jcl` | 84 | 48 | 36 |
| `DALYREJS.jcl` | 32 | 10 | 22 |
| `DEFCUST.jcl` | 47 | 24 | 23 |
| `DEFGDGB.jcl` | 63 | 41 | 22 |
| `DEFGDGD.jcl` | 94 | 54 | 40 |
| `DISCGRP.jcl` | 65 | 36 | 29 |
| `DUSRSECJ.jcl` | 92 | 49 | 43 |
| `ESDSRRDS.jcl` | 124 | 69 | 55 |
| `FTPJCL.JCL` | 42 | 13 | 29 |
| `INTCALC.jcl` | 44 | 22 | 22 |
| `INTRDRJ1.JCL` | 19 | 14 | 5 |
| `INTRDRJ2.JCL` | 14 | 9 | 5 |
| `OPENFIL.jcl` | 34 | 12 | 22 |
| `POSTTRAN.jcl` | 45 | 22 | 23 |
| `PRTCATBL.jcl` | 66 | 38 | 28 |
| `READACCT.jcl` | 50 | 28 | 22 |
| `READCARD.jcl` | 31 | 9 | 22 |
| `READCUST.jcl` | 30 | 9 | 21 |
| `READXREF.jcl` | 31 | 9 | 22 |
| `REPTFILE.jcl` | 32 | 10 | 22 |
| `TCATBALF.jcl` | 65 | 36 | 29 |
| `TRANBKP.jcl` | 71 | 40 | 31 |
| `TRANCATG.jcl` | 65 | 36 | 29 |
| `TRANFILE.jcl` | 125 | 81 | 44 |
| `TRANIDX.jcl` | 58 | 30 | 28 |
| `TRANREPT.jcl` | 84 | 52 | 32 |
| `TRANTYPE.jcl` | 65 | 36 | 29 |
| `TXT2PDF1.JCL` | 41 | 14 | 27 |
| `WAITSTEP.jcl` | 27 | 8 | 19 |
| `XREFFILE.jcl` | 106 | 68 | 38 |
| **Subtotal (38 files)** | **2429** | **1328** | **1081** |

## PROCs (`app/proc/`)

### PROCs

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `REPROC.prc` | 32 | 10 | 22 |
| `TRANREPT.prc` | 82 | 50 | 32 |
| **Subtotal (2 files)** | **114** | **60** | **54** |

## BMS map source (`app/bms/`)

### BMS map source

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COACTUP.bms` | 512 | 491 | 21 |
| `COACTVW.bms` | 378 | 357 | 21 |
| `COADM01.bms` | 167 | 146 | 21 |
| `COBIL00.bms` | 141 | 119 | 21 |
| `COCRDLI.bms` | 344 | 323 | 21 |
| `COCRDSL.bms` | 157 | 136 | 21 |
| `COCRDUP.bms` | 172 | 151 | 21 |
| `COMEN01.bms` | 167 | 146 | 21 |
| `CORPT00.bms` | 231 | 210 | 21 |
| `COSGN00.bms` | 210 | 189 | 21 |
| `COTRN00.bms` | 464 | 442 | 21 |
| `COTRN01.bms` | 273 | 252 | 21 |
| `COTRN02.bms` | 307 | 286 | 21 |
| `COUSR00.bms` | 463 | 442 | 21 |
| `COUSR01.bms` | 164 | 143 | 21 |
| `COUSR02.bms` | 169 | 147 | 22 |
| `COUSR03.bms` | 153 | 131 | 22 |
| **Subtotal (17 files)** | **4472** | **4111** | **359** |

## Assembler (`app/asm/`)

### Assembler

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COBDATFT.asm` | 84 | 61 | 23 |
| `MVSWAIT.asm` | 30 | 14 | 16 |
| **Subtotal (2 files)** | **114** | **75** | **39** |

## CSD – CICS resource definitions (`app/csd/`)

### CSD – CICS resource definitions

| File | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `CARDDEMO.CSD` | 505 | 505 | 0 |
| **Subtotal (1 files)** | **505** | **505** | **0** |

## Grand Total (core `app/` artifacts)

| Metric | Lines |
| :--- | ---: |
| Total | **36696** |
| Code | **29537** |
| Comment | **4851** |
| Blank | **2308** |

## Optional Modules (analysed separately)

These modules demonstrate DB2 / IMS / MQ integration and are not part of the core VSAM/CICS build. COBOL programs counted with the COBOL rules; JCL/DDL/DCL/PSB/DBD/CTL grouped as *other*.

### Pending Authorizations (IMS-DB2-MQ) – `app-authorization-ims-db2-mq/`

| COBOL program | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `CBPAUP0C.cbl` | 386 | 266 | 90 |
| `COPAUA0C.cbl` | 1026 | 771 | 152 |
| `COPAUS0C.cbl` | 1031 | 792 | 82 |
| `COPAUS1C.cbl` | 603 | 461 | 39 |
| `COPAUS2C.cbl` | 244 | 201 | 25 |
| `DBUNLDGS.CBL` | 366 | 211 | 155 |
| `PAUDBLOD.CBL` | 369 | 274 | 95 |
| `PAUDBUNL.CBL` | 317 | 222 | 95 |
| **Subtotal (8 programs)** | **4342** | **3198** | **733** |

### Transaction Type Management (DB2) – `app-transaction-type-db2/`

| COBOL program | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COBTUPDT.cbl` | 237 | 205 | 32 |
| `COTRTLIC.cbl` | 2098 | 1861 | 237 |
| `COTRTUPC.cbl` | 1702 | 1429 | 273 |
| **Subtotal (3 programs)** | **4037** | **3495** | **542** |

### Account/Date over MQ (VSAM-MQ) – `app-vsam-mq/`

| COBOL program | Total | Code | Comment |
| :--- | ---: | ---: | ---: |
| `COACCT01.cbl` | 620 | 601 | 19 |
| `CODATE01.cbl` | 524 | 508 | 16 |
| **Subtotal (2 programs)** | **1144** | **1109** | **35** |
