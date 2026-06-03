# CardDemo — Modernization Hotspot Report

> Prioritized ranking of the modules that carry the most modernization risk. Each module is scored
> on **Complexity**, **Risk**, and **Business Impact**; the three combine into a Priority score.
> Use this to sequence migration work and target test coverage.

## Scoring Methodology

Metrics are extracted statically from the COBOL source:

- **Complexity (1–10)** — driven by lines of code and decision density (count of `IF` + `EVALUATE`
  `WHEN` branches, a cyclomatic-complexity proxy).
- **Risk (1–10)** — external integrations (DB2 / IMS / MQ), number of files written/updated,
  financial-integrity exposure, fan-in via shared copybooks, and known code smells.
- **Business Impact (1–10)** — centrality to the core money-movement / customer-facing flows.
- **Priority = round(0.35·Complexity + 0.30·Risk + 0.35·Business Impact)**.

### Raw complexity metrics (all programs, for reference)

| Program | LOC | IF | EVAL | WHEN | Decision pts (IF+WHEN) |
|:--------|----:|---:|-----:|-----:|----:|
| COACTUPC | 4236 | 333 | 20 | 127 | 460 |
| COTRTLIC | 2098 | 174 | 32 | 63 | 237 |
| COCRDUPC | 1560 | 148 | 16 | 59 | 207 |
| COTRTUPC | 1702 | 102 | 26 | 105 | 207 |
| COCRDLIC | 1459 | 122 | 18 | 43 | 165 |
| CBTRN02C | 731 | 96 | 0 | 0 | 96 |
| COTRN02C | 783 | 28 | 26 | 63 | 91 |
| COPAUS0C | 1032 | 50 | 22 | 46 | 96 |
| CBACT04C | 652 | 86 | 0 | 0 | 86 |
| COACTVWC | 941 | 57 | 10 | 16 | 73 |
| COPAUA0C | 1026 | 53 | 10 | 21 | 74 |
| COUSR00C | 695 | 50 | 16 | 52 | 102 |
| COTRN00C | 699 | 52 | 16 | 50 | 102 |
| COPAUS1C | 604 | 34 | 10 | 20 | 54 |
| COBIL00C | 572 | 20 | 18 | 32 | 52 |

---

## Top 10 Hotspots

| # | Module | LOC | Complexity | Risk | Bus. Impact | **Priority** |
|:-:|:-------|----:|:----------:|:----:|:-----------:|:------------:|
| 1 | **COACTUPC** — Account Update | 4236 | 10 | 9 | 10 | **10** |
| 2 | **CBTRN02C** — Transaction Posting (batch) | 731 | 7 | 10 | 10 | **9** |
| 3 | **COTRTLIC** — Tran-Type List/Update/Delete (DB2) | 2098 | 9 | 8 | 6 | **8** |
| 4 | **CBACT04C** — Interest Calculation (batch) | 652 | 7 | 9 | 9 | **8** |
| 5 | **COCRDUPC** — Credit Card Update | 1560 | 9 | 7 | 8 | **8** |
| 6 | **COPAUA0C** — Authorization Decision (MQ+IMS) | 1026 | 8 | 10 | 7 | **8** |
| 7 | **COTRTUPC** — Tran-Type Add/Edit (DB2) | 1702 | 8 | 7 | 6 | **7** |
| 8 | **COPAUS0C** — Pending-Auth Summary (IMS+VSAM) | 1032 | 8 | 8 | 6 | **7** |
| 9 | **COBIL00C** — Bill Payment | 572 | 6 | 8 | 9 | **7** |
| 10 | **COCRDLIC** — Credit Card List | 1459 | 8 | 5 | 7 | **7** |

---

## Detailed Findings

### 1. COACTUPC — Account Update  *(Priority 10)*
- **Largest program in the codebase (4,236 LOC)** with the highest decision density (333 `IF`, 127
  `WHEN`). A single program that validates and rewrites every account field.
- Reads ACCTDAT + CUSTDAT and performs an in-place `REWRITE` of the account master — directly mutates
  the financial core. Pulls in 15 copybooks including the commarea, card work area, lookup codes,
  and date utilities.
- **Risk:** monolithic edit/validation logic, heavy field-by-field handling, optimistic-locking via
  re-read; very hard to test exhaustively. **Highest-value refactor candidate** — decompose into a
  validation service + persistence service.
- **Action:** carve out field validations into rules; wrap account persistence; build a golden-master
  test harness before any rewrite.

### 2. CBTRN02C — Transaction Posting  *(Priority 9)*
- The **money-movement engine**: reads DALYTRAN and updates `ACCTDAT` balances **and** `TCATBAL`
  category balances, writes posted rows to `TRANSACT`, and rejects to `DALYREJS` — six files, three
  of them written/updated, in one batch program (731 LOC, 96 `IF`).
- **Risk: maximum** — any defect corrupts customer balances. No `EVALUATE`; deeply nested `IF` flow.
  Restart/rerun safety depends on file states managed by surrounding JCL (CLOSEFIL/OPENFIL).
- **Action:** treat as the system of record for posting; reproduce with exhaustive parity tests
  (reconcile balances before/after) before migrating. Define idempotency/restart semantics explicitly.

### 3. COTRTLIC — Transaction-Type List/Update/Delete (DB2)  *(Priority 8)*
- 2,098 LOC, 174 `IF`, **16 `EXEC SQL`** — the most SQL-intensive program; demonstrates DB2 **cursor**
  open/fetch/close plus delete logic. Online CRUD over DB2 reference data.
- **Risk:** cursor + SQLCODE handling (via CSDB2RPY/CSDB2RWY), pagination state, and delete cascades.
- **Action:** good candidate to replace with a thin repository/DAO; SQL is explicit and portable.

### 4. CBACT04C — Interest Calculation  *(Priority 8)*
- 652 LOC, 86 `IF`. Reads TCATBAL + DISCGRP (rates) + XREF, **updates ACCTDAT**, and **writes interest
  transactions** to TRANSACT. Core monthly financial computation.
- **Risk:** money math with implied-decimal arithmetic and rate lookups; rounding behavior must be
  preserved exactly. Runs in the MONTHLY-InterestCalculation Control-M chain.
- **Action:** extract the interest formula and rate-resolution into a documented, unit-tested service;
  pin rounding/precision rules.

### 5. COCRDUPC — Credit Card Update  *(Priority 8)*
- 1,560 LOC, 148 `IF`, 59 `WHEN`. Read + `REWRITE` of CARDDAT with extensive field validation; touches
  sensitive PAN/CVV data.
- **Risk:** large validation surface; PCI-sensitive fields; mirrors COACTUPC's monolithic style.
- **Action:** same decomposition pattern as COACTUPC; ensure card-data masking in any new tier.

### 6. COPAUA0C — Authorization Decision Engine (MQ + IMS)  *(Priority 8)*
- 1,026 LOC. **Highest integration risk**: drives `MQOPEN/MQGET/MQPUT1/MQCLOSE` (8 `EXEC DLI` for IMS)
  to receive authorization requests, make approve/decline decisions, and update IMS — real-time and
  asynchronous.
- **Risk:** three runtimes at once (CICS + MQ + IMS), message-format coupling (CCPAURQY/CCPAURLY/
  CCPAUERY), and stateful decisioning. Hard to test without the full middleware stack.
- **Action:** isolate the decision rules from the MQ/IMS plumbing; mock the queues for testing; this is
  the natural seam for an event-driven modernization.

### 7. COTRTUPC — Transaction-Type Add/Edit (DB2)  *(Priority 7)*
- 1,702 LOC, 105 `WHEN`, 7 `EXEC SQL`. DB2 insert/update for tran-type reference data with a large
  screen-handling/validation body.
- **Action:** pairs with COTRTLIC — migrate together as one reference-data service.

### 8. COPAUS0C — Pending-Authorization Summary (IMS + VSAM)  *(Priority 7)*
- 1,032 LOC, 6 `EXEC DLI`. Reads IMS summary/detail segments and joins with ACCTDAT/CUSTDAT for a
  consolidated view.
- **Risk:** IMS hierarchical navigation logic that does not map cleanly to relational/object models.
- **Action:** model the IMS hierarchy explicitly before re-platforming; consider a read projection.

### 9. COBIL00C — Bill Payment  *(Priority 7)*
- 572 LOC. Lower complexity but **high business impact**: pays an account balance in full, writing a
  TRANSACT row and updating ACCTDAT (`REWRITE`). Direct money movement from a customer action.
- **Risk:** financial write from the online tier; needs the same balance-integrity guarantees as
  posting.
- **Action:** route through the same posting/account service introduced for CBTRN02C to avoid two code
  paths mutating balances.

### 10. COCRDLIC — Credit Card List  *(Priority 7)*
- 1,459 LOC, 122 `IF`. Browse with `STARTBR`/`READNEXT`/`READPREV` paging over CARDDAT — VSAM-specific
  cursor semantics that are verbose to reproduce.
- **Risk:** pagination + filtering logic; read-only so integrity risk is low, but high LOC and PAN
  exposure on screen.
- **Action:** replace VSAM browse with keyset pagination in the target store; mask card numbers.

---

## Cross-Cutting Risks (apply across hotspots)

| Theme | Where | Recommendation |
|:------|:------|:---------------|
| **Commarea coupling** | All online (COCOM01Y in 21 pgms) | Model the navigation/state contract first; it gates everything |
| **Balance integrity** | CBTRN02C, CBACT04C, COBIL00C | Single shared posting/account service; reconciliation tests |
| **Multi-runtime integration** | COPAUA0C, COPAUS0C/1C/2C, CODATE01, COACCT01 | Isolate MQ/IMS/DB2 plumbing behind interfaces; mock for tests |
| **Monolithic edit programs** | COACTUPC, COCRDUPC, COTRTUPC | Decompose validation vs persistence; golden-master tests |
| **Sensitive data (PCI/PII)** | COCRDUPC, COCRDLIC, COCRDSLC, COSGN00C | Mask PAN/CVV/SSN; replace plaintext `SEC-USR-PWD` with hashing |
| **Missing dependency** | CREASTMT → CBSTM03A (not in repo) | Source CBSTM03A before claiming full batch parity |
| **Duplicate layouts** | CVCUS01Y vs CUSTREC | Consolidate to one canonical Customer model |
| **Source typos in field names** | `ACCT-EXPIRAION-DATE`, `CARD-EXPIRAION-DATE` | Preserve mapping but correct names in target schema |

## Suggested Migration Sequencing

1. **Foundational:** formalize COCOM01Y commarea contract + the canonical data model (Data Dictionary).
2. **Financial core:** CBTRN02C posting + CBACT04C interest + COBIL00C behind one account/posting
   service with parity tests (highest impact, highest integrity risk).
3. **Large online edits:** COACTUPC, COCRDUPC (decompose validation/persistence).
4. **Reference data (DB2):** COTRTLIC + COTRTUPC as a transaction-type service.
5. **Optional integration modules:** Auth (MQ/IMS) and MQ inquiry programs last — they have the most
   external dependencies and can remain on the mainframe or be re-platformed as events independently.
