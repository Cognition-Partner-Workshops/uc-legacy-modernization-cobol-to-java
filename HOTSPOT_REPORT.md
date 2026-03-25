# CardDemo Hotspot Report — Top 10 Migration-Critical Modules

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across complexity, risk, and business impact
> **Purpose:** Prioritize modules for modernization effort estimation and sequencing

---

## Scoring Methodology

Each module is scored on three axes (1–10 scale):

| Axis               | Weight | Factors Considered                                                       |
|--------------------|-------:|--------------------------------------------------------------------------|
| **Complexity**     |   40%  | Lines of code, cyclomatic branches (EVALUATE/IF nesting), copybook count, VSAM file count, CICS commands, number of PERFORMs |
| **Risk**           |   30%  | Data mutation (REWRITE/WRITE), financial calculations, PII handling, error handling quality, coupling to other programs |
| **Business Impact**|   30%  | Revenue-criticality, user-facing, regulatory exposure, batch-cycle position, number of downstream dependents |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.30) + (Business Impact × 0.30)

---

## Top 10 Hotspot Summary

| Rank | Program    | LOC   | Type   | Complexity | Risk | Biz Impact | **Score** | Primary Concern                          |
|-----:|------------|------:|--------|:----------:|:----:|:----------:|:---------:|------------------------------------------|
|    1 | COACTUPC   | 4,237 | Online |    10      |  10  |     9      | **9.7**   | Largest program; mutates 3 VSAM files    |
|    2 | CBTRN02C   |   731 | Batch  |     8      |  10  |    10      | **9.2**   | Core posting engine; financial mutations |
|    3 | CBACT04C   |   652 | Batch  |     7      |  10  |    10      | **8.8**   | Interest calculator; financial core      |
|    4 | CBSTM03A   |   924 | Batch  |     9      |   7  |     9      | **8.4**   | Statement generation; complex I/O        |
|    5 | COCRDUPC   | 1,560 | Online |     9      |   8  |     7      | **8.1**   | Card update with extensive validation    |
|    6 | COCRDLIC   | 1,460 | Online |     8      |   6  |     7      | **7.1**   | Card list with browse/pagination         |
|    7 | COACTVWC   |   942 | Online |     7      |   5  |     8      | **6.7**   | Account view; multi-file reads           |
|    8 | COTRN02C   |   783 | Online |     7      |   8  |     7      | **7.3**   | Transaction add; writes TRANSACT VSAM    |
|    9 | COTRN00C   |   699 | Online |     7      |   5  |     7      | **6.4**   | Transaction list; browse/pagination      |
|   10 | CBTRN03C   |   649 | Batch  |     7      |   5  |     8      | **6.7**   | Report generation; multi-file joins      |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 9.7

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 4,237 (largest program in the entire codebase)             |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CAUP                                                       |
| **BMS Map**          | COACTUP (338 lines — largest map)                          |
| **Copybooks Used**   | 15 unique + 39 CSSETATY REPLACING inclusions               |
| **VSAM Files**       | ACCTDAT (R/W), CUSTDAT (R/W), CXACAIX (R) — 3 files       |
| **Key Operations**   | REWRITE on ACCTDAT and CUSTDAT; extensive field validation  |

**Why It's #1:**
- At 4,237 lines, it is **14× larger** than the average CICS program (300 LOC). This single module represents ~25% of all online COBOL code.
- Performs **REWRITE** operations on both Account and Customer master files — any bug here corrupts core financial data.
- Contains **39 COPY CSSETATY REPLACING** statements for dynamic BMS attribute setting — a pattern that is extremely difficult to auto-convert and requires manual Java/UI mapping.
- Includes comprehensive input validation for: SSN (3-part), US phone numbers (3-part), dates (year/month/day with leap-year logic), credit limits, FICO scores, and account status.
- Heavy use of 88-level condition names (~80+ flags) creating deeply nested branching logic.

**Modernization Recommendation:**
- Decompose into 4–5 Java service classes: `AccountService`, `CustomerService`, `AccountValidator`, `CustomerValidator`, `AccountUpdateController`.
- The 39 CSSETATY inclusions map to UI form field highlighting — implement as CSS classes or front-end validation in the target web framework.
- Estimated effort: **3–4 weeks** (manual refactoring required).

---

### #2 — CBTRN02C (Transaction Posting) — Score: 9.2

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 731                                                        |
| **Type**             | Batch                                                      |
| **JCL**              | POSTTRAN                                                   |
| **Copybooks Used**   | 5 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y)     |
| **Files Read**       | DALYTRAN, CARDXREF, ACCTDAT, TCATBALF                      |
| **Files Written**    | TRANSACT, ACCTDAT (rewrite), TCATBALF (write/rewrite), Rejects |

**Why It's #2:**
- **Core of the nightly batch cycle** — if this program fails, no transactions are posted, interest can't be calculated, and statements can't be generated. All downstream batch jobs depend on it.
- Performs **financial mutations across 4 files** in a single run: posts transactions, updates account balances, and maintains category balance totals.
- Generates a rejection file for invalid transactions — rejection logic is embedded and must be preserved exactly.
- Uses cross-reference lookups to validate card→account relationships before posting.

**Modernization Recommendation:**
- Implement as a Spring Batch job with chunk-oriented processing.
- Use database transactions to replace VSAM file-level integrity.
- Critical to build comprehensive regression test suite using `app/data/ASCII/` test data before conversion.
- Estimated effort: **2–3 weeks**.

---

### #3 — CBACT04C (Interest Calculation) — Score: 8.8

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 652                                                        |
| **Type**             | Batch                                                      |
| **JCL**              | INTCALC                                                    |
| **Copybooks Used**   | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y)     |
| **Files Read**       | TCATBALF, ACCTDAT, CARDXREF, DISCGRP                       |
| **Files Written**    | ACCTDAT (rewrite), TRANSACT (write)                        |

**Why It's #3:**
- **Financial calculation engine** — applies interest rates from Disclosure Groups to account category balances. Any arithmetic error directly impacts customer billing.
- Uses `PIC S9(10)V99` and `PIC S9(04)V99` signed decimal fields — COBOL packed decimal arithmetic has specific rounding and truncation behavior that must be exactly replicated in Java `BigDecimal`.
- Reads Disclosure Group rates and applies them per transaction category per account — a complex multi-dimensional lookup.
- Generates interest-charge transaction records that feed into the TRANSACT file.

**Modernization Recommendation:**
- Implement as a dedicated `InterestCalculationService` with `BigDecimal` precision.
- Build golden-file comparison tests: run COBOL with test data, capture output, then verify Java produces byte-identical results.
- Regulatory audit trail requirements — log all calculation parameters.
- Estimated effort: **2 weeks** (calculation logic is concentrated but financially critical).

---

### #4 — CBSTM03A (Statement Generation) — Score: 8.4

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 924                                                        |
| **Type**             | Batch                                                      |
| **JCL**              | CREASTMT                                                   |
| **Sub-programs**     | CBSTM03B (called 13 times for file I/O)                    |
| **Files Read**       | TRANSACT, CARDXREF, CUSTDAT, ACCTDAT (via CBSTM03B)       |
| **Files Written**    | Statement text file, HTML statement file                   |

**Why It's #4:**
- Generates **dual-format output** (text + HTML) requiring precise formatting logic — hundreds of WRITE statements with carefully laid out report lines.
- Uses a **sub-program call pattern** (CBSTM03A → CBSTM03B) with a shared work area for file I/O abstraction — this CALL interface must be correctly mapped.
- Joins data across 4 VSAM files to assemble complete statements per customer/card.
- HTML generation is embedded directly in COBOL WRITE statements — line-by-line HTML construction.

**Modernization Recommendation:**
- Replace with a template engine (Thymeleaf, FreeMarker) for HTML and a reporting library (JasperReports) for formatted text/PDF.
- The CBSTM03B sub-program maps to a `StatementDataAccessService`.
- Estimated effort: **2–3 weeks** (output format fidelity is the main challenge).

---

### #5 — COCRDUPC (Credit Card Update) — Score: 8.1

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 1,560                                                      |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CCUP                                                       |
| **BMS Map**          | COCRDUP (172 lines)                                        |
| **VSAM Files**       | CARDDAT (R/W), CUSTDAT (R)                                 |
| **Copybooks Used**   | 10+                                                        |

**Why It's #5:**
- Second-largest online program. Performs card data updates including **embossed name, expiry date, CVV, and active status** — all PCI DSS sensitive fields.
- **REWRITE** on CARDDAT — mutates card master records.
- Uses CSSTRPFY PF-key mapping and full input validation suite.
- Handles the edit-confirm-save workflow pattern common to all update screens.

**Modernization Recommendation:**
- Implement as REST endpoint (`PUT /api/cards/{cardNum}`) with Spring Validation.
- PCI DSS compliance: card number tokenization, CVV must not be stored post-authorization.
- Estimated effort: **2 weeks**.

---

### #6 — COCRDLIC (Credit Card List) — Score: 7.1

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 1,460                                                      |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CCLI                                                       |
| **BMS Map**          | COCRDLI (344 lines)                                        |
| **VSAM Files**       | CARDDAT (browse), CARDAIX (browse)                         |

**Why It's #6:**
- Implements **VSAM browse with forward/backward pagination** (STARTBR, READNEXT, READPREV, ENDBR) — a pattern that doesn't map directly to SQL pagination.
- Supports two browsing modes: all cards (admin) vs. cards filtered by account (regular user).
- Handles selection actions ('S' for view, 'U' for update) with dispatch to COCRDSLC or COCRDUPC.
- 7-row display array with per-row selection validation.

**Modernization Recommendation:**
- Replace browse pattern with Spring Data JPA paginated queries.
- Selection dispatch maps to front-end routing (React Router or similar).
- Estimated effort: **1.5 weeks**.

---

### #7 — COACTVWC (Account View) — Score: 6.7

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 942                                                        |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CAVW                                                       |
| **VSAM Files**       | CXACAIX (R), ACCTDAT (R), CUSTDAT (R)                     |
| **Copybooks Used**   | 14                                                         |

**Why It's #7:**
- Read-only program but joins data from **3 VSAM files** to compose a single account view screen — cross-reference lookup, then account read, then customer read.
- Includes an **abend handler** (EXEC CICS HANDLE ABEND) with custom error routing.
- Uses 14 copybooks — one of the highest dependency counts among online programs.

**Modernization Recommendation:**
- Implement as `GET /api/accounts/{acctId}` with JPA entity graph / join fetch.
- Straightforward conversion since no data mutation occurs.
- Estimated effort: **1 week**.

---

### #8 — COTRN02C (Transaction Add) — Score: 7.3

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 783                                                        |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CT02                                                       |
| **VSAM Files**       | TRANSACT (R/W), CXACAIX (R), CARDXREF (R), ACCTDAT (R)    |
| **Calls**            | CSUTLDTC (date validation)                                 |

**Why It's #8:**
- **Creates new financial transaction records** — directly affects account balances and feeds into the batch posting cycle.
- Performs cross-reference validation (card→account), date validation via CSUTLDTC utility call, and generates unique transaction IDs.
- Reads the last transaction via READPREV to generate sequential transaction IDs.
- WRITE to TRANSACT VSAM — direct financial data creation.

**Modernization Recommendation:**
- Implement as `POST /api/transactions` with Spring validation and @Transactional.
- Transaction ID generation should migrate to database sequences.
- Estimated effort: **1.5 weeks**.

---

### #9 — COTRN00C (Transaction List) — Score: 6.4

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 699                                                        |
| **Type**             | Online CICS                                                |
| **Transaction ID**   | CT00                                                       |
| **VSAM Files**       | TRANSACT (browse)                                          |

**Why It's #9:**
- Implements VSAM **browse with bidirectional pagination** similar to COCRDLIC.
- Supports filtering by account ID and transaction ID range.
- 'S' selection dispatches to COTRN01C (view).

**Modernization Recommendation:**
- Replace with paginated REST endpoint (`GET /api/transactions?page=N`).
- Estimated effort: **1 week**.

---

### #10 — CBTRN03C (Transaction Report) — Score: 6.7

| Metric               | Value                                                      |
|----------------------|------------------------------------------------------------|
| **Lines of Code**    | 649                                                        |
| **Type**             | Batch                                                      |
| **JCL**              | TRANREPT                                                   |
| **Files Read**       | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, Date Parms         |
| **Files Written**    | Report output file                                         |

**Why It's #10:**
- Joins data from **5 different files** to produce the Daily Transaction Report — the most complex multi-file join in the batch suite.
- Uses the CVTRA07Y report layout copybook with page/account/grand totals.
- Date-range filtering from parameter file — batch jobs parameterized externally.
- Report formatting with control breaks (by account, by page).

**Modernization Recommendation:**
- Implement with JasperReports or Spring Batch with FlatFileItemWriter.
- The 5-file join maps naturally to a SQL query with JOINs.
- Estimated effort: **1.5 weeks**.

---

## Effort Summary

| Rank | Program    | Est. Effort | Cumulative |
|-----:|------------|:-----------:|:----------:|
|    1 | COACTUPC   |  3–4 weeks  |   4 weeks  |
|    2 | CBTRN02C   |  2–3 weeks  |   7 weeks  |
|    3 | CBACT04C   |    2 weeks  |   9 weeks  |
|    4 | CBSTM03A   |  2–3 weeks  |  12 weeks  |
|    5 | COCRDUPC   |    2 weeks  |  14 weeks  |
|    6 | COCRDLIC   | 1.5 weeks   | 15.5 weeks |
|    7 | COACTVWC   |    1 week   | 16.5 weeks |
|    8 | COTRN02C   | 1.5 weeks   |  18 weeks  |
|    9 | COTRN00C   |    1 week   |  19 weeks  |
|   10 | CBTRN03C   | 1.5 weeks   | 20.5 weeks |

**Total estimated effort for Top 10:** ~18–21 weeks (single developer)

**Remaining 31 programs:** ~8–10 weeks (simpler programs, menus, utilities, read-only views)

**Full application estimate:** ~26–31 weeks (single developer), or ~10–13 weeks (3-person team with parallel tracks)

---

## Recommended Migration Sequence

Based on dependency analysis and risk mitigation:

| Wave | Programs                                           | Duration   | Rationale                                  |
|------|----------------------------------------------------|------------|--------------------------------------------|
| **1 — Foundation** | COCOM01Y (COMMAREA), CSUTLDTC, data model, CSUSR01Y | 2 weeks | Shared infrastructure; unblocks everything |
| **2 — Auth & Users** | COSGN00C, COUSR00-03C                          | 2 weeks    | Low risk; validates architecture patterns  |
| **3 — Read-Only**    | COACTVWC, COCRDSLC, COTRN00C, COTRN01C        | 3 weeks    | No data mutation; safe to test early       |
| **4 — Card Mgmt**    | COCRDLIC, COCRDUPC                             | 3 weeks    | Browse + update; moderate complexity       |
| **5 — Transactions** | COTRN02C (online), CBTRN02C (batch posting)    | 3 weeks    | Core financial writes; needs thorough testing |
| **6 — Financials**   | CBACT04C, COACTUPC, COBIL00C                   | 4 weeks    | Highest risk; interest calc + account update |
| **7 — Reporting**    | CBSTM03A/B, CBTRN03C, CORPT00C, CBTRN01C      | 3 weeks    | Output fidelity testing                    |
| **8 — Utilities**    | CBEXPORT, CBIMPORT, remaining batch utilities  | 2 weeks    | Data migration tooling                     |
| **9 — Navigation**   | COMEN01C, COADM01C (menu → routing)            | 1 week     | Simple once target UI framework is chosen  |
| **10 — Optional**    | Authorization, Tran Type DB2, VSAM-MQ modules  | 3–4 weeks  | Independent modules; can parallelize       |

---

## Key Risk Mitigations

| Risk                              | Mitigation                                                              |
|-----------------------------------|-------------------------------------------------------------------------|
| COBOL decimal arithmetic drift    | Use `BigDecimal` with `ROUND_HALF_EVEN`; golden-file comparison tests   |
| VSAM browse → SQL pagination      | Implement keyset pagination (not OFFSET) to preserve COBOL browse semantics |
| PII exposure during migration     | Encrypt SSN/card data at rest from Day 1; implement tokenization early  |
| Batch sequence integrity          | Implement Spring Batch job orchestration with step dependencies         |
| BMS attribute manipulation (39×)  | Map CSSETATY REPLACING to CSS classes; manual UI work required          |
| COMMAREA state management         | Map to HTTP session or JWT claims; test state transitions thoroughly    |
| EBCDIC ↔ ASCII data conversion    | Use test data in `app/data/ASCII/` for all regression testing           |
