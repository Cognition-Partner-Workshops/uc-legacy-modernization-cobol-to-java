# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
> **Purpose:** Top 10 modules ranked by modernization complexity, risk, and business impact

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Ranking](#top-10-hotspot-ranking)
3. [Detailed Analysis per Module](#detailed-analysis-per-module)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Sequence](#recommended-migration-sequence)
6. [Cross-Cutting Concerns](#cross-cutting-concerns)

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale each):

| Dimension           | Weight | What It Measures                                                   |
|---------------------|--------|--------------------------------------------------------------------|
| **Complexity**      | 35%    | Lines of code, PERFORM/EVALUATE count, # of VSAM files accessed, # copybooks, branching depth, COPY REPLACING usage |
| **Risk**            | 35%    | Data sensitivity (PII/financial), write operations to critical files, error handling patterns, ABEND handling, shared data coupling |
| **Business Impact** | 30%    | Business criticality of the function, user-facing visibility, downstream dependencies, batch chain position |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Business Impact x 0.30)

---

## Top 10 Hotspot Ranking

| Rank | Module     | Lines | Type  | Function                        | Complexity | Risk | Biz Impact | **Composite** |
|-----:|------------|------:|-------|---------------------------------|:----------:|:----:|:----------:|:-------------:|
|    1 | COACTUPC   | 4,236 | CICS  | Account Update                  |    10      |  10  |     9      |   **9.70**    |
|    2 | CBTRN02C   |   731 | Batch | Transaction Posting             |     7      |   9  |    10      |   **8.60**    |
|    3 | CBACT04C   |   652 | Batch | Interest Calculation            |     7      |   9  |     9      |   **8.30**    |
|    4 | COCRDUPC   | 1,560 | CICS  | Card Update                     |     8      |   8  |     8      |   **8.00**    |
|    5 | CBSTM03A   |   924 | Batch | Statement Generation            |     8      |   6  |     8      |   **7.30**    |
|    6 | COCRDLIC   | 1,459 | CICS  | Card List (browsable)           |     8      |   6  |     7      |   **7.00**    |
|    7 | COBIL00C   |   572 | CICS  | Bill Payment                    |     6      |   8  |     7      |   **7.00**    |
|    8 | COTRN02C   |   783 | CICS  | Transaction Add                 |     7      |   7  |     7      |   **7.00**    |
|    9 | CBTRN03C   |   649 | Batch | Daily Transaction Report        |     7      |   5  |     7      |   **6.30**    |
|   10 | COACTVWC   |   941 | CICS  | Account View                    |     6      |   6  |     7      |   **6.30**    |

---

## Detailed Analysis per Module

### #1: COACTUPC -- Account Update (Composite: 9.70)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 4,236 (largest program in the codebase by 2.7x)                  |
| **PERFORM Count**         | 64                                                                |
| **EVALUATE Count**        | 10                                                                |
| **COPY Statements**       | 18 unique copybooks (most of any program)                         |
| **COPY REPLACING (CSSETATY)** | 40+ inline expansions for field attribute manipulation        |
| **VSAM Files Accessed**   | 4 files: ACCTDATA (R/W), CARDDATA (R/W), CARDXREF (R), CUSTDATA (R/W) |
| **BMS Map**               | COACTUP (30+ input/output fields -- most complex screen)          |
| **CALL Dependencies**     | CSUTLDTC (date validation)                                        |
| **Inline Procedures**     | CSSTRPFY (string formatting), CSUTLDPY (date utility)             |

**Why It's #1:**
- By far the largest and most complex program at 4,236 lines
- Reads/writes to 4 different VSAM files in a single transaction
- Handles account balance updates, credit limit changes, and customer data modifications
- Extensive field-level validation with 40+ CSSETATY attribute-setting macros
- Contains deep nested EVALUATE/PERFORM logic for multi-step update workflow
- Any bug here directly impacts financial data integrity

**Migration Risks:**
- Multi-file transactional consistency must be preserved (CICS handles this via unit-of-work; Java needs explicit transaction management)
- Complex screen interaction with multi-step confirmation flows
- COPY REPLACING pattern requires careful template expansion
- Decimal arithmetic for financial fields must match COBOL precision exactly

**Recommended Approach:**
- Decompose into multiple Java service classes: AccountService, CardService, CustomerService
- Extract validation logic into a separate ValidationService
- Use Spring `@Transactional` to replace CICS unit-of-work semantics
- Create comprehensive regression tests for all update paths before migration

---

### #2: CBTRN02C -- Transaction Posting (Composite: 8.60)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 731                                                               |
| **PERFORM Count**         | 61                                                                |
| **EVALUATE Count**        | _(minimal -- procedural flow)_                                    |
| **VSAM Files Accessed**   | 4 files: DALYTRAN (R), CARDXREF (R), TRANSACT (W), TCATBAL (R/W) |
| **Batch Position**        | Core of nightly cycle -- Phase 3, Step 1                          |

**Why It's #2:**
- Core batch processing engine -- posts daily transactions to master file
- Updates running category balances (TCATBAL) that drive interest calculation
- Failure here stops the entire nightly batch chain
- Reads daily transactions and writes to the master transaction file
- Category balance updates involve signed decimal arithmetic

**Migration Risks:**
- Must maintain exact same posting logic and rounding behavior
- Category balance accumulation is critical for downstream interest calculation
- File locking semantics differ between VSAM and RDBMS
- Batch restart/recovery logic must be preserved

**Recommended Approach:**
- Convert to Spring Batch job with chunk-oriented processing
- Implement idempotent posting with duplicate detection
- Ensure decimal precision matches COBOL S9(09)V99 exactly (use Java BigDecimal)
- Add checkpoint/restart capability

---

### #3: CBACT04C -- Interest Calculation (Composite: 8.30)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 652                                                               |
| **PERFORM Count**         | 56                                                                |
| **VSAM Files Accessed**   | 5 files: TCATBAL (R), CARDXREF (R), DISCGRP (R), ACCTDATA (R/W), TRANSACT (W) |
| **Batch Position**        | Core of nightly cycle -- Phase 3, Step 2 (depends on POSTTRAN)    |

**Why It's #3:**
- Calculates interest charges based on category balances and disclosure group rates
- Updates account balances directly (financial write)
- Creates interest transaction records in the master file
- Complex lookup chain: Category Balance -> Cross-Reference -> Disclosure Group -> Account
- Any arithmetic error directly impacts customer billing

**Migration Risks:**
- Interest rate calculation precision is legally regulated
- Multi-step lookup chain across 5 files must produce identical results
- COBOL decimal arithmetic (truncation vs. rounding) may differ from Java defaults
- Disclosure group rate lookup logic is business-critical

**Recommended Approach:**
- Implement using BigDecimal with explicit RoundingMode.HALF_EVEN (banker's rounding)
- Create a dedicated InterestCalculationService with unit tests for every rate scenario
- Parallel-run old and new systems to validate calculation parity
- Document all rounding rules explicitly

---

### #4: COCRDUPC -- Card Update (Composite: 8.00)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 1,560                                                             |
| **PERFORM Count**         | 26                                                                |
| **EVALUATE Count**        | 16 (highest -- complex branching)                                 |
| **VSAM Files Accessed**   | 3 files: CARDDATA (R/W), CARDXREF (R), with ABEND handling       |
| **BMS Map**               | COCRDUP (20+ fields including card number, expiry, status)        |
| **CALL Dependencies**     | CSUTLDTC (date validation)                                        |

**Why It's #4:**
- Second-largest online program
- Handles sensitive card data updates (card number, CVV, expiry, status)
- Contains PCI-relevant data modifications
- 16 EVALUATE statements indicate complex conditional logic paths
- ABEND handling for error recovery

**Migration Risks:**
- PCI DSS compliance for card data handling
- Card number validation and masking logic
- Expiration date format conversions
- ABEND/error recovery must be mapped to Java exception handling

---

### #5: CBSTM03A -- Statement Generation (Composite: 7.30)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 924                                                               |
| **PERFORM Count**         | 29                                                                |
| **EVALUATE Count**        | 5                                                                 |
| **VSAM Files Accessed**   | 4 files (via CBSTM03B): TRXFL (R), CARDXREF (R), ACCTDATA (R), CUSTDATA (R) |
| **Output Files**          | STATEMNT.PS (text), STATEMNT.HTML (HTML)                          |
| **CALL Dependencies**     | CBSTM03B (file I/O subroutine)                                   |

**Why It's #5:**
- Produces customer-facing account statements in two formats (text + HTML)
- Uses mainframe control block addressing (advanced COBOL technique)
- CALL to CBSTM03B subroutine for file I/O (inter-program dependency)
- Customer-visible output -- any formatting error is directly visible
- Reads across 4 files to assemble statement data

**Migration Risks:**
- HTML generation logic is embedded in COBOL WRITE statements with hardcoded markup
- Mainframe control block addressing is a non-standard COBOL extension
- Statement formatting must match exactly for regulatory compliance
- CALL/LINKAGE SECTION interface with CBSTM03B needs careful mapping

**Recommended Approach:**
- Replace with a modern template engine (Thymeleaf, FreeMarker)
- Separate data assembly from presentation rendering
- Convert CBSTM03B file I/O to JPA/JDBC repository pattern
- Generate PDF directly instead of text-to-PDF conversion

---

### #6: COCRDLIC -- Card List (Composite: 7.00)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 1,459                                                             |
| **PERFORM Count**         | 34                                                                |
| **EVALUATE Count**        | 18 (highest EVALUATE density of any program)                      |
| **VSAM Browse Ops**       | STARTBR, READNEXT, READPREV, ENDBR (full browse pattern)         |
| **XCTL Targets**          | COCRDSLC, COCRDUPC (view/update navigation)                       |

**Why It's #6:**
- Complex browsable list with forward/backward pagination
- 18 EVALUATE statements for handling various navigation states
- Full VSAM browse pattern (STARTBR/READNEXT/READPREV/ENDBR) -- maps to cursor-based pagination
- Navigation hub that XCTLs to card view and card update programs

**Migration Risks:**
- VSAM browse semantics differ from SQL pagination (cursor position state)
- Complex key positioning logic for forward/backward navigation
- Must handle concurrent access during browse operations

---

### #7: COBIL00C -- Bill Payment (Composite: 7.00)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 572                                                               |
| **PERFORM Count**         | 38                                                                |
| **EVALUATE Count**        | 9                                                                 |
| **VSAM Files Accessed**   | 3 files: ACCTDATA (R/W), CARDXREF (R), TRANSACT (R/W)            |

**Why It's #7:**
- Directly modifies account balances (financial write)
- Creates transaction records for bill payments
- Cross-references card to account for payment processing
- Business-critical: errors affect customer account balances
- Relatively moderate code size but high data sensitivity

**Migration Risks:**
- Financial transaction atomicity (payment must be all-or-nothing)
- Account balance update must be consistent with transaction record creation
- Concurrent access handling (online payment while batch is running)

---

### #8: COTRN02C -- Transaction Add (Composite: 7.00)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 783                                                               |
| **PERFORM Count**         | 61                                                                |
| **EVALUATE Count**        | 13                                                                |
| **VSAM Files Accessed**   | 5 files: CARDDATA (R), CARDXREF (R), TRANSACT (R/W), TRANTYPE (R), TRANCATG (R) |
| **CALL Dependencies**     | CSUTLDTC (date validation)                                        |

**Why It's #8:**
- Creates new transaction records through the online interface
- Validates against card data, cross-reference, transaction types, and categories
- 5 file accesses for a single add operation (high data coupling)
- Complex validation logic with 13 EVALUATE statements
- High PERFORM count (61) relative to line count indicates dense procedural logic

**Migration Risks:**
- Multi-file validation chain must be preserved
- Transaction ID generation logic
- Lookup table validation (type codes, category codes) must match exactly

---

### #9: CBTRN03C -- Daily Transaction Report (Composite: 6.30)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 649                                                               |
| **PERFORM Count**         | 72 (highest PERFORM count relative to LOC)                        |
| **VSAM Files Accessed**   | 5 files: TRANSACT (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATE-PARMS (R) |
| **Output Files**          | REPTFILE (report output)                                          |

**Why It's #9:**
- Generates the daily transaction report used for reconciliation
- 72 PERFORMs in 649 lines = extremely dense procedural logic
- Reads from 5 different files to assemble report data
- Uses CVTRA07Y report layout structures for formatted output
- Report accuracy is critical for financial reconciliation

**Migration Risks:**
- Report formatting must match exactly (column positions, totals, page breaks)
- Date parameter file input must be mapped to configuration
- Multi-file lookup chain for type and category descriptions

---

### #10: COACTVWC -- Account View (Composite: 6.30)

| Metric                    | Value / Detail                                                    |
|---------------------------|-------------------------------------------------------------------|
| **Lines of Code**         | 941                                                               |
| **PERFORM Count**         | 21                                                                |
| **EVALUATE Count**        | 10                                                                |
| **VSAM Files Accessed**   | 4 files: ACCTDATA (R), CARDDATA (R), CARDXREF (R), CUSTDATA (R)  |
| **ABEND Handling**        | HANDLE ABEND with custom error display                            |

**Why It's #10:**
- Display-only but reads from 4 VSAM files to compose a single view
- Contains ABEND handling with custom error display screens
- Gateway to account update (COACTUPC) -- data assembled here feeds the update flow
- HANDLE ABEND / ABEND pattern requires careful mapping to Java exception handling

**Migration Risks:**
- Multi-file data assembly for a single view (joins in SQL)
- ABEND handling pattern is non-standard compared to typical Java error handling
- Screen data composition logic is tightly coupled to BMS map structure

---

## Risk Heat Map

```
                    LOW COMPLEXITY ◄─────────────────────────► HIGH COMPLEXITY
                    │                                                        │
HIGH RISK           │  COBIL00C        CBTRN02C                             │
                    │  (Bill Pay)      (Txn Post)     CBACT04C              │
                    │                                 (Interest)             │
                    │                                                        │
                    │                       COTRN02C                         │
                    │                       (Txn Add)    COCRDUPC            │
                    │                                    (Card Upd)          │
                    │                                                        │
                    │                                          COACTUPC      │
                    │                                          (Acct Upd)    │
MEDIUM RISK         │                                                        │
                    │  CORPT00C        CBTRN03C                              │
                    │  (Reports)       (Txn Rept)     CBSTM03A              │
                    │                                 (Statements)           │
                    │                  COACTVWC        COCRDLIC              │
                    │                  (Acct View)    (Card List)            │
                    │                                                        │
LOW RISK            │  COSGN00C  COMEN01C  COUSR00C-03C                     │
                    │  (Signon)  (Menu)    (User CRUD)                      │
                    │                                                        │
                    └────────────────────────────────────────────────────────┘
```

---

## Recommended Migration Sequence

Based on the hotspot analysis, here is the recommended order for tackling modernization:

### Wave 1: Foundation (Low risk, build patterns)
| Order | Module    | Rationale                                                  |
|------:|-----------|------------------------------------------------------------|
|     1 | COSGN00C  | Simple auth flow; establishes security pattern             |
|     2 | COMEN01C  | Menu navigation; establishes routing/controller pattern    |
|     3 | COADM01C  | Admin menu; reuses COMEN01C pattern                        |
|     4 | COUSR00C-03C | User CRUD; simple single-file operations               |

### Wave 2: Read-Only Views (Medium complexity, no writes)
| Order | Module    | Rationale                                                  |
|------:|-----------|------------------------------------------------------------|
|     5 | COACTVWC  | Account view; establishes multi-file read pattern          |
|     6 | COCRDSLC  | Card view; reuses account view pattern                     |
|     7 | COTRN01C  | Transaction view; simple read                              |
|     8 | COCRDLIC  | Card list; establishes browse/pagination pattern           |
|     9 | COTRN00C  | Transaction list; reuses card list pattern                 |
|    10 | CORPT00C  | Report request; establishes report trigger pattern         |

### Wave 3: Write Operations (High complexity, financial risk)
| Order | Module    | Rationale                                                  |
|------:|-----------|------------------------------------------------------------|
|    11 | COTRN02C  | Transaction add; first write operation                     |
|    12 | COCRDUPC  | Card update; second write operation                        |
|    13 | COBIL00C  | Bill payment; financial write                              |
|    14 | COACTUPC  | Account update; most complex -- tackled last in online     |

### Wave 4: Batch Processing (Highest risk, full regression needed)
| Order | Module    | Rationale                                                  |
|------:|-----------|------------------------------------------------------------|
|    15 | CBACT01C-03C | Data read utilities; simple batch pattern              |
|    16 | CBEXPORT/CBIMPORT | Data export/import; establishes batch I/O pattern|
|    17 | CBTRN01C  | Daily transaction read; simple batch read                  |
|    18 | CBTRN02C  | Transaction posting; core batch engine                     |
|    19 | CBACT04C  | Interest calculation; highest financial risk               |
|    20 | CBTRN03C  | Transaction report; establishes report generation pattern  |
|    21 | CBSTM03A/B | Statement generation; most complex batch program         |

### Wave 5: Optional Modules (If in scope)
| Order | Module            | Rationale                                            |
|------:|-------------------|------------------------------------------------------|
|    22 | VSAM-MQ module    | Simplest optional module (2 programs)                |
|    23 | Tran Type DB2     | Already uses SQL (3 programs)                        |
|    24 | Auth IMS/DB2/MQ   | Most complex optional module (8 programs)            |

---

## Cross-Cutting Concerns

These issues apply across multiple hotspot modules and should be addressed as shared infrastructure:

| Concern                        | Affected Modules                          | Recommendation                                    |
|--------------------------------|-------------------------------------------|---------------------------------------------------|
| **COMMAREA session state**     | All 17 online programs                    | Replace with HTTP session / JWT / Spring Security  |
| **VSAM file I/O**             | All programs                              | Replace with JPA repositories                     |
| **CICS XCTL navigation**      | All online programs                       | Replace with Spring MVC controllers / REST routes  |
| **BMS screen maps**           | All 17 online programs                    | Replace with React/Angular/Thymeleaf templates     |
| **COBOL decimal arithmetic**  | COACTUPC, CBTRN02C, CBACT04C, COBIL00C   | Use Java BigDecimal with explicit rounding modes   |
| **Date handling (CSUTLDTC)**  | COACTUPC, COCRDUPC, COTRN02C, CBACT04C   | Use java.time API (LocalDate, Instant)             |
| **Field attribute macros**    | COACTUPC, COCRDUPC, COBIL00C, COTRN02C   | Replace with CSS classes / form validation          |
| **Error/ABEND handling**      | COACTVWC, COCRDSLC, COACTUPC             | Map to Java exception hierarchy                    |
| **COPY REPLACING patterns**   | COACTUPC (40+), COCRDUPC, COBIL00C       | Extract to Java utility methods / annotations      |
| **Plaintext passwords**       | CSUSR01Y (COSGN00C, COUSR01C-03C)        | Implement bcrypt/scrypt hashing + OAuth2/OIDC      |
| **PII data (SSN, DOB)**      | CVCUS01Y (COACTVWC, COACTUPC, CBSTM03A)  | Implement field-level encryption + access controls  |
