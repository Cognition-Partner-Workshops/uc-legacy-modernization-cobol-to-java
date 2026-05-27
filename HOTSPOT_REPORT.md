# CardDemo Hotspot Report

> Top 10 modules prioritized by code complexity, migration risk, and business impact. Each module is scored on a 1–5 scale across three dimensions, producing a composite priority score (max 15).

---

## Scoring Methodology

| Dimension | What It Measures | Scale |
|:----------|:-----------------|:------|
| **Complexity** | LOC, cyclomatic complexity indicators (nested EVALUATEs, PERFORMs, COPY REPLACING), number of VSAM operations, REDEFINES, 88-level conditions | 1 (simple) – 5 (very complex) |
| **Risk** | Number of file dependencies, technology coupling (DB2/IMS/MQ/ASM), data mutation scope, error handling gaps, PII exposure | 1 (low) – 5 (high) |
| **Business Impact** | Criticality to daily operations, financial data handling, customer-facing, regulatory implications | 1 (utility) – 5 (mission-critical) |

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update)

| Metric | Value |
|:-------|:------|
| **LOC** | 4,236 |
| **Complexity** | 5 |
| **Risk** | 5 |
| **Business Impact** | 5 |
| **Composite Score** | **15 / 15** |

**Why it tops the list:**
- Largest program in the entire codebase by a factor of 2.7×.
- 35+ field-level validations including US phone area codes, state codes, ZIP prefixes, alphanumeric checks, yes/no flags, and signed number parsing.
- Uses `COPY CSSETATY REPLACING` 35 times for per-field error attribute highlighting — the most intensive use of COPY REPLACING in the system.
- Reads and rewrites **two VSAM files** (Account + Customer) in a single transaction.
- Includes `COPY CSLKPCDY` — a 1,000+ line lookup table of North American area codes and state/ZIP cross-references.
- Contains REDEFINES for phone number parsing and numeric field validation.
- Handles financial data: credit limits, balances, cycle credits/debits.
- Direct mutation of account and customer master records — highest blast radius of any online program.

**Migration recommendation:** Decompose into smaller services: validation service, account-write service, customer-write service. Extract CSLKPCDY lookup table to a reference data microservice or database table.

---

### #2 — CBTRN02C (Post Daily Transactions)

| Metric | Value |
|:-------|:------|
| **LOC** | 731 |
| **Complexity** | 4 |
| **Risk** | 5 |
| **Business Impact** | 5 |
| **Composite Score** | **14 / 15** |

**Why it ranks high:**
- Core batch posting engine — processes the entire daily transaction file.
- Reads 5 files (DALYTRAN, XREFFILE, ACCTFILE, TRANSACT, TCATBALF) and writes 3 (ACCTFILE, TRANSACT, TCATBALF).
- Updates account balances and transaction category running totals — financial mutation across multiple datasets.
- Failure during posting can leave files in an inconsistent state (no built-in two-phase commit).
- Uses CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y — touches nearly every core entity.
- Called daily by POSTTRAN JCL — part of the mandatory batch cycle.

**Migration recommendation:** Implement with database transactions (ACID) in target platform. Add idempotency keys and audit trail. Consider event-sourcing pattern.

---

### #3 — COCRDLIC (Credit Card List)

| Metric | Value |
|:-------|:------|
| **LOC** | 1,459 |
| **Complexity** | 5 |
| **Risk** | 4 |
| **Business Impact** | 4 |
| **Composite Score** | **13 / 15** |

**Why it ranks high:**
- Second-largest online program.
- Implements paginated VSAM browsing with STARTBR, READNEXT, READPREV, ENDBR — complex cursor management.
- Routes to two different programs via XCTL (COCRDSLC for detail, COCRDUPC for update).
- Uses CSSTRPFY (PF-key translation) — complex AID key handling.
- Filter-based record selection with multiple browse directions.
- Handles card number display (PII/PCI-DSS concern).

**Migration recommendation:** Replace VSAM browse with SQL pagination. Implement PCI-DSS compliant card masking in the target system.

---

### #4 — CBSTM03A (Account Statement Generation)

| Metric | Value |
|:-------|:------|
| **LOC** | 924 |
| **Complexity** | 5 |
| **Risk** | 4 |
| **Business Impact** | 4 |
| **Composite Score** | **13 / 15** |

**Why it ranks high:**
- Generates account statements in **two formats** (plain text and HTML) — dual output complexity.
- Calls CBSTM03B subroutine **12 times** for different formatting operations.
- Reads 4 VSAM files (XREFFILE, CUSTFILE, ACCTFILE, TRANSACT).
- Uses COSTM01 copybook (re-keyed transaction layout) and CUSTREC (alternate customer layout) — demonstrates complex REDEFINES.
- Intentionally exercises "mainframe control block addressing" and other complex patterns to stress modernization tools.
- The README notes this program deliberately incorporates various coding styles to exercise analysis tooling.

**Migration recommendation:** Replace with a templating engine (e.g., Thymeleaf, Jasper). Separate data retrieval from rendering. HTML generation should use modern web frameworks.

---

### #5 — COCRDUPC (Credit Card Update)

| Metric | Value |
|:-------|:------|
| **LOC** | 1,560 |
| **Complexity** | 4 |
| **Risk** | 4 |
| **Business Impact** | 4 |
| **Composite Score** | **12 / 15** |

**Why it ranks high:**
- Third-largest online program.
- Reads and rewrites CARDDAT VSAM file — card master data mutation.
- Reads Customer file for cross-validation.
- Uses COPY REPLACING via CSSTRPFY for AID key handling.
- Handles card expiration date, CVV, embossed name, active status — PCI-DSS sensitive fields.
- XCTL back to COCRDLIC for navigation.

**Migration recommendation:** Isolate card data updates behind a dedicated service with PCI-DSS controls. Implement field-level encryption for CVV.

---

### #6 — CBACT04C (Interest Calculator)

| Metric | Value |
|:-------|:------|
| **LOC** | 652 |
| **Complexity** | 4 |
| **Risk** | 4 |
| **Business Impact** | 4 |
| **Composite Score** | **12 / 15** |

**Why it ranks high:**
- Core financial calculation engine — computes interest on all accounts.
- Reads 4 VSAM files: ACCTFILE, TCATBALF, DISCGRP, TRANSACT.
- Writes updated balances back to ACCTFILE — financial mutation.
- Uses signed decimal arithmetic (PIC S9(10)V99) for precise financial calculations.
- Lookup-driven: maps account groups → disclosure groups → interest rates → category balances.
- Part of mandatory batch cycle (INTCALC job).

**Migration recommendation:** Implement with BigDecimal precision in Java. Extract interest rate rules to a configuration service. Add comprehensive audit logging for regulatory compliance.

---

### #7 — COCRDSLC (Credit Card Detail View)

| Metric | Value |
|:-------|:------|
| **LOC** | 887 |
| **Complexity** | 4 |
| **Risk** | 3 |
| **Business Impact** | 4 |
| **Composite Score** | **11 / 15** |

**Why it ranks high:**
- Reads Card and Customer VSAM files for card detail display.
- Uses CVCRD01Y work areas with REDEFINES for numeric-alpha card/account ID conversion.
- CSSTRPFY for AID key handling.
- XCTL navigation back to list screen.
- Displays full card details — PCI-DSS sensitive.

**Migration recommendation:** Implement card data masking for display. Use read-only DTO patterns.

---

### #8 — COTRN02C (Transaction Add)

| Metric | Value |
|:-------|:------|
| **LOC** | 783 |
| **Complexity** | 4 |
| **Risk** | 3 |
| **Business Impact** | 4 |
| **Composite Score** | **11 / 15** |

**Why it ranks high:**
- Inserts new transactions into TRANSACT VSAM file.
- Calls CSUTLDTC for date validation (which calls LE CEEDAYS).
- Reads Account and XREF files for cross-validation.
- Uses STARTBR + READPREV to determine the next available transaction key.
- Financial data entry — directly creates monetary transaction records.

**Migration recommendation:** Use database sequences for transaction ID generation instead of READPREV-based key derivation. Add duplicate detection.

---

### #9 — CBTRN03C (Transaction Detail Report)

| Metric | Value |
|:-------|:------|
| **LOC** | 649 |
| **Complexity** | 4 |
| **Risk** | 3 |
| **Business Impact** | 3 |
| **Composite Score** | **10 / 15** |

**Why it ranks high:**
- Report generation reading 4 VSAM files (TRANSACT, XREFFILE, TRANTYPE, TRANCATG).
- Joins transaction records with type and category descriptions for enriched output.
- Uses CVTRA07Y report layout with formatted headers and detail lines.
- Submitted from CICS via CORPT00C (online-to-batch bridging via TD queue).
- Date-range-based filtering.

**Migration recommendation:** Replace with SQL reporting queries or a BI tool. Eliminate the CICS-to-batch submission pattern.

---

### #10 — COPAUA0C (Authorization Request Processor)

| Metric | Value |
|:-------|:------|
| **LOC** | — |
| **Complexity** | 5 |
| **Risk** | 4 |
| **Business Impact** | 3 |
| **Composite Score** | **12 / 15** (optional module, lower priority) |

**Why it's notable:**
- Highest technology coupling in the system: CICS + IMS DB + IBM MQ in a single program.
- MQ-triggered — processes incoming authorization requests asynchronously.
- Reads from IMS DB, inserts/updates IMS segments, sends MQ responses.
- Uses 3 IMS PCBs (PADFLPCB, PASFLPCB, PAUTBPCB).
- Handles real-time financial authorization decisions.
- Extension module — lower immediate priority but highest migration complexity.

**Migration recommendation:** Replace with event-driven microservice using message broker (Kafka/SQS). Separate authorization logic from data access. Implement circuit breaker pattern.

---

## Complexity Heat Map Summary

```
        Low Impact                            High Impact
        ──────────────────────────────────────────────►

  Low   │ COBSWAIT    CBACT02C   CBACT03C   │
  Cmplx │ CBCUS01C    COUSR01C   COUSR03C   │
        │                                    │
  Med   │ COSGN00C    COTRN01C   COUSR00C   │
  Cmplx │ COMEN01C    COADM01C   COUSR02C   │
        │                                    │
  High  │ COPAUA0C*   CBTRN03C   COTRN02C   │
  Cmplx │             COCRDSLC   CBACT04C   │
        │             CBSTM03A   CBTRN02C   │
        │                        COACTUPC ◄── #1 Hotspot
        │             COCRDLIC   COCRDUPC   │
        ──────────────────────────────────────
```

---

## Migration Priority Recommendation

| Wave | Programs | Rationale |
|:-----|:---------|:----------|
| **Wave 1 — Quick Wins** | COSGN00C, COMEN01C, COADM01C, COUSR00C–COUSR03C | Simple CRUD, minimal dependencies, foundational auth/admin |
| **Wave 2 — Read-Only Views** | COACTVWC, COCRDSLC, COTRN00C, COTRN01C, CBACT02C, CBACT03C, CBCUS01C | Read-only programs, no data mutation risk |
| **Wave 3 — Transaction Processing** | COTRN02C, COBIL00C, CORPT00C, CBTRN01C, CBTRN03C | Write operations with moderate complexity |
| **Wave 4 — Core Business Logic** | COACTUPC, COCRDUPC, COCRDLIC, CBTRN02C, CBACT04C, CBSTM03A | High complexity, financial calculations, multi-file mutations |
| **Wave 5 — Extensions** | COPAUA0C, COPAUS0C–2C, COTRTLIC, COTRTUPC, COACCT01, CODATE01 | DB2/IMS/MQ integration — requires middleware migration |
| **Wave 6 — Utilities & Infra** | COBSWAIT, CSUTLDTC, CBEXPORT, CBIMPORT, JCL jobs | Support programs, batch scheduling, data migration |
