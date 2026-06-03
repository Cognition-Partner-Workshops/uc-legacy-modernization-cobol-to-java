# CardDemo Modernization Risk Register

> Top risks for the COBOL → Java modernization of CardDemo, with likelihood,
> impact, and concrete mitigations grounded in the actual codebase.

Companion to [`MODERNIZATION_BLUEPRINT.md`](./MODERNIZATION_BLUEPRINT.md),
[`DOMAIN_DECOMPOSITION.md`](./DOMAIN_DECOMPOSITION.md), and
[`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md).

## Scoring

- **Likelihood / Impact**: Low · Medium · High
- **Severity** = Likelihood × Impact, bucketed: 🟢 Low · 🟡 Medium · 🔴 High · ⚫ Critical
- **Owner**: indicative role accountable for the mitigation.

## Summary (sorted by severity)

| ID | Risk | Likelihood | Impact | Severity |
|:--:|:-----|:----------:|:------:|:--------:|
| R1 | Financial calculation divergence (rounding / fixed-point) | High | High | ⚫ |
| R2 | EBCDIC + packed/zoned-decimal data conversion errors | High | High | ⚫ |
| R3 | Posting engine (`CBTRN02C`) behavioral parity | Medium | High | 🔴 |
| R4 | Account balance dual-writer corruption during transition | Medium | High | 🔴 |
| R5 | Hidden coupling via the card XREF join | High | Medium | 🔴 |
| R6 | Security uplift gaps (plaintext passwords, PII, PAN/CVV) | Medium | High | 🔴 |
| R7 | Batch dependency-graph (Control-M/CA7) misordering | Medium | Medium | 🟡 |
| R8 | Scarce COBOL/mainframe domain knowledge | Medium | High | 🔴 |
| R9 | Incomplete/implicit business rules (no spec) | High | Medium | 🔴 |
| R10 | Optional modules (IMS/DB2/MQ) integration drift | Medium | Medium | 🟡 |
| R11 | Cutover/rollback failure (irreversible state) | Low | High | 🟡 |
| R12 | Scope creep / "improve while migrating" | Medium | Medium | 🟡 |
| R13 | Reference-data drift between DB2 and VSAM projections | Medium | Medium | 🟡 |
| R14 | Performance regression vs. mainframe batch windows | Medium | Medium | 🟡 |
| R15 | Test-data realism / coverage gaps | Medium | Medium | 🟡 |

---

## Detailed register

### R1 — Financial calculation divergence ⚫
- **Description**: Money fields are fixed-point COBOL decimals (`PIC S9(10)V99`, e.g. `ACCT-CURR-BAL`). Interest (`CBACT04C`) and posting (`CBTRN02C`) rely on COBOL's truncation/rounding semantics. A Java implementation using floating point — or a different rounding mode — will diverge by cents that compound over cycles.
- **Trigger phases**: 8 (interest), 9 (posting), 6 (account).
- **Mitigation**:
  - Represent all money as `BigDecimal` with explicit scale 2 and an agreed `RoundingMode`; **never** `double`/`float`.
  - Refactor (transpile) the arithmetic-heavy engines rather than rewriting, to preserve operation order.
  - Golden-file parity tests: dual-run new vs legacy on identical inputs and reconcile **to the cent** for ≥1 full billing cycle before any flip.
- **Owner**: Lead engineer + Finance/Controls.

### R2 — EBCDIC + packed-decimal conversion errors ⚫
- **Description**: Persistent data is EBCDIC (`app/data/EBCDIC/`) with `COMP-3` packed and zoned-decimal, signed/unsigned fields, `REDEFINES`, and `OCCURS`. Mis-converting code pages, sign nibbles, or field offsets silently corrupts balances and identifiers.
- **Trigger phases**: 0 (ACL), every data migration phase.
- **Mitigation**:
  - Centralize **all** conversion in the Anti-Corruption Layer; no ad-hoc parsing per service.
  - Drive layouts directly from the copybooks (`app/cpy/`) — single source of truth; round-trip (EBCDIC→model→EBCDIC) every record type and assert byte-equality in CI.
  - Pay special attention to signed `COMP-3` and `REDEFINES`/`OCCURS DEPENDING ON`.
- **Owner**: Data engineering.

### R3 — Posting engine parity (`CBTRN02C`) 🔴
- **Description**: `CBTRN02C` validates each daily transaction, resolves card→account via XREF, posts to `TRANSACT`, updates `TCATBAL`, and writes rejects to `DALYREJS`. Subtle differences in validation order or reject criteria change financial outcomes.
- **Trigger phase**: 9.
- **Mitigation**:
  - Treat the **reject file (`DALYREJS`) as a first-class output** — reconcile rejects record-for-record, not just accepted postings.
  - Extended **shadow** soak (dual-run, compare ledger + balances + rejects), then key-range canary, then full.
  - Migrate only after R5 (XREF), R4 (account writer), and R1 (interest) are controlled.
- **Owner**: Core platform team.

### R4 — Account balance dual-writer corruption 🔴
- **Description**: `ACCT-CURR-BAL` is written by **both** online (`COACTUPC`) and batch (`CBTRN02C`, `CBACT04C`). During transition, OLD and NEW could both update the same account and corrupt it.
- **Trigger phases**: 6, 7, 9.
- **Mitigation**:
  - Enforce **single-writer-per-dataset-per-phase**; use a shared-DB / CDC bridge so only one side mutates balances at a time.
  - Define an explicit "apply posting / apply interest to account" command as the sole balance-mutation path in the target.
  - Reconcile balance invariants every cycle; alert on any unexplained delta.
- **Owner**: Account context owner.

### R5 — Hidden coupling via card XREF 🔴
- **Description**: The customer↔account↔card cross-reference (`CVACT03Y`, `CARDXREF`) is read by nearly every financial flow (e.g., `CBTRN02C` reads XREF per record to resolve a card). It is an invisible, system-wide join with no service boundary.
- **Trigger phase**: 4 (and everything depending on it).
- **Mitigation**:
  - Extract a **card→{account, customer} resolution Open Host Service early** and route all resolution (including legacy, via a shim where feasible) through it before migrating Account/Transaction.
  - Parity-test resolution against legacy XREF reads byte-for-byte.
- **Owner**: Card context owner.

### R6 — Security uplift gaps 🔴
- **Description**: Sign-on (`COSGN00C`) compares **plaintext** passwords in `USRSEC` (README default `PASSWORD`); RACF is simulated. Customer PII (SSN, DOB, govt id in `CVCUS01Y`) and PAN/CVV (`CVACT02Y`) sit in flat files unencrypted. Carrying these forward reproduces serious vulnerabilities.
- **Trigger phases**: 1 (auth), 3 (customer PII), 4 (PAN/CVV).
- **Mitigation**:
  - Phase 1 replaces auth with OIDC/JWT + hashed credentials; force credential re-enrollment (don't migrate plaintext).
  - Encrypt PII at rest; tokenize/encrypt PAN and never persist CVV beyond authorization need.
  - Add security scanning (SAST/SCA) to the pipeline.
- **Owner**: Security + Identity context owner.

### R7 — Batch dependency-graph misordering 🟡
- **Description**: Job order/dependencies are encoded in Control-M (`CardDemo.controlm`, `INCOND`/`OUTCOND`) and CA7 (`CardDemo.ca7`) across 38 JCL members. Re-implementing the schedule risk running steps out of order (e.g., posting before backup, statements before posting).
- **Trigger phase**: 10.
- **Mitigation**:
  - Extract the dependency graph from the scheduler definitions as the **authoritative spec**; reproduce it explicitly in the new workflow engine.
  - Run new and legacy schedules in parallel against the same datasets and compare completion + outputs before shifting ownership step-by-step.
- **Owner**: Platform/SRE.

### R8 — Scarce COBOL/mainframe knowledge 🔴
- **Description**: COBOL, CICS, JCL, IMS DB, and Assembler (`MVSWAIT`, `COBDATFT`) skills are increasingly rare; misreading legacy behavior leads to wrong target logic.
- **Mitigation**:
  - Use the parity harness as the **objective oracle** — behavior is defined by legacy output, reducing reliance on tribal knowledge.
  - Capture rules as executable parity tests as they're discovered; document in the bounded-context language ([`DOMAIN_DECOMPOSITION.md`](./DOMAIN_DECOMPOSITION.md)).
  - Pair AI-assisted code analysis with SME review; re-implement (don't transpile) the two Assembler routines on JDK.
- **Owner**: Program lead.

### R9 — Incomplete/implicit business rules 🔴
- **Description**: There is no external spec; rules live only in code (validation in `COACTUPC`, reject criteria in `CBTRN02C`, interest rules in `CBACT04C`). Edge cases (error paths, unusual statuses) are easy to miss.
- **Mitigation**:
  - Derive behavior from code + **production-representative data**, not assumptions; mine real input distributions for test cases.
  - Exercise error/reject/edge paths explicitly in parity tests, not just happy paths.
  - Refactor (preserve) rather than rewrite where rules are dense and unclear.
- **Owner**: Business analyst + engineering.

### R10 — Optional-module integration drift 🟡
- **Description**: Authorization uses IMS DB + DB2 + MQ (`COPAUA0C`, `CBPAUP0C`); account-extract uses MQ (`COACCT01`, `CODATE01`). Hierarchical IMS and message contracts are easy to model incorrectly.
- **Trigger phase**: 11.
- **Mitigation**:
  - Reuse the **existing MQ contract** as the seam; swap persistence internally.
  - Shadow auth decisions (compare, don't serve) before canary; treat as an isolated workstream so issues don't block the core path.
- **Owner**: Authorization context owner.

### R11 — Cutover/rollback failure 🟡
- **Description**: A flawed cutover could leave state split between OLD and NEW with no clean rollback.
- **Mitigation**:
  - Single-source-of-truth per dataset per phase makes **rollback a routing change**, not a data repair (see [`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md) §5).
  - Keep legacy warm through a soak period; never decommission before the soak gate passes.
  - Rehearse rollback in canary.
- **Owner**: Release management.

### R12 — Scope creep ("improve while migrating") 🟡
- **Description**: Temptation to add features/redesign during migration inflates risk and defeats parity testing (you can no longer diff against legacy).
- **Mitigation**:
  - **Parity-first rule**: match legacy behavior exactly; defer enhancements to a backlog for *after* a context is cut over and stable.
  - Exceptions only where the blueprint mandates them (security uplift), and those are isolated and explicitly tested.
- **Owner**: Product + program lead.

### R13 — Reference-data drift (DB2 ↔ VSAM projections) 🟡
- **Description**: Transaction types/categories exist in DB2 *and* as VSAM projections (`TRANTYPE`/`TRANCATG`) regenerated via `TRANEXTR`. During transition the new source could drift from the legacy projection consumed by un-migrated programs.
- **Trigger phases**: 2, 9.
- **Mitigation**:
  - Make the new reference service the single source; regenerate legacy projections from it; reconcile daily.
- **Owner**: Reference-data owner.

### R14 — Batch-window performance regression 🟡
- **Description**: Mainframe batch is highly tuned; the new platform must post/compute/produce statements within the same nightly window.
- **Trigger phases**: 8, 9, 10.
- **Mitigation**:
  - Load/throughput-test posting and interest at production volumes early; design for parallelism by account key-range.
  - Use the replatform bridge as a fallback if the new engine misses the window during transition.
- **Owner**: Performance/SRE.

### R15 — Test-data realism & coverage 🟡
- **Description**: Parity testing is only as good as the data; synthetic data may miss edge cases present in production EBCDIC datasets.
- **Mitigation**:
  - Use masked production-representative data (preserving distributions, signs, edge values); supplement with targeted edge-case fixtures derived from code paths.
  - Track parity coverage per context as a release gate.
- **Owner**: QA/Data.

---

## Risk-to-phase heatmap

| Phase (Cutover Plan) | Dominant risks |
|:---------------------|:---------------|
| 0 Foundations | R2, R15 |
| 1 Identity | R6 |
| 2 Reference Data | R13 |
| 3 Customer | R2, R6 |
| 4 Card/XREF | R5, R6 |
| 5 Statements (RO) | R1 (presentation), R15 |
| 6 Account | R4, R1 |
| 7 Transaction online | R9 |
| 8 Interest | R1, R14 |
| 9 **Posting** | R1, R3, R4, R14 |
| 10 Orchestration | R7, R14 |
| 11 Authorization | R10 |
| 12 Decommission | R11 |

Cross-cutting throughout: **R8** (skills), **R9** (implicit rules), **R12**
(scope creep) — managed by the parity-first discipline and the parity harness
that underpins every phase.
