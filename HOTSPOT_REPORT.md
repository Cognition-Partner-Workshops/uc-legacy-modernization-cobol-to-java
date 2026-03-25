# CardDemo Hotspot Report — Top 10 Modules for Modernization

> **Generated**: 2026-03-25 | **Methodology**: Weighted scoring across code complexity, data coupling, business risk, and modernization effort

---

## Scoring Methodology

Each module is scored on four dimensions (1–5 scale):

| Dimension            | Weight | What It Measures                                                          |
|----------------------|--------|---------------------------------------------------------------------------|
| **Code Complexity**  | 30%    | Lines of code, cyclomatic branching, nested EVALUATE/IF, COPY REPLACING   |
| **Data Coupling**    | 25%    | Number of VSAM files accessed, copybooks included, cross-program data flow|
| **Business Risk**    | 25%    | Financial impact, data integrity, audit/compliance sensitivity             |
| **Modernization Effort** | 20% | CICS dependencies, BMS screen mapping, embedded business rules, testing scope |

**Composite Score** = (Complexity × 0.30) + (Coupling × 0.25) + (Risk × 0.25) + (Effort × 0.20)

---

## Top 10 Hotspot Ranking

| Rank | Module     | Lines  | Type   | Composite | Complexity | Coupling | Risk | Effort | Priority     |
|------|------------|--------|--------|-----------|------------|----------|------|--------|--------------|
| 1    | COACTUPC   | 4,237  | Online | **4.80**  | 5          | 5        | 5    | 4      | 🔴 Critical  |
| 2    | CBTRN02C   | 731    | Batch  | **4.30**  | 4          | 5        | 5    | 3      | 🔴 Critical  |
| 3    | CBSTM03A   | 924    | Batch  | **4.05**  | 4          | 5        | 4    | 3      | 🔴 Critical  |
| 4    | CBACT04C   | 652    | Batch  | **4.05**  | 4          | 4        | 5    | 3      | 🔴 Critical  |
| 5    | COCRDLIC   | 1,460  | Online | **3.95**  | 4          | 3        | 4    | 5      | 🟠 High      |
| 6    | COCRDUPC   | 1,560  | Online | **3.75**  | 4          | 3        | 4    | 4      | 🟠 High      |
| 7    | COBIL00C   | 572    | Online | **3.75**  | 3          | 4        | 5    | 3      | 🟠 High      |
| 8    | COACTVWC   | 942    | Online | **3.55**  | 4          | 4        | 3    | 3      | 🟠 High      |
| 9    | COTRN02C   | 783    | Online | **3.50**  | 3          | 4        | 4    | 3      | 🟡 Medium    |
| 10   | CBTRN03C   | 649    | Batch  | **3.25**  | 3          | 4        | 3    | 3      | 🟡 Medium    |

---

## Detailed Module Assessments

### 1. COACTUPC — Account Update (Rank 1, Score 4.80)

**Why it's #1**: Largest program in the entire codebase at 4,237 lines. Handles full CRUD on both account and customer master records with extensive field-level validation, date editing, and BMS screen attribute manipulation.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 5/5   | 4,237 LOC; 15+ COPY REPLACING blocks for field attributes; SSN, phone, date validation; deeply nested EVALUATE logic; multi-step confirmation flow |
| Coupling    | 5/5   | Reads/writes ACCTDAT, CUSTDAT; reads CXACAIX; 16 copybooks included; uses CSSETATY, CSUTLDWY, CSLKPCDY utilities |
| Risk        | 5/5   | Modifies financial data (balances, credit limits); updates PII (SSN, address, DOB); data integrity critical |
| Effort      | 4/5   | Complex BMS map (COACTUP) with many protected/unprotected fields; field-level error highlighting; multi-entity update in single transaction |

**Modernization Recommendations**:
- Split into separate Account and Customer update services
- Extract validation logic into a shared validation framework
- Replace BMS attribute manipulation with form validation library
- Implement optimistic locking (currently uses CICS READ for UPDATE)
- Add audit logging for all financial field changes

**Key Risks**:
- Dual-entity update (account + customer) without distributed transaction
- Complex field-level attribute setting via COPY REPLACING is fragile
- No audit trail for financial changes

---

### 2. CBTRN02C — Transaction Posting Engine (Rank 2, Score 4.30)

**Why it's #2**: Core batch posting engine that processes all daily transactions. Updates multiple VSAM files in a single batch run with complex validation and rejection logic.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 731 LOC; multi-file cross-validation; rejection handling with reason codes; running balance updates |
| Coupling    | 5/5   | Reads DALYTRAN, XREFFILE, ACCTFILE, TCATBALF; writes TRANFILE, DALYREJS; 6 data copybooks |
| Risk        | 5/5   | Financial posting engine — errors propagate to account balances, statements, and interest calculations; feeds downstream jobs |
| Effort      | 3/5   | Pure batch (no CICS/BMS); sequential file processing maps well to Spring Batch; needs transaction boundary design |

**Modernization Recommendations**:
- Convert to Spring Batch with chunk-oriented processing
- Implement idempotent processing (re-runnable without duplicates)
- Add detailed rejection reporting with business-friendly error codes
- Design compensating transactions for failure recovery
- Critical: ensure account balance updates are atomic

**Key Risks**:
- Batch failure mid-run leaves partially posted transactions
- Rejection file (DALYREJS) is write-only — no retry mechanism
- Tight coupling to nightly batch window timing

---

### 3. CBSTM03A — Statement Generation (Rank 3, Score 4.05)

**Why it's #3**: Customer-facing statement generation that reads across 4 master files, sorts transactions by card, and produces both text and HTML output. Calls CBSTM03B subroutine 12+ times for formatting.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 924 LOC; multi-level break logic (customer → account → card → transaction); page/total accumulation; dual output format |
| Coupling    | 5/5   | Reads TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE; writes STMTFILE + HTMLFILE; CALL CBSTM03B (12 invocations) |
| Risk        | 4/5   | Customer-facing output; errors visible to end-users; regulatory compliance for statement accuracy |
| Effort      | 3/5   | Batch program with straightforward report logic; CBSTM03B subroutine simplifies conversion |

**Modernization Recommendations**:
- Replace with template-based statement engine (Thymeleaf / JasperReports)
- Separate data retrieval from formatting (current code mixes both)
- Add PDF generation capability (currently text + HTML only)
- Implement statement archive and retrieval service

**Key Risks**:
- Multi-level break logic is error-prone during conversion
- HTML generation is inline COBOL string concatenation
- CBSTM03B tight coupling via USING clause requires careful interface design

---

### 4. CBACT04C — Interest Calculation (Rank 4, Score 4.05)

**Why it's #4**: Financial calculation engine that computes interest on all accounts based on disclosure group rates and transaction category balances. Directly impacts account balances.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 652 LOC; multi-table lookup logic (disclosure group → rate); balance accumulation; date-based calculation |
| Coupling    | 4/5   | Reads TCATBALF, XREFFILE, ACCTFILE, DISCGRP; writes new transactions to TRANSACT; 5 data copybooks |
| Risk        | 5/5   | Financial calculation — directly affects customer balances; regulatory/audit requirements; rounding rules critical |
| Effort      | 3/5   | Pure batch; mathematical logic maps to Java BigDecimal; needs comprehensive test suite for financial accuracy |

**Modernization Recommendations**:
- Use BigDecimal exclusively for all monetary calculations
- Externalize interest rate rules (currently embedded in DISCGRP file lookups)
- Implement calculation audit trail with before/after balances
- Add configurable rounding mode (currently implicit COBOL ROUNDED)
- Build comprehensive test suite with known calculation results

**Key Risks**:
- COBOL implicit decimal arithmetic may differ from Java BigDecimal behavior
- Rounding discrepancies could accumulate across thousands of accounts
- No existing test baseline for calculation verification

---

### 5. COCRDLIC — Credit Card List (Rank 5, Score 3.95)

**Why it's #5**: Complex browse/pagination program with VSAM STARTBR/READNEXT/READPREV operations, dual selection modes (view/update), and multi-row screen handling.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 1,460 LOC; forward/backward VSAM browse; 7-row display array; selection validation; page state management |
| Coupling    | 3/5   | Reads CARDDAT (+ CARDAIX alternate index); XCTLs to COCRDSLC and COCRDUPC |
| Risk        | 4/5   | Primary card management entry point; incorrect paging could skip/duplicate records |
| Effort      | 5/5   | VSAM browse → paginated database query; BMS array → HTML table; PF7/PF8 paging → client-side pagination; complex state in COMMAREA |

**Modernization Recommendations**:
- Replace VSAM browse with JPA paginated queries (Spring Data Pageable)
- Convert 7-row fixed display to responsive table with variable page size
- Implement server-side cursor or keyset pagination (no offset)
- Extract selection logic (S=view, U=update) into REST routing

**Key Risks**:
- VSAM browse semantics (positional) differ from SQL OFFSET/LIMIT
- Page state stored in COMMAREA must be redesigned for stateless REST
- CARDAIX alternate index path requires join query in SQL

---

### 6. COCRDUPC — Credit Card Update (Rank 6, Score 3.75)

**Why it's #6**: Card update program with field-level validation, date editing, and VSAM REWRITE operations. Shares patterns with COACTUPC but focused on card entity.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 1,560 LOC; expiration/reissue date validation; CVV editing; field-level error attributes; CSSTRPFY PF-key mapping |
| Coupling    | 3/5   | Reads/writes CARDDAT; reads CUSTDAT; 14 copybooks |
| Risk        | 4/5   | Card data modification; CVV exposure; expiration date changes affect authorization |
| Effort      | 4/5   | Complex BMS map with date field groups; attribute manipulation; multi-step confirmation |

**Modernization Recommendations**:
- Extract date validation into shared utility (reuse across account/card updates)
- Mask CVV in display (show only last digit or asterisks)
- Implement change confirmation pattern (preview → confirm → save)
- Add card status transition rules (Active → Inactive → Closed)

**Key Risks**:
- CVV displayed in full on terminal screen (PCI compliance concern)
- No card status transition validation (any status to any status)

---

### 7. COBIL00C — Bill Payment (Rank 7, Score 3.75)

**Why it's #7**: Financial transaction program that reads account balance, creates a payment transaction, and zeroes out the balance. Accesses 3 VSAM files with mixed read/write operations.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 3/5   | 572 LOC; sequential ID generation (READPREV for last ID); balance zeroing; cross-reference validation |
| Coupling    | 4/5   | Reads/writes ACCTDAT; reads CXACAIX; reads/writes TRANSACT; 6 data copybooks |
| Risk        | 5/5   | Financial transaction creation; balance modification; must be atomic; audit-critical |
| Effort      | 3/5   | Moderate BMS screen; straightforward payment logic; needs transaction atomicity design |

**Modernization Recommendations**:
- Wrap payment in database transaction (account update + transaction insert must be atomic)
- Replace READPREV-based ID generation with sequence/UUID
- Add idempotency key to prevent duplicate payments
- Implement payment amount validation (currently pays full balance only)

**Key Risks**:
- Non-atomic update: REWRITE ACCTDAT and WRITE TRANSACT are separate CICS operations
- Sequential ID generation via READPREV is fragile under concurrency
- Full-balance-only payment is a functional limitation

---

### 8. COACTVWC — Account View (Rank 8, Score 3.55)

**Why it's #8**: Read-only account display that reads across 3 VSAM files. Template for the more complex account update program.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 4/5   | 942 LOC; multi-file lookup (CXACAIX → ACCTDAT → CUSTDAT); CSSTRPFY PF-key handling; abend handling |
| Coupling    | 4/5   | Reads ACCTDAT, CUSTDAT, CXACAIX; 14 copybooks; navigates to COCRDLIC, COCRDUPC |
| Risk        | 3/5   | Read-only — no data modification risk; but displays PII (SSN, address) |
| Effort      | 3/5   | Standard view pattern; BMS → HTML form (read-only); serves as reference for account display |

**Modernization Recommendations**:
- Convert to read-only REST endpoint returning AccountDTO
- Mask sensitive fields (SSN → `***-**-1234`)
- Reuse in account update screen (view then edit pattern)
- Add caching for frequently viewed accounts

**Key Risks**:
- PII displayed without masking
- Serves as the navigation hub to card list and card update — routing must be preserved

---

### 9. COTRN02C — Transaction Add (Rank 9, Score 3.50)

**Why it's #9**: Online transaction creation with date validation, cross-reference lookup, sequential ID generation, and VSAM write.

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 3/5   | 783 LOC; date validation via CALL CSUTLDTC; cross-reference lookup; ID generation via READPREV |
| Coupling    | 4/5   | Reads CXACAIX, CCXREF; reads/writes TRANSACT; calls CSUTLDTC; 7 copybooks |
| Risk        | 4/5   | Creates financial transactions; amount validation; merchant data capture |
| Effort      | 3/5   | Standard add form; BMS → HTML form; date validation to reuse |

**Modernization Recommendations**:
- Replace READPREV ID generation with UUID or database sequence
- Extract date validation to shared service
- Implement input sanitization for merchant data
- Add duplicate transaction detection

**Key Risks**:
- Sequential ID generation not concurrency-safe
- No duplicate detection — same transaction can be entered twice
- Date validation via external CALL needs to be internalized

---

### 10. CBTRN03C — Transaction Report (Rank 10, Score 3.25)

**Why it's #10**: Batch report generator that produces the daily transaction detail report with multi-level totals (page, account, grand total).

| Dimension   | Score | Rationale                                                              |
|-------------|-------|------------------------------------------------------------------------|
| Complexity  | 3/5   | 649 LOC; multi-level break/total logic; report formatting with headers/footers; 5 data copybooks |
| Coupling    | 4/5   | Reads TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM; writes TRANREPT |
| Risk        | 3/5   | Management/audit report; accuracy important but not customer-facing |
| Effort      | 3/5   | Classic report program; maps well to JasperReports or custom report builder |

**Modernization Recommendations**:
- Replace with reporting framework (JasperReports, Apache POI, or custom)
- Externalize report layout (currently embedded in COBOL MOVE statements and CVTRA07Y)
- Add report scheduling and distribution capability
- Support multiple output formats (PDF, CSV, Excel)

**Key Risks**:
- Multi-level break logic must be thoroughly tested during conversion
- Report formatting relies on fixed 133-character print lines
- Date parameter handling via file (DATEPARM) needs redesign

---

## Modernization Wave Planning

Based on the hotspot analysis, the recommended modernization waves are:

### Wave 1 — Foundation (Weeks 1–4)
> Convert shared infrastructure first

| Module     | Rationale                                                  |
|------------|------------------------------------------------------------|
| COCOM01Y   | COMMAREA → Session/Security context                        |
| CSUSR01Y   | User security → Spring Security + JPA entity               |
| CSUTLDTC   | Date utility → Java LocalDate utilities                    |
| COSGN00C   | Authentication → Spring Security login                     |
| COMEN01C   | Menu navigation → REST API routing                         |
| COADM01C   | Admin menu → Role-based routing                            |

### Wave 2 — Read Operations (Weeks 5–8)
> Convert view/list screens with no write risk

| Module     | Rationale                                                  |
|------------|------------------------------------------------------------|
| COACTVWC   | Account view (read-only, good starting template)           |
| COCRDSLC   | Card detail view                                           |
| COTRN00C   | Transaction list (pagination pattern reference)            |
| COTRN01C   | Transaction detail view                                    |
| COUSR00C   | User list (admin)                                          |

### Wave 3 — Write Operations (Weeks 9–14)
> Convert update/create screens with careful testing

| Module     | Rationale                                                  |
|------------|------------------------------------------------------------|
| COACTUPC   | Account update (highest complexity — needs most testing)   |
| COCRDUPC   | Card update                                                |
| COTRN02C   | Transaction add                                            |
| COBIL00C   | Bill payment (financial write — needs atomicity)           |
| COUSR01-03C| User CRUD (lower risk, good practice)                      |

### Wave 4 — Batch Processing (Weeks 15–20)
> Convert batch programs with Spring Batch

| Module     | Rationale                                                  |
|------------|------------------------------------------------------------|
| CBTRN02C   | Transaction posting (critical path)                        |
| CBACT04C   | Interest calculation (financial accuracy critical)         |
| CBSTM03A/B | Statement generation                                      |
| CBTRN03C   | Transaction reporting                                      |
| CBEXPORT/CBIMPORT | Data export/import                                  |
| CORPT00C   | Report submission (online → batch bridge)                  |

### Wave 5 — Optional Modules (Weeks 21–24)
> Convert optional IMS/DB2/MQ modules if in scope

| Module     | Rationale                                                  |
|------------|------------------------------------------------------------|
| Auth module| IMS + DB2 + MQ → Spring + JPA + JMS/messaging             |
| DB2 module | Already SQL-based — closest to Java/JPA                    |
| VSAM-MQ   | MQ services → REST APIs or Spring Integration              |

---

## Risk Mitigation Checklist

- [ ] **Financial Accuracy**: Build parallel-run capability to compare COBOL vs. Java calculation results
- [ ] **Data Migration**: Design VSAM → RDBMS migration scripts with checksums and record counts
- [ ] **PII Protection**: Implement encryption for SSN, masking for card numbers (PCI-DSS)
- [ ] **Password Security**: Hash all passwords during migration (currently plaintext)
- [ ] **Audit Trail**: Add before/after logging for all financial field updates
- [ ] **Idempotency**: Design all transaction-creating operations to be idempotent
- [ ] **Batch Recovery**: Implement checkpoint/restart for all batch jobs
- [ ] **Performance**: Benchmark paginated queries against VSAM browse performance
- [ ] **Regression Testing**: Extract test data from `app/data/ASCII/` for automated test suites
- [ ] **Concurrent Access**: Replace CICS pseudo-conversational model with proper session management
