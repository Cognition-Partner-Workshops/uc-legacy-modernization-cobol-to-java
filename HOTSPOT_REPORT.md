# Hotspot Report — CardDemo Mainframe System

> Generated: 2026-05-22 | Source: `uc-legacy-modernization-cobol-to-java`

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale):

| Dimension | Criteria |
|:----------|:---------|
| **Complexity** | LOC, cyclomatic complexity indicators (nested IFs, EVALUATE, PERFORM THRU, GO TO), number of copybooks, COPY REPLACING usage, REDEFINES, multi-file I/O |
| **Risk** | Data sensitivity (PII/PCI), concurrency (CICS REWRITE), error handling quality, coupling to external systems (MQ, IMS, DB2), abend handling patterns |
| **Business Impact** | Revenue-critical operations, regulatory exposure, user-facing frequency, data integrity implications, blast radius of failure |

**Priority Score** = (Complexity × 0.3) + (Risk × 0.4) + (Business Impact × 0.3)

---

## Top 10 Hotspot Modules

### #1 — COACTUPC.cbl (Account Update)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 4,236 | |
| **Complexity** | 38 COPY REPLACING blocks, 15+ copybooks, multi-file CICS READ/REWRITE, extensive field validation with CSSETATY pattern, deep nested IF logic | **10** |
| **Risk** | Writes to ACCTDAT (balance changes), reads CUSTDAT/CXREF, processes financial data, CICS REWRITE concurrency, HANDLE ABEND | **9** |
| **Business Impact** | Core account maintenance — any bug directly affects customer balances, credit limits, account status | **10** |
| **Priority Score** | | **9.6** |

**Why it matters:** This is by far the largest and most complex program in the system. It is the single point of mutation for account master data (balances, limits, status). The 38× COPY REPLACING pattern for BMS attribute setting is a maintenance nightmare. A modernization defect here could corrupt financial records.

**Modernization Recommendation:** Decompose into Account Validation Service + Account Persistence Service. Extract the COPY REPLACING attribute logic into a reusable UI component layer. Requires extensive regression testing with financial reconciliation.

---

### #2 — CBTRN02C.cbl (Transaction Posting — Batch)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 731 | |
| **Complexity** | 4 VSAM files (read + write + rewrite), reject file handling, TCATBALF update, multi-step validation, cross-reference lookups | **8** |
| **Risk** | Core financial posting — writes TRANSACT, REWRITES account balances and category balances, writes rejection file. A failure mid-batch can leave data inconsistent | **10** |
| **Business Impact** | Processes every daily transaction. If this fails, no transactions post, statements are wrong, and interest calculations are off | **10** |
| **Priority Score** | | **9.4** |

**Why it matters:** This is the engine of the daily batch cycle. It reads DALYTRAN, validates against CXREF and ACCTDAT, updates TCATBALF running balances, rewrites ACCTDAT with new balances, and writes to TRANSACT. No built-in restart/checkpoint logic — a failure requires full rerun.

**Modernization Recommendation:** Implement as an event-driven transaction processing service with idempotent operations, checkpointing, and proper database transaction boundaries. Add dead-letter queue for rejects.

---

### #3 — COPAUA0C.cbl (Authorization Decision — MQ/IMS)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 1,026 | |
| **Complexity** | 3 technologies (CICS + IMS + MQ), 10+ copybooks including MQ API (CMQODV/CMQMDV/CMQV/CMQTML/CMQPMOV/CMQGMOV), IMS DL/I calls, cross-reference lookups | **10** |
| **Risk** | Real-time authorization decisions, MQ message handling (request/reply), IMS segment insert/update, fraud implications. Cross-system failure modes are complex | **10** |
| **Business Impact** | Card authorization is revenue-critical — downtime means declined transactions. Regulatory (PCI-DSS) exposure for card data handling | **9** |
| **Priority Score** | | **9.7** |

**Why it matters:** The most technologically complex program — integrates three middleware stacks (CICS, MQ, IMS) in a single program. MQ trigger-initiated, so it runs asynchronously. Any IMS or MQ connectivity issue causes cascading authorization failures.

**Modernization Recommendation:** Re-architect as a microservice with separate MQ consumer, authorization rules engine, and IMS data access layer. This is the highest-risk modernization target due to real-time latency requirements.

---

### #4 — CBACT04C.cbl (Interest Calculator)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 652 | |
| **Complexity** | 5 VSAM files, business rule logic for interest rates based on disclosure groups, category balance tracking, cross-reference chain traversal | **7** |
| **Risk** | Financial calculations — interest applied to balances. REWRITE to ACCTDAT. Incorrect interest means regulatory/compliance exposure and customer complaints | **9** |
| **Business Impact** | Directly affects customer billing. Interest miscalculation = financial loss + regulatory risk. Runs daily in batch cycle | **9** |
| **Priority Score** | | **8.4** |

**Why it matters:** Implements the core interest calculation business rules by traversing DISCGRP → TCATBALF → ACCTDAT. The business logic embedded here represents critical institutional knowledge about rate tiers and category-based interest application.

**Modernization Recommendation:** Extract interest rules into a configurable rules engine. Implement as a stateless calculation service with audit logging. Extensive parallel-run testing against COBOL output required.

---

### #5 — CBSTM03A.CBL (Statement Generation)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 924 | |
| **Complexity** | Dual output (plaintext + HTML), CALL to subroutine CBSTM03B, 4 input files, mainframe control block addressing, complex report formatting | **8** |
| **Risk** | Customer-facing output — statement errors are visible to customers. Uses CUSTREC (alternate copybook) creating dual-mapping risk. Subroutine coupling with CBSTM03B | **7** |
| **Business Impact** | Produces customer statements — a regulatory requirement. Statement errors trigger customer disputes and compliance issues | **8** |
| **Priority Score** | | **7.6** |

**Why it matters:** One of the most feature-rich batch programs. It demonstrates advanced COBOL patterns: subroutine calls (CBSTM03B), dual format output, and mainframe control block addressing. The CBSTM03A/B split adds testing complexity — they must be modernized as a pair.

**Modernization Recommendation:** Consolidate CBSTM03A + CBSTM03B into a single statement generation service. Replace HTML generation with a template engine. Good candidate for early modernization as output can be visually compared.

---

### #6 — COTRTLIC.cbl (Transaction Type List — DB2)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 2,098 | |
| **Complexity** | DB2 cursor-based pagination, SQL SELECT/UPDATE/DELETE, CICS BMS, copybook-based UI, extensive screen handling for list display | **8** |
| **Risk** | DB2 cursor management — uncommitted reads, orphaned cursors on abend. SQL injection not a concern (static SQL), but data integrity on DELETE operations | **7** |
| **Business Impact** | Admin function for managing transaction types — reference data that affects all transaction processing | **6** |
| **Priority Score** | | **7.0** |

**Why it matters:** The most complex DB2 program at 2,098 lines. Demonstrates cursor-based list navigation pattern that is common in mainframe UIs but complex to modernize. The DELETE capability makes it higher risk than a read-only screen.

**Modernization Recommendation:** Replace with a standard CRUD REST API + paginated list UI. DB2 cursor logic maps naturally to JPA/Spring Data pagination.

---

### #7 — COCRDLIC.cbl (Credit Card List)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 1,459 | |
| **Complexity** | VSAM browse (STARTBR/READNEXT/READPREV), bidirectional pagination, context-sensitive filtering (admin sees all, user sees own), XCTL to detail/update | **7** |
| **Risk** | Card number (PCI-DSS) displayed on screen. Browse operations hold VSAM control intervals. Context-dependent behavior adds testing matrix | **8** |
| **Business Impact** | Primary card discovery screen — gateway to card detail and update functions | **7** |
| **Priority Score** | | **7.4** |

**Why it matters:** Demonstrates the VSAM browse pattern with forward/backward pagination — a common mainframe pattern that has no direct equivalent in modern frameworks. The role-based filtering logic (admin vs. user) adds branching complexity.

**Modernization Recommendation:** Implement as a paginated REST endpoint with role-based query filtering. Card numbers must be masked/tokenized in the modern UI (PCI-DSS compliance).

---

### #8 — COCRDUPC.cbl (Credit Card Update)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 1,560 | |
| **Complexity** | Multi-screen field validation, COPY REPLACING for attributes, CICS READ FOR UPDATE / REWRITE, customer cross-reference lookup | **7** |
| **Risk** | Writes to CARDDAT (card master). Card data mutations (expiry, status, name) have PCI implications. CICS UPDATE lock duration | **8** |
| **Business Impact** | Enables card lifecycle management — activation, deactivation, reissue. Critical for customer service operations | **7** |
| **Priority Score** | | **7.4** |

**Why it matters:** Second-largest online program. Handles card master mutations which have downstream effects on transaction processing and authorization. The COPY REPLACING pattern (shared with COACTUPC) should be addressed as a common refactoring.

**Modernization Recommendation:** Extract card lifecycle operations into a Card Service. Implement optimistic locking to replace CICS READ FOR UPDATE. Add audit trail for all card mutations.

---

### #9 — COBIL00C.cbl (Bill Payment)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 572 | |
| **Complexity** | 3 VSAM files, STARTBR/READPREV for last transaction, WRITE to TRANSACT, REWRITE to ACCTDAT, timestamp generation via ASKTIME/FORMATTIME | **7** |
| **Risk** | Financial transaction creation — directly modifies account balance. Payment amount validation critical. Transaction reversal not implemented | **9** |
| **Business Impact** | Customer bill payment — revenue operation. Incorrect payment posting directly affects customer balance and billing cycle | **8** |
| **Priority Score** | | **8.1** |

**Why it matters:** Creates financial transactions and updates account balances in a single online operation without batch-style controls. The browse-for-last-transaction pattern (to generate next transaction ID) is fragile under concurrent access.

**Modernization Recommendation:** Implement as an idempotent payment service with proper transaction management. Add payment reversal capability. Replace the transaction ID generation with a sequence/UUID.

---

### #10 — COTRN02C.cbl (Transaction Add — Online)

| Metric | Value | Score |
|:-------|:------|------:|
| **LOC** | 783 | |
| **Complexity** | 3 VSAM files, cross-reference lookups (CXACAIX + CCXREF), date validation via CSUTLDTC CALL, STARTBR/READPREV for ID generation, WRITE to TRANSACT | **7** |
| **Risk** | Creates transactions in the online system. Uses alternate index (CXACAIX) for card-to-account resolution. Date validation dependency on CSUTLDTC | **7** |
| **Business Impact** | Enables manual transaction entry — used for adjustments, credits, and corrections. Data integrity critical | **7** |
| **Priority Score** | | **7.0** |

**Why it matters:** The most complex transaction creation flow in the online system. Uses both the primary XREF and the AIX for cross-reference validation, plus external CALL for date validation. The COPY-LAST-TRAN-DATA paragraph demonstrates a "clone last entry" convenience pattern.

**Modernization Recommendation:** Implement as a Transaction Creation Service with proper validation pipeline. Replace AIX lookup with a query service. Add maker-checker workflow for manual transactions.

---

## Summary — Priority Matrix

```
                        BUSINESS IMPACT
                   Low         Medium         High
              ┌───────────┬───────────────┬──────────────┐
         High │           │ #6 COTRTLIC   │ #3 COPAUA0C  │
              │           │               │ #1 COACTUPC  │
  COMPLEXITY  ├───────────┼───────────────┼──────────────┤
       Medium │           │ #7 COCRDLIC   │ #2 CBTRN02C  │
              │           │ #8 COCRDUPC   │ #4 CBACT04C  │
              │           │ #10 COTRN02C  │ #5 CBSTM03A  │
              │           │               │ #9 COBIL00C  │
              ├───────────┼───────────────┼──────────────┤
         Low  │           │               │              │
              └───────────┴───────────────┴──────────────┘
```

## Recommended Modernization Order

| Wave | Programs | Rationale |
|:-----|:---------|:----------|
| **Wave 1 — Low Risk, High Visibility** | CBACT01C, CBACT02C, CBACT03C, CBCUS01C | Simple batch readers — validate tooling and patterns |
| **Wave 2 — Utilities** | CSUTLDTC, COBSWAIT, CBSTM03B | Shared utilities — modernize once, reuse everywhere |
| **Wave 3 — User Management** | COUSR00C, COUSR01C, COUSR02C, COUSR03C, COSGN00C | Self-contained CRUD cycle with no financial data |
| **Wave 4 — Reporting** | CBTRN03C, CBSTM03A, CORPT00C | Output-comparable — can visually validate modernized output |
| **Wave 5 — Card Management** | COCRDLIC, COCRDSLC, COCRDUPC | PCI-sensitive — requires tokenization strategy |
| **Wave 6 — Account Management** | COACTVWC, COACTUPC | Highest-complexity online programs — need account-level reconciliation |
| **Wave 7 — Transaction Processing** | COTRN00C, COTRN01C, COTRN02C, COBIL00C | Financial transactions — need parallel-run validation |
| **Wave 8 — Batch Core** | CBTRN01C, CBTRN02C, CBACT04C | Revenue-critical batch — need batch restart/checkpoint |
| **Wave 9 — Data Migration** | CBEXPORT, CBIMPORT | Branch migration — can be deferred if not actively used |
| **Wave 10 — Extensions** | COPAUA0C, COPAUS0C/1C/2C, COTRTLIC, COTRTUPC, etc. | Multi-technology (IMS/DB2/MQ) — highest technical risk |

---

## Key Technical Debt Items

| # | Issue | Affected Programs | Severity |
|:-:|:------|:-----------------|:---------|
| 1 | **Plaintext passwords** in USRSEC file | COSGN00C, COUSR01C/02C | Critical |
| 2 | **No restart/checkpoint** in batch posting | CBTRN02C | High |
| 3 | **COPY REPLACING** pattern (38× in COACTUPC) | COACTUPC, COCRDUPC | High |
| 4 | **Card numbers stored unencrypted** (PCI-DSS) | All card-handling programs | Critical |
| 5 | **CVV stored persistently** | CVACT02Y (CARD-CVV-CD) | Critical |
| 6 | **Hardcoded program names** in menu copybooks | COMEN02Y, COADM02Y | Medium |
| 7 | **GO TO used for flow control** | Most online programs | Medium |
| 8 | **No audit trail** for data mutations | COACTUPC, COCRDUPC, COBIL00C | High |
| 9 | **Transaction ID generation via browse** (race condition) | COBIL00C, COTRN02C | High |
| 10 | **Dual customer record layouts** (CVCUS01Y vs CUSTREC) | CBSTM03A vs others | Medium |
