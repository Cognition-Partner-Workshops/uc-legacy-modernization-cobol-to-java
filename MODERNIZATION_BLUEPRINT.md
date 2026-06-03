# CardDemo Modernization Blueprint

> Strategy evaluation for migrating the CardDemo mainframe credit card management
> system (COBOL / CICS / VSAM / JCL, with optional DB2, IMS DB, and MQ modules)
> to a modern Java platform.

This document inventories the system's functional areas as they exist in source
today, then evaluates four modernization strategies — **Strangler Fig**,
**Replatform**, **Refactor**, and **Rewrite** — for each area, and recommends a
path. It is the strategic companion to:

- [`DOMAIN_DECOMPOSITION.md`](./DOMAIN_DECOMPOSITION.md) — bounded contexts and extraction seams
- [`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md) — phased, risk-ordered migration sequence
- [`RISK_REGISTER.md`](./RISK_REGISTER.md) — top risks and mitigations

---

## 1. Definitions of the four strategies

| Strategy | What it means here | When it wins |
|:---------|:-------------------|:-------------|
| **Strangler Fig** | Stand up a new Java service alongside the mainframe, route a slice of traffic/function to it, and incrementally grow the new system until the old one is fully encircled and retired. | When the area has clean seams (a screen, a transaction, a batch step) that can be intercepted and redirected one at a time. |
| **Replatform** ("lift & shift / rehost") | Recompile/rehost the *same* COBOL on a non-mainframe runtime (e.g., AWS Mainframe Modernization / Micro Focus / UniKix TPE — note `samples/m2/unikix/` already exists) with minimal code change. | When code is correct and stable, the goal is to exit the mainframe fast, and rewriting first is too risky. A holding state, not an end state. |
| **Refactor** ("automated transformation") | Mechanically transpile COBOL → Java preserving structure (e.g., program → class, paragraph → method, copybook → POJO/record), then iteratively clean up. | When business logic is valuable and intricate, must be preserved bit-for-bit, but the language/runtime must change. |
| **Rewrite** | Re-implement the capability from its specification on a modern stack (Spring Boot, relational DB, REST/event APIs), discarding the COBOL. | When the COBOL encodes simple or well-understood logic, or the existing design is a liability (terminal-bound flow, flat-file coupling). |

These are not mutually exclusive. The recommended program-level approach is a
**Strangler Fig umbrella** in which each functional area is migrated using the
per-area tactic (replatform / refactor / rewrite) best suited to it.

---

## 2. System snapshot (what we are modernizing)

CardDemo is the AWS "CardDemo" reference mainframe application. Source inventory
in this repo:

- **31 COBOL programs** in `app/cbl/` (online `COxxxxxC` + batch `CBxxxxxC`), plus optional-module programs under `app/app-*`.
- **~30 copybooks** in `app/cpy/` defining the data contracts (record layouts).
- **17 BMS maps** in `app/bms/` (3270 green-screen UI).
- **38 JCL members** in `app/jcl/` orchestrated by **Control-M** and **CA7** schedulers (`app/scheduler/`).
- **2 Assembler** routines (`app/asm/`): `MVSWAIT` (batch timer), `COBDATFT` (date format).
- **Data**: VSAM KSDS files (account, card, customer, xref, transaction, user security, category balance, reference data); optional **DB2** (transaction types, authorization fraud log), **IMS DB** (pending authorizations), and **IBM MQ** (authorization + account-extract request/response).

Architecturally significant facts that shape every strategy decision:

1. **Pseudo-conversational CICS with a shared COMMAREA.** Online navigation is
   driven by `COCOM01Y` (`CARDDEMO-COMMAREA`), where each program sets
   `CDEMO-TO-PROGRAM` and issues `EXEC CICS XCTL`. The menu programs
   (`COMEN01C`, `COADM01C`) dispatch by option into a program-name table. This is
   the central UI control-flow seam.
2. **VSAM is the integration bus.** Online and batch programs share the same
   indexed files by record key. The transaction posting batch (`CBTRN02C`)
   randomly reads `XREF`, `ACCOUNT`, `TCATBAL` and rewrites balances — there is no
   service boundary, only file coupling.
3. **Batch is the system of record for money movement.** `POSTTRAN` (`CBTRN02C`)
   posts daily transactions, `INTCALC` (`CBACT04C`) computes interest,
   `CREASTMT` (`CBSTM03A`) produces statements. Online screens mostly *view* and
   *stage*; nightly batch *commits* financial state.
4. **Numeric/encoding fidelity is non-negotiable.** Money fields are
   `PIC S9(10)V99` packed/zoned decimals; data on disk is EBCDIC
   (`app/data/EBCDIC/`). Any target must reproduce COBOL rounding and
   fixed-point arithmetic exactly.

---

## 3. Functional areas

The system decomposes into these functional areas (mapped to programs/data):

| # | Functional area | Key programs | Primary data | Channel |
|:--|:----------------|:-------------|:-------------|:--------|
| A | **Security & Sign-on** | `COSGN00C` | `USRSEC` (VSAM), `CSUSR01Y` | Online |
| B | **User Administration** | `COUSR00C/01C/02C/03C`, `COADM01C` | `USRSEC` | Online |
| C | **Customer Management** | `CBCUS01C` | `CUSTDATA` (VSAM), `CVCUS01Y` | Batch (+view) |
| D | **Account Management** | `COACTVWC`, `COACTUPC`, `CBACT01C` | `ACCTDATA`, `CVACT01Y` | Online + batch |
| E | **Card Management** | `COCRDLIC`, `COCRDSLC`, `COCRDUPC`, `CBACT02C`, `CBACT03C` | `CARDDATA`, `CARDXREF`, `CVACT02Y/03Y` | Online + batch |
| F | **Transaction Management** | `COTRN00C/01C/02C`, `CBTRN01C/02C` | `TRANSACT` (VSAM KSDS + AIX), `DALYTRAN`, `DALYREJS`, `CVTRA05Y/06Y` | Online + batch |
| G | **Interest & Fees** | `CBACT04C` | `TCATBAL`, `DISCGRP`, `CVTRA01Y/02Y` | Batch |
| H | **Bill Payment** | `COBIL00C` | `ACCTDATA`, `TRANSACT` | Online |
| I | **Statements & Reporting** | `CBSTM03A/B`, `CBTRN03C`, `CORPT00C` + `TXT2PDF`/`FTP` JCL | `TRANSACT`, statement files | Batch (submitted from online) |
| J | **Reference Data (Tran Type/Category)** | `COTRTLIC`, `COTRTUPC`, `COBTUPDT` (DB2 module) | DB2 `TRNTYPE`/`TRNTYCAT`, VSAM `TRANTYPE`/`TRANCATG` | Online + batch |
| K | **Authorization (real-time)** | `COPAUA0C`, `COPAUS0C/1C/2C`, `CBPAUP0C` | IMS DB pending-auth, DB2 fraud, MQ | Online + MQ + batch |
| L | **Account Extract via MQ** | `COACCT01`, `CODATE01` | `ACCTDATA`, MQ | Online/MQ |
| M | **Branch Data Migration** | `CBEXPORT`, `CBIMPORT` | `CVEXPORT` consolidated record | Batch |
| N | **Batch Orchestration** | 38 JCL members + Control-M / CA7 | All datasets, GDGs | Infra |
| O | **Shared Utilities** | `CSUTLDTC`, `COBDATFT` (asm), `MVSWAIT`/`COBSWAIT` | date/lookup copybooks | Cross-cutting |

---

## 4. Per-area strategy evaluation

For each area the table scores the four strategies (✅ strong fit, ⚠️ viable with
caveats, ❌ poor fit) and gives a recommendation with rationale.

### A. Security & Sign-on (`COSGN00C`, `USRSEC`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ⚠️ | ❌ | ✅ |

**Recommendation: Rewrite, first, as the entry seam.** Sign-on logic is thin
(lookup user in `USRSEC`, compare plaintext password, set `CDEMO-USER-TYPE`). It
is also a *security liability* — plaintext passwords (`PASSWORD` default in
README) and RACF-simulation must not be carried forward. Rewrite as a modern
auth service (OIDC/JWT, hashed credentials, roles `ADMIN`/`USER`). This area is
the natural first strangler insertion point: a new front door can issue tokens
the rest of the system trusts during the transition.

### B. User Administration (`COUSR0x`, `COADM01C`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ⚠️ | ⚠️ | ✅ |

**Recommendation: Rewrite.** Standard CRUD over `USRSEC`. No financial logic, no
downstream coupling beyond auth. Pairs naturally with area A as the first
vertical slice delivered through the strangler facade. The admin menu dispatch
(`COADM01C` program-name table) becomes routing config in the new UI.

### C. Customer Management (`CBCUS01C`, `CVCUS01Y`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ⚠️ | ✅ |

**Recommendation: Rewrite the data model + read/write API; replatform the batch
loader interim.** Customer is a stable master-data entity (`CVCUS01Y`: identity,
address, SSN, FICO). The `CVCUS01Y` layout maps cleanly to a relational
`customer` table and a CRUD service. Carries **PII (SSN, DOB, govt ID)** — the
rewrite is the moment to introduce encryption-at-rest and access controls absent
in the flat file.

### D. Account Management (`COACTVWC`, `COACTUPC`, `CBACT01C`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ✅ | ⚠️ |

**Recommendation: Refactor (transpile) the balance-bearing logic, rewrite the
screens.** `COACTUPC` contains non-trivial field-level validation and
cross-field edits that are expensive to re-specify and risky to re-derive. Keep
that logic via automated COBOL→Java transformation; replace the BMS screens
(`COACTVW`, `COACTUP`) with a REST API + modern UI. Account holds the
authoritative balance (`ACCT-CURR-BAL`, credit limits) so fidelity matters —
preservation via refactor de-risks it.

### E. Card Management (`COCRDLIC/SLC/UPC`, `CBACT02C/03C`, XREF)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ⚠️ | ✅ |

**Recommendation: Rewrite, but preserve the XREF relationship explicitly.** Card
data (`CVACT02Y`) and the customer↔account↔card cross-reference (`CVACT03Y`) are
structurally simple. The hidden complexity is the **XREF join** that nearly every
other program relies on to resolve a card number to an account/customer. In the
rewrite this becomes foreign keys / an index — but it must be modeled first
because Transaction posting depends on it. Card numbers and CVV (`CARD-CVV-CD`)
are sensitive → tokenize/encrypt.

### F. Transaction Management (`COTRN0x`, `CBTRN01C/02C`) — **core**
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ⚠️ | ✅ | ✅ | ❌ |

**Recommendation: Refactor (transpile) the posting engine; rewrite the online
list/view/add screens later.** `CBTRN02C` is the financial heart: it validates
each daily transaction, resolves card→account via XREF, posts to `TRANSACT`,
updates category balances (`TCATBAL`), and writes rejects (`DALYREJS`). A clean
rewrite risks subtle posting/rounding divergence. Transpile to preserve behavior,
wrap it in parity tests, and only then consider re-architecting to an
event-driven posting service. **This is the highest-risk area — it migrates late
(see cutover plan).**

### G. Interest & Fees (`CBACT04C`, `DISCGRP`, `TCATBAL`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ⚠️ | ✅ | ✅ | ⚠️ |

**Recommendation: Refactor (transpile).** Interest computation is rules- and
arithmetic-heavy (disclosure-group rates × category balances, fixed-point
rounding). Exact-match financial output is mandatory; preserve via transformation
and lock with golden-file parity tests against legacy `INTCALC` output.

### H. Bill Payment (`COBIL00C`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ⚠️ | ✅ |

**Recommendation: Rewrite as a command on the Transaction/Account services.**
Bill payment is "pay balance in full" → it creates a transaction and adjusts the
account. Once Account (D) and Transaction (F) seams exist, this is a thin
orchestration that is cleaner rewritten than transpiled.

### I. Statements & Reporting (`CBSTM03A/B`, `CBTRN03C`, `CORPT00C`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ⚠️ | ✅ |

**Recommendation: Rewrite using a reporting/templating stack.** Output formatting
(plain text + `TXT2PDF` + FTP delivery) is presentation, not core logic. Rebuild
as queries over the new transaction store rendered to PDF/HTML. Reporting is
read-only and side-effect-free, making it a safe, high-visibility early win that
can run in **parallel (shadow)** against legacy statements for verification.

### J. Reference Data — Tran Type/Category (DB2 module)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ✅ | ✅ | ✅ |

**Recommendation: Rewrite (CRUD over relational tables).** This optional module
is *already* relational (DB2 `TRNTYPE`/`TRNTYCAT` with cursors in `COTRTLIC`).
The data migrates almost 1:1 to the target RDBMS; the screens become a simple
admin CRUD. Low risk, and it provides the reference tables that Transaction and
Interest depend on — so it migrates early.

### K. Authorization — real-time (IMS/DB2/MQ)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ⚠️ | ⚠️ | ✅ |

**Recommendation: Rewrite as an event-driven authorization service.**
`COPAUA0C` is MQ request/response, `COPAUS0C/1C/2C` view/mark-fraud, `CBPAUP0C`
purges expired auths. The MQ boundary is *already a service interface* — ideal
strangler seam. But the persistence is IMS DB (hierarchical) + DB2, which should
not be carried forward; rewrite onto the target RDBMS/streaming platform behind
the same message contract. Optional module → can be deferred or piloted
independently.

### L. Account Extract via MQ (`COACCT01`, `CODATE01`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ✅ | ⚠️ | ❌ | ✅ |

**Recommendation: Rewrite as an API/integration endpoint.** These are
request/response demos (system date, account details over MQ). Replace with
REST/gRPC (or keep an MQ adapter for external callers during transition). Trivial
logic; rewrite.

### M. Branch Data Migration (`CBEXPORT`, `CBIMPORT`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ⚠️ | ✅ | ✅ | ⚠️ |

**Recommendation: Replatform/retain transitionally, then retire.** Export/import
of a consolidated customer record (`CVEXPORT`) is a *migration utility itself*.
It can serve as a bulk data-movement tool during cutover, then be decommissioned
once both sides share a database. Don't over-invest in modernizing a tool whose
job ends at cutover.

### N. Batch Orchestration (JCL + Control-M / CA7)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| ❌ | ✅ | ❌ | ✅ |

**Recommendation: Rewrite the orchestration, replatform JCL transitionally.** The
JCL job graph (dependencies encoded in Control-M `INCOND`/`OUTCOND`) must be
re-expressed in a modern scheduler/workflow engine (e.g., AWS Step Functions,
Airflow, control-plane of choice). During transition, rehosted JCL can run via
the M2/UniKix runtime. The dependency graph is valuable; the JCL syntax is not.

### O. Shared Utilities (`CSUTLDTC`, `COBDATFT`, `MVSWAIT`/`COBSWAIT`)
| Strangler | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| n/a | ⚠️ | ⚠️ | ✅ |

**Recommendation: Rewrite using platform-native libraries.** Date conversion and
timer/wait routines (one is Assembler, `MVSWAIT`) map to standard JDK facilities
(`java.time`, scheduler delays). Do not transpile Assembler — re-implement.
Establish these shared libs **first** so transpiled/rewritten areas depend on
modern equivalents from day one.

---

## 5. Program-level recommendation

**Adopt a Strangler Fig program, executed bottom-up by dependency, choosing per
area between rewrite (simple/liability code) and automated refactor/transpile
(financially exact, hard-to-respecify code), with selective replatform as a
transitional bridge.**

```
                 Strangler Facade (API gateway + auth)
                 routes each capability to OLD or NEW
   ┌───────────────────────────────────────────────────────────┐
   │  REWRITE                 REFACTOR/TRANSPILE     REPLATFORM  │
   │  ────────                ─────────────────      (bridge)    │
   │  Security/Sign-on (A)    Account logic (D)      JCL via M2  │
   │  User Admin (B)          Txn posting (F) ◄core  Branch I/O  │
   │  Customer (C)            Interest/Fees (G)      (M, transitional)
   │  Card (E)                                                    │
   │  Bill Pay (H)            preserved bit-for-bit              │
   │  Statements/Report (I)   with golden parity tests          │
   │  Reference Data (J)                                          │
   │  Authorization (K)                                           │
   │  Account Extract (L)                                         │
   │  Orchestration (N)                                           │
   │  Utilities (O)                                               │
   └───────────────────────────────────────────────────────────┘
                 Anti-Corruption Layer over VSAM/DB2/IMS
                 (shared data accessible to OLD + NEW)
```

### Why not a single strategy for everything?

- **Why not pure Replatform?** It exits the mainframe but freezes the
  architecture: still COBOL, still VSAM coupling, still 3270 flows. Good as a
  *bridge* (and the repo already ships `samples/m2/unikix/` for it), not as the
  destination. Use it only to buy time for high-risk areas (F, G) and to keep
  batch (N) running mid-migration.
- **Why not pure Rewrite (big bang)?** The posting/interest engines (F, G) encode
  financial behavior whose exact reproduction is the whole game; a from-scratch
  rewrite maximizes the chance of subtle monetary divergence and forces an
  all-or-nothing cutover. Too much risk concentrated at one moment.
- **Why not pure Refactor (transpile everything)?** Transpiling carries forward
  the COMMAREA/XCTL control flow, EBCDIC/flat-file assumptions, and security
  anti-patterns (plaintext passwords). You'd end up with "COBOL written in Java."
  Reserve it for the areas where behavioral fidelity outweighs design quality.

### Sequencing principle

Migrate **leaf dependencies first, money-movement last**: shared utilities (O) →
auth/users (A,B) → reference & master data (J,C,D,E) → read-only reporting (I) →
online transaction screens (F-online) → **posting/interest engines (F-batch, G)**
→ orchestration (N) → optional modules (K,L) → retire bridge & utilities (M).
Detailed ordering, gates, and rollback in [`CUTOVER_PLAN.md`](./CUTOVER_PLAN.md).

---

## 6. Cross-cutting decisions (apply to all areas)

| Concern | Decision |
|:--------|:---------|
| **Data fidelity** | Centralize EBCDIC→ASCII + packed/zoned-decimal conversion in the Anti-Corruption Layer; represent money as fixed-scale decimal (`BigDecimal` scale 2), never floating point. |
| **Parity verification** | Every refactored/rewritten area ships with golden-file / reconciliation tests comparing new output to legacy output on identical inputs (mirrors the SAS→Databricks "reconciliation report" pattern used elsewhere in this org). |
| **Strangler routing** | A facade (API gateway) owns the OLD-vs-NEW decision per capability and per key range, enabling incremental cutover and instant rollback. |
| **Shared data access** | An Anti-Corruption Layer exposes VSAM/DB2/IMS to both stacks during transition (CDC or shared-DB), so OLD and NEW never diverge on state. |
| **Security uplift** | Replace plaintext `USRSEC` passwords and RACF simulation with hashed credentials + OIDC/JWT during area A; encrypt PII (SSN, DOB) and PAN/CVV during C/E. |
| **Reference data first** | Tran types/categories/disclosure groups (J) and the XREF relationship (E) are prerequisites for F and G; migrate them before the engines that consume them. |
