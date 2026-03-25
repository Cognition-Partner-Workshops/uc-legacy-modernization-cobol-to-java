# CardDemo Hotspot Report — Top 10 Modules by Complexity, Risk & Business Impact

> **Generated:** 2026-03-25 | **Methodology:** Static analysis of LOC, cyclomatic indicators (IF/EVALUATE/PERFORM counts), CICS command density, copybook fan-out, dataset coupling, and business-criticality weighting
> **Purpose:** Prioritize modernization effort by identifying the highest-risk, highest-complexity modules

---

## Scoring Methodology

Each program is scored on three weighted dimensions:

| Dimension | Weight | Metrics Used |
|-----------|--------|-------------|
| **Complexity** | 40% | Lines of code, PERFORM count, IF count, EVALUATE count, COPY count, CICS command count |
| **Risk** | 30% | Number of datasets accessed (coupling), write operations, shared copybook fan-out, abend handling |
| **Business Impact** | 30% | Financial data involvement, transaction volume, user-facing criticality, downstream dependencies |

**Scoring Scale:** 1 (Low) → 10 (Critical)

---

## Top 10 Hotspot Modules

### Rank #1: COACTUPC — Account Update (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **4,237** | Largest program in the codebase by 3× |
| **PERFORM statements** | 64 | High procedural complexity |
| **IF statements** | 168 | Extremely high conditional branching |
| **EVALUATE statements** | 10 | Multiple decision trees |
| **CICS commands** | 17 | Heavy CICS interaction |
| **COPY statements** | 56 | Highest copybook fan-out (incl. 39× CSSETATY REPLACING) |
| **Datasets accessed** | ACCTDAT (R/W), CUSTDAT (R/W), CXACAIX (R) | 3 datasets, 2 writable |
| **Complexity Score** | **10** | |
| **Risk Score** | **10** | |
| **Business Impact Score** | **10** | |
| **Overall Score** | **10.0** | |

**Why #1:** This is the single most complex program in the entire codebase. At 4,237 lines, it is nearly 3× larger than any other module. It performs extensive field-level validation (SSN, phone, dates, credit limits, FICO scores), uses 39 COPY REPLACING macros for dynamic screen attribute setting, and directly modifies both the account and customer master files — the two most critical datasets in the system. Any bug here can corrupt financial data. This program alone may require decomposition into multiple Java services during modernization.

**Modernization Recommendation:**
- Decompose into: Account Validation Service, Account Persistence Service, Screen/UI Controller
- Extract the 39 CSSETATY REPLACING patterns into a reusable field-attribute utility
- Separate customer update logic from account update logic
- Priority: **Immediate** — highest effort, highest risk

---

### Rank #2: COCRDLIC — Credit Card List (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **1,460** | Second-largest online program |
| **PERFORM statements** | 34 | Moderate |
| **IF statements** | 122 | Very high conditional logic |
| **EVALUATE statements** | 18 | Complex decision trees |
| **CICS commands** | 18 | Highest CICS command count |
| **Datasets accessed** | CARDDAT (R), CARDAIX (R) | Card file browsing |
| **Complexity Score** | **8** | |
| **Risk Score** | **7** | |
| **Business Impact Score** | **8** | |
| **Overall Score** | **7.7** | |

**Why #2:** Complex pagination logic with CICS BROWSE operations (STARTBR/READNEXT/READPREV/ENDBR), 7-row selection array with S/U action codes, and transfer control to both card detail and card update programs. The 18 EVALUATE blocks handle multiple navigation states. High IF count reflects extensive input validation and filter logic.

**Modernization Recommendation:**
- Convert CICS BROWSE to paginated database query (Spring Data JPA)
- Replace selection array with REST endpoint + list UI component
- Priority: **High** — central navigation hub for card management

---

### Rank #3: COCRDUPC — Credit Card Update (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **1,560** | Third-largest program |
| **PERFORM statements** | 26 | Moderate |
| **IF statements** | 148 | Very high conditional logic |
| **EVALUATE statements** | 16 | Complex decision handling |
| **CICS commands** | 12 | Moderate CICS interaction |
| **Datasets accessed** | CARDDAT (R/W), CARDAIX (R) | Card data modification |
| **Complexity Score** | **8** | |
| **Risk Score** | **8** | |
| **Business Impact Score** | **8** | |
| **Overall Score** | **8.0** | |

**Why #3:** Modifies card data directly (status, embossed name, expiry dates). High IF count (148) indicates extensive field validation. Combined with 16 EVALUATE blocks for state management, this program has significant cyclomatic complexity. Card data changes have security implications (CVV, status, names).

**Modernization Recommendation:**
- Extract validation logic into a Card Validation Service
- Implement audit trail for card modifications (not present in COBOL)
- Priority: **High** — financial data mutation with security implications

---

### Rank #4: COACTVWC — Account View (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **942** | Large program |
| **PERFORM statements** | 21 | Moderate |
| **IF statements** | 57 | Moderate-high |
| **EVALUATE statements** | 10 | Complex state handling |
| **CICS commands** | 15 | High CICS interaction |
| **Datasets accessed** | ACCTDAT (R), CUSTDAT (R), CXACAIX (R) | 3 datasets, read-only |
| **Complexity Score** | **7** | |
| **Risk Score** | **5** | |
| **Business Impact Score** | **8** | |
| **Overall Score** | **6.7** | |

**Why #4:** Reads from 3 VSAM datasets (account, customer, cross-reference) to display a composite account view. The 15 CICS commands include multiple READ operations with error handling. While read-only (lower risk), it is the primary account inquiry screen and a gateway to account update. Its composite data retrieval pattern makes it a good candidate for a query/read-model service.

**Modernization Recommendation:**
- Convert to Account Query Service (read-only)
- Use a single JOIN query replacing 3 separate VSAM READs
- Priority: **High** — most-used inquiry screen

---

### Rank #5: CBTRN02C — Transaction Posting (Batch)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **731** | Large batch program |
| **PERFORM statements** | 61 | High procedural complexity |
| **IF statements** | 93 | High conditional logic |
| **Datasets accessed** | DALYTRAN (R), XREFFILE (R), ACCTFILE (R/W), TRANFILE (W), DALYREJS (W), TCATBALF (W) | 6 datasets, 4 writable |
| **Complexity Score** | **7** | |
| **Risk Score** | **10** | |
| **Business Impact Score** | **10** | |
| **Overall Score** | **8.8** | |

**Why #5 (but highest risk):** This is the core financial posting engine. It reads daily transactions, validates them against cross-references and accounts, posts to the transaction master, updates account balances, maintains category balances, and writes rejected transactions. Writing to 4 datasets in a single batch run makes it the highest-coupling batch program. A bug here directly corrupts financial records across multiple files.

**Modernization Recommendation:**
- Convert to Spring Batch job with transactional integrity (database transactions instead of VSAM)
- Implement compensating transactions for rollback capability
- Add comprehensive audit logging
- Priority: **Critical** — financial data integrity depends on this program

---

### Rank #6: CBSTM03A — Statement Generation (Batch)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **924** | Large batch program |
| **PERFORM statements** | 29 | Moderate |
| **IF statements** | 15 | Low-moderate |
| **EVALUATE statements** | 5 | Moderate |
| **Datasets accessed** | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (via CBSTM03B), STMTFILE (W), HTMLFILE (W) | Multiple input, 2 output |
| **CALL statements** | 13 (to CBSTM03B) | Tight coupling with sub-program |
| **Complexity Score** | **7** | |
| **Risk Score** | **7** | |
| **Business Impact Score** | **9** | |
| **Overall Score** | **7.6** | |

**Why #6:** Generates customer-facing account statements in both text and HTML format. The 13 CALL invocations to CBSTM03B create a tightly coupled pair that must be modernized together. Reads from 4 VSAM files via its sub-program. Statement generation is a high-visibility customer deliverable — errors appear directly on customer bills.

**Modernization Recommendation:**
- Merge CBSTM03A and CBSTM03B into a single Statement Generation Service
- Replace text/HTML output with a template engine (Thymeleaf/Jasper)
- Convert to Spring Batch with chunk-oriented processing
- Priority: **High** — customer-facing output

---

### Rank #7: COTRN02C — Transaction Add (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **783** | Moderate-large program |
| **PERFORM statements** | 61 | High |
| **IF statements** | 14 | Low |
| **EVALUATE statements** | 13 | Moderate-high |
| **CICS commands** | 11 | Moderate |
| **Datasets accessed** | TRANSACT (R/W), ACCTDAT (R), CXACAIX (R), CCXREF (R) | 4 datasets, 1 writable |
| **CALL statements** | 2 (CSUTLDTC) | Date validation calls |
| **Complexity Score** | **7** | |
| **Risk Score** | **8** | |
| **Business Impact Score** | **9** | |
| **Overall Score** | **7.9** | |

**Why #7:** Creates new transaction records — the primary data entry point for the online system. Validates card/account existence across multiple datasets before writing. Calls CSUTLDTC for date validation. Writing to the TRANSACT master is a critical financial operation. High PERFORM count (61) indicates many procedural steps in the add workflow.

**Modernization Recommendation:**
- Convert to Transaction Creation REST endpoint
- Implement input validation with Bean Validation (JSR 380)
- Add idempotency handling for duplicate prevention
- Priority: **High** — primary financial data entry point

---

### Rank #8: CBACT04C — Interest Calculation (Batch)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **652** | Moderate-large batch |
| **PERFORM statements** | 56 | High |
| **IF statements** | 86 | Very high |
| **Datasets accessed** | XREFFILE (R), ACCTFILE (R/W), DISCGRP (R), TRANSACT (R), TCATBALF (R/W) | 5 datasets, 2 writable |
| **Complexity Score** | **7** | |
| **Risk Score** | **9** | |
| **Business Impact Score** | **10** | |
| **Overall Score** | **8.5** | |

**Why #8 (but very high business impact):** Calculates interest charges on all accounts based on disclosure group rates and category balances. Financial calculation logic with 86 IF statements indicates complex business rules around rate tiers, balance categories, and accrual periods. Incorrect interest calculation has direct regulatory and financial consequences.

**Modernization Recommendation:**
- Convert to a dedicated Interest Calculation Service with extensive unit tests
- Externalize rate tables from VSAM to database with admin UI
- Implement calculation audit trail
- Priority: **Critical** — regulatory and financial accuracy required

---

### Rank #9: COTRN00C — Transaction List (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **699** | Moderate program |
| **PERFORM statements** | 43 | Moderate-high |
| **IF statements** | 26 | Moderate |
| **EVALUATE statements** | 8 | Moderate |
| **CICS commands** | 10 | Moderate |
| **Datasets accessed** | TRANSACT (R) | 1 dataset, read-only |
| **Complexity Score** | **6** | |
| **Risk Score** | **4** | |
| **Business Impact Score** | **7** | |
| **Overall Score** | **5.7** | |

**Why #9:** Primary transaction inquiry screen with CICS BROWSE pagination. Moderate complexity from pagination state management (forward/backward/filter). While read-only and lower risk, it is the most frequently used screen for transaction monitoring and serves as the navigation hub to transaction view and add screens.

**Modernization Recommendation:**
- Convert to paginated REST query endpoint with filtering
- Replace CICS BROWSE with database pagination (LIMIT/OFFSET or cursor-based)
- Priority: **Medium** — frequently used but read-only

---

### Rank #10: CORPT00C — Transaction Reports (Online CICS)

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | **649** | Moderate program |
| **PERFORM statements** | 34 | Moderate |
| **IF statements** | 20 | Moderate |
| **EVALUATE statements** | 5 | Low-moderate |
| **CICS commands** | 7 | Low-moderate |
| **CALL statements** | 2 (CSUTLDTC) | Date validation |
| **Datasets accessed** | TRANSACT (R) | 1 dataset |
| **Complexity Score** | **5** | |
| **Risk Score** | **5** | |
| **Business Impact Score** | **8** | |
| **Overall Score** | **5.9** | |

**Why #10:** Submits batch report jobs from the CICS online interface — a bridge between online and batch processing. Calls CSUTLDTC for date range validation. Report generation is a key business deliverable for management and regulatory compliance. The online-to-batch submission pattern requires careful modernization to avoid breaking the reporting pipeline.

**Modernization Recommendation:**
- Replace CICS-submitted batch with Spring Batch triggered via REST API
- Implement async report generation with status polling
- Priority: **Medium** — important but lower technical complexity

---

## Summary Ranking Table

| Rank | Program | LOC | Type | Complexity | Risk | Biz Impact | **Overall** | Key Concern |
|------|---------|-----|------|-----------|------|------------|-------------|-------------|
| **1** | COACTUPC | 4,237 | Online | 10 | 10 | 10 | **10.0** | Massive size, financial data mutation |
| **2** | CBTRN02C | 731 | Batch | 7 | 10 | 10 | **8.8** | Core posting engine, 4 writable files |
| **3** | CBACT04C | 652 | Batch | 7 | 9 | 10 | **8.5** | Interest calculation, regulatory risk |
| **4** | COCRDUPC | 1,560 | Online | 8 | 8 | 8 | **8.0** | Card data modification, 148 IF stmts |
| **5** | COTRN02C | 783 | Online | 7 | 8 | 9 | **7.9** | Financial data entry point |
| **6** | COCRDLIC | 1,460 | Online | 8 | 7 | 8 | **7.7** | Complex pagination, navigation hub |
| **7** | CBSTM03A | 924 | Batch | 7 | 7 | 9 | **7.6** | Customer-facing statements, coupled pair |
| **8** | COACTVWC | 942 | Online | 7 | 5 | 8 | **6.7** | 3-file composite read, primary inquiry |
| **9** | CORPT00C | 649 | Online | 5 | 5 | 8 | **5.9** | Online-batch bridge, reporting |
| **10** | COTRN00C | 699 | Online | 6 | 4 | 7 | **5.7** | Pagination state, most-used screen |

---

## Modernization Priority Matrix

```
                        Business Impact
                   Low         Medium        High
              ┌───────────┬───────────┬───────────┐
         High │           │ COCRDLIC  │ COACTUPC  │
              │           │ COCRDUPC  │ CBTRN02C  │
  Complexity  │           │           │ CBACT04C  │
              ├───────────┼───────────┼───────────┤
       Medium │           │ COTRN00C  │ COTRN02C  │
              │           │           │ CBSTM03A  │
              │           │           │ COACTVWC  │
              │           │           │ CORPT00C  │
              ├───────────┼───────────┼───────────┤
         Low  │ COBSWAIT  │ CBACT01-  │ COBIL00C  │
              │ CSUTLDTC  │ 03C,      │ COSGN00C  │
              │           │ CBCUS01C  │           │
              └───────────┴───────────┴───────────┘
```

---

## Recommended Modernization Wave Plan

### Wave 1 — Foundation (Weeks 1-4)
- **COCOM01Y** → Java COMMAREA equivalent (session/context object)
- **CSUSR01Y + COSGN00C + COUSR00-03C** → Spring Security + User Management Service
- **Utility programs** (CSUTLDTC, COBSWAIT) → Java utility classes

### Wave 2 — Core Financial (Weeks 5-12)
- **COACTUPC** → Account Update Service (decompose into validation + persistence)
- **CBTRN02C + CBTRN01C** → Transaction Posting Spring Batch Job
- **CBACT04C** → Interest Calculation Service
- **COBIL00C** → Bill Payment Service

### Wave 3 — Card Management (Weeks 13-18)
- **COCRDLIC + COCRDSLC + COCRDUPC** → Card Management Service (CRUD)
- **COACTVWC** → Account Query Service

### Wave 4 — Transactions & Reporting (Weeks 19-24)
- **COTRN00C + COTRN01C + COTRN02C** → Transaction Service (query + create)
- **CORPT00C + CBTRN03C** → Report Generation Service
- **CBSTM03A + CBSTM03B** → Statement Generation Service

### Wave 5 — Data Migration & Cleanup (Weeks 25-28)
- **CBEXPORT + CBIMPORT** → Data Migration Utilities
- **CBACT01-03C, CBCUS01C** → Data validation/print utilities (may be deprecated)
- **Optional modules** (IMS/DB2/MQ) → As needed
