# CardDemo Modernization Blueprint

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Target Platform:** Java / Spring Boot / Relational Database (PostgreSQL or equivalent)

---

## Table of Contents

1. [Strategy Definitions](#1-strategy-definitions)
2. [Strategy Evaluation by Functional Area](#2-strategy-evaluation-by-functional-area)
3. [Recommended Strategy Map](#3-recommended-strategy-map)
4. [Technology Target Architecture](#4-technology-target-architecture)
5. [Cross-Cutting Conversion Patterns](#5-cross-cutting-conversion-patterns)

---

## 1. Strategy Definitions

| Strategy         | Description                                                                                       | When to Use                                                          |
|-----------------|---------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| **Strangler**   | Incrementally replace legacy components by routing traffic to new implementations while keeping the old system running. New functionality wraps around the old. | When the system must remain operational during migration; when components can be isolated behind an interface. |
| **Replatform**  | Move existing logic to a new platform with minimal code changes ("lift and reshape"). Automated code conversion tools + manual adjustment. | When business logic is well-structured and can be mechanically translated; when preserving exact behavior is paramount. |
| **Refactor**    | Restructure and modernize the code while preserving external behavior. Extract services, improve architecture, but keep the core logic. | When the code is fundamentally sound but needs architectural improvement; when the domain knowledge is embedded in working code. |
| **Rewrite**     | Build from scratch using modern patterns. Discard legacy code entirely and re-implement against business requirements. | When the legacy implementation is a security liability, architecturally incompatible, or simpler to rebuild than convert. |

---

## 2. Strategy Evaluation by Functional Area

### 2.1 Security & Authentication

**Current State:** COSGN00C (260 LOC) — plaintext password comparison against USRSEC VSAM, single-character role flag (A/U).

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Low | Authentication is a single entry point — no incremental decomposition possible.                   |
| Replatform  | Low | Plaintext passwords and the CICS ASSIGN-based auth model have no modern equivalent.               |
| Refactor    | Low | The security model is fundamentally broken — refactoring preserves the wrong patterns.            |
| **Rewrite** | **High** | **Recommended.** Replace entirely with Spring Security + OAuth2/OIDC + bcrypt. The auth domain is small (260 LOC + 80-byte USRSEC record) and the legacy approach is a security liability. |

**Recommended:** **Rewrite** — Build new AuthenticationService + UserRepository. Migrate user data to a `users` table with hashed passwords. Implement RBAC to replace the single type flag.

**Effort:** Medium (2–3 weeks) | **Risk:** Low (well-understood domain, small codebase)

---

### 2.2 Account Management

**Current State:** COACTVWC (941 LOC, view) + COACTUPC (4,236 LOC, update) — reads/writes ACCTDATA + CUSTDATA VSAM. COACTUPC is the largest program with 39× COPY REPLACING and deep validation logic.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Medium | Account view can be strangled first (read-only), but update requires full migration.            |
| **Replatform** | **High** | **Recommended for view.** COACTVWC is straightforward read-only with clean VSAM READ patterns that map directly to JPA queries. |
| **Refactor** | **High** | **Recommended for update.** COACTUPC's validation logic is valuable business IP but needs decomposition into smaller services (AccountValidator, AccountUpdater, AccountMapper). |
| Rewrite     | Medium | The 4,236 LOC of COACTUPC contains encoded business rules that would be expensive to re-derive.  |

**Recommended:** **Replatform** (view) + **Refactor** (update)
- COACTVWC → `AccountViewController` with JPA `AccountRepository.findById()`
- COACTUPC → Decompose into `AccountUpdateService`, `AccountValidationService`, `AccountFieldMapper`. Extract the 39× CSSETATY REPLACING pattern into a reusable `FieldAttributeHelper` utility.

**Effort:** High (4–6 weeks) | **Risk:** High (COACTUPC is the #1 complexity hotspot)

---

### 2.3 Card Management

**Current State:** COCRDLIC (1,459 LOC, list) + COCRDSLC (887 LOC, view) + COCRDUPC (1,560 LOC, update) — CICS browse pattern with STARTBR/READNEXT/READPREV for list, VSAM READ for view, REWRITE for update.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| **Strangler** | **High** | **Recommended for list.** Card list can be strangled behind a REST API while the update stays on mainframe temporarily. |
| **Replatform** | **High** | **Recommended for view.** Clean read-only logic translates well to a query endpoint. |
| **Refactor** | **High** | **Recommended for update.** COCRDUPC's card validation (CVV, expiration, status) needs preservation with PCI-DSS compliance additions. |
| Rewrite     | Low | Business logic is sound; no fundamental architectural problems requiring a rewrite.               |

**Recommended:** **Strangler** (list) → **Replatform** (view) → **Refactor** (update)
- COCRDLIC → `GET /api/cards?accountId={id}&page={n}` — replace CICS browse with database pagination
- COCRDSLC → `GET /api/cards/{cardNumber}` — direct JPA query
- COCRDUPC → `PUT /api/cards/{cardNumber}` — `CardUpdateService` with `CardValidator` (add PCI tokenization)

**Effort:** Medium-High (3–5 weeks) | **Risk:** Medium (PCI-DSS compliance requirements for card data)

---

### 2.4 Transaction Processing (Online)

**Current State:** COTRN00C (699 LOC, list) + COTRN01C (330 LOC, view) + COTRN02C (783 LOC, add) — list uses CICS browse, add writes to TRANSACT VSAM and calls CSUTLDTC for date validation.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| **Strangler** | **High** | **Recommended for list + view.** Read-only screens can be strangled first with REST endpoints. |
| Replatform  | Medium | Transaction add (COTRN02C) has multi-file validation that needs careful handling.                 |
| **Refactor** | **High** | **Recommended for add.** COTRN02C's cross-file validation (ACCTDATA + CARDXREF + TRANSACT) should be refactored into a `TransactionCreationService` with proper transaction boundaries. |
| Rewrite     | Low | The validation logic is domain-specific business IP worth preserving.                             |

**Recommended:** **Strangler** (list/view) → **Refactor** (add)
- COTRN00C → `GET /api/transactions?page={n}` with database pagination
- COTRN01C → `GET /api/transactions/{id}`
- COTRN02C → `POST /api/transactions` — `TransactionService.create()` with `@Transactional`, replacing VSAM browse ID generation with database sequences

**Effort:** Medium (3–4 weeks) | **Risk:** Medium (transaction creation is financially sensitive)

---

### 2.5 Transaction Processing (Batch)

**Current State:** CBTRN02C (731 LOC, posting) + CBACT04C (652 LOC, interest) + CBTRN01C (494 LOC, validation) — the core nightly batch cycle reading/writing 5+ VSAM files.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Low | Batch jobs run as a complete cycle — partial strangling would create data consistency issues.      |
| **Replatform** | **High** | **Recommended for posting + validation.** The sequential file-processing pattern maps cleanly to Spring Batch ItemReader/ItemProcessor/ItemWriter. |
| Refactor    | Medium | Interest calculation (CBACT04C) contains financial precision logic that needs careful extraction.  |
| Rewrite     | Low | Batch logic is well-structured and does not need fundamental redesign.                            |

**Recommended:** **Replatform** (all three as Spring Batch jobs)
- CBTRN01C → `DailyTransactionValidationJob` (Spring Batch Step 1)
- CBTRN02C → `TransactionPostingJob` (Spring Batch Step 2) — multi-table updates within a single DB transaction
- CBACT04C → `InterestCalculationJob` (Spring Batch Step 3) — use `BigDecimal` with explicit `RoundingMode.HALF_UP`

**Effort:** High (5–7 weeks) | **Risk:** Very High (financial precision, multi-file atomicity, nightly cycle orchestration)

---

### 2.6 Reporting & Statements

**Current State:** CORPT00C (649 LOC, online report trigger) + CBTRN03C (649 LOC, batch report) + CBSTM03A/B (924+230 LOC, statement generation in text + HTML).

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Medium | Reports can be generated from the new system while online trigger stays on mainframe temporarily. |
| **Replatform** | **High** | **Recommended for batch reports.** The report format (headers, detail lines, totals) maps to JasperReports or a template engine. |
| Refactor    | Medium | CBSTM03A's 13× calls to CBSTM03B can be simplified into a single Writer class.                  |
| **Rewrite** | **High** | **Recommended for statement generation.** The dual-format output (text + HTML) is better served by a template engine (Thymeleaf/Freemarker) than line-by-line string building. |

**Recommended:** **Replatform** (daily reports) + **Rewrite** (statements)
- CORPT00C → `POST /api/reports/generate` — triggers async report generation
- CBTRN03C → `DailyTransactionReportJob` (Spring Batch) with JasperReports
- CBSTM03A/B → `StatementGenerationJob` with Thymeleaf HTML template + text template

**Effort:** Medium (3–4 weeks) | **Risk:** Low (reports are output-only, no data mutation)

---

### 2.7 Bill Payment

**Current State:** COBIL00C (572 LOC) — reads ACCTDATA + CARDXREF, creates payment transaction, updates account balance.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Medium | Could route payment requests to new service while legacy handles other functions.                |
| Replatform  | Medium | The validation + write pattern is straightforward but financially sensitive.                      |
| **Refactor** | **High** | **Recommended.** Extract into `PaymentService` with idempotency guarantees, audit logging, and proper transaction management. |
| Rewrite     | Low | Core logic is sound; needs architectural wrapping, not replacement.                               |

**Recommended:** **Refactor**
- COBIL00C → `POST /api/payments` — `PaymentService.processPayment()` with `@Transactional`, idempotency key, and audit trail

**Effort:** Medium (2–3 weeks) | **Risk:** High (money movement — requires thorough testing)

---

### 2.8 User Administration

**Current State:** COUSR00C (695 LOC, list) + COUSR01C (299 LOC, add) + COUSR02C (414 LOC, update) + COUSR03C (359 LOC, delete) — standard CRUD against USRSEC VSAM.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Medium | Could replace admin UI independently of auth.                                                    |
| **Replatform** | **High** | **Recommended.** Clean CRUD pattern maps directly to a Spring Data REST resource. |
| Refactor    | Low | Code is already well-structured — no complex logic to refactor.                                  |
| Rewrite     | Low | Standard CRUD; replatform is more efficient than rewrite.                                        |

**Recommended:** **Replatform**
- COUSR00C–03C → `UserAdminController` + `UserRepository` (Spring Data JPA) + admin web UI
- Tie into the new Spring Security user model from the Authentication rewrite

**Effort:** Low (1–2 weeks) | **Risk:** Low (standard CRUD, depends on auth rewrite)

---

### 2.9 Data Export/Import

**Current State:** CBEXPORT (582 LOC) + CBIMPORT (487 LOC) — bulk VSAM-to-sequential and sequential-to-VSAM data migration.

| Strategy     | Fit | Rationale                                                                                         |
|-------------|-----|---------------------------------------------------------------------------------------------------|
| Strangler   | Low | These are utility programs, not incremental candidates.                                           |
| **Replatform** | **High** | **Recommended.** Convert to Spring Batch bulk import/export jobs reading from/writing to the new database. |
| Refactor    | Low | Straightforward sequential processing — no architectural issues.                                  |
| Rewrite     | Low | Pattern is simple; replatform is sufficient.                                                      |

**Recommended:** **Replatform**
- CBEXPORT → `DataExportJob` (Spring Batch) writing CSV/JSON
- CBIMPORT → `DataImportJob` (Spring Batch) reading CSV/JSON into database

**Effort:** Low (1–2 weeks) | **Risk:** Low (utility functions, not business-critical)

---

## 3. Recommended Strategy Map

| Functional Area           | Strategy         | Effort    | Risk      | Key Programs                    |
|--------------------------|------------------|-----------|-----------|----------------------------------|
| Security & Authentication | **Rewrite**      | Medium    | Low       | COSGN00C                         |
| Account View              | Replatform       | Low       | Low       | COACTVWC                         |
| Account Update            | **Refactor**     | High      | High      | COACTUPC                         |
| Card List                 | Strangler        | Medium    | Medium    | COCRDLIC                         |
| Card View                 | Replatform       | Low       | Low       | COCRDSLC                         |
| Card Update               | Refactor         | Medium    | Medium    | COCRDUPC                         |
| Transaction List/View     | Strangler        | Low       | Low       | COTRN00C, COTRN01C               |
| Transaction Add           | Refactor         | Medium    | Medium    | COTRN02C                         |
| Transaction Posting       | Replatform       | High      | Very High | CBTRN02C                         |
| Interest Calculation      | Replatform       | High      | Very High | CBACT04C                         |
| Transaction Validation    | Replatform       | Medium    | High      | CBTRN01C                         |
| Daily Reports             | Replatform       | Medium    | Low       | CBTRN03C                         |
| Statement Generation      | **Rewrite**      | Medium    | Low       | CBSTM03A, CBSTM03B               |
| Report Trigger            | Refactor         | Low       | Low       | CORPT00C                         |
| Bill Payment              | Refactor         | Medium    | High      | COBIL00C                         |
| User Administration       | Replatform       | Low       | Low       | COUSR00C–03C                     |
| Data Export/Import        | Replatform       | Low       | Low       | CBEXPORT, CBIMPORT               |
| Navigation/Menus          | **Rewrite**      | Low       | Low       | COMEN01C, COADM01C (→ web UI)   |
| Date Utility              | Replatform       | Low       | Low       | CSUTLDTC (→ java.time)          |

### Strategy Distribution

| Strategy     | # Areas | % of Codebase |
|-------------|---------|---------------|
| Replatform  | 9       | ~40%          |
| Refactor    | 5       | ~35%          |
| Rewrite     | 3       | ~15%          |
| Strangler   | 3       | ~10%          |

---

## 4. Technology Target Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Web UI Layer                          │
│              (React / Angular / Thymeleaf)                   │
│    Login │ Dashboard │ Accounts │ Cards │ Transactions │ Admin│
└────────────────────────────┬────────────────────────────────┘
                             │ REST API
┌────────────────────────────┴────────────────────────────────┐
│                    Spring Boot Application                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Auth Service  │  │ Account Svc  │  │ Card Service │       │
│  │ (Spring Sec)  │  │              │  │ (PCI-DSS)    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Transaction  │  │ Payment Svc  │  │ Report Svc   │       │
│  │ Service      │  │ (idempotent) │  │ (async)      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│  ┌──────────────┐  ┌──────────────┐                          │
│  │ User Admin   │  │ Data Import/ │                          │
│  │ Service      │  │ Export Svc   │                          │
│  └──────────────┘  └──────────────┘                          │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              Spring Batch Jobs                        │   │
│  │  TransactionPostingJob │ InterestCalcJob │ StmtGenJob │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────┘
                             │ JPA / Spring Data
┌────────────────────────────┴────────────────────────────────┐
│                    PostgreSQL Database                        │
│  accounts │ cards │ card_xref │ customers │ transactions     │
│  users │ trans_types │ trans_categories │ category_balances   │
│  disclosure_groups │ daily_transactions │ audit_log           │
└─────────────────────────────────────────────────────────────┘
```

### COBOL → Java Pattern Mapping

| COBOL Pattern                  | Java Target                                         |
|-------------------------------|-----------------------------------------------------|
| CICS Transaction (CC00, etc.) | REST Controller endpoint                            |
| BMS Map (3270 screen)         | Web UI page/component + REST API                    |
| VSAM KSDS file                | JPA Entity + PostgreSQL table                       |
| Copybook record layout        | Java POJO / DTO / JPA Entity                        |
| COMMAREA                      | Spring `@SessionScope` bean or JWT session state    |
| EXEC CICS READ                | `JpaRepository.findById()`                          |
| EXEC CICS WRITE               | `JpaRepository.save()`                              |
| EXEC CICS REWRITE             | `JpaRepository.save()` (merge)                      |
| EXEC CICS DELETE              | `JpaRepository.deleteById()`                        |
| EXEC CICS STARTBR/READNEXT   | `JpaRepository.findAll(Pageable)`                   |
| EXEC CICS XCTL               | Spring MVC redirect / service call                  |
| EXEC CICS WRITEQ TD          | Spring ApplicationEvent / message queue             |
| CALL 'subprogram'            | Java method call / Spring `@Service`                |
| COPY copybook                | Java import / class composition                     |
| COPY ... REPLACING           | Generic utility method with parameters              |
| JCL batch job                | Spring Batch Job + scheduler                        |
| EVALUATE / IF                | switch / if-else                                    |
| PIC S9(n)V99                 | `BigDecimal`                                        |
| PIC X(n)                     | `String`                                            |
| PIC 9(n)                     | `long` / `int`                                      |
| WORKING-STORAGE              | Class instance fields                               |

---

## 5. Cross-Cutting Conversion Patterns

### 5.1 COMMAREA → Session State

The COBOL COMMAREA (COCOM01Y) carries user identity, navigation state, and selected entity context between CICS programs. In Java:

- **User identity** (`CDEMO-USER-ID`, `CDEMO-USER-TYPE`) → Spring Security `Authentication` principal
- **Navigation state** (`CDEMO-FROM-PROGRAM`, `CDEMO-TO-PROGRAM`) → Eliminated (web routing handles this)
- **Entity context** (`CDEMO-ACCT-ID`, `CDEMO-CARD-NUM`) → REST path parameters or query parameters

### 5.2 Pseudo-Conversational → Stateless REST

CICS pseudo-conversational programming (SEND MAP → RETURN TRANSID → RECEIVE MAP) becomes stateless REST:
- SEND MAP → Return JSON response
- RETURN TRANSID → Client-side routing
- RECEIVE MAP → Accept JSON request body

### 5.3 VSAM → Relational Database

| VSAM Concept          | Database Equivalent                       |
|----------------------|-------------------------------------------|
| KSDS (Key-Sequenced) | Table with primary key                    |
| Alternate Index      | Database index / secondary unique constraint |
| REPRO (copy)         | `INSERT INTO ... SELECT` or bulk copy     |
| IDCAMS DEFINE        | `CREATE TABLE` DDL                        |
| GDG (Generation)     | Temporal table or audit trail table       |

### 5.4 Batch Cycle → Spring Batch

The nightly CLOSEFIL → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL cycle becomes:

```java
@Configuration
public class NightlyBatchConfig {
    @Bean
    public Job nightlyCycleJob() {
        return jobBuilder.get("nightlyCycle")
            .start(transactionPostingStep())    // POSTTRAN → CBTRN02C
            .next(interestCalculationStep())    // INTCALC → CBACT04C
            .next(transactionBackupStep())      // TRANBKP
            .next(combineTransactionsStep())    // COMBTRAN
            .next(statementGenerationStep())    // CREASTMT → CBSTM03A
            .next(rebuildIndexesStep())         // TRANIDX (may not be needed with DB indexes)
            .build();
    }
}
```

The CLOSEFIL/OPENFIL steps are eliminated — the database handles concurrent access without file locking.
