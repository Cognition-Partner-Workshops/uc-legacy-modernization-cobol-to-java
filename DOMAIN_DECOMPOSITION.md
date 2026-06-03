# CardDemo Domain Decomposition

> Bounded-context identification and extraction-seam analysis for the CardDemo
> mainframe system, used to drive the service decomposition of the Java target.

Companion to [`MODERNIZATION_BLUEPRINT.md`](./MODERNIZATION_BLUEPRINT.md),
[`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md), and
[`RISK_REGISTER.md`](./RISK_REGISTER.md).

This document applies Domain-Driven Design to the *as-is* COBOL. It (1) groups
programs and data into **bounded contexts**, (2) defines the **ubiquitous
language** per context, (3) maps the **relationships** between contexts (context
map), and (4) performs **extraction-seam analysis** — identifying where a context
can be cut out of the monolith, what coupling crosses the seam, and how to break
that coupling.

---

## 1. Method

The mainframe has no service boundaries — coupling is expressed through **shared
VSAM files (by record key)**, **shared copybooks (data contracts)**, and the
**CICS COMMAREA + XCTL** control flow. We therefore identify seams by tracing:

- **Data ownership**: which program *writes* each file → that context owns the data.
- **Data sharing**: which programs *read* a file they don't own → cross-context read coupling.
- **Control flow**: `EXEC CICS XCTL PROGRAM(CDEMO-TO-PROGRAM)` chains via `COCOM01Y` → UI orchestration coupling.
- **Existing interfaces**: MQ queues and DB2/IMS already expose service-like boundaries → natural seams.

---

## 2. Bounded contexts

Seven core contexts plus three supporting/generic contexts.

### Core contexts

#### BC-1 — Identity & Access
- **Programs**: `COSGN00C` (sign-on), `COUSR00C/01C/02C/03C` (user CRUD), `COADM01C` (admin menu/dispatch).
- **Owns (writes)**: `USRSEC` VSAM file (`CSUSR01Y`: user id, name, password, type A/U).
- **Ubiquitous language**: User, Credential, Role (Admin/User), Session, Sign-on.
- **Responsibility**: authenticate users, manage user accounts, authorize by role.
- **Notes**: only context that should know about credentials. Today every program reads `CDEMO-USER-TYPE` from COMMAREA to gate admin functions — that becomes a token claim.

#### BC-2 — Customer (party master data)
- **Programs**: `CBCUS01C` (read/print); customer data consumed widely via XREF.
- **Owns**: `CUSTDATA` VSAM (`CVCUS01Y`: identity, address, SSN, DOB, govt id, FICO, EFT account).
- **Ubiquitous language**: Customer, Cardholder, SSN, FICO score, Address.
- **Responsibility**: authoritative record of the person/party.
- **Notes**: carries PII; FICO feeds risk/credit decisions elsewhere.

#### BC-3 — Account
- **Programs**: `COACTVWC` (view), `COACTUPC` (update), `CBACT01C` (batch read/print).
- **Owns**: `ACCTDATA` VSAM (`CVACT01Y`: balance, credit limit, cash limit, cycle credit/debit, open/expiry/reissue dates, group id).
- **Ubiquitous language**: Account, Current Balance, Credit Limit, Cycle Credit/Debit, Disclosure Group.
- **Responsibility**: authoritative balance and credit terms. `ACCT-GROUP-ID` links to disclosure group → interest.
- **Notes**: the *money-bearing* master entity; updated by Transaction posting and Interest batch.

#### BC-4 — Card
- **Programs**: `COCRDLIC` (list), `COCRDSLC` (view), `COCRDUPC` (update), `CBACT02C` (card print), `CBACT03C` (xref print).
- **Owns**: `CARDDATA` VSAM (`CVACT02Y`: card num, acct id, CVV, embossed name, expiry, status) **and** `CARDXREF` (`CVACT03Y`: card→customer→account).
- **Ubiquitous language**: Card, PAN, CVV, Cross-Reference (XREF), Embossed Name.
- **Responsibility**: card lifecycle + the **identity resolution** join (card↔account↔customer).
- **Notes**: the XREF is a system-wide dependency (see seam analysis). Carries PAN/CVV (sensitive).

#### BC-5 — Transaction (ledger)
- **Programs**: online `COTRN00C` (list), `COTRN01C` (view), `COTRN02C` (add); batch `CBTRN01C`/`CBTRN02C` (post daily), `CBTRN03C` (report).
- **Owns**: `TRANSACT` VSAM KSDS + AIX (`CVTRA05Y`), `DALYTRAN` (`CVTRA06Y`), `DALYREJS` (rejects).
- **Ubiquitous language**: Transaction, Posting, Daily Transaction, Reject, Transaction Type/Category, Merchant.
- **Responsibility**: record and post financial transactions; the **ledger of record**.
- **Notes**: posting (`CBTRN02C`) is the core financial process; depends on Card (XREF), Account, and Reference Data.

#### BC-6 — Billing, Interest & Statements
- **Programs**: `CBACT04C` (interest), `COBIL00C` (bill payment), `CBSTM03A/B` (statements), `CORPT00C`/`CBTRN03C` (reports).
- **Owns**: `TCATBAL` (`CVTRA01Y`, category balances), statement output files; reads `DISCGRP` (`CVTRA02Y`).
- **Ubiquitous language**: Interest, Disclosure Group, Category Balance, Statement, Bill Payment, Cycle.
- **Responsibility**: compute interest/fees, produce statements, process payments.
- **Notes**: arithmetic-exact; sits downstream of Transaction + Account.

#### BC-7 — Authorization (real-time, optional module)
- **Programs**: `COPAUA0C` (decision via MQ), `COPAUS0C/1C/2C` (summary/detail/fraud), `CBPAUP0C` (purge expired).
- **Owns**: IMS DB pending-authorization DB (`DBPAUTP0`), DB2 authorization/fraud log; MQ request/response.
- **Ubiquitous language**: Authorization Request/Response, Pending Auth, Approval/Decline, Fraud, Expiry/Purge.
- **Responsibility**: approve/decline card auth in real time; flag fraud.
- **Notes**: **already service-shaped** via MQ — the cleanest extraction seam in the system.

### Supporting / generic contexts

#### BC-8 — Reference Data (Transaction Type & Category)
- **Programs**: `COTRTLIC`, `COTRTUPC`, `COBTUPDT` (DB2 module).
- **Owns**: DB2 `TRNTYPE`/`TRNTYCAT`; VSAM `TRANTYPE`/`TRANCATG` projections.
- **Language**: Transaction Type, Transaction Category.
- **Notes**: already relational; consumed by Transaction + Billing. Classic supporting context.

#### BC-9 — Data Integration & Migration
- **Programs**: `CBEXPORT`/`CBIMPORT` (branch migration, `CVEXPORT`), `COACCT01`/`CODATE01` (MQ account/date inquiry).
- **Responsibility**: bulk export/import and external inquiry APIs.
- **Notes**: partly a migration tool (retire post-cutover), partly an integration surface (becomes API).

#### BC-10 — Batch Orchestration & Platform (generic)
- **Programs/artifacts**: 38 JCL members, Control-M (`CardDemo.controlm`) + CA7 (`CardDemo.ca7`) definitions, `MVSWAIT`/`COBSWAIT` (timer), `CSUTLDTC`/`COBDATFT` (date), GDG management.
- **Responsibility**: schedule and sequence batch; shared technical utilities.
- **Notes**: generic subdomain — buy/adopt a workflow engine; re-implement utilities on JDK.

---

## 3. Context map (relationships)

DDD relationship patterns between contexts (`U` = upstream/provider, `D` =
downstream/consumer):

```
        Identity & Access (BC-1)
                 │ (provides identity/role to all UIs)
                 ▼
   ┌─────────────────────────────────────────────────────────┐
   │                                                           │
Customer (BC-2) ──U──► Card/XREF (BC-4) ──U──► Transaction (BC-5)
   │                       ▲                        │  │
   │ (FICO)                │ (resolves card→acct)   │  │ (posts to)
   ▼                       │                        │  ▼
Authorization (BC-7) ◄─────┘                        │ Account (BC-3)
   ▲  (checks acct/limit)                           │  ▲
   │                                                │  │ (interest updates balance)
   └────────────────────────────────────────────┐  ▼  │
                                       Billing/Interest/Stmt (BC-6)
                                                    ▲
   Reference Data (BC-8) ──U──► (Transaction, Billing consume types/categories)

   Batch Orchestration (BC-10) ── sequences ──► BC-5, BC-6 batch jobs
   Data Integration (BC-9) ── export/inquiry over ──► BC-2, BC-3
```

Key relationships and recommended patterns:

| Upstream (U) | Downstream (D) | Coupling today | Target pattern |
|:-------------|:---------------|:---------------|:---------------|
| Identity (BC-1) | all online UIs | `CDEMO-USER-TYPE` in COMMAREA | **Open Host Service** (JWT/OIDC); claims replace COMMAREA flags |
| Card/XREF (BC-4) | Transaction (BC-5), Auth (BC-7), Billing (BC-6) | direct VSAM read of `CARDXREF` by key | **Open Host Service** "resolve card → {account, customer}" API |
| Reference Data (BC-8) | Transaction (BC-5), Billing (BC-6) | shared DB2/VSAM lookups | **Published Language** (stable type/category codes) |
| Account (BC-3) | Transaction (BC-5), Billing (BC-6) | shared `ACCTDATA` read+rewrite | **Customer/Supplier** with explicit "post to account" command |
| Transaction (BC-5) | Billing/Interest (BC-6) | shared `TRANSACT`/`TCATBAL` files | **Customer/Supplier**; events ("TransactionPosted") |
| Legacy VSAM/DB2/IMS | every new service (transition) | direct file access | **Anti-Corruption Layer** (ACL) over legacy stores |

---

## 4. Extraction-seam analysis

For each context: the **seam** (where to cut), the **inbound/outbound coupling**
crossing it, and the **technique** to sever it cleanly.

### Seam types present in CardDemo
1. **MQ message boundary** — already an interface; cleanest seam (BC-7, parts of BC-9).
2. **CICS XCTL / COMMAREA** — UI control-flow seam; intercept at the menu/facade (BC-1 menus dispatch to all online contexts).
3. **VSAM shared-file** — data seam; the hardest. Must be broken with an ACL + CDC or shared-DB phase so OLD and NEW agree on state.
4. **JCL batch-step** — job-graph seam; intercept by replacing one step's program while keeping its dataset I/O contract.
5. **DB2 relational** — near-clean seam; tables migrate ~1:1 (BC-8).

### BC-1 Identity & Access
- **Seam**: the sign-on transaction (`CC00` → `COSGN00C`) and the menu dispatch tables in `COMEN01C`/`COADM01C`.
- **Inbound coupling**: every online transaction trusts `CDEMO-USER-ID`/`CDEMO-USER-TYPE` from COMMAREA.
- **Outbound coupling**: reads `USRSEC` only.
- **Sever**: introduce an auth service issuing JWTs; the strangler facade validates the token and injects identity. The COMMAREA fields are replaced by token claims, so downstream contexts stop depending on the sign-on program. **Cleanest first cut** — low data coupling, high control-flow leverage.

### BC-2 Customer
- **Seam**: `CUSTDATA` ownership; readers reach it via XREF (BC-4), not directly by customer key in most flows.
- **Inbound coupling**: Card/XREF resolves `XREF-CUST-ID`; Authorization and Statements read customer attributes.
- **Sever**: expose a Customer read API; back it with the ACL over `CUSTDATA` during transition, then migrate to the relational `customer` table. PII isolation happens here.

### BC-3 Account
- **Seam**: `ACCTDATA` read/rewrite. **Hard seam** — both online (`COACTUPC`) and batch (`CBTRN02C`, `CBACT04C`) write balances.
- **Inbound coupling**: Transaction posting and Interest both rewrite `ACCT-CURR-BAL`/cycle fields; Authorization reads limit.
- **Outbound coupling**: `ACCT-GROUP-ID` → disclosure group (BC-6/BC-8).
- **Sever**: define an explicit **"apply posting / apply interest to account"** command API so balance mutation has a single owner. Until posting (BC-5) is migrated, use a shared-DB/CDC phase so legacy batch and new service never double-update. **This is the seam that gates the whole migration** (see cutover plan dependency on it).

### BC-4 Card / XREF — the linchpin
- **Seam**: `CARDDATA` + `CARDXREF`. The **XREF is read by nearly every financial flow** to resolve a card number to an account/customer (e.g., `CBTRN02C` reads `XREF-FILE` by `FD-XREF-CARD-NUM` before posting).
- **Inbound coupling**: Transaction posting, Authorization, online card screens, statements.
- **Sever**: extract a **"resolve card → {account, customer}" Open Host Service** *first* and route all resolution through it (even legacy, via a thin shim if feasible) so the rest of the decomposition has a stable identity-resolution contract. Until then the XREF is an invisible monolith-wide join that blocks clean cuts.

### BC-5 Transaction — core, hardest data seam
- **Seam**: `TRANSACT` KSDS (+AIX), `DALYTRAN`/`DALYREJS`, and the posting batch `CBTRN02C`.
- **Inbound coupling**: online add (`COTRN02C`) writes; batch posts; Billing/Interest read.
- **Outbound coupling**: posting reads XREF (BC-4), Account (BC-3), category balance (BC-6/`TCATBAL`), Reference Data (BC-8).
- **Sever**: this context can only be cut **after** its upstream dependencies (BC-4 resolution, BC-8 reference, BC-3 account command) have stable APIs. Strategy: emit a domain event `TransactionPosted` and migrate consumers to it; run new posting in **shadow** against legacy for parity before flipping. Migrates **late**.

### BC-6 Billing, Interest & Statements
- **Seam**: `TCATBAL`/`DISCGRP` ownership + `INTCALC` (`CBACT04C`) and `CREASTMT` (`CBSTM03A`) JCL steps.
- **Inbound coupling**: consumes Transaction ledger + Account balances + Reference Data.
- **Sever**: statements/reports are **read-only** → extract first as a shadow consumer of the (legacy) ledger; interest computation is a **batch-step seam** (replace `CBACT04C` behind the same dataset contract) and must preserve arithmetic exactly.

### BC-7 Authorization (optional)
- **Seam**: MQ request/response queues (`COPAUA0C`) — **already a service interface**.
- **Inbound coupling**: message contract only (plus IMS/DB2 persistence internally).
- **Sever**: stand up a new auth service behind the *same* MQ contract; swap persistence from IMS/DB2 to the target store internally. Lowest-coupling extraction; can be piloted independently of the core path.

### BC-8 Reference Data
- **Seam**: DB2 `TRNTYPE`/`TRNTYCAT` tables (and their VSAM projections).
- **Coupling**: read-mostly lookups from BC-5/BC-6.
- **Sever**: migrate tables ~1:1 to target RDBMS; expose as a small reference service / published code list. Near-clean seam → migrate **early** so consumers can point at the new source.

### BC-9 Data Integration & Migration
- **Seam**: `CBEXPORT`/`CBIMPORT` datasets (`CVEXPORT`) and MQ inquiry (`COACCT01`/`CODATE01`).
- **Sever**: repurpose export/import as the **bulk data-migration tool** during cutover (then retire); replace MQ inquiries with REST, optionally keeping an MQ adapter for external callers.

### BC-10 Batch Orchestration & Platform
- **Seam**: the Control-M/CA7 job graph (`INCOND`/`OUTCOND` dependencies) and per-step program invocation.
- **Sever**: re-express the dependency graph in a modern workflow engine; migrate **one job step at a time** by replacing the step's program while preserving its input/output dataset contract (the batch-step seam). Utilities (date/timer) become shared JDK libs adopted by all contexts up front.

---

## 5. Decomposition summary & extraction order

Ordering driven by coupling (cut low-coupling/leaf seams first, the money-bearing
shared-data seams last):

| Order | Context | Seam type | Coupling to sever | Risk |
|:-----:|:--------|:----------|:------------------|:----:|
| 1 | BC-10 Utilities (date/timer) | library | none (leaf) | Low |
| 2 | BC-1 Identity & Access | COMMAREA/menu + facade | trust flags → JWT claims | Low |
| 3 | BC-8 Reference Data | DB2 relational | lookups | Low |
| 4 | BC-2 Customer | VSAM read (via ACL) | XREF reads | Low-Med |
| 5 | BC-4 Card/XREF | Open Host Service (resolution) | system-wide XREF join | **Med-High** |
| 6 | BC-3 Account | balance command API + shared-DB | dual writers (online+batch) | **High** |
| 7 | BC-6 Statements/Reports (read-only) | shadow consumer | read coupling only | Low-Med |
| 8 | BC-5 Transaction (online add/view) | API | write to ledger | Med |
| 9 | BC-6 Interest (`CBACT04C`) | batch-step | arithmetic fidelity | **High** |
| 10 | BC-5 Transaction posting (`CBTRN02C`) | batch-step + events | core money movement | **Highest** |
| 11 | BC-10 Orchestration | workflow engine | job graph | Med |
| 12 | BC-7 Authorization | MQ contract (already a seam) | message only | Med (isolated) |
| 13 | BC-9 Integration/Migration | retire tool / API | n/a | Low |

This order is the input to the phased sequence in
[`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md).
