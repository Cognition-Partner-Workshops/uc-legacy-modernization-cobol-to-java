# Hotspot Report - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Methodology:** Static analysis of LOC, cyclomatic complexity proxies (IF/EVALUATE/PERFORM), file I/O density, copybook fan-in, and business criticality

---

## Scoring Methodology

Each module is scored across three dimensions on a 1-10 scale:

| Dimension           | Weight | Indicators                                                                 |
|---------------------|-------:|----------------------------------------------------------------------------|
| **Complexity**      |   40%  | Lines of code, IF/EVALUATE branches, PERFORM paragraphs, COPY inclusions  |
| **Risk**            |   40%  | File I/O operations, data mutation (WRITE/REWRITE/DELETE), cross-program coupling, PII handling |
| **Business Impact** |   20%  | Revenue criticality, user-facing operations, batch cycle dependency, data volume |

**Composite Score** = (Complexity x 0.40) + (Risk x 0.40) + (Business Impact x 0.20), normalized to 1-10.

---

## Top 10 Hotspot Modules

### Summary Table

| Rank | Module     | Lines | Score | Complexity | Risk | Biz Impact | Domain                  | Migration Priority |
|-----:|-----------|------:|------:|-----------:|-----:|-----------:|-------------------------|--------------------|
|    1 | COACTUPC  | 4,236 |  9.6  |       10   |   9  |        10  | Account Update (Online) | **P0 - Critical**  |
|    2 | CBTRN02C  |   731 |  9.0  |        8   |  10  |        10  | Transaction Posting     | **P0 - Critical**  |
|    3 | COCRDUPC  | 1,560 |  8.4  |        9   |   8  |         8  | Card Update (Online)    | **P0 - Critical**  |
|    4 | COCRDLIC  | 1,459 |  8.0  |        9   |   7  |         8  | Card List (Online)      | **P1 - High**      |
|    5 | CBACT04C  |   652 |  7.8  |        7   |   9  |         8  | Interest Calculation    | **P0 - Critical**  |
|    6 | CBSTM03A  |   924 |  7.6  |        6   |   8  |         9  | Statement Generation    | **P1 - High**      |
|    7 | COACTVWC  |   941 |  7.0  |        7   |   6  |         8  | Account View (Online)   | **P1 - High**      |
|    8 | COCRDSLC  |   887 |  6.8  |        7   |   6  |         7  | Card Detail View        | **P2 - Medium**    |
|    9 | CBTRN03C  |   649 |  6.6  |        7   |   6  |         7  | Transaction Report      | **P2 - Medium**    |
|   10 | COTRN02C  |   783 |  6.4  |        6   |   7  |         6  | Transaction Add         | **P1 - High**      |

---

## Detailed Analysis

### #1 - COACTUPC (Account Update) - Score: 9.6

**Location:** `app/cbl/COACTUPC.cbl` | **Lines:** 4,236 | **Type:** Online CICS

#### Why This Is the #1 Hotspot

COACTUPC is by far the largest program in the codebase (4,236 lines - nearly 3x the next largest). It is the single most complex COBOL program, handling the full account update workflow including all field-level validation, screen interaction, and VSAM updates.

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       | 4,236 | Extremely high    |
| IF statements       |   168 | Extremely high    |
| EVALUATE statements |    10 | High              |
| PERFORM paragraphs  |    64 | Very high         |
| COPY inclusions     |    56 | Extremely high (most copybook-dependent program) |
| REWRITE operations  |     2 | Mutates VSAM data |

#### Risk Factors
- **Data mutation:** Updates Account (ACCTFILE) and Card (CARDFILE) VSAM records
- **4 VSAM files accessed:** ACCTFILE, CARDFILE, XREFFILE, CUSTFILE
- **Field-level validation:** Validates dates, amounts, status codes, ZIP codes with embedded business rules
- **PII handling:** Accesses customer SSN, addresses, and account financial data
- **Copybook coupling:** 56 COPY statements - any change to shared copybooks risks breaking this program
- **UI complexity:** Complex BMS map interaction with conditional field coloring and error highlighting

#### Business Impact
- Core account management function; every customer interaction may touch this
- Directly modifies account balances and credit limits
- Regulatory implications: incorrect updates could affect credit reporting

#### Modernization Recommendations
1. **Decompose:** Split into separate services: AccountValidationService, AccountUpdateService, AccountViewController
2. **Extract validation:** Move 168 IF-statement validation rules into a dedicated rules engine or Jakarta Bean Validation annotations
3. **Separate concerns:** UI rendering (BMS map logic) should become a separate REST controller layer
4. **Test first:** Create comprehensive integration tests covering all validation paths before refactoring

---

### #2 - CBTRN02C (Transaction Posting) - Score: 9.0

**Location:** `app/cbl/CBTRN02C.cbl` | **Lines:** 731 | **Type:** Batch

#### Why This Is Critical

CBTRN02C is the **core batch engine** of CardDemo. It processes every daily transaction, updates account balances, maintains category balances, and handles transaction rejections. It touches more VSAM files (6 inputs, 4 outputs) than any other program.

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   731 | High              |
| IF statements       |    93 | Very high         |
| PERFORM paragraphs  |    61 | Very high         |
| READ operations     |     6 | Heavy I/O         |
| WRITE operations    |     5 | Heavy I/O         |
| REWRITE operations  |     2 | Data mutation     |
| COPY inclusions     |     5 | Moderate          |

#### Risk Factors
- **Highest file I/O density:** 6 reads + 5 writes + 2 rewrites = 13 file operations
- **Multi-file transaction:** Must maintain consistency across TRANSACT, ACCTFILE, TCATBALF, and DALYREJS simultaneously
- **No rollback mechanism:** VSAM has no built-in transaction rollback; partial failures can corrupt data
- **Batch window bottleneck:** This is the longest-running step in the daily batch cycle
- **Error handling:** Uses CEE3ABD (abend) for fatal errors - no graceful recovery

#### Business Impact
- Processes ALL daily credit card transactions
- Directly affects account balances (financial accuracy)
- Rejection file feeds exception reporting
- Batch cycle cannot proceed without successful completion

#### Modernization Recommendations
1. **Database transactions:** Replace multi-file VSAM updates with ACID database transactions
2. **Idempotency:** Design for replay-safe processing (transaction IDs prevent double-posting)
3. **Spring Batch:** Map to a Spring Batch job with chunk-oriented processing and automatic retry
4. **Monitoring:** Add real-time metrics for transaction counts, rejection rates, processing time

---

### #3 - COCRDUPC (Card Update) - Score: 8.4

**Location:** `app/cbl/COCRDUPC.cbl` | **Lines:** 1,560 | **Type:** Online CICS

#### Why This Is Critical

Second-largest online program. Handles credit card detail updates including card status changes, embossed name changes, and expiration date management.

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       | 1,560 | Very high         |
| IF statements       |   148 | Extremely high    |
| EVALUATE statements |    16 | Very high         |
| PERFORM paragraphs  |    26 | High              |
| COPY inclusions     |    15 | High              |
| WRITE + REWRITE     |     2 | Data mutation     |

#### Risk Factors
- **PCI-DSS sensitive:** Modifies credit card records (PAN, CVV, expiration)
- **4 VSAM files accessed:** CARDFILE, ACCTFILE, XREFFILE, CUSTFILE
- **Complex validation:** 148 IF branches for card number format, dates, status transitions
- **State machine:** Card status transitions must follow business rules (active->suspended->closed)

#### Business Impact
- Card lifecycle management is a core banking function
- Incorrect card status changes could block or enable fraudulent transactions
- Feeds downstream to authorization and transaction processing

#### Modernization Recommendations
1. **State machine pattern:** Model card status as an explicit state machine with validated transitions
2. **PCI compliance:** Ensure card data is encrypted at rest and tokenized in the modernized system
3. **Audit trail:** Add immutable audit logging for all card modifications
4. **Split from UI:** Extract card update business logic from screen handling

---

### #4 - COCRDLIC (Card List) - Score: 8.0

**Location:** `app/cbl/COCRDLIC.cbl` | **Lines:** 1,459 | **Type:** Online CICS

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       | 1,459 | Very high         |
| IF statements       |   122 | Very high         |
| EVALUATE statements |    18 | Very high         |
| PERFORM paragraphs  |    34 | High              |
| COPY inclusions     |    13 | High              |

#### Risk Factors
- **Complex pagination:** Implements forward/backward scrolling on VSAM browse with PF7/PF8 keys
- **Navigation hub:** XCTL routes to both COCRDSLC (view) and COCRDUPC (update) from selection
- **Performance:** VSAM sequential browse with filtering can be slow on large datasets

#### Business Impact
- Primary card lookup interface; every card operation starts here
- User experience bottleneck if pagination is slow

#### Modernization Recommendations
1. **Replace pagination:** Server-side pagination with SQL OFFSET/LIMIT or cursor-based pagination
2. **Search capability:** Add full-text search and filtering (not available in BMS/VSAM)
3. **Lazy loading:** Implement virtual scrolling in the modernized UI

---

### #5 - CBACT04C (Interest Calculation) - Score: 7.8

**Location:** `app/cbl/CBACT04C.cbl` | **Lines:** 652 | **Type:** Batch

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   652 | High              |
| IF statements       |    86 | Very high         |
| PERFORM paragraphs  |    56 | Very high         |
| READ operations     |     7 | Heavy I/O         |
| WRITE + REWRITE     |     3 | Data mutation     |
| COPY inclusions     |     5 | Moderate          |

#### Risk Factors
- **Financial calculation:** Interest rate computation directly affects customer bills
- **5 VSAM files:** ACCTFILE, XREFFILE, TCATBALF, DISCGRP, TRANSACT
- **Regulatory compliance:** Interest calculations must comply with Truth in Lending Act (TILA)
- **Rounding errors:** COBOL S9(04)V99 decimal arithmetic differs from Java floating-point
- **Disclosure group lookup:** Complex multi-key lookup for applicable interest rate

#### Business Impact
- Revenue-generating process: interest charges are a primary income source
- Errors directly impact customer billing and regulatory compliance
- Must match penny-for-penny with legacy system during parallel-run testing

#### Modernization Recommendations
1. **BigDecimal everywhere:** Use Java BigDecimal with HALF_EVEN rounding to match COBOL arithmetic
2. **Parallel-run testing:** Run legacy and modernized calculations side-by-side for months
3. **Externalize rates:** Move interest rate configuration to a database or config service
4. **Audit trail:** Log every calculation for regulatory audit

---

### #6 - CBSTM03A (Statement Generation) - Score: 7.6

**Location:** `app/cbl/CBSTM03A.CBL` | **Lines:** 924 | **Type:** Batch

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   924 | High              |
| WRITE operations    |    97 | **Extremely high** (most I/O-intensive program) |
| CALL to CBSTM03B   |    12 | Tight coupling to subroutine |
| READ operations     |     4 | Multiple input files |

#### Risk Factors
- **97 WRITE operations:** The most I/O-intensive program in the entire codebase
- **Tightly coupled:** Calls CBSTM03B 12 times for file I/O operations
- **Multi-file input:** Reads from XREFFILE, ACCTFILE, CUSTFILE, and TRANSACT simultaneously
- **Print formatting:** Complex report layout with headers, details, subtotals, and grand totals
- **GDG output:** Writes to Generation Data Group (versioned output files)

#### Business Impact
- Customer-facing: statements are the primary communication to cardholders
- Regulatory requirement: must produce accurate monthly statements
- Volume: generates one statement per card in the cross-reference file

#### Modernization Recommendations
1. **Template engine:** Replace COBOL print formatting with a template engine (Thymeleaf, JasperReports)
2. **PDF generation:** Generate PDF statements instead of mainframe print output
3. **Decouple I/O:** Replace CBSTM03B subroutine calls with a repository/DAO pattern
4. **Async processing:** Process statements in parallel per account using Spring Batch partitioning

---

### #7 - COACTVWC (Account View) - Score: 7.0

**Location:** `app/cbl/COACTVWC.cbl` | **Lines:** 941 | **Type:** Online CICS

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   941 | High              |
| IF statements       |    57 | High              |
| EVALUATE statements |    10 | High              |
| READ operations     |     3 | Multiple files    |
| COPY inclusions     |    15 | High              |

#### Risk Factors
- **4 VSAM files read:** ACCTFILE, CARDFILE, XREFFILE, CUSTFILE
- **PII exposure:** Displays customer personal and financial information on screen
- **Cross-entity joins:** Must correlate data from 4 separate files to compose the view
- **CSSTRPFY inclusion:** Inline paragraph for PF-key handling adds hidden complexity

#### Business Impact
- Most frequently accessed screen after login; every inquiry starts here
- Performance-sensitive: users expect instant response for account lookups

#### Modernization Recommendations
1. **Read-only API:** Implement as a simple GET endpoint with a DTO aggregating account+card+customer data
2. **Caching:** Add a caching layer for frequently accessed accounts
3. **View model:** Create an AccountSummaryDTO that joins data from multiple tables

---

### #8 - COCRDSLC (Card Detail View) - Score: 6.8

**Location:** `app/cbl/COCRDSLC.cbl` | **Lines:** 887 | **Type:** Online CICS

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   887 | High              |
| IF statements       |    68 | High              |
| EVALUATE statements |     8 | Moderate          |
| READ operations     |     2 | Moderate          |
| COPY inclusions     |    15 | High              |

#### Risk Factors
- **PCI-DSS sensitive:** Displays full card number, CVV, and expiration date
- **4 VSAM files read:** CARDFILE, ACCTFILE, XREFFILE, CUSTFILE
- **Masking absent:** No PAN masking in the COBOL source

#### Modernization Recommendations
1. **PAN masking:** Display only last 4 digits (****-****-****-1234)
2. **Role-based access:** Restrict CVV display to authorized roles only
3. **Audit logging:** Log every card detail view for PCI compliance

---

### #9 - CBTRN03C (Transaction Report) - Score: 6.6

**Location:** `app/cbl/CBTRN03C.cbl` | **Lines:** 649 | **Type:** Batch

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   649 | High              |
| IF statements       |    75 | High              |
| EVALUATE statements |     4 | Moderate          |
| PERFORM paragraphs  |    72 | Very high (highest PERFORM density) |
| READ operations     |     8 | Heavy I/O         |

#### Risk Factors
- **Highest PERFORM density:** 72 PERFORM statements in 649 lines = complex control flow
- **Multi-file read:** TRANSACT, TRANTYPE, TRANCATG with cross-referencing
- **Report formatting:** Complex multi-level break logic (page/account/grand totals)
- **CVTRA07Y dependency:** Uses the large report layout copybook

#### Modernization Recommendations
1. **Reporting framework:** Replace with JasperReports or a BI tool
2. **SQL aggregation:** Use GROUP BY and window functions instead of procedural break logic
3. **Export formats:** Add CSV/Excel export alongside PDF

---

### #10 - COTRN02C (Transaction Add) - Score: 6.4

**Location:** `app/cbl/COTRN02C.cbl` | **Lines:** 783 | **Type:** Online CICS

#### Complexity Metrics
| Metric              | Value | Assessment        |
|---------------------|------:|-------------------|
| Lines of Code       |   783 | High              |
| EVALUATE statements |    13 | High              |
| PERFORM paragraphs  |    61 | Very high         |
| COPY inclusions     |    10 | Moderate          |
| CALL to CSUTLDTC    |     2 | Date validation dependency |

#### Risk Factors
- **Transaction creation:** Directly writes to TRANSACT VSAM file
- **Cross-reference lookup:** Reads XREFFILE and ACCTFILE to validate card/account
- **Date validation:** Calls CSUTLDTC utility for date validation
- **No duplicate detection:** No built-in idempotency for transaction creation

#### Modernization Recommendations
1. **Idempotency key:** Add a client-generated idempotency key to prevent duplicate transactions
2. **Validation service:** Extract date and amount validation into reusable validators
3. **Event sourcing:** Consider event-driven architecture for transaction creation

---

## Complexity Metrics - All Programs (Reference)

| Program    | LOC   | IF  | EVAL | PERF | READ | WRITE | REWRITE | DEL | COPY | CALL | XCTL | Weighted Score |
|-----------|------:|----:|-----:|-----:|-----:|------:|--------:|----:|-----:|-----:|-----:|---------------:|
| COACTUPC  | 4,236 | 168 |   10 |   64 |    0 |     2 |       2 |   0 |   56 |    0 |    2 |           9.6  |
| CBTRN02C  |   731 |  93 |    0 |   61 |    6 |     5 |       2 |   0 |    5 |    1 |    0 |           9.0  |
| COCRDUPC  | 1,560 | 148 |   16 |   26 |    2 |     1 |       1 |   0 |   15 |    0 |    2 |           8.4  |
| COCRDLIC  | 1,459 | 122 |   18 |   34 |    3 |     0 |       0 |   0 |   13 |    3 |    3 |           8.0  |
| CBACT04C  |   652 |  86 |    0 |   56 |    7 |     2 |       1 |   0 |    5 |    1 |    0 |           7.8  |
| CBSTM03A  |   924 |  15 |    5 |   29 |    4 |    97 |       1 |   0 |    4 |   14 |    0 |           7.6  |
| COACTVWC  |   941 |  57 |   10 |   21 |    3 |     0 |       0 |   0 |   15 |    0 |    2 |           7.0  |
| COCRDSLC  |   887 |  68 |    8 |   19 |    2 |     0 |       0 |   0 |   15 |    0 |    2 |           6.8  |
| CBTRN03C  |   649 |  75 |    4 |   72 |    8 |     1 |       0 |   0 |    5 |    1 |    0 |           6.6  |
| COTRN02C  |   783 |  14 |   13 |   61 |    0 |     0 |       0 |   0 |   10 |    2 |    1 |           6.4  |
| COTRN00C  |   699 |  26 |    8 |   43 |    0 |     0 |       0 |   0 |    8 |    0 |    2 |           5.8  |
| COUSR00C  |   695 |  25 |    8 |   41 |    0 |     0 |       0 |   0 |    8 |    0 |    3 |           5.6  |
| CORPT00C  |   649 |  20 |    5 |   34 |    0 |     0 |       0 |   0 |    8 |    2 |    1 |           5.4  |
| CBEXPORT  |   582 |  16 |    0 |   45 |    5 |     5 |       0 |   0 |    6 |    1 |    0 |           5.2  |
| COBIL00C  |   572 |  10 |    9 |   38 |    0 |     0 |       0 |   0 |   10 |    0 |    1 |           5.0  |
| CBIMPORT  |   487 |  14 |    1 |   29 |    2 |     6 |       0 |   0 |    6 |    1 |    0 |           4.8  |
| CBTRN01C  |   494 |  33 |    0 |   42 |    5 |     0 |       0 |   0 |    6 |    1 |    0 |           4.6  |
| CBACT01C  |   430 |  22 |    0 |   35 |    2 |     9 |       0 |   0 |    2 |    3 |    0 |           4.4  |
| COUSR02C  |   414 |  13 |    5 |   31 |    0 |     0 |       0 |   0 |    8 |    0 |    1 |           4.0  |
| COUSR03C  |   359 |   8 |    5 |   26 |    0 |     0 |       0 |   0 |    8 |    0 |    1 |           3.6  |
| COTRN01C  |   330 |   7 |    3 |   17 |    0 |     0 |       0 |   0 |    8 |    0 |    1 |           3.4  |
| COMEN01C  |   308 |   7 |    3 |   13 |    0 |     0 |       0 |   0 |    9 |    0 |    3 |           3.2  |
| COUSR01C  |   299 |   4 |    3 |   20 |    0 |     0 |       0 |   0 |    9 |    0 |    1 |           3.0  |
| COADM01C  |   288 |  11 |    4 |   14 |    0 |     0 |       0 |   0 |    9 |    0 |    2 |           3.0  |
| COSGN00C  |   260 |   4 |    3 |   11 |    0 |     0 |       0 |   0 |    9 |    0 |    2 |           2.8  |
| CBSTM03B  |   230 |  12 |    1 |    4 |    5 |     2 |       1 |   0 |    0 |    0 |    0 |           2.6  |
| CBACT02C  |   178 |  22 |    0 |   10 |    1 |     0 |       0 |   0 |    1 |    1 |    0 |           2.2  |
| CBACT03C  |   178 |  22 |    0 |   10 |    1 |     0 |       0 |   0 |    1 |    1 |    0 |           2.2  |
| CBCUS01C  |   178 |  11 |    0 |   10 |    1 |     0 |       0 |   0 |    1 |    1 |    0 |           2.0  |
| CSUTLDTC  |   157 |   0 |    2 |    1 |    0 |     0 |       0 |   0 |    0 |    2 |    0 |           1.8  |
| COBSWAIT  |    41 |   0 |    0 |    0 |    0 |     0 |       0 |   0 |    0 |    1 |    0 |           1.0  |

---

## Migration Wave Recommendation

Based on the hotspot analysis, the recommended migration wave plan:

### Wave 0 - Foundation (Weeks 1-4)
- **Shared utilities:** CSUTLDTC (date validation), COCOM01Y (COMMAREA -> session model)
- **Data layer:** All VSAM-to-relational table migration, copybook-to-POJO conversion
- **Security:** CSUSR01Y -> User entity with hashed passwords, COSGN00C -> Spring Security

### Wave 1 - Critical Path (Weeks 5-12)
| Priority | Module   | Reason                                        |
|----------|---------|-----------------------------------------------|
| P0       | COACTUPC | Largest, most complex; blocks account features|
| P0       | CBTRN02C | Core batch engine; blocks all batch processing|
| P0       | CBACT04C | Financial calculation; requires penny-matching|
| P0       | COCRDUPC | Card update; PCI-DSS sensitive                |

### Wave 2 - High Value (Weeks 13-20)
| Priority | Module   | Reason                                        |
|----------|---------|-----------------------------------------------|
| P1       | COCRDLIC | Card list; high usage, complex pagination     |
| P1       | CBSTM03A | Statement generation; customer-facing          |
| P1       | COACTVWC | Account view; most used read operation         |
| P1       | COTRN02C | Transaction add; data entry path               |

### Wave 3 - Remaining (Weeks 21-28)
- All remaining online programs (views, lists, menus)
- Remaining batch programs (reports, read utilities, export/import)
- Extension modules (authorization, DB2 transaction types, MQ integration)

---

## Key Risk Areas for Modernization

| Risk Category          | Affected Modules                        | Mitigation                              |
|------------------------|-----------------------------------------|-----------------------------------------|
| **Financial accuracy** | CBACT04C, CBTRN02C, COBIL00C           | BigDecimal, parallel-run testing        |
| **Data consistency**   | CBTRN02C (multi-file updates)           | ACID transactions in RDBMS             |
| **PCI-DSS compliance** | COCRDUPC, COCRDSLC, COCRDLIC           | Tokenization, encryption, access control|
| **PII protection**     | COACTUPC, COACTVWC, CBSTM03A          | Field-level encryption, GDPR controls   |
| **Batch window**       | CBTRN02C, CBACT04C, CBSTM03A          | Parallel processing, async architecture |
| **Password security**  | COSGN00C + CSUSR01Y                     | bcrypt/Argon2 hashing                  |
