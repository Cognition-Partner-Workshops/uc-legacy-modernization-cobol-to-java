# CardDemo Hotspot Report — Top 10 Modules

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Identify the highest-risk, highest-complexity modules to prioritize during modernization.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Ranking](#top-10-hotspot-ranking)
3. [Detailed Module Profiles](#detailed-module-profiles)
4. [Risk Heat Map](#risk-heat-map)
5. [Modernization Recommendations](#modernization-recommendations)

---

## Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension          | Weight | Criteria                                                                                      |
|--------------------|--------|-----------------------------------------------------------------------------------------------|
| **Complexity**     | 40%    | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), number of copybook dependencies, CALL/XCTL count, number of CICS commands, COPY REPLACING usage |
| **Risk**           | 35%    | Data mutation scope (R/W to multiple VSAM files), error handling patterns, security sensitivity, coupling to other programs, state management via COMMAREA |
| **Business Impact**| 25%    | Criticality to core business operations, user-facing vs. batch, financial data handling, regulatory implications (PII, SOX), downstream dependencies |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.35) + (Business Impact × 0.25)

---

## Top 10 Hotspot Ranking

| Rank | Program      | LOC   | Type    | Complexity | Risk | Business Impact | **Composite** | Primary Domain          |
|------|-------------|-------|---------|------------|------|-----------------|---------------|-------------------------|
| 1    | **COACTUPC** | 4,236 | Online  | 10         | 10   | 9               | **9.75**      | Account Update          |
| 2    | **CBTRN02C** | 731   | Batch   | 8          | 10   | 10              | **9.20**      | Transaction Posting     |
| 3    | **CBACT04C** | 652   | Batch   | 7          | 9    | 10              | **8.45**      | Interest Calculation    |
| 4    | **COCRDLIC** | 1,459 | Online  | 9          | 7    | 7               | **7.80**      | Card List               |
| 5    | **COCRDUPC** | 1,560 | Online  | 9          | 8    | 7               | **8.15**      | Card Update             |
| 6    | **CBSTM03A** | 924   | Batch   | 8          | 6    | 8               | **7.30**      | Statement Generation    |
| 7    | **COTRN02C** | 783   | Online  | 7          | 8    | 8               | **7.60**      | Transaction Add         |
| 8    | **COSGN00C** | 260   | Online  | 4          | 9    | 9               | **7.10**      | Authentication          |
| 9    | **COTRN00C** | 699   | Online  | 7          | 5    | 7               | **6.30**      | Transaction List        |
| 10   | **COBIL00C** | 572   | Online  | 6          | 8    | 8               | **7.20**      | Bill Payment            |

---

## Detailed Module Profiles

### #1 — COACTUPC (Account Update) — Score: 9.75

**File:** `app/cbl/COACTUPC.cbl` | **Lines:** 4,236 | **Type:** Online CICS

**Why it's the #1 hotspot:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Sheer Size**        | At 4,236 lines, it is 2.7× larger than the next largest program. Single monolithic module. |
| **Copybook Explosion**| 15+ unique copybooks; CSSETATY is COPY'd with REPLACING **39 separate times** for field attribute management — extreme macro expansion. |
| **Multi-File Mutation**| Writes to BOTH `ACCTDATA` and `CUSTDATA` VSAM files in a single transaction — cross-entity update. |
| **Complex Validation**| Extensive field-by-field validation with deeply nested IF/EVALUATE logic for every account attribute. |
| **Screen Complexity** | Manages the most complex BMS map (COACTUP) with the most input fields of any screen. |
| **State Management**  | Heavy use of COMMAREA fields and working storage flags to track edit state across pseudo-conversational interactions. |

**Complexity Indicators:**
- 39× COPY CSSETATY REPLACING (field attribute management)
- 1× COPY CSSTRPFY (page formatting)
- 1× COPY CSUTLDPY (date utility)
- 1× COPY CSUTLDWY (date working storage)
- EXEC CICS READ, REWRITE across Account + Customer files
- Deep EVALUATE/IF nesting for validation of every field

**Modernization Concern:** This single module will likely decompose into 3–5 Java service classes (AccountUpdateService, AccountValidator, AccountMapper, etc.). The COPY REPLACING pattern must be replaced with a reusable field-attribute utility class.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 9.20

**File:** `app/cbl/CBTRN02C.cbl` | **Lines:** 731 | **Type:** Batch

**Why it's critical:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Core Business Logic**| This is THE transaction posting engine — the heart of the batch cycle. Every financial transaction flows through this program. |
| **Multi-File Updates** | Reads DALYTRAN + CARDXREF + ACCTDATA + TRANSACT + TCATBAL; writes TRANSACT + TCATBAL + ACCTDATA — **5 files read, 3 files written**. |
| **Financial Integrity**| Handles balance updates, category balance accumulation, and transaction validation. Errors here = financial discrepancies. |
| **Downstream Impact**  | CBACT04C (interest calc), CBSTM03A (statements), CBTRN03C (reports) all depend on data this program produces. |
| **No Error Recovery**  | Batch program — if it fails mid-run, partial updates to VSAM files require manual recovery. |

**Modernization Concern:** Must be converted to a transactional service with proper ACID guarantees (Spring `@Transactional`). The multi-file update pattern maps to a database transaction spanning Account, Transaction, and CategoryBalance tables.

---

### #3 — CBACT04C (Interest Calculation) — Score: 8.45

**File:** `app/cbl/CBACT04C.cbl` | **Lines:** 652 | **Type:** Batch

**Why it's critical:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Financial Precision**| Performs interest rate calculations using disclosure group rates applied to category balances. Rounding and precision errors = regulatory risk. |
| **Multi-File I/O**    | Reads TCATBAL + DISCGRP + ACCTDATA + TRANSACT; writes ACCTDATA + TRANSACT + TCATBAL. |
| **Business Rules**    | Encodes interest accrual business rules — these must be precisely preserved during conversion. |
| **Date Dependency**   | Accepts processing date as JCL PARM (`PARM='2022071800'`) — date-driven calculation. |
| **Regulatory Impact** | Interest calculations are subject to Truth in Lending Act (TILA) / Regulation Z compliance. |

**Modernization Concern:** Requires exhaustive test coverage with known-good input/output pairs before conversion. Consider using `BigDecimal` with explicit rounding modes. Business rules must be extracted and documented as a formal specification.

---

### #4 — COCRDLIC (Card List) — Score: 7.80

**File:** `app/cbl/COCRDLIC.cbl` | **Lines:** 1,459 | **Type:** Online CICS

**Why it's complex:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Browse Logic**      | Implements CICS STARTBR/READNEXT/READPREV/ENDBR browse pattern with forward/backward paging — complex cursor management. |
| **Screen Array**      | Manages an array of card records displayed on screen with selection logic for drill-down. |
| **State Management**  | Must maintain browse position across pseudo-conversational CICS interactions (no persistent cursor). |
| **Copybook Count**    | 11 copybooks including CVCRD01Y, CVACT02Y, CSSTRPFY. |

**Modernization Concern:** The browse/paging pattern maps to paginated REST API endpoints. The CICS browse state management can be replaced with database OFFSET/LIMIT or cursor-based pagination.

---

### #5 — COCRDUPC (Card Update) — Score: 8.15

**File:** `app/cbl/COCRDUPC.cbl` | **Lines:** 1,560 | **Type:** Online CICS

**Why it's complex:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Card Data Mutation**| Writes to CARDDATA VSAM via REWRITE — modifies sensitive card information. |
| **Validation Logic**  | Extensive card field validation (CVV, expiration, embossed name, status). |
| **Cross-Entity Read** | Reads Customer data to display alongside card details — cross-entity join at presentation layer. |
| **PCI-DSS Sensitivity**| Card numbers, CVV codes — payment card data subject to PCI compliance. |
| **Copybook Dependencies**| 13 copybooks including CSSTRPFY for formatting. |

**Modernization Concern:** PCI-DSS compliance requirements must be preserved. Card number handling needs tokenization or encryption in the modernized system.

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.30

**File:** `app/cbl/CBSTM03A.CBL` | **Lines:** 924 | **Type:** Batch

**Why it's complex:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Dual Output**       | Generates BOTH plain text AND HTML statements in a single pass — two output formats. |
| **Subroutine Pattern**| Calls CBSTM03B **13 times** for file I/O operations — tight coupling between caller and callee. |
| **Multi-File Join**   | Reads TRXFL (sorted transactions), CARDXREF, ACCTDATA, CUSTDATA — performs a 4-way data join in batch. |
| **Report Formatting** | Complex report layout with headers, page breaks, account totals, grand totals. |
| **Error Handling**    | Calls CEE3ABD (Language Environment abnormal termination) on fatal errors. |

**Modernization Concern:** Maps to a Spring Batch job with an ItemReader/ItemProcessor/ItemWriter pattern. The HTML generation should use a template engine (Thymeleaf/Freemarker) rather than procedural string building.

---

### #7 — COTRN02C (Transaction Add) — Score: 7.60

**File:** `app/cbl/COTRN02C.cbl` | **Lines:** 783 | **Type:** Online CICS

**Why it's important:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Data Creation**     | Only online program that WRITES to TRANSACT file — creates new financial transactions. |
| **Multi-File Validation** | Reads ACCTDATA + CARDXREF to validate account/card before creating transaction. |
| **Date Handling**     | Calls CSUTLDTC twice for date validation on transaction dates. |
| **Financial Risk**    | Incorrect transaction creation = financial exposure. Must validate amounts, card status, account status. |
| **VSAM Browse**       | Uses STARTBR/READPREV/ENDBR to find next available transaction ID. |

**Modernization Concern:** Transaction creation should use database sequences for ID generation instead of the VSAM browse pattern. Validation logic maps to Spring Validation annotations + custom validators.

---

### #8 — COSGN00C (Sign-on / Authentication) — Score: 7.10

**File:** `app/cbl/COSGN00C.cbl` | **Lines:** 260 | **Type:** Online CICS

**Why it's high-risk despite low LOC:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Security Gateway**  | Every user session starts here — it's the authentication chokepoint. |
| **Plaintext Passwords**| Reads USRSEC VSAM and compares password in plaintext — major security vulnerability. |
| **Role Routing**      | Determines Admin vs. Regular user flow — authorization decision point. |
| **System Info Exposure**| Uses EXEC CICS ASSIGN to retrieve APPLID and SYSID — exposes system identifiers. |

**Modernization Concern:** Must be completely replaced with modern authentication (Spring Security, OAuth2/OIDC, bcrypt password hashing). This is not a conversion — it's a rewrite. Highest security priority.

---

### #9 — COTRN00C (Transaction List) — Score: 6.30

**File:** `app/cbl/COTRN00C.cbl` | **Lines:** 699 | **Type:** Online CICS

**Why it's notable:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Browse Pattern**    | Full STARTBR/READNEXT/READPREV/ENDBR implementation for transaction browsing. |
| **Navigation Hub**    | Routes to COTRN01C (view) and COTRN02C (add) — central transaction navigation. |
| **User-Facing**       | Primary screen for all transaction inquiries — high visibility. |

**Modernization Concern:** Similar to COCRDLIC — convert browse to paginated REST endpoint with query filters.

---

### #10 — COBIL00C (Bill Payment) — Score: 7.20

**File:** `app/cbl/COBIL00C.cbl` | **Lines:** 572 | **Type:** Online CICS

**Why it's high-risk:**

| Factor                | Detail                                                                                     |
|-----------------------|--------------------------------------------------------------------------------------------|
| **Financial Transaction**| Creates payment transactions that modify account balances — money movement. |
| **Multi-File**        | Reads ACCTDATA + CARDXREF + TRANSACT; writes payment transaction. |
| **Business Critical** | Bill payments are a core revenue-impacting function. Errors = customer complaints + financial loss. |
| **Validation**        | Must validate payment amount against balance, account status, card status. |

**Modernization Concern:** Payment processing should be an isolated, idempotent service with saga pattern for distributed transactions. Requires audit logging.

---

## Risk Heat Map

Visual summary of where complexity and risk concentrate:

```
                        Business Impact →
                   Low         Medium        High
              ┌──────────┬──────────────┬──────────────┐
         High │          │  COCRDLIC    │  COACTUPC    │
              │          │  COCRDUPC    │  CBTRN02C    │
  Complexity  │          │              │  CBACT04C    │
              ├──────────┼──────────────┼──────────────┤
       Medium │          │  COTRN00C    │  COTRN02C    │
              │          │  CBSTM03A    │  COBIL00C    │
              ├──────────┼──────────────┼──────────────┤
         Low  │          │              │  COSGN00C    │
              │          │              │  (high risk  │
              │          │              │   low code)  │
              └──────────┴──────────────┴──────────────┘
```

---

## Modernization Recommendations

### Priority Wave 1 — Security & Financial Core (Highest Risk)

| Module     | Action                                                                     | Effort |
|-----------|---------------------------------------------------------------------------|--------|
| COSGN00C  | **Rewrite** — Replace with Spring Security + OAuth2/OIDC + bcrypt          | Medium |
| CBTRN02C  | **Convert carefully** — Transaction posting with ACID guarantees           | High   |
| CBACT04C  | **Convert with exhaustive testing** — Interest calc needs precision tests  | High   |
| COBIL00C  | **Convert** — Payment service with idempotency + audit trail               | Medium |

### Priority Wave 2 — Account & Card Management (High Complexity)

| Module     | Action                                                                     | Effort |
|-----------|---------------------------------------------------------------------------|--------|
| COACTUPC  | **Decompose** — Split into 3-5 services (validation, update, mapping)      | Very High |
| COCRDUPC  | **Convert** — Card update service with PCI-DSS compliance                  | High   |
| COTRN02C  | **Convert** — Transaction creation with DB sequences                       | Medium |

### Priority Wave 3 — Inquiry & Reporting (Lower Risk)

| Module     | Action                                                                     | Effort |
|-----------|---------------------------------------------------------------------------|--------|
| COCRDLIC  | **Convert** — Paginated REST endpoint                                      | Medium |
| COTRN00C  | **Convert** — Paginated REST endpoint                                      | Medium |
| CBSTM03A  | **Convert** — Spring Batch job with template engine                        | High   |

### Cross-Cutting Concerns

| Concern                | Current State                              | Target State                                     |
|-----------------------|--------------------------------------------|-------------------------------------------------|
| Authentication        | Plaintext password comparison              | Spring Security + OAuth2 + bcrypt hashing        |
| Authorization         | Single flag (A/U) in USRSEC               | Role-based access control (RBAC)                 |
| Data Access           | Direct VSAM I/O (READ/WRITE/REWRITE)      | JPA/Spring Data repositories                     |
| Transaction Management| None (VSAM updates are immediate)          | Spring @Transactional with rollback              |
| Error Handling        | EXEC CICS HANDLE ABEND                     | Global exception handlers + circuit breakers     |
| Date Handling         | CSUTLDTC → CEEDAYS                        | java.time API                                    |
| Screen I/O            | BMS maps + 3270 terminal                   | REST API + web UI (React/Angular)                |
| Batch Processing      | JCL → COBOL batch programs                 | Spring Batch jobs with scheduling                |
| Report Generation     | COBOL string manipulation                  | Template engine (Thymeleaf/JasperReports)        |
| PII Protection        | Plaintext in VSAM (SSN, DOB, card data)    | Encryption at rest + field-level masking         |
