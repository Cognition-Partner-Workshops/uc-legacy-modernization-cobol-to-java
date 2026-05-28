# CardDemo Hotspot Report — Top 10 Modules

> **System**: CardDemo – Mainframe Credit Card Management System  
> **Generated**: 2026-05-28  
> **Methodology**: Modules ranked by a weighted composite of code complexity (40%), migration risk (35%), and business impact (25%). Metrics derived from static analysis of source code: LOC, cyclomatic complexity proxies (IF/EVALUATE branches), CICS/SQL/MQ API surface, copybook fan-out, VSAM file touch count, and role in batch job chains.

---

## Executive Summary

The top 10 hotspots account for **~16,000 lines** (40% of all COBOL) and touch **every core business entity**. The three highest-priority targets for modernization are:

1. **COACTUPC** — largest program, highest branching complexity, central to account management
2. **COTRTLIC** — dual CICS + DB2 stack with cursor-based SQL patterns
3. **COCRDUPC** — high branching density with card data (PCI scope)

---

## Scoring Methodology

| Factor | Weight | Metrics Used |
|--------|-------:|-------------|
| **Code Complexity** | 40% | Lines of code, IF + EVALUATE branch count, PERFORM count, copybook fan-out |
| **Migration Risk** | 35% | Technology mix (CICS + DB2 + IMS + MQ), file I/O surface, external CALL dependencies, data format complexity (COMP-3, REDEFINES) |
| **Business Impact** | 25% | Domain criticality (financial calculations, PCI data, auth decisions), frequency of execution (online vs batch, daily vs monthly), downstream dependencies |

Scores are normalized to 1–10 scale.

---

## Top 10 Hotspot Ranking

| Rank | Program | LOC | Complexity | Risk | Impact | **Composite** | Domain |
|-----:|---------|----:|----------:|-----:|-------:|------:|--------|
| 1 | **COACTUPC** | 4,236 | 9.5 | 8.0 | 9.0 | **8.9** | Account Update |
| 2 | **COTRTLIC** | 2,098 | 8.0 | 9.5 | 6.5 | **8.1** | Tran Type List (DB2) |
| 3 | **COCRDUPC** | 1,560 | 8.5 | 7.5 | 8.0 | **8.0** | Credit Card Update |
| 4 | **COCRDLIC** | 1,459 | 8.0 | 7.0 | 8.0 | **7.7** | Credit Card List |
| 5 | **COTRTUPC** | 1,702 | 7.5 | 9.0 | 6.0 | **7.6** | Tran Type Add/Edit (DB2) |
| 6 | **COPAUA0C** | 1,026 | 7.0 | 10.0 | 7.0 | **7.6** | Auth Decision (MQ+IMS) |
| 7 | **CBTRN02C** | 731 | 7.5 | 6.0 | 9.5 | **7.5** | Transaction Posting |
| 8 | **COPAUS0C** | 1,032 | 7.0 | 8.5 | 6.5 | **7.4** | Auth Summary (IMS) |
| 9 | **COACTVWC** | 941 | 6.5 | 6.0 | 8.5 | **6.9** | Account View |
| 10 | **CBACT04C** | 652 | 7.0 | 5.5 | 9.0 | **6.9** | Interest Calculator |

---

## Detailed Module Profiles

### #1 — COACTUPC (Account Update) — Score: 8.9

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 4,236 | **Largest program in the system** — 2.7× the next largest |
| IF Branches | 174 | Extremely high cyclomatic complexity |
| PERFORM Sections | 64 | Dense procedural flow |
| CICS API Calls | 17 | XCTL, SEND/RECEIVE MAP, READ ×5 |
| VSAM Files Touched | 5 | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF (read), ACCTDATA (rewrite) |
| Copybooks Included | 58 | Very high fan-out |
| BMS Map | COACTUP | Multi-field account update screen |

**Why it's #1**: This is the most complex single program. It validates and updates the core Account entity while cross-referencing Card, Customer, and Cross-Reference files. Any migration error here directly impacts financial data. Its 174 IF branches make it the hardest to test comprehensively.

**Modernization Recommendations**:
- Decompose into separate validation, display, and persistence layers
- Extract the 5 VSAM read patterns into a shared data access service
- Target for extensive unit test coverage before migration

---

### #2 — COTRTLIC (Transaction Type List — DB2) — Score: 8.1

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 2,098 | Second largest online program |
| IF Branches | 99 | High branching |
| CICS API Calls | 12 | XCTL, SEND/RECEIVE MAP |
| SQL Statements | 16 | **Cursor-based SELECT, positioned UPDATE, DELETE** |
| Copybooks | 12 | Includes DB2 DCL copies |

**Why it's #2**: Dual technology stack (CICS + DB2) with cursor-driven pagination. The cursor lifecycle (DECLARE → OPEN → FETCH → CLOSE) is notoriously fragile to migrate. Positioned UPDATE/DELETE adds complexity. DB2 error handling spans multiple SQLCODE checks.

**Modernization Recommendations**:
- Replace cursor pagination with offset/limit or keyset pagination in target DB
- Extract SQL into a repository pattern
- Map SQLCODE error handling to exception-based patterns

---

### #3 — COCRDUPC (Credit Card Update) — Score: 8.0

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 1,560 | |
| IF Branches | 151 | **Second highest branching density** (0.097 IF/LOC) |
| PERFORM Sections | 26 | |
| CICS API Calls | 12 | XCTL, SEND/RECEIVE MAP, READ, REWRITE |
| VSAM Files Touched | 3 | CARDDATA (read + rewrite), ACCTDATA, CARDXREF |

**Why it's #3**: Manages PCI-sensitive card data (PAN, CVV, expiration). The 151 IF branches in 1,560 lines gives the highest branching density of any program. Card update logic includes validation of card number format, expiry dates, and cross-reference integrity.

**Modernization Recommendations**:
- Apply PCI DSS tokenization during migration
- Extract card validation into a dedicated service
- Prioritize security review of migrated code

---

### #4 — COCRDLIC (Credit Card List) — Score: 7.7

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 1,459 | |
| IF Branches | 131 | High complexity |
| CICS API Calls | 18 | **Highest CICS API count** — includes STARTBR, READNEXT, READPREV, ENDBR |
| VSAM Files Touched | 4 | CARDDATA (browse), ACCTDATA, CUSTDATA, CARDXREF |
| XCTL Targets | 3 | Can navigate to COCRDSLC or COCRDUPC |

**Why it's #4**: The VSAM browse pattern (STARTBR → READNEXT/READPREV → ENDBR) for paginated card listing is a common migration challenge. It serves as the gateway to card detail/update screens and handles forward/backward pagination state.

**Modernization Recommendations**:
- Replace VSAM browse with SQL query-based pagination
- Extract list/detail/update into a single CRUD controller

---

### #5 — COTRTUPC (Transaction Type Add/Edit — DB2) — Score: 7.6

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 1,702 | |
| IF Branches | 108 | |
| CICS API Calls | 12 | XCTL, SEND/RECEIVE MAP |
| SQL Statements | 7 | SELECT, INSERT, UPDATE across TRTYP and TRCAT |

**Why it's #5**: Cross-table DB2 operations (TRTYP + TRCAT) with insert-or-update logic. The program must handle both "add new" and "edit existing" modes, doubling the code paths. SQL INCLUDE of two DCL copybooks (DCLTRTYP, DCLTRCAT) adds coupling.

**Modernization Recommendations**:
- Split add and edit into separate operations/endpoints
- Use ORM-based upsert patterns in target platform

---

### #6 — COPAUA0C (Authorization Decision — MQ+IMS+CICS) — Score: 7.6

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 1,026 | |
| IF Branches | 52 | Moderate |
| CICS API Calls | 12 | READ, WRITEQ, ASKTIME, FORMATTIME |
| MQ API Calls | 4 | **MQOPEN, MQGET, MQPUT1, MQCLOSE** |
| VSAM Files Touched | 3 | CARDXREF, ACCTDATA, CUSTDATA |
| IMS Segments | 2 | CIPAUSMY (summary), CIPAUDTY (detail) |

**Why it's #6**: The **highest technology diversity** of any single program: CICS + MQ + IMS + VSAM in one module. Triggered asynchronously by MQ, it makes real-time authorization decisions reading from IMS hierarchical DB and VSAM. The MQ request/response pattern and IMS DL/I calls are the hardest mainframe constructs to migrate.

**Modernization Recommendations**:
- Replace MQ trigger with event-driven architecture (Kafka/SQS)
- Replace IMS DL/I with relational or document DB queries
- Decompose into message handler → business logic → persistence layers

---

### #7 — CBTRN02C (Transaction Posting — Batch) — Score: 7.5

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 731 | |
| IF Branches | 93 | **Highest batch program branching** |
| PERFORM Sections | 61 | Very dense procedural logic |
| Files Read/Written | 7+ | DALYTRAN, TRANSACT, XREF, ACCOUNT, TCATBALF, DISCGRP, TRANTYPE, TRANCATG |

**Why it's #7**: The core daily batch engine. It reads daily transactions, validates against reference data, updates account balances, computes category balances, and writes rejection records. The 93 IF branches in 731 lines give the highest branch density of any batch program. Errors here directly impact financial accuracy.

**Modernization Recommendations**:
- Implement as an idempotent batch job with checkpointing
- Add comprehensive reconciliation logging
- Target for parallel processing in modern batch framework (Spring Batch)

---

### #8 — COPAUS0C (Authorization Summary View — IMS) — Score: 7.4

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 1,032 | |
| IF Branches | 25 | |
| CICS API Calls | 10 | READ ×3 (VSAM + IMS) |
| IMS Segments | 2 | Summary + Detail segments |
| Copybooks | 15 | High fan-out |

**Why it's #8**: Reads from both IMS hierarchical DB and VSAM to build a composite screen. IMS segment navigation (GU, GN, GNP calls) requires careful mapping to relational queries. The dual data source pattern complicates transaction management.

**Modernization Recommendations**:
- Consolidate IMS and VSAM data into a single relational schema
- Implement a view-model layer that aggregates across data sources

---

### #9 — COACTVWC (Account View) — Score: 6.9

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 941 | |
| IF Branches | 60 | |
| CICS API Calls | 15 | XCTL, SEND/RECEIVE MAP, READ ×3 |
| VSAM Files Touched | 4 | ACCTDATA, CARDDATA, CUSTDATA, CARDXREF |

**Why it's #9**: Read-only account view, but it aggregates data from 4 VSAM files to build a single screen. High user-facing frequency means it must be migrated early. Shares much logic with COACTUPC (#1) — an opportunity for consolidation.

**Modernization Recommendations**:
- Merge view and update into a single account service with read/write modes
- Implement as a read-optimized query with joins

---

### #10 — CBACT04C (Interest Calculator — Batch) — Score: 6.9

| Metric | Value | Concern |
|--------|------:|---------|
| Lines of Code | 652 | |
| IF Branches | 86 | High for its size |
| PERFORM Sections | 56 | Very high procedural density |
| Files Read/Written | 5 | TCATBALF, XREF, ACCOUNT, DISCGRP, TRANSACT |
| Copybooks | 6 | Financial calculation data structures |

**Why it's #10**: Computes monthly interest charges using disclosure group rates applied to transaction category balances. The financial calculation logic is business-critical — any rounding or precision error post-migration has direct monetary impact. Uses 5 COBOL data files in a complex nested loop.

**Modernization Recommendations**:
- Preserve exact decimal arithmetic (avoid floating-point)
- Implement parallel calculation with BigDecimal or equivalent
- Build golden-file regression tests comparing COBOL vs Java output to the cent

---

## Migration Priority Matrix

```
          HIGH BUSINESS IMPACT
               │
    Q1 (Do     │     Q2 (Do
    First)     │     Second)
               │
  COACTUPC ●   │   ● CBTRN02C
  COCRDUPC ●   │   ● CBACT04C
  COCRDLIC ●   │   ● COACTVWC
               │
  ─────────────┼───────────────  HIGH COMPLEXITY /
               │                 MIGRATION RISK
  COPAUA0C ●   │   ● COTRTLIC
  COPAUS0C ●   │   ● COTRTUPC
               │
    Q3 (Do     │     Q4 (Do
    Third)     │     Last)
               │
          LOW BUSINESS IMPACT
```

### Recommended Migration Waves

| Wave | Programs | Rationale |
|-----:|----------|-----------|
| **Wave 1** (Core CRUD) | COACTUPC, COACTVWC, COCRDUPC, COCRDLIC, COCRDSLC | Core account and card management — highest user traffic, shared data access patterns |
| **Wave 2** (Transactions) | CBTRN02C, CBTRN01C, COTRN00C–02C, CBACT04C | Transaction processing chain — daily batch + online screens |
| **Wave 3** (DB2 Extensions) | COTRTLIC, COTRTUPC, COBTUPDT | Already use SQL — closest to modern patterns |
| **Wave 4** (IMS/MQ) | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C | Highest technical risk — IMS + MQ requires new architecture |

---

## Risk Factors for All Hotspots

| Risk Category | Affected Programs | Mitigation |
|--------------|-------------------|------------|
| **PCI Data Exposure** | COCRDUPC, COCRDLIC, COCRDSLC, COPAUA0C | Tokenize card numbers, mask CVV in migrated system |
| **Financial Precision** | COACTUPC, CBTRN02C, CBACT04C, COBIL00C | Use BigDecimal; golden-file regression tests |
| **IMS Hierarchical Data** | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C | Flatten to relational schema with careful key mapping |
| **MQ Async Patterns** | COPAUA0C, COACCT01, CODATE01 | Replace with Kafka/SQS event-driven architecture |
| **VSAM Browse Patterns** | COCRDLIC, COTRN00C, COUSR00C | Replace with SQL pagination queries |
| **Packed Decimal (COMP-3)** | CBEXPORT/IMPORT, all IMS copybooks | Ensure byte-level fidelity in data conversion |
| **REDEFINES / OCCURS** | CVEXPORT, COADM02Y, COMEN02Y | Map to polymorphic types or discriminated unions |
| **Plaintext Passwords** | CSUSR01Y / COSGN00C | Hash on migration; implement modern auth (OAuth2/SAML) |
