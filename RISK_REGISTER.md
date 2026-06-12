# CardDemo Modernization Risk Register

> Top risks for the COBOL→Java modernization of CardDemo, with likelihood, impact, and mitigations. Scoring: **L/M/H**. Exposure = Likelihood × Impact. Cross-references to `MODERNIZATION_BLUEPRINT.md` (BP), `DOMAIN_DECOMPOSITION.md` (DD), and `CUTOVER_PLAN.md` (CP).

## Heat Summary

| ID | Risk | Likelihood | Impact | Exposure | Owner area |
|:--|:--|:--:|:--:|:--:|:--|
| R1 | Financial calculation drift (posting/interest) | H | H | **Critical** | Posting & Interest (BC-7) |
| R2 | COMP-3 / `S9(n)V99` numeric conversion errors | H | H | **Critical** | Cross-cutting / data |
| R3 | `CARDXREF` file-coupling breaks on extraction | M | H | **High** | Card (BC-4) |
| R4 | Hidden business rules buried in large programs (`COACTUPC` 4,236 lines) | H | M | **High** | Account (BC-3) |
| R5 | Dual-writer account-balance inconsistency during overlap | M | H | **High** | Account / Posting |
| R6 | EBCDIC → ASCII data migration corruption | M | H | **High** | Data migration |
| R7 | Weak legacy auth carried forward (plaintext passwords) | M | H | **High** | Identity (BC-1) |
| R8 | IMS/DB2/MQ optional-module complexity underestimated | M | M | Medium | Authorization (BC-8) |
| R9 | Batch scheduling / job-dependency parity (Control-M/CA-7) | M | M | Medium | Batch orchestration |
| R10 | Inadequate parity/test oracle → silent regressions | M | H | **High** | QA / cross-cutting |
| R11 | Scarce COBOL/mainframe SME availability | M | M | Medium | Program / people |
| R12 | Scope creep from "fix it while we migrate" | M | M | Medium | Program governance |
| R13 | Big-bang temptation / stalled strangler | L | H | Medium | Program governance |
| R14 | PII/regulatory exposure during dual-run (SSN, FICO) | M | H | **High** | Customer (BC-2) / compliance |

---

## Detailed Risks & Mitigations

### R1 — Financial calculation drift *(Critical)*
**Description:** Posting (`CBTRN02C`) and interest (`CBACT04C`) implement money math whose results must match the legacy to the cent. Rounding mode, truncation, and operation ordering in COBOL fixed-point arithmetic are easy to reproduce incorrectly in Java.
**Mitigations:**
- Migrate this core **last** (CP Phase 7) behind a golden-master parity harness.
- Mandatory **parallel run** over multiple cycles incl. month-end; reconcile account balances, `TCATBALF`, and generated transactions; zero unexplained deltas is the cutover gate.
- Document and pin rounding rules; encode as tests before porting logic.

### R2 — Numeric format conversion errors *(Critical)*
**Description:** Records use COMP-3 packed decimal, signed/zoned decimal, and `S9(n)V99` implied-decimal fields (e.g. `ACCT-CURR-BAL S9(10)V99`, `TRAN-AMT S9(09)V99`, `DIS-INT-RATE S9(04)V99`). Mis-scaling or sign mishandling silently corrupts money.
**Mitigations:**
- Establish one canonical mapping: implied-decimal/COMP-3 → `BigDecimal` with explicit scale, documented per field (BP §5).
- Numeric-parity unit suite that round-trips every monetary copybook field against legacy-produced values (CP Phase 0).
- Automated copybook→schema generation to avoid hand-transcription errors.

### R3 — `CARDXREF` file-coupling breaks *(High)*
**Description:** `CARDXREF` (`CVACT03Y`) is read directly by Card, Account, Transaction, Posting, and Authorization to resolve card↔account↔customer (DD §4.1). Removing it without a replacement breaks many programs at once.
**Mitigations:**
- Make Card the relationship owner; expose a `resolve()` lookup API + cached read model.
- Anti-corruption adapter materializes a `CARDXREF`-shaped view for un-migrated COBOL during overlap.
- Migrate Card **paired with** Account (CP Phase 4); enforce FK integrity in the new schema.

### R4 — Hidden rules in large programs *(High)*
**Description:** `COACTUPC` (4,236 lines) and other large programs (`COTRTLIC` 2,098; `COCRDUPC` 1,560) concentrate edit/validation logic, dead code, and special cases that aren't documented.
**Mitigations:**
- Assisted transpilation first to faithfully recover behavior, then refactor to a clean domain model (BP §3.4).
- Characterization tests against legacy screens/outputs before rewriting any rule.
- Dead-code detection; confirm with SMEs before dropping any branch.

### R5 — Dual-writer balance inconsistency *(High)*
**Description:** Today both posting and interest (and online `COACTUPC`) write `ACCT-CURR-BAL`. During phased overlap, a legacy writer and a new writer could both mutate the same account (DD §4.3).
**Mitigations:**
- Route all balance mutations through Account's `applyPosting`/`applyInterest` commands — **one writer** (DD §4.3, CP Phase 4).
- Do not cut over posting (Phase 7) until Account (Phase 4) owns balance writes.
- Per-account routing flag so an account is served by exactly one system at a time; reconciliation on the overlap boundary.

### R6 — EBCDIC→ASCII migration corruption *(High)*
**Description:** Source data ships as EBCDIC (`app/data/EBCDIC`) with packed/binary fields; naive codepage conversion corrupts COMP-3 and signed fields (ASCII text copies in `app/data/ASCII` are not authoritative for binary fields).
**Mitigations:**
- Field-aware conversion driven by copybooks (don't treat records as text); never run a blanket EBCDIC→ASCII text translate over binary columns.
- Checksum/row-count reconciliation and sampled field-level validation post-load (CP Phase 0 harness).
- Validate against record-length expectations (e.g. ACCT 300, CUST 500, TRAN 350).

### R7 — Weak legacy auth carried forward *(High)*
**Description:** `USRSEC`/`CSUSR01Y` stores 8-char **plaintext** passwords (`SEC-USR-PWD PIC X(08)`), no hashing/lockout. Replatforming/transpiling would preserve the vulnerability.
**Mitigations:**
- **Rewrite** auth early (CP Phase 1): Spring Security + OIDC, bcrypt, RBAC from `SEC-USR-TYPE`.
- Forced password reset on user migration; never persist legacy plaintext in the new store.

### R8 — Optional-module (IMS/DB2/MQ) complexity *(Medium)*
**Description:** Authorization spans IMS hierarchical DB, DB2 (`AUTHFRDS`), and IBM MQ across 8 programs — the most heterogeneous area; IMS→relational remapping is non-trivial.
**Mitigations:**
- Exploit the existing MQ seam as the strangler insertion point (DD §4.4); keep the message contract, swap the consumer.
- Model IMS segments explicitly; preserve fraud-rule semantics from `AUTHFRDS` with dedicated tests.
- Treat as its own phase (CP Phase 6) with extra SME time.

### R9 — Batch scheduling parity *(Medium)*
**Description:** ~46 JCL jobs run as an ordered stream under Control-M/CA-7 (`app/scheduler/CardDemo.controlm`, `.ca7`) with file-lock wrap jobs (CLOSEFIL/OPENFIL) and GDG versioning. Re-expressing dependencies on a new scheduler can drop ordering constraints.
**Mitigations:**
- Reverse-engineer the dependency DAG from the scheduler definitions; encode explicitly in the new orchestrator.
- Replace CICS file-lock wrap pattern and mainframe timer waits (`COBSWAIT`/`MVSWAIT`) with platform-native concurrency controls.
- Job-level parity: compare outputs of each migrated job vs. legacy.

### R10 — Inadequate test oracle *(High)*
**Description:** Without a trustworthy parity oracle, regressions slip through silently — especially in money paths.
**Mitigations:**
- Golden-master harness built in Phase 0 and required for every slice; reconciliation report is a hard cutover gate.
- Capture legacy inputs/outputs (screens, files, reports) as fixtures before changing anything.
- Coverage gate on migrated services.

### R11 — COBOL/mainframe SME scarcity *(Medium)*
**Description:** Recovering intent from undocumented programs depends on scarce SMEs; availability gaps stall analysis.
**Mitigations:**
- Front-load knowledge capture; pair SMEs with assisted-analysis tooling to scale their time.
- Prioritize SME effort on high-risk areas (Account, Posting/Interest, Authorization).

### R12 — Scope creep *(Medium)*
**Description:** Pressure to add features or "fix" behavior mid-migration jeopardizes parity and timelines.
**Mitigations:**
- Strict rule: **migrate to parity first, enhance after cutover.** Exceptions only for security defects (R7) and PII controls (R14).
- Change-control board; backlog of post-cutover enhancements kept separate.

### R13 — Big-bang temptation / stalled strangler *(Medium)*
**Description:** Teams may abandon incremental cutover for a risky all-at-once switch, or let the strangler stall with both systems running indefinitely (carrying dual cost).
**Mitigations:**
- Commit to the phased plan (CP §3); each phase ships independently with rollback.
- Track a "percentage strangled" metric and a decommission deadline (CP Phase 8) to avoid permanent dual-run.

### R14 — PII/regulatory exposure during dual-run *(High)*
**Description:** Customer data includes SSN, govt id, DOB, FICO (`CVCUS01Y`). Replicating across legacy + new + bridges widens the exposure surface and may trigger compliance obligations.
**Mitigations:**
- Encrypt/tokenize SSN and government id at rest in the new store (CP Phase 3); encrypt all replication channels.
- Minimize/obfuscate PII in non-production parity datasets.
- Access controls, audit logging, and data-retention policy applied to bridges; retire bridges promptly at decommission (CP Phase 8).

---

## Monitoring & Governance

- **Re-score each risk at every phase gate** (CP §4); a risk's likelihood typically drops once its context is cut over and reconciled.
- **Financial reconciliation is the master gate.** Risks R1/R2/R5/R6/R10 must all be green before the Posting & Interest cutover (CP Phase 7).
- **Keep rollback warm.** Do not decommission legacy (CP Phase 8) until a full statement cycle — including month-end — runs clean on the new platform.
