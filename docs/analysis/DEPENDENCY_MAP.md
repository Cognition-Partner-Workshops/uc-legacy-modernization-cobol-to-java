# CardDemo — Dependency Map (Call Graph & Data Lineage)

> Static dependency analysis: program-to-program calls (CICS `XCTL`/`LINK`, static `CALL`),
> program-to-data access (VSAM/DB2/IMS/MQ), and job orchestration. Derived from the COBOL source,
> JCL, and scheduler definitions.

---

## 1. Online Navigation (CICS `XCTL` flow)

Online programs transfer control with `EXEC CICS XCTL` using the **commarea** (`COCOM01Y`) to carry
state. Most targets are **dynamic** — the next program name is moved into `CDEMO-TO-PROGRAM`,
`CCARD-NEXT-PROG`, or `CDEMO-MENU-OPT-PGMNAME(option)` and then `XCTL`'d. The routing tables live in
`COMEN02Y` (main menu) and `COADM02Y` (admin menu).

```
                         ┌─────────────┐
   3270 / CC00  ───────▶ │  COSGN00C   │  Signon (reads USRSEC)
                         └──────┬──────┘
              SEC-USR-TYPE='U'  │  SEC-USR-TYPE='A'
              ┌─────────────────┴───────────────────┐
              ▼                                       ▼
       ┌─────────────┐                         ┌─────────────┐
       │  COMEN01C   │ Main menu               │  COADM01C   │ Admin menu
       └──────┬──────┘ (routes via COMEN02Y)   └──────┬──────┘ (routes via COADM02Y)
              │                                        │
   ┌──────────┼───────────────┬───────────┐           ├── COUSR00C  List users
   ▼          ▼               ▼           ▼           ├── COUSR01C  Add user
 COACTVWC  COACTUPC        COCRDLIC    COTRN00C        ├── COUSR02C  Update user
 (acct      (acct          (card list)  (tran list)    ├── COUSR03C  Delete user
  view)      update)          │           │            ├── COTRTLIC  Tran-type list  [DB2]
              ▲               ▼           ▼            └── COTRTUPC  Tran-type add    [DB2]
              │           COCRDSLC    COTRN01C
        COCRDUPC          (card view) (tran view)
        (card update)         │           │
                              ▼           ▼
                         COCRDUPC      COTRN02C  (tran add)
   ┌── CORPT00C  Report request ──▶ submits batch TRANREPT (CBTRN03C)
   ├── COBIL00C  Bill payment ──▶ writes TRANSACT, updates ACCTDAT
   └── COPAUS0C  Pending-auth summary  [Auth module]
            └─▶ COPAUS1C  detail ──▶ COPAUS2C  mark fraud
```

**Confirmed control transfers**

| From | Mechanism | To | Notes |
|:-----|:----------|:---|:------|
| COSGN00C | `XCTL PROGRAM('COADM01C')` / `('COMEN01C')` | Admin or Main menu | branch on `SEC-USR-TYPE` |
| COMEN01C | `XCTL PROGRAM(CDEMO-MENU-OPT-PGMNAME(WS-OPTION))` | any user program | table = COMEN02Y |
| COADM01C | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | any admin program | table = COADM02Y |
| COCRDLIC / COCRDSLC | `XCTL PROGRAM(CCARD-NEXT-PROG)` | card detail/update | card flow chain |
| All menu children | `XCTL PROGRAM(CDEMO-TO-PROGRAM)` | back to menu | PF3 = return |
| COPAUS1C | `LINK PROGRAM(WS-PGM-AUTH-FRAUD)` → COPAUS2C | fraud marking | only `LINK` in codebase |

**Menu option → program routing (COMEN02Y, regular users)**

| Opt | Label | Program |
|:----|:------|:--------|
| 1 | Account View | COACTVWC |
| 2 | Account Update | COACTUPC |
| 3 | Credit Card List | COCRDLIC |
| 4 | Credit Card View | COCRDSLC |
| 5 | Credit Card Update | COCRDUPC |
| 6 | Transaction List | COTRN00C |
| 7 | Transaction View | COTRN01C |
| 8 | Transaction Add | COTRN02C |
| 9 | Transaction Reports | CORPT00C |
| 10 | Bill Payment | COBIL00C |
| 11 | Pending Authorization View | COPAUS0C |

---

## 2. Static Sub-program Calls (`CALL`)

These are compile-time-resolved `CALL` targets (utilities and system services).

| Caller | Calls | Purpose |
|:-------|:------|:--------|
| CBACT01C | `COBDATFT` (asm), `CEE3ABD` | date format; LE abend |
| CORPT00C, COTRN02C | `CSUTLDTC` | date validation (CSUTLDTC → `CEEDAYS`) |
| COBSWAIT | `MVSWAIT` (asm) | batch wait/timer |
| CBSTM03A | `CBSTM03B` | statement file-processing subroutine (multiple calls) |
| All `CBACT0*`, `CBTRN0*`, `CBCUS01C`, `CBEXPORT`, `CBIMPORT` | `CEE3ABD` | LE abend on fatal error |
| COACCT01, CODATE01, COPAUA0C | `MQOPEN/MQGET/MQPUT/MQPUT1/MQCLOSE` | IBM MQ API |

Shared **utility leaf nodes**: `CSUTLDTC`, `COBDATFT`, `MVSWAIT`, `CEE3ABD`, `CEEDAYS` (LE), MQ stubs.

> Card/Account/Transaction online programs do **not** statically call each other — they chain via
> `XCTL`, so the runtime call graph is data-driven through the commarea.

---

## 3. Copybook (Shared-Code) Dependencies

Copybooks are the strongest *compile-time* coupling. Heavily-shared copybooks are change-risk hubs.

| Copybook | # Programs incl. | Role |
|:---------|----------------:|:-----|
| COCOM01Y (commarea) | 21 online pgms | inter-program contract — **change ripples everywhere** |
| DFHAID / DFHBMSCA | all online | CICS AID/attribute constants |
| CSMSG01Y, COTTL01Y, CSDAT01Y | ~20 online | message/title/date boilerplate |
| CSUSR01Y | ~18 online | user/role context |
| CVACT01Y (Account) | 9 pgms | account record (online + batch) |
| CVTRA05Y (Transaction) | 9 pgms | transaction record |
| CVACT03Y (XREF) | 8 pgms | cross-reference |
| CVCRD01Y (card work area) | 7 card/acct pgms | PFK + key work area |
| CVACT02Y (Card) | 7 pgms | card record |
| CVCUS01Y (Customer) | 6 pgms | customer record |

Full program → copybook usage is enumerated in `APPLICATION_INVENTORY.md` §4 and was extracted from
every `COPY` statement in `app/**/*.cbl`.

---

## 4. Data Lineage — Programs ↔ Files

### 4.1 Batch programs (from `SELECT ... ASSIGN` + `OPEN` mode)

`R` = read (OPEN INPUT), `W` = write (OPEN OUTPUT), `U` = update (OPEN I-O).

| Program | Reads | Writes / Updates |
|:--------|:------|:-----------------|
| CBACT01C | ACCTFILE `R` | OUTFILE, ARRYFILE, VBRCFILE `W` (extracts) |
| CBACT02C | CARDFILE `R` | — (print) |
| CBACT03C | XREFFILE `R` | — (print) |
| CBCUS01C | CUSTFILE `R` | — (print) |
| CBACT04C (interest) | TCATBALF `R`, XREFFILE `R`, DISCGRP `R`, **ACCTFILE `U`** | **TRANSACT `W`** (interest txns) |
| CBTRN01C (validate) | DALYTRAN, CUSTFILE, XREFFILE, CARDFILE, ACCTFILE, TRANFILE `R` | — |
| CBTRN02C (**post**) | DALYTRAN `R`, XREFFILE `R` | **TRANSACT `W`**, DALYREJS `W`, **ACCTFILE `U`**, **TCATBALF `U`** |
| CBTRN03C (report) | TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM `R` | TRANREPT `W` |
| CBSTM03A (statements) | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE `R` (via CBSTM03B) | STMTFILE, HTMLFILE `W` |
| CBEXPORT | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE `R` | EXPFILE `W` (VB export) |
| CBIMPORT | EXPFILE `R` | CUSTOUT, ACCTOUT, XREFOUT, TRNXOUT, CARDOUT, ERROUT `W` |
| COBTUPDT | INPFILE `R` (tran-type flat) | DB2 `CARDDEMO.TRANSACTION_TYPE` (update/insert) |

**Key write paths (financial integrity hotspots):**
- `CBTRN02C` is the only program that updates `ACCTDAT` balances **and** `TCATBAL` from the daily feed.
- `CBACT04C` updates `ACCTDAT` and appends interest transactions to `TRANSACT`.
- Online `COBIL00C` writes `TRANSACT` and updates `ACCTDAT` for in-full payments.

### 4.2 Online programs (CICS file access)

| Program | VSAM files (datasets) | Access |
|:--------|:----------------------|:-------|
| COSGN00C | USRSEC | read (authenticate) |
| COACTVWC | ACCTDAT, CUSTDAT, CCXREF (CXACAIX) | read |
| COACTUPC | ACCTDAT, CUSTDAT | read + **rewrite** |
| COCRDLIC | CARDDAT (browse STARTBR/READNEXT/READPREV) | read |
| COCRDSLC | CARDDAT | read |
| COCRDUPC | CARDDAT | read + **rewrite** |
| COTRN00C | TRANSACT (browse) | read |
| COTRN01C | TRANSACT | read |
| COTRN02C | TRANSACT, ACCTDAT/CCXREF | read + **write** |
| COBIL00C | ACCTDAT, CCXREF | read + **rewrite**, write TRANSACT |
| CORPT00C | — (submits batch via TDQ/INTRDR) | — |
| COUSR00C–03C | USRSEC | read / write / rewrite / delete |
| COPAUS0C | IMS (DBPAUTP0) + ACCTDAT/CUSTDAT | read |
| COPAUS1C | IMS (update) + DB2 (insert) | update/insert |
| COPAUS2C | DB2 (AUTHFRDS) | update (fraud) |
| COPAUA0C | MQ (request/reply) + IMS | read/update |
| COTRTLIC | DB2 transaction-type (cursor) | read/update/delete |
| COTRTUPC | DB2 transaction-type | update/insert |
| CODATE01 | MQ | request/reply (system date) |
| COACCT01 | MQ + ACCTDAT | request/reply (account inquiry) |

### 4.3 File → backing dataset reference

| Logical file | DD / CICS name | Dataset (EBCDIC sample) | Record copybook |
|:-------------|:---------------|:------------------------|:----------------|
| Account master | ACCTDAT / ACCTFILE | AWS.M2.CARDDEMO.ACCTDATA.PS | CVACT01Y |
| Card master | CARDDAT / CARDFILE | AWS.M2.CARDDEMO.CARDDATA.PS | CVACT02Y |
| Card AIX | CARDAIX | (alt index on CARDDAT) | CVACT02Y |
| Customer master | CUSTDAT / CUSTFILE | AWS.M2.CARDDEMO.CUSTDATA.PS | CVCUS01Y |
| Cross-reference | CCXREF / XREFFILE | AWS.M2.CARDDEMO.CARDXREF.PS | CVACT03Y |
| XREF AIX | CXACAIX | (alt index on CCXREF) | CVACT03Y |
| Transaction master | TRANSACT / TRANFILE | (TRANSACT VSAM) | CVTRA05Y |
| Daily transactions | DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.PS | CVTRA06Y |
| Category balance | TCATBALF | AWS.M2.CARDDEMO.TCATBALF.PS | CVTRA01Y |
| Disclosure group | DISCGRP | AWS.M2.CARDDEMO.DISCGRP.PS | CVTRA02Y |
| Transaction type | TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.PS | CVTRA03Y |
| Transaction category | TRANCATG | AWS.M2.CARDDEMO.TRANCATG.PS | CVTRA04Y |
| User security | USRSEC | AWS.M2.CARDDEMO.USRSEC.PS | CSUSR01Y |
| Export | EXPFILE | AWS.M2.CARDDEMO.EXPORT.DATA.PS | CVEXPORT |
| Daily rejects | DALYREJS | (rejects out) | CVTRA06Y |

---

## 5. Job → Program → Data Orchestration (Batch)

### 5.1 End-to-end daily batch sequence (from README "Running Batch Jobs")

```
CLOSEFIL (free VSAM from CICS)
  └▶ ACCTFILE → CARDFILE → XREFFILE → CUSTFILE → TRANBKP    [IDCAMS loads]
        └▶ TRANCATG → TRANTYPE → DISCGRP → TCATBALF → DUSRSECJ   [reference loads]
              └▶ POSTTRAN  (CBTRN02C)   reads DALYTRAN ▶ writes TRANSACT, updates ACCTDAT+TCATBAL
                    └▶ INTCALC (CBACT04C)  reads TCATBAL+DISCGRP ▶ updates ACCTDAT, writes TRANSACT
                          └▶ TRANBKP ▶ COMBTRAN (SORT) ▶ CREASTMT (CBSTM03A→CBSTM03B) ▶ TRANIDX (AIX)
                                └▶ OPENFIL (return VSAM to CICS) ▶ WAITSTEP (COBSWAIT)
```

### 5.2 Control-M schedule (`app/scheduler/CardDemo.controlm`, 17 jobs / 4 folders)

| Folder (smart folder) | Job chain (INCOND/OUTCOND ordered) |
|:----------------------|:-----------------------------------|
| DAILY-TransactionBackup | CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL |
| WEEKLY-DisclosureGroupsRefresh | CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL |
| WEEKLY-TransactionTypesDBRefresh | MNTTRDB2; TRANEXTR |
| MONTHLY-InterestCalculation | CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL |

A second scheduler definition exists for **CA-7** (`app/scheduler/CardDemo.ca7`) covering the same
workload — relevant if the target platform standardizes on one scheduler.

### 5.3 Optional-module jobs

| Job | Program | Data touched |
|:----|:--------|:-------------|
| CBPAUP0J | CBPAUP0C (DFSRRC00) | IMS DBPAUTP0 — delete expired auths |
| DBPAUTP0 | DFSRRC00 / IEFBR14 | IMS DB load/unload |
| CREADB21 | DSNTEP4 | create DB2 DB + load TRNTYPE/TRNTYCAT |
| TRANEXTR | DSNTIAUL | unload DB2 tran-type → flat file |
| MNTTRDB2 | COBTUPDT | flat file → DB2 transaction-type table |

---

## 6. External / System Dependencies

| Dependency | Type | Used by |
|:-----------|:-----|:--------|
| CICS | TP monitor | all online programs, OPENFIL/CLOSEFIL (CEMT) |
| VSAM (KSDS + AIX) | data store | base application |
| DB2 | RDBMS | Tran-Type module, COPAUS2C fraud |
| IMS DB (DL/I) | hierarchical DB | Auth module (DBPAUTP0/DBPAUTX0) |
| IBM MQ | messaging | COPAUA0C, CODATE01, COACCT01 |
| Language Environment | runtime | `CEE3ABD`, `CEEDAYS` |
| Assembler services | system | COBDATFT (date), MVSWAIT (timer) |
| RACF | security | simulated via USRSEC / COSGN00C |
| Scheduler | orchestration | Control-M + CA-7 |
| FTP / TXT2PDF | output delivery | FTPJCL, TXT2PDF1 (statement distribution) |

---

## 7. Modernization-Relevant Coupling Notes

1. **Commarea (COCOM01Y) is the spine** of the online tier — 21 programs depend on its exact layout.
   Any migration must preserve or formally re-model this contract first.
2. **Dynamic `XCTL` routing** means the runtime call graph is *data-driven* (menu tables COMEN02Y /
   COADM02Y). Static analyzers under-report edges; treat the menu tables as the routing config.
3. **CBTRN02C + CBACT04C** form the financial write core (balance + category-balance + interest).
   They are the highest-integrity batch nodes.
4. **Three optional modules add three different data paradigms** (DB2, IMS, MQ) on top of the VSAM
   base — each can be migrated/retired independently.
5. **Statement generation (CBSTM03A → CBSTM03B)** plus downstream FTP/PDF jobs (FTPJCL, TXT2PDF1) form
   a distinct output-distribution sub-chain off the daily batch — migrate as one unit.
6. **IMS load/unload utilities** (PAUDBLOD, PAUDBUNL, DBUNLDGS via DFSRRC00) sit beside the Auth
   module and are needed for IMS data provisioning/migration.
