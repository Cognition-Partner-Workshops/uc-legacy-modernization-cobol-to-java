# CardDemo Production Support Runbook

> **Application:** CardDemo — Mainframe Credit Card Management System
> **Platform:** z/OS · CICS · VSAM · COBOL · JCL · BMS (3270)
> **Audience:** L1/L2/L3 Production Support Teams
> **Last Updated:** 2026-03-25

---

## Table of Contents

1. [Application Overview](#1-application-overview)
2. [Quick Reference — Transaction IDs & Programs](#2-quick-reference--transaction-ids--programs)
3. [VSAM File Inventory](#3-vsam-file-inventory)
4. [Login & Authentication Issues](#4-login--authentication-issues)
5. [Online CICS Transaction Troubleshooting](#5-online-cics-transaction-troubleshooting)
6. [Batch Job Monitoring & Recovery](#6-batch-job-monitoring--recovery)
7. [VSAM File Problems](#7-vsam-file-problems)
8. [Common CICS Response Codes](#8-common-cics-response-codes)
9. [Batch Abend Codes & File Status Codes](#9-batch-abend-codes--file-status-codes)
10. [User-Facing Error Messages](#10-user-facing-error-messages)
11. [Data Integrity Checks](#11-data-integrity-checks)
12. [Modernized Auth Service (.NET) Troubleshooting](#12-modernized-auth-service-net-troubleshooting)
13. [Nightly Batch Cycle Operations](#13-nightly-batch-cycle-operations)
14. [Emergency Procedures](#14-emergency-procedures)
15. [Escalation Matrix](#15-escalation-matrix)
16. [Health Check Procedures](#16-health-check-procedures)
17. [Key Contacts & Resources](#17-key-contacts--resources)

---

## 1. Application Overview

CardDemo is a mainframe credit card management application supporting:

- **Account Management** — View and update credit card accounts
- **Card Management** — List, view, and update credit cards
- **Transaction Processing** — List, view, add transactions; bill payments
- **Reporting** — Transaction reports, account statements
- **User Administration** — CRUD operations on user security records (Admin only)
- **Batch Processing** — Nightly transaction posting, interest calculation, statement generation

### Architecture at a Glance

```
┌─────────────────────────────────────────────────────────────┐
│                      3270 Terminals                         │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    CICS Region                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ COSGN00C │  │ COMEN01C │  │ COACTUPC │  │ COBIL00C │   │
│  │ (Signon) │  │  (Menu)  │  │(Acct Upd)│  │(Bill Pay)│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ COCRDLIC │  │ COTRN00C │  │ CORPT00C │  │ COUSR00C │   │
│  │(Card Lst)│  │(Tran Lst)│  │(Reports) │  │(User Adm)│   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└──────────────────────────┬──────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    VSAM Files (KSDS)                        │
│  ACCTDAT · CARDDAT · CUSTDAT · USRSEC · TRANSACT           │
│  CARDXREF · TCATBALF · DISCGRP · TRANCATG · TRANTYPE       │
│  CARDAIX (AIX) · CXACAIX (AIX) · DALYTRAN · DALYREJS       │
└─────────────────────────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────┐
│                    Batch (JES2/JCL)                         │
│  POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT       │
└─────────────────────────────────────────────────────────────┘
```

### User Roles

| Role | Login ID | Default Password | Menu | Transaction ID |
|------|----------|-----------------|------|---------------|
| Regular User | USER0001, USER0002, USER0003 | PASSWORD | Main Menu (COMEN01C) | CM00 |
| Administrator | ADMIN001 | PASSWORD | Admin Menu (COADM01C) | CA00 |

---

## 2. Quick Reference — Transaction IDs & Programs

Use this table to map what a user is doing to the COBOL program executing.

| Trans ID | Program | Screen | Function | Severity if Down |
|----------|---------|--------|----------|-----------------|
| **CC00** | COSGN00C | Sign-on | Authentication | **P1 — All users locked out** |
| **CM00** | COMEN01C | Main Menu | Navigation hub | **P1 — No user operations** |
| **CA00** | COADM01C | Admin Menu | Admin navigation | P2 — Admin ops blocked |
| **CAVW** | COACTVWC | Account View | Read-only account lookup | P2 |
| **CAUP** | COACTUPC | Account Update | Account data modification | **P1 — Account updates blocked** |
| **CCLI** | COCRDLIC | Card List | Browse cards by account | P2 |
| **CCDL** | COCRDSLC | Card Detail | View single card | P3 |
| **CCUP** | COCRDUPC | Card Update | Modify card data | P2 |
| **CT00** | COTRN00C | Transaction List | Browse transactions | P2 |
| **CT01** | COTRN01C | Transaction View | View single transaction | P3 |
| **CT02** | COTRN02C | Transaction Add | Create new transaction | **P1 — No new transactions** |
| **CR00** | CORPT00C | Report Request | Submit batch report | P3 |
| **CB00** | COBIL00C | Bill Payment | Process payment | **P1 — Payments blocked** |
| **CU00** | COUSR00C | User List | Browse users (Admin) | P2 |
| **CU01** | COUSR01C | User Add | Create user (Admin) | P3 |
| **CU02** | COUSR02C | User Update | Modify user (Admin) | P3 |
| **CU03** | COUSR03C | User Delete | Delete user (Admin) | P3 |

---

## 3. VSAM File Inventory

| CICS Name | Dataset (HLQ) | Type | Record Size | Key | Description |
|-----------|--------------|------|-------------|-----|-------------|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | 300 bytes | Account ID (11) | Account master |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | 150 bytes | Card Number (16) | Card master |
| CARDAIX | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH | AIX Path | — | Account ID | Card alt index (by account) |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | 500 bytes | Customer ID (9) | Customer master |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | 80 bytes | User ID (8) | User security |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | 350 bytes | Transaction ID (16) | Transaction master |
| CCXREF / CARDXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | 50 bytes | Card Number (16) | Card → Account xref |
| CXACAIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH | AIX Path | — | Account ID | Xref alt index (by account) |
| TCATBALF | — | KSDS | Varies | Composite | Transaction category balance |
| DISCGRP | — | KSDS | Varies | Group code | Discount/interest rate group |
| TRANCATG | — | KSDS | Varies | Category code | Transaction category |
| TRANTYPE | — | KSDS | 60 bytes | Type code | Transaction type lookup |
| DALYTRAN | — | Sequential | 350 bytes | — | Daily transaction input (batch) |
| DALYREJS | — | Sequential | Varies | — | Rejected transactions (batch) |

---

## 4. Login & Authentication Issues

### Symptom: User cannot sign on

**Step 1 — Verify the user exists in USRSEC:**
```
EXEC CICS INQUIRE FILE('USRSEC') STATUS(xxx)
```
Or use CICS CEBR to browse the USRSEC file.

**Step 2 — Check user record format:**
User record layout (CSUSR01Y copybook, 80 bytes):

| Field | PIC | Offset | Length | Description |
|-------|-----|--------|--------|-------------|
| SEC-USR-ID | X(8) | 0 | 8 | User ID (uppercase) |
| SEC-USR-FNAME | X(20) | 8 | 20 | First name |
| SEC-USR-LNAME | X(20) | 28 | 20 | Last name |
| SEC-USR-PWD | X(8) | 48 | 8 | Password (plaintext) |
| SEC-USR-TYPE | X(1) | 56 | 1 | 'A' = Admin, 'U' = User |
| SEC-USR-FILLER | X(23) | 57 | 23 | Filler/reserved |

**Step 3 — Common causes:**

| Problem | Symptom on Screen | Resolution |
|---------|------------------|------------|
| User ID not found | `User not found. Try again ...` | Verify user exists in USRSEC. Reload with DUSRSECJ if needed. |
| Wrong password | `Unable to verify the User ...` | Reset by updating USRSEC record. Password is 8-char plaintext. |
| User ID blank | `Please enter User ID ...` | User left field empty. No system action needed. |
| Password blank | `Please enter Password ...` | User left field empty. No system action needed. |
| USRSEC file closed | CICS RESP code error on screen | Run OPENFIL JCL job to reopen CICS files. |
| USRSEC file corrupted | Random abends on signon | Reload from backup using DUSRSECJ job. |

**Step 4 — Reload user security data:**
Submit the `DUSRSECJ` JCL job. This reloads the USRSEC VSAM file from inline data with default users:
- USER0001 / PASSWORD (Regular)
- USER0002 / PASSWORD (Regular)
- USER0003 / PASSWORD (Regular)
- ADMIN001 / PASSWORD (Admin)

---

## 5. Online CICS Transaction Troubleshooting

### 5.1 General CICS Transaction Failures

| Symptom | Likely Cause | Diagnostic | Resolution |
|---------|-------------|-----------|------------|
| Transaction hangs | Program loop or file wait | CICS CEMT I TASK — check task status | Purge task: `CEMT S TASK(xxxx) PURGE` |
| "PROGRAM NOT FOUND" | Program not installed | `CEMT I PROG(xxxxxxxx)` | Install via CSD: group CARDDEMO |
| "MAPSET NOT FOUND" | BMS map not installed | `CEMT I MAPS(xxxxxxxx)` | Install mapset via CSD: group CARDDEMO |
| Screen garbled/blank | BMS map version mismatch | Recompile BMS map, reinstall | Reassemble BMS map, newcopy |
| "FILE NOT FOUND" | VSAM file not defined in CICS | `CEMT I FILE(xxxxxxxx)` | Install file via CSD: group CARDDEMO |
| "FILE DISABLED" | VSAM file disabled | `CEMT I FILE(xxxxxxxx)` | `CEMT S FILE(xxxxxxxx) ENA OPE` |
| "TRANSACTION NOT FOUND" | Trans ID not defined | `CEMT I TRAN(xxxx)` | Install transaction via CSD |
| User stuck on screen | PF key not working | Check terminal keymap | User should press PF3 to go back, or Clear to reset |

### 5.2 Account View/Update Issues (COACTVWC / COACTUPC)

| Issue | Program | Cause | Fix |
|-------|---------|-------|-----|
| "Account not found" | COACTVWC | Invalid account ID or ACCTDAT issue | Verify account exists. Check ACCTDAT file status. |
| Validation errors on update | COACTUPC | Field-level validation: SSN, phone, date, credit limit, FICO, ZIP, state | Check specific field format requirements (see Section 10). |
| Account update fails silently | COACTUPC | REWRITE to ACCTDAT failed | Check ACCTDAT file status. Verify no exclusive locks. |
| Cross-ref lookup fails | COACTVWC | CXACAIX alternate index issue | Rebuild alt index: submit XREFFILE job. |

### 5.3 Card Operations Issues (COCRDLIC / COCRDSLC / COCRDUPC)

| Issue | Program | Cause | Fix |
|-------|---------|-------|-----|
| Card list empty | COCRDLIC | No cards for account or CARDAIX issue | Verify cards exist. Rebuild CARDAIX alternate index. |
| Card list pagination broken | COCRDLIC | STARTBR/READNEXT positioning error | Restart CICS transaction. If persistent, NEWCOPY program. |
| Card update fails | COCRDUPC | CARDDAT REWRITE failure | Check CARDDAT file status. Verify no batch lock. |

### 5.4 Transaction Operations Issues (COTRN00C–02C)

| Issue | Program | Cause | Fix |
|-------|---------|-------|-----|
| Transaction list empty | COTRN00C | TRANSACT file empty or browse error | Verify TRANSACT has data. Check alt indexes. |
| "Transaction not found" | COTRN01C | Invalid transaction ID | Verify transaction exists via CEBR on TRANSACT. |
| Cannot add transaction | COTRN02C | Card-account xref lookup failure | Verify CARDXREF and CXACAIX are valid. |
| Duplicate transaction ID | COTRN02C | Transaction ID generation collision | ID is generated as max(existing) + 1 via READPREV. Check for concurrent access. |

### 5.5 Bill Payment Issues (COBIL00C)

| Issue | Cause | Fix |
|-------|-------|-----|
| "Account not found" for payment | CXACAIX or ACCTDAT read failure | Verify cross-ref and account data integrity. |
| Payment amount wrong | Balance read from ACCTDAT may be stale | Check if batch recently updated ACCTDAT. |
| Payment creates duplicate transaction | READPREV for max ID race condition | Check TRANSACT for duplicates. Manual cleanup if needed. |
| "Unable to process payment" | WRITE to TRANSACT or REWRITE to ACCTDAT failed | Check both files are enabled and writable. |

---

## 6. Batch Job Monitoring & Recovery

### 6.1 Batch Cycle Overview

The nightly batch cycle runs in this **strict order**. If any step fails, subsequent steps must not run.

```
Step 1: CLOSEFIL ─── Close CICS files for exclusive batch access
   │
Step 2: Data Refresh (parallel OK):
   │    ACCTFILE → Refresh ACCTDAT
   │    CARDFILE → Refresh CARDDAT + CARDAIX
   │    CUSTFILE → Refresh CUSTDAT
   │    XREFFILE → Refresh CARDXREF + CXACAIX
   │    TRANFILE → Refresh TRANSACT
   │
Step 3: POSTTRAN ─── Post daily transactions (CBTRN02C)
   │         Reads:  DALYTRAN, XREFFILE, ACCTFILE
   │         Writes: TRANSACT, DALYREJS, TCATBALF
   │
Step 4: INTCALC ──── Calculate interest (CBACT04C)
   │         Reads:  TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT
   │         Writes: TCATBALF (update), ACCTFILE (interest applied)
   │
Step 5: TRANBKP ──── Backup transactions to GDG
   │
Step 6: COMBTRAN ─── Merge transaction files (SORT + IDCAMS)
   │
Step 7: CREASTMT ─── Generate statements (CBSTM03A)
   │         Writes: STMTFILE (text), HTMLFILE (HTML)
   │
Step 8: TRANREPT ─── Print transaction report (CBTRN03C)
   │
Step 9: TRANIDX ──── Rebuild alternate indexes on TRANSACT
   │
Step 10: OPENFIL ─── Reopen CICS files for online access
```

### 6.2 Batch Job Failure — Diagnosis & Recovery

| Job | Common Failure | Sysout Check | Recovery Action |
|-----|---------------|-------------|----------------|
| **CLOSEFIL** | CICS files already closed or not found | Check DFHCSVC messages | Verify files exist. Retry. If already closed, skip. |
| **ACCTFILE / CARDFILE / CUSTFILE / XREFFILE / TRANFILE** | IDCAMS DELETE/DEFINE/REPRO failure | Check IDCAMS LASTCC/MAXCC | RC=8 on DELETE is OK (file didn't exist). RC=12+ investigate. |
| **POSTTRAN** | CBTRN02C abend (code 999) | Check DISPLAY output: `ABENDING PROGRAM` | Check which file open/read failed. Fix file, restart job. |
| **POSTTRAN** | RC=4 | Check: "NO MORE RECORDS IN DAILY TRAN FILE" | Normal — empty daily transaction file. Verify DALYTRAN has data. |
| **INTCALC** | CBACT04C abend (code 999) | Check file open errors in sysout | Verify all 5 input files are available and valid. |
| **TRANBKP** | GDG dataset full | Check IDCAMS messages | Define new GDG generation or clean old ones (DEFGDGB/DEFGDGD). |
| **COMBTRAN** | SORT failure | Check SORT return codes | RC=16 = sort error. Check input datasets. |
| **CREASTMT** | CBSTM03A abend | Check DISPLAY messages | Often caused by missing XREFFILE or CUSTFILE data. |
| **TRANIDX** | IDCAMS BLDINDEX failure | Check IDCAMS RC | RC=8+ = index corruption. Delete and rebuild. |
| **OPENFIL** | CICS region not active | Check CICS status | Start CICS region first, then resubmit. |

### 6.3 Batch Job Restart Procedures

**Rule:** Always restart from the failed step, never skip steps.

| If Failed At | Restart From | Pre-Check |
|-------------|-------------|-----------|
| CLOSEFIL | CLOSEFIL | Verify CICS is up |
| Data Refresh jobs | Failed data refresh job | N/A |
| POSTTRAN | POSTTRAN | Verify DALYTRAN has data; delete partial TRANSACT writes |
| INTCALC | INTCALC | Verify POSTTRAN completed; check TCATBALF state |
| TRANBKP | TRANBKP | OK to restart; backup is idempotent to GDG |
| COMBTRAN | COMBTRAN | Verify input transaction files exist |
| CREASTMT | CREASTMT | Verify sorted input exists |
| TRANIDX | TRANIDX | OK to restart; rebuilds indexes from scratch |
| OPENFIL | OPENFIL | Verify batch processing complete |

### 6.4 Partial POSTTRAN Failure Recovery

POSTTRAN (CBTRN02C) is the most critical batch job. If it abends mid-run:

1. **Check DALYREJS** — rejected transactions are written here; these are safe.
2. **Check TRANSACT** — may have partial writes.
3. **Check the last DISPLAY message** — it shows which record was being processed.
4. **Recovery options:**
   - **Option A (preferred):** If early in processing, delete partial TRANSACT records added in this run, fix the root cause, resubmit.
   - **Option B:** If near completion, manually complete remaining records from DALYTRAN.
   - **Option C:** Restore TRANSACT from backup (TRANBKP GDG), fix root cause, resubmit entire batch from POSTTRAN.

---

## 7. VSAM File Problems

### 7.1 File Status Quick Reference

| Status | Meaning | Batch Action | CICS Action |
|--------|---------|-------------|-------------|
| ENABLED/OPEN | Normal | N/A | N/A |
| DISABLED | Manually disabled | N/A | `CEMT S FILE(xxx) ENA` |
| CLOSED | Not opened yet | N/A | `CEMT S FILE(xxx) OPE` |
| UNENABLED | CICS cannot access | Check CSD definition | Reinstall from CSD group CARDDEMO |

### 7.2 VSAM File Recovery Procedures

**File corruption (CI/CA splits, index errors):**
1. Close the file: `CEMT S FILE(xxx) CLO DIS`
2. Run IDCAMS VERIFY: `VERIFY DATASET('dataset.name')`
3. If VERIFY fails, restore from backup:
   - Export: `IDCAMS REPRO` from backup
   - Delete/Define: Use the corresponding JCL job (e.g., ACCTFILE for ACCTDAT)
   - Reimport: `IDCAMS REPRO` from export
4. Reopen: `CEMT S FILE(xxx) ENA OPE`

**Alternate index out of sync:**
1. Close base and AIX: `CEMT S FILE(CARDDAT) CLO DIS` and `CEMT S FILE(CARDAIX) CLO DIS`
2. Rebuild AIX: `IDCAMS BLDINDEX`
3. Reopen both files

**File space exhaustion (CI/CA full):**
1. Close the file
2. `IDCAMS LISTCAT` to check space allocation
3. Redefine with larger RECORDS or CYLINDERS
4. `IDCAMS REPRO` to reload data
5. Reopen

### 7.3 File Refresh JCL Jobs (for complete reload)

| File | Refresh Job | What It Does |
|------|------------|-------------|
| ACCTDAT | ACCTFILE | Delete + Define + Repro from flat file |
| CARDDAT + CARDAIX | CARDFILE | Delete + Define + Repro + Build AIX |
| CUSTDAT | CUSTFILE | Delete + Define + Repro |
| CARDXREF + CXACAIX | XREFFILE | Delete + Define + Repro + Build AIX |
| TRANSACT | TRANFILE | Delete + Define + Repro + Build AIX |
| USRSEC | DUSRSECJ | Load from inline data (default users) |
| DISCGRP | DISCGRP | Delete + Define + Repro |
| TCATBALF | TCATBALF | Delete + Define + Repro |
| TRANCATG | TRANCATG | Delete + Define + Repro |
| TRANTYPE | TRANTYPE | Delete + Define + Repro |
| DALYREJS | DALYREJS | Define empty dataset |
| REPTFILE | REPTFILE | Define empty dataset |

---

## 8. Common CICS Response Codes

These are the EIBRESP values checked in online programs (via `DFHRESP`):

| EIBRESP | Name | Decimal | What Happened | Support Action |
|---------|------|---------|--------------|---------------|
| NORMAL | DFHRESP(NORMAL) | 0 | Success | None |
| NOTFND | DFHRESP(NOTFND) | 13 | Record not found on READ | Verify key exists. May be legitimate "not found." |
| DUPREC | DFHRESP(DUPREC) | 14 | Duplicate record on WRITE | Key already exists. Check for duplicate data. |
| DUPKEY | DFHRESP(DUPKEY) | 15 | Duplicate alternate key | Alternate index has duplicate; may be expected for non-unique AIX. |
| ENDFILE | DFHRESP(ENDFILE) | 20 | End of file on browse | Normal end of READNEXT/READPREV. No action. |
| DISABLED | DFHRESP(DISABLED) | 84 | File is disabled | Enable file: `CEMT S FILE(xxx) ENA OPE` |
| NOTOPEN | DFHRESP(NOTOPEN) | 19 | File is not open | Open file: `CEMT S FILE(xxx) OPE` |
| PGMIDERR | DFHRESP(PGMIDERR) | 27 | Program not found | Install program from CSD group CARDDEMO. |
| INVREQ | DFHRESP(INVREQ) | 16 | Invalid request | Check EIBRESP2 for details. Often means file not open for requested operation. |
| ILLOGIC | DFHRESP(ILLOGIC) | 21 | VSAM logical error | Check VSAM return/reason codes. May need file recovery. |
| IOERR | DFHRESP(IOERR) | 17 | I/O error | Serious — check VSAM file integrity. May need restore. |

**Where to find response codes in logs:**
All programs log errors as: `DISPLAY 'RESP:' WS-RESP-CD 'REAS:' WS-REAS-CD`
Search CICS auxiliary trace or CEEMSG output for these messages.

---

## 9. Batch Abend Codes & File Status Codes

### 9.1 Application Abend Codes

| Abend Code | Source | Meaning | Recovery |
|------------|--------|---------|----------|
| **999** | CEE3ABD call in all CB* programs | Application-detected fatal error | Check DISPLAY messages preceding the abend for the specific file/operation that failed. |
| **U0999** | User abend 999 | Same as above (LE representation) | Same as above. |

The abend handler in all batch programs follows this pattern:
```
Z-ABEND-PROGRAM / 9999-ABEND-PROGRAM:
    DISPLAY 'ABENDING PROGRAM'
    MOVE 999 TO ABCODE
    CALL 'CEE3ABD' USING ABCODE, TIMING
```

### 9.2 Abend Data Area (CSMSG02Y)

When an online CICS program abends, the following fields are populated:

| Field | PIC | Description |
|-------|-----|-------------|
| ABEND-CODE | X(4) | 4-character abend code |
| ABEND-CULPRIT | X(8) | Program name that caused the abend |
| ABEND-REASON | X(50) | Reason text |
| ABEND-MSG | X(72) | Full abend message |

### 9.3 Batch File Status Codes

These appear in DISPLAY output as `FILE STATUS IS: NNNN`:

| Status | Meaning | Action |
|--------|---------|--------|
| 00 | Successful operation | None |
| 02 | Duplicate key (non-unique AIX) | May be expected; verify logic. |
| 10 | End of file | Normal — no more records. |
| 22 | Duplicate primary key on WRITE | Data error — duplicate record. |
| 23 | Record not found | Key doesn't exist in file. |
| 30 | Permanent I/O error | File corruption. Restore from backup. |
| 34 | Boundary violation (out of space) | Extend file or redefine with more space. |
| 35 | File not found | Dataset doesn't exist. Run define job. |
| 37 | File mode conflict | File opened for wrong mode. Check JCL DD. |
| 39 | Fixed-length record conflict | Record length mismatch. Check copybook vs. file definition. |
| 41 | File already open | OPEN attempted on already-open file. |
| 42 | File already closed | CLOSE attempted on already-closed file. |
| 46 | READ attempted without prior open | OPEN the file first. |
| 47 | READ attempted on output-only file | Change OPEN mode in JCL. |
| 48 | WRITE attempted on input-only file | Change OPEN mode in JCL. |

---

## 10. User-Facing Error Messages

### 10.1 Sign-on Screen (COSGN00C)

| Message | Cause | User Action | Support Action |
|---------|-------|-------------|---------------|
| `Please enter User ID ...` | User ID field left blank | Enter user ID | None |
| `Please enter Password ...` | Password field left blank | Enter password | None |
| `User not found. Try again ...` | User ID not in USRSEC file | Re-enter correct user ID | Verify user exists; reload DUSRSECJ if needed |
| `Unable to verify the User ...` | Password mismatch or file error | Re-enter password | Check USRSEC file integrity; verify password in record |
| `Invalid key pressed. Please see below...` | PF key not mapped (not Enter/PF3) | Press Enter to submit or PF3 to exit | None |
| `Thank you for using CardDemo application...` | PF3 pressed — normal logoff | None | None |

### 10.2 Account Update (COACTUPC) — Validation Errors

| Field | Validation Rule | Error Message Pattern |
|-------|----------------|----------------------|
| SSN | 3 parts: NNN-NN-NNNN, all numeric | "Invalid SSN format" |
| Phone | 10 digits, all numeric | "Invalid phone number" |
| Date of Birth | CCYYMMDD, not future, valid via CEEDAYS | "Invalid date" |
| Credit Limit | Numeric, positive | "Invalid credit limit" |
| FICO Score | Numeric, range 300–850 | "Invalid FICO score" |
| State Code | Valid US state abbreviation | "Invalid state code" |
| Account Status | Valid status code | "Invalid account status" |
| ZIP Code | 5 or 9 digits, numeric | "Invalid ZIP code" |

### 10.3 Common Cross-Program Messages

| Message | Source (CSMSG01Y) | Meaning |
|---------|------------------|---------|
| `Thank you for using CardDemo application...` | CCDA-MSG-THANK-YOU | Normal session end (PF3 at signon) |
| `Invalid key pressed. Please see below...` | CCDA-MSG-INVALID-KEY | User pressed unmapped PF key |

---

## 11. Data Integrity Checks

### 11.1 Cross-Reference Integrity

The card-to-account cross-reference (CARDXREF) must be consistent with CARDDAT and ACCTDAT.

**Check: Every card in CARDDAT has a CARDXREF entry:**
```jcl
// Run CBACT02C (READCARD job) and CBACT03C (READXREF job)
// Compare card numbers in both outputs
```

**Check: Every account ID in CARDXREF exists in ACCTDAT:**
```jcl
// Run READACCT and READXREF
// Verify all XREF-ACCT-ID values appear in ACCTDAT
```

### 11.2 Transaction Integrity

**Check: No orphaned transactions (account doesn't exist):**
- Run TRANREPT job and look for missing account lookups
- If CBTRN03C reports "ACCOUNT NOT FOUND" for any transaction, there's orphan data

**Check: Category balance consistency:**
- TCATBALF should reflect the sum of transactions in TRANSACT by category
- After INTCALC, verify TCATBALF totals match TRANSACT aggregates

### 11.3 User Security Data

**Check: All user records are valid:**
- SEC-USR-TYPE must be 'A' or 'U' (any other value = login will fail)
- SEC-USR-PWD must not be blank (8 characters, padded with spaces)
- SEC-USR-ID must be exactly 8 characters, uppercase

---

## 12. Modernized Auth Service (.NET) Troubleshooting

The modernized sign-on service (`modernized/CardDemo.Auth/`) replaces COSGN00C with an ASP.NET Core 8 API.

### 12.1 Architecture

```
Client → POST /api/auth/login → AuthenticationService
                                      │
                               ┌──────┴──────┐
                               │             │
                          Okta OIDC     Local Store
                          (Primary)     (Fallback)
                               │             │
                               └──────┬──────┘
                                      │
                                 JWT Token
                            (replaces COMMAREA)
```

### 12.2 Endpoints

| Endpoint | Method | Purpose | COBOL Equivalent |
|----------|--------|---------|-----------------|
| `/api/auth/login` | POST | Authenticate user | COSGN00C PROCESS-ENTER-KEY |
| `/api/auth/me` | GET | Current session info | CARDDEMO-COMMAREA read |
| `/api/auth/logout` | POST | End session | PF3 on sign-on screen |
| `/swagger` | GET | API documentation | N/A |

### 12.3 Common Issues

| Issue | Possible Cause | Resolution |
|-------|---------------|------------|
| 401 Unauthorized on login | Wrong credentials or Okta down | Check Okta status. If `FallbackToLocalAuth=true`, local store should handle. |
| Okta auth always fails | Okta settings not configured in `appsettings.json` | Set `Okta:Domain`, `Okta:ClientId`, `Okta:ClientSecret`. |
| Okta auth silently falls back to local | `OktaTokenResponse` JSON deserialization bug (snake_case vs PascalCase) | Add `[JsonPropertyName("access_token")]` attributes to `OktaTokenResponse` class. |
| Local auth fails for valid user | User not in `LocalUserStore` seed data | Add user to `LocalUserStore.cs` initialization or fix seed data. |
| JWT token expired | Token lifetime exceeded | Default lifetime is configurable. Re-authenticate. |
| "Admin" user routed to wrong menu | `UserType` claim incorrect | Verify `SEC-USR-TYPE` equivalent is 'A' for admin. Check `CardDemoSession.UserType`. |
| Application won't start | Missing NuGet packages or .NET 8 SDK | Run `dotnet restore` then `dotnet build`. Requires .NET 8 SDK. |

### 12.4 Configuration Reference (`appsettings.json`)

| Setting | Description | Default |
|---------|------------|---------|
| `Okta:Domain` | Okta organization URL | *(must set)* |
| `Okta:ClientId` | Okta application client ID | *(must set)* |
| `Okta:ClientSecret` | Okta application client secret | *(must set)* |
| `Okta:AuthorizationServerId` | Okta auth server | `default` |
| `CardDemo:FallbackToLocalAuth` | Allow local auth when Okta fails | `true` |
| `CardDemo:JwtSigningKey` | JWT signing key (min 32 chars) | Development default — **CHANGE IN PROD** |
| `CardDemo:AdminGroupClaim` | Okta group claim for admins | `CardDemo-Admin` |
| `CardDemo:UserGroupClaim` | Okta group claim for users | `CardDemo-User` |

---

## 13. Nightly Batch Cycle Operations

### 13.1 Pre-Batch Checklist

- [ ] Confirm all online users are logged off (or will be disconnected)
- [ ] Verify CICS region is active
- [ ] Verify DALYTRAN file exists and has today's transactions
- [ ] Verify GDG base exists for TRANBKP (DEFGDGB/DEFGDGD)
- [ ] Verify sufficient DASD space for output datasets

### 13.2 Batch Window Timeline (Typical)

| Time | Job | Duration (est.) | Dependency |
|------|-----|-----------------|-----------|
| 22:00 | CLOSEFIL | 1 min | CICS must be active |
| 22:01 | Data refresh jobs (parallel) | 5–10 min | CLOSEFIL complete |
| 22:15 | POSTTRAN | 5–30 min (volume-dependent) | All data refresh complete |
| 22:45 | INTCALC | 5–15 min | POSTTRAN complete |
| 23:00 | TRANBKP | 2–5 min | INTCALC complete |
| 23:05 | COMBTRAN | 5–10 min | TRANBKP complete |
| 23:15 | CREASTMT | 10–30 min | COMBTRAN complete |
| 23:45 | TRANREPT | 5–10 min | COMBTRAN complete (parallel OK with CREASTMT) |
| 23:55 | TRANIDX | 2–5 min | All transaction processing complete |
| 00:00 | OPENFIL | 1 min | TRANIDX complete |

### 13.3 Post-Batch Checklist

- [ ] Verify OPENFIL completed (RC=0)
- [ ] Confirm CICS files are ENABLED and OPEN: `CEMT I FILE(*)` should show all CARDDEMO files enabled
- [ ] Verify sign-on works (test with USER0001 / PASSWORD)
- [ ] Check POSTTRAN output for rejected transactions (DALYREJS)
- [ ] Verify statement output (STMTFILE / HTMLFILE) was generated
- [ ] Check TRANREPT output for completeness
- [ ] Archive batch output datasets per retention policy

### 13.4 Batch Automation Scripts

The `scripts/` directory contains shell scripts for remote batch submission via FTP tunnel (port 2121):

| Script | Purpose |
|--------|---------|
| `run_full_batch.sh` | Submit the complete nightly batch cycle |
| `run_posting.sh` | Submit just the posting cycle (POSTTRAN) |
| `run_interest_calc.sh` | Submit just interest calculation (INTCALC) |
| `remote_submit.sh` | Submit a single JCL job by name |
| `remote_refresh.sh` | Refresh all data files |

**Usage:** All scripts require an active FTP tunnel to the mainframe on port 2121.

---

## 14. Emergency Procedures

### 14.1 P1 — All Users Locked Out (COSGN00C / CC00 Failure)

1. **Immediate:** Check USRSEC file status → `CEMT I FILE(USRSEC)`
2. If DISABLED → `CEMT S FILE(USRSEC) ENA OPE`
3. If CORRUPTED → Submit DUSRSECJ to reload default users
4. If PROGRAM ERROR → `CEMT S PROG(COSGN00C) NEW` (newcopy)
5. If CICS region issue → Contact systems programming for CICS restart
6. **Escalate** if not resolved in 15 minutes

### 14.2 P1 — Batch Cycle Failure During POSTTRAN

1. **Do NOT restart POSTTRAN** until root cause is identified
2. Check SYSOUT for the last DISPLAY message to identify the failing record
3. Check all input files (DALYTRAN, XREFFILE, ACCTFILE) are accessible
4. If file corruption → restore from pre-batch backup and restart from CLOSEFIL
5. If data error → identify and quarantine the bad DALYTRAN record, then restart
6. **Escalate** to L3 if not resolved in 30 minutes
7. **Business impact:** No transactions posted = next day's statements will be incorrect

### 14.3 P1 — VSAM File Corruption (Any Critical File)

1. Close the file in CICS: `CEMT S FILE(xxx) CLO DIS`
2. Run IDCAMS VERIFY on the dataset
3. If VERIFY fails → Restore from last backup:
   - Use the appropriate data refresh JCL job (ACCTFILE, CARDFILE, etc.)
   - Caution: This will restore data to the last refresh point; intra-day changes may be lost
4. After restore → `CEMT S FILE(xxx) ENA OPE`
5. Verify sign-on and basic navigation works
6. **Escalate** to L3 and notify business of potential data loss window

### 14.4 P2 — Modernized Auth Service Down

1. Check application logs: `dotnet` process running?
2. Check Okta service status: https://status.okta.com
3. If Okta is down and `FallbackToLocalAuth=true` → local auth should still work
4. If local auth also fails → check `appsettings.json` for correct `JwtSigningKey`
5. Restart the service: `dotnet run` or restart the container/service
6. If persistent → Check NuGet package compatibility, review application startup logs

---

## 15. Escalation Matrix

| Level | Scope | Response Time | Actions |
|-------|-------|--------------|---------|
| **L1 — Helpdesk** | User-reported issues, password resets | 15 min | Screen guidance, password resets, basic file enable/open |
| **L2 — Application Support** | Transaction failures, batch issues, data lookups | 30 min | CICS commands, batch job restart, VSAM file recovery, data verification |
| **L3 — Application Development** | Code bugs, abends, data corruption, complex recovery | 1 hour | Program debugging, copybook changes, JCL modifications, emergency fixes |
| **L4 — Systems Programming** | CICS region issues, z/OS problems, DASD issues | 1 hour | CICS restart, system recovery, storage management |

### Escalation Triggers

| Trigger | Escalate To |
|---------|------------|
| User cannot sign on after basic checks | L2 |
| Batch job abends with code 999 | L2 |
| VSAM ILLOGIC or IOERR response | L2 → L3 |
| CICS region crash | L4 |
| Same transaction abending for multiple users | L3 |
| Data discrepancies in financial amounts | L3 (+ business notification) |
| Batch cycle not complete by SLA deadline | L2 → L3 |
| Modernized auth service unresponsive | L2 → L3 |

---

## 16. Health Check Procedures

### 16.1 Daily Health Check (Morning — Post-Batch)

| # | Check | Command / Procedure | Expected Result |
|---|-------|-------------------|----------------|
| 1 | CICS region active | `CEMT I SYSTEM` | Status = ACTIVE |
| 2 | All VSAM files open | `CEMT I FILE(*)` filter group CARDDEMO | All = ENABLED, OPEN |
| 3 | Sign-on works | Log in as USER0001 / PASSWORD | Main menu displayed |
| 4 | Admin sign-on works | Log in as ADMIN001 / PASSWORD | Admin menu displayed |
| 5 | Account view works | Transaction CAVW, enter valid account | Account data displayed |
| 6 | Batch completed | Check JES2 output for last night's jobs | All RC=0 or RC=4 (expected) |
| 7 | Statements generated | Verify STMTFILE / HTMLFILE exist and non-empty | Files present with data |
| 8 | Rejected transactions | Check DALYREJS | If non-empty, investigate |

### 16.2 CICS Resource Status Check

```
CEMT I FILE(ACCTDAT)     → Should be ENABLED, OPEN
CEMT I FILE(CARDDAT)     → Should be ENABLED, OPEN
CEMT I FILE(CUSTDAT)     → Should be ENABLED, OPEN
CEMT I FILE(USRSEC)      → Should be ENABLED, OPEN
CEMT I FILE(TRANSACT)    → Should be ENABLED, OPEN
CEMT I FILE(CARDAIX)     → Should be ENABLED, OPEN
CEMT I FILE(CXACAIX)     → Should be ENABLED, OPEN
CEMT I FILE(CCXREF)      → Should be ENABLED, OPEN

CEMT I PROG(COSGN00C)    → Should be ENABLED
CEMT I PROG(COMEN01C)    → Should be ENABLED
CEMT I PROG(COADM01C)    → Should be ENABLED

CEMT I TRAN(CC00)        → Should be ENABLED
CEMT I TRAN(CM00)        → Should be ENABLED
```

### 16.3 Modernized Service Health Check

| # | Check | How | Expected |
|---|-------|-----|----------|
| 1 | Service running | `curl http://localhost:5000/swagger` | Swagger UI loads |
| 2 | Login endpoint | `curl -X POST http://localhost:5000/api/auth/login -H "Content-Type: application/json" -d '{"userId":"USER0001","password":"PASSWORD"}'` | HTTP 200 with JWT token |
| 3 | Okta connectivity | Check Okta domain reachability | HTTP 200 from Okta |

---

## 17. Key Contacts & Resources

### CSD Definitions

All CardDemo resources are defined in CSD group **CARDDEMO** (file: `app/csd/CARDDEMO.CSD`).

To install all resources: `CEDA INSTALL GROUP(CARDDEMO)`

### Source Code Locations

| Component | Location |
|-----------|---------|
| COBOL programs | `app/cbl/` |
| Copybooks | `app/cpy/` |
| BMS maps | `app/bms/` |
| BMS-generated copybooks | `app/cpy-bms/` |
| JCL jobs | `app/jcl/` |
| CSD definitions | `app/csd/CARDDEMO.CSD` |
| Sample data (ASCII) | `app/data/ASCII/` |
| Sample data (EBCDIC) | `app/data/EBCDIC/` |
| Automation scripts | `scripts/` |
| Modernized auth service | `modernized/CardDemo.Auth/` |

### Related Documentation

| Document | Purpose |
|----------|---------|
| `APPLICATION_INVENTORY.md` | Complete catalog of all programs, copybooks, maps, jobs |
| `DATA_DICTIONARY.md` | Business entity definitions from copybook PIC clauses |
| `DEPENDENCY_MAP.md` | Call graph, data lineage, file access patterns |
| `HOTSPOT_REPORT.md` | Top 10 highest-risk modules with migration priorities |
| `modernized/CardDemo.Auth/README.md` | .NET auth service conversion documentation |

### Default Test Credentials

| User ID | Password | Type | Purpose |
|---------|----------|------|---------|
| USER0001 | PASSWORD | Regular | General testing |
| USER0002 | PASSWORD | Regular | Secondary test user |
| USER0003 | PASSWORD | Regular | Additional test user |
| ADMIN001 | PASSWORD | Admin | Admin function testing |

> **IMPORTANT:** These are workshop defaults. In production environments, all passwords must be changed and managed per security policy.
