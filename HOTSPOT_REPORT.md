# CardDemo Hotspot Report

> **Application:** AWS CardDemo &mdash; Mainframe Credit Card Management System  
> **Generated:** 2026-03-25 | **Source Repo:** `uc-legacy-modernization-cobol-to-java`

---

## Methodology

Each module was scored across three dimensions on a 1&ndash;10 scale:

| Dimension | Weight | Scoring Criteria |
|---|---|---|
| **Complexity** | 40% | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), number of PERFORM paragraphs, number of COPY includes, file I/O operations, CICS commands |
| **Risk** | 35% | Financial data mutation, multi-file updates in single run, error handling gaps, tight coupling to other modules, assembler/LE dependencies |
| **Business Impact** | 25% | Revenue criticality, user-facing frequency, data integrity consequences if broken, downstream dependencies |

**Composite Score** = (Complexity &times; 0.40) + (Risk &times; 0.35) + (Business Impact &times; 0.25)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC &mdash; Account Update (Online)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **4,236** | Largest program in the entire codebase by far |
| **CICS Commands** | 17 | READ, REWRITE, SEND MAP, RECEIVE MAP, XCTL |
| **Copybook Includes** | 58 | Heaviest include count across all programs |
| **BMS Map** | COACTUP | Complex multi-field update form |
| **Files Accessed** | ACCTDATA (R/W), CARDXREF (R) | Mutates financial balances |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **10** | 4,236 LOC is 3&times; larger than the next biggest online program. Deeply nested EVALUATE/IF logic for field-by-field validation. 58 COPY includes create a massive working-storage section. |
| Risk | **9** | Directly mutates account balances and credit limits. A bug here could corrupt financial records for every account. No batch recovery path for online updates. |
| Business Impact | **9** | Core account management function used by every customer interaction. Downstream: all transaction processing depends on correct account data. |
| **Composite** | **9.45** | |

**Modernization Recommendation:** Decompose into multiple Java service classes (AccountValidationService, AccountUpdateService, AccountViewMapper). Extract field validation into a shared validator. Critical candidate for comprehensive unit testing before and after migration.

---

### Rank 2: CBTRN02C &mdash; Transaction Posting (Batch)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **731** | |
| **Files Accessed** | 6 files | DALYTRAN(R), TRANSACT(W), XREF(R), DALYREJS(W), ACCTDATA(R/W), TCATBALF(R/W) |
| **Copybook Includes** | 6 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **JCL Job** | POSTTRAN | Critical path: first processing step in nightly batch |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 6 concurrent file handles with mixed access modes (INPUT, OUTPUT, I-O). Complex validation logic with reject handling. Multiple REWRITE operations on ACCTDATA and TCATBALF. |
| Risk | **10** | **Highest risk program.** Mutates 4 datasets in a single run (TRANSACT, DALYREJS, ACCTDATA, TCATBALF). A posting bug propagates to interest calculation, statements, and reports. No two-phase commit &mdash; partial failures leave data inconsistent. |
| Business Impact | **10** | The single most business-critical batch program. If POSTTRAN fails, no transactions are posted, interest cannot be calculated, and statements cannot be generated. Entire nightly cycle halts. |
| **Composite** | **9.20** | |

**Modernization Recommendation:** Implement as a Spring Batch job with chunk-oriented processing and database transactions. Add compensating transactions for rollback. This is the #1 candidate for integration testing with production-like data volumes.

---

### Rank 3: CBACT04C &mdash; Interest Calculator (Batch)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **652** | |
| **Files Accessed** | 5 files | TCATBALF(R), XREF(R), DISCGRP(R), ACCTDATA(R/W), TRANSACT(W) |
| **Copybook Includes** | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **JCL Job** | INTCALC | Critical path: runs after POSTTRAN |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | Multi-file join logic (category balance &rarr; cross-ref &rarr; disclosure group &rarr; account). Financial computation with decimal precision requirements. Multiple PERFORM paragraphs for different calculation paths. |
| Risk | **10** | Directly computes and writes interest charges to customer accounts. Incorrect rates or rounding errors have direct financial and regulatory consequences. Writes interest transactions to TRANSACT file. |
| Business Impact | **9** | Interest revenue is a primary revenue stream for the credit card business. Regulatory compliance (Truth in Lending Act) depends on correct interest computation. |
| **Composite** | **8.95** | |

**Modernization Recommendation:** Extract interest calculation logic into a pure computational service with BigDecimal precision. Requires extensive parallel-run testing comparing COBOL output to Java output for every account. Regulatory sign-off may be needed.

---

### Rank 4: CBSTM03A &mdash; Statement Generation (Batch)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **924** | Largest batch program |
| **External CALLs** | 13 calls to CBSTM03B | Heavy subroutine usage |
| **Files Accessed** | 6+ files | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE, STMTFILE(W), HTMLFILE(W) |
| **JCL Job** | CREASTMT | Produces customer-facing output |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **9** | 924 LOC with 13 CALL invocations to CBSTM03B subroutine. Dual-format output (text + HTML). Complex report formatting with page breaks, headers, totals. Mainframe control block addressing (TIOT). Uses ALTER statement (rare, hard to trace). |
| Risk | **7** | Read-only on source data (no mutation risk). However, produces customer-facing statements &mdash; formatting errors are visible to customers. HTML generation is brittle string concatenation. |
| Business Impact | **8** | Statements are the primary customer communication. Errors in amounts or formatting damage customer trust. Downstream: TXT2PDF1 converts to PDF for delivery. |
| **Composite** | **8.15** | |

**Modernization Recommendation:** Replace with a template-based reporting engine (JasperReports, Thymeleaf). The CBSTM03B subroutine pattern maps well to a Strategy pattern for file I/O. The ALTER statement must be carefully analyzed &mdash; it modifies GO TO targets at runtime.

---

### Rank 5: COCRDLIC &mdash; Credit Card List (Online)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **1,459** | |
| **CICS Commands** | 18 | Highest CICS command count |
| **Copybook Includes** | 14 | Including CSSTRPFY (string utilities) |
| **BMS Map** | COCRDLI | Scrollable list with array-based screen |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **9** | 18 CICS commands including STARTBR, READNEXT, READPREV for bidirectional browsing. Array-based screen handling with VARYING loops. Context-sensitive behavior (admin vs. regular user paths). Forward and backward pagination logic. |
| Risk | **6** | Read-only &mdash; no data mutation. However, incorrect filtering could expose cards from other accounts (authorization logic). Role-based access control embedded in program logic. |
| Business Impact | **7** | Entry point for all card-related operations. Navigation hub that dispatches to COCRDSLC (view) and COCRDUPC (update). |
| **Composite** | **7.55** | |

**Modernization Recommendation:** Convert to a paginated REST API + UI component. Extract the role-based filtering into a dedicated authorization service. The bidirectional browse maps to SQL OFFSET/LIMIT with sort direction.

---

### Rank 6: COCRDUPC &mdash; Credit Card Update (Online)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **1,560** | Second-largest online program |
| **CICS Commands** | 12 | READ, REWRITE, SEND/RECEIVE MAP |
| **Copybook Includes** | 16 | |
| **BMS Map** | COCRDUP | Multi-field update form |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 1,560 LOC with extensive field validation. Similar pattern to COACTUPC but for card data. Multiple screen states (fetch, display, validate, save). |
| Risk | **8** | Mutates card data including active status. Incorrect updates could disable active cards or activate expired ones. Card data is PCI-sensitive. |
| Business Impact | **7** | Card management is a core function. Status changes directly affect cardholder ability to transact. |
| **Composite** | **7.70** | |

**Modernization Recommendation:** Similar decomposition as COACTUPC. PCI-DSS compliance requirements must be incorporated into the Java implementation. Card number masking and audit logging are essential additions.

---

### Rank 7: CBTRN03C &mdash; Transaction Detail Report (Batch)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **649** | |
| **Files Accessed** | 6 files | TRANSACT(R), REPORT(W), XREF(R), TRANTYPE(R), TRANCATG(R), DATEPARM(R) |
| **Copybook Includes** | 5 | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **JCL Job** | TRANREPT | End of batch cycle |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **7** | 6 file handles with multi-file lookups (transaction &rarr; cross-ref &rarr; type &rarr; category). Page-break logic with running totals at page, account, and grand total levels. Date range parameter processing. |
| Risk | **6** | Read-only on source data. Report accuracy depends on correct lookups across 4 reference files. Incorrect totals could mislead business decisions. |
| Business Impact | **7** | Primary management reporting output. Used for daily transaction reconciliation and audit. |
| **Composite** | **6.75** | |

**Modernization Recommendation:** Replace with a parameterized SQL query + reporting framework. The multi-level total logic maps to SQL GROUP BY ROLLUP.

---

### Rank 8: COTRN02C &mdash; Transaction Add (Online)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **783** | |
| **CICS Commands** | 11 | READ, WRITE, STARTBR, READPREV, SEND/RECEIVE |
| **External CALLs** | CSUTLDTC (date validation) | |
| **BMS Map** | COTRN02 | Multi-field entry form |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **7** | Multi-step validation (card lookup via cross-ref, account verification, date validation via external CALL). STARTBR/READPREV to generate next transaction ID. Multiple CICS file operations. |
| Risk | **8** | Creates new financial transactions. Validation gaps could allow invalid transactions into the system. Writes to TRANSACT VSAM directly (no staging). |
| Business Impact | **7** | Enables online transaction entry &mdash; used for manual adjustments and corrections. |
| **Composite** | **7.25** | |

**Modernization Recommendation:** Implement as a REST POST endpoint with request validation. Transaction ID generation should use database sequences instead of the STARTBR/READPREV pattern. Add idempotency keys.

---

### Rank 9: COACTVWC &mdash; Account View (Online)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **941** | |
| **CICS Commands** | 15 | Multiple READ operations, SEND/RECEIVE MAP |
| **Copybook Includes** | 16 | |
| **BMS Map** | COACTVW | Detailed account display |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **7** | 941 LOC with 15 CICS commands. Multi-file reads to assemble a complete account view (account + cards + cross-ref). Screen formatting logic for financial display fields. |
| Risk | **5** | Read-only &mdash; no data mutation. However, incorrect display of balances or limits could cause customer confusion or incorrect decisions. |
| Business Impact | **7** | Most frequently accessed screen after login. Foundation for all account-related operations. |
| **Composite** | **6.35** | |

**Modernization Recommendation:** Convert to a REST GET endpoint aggregating data from Account, Card, and CrossRef tables. Good candidate for a read-model/CQRS pattern if performance is critical.

---

### Rank 10: COTRTLIC &mdash; Transaction Type List (Optional/DB2)

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | **2,098** | Largest program in optional modules |
| **Technology** | CICS + DB2 | Embedded SQL with cursors |
| **Module** | `app-transaction-type-db2/` | Optional DB2 module |

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 2,098 LOC with embedded SQL, cursor management, and BMS screen handling. Combined list + update + delete in a single program. |
| Risk | **5** | Reference data management &mdash; lower financial risk. However, incorrect transaction types propagate to all transaction processing. |
| Business Impact | **5** | Admin-only function. Changes are infrequent but affect the meaning of all transactions. |
| **Composite** | **6.25** | |

**Modernization Recommendation:** Straightforward CRUD REST API + admin UI. DB2 cursors map directly to JPA/Hibernate repositories. Good candidate for early migration as a low-risk proof of concept.

---

## Summary Ranking Table

| Rank | Program | Type | LOC | Complexity | Risk | Impact | **Composite** | Migration Priority |
|---|---|---|---|---|---|---|---|---|
| 1 | **COACTUPC** | Online | 4,236 | 10 | 9 | 9 | **9.45** | Phase 2 (high risk) |
| 2 | **CBTRN02C** | Batch | 731 | 8 | 10 | 10 | **9.20** | Phase 2 (high risk) |
| 3 | **CBACT04C** | Batch | 652 | 8 | 10 | 9 | **8.95** | Phase 2 (high risk) |
| 4 | **CBSTM03A** | Batch | 924 | 9 | 7 | 8 | **8.15** | Phase 2 (medium risk) |
| 5 | **COCRDLIC** | Online | 1,459 | 9 | 6 | 7 | **7.55** | Phase 1 (read-only) |
| 6 | **COCRDUPC** | Online | 1,560 | 8 | 8 | 7 | **7.70** | Phase 2 (high risk) |
| 7 | **CBTRN03C** | Batch | 649 | 7 | 6 | 7 | **6.75** | Phase 1 (reporting) |
| 8 | **COTRN02C** | Online | 783 | 7 | 8 | 7 | **7.25** | Phase 2 (writes) |
| 9 | **COACTVWC** | Online | 941 | 7 | 5 | 7 | **6.35** | Phase 1 (read-only) |
| 10 | **COTRTLIC** | Online/DB2 | 2,098 | 8 | 5 | 5 | **6.25** | Phase 1 (proof of concept) |

---

## Recommended Migration Phases

### Phase 1: Low-Risk / Read-Only (Weeks 1&ndash;6)
Build confidence and infrastructure with read-only programs:
- **COTRTLIC** &mdash; DB2 CRUD (proof of concept, already relational)
- **COACTVWC** &mdash; Account View (read-only, high visibility)
- **COCRDLIC** &mdash; Card List (read-only, complex UI patterns)
- **CBTRN03C** &mdash; Transaction Report (batch reporting)
- **COSGN00C** &mdash; Sign On (establish authentication framework)

### Phase 2: Core Business Logic (Weeks 7&ndash;16)
Migrate the critical write paths with extensive parallel testing:
- **CBTRN02C** &mdash; Transaction Posting (highest business impact)
- **CBACT04C** &mdash; Interest Calculator (regulatory sensitivity)
- **COACTUPC** &mdash; Account Update (largest program, complex validation)
- **COCRDUPC** &mdash; Card Update (PCI compliance)
- **COTRN02C** &mdash; Transaction Add (online writes)

### Phase 3: Reporting & Utilities (Weeks 17&ndash;22)
- **CBSTM03A/B** &mdash; Statement Generation (template replacement)
- **CBEXPORT/CBIMPORT** &mdash; Data Migration utilities
- Remaining online screens (COTRN00C, COTRN01C, COBIL00C, COUSR00C-03C)

### Phase 4: Optional Modules (Weeks 23&ndash;28)
- Authorization module (IMS/DB2/MQ &rarr; Spring + JMS/Kafka)
- VSAM-MQ module (MQ &rarr; JMS/Kafka)
- Transaction Type DB2 module (if not done in Phase 1)

---

## Key Risk Mitigations

| Risk | Mitigation |
|---|---|
| **Financial data corruption** | Parallel-run CBTRN02C and CBACT04C output comparison for 30+ days before cutover |
| **Interest calculation errors** | Regulatory review of Java computation logic; decimal precision testing with boundary values |
| **Password plaintext storage** | Implement BCrypt hashing in Java; migration script to hash existing passwords |
| **PCI card data exposure** | Add encryption-at-rest and field-level masking in Java layer; tokenization for card numbers |
| **Batch cycle timing** | Performance benchmark Java batch jobs against COBOL baseline; consider parallel chunk processing |
| **ALTER statement (CBSTM03A)** | Manual code analysis required &mdash; ALTER modifies GO TO targets at runtime, no direct Java equivalent |
| **COMMAREA state management** | Replace with server-side session or JWT tokens; carefully map all 17 screen states |
