# CardDemo Hotspot Report — Top 10 Modules for Modernization

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across code complexity, data coupling, business criticality, and modernization risk

---

## Scoring Methodology

Each module is scored on four dimensions (1-5 scale each, 5 = highest):

| Dimension | Weight | What It Measures |
|-----------|-------:|------------------|
| **Code Complexity** | 30% | Lines of code, nested logic depth, number of EVALUATE/IF branches, PERFORM THRU chains, GO TO usage |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, COMMAREA fields used, cross-file joins |
| **Business Criticality** | 25% | Revenue impact, user-facing visibility, data integrity role, frequency of execution |
| **Modernization Risk** | 20% | CICS-specific constructs, assembler dependencies, ALTER/GO TO, screen map complexity, state management |

**Composite Score** = (Complexity × 0.30) + (Coupling × 0.25) + (Criticality × 0.25) + (Risk × 0.20)

---

## Top 10 Hotspot Modules

### Rank #1 — COACTUPC (Account Update)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **5** | 4,237 lines — largest program by 2.7×. 39 CSSETATY COPY REPLACING macros for field attribute control. Extensive input validation (SSN, phone, dates, credit limits). Multiple EVALUATE/IF chains for field-by-field editing. |
| Data Coupling | **5** | Reads/writes 4 VSAM files (ACCTDAT, CUSTDAT, CARDXREF via AIX, CXACAIX). Includes 14 copybooks. Manages account, customer, AND card data in a single program. |
| Business Criticality | **5** | Core account maintenance — any bug corrupts financial data. Updates credit limits, balances, customer PII (SSN, DOB, address). |
| Modernization Risk | **5** | Heavy CICS SEND/RECEIVE MAP with cursor positioning. Complex screen attribute manipulation via 39 CSSETATY macros. Stateful multi-step update flow via COMMAREA. Date validation calls CSUTLDTC subroutine. |
| **Composite** | **5.00** | |

**Recommendation:** Decompose into separate Account, Customer, and Card update services. Extract validation logic into shared utility classes. Replace BMS screen state with REST API + form validation. This is the single highest-priority module.

---

### Rank #2 — CBTRN02C (Transaction Posting — Batch)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **4** | 731 lines. Opens 6 files simultaneously (DALYTRAN, TRANSACT, XREF, DALYREJS, ACCOUNT, TCATBAL). Complex posting logic with validation, rejection handling, and multi-file updates in a single transaction. |
| Data Coupling | **5** | Reads/writes 6 datasets. Updates ACCTDATA balances, writes to TRANSACT, updates TCATBALF category balances, writes rejections. Highest file fan-out of any batch program. |
| Business Criticality | **5** | Core financial engine — posts all daily transactions. Incorrect posting = incorrect balances = financial misstatement. Runs nightly; failure stops entire batch cycle. |
| Modernization Risk | **4** | Standard batch I/O (no CICS), but complex multi-file update atomicity needs careful transaction management in Java. Must maintain exact posting logic during migration. |
| **Composite** | **4.50** | |

**Recommendation:** Convert to Spring Batch job with chunk-oriented processing. Use database transactions to replace multi-file VSAM updates. Implement comprehensive reconciliation testing against legacy output.

---

### Rank #3 — COCRDLIC (Card List — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **4** | 1,460 lines. Paginated browse with forward/backward navigation (STARTBR, READNEXT, READPREV, ENDBR). Row-level selection processing (S=view, U=update). 7-row display array with per-row validation. |
| Data Coupling | **4** | Reads CARDDAT via both primary key and alternate index (CARDAIX). Multi-page state managed through extended COMMAREA (WS-THIS-PROGCOMMAREA with first/last keys, page indicators). |
| Business Criticality | **4** | Primary card lookup screen — gateway to card detail and update. Used by both admin and regular users. |
| Modernization Risk | **5** | Complex CICS browse logic (STARTBR/READNEXT/READPREV) with custom pagination state. Row selection pattern maps to list + detail REST API. Extended COMMAREA for page state needs session/cache replacement. |
| **Composite** | **4.25** | |

**Recommendation:** Replace with paginated REST API backed by SQL queries (LIMIT/OFFSET or cursor-based). Card list + selection pattern maps naturally to a data table UI component.

---

### Rank #4 — COCRDUPC (Card Update — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **4** | 1,560 lines. Second-largest online program. Input validation for card fields, date handling, status changes. REWRITE to CARDDAT file. |
| Data Coupling | **4** | Reads CARDDAT, CARDAIX, CUSTDAT. Writes back to CARDDAT. Uses CVCRD01Y work area plus CVACT02Y and CVCUS01Y data structures. |
| Business Criticality | **4** | Card maintenance — expiration dates, status, embossed name. Affects card usability. |
| Modernization Risk | **4** | CICS screen map with field-level attribute control. CSSTRPFY string formatting. Multi-step update flow via COMMAREA state. |
| **Composite** | **4.00** | |

**Recommendation:** Convert to Card update REST endpoint. Merge with COCRDSLC (view) into a single Card detail/edit service. Reuse validation from Account Update decomposition.

---

### Rank #5 — COACTVWC (Account View — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **3** | 942 lines. Three sequential VSAM reads (CXACAIX → ACCTDAT → CUSTDAT) to assemble full account view. Abend handling. |
| Data Coupling | **5** | Reads 3 VSAM files via 2 different access paths. Includes 14 copybooks (CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y). Assembles data from 4 business entities for display. |
| Business Criticality | **4** | Primary account inquiry screen. Read-only but heavily used. Gateway to understanding account state. |
| Modernization Risk | **3** | Read-only pattern is simpler. Main risk is the multi-file join pattern and BMS map complexity. |
| **Composite** | **3.80** | |

**Recommendation:** Convert to Account detail REST API with JPA joins replacing multi-file reads. Natural candidate for early migration as a read-only endpoint.

---

### Rank #6 — CBSTM03A + CBSTM03B (Statement Generation — Batch)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **4** | 924 + 230 = 1,154 lines combined. CBSTM03A calls CBSTM03B 12 times for file open/close/read. ALTER statement modifies paragraph flow dynamically. Generates both text and HTML output. |
| Data Coupling | **4** | Reads 4 files (sorted transactions, xref, customer, account). Writes 2 output files (text statement, HTML statement). Uses COSTM01 and CUSTREC specialized copybooks. |
| Business Criticality | **4** | Produces customer-facing statements. Any error = incorrect bills sent to customers. Regulatory compliance concern. |
| Modernization Risk | **4** | Uses COBOL ALTER statement (dynamically changes PERFORM targets) — very difficult to convert. Two-program CALL architecture needs refactoring. HTML generation embedded in COBOL. |
| **Composite** | **4.00** | |

**Recommendation:** Replace with template-based statement generation (e.g., Thymeleaf/FreeMarker). Convert ALTER logic to standard method dispatch. Good candidate for complete rewrite rather than line-by-line conversion.

---

### Rank #7 — CBACT04C (Interest Calculation — Batch)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **3** | 652 lines. Multi-file lookups: category balance → xref → account → disclosure group. Interest computation with rate lookups. Generates system transactions. |
| Data Coupling | **5** | Reads 4 files (TCATBALF, XREF, ACCTDAT, DISCGRP). Writes 1 file (SYSTRAN — generated interest transactions). Updates ACCTDAT balances. |
| Business Criticality | **5** | Financial calculation — interest charges directly affect customer bills. Regulatory and audit implications. Must be mathematically identical after migration. |
| Modernization Risk | **3** | Standard batch pattern. Main risk is decimal precision (COBOL COMP-3 vs. Java BigDecimal). |
| **Composite** | **3.95** | |

**Recommendation:** Use Java BigDecimal for all calculations. Implement parallel-run reconciliation comparing COBOL and Java outputs. Prioritize extensive numeric regression testing.

---

### Rank #8 — COTRN02C (Transaction Add — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **3** | 783 lines. Validates new transaction data, generates transaction ID, reads xref for card validation, writes to TRANSACT VSAM. Calls CSUTLDTC for date validation. |
| Data Coupling | **4** | Reads CXACAIX (card xref by account), CARDXREF, browses TRANSACT for ID generation. Writes new record to TRANSACT. |
| Business Criticality | **4** | Creates financial records. Data quality at point of entry is critical. |
| Modernization Risk | **3** | CICS browse for ID generation (STARTBR + READPREV to find max ID) needs conversion to DB sequence. CALL to CSUTLDTC date utility. |
| **Composite** | **3.50** | |

**Recommendation:** Replace VSAM browse-based ID generation with database sequences. Convert to POST endpoint with validation middleware.

---

### Rank #9 — COTRN00C (Transaction List — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **3** | 699 lines. Paginated VSAM browse (STARTBR/READNEXT/READPREV) over TRANSACT file. 7-row display with navigation. |
| Data Coupling | **3** | Reads TRANSACT file only. Extended COMMAREA for pagination state. |
| Business Criticality | **3** | Transaction inquiry — important for customer service but read-only. |
| Modernization Risk | **4** | Same CICS browse pagination pattern as COCRDLIC. Custom page state in COMMAREA. |
| **Composite** | **3.25** | |

**Recommendation:** Convert to paginated REST API. Reuse pagination pattern developed for Card List (COCRDLIC).

---

### Rank #10 — COSGN00C (Signon — Online)

| Dimension | Score | Evidence |
|-----------|------:|---------|
| Code Complexity | **2** | 261 lines — simple and linear. Read USRSEC, compare password, route to menu. |
| Data Coupling | **2** | Reads 1 file (USRSEC). Sets COMMAREA fields for session. |
| Business Criticality | **5** | Authentication gateway — all access flows through this. Security-critical. Plaintext password storage is a major vulnerability. |
| Modernization Risk | **3** | Simple CICS READ + XCTL. Main risk is replacing plaintext auth with proper security (Spring Security, BCrypt, JWT). |
| **Composite** | **3.00** | |

**Recommendation:** Replace with Spring Security + BCrypt password hashing + JWT tokens. Critical security improvement. Should be one of the first modules migrated to eliminate plaintext password risk.

---

## Summary Ranking Table

| Rank | Module | Lines | Type | Composite Score | Primary Risk Factor |
|-----:|--------|------:|------|----------------:|---------------------|
| 1 | **COACTUPC** | 4,237 | Online | **5.00** | Extreme complexity, 4-file updates, 39 screen macros |
| 2 | **CBTRN02C** | 731 | Batch | **4.50** | 6-file posting engine, financial integrity |
| 3 | **COCRDLIC** | 1,460 | Online | **4.25** | Complex CICS browse pagination |
| 4 | **COCRDUPC** | 1,560 | Online | **4.00** | Card update with multi-file I/O |
| 5 | **COACTVWC** | 942 | Online | **3.80** | 5-entity data assembly, high coupling |
| 6 | **CBSTM03A/B** | 1,154 | Batch | **4.00** | ALTER statement, dual-output generation |
| 7 | **CBACT04C** | 652 | Batch | **3.95** | Interest calculation, decimal precision |
| 8 | **COTRN02C** | 783 | Online | **3.50** | Transaction creation, ID generation |
| 9 | **COTRN00C** | 699 | Online | **3.25** | CICS browse pagination |
| 10 | **COSGN00C** | 261 | Online | **3.00** | Plaintext auth, security gateway |

---

## Recommended Migration Waves

### Wave 1 — Quick Wins & Security (Weeks 1-4)
| Module | Rationale |
|--------|-----------|
| COSGN00C | Eliminate plaintext passwords. Simple program, high security value. |
| COACTVWC | Read-only pattern, proves out multi-entity JOIN conversion. |
| COTRN01C | Simple read-only transaction view. Low risk. |

### Wave 2 — Core CRUD (Weeks 5-10)
| Module | Rationale |
|--------|-----------|
| COCRDLIC + COCRDSLC | Establish paginated list + detail pattern for reuse. |
| COCRDUPC | First write operation; validates update pattern. |
| COTRN00C + COTRN02C | Reuse list pattern from cards; test transaction writes. |
| COUSRxxC (00-03) | Simple CRUD, isolated to USRSEC file. |

### Wave 3 — Complex Business Logic (Weeks 11-18)
| Module | Rationale |
|--------|-----------|
| COACTUPC | Largest, most complex. Decompose into 3 services. Needs Wave 2 patterns. |
| COBIL00C | Write-heavy with balance updates. Depends on account service. |
| CORPT00C | Batch bridge — online-to-batch integration pattern. |

### Wave 4 — Batch Processing (Weeks 19-26)
| Module | Rationale |
|--------|-----------|
| CBTRN02C | Core posting engine. Spring Batch conversion with reconciliation. |
| CBACT04C | Interest calculation. Requires BigDecimal precision testing. |
| CBSTM03A/B | Statement generation. Rewrite with templates. |
| CBTRN03C | Report generation. Follows from CBTRN02C patterns. |
| CBEXPORT/CBIMPORT | Data migration utilities. May not need conversion if data moves to RDBMS. |

---

## Risk Mitigation Strategies

| Risk | Mitigation |
|------|-----------|
| Decimal precision drift | Use Java `BigDecimal` exclusively. Run parallel COBOL/Java for 3 billing cycles. Compare output to 2 decimal places. |
| CICS state management | Map COMMAREA to HTTP session or JWT claims. Document every field's lifecycle. |
| VSAM browse pagination | Replace with SQL cursor-based pagination. Test with production-scale data volumes. |
| ALTER statement (CBSTM03A) | Rewrite as strategy pattern or command dispatch. Do not attempt line-by-line conversion. |
| Plaintext passwords | Migrate to BCrypt hash on first wave. Provide password reset flow for all users. |
| Multi-file atomicity | Use database transactions (BEGIN/COMMIT/ROLLBACK) to replace VSAM multi-file updates. |
| Screen attribute manipulation (CSSETATY) | Replace with CSS classes + form validation framework. No COBOL equivalent needed. |
