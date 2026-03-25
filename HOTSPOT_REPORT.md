# CardDemo Hotspot Report — Top 10 Modules by Modernization Priority

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This report identifies the highest-priority modules for modernization based on three
> weighted dimensions: **Code Complexity**, **Migration Risk**, and **Business Impact**.

---

## Scoring Methodology

| Dimension         | Weight | Criteria                                                                                           |
|-------------------|:------:|----------------------------------------------------------------------------------------------------|
| **Complexity**    |  30%   | Lines of code, cyclomatic indicators (EVALUATE/IF nesting), number of PERFORM/CALL targets, copybook count, COPY REPLACING usage |
| **Risk**          |  35%   | Number of VSAM files accessed, cross-program dependencies (CALL/XCTL targets), CICS API surface area, data validation logic density, error-handling paths |
| **Business Impact**|  35%  | Revenue-critical function, user-facing frequency, data integrity scope (how many entities touched), downstream dependencies (other programs/jobs that depend on this module's output) |

Each dimension is scored 1–10. **Composite = (Complexity × 0.30) + (Risk × 0.35) + (Impact × 0.35)**.

---

## Top 10 Hotspot Modules

| Rank | Program    | Lines | Complexity | Risk | Impact | **Composite** | Classification           |
|-----:|------------|------:|:----------:|:----:|:------:|:-------------:|--------------------------|
|   1  | COACTUPC   | 4,236 |    10      |  9   |   9    |   **9.30**    | Online — Account Update  |
|   2  | CBTRN02C   |   731 |     7      |  9   |  10    |   **8.75**    | Batch — Transaction Posting |
|   3  | CBACT04C   |   652 |     7      |  8   |   9    |   **8.05**    | Batch — Interest Calculation |
|   4  | COCRDUPC   | 1,560 |     8      |  8   |   7    |   **7.65**    | Online — Card Update     |
|   5  | COCRDLIC   | 1,459 |     8      |  7   |   7    |   **7.30**    | Online — Card List       |
|   6  | COBIL00C   |   572 |     6      |  7   |   8    |   **7.05**    | Online — Bill Payment    |
|   7  | CBSTM03A   |   924 |     7      |  7   |   7    |   **7.00**    | Batch — Statement Gen    |
|   8  | COTRN02C   |   783 |     7      |  7   |   7    |   **7.00**    | Online — Transaction Add |
|   9  | CBTRN01C   |   494 |     5      |  7   |   8    |   **6.75**    | Batch — Tran Validation  |
|  10  | COTRN00C   |   699 |     6      |  6   |   7    |   **6.35**    | Online — Transaction List|

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 9.30

**Why it's #1:** The single largest program in the codebase at 4,236 lines. Contains the most
complex business logic including multi-field validation, date editing, SSN validation, FICO
score checks, US state/ZIP validation, and phone number formatting.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 4,236 (2× larger than the next biggest online program) |
| **Copybooks Included** | 15 (CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, CSSETATY ×37, CSSTRPFY, CSUTLDWY, CSUTLDPY, COCOM01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, DFHBMSCA, DFHAID) |
| **VSAM Files** | 4 (ACCTDAT R/U, CARDXREF R, CUSTDAT R, + card lookup via AIX) |
| **CICS API Calls** | HANDLE ABEND, XCTL, SEND MAP, RECEIVE MAP, READ, REWRITE, SEND TEXT, ABEND |
| **Validation Functions** | 15+ distinct edit routines (EDIT-DATE, EDIT-SSN, EDIT-FICO, EDIT-US-STATE, EDIT-PHONE, EDIT-YESNO, etc.) |
| **COPY REPLACING** | 37 CSSETATY COPY REPLACING blocks for BMS field attribute management |
| **Key Risk** | Touches Account + Customer + Card data in a single transaction; any regression impacts core account integrity |
| **Modernization Notes** | Break into service layer (AccountService, ValidationService) + UI controller. The 37 COPY REPLACING blocks should become a reusable attribute-setting utility. |

### #2 — CBTRN02C (Transaction Posting) — Score: 8.75

**Why it's #2:** The core batch engine that posts all daily transactions. Writes to 4 files,
performs cross-reference lookups, updates account balances, and maintains category balances.
A failure here means no transactions get posted.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 731 |
| **VSAM Files** | 6 (DALYTRAN R, TRANSACT W, CARDXREF R, DALYREJS W, ACCTDAT RU, TCATBALF RW) |
| **Business Logic** | Transaction validation (1500-VALIDATE-TRAN), cross-ref lookup, account balance update, category balance create/update, reject recording |
| **Downstream Impact** | TRANSACT file is read by CBTRN03C (reports), CBSTM03A (statements), and all online transaction screens |
| **Timestamp Logic** | Z-GET-DB2-FORMAT-TIMESTAMP — generates DB2-compatible timestamps |
| **Key Risk** | Financial data integrity — incorrect posting means wrong balances, wrong statements |
| **Modernization Notes** | Map to Spring Batch with chunk-oriented processing. Split validation, posting, and rejection into separate steps. |

### #3 — CBACT04C (Interest Calculation) — Score: 8.05

**Why it's #3:** Financial calculation engine that computes interest and fees across all accounts.
Reads from 5 VSAM files and updates account records with computed values.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 652 |
| **VSAM Files** | 5 (TCATBALF R, CARDXREF R, DISCGRP R, ACCTDAT RU, TRANSACT R) |
| **Business Logic** | Interest rate lookup by disclosure group, interest computation, fee computation, account balance update, transaction writing for interest charges |
| **Downstream Impact** | Updates ACCTDAT balances used by all account-facing programs |
| **Key Risk** | Incorrect interest = regulatory / compliance issue; must preserve exact decimal arithmetic |
| **Modernization Notes** | Use Java BigDecimal exclusively. Create InterestCalculationService with configurable rate lookup. Consider event-sourcing for auditability. |

### #4 — COCRDUPC (Card Update) — Score: 7.65

**Why it's #4:** Second-largest online program with extensive field validation. Manages card
lifecycle (activation, expiration, embossed name changes).

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 1,560 |
| **Copybooks** | 12 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y + CSSETATY + CSSTRPFY + CSUTLDWY) |
| **VSAM Files** | 3 (CARDDAT RU, CUSTDAT R, CARDAIX R) |
| **Validation** | Card status, date editing, embossed name, multiple COPY REPLACING blocks |
| **Key Risk** | Card data changes affect transaction processing and cross-reference integrity |
| **Modernization Notes** | Separate CardService from UI validation. Share validation logic with COACTUPC. |

### #5 — COCRDLIC (Card List) — Score: 7.30

**Why it's #5:** Complex list/browse screen with forward/backward pagination, filtering,
and row-selection logic. Navigates to detail (COCRDSLC) and update (COCRDUPC) screens.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 1,459 |
| **CICS API Calls** | STARTBR, READNEXT, READPREV, ENDBR, XCTL (to detail + update programs) |
| **VSAM Files** | 2 (CARDDAT R via browse, CARDAIX alternate index) |
| **UI Complexity** | 7-row array display, filter by account/card, page-forward/backward, row selection → XCTL |
| **Key Risk** | Pagination logic with STARTBR/READNEXT/READPREV is error-prone; alternate index browsing adds complexity |
| **Modernization Notes** | Replace with paginated REST API + list UI component. Pagination becomes SQL OFFSET/LIMIT or cursor-based. |

### #6 — COBIL00C (Bill Payment) — Score: 7.05

**Why it's #6:** Revenue-critical — handles bill payments that directly modify account balances
and create transaction records. Involves STARTBR/READPREV for last-transaction lookup.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 572 |
| **VSAM Files** | 3 (ACCTDAT RU, TRANSACT RW, CARDXREF R via AIX) |
| **Business Logic** | Payment amount validation, balance update, transaction record creation with timestamp, last-transaction ID retrieval via READPREV |
| **CICS API Calls** | READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR, ASKTIME, FORMATTIME |
| **Key Risk** | Financial transaction — incorrect payment processing means wrong balances |
| **Modernization Notes** | PaymentService with transactional boundaries. Use database sequences instead of READPREV for ID generation. |

### #7 — CBSTM03A (Statement Generation) — Score: 7.00

**Why it's #7:** Generates customer statements in both text and HTML formats. Calls sub-program
CBSTM03B 11 times for file I/O operations. Reads from 4 VSAM files.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 924 (+ 230 in CBSTM03B = 1,154 combined) |
| **CALL Statements** | 11 calls to CBSTM03B for I/O operations |
| **VSAM Files** | 4 read (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE) + 2 write (STMTFILE, HTMLFILE) |
| **Output Formats** | Dual output: fixed-width text + HTML |
| **Key Risk** | Complex nested loops (card → transaction), HTML generation embedded in COBOL |
| **Modernization Notes** | Replace with template engine (Thymeleaf/FreeMarker). CBSTM03B becomes a DAO layer. |

### #8 — COTRN02C (Transaction Add) — Score: 7.00

**Why it's #8:** Online transaction entry screen — validates and writes new transactions,
updating account balances in real time.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 783 |
| **VSAM Files** | 3 (TRANSACT W, CARDXREF R, ACCTDAT R) |
| **Business Logic** | Transaction type/category validation, amount validation, account lookup, real-time balance check |
| **Key Risk** | Overlaps with batch posting logic (CBTRN02C) — must stay consistent |
| **Modernization Notes** | Share TransactionService with batch posting. Validate against same business rules. |

### #9 — CBTRN01C (Transaction Validation) — Score: 6.75

**Why it's #9:** First step of the batch posting pipeline. Opens 6 VSAM files simultaneously
and validates every daily transaction against customer, card, and account master data.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 494 |
| **VSAM Files** | 6 (DALYTRAN R, CUSTDAT R, CARDXREF R, CARDDAT R, ACCTDAT R, TRANSACT R) |
| **Business Logic** | Cross-reference lookup, customer existence check, account status validation |
| **Key Risk** | Gateway to posting — if validation is wrong, bad transactions get posted |
| **Modernization Notes** | Combine with CBTRN02C into a single Spring Batch job with validation + posting steps. |

### #10 — COTRN00C (Transaction List) — Score: 6.35

**Why it's #10:** High-frequency user screen for browsing transaction history with
card-based filtering and pagination.

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 699 |
| **VSAM Files** | 2 (TRANSACT R via browse, CARDXREF R) |
| **UI Complexity** | List pagination, card number filter, browse with STARTBR/READNEXT/READPREV |
| **Key Risk** | Performance-sensitive — users browse large transaction histories |
| **Modernization Notes** | REST API with indexed queries. Consider read-replica or materialized views for performance. |

---

## Modernization Priority Matrix

```
                          Business Impact
                    Low          Medium         High
                ┌────────────┬────────────┬────────────┐
         High   │            │ COCRDLIC   │ COACTUPC   │
                │            │ COCRDUPC   │            │
Complexity      ├────────────┼────────────┼────────────┤
         Medium │ CBSTM03A   │ COTRN02C   │ CBTRN02C   │
                │            │ COTRN00C   │ CBACT04C   │
                │            │            │ COBIL00C   │
                ├────────────┼────────────┼────────────┤
         Low    │            │            │ CBTRN01C   │
                └────────────┴────────────┴────────────┘
```

---

## Recommended Modernization Waves

### Wave 1 — Foundation (Weeks 1–4)
Establish shared services and data access layer.

| Module     | Action                                                         |
|------------|----------------------------------------------------------------|
| Copybooks  | Convert all VSAM record layouts to Java POJOs / JPA entities  |
| CSUTLDTC   | Replace with `java.time` API                                   |
| COCOM01Y   | Design session/context management (Spring Security context)    |
| CSUSR01Y   | Create UserEntity + Spring Security integration                |

### Wave 2 — Batch Core (Weeks 3–8)
Migrate the financial batch pipeline first — it's the riskiest and most impactful.

| Module     | Priority | Target Architecture                                    |
|------------|:--------:|--------------------------------------------------------|
| CBTRN01C   | **P1**   | Spring Batch — ValidationStep                          |
| CBTRN02C   | **P1**   | Spring Batch — PostingStep (combined with CBTRN01C)    |
| CBACT04C   | **P1**   | Spring Batch — InterestCalculationJob                  |
| CBSTM03A/B | **P2**   | Spring Batch — StatementGenerationJob + Thymeleaf      |
| CBTRN03C   | **P2**   | Spring Batch — ReportGenerationJob                     |

### Wave 3 — Online Core (Weeks 5–12)
Migrate the most complex online screens.

| Module     | Priority | Target Architecture                                    |
|------------|:--------:|--------------------------------------------------------|
| COACTUPC   | **P1**   | REST API (AccountController) + React/Angular form      |
| COCRDUPC   | **P1**   | REST API (CardController) + form                       |
| COBIL00C   | **P1**   | REST API (PaymentController) + form                    |
| COCRDLIC   | **P2**   | REST API (CardController.list) + paginated table       |
| COTRN02C   | **P2**   | REST API (TransactionController.add) + form            |
| COTRN00C   | **P2**   | REST API (TransactionController.list) + paginated table|

### Wave 4 — Remaining Screens (Weeks 10–14)
Simpler screens and admin functions.

| Module     | Target Architecture                                            |
|------------|----------------------------------------------------------------|
| COACTVWC   | AccountController.view (read-only variant of update)           |
| COCRDSLC   | CardController.view                                            |
| COTRN01C   | TransactionController.view                                     |
| CORPT00C   | ReportController + date-range picker UI                        |
| COSGN00C   | Spring Security login form                                     |
| COMEN01C   | React/Angular navigation shell                                 |
| COADM01C   | Admin navigation shell                                         |
| COUSR00-03C| UserAdminController (CRUD)                                     |

### Wave 5 — Data Migration & Utilities (Weeks 12–16)
| Module     | Target Architecture                                            |
|------------|----------------------------------------------------------------|
| CBEXPORT   | Spring Batch export job or CLI tool                            |
| CBIMPORT   | Spring Batch import job with validation                        |
| CBACT01-03C| Replaced by JPA repository queries                             |
| CBCUS01C   | Replaced by JPA repository queries                             |
| JCL jobs   | Replaced by Spring Batch job configurations + scheduler        |

---

## Key Risk Mitigations

1. **Decimal Precision:** All financial calculations (`CBTRN02C`, `CBACT04C`, `COBIL00C`) use COBOL packed decimal (`S9(n)V99`). Java must use `BigDecimal` exclusively — never `double`/`float`.

2. **Transaction Integrity:** `CBTRN02C` updates 4 files atomically under VSAM. In Java, use database transactions with proper isolation levels.

3. **Pagination Logic:** `COCRDLIC` and `COTRN00C` use CICS STARTBR/READNEXT/READPREV with positioned reads. Map to keyset pagination (WHERE id > :lastId) rather than OFFSET for performance.

4. **COPY REPLACING:** `COACTUPC` has 37 COPY REPLACING blocks — these are effectively macro expansions. Extract into a utility method with parameters.

5. **Dual-Format Output:** `CBSTM03A` generates both text and HTML. Use a template engine with two templates rather than string concatenation.

6. **Date Handling:** Multiple date formats across the codebase (YYYY-MM-DD strings, Julian dates via CEEDAYS, CICS FORMATTIME). Standardize on `java.time.LocalDate` / `LocalDateTime`.

7. **Concurrent Access:** Online programs (COACTUPC, COCRDUPC) use CICS READ UPDATE → REWRITE for optimistic locking. Implement JPA `@Version` or optimistic locking in the database layer.
