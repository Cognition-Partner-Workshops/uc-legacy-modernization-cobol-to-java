# CardDemo Dependency Map

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Scope:** Program call graph, copybook inclusion, CICS navigation, JCL job data lineage

---

## 1. Online CICS Navigation Flow (Call Graph)

The online system uses CICS `XCTL` (transfer control) and `RETURN TRANSID` for navigation between programs. The COMMAREA (`COCOM01Y`) carries session state across transfers.

```
                          ┌───────────────┐
                          │   COSGN00C    │
                          │  (Sign-On)    │
                          │  Tran: CC00   │
                          └───────┬───────┘
                                  │ XCTL
                    ┌─────────────┴──────────────┐
                    │                            │
              ┌─────┴──────┐              ┌──────┴──────┐
              │ COMEN01C   │              │  COADM01C   │
              │ (Main Menu)│              │ (Admin Menu)│
              │ Tran: CM00 │              │ Tran: CA90  │
              └─────┬──────┘              └──────┬──────┘
                    │ XCTL                       │ XCTL
       ┌────────┬───┴───┬─────────┐        ┌────┴─────┐
       │        │       │         │        │          │
  ┌────┴───┐┌───┴──┐┌───┴───┐┌───┴───┐┌───┴───┐┌────┴───┐
  │COACTVWC││COCRD-││COTRN- ││COBIL- ││COUSR- ││CORPT-  │
  │(AcctVw)││LIC   ││00C    ││00C    ││00C    ││00C     │
  │        ││(Card ││(Tran  ││(Bill  ││(User  ││(Report)│
  │        ││List) ││List)  ││Pay)   ││List)  ││        │
  └───┬────┘└──┬───┘└──┬────┘└───────┘└──┬───┘└────────┘
      │        │       │                  │
      │ XCTL   │XCTL   │ XCTL            │ XCTL
      v        v       v                  v
  ┌────────┐┌──────┐┌──────┐       ┌──────┐┌──────┐┌──────┐
  │COACTUPC││COCRD-││COTRN-│       │COUSR-││COUSR-││COUSR-│
  │(AcctUp)││SLC   ││01C   │       │01C   ││02C   ││03C   │
  │        ││(Card ││(Tran │       │(Add  ││(Upd  ││(Del  │
  │        ││View) ││View) │       │User) ││User) ││User) │
  └────────┘└──┬───┘└──┬───┘       └──────┘└──────┘└──────┘
               │       │
               │XCTL   │ XCTL
               v       v
           ┌──────┐┌──────┐
           │COCRD-││COTRN-│
           │UPC   ││02C   │
           │(Card ││(Tran │
           │Upd)  ││Add)  │
           └──────┘└──────┘
```

### Detailed CICS Program-to-Program Navigation

| From Program | To Program | Mechanism | Condition |
|-------------|-----------|-----------|-----------|
| COSGN00C | COMEN01C | XCTL | Regular user login successful |
| COSGN00C | COADM01C | XCTL | Admin user login successful |
| COMEN01C | COACTVWC | XCTL | Menu option: Account View |
| COMEN01C | COCRDLIC | XCTL | Menu option: Card List |
| COMEN01C | COTRN00C | XCTL | Menu option: Transaction List |
| COMEN01C | COBIL00C | XCTL | Menu option: Bill Payment |
| COMEN01C | CORPT00C | XCTL | Menu option: Reports |
| COADM01C | COUSR00C | XCTL | Admin menu: User List |
| COACTVWC | COACTUPC | XCTL | User selects account to update |
| COACTUPC | COACTVWC | XCTL | Return after update |
| COCRDLIC | COCRDSLC | XCTL | User selects card to view |
| COCRDSLC | COCRDUPC | XCTL | User selects card to update |
| COCRDUPC | COCRDSLC | XCTL | Return after update |
| COTRN00C | COTRN01C | XCTL | User selects transaction to view |
| COTRN00C | COTRN02C | XCTL | User selects add transaction |
| COUSR00C | COUSR01C | XCTL | Admin selects add user |
| COUSR00C | COUSR02C | XCTL | Admin selects update user |
| COUSR00C | COUSR03C | XCTL | Admin selects delete user |
| Any program | COSGN00C | XCTL | PF3 (back to login) |
| Any program | COMEN01C | XCTL | PF3 (back to main menu) |

---

## 2. Batch Program Call Graph

### Direct CALL Dependencies

```
CBSTM03A ──CALL──> CBSTM03B   (Statement file I/O subroutine)
CBSTM03A ──CALL──> CEE3ABD    (LE abend handler)

CBACT01C ──CALL──> COBDATFT   (Assembler: date formatting)
CBACT01C ──CALL──> CEE3ABD    (LE abend handler)

CBACT02C ──CALL──> CEE3ABD    (LE abend handler)
CBACT03C ──CALL──> CEE3ABD    (LE abend handler)
CBACT04C ──CALL──> CEE3ABD    (LE abend handler)
CBCUS01C ──CALL──> CEE3ABD    (LE abend handler)
CBEXPORT ──CALL──> CEE3ABD    (LE abend handler)
CBTRN01C ──CALL──> CEE3ABD    (LE abend handler)
CBTRN02C ──CALL──> CEE3ABD    (LE abend handler — via ABEND)
CBTRN03C ──CALL──> CEE3ABD    (LE abend handler)

CSUTLDTC ──CALL──> CEEDAYS   (LE date intrinsic: Lillian date conversion)
```

| Caller | Callee | Interface | Purpose |
|--------|--------|-----------|---------|
| CBSTM03A | CBSTM03B | CALL USING WS-M03B-AREA | File open/read/close for TRNX, XREF, CUST, ACCT files |
| CBACT01C | COBDATFT | CALL USING CODATECN-REC | Format account dates via assembler routine |
| CSUTLDTC | CEEDAYS | CALL USING date params | Convert dates to Lillian format for validation |
| All batch | CEE3ABD | CALL USING ABCODE, TIMING | Abnormal termination (abend) on fatal errors |

### Optional Module Call Dependencies

```
COACCT01 ──CALL──> MQOPEN, MQGET, MQPUT, MQCLOSE  (MQ API: account inquiry)
CODATE01 ──CALL──> MQOPEN, MQGET, MQPUT, MQCLOSE  (MQ API: date service)
```

---

## 3. Copybook Inclusion Map

### Which programs COPY which copybooks:

| Copybook | Included By (Programs) | Purpose |
|----------|----------------------|---------|
| **COCOM01Y** | COSGN00C, COMEN01C, COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COADM01C, COUSR00C-03C | COMMAREA structure |
| **COTTL01Y** | All online CICS programs (18) | Screen title header |
| **CSUSR01Y** | COSGN00C, COUSR00C-03C | User security record |
| **CVACT01Y** | COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBEXPORT, CBIMPORT, CBSTM03B, COACCT01 | Account record |
| **CVACT02Y** | COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT | Card record |
| **CVACT03Y** | COCRDLIC, CBACT03C, CBACT04C, CBTRN02C, CBEXPORT, CBIMPORT, CBSTM03A | Cross-reference record |
| **CVCUS01Y** | COACTUPC, CBCUS01C, CBEXPORT, CBIMPORT, CBSTM03B | Customer record |
| **CVTRA05Y** | COTRN00C, COTRN01C, COTRN02C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT | Transaction record |
| **CVTRA06Y** | CBTRN02C | Daily transaction record |
| **CVTRA01Y** | CBACT04C, CBTRN02C | Category balance record |
| **CVTRA02Y** | CBACT04C | Disclosure group record |
| **CVTRA03Y** | CBTRN03C | Transaction type reference |
| **CVTRA04Y** | CBTRN03C | Transaction category reference |
| **CVTRA07Y** | CBTRN03C | Report layout structures |
| **COSTM01** | CBSTM03A | Statement transaction layout |
| **CVEXPORT** | CBEXPORT, CBIMPORT | Export/import record format |
| **CVCRD01Y** | COCRDLIC, COCRDSLC, COCRDUPC, COTRTLIC, COTRTUPC | Card detail display record |
| **COMEN02Y** | COMEN01C, COADM01C | Menu option definitions |
| **COADM02Y** | COADM01C | Admin menu definitions |
| **CODATECN** | CBACT01C | Date conversion (assembler IF) |
| **CSLKPCDY** | COACTUPC | Lookup code tables |
| **CSUTLDPY** | COACTUPC | Date utility parameters |
| **CSUTLDWY** | COACTUPC, COCRDUPC, COTRTUPC | Date utility working storage |
| **CSDAT01Y** | COTRN02C, COTRTLIC, COTRTUPC | Date structures |
| **CSMSG01Y** | Most online programs | Short message area |
| **CSMSG02Y** | Most online programs | Extended message area |
| **CSSETATY** | Most online programs | Screen attribute setting |
| **CSSTRPFY** | Most online programs | String parse/format |

---

## 4. VSAM File Access by Program

### Online Programs → VSAM Files

| Program | USRSEC | ACCTDATA | CARDDATA | CARDXREF | TRANSACT | CUSTDATA | TCATBAL |
|---------|--------|----------|----------|----------|----------|----------|---------|
| COSGN00C | R | | | | | | |
| COACTVWC | | R | | R | | R | |
| COACTUPC | | RW | R | R | | RW | |
| COCRDLIC | | | R | R | | | |
| COCRDSLC | | | R | R | | | |
| COCRDUPC | | | RW | R | | | |
| COTRN00C | | | | R | R | | |
| COTRN01C | | | | | R | | |
| COTRN02C | | R | | R | W | | W |
| COBIL00C | | RW | | R | W | | |
| COUSR00C | R | | | | | | |
| COUSR01C | RW | | | | | | |
| COUSR02C | RW | | | | | | |
| COUSR03C | RD | | | | | | |

**Legend:** R=Read, W=Write, RW=Read/Write (REWRITE), RD=Read/Delete

### Batch Programs → Files

| Program | Input Files | Output Files | I/O Files |
|---------|------------|--------------|-----------|
| CBACT01C | ACCTDATA (VSAM) | OUTFILE, ARRYFILE, VBRCFILE (seq) | -- |
| CBACT02C | CARDDATA (VSAM) | -- | -- |
| CBACT03C | CARDXREF (VSAM) | -- | -- |
| CBACT04C | TCATBAL, CARDXREF, DISCGRP (VSAM) | TRANSACT (seq) | ACCTDATA (VSAM) |
| CBCUS01C | CUSTDATA (VSAM) | -- | -- |
| CBTRN01C | TRANSACT (VSAM) | -- | -- |
| CBTRN02C | DALYTRAN (seq), CARDXREF (VSAM) | TRANSACT (VSAM), DALYREJS (seq) | ACCTDATA, TCATBAL (VSAM) |
| CBTRN03C | TRANSACT, TRANTYPE, TRANCATG (VSAM) | DALYREPT (seq) | -- |
| CBSTM03A | -- | STMTFILE, HTMLFILE (seq) | -- |
| CBSTM03B | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (VSAM) | -- | -- |
| CBEXPORT | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA (VSAM) | EXPFILE (seq) | -- |
| CBIMPORT | EXPFILE (seq) | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA (VSAM) | -- |

---

## 5. JCL Job → Program → File Data Lineage

### Core Batch Cycle (execution order)

```
Step 1: CLOSEFIL ──> Closes CICS files for batch window
            │
Step 2: Data Refresh (parallel)
            ├── ACCTFILE  ──[IDCAMS REPRO]──> ACCTDATA.VSAM.KSDS
            ├── CARDFILE  ──[IDCAMS REPRO]──> CARDDATA.VSAM.KSDS
            ├── CUSTFILE  ──[IDCAMS REPRO]──> CUSTDATA.VSAM.KSDS
            ├── XREFFILE  ──[IDCAMS REPRO]──> CARDXREF.VSAM.KSDS (+AIX)
            ├── TRANFILE  ──[IDCAMS REPRO]──> TRANSACT.VSAM.KSDS
            └── DUSRSECJ  ──[IDCAMS REPRO]──> USRSEC.VSAM.KSDS
            │
Step 3: POSTTRAN ──[CBTRN02C]──> DALYTRAN(in) → TRANSACT(out) + DALYREJS(rejects)
            │                     Also updates: ACCTDATA, TCATBAL
            │
Step 4: INTCALC ──[CBACT04C]──> TCATBAL + XREF + DISCGRP(in) → ACCTDATA(update) + TRANSACT(new)
            │
Step 5: TRANBKP ──[IDCAMS REPRO]──> TRANSACT.VSAM → TRANSACT.BACKUP (GDG)
            │
Step 6: COMBTRAN ──[SORT]──> Merge/combine transaction files
            │
Step 7: CREASTMT ──[SORT + CBSTM03A]──> TRANSACT → STATEMNT.PS + STATEMNT.HTML
            │
Step 8: TRANIDX ──[IDCAMS]──> Build alternate indexes on TRANSACT
            │
Step 9: OPENFIL ──> Reopens CICS files after batch completes
```

### Detailed JCL Job Data Lineage

| JCL Job | Step(s) | Reads | Writes | Utility/Program |
|---------|---------|-------|--------|-----------------|
| ACCTFILE | DEFINE, REPRO | ACCTDATA.PS (flat) | ACCTDATA.VSAM.KSDS | IDCAMS |
| CARDFILE | DEFINE, REPRO | CARDDATA.PS (flat) | CARDDATA.VSAM.KSDS | IDCAMS |
| CUSTFILE | DEFINE, REPRO | CUSTDATA.PS (flat) | CUSTDATA.VSAM.KSDS | IDCAMS |
| XREFFILE | DEFINE, REPRO, AIX | CARDXREF.PS (flat) | CARDXREF.VSAM.KSDS + AIX | IDCAMS |
| TRANFILE | DEFINE, REPRO, AIX | TRANSACT.PS (flat) | TRANSACT.VSAM.KSDS + AIX | IDCAMS |
| DUSRSECJ | DEFINE, REPRO | USRSEC.PS (flat) | USRSEC.VSAM.KSDS | IDCAMS |
| POSTTRAN | RUN | DALYTRAN, XREF, ACCT, TCATBAL | TRANSACT, DALYREJS, ACCT, TCATBAL | CBTRN02C |
| INTCALC | RUN | TCATBAL, XREF, DISCGRP, ACCT | ACCT (update), TRANSACT (interest txn) | CBACT04C |
| COMBTRAN | SORT | Multiple transaction files | Combined transaction file | SORT |
| CREASTMT | SORT, RUN | TRANSACT, XREF, CUST, ACCT | STATEMNT.PS, STATEMNT.HTML | SORT, CBSTM03A |
| TRANBKP | REPRO | TRANSACT.VSAM | TRANSACT.BACKUP (GDG) | IDCAMS |
| TRANREPT | RUN | TRANSACT, TRANTYPE, TRANCATG | DALYREPT | CBTRN03C |
| CBEXPORT | RUN | All 5 VSAM masters | EXPFILE (sequential) | CBEXPORT |
| CBIMPORT | RUN | EXPFILE (sequential) | All 5 VSAM masters | CBIMPORT |
| CLOSEFIL | CEMT | -- | -- | DFHCSDUP |
| OPENFIL | CEMT | -- | -- | DFHCSDUP |
| TXT2PDF1 | CONVERT | STATEMNT.PS | STATEMNT.PS.PDF | TXT2PDF |

---

## 6. Data Flow Diagram (End-to-End)

```
 ┌─────────────────────────────────────────────────────────────────┐
 │                    ONLINE (CICS) LAYER                          │
 │                                                                 │
 │  User ──> COSGN00C ──> COMEN01C ──> Business Programs          │
 │                                         │                       │
 │  COACTVWC/COACTUPC ←──→ ACCTDATA, CUSTDATA, CARDXREF          │
 │  COCRDLIC/SLC/UPC  ←──→ CARDDATA, CARDXREF                    │
 │  COTRN00C/01C/02C  ←──→ TRANSACT, CARDXREF, ACCTDATA, TCATBAL │
 │  COBIL00C          ←──→ ACCTDATA, CARDXREF, TRANSACT          │
 │  COUSR00C-03C      ←──→ USRSEC                                │
 │  CORPT00C          ──→  Report parameters → batch trigger      │
 └──────────────────────────────────┬──────────────────────────────┘
                                    │
                              CLOSEFIL (batch window)
                                    │
 ┌──────────────────────────────────┴──────────────────────────────┐
 │                    BATCH LAYER                                   │
 │                                                                  │
 │  DALYTRAN ──[CBTRN02C]──> TRANSACT + ACCTDATA + TCATBAL        │
 │                              │                                   │
 │  TCATBAL + DISCGRP ──[CBACT04C]──> ACCTDATA (interest applied) │
 │                              │                                   │
 │  TRANSACT ──[SORT+CBSTM03A]──> Statements (text + HTML + PDF)  │
 │  TRANSACT ──[CBTRN03C]──> Daily Transaction Report              │
 │  TRANSACT ──[IDCAMS]──> Backup (GDG)                           │
 │                                                                  │
 │  All VSAM ──[CBEXPORT]──> Export File ──[CBIMPORT]──> All VSAM │
 └──────────────────────────────────┬──────────────────────────────┘
                                    │
                              OPENFIL (reopen for CICS)
```

---

## 7. External Dependencies

| Dependency | Type | Used By | Purpose |
|-----------|------|---------|---------|
| CEEDAYS | LE Intrinsic | CSUTLDTC | Lillian date conversion |
| CEE3ABD | LE Intrinsic | All batch programs | Abnormal end (abend) handling |
| COBDATFT | Assembler | CBACT01C | Date formatting |
| MVSWAIT | Assembler | COBSWAIT | MVS wait/delay |
| DFHCSDUP | CICS Utility | CLOSEFIL/OPENFIL JCL | CICS file open/close |
| IDCAMS | MVS Utility | Most JCL jobs | VSAM define/delete/repro/print |
| SORT (DFSORT) | MVS Utility | COMBTRAN, CREASTMT | Record sorting and reformatting |
| IEBGENER | MVS Utility | INTRDRJ1 | Sequential dataset copy |
| IEFBR14 | MVS Utility | CREASTMT | Dummy step (file allocation/deletion) |
| TXT2PDF | ISV Tool | TXT2PDF1 JCL | Text to PDF conversion |
| FTP | MVS Utility | FTPJCL | File transfer |
| MQOPEN/MQGET/MQPUT/MQCLOSE | MQ API | COACCT01, CODATE01 | MQ messaging (optional module) |
