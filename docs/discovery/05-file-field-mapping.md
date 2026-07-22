# 05 – File (PF) to Field Mapping

For every physical file, the owning copybook that defines its record layout was traced from the `COPY` statements and `FD`/`01` structures in the programs that access it. Each field table lists the level number, field name, `PIC` clause, derived byte **length** and 1-based **offset** within the record. All record fields use `DISPLAY` usage (no `COMP`/`COMP-3`), so byte length equals the digit/character count; signed decimal fields (`S9(n)Vnn`) occupy one byte per digit with an implied (non-stored) decimal point and no separate sign byte.

| Physical file | Record copybook | 01 record | RECLN | VSAM key |
| :--- | :--- | :--- | ---: | :--- |
| `ACCTDATA` | `CVACT01Y.cpy` | `ACCOUNT-RECORD` | 300 | KEYS(11 0) — ACCT-ID |
| `CARDDATA` | `CVACT02Y.cpy` | `CARD-RECORD` | 150 | KEYS(16 0) — CARD-NUM; AIX on CARD-ACCT-ID |
| `CARDXREF` | `CVACT03Y.cpy` | `CARD-XREF-RECORD` | 50 | KEYS(16 0) — XREF-CARD-NUM; AIX on XREF-ACCT-ID |
| `CUSTDATA` | `CVCUS01Y.cpy` | `CUSTOMER-RECORD` | 500 | KEYS(9 0) — CUST-ID |
| `TRANSACT` | `CVTRA05Y.cpy` | `TRAN-RECORD` | 350 | KEYS(16 0) — TRAN-ID; AIX on TRAN-CARD-NUM |
| `TCATBALF` | `CVTRA01Y.cpy` | `TRAN-CAT-BAL-RECORD` | 50 | KEYS(17 0) — TRAN-CAT-KEY |
| `DISCGRP` | `CVTRA02Y.cpy` | `DIS-GROUP-RECORD` | 50 | KEYS(16 0) — DIS-GROUP-KEY |
| `TRANTYPE` | `CVTRA03Y.cpy` | `TRAN-TYPE-RECORD` | 60 | KEYS(2 0) — TRAN-TYPE |
| `TRANCATG` | `CVTRA04Y.cpy` | `TRAN-CAT-RECORD` | 60 | KEYS(6 0) — TRAN-CAT-KEY |
| `USRSEC` | `CSUSR01Y.cpy` | `SEC-USER-DATA` | 80 | KEYS(8 0) — SEC-USR-ID |

## `ACCTDATA` — `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

Record layout **`ACCOUNT-RECORD`** (copybook `app/cpy/CVACT01Y.cpy`, RECLN 300). Key: KEYS(11 0) — ACCT-ID.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `ACCOUNT-RECORD` | `*(group)*` |  |  |
| 05 | `ACCT-ID` | `9(11)` | 11 | 1 |
| 05 | `ACCT-ACTIVE-STATUS` | `X(01)` | 1 | 12 |
| 05 | `ACCT-CURR-BAL` | `S9(10)V99` | 12 | 13 |
| 05 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 | 25 |
| 05 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 12 | 37 |
| 05 | `ACCT-OPEN-DATE` | `X(10)` | 10 | 49 |
| 05 | `ACCT-EXPIRAION-DATE` | `X(10)` | 10 | 59 |
| 05 | `ACCT-REISSUE-DATE` | `X(10)` | 10 | 69 |
| 05 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 12 | 79 |
| 05 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 12 | 91 |
| 05 | `ACCT-ADDR-ZIP` | `X(10)` | 10 | 103 |
| 05 | `ACCT-GROUP-ID` | `X(10)` | 10 | 113 |
| 05 | `FILLER` | `X(178)` | 178 | 123 |

## `CARDDATA` — `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

Record layout **`CARD-RECORD`** (copybook `app/cpy/CVACT02Y.cpy`, RECLN 150). Key: KEYS(16 0) — CARD-NUM; AIX on CARD-ACCT-ID.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `CARD-RECORD` | `*(group)*` |  |  |
| 05 | `CARD-NUM` | `X(16)` | 16 | 1 |
| 05 | `CARD-ACCT-ID` | `9(11)` | 11 | 17 |
| 05 | `CARD-CVV-CD` | `9(03)` | 3 | 28 |
| 05 | `CARD-EMBOSSED-NAME` | `X(50)` | 50 | 31 |
| 05 | `CARD-EXPIRAION-DATE` | `X(10)` | 10 | 81 |
| 05 | `CARD-ACTIVE-STATUS` | `X(01)` | 1 | 91 |
| 05 | `FILLER` | `X(59)` | 59 | 92 |

## `CARDXREF` — `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

Record layout **`CARD-XREF-RECORD`** (copybook `app/cpy/CVACT03Y.cpy`, RECLN 50). Key: KEYS(16 0) — XREF-CARD-NUM; AIX on XREF-ACCT-ID.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `CARD-XREF-RECORD` | `*(group)*` |  |  |
| 05 | `XREF-CARD-NUM` | `X(16)` | 16 | 1 |
| 05 | `XREF-CUST-ID` | `9(09)` | 9 | 17 |
| 05 | `XREF-ACCT-ID` | `9(11)` | 11 | 26 |
| 05 | `FILLER` | `X(14)` | 14 | 37 |

## `CUSTDATA` — `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

Record layout **`CUSTOMER-RECORD`** (copybook `app/cpy/CVCUS01Y.cpy`, RECLN 500). Key: KEYS(9 0) — CUST-ID.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `CUSTOMER-RECORD` | `*(group)*` |  |  |
| 05 | `CUST-ID` | `9(09)` | 9 | 1 |
| 05 | `CUST-FIRST-NAME` | `X(25)` | 25 | 10 |
| 05 | `CUST-MIDDLE-NAME` | `X(25)` | 25 | 35 |
| 05 | `CUST-LAST-NAME` | `X(25)` | 25 | 60 |
| 05 | `CUST-ADDR-LINE-1` | `X(50)` | 50 | 85 |
| 05 | `CUST-ADDR-LINE-2` | `X(50)` | 50 | 135 |
| 05 | `CUST-ADDR-LINE-3` | `X(50)` | 50 | 185 |
| 05 | `CUST-ADDR-STATE-CD` | `X(02)` | 2 | 235 |
| 05 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | 3 | 237 |
| 05 | `CUST-ADDR-ZIP` | `X(10)` | 10 | 240 |
| 05 | `CUST-PHONE-NUM-1` | `X(15)` | 15 | 250 |
| 05 | `CUST-PHONE-NUM-2` | `X(15)` | 15 | 265 |
| 05 | `CUST-SSN` | `9(09)` | 9 | 280 |
| 05 | `CUST-GOVT-ISSUED-ID` | `X(20)` | 20 | 289 |
| 05 | `CUST-DOB-YYYY-MM-DD` | `X(10)` | 10 | 309 |
| 05 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | 10 | 319 |
| 05 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 1 | 329 |
| 05 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | 3 | 330 |
| 05 | `FILLER` | `X(168)` | 168 | 333 |

## `TRANSACT` — `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

Record layout **`TRAN-RECORD`** (copybook `app/cpy/CVTRA05Y.cpy`, RECLN 350). Key: KEYS(16 0) — TRAN-ID; AIX on TRAN-CARD-NUM.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `TRAN-RECORD` | `*(group)*` |  |  |
| 05 | `TRAN-ID` | `X(16)` | 16 | 1 |
| 05 | `TRAN-TYPE-CD` | `X(02)` | 2 | 17 |
| 05 | `TRAN-CAT-CD` | `9(04)` | 4 | 19 |
| 05 | `TRAN-SOURCE` | `X(10)` | 10 | 23 |
| 05 | `TRAN-DESC` | `X(100)` | 100 | 33 |
| 05 | `TRAN-AMT` | `S9(09)V99` | 11 | 133 |
| 05 | `TRAN-MERCHANT-ID` | `9(09)` | 9 | 144 |
| 05 | `TRAN-MERCHANT-NAME` | `X(50)` | 50 | 153 |
| 05 | `TRAN-MERCHANT-CITY` | `X(50)` | 50 | 203 |
| 05 | `TRAN-MERCHANT-ZIP` | `X(10)` | 10 | 253 |
| 05 | `TRAN-CARD-NUM` | `X(16)` | 16 | 263 |
| 05 | `TRAN-ORIG-TS` | `X(26)` | 26 | 279 |
| 05 | `TRAN-PROC-TS` | `X(26)` | 26 | 305 |
| 05 | `FILLER` | `X(20)` | 20 | 331 |

## `TCATBALF` — `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

Record layout **`TRAN-CAT-BAL-RECORD`** (copybook `app/cpy/CVTRA01Y.cpy`, RECLN 50). Key: KEYS(17 0) — TRAN-CAT-KEY.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `TRAN-CAT-BAL-RECORD` | `*(group)*` |  |  |
| 05 | `TRAN-CAT-KEY` | `*(group)*` |  |  |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`TRANCAT-ACCT-ID` | `9(11)` | 11 | 1 |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`TRANCAT-TYPE-CD` | `X(02)` | 2 | 12 |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`TRANCAT-CD` | `9(04)` | 4 | 14 |
| 05 | `TRAN-CAT-BAL` | `S9(09)V99` | 11 | 18 |
| 05 | `FILLER` | `X(22)` | 22 | 29 |

## `DISCGRP` — `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

Record layout **`DIS-GROUP-RECORD`** (copybook `app/cpy/CVTRA02Y.cpy`, RECLN 50). Key: KEYS(16 0) — DIS-GROUP-KEY.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `DIS-GROUP-RECORD` | `*(group)*` |  |  |
| 05 | `DIS-GROUP-KEY` | `*(group)*` |  |  |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`DIS-ACCT-GROUP-ID` | `X(10)` | 10 | 1 |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`DIS-TRAN-TYPE-CD` | `X(02)` | 2 | 11 |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`DIS-TRAN-CAT-CD` | `9(04)` | 4 | 13 |
| 05 | `DIS-INT-RATE` | `S9(04)V99` | 6 | 17 |
| 05 | `FILLER` | `X(28)` | 28 | 23 |

## `TRANTYPE` — `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

Record layout **`TRAN-TYPE-RECORD`** (copybook `app/cpy/CVTRA03Y.cpy`, RECLN 60). Key: KEYS(2 0) — TRAN-TYPE.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `TRAN-TYPE-RECORD` | `*(group)*` |  |  |
| 05 | `TRAN-TYPE` | `X(02)` | 2 | 1 |
| 05 | `TRAN-TYPE-DESC` | `X(50)` | 50 | 3 |
| 05 | `FILLER` | `X(08)` | 8 | 53 |

## `TRANCATG` — `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

Record layout **`TRAN-CAT-RECORD`** (copybook `app/cpy/CVTRA04Y.cpy`, RECLN 60). Key: KEYS(6 0) — TRAN-CAT-KEY.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `TRAN-CAT-RECORD` | `*(group)*` |  |  |
| 05 | `TRAN-CAT-KEY` | `*(group)*` |  |  |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`TRAN-TYPE-CD` | `X(02)` | 2 | 1 |
| 10 | &nbsp;&nbsp;&nbsp;&nbsp;`TRAN-CAT-CD` | `9(04)` | 4 | 3 |
| 05 | `TRAN-CAT-TYPE-DESC` | `X(50)` | 50 | 7 |
| 05 | `FILLER` | `X(04)` | 4 | 57 |

## `USRSEC` — `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

Record layout **`SEC-USER-DATA`** (copybook `app/cpy/CSUSR01Y.cpy`, RECLN 80). Key: KEYS(8 0) — SEC-USR-ID.

| Lvl | Field | PIC | Len | Offset |
| ---: | :--- | :--- | ---: | ---: |
| 01 | `SEC-USER-DATA` | `*(group)*` |  |  |
| 05 | `SEC-USR-ID` | `X(08)` | 8 | 1 |
| 05 | `SEC-USR-FNAME` | `X(20)` | 20 | 9 |
| 05 | `SEC-USR-LNAME` | `X(20)` | 20 | 29 |
| 05 | `SEC-USR-PWD` | `X(08)` | 8 | 49 |
| 05 | `SEC-USR-TYPE` | `X(01)` | 1 | 57 |
| 05 | `SEC-USR-FILLER` | `X(23)` | 23 | 58 |
