# CardDemo Cutover Plan

> Phased migration sequence from **lowest risk to highest risk**, taking the
> CardDemo mainframe system from COBOL/CICS/VSAM to the Java target under a
> Strangler Fig program.

Companion to [`MODERNIZATION_BLUEPRINT.md`](./MODERNIZATION_BLUEPRINT.md),
[`DOMAIN_DECOMPOSITION.md`](./DOMAIN_DECOMPOSITION.md), and
[`RISK_REGISTER.md`](./RISK_REGISTER.md).

---

## 1. Principles

1. **Lowest risk first.** Begin with leaf/read-only/low-coupling capabilities to
   build the platform, the parity-test harness, and team confidence before
   touching money movement.
2. **Money movement migrates last.** Transaction posting (`CBTRN02C`) and
   interest (`CBACT04C`) are migrated only after every dependency (card
   resolution, reference data, account command API) is stable and parity-proven.
3. **Every cutover is reversible.** The strangler facade can route any capability
   back to the mainframe instantly; no phase deletes the legacy path until its
   replacement has run clean in production for a defined soak period.
4. **Parity before flip.** New code runs in **shadow** (dual-run, compare, don't
   serve) until reconciliation is green, then **canary** (serve a slice), then
   **full**.
5. **Data stays single-sourced.** During each phase, exactly one side owns each
   writable dataset; the other reads via the Anti-Corruption Layer (ACL) or CDC.
   No silent dual-writes to the same record.

### Cutover pattern per capability

```
 Build ─► Shadow (dual-run + reconcile) ─► Canary (route % or key-range)
       ─► Full cutover ─► Soak ─► Decommission legacy path
            ▲                                  │
            └──────────── rollback ◄───────────┘  (facade reroute to legacy)
```

### Risk legend
🟢 Low · 🟡 Medium · 🔴 High · ⚫ Highest

---

## 2. Phase 0 — Foundations (no functional cutover) · 🟢

Stand up everything needed before a single capability moves.

| Workstream | Deliverable |
|:-----------|:------------|
| Strangler facade | API gateway/router that decides OLD vs NEW per capability + per key range; default = OLD. |
| Anti-Corruption Layer | EBCDIC↔ASCII + packed/zoned-decimal (`COMP-3`) conversion; access to VSAM/DB2/IMS for the new stack. Money = fixed-scale decimal (scale 2). |
| Parity harness | Golden-file/reconciliation framework: feed identical inputs to OLD and NEW, diff outputs, report. (Mirrors the org's SAS→Databricks reconciliation pattern.) |
| Replatform bridge | Validate the rehost runtime already seeded in `samples/m2/unikix/` so legacy can run off-mainframe during transition and batch keeps flowing. |
| Shared utilities (BC-10) | Re-implement date (`CSUTLDTC`/`COBDATFT`) and timer (`MVSWAIT`/`COBSWAIT`) on JDK; publish as the shared lib all later phases use. |
| CI/CD + observability | Pipeline, test gates, logging/metrics/tracing, data-reconciliation dashboards. |

**Exit gate:** facade can route a trivial call to a NEW no-op service and back;
ACL round-trips a known EBCDIC record byte-for-byte; parity harness runs in CI.

---

## 3. Phase sequence (low → high risk)

### Phase 1 — Identity & Access (BC-1) · 🟢
- **Scope**: replace sign-on (`COSGN00C`) and user admin (`COUSR0x`, `COADM01C`) with an auth service (OIDC/JWT, hashed credentials, roles Admin/User) + user CRUD.
- **Strategy**: Rewrite.
- **Data**: migrate `USRSEC` → identity store; **drop plaintext passwords** (force reset / re-enroll).
- **Cutover**: facade authenticates all traffic via new tokens; legacy programs receive identity injected into COMMAREA (`CDEMO-USER-ID/TYPE`) by the facade during transition.
- **Why first**: low data coupling, high control-flow leverage (every later online cutover relies on the facade trusting tokens), and it removes a security liability immediately.
- **Rollback**: facade falls back to legacy `CC00` sign-on.
- **Exit gate**: 100% of online entry authenticated by new service; admin/user roles enforced; zero auth regressions in canary.

### Phase 2 — Reference Data (BC-8) · 🟢
- **Scope**: transaction types/categories (`COTRTLIC`, `COTRTUPC`, `COBTUPDT`) and disclosure-group reference.
- **Strategy**: Rewrite (already relational in DB2).
- **Data**: migrate DB2 `TRNTYPE`/`TRNTYCAT` ~1:1 to target RDBMS; new admin CRUD UI.
- **Cutover**: new service becomes source of truth for reference codes; legacy VSAM projections (`TRANTYPE`/`TRANCATG`) regenerated from it via the existing extract jobs (`TRANEXTR`) during transition.
- **Why early**: read-mostly, low risk, and it supplies the codes that Transaction/Interest will need from the new platform.
- **Exit gate**: reference reads served from NEW; legacy projections reconciled daily.

### Phase 3 — Customer master (BC-2) · 🟢🟡
- **Scope**: customer read/write API + relational `customer` model (`CVCUS01Y`).
- **Strategy**: Rewrite model + API; replatform `CBCUS01C` loader transitionally.
- **Data**: ACL exposes `CUSTDATA`; backfill to RDBMS; **encrypt PII (SSN, DOB, govt id)**.
- **Cutover**: NEW owns customer reads first (shadow→canary), then writes; legacy reads customer via ACL.
- **Exit gate**: customer reads from NEW with reconciliation green; PII encrypted at rest.

### Phase 4 — Card resolution & Card mgmt (BC-4) · 🟡🔴
- **Scope**: card CRUD (`COCRDLIC/SLC/UPC`) **and** the system-wide **card→{account, customer} resolution** (XREF, `CVACT03Y`).
- **Strategy**: Rewrite; expose resolution as an Open Host Service.
- **Critical action**: route **all** card-resolution lookups — including legacy ones where feasible via a shim — through the new resolution API so later phases have a stable identity contract.
- **Data**: migrate `CARDDATA`+`CARDXREF`; tokenize/encrypt PAN/CVV.
- **Cutover**: resolution served by NEW in shadow until byte-equivalent to legacy XREF reads, then full.
- **Why here**: the XREF is the linchpin join (Transaction posting reads it per record); it must be stable **before** Account/Transaction migrate, but it is itself simple data.
- **Exit gate**: resolution API parity-proven against legacy XREF; PAN/CVV protected.

### Phase 5 — Statements & Reporting (BC-6 read-only) · 🟢🟡
- **Scope**: statements (`CBSTM03A/B`), transaction reports (`CBTRN03C`, `CORPT00C`), `TXT2PDF`/FTP delivery.
- **Strategy**: Rewrite as queries + PDF/HTML rendering.
- **Cutover**: run NEW statements in **shadow** against legacy `CREASTMT` output and diff every field; flip delivery only when 100% match across a full cycle.
- **Why here**: read-only and side-effect-free → safe, high-visibility win that exercises the ledger read paths before any ledger write is migrated.
- **Exit gate**: a full statement cycle matches legacy field-for-field.

### Phase 6 — Account command API (BC-3) · 🔴
- **Scope**: account view/update (`COACTVWC`, `COACTUPC`) + an explicit **"apply posting / apply interest to account"** command that becomes the single owner of balance mutation.
- **Strategy**: Refactor (transpile `COACTUPC` validation) + new command API.
- **Data**: **shared-DB / CDC phase** so legacy batch (`CBTRN02C`, `CBACT04C`) and the new service never double-update `ACCT-CURR-BAL`. One writer at a time, enforced.
- **Cutover**: online view/update move first (lower risk); balance-mutation command stays dormant (legacy batch still posts) until Phase 8–9.
- **Why high risk**: account is the money-bearing master with **two writers** (online + batch); coordinating ownership is the central hard problem.
- **Exit gate**: account reads/updates from NEW; balance invariants reconciled every cycle; documented single-writer ownership.

### Phase 7 — Transaction online (BC-5 add/view/list) · 🟡
- **Scope**: `COTRN00C/01C/02C` — list/view/add transactions.
- **Strategy**: Rewrite screens to API; writes go to the ledger via the ACL (legacy posting still authoritative).
- **Cutover**: new "add transaction" stages into `DALYTRAN` exactly as legacy does, so nightly posting is unchanged; emit `TransactionStaged` events for new consumers.
- **Exit gate**: online transaction entry served by NEW; staged records identical to legacy format; posting still green.

### Phase 8 — Interest & Fees engine (BC-6, `CBACT04C`) · 🔴
- **Scope**: `INTCALC` interest computation over disclosure groups × category balances.
- **Strategy**: Refactor (transpile) for exact arithmetic; **batch-step seam** — replace the `CBACT04C` step behind the same dataset I/O contract.
- **Cutover**: dual-run NEW vs legacy interest for ≥1 full billing cycle; reconcile every accrual to the cent; flip only on 100% match.
- **Why late/high**: monetary correctness; feeds account balances and statements.
- **Exit gate**: interest amounts match legacy to the cent across a full cycle.

### Phase 9 — Transaction posting engine (BC-5, `CBTRN02C`) · ⚫
- **Scope**: the core — post daily transactions, validate, resolve via card API (Phase 4), update account via command (Phase 6) and category balances, write rejects.
- **Strategy**: Refactor (transpile) the posting logic; later re-architect to event-driven once parity holds.
- **Cutover**: **shadow** the new posting against legacy nightly for an extended soak — compare posted ledger, balances, category balances, and the **reject file (`DALYREJS`) record-for-record** (rejects are as load-bearing as accepts). Canary by account key-range, then full.
- **Why last**: highest blast radius; depends on Phases 4, 6, 8 being stable.
- **Rollback**: facade/scheduler reverts the posting step to legacy `CBTRN02C`; because state is single-sourced, reverting is a routing change, not a data repair.
- **Exit gate**: extended shadow soak with zero posting/balance/reject discrepancies; sign-off from finance/controls.

### Phase 10 — Batch orchestration (BC-10) · 🟡
- **Scope**: re-express the Control-M/CA7 job graph (`INCOND`/`OUTCOND`) in a modern workflow engine.
- **Strategy**: Rewrite orchestration; migrate **one step at a time** preserving each step's dataset contract.
- **Cutover**: run new and legacy schedules in parallel against the same datasets; compare completion + outputs; shift step ownership incrementally.
- **Exit gate**: full nightly cycle orchestrated by NEW with identical outputs; GDG/dataset lifecycle handled.

### Phase 11 — Authorization (BC-7, optional) · 🟡
- **Scope**: real-time auth (`COPAUA0C`), summary/detail/fraud (`COPAUS0C/1C/2C`), purge (`CBPAUP0C`).
- **Strategy**: Rewrite behind the **existing MQ contract**; swap IMS/DB2 persistence to target store.
- **Cutover**: new service consumes the same MQ queues in shadow (decision compared, not served), then canary, then full. Isolated from the core path, so it can run on its own timeline.
- **Exit gate**: auth decisions match legacy in shadow; fraud/purge behavior verified.

### Phase 12 — Integration cleanup & decommission (BC-9, bridge) · 🟢
- **Scope**: replace MQ inquiries (`COACCT01`, `CODATE01`) with REST (optionally keep an MQ adapter for external callers); retire branch export/import (`CBEXPORT`/`CBIMPORT`) once both sides share the DB; shut down the replatform bridge and ACL; decommission VSAM/DB2/IMS and the mainframe footprint.
- **Exit gate**: no traffic routes to legacy; legacy datasets archived; mainframe/bridge retired.

---

## 4. Sequence at a glance

| Phase | Capability (BC) | Strategy | Risk | Gated by |
|:-----:|:----------------|:---------|:----:|:---------|
| 0 | Foundations (facade, ACL, parity, bridge, utils) | — | 🟢 | — |
| 1 | Identity & Access (BC-1) | Rewrite | 🟢 | Phase 0 |
| 2 | Reference Data (BC-8) | Rewrite | 🟢 | Phase 0 |
| 3 | Customer (BC-2) | Rewrite | 🟢🟡 | 0 |
| 4 | Card + XREF resolution (BC-4) | Rewrite + OHS | 🟡🔴 | 2,3 |
| 5 | Statements & Reporting (BC-6 RO) | Rewrite (shadow) | 🟢🟡 | 0 |
| 6 | Account command API (BC-3) | Refactor + API | 🔴 | 4 |
| 7 | Transaction online (BC-5) | Rewrite | 🟡 | 4,6 |
| 8 | Interest engine `CBACT04C` (BC-6) | Refactor | 🔴 | 2,6 |
| 9 | **Posting engine `CBTRN02C` (BC-5)** | Refactor | ⚫ | 4,6,8 |
| 10 | Batch orchestration (BC-10) | Rewrite | 🟡 | 9 |
| 11 | Authorization (BC-7, optional) | Rewrite | 🟡 | independent |
| 12 | Integration cleanup & decommission | Rewrite/retire | 🟢 | all |

---

## 5. Gating, parity, and rollback (applies to every phase)

- **Shadow gate**: new path runs on production inputs, outputs compared, nothing
  served. Advance only when reconciliation is 100% (financial) / within agreed
  tolerance (presentation).
- **Canary gate**: serve a bounded slice (key-range or %). Watch error rate,
  latency, and reconciliation. Auto-rollback on threshold breach.
- **Full + soak**: serve all traffic; keep legacy warm for a defined soak before
  decommissioning.
- **Rollback** is always a **facade/scheduler routing change**, never a data
  migration — guaranteed by the single-writer/single-source-of-truth rule per
  dataset per phase.
- **Reconciliation artifacts** (diffs, balance/ledger/reject comparisons) are
  retained per cycle as the audit trail and the go/no-go evidence for each gate.
