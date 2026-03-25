# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Methodology**: Weighted scoring across Complexity, Risk, and Business Impact

---

## Scoring Methodology

Each module is scored on three dimensions (1–5 scale):

| Dimension          | Weight | Criteria                                                                 |
|--------------------|--------|--------------------------------------------------------------------------|
| **Complexity**     | 35%    | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), COPY count, number of PERFORM paragraphs, external calls |
| **Risk**           | 30%    | Data sensitivity (PII/PCI), write operations to critical files, error handling gaps, shared state via COMMAREA |
| **Business Impact**| 35%    | Revenue criticality, user-facing frequency, downstream dependencies, batch cycle position |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.30) + (Business Impact × 0.35), scaled to 100.

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | **4,236** (largest program in the system by 2.7×)                    |
| **Complexity Score** | **5/5** — 38 COPY REPLACING macros (CSSETATY), deeply nested EVALUATE/IF, 50+ PERFORM paragraphs, inline date validation, FICO score editing, US state code validation, field-level attribute control |
| **Risk Score**       | **5/5** — Writes to ACCTDATA (financial balances), reads CUSTDATA (PII/SSN), reads CARDXREF (PAN), handles credit limits and cash advance limits |
| **Business Impact**  | **5/5** — Core account maintenance screen; every balance adjustment, credit limit change, and account status update flows through this program |
| **Composite Score**  | **100** |
| **Key Concerns**     | Monolithic design: UI rendering, validation, VSAM I/O, and business rules all in one program. The 38× COPY REPLACING pattern creates massive code expansion. Field-level attribute management is brittle and error-prone. |
| **Modernization Rec**| Decompose into: (1) Account REST API service, (2) Validation service, (3) UI form component. Extract the 38 attribute-setting macros into a reusable UI framework. |

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 731                                                                   |
| **Complexity Score** | **4/5** — Reads 3 VSAM files + 1 sequential input, writes to 3 files (TRANSACT, TCATBAL, DALYREJS), complex validation logic with reject handling, multi-file coordination |
| **Risk Score**       | **5/5** — **Financial core**: posts transactions to master file, updates account balances and category balances. A bug here means incorrect balances across the entire portfolio. Writes rejected transactions to separate file. |
| **Business Impact**  | **5/5** — The heart of the daily batch cycle. Every transaction in the system is posted by this program. Downstream programs (INTCALC, CREASTMT, TRANREPT) all depend on its output. |
| **Composite Score**  | **94** |
| **Key Concerns**     | Multi-file transactional consistency without database-level commit/rollback. Reject handling creates a separate output stream that must be manually reconciled. No retry logic for partial failures. |
| **Modernization Rec**| Convert to Spring Batch with database transactions (ACID). Implement dead-letter queue pattern for rejects. Add idempotency keys to prevent double-posting. |

---

### Rank 3: CBACT04C — Interest Calculation Engine

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 652                                                                   |
| **Complexity Score** | **4/5** — Reads 5 VSAM files, performs multi-step financial calculations (interest + fees), nested loops through category balances, cross-references disclosure groups for rate lookup |
| **Risk Score**       | **5/5** — **Financial calculation**: computes interest charges applied to customer accounts. Incorrect calculations directly impact revenue and regulatory compliance. Writes updated balances back to ACCTDATA. |
| **Business Impact**  | **5/5** — Runs after transaction posting in batch cycle. Interest revenue is a primary income stream. Must be auditable and regulatorily compliant (TILA, Reg Z). |
| **Composite Score**  | **94** |
| **Key Concerns**     | Financial precision with COBOL COMPUTE using S9(10)V99 arithmetic. Rate lookup joins across TCATBAL → CARDXREF → DISCGRP. No unit test coverage. Date-based parameter passing via JCL PARM. |
| **Modernization Rec**| Implement as a calculation service with BigDecimal arithmetic. Add comprehensive unit tests with known test vectors. Create audit trail for every calculation. Externalize rate configuration. |

---

### Rank 4: COCRDUPC — Credit Card Update

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 1,560                                                                |
| **Complexity Score** | **4/5** — Similar pattern to COACTUPC but for card data. Field validation, attribute management, VSAM READ/REWRITE cycle. Multiple COPY includes. |
| **Risk Score**       | **4/5** — Writes to CARDDATA (PAN, CVV, expiration). PCI-DSS compliance scope. Card status changes (active/inactive) affect transaction authorization. |
| **Business Impact**  | **4/5** — Card maintenance is frequent but less critical than account-level changes. Card reissue, status changes, and name updates flow through here. |
| **Composite Score**  | **80** |
| **Key Concerns**     | PCI-DSS scope: handles card numbers and CVVs. Card status changes should trigger downstream notifications. No audit trail for who changed what. |
| **Modernization Rec**| Implement PCI-compliant card vault service. Tokenize PANs. Add change audit logging. Separate card lifecycle management from UI. |

---

### Rank 5: COCRDLIC — Credit Card List

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 1,459                                                                |
| **Complexity Score** | **4/5** — Complex list pagination logic using VSAM STARTBR/READNEXT/READPREV. Multiple selection modes (view/update). Dynamic screen building with variable-length list. |
| **Risk Score**       | **3/5** — Read-only for data, but displays card numbers on screen (PCI masking needed). Navigation hub that can XCTL to COCRDSLC or COCRDUPC. |
| **Business Impact**  | **4/5** — Primary card lookup screen. Every card operation starts here. High usage frequency. |
| **Composite Score**  | **73** |
| **Key Concerns**     | VSAM browse logic (STARTBR/READNEXT/RESETBR) is complex and stateful across CICS pseudo-conversational cycles. Card numbers displayed unmasked. |
| **Modernization Rec**| Replace with paginated REST API + grid UI component. Implement PAN masking (show last 4 only). Server-side pagination instead of VSAM browse. |

---

### Rank 6: CBSTM03A/B — Statement Generation

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 924 + 230 = 1,154 (combined)                                        |
| **Complexity Score** | **4/5** — Main/subroutine architecture (CBSTM03A calls CBSTM03B via CALL). Dual output: plain text + HTML. Nested loops: XREF → Customer → Account → Transactions. Mainframe control block addressing. 14 CALL invocations to subroutine. |
| **Risk Score**       | **3/5** — Read-only from VSAM files. Writes output files. Lower financial risk but statements are customer-facing and regulated (TILA). |
| **Business Impact**  | **4/5** — Customer-facing statements. Regulatory requirement. Generated at end of batch cycle. |
| **Composite Score**  | **73** |
| **Key Concerns**     | CALL-based subroutine architecture with shared data area (WS-M03B-AREA). HTML generation via COBOL string concatenation is fragile. Mainframe-specific TIOT addressing. Statement format must comply with regulations. |
| **Modernization Rec**| Replace with template engine (Thymeleaf/FreeMarker). Generate PDF directly. Implement as Spring Batch step with itemReader/itemWriter pattern. |

---

### Rank 7: COACTVWC — Account View

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 941                                                                  |
| **Complexity Score** | **3/5** — Read-only but performs 3 VSAM reads (ACCTDATA, CARDXREF, CUSTDATA). Screen formatting logic. Error handling with SEND TEXT for long messages. |
| **Risk Score**       | **3/5** — Displays PII (customer name, SSN area) and financial data (balances, credit limits). Read-only reduces write risk. |
| **Business Impact**  | **4/5** — Most-used screen for customer service inquiries. First screen agents navigate to for account lookup. High query volume. |
| **Composite Score**  | **67** |
| **Key Concerns**     | Displays sensitive data without role-based field masking. Three sequential VSAM reads per screen display could be consolidated. ABEND handling inline. |
| **Modernization Rec**| Convert to read-only REST endpoint with field-level access control. Cache frequently accessed accounts. Add PII masking based on user role. |

---

### Rank 8: COTRN02C — Transaction Add (Online)

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 783                                                                  |
| **Complexity Score** | **3/5** — Input validation, VSAM writes to TRANSACT and TCATBAL, CICS ASKTIME for timestamps. Reads CARDXREF for card validation. |
| **Risk Score**       | **4/5** — Creates new financial transactions. Writes to TRANSACT master and updates TCATBAL. Online transaction entry is a fraud vector. |
| **Business Impact**  | **4/5** — Manual transaction entry point (adjustments, corrections). Less volume than batch posting but used for real-time corrections. |
| **Composite Score**  | **73** |
| **Key Concerns**     | Online writes to TRANSACT can conflict with batch posting if CICS files aren't closed. No duplicate transaction detection. Timestamp generation via CICS ASKTIME. |
| **Modernization Rec**| Implement as transactional REST endpoint with optimistic locking. Add duplicate detection. Integrate with fraud screening service. |

---

### Rank 9: COTRN00C — Transaction List

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 699                                                                  |
| **Complexity Score** | **3/5** — VSAM browse with pagination (similar pattern to COCRDLIC). Filter by account. Navigation to COTRN01C for detail view. |
| **Risk Score**       | **2/5** — Read-only. Displays transaction history. Low write risk. |
| **Business Impact**  | **3/5** — Transaction inquiry is frequent but not business-critical. Supports customer service and dispute resolution. |
| **Composite Score**  | **53** |
| **Key Concerns**     | VSAM browse pagination state management across pseudo-conversational cycles. Performance with large transaction volumes. |
| **Modernization Rec**| Replace with paginated REST API. Add search/filter capabilities. Consider Elasticsearch for transaction search at scale. |

---

### Rank 10: CBTRN03C — Transaction Detail Report (Batch)

| Metric              | Value / Assessment                                                    |
|----------------------|-----------------------------------------------------------------------|
| **Lines of Code**    | 649                                                                  |
| **Complexity Score** | **3/5** — Reads 6 files (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, REPTFILE). Report formatting with page/account/grand totals. Date range filtering. |
| **Risk Score**       | **2/5** — Read-only report generation. Output goes to print file. Low risk of data corruption. |
| **Business Impact**  | **3/5** — Daily transaction report used by operations and audit. Regulatory requirement for transaction monitoring. |
| **Composite Score**  | **53** |
| **Key Concerns**     | Reads 6 files with sequential cross-referencing (no indexed joins). Report formatting hardcoded in COBOL. Date parameter file must be maintained. Uses CVTRA07Y copybook for print layout. |
| **Modernization Rec**| Replace with SQL-based reporting (Spring Batch + JasperReports or similar). Parameterize via API instead of flat file. Output to PDF/Excel. |

---

## Summary Heatmap

```
                 Complexity    Risk    Business Impact    Composite
                 ──────────   ──────   ───────────────    ─────────
 1. COACTUPC     █████ 5     █████ 5   █████ 5           ███████ 100
 2. CBTRN02C     ████░ 4     █████ 5   █████ 5           ██████░  94
 3. CBACT04C     ████░ 4     █████ 5   █████ 5           ██████░  94
 4. COCRDUPC     ████░ 4     ████░ 4   ████░ 4           █████░░  80
 5. COCRDLIC     ████░ 4     ███░░ 3   ████░ 4           ████░░░  73
 6. CBSTM03A/B   ████░ 4     ███░░ 3   ████░ 4           ████░░░  73
 7. COACTVWC     ███░░ 3     ███░░ 3   ████░ 4           ████░░░  67
 8. COTRN02C     ███░░ 3     ████░ 4   ████░ 4           ████░░░  73
 9. COTRN00C     ███░░ 3     ██░░░ 2   ███░░ 3           ███░░░░  53
10. CBTRN03C     ███░░ 3     ██░░░ 2   ███░░ 3           ███░░░░  53
```

---

## Recommended Modernization Waves

### Wave 1 — Foundation (Weeks 1–4)
**Target**: Data layer and utilities
- Convert VSAM KSDS files → relational database tables (PostgreSQL/Oracle)
- Implement JPA entities from copybook PIC clauses (see DATA_DICTIONARY.md)
- Build shared services: date utilities (replace CSUTLDTC/COBDATFT), lookup tables (replace CSLKPCDY)
- Implement user authentication with proper password hashing (replace COSGN00C/CSUSR01Y plaintext)

### Wave 2 — Batch Core (Weeks 5–8)
**Target**: Hotspots #2, #3, #6, #10
- CBTRN02C → Spring Batch transaction posting job with database transactions
- CBACT04C → Interest calculation service with BigDecimal and audit trail
- CBSTM03A/B → Statement generation with PDF template engine
- CBTRN03C → SQL-based reporting with parameterized queries

### Wave 3 — Online CRUD (Weeks 9–14)
**Target**: Hotspots #1, #4, #5, #7, #8, #9
- COACTUPC → Account management REST API + React/Angular form
- COCRDUPC → Card management REST API with PCI tokenization
- COCRDLIC/COACTVWC → Read APIs with pagination
- COTRN02C (online) → Transaction entry API with fraud screening
- COTRN00C/COTRN01C → Transaction inquiry API

### Wave 4 — Admin & Extensions (Weeks 15–18)
**Target**: Admin functions and optional modules
- COUSR00C–COUSR03C → User management with Spring Security
- COADM01C/COMEN01C → Replace with web application routing
- Optional: Authorization module (IMS/DB2/MQ) → Event-driven architecture
- Optional: Transaction type DB2 module → Standard CRUD service

---

## Risk Mitigation Notes

1. **Data Migration**: VSAM → RDBMS migration must preserve all key structures, alternate indexes, and record-level locking semantics.
2. **Batch Window**: The CLOSEFIL→OPENFIL cycle must be replaced with online/batch coexistence (no more file locking).
3. **Financial Precision**: All monetary calculations must use BigDecimal (not double/float) to match COBOL `S9(n)V99` precision.
4. **PCI Compliance**: Card numbers (PAN), CVV, and expiration dates require tokenization and encryption in the new platform.
5. **Regression Testing**: Use `app/data/ASCII/` sample data as golden test sets. Verify account balances, interest calculations, and statement output character-by-character.
6. **Parallel Run**: Recommended 2–4 week parallel run comparing mainframe and Java batch output before cutover.
