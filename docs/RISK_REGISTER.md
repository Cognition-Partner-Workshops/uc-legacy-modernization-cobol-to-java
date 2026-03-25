# CardDemo Risk Register

> **Purpose:** Identify, classify, and mitigate the top risks to the CardDemo COBOL-to-Java modernization
> **Methodology:** Each risk scored on Likelihood (1-5) x Impact (1-5) = Risk Score (1-25)
> **Review cadence:** Reassess monthly during migration; weekly during Phase 3 (high-risk financial modules)

---

## Risk Scoring Key

| Score | Likelihood | Impact |
|:-----:|------------|--------|
| 1 | Rare | Negligible |
| 2 | Unlikely | Minor |
| 3 | Possible | Moderate |
| 4 | Likely | Major |
| 5 | Almost certain | Critical |

| Risk Score | Rating | Action |
|:----------:|--------|--------|
| 1-5 | **Low** | Accept / monitor |
| 6-12 | **Medium** | Mitigate proactively |
| 13-19 | **High** | Active mitigation required; escalation plan |
| 20-25 | **Critical** | Blocker; must resolve before proceeding |

---

## Top Risks

### RISK-01: Decimal Arithmetic Divergence

| Attribute | Value |
|-----------|-------|
| **Category** | Technical -- Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **20 -- Critical** |
| **Affected Phase** | Phase 3 (Batch Posting, Interest Calculation, Bill Payment) |
| **Affected Programs** | CBTRN02C, CBACT04C, COBIL00C, COTRN02C |

**Description:**
COBOL uses fixed-point decimal arithmetic with `PIC S9(10)V99 COMP-3` and `COMPUTE ... ROUNDED`. Java's `double`/`float` types introduce floating-point errors. Even with `BigDecimal`, differences in intermediate rounding, truncation order, and division scale can produce penny discrepancies that compound across millions of transactions.

**Specific Exposure:**
- CBTRN02C: Balance update after posting (`ACCT-CURR-BAL = ACCT-CURR-BAL + TRAN-AMT`)
- CBACT04C: Interest = `BALANCE * RATE / 12 ROUNDED` -- division scale matters
- COBIL00C: Payment deduction -- `ACCT-CURR-BAL = ACCT-CURR-BAL - PAYMENT-AMT`
- Category balance accumulation in TCATBALF

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Use `BigDecimal` exclusively for all financial fields; ban `double`/`float` in financial domain | Dev lead | Pending |
| 2 | Create arithmetic parity test suite: 500+ test cases covering addition, subtraction, multiplication, division with known COBOL outputs | QA | Pending |
| 3 | Run parallel batch cycles (COBOL + Java) for 4+ weeks; auto-diff every output field | DevOps | Pending |
| 4 | Document exact `RoundingMode` for each COMPUTE statement in COBOL (map `ROUNDED` to `HALF_UP`) | Analyst | Pending |
| 5 | Implement `ArithmeticPolicy` class that centralizes all rounding rules; unit test against COBOL reference outputs | Dev | Pending |

**Residual Risk After Mitigation:** Low (3) -- with comprehensive parallel-run validation, residual risk is limited to edge cases not covered by test data.

---

### RISK-02: ACCTFILE Write Contention Across Bounded Contexts

| Attribute | Value |
|-----------|-------|
| **Category** | Architecture -- Data Ownership |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Score** | **16 -- High** |
| **Affected Phase** | Phase 2-3 (Account Management, Transaction Processing) |
| **Affected Programs** | COACTUPC, COBIL00C, CBTRN02C, CBACT04C |

**Description:**
ACCTFILE (Account Master) is written by 4 programs across 2 bounded contexts (Account Management and Transaction Processing). In the target architecture, both `account-service` and `transaction-service` need to update account balances. Without a clear ownership model, this creates race conditions, deadlocks, or split-brain inconsistencies.

**Specific Exposure:**
- Admin updates credit limit (COACTUPC) while batch posting updates balance (CBTRN02C) = potential lost update
- Bill payment (COBIL00C) updates balance while interest calculation (CBACT04C) reads balance = dirty read risk
- During strangler transition: Java writes to database while COBOL writes to VSAM = dual-write inconsistency

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Designate `account-service` as sole owner of `accounts` table; all balance updates go through Account API | Architect | Pending |
| 2 | Implement `PUT /accounts/{id}/balance` with optimistic locking (`@Version`) | Dev | Pending |
| 3 | During Phase 2-3 transition, use database as single source of truth (disable VSAM writes before enabling Java writes) | DevOps | Pending |
| 4 | Add row-level locking (`SELECT FOR UPDATE`) for balance operations | Dev | Pending |
| 5 | Create integration test: concurrent balance update from payment + posting + interest = correct final balance | QA | Pending |

**Residual Risk After Mitigation:** Low (4) -- with clear ownership and database transactions, contention is manageable.

---

### RISK-03: Loss of Business Rules During COACTUPC Decomposition

| Attribute | Value |
|-----------|-------|
| **Category** | Technical -- Knowledge Loss |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **15 -- High** |
| **Affected Phase** | Phase 2 (Account Management) |
| **Affected Programs** | COACTUPC (4,236 LOC) |

**Description:**
COACTUPC is the largest program (20% of total LOC) containing 800+ phone area code validations, 50+ state code validations, 100+ state-zip cross-validations, date validations, credit limit rules, and FICO score handling. Decomposing this monolith into multiple Java services risks omitting or misinterpreting embedded business rules, especially those expressed as multi-level nested IF/EVALUATE statements.

**Specific Exposure:**
- CSLKPCDY copybook: massive lookup table embedded in COBOL working storage -- easy to miss entries
- Cross-field validation: e.g., if state = 'NY' then zip must start with '1' -- logic buried in nested conditions
- COACTUPC has distinct validation paths for account fields vs. customer fields vs. card fields

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Extract all validation rules from COACTUPC and CSLKPCDY into a requirements document before writing any Java code | Analyst | Pending |
| 2 | Create exhaustive validation test suite (one test per validation rule) with COBOL-verified expected outcomes | QA | Pending |
| 3 | Externalize lookup tables (area codes, state codes, zip ranges) into database reference tables rather than hardcoding | Dev | Pending |
| 4 | Peer review decomposition design with mainframe SME before implementation | Architect + SME | Pending |
| 5 | Implement feature flags: run new validation in parallel with legacy validation during transition; log discrepancies | Dev | Pending |

**Residual Risk After Mitigation:** Medium (6) -- some edge-case rules may be discovered post-migration via production incidents.

---

### RISK-04: Batch Cycle Temporal Coupling Violation

| Attribute | Value |
|-----------|-------|
| **Category** | Architecture -- Orchestration |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12 -- Medium** |
| **Affected Phase** | Phase 3 (Batch Processing) |
| **Affected JCL** | CLOSEFIL → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL |

**Description:**
The mainframe batch cycle enforces strict sequential ordering via JCL job dependencies and file-level locking (CLOSEFIL/OPENFIL). In the Java target, with RDBMS row-level locking, jobs can theoretically run concurrently. However, business logic still requires ordering: interest must be calculated after posting, and statements must include all posted transactions. If batch job orchestration is not implemented correctly, financial outputs will be incorrect.

**Specific Exposure:**
- Interest calculated on stale data if posting hasn't completed
- Statement generated before posting = missing transactions
- Backup taken mid-posting = inconsistent snapshot

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Implement Spring Batch job orchestration with explicit step dependencies (posting → interest → statements) | Dev | Pending |
| 2 | Use database read-committed or serializable isolation for batch jobs to prevent dirty reads | Dev | Pending |
| 3 | Add completion markers (batch_runs table with status) to enforce ordering | Dev | Pending |
| 4 | Eliminate CLOSEFIL/OPENFIL pattern; replace with DB transaction isolation | Architect | Pending |
| 5 | Create end-to-end batch cycle integration test: seed data → post → calc interest → generate statement → verify all outputs | QA | Pending |

**Residual Risk After Mitigation:** Low (4) -- database transactions naturally enforce consistency.

---

### RISK-05: CICS Pseudo-Conversational State Loss

| Attribute | Value |
|-----------|-------|
| **Category** | Technical -- Architecture |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9 -- Medium** |
| **Affected Phase** | Phase 1-3 (All online programs) |
| **Affected Programs** | All 17 CO* online CICS programs |

**Description:**
CICS programs use pseudo-conversational design: the program exits between user interactions, passing state via COMMAREA (up to 32KB). The next interaction re-invokes the program with the saved COMMAREA. Translating this to REST APIs requires careful state management -- using JWT tokens, server-side sessions, or request context. Incorrect translation can cause state loss, resulting in data displayed on one screen not matching what gets saved on the next.

**Specific Exposure:**
- COACTUPC/COCRDUPC: display account/card → user edits → submit → program compares original (from COMMAREA) vs new values
- If the "original" state is lost between REST calls, the compare-and-write pattern breaks
- COTRN02C: "copy last transaction" feature relies on COMMAREA retaining previous transaction data

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Map COMMAREA fields to DTOs; pass as request/response bodies (not hidden state) | Dev | Pending |
| 2 | For edit-compare-save pattern: read record at save time and compare with submitted original values (optimistic locking) | Dev | Pending |
| 3 | Use JPA `@Version` for optimistic concurrency instead of manual field comparison | Dev | Pending |
| 4 | Document every COMMAREA field and its lifecycle for each program's pseudo-conversational flow | Analyst | Pending |
| 5 | Test multi-step workflows end-to-end: display → edit → confirm → save, including concurrent user scenarios | QA | Pending |

**Residual Risk After Mitigation:** Low (3) -- REST + JPA `@Version` is a well-established pattern.

---

### RISK-06: Plaintext Password Exposure During Migration

| Attribute | Value |
|-----------|-------|
| **Category** | Security |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **15 -- High** |
| **Affected Phase** | Phase 1 (IAM Migration) |
| **Affected Programs** | COSGN00C, CSUSR01Y copybook, USRSEC VSAM |

**Description:**
The current system stores passwords in plaintext in the USRSEC VSAM file (`SEC-USR-PWD PIC X(08)`). During migration, these passwords must be extracted, hashed, and loaded into the new database. If the migration process exposes plaintext passwords (in logs, intermediate files, or error messages), or if the hashing is incorrectly implemented, user credentials are compromised.

**Specific Exposure:**
- CBEXPORT may export USRSEC with plaintext passwords
- ETL pipeline may log password fields
- If BCrypt salt is not generated per-user, rainbow table attacks are possible
- 8-character max password length in COBOL means weak passwords

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Hash passwords during ETL (never store plaintext in target database) | Dev | Pending |
| 2 | Use BCrypt with per-user salt (Spring Security's `BCryptPasswordEncoder`) | Dev | Pending |
| 3 | Mask password fields in all logs, error messages, and monitoring | DevOps | Pending |
| 4 | Force password reset for all users after migration (eliminate weak 8-char passwords) | Product | Pending |
| 5 | Delete USRSEC export files immediately after successful migration; verify deletion | DevOps | Pending |
| 6 | Implement password complexity rules in new system (min 12 chars, complexity requirements) | Dev | Pending |

**Residual Risk After Mitigation:** Low (3) -- standard security practices eliminate exposure.

---

### RISK-07: Data Synchronization During Strangler Transition

| Attribute | Value |
|-----------|-------|
| **Category** | Architecture -- Data Consistency |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Score** | **16 -- High** |
| **Affected Phase** | Phase 2-3 (during parallel operation) |
| **Affected Files** | All VSAM files during dual-write period |

**Description:**
During the strangler fig transition, some operations run on Java (new) while others still run on COBOL (legacy). If both systems write to their respective data stores (database and VSAM), synchronization is required. Any failure in synchronization causes data divergence, which compounds over time and becomes increasingly difficult to reconcile.

**Specific Exposure:**
- Card updated in Java → VSAM not updated → COBOL batch reads stale card data
- Transaction posted via COBOL batch → database not updated → Java shows stale transaction list
- Customer updated in Java → COBOL statement generation uses stale customer name/address

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Minimize dual-write window: cut over entire functional areas at once (not individual operations) | Architect | Pending |
| 2 | Database is source of truth during transition; VSAM updated via sync job (not dual-write) | Dev | Pending |
| 3 | Implement daily automated reconciliation between database and VSAM (see CUTOVER_PLAN.md §7) | DevOps | Pending |
| 4 | Use change-data-capture (CDC) from database to sync VSAM if dual-write unavoidable | Dev | Pending |
| 5 | Define rollback checkpoints: daily snapshots of both VSAM and database that can be restored as a pair | DevOps | Pending |

**Residual Risk After Mitigation:** Medium (8) -- dual-write periods inherently carry consistency risk; minimize duration.

---

### RISK-08: BMS-to-Web UI Field Mapping Errors

| Attribute | Value |
|-----------|-------|
| **Category** | Technical -- UI/UX |
| **Likelihood** | 3 (Possible) |
| **Impact** | 2 (Minor) |
| **Risk Score** | **6 -- Medium** |
| **Affected Phase** | Phase 1-3 (all online modules) |
| **Affected Artifacts** | 17 BMS maps → HTML/React forms |

**Description:**
BMS maps define 3270 terminal screens with field-level attributes (protected/unprotected, bright/dark, numeric/alphanumeric). Translating these to web forms risks losing field constraints (e.g., numeric-only fields accepting text), display formatting (field lengths, cursor positioning), and accessibility features. While not a data integrity risk, it degrades user experience and may cause user errors.

**Specific Exposure:**
- COACTUP.bms has 30+ fields with specific attribute bytes -- each must map to HTML input attributes
- COCRDLI.bms has a 7-row repeating group -- must map to a data table with selection
- COSGN00.bms has a password field with dark attribute -- must map to `<input type="password">`

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Create field mapping spreadsheet: BMS field → HTML input type + attributes + validation rules | Analyst | Pending |
| 2 | Implement client-side validation matching BMS field constraints (maxlength, pattern, required) | Dev | Pending |
| 3 | User acceptance testing (UAT) with mainframe users for each screen | QA + Users | Pending |
| 4 | Retain 3270 screen captures as reference during UI development | Analyst | Pending |

**Residual Risk After Mitigation:** Low (2) -- UI issues are visible and easily correctable.

---

### RISK-09: Mainframe SME Knowledge Attrition

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational -- People |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12 -- Medium** |
| **Affected Phase** | All phases |

**Description:**
Understanding COBOL/CICS/VSAM/JCL requires specialized expertise. If mainframe SMEs leave the project (retirement, reassignment, attrition) before migration is complete, the remaining team loses the ability to interpret complex business rules, debug COBOL-specific behaviors, or resolve discrepancies between COBOL and Java outputs.

**Specific Exposure:**
- COACTUPC (4,236 LOC) business rules require COBOL expertise to interpret
- CBACT04C interest calculation logic requires understanding of COBOL decimal arithmetic
- JCL job scheduling dependencies are tribal knowledge

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Complete APPLICATION_INVENTORY.md, DATA_DICTIONARY.md, DEPENDENCY_MAP.md, HOTSPOT_REPORT.md (this effort) | Analyst | In Progress |
| 2 | Record knowledge transfer sessions with SMEs for each functional area | PM | Pending |
| 3 | Document every business rule decision in a decision log (ADR format) | Analyst | Pending |
| 4 | Pair mainframe SME with Java developer for each module during migration | Dev lead | Pending |
| 5 | Retain at least one SME under contract through Phase 4 (decommission) | PM | Pending |

**Residual Risk After Mitigation:** Medium (6) -- documentation reduces but doesn't eliminate dependency on SMEs.

---

### RISK-10: Assembler Dependency Replacement

| Attribute | Value |
|-----------|-------|
| **Category** | Technical -- Compatibility |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **6 -- Medium** |
| **Affected Phase** | Phase 1, 3 |
| **Affected Programs** | COBDATFT (ASM), MVSWAIT (ASM), called by CBACT01C and COBSWAIT |

**Description:**
Two assembler programs provide low-level services: COBDATFT (date formatting) and MVSWAIT (MVS wait/delay). These have no direct Java equivalent and must be replaced with Java library calls. While straightforward, incorrect date formatting could cascade through reporting (CBACT01C uses formatted dates in output).

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| 1 | Replace COBDATFT with `java.time.format.DateTimeFormatter` | Dev | Pending |
| 2 | Replace MVSWAIT with `Thread.sleep()` or `ScheduledExecutorService` | Dev | Pending |
| 3 | Create date formatting test suite covering all date formats used by CBACT01C | QA | Pending |
| 4 | Verify COBDATFT output format by running assembler program and capturing results | SME | Pending |

**Residual Risk After Mitigation:** Low (2) -- Java date libraries are mature and well-tested.

---

## Risk Summary Matrix

| ID | Risk | L | I | Score | Rating | Phase | Status |
|----|------|:-:|:-:|:-----:|:------:|:-----:|--------|
| RISK-01 | Decimal arithmetic divergence | 4 | 5 | **20** | Critical | 3 | Open |
| RISK-02 | ACCTFILE write contention | 4 | 4 | **16** | High | 2-3 | Open |
| RISK-03 | Business rule loss (COACTUPC) | 3 | 5 | **15** | High | 2 | Open |
| RISK-06 | Plaintext password exposure | 3 | 5 | **15** | High | 1 | Open |
| RISK-07 | Data sync during strangler transition | 4 | 4 | **16** | High | 2-3 | Open |
| RISK-04 | Batch cycle temporal coupling | 3 | 4 | **12** | Medium | 3 | Open |
| RISK-09 | SME knowledge attrition | 3 | 4 | **12** | Medium | All | Open |
| RISK-05 | CICS pseudo-conversational state loss | 3 | 3 | **9** | Medium | 1-3 | Open |
| RISK-08 | BMS-to-Web UI mapping errors | 3 | 2 | **6** | Medium | 1-3 | Open |
| RISK-10 | Assembler dependency replacement | 2 | 3 | **6** | Medium | 1, 3 | Open |

---

## Risk Heatmap

```
              Impact
         1    2    3    4    5
       ┌────┬────┬────┬────┬────┐
    5  │    │    │    │    │    │
       ├────┼────┼────┼────┼────┤
L   4  │    │    │    │R02 │R01 │
i      │    │    │    │R07 │    │
k   ├────┼────┼────┼────┼────┤
e   3  │    │R08 │R05 │R04 │R03 │
l      │    │    │    │R09 │R06 │
i   ├────┼────┼────┼────┼────┤
h   2  │    │    │R10 │    │    │
o      ├────┼────┼────┼────┼────┤
o   1  │    │    │    │    │    │
d      └────┴────┴────┴────┴────┘

  Legend: Green (1-5) │ Yellow (6-12) │ Orange (13-19) │ Red (20-25)
```

---

## Risk Response Plan Summary

| Priority | Risk IDs | Immediate Action |
|:--------:|----------|-----------------|
| **P0** | RISK-01 | Build `BigDecimal` arithmetic test suite before Phase 3 starts |
| **P0** | RISK-06 | Hash passwords during Phase 1 ETL; force password reset |
| **P1** | RISK-02, RISK-07 | Design data ownership model and sync strategy in Phase 0 |
| **P1** | RISK-03 | Extract COACTUPC business rules document before Phase 2 starts |
| **P2** | RISK-04 | Design Spring Batch job orchestration in Phase 0 |
| **P2** | RISK-09 | Schedule SME knowledge transfer sessions immediately |
| **P3** | RISK-05, RISK-08, RISK-10 | Address during standard development; no special pre-work needed |
