# HOTSPOT REPORT -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Top 10 modules prioritized by complexity, risk, and business impact for modernization planning

---

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale each, 30 max):

| Dimension | Criteria | Weight |
|-----------|----------|--------|
| **Complexity** | LOC, number of PERFORM paragraphs, COPY includes, CICS commands, file I/O operations, conditional branching depth, COPY REPLACING usage | 10 |
| **Risk** | Data mutation (REWRITE/DELETE/WRITE), financial calculations, multi-file updates in single transaction, error handling gaps, cross-system calls (MQ/DB2/IMS) | 10 |
| **Business Impact** | Criticality to core business flow, user-facing, data integrity dependency, batch cycle position, downstream dependencies | 10 |

---

## Top 10 Hotspot Modules

### #1: COACTUPC.cbl -- Account Update

| Metric | Value |
|--------|-------|
| **LOC** | 4,236 (largest program in codebase) |
| **Type** | Online CICS |
| **Complexity Score** | 10/10 |
| **Risk Score** | 9/10 |
| **Business Impact Score** | 10/10 |
| **Total Score** | **29/30** |

**Why it's #1:**
- **Largest program by far** -- 4,236 LOC is 2.7x the next largest core program. Extremely high cyclomatic complexity.
- **38 COPY REPLACING invocations** of CSSETATY for BMS attribute management -- generates massive inline code.
- **15+ copybook includes** spanning account, card, customer, cross-reference, and user entities.
- **Reads 5 VSAM files** (ACCTDATA, CARDXREF, CUSTDATA, and more) in a single transaction flow.
- **Writes to account master** -- direct financial data mutation.
- **HANDLE ABEND** / **ABEND** commands indicate complex error recovery paths.
- **Core business function** -- account modification is the most sensitive operation in a credit card system.

**Modernization Risks:**
- Monolithic structure must be decomposed into service layer + validation + persistence.
- BMS attribute logic (38 COPY REPLACING blocks) has no direct Java equivalent; needs UI framework mapping.
- Multi-file reads within a single pseudo-conversational flow need careful transaction boundary design.

**Recommended Approach:** Decompose into `AccountUpdateService`, `AccountValidator`, `AccountRepository`. Map BMS field attributes to form validation annotations.

---

### #2: CBTRN02C.cbl -- Transaction Posting (Full)

| Metric | Value |
|--------|-------|
| **LOC** | 731 |
| **Type** | Batch |
| **Complexity Score** | 8/10 |
| **Risk Score** | 10/10 |
| **Business Impact Score** | 10/10 |
| **Total Score** | **28/30** |

**Why it's #2:**
- **Highest-risk batch program** -- processes daily transactions and updates the master transaction file.
- **6 file I/O operations** -- reads DALYTRAN, TRANFILE, XREFFILE, ACCTFILE; writes DALYREJS, TCATBALF.
- **Multi-file update** in single run: updates transaction master, account balances, and category balances simultaneously.
- **Rejection handling** -- creates DALYREJS file for failed transactions, requiring reconciliation logic.
- **7 distinct paragraph sections** indicating complex control flow.
- **Critical batch cycle position** -- Step 3 in nightly cycle; all downstream jobs depend on its output.

**Modernization Risks:**
- Must preserve exactly-once semantics for financial postings.
- Rejection handling must be mapped to Spring Batch skip/retry policies.
- Multi-file updates need database transaction management (ACID).

**Recommended Approach:** Spring Batch job with `ItemReader` (daily trans), `ItemProcessor` (validation + xref lookup), `ItemWriter` (multi-table commit). Use `@Transactional` for atomicity.

---

### #3: COTRTLIC.cbl -- Transaction Type List (DB2)

| Metric | Value |
|--------|-------|
| **LOC** | 2,098 |
| **Type** | Online CICS + DB2 |
| **Complexity Score** | 9/10 |
| **Risk Score** | 8/10 |
| **Business Impact Score** | 8/10 |
| **Total Score** | **25/30** |

**Why it's #3:**
- **Second-largest program overall** (2,098 LOC) with mixed CICS + DB2 technology.
- **10+ embedded SQL statements** including cursor-based pagination, SELECT, DELETE, UPDATE.
- **SYNCPOINT operations** for explicit DB2 commit/rollback control.
- **12 copybook includes** spanning BMS, DB2, and business entities.
- **Demonstrates cursor-based paging** -- complex stateful DB2 cursor management across pseudo-conversational flow.
- **DSNTIAC error formatting** adds external dependency complexity.

**Modernization Risks:**
- Cursor-based paging must map to JPA/Spring Data pagination.
- SYNCPOINT semantics differ from Java transaction management.
- Mixed CICS/DB2 error handling requires unified exception strategy.

**Recommended Approach:** Spring MVC controller + JPA repository with `Pageable`. Replace SYNCPOINT with `@Transactional`.

---

### #4: COTRTUPC.cbl -- Transaction Type Update (DB2)

| Metric | Value |
|--------|-------|
| **LOC** | 1,702 |
| **Type** | Online CICS + DB2 |
| **Complexity Score** | 9/10 |
| **Risk Score** | 8/10 |
| **Business Impact Score** | 7/10 |
| **Total Score** | **24/30** |

**Why it's #4:**
- **1,702 LOC** with embedded SQL for INSERT/UPDATE operations.
- **HANDLE ABEND + explicit ABEND** for error recovery.
- **3 SYNCPOINT operations** (commit paths for different outcomes).
- **COPY REPLACING pattern** for BMS attributes (similar to COACTUPC).
- **DB2 table joins** across TRTYP and TRCAT tables.

**Modernization Risks:**
- Multiple SYNCPOINT paths need careful transaction boundary mapping.
- ABEND recovery must map to exception handlers.
- DB2 embedded SQL to JPA entity mapping.

**Recommended Approach:** Spring MVC + `@Transactional` service with JPA entities for TRTYP/TRCAT.

---

### #5: COCRDUPC.cbl -- Credit Card Update

| Metric | Value |
|--------|-------|
| **LOC** | 1,560 |
| **Type** | Online CICS |
| **Complexity Score** | 8/10 |
| **Risk Score** | 8/10 |
| **Business Impact Score** | 8/10 |
| **Total Score** | **24/30** |

**Why it's #5:**
- **1,560 LOC** -- third-largest core CICS program.
- **8 paragraph sections** with complex branching.
- **Writes to CARDDATA** -- PCI-sensitive card data modification.
- **HANDLE ABEND** error recovery paths.
- **COPY REPLACING** for BMS attribute management.
- **Reads CARDDATA + CUSTDATA** and rewrites card records.

**Modernization Risks:**
- Card data is PCI DSS sensitive -- Java migration must implement encryption at rest.
- Card number handling requires tokenization strategy.
- BMS attribute mapping complexity.

**Recommended Approach:** `CardUpdateService` with PCI-compliant data handling. Encrypt CARD-CVV-CD and mask CARD-NUM in UI.

---

### #6: COCRDLIC.cbl -- Credit Card List

| Metric | Value |
|--------|-------|
| **LOC** | 1,459 |
| **Type** | Online CICS |
| **Complexity Score** | 8/10 |
| **Risk Score** | 6/10 |
| **Business Impact Score** | 8/10 |
| **Total Score** | **22/30** |

**Why it's #6:**
- **1,459 LOC** with complex STARTBR/READNEXT/READPREV browse logic for paginated card list.
- **9 paragraph sections** -- highest paragraph count among core CICS programs.
- **Dual behavior** -- shows all cards for admin, only account-linked cards for regular users.
- **3 XCTL calls** dispatching to card view and card update.
- **Forward and backward paging** using VSAM browse operations.

**Modernization Risks:**
- VSAM browse pagination must map to database cursor or offset/limit queries.
- Role-based filtering logic needs Spring Security integration.
- Bidirectional paging state management across pseudo-conversational flow.

**Recommended Approach:** Spring Data JPA with `Pageable` + `@PreAuthorize` for role-based card visibility.

---

### #7: COPAUA0C.cbl -- Card Authorization Decision (MQ)

| Metric | Value |
|--------|-------|
| **LOC** | 1,026 |
| **Type** | CICS + MQ |
| **Complexity Score** | 8/10 |
| **Risk Score** | 9/10 |
| **Business Impact Score** | 7/10 |
| **Total Score** | **24/30** (ranked #7 due to optional module status) |

**Why it's #7:**
- **MQ-triggered program** -- reads authorization requests from queue, makes approve/decline decision, sends reply.
- **6 MQ API calls** (MQOPEN, MQGET, MQPUT1, MQCLOSE) with full handle management.
- **Reads 3 VSAM files** (CARDXREF, ACCTDATA, CUSTDATA) for authorization validation.
- **CICS RETRIEVE** for initial trigger context.
- **WRITEQ TS** for audit trail.
- **Real-time financial decision** -- authorization approve/decline is latency-critical.

**Modernization Risks:**
- MQ integration must map to JMS or Spring Cloud Stream.
- Real-time decision logic needs low-latency design.
- CICS trigger mechanism has no direct Java equivalent.

**Recommended Approach:** Spring Boot + JMS `@JmsListener` for request processing. REST fallback for synchronous authorization.

---

### #8: CBSTM03A.CBL -- Statement Generation

| Metric | Value |
|--------|-------|
| **LOC** | 924 |
| **Type** | Batch |
| **Complexity Score** | 7/10 |
| **Risk Score** | 7/10 |
| **Business Impact Score** | 9/10 |
| **Total Score** | **23/30** |

**Why it's #8:**
- **Dual output format** -- generates both plain text AND HTML statements simultaneously.
- **13 CALL invocations** to CBSTM03B subroutine for file I/O operations.
- **15 paragraph sections** -- highest paragraph count of any program in the codebase.
- **4 input files** (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE) correlated for statement generation.
- **Customer-facing output** -- statements are the primary customer communication artifact.

**Modernization Risks:**
- HTML generation in COBOL must be replaced with proper template engine (Thymeleaf, etc.).
- Subroutine call pattern (CBSTM03B) needs refactoring into Java service methods.
- Multi-file correlation logic maps to complex SQL joins.

**Recommended Approach:** Spring Batch job with JPA for data retrieval. Thymeleaf templates for HTML statements. PDF generation via iText or similar.

---

### #9: CBACT04C.cbl -- Interest Calculation

| Metric | Value |
|--------|-------|
| **LOC** | 652 |
| **Type** | Batch |
| **Complexity Score** | 7/10 |
| **Risk Score** | 10/10 |
| **Business Impact Score** | 9/10 |
| **Total Score** | **26/30** (ranked #9 due to lower LOC complexity) |

**Why it's #9:**
- **Financial calculation engine** -- computes interest on account balances by transaction category.
- **5 file I/O operations** spanning TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT.
- **Reads discount/interest rate table** (DISCGRP) and applies rates to category balances.
- **Creates interest transactions** written to TRANSACT -- generates financial records.
- **Parameterized execution** via JCL PARM (date parameter).

**Modernization Risks:**
- **Highest financial risk** -- incorrect interest calculation directly impacts customer accounts.
- Decimal precision must be preserved exactly (COBOL `S9(09)V99` to Java `BigDecimal`).
- Interest rate lookup logic (group -> type -> category -> rate) is a complex join.
- Must produce bit-identical results during parallel run validation.

**Recommended Approach:** Spring Batch with `BigDecimal` arithmetic throughout. Extensive unit testing with COBOL output comparison. Consider parallel-run validation period.

---

### #10: COBIL00C.cbl -- Bill Payment

| Metric | Value |
|--------|-------|
| **LOC** | 572 |
| **Type** | Online CICS |
| **Complexity Score** | 6/10 |
| **Risk Score** | 9/10 |
| **Business Impact Score** | 9/10 |
| **Total Score** | **24/30** (ranked #10 due to moderate complexity) |

**Why it's #10:**
- **Financial transaction creation** -- pays account balance and creates payment transaction record.
- **REWRITE to ACCTDATA** -- directly modifies account balance (financial mutation).
- **WRITE to TRANSACT** -- creates new transaction record.
- **ASKTIME/FORMATTIME** for transaction timestamp generation.
- **STARTBR/READPREV** for last-transaction lookup.
- **Reads 3 files** (ACCTDATA, CARDXREF, TRANSACT) in a single operation.

**Modernization Risks:**
- Multi-file update (account balance + new transaction) must be atomic.
- Payment amount validation and balance verification logic is business-critical.
- Timestamp handling differs between CICS and Java.

**Recommended Approach:** `BillPaymentService` with `@Transactional`. Validate sufficient balance, update account, create transaction in single DB transaction.

---

## Summary Ranking Table

| Rank | Program | LOC | Type | Complexity | Risk | Impact | **Total** | Primary Concern |
|------|---------|-----|------|-----------|------|--------|-----------|-----------------|
| 1 | **COACTUPC** | 4,236 | CICS | 10 | 9 | 10 | **29** | Monolithic size, 38 COPY REPLACING, multi-file writes |
| 2 | **CBTRN02C** | 731 | Batch | 8 | 10 | 10 | **28** | Multi-file financial posting, rejection handling |
| 3 | **COTRTLIC** | 2,098 | CICS+DB2 | 9 | 8 | 8 | **25** | DB2 cursor paging, SYNCPOINT, mixed technology |
| 4 | **COTRTUPC** | 1,702 | CICS+DB2 | 9 | 8 | 7 | **24** | DB2 writes, multiple SYNCPOINT paths, ABEND |
| 5 | **COCRDUPC** | 1,560 | CICS | 8 | 8 | 8 | **24** | PCI-sensitive card data writes |
| 6 | **COCRDLIC** | 1,459 | CICS | 8 | 6 | 8 | **22** | Complex browse paging, role-based filtering |
| 7 | **COPAUA0C** | 1,026 | CICS+MQ | 8 | 9 | 7 | **24** | MQ integration, real-time auth decisions |
| 8 | **CBSTM03A** | 924 | Batch | 7 | 7 | 9 | **23** | Dual-format output, 13 subroutine calls |
| 9 | **CBACT04C** | 652 | Batch | 7 | 10 | 9 | **26** | Financial interest calculation, decimal precision |
| 10 | **COBIL00C** | 572 | CICS | 6 | 9 | 9 | **24** | Financial payment, multi-file atomic update |

---

## Modernization Priority Matrix

```
                          HIGH BUSINESS IMPACT
                                 |
                    Q2           |           Q1
              (Plan Carefully)   |    (Migrate First)
                                 |
        CBACT04C [9]             |    COACTUPC [1]
        CBSTM03A [8]            |    CBTRN02C [2]
                                 |    COBIL00C [10]
   LOW COMPLEXITY ---------------+--------------- HIGH COMPLEXITY
                                 |
        COPAUA0C [7]             |    COTRTLIC [3]
                                 |    COTRTUPC [4]
              (Quick Wins)       |    COCRDUPC [5]
                    Q3           |    COCRDLIC [6]
                                 |           Q4
                                 |    (Defer / Refactor)
                          LOW BUSINESS IMPACT
```

### Recommended Migration Wave Plan

| Wave | Programs | Rationale |
|------|----------|-----------|
| **Wave 1: Foundation** | COSGN00C, COMEN01C, COADM01C, CSUTLDTC | Low risk navigation/auth layer; establishes security framework |
| **Wave 2: Read-Only Views** | COACTVWC, COCRDSLC, COTRN01C, COTRN00C, COCRDLIC | Read-only programs; validates data access layer |
| **Wave 3: User CRUD** | COUSR00C, COUSR01C, COUSR02C, COUSR03C | Self-contained admin CRUD; tests full write path |
| **Wave 4: Financial Core** | COBIL00C, COTRN02C (online), CBTRN02C (batch) | Highest business value; requires extensive testing |
| **Wave 5: Account & Card Updates** | COACTUPC, COCRDUPC | Most complex programs; decomposition needed |
| **Wave 6: Batch Processing** | CBACT04C, CBSTM03A/B, CBTRN03C, CBTRN01C | Batch cycle; Spring Batch migration |
| **Wave 7: Reporting & Utility** | CORPT00C, CBACT01C-03C, CBCUS01C, CBEXPORT, CBIMPORT, COBSWAIT | Lower risk utilities and reporting |
| **Wave 8: Optional Modules** | Auth (COPAUA0C, etc.), DB2 (COTRTLIC, etc.), MQ (COACCT01, CODATE01) | Cross-technology; depends on target architecture decisions |

---

## Key Technical Debt Items

| # | Issue | Affected Programs | Severity |
|---|-------|------------------|----------|
| 1 | **Plain-text passwords** in USRSEC VSAM | COSGN00C, COUSR01C, COUSR02C | Critical |
| 2 | **No input validation** on BMS fields (accepts any terminal input) | All online programs | High |
| 3 | **COPY REPLACING abuse** -- COACTUPC has 38 identical COPY REPLACING blocks | COACTUPC, COCRDUPC, COTRTUPC | High |
| 4 | **No logging/audit trail** in core programs (no WRITEQ TS or journaling) | Most online programs | Medium |
| 5 | **Hard-coded DSNs** in JCL (AWS.M2.CARDDEMO.*) | All JCL jobs | Medium |
| 6 | **GOTO-like patterns** via PERFORM THRU with multiple exit points | CBTRN02C, CBACT04C | Medium |
| 7 | **UNUSED1Y.cpy** copybook still in codebase | None currently | Low |
| 8 | **No retry/recovery** in batch programs (CEE3ABD abend on any error) | All batch programs | Medium |
| 9 | **Date field inconsistency** -- mix of X(10) date strings and numeric formats | Multiple copybooks | Low |
| 10 | **Large FILLER fields** in records (e.g., 178 bytes in CVACT01Y) | Data copybooks | Low |
