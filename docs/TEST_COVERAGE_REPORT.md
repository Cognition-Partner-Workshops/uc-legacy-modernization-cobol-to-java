# CardDemo Test Coverage Report

> **System**: CardDemo -- Credit Card Management System
> **Date**: 2026-03-25
> **Purpose**: Map every testable function/paragraph in each COBOL module, classify as included or not included in the recommended test plan, and assign priority/impact ratings to guide modernization testing effort.

---

## Table of Contents

1. [Test Plan Methodology](#test-plan-methodology)
2. [Coverage Summary](#coverage-summary)
3. [Online Programs -- Detailed Coverage](#online-programs----detailed-coverage)
4. [Batch Programs -- Detailed Coverage](#batch-programs----detailed-coverage)
5. [Shared Utilities -- Detailed Coverage](#shared-utilities----detailed-coverage)
6. [Duplicate Logic Candidates](#duplicate-logic-candidates)
7. [Risk-Based Test Prioritization](#risk-based-test-prioritization)

---

## Test Plan Methodology

### Priority Levels

| Priority | Definition | Testing Approach |
|---|---|---|
| **P0 -- Critical** | Financial calculations, data mutations, authentication. Failure causes monetary loss, security breach, or data corruption. | Unit + integration + regression + parallel-run validation |
| **P1 -- High** | Core business workflows, validation logic, cross-program navigation. Failure blocks key operations. | Unit + integration + smoke tests |
| **P2 -- Medium** | Display/formatting, pagination, screen management. Failure degrades UX but no data impact. | Integration + manual verification |
| **P3 -- Low** | File open/close, error display, utility routines. Infrastructure plumbing. | Smoke test only |

### Impact Ratings

| Impact | Definition |
|---|---|
| **Financial** | Directly affects account balances, transactions, or interest calculations |
| **Security** | Authentication, authorization, PCI-sensitive data handling |
| **Data Integrity** | Creates, updates, or deletes master records |
| **Operational** | Report generation, batch sequencing, data migration |
| **User Experience** | Screen display, navigation, pagination |

### Included vs. Not Included

- **Included in Test Plan**: Functions with business logic, validation, data mutation, or cross-program interaction that require explicit test cases during modernization.
- **Not Included in Test Plan**: Infrastructure plumbing (file open/close, abend handlers, screen send/receive mechanics) that are replaced by framework equivalents in Java and do not need dedicated test cases -- they are covered implicitly by integration tests.

---

## Coverage Summary

| Module | Type | Total Functions | In Test Plan | Not in Plan | Coverage % | Top Priority |
|---|---|---|---|---|---|---|
| **COACTUPC** | Online | 42 | 26 | 16 | 62% | P0 |
| **CBTRN02C** | Batch | 16 | 9 | 7 | 56% | P0 |
| **CBACT04C** | Batch | 15 | 7 | 8 | 47% | P0 |
| **CBSTM03A** | Batch | 18 | 8 | 10 | 44% | P1 |
| **COBIL00C** | Online | 11 | 6 | 5 | 55% | P0 |
| **COCRDUPC** | Online | 25 | 14 | 11 | 56% | P1 |
| **COCRDLIC** | Online | 20 | 10 | 10 | 50% | P2 |
| **COSGN00C** | Online | 5 | 3 | 2 | 60% | P0 |
| **COTRN02C** | Online | 10 | 5 | 5 | 50% | P0 |
| **CBTRN03C** | Batch | 15 | 7 | 8 | 47% | P1 |
| **COTRN00C** | Online | 12 | 7 | 5 | 58% | P2 |
| **COTRN01C** | Online | 6 | 2 | 4 | 33% | P3 |
| **COACTVWC** | Online | 16 | 7 | 9 | 44% | P2 |
| **COCRDSLC** | Online | 16 | 7 | 9 | 44% | P2 |
| **COMEN01C** | Online | 7 | 4 | 3 | 57% | P1 |
| **COADM01C** | Online | 7 | 4 | 3 | 57% | P1 |
| **CORPT00C** | Online | 7 | 3 | 4 | 43% | P1 |
| **COUSR00C** | Online | 12 | 7 | 5 | 58% | P2 |
| **COUSR01C** | Online | 6 | 3 | 3 | 50% | P1 |
| **COUSR02C** | Online | 8 | 4 | 4 | 50% | P1 |
| **COUSR03C** | Online | 6 | 3 | 3 | 50% | P1 |
| **CBACT01C** | Batch | 11 | 4 | 7 | 36% | P2 |
| **CBACT02C** | Batch | 4 | 1 | 3 | 25% | P2 |
| **CBACT03C** | Batch | 4 | 1 | 3 | 25% | P2 |
| **CBCUS01C** | Batch | 4 | 1 | 3 | 25% | P2 |
| **CBTRN01C** | Batch | 14 | 4 | 10 | 29% | P1 |
| **CBSTM03B** | Batch | 11 | 4 | 7 | 36% | P1 |
| **CBEXPORT** | Batch | 16 | 8 | 8 | 50% | P1 |
| **CBIMPORT** | Batch | 14 | 8 | 6 | 57% | P1 |
| **CSUTLDTC** | Utility | 1 | 1 | 0 | 100% | P1 |
| **COBSWAIT** | Utility | 1 | 0 | 1 | 0% | P3 |
| **Totals** | | **337** | **172** | **165** | **51%** | |

---

## Online Programs -- Detailed Coverage

### COSGN00C -- Sign-On (5 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P0** | Security | Test full sign-on flow: valid credentials route to correct menu (user vs. admin), invalid credentials show error, empty fields rejected |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P0** | Security | Test credential validation: read USRSEC file, compare user ID + password, route by SEC-USR-TYPE ('U' -> COMEN01C, 'A' -> COADM01C), handle user-not-found |
| 3 | `SEND-SIGNON-SCREEN` | No | P3 | UX | Framework-level screen send -- covered by integration test of sign-on flow |
| 4 | `SEND-PLAIN-TEXT` | No | P3 | UX | Error message display utility -- covered implicitly |
| 5 | `POPULATE-HEADER-INFO` | Yes | **P2** | UX | Test header population: date, time, program name, transaction ID displayed correctly |

**Key test scenarios**:
- Valid regular user login -> navigates to main menu (COMEN01C)
- Valid admin login -> navigates to admin menu (COADM01C)
- Invalid password -> error message, no navigation
- Non-existent user ID -> error message
- Plain-text password comparison (security regression test for migration)

---

### COMEN01C -- Main Menu (7 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Operational | Test menu initialization, COMMAREA handling, PF key routing |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Operational | Test each menu option (1-11) routes to correct program via XCTL |
| 3 | `RETURN-TO-SIGNON-SCREEN` | Yes | **P2** | Security | Test PF3 returns to sign-on, COMMAREA cleared |
| 4 | `SEND-MENU-SCREEN` | No | P3 | UX | Screen send mechanics -- implicit |
| 5 | `RECEIVE-MENU-SCREEN` | No | P3 | UX | Screen receive mechanics -- implicit |
| 6 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header utility -- covered by integration |
| 7 | `BUILD-MENU-OPTIONS` | Yes | **P2** | UX | Test menu options are built correctly based on available programs |

---

### COADM01C -- Admin Menu (7 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Security | Test admin menu initialization, verify admin-only access |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Security | Test admin options (1-6) route to correct programs (COUSR00C-03C, COTRTLIC, COTRTUPC) |
| 3 | `RETURN-TO-SIGNON-SCREEN` | Yes | **P2** | Security | Test PF3 returns to sign-on |
| 4 | `SEND-MENU-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 5 | `RECEIVE-MENU-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 6 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header utility -- implicit |
| 7 | `BUILD-MENU-OPTIONS` | Yes | **P2** | UX | Test admin menu options built correctly |

---

### COACTUPC -- Account Update (42 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN` | Yes | **P0** | Data Integrity | Test main control flow: first-time vs. return, COMMAREA state management |
| 2 | `COMMON-RETURN` | No | P3 | UX | CICS RETURN with TRANSID -- framework-level |
| 3 | `1100-RECEIVE-MAP` | No | P3 | UX | CICS RECEIVE MAP mechanics -- implicit |
| 4 | `1200-EDIT-MAP-INPUTS` | Yes | **P0** | Data Integrity | Test full field validation orchestration for account update |
| 5 | `1205-COMPARE-OLD-NEW` | Yes | **P1** | Data Integrity | Test change detection: identify which fields were modified |
| 6 | `1210-EDIT-ACCOUNT` | Yes | **P0** | Data Integrity | Test account number validation: numeric, 11 digits, exists in ACCTFILE |
| 7 | `1215-EDIT-MANDATORY` | Yes | **P0** | Data Integrity | Test mandatory field validation: reject blank/space-filled required fields |
| 8 | `1220-EDIT-YESNO` | Yes | **P1** | Data Integrity | Test Y/N field validation for boolean flags |
| 9 | `1225-EDIT-ALPHA-REQD` | Yes | **P1** | Data Integrity | Test required alphabetic field validation |
| 10 | `1230-EDIT-ALPHANUM-REQD` | Yes | **P1** | Data Integrity | Test required alphanumeric field validation |
| 11 | `1235-EDIT-ALPHA-OPT` | Yes | **P2** | Data Integrity | Test optional alphabetic field validation |
| 12 | `1240-EDIT-ALPHANUM-OPT` | Yes | **P2** | Data Integrity | Test optional alphanumeric field validation |
| 13 | `1245-EDIT-NUM-REQD` | Yes | **P0** | Financial | Test required numeric field validation (credit limits, balances) |
| 14 | `1250-EDIT-SIGNED-9V2` | Yes | **P0** | Financial | Test signed decimal field validation (PIC S9V2 -- amounts with 2 decimal places) |
| 15 | `1260-EDIT-US-PHONE-NUM` | Yes | **P1** | Data Integrity | Test US phone number format validation (area code + prefix + line number) |
| 16 | `EDIT-AREA-CODE` | Yes | **P1** | Data Integrity | Test area code: 3 digits, numeric |
| 17 | `EDIT-US-PHONE-PREFIX` | Yes | **P1** | Data Integrity | Test phone prefix: 3 digits, numeric |
| 18 | `EDIT-US-PHONE-LINENUM` | Yes | **P1** | Data Integrity | Test line number: 4 digits, numeric |
| 19 | `1265-EDIT-US-SSN` | Yes | **P0** | Security | Test SSN validation: 9 digits, numeric, PII handling |
| 20 | `1270-EDIT-US-STATE-CD` | Yes | **P1** | Data Integrity | Test US state code validation against lookup table (CSLKPCDY) |
| 21 | `1275-EDIT-FICO-SCORE` | Yes | **P1** | Data Integrity | Test FICO score validation: numeric, within valid range |
| 22 | `1280-EDIT-US-STATE-ZIP-CD` | Yes | **P1** | Data Integrity | Test ZIP code validation: 5 or 9 digits, state-ZIP consistency |
| 23 | `2000-DECIDE-ACTION` | Yes | **P1** | Operational | Test action routing: view -> edit -> confirm -> save flow |
| 24 | `3000-SEND-MAP` | No | P3 | UX | Screen send orchestration -- implicit |
| 25 | `3100-SCREEN-INIT` | No | P3 | UX | Screen initialization -- implicit |
| 26 | `3200-SETUP-SCREEN-VARS` | No | P3 | UX | Screen variable setup -- implicit |
| 27 | `3201-SHOW-INITIAL-VALUES` | No | P3 | UX | Initial value display -- implicit |
| 28 | `3202-SHOW-ORIGINAL-VALUES` | No | P3 | UX | Original value display -- implicit |
| 29 | `3203-SHOW-UPDATED-VALUES` | No | P3 | UX | Updated value display -- implicit |
| 30 | `3250-SETUP-INFOMSG` | No | P3 | UX | Info message setup -- implicit |
| 31 | `3300-SETUP-SCREEN-ATTRS` | No | P3 | UX | Screen attribute setup -- implicit |
| 32 | `3310-PROTECT-ALL-ATTRS` | No | P3 | UX | Field protection -- implicit |
| 33 | `3320-UNPROTECT-FEW-ATTRS` | No | P3 | UX | Field unprotection -- implicit |
| 34 | `3390-SETUP-INFOMSG-ATTRS` | No | P3 | UX | Message attributes -- implicit |
| 35 | `3400-SEND-SCREEN` | No | P3 | UX | CICS SEND MAP -- implicit |
| 36 | `9000-READ-ACCT` | Yes | **P1** | Data Integrity | Test account read: CICS READ on ACCTFILE, handle NOTFND |
| 37 | `9200-GETCARDXREF-BYACCT` | Yes | **P1** | Data Integrity | Test cross-reference lookup by account |
| 38 | `9300-GETACCTDATA-BYACCT` | Yes | **P1** | Data Integrity | Test account data retrieval |
| 39 | `9400-GETCUSTDATA-BYCUST` | Yes | **P1** | Data Integrity | Test customer data retrieval by customer ID |
| 40 | `9500-STORE-FETCHED-DATA` | No | P3 | UX | Working storage population -- implicit |
| 41 | `9600-WRITE-PROCESSING` | Yes | **P0** | Data Integrity | Test account update: CICS REWRITE on ACCTFILE, verify all fields persisted correctly |
| 42 | `9700-CHECK-CHANGE-IN-REC` | Yes | **P1** | Data Integrity | Test concurrent modification detection (optimistic locking equivalent) |

**Key test scenarios**:
- Update each field type individually and verify persistence
- Validation rejection for each field type (numeric, alpha, phone, SSN, state, ZIP, FICO)
- Concurrent modification detection (another user changed record)
- Full round-trip: view -> edit -> confirm -> save -> verify
- Cross-reference lookup with multiple cards per account

---

### COACTVWC -- Account View (16 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN` | Yes | **P2** | UX | Test view flow: COMMAREA handling, first-time vs. return |
| 2 | `COMMON-RETURN` | No | P3 | UX | CICS RETURN -- framework-level |
| 3 | `1100-SCREEN-INIT` | No | P3 | UX | Screen init -- implicit |
| 4 | `1200-SETUP-SCREEN-VARS` | No | P3 | UX | Screen vars -- implicit |
| 5 | `1300-SETUP-SCREEN-ATTRS` | No | P3 | UX | Screen attrs -- implicit |
| 6 | `1400-SEND-SCREEN` | No | P3 | UX | CICS SEND -- implicit |
| 7 | `2000-PROCESS-INPUTS` | Yes | **P2** | UX | Test input processing: account number entry, PF key handling |
| 8 | `2100-RECEIVE-MAP` | No | P3 | UX | CICS RECEIVE -- implicit |
| 9 | `2200-EDIT-MAP-INPUTS` | Yes | **P2** | Data Integrity | Test account number validation for view |
| 10 | `2210-EDIT-ACCOUNT` | Yes | **P2** | Data Integrity | Test account number format: 11 digits, numeric |
| 11 | `9000-READ-ACCT` | Yes | **P2** | Data Integrity | Test account read: existing account displays data, non-existent shows error |
| 12 | `9200-GETCARDXREF-BYACCT` | Yes | **P2** | Data Integrity | Test cross-reference retrieval for card display |
| 13 | `9300-GETACCTDATA-BYACCT` | Yes | **P2** | Data Integrity | Test account data retrieval |
| 14 | `9400-GETCUSTDATA-BYCUST` | No | P3 | UX | Customer data retrieval -- tested via integration |
| 15 | `SEND-PLAIN-TEXT` | No | P3 | UX | Text display utility -- implicit |
| 16 | `SEND-LONG-TEXT` | No | P3 | UX | Long text display -- implicit |

---

### COCRDLIC -- Credit Card List (20 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN` | Yes | **P2** | UX | Test list initialization and COMMAREA state management |
| 2 | `COMMON-RETURN` | No | P3 | UX | CICS RETURN -- implicit |
| 3 | `1100-SCREEN-INIT` | No | P3 | UX | Screen init -- implicit |
| 4 | `1200-SCREEN-ARRAY-INIT` | No | P3 | UX | Array init for list display -- implicit |
| 5 | `1250-SETUP-ARRAY-ATTRIBS` | No | P3 | UX | Array attributes -- implicit |
| 6 | `1300-SETUP-SCREEN-ATTRS` | No | P3 | UX | Screen attrs -- implicit |
| 7 | `1400-SETUP-MESSAGE` | No | P3 | UX | Message setup -- implicit |
| 8 | `2000-RECEIVE-MAP` | No | P3 | UX | CICS RECEIVE -- implicit |
| 9 | `2100-RECEIVE-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 10 | `2200-EDIT-INPUTS` | Yes | **P2** | Data Integrity | Test input validation for filter criteria |
| 11 | `2210-EDIT-ACCOUNT` | Yes | **P2** | Data Integrity | Test account filter validation |
| 12 | `2220-EDIT-CARD` | Yes | **P2** | Data Integrity | Test card number filter validation |
| 13 | `2250-EDIT-ARRAY` | Yes | **P2** | UX | Test selection from list array (which card selected for view/update) |
| 14 | `9000-READ-FORWARD` | Yes | **P2** | UX | Test forward pagination: STARTBR/READNEXT sequence, end-of-file handling |
| 15 | `9100-READ-BACKWARDS` | Yes | **P2** | UX | Test backward pagination: READPREV sequence, beginning-of-file handling |
| 16 | `9500-FILTER-RECORDS` | Yes | **P2** | UX | Test card filtering by account number |
| 17 | `SEND-PLAIN-TEXT` | No | P3 | UX | Text utility -- implicit |
| 18 | `SEND-LONG-TEXT` | No | P3 | UX | Long text -- implicit |
| 19 | `ABEND-ROUTINE` | No | P3 | Operational | Error handler -- implicit |
| 20 | _(various EXIT paragraphs)_ | No | P3 | -- | Flow control -- implicit |

**Key test scenarios**:
- List all cards (no filter) with forward/backward pagination
- Filter by account number
- Select card for view (XCTL to COCRDSLC)
- Select card for update (XCTL to COCRDUPC)
- Empty result set handling
- PCI: verify card numbers are properly displayed (masking in Java)

---

### COCRDSLC -- Credit Card View (16 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN` | Yes | **P2** | UX | Test view flow initialization |
| 2 | `COMMON-RETURN` | No | P3 | UX | CICS RETURN -- implicit |
| 3 | `1100-SCREEN-INIT` | No | P3 | UX | Screen init -- implicit |
| 4 | `1200-SETUP-SCREEN-VARS` | No | P3 | UX | Screen vars -- implicit |
| 5 | `1300-SETUP-SCREEN-ATTRS` | No | P3 | UX | Screen attrs -- implicit |
| 6 | `1400-SEND-SCREEN` | No | P3 | UX | CICS SEND -- implicit |
| 7 | `2000-PROCESS-INPUTS` | Yes | **P2** | UX | Test input handling for card view |
| 8 | `2100-RECEIVE-MAP` | No | P3 | UX | CICS RECEIVE -- implicit |
| 9 | `2200-EDIT-MAP-INPUTS` | Yes | **P2** | Data Integrity | Test card view input validation |
| 10 | `2210-EDIT-ACCOUNT` | Yes | **P2** | Data Integrity | Test account number validation |
| 11 | `2220-EDIT-CARD` | Yes | **P2** | Data Integrity | Test card number validation |
| 12 | `9000-READ-DATA` | Yes | **P2** | Data Integrity | Test card data retrieval from CARDFILE |
| 13 | `9100-GETCARD-BYACCTCARD` | Yes | **P2** | Data Integrity | Test card lookup by account + card number |
| 14 | `9150-GETCARD-BYACCT` | No | P3 | Data Integrity | Alternate lookup -- covered by integration |
| 15 | `SEND-LONG-TEXT` | No | P3 | UX | Text utility -- implicit |
| 16 | `SEND-PLAIN-TEXT` | No | P3 | UX | Text utility -- implicit |

---

### COCRDUPC -- Credit Card Update (25 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN` | Yes | **P1** | Data Integrity | Test update flow: state management across pseudo-conversational transactions |
| 2 | `COMMON-RETURN` | No | P3 | UX | CICS RETURN -- implicit |
| 3 | `1100-RECEIVE-MAP` | No | P3 | UX | CICS RECEIVE -- implicit |
| 4 | `1200-EDIT-MAP-INPUTS` | Yes | **P1** | Data Integrity | Test full card field validation orchestration |
| 5 | `1210-EDIT-ACCOUNT` | Yes | **P1** | Data Integrity | Test account number validation for card update |
| 6 | `1220-EDIT-CARD` | Yes | **P1** | Data Integrity | Test card number validation |
| 7 | `1230-EDIT-NAME` | Yes | **P1** | Data Integrity | Test cardholder name validation |
| 8 | `1240-EDIT-CARDSTATUS` | Yes | **P0** | Data Integrity | Test card status validation: valid status codes (A=Active, I=Inactive, etc.), status transitions |
| 9 | `1250-EDIT-EXPIRY-MON` | Yes | **P1** | Data Integrity | Test expiry month validation: 01-12, numeric |
| 10 | `1260-EDIT-EXPIRY-YEAR` | Yes | **P1** | Data Integrity | Test expiry year validation: reasonable range (current year to current+10) |
| 11 | `2000-DECIDE-ACTION` | Yes | **P1** | Operational | Test action routing: view -> edit -> confirm -> save |
| 12 | `3000-SEND-MAP` | No | P3 | UX | Screen send -- implicit |
| 13 | `3100-SCREEN-INIT` | No | P3 | UX | Screen init -- implicit |
| 14 | `3200-SETUP-SCREEN-VARS` | No | P3 | UX | Screen vars -- implicit |
| 15 | `3250-SETUP-INFOMSG` | No | P3 | UX | Info message -- implicit |
| 16 | `3300-SETUP-SCREEN-ATTRS` | No | P3 | UX | Screen attrs -- implicit |
| 17 | `3400-SEND-SCREEN` | No | P3 | UX | CICS SEND -- implicit |
| 18 | `9000-READ-DATA` | Yes | **P1** | Data Integrity | Test card data read from CARDFILE |
| 19 | `9100-GETCARD-BYACCTCARD` | Yes | **P1** | Data Integrity | Test card lookup for update |
| 20 | `9200-WRITE-PROCESSING` | Yes | **P0** | Data Integrity | Test card update: CICS REWRITE on CARDFILE, verify all fields persisted |
| 21 | `9300-CHECK-CHANGE-IN-REC` | Yes | **P1** | Data Integrity | Test concurrent modification detection |
| 22 | `ABEND-ROUTINE` | No | P3 | Operational | Error handler -- implicit |
| 23-25 | _(EXIT paragraphs)_ | No | P3 | -- | Flow control -- implicit |

**Key test scenarios**:
- Update card status (Active <-> Inactive) and verify downstream impact
- Update expiration date with valid/invalid months and years
- Inline date validation (month 1-12, year 1950-2099) -- note: does NOT use CSUTLDTC
- Concurrent modification detection
- PCI: CVV field handling (should never be displayed in Java version)

---

### COTRN00C -- Transaction List (12 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P2** | UX | Test transaction list initialization |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P2** | UX | Test transaction selection for view |
| 3 | `PROCESS-PF7-KEY` | Yes | **P2** | UX | Test page backward (PF7) |
| 4 | `PROCESS-PF8-KEY` | Yes | **P2** | UX | Test page forward (PF8) |
| 5 | `PROCESS-PAGE-FORWARD` | Yes | **P2** | UX | Test forward pagination logic |
| 6 | `PROCESS-PAGE-BACKWARD` | Yes | **P2** | UX | Test backward pagination logic |
| 7 | `POPULATE-TRAN-DATA` | Yes | **P2** | UX | Test transaction data population for display |
| 8 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 9 | `SEND-TRNLST-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 10 | `RECEIVE-TRNLST-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 11 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header utility -- implicit |
| 12 | `STARTBR-TRANSACT-FILE` / `ENDBR-TRANSACT-FILE` | No | P3 | UX | Browse control -- implicit |

---

### COTRN01C -- Transaction View (6 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P2** | UX | Test transaction view initialization |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P2** | UX | Test view display of selected transaction |
| 3 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 4 | `SEND-TRNVIEW-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 5 | `RECEIVE-TRNVIEW-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 6 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header -- implicit |

---

### COTRN02C -- Transaction Add (10 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P0** | Financial | Test transaction add flow: input -> validate -> write |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P0** | Financial | Test transaction creation on Enter: validate all fields, generate ID, write to TRANSACT |
| 3 | `VALIDATE-INPUT-KEY-FIELDS` | Yes | **P0** | Financial | Test key field validation: card number exists in XREFFILE, account valid |
| 4 | `VALIDATE-INPUT-DATA-FIELDS` | Yes | **P0** | Financial | Test data field validation: amount numeric, transaction type valid (TRANTYPE), category valid (TRANCATG), dates valid (CSUTLDTC) |
| 5 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 6 | `SEND-TRNADD-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 7 | `RECEIVE-TRNADD-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 8 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header -- implicit |
| 9 | `STARTBR-TRANSACT-FILE` | Yes | **P1** | Data Integrity | Test transaction ID generation via browse (get max ID + 1) |
| 10 | `ENDBR-TRANSACT-FILE` | No | P3 | Data Integrity | Browse cleanup -- implicit |

**Key test scenarios**:
- Create transaction with valid card, type, category, amount, dates
- Reject invalid card number (not in XREFFILE)
- Reject invalid transaction type (not in TRANTYPE)
- Reject invalid category (not in TRANCATG)
- Reject invalid dates (CSUTLDTC returns error)
- Transaction ID uniqueness
- Verify written record matches all input fields

---

### COBIL00C -- Bill Payment (11 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P0** | Financial | Test bill payment flow: input -> validate -> update balance -> write transaction |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P0** | Financial | Test payment processing: validate amount, read account, update balance, write transaction atomically |
| 3 | `GET-CURRENT-TIMESTAMP` | Yes | **P1** | Data Integrity | Test timestamp generation for transaction record |
| 4 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 5 | `SEND-BILLPAY-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 6 | `RECEIVE-BILLPAY-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 7 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header -- implicit |
| 8 | `UPDATE-ACCTDAT-FILE` | Yes | **P0** | Financial | Test account balance update: REWRITE ACCTFILE with reduced balance, verify arithmetic precision |
| 9 | `STARTBR-TRANSACT-FILE` | Yes | **P0** | Financial | Test transaction ID generation for payment record |
| 10 | `ENDBR-TRANSACT-FILE` | No | P3 | Data Integrity | Browse cleanup -- implicit |
| 11 | `CLEAR-CURRENT-SCREEN` | Yes | **P2** | UX | Test screen clear after successful payment |

**Key test scenarios**:
- Pay exact balance -> account balance = 0
- Partial payment -> verify remaining balance arithmetic (BigDecimal precision)
- Payment exceeding balance -> rejection
- Zero amount payment -> rejection
- Atomicity: balance update + transaction creation must both succeed or both fail
- Verify TRANSACT record: correct amount, timestamp, transaction type

---

### CORPT00C -- Reports (7 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Operational | Test report request flow |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Operational | Test report submission: validate date range, write job request to TDQ |
| 3 | `WIRTE-JOBSUB-TDQ` | Yes | **P1** | Operational | Test TDQ write for batch job submission (triggers CBTRN03C) |
| 4 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 5 | `SEND-TRNRPT-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 6 | `RECEIVE-TRNRPT-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 7 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header -- implicit |

---

### COUSR00C -- User List (12 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P2** | Security | Test user list initialization |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P2** | Security | Test user selection for update/delete |
| 3 | `PROCESS-PF7-KEY` | Yes | **P2** | UX | Test page backward |
| 4 | `PROCESS-PF8-KEY` | Yes | **P2** | UX | Test page forward |
| 5 | `PROCESS-PAGE-FORWARD` | Yes | **P2** | UX | Test forward pagination |
| 6 | `PROCESS-PAGE-BACKWARD` | Yes | **P2** | UX | Test backward pagination |
| 7 | `POPULATE-USER-DATA` | Yes | **P2** | Security | Test user data population (verify password not displayed) |
| 8 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 9 | `SEND-USRLST-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 10 | `RECEIVE-USRLST-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 11 | `POPULATE-HEADER-INFO` | No | P3 | UX | Header -- implicit |
| 12 | `STARTBR-USER-SEC-FILE` / `ENDBR-USER-SEC-FILE` | No | P3 | UX | Browse control -- implicit |

---

### COUSR01C -- User Add (6 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Security | Test user creation flow |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Security | Test user creation: validate user ID uniqueness, validate user type, write to USRSEC |
| 3 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 4 | `SEND-USRADD-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 5 | `RECEIVE-USRADD-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 6 | `CLEAR-CURRENT-SCREEN` | Yes | **P2** | UX | Test screen clear after user add |

---

### COUSR02C -- User Update (8 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Security | Test user update flow |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Security | Test user update: validate fields, route to update |
| 3 | `UPDATE-USER-INFO` | Yes | **P1** | Security | Test user info validation before write |
| 4 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 5 | `SEND-USRUPD-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 6 | `RECEIVE-USRUPD-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 7 | `UPDATE-USER-SEC-FILE` | Yes | **P0** | Security | Test user security record update: CICS REWRITE on USRSEC, password change handling |
| 8 | `CLEAR-CURRENT-SCREEN` | No | P3 | UX | Screen clear -- implicit |

---

### COUSR03C -- User Delete (6 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Security | Test user delete flow |
| 2 | `PROCESS-ENTER-KEY` | Yes | **P1** | Security | Test user deletion: confirm prompt, CICS DELETE on USRSEC |
| 3 | `RETURN-TO-PREV-SCREEN` | No | P3 | UX | Navigation -- implicit |
| 4 | `SEND-USRDEL-SCREEN` | No | P3 | UX | Screen send -- implicit |
| 5 | `RECEIVE-USRDEL-SCREEN` | No | P3 | UX | Screen receive -- implicit |
| 6 | `CLEAR-CURRENT-SCREEN` | Yes | **P2** | UX | Test screen clear after delete |

---

## Batch Programs -- Detailed Coverage

### CBTRN02C -- Transaction Posting (16 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-DALYTRAN-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `0400-ACCTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 3 | `2000-POST-TRANSACTION` | Yes | **P0** | Financial | Test full posting logic: read daily transaction, validate, lookup XREF, update account, write to TRANSACT, update TCATBAL |
| 4 | `2500-WRITE-REJECT-REC` | Yes | **P0** | Financial | Test rejection: invalid transactions written to DALYREJS with reason code |
| 5 | `2700-UPDATE-TCATBAL` | Yes | **P0** | Financial | Test category balance update: correct category found, balance arithmetic |
| 6 | `2700-A-CREATE-TCATBAL-REC` | Yes | **P0** | Financial | Test new category balance creation when category doesn't exist |
| 7 | `2700-B-UPDATE-TCATBAL-REC` | Yes | **P0** | Financial | Test existing category balance update |
| 8 | `2800-UPDATE-ACCOUNT-REC` | Yes | **P0** | Financial | Test account record update: ACCT-CURR-BAL, ACCT-CURR-CYC-DEBIT/CREDIT |
| 9 | `2900-WRITE-TRANSACTION-FILE` | Yes | **P0** | Financial | Test transaction write to TRANSACT with DB2-format timestamp |
| 10 | `Z-GET-DB2-FORMAT-TIMESTAMP` | Yes | **P1** | Data Integrity | Test timestamp formatting for transaction records |
| 11 | `9000-DALYTRAN-CLOSE` | No | P3 | Operational | File close -- implicit |
| 12 | `9100-TRANFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 13 | `9200-XREFFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 14 | `9300-DALYREJS-CLOSE` | No | P3 | Operational | File close -- implicit |
| 15 | `9400-ACCTFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 16 | `9500-TCATBALF-CLOSE` | No | P3 | Operational | File close -- implicit |

**Key test scenarios**:
- Post valid debit transaction -> account balance decreased, TCATBAL updated, TRANSACT written
- Post valid credit transaction -> account balance increased
- Reject transaction with invalid card (not in XREF) -> written to DALYREJS
- Post transaction for new category -> TCATBAL record created
- Post transaction for existing category -> TCATBAL record updated
- Verify decimal precision across all balance calculations
- Parallel-run: compare COBOL output with Java output for identical input

---

### CBACT04C -- Interest Calculation (15 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-TCATBALF-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `0400-TRANFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 3 | `1100-GET-ACCT-DATA` | Yes | **P0** | Financial | Test account data retrieval for interest calculation |
| 4 | `1110-GET-XREF-DATA` | Yes | **P1** | Data Integrity | Test cross-reference lookup to find account group |
| 5 | `1200-GET-INTEREST-RATE` | Yes | **P0** | Financial | Test interest rate lookup: account -> group -> type -> category -> rate from DISCGRP |
| 6 | `1200-A-GET-DEFAULT-INT-RATE` | Yes | **P0** | Financial | Test default rate fallback when specific rate not found |
| 7 | `1300-COMPUTE-INTEREST` | Yes | **P0** | Financial | Test interest computation: rate x category balance, verify decimal precision (PIC S9(10)V99) |
| 8 | `1300-B-WRITE-TX` | Yes | **P0** | Financial | Test interest charge transaction creation in TRANSACT |
| 9 | `1400-COMPUTE-FEES` | Yes | **P0** | Financial | Test fee computation logic |
| 10 | `Z-GET-DB2-FORMAT-TIMESTAMP` | No | P3 | Data Integrity | Timestamp utility -- tested in CBTRN02C (duplicate logic) |
| 11-15 | _(file close + abend paragraphs)_ | No | P3 | Operational | Infrastructure -- implicit |

**Key test scenarios**:
- Calculate interest for account with single category balance
- Calculate interest for account with multiple category balances
- Default rate fallback when DISCGRP entry missing
- Zero balance -> no interest transaction created
- Verify interest amount precision to 2 decimal places
- Parallel-run validation against COBOL output

---

### CBSTM03A -- Statement Generation (18 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-START` | Yes | **P1** | Operational | Test statement generation main loop: iterate accounts, generate statements |
| 2 | `2000-CUSTFILE-GET` | Yes | **P1** | Data Integrity | Test customer data retrieval for statement header |
| 3 | `3000-ACCTFILE-GET` | Yes | **P1** | Data Integrity | Test account data retrieval for statement |
| 4 | `4000-TRNXFILE-GET` | Yes | **P1** | Data Integrity | Test transaction retrieval for statement detail lines |
| 5 | `5000-CREATE-STATEMENT` | Yes | **P1** | Operational | Test statement assembly: header + detail lines + totals |
| 6 | `5100-WRITE-HTML-HEADER` | Yes | **P1** | Operational | Test HTML header generation |
| 7 | `5200-WRITE-HTML-NMADBS` | Yes | **P1** | Operational | Test HTML name/address block generation |
| 8 | `6000-WRITE-TRANS` | Yes | **P1** | Operational | Test transaction line formatting in statement |
| 9 | `8100-FILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 10 | `8100-TRNXFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 11 | `8200-XREFFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 12 | `8300-CUSTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 13 | `8400-ACCTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 14 | `8500-READTRNX-READ` | No | P3 | Operational | File read -- covered by integration |
| 15-18 | _(file close + abend paragraphs)_ | No | P3 | Operational | Infrastructure -- implicit |

**Key test scenarios**:
- Generate statement for account with 0 transactions
- Generate statement for account with many transactions (page overflow -> calls CBSTM03B)
- Verify HTML output format correctness
- Verify print spool output format
- Statement totals match sum of transaction amounts
- Customer name/address correctly populated

---

### CBSTM03B -- Statement File Handler (11 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-START` | Yes | **P1** | Operational | Test file handler initialization via CALL from CBSTM03A |
| 2 | `2000-XREFFILE-PROC` | Yes | **P1** | Data Integrity | Test cross-reference file processing for card-to-customer mapping |
| 3 | `3000-CUSTFILE-PROC` | Yes | **P1** | Data Integrity | Test customer file processing |
| 4 | `4000-ACCTFILE-PROC` | Yes | **P1** | Data Integrity | Test account file processing |
| 5-11 | _(EXIT paragraphs + file handling)_ | No | P3 | Operational | Flow control -- implicit |

**Note**: CBSTM03A and CBSTM03B should be tested together as an integrated unit since CBSTM03A calls CBSTM03B 13 times for file handling operations.

---

### CBTRN03C -- Daily Transaction Report (15 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-TRANFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `0400-TRANCATG-OPEN` | No | P3 | Operational | File open -- implicit |
| 3 | `1100-WRITE-TRANSACTION-REPORT` | Yes | **P1** | Operational | Test main report generation loop |
| 4 | `1110-WRITE-PAGE-TOTALS` | Yes | **P1** | Operational | Test page total calculation and formatting |
| 5 | `1120-WRITE-ACCOUNT-TOTALS` | Yes | **P1** | Operational | Test account-level total aggregation |
| 6 | `1110-WRITE-GRAND-TOTALS` | Yes | **P1** | Operational | Test grand total calculation across all accounts |
| 7 | `1120-WRITE-HEADERS` | Yes | **P2** | Operational | Test report header formatting |
| 8 | `1111-WRITE-REPORT-REC` | No | P3 | Operational | Record write -- implicit |
| 9 | `1120-WRITE-DETAIL` | Yes | **P1** | Operational | Test detail line formatting with transaction data |
| 10 | `9000-TRANFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 11-15 | _(file close + abend paragraphs)_ | No | P3 | Operational | Infrastructure -- implicit |

**Key test scenarios**:
- Report with transactions in date range
- Report with no transactions in date range -> empty report with headers
- Page overflow -> correct page totals
- Account totals match sum of detail lines
- Grand totals match sum of account totals
- Date range filtering accuracy

---

### CBTRN01C -- Transaction Validation (14 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `MAIN-PARA` | Yes | **P1** | Data Integrity | Test transaction validation main flow |
| 2 | `2000-LOOKUP-XREF` | Yes | **P1** | Data Integrity | Test cross-reference lookup for card validation |
| 3 | `3000-READ-ACCOUNT` | Yes | **P1** | Data Integrity | Test account read for validation |
| 4 | `0000-DALYTRAN-OPEN` | No | P3 | Operational | File open -- implicit |
| 5 | `0400-ACCTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 6 | `9000-DALYTRAN-CLOSE` | No | P3 | Operational | File close -- implicit |
| 7 | `9100-CUSTFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 8 | `9200-XREFFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 9 | `9300-CARDFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 10 | `9400-ACCTFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 11 | `9500-TRANFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 12 | `Z-ABEND-PROGRAM` | No | P3 | Operational | Abend handler -- implicit |
| 13 | `Z-DISPLAY-IO-STATUS` | No | P3 | Operational | I/O status display -- implicit |
| 14 | _(additional logic in MAIN-PARA)_ | Yes | **P1** | Data Integrity | Test multi-file cross-validation logic |

---

### CBACT01C -- Account File Refresh (11 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-ACCTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `2000-OUTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 3 | `3000-ARRFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 4 | `4000-VBRFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 5 | `1100-DISPLAY-ACCT-RECORD` | Yes | **P2** | Operational | Test account record display/logging |
| 6 | `1300-POPUL-ACCT-RECORD` | Yes | **P2** | Data Integrity | Test account record population from input |
| 7 | `1350-WRITE-ACCT-RECORD` | Yes | **P2** | Data Integrity | Test account record write to output |
| 8 | `1400-POPUL-ARRAY-RECORD` | Yes | **P2** | Data Integrity | Test array record population |
| 9 | `1450-WRITE-ARRY-RECORD` | No | P3 | Operational | Array write -- implicit |
| 10 | `9000-ACCTFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 11 | `9999-ABEND-PROGRAM` | No | P3 | Operational | Abend handler -- implicit |

---

### CBACT02C -- Card File Refresh (4 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-CARDFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `9000-CARDFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 3 | `9999-ABEND-PROGRAM` | No | P3 | Operational | Abend handler -- implicit |
| 4 | _(main processing in PROCEDURE DIVISION)_ | Yes | **P2** | Data Integrity | Test card file sequential read and load |

---

### CBACT03C -- Cross-Reference File Refresh (4 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-XREFFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `9000-XREFFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 3 | `9999-ABEND-PROGRAM` | No | P3 | Operational | Abend handler -- implicit |
| 4 | _(main processing in PROCEDURE DIVISION)_ | Yes | **P2** | Data Integrity | Test cross-reference file sequential read and load |

---

### CBCUS01C -- Customer File Refresh (4 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-CUSTFILE-OPEN` | No | P3 | Operational | File open -- implicit |
| 2 | `9000-CUSTFILE-CLOSE` | No | P3 | Operational | File close -- implicit |
| 3 | `Z-ABEND-PROGRAM` | No | P3 | Operational | Abend handler -- implicit |
| 4 | _(main processing in PROCEDURE DIVISION)_ | Yes | **P2** | Data Integrity | Test customer file sequential read and load |

---

### CBEXPORT -- Data Export (16 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN-PROCESSING` | Yes | **P1** | Operational | Test full export orchestration |
| 2 | `1100-OPEN-FILES` | No | P3 | Operational | File open -- implicit |
| 3 | `2000-EXPORT-CUSTOMERS` | Yes | **P1** | Data Integrity | Test customer export: all records, correct format |
| 4 | `2100-READ-CUSTOMER-RECORD` | No | P3 | Operational | File read -- implicit |
| 5 | `2200-CREATE-CUSTOMER-EXP-REC` | Yes | **P1** | Data Integrity | Test customer export record format (500-byte layout) |
| 6 | `3000-EXPORT-ACCOUNTS` | Yes | **P1** | Data Integrity | Test account export |
| 7 | `3100-READ-ACCOUNT-RECORD` | No | P3 | Operational | File read -- implicit |
| 8 | `3200-CREATE-ACCOUNT-EXP-REC` | Yes | **P1** | Data Integrity | Test account export record format |
| 9 | `4000-EXPORT-XREFS` | Yes | **P1** | Data Integrity | Test cross-reference export |
| 10 | `4100-READ-XREF-RECORD` | No | P3 | Operational | File read -- implicit |
| 11 | `4200-CREATE-XREF-EXPORT-RECORD` | Yes | **P1** | Data Integrity | Test XREF export record format |
| 12 | `5000-EXPORT-TRANSACTIONS` | No | P3 | Data Integrity | Transaction export -- covered by integration |
| 13 | `5100-READ-TRANSACTION-RECORD` | No | P3 | Operational | File read -- implicit |
| 14 | `5200-CREATE-TRAN-EXP-REC` | No | P3 | Data Integrity | Format -- covered by integration |
| 15 | `5500-EXPORT-CARDS` / `5600-READ-CARD-RECORD` / `5700-CREATE-CARD-EXPORT-RECORD` | No | P3 | Data Integrity | Card export -- covered by integration |
| 16 | `6000-FINALIZE` | No | P3 | Operational | Cleanup -- implicit |

---

### CBIMPORT -- Data Import (14 functions)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `0000-MAIN-PROCESSING` | Yes | **P1** | Operational | Test full import orchestration |
| 2 | `1100-OPEN-FILES` | No | P3 | Operational | File open -- implicit |
| 3 | `2000-PROCESS-EXPORT-FILE` | Yes | **P1** | Data Integrity | Test import main loop: read + route by type |
| 4 | `2100-READ-EXPORT-RECORD` | No | P3 | Operational | File read -- implicit |
| 5 | `2200-PROCESS-RECORD-BY-TYPE` | Yes | **P1** | Data Integrity | Test record type routing (customer/account/xref/transaction/card) |
| 6 | `2300-PROCESS-CUSTOMER-RECORD` | Yes | **P1** | Data Integrity | Test customer import: parse + write to CUSTOUT |
| 7 | `2400-PROCESS-ACCOUNT-RECORD` | Yes | **P1** | Data Integrity | Test account import |
| 8 | `2500-PROCESS-XREF-RECORD` | Yes | **P1** | Data Integrity | Test XREF import |
| 9 | `2600-PROCESS-TRAN-RECORD` | Yes | **P1** | Data Integrity | Test transaction import |
| 10 | `2650-PROCESS-CARD-RECORD` | Yes | **P1** | Data Integrity | Test card import |
| 11 | `2700-PROCESS-UNKNOWN-RECORD` | Yes | **P1** | Data Integrity | Test unknown record type handling -> error file |
| 12 | `2750-WRITE-ERROR` | No | P3 | Operational | Error write -- covered by unknown record test |
| 13 | `3000-VALIDATE-IMPORT` | No | P3 | Operational | Validation summary -- covered by integration |
| 14 | `4000-FINALIZE` | No | P3 | Operational | Cleanup -- implicit |

**Key test scenarios for CBEXPORT + CBIMPORT**:
- Round-trip: export all data -> import -> verify data matches original
- Import with unknown record type -> written to ERROUT
- Import with corrupt data -> error handling
- Empty input files -> graceful handling
- Record count validation

---

## Shared Utilities -- Detailed Coverage

### CSUTLDTC -- Date Validation (1 function)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | `A000-MAIN` | Yes | **P1** | Data Integrity | Test date validation: valid dates accepted, invalid dates rejected (Feb 30, month 13, etc.), format handling (multiple date formats via LS-DATE-FORMAT), LE CEEDAYS integration |

**Key test scenarios**:
- Valid date in each supported format -> success
- Invalid month (00, 13) -> error
- Invalid day (Feb 29 non-leap year, Apr 31) -> error
- Leap year handling (Feb 29) -> success
- Boundary dates (Jan 1, Dec 31) -> success
- Empty/blank date -> error

### COBSWAIT -- Wait Utility (1 function)

| # | Function/Paragraph | In Plan? | Priority | Impact | Test Description |
|---|---|---|---|---|---|
| 1 | _(CALL MVSWAIT in main logic)_ | No | **P3** | Operational | Assembler wait utility -- no business logic to test, replaced by Thread.sleep() or scheduler delay in Java |

---

## Duplicate Logic Candidates

The following duplicate logic patterns were identified across modules. During migration, these should be consolidated into shared Java services to reduce code duplication and testing surface.

| Duplicate Pattern | Modules | Recommended Java Consolidation | Testing Impact |
|---|---|---|---|
| **Abend/error handling** (`9999-ABEND-PROGRAM`, `Z-ABEND-PROGRAM`, `ABEND-ROUTINE`) | CBACT01C-04C, CBTRN01C-03C, CBSTM03A/B, CBEXPORT, CBIMPORT, COCRDLIC, COCRDSLC, COCRDUPC, COACTUPC, COACTVWC | Single exception handler class with structured logging | Reduces ~15 separate test paths to 1 |
| **File I/O status display** (`9910-DISPLAY-IO-STATUS`, `Z-DISPLAY-IO-STATUS`) | CBACT01C-04C, CBTRN01C-03C, CBSTM03A/B | Shared I/O error logging utility | Reduces ~10 test paths to 1 |
| **DB2-format timestamp generation** (`Z-GET-DB2-FORMAT-TIMESTAMP`) | CBTRN02C, CBACT04C | Single `TimestampUtil.toDb2Format()` method | Reduces 2 test paths to 1 |
| **Screen send utilities** (`SEND-PLAIN-TEXT`, `SEND-LONG-TEXT`) | COSGN00C, COACTVWC, COCRDLIC, COCRDSLC | Single message display service | Reduces ~8 test paths to 1 |
| **Header population** (`POPULATE-HEADER-INFO`) | All online programs (14 programs) | Single header component/interceptor | Reduces 14 test paths to 1 |
| **Account number validation** (`EDIT-ACCOUNT` variants: `1210-EDIT-ACCOUNT`, `2210-EDIT-ACCOUNT`) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Single `AccountValidator.validate()` method | Reduces 5 test paths to 1 |
| **Card number validation** (`EDIT-CARD` variants: `1220-EDIT-CARD`, `2220-EDIT-CARD`) | COCRDLIC, COCRDSLC, COCRDUPC | Single `CardValidator.validate()` method | Reduces 3 test paths to 1 |
| **CICS RETURN with TRANSID** (`COMMON-RETURN`) | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC | Framework-level (Spring MVC return) | Eliminated in Java |
| **Browse control** (`STARTBR`/`ENDBR` file pairs) | COTRN00C, COTRN02C, COBIL00C, COUSR00C, COCRDLIC | Database pagination service | Reduces 5 test paths to 1 |
| **Cross-reference lookup** (`GETCARDXREF-BYACCT`, `9200-GETCARDXREF-BYACCT`) | COACTUPC, COACTVWC | Single `CardXrefRepository.findByAccount()` | Reduces 2 test paths to 1 |
| **Card data read** (`GETCARD-BYACCTCARD`, `9100-GETCARD-BYACCTCARD`) | COCRDSLC, COCRDUPC | Single `CardRepository.findByAccountAndCard()` | Reduces 2 test paths to 1 |

**Estimated testing reduction from deduplication**: ~65 redundant test paths eliminated, reducing total unique test cases by approximately 19%.

---

## Risk-Based Test Prioritization

### P0 -- Critical Tests (Must Pass Before Go-Live)

| Test Area | Module(s) | # Test Cases | Rationale |
|---|---|---|---|
| Transaction posting accuracy | CBTRN02C | 8-10 | Financial: incorrect posting = monetary loss |
| Interest calculation precision | CBACT04C | 6-8 | Financial: interest errors compound across accounts |
| Bill payment atomicity | COBIL00C | 5-7 | Financial: partial updates = data corruption |
| Account update validation | COACTUPC | 15-20 | Data Integrity: largest program, most validation rules |
| Transaction add validation | COTRN02C (online) | 8-10 | Financial: creates new financial records |
| Authentication flow | COSGN00C | 4-5 | Security: all access flows through sign-on |
| User security update | COUSR02C (`UPDATE-USER-SEC-FILE`) | 3-4 | Security: password/role changes |
| Card status update | COCRDUPC (`9200-WRITE-PROCESSING`) | 4-5 | Data Integrity: status changes affect transaction processing |

**Estimated P0 test cases: 53-69**

### P1 -- High Priority Tests (Must Pass for Feature Completeness)

| Test Area | Module(s) | # Test Cases | Rationale |
|---|---|---|---|
| Statement generation | CBSTM03A + CBSTM03B | 6-8 | Operational: customer-facing output |
| Transaction report | CBTRN03C | 5-7 | Operational: daily reconciliation |
| Date validation | CSUTLDTC | 6-8 | Shared utility: used by multiple programs |
| Menu navigation | COMEN01C, COADM01C | 4-6 | Operational: all user flows start here |
| Data export/import | CBEXPORT, CBIMPORT | 6-8 | Operational: data migration path |
| Transaction validation | CBTRN01C | 3-4 | Data Integrity: batch pre-validation |
| User CRUD | COUSR00C-03C | 8-10 | Security: user management |
| Card update validation | COCRDUPC | 8-10 | Data Integrity: PCI-sensitive operations |
| Report submission | CORPT00C | 2-3 | Operational: triggers batch reporting |

**Estimated P1 test cases: 48-64**

### P2 -- Medium Priority Tests (Should Pass for Quality)

| Test Area | Module(s) | # Test Cases | Rationale |
|---|---|---|---|
| Account view | COACTVWC | 4-5 | UX: read-only, no data risk |
| Card list pagination | COCRDLIC | 5-7 | UX: pagination and filtering |
| Card view | COCRDSLC | 3-4 | UX: read-only display |
| Transaction list pagination | COTRN00C | 4-5 | UX: pagination |
| Transaction view | COTRN01C | 2-3 | UX: read-only display |
| User list pagination | COUSR00C | 4-5 | UX: pagination |
| Data loaders | CBACT01C-03C, CBCUS01C | 4-6 | Operational: data refresh |

**Estimated P2 test cases: 26-35**

### P3 -- Low Priority Tests (Covered Implicitly)

Infrastructure functions (file open/close, screen send/receive, abend handlers, header population) -- covered by integration tests of their parent modules. No dedicated test cases needed.

**Estimated P3 test cases: 0 dedicated (covered implicitly by P0-P2 tests)**

---

### Total Test Effort Estimate

| Priority | Test Cases | Effort (Days) | Notes |
|---|---|---|---|
| P0 -- Critical | 53-69 | 15-20 | Parallel-run validation required for financial modules |
| P1 -- High | 48-64 | 12-16 | Focus on integration testing |
| P2 -- Medium | 26-35 | 5-8 | Can be automated with UI testing framework |
| P3 -- Low | 0 | 0 | Covered implicitly |
| **Total** | **127-168** | **32-44** | After deduplication: ~103-136 unique test cases |

**Deduplication savings**: Consolidating the 11 duplicate logic patterns identified above reduces the unique test surface by ~19%, saving approximately 24-32 test cases and 6-9 development days.
