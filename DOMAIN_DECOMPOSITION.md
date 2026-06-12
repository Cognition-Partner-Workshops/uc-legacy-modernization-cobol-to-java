# CardDemo Domain Decomposition

> Identifies the **bounded contexts** inside the CardDemo monolith and analyzes the **extraction seams** that must be cut to pull each context out as an independent Java service. Read alongside `MODERNIZATION_BLUEPRINT.md` (per-area strategy) and `CUTOVER_PLAN.md` (sequencing).

## 1. Method

Boundaries are inferred from three signals in the source:

1. **Data ownership** — which VSAM/DB2 store a program is the authoritative writer of (`SELECT … ASSIGN`, `EXEC CICS READ/WRITE/REWRITE`, `EXEC SQL`).
2. **Control flow** — online navigation via `EXEC CICS XCTL` + shared `COMMAREA` (`COCOM01Y`), and batch job ordering in `app/scheduler/CardDemo.controlm`.
3. **Cohesion of business rules** — clusters of programs that change together and speak the same ubiquitous language (account, card, transaction, authorization).

The monolith integrates **through shared files**, not interfaces. The decomposition's central task is to convert implicit file-coupling into explicit context boundaries with owned data and published contracts.

## 2. Context Map

```
                         ┌──────────────────────┐
                         │   Identity & Access   │  (USRSEC)
                         │  COSGN00C, COUSR0x    │
                         └──────────┬───────────┘
                                    │ authenticates / authorizes
        ┌───────────────┬──────────┼─────────────┬──────────────────┐
        ▼               ▼          ▼             ▼                  ▼
┌─────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────────┐  ┌──────────────┐
│  Customer   │  │  Account   │  │   Card     │  │  Transaction  │  │ Reference    │
│  CUSTDAT    │  │  ACCTDAT   │  │ CARDDAT +  │  │ TRANSACT +    │  │ TRANTYPE,    │
│  CVCUS01Y   │  │  CVACT01Y  │  │ CARDXREF   │  │ DALYTRAN      │  │ TRANCATG,    │
│             │  │            │  │ (the join) │  │ CVTRA05/06Y   │  │ DISCGRP      │
└─────┬───────┘  └─────┬──────┘  └─────┬──────┘  └──────┬────────┘  └──────┬───────┘
      │  customer↔acct  │  acct↔card    │  card↔tran      │  rates/types     │
      └─────────────────┴───────┬───────┴─────────────────┴──────────────────┘
                                ▼
                  ┌────────────────────────────┐
                  │ Billing, Interest & Posting │  (TCATBALF, statements, reports)
                  │ CBTRN02C, CBACT04C, CBSTM03A│
                  └──────────────┬─────────────┘
                                 ▼
                  ┌────────────────────────────┐
                  │  Authorization & Fraud      │  (IMS + DB2 AUTHFRDS + MQ)  [optional]
                  │  COPAUA0C, COPAUS0/1/2C     │
                  └────────────────────────────┘
```

## 3. Bounded Contexts

### BC-1 — Identity & Access
- **Responsibility:** authentication, user lifecycle, role (`U`/`A`) authorization.
- **Owns:** `USRSEC` (`CSUSR01Y`: `SEC-USR-ID`, `SEC-USR-PWD`, `SEC-USR-TYPE`).
- **Programs:** `COSGN00C` (CC00), `COUSR00C`–`COUSR03C`, `COADM01C`, `COMEN01C` (menu/routing).
- **Language:** User, Role, Credential, Session.
- **Notes:** Plaintext 8-char password is a defect to fix on extraction, not preserve. Natural home for the strangler gateway.

### BC-2 — Customer (Party / Master Data)
- **Responsibility:** customer identity, contact, KYC-adjacent attributes.
- **Owns:** `CUSTDAT` (`CVCUS01Y`) — incl. `CUST-SSN`, `CUST-GOVT-ISSUED-ID`, `CUST-DOB`, `CUST-FICO-CREDIT-SCORE`.
- **Programs:** `CBCUS01C`; read by account/card/statement flows.
- **Language:** Customer, Address, SSN, FICO score.
- **Notes:** PII-heavy → encryption/tokenization + governance is part of the boundary.

### BC-3 — Account
- **Responsibility:** account lifecycle, balances, credit limits, cycle credit/debit, group assignment.
- **Owns:** `ACCTDAT` (`CVACT01Y`: `ACCT-CURR-BAL`, `ACCT-CREDIT-LIMIT`, `ACCT-CASH-CREDIT-LIMIT`, `ACCT-GROUP-ID`).
- **Programs:** `COACTVWC` (CAVW), `COACTUPC` (CAUP, 4,236 lines of validation), `CBACT01C`.
- **Language:** Account, Balance, Credit Limit, Cycle, Account Group.
- **Notes:** Account balance is co-written by Posting & Interest (BC-7) → that write path is a consistency hotspot, not a separate owner.

### BC-4 — Card
- **Responsibility:** card issuance/status, embossing, and the **card↔account↔customer cross-reference**.
- **Owns:** `CARDDAT` (`CVACT02Y`) and **`CARDXREF` (`CVACT03Y`)** — the relationship file with AIX on account and customer.
- **Programs:** `COCRDLIC` (CCLI), `COCRDSLC` (CCDL), `COCRDUPC` (CCUP), `CBACT02C`, `CBACT03C`.
- **Language:** Card, PAN, CVV, Embossed Name, Cross-Reference.
- **Notes:** The XREF file is the single most depended-upon seam in the system (see §4.1).

### BC-5 — Transaction
- **Responsibility:** capture (online add), inquiry, and intake of the daily transaction feed.
- **Owns:** `TRANSACT` (`CVTRA05Y`) and `DALYTRAN` (`CVTRA06Y`).
- **Programs:** `COTRN00C`/`COTRN01C`/`COTRN02C` (CT00–02); `CBTRN01C` (validation/read).
- **Language:** Transaction, Daily Transaction, Merchant, Amount.
- **Notes:** Producer for the posting pipeline; `DALYTRAN` is the contract handoff to BC-7.

### BC-6 — Reference Data
- **Responsibility:** transaction types, categories, and disclosure-group interest rates.
- **Owns:** `TRANTYPE` (`CVTRA03Y`), `TRANCATG` (`CVTRA04Y`), `DISCGRP` (`CVTRA02Y`). DB2 variants exist (`TRNTYPE.ddl`, `TRNTYCAT.ddl`).
- **Programs:** `COTRTLIC` (CTLI), `COTRTUPC` (CTTU), `COBTUPDT`.
- **Language:** Transaction Type, Category, Disclosure Group, Interest Rate.
- **Notes:** High fan-in (read by posting, interest, reporting). Extract early and expose as cached, versioned read model.

### BC-7 — Billing, Interest & Posting
- **Responsibility:** post daily transactions to accounts, maintain category balances, compute interest, produce statements/reports, bill payment.
- **Owns:** `TCATBALF` (`CVTRA01Y`, category balances); orchestrates writes to Account.
- **Programs:** `CBTRN02C` (POSTTRAN), `CBACT04C` (INTCALC), `CBSTM03A`/`CBSTM03B` (statements), `CBTRN03C` (reports), `COBIL00C` (CB00), `CORPT00C` (CR00).
- **Language:** Posting, Category Balance, Interest, Statement, Cycle Close.
- **Notes:** Financial core. Reads many contexts (Account, Card/XREF, Reference, Transaction). Must be extracted **last** behind a parity harness.

### BC-8 — Authorization & Fraud (optional module)
- **Responsibility:** real-time authorization request/response, pending-auth summary/detail, fraud scoring, expiry purge.
- **Owns:** IMS pending-auth segments + DB2 `AUTHFRDS` (fraud).
- **Programs:** `COPAUA0C` (MQ-driven), `COPAUS0C`/`1C`/`2C`, `CBPAUP0C` (purge), `PAUDBLOD`/`PAUDBUNL`, `DBUNLDGS`.
- **Tech seams:** IBM MQ (request/response), IMS DB, DB2.
- **Notes:** Already has an async (MQ) boundary — the cleanest existing seam in the whole system.

### BC-9 — Platform / Shared Kernel
- **Responsibility:** cross-cutting utilities — COBOL date validation (`CSUTLDTC`), COBOL timer driver (`COBSWAIT`), **assembler** modules `COBDATFT` (date-format conversion) and `MVSWAIT` (timer control) in `app/asm`, branch export/import (`CBEXPORT`/`CBIMPORT`, `CVEXPORT`), and the shared `COMMAREA` (`COCOM01Y`).
- **Notes:** Not a business domain. Decompose into shared libraries (`java.time`, scheduler-native waits) and migration tooling; **do not** let `COCOM01Y` leak into new service contracts. The two assembler modules are not COBOL and aren't candidates for COBOL→Java transpilation — retire them and rely on JVM/scheduler-native equivalents.

## 4. Extraction Seam Analysis

A *seam* is where we can cut a context out with a stable contract on each side. Seams are ranked by extraction difficulty.

### 4.1 The `CARDXREF` join (hardest, highest-leverage seam)
- **What couples:** `CARDXREF` (`XREF-CARD-NUM`, `XREF-CUST-ID`, `XREF-ACCT-ID`) is read directly by Card, Account, Transaction, Posting, and Authorization flows to resolve relationships. It is an implicit foreign-key table exposed as a shared file with alternate indexes.
- **Cut strategy:** Card context becomes the **owner/publisher** of the relationship. Replace direct file reads with a `resolve(card|account|customer)` lookup API + projected read model (cache). Backfill referential integrity (FK constraints) in the new relational schema.
- **Bridge during migration:** continue materializing a `CARDXREF`-shaped file/view from the new owner so un-migrated COBOL keeps working (anti-corruption output adapter).

### 4.2 `DALYTRAN` → posting handoff (clean data seam)
- **What couples:** Transaction capture writes `DALYTRAN`; `CBTRN02C` consumes it. The record layout (`CVTRA06Y`) *is* the contract.
- **Cut strategy:** turn the file handoff into an event/queue (`TransactionCaptured`). New Transaction service emits events; posting (legacy or new) consumes. Keep emitting `DALYTRAN`-shaped records until BC-7 migrates.

### 4.3 Account balance co-write (consistency seam, not an ownership seam)
- **What couples:** Both `CBTRN02C` (posting) and `CBACT04C` (interest) update `ACCT-CURR-BAL`/cycle fields in `ACCTDAT`. Online `COACTUPC` also writes account fields.
- **Cut strategy:** Account context owns the balance; Posting/Interest invoke Account through a transactional `applyPosting` / `applyInterest` command rather than rewriting the record directly. This removes the dual-writer ambiguity that exists today.

### 4.4 MQ request/response (ready-made seam)
- **What couples:** Authorization (`COPAUA0C`) and inquiry endpoints (`COACCT01`/`CODATE01`) already communicate over IBM MQ.
- **Cut strategy:** keep the message contract, swap the consumer/producer to the new service. This is the lowest-friction strangler insertion point — front the queue with a new auth service.

### 4.5 CICS COMMAREA / BMS (presentation seam)
- **What couples:** Online programs share `COCOM01Y` and are chained via `XCTL`; UI is 3270 BMS maps (21 maps).
- **Cut strategy:** the COMMAREA is *presentation/session state*, not a domain contract. Replace with stateless REST + a modern SPA; map each transaction (CC00, CAVW, CT02, …) to an endpoint. Do **not** reproduce COMMAREA fields in service APIs.

### 4.6 Reference-data fan-in (read-model seam)
- **What couples:** Many programs read `TRANTYPE`/`TRANCATG`/`DISCGRP`.
- **Cut strategy:** extract Reference first; publish a versioned, cacheable read API. Consumers depend on the API/cache, not the files — this unblocks every other context.

## 5. Seam Difficulty & Ownership Summary

| Seam | Type | Difficulty | New owner | Bridge mechanism |
|:--|:--|:--|:--|:--|
| `CARDXREF` join | Shared FK file | **Hard** | Card | Materialized XREF view/file |
| `DALYTRAN` handoff | File → event | Medium | Transaction → Posting | Emit DALYTRAN-shaped records |
| Account balance co-write | Dual writer | **Hard** | Account | `applyPosting`/`applyInterest` commands |
| MQ auth/inquiry | Async message | **Easy** | Authorization | Keep MQ contract, swap endpoint |
| COMMAREA / BMS | Presentation/session | Medium | per-context APIs | New SPA + REST, no COMMAREA leakage |
| Reference fan-in | Read model | Easy | Reference | Versioned cached read API |
| USRSEC auth | Credential store | Easy | Identity | Auth gateway in front of legacy |

## 6. Decomposition Principles

- **One writer per store.** Eliminate dual-writers (account balance) by routing mutations through the owning context.
- **Contracts, not records.** Published APIs/events replace copybook-shaped file sharing; copybooks (`COCOM01Y`, XREF layouts) are migration inputs, never new public contracts.
- **Anti-corruption adapters** materialize legacy-shaped files/queues so un-migrated COBOL keeps functioning during the strangler period.
- **Extract by fan-in, cut over by risk.** Pull low-coupling, high-fan-in contexts (Identity, Reference) first to unblock others; cut the financial core (Posting/Interest) last under parity testing — see `CUTOVER_PLAN.md`.
