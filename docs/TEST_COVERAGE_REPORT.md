# Test Coverage Report: COBOL Module Functions

**Generated:** 2026-03-25  
**Purpose:** Catalog all PERFORM paragraphs (functions) in each COBOL module to support test planning and coverage analysis for mainframe-to-Java modernization.

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Online CICS Programs](#online-cics-programs)
3. [Batch Programs](#batch-programs)
4. [Optional Module Programs](#optional-module-programs)
5. [Test Coverage Recommendations](#test-coverage-recommendations)

---

## Executive Summary

This report catalogs **all 1,247 PERFORM paragraphs** (functions) across **44 COBOL programs** in the CardDemo application. Each module is documented with:

- **Lines of Code (LOC)** — Total program size
- **Function Count** — Number of PERFORM paragraphs (testable units)
- **Complexity Metrics** — CICS calls, PERFORM statements, EVALUATE statements, IF statements
- **Function List** — Complete enumeration of all paragraphs with categorization

### Coverage Statistics by Module Type

| Module Type | Programs | Total LOC | Total Functions | Avg Functions/Program |
|-------------|----------|-----------|-----------------|----------------------|
| Online CICS | 20 | 15,725 | 633 | 31.7 |
| Batch | 11 | 5,155 | 267 | 24.3 |
| Optional Modules | 13 | 9,935 | 347 | 26.7 |
| **TOTAL** | **44** | **30,815** | **1,247** | **28.3** |

### Complexity Distribution

| Complexity Tier | Programs | Characteristics |
|-----------------|----------|-----------------|
| **High** (100+ decisions) | 8 | COACTUPC (168 IFs), COCRDUPC (148), COCRDLIC (122), COTRTUPC (102), CBTRN02C (93), COTRTLIC (89), CBACT04C (86), CBTRN03C (75) |
| **Medium** (30-99 decisions) | 18 | Most online CICS and batch programs |
| **Low** (<30 decisions) | 18 | Utilities, simple batch, data loaders |

---

## Online CICS Programs

### 1. COACTUPC — Account Update (4,236 LOC, 77 functions)

**Complexity:** 17 CICS calls, 64 PERFORM, 10 EVALUATE, 168 IF  
**Business Function:** Update account and customer master records with extensive field validation  
**Test Priority:** CRITICAL — Largest program, highest complexity, PII/PCI data handling

#### Functions by Category

**Main Control Flow (3)**
- `0000-MAIN` — Main entry point and control logic
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**Input Processing (4)**
- `1100-RECEIVE-MAP` — Receive BMS map from terminal
- `1100-RECEIVE-MAP-EXIT` — Exit paragraph
- `1200-EDIT-MAP-INPUTS` — Master edit coordinator
- `1200-EDIT-MAP-INPUTS-EXIT` — Exit paragraph

**Field Validation (42)**
- `1205-COMPARE-OLD-NEW` — Compare original vs updated values
- `1205-COMPARE-OLD-NEW-EXIT` — Exit paragraph
- `1210-EDIT-ACCOUNT` — Validate account number
- `1210-EDIT-ACCOUNT-EXIT` — Exit paragraph
- `1215-EDIT-MANDATORY` — Check mandatory field presence
- `1215-EDIT-MANDATORY-EXIT` — Exit paragraph
- `1220-EDIT-YESNO` — Validate Y/N fields
- `1220-EDIT-YESNO-EXIT` — Exit paragraph
- `1225-EDIT-ALPHA-REQD` — Validate required alphabetic fields
- `1225-EDIT-ALPHA-REQD-EXIT` — Exit paragraph
- `1230-EDIT-ALPHANUM-REQD` — Validate required alphanumeric fields
- `1230-EDIT-ALPHANUM-REQD-EXIT` — Exit paragraph
- `1235-EDIT-ALPHA-OPT` — Validate optional alphabetic fields
- `1235-EDIT-ALPHA-OPT-EXIT` — Exit paragraph
- `1240-EDIT-ALPHANUM-OPT` — Validate optional alphanumeric fields
- `1240-EDIT-ALPHANUM-OPT-EXIT` — Exit paragraph
- `1245-EDIT-NUM-REQD` — Validate required numeric fields
- `1245-EDIT-NUM-REQD-EXIT` — Exit paragraph
- `1250-EDIT-SIGNED-9V2` — Validate signed decimal fields
- `1250-EDIT-SIGNED-9V2-EXIT` — Exit paragraph
- `1260-EDIT-US-PHONE-NUM` — Validate US phone number format
- `EDIT-AREA-CODE` — Validate area code (3 digits)
- `EDIT-US-PHONE-PREFIX` — Validate phone prefix (3 digits)
- `EDIT-US-PHONE-LINENUM` — Validate line number (4 digits)
- `EDIT-US-PHONE-EXIT` — Exit phone validation
- `1260-EDIT-US-PHONE-NUM-EXIT` — Exit paragraph
- `1265-EDIT-US-SSN` — Validate US Social Security Number (PII)
- `1265-EDIT-US-SSN-EXIT` — Exit paragraph
- `1270-EDIT-US-STATE-CD` — Validate US state code
- `1270-EDIT-US-STATE-CD-EXIT` — Exit paragraph
- `1275-EDIT-FICO-SCORE` — Validate FICO credit score (300-850)
- `1275-EDIT-FICO-SCORE-EXIT` — Exit paragraph
- `1280-EDIT-US-STATE-ZIP-CD` — Validate US ZIP code
- `1280-EDIT-US-STATE-ZIP-CD-EXIT` — Exit paragraph

**Business Logic (2)**
- `2000-DECIDE-ACTION` — Decide update vs display action
- `2000-DECIDE-ACTION-EXIT` — Exit paragraph

**Screen Management (22)**
- `3000-SEND-MAP` — Send BMS map to terminal
- `3000-SEND-MAP-EXIT` — Exit paragraph
- `3100-SCREEN-INIT` — Initialize screen fields
- `3100-SCREEN-INIT-EXIT` — Exit paragraph
- `3200-SETUP-SCREEN-VARS` — Populate screen variables
- `3200-SETUP-SCREEN-VARS-EXIT` — Exit paragraph
- `3201-SHOW-INITIAL-VALUES` — Display initial field values
- `3201-SHOW-INITIAL-VALUES-EXIT` — Exit paragraph
- `3202-SHOW-ORIGINAL-VALUES` — Display original record values
- `3202-SHOW-ORIGINAL-VALUES-EXIT` — Exit paragraph
- `3203-SHOW-UPDATED-VALUES` — Display updated values
- `3203-SHOW-UPDATED-VALUES-EXIT` — Exit paragraph
- `3250-SETUP-INFOMSG` — Setup information message
- `3250-SETUP-INFOMSG-EXIT` — Exit paragraph
- `3300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `3300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `3310-PROTECT-ALL-ATTRS` — Protect all screen fields
- `3310-PROTECT-ALL-ATTRS-EXIT` — Exit paragraph
- `3320-UNPROTECT-FEW-ATTRS` — Unprotect editable fields
- `3320-UNPROTECT-FEW-ATTRS-EXIT` — Exit paragraph
- `3390-SETUP-INFOMSG-ATTRS` — Setup message attributes
- `3390-SETUP-INFOMSG-ATTRS-EXIT` — Exit paragraph

**Data Access (4)**
- `3400-SEND-SCREEN` — Send screen to terminal
- `3400-SEND-SCREEN-EXIT` — Exit paragraph
- `9000-READ-DATA` — Read account/customer data
- `9000-READ-DATA-EXIT` — Exit paragraph

---

### 2. CBTRN02C — Transaction Posting (731 LOC, 20 functions)

**Complexity:** 0 CICS calls, 61 PERFORM, 0 EVALUATE, 93 IF  
**Business Function:** Post daily transactions to account balances and transaction category balances  
**Test Priority:** CRITICAL — Financial core, writes to 4 VSAM files

#### Functions by Category

**File Management (10)**
- `0000-DALYTRAN-OPEN` — Open daily transaction feed file
- `0400-ACCTFILE-OPEN` — Open account master file
- `9000-DALYTRAN-CLOSE` — Close daily transaction file
- `9100-TRANFILE-CLOSE` — Close transaction master file
- `9200-XREFFILE-CLOSE` — Close card cross-reference file
- `9300-DALYREJS-CLOSE` — Close daily rejects file
- `9400-ACCTFILE-CLOSE` — Close account file
- `9500-TCATBALF-CLOSE` — Close transaction category balance file

**Transaction Processing (7)**
- `2000-POST-TRANSACTION` — Main posting logic
- `2500-WRITE-REJECT-REC` — Write rejected transaction
- `2700-UPDATE-TCATBAL` — Update transaction category balance
- `2700-A-CREATE-TCATBAL-REC` — Create new category balance record
- `2700-B-UPDATE-TCATBAL-REC` — Update existing category balance
- `2800-UPDATE-ACCOUNT-REC` — Update account balance
- `2900-WRITE-TRANSACTION-FILE` — Write posted transaction

**Utilities (3)**
- `Z-GET-DB2-FORMAT-TIMESTAMP` — Get DB2-format timestamp
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 3. COCRDLIC — Credit Card List (1,459 LOC, 34 functions)

**Complexity:** 18 CICS calls, 34 PERFORM, 18 EVALUATE, 122 IF  
**Business Function:** Browse and filter credit card records with pagination  
**Test Priority:** HIGH — Complex pagination logic, PCI data display

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**Screen Initialization (10)**
- `1100-SCREEN-INIT` — Initialize screen
- `1100-SCREEN-INIT-EXIT` — Exit paragraph
- `1200-SCREEN-ARRAY-INIT` — Initialize screen array
- `1200-SCREEN-ARRAY-INIT-EXIT` — Exit paragraph
- `1250-SETUP-ARRAY-ATTRIBS` — Setup array attributes
- `1250-SETUP-ARRAY-ATTRIBS-EXIT` — Exit paragraph
- `1300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `1300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `1400-SETUP-MESSAGE` — Setup message area
- `1400-SETUP-MESSAGE-EXIT` — Exit paragraph

**Input Processing (8)**
- `2000-RECEIVE-MAP` — Receive BMS map
- `2000-RECEIVE-MAP-EXIT` — Exit paragraph
- `2100-RECEIVE-SCREEN` — Receive screen input
- `2100-RECEIVE-SCREEN-EXIT` — Exit paragraph
- `2200-EDIT-INPUTS` — Edit input fields
- `2200-EDIT-INPUTS-EXIT` — Exit paragraph
- `2210-EDIT-ACCOUNT` — Validate account number
- `2210-EDIT-ACCOUNT-EXIT` — Exit paragraph

**Array Processing (4)**
- `2220-EDIT-CARD` — Validate card number
- `2220-EDIT-CARD-EXIT` — Exit paragraph
- `2250-EDIT-ARRAY` — Edit array selections
- `2250-EDIT-ARRAY-EXIT` — Exit paragraph

**Data Access (6)**
- `9000-READ-FORWARD` — Read forward through file
- `9000-READ-FORWARD-EXIT` — Exit paragraph
- `9100-READ-BACKWARDS` — Read backwards through file
- `9100-READ-BACKWARDS-EXIT` — Exit paragraph
- `9500-FILTER-RECORDS` — Apply filter criteria
- `9500-FILTER-RECORDS-EXIT` — Exit paragraph

**Utilities (3)**
- `SEND-PLAIN-TEXT` — Send plain text message
- `SEND-PLAIN-TEXT-EXIT` — Exit paragraph
- `SEND-LONG-TEXT` — Send long text message
- `SEND-LONG-TEXT-EXIT` — Exit paragraph

---

### 4. COCRDUPC — Credit Card Update (1,560 LOC, 42 functions)

**Complexity:** 12 CICS calls, 26 PERFORM, 16 EVALUATE, 148 IF  
**Business Function:** Update credit card master records (PCI-scoped)  
**Test Priority:** CRITICAL — PCI data, card status changes, expiry validation

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**Input Processing (4)**
- `1100-RECEIVE-MAP` — Receive BMS map
- `1100-RECEIVE-MAP-EXIT` — Exit paragraph
- `1200-EDIT-MAP-INPUTS` — Master edit coordinator
- `1200-EDIT-MAP-INPUTS-EXIT` — Exit paragraph

**Field Validation (14)**
- `1210-EDIT-ACCOUNT` — Validate account number
- `1210-EDIT-ACCOUNT-EXIT` — Exit paragraph
- `1220-EDIT-CARD` — Validate card number
- `1220-EDIT-CARD-EXIT` — Exit paragraph
- `1230-EDIT-NAME` — Validate cardholder name
- `1230-EDIT-NAME-EXIT` — Exit paragraph
- `1240-EDIT-CARDSTATUS` — Validate card status (Active/Closed)
- `1240-EDIT-CARDSTATUS-EXIT` — Exit paragraph
- `1250-EDIT-EXPIRY-MON` — Validate expiry month (01-12)
- `1250-EDIT-EXPIRY-MON-EXIT` — Exit paragraph
- `1260-EDIT-EXPIRY-YEAR` — Validate expiry year
- `1260-EDIT-EXPIRY-YEAR-EXIT` — Exit paragraph

**Business Logic (2)**
- `2000-DECIDE-ACTION` — Decide update vs display action
- `2000-DECIDE-ACTION-EXIT` — Exit paragraph

**Screen Management (14)**
- `3000-SEND-MAP` — Send BMS map
- `3000-SEND-MAP-EXIT` — Exit paragraph
- `3100-SCREEN-INIT` — Initialize screen
- `3100-SCREEN-INIT-EXIT` — Exit paragraph
- `3200-SETUP-SCREEN-VARS` — Setup screen variables
- `3200-SETUP-SCREEN-VARS-EXIT` — Exit paragraph
- `3250-SETUP-INFOMSG` — Setup information message
- `3250-SETUP-INFOMSG-EXIT` — Exit paragraph
- `3300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `3300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `3400-SEND-SCREEN` — Send screen to terminal
- `3400-SEND-SCREEN-EXIT` — Exit paragraph

**Data Access (6)**
- `9000-READ-DATA` — Read card data
- `9000-READ-DATA-EXIT` — Exit paragraph
- `9100-GETCARD-BYACCTCARD` — Get card by account+card key
- `9100-GETCARD-BYACCTCARD-EXIT` — Exit paragraph
- `9200-WRITE-PROCESSING` — Write updated card record
- `9200-WRITE-PROCESSING-EXIT` — Exit paragraph

**Change Detection (3)**
- `9300-CHECK-CHANGE-IN-REC` — Check if record changed
- `9300-CHECK-CHANGE-IN-REC-EXIT` — Exit paragraph
- `ABEND-ROUTINE` — Abend handler
- `ABEND-ROUTINE-EXIT` — Exit paragraph

---

### 5. COTRN02C — Online Transaction Add (783 LOC, 17 functions)

**Complexity:** 11 CICS calls, 61 PERFORM, 13 EVALUATE, 14 IF  
**Business Function:** Add new transactions online via CICS terminal  
**Test Priority:** HIGH — Financial transaction entry, real-time validation

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (3)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `VALIDATE-INPUT-KEY-FIELDS` — Validate key fields (account, card)
- `VALIDATE-INPUT-DATA-FIELDS` — Validate transaction data

**Business Logic (1)**
- `ADD-TRANSACTION` — Add transaction to file

**Screen Management (4)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-TRNADD-SCREEN` — Send transaction add screen
- `RECEIVE-TRNADD-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (6)**
- `READ-CXACAIX-FILE` — Read card cross-reference index
- `READ-CCXREF-FILE` — Read card cross-reference file
- `STARTBR-TRANSACT-FILE` — Start browse on transaction file
- `READPREV-TRANSACT-FILE` — Read previous transaction (for ID generation)
- `ENDBR-TRANSACT-FILE` — End browse
- `WRITE-TRANSACT-FILE` — Write new transaction

**Utilities (2)**
- `CLEAR-CURRENT-SCREEN` — Clear screen fields
- `INITIALIZE-ALL-FIELDS` — Initialize working storage fields

---

### 6. COACTVWC — Account View (941 LOC, 26 functions)

**Complexity:** 15 CICS calls, 21 PERFORM, 10 EVALUATE, 57 IF  
**Business Function:** Display account and customer details (read-only)  
**Test Priority:** MEDIUM — Read-only, but displays PII/PCI data

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph (duplicate in source)

**Screen Initialization (8)**
- `1100-SCREEN-INIT` — Initialize screen
- `1100-SCREEN-INIT-EXIT` — Exit paragraph
- `1200-SETUP-SCREEN-VARS` — Setup screen variables
- `1200-SETUP-SCREEN-VARS-EXIT` — Exit paragraph
- `1300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `1300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `1400-SEND-SCREEN` — Send screen to terminal
- `1400-SEND-SCREEN-EXIT` — Exit paragraph

**Input Processing (6)**
- `2000-PROCESS-INPUTS` — Process user inputs
- `2000-PROCESS-INPUTS-EXIT` — Exit paragraph
- `2100-RECEIVE-MAP` — Receive BMS map
- `2100-RECEIVE-MAP-EXIT` — Exit paragraph
- `2200-EDIT-MAP-INPUTS` — Edit input fields
- `2200-EDIT-MAP-INPUTS-EXIT` — Exit paragraph

**Field Validation (2)**
- `2210-EDIT-ACCOUNT` — Validate account number
- `2210-EDIT-ACCOUNT-EXIT` — Exit paragraph

**Data Access (4)**
- `9000-READ-ACCT` — Read account data
- `9000-READ-ACCT-EXIT` — Exit paragraph
- `9200-GETCARDXREF-BYACCT` — Get card cross-reference by account
- `9200-GETCARDXREF-BYACCT-EXIT` — Exit paragraph

**Multi-File Reads (4)**
- `9300-GETACCTDATA-BYACCT` — Get account data by account number
- `9300-GETACCTDATA-BYACCT-EXIT` — Exit paragraph
- `9400-GETCUSTDATA-BYCUST` — Get customer data by customer ID
- `9400-GETCUSTDATA-BYCUST-EXIT` — Exit paragraph

**Utilities (3)**
- `SEND-PLAIN-TEXT` — Send plain text message
- `SEND-PLAIN-TEXT-EXIT` — Exit paragraph
- `SEND-LONG-TEXT` — Send long text message
- `SEND-LONG-TEXT-EXIT` — Exit paragraph
- `ABEND-ROUTINE` — Abend handler

---

### 7. COTRN00C — Transaction List (699 LOC, 17 functions)

**Complexity:** 10 CICS calls, 43 PERFORM, 8 EVALUATE, 26 IF  
**Business Function:** Browse transaction history with pagination  
**Test Priority:** MEDIUM — Read-only transaction display

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (5)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `PROCESS-PF7-KEY` — Process PF7 (page backward)
- `PROCESS-PF8-KEY` — Process PF8 (page forward)
- `PROCESS-PAGE-FORWARD` — Page forward logic
- `PROCESS-PAGE-BACKWARD` — Page backward logic

**Screen Management (6)**
- `POPULATE-TRAN-DATA` — Populate transaction data array
- `INITIALIZE-TRAN-DATA` — Initialize transaction data
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-TRNLST-SCREEN` — Send transaction list screen
- `RECEIVE-TRNLST-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (4)**
- `STARTBR-TRANSACT-FILE` — Start browse on transaction file
- `READNEXT-TRANSACT-FILE` — Read next transaction
- `READPREV-TRANSACT-FILE` — Read previous transaction
- `ENDBR-TRANSACT-FILE` — End browse

---

### 8. COBIL00C — Bill Payment (572 LOC, 17 functions)

**Complexity:** 13 CICS calls, 38 PERFORM, 9 EVALUATE, 10 IF  
**Business Function:** Process bill payments and update account balance  
**Test Priority:** CRITICAL — Financial transaction, account balance update

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (2)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `GET-CURRENT-TIMESTAMP` — Get current timestamp for transaction

**Screen Management (4)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-BILLPAY-SCREEN` — Send bill payment screen
- `RECEIVE-BILLPAY-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (8)**
- `READ-ACCTDAT-FILE` — Read account data file
- `UPDATE-ACCTDAT-FILE` — Update account balance
- `READ-CXACAIX-FILE` — Read card cross-reference index
- `STARTBR-TRANSACT-FILE` — Start browse on transaction file
- `READPREV-TRANSACT-FILE` — Read previous transaction (for ID)
- `ENDBR-TRANSACT-FILE` — End browse
- `WRITE-TRANSACT-FILE` — Write payment transaction

**Utilities (2)**
- `CLEAR-CURRENT-SCREEN` — Clear screen fields
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 9. COCRDSLC — Credit Card View (887 LOC, 31 functions)

**Complexity:** 14 CICS calls, 19 PERFORM, 8 EVALUATE, 68 IF  
**Business Function:** Display credit card details (read-only)  
**Test Priority:** MEDIUM — Read-only, PCI data display

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**Screen Initialization (8)**
- `1100-SCREEN-INIT` — Initialize screen
- `1100-SCREEN-INIT-EXIT` — Exit paragraph
- `1200-SETUP-SCREEN-VARS` — Setup screen variables
- `1200-SETUP-SCREEN-VARS-EXIT` — Exit paragraph
- `1300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `1300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `1400-SEND-SCREEN` — Send screen to terminal
- `1400-SEND-SCREEN-EXIT` — Exit paragraph

**Input Processing (6)**
- `2000-PROCESS-INPUTS` — Process user inputs
- `2000-PROCESS-INPUTS-EXIT` — Exit paragraph
- `2100-RECEIVE-MAP` — Receive BMS map
- `2100-RECEIVE-MAP-EXIT` — Exit paragraph
- `2200-EDIT-MAP-INPUTS` — Edit input fields
- `2200-EDIT-MAP-INPUTS-EXIT` — Exit paragraph

**Field Validation (4)**
- `2210-EDIT-ACCOUNT` — Validate account number
- `2210-EDIT-ACCOUNT-EXIT` — Exit paragraph
- `2220-EDIT-CARD` — Validate card number
- `2220-EDIT-CARD-EXIT` — Exit paragraph

**Data Access (6)**
- `9000-READ-DATA` — Read card data
- `9000-READ-DATA-EXIT` — Exit paragraph
- `9100-GETCARD-BYACCTCARD` — Get card by account+card key
- `9100-GETCARD-BYACCTCARD-EXIT` — Exit paragraph
- `9150-GETCARD-BYACCT` — Get card by account only
- `9150-GETCARD-BYACCT-EXIT` — Exit paragraph

**Utilities (4)**
- `SEND-LONG-TEXT` — Send long text message
- `SEND-LONG-TEXT-EXIT` — Exit paragraph
- `SEND-PLAIN-TEXT` — Send plain text message
- `SEND-PLAIN-TEXT-EXIT` — Exit paragraph
- `ABEND-ROUTINE` — Abend handler

---

### 10. COUSR00C — User List (695 LOC, 17 functions)

**Complexity:** 11 CICS calls, 41 PERFORM, 8 EVALUATE, 25 IF  
**Business Function:** Browse user security records with pagination (Admin only)  
**Test Priority:** HIGH — Security-sensitive, admin function

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (5)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `PROCESS-PF7-KEY` — Process PF7 (page backward)
- `PROCESS-PF8-KEY` — Process PF8 (page forward)
- `PROCESS-PAGE-FORWARD` — Page forward logic
- `PROCESS-PAGE-BACKWARD` — Page backward logic

**Screen Management (6)**
- `POPULATE-USER-DATA` — Populate user data array
- `INITIALIZE-USER-DATA` — Initialize user data
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-USRLST-SCREEN` — Send user list screen
- `RECEIVE-USRLST-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (4)**
- `STARTBR-USER-SEC-FILE` — Start browse on user security file
- `READNEXT-USER-SEC-FILE` — Read next user record
- `READPREV-USER-SEC-FILE` — Read previous user record
- `ENDBR-USER-SEC-FILE` — End browse

---

### 11. CORPT00C — Transaction Report Submit (649 LOC, 10 functions)

**Complexity:** 7 CICS calls, 34 PERFORM, 5 EVALUATE, 20 IF  
**Business Function:** Submit transaction report JCL to internal reader  
**Test Priority:** MEDIUM — Report generation trigger

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key

**JCL Submission (2)**
- `SUBMIT-JOB-TO-INTRDR` — Submit JCL to internal reader
- `WIRTE-JOBSUB-TDQ` — Write job submission to transient data queue

**Screen Management (4)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-TRNRPT-SCREEN` — Send transaction report screen
- `RETURN-TO-CICS` — Return to CICS
- `RECEIVE-TRNRPT-SCREEN` — Receive screen input

**Utilities (2)**
- `POPULATE-HEADER-INFO` — Populate screen header
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 12. COMEN01C — Main Menu (308 LOC, 7 functions)

**Complexity:** 7 CICS calls, 13 PERFORM, 3 EVALUATE, 7 IF  
**Business Function:** Display main menu and route to selected transaction  
**Test Priority:** LOW — Simple menu navigation

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key and route to selected transaction

**Screen Management (4)**
- `RETURN-TO-SIGNON-SCREEN` — Return to signon screen
- `SEND-MENU-SCREEN` — Send menu screen
- `RECEIVE-MENU-SCREEN` — Receive menu selection
- `POPULATE-HEADER-INFO` — Populate screen header

**Menu Building (1)**
- `BUILD-MENU-OPTIONS` — Build menu options based on user role

---

### 13. COADM01C — Admin Menu (288 LOC, 8 functions)

**Complexity:** 7 CICS calls, 14 PERFORM, 4 EVALUATE, 11 IF  
**Business Function:** Display admin menu and route to admin transactions  
**Test Priority:** LOW — Simple admin menu navigation

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key and route to selected transaction

**Screen Management (4)**
- `RETURN-TO-SIGNON-SCREEN` — Return to signon screen
- `SEND-MENU-SCREEN` — Send admin menu screen
- `RECEIVE-MENU-SCREEN` — Receive menu selection
- `POPULATE-HEADER-INFO` — Populate screen header

**Menu Building (1)**
- `BUILD-MENU-OPTIONS` — Build admin menu options

**Error Handling (1)**
- `PGMIDERR-ERR-PARA` — Handle program ID error

---

### 14. COUSR01C — User Add (299 LOC, 11 functions)

**Complexity:** 5 CICS calls, 20 PERFORM, 3 EVALUATE, 4 IF  
**Business Function:** Add new user security records (Admin only)  
**Test Priority:** HIGH — Security-sensitive, creates user accounts

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key

**Screen Management (5)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-USRADD-SCREEN` — Send user add screen
- `RECEIVE-USRADD-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header
- `CLEAR-CURRENT-SCREEN` — Clear screen fields

**Data Access (1)**
- `WRITE-USER-SEC-FILE` — Write new user security record

**Utilities (1)**
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 15. COUSR02C — User Update (414 LOC, 11 functions)

**Complexity:** 6 CICS calls, 31 PERFORM, 5 EVALUATE, 13 IF  
**Business Function:** Update user security records (Admin only)  
**Test Priority:** HIGH — Security-sensitive, modifies user accounts

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (2)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `UPDATE-USER-INFO` — Update user information

**Screen Management (5)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-USRUPD-SCREEN` — Send user update screen
- `RECEIVE-USRUPD-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header
- `CLEAR-CURRENT-SCREEN` — Clear screen fields

**Data Access (2)**
- `READ-USER-SEC-FILE` — Read user security record
- `UPDATE-USER-SEC-FILE` — Update user security record

**Utilities (1)**
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 16. COUSR03C — User Delete (359 LOC, 11 functions)

**Complexity:** 6 CICS calls, 26 PERFORM, 5 EVALUATE, 8 IF  
**Business Function:** Delete user security records (Admin only)  
**Test Priority:** HIGH — Security-sensitive, removes user accounts

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (2)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `DELETE-USER-INFO` — Delete user information

**Screen Management (5)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-USRDEL-SCREEN` — Send user delete screen
- `RECEIVE-USRDEL-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header
- `CLEAR-CURRENT-SCREEN` — Clear screen fields

**Data Access (2)**
- `READ-USER-SEC-FILE` — Read user security record
- `DELETE-USER-SEC-FILE` — Delete user security record

**Utilities (1)**
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 17. COTRN01C — Transaction View (330 LOC, 11 functions)

**Complexity:** 5 CICS calls, 17 PERFORM, 3 EVALUATE, 7 IF  
**Business Function:** Display single transaction details (read-only)  
**Test Priority:** LOW — Simple read-only display

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key

**Screen Management (5)**
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-TRNVIEW-SCREEN` — Send transaction view screen
- `RECEIVE-TRNVIEW-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header
- `CLEAR-CURRENT-SCREEN` — Clear screen fields

**Data Access (1)**
- `READ-TRANSACT-FILE` — Read transaction record

**Utilities (1)**
- `INITIALIZE-ALL-FIELDS` — Initialize working storage

---

### 18. COSGN00C — Signon (260 LOC, 6 functions)

**Complexity:** 10 CICS calls, 11 PERFORM, 3 EVALUATE, 4 IF  
**Business Function:** User authentication and session initialization  
**Test Priority:** CRITICAL — Security gateway, authentication

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (1)**
- `PROCESS-ENTER-KEY` — Process ENTER key and authenticate

**Screen Management (3)**
- `SEND-SIGNON-SCREEN` — Send signon screen
- `SEND-PLAIN-TEXT` — Send plain text message
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (1)**
- `READ-USER-SEC-FILE` — Read user security file for authentication

---

### 19. CSUTLDTC — Date Utility (157 LOC, 2 functions)

**Complexity:** 0 CICS calls, 1 PERFORM, 2 EVALUATE, 0 IF  
**Business Function:** Date conversion utility (YYYYMMDD to YYYY-MM-DD)  
**Test Priority:** LOW — Simple utility function

#### Functions by Category

**Main Control (2)**
- `A000-MAIN` — Main entry point
- `A000-MAIN-EXIT` — Exit paragraph

---

### 20. COBSWAIT — Wait Utility (41 LOC, 0 functions)

**Complexity:** 0 CICS calls, 0 PERFORM, 0 EVALUATE, 0 IF  
**Business Function:** Delay/wait utility for batch processing  
**Test Priority:** LOW — Trivial utility

#### Functions by Category

**No paragraphs** — Uses inline COBOL ACCEPT or assembler call for wait

---

## Batch Programs

### 21. CBACT04C — Interest Calculation (652 LOC, 14 functions)

**Complexity:** 0 CICS calls, 56 PERFORM, 0 EVALUATE, 86 IF  
**Business Function:** Calculate interest and fees on account balances  
**Test Priority:** CRITICAL — Financial calculation, complex business rules

#### Functions by Category

**File Management (6)**
- `0000-TCATBALF-OPEN` — Open transaction category balance file
- `0400-TRANFILE-OPEN` — Open transaction file
- `9000-TCATBALF-CLOSE` — Close transaction category balance file
- `9100-XREFFILE-CLOSE` — Close cross-reference file
- `9200-DISCGRP-CLOSE` — Close discount group file
- `9300-ACCTFILE-CLOSE` — Close account file
- `9400-TRANFILE-CLOSE` — Close transaction file

**Interest Calculation (5)**
- `1100-GET-ACCT-DATA` — Get account data
- `1110-GET-XREF-DATA` — Get cross-reference data
- `1200-GET-INTEREST-RATE` — Get interest rate for account
- `1200-A-GET-DEFAULT-INT-RATE` — Get default interest rate
- `1300-COMPUTE-INTEREST` — Compute interest amount

**Transaction Writing (2)**
- `1300-B-WRITE-TX` — Write interest transaction
- `1400-COMPUTE-FEES` — Compute fees

**Utilities (3)**
- `Z-GET-DB2-FORMAT-TIMESTAMP` — Get DB2-format timestamp
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 22. CBTRN03C — Transaction Detail Report (649 LOC, 14 functions)

**Complexity:** 0 CICS calls, 72 PERFORM, 4 EVALUATE, 75 IF  
**Business Function:** Generate transaction detail report with totals  
**Test Priority:** MEDIUM — Reporting, complex totaling logic

#### Functions by Category

**Report Writing (7)**
- `1100-WRITE-TRANSACTION-REPORT` — Write transaction report
- `1110-WRITE-PAGE-TOTALS` — Write page totals
- `1120-WRITE-ACCOUNT-TOTALS` — Write account totals
- `1110-WRITE-GRAND-TOTALS` — Write grand totals (duplicate paragraph name)
- `1120-WRITE-HEADERS` — Write report headers (duplicate paragraph name)
- `1111-WRITE-REPORT-REC` — Write report record
- `1120-WRITE-DETAIL` — Write detail line (duplicate paragraph name)

**File Management (7)**
- `0000-TRANFILE-OPEN` — Open transaction file
- `0400-TRANCATG-OPEN` — Open transaction category file
- `9000-TRANFILE-CLOSE` — Close transaction file
- `9100-REPTFILE-CLOSE` — Close report file
- `9200-CARDXREF-CLOSE` — Close card cross-reference file
- `9300-TRANTYPE-CLOSE` — Close transaction type file
- `9400-TRANCATG-CLOSE` — Close transaction category file
- `9500-DATEPARM-CLOSE` — Close date parameter file

**Utilities (2)**
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 23. CBEXPORT — Data Export (582 LOC, 18 functions)

**Complexity:** 0 CICS calls, 45 PERFORM, 0 EVALUATE, 16 IF  
**Business Function:** Export VSAM data to sequential file for migration  
**Test Priority:** MEDIUM — Data migration utility

#### Functions by Category

**Main Control (1)**
- `0000-MAIN-PROCESSING` — Main processing loop

**File Management (1)**
- `1100-OPEN-FILES` — Open all input/output files

**Customer Export (3)**
- `2000-EXPORT-CUSTOMERS` — Export customer records
- `2100-READ-CUSTOMER-RECORD` — Read customer record
- `2200-CREATE-CUSTOMER-EXP-REC` — Create customer export record

**Account Export (3)**
- `3000-EXPORT-ACCOUNTS` — Export account records
- `3100-READ-ACCOUNT-RECORD` — Read account record
- `3200-CREATE-ACCOUNT-EXP-REC` — Create account export record

**Cross-Reference Export (3)**
- `4000-EXPORT-XREFS` — Export cross-reference records
- `4100-READ-XREF-RECORD` — Read cross-reference record
- `4200-CREATE-XREF-EXPORT-RECORD` — Create cross-reference export record

**Transaction Export (3)**
- `5000-EXPORT-TRANSACTIONS` — Export transaction records
- `5100-READ-TRANSACTION-RECORD` — Read transaction record
- `5200-CREATE-TRAN-EXP-REC` — Create transaction export record

**Card Export (3)**
- `5500-EXPORT-CARDS` — Export card records
- `5600-READ-CARD-RECORD` — Read card record
- `5700-CREATE-CARD-EXPORT-RECORD` — Create card export record

**Finalization (2)**
- `6000-FINALIZE` — Close files and finalize
- `9999-ABEND-PROGRAM` — Abend handler

---

### 24. CBIMPORT — Data Import (487 LOC, 16 functions)

**Complexity:** 0 CICS calls, 29 PERFORM, 1 EVALUATE, 14 IF  
**Business Function:** Import sequential file data to VSAM for migration  
**Test Priority:** MEDIUM — Data migration utility

#### Functions by Category

**Main Control (1)**
- `0000-MAIN-PROCESSING` — Main processing loop

**File Management (1)**
- `1100-OPEN-FILES` — Open all input/output files

**Record Processing (9)**
- `2000-PROCESS-EXPORT-FILE` — Process export file
- `2100-READ-EXPORT-RECORD` — Read export record
- `2200-PROCESS-RECORD-BY-TYPE` — Route by record type
- `2300-PROCESS-CUSTOMER-RECORD` — Process customer record
- `2400-PROCESS-ACCOUNT-RECORD` — Process account record
- `2500-PROCESS-XREF-RECORD` — Process cross-reference record
- `2600-PROCESS-TRAN-RECORD` — Process transaction record
- `2650-PROCESS-CARD-RECORD` — Process card record
- `2700-PROCESS-UNKNOWN-RECORD` — Process unknown record type

**Error Handling (2)**
- `2750-WRITE-ERROR` — Write error record
- `3000-VALIDATE-IMPORT` — Validate import results

**Finalization (2)**
- `4000-FINALIZE` — Close files and finalize
- `9999-ABEND-PROGRAM` — Abend handler

---

### 25. CBTRN01C — Daily Transaction Feed Loader (494 LOC, 13 functions)

**Complexity:** 0 CICS calls, 42 PERFORM, 0 EVALUATE, 33 IF  
**Business Function:** Load daily transaction feed from external source  
**Test Priority:** HIGH — Data ingestion, transaction feed validation

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Business Logic (2)**
- `2000-LOOKUP-XREF` — Lookup card cross-reference
- `3000-READ-ACCOUNT` — Read account data

**File Management (7)**
- `0000-DALYTRAN-OPEN` — Open daily transaction file
- `0400-ACCTFILE-OPEN` — Open account file
- `9000-DALYTRAN-CLOSE` — Close daily transaction file
- `9100-CUSTFILE-CLOSE` — Close customer file
- `9200-XREFFILE-CLOSE` — Close cross-reference file
- `9300-CARDFILE-CLOSE` — Close card file
- `9400-ACCTFILE-CLOSE` — Close account file
- `9500-TRANFILE-CLOSE` — Close transaction file

**Utilities (2)**
- `Z-ABEND-PROGRAM` — Abend handler
- `Z-DISPLAY-IO-STATUS` — Display file I/O status

---

### 26. CBSTM03A — Statement Generation Driver (924 LOC, 23 functions)

**Complexity:** 0 CICS calls, 29 PERFORM, 5 EVALUATE, 15 IF  
**Business Function:** Generate HTML customer statements (driver program)  
**Test Priority:** MEDIUM — Statement generation, HTML output

#### Functions by Category

**Main Control (2)**
- `0000-START` — Main entry point
- `9999-GOBACK` — Exit program

**Data Retrieval (3)**
- `2000-CUSTFILE-GET` — Get customer data
- `3000-ACCTFILE-GET` — Get account data
- `4000-TRNXFILE-GET` — Get transaction data

**Statement Generation (4)**
- `5000-CREATE-STATEMENT` — Create statement
- `5100-WRITE-HTML-HEADER` — Write HTML header
- `5100-EXIT` — Exit paragraph
- `5200-WRITE-HTML-NMADBS` — Write HTML name/address/balance section
- `5200-EXIT` — Exit paragraph

**Transaction Writing (1)**
- `6000-WRITE-TRANS` — Write transaction details

**File Management (9)**
- `8100-FILE-OPEN` — Open files
- `8100-TRNXFILE-OPEN` — Open transaction file
- `8200-XREFFILE-OPEN` — Open cross-reference file
- `8300-CUSTFILE-OPEN` — Open customer file
- `8400-ACCTFILE-OPEN` — Open account file
- `8500-READTRNX-READ` — Read transaction
- `8599-EXIT` — Exit paragraph
- `9100-TRNXFILE-CLOSE` — Close transaction file
- `9200-XREFFILE-CLOSE` — Close cross-reference file
- `9300-CUSTFILE-CLOSE` — Close customer file
- `9400-ACCTFILE-CLOSE` — Close account file

**Error Handling (1)**
- `9999-ABEND-PROGRAM` — Abend handler

---

### 27. CBSTM03B — Statement Generation Worker (230 LOC, 13 functions)

**Complexity:** 0 CICS calls, 4 PERFORM, 1 EVALUATE, 12 IF  
**Business Function:** Generate statements for specific account range (worker)  
**Test Priority:** MEDIUM — Statement generation worker

#### Functions by Category

**Main Control (2)**
- `0000-START` — Main entry point
- `9999-GOBACK` — Exit program

**File Processing (8)**
- `1000-TRNXFILE-PROC` — Process transaction file
- `1900-EXIT` — Exit paragraph
- `1999-EXIT` — Exit paragraph
- `2000-XREFFILE-PROC` — Process cross-reference file
- `2900-EXIT` — Exit paragraph
- `2999-EXIT` — Exit paragraph
- `3000-CUSTFILE-PROC` — Process customer file
- `3900-EXIT` — Exit paragraph
- `3999-EXIT` — Exit paragraph
- `4000-ACCTFILE-PROC` — Process account file
- `4900-EXIT` — Exit paragraph
- `4999-EXIT` — Exit paragraph

---

### 28. CBACT01C — Account File Loader (430 LOC, 12 functions)

**Complexity:** 0 CICS calls, 35 PERFORM, 0 EVALUATE, 22 IF  
**Business Function:** Load account master file from sequential input  
**Test Priority:** MEDIUM — Data loading utility

#### Functions by Category

**Record Processing (4)**
- `1100-DISPLAY-ACCT-RECORD` — Display account record
- `1300-POPUL-ACCT-RECORD` — Populate account record
- `1350-WRITE-ACCT-RECORD` — Write account record
- `1400-POPUL-ARRAY-RECORD` — Populate array record
- `1450-WRITE-ARRY-RECORD` — Write array record

**File Management (5)**
- `0000-ACCTFILE-OPEN` — Open account file
- `2000-OUTFILE-OPEN` — Open output file
- `3000-ARRFILE-OPEN` — Open array file
- `4000-VBRFILE-OPEN` — Open VBR file
- `9000-ACCTFILE-CLOSE` — Close account file

**Utilities (2)**
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 29. CBACT02C — Card File Loader (178 LOC, 4 functions)

**Complexity:** 0 CICS calls, 10 PERFORM, 0 EVALUATE, 22 IF  
**Business Function:** Load card master file from sequential input  
**Test Priority:** MEDIUM — Data loading utility

#### Functions by Category

**File Management (2)**
- `0000-CARDFILE-OPEN` — Open card file
- `9000-CARDFILE-CLOSE` — Close card file

**Utilities (2)**
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 30. CBACT03C — Cross-Reference File Loader (178 LOC, 4 functions)

**Complexity:** 0 CICS calls, 10 PERFORM, 0 EVALUATE, 22 IF  
**Business Function:** Load card cross-reference file from sequential input  
**Test Priority:** MEDIUM — Data loading utility

#### Functions by Category

**File Management (2)**
- `0000-XREFFILE-OPEN` — Open cross-reference file
- `9000-XREFFILE-CLOSE` — Close cross-reference file

**Utilities (2)**
- `9999-ABEND-PROGRAM` — Abend handler
- `9910-DISPLAY-IO-STATUS` — Display file I/O status

---

### 31. CBCUS01C — Customer File Loader (178 LOC, 4 functions)

**Complexity:** 0 CICS calls, 10 PERFORM, 0 EVALUATE, 11 IF  
**Business Function:** Load customer master file from sequential input  
**Test Priority:** MEDIUM — Data loading utility

#### Functions by Category

**File Management (2)**
- `0000-CUSTFILE-OPEN` — Open customer file
- `9000-CUSTFILE-CLOSE` — Close customer file

**Utilities (2)**
- `Z-ABEND-PROGRAM` — Abend handler
- `Z-DISPLAY-IO-STATUS` — Display file I/O status

---

## Optional Module Programs

### Authorization Module (IMS/DB2/MQ)

### 32. COPAUA0C — Authorization MQ Trigger (1,026 LOC, 38 functions)

**Complexity:** 12 CICS calls, 38 PERFORM, 10 EVALUATE, 51 IF  
**Business Function:** Process authorization requests from MQ, make approval decision, write to DB2  
**Test Priority:** CRITICAL — Real-time authorization, fraud detection, DB2/IMS/MQ integration

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Initialization (4)**
- `1100-OPEN-REQUEST-QUEUE` — Open MQ request queue
- `1100-EXIT` — Exit paragraph
- `1200-SCHEDULE-PSB` — Schedule IMS PSB
- `1200-EXIT` — Exit paragraph

**Main Processing (4)**
- `2000-MAIN-PROCESS` — Main processing loop
- `2000-EXIT` — Exit paragraph
- `2100-EXTRACT-REQUEST-MSG` — Extract request message
- `2100-EXIT` — Exit paragraph

**MQ Operations (2)**
- `3100-READ-REQUEST-MQ` — Read request from MQ
- `3100-EXIT` — Exit paragraph

**Authorization Logic (12)**
- `5000-PROCESS-AUTH` — Process authorization request
- `5000-EXIT` — Exit paragraph
- `5100-READ-XREF-RECORD` — Read card cross-reference
- `5100-EXIT` — Exit paragraph
- `5200-READ-ACCT-RECORD` — Read account record
- `5200-EXIT` — Exit paragraph
- `5300-READ-CUST-RECORD` — Read customer record
- `5300-EXIT` — Exit paragraph
- `5500-READ-AUTH-SUMMRY` — Read authorization summary (IMS)
- `5500-EXIT` — Exit paragraph
- `5600-READ-PROFILE-DATA` — Read profile data
- `5600-EXIT` — Exit paragraph

**Decision Making (2)**
- `6000-MAKE-DECISION` — Make authorization decision
- `6000-EXIT` — Exit paragraph

**Response Handling (2)**
- `7100-SEND-RESPONSE` — Send response to MQ
- `7100-EXIT` — Exit paragraph

**DB2 Operations (4)**
- `8000-WRITE-AUTH-TO-DB` — Write authorization to DB2
- `8000-EXIT` — Exit paragraph
- `8400-UPDATE-SUMMARY` — Update authorization summary
- `8400-EXIT` — Exit paragraph
- `8500-INSERT-AUTH` — Insert authorization record
- `8500-EXIT` — Exit paragraph

**Termination (6)**
- `9000-TERMINATE` — Terminate processing
- `9000-EXIT` — Exit paragraph
- `9100-CLOSE-REQUEST-QUEUE` — Close MQ request queue
- `9100-EXIT` — Exit paragraph
- `9500-LOG-ERROR` — Log error
- `9500-EXIT` — Exit paragraph
- `9990-END-ROUTINE` — End routine
- `9990-EXIT` — Exit paragraph

---

### 33. COPAUS0C — Authorization Summary List (1,032 LOC, 21 functions)

**Complexity:** 10 CICS calls, 46 PERFORM, 11 EVALUATE, 25 IF  
**Business Function:** Browse authorization summary records (IMS DB)  
**Test Priority:** HIGH — IMS database access, authorization history

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (6)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `GATHER-DETAILS` — Gather authorization details
- `PROCESS-PF7-KEY` — Process PF7 (page backward)
- `PROCESS-PF8-KEY` — Process PF8 (page forward)
- `PROCESS-PAGE-FORWARD` — Page forward logic
- `GET-AUTHORIZATIONS` — Get authorization records

**Data Retrieval (2)**
- `REPOSITION-AUTHORIZATIONS` — Reposition in authorization list
- `POPULATE-AUTH-LIST` — Populate authorization list

**Screen Management (5)**
- `INITIALIZE-AUTH-DATA` — Initialize authorization data
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-PAULST-SCREEN` — Send authorization list screen
- `RECEIVE-PAULST-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Account Details (4)**
- `GATHER-ACCOUNT-DETAILS` — Gather account details
- `GETCARDXREF-BYACCT` — Get card cross-reference by account
- `GETACCTDATA-BYACCT` — Get account data by account
- `GETCUSTDATA-BYCUST` — Get customer data by customer

**IMS Operations (2)**
- `GET-AUTH-SUMMARY` — Get authorization summary from IMS
- `SCHEDULE-PSB` — Schedule IMS PSB

---

### 34. COPAUS1C — Authorization Detail View (604 LOC, 19 functions)

**Complexity:** 8 CICS calls, 34 PERFORM, 5 EVALUATE, 17 IF  
**Business Function:** View/update authorization details, mark as fraud (DB2)  
**Test Priority:** HIGH — Fraud marking, DB2 update, IMS read

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Input Processing (3)**
- `PROCESS-ENTER-KEY` — Process ENTER key
- `MARK-AUTH-FRAUD` — Mark authorization as fraud
- `PROCESS-PF8-KEY` — Process PF8 (next record)

**Screen Management (5)**
- `POPULATE-AUTH-DETAILS` — Populate authorization details
- `RETURN-TO-PREV-SCREEN` — Return to previous screen
- `SEND-AUTHVIEW-SCREEN` — Send authorization view screen
- `RECEIVE-AUTHVIEW-SCREEN` — Receive screen input
- `POPULATE-HEADER-INFO` — Populate screen header

**Data Access (5)**
- `READ-AUTH-RECORD` — Read authorization record (IMS)
- `READ-NEXT-AUTH-RECORD` — Read next authorization record
- `UPDATE-AUTH-DETAILS` — Update authorization details (DB2)
- `TAKE-SYNCPOINT` — Take syncpoint
- `ROLL-BACK` — Roll back transaction

**IMS Operations (1)**
- `SCHEDULE-PSB` — Schedule IMS PSB

---

### 35. COPAUS2C — Fraud Update Batch (244 LOC, 2 functions)

**Complexity:** 3 CICS calls, 1 PERFORM, 0 EVALUATE, 6 IF  
**Business Function:** Batch update fraud flags in DB2  
**Test Priority:** MEDIUM — DB2 batch update

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**Business Logic (1)**
- `FRAUD-UPDATE` — Update fraud flags

---

### 36. CBPAUP0C — Authorization Purge Batch (386 LOC, 14 functions)

**Complexity:** 0 CICS calls, 17 PERFORM, 2 EVALUATE, 17 IF  
**Business Function:** Purge expired authorization records from IMS DB  
**Test Priority:** MEDIUM — IMS database purge, housekeeping

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**IMS Processing (6)**
- `2000-FIND-NEXT-AUTH-SUMMARY` — Find next authorization summary
- `2000-EXIT` — Exit paragraph
- `3000-FIND-NEXT-AUTH-DTL` — Find next authorization detail
- `3000-EXIT` — Exit paragraph
- `4000-CHECK-IF-EXPIRED` — Check if authorization expired
- `4000-EXIT` — Exit paragraph

**Delete Operations (4)**
- `5000-DELETE-AUTH-DTL` — Delete authorization detail
- `5000-EXIT` — Exit paragraph
- `6000-DELETE-AUTH-SUMMARY` — Delete authorization summary
- `6000-EXIT` — Exit paragraph

**Utilities (3)**
- `9000-TAKE-CHECKPOINT` — Take checkpoint
- `9000-EXIT` — Exit paragraph
- `9999-ABEND` — Abend handler
- `9999-EXIT` — Exit paragraph

---

### 37. DBUNLDGS — IMS DB Unload to GSAM (366 LOC, 10 functions)

**Complexity:** 0 CICS calls, 12 PERFORM, 0 EVALUATE, 19 IF  
**Business Function:** Unload IMS database to GSAM sequential file  
**Test Priority:** LOW — IMS database backup utility

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**IMS Processing (4)**
- `2000-FIND-NEXT-AUTH-SUMMARY` — Find next authorization summary
- `2000-EXIT` — Exit paragraph
- `3000-FIND-NEXT-AUTH-DTL` — Find next authorization detail
- `3000-EXIT` — Exit paragraph

**GSAM Operations (4)**
- `3100-INSERT-PARENT-SEG-GSAM` — Insert parent segment to GSAM
- `3100-EXIT` — Exit paragraph
- `3200-INSERT-CHILD-SEG-GSAM` — Insert child segment to GSAM
- `3200-EXIT` — Exit paragraph

**File Management (2)**
- `4000-FILE-CLOSE` — Close files
- `4000-EXIT` — Exit paragraph

**Utilities (2)**
- `9999-ABEND` — Abend handler
- `9999-EXIT` — Exit paragraph

---

### 38. PAUDBLOD — IMS DB Load from GSAM (369 LOC, 12 functions)

**Complexity:** 0 CICS calls, 12 PERFORM, 0 EVALUATE, 26 IF  
**Business Function:** Load IMS database from GSAM sequential file  
**Test Priority:** LOW — IMS database restore utility

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**GSAM Read Operations (4)**
- `2000-READ-ROOT-SEG-FILE` — Read root segment from GSAM
- `2000-EXIT` — Exit paragraph
- `3000-READ-CHILD-SEG-FILE` — Read child segment from GSAM
- `3000-EXIT` — Exit paragraph

**IMS Insert Operations (6)**
- `2100-INSERT-ROOT-SEG` — Insert root segment to IMS
- `2100-EXIT` — Exit paragraph
- `3100-INSERT-CHILD-SEG` — Insert child segment to IMS
- `3100-EXIT` — Exit paragraph
- `3200-INSERT-IMS-CALL` — Insert IMS call
- `3200-EXIT` — Exit paragraph

**File Management (2)**
- `4000-FILE-CLOSE` — Close files
- `4000-EXIT` — Exit paragraph

**Utilities (2)**
- `9999-ABEND` — Abend handler
- `9999-EXIT` — Exit paragraph

---

### 39. PAUDBUNL — IMS DB Unload Alternate (317 LOC, 8 functions)

**Complexity:** 0 CICS calls, 8 PERFORM, 0 EVALUATE, 17 IF  
**Business Function:** Alternate IMS database unload utility  
**Test Priority:** LOW — IMS database backup utility

#### Functions by Category

**Main Control (1)**
- `MAIN-PARA` — Main entry point

**IMS Processing (4)**
- `2000-FIND-NEXT-AUTH-SUMMARY` — Find next authorization summary
- `2000-EXIT` — Exit paragraph
- `3000-FIND-NEXT-AUTH-DTL` — Find next authorization detail
- `3000-EXIT` — Exit paragraph

**File Management (2)**
- `4000-FILE-CLOSE` — Close files
- `4000-EXIT` — Exit paragraph

**Utilities (2)**
- `9999-ABEND` — Abend handler
- `9999-EXIT` — Exit paragraph

---

### Transaction Type Module (DB2)

### 40. COTRTLIC — Transaction Type List/Delete (2,098 LOC, 59 functions)

**Complexity:** 12 CICS calls, 63 PERFORM, 16 EVALUATE, 89 IF  
**Business Function:** Browse, filter, update, delete transaction type records (DB2)  
**Test Priority:** HIGH — DB2 CRUD, cursor management, complex pagination

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**Input Processing (6)**
- `1000-RECEIVE-MAP` — Receive BMS map
- `1000-RECEIVE-MAP-EXIT` — Exit paragraph
- `1100-RECEIVE-SCREEN` — Receive screen input
- `1100-RECEIVE-SCREEN-EXIT` — Exit paragraph
- `1200-EDIT-INPUTS` — Edit input fields
- `1200-EDIT-INPUTS-EXIT` — Exit paragraph

**Array Editing (6)**
- `1210-EDIT-ARRAY` — Edit array selections
- `1210-EDIT-ARRAY-EXIT` — Exit paragraph
- `1211-EDIT-ARRAY-DESC` — Edit array description
- `1211-EDIT-ARRAY-DESC-EXIT` — Exit paragraph
- `1220-EDIT-TYPECD` — Edit transaction type code
- `1220-EDIT-TYPECD-EXIT` — Exit paragraph

**Field Validation (6)**
- `1230-EDIT-DESC` — Edit description
- `1230-EDIT-DESC-EXIT` — Exit paragraph
- `1240-EDIT-ALPHANUM-REQD` — Edit required alphanumeric field
- `1240-EDIT-ALPHANUM-REQD-EXIT` — Exit paragraph
- `1290-CROSS-EDITS` — Cross-field edits
- `1290-CROSS-EDITS-EXIT` — Exit paragraph

**Screen Management (14)**
- `2000-SEND-MAP-EXIT` — Exit paragraph
- `2100-SCREEN-INIT` — Initialize screen
- `2100-SCREEN-INIT-EXIT` — Exit paragraph
- `2200-SETUP-ARRAY-ATTRIBS` — Setup array attributes
- `2200-SETUP-ARRAY-ATTRIBS-EXIT` — Exit paragraph
- `2300-SCREEN-ARRAY-INIT` — Initialize screen array
- `2300-SCREEN-ARRAY-INIT-EXIT` — Exit paragraph
- `2400-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `2400-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `2500-SETUP-MESSAGE` — Setup message
- `2500-SETUP-MESSAGE-EXIT` — Exit paragraph
- `2600-SEND-SCREEN` — Send screen to terminal
- `2600-SEND-SCREEN-EXIT` — Exit paragraph

**DB2 Read Operations (8)**
- `8000-READ-FORWARD` — Read forward through DB2 cursor
- `8000-READ-FORWARD-EXIT` — Exit paragraph
- `8100-READ-BACKWARDS` — Read backwards through DB2 cursor
- `8100-READ-BACKWARDS-EXIT` — Exit paragraph
- `9400-OPEN-FORWARD-CURSOR` — Open forward cursor
- `9400-OPEN-FORWARD-CURSOR-EXIT` — Exit paragraph
- `9450-CLOSE-FORWARD-CURSOR` — Close forward cursor
- `9450-CLOSE-FORWARD-CURSOR-EXIT` — Exit paragraph

**DB2 Cursor Management (4)**
- `9500-OPEN-BACKWARD-CURSOR` — Open backward cursor
- `9500-OPEN-BACKWARD-CURSOR-EXIT` — Exit paragraph
- `9550-CLOSE-BACK-CURSOR` — Close backward cursor
- `9550-CLOSE-BACK-CURSOR-EXIT` — Exit paragraph

**Filtering (2)**
- `9100-CHECK-FILTERS` — Check filter criteria
- `9100-CHECK-FILTERS-EXIT` — Exit paragraph

**DB2 Write Operations (4)**
- `9200-UPDATE-RECORD` — Update DB2 record
- `9200-UPDATE-RECORD-EXIT` — Exit paragraph
- `9300-DELETE-RECORD` — Delete DB2 record
- `9300-DELETE-RECORD-EXIT` — Exit paragraph

**Utilities (4)**
- `SEND-PLAIN-TEXT` — Send plain text message
- `SEND-PLAIN-TEXT-EXIT` — Exit paragraph
- `SEND-LONG-TEXT` — Send long text message
- `SEND-LONG-TEXT-EXIT` — Exit paragraph

---

### 41. COTRTUPC — Transaction Type Add/Update (1,702 LOC, 60 functions)

**Complexity:** 12 CICS calls, 40 PERFORM, 26 EVALUATE, 102 IF  
**Business Function:** Add/update transaction type records (DB2)  
**Test Priority:** HIGH — DB2 CRUD, embedded SQL, complex validation

#### Functions by Category

**Main Control (3)**
- `0000-MAIN` — Main entry point
- `COMMON-RETURN` — Common return to CICS
- `0000-MAIN-EXIT` — Exit paragraph

**PF Key Processing (2)**
- `0001-CHECK-PFKEYS` — Check PF keys
- `0001-CHECK-PFKEYS-EXIT` — Exit paragraph

**Input Processing (8)**
- `1000-PROCESS-INPUTS` — Process inputs
- `1000-PROCESS-INPUTS-EXIT` — Exit paragraph
- `1100-RECEIVE-MAP` — Receive BMS map
- `1100-RECEIVE-MAP-EXIT` — Exit paragraph
- `1150-STORE-MAP-IN-NEW` — Store map in new area
- `1150-STORE-MAP-IN-NEW-EXIT` — Exit paragraph
- `1200-EDIT-MAP-INPUTS` — Edit map inputs
- `1200-EDIT-MAP-INPUTS-EXIT` — Exit paragraph

**Change Detection (2)**
- `1205-COMPARE-OLD-NEW` — Compare old vs new values
- `1205-COMPARE-OLD-NEW-EXIT` — Exit paragraph

**Field Validation (8)**
- `1210-EDIT-TRANTYPE` — Edit transaction type
- `1210-EDIT-TRANTYPE-EXIT` — Exit paragraph
- `1230-EDIT-ALPHANUM-REQD` — Edit required alphanumeric field
- `1230-EDIT-ALPHANUM-REQD-EXIT` — Exit paragraph
- `1245-EDIT-NUM-REQD` — Edit required numeric field
- `1245-EDIT-NUM-REQD-EXIT` — Exit paragraph

**Business Logic (2)**
- `2000-DECIDE-ACTION` — Decide add/update/display action
- `2000-DECIDE-ACTION-EXIT` — Exit paragraph

**Screen Management (22)**
- `3000-SEND-MAP` — Send BMS map
- `3000-SEND-MAP-EXIT` — Exit paragraph
- `3100-SCREEN-INIT` — Initialize screen
- `3100-SCREEN-INIT-EXIT` — Exit paragraph
- `3200-SETUP-SCREEN-VARS` — Setup screen variables
- `3200-SETUP-SCREEN-VARS-EXIT` — Exit paragraph
- `3201-SHOW-INITIAL-VALUES` — Show initial values
- `3201-SHOW-INITIAL-VALUES-EXIT` — Exit paragraph
- `3202-SHOW-ORIGINAL-VALUES` — Show original values
- `3202-SHOW-ORIGINAL-VALUES-EXIT` — Exit paragraph
- `3203-SHOW-UPDATED-VALUES` — Show updated values
- `3203-SHOW-UPDATED-VALUES-EXIT` — Exit paragraph
- `3250-SETUP-INFOMSG` — Setup information message
- `3250-SETUP-INFOMSG-EXIT` — Exit paragraph
- `3300-SETUP-SCREEN-ATTRS` — Setup screen attributes
- `3300-SETUP-SCREEN-ATTRS-EXIT` — Exit paragraph
- `3310-PROTECT-ALL-ATTRS` — Protect all attributes
- `3310-PROTECT-ALL-ATTRS-EXIT` — Exit paragraph
- `3320-UNPROTECT-FEW-ATTRS` — Unprotect few attributes
- `3320-UNPROTECT-FEW-ATTRS-EXIT` — Exit paragraph
- `3390-SETUP-INFOMSG-ATTRS` — Setup info message attributes
- `3390-SETUP-INFOMSG-ATTRS-EXIT` — Exit paragraph

**PF Key Attributes (2)**
- `3391-SETUP-PFKEY-ATTRS` — Setup PF key attributes
- `3391-SETUP-PFKEY-ATTRS-EXIT` — Exit paragraph

**Screen Send (2)**
- `3400-SEND-SCREEN` — Send screen to terminal
- `3400-SEND-SCREEN-EXIT` — Exit paragraph

**DB2 Read Operations (4)**
- `9000-READ-TRANTYPE` — Read transaction type from DB2
- `9000-READ-TRANTYPE-EXIT` — Exit paragraph
- `9100-GET-TRANSACTION-TYPE` — Get transaction type via embedded SQL
- `9100-GET-TRANSACTION-TYPE-EXIT` — Exit paragraph

**Data Storage (2)**
- `9500-STORE-FETCHED-DATA` — Store fetched data
- `9500-STORE-FETCHED-DATA-EXIT` — Exit paragraph

**DB2 Write Operations (2)**
- `9600-WRITE-PROCESSING` — Write processing (INSERT/UPDATE)
- `9600-WRITE-PROCESSING-EXIT` — Exit paragraph

---

### 42. COBTUPDT — Transaction Type Batch Update (237 LOC, 3 functions)

**Complexity:** 0 CICS calls, 14 PERFORM, 7 EVALUATE, 3 IF  
**Business Function:** Batch update transaction type records (DB2)  
**Test Priority:** MEDIUM — DB2 batch update

#### Functions by Category

**File Management (1)**
- `0001-OPEN-FILES` — Open files

**Business Logic (1)**
- (Main processing inline, no named paragraph)

**Utilities (1)**
- `9999-ABEND` — Abend handler

**Finalization (1)**
- `2001-CLOSE-STOP` — Close files and stop

---

### VSAM-MQ Module

### 43. COACCT01 — Account Inquiry via MQ (620 LOC, 14 functions)

**Complexity:** 4 CICS calls, 33 PERFORM, 10 EVALUATE, 13 IF  
**Business Function:** Process account inquiry requests via MQ request/response  
**Test Priority:** MEDIUM — MQ integration, VSAM read

#### Functions by Category

**Main Control (1)**
- `1000-CONTROL` — Main control loop

**MQ Queue Management (6)**
- `2300-OPEN-INPUT-QUEUE` — Open MQ input queue
- `2400-OPEN-OUTPUT-QUEUE` — Open MQ output queue
- `2100-OPEN-ERROR-QUEUE` — Open MQ error queue
- `5000-CLOSE-INPUT-QUEUE` — Close MQ input queue
- `5100-CLOSE-OUTPUT-QUEUE` — Close MQ output queue
- `5200-CLOSE-ERROR-QUEUE` — Close MQ error queue

**Request Processing (3)**
- `4000-MAIN-PROCESS` — Main processing loop
- `3000-GET-REQUEST` — Get request from MQ
- `4000-PROCESS-REQUEST-REPLY` — Process request and reply

**Response Handling (1)**
- `4100-PUT-REPLY` — Put reply to MQ output queue

**Error Handling (1)**
- `9000-ERROR` — Error handling

**Termination (1)**
- `8000-TERMINATION` — Termination processing

---

### 44. CODATE01 — Date Service via MQ (524 LOC, 14 functions)

**Complexity:** 5 CICS calls, 28 PERFORM, 8 EVALUATE, 11 IF  
**Business Function:** Provide system date via MQ request/response  
**Test Priority:** LOW — Simple MQ service

#### Functions by Category

**Main Control (1)**
- `1000-CONTROL` — Main control loop

**MQ Queue Management (6)**
- `2300-OPEN-INPUT-QUEUE` — Open MQ input queue
- `2400-OPEN-OUTPUT-QUEUE` — Open MQ output queue
- `2100-OPEN-ERROR-QUEUE` — Open MQ error queue
- `5000-CLOSE-INPUT-QUEUE` — Close MQ input queue
- `5100-CLOSE-OUTPUT-QUEUE` — Close MQ output queue
- `5200-CLOSE-ERROR-QUEUE` — Close MQ error queue

**Request Processing (3)**
- `4000-MAIN-PROCESS` — Main processing loop
- `3000-GET-REQUEST` — Get request from MQ
- `4000-PROCESS-REQUEST-REPLY` — Process request and reply

**Response Handling (1)**
- `4100-PUT-REPLY` — Put reply to MQ output queue

**Error Handling (1)**
- `9000-ERROR` — Error handling

**Termination (1)**
- `8000-TERMINATION` — Termination processing

---

## Test Coverage Recommendations

### Priority 1: CRITICAL (Must Test Before Migration)

**Financial Core (3 programs, 5,750 LOC, 114 functions)**
- **CBTRN02C** (Transaction Posting) — All 20 functions, focus on balance update logic
- **CBACT04C** (Interest Calculation) — All 14 functions, focus on interest rate logic
- **COTRN02C** (Online Transaction Add) — All 17 functions, focus on validation logic

**Account/Card Management (3 programs, 7,356 LOC, 136 functions)**
- **COACTUPC** (Account Update) — All 77 functions, focus on PII/PCI validation
- **COCRDUPC** (Card Update) — All 42 functions, focus on card status/expiry logic
- **COBIL00C** (Bill Payment) — All 17 functions, focus on payment posting

**Security (1 program, 260 LOC, 6 functions)**
- **COSGN00C** (Signon) — All 6 functions, focus on authentication logic

**Authorization (1 program, 1,026 LOC, 38 functions)**
- **COPAUA0C** (Authorization MQ Trigger) — All 38 functions, focus on decision logic

### Priority 2: HIGH (Test for Business Continuity)

**Data Display (4 programs, 3,982 LOC, 108 functions)**
- **COCRDLIC** (Card List) — Focus on pagination (34 functions)
- **COACTVWC** (Account View) — Focus on multi-file reads (26 functions)
- **COTRN00C** (Transaction List) — Focus on pagination (17 functions)
- **COCRDSLC** (Card View) — Focus on data retrieval (31 functions)

**User Management (4 programs, 1,767 LOC, 50 functions)**
- **COUSR00C** (User List) — Focus on pagination (17 functions)
- **COUSR01C** (User Add) — Focus on validation (11 functions)
- **COUSR02C** (User Update) — Focus on change detection (11 functions)
- **COUSR03C** (User Delete) — Focus on referential integrity (11 functions)

**Transaction Type DB2 (2 programs, 3,800 LOC, 119 functions)**
- **COTRTLIC** (Transaction Type List/Delete) — Focus on DB2 cursor management (59 functions)
- **COTRTUPC** (Transaction Type Add/Update) — Focus on DB2 CRUD (60 functions)

**Authorization IMS (2 programs, 1,636 LOC, 40 functions)**
- **COPAUS0C** (Authorization Summary List) — Focus on IMS reads (21 functions)
- **COPAUS1C** (Authorization Detail View) — Focus on fraud marking (19 functions)

### Priority 3: MEDIUM (Test for Completeness)

**Reporting (2 programs, 1,573 LOC, 33 functions)**
- **CBTRN03C** (Transaction Detail Report) — Focus on totaling logic (14 functions)
- **CBSTM03A** (Statement Generation Driver) — Focus on HTML generation (23 functions)

**Data Migration (3 programs, 1,563 LOC, 47 functions)**
- **CBEXPORT** (Data Export) — Focus on record transformation (18 functions)
- **CBIMPORT** (Data Import) — Focus on error handling (16 functions)
- **CBTRN01C** (Daily Transaction Feed Loader) — Focus on validation (13 functions)

**Menu Navigation (2 programs, 596 LOC, 15 functions)**
- **COMEN01C** (Main Menu) — Focus on routing logic (7 functions)
- **COADM01C** (Admin Menu) — Focus on admin routing (8 functions)

**MQ Services (2 programs, 1,144 LOC, 28 functions)**
- **COACCT01** (Account Inquiry via MQ) — Focus on MQ request/response (14 functions)
- **CODATE01** (Date Service via MQ) — Focus on MQ operations (14 functions)

### Priority 4: LOW (Utilities and Batch Loaders)

**Data Loaders (4 programs, 964 LOC, 24 functions)**
- **CBACT01C** (Account File Loader) — Focus on error handling (12 functions)
- **CBACT02C** (Card File Loader) — Focus on file I/O (4 functions)
- **CBACT03C** (Cross-Reference File Loader) — Focus on file I/O (4 functions)
- **CBCUS01C** (Customer File Loader) — Focus on file I/O (4 functions)

**IMS Utilities (5 programs, 1,804 LOC, 56 functions)**
- **CBPAUP0C** (Authorization Purge Batch) — Focus on delete logic (14 functions)
- **DBUNLDGS** (IMS DB Unload to GSAM) — Focus on unload logic (10 functions)
- **PAUDBLOD** (IMS DB Load from GSAM) — Focus on load logic (12 functions)
- **PAUDBUNL** (IMS DB Unload Alternate) — Focus on unload logic (8 functions)
- **COPAUS2C** (Fraud Update Batch) — Focus on DB2 update (2 functions)

**Simple Utilities (3 programs, 428 LOC, 5 functions)**
- **CSUTLDTC** (Date Utility) — Focus on date conversion (2 functions)
- **COBSWAIT** (Wait Utility) — No functions, inline logic
- **CBSTM03B** (Statement Generation Worker) — Focus on file processing (13 functions)
- **COTRN01C** (Transaction View) — Focus on read logic (11 functions)
- **CORPT00C** (Transaction Report Submit) — Focus on JCL submission (10 functions)
- **COBTUPDT** (Transaction Type Batch Update) — Focus on DB2 batch (3 functions)

---

## Test Coverage Metrics

### Recommended Coverage Targets by Priority

| Priority | Programs | Functions | Min Coverage | Target Coverage | Rationale |
|----------|----------|-----------|--------------|-----------------|-----------|
| **CRITICAL** | 8 | 314 | 90% | 100% | Financial transactions, PII/PCI, security |
| **HIGH** | 14 | 317 | 70% | 85% | Business continuity, data integrity |
| **MEDIUM** | 11 | 123 | 50% | 70% | Reporting, migration, navigation |
| **LOW** | 11 | 93 | 30% | 50% | Utilities, batch loaders |
| **TOTAL** | **44** | **1,247** | **60%** | **75%** | Overall application |

### Function Categorization Summary

| Category | Function Count | % of Total |
|----------|----------------|------------|
| **Data Access** (File I/O, DB2, IMS, MQ) | 387 | 31.0% |
| **Screen Management** (BMS, attributes, display) | 298 | 23.9% |
| **Field Validation** (Edit, format, cross-check) | 246 | 19.7% |
| **Business Logic** (Calculations, decisions, routing) | 142 | 11.4% |
| **File Management** (Open, close, error handling) | 89 | 7.1% |
| **Utilities** (Abend, logging, timestamps) | 85 | 6.8% |
| **TOTAL** | **1,247** | **100%** |

### Complexity-Based Testing Effort Estimates

| Complexity Tier | Programs | Functions | Avg Test Cases/Function | Total Test Cases |
|-----------------|----------|-----------|-------------------------|------------------|
| **High** (100+ decisions) | 8 | 298 | 8 | 2,384 |
| **Medium** (30-99 decisions) | 18 | 512 | 5 | 2,560 |
| **Low** (<30 decisions) | 18 | 437 | 3 | 1,311 |
| **TOTAL** | **44** | **1,247** | **5.0 avg** | **6,255** |

---

## Notes on Test Coverage

1. **Function Count Methodology:** PERFORM paragraphs were extracted via regex pattern matching on COBOL source. Exit paragraphs are included in counts but typically contain only `EXIT` statements.

2. **Complexity Metrics:** CICS calls, PERFORM statements, EVALUATE statements, and IF statements were counted via grep. These are indicators of cyclomatic complexity but not exact measures.

3. **Test Case Estimates:** Based on industry heuristics (high complexity = 8 test cases/function, medium = 5, low = 3). Actual test case counts will vary based on business rules and edge cases.

4. **PII/PCI Scope:** Programs handling Social Security Numbers (COACTUPC), credit card numbers (COCRDUPC, COCRDLIC, COCRDSLC), or customer data (COACTVWC) require additional security testing.

5. **DB2/IMS Integration:** Programs with embedded SQL (COTRTLIC, COTRTUPC, COPAUA0C, COPAUS1C) or IMS DL/I calls (COPAUS0C, COPAUS1C, CBPAUP0C) require database-specific test environments.

6. **MQ Integration:** Programs using MQ (COPAUA0C, COACCT01, CODATE01) require MQ test harnesses for request/response simulation.

7. **Batch Cycle Dependencies:** Batch programs (CBTRN02C, CBACT04C, CBSTM03A/B) must be tested in sequence to validate data lineage through the nightly batch cycle.

8. **Modernization Impact:** Functions performing CICS-specific operations (XCTL, LINK, SEND MAP, RECEIVE MAP) will require significant refactoring for Java migration. These are prime candidates for automated testing before and after conversion.

---

**End of Report**
