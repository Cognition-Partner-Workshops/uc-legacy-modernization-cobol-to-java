# CardDemo Hotspot Report

> **System**: CardDemo -- Credit Card Management System
> **Date**: 2026-03-25
> **Purpose**: Identify the top 10 modules requiring the most attention during modernization, prioritized by complexity, risk, and business impact.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Summary](#top-10-hotspot-summary)
3. [Detailed Analysis](#detailed-analysis)
4. [Modernization Risk Matrix](#modernization-risk-matrix)
5. [Recommended Migration Order](#recommended-migration-order)

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|---|---|---|
| **Complexity** | 35% | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), number of file I/O operations, copybook dependencies, validation logic density |
| **Risk** | 35% | Data sensitivity (PII/PCI), financial calculation accuracy, error handling gaps, cross-program dependencies, state management complexity |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, downstream dependencies, data integrity role, regulatory implications |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Business Impact x 0.30)

---

## Top 10 Hotspot Summary

| Rank | Module | Type | Lines | Complexity | Risk | Impact | **Composite** |
|---|---|---|---|---|---|---|---|
| **1** | `COACTUPC` | Online | 4,236 | 10 | 9 | 9 | **9.35** |
| **2** | `CBTRN02C` | Batch | 731 | 8 | 10 | 10 | **9.30** |
| **3** | `CBACT04C` | Batch | 652 | 8 | 9 | 9 | **8.65** |
| **4** | `CBSTM03A` | Batch | 924 | 9 | 7 | 8 | **8.00** |
| **5** | `COBIL00C` | Online | 572 | 6 | 9 | 8 | **7.65** |
| **6** | `COCRDUPC` | Online | 1,560 | 8 | 8 | 6 | **7.40** |
| **7** | `COCRDLIC` | Online | 1,459 | 8 | 7 | 7 | **7.35** |
| **8** | `COSGN00C` | Online | 260 | 4 | 9 | 8 | **6.95** |
| **9** | `COTRN02C` | Online | 783 | 7 | 7 | 6 | **6.70** |
| **10** | `CBTRN03C` | Batch | 649 | 7 | 6 | 7 | **6.65** |

---

## Detailed Analysis

### #1: COACTUPC -- Account Update (Composite: 9.35)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **10** | Largest program in codebase at 4,236 lines. Extensive field-level validation: phone area codes, state codes, ZIP codes, date ranges, credit limits. Uses CSLKPCDY (1,318-line lookup table). Multiple EVALUATE/IF blocks for each field. 5+ VSAM file reads (ACCTFILE, CARDFILE, CUSTFILE). Complex screen re-entry logic with COMMAREA state. |
| Risk | **9** | Modifies account master data (balances, limits, dates). PII exposure (customer address, phone). Financial fields (credit limits, balances) require exact decimal precision. Any bug directly corrupts account data. Multiple EXEC CICS READ/REWRITE operations with error handling. |
| Business Impact | **9** | Core account maintenance function used by all operators. Incorrect updates cascade to interest calculations, statements, and billing. Highest user interaction frequency for account-related tasks. |

**Modernization Concerns**:
- Massive monolithic program should be decomposed into: validation service, account service, customer service, UI controller
- Embedded validation lookup tables (1,318 lines of area codes) should become database reference tables or external validation services
- Field-level error highlighting logic (`CSSETATY.cpy`) needs UI framework equivalent
- CICS pseudo-conversational pattern (COMMAREA re-entry) maps to HTTP session/state management

---

### #2: CBTRN02C -- Transaction Posting (Composite: 9.30)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 731 lines with multi-file I/O: reads DALYTRAN, XREFFILE; updates ACCTFILE, TRANFILE, TCATBALF; writes DALYREJS. Complex validation: card existence, account status, balance limits. Transaction categorization logic. Reject handling with detailed error codes. |
| Risk | **10** | Core financial posting engine. Errors directly affect account balances. Dual-entry accounting: must update both ACCTFILE and TCATBALF atomically. Reject file (DALYREJS) is only audit trail for failed postings. No rollback mechanism -- partial failures leave inconsistent state. |
| Business Impact | **10** | Central to daily batch cycle. All transactions flow through this program. Downstream: interest calculation, statements, and reports all depend on correct posting. Revenue-critical: incorrect postings = financial loss. |

**Modernization Concerns**:
- Must implement database transactions (BEGIN/COMMIT/ROLLBACK) to replace VSAM's lack of atomic multi-file updates
- Reject handling needs structured error reporting (not just sequential file output)
- Consider event-driven architecture: transaction posted -> triggers downstream processing
- Performance: current sequential processing should be parallelized in Java

---

### #3: CBACT04C -- Interest Calculation (Composite: 8.65)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 652 lines with financial calculation logic. Reads 5 files: TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT. Multi-level interest rate lookup: account -> group -> type -> category -> rate. Iterates over all transaction category balances per account. |
| Risk | **9** | Financial calculation accuracy is paramount -- rounding errors compound across thousands of accounts. Interest rates from DISCGRP must be applied precisely. Creates new transactions in TRANSACT for interest charges. Updates ACCTFILE balances. Regulatory compliance: interest calculation rules are audited. |
| Business Impact | **9** | Directly affects revenue (interest income). Runs daily in batch cycle. Errors are difficult to detect and reverse. Downstream: statements show interest charges, so errors are customer-visible. |

**Modernization Concerns**:
- Use `BigDecimal` with explicit rounding modes (HALF_UP for financial) -- never `double`/`float`
- Interest calculation rules should be externalized as configurable business rules (not hardcoded COBOL logic)
- Consider a rules engine for complex rate determination (account group x transaction type x category)
- Must preserve exact decimal behavior of COBOL `PIC S9(10)V99` arithmetic

---

### #4: CBSTM03A -- Statement Generation (Composite: 8.00)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **9** | 924 lines with complex report formatting. Page layout management: headers, detail lines, subtotals, page breaks, overflow handling. Calls CBSTM03B for overflow. Multiple output formats (print spool + HTML). Date range filtering. Account-level and grand total accumulation. |
| Risk | **7** | Customer-facing output -- format errors are visible but not financially destructive. Statement accuracy depends on correct upstream data. HTML generation adds format risk. Page overflow logic is fragile. |
| Business Impact | **8** | Regulatory requirement: must produce monthly statements. Customer communication channel. Audit trail for account activity. Errors erode customer trust. |

**Modernization Concerns**:
- Replace print-spool formatting with modern reporting framework (JasperReports, PDF generation)
- HTML output should use template engine, not COBOL string concatenation
- CBSTM03A + CBSTM03B should merge into a single service with proper pagination
- Consider generating statements on-demand (not just batch) for modern self-service

---

### #5: COBIL00C -- Bill Payment (Composite: 7.65)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **6** | 572 lines -- moderate size. Reads XREFFILE and ACCTFILE, updates ACCTFILE balance, writes new transaction to TRANSACT. Validation of payment amount against balance. |
| Risk | **9** | Direct financial transaction: modifies account balance. Creates debit transactions. No reversal mechanism in current code. EXEC CICS REWRITE on ACCTFILE must succeed atomically with WRITE to TRANSACT. Partial failure = inconsistent data. |
| Business Impact | **8** | Revenue-critical: bill payments reduce account balances. High-frequency user operation. Errors directly affect customer accounts and potentially result in disputes. |

**Modernization Concerns**:
- Critical to wrap in database transaction for atomicity (balance update + transaction creation)
- Add idempotency key to prevent duplicate payments
- Implement payment reversal/void capability
- Consider async payment processing with confirmation for large amounts

---

### #6: COCRDUPC -- Credit Card Update (Composite: 7.40)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 1,560 lines with field-level validation for card data. Date validation via CSUTLDTC. Multi-file reads (CARDFILE, ACCTFILE). Screen re-entry with COMMAREA state tracking. Error highlighting and message management. |
| Risk | **8** | Modifies card master data. Card status changes affect downstream transaction processing. Expiration date changes have PCI implications. CVV is a PCI-sensitive field. |
| Business Impact | **6** | Important but lower frequency than account updates. Card status changes (activate/deactivate) are operationally significant. |

**Modernization Concerns**:
- PCI-DSS compliance: CVV should never be displayed or stored in plain text
- Card status changes should trigger events (e.g., notify fraud system)
- Merge validation logic with a shared validation service (reuse across card list/view/update)
- Field-level error handling pattern is repeated across many programs -- abstract into framework

---

### #7: COCRDLIC -- Credit Card List (Composite: 7.35)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **8** | 1,459 lines. Complex paginated browse logic: STARTBR/READNEXT/READPREV for forward/backward scrolling. Filter by account. Multiple XCTL targets (view, update). Screen state management across pseudo-conversational CICS. Generic key positioning for browse restart. |
| Risk | **7** | Read-only display, but card numbers (PAN) are PCI-sensitive. Navigation bugs could display wrong customer's cards. Cross-reference lookup (XREFFILE + CARDFILE) must stay consistent. |
| Business Impact | **7** | Primary card lookup interface. Gateway to card view/update functions. Used frequently by operators. |

**Modernization Concerns**:
- VSAM browse (STARTBR/READNEXT/READPREV) maps to database cursor/pagination (LIMIT/OFFSET or keyset pagination)
- PCI compliance: card numbers must be masked in transit and at rest
- Paginated list pattern is reusable -- build a generic paginated list component
- XCTL-based drill-down (list -> view/update) maps to REST API + SPA routing

---

### #8: COSGN00C -- Sign-On (Composite: 6.95)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **4** | 260 lines -- relatively simple. Reads USRSEC file, compares credentials, routes to menu. Straightforward logic. |
| Risk | **9** | Security gateway: all access flows through sign-on. Passwords stored in plain text (PIC X(08)). No password hashing, no brute-force protection, no session timeout, no MFA. Authentication bypass = full system compromise. |
| Business Impact | **8** | Single entry point for all users. Authentication failure blocks all operations. Security audit finding. |

**Modernization Concerns**:
- **Critical security gap**: plain-text passwords must be replaced with bcrypt/Argon2 hashing
- Implement proper session management (JWT/OAuth2) instead of COMMAREA-based user ID passing
- Add MFA, account lockout, password complexity rules
- Consider integration with enterprise identity provider (LDAP, SAML, OIDC)
- This should be modernized early to establish the security foundation

---

### #9: COTRN02C -- Transaction Add (Composite: 6.70)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **7** | 783 lines with multi-field validation. Validates transaction type (TRANTYPE), category (TRANCATG), dates (CSUTLDTC), and card (XREFFILE). Generates transaction ID. Writes to TRANSACT via EXEC CICS WRITE. Browse operations for ID generation. |
| Risk | **7** | Creates new financial transactions. Validation errors could allow invalid transactions. Transaction ID generation must be unique. Writes directly to TRANSACT VSAM. |
| Business Impact | **6** | Enables manual transaction entry by operators. Lower volume than batch posting but important for adjustments and corrections. |

**Modernization Concerns**:
- Transaction ID generation should use database sequences or UUIDs
- Validation rules should be externalized and shared with batch posting (CBTRN02C)
- Consider making this a REST API endpoint for integration with external systems
- Add transaction approval workflow for amounts above threshold

---

### #10: CBTRN03C -- Daily Transaction Report (Composite: 6.65)

| Dimension | Score | Rationale |
|---|---|---|
| Complexity | **7** | 649 lines. Multi-file input: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM. Report formatting with headers, detail lines, page/account/grand totals. Date range filtering. Uses CVTRA07Y report layout copybook. |
| Risk | **6** | Read-only reporting -- no data modification. Incorrect reports could mislead operations but don't corrupt data. Date parameter handling must be accurate. |
| Business Impact | **7** | Key operational report for daily reconciliation. Used by operations and management. Regulatory requirement for transaction audit trail. |

**Modernization Concerns**:
- Replace fixed-format report with modern reporting framework
- Enable on-demand report generation (not just batch)
- Add export formats: PDF, CSV, Excel
- Consider real-time dashboards as supplement to batch reports

---

## Modernization Risk Matrix

```
                     HIGH RISK
                        |
    COSGN00C (auth)     |     CBTRN02C (posting)
    COBIL00C (payment)  |     CBACT04C (interest)
                        |     COACTUPC (acct update)
                        |
  LOW IMPACT -----------+----------- HIGH IMPACT
                        |
    COCRDUPC (card upd)  |     CBSTM03A (statements)
    COCRDLIC (card list) |     CBTRN03C (reporting)
    COTRN02C (tran add)  |
                        |
                     LOW RISK
```

---

## Recommended Migration Order

Based on the hotspot analysis, the recommended migration sequence balances risk reduction, dependency management, and incremental value delivery.

### Phase 1: Foundation (Weeks 1-4)

| Priority | Module | Rationale |
|---|---|---|
| 1a | `COSGN00C` | Security foundation. Small (260 lines) but critical. Establishes authentication framework (JWT/OAuth2), password hashing, session management. All other modules depend on auth. |
| 1b | `CSUSR01Y` + `COCOM01Y` | Data model foundation. Define User and COMMAREA as Java POJOs/DTOs. Used by every online program. |
| 1c | `CSUTLDTC` | Shared utility. Date validation called by COTRN02C and CORPT00C. COACTUPC uses inline date validation via CSUTLDPY/CSUTLDWY copybooks. Build as reusable Java service. |

### Phase 2: Core Data (Weeks 5-8)

| Priority | Module | Rationale |
|---|---|---|
| 2a | `CVACT01Y` + `CVACT02Y` + `CVCUS01Y` + `CVACT03Y` | Core entities. Define Account, Card, Customer, CrossRef as JPA entities. Database schema creation. |
| 2b | `CBACT01C` + `CBACT02C` + `CBACT03C` + `CBCUS01C` | Data loaders. Simple programs (178-430 lines). Establish batch processing framework (Spring Batch). |
| 2c | `COACTVWC` | Account View. Read-only, moderate complexity. First online screen -- validates CICS-to-REST pattern. |

### Phase 3: Financial Core (Weeks 9-14)

| Priority | Module | Rationale |
|---|---|---|
| 3a | `CBTRN02C` | Transaction posting. Highest business impact. Requires atomic database transactions. |
| 3b | `CBACT04C` | Interest calculation. Depends on correct posting. Requires exact decimal arithmetic. |
| 3c | `COBIL00C` | Bill payment. Direct financial operations. Requires atomicity pattern from 3a. |

### Phase 4: Full CRUD (Weeks 15-20)

| Priority | Module | Rationale |
|---|---|---|
| 4a | `COACTUPC` | Largest program. Decompose into multiple services. Depends on validation framework. |
| 4b | `COCRDUPC` + `COCRDLIC` + `COCRDSLC` | Card management suite. Reuse patterns from account modules. |
| 4c | `COTRN00C` + `COTRN01C` + `COTRN02C` | Transaction management suite. Depends on posting framework from Phase 3. |

### Phase 5: Reporting & Admin (Weeks 21-24)

| Priority | Module | Rationale |
|---|---|---|
| 5a | `CBSTM03A` + `CBSTM03B` | Statement generation. Replace with modern reporting framework. |
| 5b | `CBTRN03C` + `CORPT00C` | Transaction reports. Build on reporting framework from 5a. |
| 5c | `COUSR00C-03C` + `COADM01C` | Admin functions. Lower risk, lower impact. |

### Phase 6: Data Migration & Optional (Weeks 25-28)

| Priority | Module | Rationale |
|---|---|---|
| 6a | `CBEXPORT` + `CBIMPORT` | Data migration utilities. Adapt for final data cutover. |
| 6b | Optional modules (DB2, IMS/MQ) | Only if target architecture requires equivalent functionality. |
| 6c | JCL jobs -> scheduled tasks | Convert batch orchestration to modern scheduler (cron, Quartz, Spring Batch). |

---

## Key Modernization Risks

| Risk | Affected Modules | Mitigation |
|---|---|---|
| **Decimal precision loss** | CBTRN02C, CBACT04C, COBIL00C, COACTUPC | Use `BigDecimal` exclusively for all financial fields. Define rounding rules. Run parallel calculations during transition. |
| **Plain-text passwords** | COSGN00C, CSUSR01Y | Immediate security remediation. Hash all passwords before go-live. |
| **PCI data exposure** | COCRDLIC, COCRDSLC, COCRDUPC, CVACT02Y | Implement card masking, tokenization. Never store CVV. Encrypt PAN at rest. |
| **Atomic multi-file updates** | CBTRN02C, COBIL00C, CBACT04C | Replace VSAM I-O with database transactions (ACID). Implement compensating transactions for failures. |
| **CICS pseudo-conversational state** | All online programs | Replace COMMAREA with HTTP session or JWT claims. Implement stateless REST API design. |
| **Embedded validation tables** | COACTUPC (CSLKPCDY) | Extract to database reference tables or external validation API. |
| **Report formatting** | CBSTM03A, CBTRN03C | Replace COBOL print formatting with template engine (JasperReports, Thymeleaf). |
| **Batch sequencing** | JCL job chain | Implement workflow orchestration (Spring Batch, Airflow). Maintain dependency graph. |
