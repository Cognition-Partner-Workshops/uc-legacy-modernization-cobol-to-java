# CardDemo Cutover Plan

> Phased migration sequence ordered **lowest-risk → highest-risk**, using the bounded contexts from `DOMAIN_DECOMPOSITION.md` and the per-area strategies from `MODERNIZATION_BLUEPRINT.md`. The overarching pattern is **strangler**: a façade/gateway routes each transaction or job to legacy or new, slice by slice, with data bridged until each slice is cut over.

## 1. Cutover Principles

1. **Sequence by risk, not by org chart.** Read-mostly, low-coupling, high-fan-in contexts go first; the money-moving batch core goes last.
2. **Every slice is reversible.** Each phase ships behind a routing flag with a documented rollback to the legacy transaction/job.
3. **Parity before promotion.** No slice is cut over until new output matches legacy output for a defined reconciliation window (to the cent for financial data).
4. **Bridge, don't freeze.** Legacy keeps running during migration via anti-corruption adapters that materialize legacy-shaped files (`CARDXREF`, `DALYTRAN`) and MQ messages.
5. **One writer per store at cutover.** When a context is cut over, legacy writers to its store are disabled (or redirected through the new owner's API).

## 2. Phase 0 — Foundation & Bridge (no business cutover)

**Goal:** make incremental cutover possible.

- Stand up target platform: Java 21 + Spring Boot service template, PostgreSQL, CI/CD, observability, secrets management.
- Build the **strangler router/gateway** in front of the online channel (initially 100% pass-through to CICS) and a batch dispatcher that can route a job to legacy or new.
- Stand up **data bridge / CDC**: keep legacy VSAM as system-of-record initially; replicate to PostgreSQL read models. Build anti-corruption adapters that can re-materialize `CARDXREF`-shaped and `DALYTRAN`-shaped artifacts.
- Build the **parity harness**: a golden-master framework that runs legacy vs. new for a given input and produces a reconciliation report (row counts, field diffs, financial deltas). Reuse the reconciliation discipline from this org's SAS→Databricks migrations.
- Establish the canonical numeric mapping: COBOL `S9(n)V99` / COMP-3 → `BigDecimal` with explicit scale & rounding, locked by a numeric-parity unit suite.

**Exit criteria:** gateway live (pass-through), CDC flowing, parity harness produces a clean report on a sample dataset, rollback tested.

## 3. Phase Sequence (lowest → highest risk)

| Phase | Context(s) | Strategy | Risk | Why here |
|:--|:--|:--|:--|:--|
| 0 | Foundation/bridge | — | — | Enables everything |
| 1 | Identity & Access (BC-1) | Rewrite (gateway) | Low | Thin logic; becomes the strangler front door |
| 2 | Reference Data (BC-6) | Refactor→Rewrite | Low | High fan-in; unblocks all consumers; DB2 DDL already exists |
| 3 | Customer (BC-2) | Refactor | Low–Med | Near-pure master data; mostly reads downstream |
| 4 | Card + XREF (BC-4) & Account (BC-3) | Refactor | Med–High | Must be paired — they share the XREF join and balance edits |
| 5 | Transaction capture/inquiry (BC-5) | Strangler + Rewrite API | Medium | Producer of DALYTRAN; bridge keeps posting fed |
| 6 | Billing/Statements/Reporting + Authorization (BC-7 read paths, BC-8) | Rewrite / Strangler | Med–High | Output + the MQ-seam auth module |
| 7 | **Posting & Interest core (BC-7 write paths)** | Refactor + parity | **High** | Money math; co-writes account balances; migrate last |
| 8 | Decommission | — | Med | Retire CICS/VSAM/IMS/MQ + bridges |

### Phase 1 — Identity & Access
- Re-implement signon/user management as a Spring Security + OIDC auth service; migrate `USRSEC` users with forced password reset (replace 8-char plaintext with bcrypt).
- Route `CC00` (and user-admin transactions `CU00`–`CU03`) through the gateway to the new auth service; legacy menu/routing delegates to gateway-issued identity.
- **Cutover gate:** all sessions authenticate via new service; legacy `COSGN00C` path disabled.
- **Rollback:** flip gateway auth route back to CICS `CC00`.

### Phase 2 — Reference Data
- Build Reference CRUD service (transaction types, categories, disclosure groups) on PostgreSQL; seed from `TRANTYPE`/`TRANCATG`/`DISCGRP` (and DB2 `TRNTYPE`/`TRNTYCAT`).
- Publish a versioned, cached read API. Point new consumers at it; legacy continues reading files materialized from the new owner.
- Route admin transactions `CTTU`/`CTLI` to the new service.
- **Cutover gate:** new service is system-of-record for reference data; legacy files are generated downstream.
- **Rollback:** revert reference reads to legacy files (still maintained during overlap).

### Phase 3 — Customer
- Migrate `CUSTDAT` into a Customer master-data service; encrypt/tokenize SSN, govt id; add validation.
- Route customer reads/writes; downstream contexts consume customer via API.
- **Cutover gate:** Customer service authoritative; CDC reversed (new → legacy view) for un-migrated readers.
- **Rollback:** re-point to VSAM `CUSTDAT`.

### Phase 4 — Card + XREF and Account (paired)
- **This is the keystone phase.** Card and Account are migrated together because they share the `CARDXREF` join (BC-4 §4.1) and the account-balance edit surface.
- Card context becomes the owner/publisher of the relationship; replace direct `CARDXREF` reads across the system with the `resolve()` lookup API; materialize a `CARDXREF`-shaped view for un-migrated batch.
- Migrate `ACCTDAT`; recover `COACTUPC` validation via assisted transpilation, then express as an Account domain model. Account exposes `applyPosting`/`applyInterest` commands so future posting routes mutations through the owner (removing the dual-writer).
- Route online `CAVW`/`CAUP`, `CCLI`/`CCDL`/`CCUP`.
- **Cutover gate:** Account & Card authoritative; XREF reads served by API; balance writes only via Account commands.
- **Rollback:** re-point online card/account transactions to CICS and restore file-based XREF (overlap window kept warm).

### Phase 5 — Transaction Capture & Inquiry
- New Transaction service owns `TRANSACT`; online add/list/view (`CT00`/`CT01`/`CT02`) routed to it.
- Capture emits a `TransactionCaptured` event **and** continues writing `DALYTRAN`-shaped records (bridge) so legacy posting still runs.
- **Cutover gate:** all online transaction capture on new service; DALYTRAN bridge verified.
- **Rollback:** route capture back to CICS `CT0x`.

### Phase 6 — Billing/Statements/Reporting + Authorization
- Rebuild statements/reports (`CBSTM03A`, `CBTRN03C`, `CORPT00C`) on a modern reporting stack reading migrated data; bill payment (`COBIL00C`) reuses migrated posting commands.
- Authorization (BC-8): exploit the existing **MQ seam** — front the auth queue with a new service, replace IMS access with relational/document model, preserve `AUTHFRDS` fraud semantics; migrate purge job `CBPAUP0C`.
- **Cutover gate:** new statements/reports reconcile to legacy; auth requests served by new service over the same MQ contract.
- **Rollback:** revert MQ consumer to `COPAUA0C`; re-enable legacy report jobs.

### Phase 7 — Posting & Interest Core (highest risk, last)
- Migrate `CBTRN02C` (POSTTRAN) and `CBACT04C` (INTCALC) via assisted transpilation under the parity harness.
- Run **parallel run**: new posting/interest executes alongside legacy on the same `DALYTRAN` input for N cycles; reconcile account balances, category balances (`TCATBALF`), and generated transactions to the cent. Investigate every delta (rounding, ordering, COMP-3 conversion).
- Posting/interest apply changes through Account's `applyPosting`/`applyInterest` commands (single writer).
- **Cutover gate:** zero unexplained financial deltas across a full statement cycle (incl. month-end) in parallel run.
- **Rollback:** disable new posting, fall back to legacy batch stream (kept runnable until decommission sign-off).

### Phase 8 — Decommission
- Retire CICS region, VSAM datasets, IMS DB, IBM MQ, and all anti-corruption bridges once every context is cut over and a full cycle has run clean on the new platform.
- Remove the strangler routing flags; the gateway becomes the permanent edge.
- Archive legacy data per retention/regulatory policy.

## 4. Cross-Phase Cutover Mechanics

- **Routing flags:** every transaction (CC00, CAVW, CT02, …) and every job (POSTTRAN, INTCALC, …) has an independent legacy/new flag at the gateway/dispatcher.
- **Dual-run windows:** financial contexts (Phases 4–7) run new + legacy in parallel and reconcile before the flag flips.
- **Data direction:** early phases replicate legacy→new (legacy is SoR); at each context's cutover, the direction reverses (new is SoR, legacy view materialized for stragglers).
- **Definition of done per phase:** functional parity tests green, reconciliation report clean for the required window, rollback rehearsed, runbook + on-call updated.

## 5. Suggested Wave Grouping (for delivery planning)

| Wave | Phases | Theme | Dependency unlocked |
|:--|:--|:--|:--|
| A | 0–2 | Platform + identity + reference | Foundation for all consumers |
| B | 3–4 | Master data + accounts/cards | Removes the XREF file-coupling |
| C | 5–6 | Transactions + outputs + auth | Online channel fully modern |
| D | 7–8 | Financial batch core + decommission | Mainframe exit |

> Waves are dependency-ordered, not date-bound. Do not start Wave D's posting cutover until Account (Phase 4) exposes single-writer balance commands and the parity harness is proven on real cycle data.
