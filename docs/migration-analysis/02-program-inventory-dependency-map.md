# CardDemo Program Inventory & Dependency Map

## 1. Program Inventory Summary

| Category | Count | Prefix | Description |
|----------|-------|--------|-------------|
| Online CICS Programs | 17 | CO* | Real-time transaction processing |
| Batch Programs | 8 | CB* | Scheduled batch processing |
| Optional Module Programs | 13 | CO*/CB* | DB2, IMS DB, MQ integration |
| Assembler Programs | 2 | - | System utilities |
| **Total** | **40** | | |

---

## 2. Online CICS Programs (Core)

### 2.1 Authentication & Navigation

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| COSGN00C | CC00 | 261 | Sign-on / Authentication | USRSEC (R) | COMEN01C, COADM01C | (entry point) |
| COMEN01C | CM00 | 309 | Main Menu (Regular User) | - | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC, COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, COPAUS0C | COSGN00C |
| COADM01C | CA00 | 289 | Admin Menu | - | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COTRTLIC, COTRTUPC | COSGN00C |

### 2.2 Account Management

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| COACTVWC | - | 942 | Account View | ACCTDAT (R), CUSTDAT (R), CARDXREF (R) | COMEN01C | COMEN01C |
| COACTUPC | - | 4237 | Account Update | ACCTDAT (R/W), CUSTDAT (R/W), CARDXREF (R) | COMEN01C | COMEN01C |

### 2.3 Card Management

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| COCRDLIC | CCLI | 1460 | Credit Card List | CARDDAT (R), CARDAIX (R) | COCRDSLC, COCRDUPC, COMEN01C | COMEN01C |
| COCRDSLC | CCDL | 888 | Credit Card View | CARDDAT (R), CARDAIX (R), CUSTDAT (R) | COCRDLIC, COMEN01C | COMEN01C, COCRDLIC |
| COCRDUPC | CCUP | 1561 | Credit Card Update | CARDDAT (R/W), CARDAIX (R) | COCRDLIC, COMEN01C | COMEN01C, COCRDLIC |

### 2.4 Transaction Management

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| COTRN00C | CT00 | 700 | Transaction List | TRANSACT (R) | COTRN01C, COMEN01C | COMEN01C |
| COTRN01C | CT01 | 331 | Transaction View | TRANSACT (R) | COTRN00C, COMEN01C | COMEN01C, COTRN00C |
| COTRN02C | CT02 | 784 | Transaction Add | TRANSACT (W), ACCTDAT (R), CCXREF (R), CXACAIX (R) | COMEN01C | COMEN01C |

### 2.5 Reporting & Bill Payment

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| CORPT00C | CR00 | 650 | Transaction Reports | TRANSACT (R), TDQ (W) | COMEN01C | COMEN01C |
| COBIL00C | CB00 | 573 | Bill Payment | TRANSACT (W), ACCTDAT (R/W), CXACAIX (R) | COMEN01C | COMEN01C |

### 2.6 User Management (Admin Only)

| Program | Trans ID | Lines | Function | VSAM Files | Calls To | Called From |
|---------|----------|-------|----------|------------|----------|-------------|
| COUSR00C | CU00 | 696 | User List | USRSEC (R) | COUSR02C, COUSR03C, COADM01C | COADM01C |
| COUSR01C | CU01 | 300 | User Add | USRSEC (W) | COADM01C | COADM01C |
| COUSR02C | CU02 | 415 | User Update | USRSEC (R/W) | COADM01C | COADM01C, COUSR00C |
| COUSR03C | CU03 | 360 | User Delete | USRSEC (R/D) | COADM01C | COADM01C, COUSR00C |

---

## 3. Batch Programs

| Program | Lines | Function | Input Files | Output Files | Key Operations |
|---------|-------|----------|-------------|--------------|----------------|
| CBTRN02C | 732 | Transaction Posting | DALYTRAN (daily trans), XREFFILE, ACCTFILE | TRANSACT, DALYREJS, TCATBAL | Validate daily transactions, post to master, update account balances, track category balances |
| CBACT04C | ~500 | Interest Calculation | ACCTDAT, TRANSACT | ACCTDAT | Calculate and apply interest to accounts |
| CBSTM03A | ~600 | Statement Generation (Sort) | TRANSACT | Sorted work file | Sort transactions for statement generation |
| CBSTM03B | ~600 | Statement Generation (Print) | Sorted work file, CUSTDAT, ACCTDAT | Statement report | Generate customer statements |
| CBTRN03C | ~500 | Transaction Report | TRANSACT | Report file | Generate transaction reports by date range |
| CBEXPORT | ~300 | Data Export | VSAM files | Sequential files | Export VSAM data to sequential format |
| CBIMPORT | ~300 | Data Import | Sequential files | VSAM files | Import sequential data to VSAM files |
| COBSWAIT | ~50 | Wait Utility | - | - | Assembler-assisted wait for batch timing |

---

## 4. Optional Module Programs

### 4.1 Authorization Module (IMS DB + DB2 + MQ)

| Program | Type | Function |
|---------|------|----------|
| COPAUA0C | Online | MQ trigger for authorization requests |
| COPAUS0C | Online | Pending authorization summary view |
| COPAUS1C | Online | Pending authorization detail view |
| COPAUS2C | Online | Mark transaction as fraud (writes to DB2) |
| CBPAUP0C | Batch | Purge old authorization records |

### 4.2 Transaction Type Management (DB2)

| Program | Type | Function |
|---------|------|----------|
| COTRTLIC | Online | Transaction type list and delete (DB2 cursors) |
| COTRTUPC | Online | Transaction type add and edit (DB2 embedded SQL) |
| COBTUPDT | Batch | Batch update of transaction types |

### 4.3 VSAM-MQ Integration

| Program | Type | Function |
|---------|------|----------|
| CODATE01 | Online | MQ request/response for system date |
| COACCT01 | Online | MQ request/response for account inquiry |

---

## 5. Program Dependency Map

### 5.1 Navigation Call Chain

```
COSGN00C (Sign-on)
├── [Admin User] --> COADM01C (Admin Menu)
│   ├── 1. COUSR00C (User List)
│   │   ├── [U] COUSR02C (User Update)
│   │   └── [D] COUSR03C (User Delete)
│   ├── 2. COUSR01C (User Add)
│   ├── 3. COUSR02C (User Update)
│   ├── 4. COUSR03C (User Delete)
│   ├── 5. COTRTLIC (Tran Type List - DB2)
│   └── 6. COTRTUPC (Tran Type Maintenance - DB2)
│
└── [Regular User] --> COMEN01C (Main Menu)
    ├──  1. COACTVWC (Account View)
    ├──  2. COACTUPC (Account Update)
    ├──  3. COCRDLIC (Card List)
    │   ├── [S] COCRDSLC (Card View)
    │   └── [U] COCRDUPC (Card Update)
    ├──  4. COCRDSLC (Card View)
    ├──  5. COCRDUPC (Card Update)
    ├──  6. COTRN00C (Transaction List)
    │   └── [S] COTRN01C (Transaction View)
    ├──  7. COTRN01C (Transaction View)
    ├──  8. COTRN02C (Transaction Add)
    ├──  9. CORPT00C (Transaction Reports)
    ├── 10. COBIL00C (Bill Payment)
    └── 11. COPAUS0C (Pending Auth View - optional)
```

### 5.2 VSAM File Access Map

```
USRSEC (User Security)
├── COSGN00C ---- READ (authenticate)
├── COUSR00C ---- BROWSE (list users)
├── COUSR01C ---- WRITE (add user)
├── COUSR02C ---- READ/REWRITE (update user)
└── COUSR03C ---- READ/DELETE (delete user)

ACCTDAT (Account Master)
├── COACTVWC ---- READ (view account)
├── COACTUPC ---- READ/REWRITE (update account)
├── COTRN02C ---- READ (validate for new transaction)
├── COBIL00C ---- READ/REWRITE (bill payment updates balance)
├── CBTRN02C ---- READ/REWRITE (batch posting updates balance)
└── CBACT04C ---- READ/REWRITE (interest calculation)

CARDDAT (Card Master)
├── COCRDLIC ---- BROWSE (list cards)
├── COCRDSLC ---- READ (view card)
└── COCRDUPC ---- READ/REWRITE (update card)

CARDAIX (Card Alternate Index - by Account)
├── COCRDLIC ---- BROWSE (list by account)
├── COCRDSLC ---- READ (lookup by account)
└── COCRDUPC ---- READ (lookup by account)

CUSTDAT (Customer Master)
├── COACTVWC ---- READ (view customer info)
├── COACTUPC ---- READ/REWRITE (update customer)
├── COCRDSLC ---- READ (card detail includes customer)
└── CBSTM03B ---- READ (statement generation)

TRANSACT (Transaction Master)
├── COTRN00C ---- BROWSE (list transactions)
├── COTRN01C ---- READ (view transaction)
├── COTRN02C ---- WRITE (add transaction)
├── COBIL00C ---- BROWSE/WRITE (bill payment creates transaction)
├── CBTRN02C ---- WRITE (batch post transactions)
├── CBTRN03C ---- READ (transaction reporting)
└── CORPT00C ---- READ (online report generation)

CCXREF (Card Cross-Reference)
├── COACTVWC ---- READ (account->card lookup)
├── COTRN02C ---- READ (card->account lookup)
├── COBIL00C ---- READ (account->card lookup)
└── CBTRN02C ---- READ (validate card number)

CXACAIX (Cross-Reference Alternate Index - by Account)
├── COTRN02C ---- READ (account->card lookup)
└── COBIL00C ---- READ (account->card lookup)

TCATBAL (Transaction Category Balance)
└── CBTRN02C ---- READ/WRITE/REWRITE (maintain category balances)
```

---

## 6. Copybook Usage Matrix

| Copybook | Description | Used By Programs |
|----------|-------------|-----------------|
| COCOM01Y | COMMAREA (inter-program communication) | ALL online programs |
| COMEN02Y | Main menu option definitions | COMEN01C |
| COADM02Y | Admin menu option definitions | COADM01C |
| COTTL01Y | Screen titles/headers | ALL online programs |
| CSDAT01Y | Date/time formatting | ALL online programs |
| CSMSG01Y | Common messages | ALL online programs |
| CSUSR01Y | User security record (80 bytes) | COSGN00C, COUSR00C-03C, COADM01C |
| CVACT01Y | Account record (300 bytes) | COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBTRN02C |
| CVACT02Y | Card record (150 bytes) | COCRDLIC, COCRDSLC, COCRDUPC |
| CVACT03Y | Card cross-reference (50 bytes) | COACTVWC, COTRN02C, COBIL00C, CBTRN02C |
| CVCUS01Y | Customer record (500 bytes) | COACTVWC, COACTUPC, COCRDSLC |
| CVTRA05Y | Transaction record (350 bytes) | COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN02C |
| CVTRA06Y | Daily transaction record (350 bytes) | CBTRN02C |
| CVTRA01Y | Transaction category balance (50 bytes) | CBTRN02C |
| CVCRD01Y | Card data variables | COCRDLIC, COCRDSLC, COCRDUPC |
| CSUTLDWY | Date utility working storage | COACTUPC |
| DFHAID | CICS attention identifier constants | ALL online programs |
| DFHBMSCA | BMS screen attribute constants | ALL online programs |

---

## 7. BMS Screen Map Inventory

| Map Set | Map Name | Associated Program | Screen Function |
|---------|----------|-------------------|-----------------|
| COSGN00 | COSGN0A | COSGN00C | Sign-on screen |
| COMEN01 | COMEN1A | COMEN01C | Main menu |
| COADM01 | COADM1A | COADM01C | Admin menu |
| COACTVW | COACTVWA | COACTVWC | Account view |
| COACTUP | COACTUPA | COACTUPC | Account update |
| COCRDLI | COCRDLIA | COCRDLIC | Card list |
| COCRDSL | COCRDSLA | COCRDSLC | Card view detail |
| COCRDUP | CCRDUPA | COCRDUPC | Card update |
| COTRN00 | COTRN0A | COTRN00C | Transaction list |
| COTRN01 | COTRN1A | COTRN01C | Transaction view |
| COTRN02 | COTRN2A | COTRN02C | Transaction add |
| CORPT00 | CORPT0A | CORPT00C | Transaction report |
| COBIL00 | COBIL0A | COBIL00C | Bill payment |
| COUSR00 | COUSR0A | COUSR00C | User list |
| COUSR01 | COUSR1A | COUSR01C | User add |
| COUSR02 | COUSR2A | COUSR02C | User update |
| COUSR03 | COUSR3A | COUSR03C | User delete |

---

## 8. Duplicate Logic Identified for Consolidation

The following patterns of duplicate logic were identified across multiple programs and should be consolidated during Java migration:

### 8.1 Header Population (POPULATE-HEADER-INFO)
**Found in**: ALL 17 online programs
**Pattern**: Each program has an identical `POPULATE-HEADER-INFO` paragraph that formats date/time and populates screen header fields.
**Consolidation**: Extract to a shared `HeaderService` or `BaseController` method.

### 8.2 Screen Send/Receive Pattern
**Found in**: ALL 17 online programs
**Pattern**: Each program has nearly identical SEND MAP / RECEIVE MAP paragraphs with only the map name differing.
**Consolidation**: In Spring, this is handled automatically by the framework's request/response handling.

### 8.3 COMMAREA Initialization and Program Transfer
**Found in**: ALL 17 online programs
**Pattern**: Identical logic for checking EIBCALEN, initializing COMMAREA, handling PF3 (return to previous screen), XCTL to next program.
**Consolidation**: Extract to a `NavigationService` or use Spring's request routing.

### 8.4 Error Flag Handling
**Found in**: ALL online programs
**Pattern**: `WS-ERR-FLG`, `ERR-FLG-ON`, `ERR-FLG-OFF` with identical flag check/set logic.
**Consolidation**: Use Java exceptions and Spring's `@ControllerAdvice` for centralized error handling.

### 8.5 VSAM File Read with Error Handling
**Found in**: COACTVWC, COACTUPC, COBIL00C, COTRN01C, COTRN02C, COUSR02C, COUSR03C
**Pattern**: Identical `READ DATASET ... RESP ... EVALUATE WS-RESP-CD WHEN DFHRESP(NORMAL) ... WHEN DFHRESP(NOTFND) ... WHEN OTHER` blocks.
**Consolidation**: Extract to a shared `DataAccessService` with generic read/write methods, or use Spring Data JPA repositories which handle this automatically.

### 8.6 Pagination Logic (STARTBR/READNEXT/READPREV/ENDBR)
**Found in**: COTRN00C, COUSR00C, COCRDLIC
**Pattern**: Identical forward/backward pagination with 10-record pages, next-page flag management.
**Consolidation**: Use Spring Data's `Pageable` and `Page<T>` for all list operations.

### 8.7 Input Validation Patterns
**Found in**: COACTUPC, COCRDUPC, COTRN02C, COBIL00C
**Pattern**: Repetitive field-by-field validation (empty check, numeric check, range check) with same error message handling.
**Consolidation**: Use Bean Validation (JSR 380) annotations (`@NotNull`, `@Size`, `@Pattern`, etc.) on DTOs.

### 8.8 Date Validation and Formatting
**Found in**: COTRN02C, CORPT00C, COACTUPC
**Pattern**: Manual date format validation (YYYY-MM-DD) and date arithmetic.
**Consolidation**: Use `java.time.LocalDate` with `DateTimeFormatter` and validation annotations.

### 8.9 Identical File Read Operations (Account + Cross-Reference)
**Found in**: COBIL00C, COTRN02C
**Pattern**: Both programs read ACCTDAT and CXACAIX with identical logic to resolve account-to-card relationships.
**Consolidation**: Create a single `AccountCardResolutionService` with shared lookup methods.

### 8.10 Estimated Duplicate Line Reduction

| Duplicate Pattern | Instances | ~Lines Each | Total Duplicate Lines | Consolidation Savings |
|------------------|-----------|-------------|----------------------|----------------------|
| Header Population | 17 | 15 | 255 | ~240 lines |
| Send/Receive Screen | 17 | 20 | 340 | ~320 lines |
| COMMAREA/Navigation | 17 | 25 | 425 | ~400 lines |
| Error Flag Handling | 17 | 10 | 170 | ~160 lines |
| VSAM Read + Error | 7 | 30 | 210 | ~180 lines |
| Pagination Logic | 3 | 80 | 240 | ~220 lines |
| Input Validation | 4 | 50 | 200 | ~180 lines |
| Date Handling | 3 | 20 | 60 | ~50 lines |
| **Total** | | | **~1,900** | **~1,750 lines** |

This represents approximately **13% of the total online COBOL codebase** that can be consolidated through proper Java design patterns and framework capabilities.

---

## 9. Complexity Assessment

| Program | Lines | Complexity | Migration Effort | Notes |
|---------|-------|-----------|-----------------|-------|
| COSGN00C | 261 | Low | Low | Simple authentication logic |
| COMEN01C | 309 | Low | Low | Menu routing only |
| COADM01C | 289 | Low | Low | Menu routing only |
| COACTVWC | 942 | Medium | Medium | Multi-file reads, display formatting |
| **COACTUPC** | **4237** | **High** | **High** | **Largest program; extensive validation, multi-file updates** |
| COCRDLIC | 1460 | Medium | Medium | Pagination, alternate index browsing |
| COCRDSLC | 888 | Medium | Low-Medium | Read and display |
| **COCRDUPC** | **1561** | **High** | **Medium-High** | **Complex validation, optimistic locking, update workflow** |
| COTRN00C | 700 | Medium | Medium | Pagination, selection handling |
| COTRN01C | 331 | Low | Low | Simple read and display |
| COTRN02C | 784 | Medium | Medium | Multi-file validation, transaction creation |
| CORPT00C | 650 | Medium | Medium | JCL job submission from online, date handling |
| COBIL00C | 573 | Medium | Medium | Multi-file operations, transaction creation |
| COUSR00C | 696 | Medium | Medium | Pagination, user list |
| COUSR01C | 300 | Low | Low | Simple user add |
| COUSR02C | 415 | Low-Medium | Low | Read, modify, rewrite |
| COUSR03C | 360 | Low | Low | Read, delete |
| **CBTRN02C** | **732** | **High** | **High** | **Core batch; 6 files, validation, posting, category tracking** |
| CBACT04C | ~500 | Medium | Medium | Interest calculation logic |
| CBSTM03A/B | ~1200 | Medium | Medium | Statement generation (two-pass) |
| CBTRN03C | ~500 | Medium | Medium | Report generation |

**Overall Migration Effort Estimate**: The core 17 online programs + 8 batch programs represent approximately **15,000-17,000 lines of COBOL** to be converted.
