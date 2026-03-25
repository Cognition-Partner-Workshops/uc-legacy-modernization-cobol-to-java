# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Methodology:** Weighted scoring across code complexity, coupling, business impact, and migration risk

---

## Scoring Methodology

Each module is scored on four dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|---|---|---|
| **Code Complexity** | 30% | Lines of code, PERFORM count, EVALUATE branches, nested logic depth |
| **Coupling** | 25% | Number of copybook dependencies, VSAM files accessed, programs called/calling |
| **Business Impact** | 25% | Criticality to business operations, financial calculations, data integrity |
| **Migration Risk** | 20% | CICS dependencies, VSAM I/O patterns, assembler calls, unique patterns |

**Composite Score** = (Complexity × 0.30) + (Coupling × 0.25) + (Business Impact × 0.25) + (Migration Risk × 0.20)

---

## Top 10 Hotspot Modules

### Rank #1: COACTUPC.cbl — Account Update

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **4,236** (largest program by far) | |
| PERFORM Statements | 64 | |
| EVALUATE Statements | 10 | |
| Copybooks Used | 18 (most of any program) | |
| VSAM Files Accessed | 5 (ACCTDATA, CARDXREF, CARDDATA, CUSTDATA, USRSEC) | |
| CICS Commands | 17 (HANDLE ABEND, READ ×5, REWRITE, SEND, RECEIVE, XCTL) | |
| BMS Map | COACTUP.bms | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **10** | 4,236 LOC is 3× the next largest online program. Uses COPY REPLACING for dynamic attribute setting. Deep nested EVALUATE logic for field validation. |
| Coupling | **10** | 18 copybook dependencies. Reads 5 VSAM files. Uses CSSETATY with COPY REPLACING (unique pattern). References CSUTLDWY, CSLKPCDY (exclusive copybooks). |
| Business Impact | **9** | Account updates directly modify credit limits, account status, and balances. Incorrect updates = financial exposure. |
| Migration Risk | **9** | COPY REPLACING pattern requires special handling. HANDLE ABEND with custom recovery. Multiple READ/REWRITE across files in pseudo-transaction. |
| **Composite Score** | **9.55** | |

**Migration Recommendation:** Decompose into multiple service methods (AccountValidation, AccountUpdate, CreditLimitChange). Convert COPY REPLACING to Java inheritance/composition. Implement proper database transactions for multi-file updates.

---

### Rank #2: CBTRN02C.cbl — Transaction Posting (Batch)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **731** | |
| PERFORM Statements | 61 | |
| EVALUATE Statements | 0 | |
| Copybooks Used | 5 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y) | |
| VSAM Files Accessed | 6 (DALYTRAN, TRANSACT, XREF, DALYREJS, ACCTDATA, TCATBALF) | |
| File I/O Operations | 11 (highest of any batch program) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **8** | 731 LOC with 61 PERFORMs (highest density). Complex multi-file update logic with reject handling. |
| Coupling | **9** | Reads/writes 6 different files. Core of the batch cycle -- every downstream job depends on its output. |
| Business Impact | **10** | **Core transaction posting engine.** Posts daily transactions to master file, updates account balances, and category balances. Any bug = financial data corruption. |
| Migration Risk | **8** | Multi-file sequential/VSAM I/O must become database transactions. Reject file handling needs error queue equivalent. GDG output for rejects. |
| **Composite Score** | **8.75** | |

**Migration Recommendation:** Convert to Spring Batch with chunk-oriented processing. Use database transactions for atomicity across account/transaction/balance updates. Implement dead-letter queue for rejected transactions.

---

### Rank #3: COCRDLIC.cbl — Card List

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **1,459** | |
| PERFORM Statements | 34 | |
| EVALUATE Statements | 18 (highest of any program) | |
| Copybooks Used | 13 | |
| VSAM Files Accessed | 2 (CARDDATA, CARDXREF via STARTBR/READNEXT/READPREV) | |
| CICS Commands | 18 (XCTL ×3, STARTBR, READNEXT, READPREV, ENDBR, SEND, RECEIVE) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **9** | 1,459 LOC with 18 EVALUATEs. Complex bidirectional browse logic with pagination. Role-based filtering (admin sees all, user sees own). |
| Coupling | **8** | 13 copybooks. 3 outbound XCTLs to card detail/update programs. |
| Business Impact | **7** | Primary card inquiry screen. Browse errors could expose wrong cards to wrong users (security). |
| Migration Risk | **8** | VSAM browse (STARTBR/READNEXT/READPREV/ENDBR) pattern is hard to map to SQL pagination. Role-based filtering is inline. |
| **Composite Score** | **8.05** | |

**Migration Recommendation:** Replace VSAM browse with SQL pagination (LIMIT/OFFSET or keyset). Extract role-based filtering to a security service. Convert to paginated REST endpoint + frontend table component.

---

### Rank #4: COCRDUPC.cbl — Card Update

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **1,560** | |
| PERFORM Statements | 26 | |
| EVALUATE Statements | 16 | |
| Copybooks Used | 15 | |
| VSAM Files Accessed | 3 (CARDDATA, CARDXREF, CUSTDATA) | |
| CICS Commands | 12 (HANDLE ABEND, READ ×2, REWRITE, SEND, RECEIVE, ABEND) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **8** | 1,560 LOC with 16 EVALUATEs. Extensive field-level validation for card updates. |
| Coupling | **8** | 15 copybooks. Reads customer and xref data for cross-validation. |
| Business Impact | **8** | Card status changes (activate/deactivate) directly affect cardholder ability to transact. |
| Migration Risk | **8** | HANDLE ABEND with ABEND recovery. Multi-file read pattern for validation before REWRITE. |
| **Composite Score** | **8.00** | |

**Migration Recommendation:** Extract validation rules into a CardValidationService. Use JPA entity with @Transactional for atomic card updates. Map HANDLE ABEND to try/catch with compensating actions.

---

### Rank #5: CBACT04C.cbl — Interest Calculation (Batch)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **652** | |
| PERFORM Statements | 56 | |
| EVALUATE Statements | 0 | |
| Copybooks Used | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y) | |
| VSAM Files Accessed | 5 (TCATBALF, CARDXREF+AIX, ACCTDATA, DISCGRP, SYSTRAN) | |
| File I/O Operations | 9 | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **7** | 652 LOC with 56 PERFORMs (very high density). Multi-step calculation: lookup rate by group → compute per category → write interest transactions. |
| Coupling | **8** | Reads 5 VSAM files including alternate index path. Writes to GDG. |
| Business Impact | **10** | **Financial calculation engine.** Computes interest charges on all accounts. Errors = billing disputes, regulatory issues, revenue loss. |
| Migration Risk | **7** | VSAM alternate index path access. Date parameter from JCL PARM. GDG output pattern. |
| **Composite Score** | **8.00** | |

**Migration Recommendation:** Convert to Spring Batch with precise BigDecimal arithmetic. Implement comprehensive audit trail. Extract rate lookup to a separate service. Add reconciliation step to verify interest totals.

---

### Rank #6: CBSTM03A.CBL — Statement Generation (Batch)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **924** | |
| PERFORM Statements | 29 | |
| EVALUATE Statements | 5 | |
| Copybooks Used | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y) | |
| VSAM Files Accessed | 4 (TRXFL, XREF, ACCTDATA, CUSTDATA) | |
| CALL Statements | 14 (calls CBSTM03B 13 times + CEE3ABD) | |
| File I/O Operations | 101 (highest of any program) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **8** | 924 LOC. Dual output format (text + HTML). 101 file I/O operations. Complex called-subroutine pattern with CBSTM03B. |
| Coupling | **8** | Tightly coupled with CBSTM03B subroutine (13 calls). Reads 4 VSAM files. Produces 2 output files. |
| Business Impact | **8** | Customer-facing statements. Errors visible to cardholders. Regulatory requirement for accurate statements. |
| Migration Risk | **7** | CALL subroutine pattern maps to method calls. Dual output (text/HTML) needs template engine. Heavy file I/O. |
| **Composite Score** | **7.80** | |

**Migration Recommendation:** Use a template engine (Thymeleaf/FreeMarker) for HTML output. Convert subroutine to service class. Replace file I/O with database queries + streaming output.

---

### Rank #7: COACTVWC.cbl — Account View

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **941** | |
| PERFORM Statements | 21 | |
| EVALUATE Statements | 10 | |
| Copybooks Used | 15 | |
| VSAM Files Accessed | 4 (ACCTDATA, CARDDATA, CARDXREF, CUSTDATA) | |
| CICS Commands | 15 (HANDLE ABEND, READ ×3, SEND, RECEIVE, ABEND) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **7** | 941 LOC with 10 EVALUATEs. Multi-entity view assembly (account + cards + customer). HANDLE ABEND recovery. |
| Coupling | **8** | 15 copybooks. Reads 4 VSAM files to assemble a composite view. |
| Business Impact | **7** | Primary account inquiry -- used by every user session. Must show accurate data. |
| Migration Risk | **7** | Multi-file read assembly → SQL JOIN. HANDLE ABEND pattern. CSSTRPFY string processing. |
| **Composite Score** | **7.25** | |

**Migration Recommendation:** Convert to a single SQL JOIN query across accounts/cards/customers/xref tables. Create AccountDetailDTO. Map to REST GET endpoint.

---

### Rank #8: COTRN02C.cbl — Transaction Add (Online)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **783** | |
| PERFORM Statements | 61 (highest of any online program) | |
| EVALUATE Statements | 13 | |
| Copybooks Used | 10 | |
| VSAM Files Accessed | 3 (TRANSACT, ACCTDATA, CARDXREF) | |
| CALL Statements | 2 (CSUTLDTC for date validation) | |
| CICS Commands | 11 (READ ×2, STARTBR, READPREV, ENDBR, WRITE) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **7** | 783 LOC with 61 PERFORMs and 13 EVALUATEs. Multi-step validation + write. Generates unique transaction IDs via READPREV. |
| Coupling | **7** | 10 copybooks. Reads xref and account for validation. Calls date utility. |
| Business Impact | **8** | Creates financial transactions. Must validate card, account, and amount before write. Direct financial impact. |
| Migration Risk | **7** | Unique ID generation via VSAM READPREV (last key + 1) → needs sequence/UUID strategy. Date validation via CALL. |
| **Composite Score** | **7.25** | |

**Migration Recommendation:** Use database sequences or UUIDs for transaction IDs. Extract validation to TransactionValidationService. Implement @Transactional for atomic write.

---

### Rank #9: CBTRN03C.cbl — Transaction Report (Batch)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **649** | |
| PERFORM Statements | 72 (highest absolute PERFORM count) | |
| EVALUATE Statements | 4 | |
| Copybooks Used | 5 (CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y) | |
| VSAM Files Accessed | 4 (TRANSACT, XREF, TRANTYPE, TRANCATG) + DATE-PARMS | |
| File I/O Operations | 9 | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **7** | 649 LOC with 72 PERFORMs (highest density in codebase). Complex report formatting with page breaks, totals, subtotals. |
| Coupling | **7** | Reads 4 VSAM reference files + date parameter file. Uses CVTRA07Y report layout copybook. |
| Business Impact | **7** | Daily transaction report -- used for reconciliation and audit. Regulatory reporting dependency. |
| Migration Risk | **6** | Report generation maps well to JasperReports or similar. Date parameter file → configuration. SORT pre-step in JCL. |
| **Composite Score** | **6.85** | |

**Migration Recommendation:** Use JasperReports or Spring Batch FlatFileItemWriter. Replace SORT pre-step with SQL ORDER BY. Convert date parameters to application configuration.

---

### Rank #10: COTRN00C.cbl — Transaction List (Online)

| Metric | Value | Score |
|---|---|---|
| Lines of Code | **699** | |
| PERFORM Statements | 43 | |
| EVALUATE Statements | 8 | |
| Copybooks Used | 8 | |
| VSAM Files Accessed | 1 (TRANSACT via STARTBR/READNEXT/READPREV/ENDBR) | |
| CICS Commands | 11 (STARTBR, READNEXT, READPREV, ENDBR, SEND ×2, RECEIVE) | |

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **7** | 699 LOC with 43 PERFORMs and 8 EVALUATEs. Bidirectional browse with page up/down. |
| Coupling | **6** | 8 copybooks. Single VSAM file but complex browse pattern. Routes to COTRN01C (view) and COTRN02C (add). |
| Business Impact | **7** | Primary transaction inquiry screen. Used by all users for transaction lookup. |
| Migration Risk | **7** | VSAM browse pagination → SQL pagination. Complex STARTBR/READNEXT/READPREV state management. |
| **Composite Score** | **6.75** | |

**Migration Recommendation:** Replace VSAM browse with SQL pagination. Use keyset pagination for performance. Convert to paginated REST endpoint.

---

## Composite Score Summary

| Rank | Module | Type | LOC | Complexity | Coupling | Business Impact | Migration Risk | **Composite** |
|---|---|---|---|---|---|---|---|---|
| **1** | **COACTUPC** | Online | 4,236 | 10 | 10 | 9 | 9 | **9.55** |
| **2** | **CBTRN02C** | Batch | 731 | 8 | 9 | 10 | 8 | **8.75** |
| **3** | **COCRDLIC** | Online | 1,459 | 9 | 8 | 7 | 8 | **8.05** |
| **4** | **COCRDUPC** | Online | 1,560 | 8 | 8 | 8 | 8 | **8.00** |
| **5** | **CBACT04C** | Batch | 652 | 7 | 8 | 10 | 7 | **8.00** |
| **6** | **CBSTM03A** | Batch | 924 | 8 | 8 | 8 | 7 | **7.80** |
| **7** | **COACTVWC** | Online | 941 | 7 | 8 | 7 | 7 | **7.25** |
| **8** | **COTRN02C** | Online | 783 | 7 | 7 | 8 | 7 | **7.25** |
| **9** | **CBTRN03C** | Batch | 649 | 7 | 7 | 7 | 6 | **6.85** |
| **10** | **COTRN00C** | Online | 699 | 7 | 6 | 7 | 7 | **6.75** |

---

## Migration Priority Recommendations

### Phase 1 — Critical Path (Highest Risk + Impact)

| Module | Priority | Rationale |
|---|---|---|
| CBTRN02C (Transaction Posting) | **P0 -- Migrate First** | Core batch engine. All downstream processing depends on it. Financial data integrity. |
| CBACT04C (Interest Calculation) | **P0 -- Migrate First** | Financial calculation. Revenue-impacting. Regulatory compliance. |
| COACTUPC (Account Update) | **P1 -- Early** | Largest codebase. Needs decomposition. Drives many data changes. |

### Phase 2 — High Value

| Module | Priority | Rationale |
|---|---|---|
| COTRN02C (Transaction Add) | **P1** | Online transaction creation. Financial writes. |
| COBIL00C (Bill Payment) | **P1** | Payment processing. Financial writes to TRANSACT + ACCTDATA. |
| CBSTM03A/B (Statement Gen) | **P2** | Customer-facing output. Template conversion needed. |

### Phase 3 — Standard Migration

| Module | Priority | Rationale |
|---|---|---|
| COCRDLIC/COCRDSLC/COCRDUPC | **P2** | Card management CRUD. Well-structured. |
| COTRN00C/COTRN01C | **P2** | Transaction inquiry. Read-only patterns. |
| COACTVWC | **P2** | Account view. Read-only composite query. |

### Phase 4 — Lower Risk

| Module | Priority | Rationale |
|---|---|---|
| COUSR00C-03C | **P3** | User management CRUD. Simple patterns. Can use Spring Security. |
| COSGN00C | **P3** | Sign-on. Replace with Spring Security + OAuth2. |
| COMEN01C/COADM01C | **P3** | Menu navigation. Disappears in modern UI (SPA routing). |
| CBACT01C-03C, CBCUS01C | **P3** | Read/print utilities. May not be needed post-migration. |
| COBSWAIT, CSUTLDTC | **P3** | Utilities. Trivial to replace (Thread.sleep, java.time). |

---

## Key Risk Areas for Migration

| Risk Area | Description | Mitigation |
|---|---|---|
| **Plain-text Passwords** | USRSEC stores passwords unencrypted (CSUSR01Y) | Implement bcrypt/scrypt hashing from day one |
| **PII Exposure** | SSN, DOB, Gov ID stored unencrypted in CVCUS01Y | Encrypt at rest, mask in UI, implement column-level encryption |
| **PCI-DSS Compliance** | Card numbers (PAN) stored in clear across multiple files | Tokenization service, mask PAN in all displays |
| **No Transaction Boundaries** | CICS pseudo-conversational has no true ACID across files | Use database transactions (@Transactional) for all multi-table writes |
| **VSAM Browse → SQL** | STARTBR/READNEXT/READPREV patterns pervasive in list screens | Implement keyset pagination; avoid OFFSET for large tables |
| **COPY REPLACING** | COACTUPC uses COPY REPLACING for dynamic attribute setting | Requires custom code generation or Java reflection approach |
| **Assembler Dependencies** | MVSWAIT, COBDATFT called from COBOL | Replace with Java equivalents (Thread.sleep, DateTimeFormatter) |
| **GDG Output** | Several batch jobs write to Generation Data Groups | Replace with timestamped files or database audit tables |
| **TDQ Job Submission** | CORPT00C submits batch JCL via CICS TDQ | Replace with message queue (JMS/Kafka) or scheduled job trigger |
| **ID Generation** | COTRN02C generates IDs via VSAM READPREV (last key + 1) | Use database sequences or UUID generation |
