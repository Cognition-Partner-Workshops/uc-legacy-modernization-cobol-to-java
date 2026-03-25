# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
>
> This report identifies the top 10 highest-priority modules for modernization, ranked by a composite score of code complexity, migration risk, and business impact.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
3. [Detailed Analysis per Module](#detailed-analysis)
4. [Complexity Metrics Summary (All Programs)](#complexity-metrics-summary)
5. [Risk Heat Map](#risk-heat-map)
6. [Recommended Modernization Sequence](#recommended-modernization-sequence)

---

## Scoring Methodology

Each module is evaluated across three dimensions on a 1-10 scale:

| Dimension           | Weight | Factors Measured                                                           |
|---------------------|--------|----------------------------------------------------------------------------|
| **Code Complexity** | 35%    | Lines of code, copybook count, EVALUATE/IF nesting depth, number of PERFORM paragraphs, file I/O operations, REPLACING/REDEFINES usage |
| **Migration Risk**  | 35%    | Multi-file I/O, CICS API usage density, assembler/LE calls, screen map complexity, data validation patterns, error handling paths |
| **Business Impact** | 30%    | Transaction volume affected, financial data exposure, number of downstream dependents, user-facing criticality, regulatory sensitivity |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Impact x 0.30)

---

## Top 10 Hotspot Rankings

| Rank | Program    | Type   | LOC   | Complexity | Risk | Impact | **Score** | Primary Concern                          |
|------|------------|--------|-------|------------|------|--------|-----------|------------------------------------------|
| 1    | COACTUPC   | Online | 4,237 | 10         | 10   | 9      | **9.70**  | Largest program; 40+ field validations   |
| 2    | CBTRN02C   | Batch  | 731   | 8          | 9    | 10     | **8.95**  | Core transaction posting; 6 files        |
| 3    | CBACT04C   | Batch  | 652   | 7          | 8    | 10     | **8.25**  | Interest calc; financial accuracy        |
| 4    | CBSTM03A   | Batch  | 924   | 9          | 8    | 7      | **8.05**  | Statement gen; ALTER verb; calls sub-pgm |
| 5    | COCRDLIC   | Online | 1,460 | 8          | 7    | 7      | **7.35**  | Browse/pagination; multi-file reads      |
| 6    | COCRDUPC   | Online | 1,560 | 8          | 7    | 7      | **7.35**  | Card update; REWRITE + validation        |
| 7    | COSGN00C   | Online | 261   | 3          | 6    | 10     | **6.15**  | Authentication gateway; security-critical|
| 8    | CBEXPORT   | Batch  | 582   | 7          | 7    | 6      | **6.70**  | Multi-entity export; REDEFINES layout    |
| 9    | COTRN02C   | Online | 783   | 7          | 7    | 7      | **7.00**  | Transaction add; date calls; 3 files     |
| 10   | COTRN00C   | Online | 699   | 6          | 6    | 7      | **6.30**  | Transaction browse; STARTBR/READNEXT     |

---

## Detailed Analysis

### Rank 1: COACTUPC (Account Update) - Score: 9.70

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 4,237 (largest program in the entire application)        |
| **Copybooks Included**    | 15 (COCOM01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CSUSR01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, COTTL01Y, COACTUP, CSSETATY, CSSTRPFY, CSLKPCDY, CSUTLDWY, CSUTLDPY) |
| **VSAM Files Accessed**   | 3 (ACCTDAT R/W, CUSTDAT R/W, CXACAIX R)                 |
| **COPY REPLACING**        | 40+ uses of CSSETATY with REPLACING for field attributes |
| **Validation Patterns**   | US state code lookup (50 states), ZIP code validation, phone area code validation (1,000+ entries in CSLKPCDY), date validation, SSN format checking |
| **Screen Fields**         | 25+ editable fields on single BMS map                    |

**Why It's #1:**
- At 4,237 lines, it is 2.7x larger than the next largest program
- Contains the most complex validation logic in the entire application, including embedded lookup tables for all US states, area codes, and ZIP code ranges
- Uses the `COPY ... REPLACING` pattern over 40 times for field attribute management, which has no direct Java equivalent
- Performs REWRITE operations on two master files (Account + Customer), making it a dual-entity update with transaction integrity concerns
- The CSLKPCDY copybook alone is 1,314 lines of lookup table data that needs to be extracted to a reference data service

**Modernization Recommendations:**
- Decompose into separate Account Update and Customer Update services
- Extract validation logic into a shared validation library
- Replace CSLKPCDY lookup tables with a database-driven reference data service
- Convert CSSETATY field attribute pattern to annotation-based validation (e.g., Bean Validation / JSR 380)
- Estimated effort: **High** (3-4 weeks)

---

### Rank 2: CBTRN02C (Transaction Posting) - Score: 8.95

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 731                                                      |
| **Copybooks Included**    | 2 (CVTRA05Y, CVTRA06Y)                                  |
| **Files Accessed**        | 6 (DALYTRAN R, TRANSACT W, ACCTDAT R/W, CARDXREF R, TCATBALF R/W, DALYREJS W) |
| **Error Handling**        | CEE3ABD abend call; multi-file rollback concerns         |
| **Batch Operations**      | Sequential read of input, keyed read/write of 4 files    |

**Why It's #2:**
- This is the **core financial engine** of the application - every transaction that flows through the system is posted by this program
- Touches 6 different files in a single run, creating complex data integrity requirements
- Updates account balances (financial accuracy is critical) and category balances (for interest calculation downstream)
- Any bug in modernization directly affects financial statements and regulatory reporting
- Rejection handling writes to a separate file, requiring error recovery design

**Modernization Recommendations:**
- Implement as a Spring Batch job with chunk-oriented processing
- Use database transactions (ACID) to replace multi-file consistency
- Create dedicated reject handling with retry/dead-letter queue
- Add comprehensive reconciliation logging
- Estimated effort: **High** (2-3 weeks)

---

### Rank 3: CBACT04C (Interest Calculation) - Score: 8.25

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 652                                                      |
| **Copybooks Included**    | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y)   |
| **Files Accessed**        | 5 (TCATBALF R, CARDXREF/AIX R, ACCTDAT R/W, DISCGRP R, SYSTRAN W) |
| **Financial Logic**       | Interest = Category Balance x Disclosure Rate / 365      |
| **Error Handling**        | CEE3ABD abend call                                       |

**Why It's #3:**
- **Financial accuracy is paramount** - incorrect interest calculations have direct regulatory and customer impact
- Complex join logic across 5 files: reads category balances, looks up interest rates from disclosure groups, and applies them to generate interest transactions
- The interest calculation formula (balance x rate / 365) must be preserved exactly during migration
- Generated interest transactions feed into the next batch cycle (COMBTRAN), creating a cascading dependency
- Rounding and precision handling in COBOL `PIC S9(09)V99` must map correctly to Java `BigDecimal`

**Modernization Recommendations:**
- Implement with `BigDecimal` for all monetary calculations (never use `double`/`float`)
- Create a dedicated Interest Calculation Service with comprehensive unit tests
- Add audit logging for every interest charge generated
- Implement configurable rate tables (replace VSAM with database)
- Estimated effort: **High** (2-3 weeks)

---

### Rank 4: CBSTM03A (Statement Generation) - Score: 8.05

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 924                                                      |
| **Copybooks Included**    | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y)                |
| **Files Accessed**        | 6 (TRXFL R, CARDXREF R, ACCTDAT R, CUSTDAT R, STMTFILE W, HTMLFILE W) |
| **Sub-program Calls**     | CBSTM03B called 13+ times for formatting                 |
| **Output Formats**        | Dual output: plain text (.PS) + HTML                     |
| **Special COBOL**         | Uses `ALTER` verb (self-modifying code)                  |

**Why It's #4:**
- Uses the **ALTER verb** (lines 300-309), which is self-modifying code - one of the most difficult COBOL constructs to translate to Java, as it dynamically changes paragraph branch targets at runtime
- Generates customer-facing statements in two formats (text and HTML), requiring layout preservation
- Calls sub-program CBSTM03B 13+ times for print formatting, creating tight coupling between the two modules
- Reads 4 master files and produces 2 output files, with complex control break logic for account grouping
- The file OPEN sequence uses ALTER to redirect paragraph flow based on which file needs to be opened next

**Modernization Recommendations:**
- Replace ALTER verb with explicit conditional logic (switch/if-else)
- Migrate CBSTM03A + CBSTM03B as a single Spring Batch job with a template engine (Thymeleaf/FreeMarker) for statement rendering
- Replace fixed-format text output with PDF generation
- Consider modern statement delivery (email, portal)
- Estimated effort: **High** (2-3 weeks)

---

### Rank 5: COCRDLIC (Credit Card List) - Score: 7.35

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 1,460                                                    |
| **Copybooks Included**    | 9 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSMSG02Y) |
| **Files Accessed**        | 2 (CARDDAT R/Browse, CARDAIX R/Browse)                   |
| **CICS Operations**       | SEND MAP, RECEIVE MAP, STARTBR, READNEXT, READPREV, ENDBR |
| **Navigation**            | XCTLs to COCRDSLC (detail), COCRDUPC (update), COMEN01C (menu) |

**Why It's #5:**
- Implements forward/backward **pagination** over VSAM browse, which is a complex pattern to modernize (STARTBR/READNEXT/READPREV/ENDBR maps to SQL cursor or paginated query)
- At 1,460 lines, it has substantial screen handling logic including row selection ('S' for view, 'U' for update)
- Uses alternate index path (CARDAIX) for account-based card lookup
- Serves as a navigation hub that transfers control to two other programs (detail and update)
- The browse state must be maintained across pseudo-conversational CICS interactions

**Modernization Recommendations:**
- Implement as a REST endpoint returning paginated JSON (Spring Data pagination)
- Replace VSAM browse with SQL `ORDER BY ... LIMIT ... OFFSET` or keyset pagination
- Create a Card List UI component with server-side pagination
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 6: COCRDUPC (Credit Card Update) - Score: 7.35

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 1,560                                                    |
| **Copybooks Included**    | 12 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y) |
| **Files Accessed**        | 3 (CARDDAT R/W, CUSTDAT R, possibly CARDXREF)           |
| **CICS Operations**       | READ, REWRITE, SEND MAP, RECEIVE MAP                    |
| **Validation**            | Card number, expiration date, status, embossed name      |

**Why It's #6:**
- Second largest online program (1,560 lines) with significant validation logic
- Performs REWRITE operations on card master file (CARDDAT), requiring optimistic locking consideration
- Uses CSSTRPFY (string strip function) via COPY, which needs equivalent Java utility
- Reads across card and customer files for context display
- Includes confirm/cancel workflow pattern that maps to a multi-step form in web UI

**Modernization Recommendations:**
- Create a Card Update REST endpoint with request validation
- Implement optimistic locking (version field or ETag)
- Extract validation into shared Bean Validation constraints
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 7: COSGN00C (Sign-On / Authentication) - Score: 6.15

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 261 (small but critical)                                 |
| **Copybooks Included**    | 7 (COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID/DFHBMSCA) |
| **Files Accessed**        | 1 (USRSEC R)                                             |
| **Security Concern**      | Plain-text password comparison; no encryption/hashing    |

**Why It's #7:**
- Despite being only 261 lines, this is the **single entry point** for the entire application
- Every user session flows through this program - it is the **authentication gateway**
- Currently uses **plain-text password storage and comparison** (PIC X(08)), which is a critical security modernization target
- Determines user routing: Admin users → COADM01C, Regular users → COMEN01C
- Establishes the COMMAREA session context that all downstream programs depend on
- A bug here blocks all application access

**Modernization Recommendations:**
- Replace with Spring Security authentication (OAuth2/JWT)
- Implement bcrypt/scrypt password hashing
- Add multi-factor authentication support
- Create session management with proper timeout handling
- This should be modernized **first** as all other programs depend on authentication
- Estimated effort: **Medium** (1-2 weeks, but high design importance)

---

### Rank 8: CBEXPORT (Data Export) - Score: 6.70

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 582                                                      |
| **Copybooks Included**    | 6 (CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT) |
| **Files Accessed**        | 6 (CUSTDAT R, ACCTDAT R, CARDXREF R, TRANSACT R, CARDDAT R, EXPORT W) |
| **Data Patterns**         | REDEFINES-based multi-record type output                 |
| **COMP/COMP-3 Fields**    | Uses packed decimal and binary fields in export layout    |

**Why It's #8:**
- Reads from **all 5 master files** and writes a unified export file
- The CVEXPORT copybook uses `REDEFINES` to overlay 5 different record types in the same 500-byte record, a pattern that requires a discriminated union or polymorphic type in Java
- Uses `COMP` (binary) and `COMP-3` (packed decimal) fields for storage optimization, which need careful byte-level conversion
- Part of the data migration pathway - critical for any phased modernization approach
- The EXPORT-REC-TYPE field (C/A/T/X/D) discriminates the record type

**Modernization Recommendations:**
- Implement as a Spring Batch export job writing JSON or CSV
- Replace REDEFINES with a class hierarchy (base ExportRecord + subclasses)
- Handle COMP/COMP-3 encoding if interfacing with legacy systems during transition
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 9: COTRN02C (Transaction Add) - Score: 7.00

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 783                                                      |
| **Copybooks Included**    | 9 (COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID/DFHBMSCA) |
| **Files Accessed**        | 4 (TRANSACT R/W, CXACAIX R, CCXREF R, WS-TRANSACT-FILE browse) |
| **Sub-program Calls**     | CSUTLDTC called 2 times for date validation              |
| **CICS Operations**       | READ, WRITE, STARTBR, READPREV, ENDBR, SEND MAP, RECEIVE MAP |

**Why It's #9:**
- Creates new financial transactions - a **write-path** program with direct balance impact
- Generates unique transaction IDs by reading the last transaction (READPREV on STARTBR) and incrementing
- Calls CSUTLDTC utility for date validation (origination and processing dates)
- Reads cross-reference files to validate the card-account relationship before writing
- The ID generation pattern (read-last + increment) needs to be replaced with a sequence generator to avoid race conditions

**Modernization Recommendations:**
- Implement as a Transaction Create REST endpoint
- Replace READPREV ID generation with database sequence or UUID
- Use database transactions for atomicity
- Add event publishing for downstream processing
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 10: COTRN00C (Transaction List/Browse) - Score: 6.30

| Metric                    | Value                                                    |
|---------------------------|----------------------------------------------------------|
| **Lines of Code**         | 699                                                      |
| **Copybooks Included**    | 7 (COCOM01Y, COTRN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID/DFHBMSCA) |
| **Files Accessed**        | 1 (TRANSACT R/Browse)                                    |
| **CICS Operations**       | STARTBR, READNEXT, READPREV, ENDBR, SEND MAP, RECEIVE MAP |
| **Navigation**            | XCTLs to COTRN01C (detail view), COMEN01C (menu)        |

**Why It's #10:**
- Implements bidirectional browse (forward/backward pagination) over the transaction VSAM file
- Uses STARTBR with GTEQ (greater-than-or-equal) positioning for efficient browse
- Displays multiple transactions per screen with selection capability
- The READNEXT/READPREV pattern across pseudo-conversational boundaries requires careful state management in modernization
- Links to transaction detail view (COTRN01C) via XCTL

**Modernization Recommendations:**
- Implement as a paginated REST endpoint using Spring Data
- Replace VSAM browse with SQL query with ORDER BY and pagination
- Maintain filter/search criteria in URL parameters (stateless)
- Estimated effort: **Low-Medium** (1 week)

---

## Complexity Metrics Summary

### All Programs Ranked by Lines of Code

| Rank | Program    | LOC   | Type   | Copybooks | Files | Calls Out | Called By      |
|------|------------|-------|--------|-----------|-------|-----------|----------------|
| 1    | COACTUPC   | 4,237 | Online | 15        | 3     | 0         | COMEN01C       |
| 2    | COCRDUPC   | 1,560 | Online | 12        | 3     | 0         | COMEN01C, COCRDLIC |
| 3    | COCRDLIC   | 1,460 | Online | 9         | 2     | 2 XCTLs   | COMEN01C       |
| 4    | COACTVWC   | 942   | Online | 11        | 4     | 0         | COMEN01C       |
| 5    | CBSTM03A   | 924   | Batch  | 4         | 6     | 13 CALLs  | CREASTMT.JCL   |
| 6    | COCRDSLC   | 888   | Online | 10        | 3     | 0         | COCRDLIC       |
| 7    | COTRN02C   | 783   | Online | 9         | 4     | 2 CALLs   | COMEN01C       |
| 8    | CBTRN02C   | 731   | Batch  | 2         | 6     | 1 CALL    | POSTTRAN.jcl   |
| 9    | COTRN00C   | 699   | Online | 7         | 1     | 1 XCTL    | COMEN01C       |
| 10   | COUSR00C   | 695   | Online | 7         | 1     | 2 XCTLs   | COADM01C       |
| 11   | CBACT04C   | 652   | Batch  | 5         | 5     | 1 CALL    | INTCALC.jcl    |
| 12   | CORPT00C   | 649   | Online | 7         | 1     | 2 CALLs   | COMEN01C       |
| 13   | CBTRN03C   | 649   | Batch  | 5         | 4     | 1 CALL    | TRANREPT.jcl   |
| 14   | CBEXPORT   | 582   | Batch  | 6         | 6     | 1 CALL    | CBEXPORT.jcl   |
| 15   | COBIL00C   | 572   | Online | 7         | 3     | 0         | COMEN01C       |
| 16   | CBTRN01C   | 494   | Batch  | 1         | 1     | 1 CALL    | -              |
| 17   | CBIMPORT   | 487   | Batch  | 1         | 1     | 1 CALL    | CBIMPORT.jcl   |
| 18   | CBACT01C   | 430   | Batch  | 2         | 1     | 2 CALLs   | -              |
| 19   | COUSR02C   | 414   | Online | 7         | 1     | 0         | COADM01C, COUSR00C |
| 20   | COUSR03C   | 359   | Online | 7         | 1     | 0         | COADM01C, COUSR00C |
| 21   | COTRN01C   | 330   | Online | 6         | 1     | 0         | COTRN00C       |
| 22   | COMEN01C   | 309   | Online | 9         | 0     | 11 XCTLs  | COSGN00C       |
| 23   | COUSR01C   | 299   | Online | 7         | 1     | 0         | COADM01C       |
| 24   | COADM01C   | 288   | Online | 8         | 0     | 6 XCTLs   | COSGN00C       |
| 25   | COSGN00C   | 261   | Online | 7         | 1     | 2 XCTLs   | (entry point)  |
| 26   | CBSTM03B   | 230   | Batch  | 0         | 0     | 0         | CBSTM03A       |
| 27   | CBACT02C   | 178   | Batch  | 1         | 1     | 1 CALL    | -              |
| 28   | CBACT03C   | 178   | Batch  | 1         | 1     | 1 CALL    | -              |
| 29   | CBCUS01C   | 178   | Batch  | 1         | 1     | 1 CALL    | -              |
| 30   | CSUTLDTC   | 157   | Util   | 0         | 0     | 1 CALL    | COTRN02C, CORPT00C |
| 31   | COBSWAIT   | 41    | Util   | 0         | 0     | 1 CALL    | WAITSTEP.jcl   |

**Total Core COBOL:** 20,650 lines across 31 programs

---

## Risk Heat Map

```
                    LOW Business Impact          HIGH Business Impact
                    ◄──────────────────────────────────────────────►

HIGH Complexity   │ CBSTM03A (Statements)    │ COACTUPC (Acct Update) │
     ▲            │ COCRDLIC (Card List)      │ CBTRN02C (Posting)     │
     │            │ COCRDUPC (Card Update)    │ CBACT04C (Interest)    │
     │            ├──────────────────────────-┼────────────────────────┤
     │            │ CBEXPORT (Export)         │ COTRN02C (Tran Add)    │
     │            │ CBIMPORT (Import)         │ COBIL00C (Bill Pay)    │
     │            │ COTRN00C (Tran List)      │ COSGN00C (Sign-On)     │
     │            ├───────────────────────────┼────────────────────────┤
LOW Complexity    │ CBACT01C-03C (Data Load)  │ COMEN01C (Main Menu)   │
                  │ CBCUS01C (Customer Load)  │ COADM01C (Admin Menu)  │
                  │ COBSWAIT (Wait)           │ COUSR00C-03C (Users)   │
                  │ CSUTLDTC (Date Util)      │                        │
                  └───────────────────────────┴────────────────────────┘
```

**Legend:**
- **Top-Right (High Complexity + High Impact):** Modernize with maximum care and testing
- **Top-Left (High Complexity + Low Impact):** Simplify during modernization
- **Bottom-Right (Low Complexity + High Impact):** Quick wins - modernize early for high ROI
- **Bottom-Left (Low Complexity + Low Impact):** Straightforward conversion; lower priority

---

## Recommended Modernization Sequence

Based on the hotspot analysis, here is the recommended order for modernizing the CardDemo application:

### Phase 1: Foundation (Weeks 1-3)
*Build the core infrastructure that all other modules depend on.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 1     | COSGN00C   | Authentication gateway - all programs depend on it      |
| 2     | COMEN01C   | Navigation hub - defines the application structure      |
| 3     | COADM01C   | Admin navigation - needed for user management testing   |
| 4     | COCOM01Y   | COMMAREA → Session/DTO design (shared by all programs)  |

### Phase 2: Core Business Logic (Weeks 3-8)
*Modernize the highest-risk financial processing modules.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 5     | CBTRN02C   | Core posting engine - highest business impact            |
| 6     | CBACT04C   | Interest calculation - financial accuracy critical       |
| 7     | COACTUPC   | Largest/most complex program - account update            |
| 8     | COTRN02C   | Transaction creation - write-path for new transactions   |

### Phase 3: Read/Browse Operations (Weeks 8-11)
*Modernize the inquiry and browse programs.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 9     | COTRN00C   | Transaction list - high-use browse pattern               |
| 10    | COCRDLIC   | Card list - complex pagination                           |
| 11    | COACTVWC   | Account view - multi-file read                           |
| 12    | COCRDSLC   | Card detail view                                         |
| 13    | COTRN01C   | Transaction detail view                                  |

### Phase 4: Update Operations & Reports (Weeks 11-14)
*Modernize remaining update programs and reporting.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 14    | COCRDUPC   | Card update                                              |
| 15    | COBIL00C   | Bill payment - financial write operation                  |
| 16    | CBSTM03A/B | Statement generation (pair)                              |
| 17    | CBTRN03C   | Transaction report generation                            |
| 18    | CORPT00C   | Report request (online)                                  |

### Phase 5: Admin & Utilities (Weeks 14-16)
*Modernize administrative functions and batch utilities.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 19    | COUSR00C   | User list                                                |
| 20    | COUSR01C   | User add                                                 |
| 21    | COUSR02C   | User update                                              |
| 22    | COUSR03C   | User delete                                              |
| 23    | CBEXPORT   | Data export                                              |
| 24    | CBIMPORT   | Data import                                              |

### Phase 6: Data Loading & Infrastructure (Weeks 16-17)
*Convert JCL jobs and remaining batch programs.*

| Order | Module     | Rationale                                               |
|-------|------------|---------------------------------------------------------|
| 25    | CBACT01C-03C | Data loading utilities                                |
| 26    | CBCUS01C   | Customer data loader                                     |
| 27    | JCL Jobs   | Convert to Spring Batch jobs / scheduled tasks           |
| 28    | COBSWAIT   | Replace with Java Thread.sleep or scheduler              |
| 29    | CSUTLDTC   | Replace with java.time API                               |

### Optional Modules (Weeks 17-20, if in scope)

| Order | Module              | Rationale                                        |
|-------|---------------------|--------------------------------------------------|
| 30    | Authorization (IMS)  | Complex - IMS/DB2/MQ integration                |
| 31    | Tran Type (DB2)      | Already DB2 - simpler SQL migration             |
| 32    | VSAM-MQ              | MQ integration needs middleware mapping          |
