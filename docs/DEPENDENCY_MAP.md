# CardDemo Dependency Map

> **Generated for**: Mainframe-to-Java Modernization Assessment
> **Application**: CardDemo - Credit Card Management System
> **Analysis**: Call graph, CICS transfer control, copybook inclusion, and data lineage

---

## Table of Contents

1. [Online Program Call Graph (CICS)](#online-program-call-graph-cics)
2. [Batch Program Call Graph](#batch-program-call-graph)
3. [CICS Transaction Flow](#cics-transaction-flow)
4. [Copybook Dependency Matrix](#copybook-dependency-matrix)
5. [VSAM File Access Matrix](#vsam-file-access-matrix)
6. [JCL Job → Program Execution Map](#jcl-job--program-execution-map)
7. [JCL Job → Dataset Lineage](#jcl-job--dataset-lineage)
8. [Batch Processing Pipeline](#batch-processing-pipeline)
9. [End-to-End Data Flow](#end-to-end-data-flow)

---

## Online Program Call Graph (CICS)

All online programs communicate via CICS `XCTL` (transfer control) passing a shared COMMAREA (`COCOM01Y`). No direct `CALL` statements exist between online programs.

```
                        ┌─────────────┐
                        │  COSGN00C   │  ← Entry point (Txn CC00)
                        │  (Sign-on)  │
                        └──────┬──────┘
                               │ XCTL (on successful login)
                    ┌──────────┴──────────┐
                    │                     │
              ┌─────┴─────┐         ┌─────┴─────┐
              │ COMEN01C  │         │ COADM01C  │
              │(Main Menu)│         │(Admin Menu)│
              │  Txn CM00 │         │  Txn CA00  │
              └─────┬─────┘         └─────┬─────┘
                    │                     │
        ┌───────┬───┴───┬────────┐    ┌───┴──────────┐
        │       │       │        │    │              │
   ┌────┴──┐┌──┴───┐┌──┴──┐┌───┴──┐┌┴─────┐  ┌────┴──┐
   │COACTV ││COCRD ││COTRN││COBIL ││COUSR │  │COUSR  │
   │  WC   ││ LIC  ││ 00C ││ 00C  ││ 00C  │  │ 01C   │
   │(View  ││(Card ││(Txn ││(Bill ││(List │  │(Add   │
   │ Acct) ││ List)││List) ││ Pay) ││Users)│  │ User) │
   └───┬───┘└──┬───┘└──┬──┘└──────┘└──┬───┘  └───────┘
       │       │       │               │
  ┌────┴──┐┌──┴───┐┌──┴──┐      ┌────┴──┐  ┌───────┐
  │COACTU ││COCRD ││COTRN│      │COUSR  │  │COUSR  │
  │  PC   ││ SLC  ││ 01C │      │ 02C   │  │ 03C   │
  │(Update││(Card ││(View│      │(Update│  │(Delete│
  │ Acct) ││Detail)││ Txn)│      │ User) │  │ User) │
  └───────┘└──┬───┘└──┬──┘      └───────┘  └───────┘
              │       │
         ┌────┴──┐┌──┴───┐
         │COCRD  ││COTRN │
         │ UPC   ││ 02C  │
         │(Update││(Add  │
         │ Card) ││ Txn) │
         └───────┘└──┬───┘
                     │
                     │ CALL (not XCTL)
                     ▼
               ┌───────────┐
               │ CSUTLDTC  │
               │(Date Util)│
               └─────┬─────┘
                     │ CALL
                     ▼
               ┌───────────┐
               │ CEEDAYS   │
               │(LE Runtime)│
               └───────────┘
```

### XCTL Transfer Details

| Source Program | Target Program(s) | Trigger | Direction |
|---------------|-------------------|---------|-----------|
| COSGN00C | COMEN01C | Regular user login | Forward |
| COSGN00C | COADM01C | Admin user login | Forward |
| COMEN01C | COACTVWC, COCRDLIC, COTRN00C, COBIL00C, CORPT00C | Menu option selected | Forward |
| COMEN01C | COSGN00C | PF3 (back to sign-on) | Backward |
| COADM01C | COUSR00C, COUSR01C | Admin menu option | Forward |
| COADM01C | COMEN01C | PF3 (back to main menu) | Backward |
| COACTVWC | COACTUPC | Select account to update | Forward |
| COACTVWC | COMEN01C | PF3 (back to menu) | Backward |
| COACTUPC | COMEN01C | PF3 (back to menu) | Backward |
| COCRDLIC | COCRDSLC | Select card to view | Forward |
| COCRDLIC | COCRDUPC | Select card to update | Forward |
| COCRDLIC | COMEN01C | PF3 (back to menu) | Backward |
| COCRDSLC | COCRDUPC | Navigate to update | Forward |
| COCRDSLC | COMEN01C | PF3 (back to menu) | Backward |
| COCRDUPC | COMEN01C | PF3 (back to menu) | Backward |
| COTRN00C | COTRN01C | Select transaction to view | Forward |
| COTRN00C | COTRN02C | Select to add new | Forward |
| COTRN00C | COMEN01C | PF3 (back to menu) | Backward |
| COTRN01C | COMEN01C | PF3 (back to menu) | Backward |
| COTRN02C | COMEN01C | PF3 (back to menu) | Backward |
| COBIL00C | COMEN01C | PF3 (back to menu) | Backward |
| CORPT00C | COMEN01C | PF3 (back to menu) | Backward |
| COUSR00C | COUSR01C | Add new user | Forward |
| COUSR00C | COUSR02C | Select user to update | Forward |
| COUSR00C | COUSR03C | Select user to delete | Forward |
| COUSR00C | COADM01C | PF3 (back to admin menu) | Backward |
| COUSR01C | COADM01C | PF3 (back to admin menu) | Backward |
| COUSR02C | COADM01C | PF3 (back to admin menu) | Backward |
| COUSR03C | COADM01C | PF3 (back to admin menu) | Backward |

### Direct CALL Dependencies (Subroutine Calls)

| Caller | Callee | Purpose | Linkage Data |
|--------|--------|---------|--------------|
| COTRN02C | CSUTLDTC | Validate/convert date input | CSUTLDTC-DATE |
| CORPT00C | CSUTLDTC | Validate report date range | CSUTLDTC-DATE |
| CSUTLDTC | CEEDAYS | LE date conversion service | Date structure |
| CBACT01C | COBDATFT | Format date for printing | CODATECN-REC |
| CBSTM03A | CBSTM03B | File I/O for statement gen | WS-M03B-AREA |
| COBSWAIT | MVSWAIT | System wait (assembler) | MVSWAIT-TIME |
| CBACT01C–04C, CBTRN01C–03C, CBCUS01C, CBSTM03A | CEE3ABD | LE abnormal termination | ABCODE, TIMING |

---

## Batch Program Call Graph

```
┌─────────────────────────────────────────────────────────┐
│                  BATCH JOB EXECUTION                     │
└─────────────────────────────────────────────────────────┘

  POSTTRAN.jcl                INTCALC.jcl
       │                           │
       ▼                           ▼
  ┌─────────┐                ┌─────────┐
  │CBTRN02C │                │CBACT04C │
  │(Post    │                │(Interest│
  │ Trans)  │                │ Calc)   │
  └─────────┘                └────┬────┘
                                  │ CALL
                                  ▼
                            ┌───────────┐
                            │ CEE3ABD   │
                            │(Abort Svc)│
                            └───────────┘

  CREASTMT.JCL                TRANREPT.jcl
       │                           │
       ▼                           ▼
  ┌─────────┐                ┌─────────┐
  │CBSTM03A │                │CBTRN03C │
  │(Stmt    │                │(Tran    │
  │ Gen)    │                │ Report) │
  └────┬────┘                └─────────┘
       │ CALL (10x)
       ▼
  ┌─────────┐
  │CBSTM03B │
  │(File I/O│
  │ Handler)│
  └─────────┘

  READACCT.jcl    READCARD.jcl    READCUST.jcl    READXREF.jcl
       │               │               │               │
       ▼               ▼               ▼               ▼
  ┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
  │CBACT01C │    │CBACT02C │    │CBCUS01C │    │CBACT03C │
  │(Read    │    │(Read    │    │(Read    │    │(Read    │
  │ Accts)  │    │ Cards)  │    │ Custs)  │    │ XRef)   │
  └────┬────┘    └─────────┘    └─────────┘    └─────────┘
       │ CALL
       ▼
  ┌─────────┐
  │COBDATFT │
  │(Asm Date│
  │ Format) │
  └─────────┘

  CBEXPORT.jcl               CBIMPORT.jcl
       │                           │
       ▼                           ▼
  ┌─────────┐                ┌─────────┐
  │CBEXPORT │                │CBIMPORT │
  │(Export  │                │(Import  │
  │ Data)   │                │ Data)   │
  └─────────┘                └─────────┘

  WAITSTEP.jcl
       │
       ▼
  ┌─────────┐    CALL     ┌─────────┐
  │COBSWAIT │────────────▶│MVSWAIT  │
  │(Wait)   │             │(Asm)    │
  └─────────┘             └─────────┘
```

---

## CICS Transaction Flow

```
User Terminal (3270)
       │
       ▼
  ┌─────────┐
  │  CC00   │ ← Initial CICS transaction
  │COSGN00C │
  └────┬────┘
       │ Reads: USRSEC (authenticate)
       │ XCTL → CM00 or CA00
       ▼
  ┌─────────┐                    ┌─────────┐
  │  CM00   │                    │  CA00   │
  │COMEN01C │                    │COADM01C │
  └────┬────┘                    └────┬────┘
       │                              │
  ┌────┴─────────────────┐     ┌──────┴───────┐
  │    │    │    │    │   │     │              │
  ▼    ▼    ▼    ▼    ▼   ▼     ▼              ▼
 CA01 CC01 CT00 CB00 CR00     CU00          CU01
 View Card  Txn Bill Rpt      User          Add
 Acct List  List Pay  Req     List          User
  │    │    │              │    │
  ▼    ▼    ▼              ▼    ▼
 CA02 CC02 CT01           CU02  CU03
 Updt Card  View          Updt  Del
 Acct Sel   Txn           User  User
       │    │
       ▼    ▼
      CC03 CT02
      Updt Add
      Card  Txn
```

---

## Copybook Dependency Matrix

Shows which copybooks are included (`COPY`) by each COBOL program.

### Online Programs

| Copybook | COSGN00C | COMEN01C | COADM01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | CORPT00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| COCOM01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| COTTL01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| CSDAT01Y | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| CSMSG01Y | ● | ● | | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| DFHAID | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| DFHBMSCA | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● | ● |
| CSUSR01Y | ● | ● | | | ● | | | ● | | | | | | | ● | ● | ● |
| CSMSG02Y | | | | | ● | | | ● | | | | | | | | | |
| COMEN02Y | | ● | | | | | | | | | | | | | | | |
| COADM02Y | | | ● | | | | | | | | | | | | | | |
| CVACT01Y | | | | ● | ● | | | | | | ● | | | | | | |
| CVACT02Y | | | | | | | | ● | | | | | | | | | |
| CVACT03Y | | | | ● | ● | | | | | | ● | | | | | | |
| CVCUS01Y | | | | ● | ● | | | ● | | | | | | | | | |
| CVCRD01Y | | | | | ● | ● | ● | ● | | | | | | | | | |
| CVTRA05Y | | | | | | | | | ● | | ● | | ● | | | | |
| CSLKPCDY | | | | | ● | | | | | | | | | | | | |
| CSSETATY | | | | | ● | | | | | | | | | | | | |
| CSSTRPFY | | | | | ● | | ● | | | | | | | | | | |
| CSUTLDPY | | | | | ● | | | | | | | | | | | | |
| CSUTLDWY | | | | | ● | | | | | | | | | | | | |

### Batch Programs

| Copybook | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| CVACT01Y | ● | | | ● | | | | | ● | | ● | |
| CVACT02Y | | ● | | | | | | | | | ● | |
| CVACT03Y | | | ● | ● | | | | ● | ● | | ● | |
| CVCUS01Y | | | | | ● | | | | | | ● | |
| CVTRA05Y | | | | | | | | ● | | | ● | |
| CVTRA06Y | | | | | | ● | ● | | | | | |
| CVTRA03Y | | | | | | | | ● | | | | |
| CVTRA04Y | | | | | | | | ● | | | | |
| CVTRA07Y | | | | | | | | ● | | | | |
| COSTM01 | | | | | | | | | ● | | | |
| CUSTREC | | | | | | | | | ● | | | |
| CODATECN | ● | | | | | | | | | | | |
| CVEXPORT | | | | | | | | | | | ● | ● |

---

## VSAM File Access Matrix

Shows which programs read (R), write (W), rewrite (U), delete (D), or browse (B) each VSAM file.

### Online Programs (via CICS)

| VSAM File | COSGN00C | COMEN01C | COACTVWC | COACTUPC | COCRDLIC | COCRDSLC | COCRDUPC | COTRN00C | COTRN01C | COTRN02C | COBIL00C | CORPT00C | COUSR00C | COUSR01C | COUSR02C | COUSR03C |
|-----------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| USRSEC | R | | | | | | | | | | | | B | W | RU | RD |
| ACCTDATA | | | R | RU | | | | | | R | R | | | | | |
| CARDDATA | | | | | B | R | RU | | | | | | | | | |
| CARDXREF | | | R | R | | R | R | | | R | R | | | | | |
| CUSTDATA | | | R | RU | | | R | | | | | | | | | |
| TRANSACT | | | | | | | | B | R | RW | RW | | | | | |
| TRANTYPE | | | | | | | | | | | | | | | | |
| TRANCATG | | | | | | | | | | | | | | | | |
| TCATBAL | | | | | | | | | | | | | | | | |

**Legend**: R=Read, W=Write, U=Rewrite/Update, D=Delete, B=Browse (STARTBR/READNEXT/READPREV)

### Batch Programs (via File I/O)

| VSAM File / Dataset | CBACT01C | CBACT02C | CBACT03C | CBACT04C | CBCUS01C | CBTRN01C | CBTRN02C | CBTRN03C | CBSTM03A | CBSTM03B | CBEXPORT | CBIMPORT |
|---------------------|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|:--------:|
| ACCTDATA | R | | | RW | | R | RW | | R | | R | |
| CARDDATA | | R | | | | R | | | | | R | |
| CARDXREF | | | R | RW | | R | | R | R | | R | |
| CUSTDATA | | | | | R | R | | | R | | R | |
| TRANSACT | | | | | | R | RW | R | | | R | |
| DALYTRAN | | | | | | R | R | | | | | |
| TCATBAL | | | | RW | | | RW | | | | | |
| DISCGRP | | | | R | | | | | | | | |
| TRANTYPE | | | | | | | | R | | | | |
| TRANCATG | | | | | | | | R | | | | |
| DALYREJS | | | | | | | W | | | | | |
| TRNXFILE | | | | | | | | | | R | | |
| STMTFILE | | | | | | | | | W | | | |
| HTMLFILE | | | | | | | | | W | | | |
| TRANREPT | | | | | | | | W | | | | |
| EXPFILE | | | | | | | | | | | W | R |
| CUSTOUT | | | | | | | | | | | | W |
| ACCTOUT | | | | | | | | | | | | W |
| XREFOUT | | | | | | | | | | | | W |
| TRNXOUT | | | | | | | | | | | | W |
| ERROUT | | | | | | | | | | | | W |

**Legend**: R=Read (INPUT), W=Write (OUTPUT), RW=Read+Write (I-O)

---

## JCL Job → Program Execution Map

| JCL Job | Step | Program Executed | Type | Purpose |
|---------|------|-----------------|------|---------|
| POSTTRAN | STEP15 | CBTRN02C | COBOL | Post daily transactions |
| INTCALC | STEP15 | CBACT04C | COBOL | Calculate interest |
| CREASTMT | STEP010 | SORT | Utility | Sort transactions by card |
| CREASTMT | STEP020 | IDCAMS | Utility | REPRO into VSAM |
| CREASTMT | STEP040 | CBSTM03A | COBOL | Generate statements |
| TRANREPT | STEP05R | SORT | Utility | Sort transactions |
| TRANREPT | STEP10R | CBTRN03C | COBOL | Generate transaction report |
| COMBTRAN | STEP05R | SORT | Utility | Merge transaction files |
| COMBTRAN | STEP10 | IDCAMS | Utility | REPRO merged data into VSAM |
| READACCT | STEP05 | CBACT01C | COBOL | Read/print accounts |
| READCARD | STEP05 | CBACT02C | COBOL | Read/print cards |
| READCUST | STEP05 | CBCUS01C | COBOL | Read/print customers |
| READXREF | STEP05 | CBACT03C | COBOL | Read/print cross-ref |
| CBEXPORT | STEP02 | CBEXPORT | COBOL | Export all data |
| CBIMPORT | STEP01 | CBIMPORT | COBOL | Import all data |
| WAITSTEP | WAIT | COBSWAIT | COBOL | Wait for delay |
| TRANBKP | STEP10 | IDCAMS REPRO | Utility | Backup transactions |
| PRTCATBL | STEP10R | SORT | Utility | Sort/print category balances |
| ACCTFILE | multiple | IDCAMS | Utility | Delete/define/load account VSAM |
| CARDFILE | multiple | IDCAMS | Utility | Delete/define/load card VSAM |
| CUSTFILE | multiple | IDCAMS | Utility | Delete/define/load customer VSAM |
| TRANFILE | multiple | IDCAMS | Utility | Delete/define/load transaction VSAM |
| XREFFILE | multiple | IDCAMS | Utility | Delete/define/load cross-ref VSAM |
| DUSRSECJ | multiple | IEBGENER + IDCAMS | Utility | Load user security VSAM |
| TRANTYPE | multiple | IDCAMS | Utility | Define/load transaction types |
| TRANCATG | multiple | IDCAMS | Utility | Define/load transaction categories |
| TCATBALF | multiple | IDCAMS | Utility | Define/load category balances |
| DISCGRP | multiple | IDCAMS | Utility | Define/load disclosure groups |
| CLOSEFIL | CLCIFIL | SDSF | Utility | Close CICS files |
| OPENFIL | OPCIFIL | SDSF | Utility | Open CICS files |
| TRANIDX | multiple | IDCAMS | Utility | Define alternate index on TRANSACT |
| DEFGDGB | STEP05 | IDCAMS | Utility | Define GDG base |
| DEFGDGD | multiple | IDCAMS + IEBGENER | Utility | Define GDG datasets + backup |
| CBADMCDJ | STEP1 | DFHCSDUP | CICS Utility | Load CSD definitions |
| TXT2PDF1 | TXT2PDF | IKJEFT1B | TSO | Convert text to PDF |
| FTPJCL | STEP1 | FTP | System | FTP file transfer |

---

## JCL Job → Dataset Lineage

Shows which datasets each JCL job reads from and writes to.

| JCL Job | Input Datasets (Reads) | Output Datasets (Writes) |
|---------|----------------------|------------------------|
| **POSTTRAN** | DALYTRAN.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, TCATBAL.VSAM.KSDS | TRANSACT.VSAM.KSDS (append), DALYREJS, ACCTDATA (update), TCATBAL (update) |
| **INTCALC** | TCATBAL.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, DISCGRP.VSAM.KSDS | TRANSACT (new interest txns), TCATBAL (update), ACCTDATA (update) |
| **COMBTRAN** | TRANSACT.VSAM.KSDS, DALYTRAN sequential | TRANSACT.VSAM.KSDS (merged) |
| **CREASTMT** | TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, ACCTDATA.VSAM.KSDS, CUSTDATA.VSAM.KSDS | STATEMNT.PS (text), STATEMNT.HTML (HTML) |
| **TRANREPT** | TRANSACT.VSAM.KSDS, CARDXREF.VSAM.KSDS, TRANTYPE.VSAM.KSDS, TRANCATG.VSAM.KSDS | TRANREPT.PS (report) |
| **TRANBKP** | TRANSACT.VSAM.KSDS | TRANSACT.BKUP.SEQ |
| **CBEXPORT** | CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA (all VSAM) | EXPFILE (flat export) |
| **CBIMPORT** | EXPFILE (flat export) | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, ERROUT |
| **ACCTFILE** | ACCTDATA.PS (flat file) | ACCTDATA.VSAM.KSDS |
| **CARDFILE** | CARDDATA.PS (flat file) | CARDDATA.VSAM.KSDS |
| **CUSTFILE** | CUSTDATA.PS (flat file) | CUSTDATA.VSAM.KSDS |
| **TRANFILE** | TRANSACT.PS (flat file) | TRANSACT.VSAM.KSDS |
| **XREFFILE** | CARDXREF.PS (flat file) | CARDXREF.VSAM.KSDS |
| **DUSRSECJ** | USRSEC.PS (flat file) | USRSEC.VSAM.KSDS |

---

## Batch Processing Pipeline

The nightly batch cycle follows this strict execution order:

```
Phase 1: CLOSE CICS FILES
┌──────────┐
│ CLOSEFIL │ Close all CICS-managed VSAM files
└────┬─────┘
     │
Phase 2: DATA REFRESH (if needed)
     ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│ ACCTFILE │ │ CARDFILE │ │ CUSTFILE │ │ TRANFILE │ │ XREFFILE │
│(Refresh  │ │(Refresh  │ │(Refresh  │ │(Refresh  │ │(Refresh  │
│ Accounts)│ │ Cards)   │ │ Custs)   │ │ Trans)   │ │ XRef)    │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
     │             │             │             │             │
Phase 3: CORE BATCH PROCESSING
     └─────────────┴─────────────┴──────┬──────┴─────────────┘
                                        ▼
                                  ┌──────────┐
                                  │ POSTTRAN │ Post daily transactions
                                  │ CBTRN02C │ to master file
                                  └────┬─────┘
                                       │
                                       ▼
                                  ┌──────────┐
                                  │ INTCALC  │ Calculate interest on
                                  │ CBACT04C │ all accounts
                                  └────┬─────┘
                                       │
Phase 4: BACKUP & MERGE
                                       ▼
                                  ┌──────────┐
                                  │ TRANBKP  │ Backup transaction
                                  │ REPRO    │ master
                                  └────┬─────┘
                                       │
                                       ▼
                                  ┌──────────┐
                                  │ COMBTRAN │ Merge daily + master
                                  │ SORT     │ transactions
                                  └────┬─────┘
                                       │
Phase 5: REPORTING & STATEMENTS
                                       ▼
                            ┌──────────┐   ┌──────────┐
                            │ CREASTMT │   │ TRANREPT │
                            │ CBSTM03A │   │ CBTRN03C │
                            │(Stmts)   │   │(Reports) │
                            └────┬─────┘   └────┬─────┘
                                 │               │
Phase 6: INDEX REBUILD
                                 └───────┬───────┘
                                         ▼
                                   ┌──────────┐
                                   │ TRANIDX  │ Rebuild alternate
                                   │ IDCAMS   │ indexes
                                   └────┬─────┘
                                        │
Phase 7: REOPEN CICS FILES
                                        ▼
                                  ┌──────────┐
                                  │ OPENFIL  │ Reopen CICS-managed
                                  │          │ VSAM files
                                  └──────────┘
```

---

## End-to-End Data Flow

### Transaction Lifecycle

```
1. ONLINE ENTRY                    2. STAGING                    3. BATCH POSTING
┌──────────┐                  ┌──────────────┐            ┌──────────────┐
│ COTRN02C │ ──CICS WRITE──▶ │  TRANSACT    │            │  POSTTRAN    │
│(Add Txn) │                  │  VSAM KSDS   │◀───────────│  CBTRN02C   │
└──────────┘                  └──────────────┘            └──────┬───────┘
                                                                 │
                              ┌──────────────┐                   │ Updates
                              │  DALYTRAN    │───────────────────┘
                              │  (Daily Txns)│   Reads daily txns
                              └──────────────┘
                                                           ┌──────────────┐
                                     Also Updates: ───────▶│  ACCTDATA    │
                                                           │  (Balances)  │
                                                           └──────────────┘
                                                           ┌──────────────┐
                                                    ──────▶│  TCATBAL     │
                                                           │  (Cat Bals)  │
                                                           └──────────────┘

4. INTEREST CALC                   5. REPORTING                  6. STATEMENTS
┌──────────┐                  ┌──────────────┐            ┌──────────────┐
│ INTCALC  │ reads DISCGRP   │  TRANREPT    │            │  CREASTMT    │
│ CBACT04C │ + TCATBAL       │  CBTRN03C    │            │  CBSTM03A   │
│          │ writes interest  │  reads TRAN, │            │  reads all   │
│          │ txns to TRANSACT │  TYPE, CAT   │            │  master files│
└──────────┘                  │  writes RPT  │            │  writes TXT  │
                              └──────────────┘            │  + HTML stmts│
                                                          └──────────────┘
```

### Account View Flow (Online)

```
User selects "View Account" from Main Menu
       │
       ▼
  COACTVWC reads:
    1. CARDXREF (by account) → gets card number
    2. ACCTDATA (by acct-id) → gets account details
    3. CUSTDATA (by cust-id) → gets customer info
       │
       ▼
  Displays on BMS map COACTVW
       │
       ▼
  User selects "Update" → XCTL to COACTUPC
    1. Reads same 3 files for display
    2. On confirm: REWRITE ACCTDATA + CUSTDATA
```

### Bill Payment Flow (Online)

```
User selects "Bill Payment" from Main Menu
       │
       ▼
  COBIL00C:
    1. READ CARDXREF (validate card) → gets acct-id
    2. READ ACCTDATA (get balance)
    3. STARTBR/READPREV TRANSACT (get last txn ID)
    4. WRITE TRANSACT (new payment txn)
    5. REWRITE ACCTDATA (update balance)
```
