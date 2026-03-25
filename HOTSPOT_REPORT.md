# CardDemo Hotspot Report - Top 10 Modules

> **Generated**: 2026-03-25 | **Methodology**: Weighted scoring of LOC, cyclomatic complexity indicators, coupling, data sensitivity, and business criticality
> **Purpose**: Prioritize modules for modernization based on risk and effort

---

## Scoring Methodology

Each module is scored on five dimensions (1-5 scale):

| Dimension          | Weight | Criteria                                                                                    |
|--------------------|-------:|---------------------------------------------------------------------------------------------|
| **Code Complexity**    | 30%    | Lines of code, number of PERFORMs, EVALUATE/IF nesting, number of COPY statements          |
| **Coupling Risk**      | 25%    | Number of copybook dependencies, files accessed, programs called/calling                    |
| **Data Sensitivity**   | 20%    | PII fields handled, financial calculations, security-critical operations                    |
| **Business Impact**    | 15%    | Revenue impact, user-facing criticality, regulatory exposure                                |
| **Migration Difficulty**| 10%   | Non-standard patterns, assembler calls, CICS-specific features, MQ/IMS/DB2 dependencies    |

**Score Formula**: `(Complexity x 0.30) + (Coupling x 0.25) + (Data Sensitivity x 0.20) + (Business Impact x 0.15) + (Migration Difficulty x 0.10)`

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC - Account Update

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   5   | **4,236 lines** - largest program in the codebase by far. Heavy validation logic with nested EVALUATE/IF. 14 COPY statements. |
| Coupling Risk      |   5   | Accesses 4 VSAM files (ACCTDATA R/W, CUSTDATA R, CARDXREF R, USRSEC R). 14 copybook dependencies including CSLKPCDY (1,318 lines of lookup tables). |
| Data Sensitivity   |   5   | Modifies account balances, credit limits, and account status. Financial data integrity critical. |
| Business Impact    |   5   | Core account management. Errors directly impact customer balances and credit availability. Regulatory audit trail required. |
| Migration Difficulty|  4   | Complex BMS map interaction (COACTUP - 426 lines), date validation via CSUTLDWY, state/ZIP lookups via CSLKPCDY. |
| **Weighted Score** | **4.85** | |

**Modernization Notes**:
- Break into multiple services: AccountValidation, AccountUpdate, AddressValidation
- Extract CSLKPCDY lookups into a reference data service
- Implement optimistic locking (COBOL uses READ-FOR-UPDATE)
- Map to REST PUT `/api/accounts/{id}` endpoint

---

### Rank 2: CBTRN02C - Transaction Posting (Batch)

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **731 lines**. Multi-step validation, cross-file lookups, reject handling. Opens 6 files simultaneously. |
| Coupling Risk      |   5   | Reads/writes 6 VSAM files (DALYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBALF, DALYREJS). 5 copybook dependencies. |
| Data Sensitivity   |   5   | Posts financial transactions. Updates account balances. Creates category balance aggregates. |
| Business Impact    |   5   | **Core batch processing** - if this fails, no transactions are posted. Direct revenue impact. |
| Migration Difficulty|  3   | Standard batch pattern. Maps well to Spring Batch ItemReader/Processor/Writer. |
| **Weighted Score** | **4.55** | |

**Modernization Notes**:
- Map to Spring Batch job with chunk-oriented processing
- Implement transaction rollback (COBOL uses manual file status checks)
- DALYREJS (rejects file) should become a database table or dead-letter queue
- Consider event-driven architecture for real-time posting

---

### Rank 3: COTRTLIC - Transaction Type List (DB2)

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   5   | **2,098 lines**. Complex DB2 cursor-based paging with forward/backward navigation. |
| Coupling Risk      |   4   | DB2 + CICS + BMS. Embedded SQL with cursor management. Multiple copybook dependencies. |
| Data Sensitivity   |   3   | Reference data management. Low financial risk but affects transaction classification. |
| Business Impact    |   3   | Admin function for transaction type configuration. Affects all downstream processing. |
| Migration Difficulty|  5   | DB2 cursor paging logic with FETCH NEXT/PRIOR. Embedded SQL must be converted to JPA/JDBC. |
| **Weighted Score** | **4.10** | |

**Modernization Notes**:
- Convert embedded SQL to Spring Data JPA with `Pageable`
- Replace cursor-based paging with offset/limit queries
- Map to REST GET `/api/admin/transaction-types?page=N`

---

### Rank 4: COTRTUPC - Transaction Type Update (DB2)

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   5   | **1,702 lines**. Complex validation, DB2 INSERT/UPDATE/DELETE with error handling. Date validation via CSUTLDWY. |
| Coupling Risk      |   4   | DB2 CRUD operations + CICS + BMS. Two SQL INCLUDE statements (DCLTRTYP, DCLTRCAT). |
| Data Sensitivity   |   3   | Manages reference data that controls financial categorization. |
| Business Impact    |   3   | Admin function. Changes affect transaction categorization and reporting. |
| Migration Difficulty|  5   | Embedded SQL CRUD must become JPA repository methods. Complex screen-to-DB mapping. |
| **Weighted Score** | **4.10** | |

**Modernization Notes**:
- Convert to Spring Data JPA @Repository with @Transactional methods
- Map to REST PUT/POST/DELETE `/api/admin/transaction-types/{id}`
- Replace CICS SYNCPOINT with Spring @Transactional

---

### Rank 5: COCRDLIC - Credit Card List

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **1,459 lines**. Complex browsing with STARTBR/READNEXT/READPREV. Forward and backward pagination. Conditional display based on user type. |
| Coupling Risk      |   4   | VSAM KSDS browse operations. 8 copybook dependencies. XCTLs to COCRDSLC and COCRDUPC. |
| Data Sensitivity   |   4   | Displays card numbers (PAN) - PCI DSS compliance concern. Must be masked in modernized system. |
| Business Impact    |   4   | Primary card lookup screen. Gateway to card detail and update operations. |
| Migration Difficulty|  3   | VSAM browse maps to JPA pagination. STARTBR/READNEXT pattern maps to cursor-based API. |
| **Weighted Score** | **3.85** | |

**Modernization Notes**:
- Implement card number masking (show only last 4 digits) per PCI DSS
- Convert VSAM browse to Spring Data JPA `findAll(Pageable)`
- Map to REST GET `/api/cards?page=N&accountId=X`

---

### Rank 6: COPAUA0C - Card Authorization Decision (MQ)

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **1,026 lines**. MQ message processing with multiple queue operations. Cross-reference and account lookups. |
| Coupling Risk      |   5   | MQ Series (MQOPEN, MQGET, MQPUT, MQCLOSE) + CICS + VSAM reads (ACCTDATA, CARDXREF, CUSTDATA). Triggered by MQ. |
| Data Sensitivity   |   5   | **Real-time authorization decisions**. Approves/declines card transactions. Direct financial impact. |
| Business Impact    |   5   | Revenue-critical. Authorization failures = lost sales. Must have sub-second response time. |
| Migration Difficulty|  4   | MQ integration must be replaced with modern messaging (Kafka, RabbitMQ). CICS RETRIEVE for trigger data. |
| **Weighted Score** | **4.65** | |

**Modernization Notes**:
- Replace MQ trigger with event-driven microservice (Kafka consumer or REST endpoint)
- Maintain sub-second SLA for authorization decisions
- Implement circuit breaker pattern for downstream service calls
- Consider separate deployment for authorization (independent scaling)

> **Note**: Ranked #6 overall because it is in the optional authorization module, not the core application. If authorization is in scope, this should be prioritized higher.

---

### Rank 7: COPAUS0C - Authorization Summary View

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **1,032 lines**. IMS database browse with BMS screen interaction. Multiple CICS commands. |
| Coupling Risk      |   4   | IMS DB + CICS + BMS + VSAM reads. 14 copybook dependencies. |
| Data Sensitivity   |   4   | Displays authorization decisions and account status. Fraud detection data. |
| Business Impact    |   3   | Operational monitoring. Used by fraud analysts to review authorization patterns. |
| Migration Difficulty|  5   | IMS DL/I calls must be converted to relational queries. Most complex migration pattern. |
| **Weighted Score** | **3.90** | |

**Modernization Notes**:
- Replace IMS segment navigation with JPA entity relationships
- Convert BMS screen to web dashboard with filtering and search
- Add real-time refresh capability (replace terminal polling)

---

### Rank 8: COACTVWC - Account View

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **941 lines**. Multi-file reads to assemble complete account picture. Complex screen population. |
| Coupling Risk      |   4   | Reads 4 VSAM files (ACCTDATA, CARDDATA, CUSTDATA, CARDXREF). 14 copybook dependencies. |
| Data Sensitivity   |   4   | Displays customer PII (name, address), account balances, and card details. |
| Business Impact    |   4   | Most frequently accessed screen - primary customer service tool. |
| Migration Difficulty|  3   | Read-only screen. Maps cleanly to a REST GET endpoint with DTO assembly. |
| **Weighted Score** | **3.85** | |

**Modernization Notes**:
- Map to REST GET `/api/accounts/{id}/details` (aggregate multiple entities)
- Implement a read-optimized view/DTO (AccountDetailsDTO)
- Consider CQRS pattern: separate read model from write model

---

### Rank 9: CBSTM03A - Statement Generation

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **924 lines**. Multi-file cross-reference assembly. Calls subroutine CBSTM03B. Generates HTML output. |
| Coupling Risk      |   4   | Reads 4 VSAM files. Calls CBSTM03B subroutine. Writes HTML and text statement files. |
| Data Sensitivity   |   5   | Generates customer statements with full financial details, PII, and transaction history. Regulatory document. |
| Business Impact    |   4   | Monthly customer-facing deliverable. Errors visible to customers. Compliance requirement. |
| Migration Difficulty|  3   | HTML generation maps to a templating engine (Thymeleaf, Jasper Reports). |
| **Weighted Score** | **4.05** | |

**Modernization Notes**:
- Replace inline HTML generation with a template engine (Thymeleaf or JasperReports)
- Map CBSTM03A + CBSTM03B to a single Spring Batch job with a Statement service
- Add PDF generation natively (replace TXT2PDF1 JCL step)
- Consider e-statement delivery via email/portal

---

### Rank 10: COCRDUPC - Credit Card Update

| Dimension          | Score | Rationale                                                                     |
|--------------------|------:|-------------------------------------------------------------------------------|
| Code Complexity    |   4   | **1,399 lines**. Field-level validation, status transitions, card reissue logic. |
| Coupling Risk      |   3   | Reads/writes CARDDATA VSAM. 10 copybook dependencies. |
| Data Sensitivity   |   5   | Modifies card status, embossed name, expiration date. Card data is PCI DSS scope. |
| Business Impact    |   4   | Card lifecycle management. Incorrect updates can disable customer cards. |
| Migration Difficulty|  3   | Standard CRUD with validation. Maps to REST PUT `/api/cards/{cardNum}`. |
| **Weighted Score** | **3.85** | |

**Modernization Notes**:
- Implement PCI DSS controls in modernized system
- Card number should be tokenized, not stored in plaintext
- Add audit logging for all card modifications
- Implement card status state machine (Active -> Suspended -> Closed)

---

## Summary Table

| Rank | Module    | Lines | Type       | Score | Primary Risk                              | Recommended Wave |
|------|-----------|------:|------------|------:|-------------------------------------------|-----------------|
|  1   | COACTUPC  | 4,236 | Online     | 4.85  | Complexity + financial data integrity     | Wave 2 (core)   |
|  2   | CBTRN02C  |   731 | Batch      | 4.55  | Multi-file financial posting              | Wave 1 (batch)  |
|  3   | COTRTLIC  | 2,098 | Online/DB2 | 4.10  | DB2 cursor complexity                     | Wave 3 (optional)|
|  4   | COTRTUPC  | 1,702 | Online/DB2 | 4.10  | DB2 CRUD complexity                       | Wave 3 (optional)|
|  5   | COCRDLIC  | 1,459 | Online     | 3.85  | PCI DSS + browse complexity               | Wave 2 (core)   |
|  6   | COPAUA0C  | 1,026 | Online/MQ  | 4.65  | Real-time auth + MQ dependency            | Wave 3 (optional)|
|  7   | COPAUS0C  | 1,032 | Online/IMS | 3.90  | IMS dependency                            | Wave 3 (optional)|
|  8   | COACTVWC  |   941 | Online     | 3.85  | Multi-file read + PII exposure            | Wave 1 (core)   |
|  9   | CBSTM03A  |   924 | Batch      | 4.05  | Statement generation + compliance         | Wave 2 (batch)  |
| 10   | COCRDUPC  | 1,399 | Online     | 3.85  | PCI DSS + card lifecycle                  | Wave 2 (core)   |

---

## Recommended Migration Waves

### Wave 1 - Foundation (Low Risk, High Value)
**Goal**: Establish patterns and prove the migration approach.

| Module   | Rationale                                                      |
|----------|----------------------------------------------------------------|
| COACTVWC | Read-only, well-bounded. Good first screen to migrate.         |
| CBTRN02C | Core batch. Establishes Spring Batch patterns for all batch.   |
| COSGN00C | Simple auth flow. Establishes Spring Security patterns.        |
| COMEN01C | Menu routing. Establishes navigation/routing patterns.         |

### Wave 2 - Core Business Logic
**Goal**: Migrate the highest-value and most complex core modules.

| Module   | Rationale                                                      |
|----------|----------------------------------------------------------------|
| COACTUPC | Highest complexity. Establishes write/validation patterns.     |
| COCRDLIC | Card browse. Establishes paginated list patterns.              |
| COCRDUPC | Card update. Completes card management domain.                 |
| CBSTM03A | Statement generation. Customer-facing deliverable.             |
| COBIL00C | Bill payment. Revenue-critical financial operation.            |

### Wave 3 - Optional Modules & Remaining
**Goal**: Migrate optional DB2/IMS/MQ modules and remaining programs.

| Module   | Rationale                                                      |
|----------|----------------------------------------------------------------|
| COTRTLIC | DB2 module. Requires DB2-to-JPA migration patterns.            |
| COTRTUPC | DB2 module. Pairs with COTRTLIC.                               |
| COPAUA0C | MQ authorization. Requires messaging migration patterns.       |
| COPAUS0C | IMS module. Most complex migration (IMS-to-relational).        |
| Remaining COUSR*, COTRN* | Straightforward CRUD following established patterns. |

---

## Key Risks and Mitigations

| Risk                           | Impact | Mitigation                                                          |
|--------------------------------|--------|---------------------------------------------------------------------|
| Financial calculation drift    | High   | Parallel run: execute COBOL and Java side-by-side, compare results  |
| VSAM-to-RDBMS data migration  | High   | Use CBEXPORT/CBIMPORT as migration bridge; validate row counts      |
| PCI DSS compliance gap         | High   | Implement tokenization before migrating card data                   |
| Plaintext passwords (CSUSR01Y) | High   | Hash all passwords during migration; enforce password policy        |
| CICS COMMAREA session state    | Medium | Map to HTTP session or JWT; test navigation flows end-to-end        |
| Batch window timing            | Medium | Benchmark Java batch against COBOL batch; optimize with parallelism |
| BMS screen fidelity            | Low    | Screen-by-screen UAT with business users                           |
