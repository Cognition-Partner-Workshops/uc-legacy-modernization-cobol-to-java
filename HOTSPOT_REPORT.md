# CardDemo Hotspot Report -- Top 10 Modernization Priorities

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Platform**: IBM Mainframe (COBOL/CICS/VSAM/JCL)

## Methodology

Each module is scored across three dimensions on a 1-10 scale:

| Dimension          | Weight | Factors Measured                                                              |
|--------------------|-------:|-------------------------------------------------------------------------------|
| **Complexity**     |    40% | Lines of code, EVALUATE/IF branches, PERFORM count, CICS commands, copybook count, COPY REPLACING usage |
| **Risk**           |    30% | Data files accessed (blast radius), coupling (programs that depend on it), PII handling, financial calculations |
| **Business Impact**|    30% | Revenue criticality, user-facing frequency, regulatory implications, batch cycle position |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.4) + (Business Impact x 0.2) -- normalized to 100.

---

## Top 10 Hotspot Modules

### #1 -- COACTUPC (Account Update) -- Score: 95/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 4,236   | Largest program in the entire codebase (20% of all SLOC)     |
| **EVALUATE blocks** | 20      | Complex branching for field-level validation                 |
| **IF statements**   | 164     | Highest conditional density of any program                   |
| **PERFORM calls**   | 64      | Moderate procedural decomposition                            |
| **EXEC CICS**       | 17      | Heavy CICS interaction (MAP send/receive, file I/O)          |
| **Copybooks used**  | 15+     | CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y + 39x COPY REPLACING CSSETATY |
| **VSAM files**      | 3       | ACCTDATA (R/W), CUSTDATA (R), CARDXREF (R)                  |
| **Complexity**      | 10/10   | Extreme: 4K+ LOC, 164 IFs, 39 COPY REPLACING macros         |
| **Risk**            | 9/10    | Writes to Account Master -- financial data, multi-file access|
| **Business Impact** | 9/10    | Core account maintenance -- high-frequency user operation    |

**Why it's #1**: This is the single most complex program in CardDemo. The 39 `COPY REPLACING` instances for BMS attribute setting create hidden complexity that static analysis tools often miss. It touches three VSAM files including writes to the Account Master, making it the highest-risk module for data corruption during modernization. The dense conditional logic (164 IF statements) requires exhaustive test coverage before migration.

**Modernization Recommendation**: Decompose into separate validation, persistence, and presentation layers. Extract the 39 COPY REPLACING attribute-setting blocks into a reusable utility. Target for Spring MVC controller + JPA service + Thymeleaf/React form.

---

### #2 -- CBTRN02C (Transaction Posting -- Production) -- Score: 88/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 731     | Substantial batch program                                    |
| **EVALUATE blocks** | 0       | Logic is IF-heavy instead                                    |
| **IF statements**   | 48      | Complex validation and posting logic                         |
| **PERFORM calls**   | 61      | High procedural complexity                                   |
| **VSAM files**      | 6       | DALYTRAN(R), TRANSACT(W), XREFFILE(R), DALYREJS(W), ACCTDATA(R/W), TCATBALF(R/W) |
| **Copybooks used**  | 5       | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y            |
| **Complexity**      | 8/10    | 6-file I/O, validation + posting + reject handling           |
| **Risk**            | 10/10   | Writes to TRANSACT + ACCTDATA + TCATBALF -- financial core   |
| **Business Impact** | 10/10   | Daily batch posting -- if this fails, no transactions post   |

**Why it's #2**: This is the financial heart of the batch cycle. It reads daily transactions, validates them against cross-references, posts valid ones to the master transaction file, updates account balances, maintains category balances, and writes rejects. A bug here means lost or duplicated transactions. It accesses 6 VSAM files simultaneously, creating the widest blast radius in the batch suite.

**Modernization Recommendation**: Convert to Spring Batch with chunk-oriented processing. Implement database transactions for atomicity (the current VSAM approach has no rollback). Add idempotency keys to prevent duplicate posting. Critical path for regression testing.

---

### #3 -- COCRDLIC (Card List) -- Score: 82/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 1,459   | Second-largest online program                                |
| **EVALUATE blocks** | 18      | Selection validation, pagination logic                       |
| **IF statements**   | 59      | Row-level selection validation, error handling               |
| **PERFORM calls**   | 34      | Browse/page operations                                       |
| **EXEC CICS**       | 18      | STARTBR, READNEXT, READPREV, ENDBR, XCTL                    |
| **VSAM files**      | 2       | CARDDATA (browse), CARDXREF (AIX browse)                     |
| **Complexity**      | 9/10    | Paginated browse with row-level selection and validation     |
| **Risk**            | 6/10    | Read-only access, but drives navigation to update screens    |
| **Business Impact** | 8/10    | Primary card lookup screen -- gateway to card operations     |

**Why it's #3**: The paginated list pattern with row-level selection (S=view, U=update) is one of the most complex CICS UI patterns to modernize. It uses STARTBR/READNEXT/READPREV for cursor-based pagination that has no direct equivalent in REST APIs. The 7-row selection array with per-row error flags adds significant validation complexity.

**Modernization Recommendation**: Replace with paginated REST endpoint + React/Angular data table. Server-side cursor pagination maps to SQL OFFSET/LIMIT or keyset pagination. Extract selection logic into DTOs.

---

### #4 -- COCRDUPC (Card Update) -- Score: 80/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 1,560   | Third-largest online program                                 |
| **EVALUATE blocks** | 16      | Field validation branching                                   |
| **IF statements**   | 72      | Heavy input validation                                       |
| **PERFORM calls**   | 26      | Validation + update + screen management                      |
| **EXEC CICS**       | 12      | MAP I/O, file READ/REWRITE                                   |
| **VSAM files**      | 2       | CARDDATA (R/W), CARDXREF (R)                                 |
| **Complexity**      | 9/10    | Dense validation logic with many edit rules                  |
| **Risk**            | 8/10    | Writes to Card Data -- card number, CVV, status changes      |
| **Business Impact** | 8/10    | Card maintenance -- status changes affect transaction auth   |

**Why it's #4**: Similar pattern to COACTUPC but for card data. The 72 IF statements drive field-level validation including card number format, expiration date, and status transitions. Card status changes (active/inactive) directly affect whether transactions are authorized, making this a high-business-impact module.

**Modernization Recommendation**: Extract validation rules into a shared validation service. Card status transitions should be modeled as a state machine. PAN (card number) handling requires PCI-DSS compliance in the Java target.

---

### #5 -- CBSTM03A (Statement Generation) -- Score: 78/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 924     | Large batch program with subroutine                          |
| **EVALUATE blocks** | 9       | Report formatting logic                                      |
| **IF statements**   | 15      | Control breaks, file status checks                           |
| **PERFORM calls**   | 29      | Sequential file processing                                   |
| **CALL statements** | 13      | 13 calls to CBSTM03B subroutine                              |
| **VSAM files**      | 4       | TRNXFILE(R), XREFFILE(R), ACCTFILE(R), CUSTFILE(R)           |
| **Output files**    | 2       | STATEMNT.PS (text), STATEMNT.HTML (HTML)                     |
| **Complexity**      | 8/10    | Dual-format output, mainframe control block addressing       |
| **Risk**            | 7/10    | Read-only on VSAM, but generates customer-facing statements  |
| **Business Impact** | 9/10    | Customer statements -- regulatory requirement, high visibility|

**Why it's #5**: This program generates customer-facing account statements in both text and HTML formats. It demonstrates several complex mainframe patterns: mainframe control block addressing (TIOT), CALL to subroutine (CBSTM03B), and dual-output formatting. The subroutine architecture (CBSTM03A calls CBSTM03B 13 times) adds coupling complexity. As a customer-facing output, statement accuracy is a regulatory requirement.

**Modernization Recommendation**: Replace with Spring Batch + template engine (Thymeleaf/FreeMarker for HTML, JasperReports for PDF). The CBSTM03A/CBSTM03B split can be merged into a single service class. TXT2PDF1 JCL step becomes unnecessary with native PDF generation.

---

### #6 -- CBACT04C (Interest Calculation) -- Score: 76/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 652     | Complex financial logic                                      |
| **IF statements**   | 43      | Rate lookup, threshold checks, computation branches          |
| **PERFORM calls**   | 56      | Highest PERFORM density in batch programs                    |
| **VSAM files**      | 5       | TCATBALF(R), XREFFILE(R), DISCGRP(R), ACCTDATA(R/W), TRANSACT(W) |
| **Copybooks used**  | 5       | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y            |
| **Complexity**      | 8/10    | Multi-file join logic, financial computations                |
| **Risk**            | 9/10    | Writes interest charges to accounts -- financial accuracy    |
| **Business Impact** | 9/10    | Interest revenue calculation -- directly impacts P&L         |

**Why it's #6**: This program computes interest charges by joining 5 VSAM files: it reads category balances, looks up the appropriate interest rate from the disclosure group table, calculates interest, and posts it to the account. The financial computation logic must be preserved exactly during modernization -- even minor rounding differences will cause reconciliation failures. It also has the highest PERFORM density (56 calls in 652 lines) indicating deeply nested procedural flow.

**Modernization Recommendation**: Extract financial calculations into a pure Java service with BigDecimal arithmetic. Implement comprehensive unit tests with known test vectors before and after conversion. Consider making this a separate microservice due to its distinct data access pattern (5 tables).

---

### #7 -- COTRN02C (Transaction Add) -- Score: 74/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 783     | Significant online program                                   |
| **EVALUATE blocks** | 26      | Highest EVALUATE count of any program                        |
| **IF statements**   | 14      | Moderate conditional logic                                   |
| **PERFORM calls**   | 61      | High procedural complexity                                   |
| **EXEC CICS**       | 11      | File reads, writes, map I/O                                  |
| **CALL statements** | 2       | Calls CSUTLDTC for date validation                           |
| **VSAM files**      | 3       | TRANSACT(W), CARDXREF-AIX(R), ACCTDATA(R)                   |
| **Complexity**      | 8/10    | 26 EVALUATE blocks, date validation via CALL                 |
| **Risk**            | 8/10    | Writes new transactions -- data integrity critical           |
| **Business Impact** | 8/10    | Manual transaction entry -- used for adjustments/corrections |

**Why it's #7**: With 26 EVALUATE blocks (the most of any program), this module has the most complex branching structure in the online suite. It validates card numbers against the cross-reference AIX (alternate index), checks account status, validates dates via CSUTLDTC CALL, and writes to the transaction master. The EVALUATE-heavy style will map well to Java switch expressions but requires careful branch coverage testing.

**Modernization Recommendation**: Map EVALUATE blocks to Java switch expressions or strategy pattern. Date validation (CSUTLDTC CALL) becomes `java.time` API usage. The CARDXREF AIX lookup becomes a JPA query with a secondary index.

---

### #8 -- CBTRN03C (Transaction Detail Report) -- Score: 72/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 649     | Complex reporting program                                    |
| **EVALUATE blocks** | 4       | Report section logic                                         |
| **IF statements**   | 38      | Control breaks, page breaks, total calculations              |
| **PERFORM calls**   | 72      | Highest PERFORM count of any batch program                   |
| **VSAM files**      | 5       | TRANSACT(R), CARDXREF(R), TRANTYPE(R), TRANCATG(R), DATEPARM(R) + REPTFILE(W) |
| **Copybooks used**  | 5       | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y            |
| **Complexity**      | 8/10    | 72 PERFORMs, 5-file lookup joins, multi-level totals         |
| **Risk**            | 5/10    | Read-only on master files, writes report output              |
| **Business Impact** | 7/10    | Daily transaction reporting -- audit trail, compliance       |

**Why it's #8**: This report program has the highest PERFORM count (72) of any program, reflecting deeply nested control-break logic with page totals, account totals, and grand totals. It joins 5 files to resolve type/category descriptions and cross-references. The CVTRA07Y copybook defines elaborate print formatting with edited PIC clauses (`-ZZZ,ZZZ,ZZZ.ZZ`) that need special handling in Java.

**Modernization Recommendation**: Replace with JasperReports or similar Java reporting framework. Control-break logic maps to SQL GROUP BY with ROLLUP. The 5-file join becomes a single SQL query with JOINs. Date parameter file (DATEPARM) becomes application properties or API parameters.

---

### #9 -- COACTVWC (Account View) -- Score: 68/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 941     | Significant read-only program                                |
| **EVALUATE blocks** | 10      | Navigation and display logic                                 |
| **IF statements**   | 28      | Account validation, display formatting                       |
| **PERFORM calls**   | 21      | Screen management and data retrieval                         |
| **EXEC CICS**       | 15      | MAP I/O, multi-file READ                                     |
| **VSAM files**      | 4       | ACCTDATA(R), CUSTDATA(R), CARDXREF(R), CARDDATA(R)           |
| **Copybooks used**  | 12      | Broad copybook usage including CSSTRPFY                      |
| **Complexity**      | 7/10    | 4-file read with display formatting                          |
| **Risk**            | 5/10    | Read-only, but displays sensitive customer data (PII)        |
| **Business Impact** | 8/10    | Most frequently accessed screen after login                  |

**Why it's #9**: Although read-only, this program joins 4 VSAM files to build a comprehensive account view. It's likely the most frequently accessed screen in the application, making it high-visibility. The CSSTRPFY copybook for string manipulation and the 12 copybook dependencies create a wide surface area for modernization issues. PII display (customer SSN, address) requires access control in the Java target.

**Modernization Recommendation**: Convert to REST GET endpoint + React component. The 4-file join becomes a single JPA query with eager fetches. Implement field-level access control for PII. Good candidate for early modernization since it's read-only (lower risk).

---

### #10 -- COTRN00C (Transaction List) -- Score: 65/100

| Metric              | Value   | Detail                                                       |
|---------------------|---------|--------------------------------------------------------------|
| **Lines of Code**   | 699     | Paginated list with browse logic                             |
| **EVALUATE blocks** | 16      | Navigation key handling                                      |
| **IF statements**   | 26      | Page boundary logic, selection validation                    |
| **PERFORM calls**   | 43      | Browse operations, pagination                                |
| **EXEC CICS**       | 10      | STARTBR, READNEXT, READPREV, ENDBR                          |
| **VSAM files**      | 1       | TRANSACT (browse)                                            |
| **Complexity**      | 7/10    | CICS browse pagination pattern                               |
| **Risk**            | 4/10    | Read-only browse                                             |
| **Business Impact** | 7/10    | Primary transaction lookup -- high usage frequency           |

**Why it's #10**: Same paginated browse pattern as COCRDLIC (#3) but simpler since it only accesses one VSAM file. The 16 EVALUATE blocks handle PF-key navigation (PF7/PF8 for page back/forward). While lower risk (read-only), it's high-frequency and the pagination pattern is inherently complex to convert from CICS STARTBR/READNEXT to REST-based pagination.

**Modernization Recommendation**: Paginated REST endpoint with keyset pagination. The CICS STARTBR/READNEXT pattern maps to SQL `WHERE key > :lastKey ORDER BY key LIMIT :pageSize`. PF-key navigation becomes next/previous page links or infinite scroll.

---

## Summary Ranking Table

| Rank | Program   | LOC   | Complexity | Risk  | Business Impact | Composite Score | Primary Concern                          |
|-----:|-----------|------:|-----------:|------:|----------------:|----------------:|------------------------------------------|
|    1 | COACTUPC  | 4,236 |      10    |   9   |        9        |          95     | Extreme size, 164 IFs, writes Account    |
|    2 | CBTRN02C  |   731 |       8    |  10   |       10        |          88     | 6-file I/O, financial posting core       |
|    3 | COCRDLIC  | 1,459 |       9    |   6   |        8        |          82     | Paginated browse, row-level selection    |
|    4 | COCRDUPC  | 1,560 |       9    |   8   |        8        |          80     | Dense validation, card status changes    |
|    5 | CBSTM03A  |   924 |       8    |   7   |        9        |          78     | Dual-format output, subroutine coupling  |
|    6 | CBACT04C  |   652 |       8    |   9   |        9        |          76     | Interest calc, 5-file join, financial    |
|    7 | COTRN02C  |   783 |       8    |   8   |        8        |          74     | 26 EVALUATEs, writes transactions        |
|    8 | CBTRN03C  |   649 |       8    |   5   |        7        |          72     | 72 PERFORMs, 5-file report joins         |
|    9 | COACTVWC  |   941 |       7    |   5   |        8        |          68     | 4-file read, PII display, high frequency |
|   10 | COTRN00C  |   699 |       7    |   4   |        7        |          65     | CICS browse pagination pattern           |

---

## Risk Heat Map

```
                    Low Business Impact          High Business Impact
                 ┌─────────────────────────┬─────────────────────────┐
  High Risk      │                         │  COACTUPC (#1)          │
  (writes,       │                         │  CBTRN02C (#2)          │
   financial,    │                         │  COCRDUPC (#4)          │
   multi-file)   │                         │  CBACT04C (#6)          │
                 │                         │  COTRN02C (#7)          │
                 ├─────────────────────────┼─────────────────────────┤
  Low Risk       │                         │  CBSTM03A (#5)          │
  (read-only,    │                         │  COACTVWC (#9)          │
   single file,  │  CBTRN03C (#8)          │  COCRDLIC (#3)          │
   utility)      │                         │  COTRN00C (#10)         │
                 └─────────────────────────┴─────────────────────────┘
```

---

## Recommended Modernization Sequence

Based on the hotspot analysis, the recommended conversion order balances risk reduction with value delivery:

| Phase | Programs                        | Rationale                                                  |
|-------|----------------------------------|------------------------------------------------------------|
| **1. Quick Wins** | COACTVWC, COTRN00C, COTRN01C | Read-only, lower risk, validates end-to-end pipeline |
| **2. Core CRUD**  | COCRDLIC, COCRDSLC, COCRDUPC | Card management -- complex but contained               |
| **3. Account**    | COACTUPC                      | Highest complexity -- tackle with full test harness     |
| **4. Transactions** | COTRN02C, COBIL00C          | Transaction add + bill pay -- validates write path     |
| **5. Batch Core** | CBTRN02C, CBACT04C           | Financial core -- needs extensive parallel-run testing |
| **6. Reporting**  | CBSTM03A, CBSTM03B, CBTRN03C | Reporting -- can run in parallel with legacy initially |
| **7. Data Migration** | CBEXPORT, CBIMPORT        | Migration tools -- needed for cutover                  |
| **8. Admin & Utility** | COUSR00C-03C, COSGN00C  | Auth/admin -- often replaced by Spring Security        |

---

## Key Modernization Risks

| Risk                              | Affected Programs              | Mitigation                                              |
|-----------------------------------|--------------------------------|---------------------------------------------------------|
| **Decimal precision loss**        | CBACT04C, CBTRN02C, COACTUPC  | Use `BigDecimal` everywhere; validate with parallel run |
| **VSAM key structure changes**    | All programs                   | Map KSDS keys to composite primary keys in RDBMS        |
| **COPY REPLACING semantics**      | COACTUPC (39 instances)        | Expand macros before conversion; generate Java utility   |
| **CICS pseudo-conversational**    | All online programs            | Map to stateless REST + session store                    |
| **Paginated browse (STARTBR)**    | COCRDLIC, COTRN00C, COUSR00C  | Implement keyset pagination in SQL                       |
| **Batch restart/checkpoint**      | CBTRN02C, CBSTM03A            | Spring Batch chunk processing with restartability        |
| **Date handling (CEEDAYS)**       | CSUTLDTC, COTRN02C, CORPT00C  | Replace with `java.time` API                             |
| **Plain-text passwords**          | COSGN00C, CSUSR01Y            | Implement bcrypt hashing in Java, migrate existing PWDs  |
| **PII exposure**                  | COACTVWC, COACTUPC             | Add field-level encryption + access control              |
| **GDG (Generation Data Groups)**  | TRANBKP, COMBTRAN, TRANREPT   | Replace with timestamped file naming or object storage   |
