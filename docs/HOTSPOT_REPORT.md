# Hotspot Report — CardDemo Mainframe System

> **Generated**: 2026-05-27 | **Methodology**: Static complexity analysis + business impact assessment

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale):

| Dimension | Criteria |
|:----------|:---------|
| **Complexity** | LOC, cyclomatic complexity (IF/EVALUATE branches), number of paragraphs/sections, data structures accessed, copybooks included |
| **Risk** | Number of file I/O operations, CICS API calls, external integrations (DB2/IMS/MQ), error handling patterns, data mutation scope |
| **Business Impact** | Revenue criticality, number of downstream dependents, user-facing vs. internal, data integrity implications |

**Priority Score** = (Complexity × 0.4) + (Risk × 0.35) + (Business Impact × 0.25)

---

## Top 10 Hotspot Modules

| Rank | Program | LOC | Priority Score | Complexity | Risk | Impact | Recommendation |
|:----:|:--------|----:|:--------------:|:----------:|:----:|:------:|:---------------|
| 1 | COACTUPC | 4,236 | **9.15** | 10 | 9 | 8 | Decompose into sub-modules |
| 2 | COTRTLIC | 2,098 | **8.15** | 9 | 8 | 7 | Isolate DB2 logic |
| 3 | COCRDUPC | 1,560 | **7.75** | 8 | 8 | 7 | Extract validation logic |
| 4 | CBTRN02C | 731 | **7.70** | 7 | 9 | 7 | High-risk batch; add checkpoints |
| 5 | CBACT04C | 652 | **7.60** | 7 | 8 | 8 | Critical financial calc |
| 6 | COCRDLIC | 1,459 | **7.40** | 8 | 7 | 7 | Modularize browse logic |
| 7 | COTRTUPC | 1,702 | **7.15** | 8 | 7 | 6 | Isolate DB2 CRUD |
| 8 | CBSTM03A | 924 | **7.00** | 7 | 7 | 7 | Complex output formatting |
| 9 | COACTVWC | 941 | **6.75** | 7 | 7 | 6 | Multi-file joins |
| 10 | COTRN02C | 783 | **6.60** | 6 | 7 | 7 | Date validation dependency |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) ⚠️ CRITICAL

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 4,236 | Largest program in codebase (2.7× next largest) |
| Control Flow Statements | 470 | Extremely high branching |
| Paragraphs/Sections | 91 | Deeply structured |
| CICS API Calls | 17 | Heavy platform coupling |
| Files Accessed | 3 (ACCTDATA, CUSTDATA, CARDXREF) | Multi-entity mutations |
| Copybooks Included | 12 | High coupling surface |

**Why it's #1**:
- Handles the most complex business operation (full account update with customer and card validation)
- Mutates 3 critical VSAM files in a single transaction
- Contains inline screen I/O, data validation, and business rules in one monolith
- Any bug directly impacts account balances (financial data integrity risk)

**Migration Recommendation**: Decompose into 3-4 bounded services:
1. Account validation service
2. Customer data update service  
3. Card cross-reference verification
4. Screen presentation layer (separate from business logic)

---

### #2 — COTRTLIC (Transaction Type List — DB2)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 2,098 | Second largest |
| Control Flow Statements | 304 | Very high |
| CICS + SQL Calls | 28 | Heavy DB2 cursor operations |
| Module | Optional (DB2) | Adds DB2 migration complexity |

**Why it's #2**:
- Combines CICS screen logic with DB2 cursor management (OPEN/FETCH/CLOSE)
- Implements both list browsing and inline delete — two responsibilities
- DB2 dependency introduces two-phase commit considerations
- High branch count suggests complex conditional navigation

**Migration Recommendation**: Split into API endpoint (list/delete) + UI layer. DB2 cursor logic maps to paginated query service.

---

### #3 — COCRDUPC (Credit Card Update)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 1,560 | Large |
| Control Flow Statements | 212 | High branching |
| CICS API Calls | 12 | Moderate platform coupling |
| Files Accessed | 1 (CARDDATA) + validation reads | Write to financial data |

**Why it's #3**:
- Updates credit card master data (card numbers, CVV, status)
- Validation logic for card expiry dates, status transitions
- Single VSAM REWRITE but with complex pre-validation
- Linked to from Card List (COCRDLIC) — navigation coupling

**Migration Recommendation**: Extract validation rules into testable service. Separate card state machine from presentation.

---

### #4 — CBTRN02C (Post Daily Transactions) ⚠️ HIGH RISK

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 731 | Moderate |
| Control Flow Statements | 159 | High for a batch program |
| Files Accessed | 6 (input: DALYTRAN, XREFFILE; output: TRANSACT, DALYREJS, ACCTFILE, TCATBALF) | Most files of any batch |
| Batch Impact | Updates account balances | Financial integrity critical |

**Why it's #4**:
- Core daily batch — processes ALL daily transactions
- Writes to 4 output files simultaneously (complex commit logic)
- Rejected transactions written to DALYREJS (error path complexity)
- Updates ACCTFILE balances — directly affects customer statements
- No checkpoint/restart built in — failure requires full rerun

**Migration Recommendation**: Highest-priority batch for modernization. Implement idempotent processing with checkpoint/restart. Convert to event-driven transaction stream.

---

### #5 — CBACT04C (Interest Calculation) ⚠️ FINANCIAL

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 652 | Moderate |
| Control Flow Statements | 144 | High complexity ratio (0.22 per LOC) |
| Files Accessed | 5 (TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT) | Complex data joins |
| Financial Impact | Calculates interest charges | Revenue-critical |

**Why it's #5**:
- Implements interest rate calculation algorithm
- Joins 5 different data sources (complex data assembly)
- Directly modifies ACCTFILE (account balance updates)
- Business rules around disclosure groups and rate tiers
- Errors directly impact customer bills

**Migration Recommendation**: Extract calculation engine with comprehensive unit tests. Implement decimal arithmetic precision testing. This module requires the highest test coverage during migration.

---

### #6 — COCRDLIC (Credit Card List)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 1,459 | Large |
| Control Flow Statements | 191 | High |
| CICS API Calls | 18 | Highest CICS call count |
| Browse Operations | STARTBR, READNEXT, READPREV, ENDBR | Full browse pattern |

**Why it's #6**:
- Implements forward/backward VSAM browse with pagination
- 18 CICS calls = high platform coupling
- Navigation hub — dispatches to Card Detail or Card Update
- Complex key management for browse positioning

**Migration Recommendation**: Replace VSAM browse with indexed query. Separate list pagination from navigation dispatch.

---

### #7 — COTRTUPC (Transaction Type Maintenance — DB2)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 1,702 | Large |
| Control Flow Statements | 193 | High |
| CICS + SQL Calls | 19 | Heavy DB2 integration |
| Module | Optional (DB2) | DB2 dependency |

**Why it's #7**:
- INSERT and UPDATE to DB2 tables
- Complex field validation for transaction type codes
- Screen-driven CRUD with error handling for SQL states
- Paired with COTRTLIC — migration must handle both together

**Migration Recommendation**: Convert to REST API with standard CRUD operations. SQL logic maps directly to JPA/Spring Data.

---

### #8 — CBSTM03A (Statement Generation)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 924 | Moderate-Large |
| Control Flow Statements | 88 | Moderate |
| Sub-program Calls | 10 calls to CBSTM03B | Tight coupling |
| Output Formats | STMT + HTML files | Dual output complexity |

**Why it's #8**:
- Generates customer statements in both text and HTML
- Calls sub-module CBSTM03B 10 times for data retrieval
- Complex report formatting with headers, totals, page breaks
- Must maintain exact output compatibility during migration

**Migration Recommendation**: Replace with template engine (Thymeleaf/FreeMarker). CBSTM03B data retrieval becomes a query service.

---

### #9 — COACTVWC (Account View)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 941 | Moderate |
| Control Flow Statements | 98 | Moderate |
| CICS API Calls | 15 | Significant |
| Files Accessed | 3 (CARDXREF, ACCTDATA, CUSTDATA) | Multi-file reads |

**Why it's #9**:
- Read-only but joins 3 VSAM files for a single view
- Complex alternate index usage (CARDXREF by account path)
- Heavy screen formatting logic
- Foundation for COACTUPC — view pattern reused in update

**Migration Recommendation**: Convert to aggregate query returning DTO. Good candidate for read-model/CQRS pattern.

---

### #10 — COTRN02C (Transaction Add)

| Metric | Value | Assessment |
|:-------|:------|:-----------|
| Lines of Code | 783 | Moderate |
| Control Flow Statements | 120 | Moderate-High |
| CICS API Calls | 11 | Moderate |
| External Dependency | Calls CSUTLDTC for date validation | Utility coupling |
| Files Accessed | CARDXREF, TRANSACT | Read + Write |

**Why it's #10**:
- Adds new transactions to VSAM master
- Date validation via external utility (CSUTLDTC → CEEDAYS)
- Cross-reference validation before write
- Direct impact on transaction master integrity

**Migration Recommendation**: Convert to transaction creation API with built-in validation. Replace CEEDAYS call with Java date library.

---

## Migration Priority Matrix

```
                    HIGH BUSINESS IMPACT
                          │
         ┌────────────────┼────────────────┐
         │                │                │
         │  CBACT04C(#5)  │  COACTUPC(#1)  │
         │  CBTRN02C(#4)  │  COCRDUPC(#3)  │
         │                │  COTRN02C(#10) │
         │                │                │
HIGH ────┼────────────────┼────────────────┤──── HIGH
RISK     │                │                │    COMPLEXITY
         │                │                │
         │  CBSTM03A(#8)  │  COTRTLIC(#2)  │
         │                │  COTRTUPC(#7)  │
         │                │  COCRDLIC(#6)  │
         │  COACTVWC(#9)  │                │
         │                │                │
         └────────────────┼────────────────┘
                          │
                    LOW BUSINESS IMPACT
```

---

## Recommended Migration Waves

| Wave | Programs | Rationale | Estimated Effort |
|:-----|:---------|:----------|:-----------------|
| **Wave 1** (Quick Wins) | COACTVWC, COTRN02C | Read-only view + simple write; proves migration patterns | Low |
| **Wave 2** (Core CRUD) | COCRDUPC, COCRDLIC, COTRTLIC, COTRTUPC | Standard CRUD; DB2 programs map well to JPA | Medium |
| **Wave 3** (Complex Online) | COACTUPC | Must decompose first; highest LOC, multi-file mutations | High |
| **Wave 4** (Critical Batch) | CBTRN02C, CBACT04C, CBSTM03A | Financial calculations; need exhaustive test suites | High |

---

## Key Risk Factors for Migration

1. **COACTUPC size** — At 4,236 LOC, this single program equals the size of many modern microservices. Must be decomposed before translation.
2. **CBTRN02C data integrity** — Updates 4 files atomically. Java equivalent needs distributed transaction handling or saga pattern.
3. **CBACT04C precision** — Interest calculations require exact decimal arithmetic. Migration must validate to the penny.
4. **VSAM browse patterns** — STARTBR/READNEXT/READPREV in COCRDLIC/COTRN00C have no direct Java equivalent; need cursor-based pagination design.
5. **Commarea coupling** — All 17 online programs share state via COCOM01Y. Migration must design equivalent session management.
6. **DB2 cursor lifecycle** — COTRTLIC manages cursor open/fetch/close across pseudo-conversational transactions. Needs stateless redesign.
