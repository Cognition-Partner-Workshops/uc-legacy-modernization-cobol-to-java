# Hotspot Report -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25  
> **Methodology:** Modules ranked by a composite score of structural complexity, migration risk, and business impact.  
> **Scoring:** Each dimension scored 1-5 (5 = highest). **Composite = Complexity x 0.35 + Risk x 0.35 + Business Impact x 0.30**

---

## Scoring Criteria

| Dimension | Score 1 (Low) | Score 3 (Medium) | Score 5 (High) |
|-----------|--------------|-------------------|----------------|
| **Complexity** | < 200 LOC, linear flow, no EVALUATE | 400-800 LOC, moderate branching | > 1000 LOC, deep nesting, many EVALUATEs, COPY REPLACING |
| **Risk** | Read-only, no shared state | Writes to 1-2 files, moderate validation | Writes to 3+ files, financial calculations, security-sensitive |
| **Business Impact** | Utility / display-only | Core CRUD for one entity | Multi-entity writes, financial posting, customer-facing output |

---

## Top 10 Hotspot Modules

### #1. COACTUPC -- Account Update (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 4,236 (largest program in the codebase) |
| **EVALUATE statements** | 20 |
| **PERFORM calls** | 64 |
| **COPY statements** | 39 (including 38 COPY REPLACING for CSSETATY) |
| **CICS operations** | READ (5), SEND (2), RECEIVE (1), XCTL (1), HANDLE ABEND (2) |
| **Copybook dependencies** | 14 (CVCRD01Y, CSLKPCDY, CVACT01Y, CVACT03Y, CVCUS01Y, CSSETATY, CSSTRPFY, CSUTLDPY, etc.) |
| **VSAM files accessed** | ACCTDAT (R/W), CUSTDAT (R), CARDXREF (R), CARDDAT (R) |
| **Complexity** | **5** -- Largest program; 38 COPY REPLACING macros; extensive field-by-field validation (SSN, FICO, dates, state codes, signed decimals); deep EVALUATE nesting |
| **Risk** | **5** -- Writes to account master (financial data); validates and updates customer PII (SSN, DOB); complex attribute manipulation; abend handling |
| **Business Impact** | **5** -- Core account management; any bug directly affects account balances, credit limits, and customer data |
| **Composite Score** | **5.00** |

**Modernization Recommendations:**
- Break into 4-5 Java service classes: AccountValidator, AccountUpdater, FieldFormatter, LookupService
- Replace 38 COPY REPLACING macros with a generic field-attribute helper
- Externalize CSLKPCDY (1,318-line lookup table) to a database reference table
- Replace PIC-based validation with Bean Validation annotations (`@Pattern`, `@Size`, `@Past`)
- Critical to unit-test every validation path -- high regression risk

---

### #2. CBTRN02C -- Transaction Posting (Batch)

| Metric | Value |
|--------|-------|
| **LOC** | 731 |
| **EVALUATE statements** | 0 |
| **PERFORM calls** | 62 |
| **Files accessed** | DALYTRAN (R), TRANFILE (R/W), XREFFILE (R), DALYREJS (W), ACCTFILE (R/W), TCATBALF (R/W) |
| **Copybook dependencies** | 5 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y) |
| **Complexity** | **4** -- Multi-file I/O; transaction validation and reject logic; updates 3 VSAM files in a single pass |
| **Risk** | **5** -- Core financial posting engine; incorrect posting corrupts account balances and category totals; reject handling must be bullet-proof |
| **Business Impact** | **5** -- Every daily transaction flows through this program; failure means no transactions get posted |
| **Composite Score** | **4.65** |

**Modernization Recommendations:**
- Map to a Spring Batch job with chunk-oriented processing (reader -> processor -> writer)
- Implement database transactions (ACID) to replace single-record VSAM updates
- Add comprehensive audit logging -- the current program has no audit trail
- Reject handling should become a dead-letter queue pattern
- Idempotency checks needed to prevent double-posting

---

### #3. CBACT04C -- Interest Calculation (Batch)

| Metric | Value |
|--------|-------|
| **LOC** | 652 |
| **EVALUATE statements** | 0 |
| **PERFORM calls** | 57 |
| **Files accessed** | TCATBALF (R), XREFFILE (R), DISCGRP (R), ACCTFILE (R/W), TRANSACT (W) |
| **Copybook dependencies** | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y) |
| **Complexity** | **4** -- Financial arithmetic (interest and fee computation); multi-file lookup chain (balance -> xref -> discount group -> account); writes interest transactions back to master |
| **Risk** | **5** -- Incorrect interest = regulatory/compliance exposure; directly modifies account balances; rate lookup errors compound across all accounts |
| **Business Impact** | **5** -- Interest revenue calculation; customer billing accuracy; regulatory compliance |
| **Composite Score** | **4.65** |

**Modernization Recommendations:**
- Use `BigDecimal` with explicit rounding modes (`HALF_EVEN` for financial calculations)
- Implement as a dedicated Spring Batch step with its own transaction boundary
- Add reconciliation checks: sum of interest transactions must match delta in account balances
- Rate lookups should be cached (disclosure groups change infrequently)
- Extensive parameterized testing with known interest scenarios

---

### #4. COCRDLIC -- Card List (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 1,459 |
| **EVALUATE statements** | 18 |
| **PERFORM calls** | 34 |
| **CICS operations** | XCTL (3), STARTBR (2), READNEXT (2), READPREV (2), SEND (3), RECEIVE (1), ENDBR (1) |
| **Copybook dependencies** | 8 (CVCRD01Y, CVACT02Y, CSSTRPFY, etc.) |
| **Complexity** | **4** -- Complex pagination logic (forward/backward browse); 7-row array management with selection validation; multiple XCTL exits to detail/update screens |
| **Risk** | **3** -- Read-heavy with browse operations; XCTL navigation errors could strand users; no direct data modification |
| **Business Impact** | **4** -- Primary card discovery interface; gateway to card view and update |
| **Composite Score** | **3.65** |

**Modernization Recommendations:**
- Replace CICS STARTBR/READNEXT pagination with SQL `LIMIT/OFFSET` or cursor-based pagination
- Array management maps to a paginated REST endpoint returning JSON
- Selection logic becomes URL routing (e.g., `/cards/{cardNum}/view` vs `/cards/{cardNum}/edit`)
- Consider a unified Card List + Detail SPA component

---

### #5. COCRDUPC -- Card Update (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 1,560 |
| **EVALUATE statements** | 16 |
| **PERFORM calls** | 26 |
| **CICS operations** | READ (2), SEND (2), RECEIVE (1), XCTL (1), HANDLE ABEND (2), ABEND (1) |
| **Copybook dependencies** | 8 (CVCRD01Y, CVACT02Y, CVCUS01Y, CSSTRPFY, etc.) |
| **Complexity** | **4** -- Field validation, confirmation flow (display -> confirm -> write), card status management |
| **Risk** | **4** -- Writes to card master; card status changes (activate/deactivate) are security-sensitive; abend handling present |
| **Business Impact** | **4** -- Card lifecycle management; incorrect updates affect card usability |
| **Composite Score** | **4.00** |

**Modernization Recommendations:**
- Split into CardUpdateController + CardValidationService + CardRepository
- Card status changes should emit domain events (for audit and downstream notification)
- Add optimistic locking to prevent concurrent card updates
- Confirmation flow maps to a two-step REST API or form wizard

---

### #6. CBSTM03A -- Statement Generation Driver (Batch)

| Metric | Value |
|--------|-------|
| **LOC** | 924 |
| **EVALUATE statements** | 9 |
| **PERFORM calls** | 33 |
| **CALL statements** | 14 (13 to CBSTM03B, 1 to CEE3ABD) |
| **Files accessed** | TRNXFILE (R), XREFFILE (R), ACCTFILE (R), CUSTFILE (R) -> STMTFILE (W), HTMLFILE (W) |
| **Copybook dependencies** | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y) |
| **Complexity** | **4** -- Multi-format output (text + HTML); iterates across card cross-references; calls CBSTM03B 13 times for different I/O operations; control-break logic for customer/account grouping |
| **Risk** | **3** -- Read-only from VSAM; output is informational (statements); tightly coupled to CBSTM03B sub-program |
| **Business Impact** | **4** -- Customer-facing output; statement errors erode customer trust; formatting must be pixel-perfect |
| **Composite Score** | **3.65** |

**Modernization Recommendations:**
- Replace with a template engine (Thymeleaf / Apache FOP for PDF)
- CBSTM03A + CBSTM03B should merge into a single StatementService
- Consider async generation with customer notification
- HTML generation logic is a natural fit for modern web templating

---

### #7. COTRN02C -- Transaction Add (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 783 |
| **EVALUATE statements** | 26 (highest EVALUATE density in the codebase) |
| **PERFORM calls** | 61 |
| **CICS operations** | READ (2), WRITE (1), STARTBR (1), READPREV (1), ENDBR (1), SEND (1), RECEIVE (1) |
| **CALL statements** | 2 (CSUTLDTC for date conversion) |
| **Files accessed** | TRANSACT (R/W), CARDXREF (R) via CXACAIX alt-index |
| **Complexity** | **5** -- 26 EVALUATE statements (most in the codebase); extensive input validation across 10+ fields; generates transaction ID via READPREV of last key; date validation via CSUTLDTC calls |
| **Risk** | **4** -- Writes transactions directly to master file; must validate card-account relationship via cross-reference; incorrect transaction ID generation could cause duplicate keys |
| **Business Impact** | **4** -- Online transaction entry; any error creates invalid financial records |
| **Composite Score** | **4.30** |

**Modernization Recommendations:**
- Replace EVALUATE cascades with a validation framework (Bean Validation / custom validators)
- Transaction ID generation should use a sequence or UUID, not READPREV-based derivation
- Input validation maps to a DTO with annotations
- Cross-reference lookup becomes a JPA join query

---

### #8. COBIL00C -- Bill Payment (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 572 |
| **EVALUATE statements** | 18 |
| **PERFORM calls** | 38 |
| **CICS operations** | READ (2), WRITE (1), REWRITE (1), STARTBR (1), READPREV (1), ENDBR (1), ASKTIME (1), FORMATTIME (1) |
| **Files accessed** | ACCTDAT (R/W), TRANSACT (R/W), CXACAIX (R) |
| **Complexity** | **3** -- Moderate size; payment amount validation; timestamp generation; transaction writing + account balance update in single flow |
| **Risk** | **5** -- Financial transaction: writes payment to transaction file AND updates account balance; both must succeed atomically; no rollback mechanism in current VSAM implementation |
| **Business Impact** | **5** -- Direct money movement; customer-initiated payment; errors = customer financial harm |
| **Composite Score** | **4.30** |

**Modernization Recommendations:**
- Wrap payment in a database transaction (ACID) -- critical atomicity requirement
- Add idempotency key to prevent double payments
- Implement payment amount limits and velocity checks
- Audit log every payment attempt (success and failure)
- Consider event sourcing for payment lifecycle

---

### #9. CBTRN03C -- Transaction Report (Batch)

| Metric | Value |
|--------|-------|
| **LOC** | 649 |
| **EVALUATE statements** | 4 |
| **PERFORM calls** | 73 (highest PERFORM count per LOC in batch programs) |
| **Files accessed** | TRANFILE (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATEPARM (R) -> TRANREPT (W) |
| **Copybook dependencies** | 5 (CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y) |
| **Complexity** | **4** -- Control-break report logic (page/account/grand totals); multi-file lookups (type + category descriptions); date range filtering from parameter file; header/detail/total formatting |
| **Risk** | **2** -- Read-only from master files; output is a report file; no data modification |
| **Business Impact** | **3** -- Daily operations report; used for reconciliation and audit |
| **Composite Score** | **3.00** |

**Modernization Recommendations:**
- Replace with JasperReports or a SQL-based reporting query
- Control-break logic becomes `GROUP BY` with window functions
- Parameter file (DATEPARM) becomes API query parameters
- Consider real-time dashboard instead of batch-generated flat file

---

### #10. COTRN00C -- Transaction List (Online)

| Metric | Value |
|--------|-------|
| **LOC** | 699 |
| **EVALUATE statements** | 16 |
| **PERFORM calls** | 47 |
| **CICS operations** | STARTBR (1), READNEXT (1), READPREV (1), ENDBR (1), SEND (2), RECEIVE (1) |
| **Files accessed** | TRANSACT (browse) |
| **Complexity** | **3** -- Pagination logic (forward/backward); 10-row display array; selection handling with XCTL to detail/add screens |
| **Risk** | **2** -- Read-only browse; no data modification |
| **Business Impact** | **4** -- Primary transaction discovery screen; gateway to view and add transactions |
| **Composite Score** | **3.00** |

**Modernization Recommendations:**
- Replace with paginated REST endpoint + data table UI component
- STARTBR/READNEXT becomes SQL query with pagination
- Selection routing becomes client-side navigation

---

## Summary Ranking

| Rank | Program | Type | LOC | Complexity | Risk | Biz Impact | **Composite** |
|------|---------|------|-----|-----------|------|------------|---------------|
| 1 | **COACTUPC** | Online | 4,236 | 5 | 5 | 5 | **5.00** |
| 2 | **CBTRN02C** | Batch | 731 | 4 | 5 | 5 | **4.65** |
| 3 | **CBACT04C** | Batch | 652 | 4 | 5 | 5 | **4.65** |
| 4 | **COTRN02C** | Online | 783 | 5 | 4 | 4 | **4.35** |
| 5 | **COBIL00C** | Online | 572 | 3 | 5 | 5 | **4.30** |
| 6 | **COCRDUPC** | Online | 1,560 | 4 | 4 | 4 | **4.00** |
| 7 | **CBSTM03A** | Batch | 924 | 4 | 3 | 4 | **3.65** |
| 8 | **COCRDLIC** | Online | 1,459 | 4 | 3 | 4 | **3.65** |
| 9 | **CBTRN03C** | Batch | 649 | 4 | 2 | 3 | **3.00** |
| 10 | **COTRN00C** | Online | 699 | 3 | 2 | 4 | **2.95** |

---

## Migration Priority Waves (Recommended)

### Wave 1 -- High-Risk Financial Core (Months 1-3)
Programs: **CBTRN02C**, **CBACT04C**, **COBIL00C**  
*Rationale:* These programs handle money movement and financial calculations. Errors here have the highest blast radius. Modernize first with extensive testing and parallel-run validation.

### Wave 2 -- Core Account & Card Management (Months 3-6)
Programs: **COACTUPC**, **COCRDUPC**, **COTRN02C** (online add)  
*Rationale:* Complex CRUD with heavy validation. COACTUPC is the single largest program and will require decomposition into multiple services.

### Wave 3 -- Reporting & Statements (Months 6-8)
Programs: **CBSTM03A/B**, **CBTRN03C**, **CORPT00C**  
*Rationale:* Output-focused programs with lower risk. Modern reporting tools (JasperReports, SQL views) can replace batch report generation.

### Wave 4 -- Navigation, Lists, & Utilities (Months 8-10)
Programs: **COCRDLIC**, **COTRN00C**, **COMEN01C**, **COADM01C**, **COSGN00C**, remaining programs  
*Rationale:* Lower complexity programs that mainly handle screen navigation and data display. These can be bulk-converted once the core services are in place.

---

## Key Modernization Risks

| Risk | Affected Programs | Mitigation |
|------|------------------|------------|
| **Financial accuracy** | CBTRN02C, CBACT04C, COBIL00C | Parallel-run with COBOL for 2+ cycles; reconciliation reports |
| **Plaintext passwords** | COSGN00C, CSUSR01Y | Immediate: hash passwords; implement proper auth (OAuth2/JWT) |
| **VSAM-to-RDBMS data migration** | All programs | Staged migration with data validation scripts; maintain VSAM fallback |
| **COPY REPLACING patterns** | COACTUPC (38 instances) | Requires careful refactoring; each REPLACING generates unique code |
| **Control-break report logic** | CBTRN03C, CBSTM03A | Map to SQL GROUP BY / window functions; verify totals match |
| **CICS COMMAREA state** | All 17 online programs | Replace with HTTP session or JWT claims; stateless preferred |
| **Hardcoded lookup tables** | CSLKPCDY (1,318 lines) | Externalize to database; add admin UI for maintenance |
| **No audit trail** | CBTRN02C, CBACT04C | Add audit logging from day one in Java implementation |
| **Tightly coupled sub-programs** | CBSTM03A <-> CBSTM03B | Merge into single service; CBSTM03B is called 13 times |
| **GDG (Generation Data Groups)** | TRANBKP, TRANREPT, CREASTMT | Replace with timestamped files or database versioning |
