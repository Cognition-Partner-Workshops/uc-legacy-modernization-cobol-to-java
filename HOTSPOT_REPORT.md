# Hotspot Report — CardDemo COBOL Codebase

> **Purpose:** Identify the top 10 modules that present the highest modernization risk based on code complexity, dependency breadth, data sensitivity, and business impact. Use this to prioritize conversion sequencing.

---

## Table of Contents

1. [Scoring Methodology](#1-scoring-methodology)
2. [Top 10 Hotspot Modules](#2-top-10-hotspot-modules)
3. [Detailed Module Assessments](#3-detailed-module-assessments)
4. [Recommended Modernization Sequence](#4-recommended-modernization-sequence)
5. [Risk Mitigation Strategies](#5-risk-mitigation-strategies)

---

## 1. Scoring Methodology

Each module is scored across four dimensions on a 1–5 scale:

| Dimension | Weight | What It Measures |
|-----------|-------:|------------------|
| **Code Complexity** | 30% | LOC, cyclomatic indicators (IF/EVALUATE count), nesting depth, number of PERFORMs |
| **Dependency Breadth** | 25% | Number of copybooks, VSAM files accessed, CICS commands, external CALLs |
| **Business Impact** | 25% | Criticality to core business operations, data sensitivity (PII/financial), revenue impact |
| **Migration Risk** | 20% | Technology-specific challenges (CICS, BMS, VSAM I/O patterns), testability, side effects |

**Composite Score** = (Complexity × 0.30) + (Dependencies × 0.25) + (Business Impact × 0.25) + (Migration Risk × 0.20)

---

## 2. Top 10 Hotspot Modules

| Rank | Module | LOC | Complexity | Dependencies | Business Impact | Migration Risk | **Composite** |
|-----:|--------|----:|:----------:|:------------:|:---------------:|:--------------:|:-------------:|
| 1 | **COACTUPC.cbl** | 4,236 | 5 | 5 | 5 | 5 | **5.00** |
| 2 | **CBTRN02C.cbl** | 731 | 4 | 5 | 5 | 4 | **4.50** |
| 3 | **COCRDUPC.cbl** | 1,560 | 5 | 4 | 4 | 4 | **4.30** |
| 4 | **CBSTM03A.CBL** | 924 | 4 | 4 | 5 | 4 | **4.25** |
| 5 | **CBACT04C.cbl** | 652 | 4 | 4 | 5 | 3 | **4.05** |
| 6 | **COCRDLIC.cbl** | 1,459 | 5 | 4 | 3 | 4 | **4.05** |
| 7 | **COTRN02C.cbl** | 783 | 4 | 3 | 4 | 4 | **3.75** |
| 8 | **CBTRN03C.cbl** | 649 | 4 | 4 | 3 | 3 | **3.55** |
| 9 | **COBIL00C.cbl** | 572 | 3 | 3 | 5 | 3 | **3.50** |
| 10 | **COSGN00C.cbl** | 260 | 2 | 3 | 5 | 3 | **3.20** |

---

## 3. Detailed Module Assessments

### Rank 1: COACTUPC.cbl — Account Update

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 4,236 | Largest program by 3× — far exceeds typical COBOL module |
| IF Statements | 164 | Extremely high branching complexity |
| EVALUATE Statements | 20 | Multiple multi-way decision points |
| EXEC CICS Commands | 17 | Heavy CICS interaction (SEND, RECEIVE, READ, REWRITE) |
| COPY Statements | 56 | Highest copybook dependency count in the codebase |
| VSAM Files Accessed | 4 | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF |
| PERFORMs | 64 | Large number of internal subroutine calls |

**Why #1:** This is the single most complex program in the codebase. It handles account updates with extensive field-level validation (ZIP codes, state codes, phone numbers, dates), multi-file lookups, and complex screen interaction. It includes inline validation logic (via CSLKPCDY for US phone area codes and state codes) that alone adds ~1,300 lines of lookup tables.

**Modernization Challenges:**
- Must decompose into multiple Java service classes (validation, persistence, UI)
- Field-level validation logic embedded in COBOL needs extraction into a validation service
- BMS screen interaction maps to a complex multi-field form with conditional field editing
- Read-for-update VSAM pattern needs transactional equivalent (JPA `@Transactional`)

**Recommended Approach:** Decompose into AccountUpdateService, AccountValidationService, and AccountUpdateController. Extract lookup tables into reference data tables.

---

### Rank 2: CBTRN02C.cbl — Transaction Posting (Batch)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 731 | Moderate size but high business logic density |
| IF Statements | 48 | Complex validation and branching |
| PERFORM Statements | 61 | Many internal routines |
| VSAM Files Accessed | 6 | DAILYTRAN, TRANSACT, CARDXREF, DALYREJS, ACCTDATA, TCATBAL |
| Copybooks | 5 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |

**Why #2:** This is the core transaction posting engine — the heart of the batch cycle. Every daily transaction flows through this program. It validates transactions, looks up cross-references, posts to the master transaction file, updates account balances, maintains category balances, and writes rejects. A bug here means financial data corruption.

**Modernization Challenges:**
- Multi-file update atomicity (currently relies on VSAM I/O ordering, no explicit transactions)
- Reject handling logic needs equivalent error-handling strategy
- Must maintain exact numerical precision for financial calculations
- Performance critical: processes entire day's transactions in a single batch run

**Recommended Approach:** Spring Batch job with chunk-oriented processing. Use `@Transactional` for atomicity. Implement a dedicated TransactionPostingService with reject queue.

---

### Rank 3: COCRDUPC.cbl — Card Update (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 1,560 | Second-largest online program |
| IF Statements | 72 | High validation complexity |
| EVALUATE Statements | 16 | Multi-way branching |
| EXEC CICS Commands | 12 | SEND, RECEIVE, READ, REWRITE |
| COPY Statements | 15 | Moderate dependency set |

**Why #3:** Card update involves sensitive data (card numbers, CVV, expiration dates) with extensive validation. The program manages card status changes, name updates, and expiration date modifications with cross-reference lookups. Its high code complexity (5) combined with broad dependencies and PCI-DSS implications push it above the batch modules.

**Modernization Challenges:**
- PCI-DSS compliance considerations for card data handling
- Complex validation logic for card number format, dates, status transitions
- BMS screen interaction with conditional field protection/highlighting

**Recommended Approach:** CardUpdateService with validation decorators. Ensure PCI-DSS compliant data handling (tokenization, encryption at rest).

---

### Rank 4: CBSTM03A.CBL — Statement Generation (Batch)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 924 | Plus 230 in subroutine CBSTM03B |
| IF Statements | 15 | Moderate branching |
| EVALUATE Statements | 9 | Multi-way decision points |
| External CALLs | 4 | Calls CBSTM03B subroutine repeatedly |
| VSAM Files Read | 4 | TRANSACT, CARDXREF, ACCTDATA, CUSTDATA |
| Output Formats | 2 | HTML file + flat print file (PS) |

**Why #4:** Statement generation is a customer-facing output — errors are immediately visible to cardholders. The program reads across 4 VSAM files, aggregates transactions by card/account, and generates both HTML and print-format statements. The CALL-based subroutine pattern (CBSTM03A calls CBSTM03B for each I/O operation) adds conversion complexity.

**Modernization Challenges:**
- Multi-file join logic must be replicated as SQL JOINs or JPA queries
- HTML generation is inline COBOL string manipulation — needs template engine (Thymeleaf, etc.)
- CALL to CBSTM03B subroutine needs method extraction or separate service
- Statement formatting and page-break logic is character-position-based

**Recommended Approach:** Spring Batch reader/processor/writer pattern. Use JPA for data access, Thymeleaf or PDF library for statement rendering. Merge CBSTM03A/B into a single StatementGenerationService.

---

### Rank 5: CBACT04C.cbl — Interest Calculation (Batch)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 652 | Moderate size |
| IF Statements | 43 | Significant branching for rate logic |
| VSAM Files Accessed | 5 | TCATBAL, CARDXREF, DISCGRP, ACCTDATA, TRANSACT |
| Copybooks | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |

**Why #5:** Interest calculation directly affects customer billing. It reads category balances, looks up applicable discount/interest rates by account group, computes interest charges, and creates interest transactions. Financial precision is paramount — any rounding difference means compliance issues.

**Modernization Challenges:**
- Decimal precision: COBOL `PIC S9(10)V99` has implicit decimal — must use `BigDecimal` in Java
- Rate lookup logic across multiple reference files (DISCGRP by group+type+category)
- Updates both ACCTDATA and TRANSACT — requires transactional consistency
- Business rules for fee computation embedded in procedural code

**Recommended Approach:** Dedicated InterestCalculationService with BigDecimal arithmetic. Spring Batch job with database transactions. Extract rate lookup into a RateService.

---

### Rank 6: COCRDLIC.cbl — Card List (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 1,459 | Large for a list screen |
| IF Statements | 59 | Complex pagination logic |
| EVALUATE Statements | 18 | Multi-way branching |
| EXEC CICS Commands | 18 | STARTBR, READNEXT, READPREV, RESETBR |

**Why #6:** Paginated list display with VSAM browse operations (STARTBR/READNEXT/READPREV) that have no direct equivalent in RDBMS paging. The selection logic (choosing a card from the list to view/update) adds navigation complexity.

**Modernization Challenges:**
- VSAM browse → SQL pagination (OFFSET/LIMIT or keyset pagination)
- Row-selection pattern (user types 'S' next to a row) → HTML form/link click
- Page forward/backward with repositioning logic

**Recommended Approach:** Spring Data JPA with `Pageable`. REST endpoint returning page results. React/Angular table component with pagination.

---

### Rank 7: COTRN02C.cbl — Transaction Add (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 783 | Moderate size |
| EVALUATE Statements | 26 | Highest EVALUATE count in core codebase |
| EXEC CICS Commands | 11 | READ, WRITE, SEND, RECEIVE |
| VSAM Files | 3 | TRANSACT, CARDXREF, ACCTDATA |

**Why #7:** Adding a transaction involves multi-file validation (verify card exists via XREF, verify account active, check credit limits) before writing. The high EVALUATE count indicates complex conditional logic for transaction type handling.

**Modernization Challenges:**
- Multi-step validation across 3 files must become transactional
- Transaction ID generation logic needs equivalent (UUID or sequence)
- Amount validation with credit limit checking is business-critical

**Recommended Approach:** TransactionService with `@Transactional` method. Validation chain pattern. REST API endpoint.

---

### Rank 8: CBTRN03C.cbl — Transaction Report (Batch)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 649 | Moderate size |
| IF Statements | 38 | Report control break logic |
| EVALUATE Statements | 4 | Lookup-driven branching |
| PERFORM Statements | 72 | Highest PERFORM count — many small routines |
| VSAM Files Read | 6 | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, REPTFILE |

**Why #8:** Complex report with control breaks (page totals, account totals, grand totals), multiple reference file lookups, and date-range filtering. The 72 PERFORM statements indicate highly modular but intricate logic.

**Modernization Challenges:**
- Control-break reporting pattern has no direct Java equivalent
- Character-position-based print formatting (132-column report)
- Multiple running totals that must balance exactly
- Date range parameter handling

**Recommended Approach:** Spring Batch with custom ItemProcessor. JasperReports or programmatic PDF generation. Replace character-position formatting with template-based layout.

---

### Rank 9: COBIL00C.cbl — Bill Payment (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 572 | Moderate size |
| EVALUATE Statements | 18 | Complex payment flow |
| EXEC CICS Commands | 13 | Multiple file operations |
| VSAM Files | 3 | TRANSACT, ACCTDATA, CARDXREF |

**Why #9:** Bill payment is a financial transaction that modifies account balances. Despite moderate code size, the business impact is maximum — payment errors directly affect customers. Requires proper transaction isolation.

**Modernization Challenges:**
- Financial transaction atomicity (update account balance + create transaction record)
- Payment amount validation against account balance
- Idempotency for payment processing (prevent double payments)

**Recommended Approach:** BillPaymentService with idempotency key. `@Transactional` with proper isolation level. Event-driven architecture for payment confirmation.

---

### Rank 10: COSGN00C.cbl — Sign-on (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | 260 | Small but critical |
| EXEC CICS Commands | 10 | SEND, RECEIVE, READ, RETURN TRANSID |
| VSAM File | 1 | USRSEC |

**Why #10:** Though small, this is the authentication gateway — every user interaction starts here. The current implementation stores passwords in plaintext and uses simple string comparison. This is a security-critical module that must be completely re-architected.

**Modernization Challenges:**
- Plaintext password storage → must implement proper hashing (bcrypt/Argon2)
- Simple VSAM lookup → Spring Security with proper authentication provider
- Session establishment pattern (COMMAREA) → HTTP session or JWT tokens
- Role-based access (Admin/Regular) → Spring Security roles/authorities

**Recommended Approach:** Replace entirely with Spring Security. Implement password hashing, session management, and RBAC. This should be one of the first modules converted as other modules depend on it.

---

## 4. Recommended Modernization Sequence

Based on dependency analysis and risk assessment, the recommended conversion order is:

### Phase 1: Foundation (Weeks 1–3)
| Order | Module | Rationale |
|------:|--------|-----------|
| 1 | **Copybooks → Java POJOs/DTOs** | All programs depend on these data structures. Convert first. |
| 2 | **COCOM01Y → Session/Context DTO** | All online programs share this. Must exist before any UI program. |
| 3 | **COSGN00C → Spring Security** | Authentication gateway. All programs need auth context. |
| 4 | **Shared copybooks → Utility classes** | CSDAT01Y, CSMSG01Y, CSUTLDWY, CSLKPCDY → shared services. |

### Phase 2: Read-Only Screens (Weeks 4–6)
| Order | Module | Rationale |
|------:|--------|-----------|
| 5 | **COMEN01C + COADM01C → Navigation** | Menu/routing. Low risk, high visibility. |
| 6 | **COACTVWC → Account View** | Read-only — safe to test without data modification. |
| 7 | **COCRDSLC → Card Detail** | Read-only view. |
| 8 | **COTRN01C → Transaction View** | Read-only view. |
| 9 | **COUSR00C → User List** | Read-only list with pagination. |

### Phase 3: Write Operations (Weeks 7–10)
| Order | Module | Rationale |
|------:|--------|-----------|
| 10 | **COUSR01C-03C → User CRUD** | Low-risk write operations (admin only). Good test of write patterns. |
| 11 | **COTRN02C → Transaction Add** | First business-critical write. Requires validation framework. |
| 12 | **COCRDUPC → Card Update** | Sensitive data — validates PCI patterns. |
| 13 | **COACTUPC → Account Update** | Largest, most complex — convert last among online programs. |
| 14 | **COBIL00C → Bill Payment** | Financial transaction — needs thorough testing. |

### Phase 4: Batch Processing (Weeks 11–14)
| Order | Module | Rationale |
|------:|--------|-----------|
| 15 | **CBTRN02C → Transaction Posting** | Core batch engine. Must be correct before downstream. |
| 16 | **CBACT04C → Interest Calculation** | Financial computation — requires exact decimal matching. |
| 17 | **CBTRN03C → Transaction Report** | Report generation — can validate against COBOL output. |
| 18 | **CBSTM03A/B → Statement Generation** | Customer-facing output — needs parallel run testing. |
| 19 | **CBEXPORT/CBIMPORT → Data Migration** | Convert last — may be replaced by ETL tools. |

### Phase 5: Optional Modules (Weeks 15+)
| Order | Module | Rationale |
|------:|--------|-----------|
| 20 | **Authorization Module** | IMS/DB2/MQ — different technology stack, independent. |
| 21 | **Transaction Type DB2 Module** | DB2 embedded SQL — more straightforward with JPA. |
| 22 | **VSAM-MQ Module** | MQ integration — convert to JMS or event-driven architecture. |

---

## 5. Risk Mitigation Strategies

### 5.1 Data Integrity Risks

| Risk | Affected Modules | Mitigation |
|------|-----------------|------------|
| Decimal precision loss | CBTRN02C, CBACT04C, COBIL00C | Use `BigDecimal` exclusively for monetary fields. Run parallel comparison tests with COBOL output. |
| Transaction atomicity | CBTRN02C, COBIL00C, COACTUPC | Wrap multi-table updates in `@Transactional`. Test rollback scenarios. |
| Sign handling (signed packed decimal) | All financial fields | Map COBOL `PIC S9(n)V99` to `BigDecimal` with explicit sign handling. |
| Date format inconsistencies | All date fields | Centralize date parsing (X(10) → `LocalDate`). Handle both YYYY-MM-DD and MM/DD/YY formats found in CSDAT01Y/CSUTLDWY. |

### 5.2 Behavioral Equivalence Risks

| Risk | Affected Modules | Mitigation |
|------|-----------------|------------|
| Pagination behavior change | COCRDLIC, COTRN00C, COUSR00C | VSAM browse (STARTBR/READNEXT) behaves differently from SQL OFFSET. Use keyset pagination for exact equivalence. |
| Report formatting differences | CBTRN03C, CBSTM03A | Character-position layouts must match exactly. Run diff tests on output. |
| Validation logic gaps | COACTUPC, COCRDUPC | Extract all IF/EVALUATE validation rules into a test suite before converting. |
| COMMAREA state management | All online programs | Map COMMAREA fields to session attributes. Test multi-screen workflows end-to-end. |

### 5.3 Technology Migration Risks

| COBOL Technology | Java Equivalent | Risk Level | Notes |
|-----------------|----------------|:----------:|-------|
| CICS SEND/RECEIVE MAP | REST API + Web UI | Medium | Screen flow logic needs complete redesign |
| VSAM KSDS | JPA/Hibernate + RDBMS | Medium | Key structure and access patterns change |
| VSAM Browse (STARTBR) | SQL queries with pagination | Medium | Keyset pagination recommended |
| COBOL COPY | Java imports / shared DTOs | Low | Straightforward mapping |
| COBOL PERFORM | Java method calls | Low | Direct mapping |
| COBOL EVALUATE | Java switch / if-else | Low | Direct mapping |
| Batch JCL | Spring Batch + scheduler | Medium | Job sequencing and restart logic |
| BMS Maps | HTML/React forms | Medium | Field-level attributes need CSS equivalent |
| COMMAREA | HTTP session / JWT | Medium | State management paradigm shift |
| Plaintext passwords | bcrypt / Argon2 | **High** | Must not migrate plaintext — hash on import |
| EBCDIC data | UTF-8 | Low | One-time conversion during data migration |
| GDG (Generation Data Groups) | Timestamped files / versioned storage | Low | Use date-stamped directories or S3 versioning |
