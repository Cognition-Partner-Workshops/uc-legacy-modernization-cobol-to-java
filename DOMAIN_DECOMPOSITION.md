# CardDemo Domain Decomposition

## Overview

This document identifies bounded contexts within the CardDemo COBOL application, maps program-to-data dependencies, analyzes extraction seams, and defines the target microservice architecture.

---

## Bounded Contexts

### BC-1: Identity & Access Management

**Responsibility**: User authentication, authorization, role management, session context.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `COSGN00C` (261 LOC) | Sign-on, credential validation | USRSEC (read) |
| `COADM01C` (289 LOC) | Admin menu dispatch | USRSEC (implicit via commarea user type) |
| `COMEN01C` (309 LOC) | Regular user menu dispatch | USRSEC (implicit via commarea user type) |
| `COUSR00C` (696 LOC) | User list (admin) | USRSEC (browse) |
| `COUSR01C` | User add (admin) | USRSEC (write) |
| `COUSR02C` | User update (admin) | USRSEC (read/write) |
| `COUSR03C` | User delete (admin) | USRSEC (delete) |
| `CSUSR01Y` | User security record layout | — |
| `COCOM01Y` | Commarea (carries user-id, user-type) | — |
| `COADM02Y` | Admin menu options config | — |
| `COMEN02Y` | Regular menu options config | — |

**VSAM Files Owned**: USRSEC

**Extraction Seam Analysis**:
- **Inbound coupling**: Every CICS program reads `CDEMO-USER-ID` and `CDEMO-USER-TYPE` from the COMMAREA (`COCOM01Y`). This is the primary cross-context dependency.
- **Outbound coupling**: Sign-on (`COSGN00C`) transfers control via `EXEC CICS XCTL` to either `COADM01C` or `COMEN01C` based on user type.
- **Seam quality**: **Clean**. The COMMAREA acts as a natural anti-corruption layer. The user context (ID + type) is the only data that flows outward. Replace COMMAREA propagation with JWT claims or session attributes.
- **Extraction difficulty**: **Low**. USRSEC is only accessed by programs in this context. No other context writes to it.

---

### BC-2: Account Management

**Responsibility**: Account lifecycle (view, update), credit limits, balances, account status, customer data.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `COACTVWC` (942 LOC) | Account view | ACCTDAT (read), CUSTDAT (read), CARDXREF/CXACAIX (read) |
| `COACTUPC` (4,237 LOC) | Account update | ACCTDAT (read/write), CUSTDAT (read/write), CARDXREF (read), CARDDAT/CARDAIX (read) |
| `CVACT01Y` | Account record (300 bytes) | — |
| `CVCUS01Y` | Customer record (500 bytes) | — |
| `CVACT03Y` | Card cross-reference (50 bytes) | — |

**VSAM Files Owned**: ACCTDAT, CUSTDAT

**VSAM Files Referenced (Cross-Context)**: CARDXREF/CXACAIX (shared with Card Management), CARDDAT/CARDAIX (shared with Card Management)

**Extraction Seam Analysis**:
- **Inbound coupling**: Batch programs (`CBTRN02C`, `CBACT04C`) update ACCTDAT directly. Bill payment (`COBIL00C`) reads and updates ACCTDAT. This is the most coupled data store in the system.
- **Outbound coupling**: `COACTVWC` reads CUSTDAT and CARDXREF to display related customer and card info. `COACTUPC` reads CARDDAT via alternate index for card details.
- **Seam quality**: **Moderate**. Account data is accessed by 5+ programs across 3 contexts. The account record (`CVACT01Y`) must become the authoritative data contract. Cross-context reads (card, customer) can be replaced with API calls.
- **Extraction difficulty**: **High**. `COACTUPC` at 4,237 lines contains deeply nested validation logic for SSN, phone numbers, dates, credit limits, FICO scores, and status flags. All validation rules must be preserved.

**Internal Entity Relationships**:
```
ACCOUNT (ACCTDAT)
  ├── ACCT-ID (PK, 9-11)
  ├── owns → CUSTOMER (CUSTDAT) via CUST-ID in CARDXREF
  ├── owns → CARD(s) (CARDDAT) via CARDXREF
  └── referenced by → TRANSACTION (TRANSACT) via card-to-account lookup
```

---

### BC-3: Card Management

**Responsibility**: Credit card lifecycle (list, view, update), card status, embossed name, CVV, expiration.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `COCRDLIC` (1,460 LOC) | Card list with pagination | CARDDAT/CARDAIX (browse) |
| `COCRDSLC` | Card detail view | CARDDAT (read) |
| `COCRDUPC` | Card update | CARDDAT (read/write) |
| `CVACT02Y` | Card record (150 bytes) | — |
| `CVCRD01Y` | Card working storage | — |

**VSAM Files Owned**: CARDDAT, CARDAIX (alternate index by account)

**VSAM Files Referenced (Cross-Context)**: CARDXREF/CXACAIX (shared cross-reference)

**Extraction Seam Analysis**:
- **Inbound coupling**: Account view/update reads CARDDAT via alternate index. Transaction posting (`CBTRN02C`) validates cards via CARDXREF. Bill payment (`COBIL00C`) reads CXACAIX.
- **Outbound coupling**: Card list navigates to card detail/update within the same context (clean internal flow).
- **Seam quality**: **Good**. CARDDAT is the primary data store; CARDXREF is the join table. If CARDXREF is absorbed into the Card context (as it links card→customer→account), the Card service owns its primary data.
- **Extraction difficulty**: **Medium**. Pagination via CICS STARTBR/READNEXT/READPREV is the main technical challenge. Role-based filtering (admin sees all cards, user sees only their account's cards) needs preservation.

---

### BC-4: Transaction Management (Online)

**Responsibility**: Transaction inquiry (list, view), manual transaction entry, transaction data display.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `COTRN00C` (700 LOC) | Transaction list with pagination | TRANSACT (browse) |
| `COTRN01C` | Transaction detail view | TRANSACT (read) |
| `COTRN02C` | Transaction add (manual entry) | TRANSACT (write) |
| `CVTRA05Y` | Transaction record (350 bytes) | — |

**VSAM Files Owned**: TRANSACT (shared ownership with Batch Processing)

**Extraction Seam Analysis**:
- **Inbound coupling**: Bill payment writes to TRANSACT. Batch posting (`CBTRN02C`) writes to TRANSACT. Report generation reads TRANSACT.
- **Outbound coupling**: Transaction list dispatches to transaction view via commarea.
- **Seam quality**: **Moderate**. TRANSACT is the highest-traffic data store, written by both online and batch processes. During transition, dual-write or change-data-capture (CDC) is needed.
- **Extraction difficulty**: **Medium**. The online programs are CICS-specific but the business logic is straightforward CRUD with pagination.

---

### BC-5: Batch Processing & Settlement

**Responsibility**: Daily transaction posting, interest calculation, statement generation, data backup, batch cycle orchestration.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `CBTRN02C` (732 LOC) | Transaction posting | DALYTRAN (read), TRANSACT (write), XREFFILE (read), ACCTDAT (read/write), TCATBALF (read/write) |
| `CBACT04C` (653 LOC) | Interest calculation | TCATBALF (read), XREFFILE (read), ACCTDAT (read/write), DISCGRP (read), TRANSACT (write) |
| `CBSTM03A/B` | Statement generation | TRANSACT (read), ACCTDAT (read), CUSTDAT (read) |
| `CBTRN03C` | Transaction report | TRANSACT (read) |
| `CBTRN01C` | Combine transactions | TRANSACT (read/write) |
| `CBACT01C-03C` | Account data utilities | ACCTDAT, CARDDAT, CUSTDAT |
| `CBCUS01C` | Customer data utility | CUSTDAT |
| `CBEXPORT/CBIMPORT` | Data export/import | Multiple files |
| `CVTRA06Y` | Daily transaction record (350 bytes) | — |
| `CVTRA01Y` | Transaction category balance (50 bytes) | — |
| `CVTRA02Y` | Disclosure group / interest rates (50 bytes) | — |

**VSAM Files Owned**: DALYTRAN, TCATBALF, DISCGRP, DALYREJS

**VSAM Files Referenced (Cross-Context)**: ACCTDAT (Account Mgmt), TRANSACT (Transaction Mgmt), CARDXREF (Card Mgmt), CUSTDAT (Account Mgmt)

**Batch Cycle Order** (from JCL analysis):
```
CLOSEFIL → Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
         → POSTTRAN (CBTRN02C)
         → INTCALC (CBACT04C)
         → TRANBKP
         → COMBTRAN
         → CREASTMT (CBSTM03A/B)
         → TRANIDX
         → OPENFIL
```

**Extraction Seam Analysis**:
- **Inbound coupling**: Batch depends on data from Account, Card, and Transaction contexts. CLOSEFIL/OPENFIL manage CICS file availability.
- **Outbound coupling**: Posting updates ACCTDAT (Account context) and writes to TRANSACT (Transaction context). Interest calculation also updates ACCTDAT.
- **Seam quality**: **Poor**. This is the most tightly coupled context. It reads and writes across every other data store. The CLOSEFIL/OPENFIL pattern creates a system-wide lock.
- **Extraction difficulty**: **Very High**. The entire batch cycle must be migrated as a unit. Partial extraction would break the end-of-day processing pipeline. The sequence dependencies between steps are implicit in JCL job ordering.

---

### BC-6: Bill Payment

**Responsibility**: Online bill payment processing, payment transaction creation, balance settlement.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `COBIL00C` (573 LOC) | Bill payment | ACCTDAT (read/write), TRANSACT (read/write), CXACAIX (read) |
| Uses: `CVACT01Y`, `CVACT03Y`, `CVTRA05Y` | Shared data structures | — |

**VSAM Files Owned**: None (operates on data owned by Account and Transaction contexts)

**Extraction Seam Analysis**:
- **Inbound coupling**: None. Bill payment is invoked from the main menu.
- **Outbound coupling**: Reads ACCTDAT (Account), writes to TRANSACT (Transaction), reads CXACAIX (Card). A pure consumer of other contexts' data.
- **Seam quality**: **Good for extraction**. Despite touching three data stores, the operation is a self-contained workflow: read balance → confirm → create transaction → update balance. This maps cleanly to a saga or orchestrated service call.
- **Extraction difficulty**: **Medium**. Transaction ID generation (READPREV to get last ID, then +1) is a concurrency risk. Must be replaced with a database sequence or UUID.

---

### BC-7: Reporting

**Responsibility**: Report generation, statement production, online report submission.

| Artifact | Role | Data Dependencies |
|----------|------|-------------------|
| `CORPT00C` (650 LOC) | Report submission via TDQ | TRANSACT (indirect, via batch job) |
| `CBTRN03C` | Batch report generator | TRANSACT (read) |
| `CBSTM03A/B` | Statement generation | TRANSACT (read), ACCTDAT (read), CUSTDAT (read) |
| JCL: TRANREPT, CREASTMT | Batch job definitions | — |
| `CSUTLDTC` | Date validation utility | — |

**VSAM Files Owned**: None (read-only access to other contexts' data)

**Extraction Seam Analysis**:
- **Inbound coupling**: Invoked from main menu.
- **Outbound coupling**: Submits JCL to internal reader via TDQ. Batch report programs read TRANSACT, ACCTDAT, CUSTDAT.
- **Seam quality**: **Good**. Reporting is inherently read-only. A modern reporting service can read from a replicated data store without any coupling to the write path.
- **Extraction difficulty**: **Low** (for the query side). The TDQ-to-internal-reader submission pattern must be entirely replaced with a modern job scheduling mechanism.

---

### BC-8: Optional Integration Modules

**Responsibility**: Authorization processing (IMS/DB2/MQ), transaction type management (DB2), account inquiry (VSAM/MQ).

| Sub-Context | Programs | Technologies |
|-------------|----------|-------------|
| Authorization | `COPAUA0C`, `COPAUS0C`, `COPAUS1C`, `COPAUS2C`, `CBPAUP0C` + 3 others | IMS DB, DB2, MQ |
| Transaction Types | `COTRTUPC`, `COTRTLIC`, `COBTUPDT` | DB2 |
| Account Inquiry | `CDRD/CODATE01`, `CDRA/COACCT01` | VSAM, MQ |

**Extraction Seam Analysis**:
- **Seam quality**: **Moderate**. MQ-based modules already have message interfaces. DB2 modules have SQL which maps to modern RDBMS. IMS DB requires a complete data model redesign.
- **Extraction difficulty**: **High** (authorization), **Low** (transaction types), **Medium** (VSAM/MQ inquiry).

---

## Cross-Context Data Flow Map

```
                    ┌─────────────────────┐
                    │  BC-1: Identity &    │
                    │  Access Management   │
                    │  (USRSEC)            │
                    └────────┬────────────┘
                             │ user-id, user-type
                             │ (via COMMAREA)
              ┌──────────────┼──────────────────┐
              ▼              ▼                   ▼
   ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐
   │ BC-2: Account│  │ BC-4: Txn    │  │ BC-7: Reporting  │
   │ Management   │  │ Management   │  │ (read-only)      │
   │ (ACCTDAT,    │◄─┤ (TRANSACT)   │  │                  │
   │  CUSTDAT)    │  └──────┬───────┘  └──────────────────┘
   └──────┬───────┘         │                   ▲
          │                 │                   │ reads
          │ card lookup     │ writes            │
          ▼                 ▼                   │
   ┌──────────────┐  ┌──────────────────┐      │
   │ BC-3: Card   │  │ BC-5: Batch      │──────┘
   │ Management   │  │ Processing       │
   │ (CARDDAT,    │◄─┤ (DALYTRAN,       │
   │  CARDXREF)   │  │  TCATBALF,       │
   └──────────────┘  │  DISCGRP)        │
                     └────────┬─────────┘
                              │ updates ACCTDAT
                              │ writes TRANSACT
                              ▼
                     ┌──────────────────┐
                     │ BC-6: Bill       │
                     │ Payment          │
                     │ (no owned data)  │
                     └──────────────────┘
```

---

## Extraction Seam Priority

| Seam | Quality | Difficulty | Priority | Rationale |
|------|---------|-----------|----------|-----------|
| BC-1 → rest of system | Clean | Low | **P0 - Extract First** | Only passes user context via COMMAREA. JWT/session replacement is well-understood. |
| BC-7 → rest of system | Clean | Low | **P1 - Extract Early** | Read-only access. Can run from replicated data. No write-side coupling. |
| BC-3 → BC-2 | Good | Medium | **P2** | Card data referenced by Account but via well-defined XREF. Replace with API call. |
| BC-6 → BC-2, BC-4 | Good | Medium | **P3** | Self-contained workflow. Convert to orchestrated saga pattern. |
| BC-4 → BC-5 | Moderate | Medium | **P4** | TRANSACT shared between online and batch. Need CDC or dual-write strategy. |
| BC-2 → BC-3, BC-5 | Moderate | High | **P5** | ACCTDAT widely referenced. Account service must be authoritative source before batch extraction. |
| BC-5 → all contexts | Poor | Very High | **P6 - Extract Last** | Batch cycle touches every data store. Extract only after all other contexts are modernized. |

---

## Shared Kernel: CARDXREF / CXACAIX

The card cross-reference file (`CVACT03Y`) is a shared kernel that links:
- **Card Number** → **Customer ID** → **Account ID**

This 50-byte record is referenced by:
- Account View (`COACTVWC`) - to find cards for an account
- Account Update (`COACTUPC`) - to find cards for an account
- Bill Payment (`COBIL00C`) - to find card number for a payment transaction
- Transaction Posting (`CBTRN02C`) - to validate card→account relationship
- Interest Calculation (`CBACT04C`) - to find card for account

**Recommendation**: In the target state, CARDXREF is eliminated. The relationships are modeled as foreign keys:
- `cards.account_id` → `accounts.id`
- `cards.customer_id` → `customers.id`

During transition, the CARDXREF must be kept in sync via CDC or dual-write until all consumers are migrated.

---

## Target Microservice Architecture

| Service | Source Contexts | Primary DB Tables | API Style |
|---------|----------------|-------------------|-----------|
| `auth-service` | BC-1 | `users`, `roles`, `sessions` | REST + JWT |
| `account-service` | BC-2 | `accounts`, `customers` | REST |
| `card-service` | BC-3 | `cards`, `card_xref` (transitional) | REST |
| `transaction-service` | BC-4 | `transactions` | REST + Event |
| `batch-service` | BC-5 | `daily_transactions`, `category_balances`, `disclosure_groups` | Spring Batch |
| `payment-service` | BC-6 | (none - orchestrates other services) | REST + Saga |
| `reporting-service` | BC-7 | Read replica / materialized views | REST + Async |
