# Domain Decomposition — CardDemo System

## Overview

This document identifies bounded contexts within the CardDemo mainframe application, maps their data ownership and inter-domain dependencies, and analyzes extraction seams — the natural boundaries where legacy modules can be separated into independent microservices with minimal coupling.

---

## Bounded Context Map

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          CardDemo Bounded Contexts                            │
│                                                                              │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐  │
│  │  Identity   │    │   Customer   │    │   Account    │    │    Card    │  │
│  │  & Access   │───▶│  Management  │◀───│  Management  │◀──▶│ Management │  │
│  └─────────────┘    └──────────────┘    └──────────────┘    └────────────┘  │
│         │                   │                   │ ▲                 │        │
│         │                   │                   │ │                 │        │
│         ▼                   ▼                   ▼ │                 ▼        │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐  │
│  │    Admin    │    │  Statement   │    │ Transaction  │    │Cross-Ref   │  │
│  │ Operations  │    │ & Reporting  │    │  Processing  │    │  (Shared)  │  │
│  └─────────────┘    └──────────────┘    └──────────────┘    └────────────┘  │
│                                                │                            │
│                                                ▼                            │
│  ┌─────────────┐    ┌──────────────┐    ┌──────────────┐    ┌────────────┐  │
│  │    MQ       │    │  Reference   │    │Authorization │    │  Branch    │  │
│  │ Integration │    │    Data      │───▶│  & Fraud     │    │ Migration  │  │
│  └─────────────┘    └──────────────┘    └──────────────┘    └────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

Legend:  ───▶  depends-on (data flow direction)
         ◀──▶  bidirectional dependency
```

---

## Bounded Context Definitions

### BC-1: Identity & Access

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | User authentication, session management, role-based access |
| **Owned Data** | `USRSEC` VSAM (user credentials, roles) |
| **Programs** | `COSGN00C` (sign-on), `COUSR00C`–`COUSR03C` (user CRUD) |
| **Upstream Dependencies** | None |
| **Downstream Consumers** | Every other context (authenticated user context via COMMAREA) |
| **Ubiquitous Language** | User, Admin, Password, UserType, SignOn |

**Data Model (from `CSUSR01Y`):**
```
SEC-USER-DATA
├── SEC-USR-ID          PIC X(08)   -- Primary key
├── SEC-USR-FNAME       PIC X(20)
├── SEC-USR-LNAME       PIC X(20)
├── SEC-USR-PWD         PIC X(08)   -- Plaintext (to be replaced)
├── SEC-USR-TYPE        PIC X(01)   -- 'A'=Admin, 'U'=User
└── SEC-USR-FILLER      PIC X(23)
```

---

### BC-2: Customer Management

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Customer profile data (PII, demographics, credit score) |
| **Owned Data** | `CUSTDATA` VSAM KSDS (500-byte customer records) |
| **Programs** | `CBCUS01C` (batch load) |
| **Upstream Dependencies** | None (master data origin) |
| **Downstream Consumers** | Account (via XREF), Statement, Authorization, Branch Migration |
| **Ubiquitous Language** | Customer, FICO Score, PrimaryCardHolder, EFT Account |

**Data Model (from `CVCUS01Y`):**
```
CUSTOMER-RECORD
├── CUST-ID                    PIC 9(09)   -- Primary key
├── CUST-FIRST-NAME            PIC X(25)
├── CUST-MIDDLE-NAME           PIC X(25)
├── CUST-LAST-NAME             PIC X(25)
├── CUST-ADDR-LINE-1..3        PIC X(50) each
├── CUST-ADDR-STATE-CD         PIC X(02)
├── CUST-ADDR-COUNTRY-CD       PIC X(03)
├── CUST-ADDR-ZIP              PIC X(10)
├── CUST-PHONE-NUM-1..2        PIC X(15) each
├── CUST-SSN                   PIC 9(09)
├── CUST-GOVT-ISSUED-ID        PIC X(20)
├── CUST-DOB-YYYY-MM-DD        PIC X(10)
├── CUST-EFT-ACCOUNT-ID        PIC X(10)
├── CUST-PRI-CARD-HOLDER-IND   PIC X(01)
├── CUST-FICO-CREDIT-SCORE     PIC 9(03)
└── FILLER                     PIC X(168)
```

---

### BC-3: Account Management

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Account lifecycle — balance management, credit limits, status transitions |
| **Owned Data** | `ACCTDATA` VSAM KSDS (300-byte account records) |
| **Programs** | `COACTVWC` (view), `COACTUPC` (update) |
| **Upstream Dependencies** | Customer (account holder), Card (linked cards) |
| **Downstream Consumers** | Transaction Posting, Interest Calc, Bill Payment, Authorization, Statement |
| **Ubiquitous Language** | Account, CreditLimit, CashCreditLimit, CurrentBalance, CycleCredit, CycleDebit, GroupID |

**Data Model (from `CVACT01Y`):**
```
ACCOUNT-RECORD
├── ACCT-ID                 PIC 9(11)       -- Primary key
├── ACCT-ACTIVE-STATUS      PIC X(01)
├── ACCT-CURR-BAL           PIC S9(10)V99   -- Signed decimal
├── ACCT-CREDIT-LIMIT       PIC S9(10)V99
├── ACCT-CASH-CREDIT-LIMIT  PIC S9(10)V99
├── ACCT-OPEN-DATE          PIC X(10)
├── ACCT-EXPIRAION-DATE     PIC X(10)
├── ACCT-REISSUE-DATE       PIC X(10)
├── ACCT-CURR-CYC-CREDIT    PIC S9(10)V99
├── ACCT-CURR-CYC-DEBIT     PIC S9(10)V99
├── ACCT-ADDR-ZIP           PIC X(10)
├── ACCT-GROUP-ID           PIC X(10)       -- Links to Disclosure Group
└── FILLER                  PIC X(178)
```

---

### BC-4: Card Management

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Credit card issuance, activation, status management |
| **Owned Data** | `CARDDATA` VSAM KSDS (150-byte card records) |
| **Programs** | `COCRDLIC` (list), `COCRDSLC` (detail), `COCRDUPC` (update) |
| **Upstream Dependencies** | Account (card linked to account) |
| **Downstream Consumers** | Transaction (card number on every transaction), Authorization |
| **Ubiquitous Language** | CardNumber, CVV, EmbossedName, ExpirationDate, ActiveStatus |

**Data Model (from `CVACT02Y`):**
```
CARD-RECORD
├── CARD-NUM               PIC X(16)   -- Primary key
├── CARD-ACCT-ID           PIC 9(11)   -- FK → Account
├── CARD-CVV-CD            PIC 9(03)
├── CARD-EMBOSSED-NAME     PIC X(50)
├── CARD-EXPIRAION-DATE    PIC X(10)
├── CARD-ACTIVE-STATUS     PIC X(01)
└── FILLER                 PIC X(59)
```

---

### BC-5: Cross-Reference (Shared Kernel)

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Maps Customer ↔ Account ↔ Card relationships |
| **Owned Data** | `CARDXREF` VSAM KSDS (50-byte xref records) |
| **Programs** | Referenced by most online programs via COMMAREA lookups |
| **Nature** | Shared Kernel — not an independent service but a relationship table consumed by multiple contexts |
| **Ubiquitous Language** | CardNumber, CustomerID, AccountID |

**Data Model (from `CVACT03Y`):**
```
CARD-XREF-RECORD
├── XREF-CARD-NUM    PIC X(16)   -- Primary key
├── XREF-CUST-ID    PIC 9(09)   -- FK → Customer
├── XREF-ACCT-ID    PIC 9(11)   -- FK → Account
└── FILLER           PIC X(14)
```

**Extraction Note:** In the target architecture, this becomes a materialized view or a relationship maintained within the Account service (since the Account owns the Card-to-Customer mapping).

---

### BC-6: Transaction Processing

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Transaction capture (online), posting (batch), balance updates |
| **Owned Data** | `TRANSACT` VSAM KSDS (350-byte), `DALYTRAN` sequential, `TCATBALF` (category balance), `DALYREJS` (rejections) |
| **Programs** | Online: `COTRN00C`–`COTRN02C`; Batch: `CBTRN01C`–`CBTRN03C`, `CBACT04C` (interest) |
| **Upstream Dependencies** | Account (balance updates), Card (validation), Reference Data (type/category lookup) |
| **Downstream Consumers** | Statement, Reporting |
| **Ubiquitous Language** | Transaction, PostingDate, OriginTimestamp, CategoryCode, TypeCode, MerchantInfo |

**Data Model (from `CVTRA05Y`):**
```
TRAN-RECORD
├── TRAN-ID              PIC X(16)       -- Primary key (auto-generated)
├── TRAN-TYPE-CD         PIC X(02)       -- FK → Transaction Type
├── TRAN-CAT-CD          PIC 9(04)       -- FK → Transaction Category
├── TRAN-SOURCE          PIC X(10)
├── TRAN-DESC            PIC X(100)
├── TRAN-AMT             PIC S9(09)V99   -- Signed amount
├── TRAN-MERCHANT-ID     PIC 9(09)
├── TRAN-MERCHANT-NAME   PIC X(50)
├── TRAN-MERCHANT-CITY   PIC X(50)
├── TRAN-MERCHANT-ZIP    PIC X(10)
├── TRAN-CARD-NUM        PIC X(16)       -- FK → Card
├── TRAN-ORIG-TS         PIC X(26)       -- ISO-like timestamp
├── TRAN-PROC-TS         PIC X(26)       -- Processing timestamp
└── FILLER               PIC X(20)
```

**Sub-models:**
```
TRAN-CAT-BAL-RECORD (CVTRA01Y)           TRAN-TYPE-RECORD (CVTRA03Y)
├── TRANCAT-ACCT-ID  PIC 9(11)           ├── TRAN-TYPE      PIC X(02)
├── TRANCAT-TYPE-CD  PIC X(02)           ├── TRAN-TYPE-DESC PIC X(50)
├── TRANCAT-CD       PIC 9(04)           └── FILLER         PIC X(08)
├── TRAN-CAT-BAL     PIC S9(09)V99
└── FILLER           PIC X(22)
```

---

### BC-7: Statement & Reporting

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Generate account statements and transaction reports |
| **Owned Data** | Output files only (STMTFILE, HTMLFILE, report spool) |
| **Programs** | `CBSTM03A` (batch statement), `CBSTM03B` (subroutine), `CORPT00C` (online) |
| **Upstream Dependencies** | Transaction, Account, Customer, XREF |
| **Downstream Consumers** | End users (output only) |
| **Ubiquitous Language** | Statement, BillingCycle, StatementDate, TotalAmount |

---

### BC-8: Bill Payment

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Process payments against account balances |
| **Owned Data** | None (writes to Transaction and Account stores) |
| **Programs** | `COBIL00C` |
| **Upstream Dependencies** | Account (debit), Transaction (payment record), Card |
| **Downstream Consumers** | None (terminal operation) |
| **Ubiquitous Language** | Payment, FullPayment, PartialPayment, PaymentTransaction |

---

### BC-9: Authorization & Fraud (Extension)

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Real-time authorization decisioning, fraud flagging |
| **Owned Data** | IMS DB (auth records), DB2 `AUTHFRDS` table (fraud cases), MQ queues |
| **Programs** | `COPAUA0C` (MQ processor), `COPAUS0C`/`1C`/`2C` (screens), `CBPAUP0C` (purge) |
| **Upstream Dependencies** | Account (available credit), Customer (validation), Card (card status), XREF |
| **Downstream Consumers** | Fraud Analytics (downstream of DB2 table) |
| **Ubiquitous Language** | Authorization, ApprovalCode, DeclineReason, FraudFlag, PendingAuth, MerchantCategoryCode |

---

### BC-10: Reference Data (Extension)

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Manage transaction types and categories, disclosure groups, interest rates |
| **Owned Data** | DB2 tables (TRAN_TYPE, TRAN_CATEGORY), `DISCGRP` VSAM, `TRANCATG` VSAM, `TRANTYPE` VSAM |
| **Programs** | `COTRTLIC`, `COTRTUPC`, `COBTUPDT` |
| **Upstream Dependencies** | None (master/reference data) |
| **Downstream Consumers** | Transaction Processing, Interest Calc, Statement |
| **Ubiquitous Language** | TransactionType, CategoryCode, DisclosureGroup, InterestRate |

**Data Model (from `CVTRA02Y` — Disclosure Group):**
```
DIS-GROUP-RECORD
├── DIS-ACCT-GROUP-ID    PIC X(10)   -- Composite key part
├── DIS-TRAN-TYPE-CD     PIC X(02)   -- Composite key part
├── DIS-TRAN-CAT-CD      PIC 9(04)   -- Composite key part
├── DIS-INT-RATE         PIC S9(04)V99   -- Interest rate
└── FILLER               PIC X(28)
```

---

### BC-11: Branch Migration

| Attribute | Value |
|-----------|-------|
| **Core Responsibility** | Export/import complete customer portfolios for branch consolidation |
| **Owned Data** | Multi-record export file format |
| **Programs** | `CBEXPORT`, `CBIMPORT` |
| **Upstream Dependencies** | Customer, Account, Card, XREF, Transaction |
| **Downstream Consumers** | Receiving branch systems |
| **Ubiquitous Language** | ExportFile, RecordType, BatchChecksum, MigrationRun |

---

## Extraction Seam Analysis

An **extraction seam** is a boundary in the legacy system where a module can be separated with minimal changes to the remaining system. Ideal seams have:
- Clear data ownership (single writer)
- Well-defined interface (COMMAREA fields, MQ messages, file I/O boundaries)
- Low fan-out (few downstream dependents for writes)

### Seam Quality Rating

| Bounded Context | Seam Quality | Extraction Seam | Integration Pattern |
|----------------|:------------:|-----------------|---------------------|
| Identity & Access | 🟢 Excellent | USRSEC file read in `COSGN00C` → Replace with token validation | API Gateway + JWT |
| Customer | 🟢 Excellent | CUSTFILE read-only in most programs → API replacement transparent | REST API + CDC sync |
| Card Management | 🟢 Excellent | CARDDATA keyed reads → REST GET by card number | REST API |
| Reference Data | 🟢 Excellent | VSAM/DB2 lookups → REST cache-backed reference API | REST + local cache |
| Statement & Reporting | 🟢 Excellent | Pure output consumer, no write-back → New service reads from APIs | Event-driven generation |
| MQ Integration | 🟢 Excellent | MQ interface IS the seam — same message format, new consumer | MQ → REST bridge |
| Account Management | 🟡 Good | ACCTFILE writes from multiple programs (posting, payment, interest) → Need Account service as single writer | Command API (writes) + Query API (reads) |
| Transaction (Online) | 🟡 Good | TRANSACT VSAM writes from online + batch reading → Separate entry from processing | Event sourcing with legacy file bridge |
| Bill Payment | 🟡 Good | Orchestrates Account + Transaction writes → Needs both services available first | Saga/orchestration |
| Authorization & Fraud | 🟡 Good | MQ provides natural boundary, but IMS + DB2 + VSAM three-resource commit complicates | Event-driven with outbox pattern |
| Transaction (Batch) | 🔴 Difficult | Reads/writes 6+ files atomically; CLOSEFIL/OPENFIL ceremony | Dual-run with reconciliation |
| Branch Migration | 🔴 Difficult | Touches all master files — depends on ALL other domains being available | Last to migrate; ETL replacement |

---

## Dependency Graph (Data Flow)

```
                    ┌──────────────┐
                    │  Reference   │
                    │    Data      │
                    └──────┬───────┘
                           │ (type/category lookups)
                           ▼
┌────────────┐     ┌──────────────┐     ┌──────────────┐
│  Customer  │────▶│   Account    │◀────│    Card      │
└────────────┘     └──────┬───────┘     └──────────────┘
      │                   │                     │
      │                   │ (balance updates)   │ (card validation)
      │                   ▼                     │
      │            ┌──────────────┐             │
      │            │ Transaction  │◀────────────┘
      │            │  Processing  │
      │            └──────┬───────┘
      │                   │
      │    ┌──────────────┼──────────────┐
      │    ▼              ▼              ▼
      │ ┌────────┐ ┌──────────┐ ┌───────────────┐
      │ │Interest│ │ Posting  │ │  Bill Payment │
      │ │  Calc  │ │  Batch   │ │               │
      │ └────────┘ └──────────┘ └───────────────┘
      │                   │
      ▼                   ▼
┌──────────────┐   ┌──────────────┐
│  Statement   │   │Authorization │
│  & Reporting │   │  & Fraud     │
└──────────────┘   └──────────────┘
```

---

## COMMAREA as Integration Contract

The `COCOM01Y` copybook defines the **COMMAREA** — the inter-program communication area that acts as a shared contract between all online CICS programs:

```
CARDDEMO-COMMAREA
├── CDEMO-GENERAL-INFO
│   ├── FROM-TRANID / FROM-PROGRAM   -- Routing context
│   ├── TO-TRANID / TO-PROGRAM       -- Navigation target
│   ├── USER-ID                       -- Authenticated user
│   ├── USER-TYPE ('A'/'U')          -- Role
│   └── PGM-CONTEXT (0=enter, 1=re-enter)
├── CDEMO-CUSTOMER-INFO
│   ├── CUST-ID
│   ├── CUST-FNAME / MNAME / LNAME
├── CDEMO-ACCOUNT-INFO
│   ├── ACCT-ID
│   └── ACCT-STATUS
├── CDEMO-CARD-INFO
│   └── CARD-NUM
└── CDEMO-MORE-INFO
    ├── LAST-MAP
    └── LAST-MAPSET
```

**Modernization Implication:** The COMMAREA is the "session state" contract. In the target architecture, this maps to:
- **General Info** → JWT claims + API Gateway routing headers
- **Customer/Account/Card Info** → Query parameters or request body in REST calls
- **More Info** → Frontend routing state (React Router / URL params)

---

## Shared Data Anti-Patterns (to resolve)

| Anti-Pattern | Location | Resolution |
|-------------|----------|------------|
| Multiple writers to ACCTFILE | `COACTUPC`, `CBTRN02C`, `CBACT04C`, `COBIL00C` | Account Service becomes single writer; others use Command API |
| Multiple writers to TRANSACT | `COTRN02C` (online add), `CBTRN02C` (batch posting) | Transaction Service with separate entry vs. posting pipelines |
| CLOSEFIL/OPENFIL ceremony | All batch jobs must lock VSAM files from CICS | Eliminated by database (PostgreSQL MVCC — no file locking) |
| XREF as join table | Used by Auth, Statement, Posting, Card programs | Materialized as a relationship in Account Service; exposed via API |
| Reference data file extracts | `TRANEXTR` JCL extracts DB2 → VSAM for CICS programs | Single source in Reference Data Service; consumers call API |

---

## Recommended Domain Service Boundaries

| Service | Owns | Exposes | Consumes |
|---------|------|---------|----------|
| `carddemo-auth` | User credentials, tokens | `/auth/login`, `/auth/validate`, `/users/**` | — |
| `carddemo-customer` | Customer profiles | `/customers/**` | — |
| `carddemo-account` | Accounts, XREF relationships | `/accounts/**`, `/accounts/{id}/cards` | Customer API |
| `carddemo-card` | Card records | `/cards/**` | Account API |
| `carddemo-transaction` | Transactions, daily files | `/transactions/**`, domain events | Account API, Card API, RefData API |
| `carddemo-posting` | Category balances | Spring Batch job | Transaction events, Account API, RefData API |
| `carddemo-interest` | Interest calculation logic | Spring Batch job | Account API, RefData API (disclosure groups) |
| `carddemo-statement` | Generated statements | `/statements/**` | Transaction API, Account API, Customer API |
| `carddemo-payment` | Payment orchestration | `/payments/**` | Account API, Transaction API |
| `carddemo-authorization` | Auth decisions, fraud flags | MQ/Kafka consumer + `/authorizations/**` | Account API, Card API, Customer API |
| `carddemo-refdata` | Types, categories, disclosure groups | `/reference/**` | — |
