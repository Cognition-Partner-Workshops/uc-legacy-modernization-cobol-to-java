# Hotspot Report - CardDemo

> Top 10 modules prioritized by complexity, risk, and business impact for modernization planning.

---

## Scoring Methodology

| Factor | Weight | Description |
|--------|-------:|-------------|
| **Complexity** | 35% | Lines of code + control flow density (IF/EVALUATE/PERFORM per LOC) |
| **Coupling** | 20% | Number of copybooks, VSAM files, and external calls |
| **Business Impact** | 30% | Revenue criticality, user-facing, data mutation scope |
| **Migration Risk** | 15% | CICS dependencies, packed decimals, REDEFINES, DB2/MQ/IMS usage |

### Complexity Score Formula
```
complexity_raw = LOC + (control_flow_statements * 5)
normalized = complexity_raw / max(all_modules) * 10
```

---

## Top 10 Hotspot Modules

| Rank | Module | LOC | CF Stmts | Complexity | Coupling | Biz Impact | Mig Risk | **Score** |
|-----:|--------|----:|------:|------:|------:|------:|------:|------:|
| 1 | **COACTUPC** | 4,236 | 419 | 10.0 | 9.5 | 9.0 | 8.0 | **9.3** |
| 2 | **CBTRN02C** | 731 | 159 | 3.5 | 8.0 | 10.0 | 7.0 | **7.4** |
| 3 | **COTRTLIC** | 2,098 | 276 | 6.5 | 7.0 | 6.0 | 9.5 | **7.0** |
| 4 | **COCRDUPC** | 1,560 | 191 | 4.8 | 8.0 | 8.5 | 7.0 | **7.0** |
| 5 | **COCRDLIC** | 1,459 | 175 | 4.5 | 7.0 | 8.0 | 7.0 | **6.6** |
| 6 | **COTRTUPC** | 1,702 | 170 | 5.0 | 6.5 | 6.0 | 9.5 | **6.5** |
| 7 | **CBACT04C** | 652 | 144 | 3.0 | 8.5 | 9.0 | 5.0 | **6.4** |
| 8 | **CBSTM03A** | 924 | 73 | 2.5 | 5.0 | 8.0 | 6.0 | **5.5** |
| 9 | **COPAUA0C** | 1,026 | 102 | 3.2 | 6.0 | 7.0 | 9.0 | **6.1** |
| 10 | **COACTVWC** | 941 | 89 | 2.7 | 8.0 | 7.0 | 6.0 | **5.7** |

---

## Detailed Analysis

### #1 COACTUPC - Account Update (Score: 9.3)

**Why it's #1:** Largest program in the system (4,236 LOC), highest control flow density (419 IF/EVALUATE/PERFORM statements), and touches the most critical business entity (account balances).

| Factor | Details |
|--------|---------|
| **Complexity** | 4,236 lines. 419 control-flow statements (10% density). Uses COPY REPLACING 39 times for field attribute setting. Includes inline date validation via CSUTLDPY (375-line copybook). |
| **Coupling** | 14 copybooks. Reads/writes: ACCTDATA, CUSTDATA, CARDXREF. References CSLKPCDY (1,318-line lookup tables). |
| **Business Impact** | Primary account modification path. Updates credit limits, balances, status. Direct impact on financial accuracy. |
| **Migration Risk** | Heavy BMS map interaction. Complex COMMAREA navigation. COPY REPLACING pattern unusual for modern languages. |

**Recommendation:** Decompose into smaller services: Account Validation, Account Persistence, Screen Controller. Extract date validation and lookup code into shared utilities first.

---

### #2 CBTRN02C - Post Transactions (Score: 7.4)

**Why it's #2:** The core batch financial engine. Updates account balances, category balances, and transaction status in a single pass. Any bug here directly affects money.

| Factor | Details |
|--------|---------|
| **Complexity** | 731 lines, 159 control-flow statements (22% density - highest ratio). Multi-file update logic. |
| **Coupling** | Reads: DALYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBALF. Writes: TRANSACT, TCATBALF, ACCTDATA. |
| **Business Impact** | **Critical path.** Every daily transaction flows through this. Balance accuracy depends entirely on this program. Runs in POSTTRAN job daily. |
| **Migration Risk** | Sequential VSAM processing with cross-file lookups. Multi-update transaction semantics (no built-in rollback). Signed decimal arithmetic. |

**Recommendation:** Highest priority for comprehensive test coverage before migration. Consider migrating to a database transaction with ACID guarantees. The implicit "transaction" spanning multiple VSAM writes should become an explicit DB transaction.

---

### #3 COTRTLIC - Transaction Type List/Delete (Score: 7.0)

**Why it's #3:** Complex DB2 cursor-based program with embedded SQL, inline screen management, and delete operations on reference data.

| Factor | Details |
|--------|---------|
| **Complexity** | 2,098 lines. 276 control-flow statements. DB2 cursor logic with FETCH loops. |
| **Coupling** | DB2 table (TRAN_TYPE). BMS map COTRTLI. Cursor-based pagination. |
| **Business Impact** | Manages transaction type reference data used by all transaction processing. |
| **Migration Risk** | Embedded SQL (EXEC SQL). CICS + DB2 combined. Cursor lifecycle management. Two-phase commit considerations. |

**Recommendation:** Natural candidate for a CRUD REST API. DB2 cursor logic maps well to JPA/JDBC pagination. Separate DB access layer from presentation logic.

---

### #4 COCRDUPC - Credit Card Update (Score: 7.0)

**Why it's #4:** Large program managing sensitive card data (PAN, CVV, expiration). Regulatory implications (PCI-DSS in modern context).

| Factor | Details |
|--------|---------|
| **Complexity** | 1,560 lines. 191 control-flow statements. Complex screen validation logic. |
| **Coupling** | Reads/writes: CARDDATA, CARDXREF. BMS map COCRDUP. COMMAREA for navigation. |
| **Business Impact** | Direct card data modification. In a real system, PCI-DSS scope. Cardholder name, status, expiration updates. |
| **Migration Risk** | BMS screen interaction. Card number handling (potential encryption needs post-migration). XCTL navigation pattern. |

**Recommendation:** Modernize with encryption-at-rest for card data. Separate validation rules into a domain service. Add audit logging that doesn't exist in COBOL version.

---

### #5 COCRDLIC - Credit Card List (Score: 6.6)

**Why it's #5:** Complex list/pagination logic with multiple navigation paths (view detail, update, return to menu).

| Factor | Details |
|--------|---------|
| **Complexity** | 1,459 lines. 175 control-flow statements. Browse/scroll implementation. |
| **Coupling** | CARDDATA, CARDXREF VSAM files. Navigates to COCRDSLC or COCRDUPC via XCTL. CSLKPCDY lookups. |
| **Business Impact** | Primary card discovery interface. High usage by both users and admins. |
| **Migration Risk** | VSAM browse (START/READNEXT) pattern. Pagination state management via COMMAREA. Multiple XCTL exit points. |

**Recommendation:** Replace VSAM browse with indexed database query + offset/limit pagination. The XCTL multi-destination pattern maps to a front-end router.

---

### #6 COTRTUPC - Transaction Type Add/Edit (Score: 6.5)

**Why it's #6:** DB2 INSERT/UPDATE with validation, combined with CICS screen management.

| Factor | Details |
|--------|---------|
| **Complexity** | 1,702 lines. 170 control-flow statements. Dual-mode (add vs. edit) logic. |
| **Coupling** | DB2 TRAN_TYPE table. BMS map COTRTUP. Validates against existing records. |
| **Business Impact** | Maintains reference data integrity. Incorrect types propagate to all transactions. |
| **Migration Risk** | Embedded SQL. Dual-mode screen (INSERT vs UPDATE paths). Two-phase commit with CICS. |

**Recommendation:** Clean CRUD candidate. Split into separate Create and Update endpoints. Validation logic becomes a shared service.

---

### #7 CBACT04C - Interest Calculation (Score: 6.4)

**Why it's #7:** Financial calculation engine with multi-file lookups and signed decimal arithmetic. Correctness is critical for billing accuracy.

| Factor | Details |
|--------|---------|
| **Complexity** | 652 lines. 144 control-flow statements (22% density). Nested loops over accounts and categories. |
| **Coupling** | Reads: ACCTDATA, CARDXREF, DISCGRP, TCATBALF, TRANSACT. Writes: TRANSACT (interest entries), TCATBALF. |
| **Business Impact** | **Revenue-generating.** Interest charges are a primary income source for card issuers. Calculation errors = financial liability. |
| **Migration Risk** | Signed decimal (S9(10)V99) arithmetic precision. Multiple VSAM file coordination. Rate lookups from DISCGRP. |

**Recommendation:** Requires extensive parallel-run testing. Use BigDecimal in Java with explicit rounding mode. Generate comprehensive test cases from production data before migration.

---

### #8 CBSTM03A - Statement Generation (Score: 5.5)

**Why it's #8:** Complex report generator that calls subroutine CBSTM03B 13 times. Produces customer-facing financial statements.

| Factor | Details |
|--------|---------|
| **Complexity** | 924 lines. 73 control-flow statements. 13 CALL sites to CBSTM03B. Multi-format output. |
| **Coupling** | Calls CBSTM03B (subroutine). Reads sorted transaction input + CARDXREF + CUSTDATA. Writes formatted report output. |
| **Business Impact** | Customer-facing billing statement. Regulatory requirement to produce accurate statements. |
| **Migration Risk** | Report formatting logic (column alignment, page breaks). CALL/subroutine interface. GDG output for versioning. |

**Recommendation:** Replace with a template-based PDF generation engine (e.g., Apache FOP, Jasper). The subroutine pattern maps to method calls in a ReportBuilder class.

---

### #9 COPAUA0C - Process Authorization Requests (Score: 6.1)

**Why it's #9:** Most technically complex integration point. MQ trigger program that reads requests, queries IMS DB, and writes to DB2.

| Factor | Details |
|--------|---------|
| **Complexity** | 1,026 lines. 102 control-flow statements. Three-system integration (MQ + IMS + DB2). |
| **Coupling** | IBM MQ (request/response queues). IMS DB (hierarchical segments). DB2 (auth log). VSAM (account data). |
| **Business Impact** | Real-time authorization decisions. Latency-sensitive. Availability impacts card acceptance. |
| **Migration Risk** | **Highest technical risk.** Three middleware dependencies (MQ, IMS, DB2). PCB definitions. DL/I calls. Two-phase commit across systems. |

**Recommendation:** Redesign as an event-driven microservice. Replace IMS with a relational model. Replace MQ with a modern message broker (Kafka/SQS). This should be one of the later modules migrated due to complexity.

---

### #10 COACTVWC - Account View (Score: 5.7)

**Why it's #10:** Read-only but highly coupled program that joins data from 4 VSAM files for display.

| Factor | Details |
|--------|---------|
| **Complexity** | 941 lines. 89 control-flow statements. Multi-file read joins. |
| **Coupling** | Reads: ACCTDATA, CARDDATA, CUSTDATA, CARDXREF. BMS map COACTVW. |
| **Business Impact** | Most-used screen for customer service. Any outage blocks account inquiries. |
| **Migration Risk** | Four-file "join" logic in procedural COBOL. XCTL back-navigation. CSSTRPFY string processing. |

**Recommendation:** Good early migration candidate (read-only, no writes). Maps to a simple REST GET endpoint backed by a SQL JOIN query. Low risk, high visibility win.

---

## Migration Priority Matrix

```
                    HIGH BUSINESS IMPACT
                           │
    ┌──────────────────────┼──────────────────────┐
    │  MIGRATE LATER       │  MIGRATE WITH CARE   │
    │  (Complex + Critical)│  (Critical + Simpler)│
    │                      │                      │
    │  COPAUA0C            │  CBTRN02C            │
    │  COTRTLIC            │  CBACT04C            │
    │  COTRTUPC            │  CBSTM03A            │
    │                      │                      │
LOW ├──────────────────────┼──────────────────────┤ HIGH
COMPLEXITY                 │                        COMPLEXITY
    │                      │                      │
    │  QUICK WINS          │  MIGRATE FIRST       │
    │  (Simple + Lower Biz)│  (Complex but Good   │
    │                      │   Modernization ROI) │
    │  COACTVWC            │  COACTUPC            │
    │  COCRDLIC            │  COCRDUPC            │
    │                      │                      │
    └──────────────────────┼──────────────────────┘
                           │
                    LOW BUSINESS IMPACT
```

## Recommended Migration Order

| Phase | Modules | Rationale |
|-------|---------|-----------|
| **Phase 1: Quick Wins** | COACTVWC, COTRN00C, COTRN01C | Read-only, demonstrates migration capability |
| **Phase 2: Core CRUD** | COACTUPC, COCRDUPC, COUSR00-03C | Account/Card/User management with writes |
| **Phase 3: Batch Engine** | CBTRN02C, CBACT04C, CBSTM03A | Financial processing - requires extensive testing |
| **Phase 4: DB2 Programs** | COTRTLIC, COTRTUPC, COBTUPDT | Already SQL-based, maps to JPA naturally |
| **Phase 5: Integration** | COPAUA0C, COPAUS0C/1C, COACCT01, CODATE01 | MQ/IMS - requires architecture redesign |

---

## Key Risks Across All Modules

| Risk Category | Affected Modules | Mitigation |
|--------------|-----------------|------------|
| **Decimal Precision** | CBTRN02C, CBACT04C, COACTUPC | Use BigDecimal, define rounding rules, parallel-run comparison |
| **VSAM Semantics** | All batch programs | Map KSDS to indexed tables; browse → cursor/pagination |
| **COMMAREA State** | All online programs | Replace with HTTP session or JWT token state |
| **BMS Screen Logic** | All online programs | Separate presentation from business logic |
| **Two-Phase Commit** | COPAUA0C, COTRTLIC | Use compensating transactions or saga pattern |
| **GDG Versioning** | TRANBKP, CREASTMT | Replace with timestamped file/table partitions |
| **Packed Decimal I/O** | CBEXPORT, CBIMPORT | Decode during data migration, store as native types |
