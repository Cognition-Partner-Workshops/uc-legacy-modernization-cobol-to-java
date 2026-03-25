# Hotspot Report - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Methodology:** Weighted scoring across Complexity, Risk, and Business Impact

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 40% | Lines of code, number of file I/O operations, number of copybook dependencies, validation logic depth, branching complexity |
| **Risk** | 35% | Data integrity impact, error handling gaps, PII exposure, financial calculation sensitivity, multi-file update atomicity |
| **Business Impact** | 25% | Core vs. optional, user-facing vs. batch, revenue impact, regulatory exposure |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.35) + (Business Impact × 0.25)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update (Score: 9.40)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 10 | 4,236 lines — largest program in the codebase. Extensive field-by-field validation (SSN, phone, dates, credit limits, ZIP codes). Complex data-change tracking with before/after comparison. Reads/writes 4 VSAM files (ACCTDAT, CUSTDAT, CARDDAT, CXACAIX). Multiple REDEFINES and EVALUATE blocks. |
| **Risk** | 9 | Updates financial master data (credit limits, balances). Handles PII (SSN, DOB, addresses). Multi-file REWRITE without true transaction atomicity — partial update risk if abend occurs mid-update. No rollback mechanism for cross-file changes. |
| **Business Impact** | 9 | Core user-facing function for account maintenance. Incorrect updates directly affect customer balances and credit limits. Regulatory exposure (PII handling). |

**Modernization Recommendations:**
- Decompose into smaller service methods (validation, persistence, change tracking)
- Wrap multi-file updates in a database transaction
- Add audit logging for all account changes
- Implement field-level encryption for PII
- Estimated effort: **High** (3-4 weeks)

---

### Rank 2: CBTRN02C — Transaction Posting (Score: 8.70)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 8 | 731 lines. Reads DALYTRAN, cross-validates against XREFFILE, updates ACCTFILE balances and TCATBALF category balances. Writes rejected records to DALYREJS. Multi-step validation pipeline. |
| **Risk** | 10 | Core financial processing — posts transactions to account balances. Incorrect posting directly affects customer balances. Rejected transaction handling must be bulletproof. Updates 3 files atomically (TRANFILE, ACCTFILE, TCATBALF). |
| **Business Impact** | 8 | Heart of the daily batch cycle. Every transaction flows through this program. Failure stops the entire batch pipeline. |

**Modernization Recommendations:**
- Implement as a Spring Batch job with chunk-oriented processing
- Add retry/skip policies for individual transaction failures
- Database transactions for atomicity
- Comprehensive audit trail for posted vs. rejected transactions
- Estimated effort: **High** (2-3 weeks)

---

### Rank 3: COCRDUPC — Card Update (Score: 8.15)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 9 | 1,560 lines. Second-largest online program. Card field validation (card number format, expiry dates, CVV, embossed name). Reads CARDDAT via both primary key and alternate index. REWRITE to CARDDAT. Uses CSSTRPFY string utility. |
| **Risk** | 8 | Updates PCI-sensitive card data (card numbers, CVV, expiry). PCI DSS compliance implications. Card status changes affect transaction authorization. |
| **Business Impact** | 7 | User-facing card maintenance. Incorrect card data can block legitimate transactions or enable unauthorized ones. |

**Modernization Recommendations:**
- PCI DSS compliant field handling (tokenization, encryption)
- Never store CVV post-authorization
- Input validation as reusable service
- Estimated effort: **Medium-High** (2-3 weeks)

---

### Rank 4: CBACT04C — Interest Calculation (Score: 7.95)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 7 | 652 lines. Reads 5 files (TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANFILE). Multi-level interest rate lookup (account group → transaction category → disclosure rate). Generates interest transaction records. |
| **Risk** | 9 | Financial calculation accuracy is critical — directly affects customer statements and balances. Rounding errors compound across accounts. Interest rate lookup chain must resolve correctly. Writes both interest transactions and updated balances. |
| **Business Impact** | 8 | Core financial function. Interest calculation errors have regulatory and legal consequences. Affects every active account monthly. |

**Modernization Recommendations:**
- Use BigDecimal for all monetary calculations
- Comprehensive unit tests for interest rate scenarios
- Reconciliation report for calculated vs. expected interest
- Estimated effort: **Medium** (2 weeks)

---

### Rank 5: COCRDLIC — Card List (Score: 7.40)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 8 | 1,459 lines. Paginated list display with STARTBR/READNEXT/ENDBR VSAM browsing. Handles forward/backward pagination, record selection, and alternate index navigation. Dynamic BMS field attribute management. |
| **Risk** | 7 | Displays PCI-sensitive card numbers on screen. Pagination state management across pseudo-conversational CICS interactions. |
| **Business Impact** | 7 | Primary card lookup interface. Gateway to card detail/update functions. Performance directly affects user productivity. |

**Modernization Recommendations:**
- Server-side pagination with offset/limit queries
- Card number masking in list view (show last 4 only)
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 6: CBSTM03A — Statement Generation (Score: 7.40)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 8 | 924 lines. Reads 4 files, produces dual-format output (plain text + HTML). Calls CBSTM03B subroutine for file I/O. Complex report layout formatting with page breaks, headers, account/page/grand totals. |
| **Risk** | 7 | Statement accuracy is customer-facing and legally binding. HTML generation creates injection risk if data contains special characters. Multi-format output doubles testing surface. |
| **Business Impact** | 7 | Customer statements are a regulatory requirement. Errors in statements lead to disputes and compliance issues. |

**Modernization Recommendations:**
- Template engine for statement formatting (Thymeleaf/FreeMarker)
- PDF generation instead of text+HTML
- Statement data model separate from rendering
- Estimated effort: **Medium** (2 weeks)

---

### Rank 7: COTRN02C — Transaction Add (Score: 7.35)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 7 | 783 lines. Validates transaction fields, resolves card → account via CXACAIX cross-reference, writes to TRANSACT VSAM. Date/amount/merchant validation. |
| **Risk** | 8 | Creates new financial transactions. Incorrect transaction amounts directly affect account balances. Must validate card is active and account exists before posting. |
| **Business Impact** | 7 | Core transaction entry for online users. Revenue-generating function. |

**Modernization Recommendations:**
- Idempotency key to prevent duplicate transactions
- Input validation as shared service
- Async processing for high-volume scenarios
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 8: CBTRN03C — Transaction Report (Score: 6.75)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 7 | 649 lines. Reads 5 files (TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM). Complex report formatting with headers, detail lines, page/account/grand totals. Date-range filtering. |
| **Risk** | 7 | Report accuracy affects management decisions and audit compliance. Joins across 5 files must be consistent. Date parameter handling must be precise. |
| **Business Impact** | 6 | Daily operational report. Used for reconciliation and audit. Not directly customer-facing. |

**Modernization Recommendations:**
- SQL-based reporting with GROUP BY aggregation
- Parameterized report with date range, account filters
- Export to CSV/PDF
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 9: COBIL00C — Bill Payment (Score: 6.70)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 6 | 572 lines. Resolves card → account via CXACAIX, reads account balance, creates payment transaction, updates account balance. |
| **Risk** | 8 | Financial transaction — pays account balance in full. Creates a debit transaction and updates the account balance. Two-file update (TRANSACT write + ACCTDAT rewrite) without atomicity. |
| **Business Impact** | 6 | Customer-facing payment function. Directly affects account balances. Revenue-critical. |

**Modernization Recommendations:**
- Database transaction for payment + balance update atomicity
- Payment confirmation with idempotency
- Partial payment support (currently only full balance)
- Estimated effort: **Medium** (1-2 weeks)

---

### Rank 10: COTRN00C — Transaction List (Score: 6.65)

| Dimension | Score | Justification |
|-----------|-------|---------------|
| **Complexity** | 7 | 699 lines. Paginated transaction browsing with VSAM STARTBR/READNEXT. Filter by account/card. Dynamic column display. |
| **Risk** | 6 | Read-only, but displays financial transaction data. Pagination state must be consistent across pseudo-conversational interactions. |
| **Business Impact** | 7 | Primary transaction inquiry screen. Most frequently accessed function for customer service. |

**Modernization Recommendations:**
- Indexed database queries with pagination
- Search/filter capabilities
- Estimated effort: **Low-Medium** (1 week)

---

## Summary Ranking Table

| Rank | Program | Type | Lines | Complexity | Risk | Biz Impact | **Score** | Primary Concern |
|------|---------|------|-------|:----------:|:----:|:----------:|:---------:|----------------|
| 1 | **COACTUPC** | Online | 4,236 | 10 | 9 | 9 | **9.40** | Massive size, multi-file PII updates |
| 2 | **CBTRN02C** | Batch | 731 | 8 | 10 | 8 | **8.70** | Core financial posting, atomicity |
| 3 | **COCRDUPC** | Online | 1,560 | 9 | 8 | 7 | **8.15** | PCI card data, complex validation |
| 4 | **CBACT04C** | Batch | 652 | 7 | 9 | 8 | **7.95** | Interest calculation accuracy |
| 5 | **COCRDLIC** | Online | 1,459 | 8 | 7 | 7 | **7.40** | Pagination complexity, card display |
| 5 | **CBSTM03A** | Batch | 924 | 8 | 7 | 7 | **7.40** | Dual-format output, report accuracy |
| 7 | **COTRN02C** | Online | 783 | 7 | 8 | 7 | **7.35** | Transaction creation, validation |
| 8 | **CBTRN03C** | Batch | 649 | 7 | 7 | 6 | **6.75** | Multi-file join, report accuracy |
| 9 | **COBIL00C** | Online | 572 | 6 | 8 | 6 | **6.70** | Payment atomicity, balance updates |
| 10 | **COTRN00C** | Online | 699 | 7 | 6 | 7 | **6.65** | Pagination, high-traffic screen |

---

## Modernization Priority Recommendation

### Wave 1 — High Priority (Immediate)
| Module | Rationale | Estimated Effort |
|--------|-----------|-----------------|
| COACTUPC | Largest, most complex, highest risk. Decompose first. | 3-4 weeks |
| CBTRN02C | Core batch financial processing. Foundation for all batch. | 2-3 weeks |
| CBACT04C | Interest calculations — highest financial accuracy need. | 2 weeks |

### Wave 2 — Medium Priority
| Module | Rationale | Estimated Effort |
|--------|-----------|-----------------|
| COCRDUPC | PCI compliance driver. Card data handling modernization. | 2-3 weeks |
| COTRN02C | Transaction creation — pairs with CBTRN02C posting. | 1-2 weeks |
| COBIL00C | Payment atomicity. Revenue-critical function. | 1-2 weeks |

### Wave 3 — Standard Priority
| Module | Rationale | Estimated Effort |
|--------|-----------|-----------------|
| COCRDLIC | Pagination modernization. Card list/search. | 1-2 weeks |
| CBSTM03A | Statement generation. Template-based approach. | 2 weeks |
| COTRN00C | Transaction list. Standard CRUD modernization. | 1 week |
| CBTRN03C | Reporting. SQL-based replacement. | 1-2 weeks |

### Cross-Cutting Concerns (All Waves)
- **COCOM01Y (COMMAREA) → HTTP Session/JWT:** Affects all 17 online programs
- **CSUSR01Y (User Security) → Spring Security:** Password hashing, role-based access
- **BMS Maps → Web UI:** All 17 screens need frontend equivalent
- **VSAM → RDBMS:** Schema migration for all 10+ VSAM datasets
- **CICS Pseudo-conversational → Stateless REST:** Architectural pattern change

---

## Risk Heatmap

```
                    LOW Business Impact    MED Business Impact    HIGH Business Impact
                   ─────────────────────  ─────────────────────  ─────────────────────
HIGH Risk         │                      │ COBIL00C             │ CBTRN02C, CBACT04C  │
                  │                      │                      │ COACTUPC             │
                  ├──────────────────────┼──────────────────────┼─────────────────────┤
MED Risk          │ CBTRN03C             │ COTRN02C, COCRDLIC   │ COCRDUPC             │
                  │                      │ CBSTM03A             │                      │
                  ├──────────────────────┼──────────────────────┼─────────────────────┤
LOW Risk          │ CBACT01C-03C         │ COTRN00C, COTRN01C   │ COSGN00C             │
                  │ CBCUS01C             │                      │                      │
                  └──────────────────────┴──────────────────────┴─────────────────────┘
```

---

## Appendix: Optional Module Hotspots

These modules are not ranked in the top 10 because they are optional extensions, but they warrant attention if deployed:

| Module | Lines | Concern |
|--------|-------|---------|
| COTRTLIC (DB2) | 2,098 | Largest optional program. DB2 cursor management adds complexity. |
| COTRTUPC (DB2) | 1,702 | Embedded SQL with dynamic transaction type CRUD. |
| COPAUS0C (IMS) | 1,032 | IMS database access + BMS. Most complex authorization program. |
| COPAUA0C (MQ) | 1,026 | MQ trigger-based authorization. Real-time decision engine. |
