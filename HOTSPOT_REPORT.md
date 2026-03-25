# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> Modules ranked by composite score of code complexity, data risk, dependency fan-out, and business impact.

---

## Scoring Methodology

Each module is scored on four dimensions (1–5 scale each, 5 = highest risk/priority):

| Dimension | What It Measures | Scoring Criteria |
|-----------|-----------------|------------------|
| **Complexity** | Code size, cyclomatic complexity, nesting depth, EVALUATE/IF branches, REDEFINES, COPY count | 5 = >2000 LOC or >20 copybooks; 1 = <200 LOC, simple logic |
| **Data Risk** | Number of VSAM files touched, financial calculations, PII handling, update operations | 5 = Multi-file write with financial calculations; 1 = Read-only, non-financial |
| **Dependency Fan-out** | Programs called/calling this module, copybooks included, JCL jobs depending on it | 5 = Central hub (>5 callers/callees); 1 = Leaf node, standalone |
| **Business Impact** | Revenue criticality, regulatory exposure, user-facing importance, failure blast radius | 5 = Payment/balance-affecting, regulatory; 1 = Utility, non-critical |

**Composite Score** = Complexity + Data Risk + Dependency + Business Impact (max 20)

---

## Top 10 Hotspot Rankings

### 🔴 #1 — COACTUPC (Account Update) — Score: 19/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **5** | 4,237 lines — largest program in the entire codebase. 40+ COPY CSSETATY (field-level attribute setting), extensive date validation (DOB, open date, expiry, reissue), SSN validation, phone number parsing, signed decimal editing. Deep EVALUATE nesting with 30+ field-level validation branches. |
| Data Risk | **5** | Writes to ACCTDAT (balances, limits, status), reads CARDAIX, CXACAIX, CUSTDAT. Modifies financial fields: credit limit, cash advance limit, current balance, cycle credits/debits. Handles PII: SSN, DOB. |
| Dependency | **4** | Includes 15+ unique copybooks (most of any program). Called from COMEN01C menu. References CVACT01Y, CVACT03Y, CVCUS01Y, CSLKPCDY, CSSETATY, CSUTLDWY, CSUTLDPY. Uses CSSTRPFY utility. |
| Business Impact | **5** | Core account maintenance — any bug here directly corrupts account balances and credit limits. Regulatory exposure (credit limit changes, PII updates). |

**Modernization Risk:** VERY HIGH — Requires exhaustive test coverage for all 30+ field validations. Consider decomposing into AccountUpdateService + AccountValidationService + CustomerValidationService.

**Recommended Approach:** Strangler fig pattern. Extract field validation into a shared validation library first, then migrate the account update logic.

---

### 🔴 #2 — CBTRN02C (Transaction Posting) — Score: 18/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 731 lines. Reads daily transactions, validates against cross-reference, posts to master, updates account balances. Multi-file coordination with error handling for each file operation. |
| Data Risk | **5** | Writes to TRANSACT and ACCTDAT simultaneously. Updates account balances during posting. Reads DALYTRAN, XREFFILE. Financial integrity — double-entry-style balance updates. |
| Dependency | **5** | Central to nightly batch cycle (POSTTRAN JCL). Feeds INTCALC, COMBTRAN, CREASTMT downstream. Failure halts entire batch processing chain. |
| Business Impact | **4** | Transaction posting is the financial heartbeat — all daily card transactions flow through this program. Incorrect posting = incorrect balances = regulatory findings. |

**Modernization Risk:** VERY HIGH — Must preserve exact decimal arithmetic (COMP-3 packed decimal). Requires idempotent design for retry scenarios. Map to Spring Batch with chunk-oriented processing and transaction rollback.

**Recommended Approach:** Migrate to Spring Batch job with database transactions. Implement compensating transactions for failure recovery.

---

### 🔴 #3 — CBACT04C (Interest Calculation) — Score: 17/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 652 lines. Complex financial arithmetic — interest rate lookup by account group + transaction type + category. Rate application with rounding rules. Multi-table join logic (ACCTDAT + TCATBALF + DISCGRP). |
| Data Risk | **5** | Writes updated balances to ACCTDAT. Reads rate tables (TCATBALF, DISCGRP). Financial precision critical — COMP-3 packed decimal arithmetic with implicit decimal points. |
| Dependency | **4** | Called by INTCALC JCL job. Depends on TCATBALF and DISCGRP reference data. Downstream: updated balances feed statement generation. |
| Business Impact | **4** | Interest calculation directly affects customer billing. Rounding errors compound across millions of accounts. Regulatory requirement for accurate interest disclosure. |

**Modernization Risk:** HIGH — Financial calculation precision is critical. Must validate Java `BigDecimal` rounding matches COBOL `COMP-3` behavior exactly. Comprehensive parallel-run testing required.

**Recommended Approach:** Implement as a dedicated InterestCalculationService with `BigDecimal(MathContext.DECIMAL128)`. Run parallel with mainframe for minimum 3 billing cycles before cutover.

---

### 🟠 #4 — COCRDLIC (Credit Card List) — Score: 15/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 1,460 lines. Paginated browsing with STARTBR/READNEXT/READPREV/ENDBR cycle. 7-row display array with selection handling (S=View, U=Update). Forward/backward page tracking via COMMAREA. Complex context management. |
| Data Risk | **3** | Read-only browsing of CARDDAT and CARDAIX. No writes, but displays sensitive card numbers. |
| Dependency | **4** | Hub for card management — navigates to COCRDSLC (detail) and COCRDUPC (update). Called from both COMEN01C and COACTVWC. |
| Business Impact | **4** | Primary card management interface. High user traffic. Pagination bugs = cards invisible to operators. |

**Modernization Risk:** MEDIUM-HIGH — VSAM browse semantics (STARTBR/READNEXT with positioning) don't map directly to SQL. Requires careful pagination design with keyset pagination (not OFFSET).

**Recommended Approach:** Map to Spring Data JPA with `Pageable` / keyset pagination. Replace COMMAREA-based page state with stateless REST query parameters.

---

### 🟠 #5 — COCRDUPC (Credit Card Update) — Score: 15/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 1,560 lines. Field-level validation, card status changes, expiration date management. Similar pattern to COACTUPC but for card records. Uses CSSTRPFY for data formatting. |
| Data Risk | **4** | Writes to CARDDAT (card status, embossed name, expiry). Reads CARDAIX. Handles sensitive card data (card number, CVV, expiry). PCI-DSS implications. |
| Dependency | **3** | Called from COCRDLIC. Returns to card list or menu. Uses CVCRD01Y work area, CVACT02Y record layout. |
| Business Impact | **4** | Card status changes (active/inactive) affect real-time authorization. Incorrect updates = blocked/unblocked cards. PCI-DSS compliance scope. |

**Modernization Risk:** MEDIUM-HIGH — PCI-DSS requirements for card data handling must be maintained. Card number masking, audit logging, and encryption-at-rest required in modernized system.

**Recommended Approach:** Implement with Spring Security + field-level encryption. Add audit trail for all card status changes.

---

### 🟠 #6 — CBSTM03A (Statement Generation — Main) — Score: 15/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 924 lines. Multi-file join across XREFFILE, CUSTFILE, ACCTFILE, TRANSACT. Generates both plain-text and HTML output. Calls CBSTM03B subroutine 13 times for file I/O. Complex formatting and report layout logic. |
| Data Risk | **4** | Reads 4 VSAM files. Writes STMTFILE (PS) and HTMLFILE. Contains customer PII (name, address) and financial data (balances, transactions). |
| Dependency | **3** | Called by CREASTMT JCL. Calls CBSTM03B subroutine. Output feeds TXT2PDF1 for PDF generation. |
| Business Impact | **4** | Customer-facing statements — regulatory requirement (Truth in Lending Act). Errors visible to customers. Statement accuracy directly affects customer trust. |

**Modernization Risk:** MEDIUM-HIGH — Report formatting logic is tightly coupled to 80-column/132-column mainframe print formats. HTML generation is rudimentary. Modern replacement should use a template engine (Thymeleaf/Jasper).

**Recommended Approach:** Replace with Spring Batch job using Jasper Reports or Thymeleaf templates. Maintain dual output (PDF + HTML).

---

### 🟡 #7 — COACTVWC (Account View) — Score: 14/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 942 lines. Multi-file read (ACCTDAT, CARDAIX, CXACAIX, CUSTDAT). Complex error handling with 88-level condition names. PERFORM THRU patterns with GO TO exits. |
| Data Risk | **3** | Read-only — displays account and customer data but does not modify. Exposes financial balances and customer PII. |
| Dependency | **3** | Called from COMEN01C menu. Can navigate to COCRDLIC, COCRDSLC, COCRDUPC. Uses 14 copybooks. |
| Business Impact | **4** | Primary account inquiry screen — high-frequency use by operators. Must display accurate real-time data. |

**Modernization Risk:** MEDIUM — Read-only simplifies migration. Main challenge is multi-file join logic and VSAM alternate index lookups.

**Recommended Approach:** Map to AccountViewController calling AccountService.findById() with JPA eager/lazy loading for related entities.

---

### 🟡 #8 — COTRN02C (Transaction Add) — Score: 14/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 783 lines. Date validation via CSUTLDTC calls. Cross-reference lookup (CXACAIX, CCXREF). Transaction ID generation via READPREV on TRANSACT file. Multiple file accesses. |
| Data Risk | **4** | Writes new records to TRANSACT. Reads ACCTDAT, CCXREF, CXACAIX for validation. Generates unique transaction IDs. Financial amount entry. |
| Dependency | **3** | Called from COTRN00C (transaction list). Calls CSUTLDTC utility. Writes to TRANSACT file consumed by batch cycle. |
| Business Impact | **4** | Manual transaction entry — used for adjustments, corrections, and manual postings. Incorrect entries directly affect balances. |

**Modernization Risk:** MEDIUM — Transaction ID generation (READPREV to get last ID) needs replacement with database sequence. Date validation logic is reusable.

**Recommended Approach:** Map to TransactionController + TransactionService. Use database sequences for ID generation. Reuse date validation as shared utility.

---

### 🟡 #9 — CBTRN03C (Transaction Report) — Score: 13/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 649 lines. Multi-file join (TRANFILE, CARDXREF, TRANTYPE, CUSTFILE). Report formatting with CVTRA07Y headers/detail lines. Date range filtering. Sequential file I/O with status checking. |
| Data Risk | **3** | Read-only across 4 files. Writes report output to REPTFILE. No financial updates but displays transaction amounts. |
| Dependency | **3** | Called by TRANREPT JCL. Reads TRTEFMT (format file), TRANTYPE, CARDXREF, CUSTFILE. Depends on TRANBKP completing first. |
| Business Impact | **3** | Daily transaction reports for operational review. Important for reconciliation but not customer-facing. |

**Modernization Risk:** MEDIUM — Report logic can be replaced with SQL query + reporting framework. Main challenge is matching exact report layout if downstream systems parse the fixed-format output.

**Recommended Approach:** Replace with SQL-based report query. Use Spring Batch for report generation. Consider modern BI tools for ad-hoc reporting.

---

### 🟡 #10 — CBEXPORT / CBIMPORT (Data Export/Import) — Score: 13/20

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | CBEXPORT: 582 lines, CBIMPORT: 487 lines. Reads/writes all 5 core VSAM files. Uses CVEXPORT composite record with REDEFINES for polymorphic record types. Sequence numbering, timestamp generation. |
| Data Risk | **4** | Touches ALL master files (Account, Card, Customer, Cross-ref, Transaction). Full data extract/load — corruption affects entire database. Export contains all PII and financial data. |
| Dependency | **3** | Standalone but critical for data migration. CBEXPORT reads all VSAM; CBIMPORT writes all VSAM. Used for disaster recovery and environment refresh. |
| Business Impact | **3** | Not part of daily operations but critical for: (a) initial data migration to Java, (b) disaster recovery, (c) test data provisioning. |

**Modernization Risk:** MEDIUM — The export format (CVEXPORT copybook) serves as a de-facto data migration interface. May be the primary tool for populating the new relational database during cutover.

**Recommended Approach:** Use CBEXPORT output as the migration source. Build a Java import utility that reads the sequential export file and populates JPA entities. Validate record counts and checksums.

---

## Summary Ranking Table

| Rank | Program | LOC | Complexity | Data Risk | Dependency | Business Impact | **Total** | Migration Phase |
|------|---------|-----|:----------:|:---------:|:----------:|:---------------:|:---------:|:---------------:|
| 1 | **COACTUPC** | 4,237 | 5 | 5 | 4 | 5 | **19** | Phase 3 (Complex) |
| 2 | **CBTRN02C** | 731 | 4 | 5 | 5 | 4 | **18** | Phase 3 (Complex) |
| 3 | **CBACT04C** | 652 | 4 | 5 | 4 | 4 | **17** | Phase 3 (Complex) |
| 4 | **COCRDLIC** | 1,460 | 4 | 3 | 4 | 4 | **15** | Phase 2 (Medium) |
| 5 | **COCRDUPC** | 1,560 | 4 | 4 | 3 | 4 | **15** | Phase 2 (Medium) |
| 6 | **CBSTM03A** | 924 | 4 | 4 | 3 | 4 | **15** | Phase 2 (Medium) |
| 7 | **COACTVWC** | 942 | 4 | 3 | 3 | 4 | **14** | Phase 2 (Medium) |
| 8 | **COTRN02C** | 783 | 3 | 4 | 3 | 4 | **14** | Phase 2 (Medium) |
| 9 | **CBTRN03C** | 649 | 4 | 3 | 3 | 3 | **13** | Phase 1 (Simple) |
| 10 | **CBEXPORT/IMPORT** | 1,069 | 3 | 4 | 3 | 3 | **13** | Phase 0 (Foundation) |

---

## Recommended Migration Phases

### Phase 0 — Foundation (Weeks 1–2)
- **CBEXPORT/CBIMPORT** → Build data migration pipeline
- **CSUTLDTC** → Date utility → shared Java utility library
- **COCOM01Y** → COMMAREA → shared DTO/context objects
- **All copybooks** → JPA entity classes
- Database schema creation from Data Dictionary

### Phase 1 — Low-Risk Batch (Weeks 3–4)
- **CBACT01C/02C/03C, CBCUS01C** → Simple file readers → Spring Batch readers
- **CBTRN01C** → Transaction file reader
- **CBTRN03C** → Transaction report → SQL query + report template
- **COUSR00C–03C** → User CRUD → Spring Security UserDetailsService

### Phase 2 — Core Online (Weeks 5–8)
- **COSGN00C** → Spring Security authentication
- **COMEN01C, COADM01C** → Navigation → React/Angular routing
- **COACTVWC** → Account view → REST GET endpoint
- **COCRDLIC, COCRDSLC** → Card list/detail → paginated REST endpoints
- **COCRDUPC** → Card update → REST PUT endpoint
- **COTRN00C, COTRN01C** → Transaction list/view → REST endpoints
- **COTRN02C** → Transaction add → REST POST endpoint
- **CORPT00C, COBIL00C** → Reports, Bill pay → REST endpoints
- **CBSTM03A/B** → Statement generation → Spring Batch + template engine

### Phase 3 — Complex Financial (Weeks 9–12)
- **COACTUPC** → Account update (decompose into services)
- **CBTRN02C** → Transaction posting → Spring Batch with DB transactions
- **CBACT04C** → Interest calculation → dedicated financial service
- Parallel run validation against mainframe

### Phase 4 — Optional Modules (Weeks 13+)
- Authorization module (IMS/DB2/MQ) → Spring Boot + message broker
- Transaction Type DB2 module → JPA CRUD
- VSAM-MQ module → REST API replacement

---

## Key Risk Mitigations

1. **Financial Precision**: Use `BigDecimal` with explicit scale/rounding mode for ALL monetary calculations. Run parallel with mainframe for 3+ billing cycles.
2. **PII Handling**: Implement encryption-at-rest (AES-256) for SSN, card numbers, DOB. Add field-level access controls.
3. **VSAM Browse → SQL Pagination**: Replace STARTBR/READNEXT with keyset pagination (`WHERE id > :lastId ORDER BY id LIMIT :pageSize`). Do NOT use OFFSET pagination.
4. **COMMAREA → Stateless REST**: The COMMAREA pattern (inter-program state) maps to either: (a) JWT claims for session context, or (b) query parameters for screen navigation state.
5. **Batch Window**: Maintain batch job sequencing (CLOSEFIL → process → OPENFIL pattern). In Java, use Spring Batch job dependencies or Airflow DAGs.
6. **IDCAMS → DDL**: All DEFINE CLUSTER JCL translates to SQL CREATE TABLE + CREATE INDEX statements. Alternate indexes become secondary indexes or foreign keys.
7. **Testing**: Prioritize programs by this hotspot ranking. Highest-scoring modules need the most test cases. Use mainframe export data (`app/data/ASCII/`) for integration test fixtures.
