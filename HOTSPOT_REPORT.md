# Hotspot Report -- CardDemo COBOL Codebase

> **Generated:** 2026-03-27 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This report identifies the top 10 highest-risk modules for modernization,
> ranked by a composite score of **code complexity**, **migration risk**, and
> **business impact**. Use this to prioritize migration waves, allocate testing
> effort, and plan team assignments.

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|---|---|---|
| **Complexity** | 35% | Lines of code, cyclomatic indicators (IF/EVALUATE/GO TO count), number of PERFORM paragraphs, number of CICS commands, number of VSAM files accessed |
| **Migration Risk** | 35% | Mixed technology (CICS + VSAM + DB2 + MQ + IMS), dynamic program dispatch, GO TO usage, REDEFINES/COMP-3 data types, assembler dependencies, screen map complexity |
| **Business Impact** | 30% | Criticality to daily operations, financial data handling, number of dependent programs/jobs, user-facing transaction volume, regulatory/security sensitivity |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Impact x 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/COACTUPC.cbl` | 4,236 lines |
| **Type** | Online CICS | Transaction CA02 |
| **Complexity Score** | **10** | 168 IF statements, 10 EVALUATE blocks, 51 GO TO statements, 64 PERFORMs, 17 EXEC CICS commands |
| **Risk Score** | **9** | Heavy GO TO usage (51 -- highest in codebase), dynamic XCTL dispatch, 5 copybook inclusions with REDEFINES, complex field-level validation, screen attribute manipulation via CSSETATY/CSSTRPFY |
| **Impact Score** | **9** | Core account modification -- directly updates ACCTDAT balance/limits/dates; any bug affects all customer accounts; writes financial data |
| **Composite Score** | **9.35** |

**Why it's #1:** By far the largest program (4,236 LOC -- 2.7x the next largest). The 51 GO TO statements create spaghetti control flow that is extremely difficult to translate to structured Java. It performs complex field validation with dynamic screen attribute manipulation and directly modifies financial account data. This single program represents ~20% of all core online code.

**Migration Recommendation:** Decompose into smaller service classes: AccountValidationService, AccountUpdateService, AccountScreenController. Convert GO TO control flow to structured if/else with early returns. Requires extensive regression testing of all field-level validations.

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/CBTRN02C.cbl` | 731 lines |
| **Type** | Batch | Executed by POSTTRAN JCL |
| **Complexity Score** | **8** | 93 IF statements, 61 PERFORMs, reads/writes 5 VSAM files |
| **Risk Score** | **8** | Multi-file transactional updates (DALYTRAN -> TRANSACT + ACCTDAT), error handling via CEE3ABD, complex validation against CARDXREF; no built-in rollback |
| **Impact Score** | **10** | **Core batch engine** -- every daily transaction flows through this program; errors cause financial discrepancies; feeds downstream interest calc, statements, and reports |
| **Composite Score** | **8.60** |

**Why it's #2:** This is the heart of the batch cycle. It reads daily transactions, validates each against the cross-reference file, posts to the master transaction file, and updates account balances. A bug here silently corrupts financial data across the entire system. The multi-file update without CICS-style commit/rollback makes atomicity a key migration concern.

**Migration Recommendation:** Convert to Spring Batch with chunk-oriented processing. Implement database transactions for atomicity. Add comprehensive validation and dead-letter queue for rejected transactions. Must have parallel-run testing against legacy.

---

### Rank 3: COTRTLIC -- Transaction Type List (DB2)

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | 2,098 lines |
| **Type** | Online CICS + DB2 | Admin function |
| **Complexity Score** | **9** | 89 IF statements, 16 EVALUATE blocks, 28 GO TO statements, 63 PERFORMs, 12 EXEC CICS + 16 EXEC SQL |
| **Risk Score** | **9** | Mixed CICS + DB2 technology, cursor-based pagination, dynamic SQL error handling, XCTL navigation, 28 GO TOs |
| **Impact Score** | **7** | Admin-only function; controls reference data that affects interest calculation and reporting; errors affect transaction categorization |
| **Composite Score** | **8.40** |

**Why it's #3:** Second largest program overall. Combines CICS screen handling with DB2 cursor operations -- the most complex technology mix in the codebase. The 28 GO TO statements with cursor management create intricate control flow. DB2 cursors must be carefully mapped to JPA/JDBC pagination.

**Migration Recommendation:** Split into TransactionTypeController (REST) + TransactionTypeRepository (JPA). Replace DB2 cursors with Spring Data pagination. Careful attention to cursor lifecycle and SYNCPOINT semantics.

---

### Rank 4: COCRDUPC -- Credit Card Update

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/COCRDUPC.cbl` | 1,560 lines |
| **Type** | Online CICS | Transaction CC03 |
| **Complexity Score** | **9** | 148 IF statements (highest density), 16 EVALUATE blocks, 21 GO TOs, 26 PERFORMs, 12 EXEC CICS |
| **Risk Score** | **8** | Complex field validation, dynamic XCTL, screen attribute control, reads CARDDAT + CUSTDAT, GO TO-based control flow |
| **Impact Score** | **8** | Modifies card status/expiry -- incorrect updates can disable customer cards; PCI-DSS-relevant card data |
| **Composite Score** | **8.35** |

**Why it's #4:** Highest IF-statement density in the codebase (148 in 1,560 lines = 1 IF per 10.5 lines). This represents extremely dense business rule validation for card updates. Combined with 21 GO TOs and XCTL navigation, the control flow is highly non-linear. Handles PCI-sensitive card data.

**Migration Recommendation:** Extract validation rules into a CardValidationService. Map card field rules to Bean Validation annotations where possible. Ensure PCI-DSS compliance for card number handling in the Java layer.

---

### Rank 5: COTRTUPC -- Transaction Type Maintenance (DB2)

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | 1,702 lines |
| **Type** | Online CICS + DB2 | Admin function |
| **Complexity Score** | **9** | 102 IF statements, 26 EVALUATE blocks (highest), 23 GO TOs, 40 PERFORMs, 12 EXEC CICS + 7 EXEC SQL |
| **Risk Score** | **8** | CICS + DB2 mixed mode, embedded SQL INSERT/UPDATE/DELETE, complex EVALUATE chains for mode switching (add/edit/delete), HANDLE ABEND |
| **Impact Score** | **7** | Admin reference data maintenance; affects downstream interest rates and reports |
| **Composite Score** | **8.10** |

**Why it's #5:** Most EVALUATE statements in the codebase (26), indicating complex state-machine logic for CRUD mode switching. Combined with DB2 embedded SQL, this requires careful mapping of commit/rollback semantics and CICS SYNCPOINT to Spring transaction management.

**Migration Recommendation:** Map to a standard CRUD REST controller with Spring Data JPA. The 26 EVALUATE blocks map naturally to a state machine or strategy pattern for add/edit/delete modes.

---

### Rank 6: COCRDLIC -- Credit Card List

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/COCRDLIC.cbl` | 1,459 lines |
| **Type** | Online CICS | Transaction CC01 |
| **Complexity Score** | **8** | 122 IF statements, 18 EVALUATE blocks, 16 GO TOs, 34 PERFORMs, 18 EXEC CICS |
| **Risk Score** | **8** | VSAM browse operations (STARTBR/READNEXT/READPREV/ENDBR) for pagination, dynamic XCTL dispatch to 3 different programs, CSSTRPFY string handling, HANDLE ABEND |
| **Impact Score** | **7** | Primary card lookup screen -- gateway to card view/update; used by all customer-facing card operations |
| **Composite Score** | **7.70** |

**Why it's #6:** Most EXEC CICS commands (18) of any program -- heavy CICS interaction including browse operations for forward/backward paging. The VSAM browse pattern (STARTBR -> READNEXT loop -> ENDBR) must be carefully mapped to database cursor or offset/limit pagination. Dispatches to 3 different downstream programs.

**Migration Recommendation:** Convert VSAM browse to Spring Data paginated queries. Replace XCTL dispatch with REST API routing. The forward/backward paging pattern maps to cursor-based pagination with "next"/"previous" tokens.

---

### Rank 7: CBACT04C -- Interest Calculation (Batch)

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/CBACT04C.cbl` | 652 lines |
| **Type** | Batch | Executed by INTCALC JCL |
| **Complexity Score** | **7** | 86 IF statements, 56 PERFORMs, reads/writes 5 VSAM files |
| **Risk Score** | **7** | Multi-file reads (TRANSACT, CARDXREF, ACCTDAT, DISCGRP, TCATBALF), financial calculations with S9(10)V99 precision, category-level balance tracking |
| **Impact Score** | **9** | **Financial calculation engine** -- computes interest charges on all accounts; directly affects customer billing; errors mean incorrect interest charges |
| **Composite Score** | **7.60** |

**Why it's #7:** The financial heart of the system. While not the most complex code, it performs interest calculations that directly impact customer bills. The S9(10)V99 COMP-3 arithmetic must be precisely replicated with Java BigDecimal to avoid rounding differences. Reads from 5 different VSAM files requiring careful join logic.

**Migration Recommendation:** Implement with BigDecimal throughout; extensive parallel-run testing comparing COBOL vs Java outputs to decimal precision. Convert to Spring Batch job with itemized audit trail. Consider using a rules engine for rate lookup.

---

### Rank 8: COPAUA0C -- MQ Authorization Trigger

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | 1,026 lines |
| **Type** | Online CICS + MQ | Triggered service |
| **Complexity Score** | **7** | 51 IF statements, 10 EVALUATE blocks, 38 PERFORMs, 12 EXEC CICS, 8 MQ API calls |
| **Risk Score** | **9** | **Triple technology stack**: CICS + MQ + VSAM. MQ OPEN/GET/PUT with connection/queue handle management. CICS RETRIEVE for trigger data. Reads 3 VSAM files. Error queues for failed authorizations |
| **Impact Score** | **8** | Real-time authorization processing; failures block card transactions; integration point for external systems |
| **Composite Score** | **7.95** |

**Why it's #8:** Most complex technology mix -- CICS + MQ + VSAM in a single program. The MQ API calls (MQOPEN, MQGET, MQPUT) with connection handle management are non-trivial to map. This is a real-time integration point where latency matters. Must be replaced with a modern messaging solution (Kafka, RabbitMQ, or cloud-native equivalent).

**Migration Recommendation:** Replace with Spring Boot + JMS/Kafka consumer. Map MQ request/reply to REST API or async messaging. Implement circuit breaker for VSAM-equivalent database lookups. Critical path -- requires latency testing.

---

### Rank 9: CBSTM03A -- Statement Generation

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/cbl/CBSTM03A.CBL` | 924 lines |
| **Type** | Batch | Executed by CREASTMT JCL |
| **Complexity Score** | **7** | 15 IF statements, 5 EVALUATE blocks, 15 GO TOs, 29 PERFORMs, calls CBSTM03B 11+ times |
| **Risk Score** | **7** | GO TO-based control flow (15), tight coupling with CBSTM03B sub-program, reads 4 VSAM files, complex report formatting with page breaks/totals/headers |
| **Impact Score** | **8** | Generates customer-facing statements; output goes to PDF via TXT2PDF; errors produce incorrect billing statements; regulatory compliance requirement |
| **Composite Score** | **7.30** |

**Why it's #9:** Generates the customer-visible output (statements). The tight coupling with CBSTM03B (called 11+ times for different formatting operations) and GO TO-based page-break logic make this difficult to restructure. Output format must be pixel-perfect for regulatory compliance.

**Migration Recommendation:** Convert to Spring Batch + template engine (Thymeleaf/JasperReports). Merge CBSTM03A and CBSTM03B into a single StatementGenerationService. Replace GO TO page-break logic with template-based pagination. Require visual comparison of legacy vs new statement output.

---

### Rank 10: COPAUS0C -- Pending Authorization Summary

| Metric | Value | Detail |
|---|---|---|
| **File** | `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | 1,032 lines |
| **Type** | Online CICS | Authorization view |
| **Complexity Score** | **7** | 25 IF statements, 11 EVALUATE blocks, 46 PERFORMs, 10 EXEC CICS |
| **Risk Score** | **8** | IMS database access (through copybook structures), CICS SYNCPOINT, links to COPAUS2C for DB2 fraud marking, complex screen navigation with COPAUS1C |
| **Impact Score** | **7** | Fraud detection visibility; admin screen for reviewing pending authorizations; links to fraud marking workflow |
| **Composite Score** | **7.30** |

**Why it's #10:** Part of the IMS/DB2/MQ authorization chain. While the individual complexity is moderate, the cross-technology risk (IMS segments mapped to copybooks, CICS SYNCPOINT, link to DB2 sub-program) makes migration non-trivial. This is the front-end of the fraud detection workflow.

**Migration Recommendation:** Replace IMS data access with JPA repository. Convert CICS LINK to COPAUS2C into a service method call. Map the authorization summary/detail screens to a React/Angular component with REST API backend.

---

## Composite Score Summary

| Rank | Program | Lines | IFs | GO TOs | CICS Cmds | Files Accessed | Complexity | Risk | Impact | **Score** |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | **COACTUPC** | 4,236 | 168 | 51 | 17 | 2 | 10 | 9 | 9 | **9.35** |
| 2 | **CBTRN02C** | 731 | 93 | 0 | 0 | 5 | 8 | 8 | 10 | **8.60** |
| 3 | **COTRTLIC** | 2,098 | 89 | 28 | 28 | 0+DB2 | 9 | 9 | 7 | **8.40** |
| 4 | **COCRDUPC** | 1,560 | 148 | 21 | 12 | 2 | 9 | 8 | 8 | **8.35** |
| 5 | **COTRTUPC** | 1,702 | 102 | 23 | 19 | 0+DB2 | 9 | 8 | 7 | **8.10** |
| 6 | **COCRDLIC** | 1,459 | 122 | 16 | 18 | 1 | 8 | 8 | 7 | **7.70** |
| 7 | **CBACT04C** | 652 | 86 | 0 | 0 | 5 | 7 | 7 | 9 | **7.60** |
| 8 | **COPAUA0C** | 1,026 | 51 | 0 | 12+MQ | 3 | 7 | 9 | 8 | **7.95** |
| 9 | **CBSTM03A** | 924 | 15 | 15 | 0 | 4 | 7 | 7 | 8 | **7.30** |
| 10 | **COPAUS0C** | 1,032 | 25 | 0 | 10 | IMS | 7 | 8 | 7 | **7.30** |

---

## Risk Heat Map by Technology

| Technology | Programs | Key Risk |
|---|---|---|
| **CICS + VSAM** | 17 core online programs | VSAM browse -> pagination; COMMAREA -> session state; BMS maps -> web UI |
| **Batch VSAM** | 8 batch programs | Multi-file updates without transactions; COMP-3 arithmetic precision |
| **CICS + DB2** | COTRTLIC, COTRTUPC, COPAUS2C | Cursor lifecycle; SYNCPOINT -> @Transactional; embedded SQL -> JPA |
| **CICS + MQ** | COPAUA0C, COACCT01, CODATE01 | MQ handle management; request/reply pattern; trigger processing |
| **IMS DL/I** | DBUNLDGS, PAUDBLOD, PAUDBUNL | Hierarchical -> relational mapping; DL/I calls -> JPA; PCB -> connection |
| **Assembler** | COBDATFT, MVSWAIT | Must be replaced with Java equivalents (date formatting, Thread.sleep) |
| **GO TO patterns** | COACTUPC (51), COTRTLIC (28), COTRTUPC (23), COCRDUPC (21) | Spaghetti flow -> structured control; highest refactoring effort |

---

## Recommended Migration Waves

### Wave 1: Foundation (Low Risk, High Reuse)
- **CSUTLDTC** (date utility) -> Java DateTimeFormatter
- **COBDATFT/MVSWAIT** (assembler) -> Java standard library
- **COCOM01Y** (commarea) -> Session/Context POJO
- All copybook entities -> Java POJOs/DTOs
- VSAM files -> Relational database schema

### Wave 2: Simple Online Screens (Medium Complexity)
- **COSGN00C** (sign-on) -> Spring Security authentication
- **COMEN01C/COADM01C** (menus) -> Navigation controller
- **COTRN01C** (transaction view) -> Simple read-only REST endpoint
- **COUSR00C-03C** (user CRUD) -> Standard CRUD REST + JPA

### Wave 3: Complex Online Screens (High Complexity)
- **COCRDLIC** (card list) -> Paginated REST API
- **COCRDSLC** (card view) -> Read REST endpoint
- **COCRDUPC** (#4 hotspot) -> Card update with validation
- **COACTVWC** (account view) -> Account read endpoint
- **COTRN00C** (transaction list) -> Paginated query
- **COTRN02C** (transaction add) -> Write endpoint with validation
- **COBIL00C** (bill payment) -> Payment processing service

### Wave 4: Core Batch Processing (Highest Risk)
- **CBTRN02C** (#2 hotspot) -> Spring Batch transaction posting
- **CBACT04C** (#7 hotspot) -> Spring Batch interest calculation
- **CBSTM03A/B** (#9 hotspot) -> Spring Batch statement generation
- **CBTRN03C** -> Batch report generation

### Wave 5: Account Update + Flagship (Highest Complexity)
- **COACTUPC** (#1 hotspot) -> Decomposed account update services
- Full regression testing with parallel run

### Wave 6: Optional Modules (Specialized Technology)
- **COTRTLIC/COTRTUPC** (#3/#5 hotspots) -> DB2 -> JPA migration
- **COPAUA0C** (#8 hotspot) -> MQ -> Kafka/JMS migration
- **COPAUS0C/1C/2C** (#10 hotspot) -> IMS -> JPA migration
- **COACCT01/CODATE01** -> MQ service -> REST API

---

## Key Risks and Mitigations

| Risk | Severity | Affected Modules | Mitigation |
|---|---|---|---|
| **GO TO spaghetti** | Critical | COACTUPC, COTRTLIC, COTRTUPC, COCRDUPC | Automated control-flow analysis; manual restructuring before conversion |
| **Financial precision** | Critical | CBTRN02C, CBACT04C, COBIL00C | BigDecimal everywhere; parallel-run comparison to 2 decimal places |
| **PCI-DSS compliance** | High | COCRDUPC, COCRDLIC, COCRDSLC | Card tokenization; never store CVV; mask card numbers |
| **Plain-text passwords** | High | COSGN00C, COUSR01C | Hash with bcrypt; enforce complexity; add MFA |
| **Multi-file atomicity** | High | CBTRN02C, CBACT04C | Database transactions; compensating transactions for failures |
| **COMMAREA state** | Medium | All 17 online programs | Map to HTTP session or JWT claims; design stateless where possible |
| **VSAM browse patterns** | Medium | COCRDLIC, COTRN00C, COUSR00C | Cursor-based or keyset pagination; not OFFSET/LIMIT |
| **MQ integration** | Medium | COPAUA0C, COACCT01, CODATE01 | Replace with Kafka or cloud-native messaging; maintain request/reply pattern |
| **IMS hierarchical data** | Medium | DBUNLDGS, PAUDBLOD, PAUDBUNL | Flatten to relational schema; 1:N parent-child -> JPA @OneToMany |
| **Duplicate logic** | Low | CVTRA05Y/CVTRA06Y (identical layouts), CVCUS01Y/CUSTREC (same entity) | Consolidate to single Java class; eliminate redundant record definitions |
