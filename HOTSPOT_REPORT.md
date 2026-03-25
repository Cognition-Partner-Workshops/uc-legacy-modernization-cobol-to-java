# CardDemo Hotspot Report — Top 10 Modules by Modernization Priority

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
>
> This report identifies the top 10 highest-priority modules for modernization, ranked by a
> composite score of **code complexity**, **migration risk**, and **business impact**.

---

## Scoring Methodology

Each module is scored on three axes (1–5 scale, 5 = highest):

| Dimension | What It Measures |
|-----------|-----------------|
| **Complexity** | Lines of code, number of PERFORM/EVALUATE branches, VSAM file handles, copybook inclusions, nesting depth, use of GO TO |
| **Risk** | Data sensitivity (PII/PCI), concurrency concerns, multi-file update atomicity, external interface coupling (MQ/IMS/DB2), error-handling patterns |
| **Business Impact** | Revenue criticality, user-facing importance, batch-cycle dependency, number of downstream consumers |

**Composite Score** = (Complexity × 0.30) + (Risk × 0.40) + (Business Impact × 0.30)

Risk is weighted highest because it represents the greatest source of defects during migration.

---

## Top 10 Hotspot Modules

### Rank 1 — `COACTUPC.cbl` (Account Update)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 4,236 (largest program in the codebase) |
| **CICS Transaction** | CA02 |
| **Complexity Score** | 5 / 5 |
| **Risk Score** | 5 / 5 |
| **Business Impact Score** | 5 / 5 |
| **Composite Score** | **5.00** |

**Why it's #1:**
- Largest single COBOL program — nearly double the next largest. Contains deeply nested EVALUATE/PERFORM logic with dozens of conditional branches for field-level validation.
- Reads/writes 3 VSAM files simultaneously (ACCTDATA, CARDXREF, CUSTDATA) with REWRITE operations that must be atomic.
- Manages PCI-sensitive data (card numbers via XREF) and financial data (account balances, credit limits).
- Uses HANDLE ABEND for error recovery, complex screen map interaction with multiple map sends.
- Includes the `CSLKPCDY` copybook (1,318-line lookup code table) for state/country validation.
- Central to the account management workflow — all account changes flow through this program.

**Modernization Recommendations:**
- Decompose into multiple Java service classes: `AccountValidationService`, `AccountUpdateService`, `AccountLookupService`.
- Extract the lookup code table into a database reference table or enum.
- Implement optimistic locking to replace VSAM record-level locking.
- Apply field-level validation using Bean Validation (JSR 380) annotations.

---

### Rank 2 — `CBTRN02C.cbl` (Transaction Posting — Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 731 |
| **CICS Transaction** | N/A (batch) |
| **Complexity Score** | 4 / 5 |
| **Risk Score** | 5 / 5 |
| **Business Impact Score** | 5 / 5 |
| **Composite Score** | **4.70** |

**Why it's #2:**
- Core batch engine: posts daily transactions to the master transaction file. Failure here means financial data loss.
- Updates 3 VSAM files in a single run: TRANSACT (write), ACCTDATA (update balance), TCATBALF (update category balances).
- Multi-step validation: card number → XREF → account → transaction type → category.
- Reads 7 files: DALYTRAN, CARDXREF, CARDDATA, ACCTDATA, TRANSACT, TCATBALF, TRANTYPE, TRANCATG.
- No built-in checkpoint/restart — an ABEND mid-run could leave files in an inconsistent state.
- Runs nightly in the critical batch window; delays cascade to INTCALC, CREASTMT, TRANREPT.

**Modernization Recommendations:**
- Convert to Spring Batch with chunk-oriented processing and database transactions.
- Implement checkpoint/restart with `ItemReader`/`ItemWriter` patterns.
- Replace multi-file VSAM updates with a single database transaction (ACID).
- Add dead-letter queue for rejected transactions instead of ABEND.

---

### Rank 3 — `COCRDLIC.cbl` (Card List)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,459 |
| **CICS Transaction** | CC01 |
| **Complexity Score** | 4 / 5 |
| **Risk Score** | 4 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **4.00** |

**Why it's #3:**
- Second largest online program. Implements scrollable browse (STARTBR/READNEXT/READPREV/ENDBR) with forward/backward pagination.
- Navigation hub: dispatches to COCRDSLC (view) and COCRDUPC (update) via XCTL with program name literals.
- Handles PCI-sensitive card numbers — must mask in the modernized UI.
- Complex screen management: builds a multi-row list display from VSAM browse results.
- Uses the CARDXREF AIX (alternate index) path for account-based card lookups.

**Modernization Recommendations:**
- Convert VSAM browse to paginated SQL query (`LIMIT`/`OFFSET` or cursor-based).
- Implement as a REST endpoint returning JSON; build React/Angular list component.
- Apply PCI-DSS masking (show only last 4 digits) in the API response layer.

---

### Rank 4 — `COCRDUPC.cbl` (Card Update)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,560 |
| **CICS Transaction** | CC03 |
| **Complexity Score** | 4 / 5 |
| **Risk Score** | 5 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **4.40** |

**Why it's #4:**
- Third largest program. Mirrors COACTUPC's patterns for card data: multi-field validation, REWRITE operations, HANDLE ABEND.
- Directly updates card records including card status (active/inactive), embossed name, and expiration date — PCI-relevant data.
- Reads from CARDDATA and CARDXREF files; writes back to CARDDATA.
- Uses GO TO for some control flow (increases migration complexity).
- Contains abend-handling routine with EXEC CICS ABEND — needs careful mapping to Java exception handling.

**Modernization Recommendations:**
- Map to `CardUpdateService` with transactional database operations.
- Use Java exception handling (try/catch) instead of HANDLE ABEND.
- Encrypt card data at rest in the target database.

---

### Rank 5 — `CBACT04C.cbl` (Interest Calculation)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 652 |
| **CICS Transaction** | N/A (batch) |
| **Complexity Score** | 4 / 5 |
| **Risk Score** | 5 / 5 |
| **Business Impact Score** | 5 / 5 |
| **Composite Score** | **4.70** |

**Why it's tied for #2 on raw score but ranked #5 due to narrower scope:**
- Implements the core financial calculation: interest on category balances using disclosure group rates.
- Reads 4 files: TCATBALF, CARDXREF (via AIX), ACCTDATA, DISCGRP.
- Writes system-generated interest transactions to the SYSTRAN GDG dataset.
- Decimal arithmetic with `S9(09)V99` fields — rounding errors during migration to Java `BigDecimal` are a top risk.
- Any bug in interest calculation has direct revenue/compliance impact.

**Modernization Recommendations:**
- Implement as a Spring Batch job with explicit `BigDecimal` arithmetic (use `RoundingMode.HALF_EVEN` to match COBOL behavior).
- Write comprehensive golden-file tests comparing COBOL output to Java output for identical input.
- Consider regulatory audit trail requirements.

---

### Rank 6 — `CBSTM03A.CBL` (Statement Generation)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 924 (+ 230 in subroutine CBSTM03B) |
| **CICS Transaction** | N/A (batch) |
| **Complexity Score** | 4 / 5 |
| **Risk Score** | 4 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **4.00** |

**Why it's #6:**
- Two-program unit (CBSTM03A calls CBSTM03B for all file I/O) — demonstrates the COBOL subroutine-via-CALL pattern.
- Produces both text (`STATEMNT.PS`) and HTML (`STATEMNT.HTML`) output — multi-format rendering logic.
- Reads 4 VSAM files via CBSTM03B: TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE.
- Contains complex GO TO-driven state machine for file sequencing (open file A → read → open file B → loop).
- Customer-facing output — statement accuracy is critical for regulatory compliance.

**Modernization Recommendations:**
- Replace with a template-based reporting framework (JasperReports, Thymeleaf, or Apache POI).
- Convert CALL/USING linkage to simple Java method calls between classes.
- Replace the GO TO state machine with structured loops.

---

### Rank 7 — `COTRN02C.cbl` (Transaction Add — Online)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 783 |
| **CICS Transaction** | CT02 |
| **Complexity Score** | 3 / 5 |
| **Risk Score** | 4 / 5 |
| **Business Impact Score** | 5 / 5 |
| **Composite Score** | **4.10** |

**Why it's #7:**
- Primary online transaction entry point — every new credit card transaction passes through here.
- Calls CSUTLDTC for date validation; reads CARDXREF, ACCTDATA; writes to TRANSACT.
- Generates unique transaction IDs using STARTBR/READPREV to find the highest existing ID — a concurrency risk.
- Contains 4 VSAM file interactions with error handling for each.

**Modernization Recommendations:**
- Use database sequence/UUID for transaction ID generation instead of READPREV-based logic.
- Convert to a REST POST endpoint with request validation.
- Wrap in a database transaction for atomicity.

---

### Rank 8 — `CBTRN03C.cbl` (Transaction Report — Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 649 |
| **CICS Transaction** | N/A (batch) |
| **Complexity Score** | 3 / 5 |
| **Risk Score** | 3 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **3.40** |

**Why it's #8:**
- Generates the Daily Transaction Report — reads transactions, joins with type/category master data.
- Reads 5 files: DALYTRAN, CARDXREF, TRANTYPE, TRANCATG, DATEPARM.
- Uses the CVTRA07Y report formatting copybook with page/account/grand totals.
- Report output is consumed downstream (printed, archived to GDG, potentially converted to PDF via TXT2PDF1).

**Modernization Recommendations:**
- Convert to Spring Batch with a `FlatFileItemWriter` or reporting library.
- Replace date parameter file (DATEPARM) with command-line arguments or application properties.
- Consider direct PDF generation instead of text → PDF conversion.

---

### Rank 9 — `COACTVWC.cbl` (Account View)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 941 |
| **CICS Transaction** | CA01 |
| **Complexity Score** | 3 / 5 |
| **Risk Score** | 3 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **3.40** |

**Why it's #9:**
- Read-only account display but reads 3 VSAM files (ACCTDATA, CARDXREF, CUSTDATA).
- Contains HANDLE ABEND pattern and abend-exit routines.
- Relatively large for a read-only screen due to field formatting and error handling.
- Entry point to the account management workflow — most users navigate here first.

**Modernization Recommendations:**
- Convert to a simple REST GET endpoint with a DTO response.
- Remove abend handling; use standard exception mapping.
- Good candidate for early migration as a low-risk proof-of-concept.

---

### Rank 10 — `COSGN00C.cbl` (Sign On)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 260 |
| **CICS Transaction** | CC00 |
| **Complexity Score** | 2 / 5 |
| **Risk Score** | 5 / 5 |
| **Business Impact Score** | 4 / 5 |
| **Composite Score** | **3.80** |

**Why it's #10:**
- The gateway to the entire application — all users must authenticate through this program.
- Reads USRSEC VSAM file and compares plaintext passwords — a critical security vulnerability.
- Routes users to COMEN01C (regular) or COADM01C (admin) based on user type — role-based access control.
- Small LOC but disproportionately high risk due to authentication security concerns.

**Modernization Recommendations:**
- Replace with Spring Security authentication (form-based or OAuth2/OIDC).
- Implement password hashing (bcrypt) — never store plaintext passwords.
- Add session management, CSRF protection, brute-force lockout.
- Consider this as the first module to modernize to establish the security foundation.

---

## Heatmap Summary

| Rank | Module | LOC | Complexity | Risk | Biz Impact | **Composite** |
|------|--------|-----|------------|------|------------|---------------|
| 1 | COACTUPC (Account Update) | 4,236 | 5 | 5 | 5 | **5.00** |
| 2 | CBTRN02C (Transaction Posting) | 731 | 4 | 5 | 5 | **4.70** |
| 3 | COCRDLIC (Card List) | 1,459 | 4 | 4 | 4 | **4.00** |
| 4 | COCRDUPC (Card Update) | 1,560 | 4 | 5 | 4 | **4.40** |
| 5 | CBACT04C (Interest Calculation) | 652 | 4 | 5 | 5 | **4.70** |
| 6 | CBSTM03A+B (Statement Gen) | 1,154 | 4 | 4 | 4 | **4.00** |
| 7 | COTRN02C (Transaction Add) | 783 | 3 | 4 | 5 | **4.10** |
| 8 | CBTRN03C (Transaction Report) | 649 | 3 | 3 | 4 | **3.40** |
| 9 | COACTVWC (Account View) | 941 | 3 | 3 | 4 | **3.40** |
| 10 | COSGN00C (Sign On) | 260 | 2 | 5 | 4 | **3.80** |

---

## Recommended Migration Waves

Based on the hotspot analysis, the following wave strategy is recommended:

### Wave 0 — Foundation (Weeks 1–3)
- **COSGN00C** (Sign On) → Establish Spring Security authentication
- **CSUSR01Y** data model → User/role tables in target database
- **COCOM01Y** COMMAREA → Java session/context object

### Wave 1 — Read-Only Screens (Weeks 4–6)
- **COACTVWC** (Account View) — low-risk proof of concept
- **COCRDSLC** (Card View) — low-risk, read-only
- **COTRN01C** (Transaction View) — low-risk, read-only

### Wave 2 — List/Browse Screens (Weeks 7–10)
- **COCRDLIC** (Card List) — introduces pagination pattern
- **COTRN00C** (Transaction List) — reuses pagination
- **COUSR00C** (User List) — admin function

### Wave 3 — Update Screens (Weeks 11–16)
- **COACTUPC** (Account Update) — most complex; requires decomposition
- **COCRDUPC** (Card Update) — second most complex update
- **COTRN02C** (Transaction Add) — introduces write pattern
- **COBIL00C** (Bill Payment)

### Wave 4 — Batch Processing (Weeks 17–22)
- **CBTRN02C** (Transaction Posting) — core batch engine
- **CBACT04C** (Interest Calculation) — financial critical
- **CBSTM03A/B** (Statement Generation)
- **CBTRN03C** (Transaction Report)

### Wave 5 — Admin & Utilities (Weeks 23–25)
- **COUSR01C–03C** (User CRUD)
- **COADM01C** (Admin Menu)
- **CORPT00C** (Report Request)
- **CBEXPORT/CBIMPORT** (Data Migration)
- Remaining utility programs

---

## Key Risk Patterns Across All Hotspots

| Risk Pattern | Affected Modules | Mitigation |
|---|---|---|
| **Plaintext passwords** | COSGN00C, COUSR01C-03C | Hash with bcrypt; implement proper auth |
| **PCI data in memory** | COACTUPC, COCRDLIC, COCRDUPC, COTRN02C | Tokenize card numbers; mask in logs/UI |
| **Non-atomic multi-file updates** | CBTRN02C, CBACT04C, COACTUPC | Use database transactions (ACID) |
| **GO TO control flow** | CBSTM03A, COCRDUPC, COCRDLIC | Restructure to structured loops |
| **COBOL decimal arithmetic** | CBACT04C, CBTRN02C | Use `BigDecimal` with explicit rounding |
| **No checkpoint/restart** | All batch programs | Implement Spring Batch restart capability |
| **HANDLE ABEND** | COACTUPC, COCRDUPC, COACTVWC, COCRDSLC | Map to Java exception handling |
| **Sequence generation via READPREV** | COTRN02C | Replace with database sequences or UUIDs |
