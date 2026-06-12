# CardDemo Modernization Blueprint

> Target migration: **COBOL/CICS/VSAM mainframe → Java (Spring Boot) on a distributed/cloud platform.**
> This document evaluates four modernization strategies — **Strangler**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the CardDemo application, and recommends a per-area strategy.

## 1. System at a Glance

CardDemo is a credit-card management system delivered as two execution surfaces over a shared VSAM data store:

- **Online (CICS)** — ~25 pseudo-conversational COBOL programs driven by BMS 3270 maps, navigated via `EXEC CICS XCTL` and a shared `COMMAREA` (`COCOM01Y`). Entry point is the signon transaction `CC00` (`COSGN00C`).
- **Batch (JCL)** — ~38 jobs orchestrated by Control-M / CA-7 (`app/scheduler/CardDemo.controlm`, `CardDemo.ca7`). Posting (`CBTRN02C`), interest (`CBACT04C`), statements (`CBSTM03A`) and reporting (`CBTRN03C`) run as a nightly stream.
- **Optional modules** — Authorizations (IMS DB + DB2 + IBM MQ), Transaction-Type management (DB2), and account/date inquiry over MQ.

### Source inventory (counts)

| Artifact | Count | Location |
|:--|--:|:--|
| COBOL programs (`.cbl`/`.CBL`) | 39 | `app/cbl`, `app/app-*/cbl` |
| Copybooks (`.cpy`) | 41 | `app/cpy` |
| JCL jobs | 38 | `app/jcl`, `app/app-*/jcl` |
| BMS maps | 21 | `app/bms` |
| DB2 DDL/DCL | 9 | `app/app-*/ddl`, `app/app-*/dcl` |

### Persistence model (primary VSAM stores)

| Store | Copybook | RECLN | Key | Owning area |
|:--|:--|--:|:--|:--|
| Account master (`ACCTDAT`) | `CVACT01Y` | 300 | `ACCT-ID 9(11)` | Account |
| Card master (`CARDDAT`) | `CVACT02Y` | 150 | `CARD-NUM X(16)` | Card |
| Card cross-ref (`CARDXREF`) | `CVACT03Y` | 50 | `XREF-CARD-NUM` (+ AIX on acct/cust) | Card / integration seam |
| Customer master (`CUSTDAT`) | `CVCUS01Y` | 500 | `CUST-ID 9(09)` | Customer |
| Transaction master (`TRANSACT`) | `CVTRA05Y` | 350 | `TRAN-ID X(16)` | Transaction |
| Daily transaction (`DALYTRAN`) | `CVTRA06Y` | 350 | `DALYTRAN-ID` | Transaction (intake) |
| Category balances (`TCATBALF`) | `CVTRA01Y` | 50 | acct+type+cat | Billing/Interest |
| Disclosure group (`DISCGRP`) | `CVTRA02Y` | 50 | group+type+cat | Reference (rates) |
| Transaction type (`TRANTYPE`) | `CVTRA03Y` | 60 | `TRAN-TYPE X(02)` | Reference |
| Transaction category (`TRANCATG`) | `CVTRA04Y` | 60 | type+cat | Reference |
| User security (`USRSEC`) | `CSUSR01Y` | 80 | `SEC-USR-ID X(08)` | Security |

> **Critical structural fact:** Programs integrate *through shared files*, not through APIs. The `CARDXREF` file is the join that links Card → Customer → Account. Any decomposition must replace this implicit, file-level coupling with explicit contracts.

## 2. Strategy Definitions (as applied here)

| Strategy | Meaning in this codebase |
|:--|:--|
| **Strangler** | Stand up a façade/router in front of the mainframe; incrementally redirect individual transactions/jobs to new Java services while the legacy system keeps running. Data is bridged (CDC / dual-write) until each slice is cut over. |
| **Replatform** | Recompile/rehost COBOL largely as-is onto a distributed runtime (AWS Mainframe Modernization / UniKix — see `samples/m2/unikix`), VSAM → emulated files or RDBMS, CICS → emulated TP monitor. Behavior preserved, language unchanged. |
| **Refactor** | Automated/assisted COBOL→Java transpilation, then human cleanup into idiomatic Spring services. Logic preserved but expressed in Java with a relational schema. |
| **Rewrite** | Re-implement the functional area from business requirements in modern Java/Spring, discarding the COBOL implementation. Highest fidelity risk, highest modernization payoff. |

## 3. Per-Area Strategy Evaluation

Each area is scored on the legacy traits that drive strategy choice. Recommendation is **bolded**.

### 3.1 Security & Sign-on
**Programs:** `COSGN00C` (CC00), `COUSR00C`–`COUSR03C` (CU00–03), `COADM01C`, `COMEN01C` · **Data:** `USRSEC` (`CSUSR01Y`)

- Logic is thin: read `USRSEC`, compare `SEC-USR-PWD` (plaintext `PIC X(08)`), branch by `SEC-USR-TYPE` (`U`/`A`).
- This is a security *liability* (8-char plaintext passwords, no hashing, no lockout) and should not be preserved.

| Strangler | Replatform | Refactor | **Rewrite** |
|:--|:--|:--|:--|
| Useful as the first seam (auth gateway in front of legacy) | Carries the plaintext-password flaw forward | Transpiling weak auth is wasted effort | **Replace with Spring Security + OIDC, bcrypt, RBAC** |

**Recommendation: Rewrite**, delivered early as a shared auth service / gateway that also becomes the strangler front door. Migrate `USRSEC` users into an identity store with forced password reset.

### 3.2 Reference Data (Transaction Types, Categories, Disclosure Groups)
**Programs:** `COTRTLIC`/`COTRTUPC`/`COBTUPDT` (DB2 module) · **Data:** `TRANTYPE`, `TRANCATG`, `DISCGRP`

> Note: `TCATBALF` (category balances, `CVTRA01Y`) is **not** reference data — it is owned by Billing/Interest/Posting (see §3.7 and `DOMAIN_DECOMPOSITION.md` BC-7). It is read/written only by `CBTRN02C` and `CBACT04C`.

- Already partly relational (DB2 DDL exists: `TRNTYPE.ddl`, `TRNTYCAT.ddl`). Low business logic, high fan-in (read by posting, interest, reporting).
- Small, stable, self-contained — ideal "first real slice."

| Strangler | Replatform | **Refactor** | Rewrite |
|:--|:--|:--|:--|
| Good first strangled service | Unnecessary; little CICS to emulate | **CRUD + reference tables map cleanly to JPA entities** | Overkill for simple CRUD |

**Recommendation: Refactor → Rewrite** as a small Spring CRUD service backed by relational tables. Because so many components read it, expose it as a versioned API + cached read model early.

### 3.3 Customer
**Programs:** `CBCUS01C` (batch print) · **Data:** `CUSTDAT` (`CVCUS01Y`, 500B incl. SSN, DOB, FICO)

- Almost pure data with minimal behavior. Contains PII/regulated fields (SSN `9(09)`, govt id, FICO).

| Strangler | Replatform | **Refactor** | Rewrite |
|:--|:--|:--|:--|
| Works as a master-data service behind the façade | Low value | **Map record → entity, add validation & encryption at rest** | Few rules to recover |

**Recommendation: Refactor** into a Customer master-data service; add field-level encryption/tokenization for SSN and governance for PII during migration.

### 3.4 Account
**Programs:** `COACTVWC` (CAVW), `COACTUPC` (CAUP — **4,236 lines, largest program**), `CBACT01C` · **Data:** `ACCTDAT` (`CVACT01Y`)

- Account holds money state (`ACCT-CURR-BAL`, credit limits, cycle credit/debit). `COACTUPC` concentrates heavy field-level edit/validation logic.
- High business value, complex validation worth preserving exactly.

| Strangler | Replatform | **Refactor (assisted) → Rewrite validation** | Rewrite |
|:--|:--|:--|:--|
| Core slice to strangle after reference/customer | Defers risk, no modernization | **Transpile to recover edit rules, then re-express as domain model** | Full rewrite high-risk due to 4k-line validation surface |

**Recommendation: Refactor with assisted transpilation** to faithfully recover the `COACTUPC` validation rules, then incrementally rewrite them into a clean Account domain model with explicit invariants. Treat balance fields as money (`BigDecimal`, scale 2) mapped from `S9(10)V99`.

### 3.5 Card
**Programs:** `COCRDLIC` (CCLI), `COCRDSLC` (CCDL), `COCRDUPC` (CCUP), `CBACT02C`/`CBACT03C` · **Data:** `CARDDAT`, `CARDXREF`

- Card update has meaningful validation; the **`CARDXREF` file is the system's central join** (card↔customer↔account).
- Owns the integration seam everyone else depends on.

| Strangler | Replatform | **Refactor** | Rewrite |
|:--|:--|:--|:--|
| Cut over with Account as a paired slice | Low value | **Recover card rules; replace XREF file with FK relationships/API** | Card rules modest but XREF dependency raises rewrite risk |

**Recommendation: Refactor**, paired with Account. The deliverable is replacing the `CARDXREF` flat-file join with explicit relational keys and a lookup API so downstream consumers stop reading the file directly.

### 3.6 Transaction Capture & Inquiry (online)
**Programs:** `COTRN00C`/`COTRN01C`/`COTRN02C` (CT00–02) · **Data:** `TRANSACT`, `DALYTRAN`

- Add/list/view transactions; moderate validation. Feeds the batch posting pipeline via `DALYTRAN`.

| **Strangler** | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| **Natural slice: redirect CT00/01/02 to a new Transaction service writing to new store + bridge to DALYTRAN** | Defers | Viable but online UX should be rebuilt anyway | Behavior simple enough to rewrite the API |

**Recommendation: Strangler + Rewrite of the API**, keeping a bridge that still emits `DALYTRAN`-shaped records so the legacy batch posting continues to work until that pipeline is migrated.

### 3.7 Posting & Interest (batch core)
**Programs:** `CBTRN02C` (POSTTRAN), `CBTRN01C`, `CBACT04C` (INTCALC) · **Data:** reads/writes `ACCTDAT`, `TCATBALF`, `TRANSACT`, `DISCGRP`, `XREF`

- This is the **financial heart**: posting updates balances and category balances; interest calc applies `DISCGRP` rates. Highest correctness sensitivity; both jobs mutate account balances → ordering/consistency matters.

| Strangler | Replatform | **Refactor (assisted) with parity harness** | Rewrite |
|:--|:--|:--|:--|
| Hard to strangle a tightly-coupled batch chain mid-stream | Buys time but no payoff | **Transpile, wrap in golden-master parity tests, then modularize** | Rewrite risk to money math is severe |

**Recommendation: Refactor under a strict parity harness.** Migrate last (highest risk). Run new vs. legacy in parallel and reconcile to the cent before cutover. Convert COMP-3/`S9(n)V99` arithmetic with explicit rounding rules verified against legacy output.

### 3.8 Billing, Statements & Reporting
**Programs:** `COBIL00C` (CB00), `CORPT00C` (CR00), `CBTRN03C` (TRANREPT), `CBSTM03A`/`CBSTM03B` (CREASTMT, emits HTML) · **Data:** `TRANSACT`, `TCATBALF`, account/customer

- Output-oriented (statements, reports). `CBSTM03A` already produces HTML — a hint that presentation can be modernized cleanly.

| Strangler | Replatform | Refactor | **Rewrite** |
|:--|:--|:--|:--|
| Can run new reporting off the migrated data store | Low value | Viable for statement math | **Rebuild reporting/statements on modern reporting stack once data is migrated** |

**Recommendation: Rewrite** on a modern reporting/templating stack, consuming the migrated relational data. Bill-payment *posting* logic should reuse the migrated posting service.

### 3.9 Authorizations (optional: IMS + DB2 + MQ)
**Programs:** `COPAUA0C`, `COPAUS0C`/`1C`/`2C`, `CBPAUP0C`, `PAUDBLOD`/`PAUDBUNL`, `DBUNLDGS` · **Tech:** IMS DB, DB2 (`AUTHFRDS`), IBM MQ

- Most technically diverse area (hierarchical IMS + relational DB2 + async MQ). Real-time request/response and fraud table.

| **Strangler** | Replatform | Refactor | Rewrite |
|:--|:--|:--|:--|
| **MQ request/response is already an integration seam — front it with a new auth service and swap the consumer** | IMS/MQ emulation is costly and complex | IMS hierarchical → relational mapping is non-trivial to transpile | Rewrite the service but reuse fraud-rule semantics |

**Recommendation: Strangler + Rewrite**, exploiting the existing MQ boundary as a ready-made seam. Replace IMS access with a relational/document model; preserve the fraud-rule semantics from `AUTHFRDS`.

### 3.10 Account/Date Inquiry over MQ
**Programs:** `COACCT01` (CDRA), `CODATE01` (CDRD)

- Thin request/response demos over MQ.

**Recommendation: Rewrite** as small REST/event endpoints once Account is migrated; retire MQ-specific plumbing.

### 3.11 Branch Migration / Export-Import & Utilities
**Programs:** `CBEXPORT`/`CBIMPORT` (`CVEXPORT`), date utils `CSUTLDTC`/`COBDATFT`, timer `COBSWAIT`/`MVSWAIT`

- `CBEXPORT`/`CBIMPORT` are data-movement tools; date/timer utilities are infrastructure.

**Recommendation: Rewrite/retire.** Replace export/import with standard ETL or the migration tooling itself; replace date utilities with `java.time`; drop mainframe timer waits (scheduler-native in the new platform).

## 4. Strategy Summary Matrix

The final column references the execution **phase** numbers defined in `CUTOVER_PLAN.md` (Phases 0–8) so the two documents stay 1:1. (Those phases roll up into delivery Waves A–D in `CUTOVER_PLAN.md` §5.)

| Functional area | Recommended strategy | Migration risk | Cutover phase |
|:--|:--|:--|:--|
| Security & Sign-on | **Rewrite** (auth gateway / strangler front door) | Low–Med | 1 |
| Reference Data | **Refactor → Rewrite** (CRUD service) | Low | 2 |
| Customer | **Refactor** (master data + PII controls) | Low–Med | 3 |
| Card (+ XREF seam) | **Refactor** | Medium | 4 |
| Account | **Refactor (assisted)** | Med–High | 4 |
| Transaction capture/inquiry | **Strangler + Rewrite API** | Medium | 5 |
| Billing/Statements/Reporting | **Rewrite** | Medium | 6 |
| Authorizations (IMS/DB2/MQ) | **Strangler + Rewrite** | High | 6 |
| MQ inquiry endpoints | **Rewrite** | Low | 6 |
| Posting & Interest (batch core) | **Refactor + parity harness** | **High** | 7 (last) |
| Export/Import & utilities | **Rewrite / retire** | Low | spread across phases |

## 5. Cross-Cutting Recommendations

- **Pick one anchor target stack** and apply consistently: Java 21 + Spring Boot, PostgreSQL (relational replacement for VSAM/DB2), Spring Security/OIDC, Kafka or SQS/SNS replacing IBM MQ seams. The repo already ships `samples/m2` (AWS Mainframe Modernization / UniKix) for an interim replatform runtime if a bridge period is needed.
- **Replatform only as a bridge, never as the destination.** UniKix/M2 rehosting (`samples/m2/unikix`) is valuable to de-risk the *infrastructure* exit from the mainframe quickly, but it preserves COBOL and the file-coupling — keep the refactor/rewrite waves moving.
- **Money first:** every `S9(n)V99` / COMP-3 field becomes `BigDecimal` with documented scale and rounding; build a numeric-parity test before touching posting/interest.
- **Kill the file-coupling:** the `CARDXREF` join and shared-file integration must be replaced by APIs/foreign keys — this is the single biggest architectural change and is detailed in `DOMAIN_DECOMPOSITION.md`.
- **Parity by construction:** maintain a golden-master harness (legacy output vs. new output) for every batch job, mirroring the reconciliation pattern used elsewhere in this org's SAS→Databricks migrations.

See `DOMAIN_DECOMPOSITION.md` for bounded contexts and extraction seams, `CUTOVER_PLAN.md` for the phased sequence, and `RISK_REGISTER.md` for risks and mitigations.
