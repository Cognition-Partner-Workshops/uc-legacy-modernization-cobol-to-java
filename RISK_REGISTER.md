# CardDemo Risk Register

> **Generated**: 2026-03-25 | **Scope**: Modernization risks for CardDemo COBOL-to-Java migration

## Risk Scoring

Each risk is scored on two dimensions (1-5 scale):

| Score | Likelihood | Impact |
|---|---|---|
| 1 | Very unlikely | Negligible -- no user-visible effect |
| 2 | Unlikely | Minor -- workaround available |
| 3 | Possible | Moderate -- feature degradation or delay |
| 4 | Likely | Major -- data integrity or service outage |
| 5 | Very likely | Critical -- financial loss, regulatory, or data corruption |

**Risk Score** = Likelihood x Impact (1-25)

---

## Top Risks

### RISK-01: Decimal Precision Loss in Financial Calculations

| Attribute | Value |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **20** |
| **Affected Phases** | Phase 6 (Transaction Writes), Phase 7 (Account Mgmt), Phase 8 (Batch Pipeline) |
| **Affected Programs** | CBTRN02C, CBACT04C, COBIL00C, COACTUPC |

**Description:**
COBOL uses COMP-3 packed decimal arithmetic with implicit decimal points (e.g., `PIC S9(10)V99` = 12-digit signed number with 2 implicit decimal places). Java's `double`/`float` types introduce floating-point rounding errors. Even Java's `BigDecimal` can produce different results if scale and rounding mode are not configured identically to COBOL behavior.

Key risk areas:
- Interest calculation (CBACT04C): `PIC S9(04)V99` interest rates applied to `PIC S9(10)V99` balances
- Transaction posting (CBTRN02C): Category balance accumulation across thousands of transactions
- Bill payment (COBIL00C): Balance reduction must be exact to the penny

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M1.1 | Use `BigDecimal` with explicit `ROUND_HALF_EVEN` (banker's rounding) for all financial fields | Dev | Planned |
| M1.2 | Map every COMP-3/COMP field to `BigDecimal` with scale matching the PIC `V` position | Dev | Planned |
| M1.3 | Build a parallel-run comparison framework that compares COBOL and Java output for every financial field to the penny | QA | Planned |
| M1.4 | Run 30-day parallel batch cycle before cutover, flagging any balance discrepancy | QA | Planned |
| M1.5 | Create a "decimal precision test suite" using real VSAM data samples from `app/data/ASCII/` | QA | Planned |

**Residual Risk After Mitigation:** Low (score 4) -- parallel-run testing catches discrepancies before cutover.

---

### RISK-02: Batch Pipeline Sequencing Errors

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **15** |
| **Affected Phases** | Phase 8 (Batch Pipeline) |
| **Affected Programs** | CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN03C |

**Description:**
The nightly batch cycle is a strict 12-step sequence: CLOSEFIL → data refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE) → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL. Each step depends on the previous step's output. In JCL, step dependencies are implicit (job ordering + condition codes). In Spring Batch, these must be explicitly modeled.

Specific risks:
- CLOSEFIL/OPENFIL coordinate with CICS to prevent online access during batch -- this pattern disappears in a database-backed system
- COMBTRAN (SORT + IDCAMS) combines daily and system transactions -- SORT order must be exactly replicated
- TRANBKP creates a backup before combining -- must handle failure/restart correctly

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M2.1 | Model batch dependencies as a directed acyclic graph (DAG) in Spring Batch or AWS Step Functions | Dev | Planned |
| M2.2 | Replace CLOSEFIL/OPENFIL with database transaction isolation (SERIALIZABLE for batch, READ COMMITTED for online) | Dev | Planned |
| M2.3 | Implement step-level restart: each Spring Batch step is independently restartable from its last checkpoint | Dev | Planned |
| M2.4 | Pre-batch database snapshot for rollback (replacing TRANBKP VSAM copy) | Ops | Planned |
| M2.5 | End-to-end batch cycle test with real data volume (10K+ transactions) comparing step-by-step output | QA | Planned |

**Residual Risk After Mitigation:** Low (score 6) -- explicit DAG modeling and restart capability reduce operational risk.

---

### RISK-03: Data Synchronization Failures During Dual-Run

| Attribute | Value |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Score** | **16** |
| **Affected Phases** | Phases 3-8 (all dual-run periods) |
| **Affected Programs** | All programs during transition |

**Description:**
During the strangler fig transition, data must be synchronized between VSAM and PostgreSQL. Two-way sync introduces the risk of conflicts, stale reads, and data loss. Specific scenarios:
- Online user updates account via legacy COBOL while batch job runs on new Java system
- Card update via Java writes to PostgreSQL but VSAM sync fails silently
- Sync lag means Java reads stale data from PostgreSQL while VSAM has the latest
- EBCDIC ↔ UTF-8 encoding differences cause data corruption in sync

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M3.1 | Use **one-way sync only** (VSAM → PostgreSQL) until each module's write path cuts over to Java | Dev | Planned |
| M3.2 | Implement change data capture (CDC) with < 5-minute sync lag and monitoring alerts | Data | Planned |
| M3.3 | Add reconciliation job: nightly comparison of VSAM record counts and checksums vs. PostgreSQL | Data | Planned |
| M3.4 | During dual-write phases, use VSAM as source of truth; PostgreSQL is secondary until cutover | Dev | Planned |
| M3.5 | Test EBCDIC → ASCII → UTF-8 conversion pipeline with all special characters in sample data | QA | Planned |

**Residual Risk After Mitigation:** Medium (score 8) -- one-way sync eliminates most conflict scenarios but sync lag remains.

---

### RISK-04: PCI DSS Compliance Gap During Card Data Migration

| Attribute | Value |
|---|---|
| **Category** | Security & Compliance |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **15** |
| **Affected Phases** | Phase 3 (Card Reads), Phase 5 (Card Writes) |
| **Affected Programs** | COCRDLIC, COCRDSLC, COCRDUPC |

**Description:**
CardDemo stores credit card data in plaintext in VSAM (CVACT02Y: card number `PIC X(16)`, CVV `PIC 9(03)`, expiration `PIC X(10)`). The Java target system must implement PCI DSS-compliant encryption at rest and in transit. During migration:
- Card numbers transit the sync pipeline in plaintext
- Dual-write periods expose card data in two systems simultaneously
- API responses must mask card numbers and never expose CVV
- Database must use field-level encryption for PAN (Primary Account Number)

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M4.1 | Encrypt card data during sync pipeline (encrypt before writing to PostgreSQL) | Security | Planned |
| M4.2 | Implement field-level encryption for PAN and CVV in PostgreSQL (pgcrypto or application-level AES-256) | Dev | Planned |
| M4.3 | API response masking: return only last 4 digits of card number, never return CVV | Dev | Planned |
| M4.4 | TLS 1.3 for all API communication; mTLS between services | Platform | Planned |
| M4.5 | PCI DSS assessment of the new architecture before handling live card data | Compliance | Planned |
| M4.6 | Consider tokenization service (AWS Payment Cryptography or Vault) instead of direct encryption | Security | Planned |

**Residual Risk After Mitigation:** Low (score 5) -- standard PCI controls; requires compliance validation.

---

### RISK-05: COACTUPC Decomposition Introduces Behavioral Regression

| Attribute | Value |
|---|---|
| **Category** | Functional |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Score** | **16** |
| **Affected Phases** | Phase 7 (Account Management) |
| **Affected Programs** | COACTUPC (4,236 lines) |

**Description:**
COACTUPC is the largest program (4,236 lines) with 17 CICS commands and complex field-level validation. Decomposing it into multiple Java services (AccountController, AccountService, AccountValidator) risks:
- Missing validation rules buried deep in nested EVALUATE/IF statements
- Screen state transitions (pseudo-conversational CICS) not fully captured
- Field-level error messaging differences
- VSAM record locking semantics not replicated by optimistic locking
- Hidden business rules in PERFORM paragraph ordering

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M5.1 | Create a comprehensive validation rule catalog by analyzing every IF/EVALUATE in COACTUPC before writing Java code | Dev | Planned |
| M5.2 | Build a screen-by-screen test harness that compares COBOL BMS output with Java API responses for identical inputs | QA | Planned |
| M5.3 | Implement the decomposition incrementally: first extract validators, then separate read/write, then split by entity | Dev | Planned |
| M5.4 | 100% field-level test coverage for account update validation (valid and invalid inputs for every field) | QA | Planned |
| M5.5 | Retain COBOL COACTUPC on mainframe as fallback for 30 days post-cutover | Ops | Planned |

**Residual Risk After Mitigation:** Medium (score 8) -- comprehensive testing reduces but cannot eliminate risk for a 4,236-line program.

---

### RISK-06: Authentication Cutover Causes Service Disruption

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **10** |
| **Affected Phases** | Phase 1 (Identity & Access) |
| **Affected Programs** | COSGN00C |

**Description:**
Authentication is a single point of entry. If the new identity-service fails during cutover, all users are locked out of the entire application. Specific scenarios:
- JWT token issuance fails under load
- Password hash migration error (BCrypt hash doesn't match original password)
- CICS COMMAREA session state not properly replicated in JWT claims
- API Gateway routing misconfiguration sends auth requests to wrong service

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M6.1 | Test login for **every existing user** in USRSEC before cutover (not a sample -- all users) | QA | Planned |
| M6.2 | Implement API Gateway fallback: if identity-service returns 5xx, route to COBOL COSGN00C | Platform | Planned |
| M6.3 | Load test identity-service at 2x peak concurrent users before cutover | QA | Planned |
| M6.4 | Blue/green deployment: new service runs alongside old, traffic switch is instant and reversible | Platform | Planned |
| M6.5 | Keep COBOL COSGN00C running for 14 days post-cutover as hot standby | Ops | Planned |

**Residual Risk After Mitigation:** Very Low (score 2) -- comprehensive user testing + instant fallback.

---

### RISK-07: Reporting Output Mismatch

| Attribute | Value |
|---|---|
| **Category** | Functional |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9** |
| **Affected Phases** | Phase 4 (Reporting), Phase 8 (Statement Generation) |
| **Affected Programs** | CBTRN03C, CBSTM03A/B, CORPT00C |

**Description:**
COBOL report formatting uses fixed-width columns, page breaks at specific line counts, and control break totals (per-account, per-type, grand total). Modern reporting frameworks produce different visual output. Specific risks:
- CBSTM03A uses ALTER/GO TO for control flow -- difficult to replicate exactly
- Subtotal rounding differences accumulate across large reports
- Page break positions differ between fixed-width and PDF/HTML output
- SORT pre-processing order in JCL may differ from SQL ORDER BY collation

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M7.1 | Focus on **data accuracy** (amounts, totals) rather than pixel-perfect formatting | Dev | Planned |
| M7.2 | Compare report grand totals and account-level subtotals against COBOL output for 30-day batch run | QA | Planned |
| M7.3 | For CBSTM03A, create a detailed flowchart of ALTER/GO TO paths before attempting Java conversion | Dev | Planned |
| M7.4 | Verify SORT collation: compare JCL SORT output order with SQL ORDER BY for same dataset | QA | Planned |
| M7.5 | Accept formatting differences; get business sign-off on new report layout before cutover | PM | Planned |

**Residual Risk After Mitigation:** Low (score 3) -- data accuracy testing catches real issues; formatting differences are cosmetic.

---

### RISK-08: Loss of COBOL Expertise During Migration

| Attribute | Value |
|---|---|
| **Category** | Organizational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12** |
| **Affected Phases** | All phases |
| **Affected Programs** | All programs |

**Description:**
COBOL expertise is concentrated in a small number of engineers. If these experts leave or become unavailable during the migration, the team loses the ability to:
- Interpret complex COBOL patterns (ALTER/GO TO, REDEFINES, implicit decimal points)
- Validate that Java behavior matches COBOL behavior
- Debug discrepancies between old and new systems
- Understand undocumented business rules embedded in COBOL code

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M8.1 | Document all COBOL-specific patterns and business rules discovered during analysis (this analysis artifact set is a starting point) | Dev | In Progress |
| M8.2 | Record knowledge transfer sessions with COBOL experts on video for each functional area | PM | Planned |
| M8.3 | Build automated comparison tests that validate behavior without requiring COBOL expertise to run | QA | Planned |
| M8.4 | Retain COBOL expert(s) on consulting basis through Phase 8 completion | PM | Planned |
| M8.5 | Use AI-assisted COBOL analysis tools (e.g., AWS M2 analysis, COBOL-to-Java transpilers) as a second opinion | Dev | Planned |

**Residual Risk After Mitigation:** Medium (score 6) -- documentation and tests reduce dependency but cannot fully replace human expertise.

---

### RISK-09: Performance Degradation Under Load

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9** |
| **Affected Phases** | Phases 3-8 (all service deployments) |
| **Affected Programs** | COCRDLIC, COTRN00C (browse/pagination programs) |

**Description:**
CICS + VSAM is optimized for the specific access patterns of CardDemo (KSDS keyed reads, sequential browse). Java + PostgreSQL may perform differently:
- VSAM STARTBR/READNEXT is a cursor-based sequential read -- database OFFSET/LIMIT can be slower for deep pagination
- CICS pre-allocates resources per transaction -- Spring Boot thread pool may behave differently under load
- Microservice communication adds network latency (XCTL is in-process, REST is over-network)
- Batch processing with sequential file I/O may be faster than database row-level operations

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M9.1 | Use keyset pagination (WHERE id > last_seen_id) instead of OFFSET/LIMIT for browse operations | Dev | Planned |
| M9.2 | Load test each service at 2x expected peak before cutover | QA | Planned |
| M9.3 | Implement database indexing strategy matching VSAM key structures (primary key = KSDS key, alternate index = AIX) | Dev | Planned |
| M9.4 | Use connection pooling (HikariCP) and configure pool sizes based on CICS maxactive transaction count | Dev | Planned |
| M9.5 | For batch: use Spring Batch chunk processing with tuned chunk size; benchmark against COBOL batch timing | Dev | Planned |

**Residual Risk After Mitigation:** Low (score 3) -- standard performance engineering practices.

---

### RISK-10: Incomplete Cross-Reference Resolution During Migration

| Attribute | Value |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12** |
| **Affected Phases** | Phases 3-7 (all functional migrations) |
| **Affected Programs** | All programs using CARDXREF |

**Description:**
CARDXREF (CVACT03Y) links cards to accounts and customers. It is accessed by Account Management, Card Management, Transaction Management, Bill Payment, and Batch Processing. During migration, this cross-reference is the most shared data structure. Risks:
- Card-service takes ownership of CARDXREF in Phase 3, but Account and Transaction services need it too
- Stale cross-reference data causes card-to-account lookup failures
- New card issuance (if implemented) must update cross-reference atomically
- Batch programs read CARDXREF via alternate index (AIX) -- database index must replicate this access pattern

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M10.1 | Card-service exposes cross-reference via API: GET /cards/{number}/account, GET /accounts/{id}/cards | Dev | Planned |
| M10.2 | Other services call card-service API (not direct database access) for cross-reference lookups | Dev | Planned |
| M10.3 | Cache cross-reference data with short TTL (5 minutes) in consuming services to reduce API call volume | Dev | Planned |
| M10.4 | Database index on (card_number, account_id, customer_id) replicates VSAM KSDS + AIX access patterns | Dev | Planned |
| M10.5 | Reconciliation: nightly comparison of CARDXREF VSAM vs. PostgreSQL cross-reference table | Data | Planned |

**Residual Risk After Mitigation:** Low (score 4) -- API-based access + reconciliation prevents data drift.

---

## Risk Summary Matrix

| Risk ID | Description | Likelihood | Impact | Score | Phase(s) | Residual Score |
|---|---|---|---|---|---|---|
| **RISK-01** | Decimal precision loss in financial calculations | 4 | 5 | **20** | 6, 7, 8 | 4 |
| **RISK-03** | Data sync failures during dual-run | 4 | 4 | **16** | 3-8 | 8 |
| **RISK-05** | COACTUPC decomposition regression | 4 | 4 | **16** | 7 | 8 |
| **RISK-02** | Batch pipeline sequencing errors | 3 | 5 | **15** | 8 | 6 |
| **RISK-04** | PCI DSS compliance gap during card migration | 3 | 5 | **15** | 3, 5 | 5 |
| **RISK-08** | Loss of COBOL expertise | 3 | 4 | **12** | All | 6 |
| **RISK-10** | Incomplete cross-reference resolution | 3 | 4 | **12** | 3-7 | 4 |
| **RISK-06** | Authentication cutover disruption | 2 | 5 | **10** | 1 | 2 |
| **RISK-07** | Reporting output mismatch | 3 | 3 | **9** | 4, 8 | 3 |
| **RISK-09** | Performance degradation under load | 3 | 3 | **9** | 3-8 | 3 |

### Risk Heat Map

```
Impact
  5 │ RISK-06    RISK-02,04   RISK-01
    │                                  
  4 │            RISK-08,10   RISK-03,05
    │                                  
  3 │            RISK-07,09            
    │                                  
  2 │                                  
    │                                  
  1 │                                  
    └────────────────────────────────
      1         2         3         4         5
                    Likelihood
```

---

## Risk Monitoring

### Key Risk Indicators (KRIs)

| KRI | Threshold | Monitoring Frequency | Action If Exceeded |
|---|---|---|---|
| Parallel-run balance discrepancy count | 0 per day | Daily during dual-run | Halt cutover, investigate |
| Data sync lag (VSAM → PostgreSQL) | < 5 minutes | Continuous | Alert team, check CDC pipeline |
| API Gateway error rate | < 0.1% | Continuous | Route traffic to fallback |
| Batch cycle completion time | < 120% of COBOL baseline | Nightly | Tune chunk sizes, review queries |
| Cross-reference record count mismatch | 0 | Nightly | Reconcile, investigate source |
| PCI scan findings (critical/high) | 0 | Weekly | Block card data migration |
| COBOL expert availability | >= 1 FTE | Weekly | Engage consulting backup |

### Escalation Path

| Severity | Response Time | Escalation |
|---|---|---|
| Critical (score >= 15) | 1 hour | Project lead + architecture team |
| Major (score 10-14) | 4 hours | Technical lead |
| Moderate (score 5-9) | 1 business day | Development team |
| Low (score 1-4) | Next sprint planning | Product backlog |
