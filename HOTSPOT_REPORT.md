# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Methodology:** Weighted scoring across Code Complexity, Business Risk, and Modernization Impact

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Code Complexity** | 35% | Lines of code, EVALUATE/IF nesting depth, number of COPY includes, CICS commands, CALL statements, number of VSAM file operations |
| **Business Risk** | 35% | Criticality to business operations, data sensitivity (PII/financial), blast radius if defects occur, number of upstream/downstream dependencies |
| **Modernization Impact** | 30% | Effort to convert, technology diversity (VSAM+CICS+DB2+MQ), value unlocked by modernizing, pattern reuse potential for other modules |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Impact × 0.30)

---

## Top 10 Hotspot Modules

### Rank #1 — COACTUPC (Account Update)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **4,236** (largest program) | |
| IF Statements | 164 | |
| EVALUATE Blocks | 20 | |
| VSAM File Accesses | 5 (ACCTFILE, CARDFILE, CUSTFILE, CARDXREF + rewrite) | |
| Copybooks Included | 12+ (including CSLKPCDY at 718 lines) | |
| **Code Complexity** | | **10/10** |
| **Business Risk** | | **9/10** |
| **Modernization Impact** | | **9/10** |
| **Composite Score** | | **9.35** |

**Why #1:** This is the most complex program in the entire codebase by every metric. It handles account updates with extensive field-level validation (address, credit limit, status changes), reads/writes 4 VSAM files, includes country/state lookup tables (718-line copybook), and manages multiple screen maps for the account update workflow. A defect here directly impacts account balances and credit limits.

**Modernization Recommendations:**
- Decompose into multiple microservices: AccountValidationService, AccountUpdateService, AddressLookupService
- Extract country/state lookup (CSLKPCDY) into a reference data service or database table
- Implement field-level validation using Java Bean Validation (JSR 380)
- Replace VSAM multi-file reads with JPA repository joins
- **Estimated Effort:** 8–12 weeks

---

### Rank #2 — CBTRN02C (Transaction Posting — Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 731 | |
| IF Statements | 48 | |
| VSAM Files Accessed | **7** (most files of any program) | |
| Business Function | Core daily transaction posting | |
| **Code Complexity** | | **8/10** |
| **Business Risk** | | **10/10** |
| **Modernization Impact** | | **9/10** |
| **Composite Score** | | **9.00** |

**Why #2:** This is the financial heart of the batch cycle. It reads daily transactions, validates them against transaction types/categories, posts to the master transaction file, updates category balances, and writes rejects. It touches 7 VSAM files in a single run. Any error results in incorrect balances, failed postings, or lost transactions.

**Modernization Recommendations:**
- Convert to Spring Batch job with chunk-oriented processing
- Implement idempotent transaction posting with transaction IDs
- Add comprehensive audit logging for every posting decision
- Replace VSAM file I/O with database transactions (ACID guarantees)
- Implement dead-letter queue for rejected transactions
- **Estimated Effort:** 6–8 weeks

---

### Rank #3 — CBACT04C (Interest Calculation — Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 652 | |
| IF Statements | 43 | |
| VSAM Files Accessed | 4 (CARDXREF, ACCTFILE, TCATBALF, DISCGRP) | |
| Business Function | Interest calculation per category per account | |
| **Code Complexity** | | **7/10** |
| **Business Risk** | | **10/10** |
| **Modernization Impact** | | **9/10** |
| **Composite Score** | | **8.65** |

**Why #3:** Interest calculation is a regulated financial function. It cross-references cards, accounts, category balances, and disclosure group interest rates to compute interest charges. Errors directly impact customer billing and have regulatory implications (TILA, CFPB). The disclosure group lookup pattern adds complexity.

**Modernization Recommendations:**
- Implement as a Spring Batch job with configurable interest rules engine
- Extract interest rate configuration to a rules table / configuration service
- Add comprehensive calculation audit trail for regulatory compliance
- Implement parallel processing by account partition for performance
- Unit test every interest calculation scenario
- **Estimated Effort:** 6–8 weeks

---

### Rank #4 — COCRDUPC (Card Update — Online)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 1,560 | |
| IF Statements | 72 | |
| EVALUATE Blocks | 16 | |
| VSAM File Accesses | 2 (CARDFILE, CARDXREF) | |
| **Code Complexity** | | **8/10** |
| **Business Risk** | | **8/10** |
| **Modernization Impact** | | **8/10** |
| **Composite Score** | | **8.00** |

**Why #4:** Second-largest online program with extensive card data validation. Manages card status changes (activate, deactivate, reissue), embossed name updates, and expiration date management. Card data changes have PCI-DSS compliance implications.

**Modernization Recommendations:**
- Implement card update REST API with PCI-DSS compliant field handling
- Mask card numbers in all logs and responses
- Add card status state machine (e.g., Active → Suspended → Closed)
- Separate validation logic into a CardValidationService
- **Estimated Effort:** 4–6 weeks

---

### Rank #5 — COCRDLIC (Card List — Online)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 1,459 | |
| IF Statements | 59 | |
| EVALUATE Blocks | 18 | |
| VSAM Operations | 6 (STARTBR, READNEXT, READPREV, ENDBR, READNEXT loop) | |
| **Code Complexity** | | **8/10** |
| **Business Risk** | | **7/10** |
| **Modernization Impact** | | **8/10** |
| **Composite Score** | | **7.65** |

**Why #5:** Complex browse/pagination logic using VSAM STARTBR/READNEXT/READPREV — a pattern that doesn't translate directly to SQL. The program handles forward/backward paging, search filters, and selection for drill-down. This pattern is reused in COTRN00C and COUSR00C, so solving it here creates a reusable pagination pattern for the modern system.

**Modernization Recommendations:**
- Convert VSAM browse to paginated SQL queries (OFFSET/FETCH or keyset pagination)
- Create a reusable PaginatedListService pattern for all list screens
- Implement server-side filtering and sorting
- **Estimated Effort:** 3–5 weeks

---

### Rank #6 — CBSTM03A + CBSTM03B (Statement Generation — Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 924 + 230 = **1,154** (combined) | |
| IF Statements | 15 + 12 = 27 | |
| CALL Statements | 14 (CBSTM03A → CBSTM03B) + 1 (CEE3ABD) | |
| VSAM Files Read | 4 (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE) | |
| Output Formats | 2 (plaintext + HTML) | |
| **Code Complexity** | | **8/10** |
| **Business Risk** | | **7/10** |
| **Modernization Impact** | | **8/10** |
| **Composite Score** | | **7.60** |

**Why #6:** Most architecturally complex batch program — uses CALL subroutine pattern, control block addressing, multiple output formats, and reads 4 VSAM files. Demonstrates mainframe patterns (TIOT addressing, sub-program linkage) that require careful conversion. Generates customer-facing statements, so output formatting must be pixel-perfect.

**Modernization Recommendations:**
- Convert to Spring Batch with JasperReports or Apache FOP for PDF/HTML generation
- Replace CALL subroutine pattern with Spring service injection
- Implement template-based statement formatting
- Add email/digital delivery channel alongside print
- **Estimated Effort:** 5–7 weeks

---

### Rank #7 — COTRN02C (Transaction Add — Online)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 783 | |
| EVALUATE Blocks | **26** (highest EVALUATE count) | |
| IF Statements | 14 | |
| VSAM Operations | 6 (READ, STARTBR, READPREV, ENDBR, WRITE) | |
| **Code Complexity** | | **7/10** |
| **Business Risk** | | **9/10** |
| **Modernization Impact** | | **7/10** |
| **Composite Score** | | **7.60** |

**Why #7:** Primary online transaction entry point with the highest EVALUATE block count. Validates transaction details against card/account data, generates transaction IDs using READPREV for sequence, and writes to the master transaction file. Any defect here creates incorrect financial records.

**Modernization Recommendations:**
- Implement as a Transaction REST API with request validation
- Replace READPREV ID generation with UUID or database sequence
- Add real-time transaction authorization before posting
- Implement event-driven architecture (publish transaction events)
- **Estimated Effort:** 4–5 weeks

---

### Rank #8 — CBTRN03C (Daily Transaction Report — Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 649 | |
| IF Statements | 38 | |
| EVALUATE Blocks | 4 | |
| VSAM Files Read | 5 (TRANSACT, TRANTYPE, TRANCATG, CARDXREF, ACCTFILE) | |
| **Code Complexity** | | **7/10** |
| **Business Risk** | | **7/10** |
| **Modernization Impact** | | **7/10** |
| **Composite Score** | | **7.00** |

**Why #8:** Key compliance and operations report that reads 5 VSAM files. Uses the CVTRA07Y report layout copybook with page/account/grand totals. The report formatting logic (control breaks, subtotals) is a common mainframe pattern that needs careful conversion.

**Modernization Recommendations:**
- Convert to Spring Batch report job with JasperReports
- Replace control break logic with GROUP BY/window functions in SQL
- Add report scheduling and distribution (email, portal)
- **Estimated Effort:** 3–4 weeks

---

### Rank #9 — COACTVWC (Account View — Online)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 941 | |
| IF Statements | 28 | |
| EVALUATE Blocks | 10 | |
| VSAM File Accesses | 3 (ACCTFILE, CARDFILE, CUSTFILE) | |
| **Code Complexity** | | **6/10** |
| **Business Risk** | | **7/10** |
| **Modernization Impact** | | **8/10** |
| **Composite Score** | | **6.95** |

**Why #9:** Account view is the highest-traffic screen — every user session hits it. It aggregates data from 3 VSAM files to present a consolidated account view. While read-only, its performance characteristics and data aggregation pattern will define the modern API layer's account endpoint.

**Modernization Recommendations:**
- Implement as GET /accounts/{id} REST endpoint
- Use database JOIN to replace 3 separate VSAM reads
- Add caching layer (Redis) for frequently accessed accounts
- Design as the foundational account API that other services consume
- **Estimated Effort:** 2–3 weeks

---

### Rank #10 — COSGN00C + CSUSR01Y (Sign-On + User Security)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | 260 (COSGN00C) + 80-byte record (CSUSR01Y) | |
| EVALUATE Blocks | 6 | |
| VSAM Operations | 1 (READ USRSEC) | |
| **Code Complexity** | | **4/10** |
| **Business Risk** | | **10/10** |
| **Modernization Impact** | | **8/10** |
| **Composite Score** | | **7.10** |

**Why #10:** Although small in code size, this is a **critical security hotspot**. Passwords are stored in plaintext (PIC X(08) in CSUSR01Y). The authentication model has no password hashing, no session timeout, no failed login lockout, no MFA. This must be one of the first things modernized to meet any security standard.

**Modernization Recommendations:**
- Replace with Spring Security + OAuth2/OIDC
- Hash passwords with bcrypt/argon2 (never plaintext)
- Implement session management with JWT tokens
- Add MFA support, account lockout, and audit logging
- Consider integration with enterprise identity provider (AD/LDAP/Okta)
- **Estimated Effort:** 3–4 weeks (greenfield replacement, not conversion)

---

## Summary Rankings

| Rank | Program | Composite Score | Complexity | Risk | Impact | LOC | Key Concern |
|------|---------|----------------|------------|------|--------|-----|-------------|
| 1 | **COACTUPC** | **9.35** | 10 | 9 | 9 | 4,236 | Largest, most complex program |
| 2 | **CBTRN02C** | **9.00** | 8 | 10 | 9 | 731 | Core financial posting, 7 files |
| 3 | **CBACT04C** | **8.65** | 7 | 10 | 9 | 652 | Regulated interest calculations |
| 4 | **COCRDUPC** | **8.00** | 8 | 8 | 8 | 1,560 | Card updates, PCI-DSS scope |
| 5 | **COCRDLIC** | **7.65** | 8 | 7 | 8 | 1,459 | Complex browse/pagination |
| 6 | **CBSTM03A/B** | **7.60** | 8 | 7 | 8 | 1,154 | Statement gen, subroutine pattern |
| 7 | **COTRN02C** | **7.60** | 7 | 9 | 7 | 783 | Transaction entry, most EVALUATEs |
| 8 | **CBTRN03C** | **7.00** | 7 | 7 | 7 | 649 | Compliance reporting, 5 files |
| 9 | **COACTVWC** | **6.95** | 6 | 7 | 8 | 941 | High-traffic, defines API pattern |
| 10 | **COSGN00C** | **7.10** | 4 | 10 | 8 | 260 | Plaintext passwords, no MFA |

---

## Recommended Modernization Sequence

Based on the hotspot analysis, the recommended migration wave plan:

### Wave 1 — Foundation & Security (Weeks 1–6)
1. **COSGN00C / CSUSR01Y** → Spring Security + OAuth2 (security is non-negotiable)
2. **COACTVWC** → Account View API (defines the data model and API patterns)
3. **Database schema** → Convert all VSAM copybook layouts to relational tables

### Wave 2 — Core Business Logic (Weeks 7–18)
4. **COACTUPC** → Account Update API (decomposed into services)
5. **COCRDUPC** → Card Update API
6. **COTRN02C** → Transaction Add API
7. **COCRDLIC** → Card List API (establishes pagination pattern)

### Wave 3 — Batch Processing (Weeks 19–30)
8. **CBTRN02C** → Spring Batch transaction posting
9. **CBACT04C** → Spring Batch interest calculation
10. **CBSTM03A/B** → Spring Batch statement generation
11. **CBTRN03C** → Spring Batch daily report

### Wave 4 — Remaining Online + Optional Modules (Weeks 31–40)
12. Remaining CICS programs (COTRN00C, COTRN01C, CORPT00C, COBIL00C, COUSR*)
13. Optional modules (Auth/DB2/MQ) if in scope

**Total Estimated Effort:** 35–45 weeks for a team of 4–6 developers.

---

## Risk Mitigation Notes

| Risk | Mitigation |
|------|-----------|
| **Plaintext passwords** | Prioritize COSGN00C replacement in Wave 1 |
| **PII exposure** (SSN, DOB in CVCUS01Y) | Implement field-level encryption in database schema |
| **No audit trail** | Add audit logging from Wave 1; all data mutations must be logged |
| **VSAM → RDBMS fidelity** | Comprehensive data migration validation with record-count reconciliation |
| **Batch window dependency** | Modern batch can run with database concurrency; eliminate CLOSEFIL/OPENFIL pattern |
| **CSLKPCDY hardcoded lookups** | Extract 718-line lookup table to reference data service immediately |
| **Report format fidelity** | Parallel run old and new reports; diff outputs before cutover |
