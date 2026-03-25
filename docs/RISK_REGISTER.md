# CardDemo Risk Register

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Catalog the top migration risks with likelihood, impact, and mitigations.

---

## Table of Contents

1. [Risk Scoring Framework](#1-risk-scoring-framework)
2. [Top Risks — Ranked](#2-top-risks--ranked)
3. [Detailed Risk Profiles](#3-detailed-risk-profiles)
4. [Risk Heat Map](#4-risk-heat-map)
5. [Monitoring & Early Warning Indicators](#5-monitoring--early-warning-indicators)

---

## 1. Risk Scoring Framework

| Dimension       | 1 (Low)                | 2 (Medium)                  | 3 (High)                         |
|----------------|------------------------|-----------------------------|----------------------------------|
| **Likelihood** | Unlikely (<20%)        | Possible (20–60%)           | Likely (>60%)                    |
| **Impact**     | Minor delay, workaround available | Significant rework, 2–4 week delay | Project failure, data loss, regulatory breach |

**Risk Score** = Likelihood × Impact (max = 9)

---

## 2. Top Risks — Ranked

| Rank | ID    | Risk                                        | Likelihood | Impact | **Score** | Phase |
|------|-------|---------------------------------------------|------------|--------|-----------|-------|
| 1    | R-001 | Financial calculation precision divergence   | 3          | 3      | **9**     | 4     |
| 2    | R-002 | VSAM-to-DB data migration integrity loss     | 2          | 3      | **6**     | 2     |
| 3    | R-003 | Batch cycle atomicity gap                    | 3          | 2      | **6**     | 4     |
| 4    | R-004 | Undocumented business rules in COACTUPC      | 3          | 2      | **6**     | 3     |
| 5    | R-005 | Dual-write data divergence during coexistence| 2          | 3      | **6**     | 3     |
| 6    | R-006 | Security regression (auth bypass, PII leak)  | 2          | 3      | **6**     | 1     |
| 7    | R-007 | COBOL SME knowledge attrition               | 2          | 2      | **4**     | All   |
| 8    | R-008 | EBCDIC/ASCII encoding mismatches             | 2          | 2      | **4**     | 2     |
| 9    | R-009 | Performance degradation (batch window overrun)| 2          | 2      | **4**     | 4     |
| 10   | R-010 | PCI-DSS compliance gap in card data handling | 1          | 3      | **3**     | 3     |
| 11   | R-011 | Date format misinterpretation                | 2          | 1      | **2**     | 2–3   |
| 12   | R-012 | CICS pseudo-conversational state loss        | 2          | 1      | **2**     | 3     |

---

## 3. Detailed Risk Profiles

### R-001 — Financial Calculation Precision Divergence

**Risk Score: 9 (Critical)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | Interest calculations (CBACT04C) and balance updates (CBTRN02C) use COBOL `COMPUTE ROUNDED` with `PIC S9(10)V99` (2 decimal places). Java `BigDecimal` rounding modes may not produce identical results for all edge cases. |
| **Root Cause**  | COBOL `COMPUTE ROUNDED` uses "round half away from zero" by default. Java's closest equivalent is `RoundingMode.HALF_UP`, but COBOL's intermediate computation precision differs from Java's arbitrary-precision `BigDecimal`. |
| **Affected**    | CBACT04C (interest calc), CBTRN02C (balance update), COBIL00C (payment processing)        |
| **Impact**      | 1-cent discrepancies across thousands of accounts → regulatory audit findings, customer complaints, SOX compliance risk |
| **Likelihood**  | **High** — precision divergence is the #1 cause of batch migration failures in financial systems |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Build golden-file test suite with 10,000+ known-good input/output pairs from production batch runs | QA | Pending |
| 2 | Implement differential testing: run legacy + modern in parallel, compare every output field | QA | Pending |
| 3 | Use `BigDecimal` with explicit scale (2) and `RoundingMode.HALF_UP` everywhere | Dev | Pending |
| 4 | Document every COMPUTE statement in CBACT04C and CBTRN02C with expected rounding behavior | BA | Pending |
| 5 | Regulatory review of modernized interest calculations before Phase 4 go-live | Compliance | Pending |

---

### R-002 — VSAM-to-DB Data Migration Integrity Loss

**Risk Score: 6 (High)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | Migrating 12 VSAM KSDS files to PostgreSQL tables risks data loss or corruption due to encoding, field mapping, or referential integrity differences. VSAM has no referential integrity enforcement — the database will. |
| **Root Cause**  | VSAM stores fixed-length records with no schema enforcement. Some records may have invalid data (e.g., blank account IDs, orphaned cross-references) that pass silently in VSAM but violate database constraints. |
| **Affected**    | All 12 core VSAM datasets, especially ACCTDATA, TRANSACT, CARDXREF                        |
| **Impact**      | Records rejected by database constraints → missing accounts or transactions in modern system |
| **Likelihood**  | **Medium** — common in mainframe migrations but detectable with pre-migration data profiling |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Profile all VSAM data before migration: identify nulls, orphans, encoding issues | DBA | Pending |
| 2 | Create data cleansing rules for invalid records (quarantine, not discard) | BA/DBA | Pending |
| 3 | Run record-count reconciliation after every migration batch: VSAM count = DB count | DBA | Pending |
| 4 | Implement field-level checksum verification on random 10% sample | QA | Pending |
| 5 | Keep VSAM files available for 90 days post-migration for recovery | Ops | Pending |

---

### R-003 — Batch Cycle Atomicity Gap

**Risk Score: 6 (High)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | The legacy nightly batch cycle (CLOSEFIL → POSTTRAN → INTCALC → ... → OPENFIL) achieves atomicity through VSAM file locking (CLOSEFIL prevents online access). The database does not use this pattern — Spring Batch jobs must handle concurrent online access. |
| **Root Cause**  | VSAM CLOSEFIL/OPENFIL is a coarse-grained lock that prevents all online I/O during batch. The database alternative (row-level locking, snapshot isolation) is fundamentally different and may produce different outcomes under concurrency. |
| **Affected**    | CBTRN02C (posting), CBACT04C (interest), entire nightly cycle                              |
| **Impact**      | Dirty reads during batch → incorrect balances, double-posting, inconsistent statements     |
| **Likelihood**  | **High** — concurrency issues are subtle and often only manifest under production load      |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Use `SERIALIZABLE` transaction isolation for critical batch steps (posting, interest) | Dev | Pending |
| 2 | Implement a maintenance window flag that blocks online writes during batch (soft equivalent of CLOSEFIL) | Dev | Pending |
| 3 | Load test: simulate concurrent online + batch writes and verify data consistency | QA | Pending |
| 4 | Design batch jobs to be idempotent — safe to re-run if interrupted | Dev | Pending |
| 5 | Add post-batch reconciliation step that verifies balance consistency | QA | Pending |

---

### R-004 — Undocumented Business Rules in COACTUPC

**Risk Score: 6 (High)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | COACTUPC (4,236 LOC) contains extensive field validation and business logic embedded in deeply nested IF/EVALUATE structures. The 39× COPY CSSETATY REPLACING pattern encodes field-level attribute management rules that are not documented anywhere outside the code. |
| **Root Cause**  | Business rules were encoded directly into COBOL code over years of maintenance without corresponding documentation. The COPY REPLACING pattern obscures the actual validation logic behind macro expansion. |
| **Affected**    | COACTUPC → AccountUpdateService (modernized)                                               |
| **Impact**      | Missed business rules → validation gaps in modern system → invalid data accepted or valid updates rejected |
| **Likelihood**  | **High** — 4,236 LOC virtually guarantees some rules will be missed in manual conversion    |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Expand all COPY REPLACING macros to produce a fully resolved source listing | Dev | Pending |
| 2 | Document every EVALUATE/IF branch with business rule description | BA/COBOL SME | Pending |
| 3 | Build a validation test suite: 500+ test cases covering every field combination | QA | Pending |
| 4 | Pair COBOL SME with Java developer during COACTUPC conversion | PM | Pending |
| 5 | Conduct code review of modernized validation with business stakeholders | BA | Pending |

---

### R-005 — Dual-Write Data Divergence During Coexistence

**Risk Score: 6 (High)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | During Phase 3, the system switches from VSAM-primary to DB-primary. The bidirectional sync between VSAM and PostgreSQL may experience lag, conflicts, or missed updates, causing the two systems to diverge. |
| **Root Cause**  | VSAM file updates are immediate (no transaction log). Syncing to/from PostgreSQL requires CDC (Change Data Capture) or periodic batch sync, both of which introduce latency windows where data differs. |
| **Affected**    | ACCTDATA, TRANSACT, CARDDATA — all files with online write operations                      |
| **Impact**      | Users see different data depending on which system serves their request; financial records temporarily inconsistent |
| **Likelihood**  | **Medium** — well-understood problem but requires careful engineering of the sync mechanism  |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Implement near-real-time CDC (< 1 second lag) using database triggers or WAL streaming | Dev | Pending |
| 2 | Run continuous reconciliation checks during coexistence (every 5 minutes) | Ops | Pending |
| 3 | Implement conflict resolution: DB wins, log VSAM-side changes for manual review | Dev | Pending |
| 4 | Minimize coexistence duration — aim for 2 weeks max per service cutover | PM | Pending |
| 5 | Alert on reconciliation drift > 10 records; halt traffic routing on drift > 100 | Ops | Pending |

---

### R-006 — Security Regression (Auth Bypass, PII Leak)

**Risk Score: 6 (High)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | The authentication rewrite (COSGN00C → Spring Security) and PII handling (CVCUS01Y has SSN, DOB, FICO; CVACT02Y has card numbers) must be implemented with zero security regressions. The legacy system uses plaintext passwords and no encryption — the modern system must be strictly more secure. |
| **Root Cause**  | Security is being rewritten, not converted. New code = new attack surface. PII data that was "secure by obscurity" on mainframe will be accessible via REST APIs. |
| **Affected**    | COSGN00C (auth), CVCUS01Y (PII), CVACT02Y (card data), all API endpoints                  |
| **Impact**      | Data breach, regulatory fines (PCI-DSS, CCPA/GDPR), reputational damage                   |
| **Likelihood**  | **Medium** — Spring Security is mature, but misconfiguration is common                      |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Use Spring Security starter with OAuth2/OIDC — don't build custom auth | Dev | Pending |
| 2 | Encrypt PII at rest (AES-256) and in transit (TLS 1.3) | Dev | Pending |
| 3 | Implement field-level masking for SSN, card numbers in API responses | Dev | Pending |
| 4 | Penetration test after Phase 1 and Phase 3 completion | Security | Pending |
| 5 | PCI-DSS compliance scan for card data handling endpoints | Security | Pending |
| 6 | bcrypt password hashing with cost factor ≥ 12 for user migration | Dev | Pending |

---

### R-007 — COBOL SME Knowledge Attrition

**Risk Score: 4 (Medium)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | The migration requires deep COBOL expertise to interpret business rules, validate conversions, and resolve discrepancies. COBOL developers are retiring, and finding replacements is increasingly difficult. |
| **Root Cause**  | Industry-wide COBOL skills shortage. Business rules are encoded in code, not documentation. |
| **Affected**    | All phases, especially Phases 3–4 where complex business logic is converted                |
| **Impact**      | Conversion errors due to misunderstood business rules; delays waiting for SME availability  |
| **Likelihood**  | **Medium** — manageable if SME is engaged early and knowledge is captured                   |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Engage COBOL SME in Phase 1 and capture all business rules in documentation | PM | Pending |
| 2 | Record video walkthroughs of complex programs (COACTUPC, CBTRN02C, CBACT04C) | COBOL SME | Pending |
| 3 | Build comprehensive golden-file test suite as "executable documentation" | QA | Pending |
| 4 | Cross-train 2 Java developers on COBOL reading skills in first 2 weeks | PM | Pending |
| 5 | Use automated COBOL analysis tools for control flow and data flow extraction | Dev | Pending |

---

### R-008 — EBCDIC/ASCII Encoding Mismatches

**Risk Score: 4 (Medium)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | COBOL on mainframe uses EBCDIC encoding. VSAM COMP (binary) and COMP-3 (packed decimal) fields require specific decoding. Misinterpretation during data migration produces corrupted numeric or text values. |
| **Root Cause**  | The CardDemo sample data in `app/data/ASCII/` is pre-converted to ASCII, but production mainframe data would be EBCDIC. COMP fields store data in binary format that doesn't translate to ASCII directly. |
| **Affected**    | Data migration (Phase 2), especially numeric fields (ACCT-CURR-BAL, TRAN-AMT)             |
| **Impact**      | Corrupted balances, wrong transaction amounts, garbled customer names                       |
| **Likelihood**  | **Medium** — well-known problem with established tooling, but still easy to miss edge cases |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Use established COBOL-to-Java data conversion libraries (e.g., JRecord, cb2xml) | Dev | Pending |
| 2 | Build copybook-aware parser that respects PIC clause definitions for field extraction | Dev | Pending |
| 3 | Verify converted data against known good values for every field type | QA | Pending |
| 4 | Handle COMP, COMP-3, DISPLAY numeric separately with specific decoding logic | Dev | Pending |
| 5 | Test with production-like EBCDIC data, not just the ASCII samples | QA | Pending |

---

### R-009 — Performance Degradation (Batch Window Overrun)

**Risk Score: 4 (Medium)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | The legacy batch cycle completes within a defined nightly window (e.g., 2 hours). Spring Batch on a cloud database may be slower due to network latency, ORM overhead, and lack of mainframe I/O optimization. |
| **Root Cause**  | Mainframe VSAM is optimized for sequential batch processing. PostgreSQL requires different optimization (batch inserts, connection pooling, index management). |
| **Affected**    | Phase 4 — entire nightly batch cycle                                                       |
| **Impact**      | Batch jobs overrun into online hours → delayed account updates, missed SLAs                 |
| **Likelihood**  | **Medium** — depends on data volume and optimization effort                                 |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Benchmark batch jobs with production-scale data volumes before Phase 4 | QA | Pending |
| 2 | Use Spring Batch chunk-oriented processing with optimal chunk sizes | Dev | Pending |
| 3 | Disable/defer non-essential database indexes during batch, rebuild after | DBA | Pending |
| 4 | Use JDBC batch inserts (not JPA `save()` loops) for high-volume writes | Dev | Pending |
| 5 | Provision dedicated database connection pool for batch jobs (separate from online) | DBA | Pending |

---

### R-010 — PCI-DSS Compliance Gap in Card Data Handling

**Risk Score: 3 (Medium)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | Card numbers (PAN), CVV codes, and expiration dates in CVACT02Y are stored in plaintext in VSAM. The modern system must implement PCI-DSS Level 1 controls: tokenization, encryption, access logging, and network segmentation. |
| **Root Cause**  | Legacy mainframe was considered a "secure perimeter" — data at rest was not encrypted. Moving to cloud/distributed architecture requires explicit encryption. |
| **Affected**    | COCRDUPC, COCRDSLC, COCRDLIC, CARDDATA table                                              |
| **Impact**      | PCI-DSS non-compliance → fines up to $500K/month, loss of card processing capability       |
| **Likelihood**  | **Low** — if PCI requirements are addressed from the start (Phase 3 design)                |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Implement card number tokenization using a PCI-certified vault (e.g., Basis Theory, VGS) | Dev | Pending |
| 2 | Store CVV in encrypted column with application-level decryption only when needed | Dev | Pending |
| 3 | Implement column-level encryption for PAN in `cards` table | DBA | Pending |
| 4 | Mask card numbers in all API responses (show last 4 digits only) | Dev | Pending |
| 5 | Engage PCI QSA (Qualified Security Assessor) before Phase 3 go-live | Security | Pending |

---

### R-011 — Date Format Misinterpretation

**Risk Score: 2 (Low)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | COBOL uses multiple date formats: `YYYYMMDD` in batch PARMs, `YYYY-MM-DD` in VSAM records, and Lilian dates via CEEDAYS. Java `java.time.LocalDate` parsing must match exactly. |
| **Root Cause**  | Date utility CSUTLDTC calls CEEDAYS (IBM Language Environment) for date validation. Java's `java.time` API handles dates differently — especially for invalid dates and leap year edge cases. |
| **Affected**    | COTRN02C (transaction date validation), CBACT04C (processing date), COACTUPC (date fields) |
| **Impact**      | Transactions assigned wrong dates, interest calculated for wrong periods                    |
| **Likelihood**  | **Medium** — date handling is a common migration pitfall but well-testable                  |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Map every date field and its format across all copybooks | BA | Pending |
| 2 | Build date conversion test suite with edge cases (leap years, month boundaries, century rollover) | QA | Pending |
| 3 | Replace CSUTLDTC/CEEDAYS with `java.time.LocalDate` + `DateTimeFormatter` with strict parsing | Dev | Pending |

---

### R-012 — CICS Pseudo-Conversational State Loss

**Risk Score: 2 (Low)**

| Attribute       | Detail                                                                                     |
|----------------|--------------------------------------------------------------------------------------------|
| **Description** | CICS pseudo-conversational programming stores state in COMMAREA between interactions. The modern REST/web architecture is stateless. Complex multi-step operations (COACTUPC edit flow) may lose state if the session-to-stateless mapping is incorrect. |
| **Root Cause**  | COMMAREA carries 11+ fields of state (from-program, to-program, user-id, acct-id, card-num, etc.) that must be mapped to either JWT claims, session storage, or client-side state. |
| **Affected**    | COACTUPC, COCRDUPC, COCRDLIC (browse position), all multi-screen workflows                 |
| **Impact**      | Users lose context mid-operation → forced to restart, poor UX                               |
| **Likelihood**  | **Medium** — straightforward to solve with proper web state management                      |

**Mitigations:**

| # | Mitigation                                                     | Owner    | Status  |
|---|----------------------------------------------------------------|----------|---------|
| 1 | Map each COMMAREA field to its modern equivalent (JWT claim, query param, or eliminated) | Dev | Pending |
| 2 | Use client-side state (React/Angular state management) for multi-step workflows | Frontend | Pending |
| 3 | Test full user journeys end-to-end: login → navigate → update → verify | QA | Pending |

---

## 4. Risk Heat Map

```
                          Impact →
                   Low (1)      Medium (2)      High (3)
              ┌────────────┬──────────────┬──────────────┐
  High (3)    │            │ R-003 Batch  │ R-001 Fin.   │
  Likelihood  │            │   Atomicity  │   Precision  │
              │            │ R-004 Undoc. │              │
              │            │   Rules      │              │
              ├────────────┼──────────────┼──────────────┤
  Medium (2)  │ R-011 Date │ R-007 SME    │ R-002 Data   │
              │ R-012 State│ R-008 EBCDIC │   Migration  │
              │            │ R-009 Perf.  │ R-005 Dual-  │
              │            │              │   Write      │
              │            │              │ R-006 Sec.   │
              ├────────────┼──────────────┼──────────────┤
  Low (1)     │            │              │ R-010 PCI    │
              │            │              │              │
              └────────────┴──────────────┴──────────────┘
```

**Action zones:**
- **Top-right (score 6–9):** Active mitigation required — dedicated owner, weekly tracking
- **Middle band (score 3–4):** Monitor and mitigate — review bi-weekly
- **Bottom-left (score 1–2):** Accept — standard engineering practices sufficient

---

## 5. Monitoring & Early Warning Indicators

| Indicator                                 | Threshold                     | Risk Triggered       | Action                          |
|------------------------------------------|-------------------------------|----------------------|----------------------------------|
| Interest calc diff (legacy vs modern)    | Any non-zero difference       | R-001                | Halt Phase 4, investigate        |
| VSAM-DB record count mismatch           | > 0 records                   | R-002                | Pause migration, reconcile       |
| Batch job duration increase              | > 50% above legacy baseline   | R-009                | Optimize before cutover          |
| Dual-write reconciliation drift          | > 10 records different        | R-005                | Alert; > 100 = halt traffic      |
| Authentication failure rate              | > 1%                          | R-006                | Rollback to legacy auth          |
| COBOL SME availability                   | < 10 hours/week               | R-007                | Escalate to project sponsor      |
| Data migration field checksum failures   | > 0.1% of records             | R-008                | Pause migration, fix decoder     |
| PCI scan findings                        | Any critical/high             | R-010                | Block Phase 3 go-live            |
| Validation rule parity test failures     | Any failure                   | R-004                | Pause COACTUPC cutover           |
