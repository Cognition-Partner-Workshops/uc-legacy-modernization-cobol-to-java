# CardDemo Hotspot Report — Top 10 Modules

> **System**: CardDemo — Mainframe Credit Card Management System
> **Purpose**: Prioritize modules for modernization by complexity, risk, and business impact
> **Last Updated**: 2026-05-27

---

## Scoring Methodology

Each module is scored on three dimensions (1–5 scale each, 15 max):

| Dimension | Weight | Criteria |
|:----------|:-------|:---------|
| **Complexity** | 5 pts | LOC, branching depth (IF/EVALUATE), PERFORM count, copybook dependencies, CICS commands, CALL chains |
| **Risk** | 5 pts | Data mutation scope (REWRITE/WRITE), financial calculation logic, cross-file dependencies, error handling gaps, security sensitivity |
| **Business Impact** | 5 pts | Transaction volume (online vs batch), revenue-critical functions, regulatory exposure (billing, interest, PII), downstream dependencies |

---

## Top 10 Hotspot Ranking

| Rank | Program | LOC | Type | Score | Complexity | Risk | Business Impact |
|-----:|:--------|----:|:-----|------:|-----------:|-----:|----------------:|
| **1** | **COACTUPC** | 4,236 | Online | **14** | 5 | 5 | 4 |
| **2** | **CBTRN02C** | 731 | Batch | **14** | 4 | 5 | 5 |
| **3** | **CBACT04C** | 652 | Batch | **13** | 4 | 5 | 4 |
| **4** | **COTRTLIC** | 2,098 | Online | **12** | 5 | 4 | 3 |
| **5** | **COCRDUPC** | 1,560 | Online | **12** | 5 | 4 | 3 |
| **6** | **COTRTUPC** | 1,702 | Online | **11** | 5 | 3 | 3 |
| **7** | **CBSTM03A** | 924 | Batch | **11** | 4 | 3 | 4 |
| **8** | **COCRDLIC** | 1,459 | Online | **11** | 4 | 3 | 4 |
| **9** | **COPAUA0C** | 1,026 | Online | **11** | 4 | 4 | 3 |
| **10** | **COBIL00C** | 572 | Online | **10** | 3 | 4 | 3 |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 14/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 4,236 |
| IF Statements | 174 |
| EVALUATE Statements | 20 |
| PERFORM Calls | 64 |
| Copybook Dependencies | 58 |
| CICS Commands | 17 |

**Complexity (5/5)**: The largest program in the system at 4,236 lines — nearly 3× the next-largest core program. Contains 174 IF branches and 20 EVALUATE blocks creating deep nesting. Includes inline data validation with the CSLKPCDY lookup tables (phone area codes, state codes, ZIP code combinations), date validation via CSUTLDWY, and extensive field-by-field screen editing logic. References 9+ copybooks.

**Risk (5/5)**: Directly mutates three master files in a single transaction: Account (REWRITE), Customer (REWRITE), and Card (READ via AIX). Financial fields (balance, credit limit, cash limit) are editable. Any bug in validation or REWRITE logic could corrupt account balances. The program handles the full update lifecycle: read-for-update, validate all fields, display confirmation, then commit — a pattern prone to lost-update race conditions in CICS pseudo-conversational mode.

**Business Impact (4/5)**: Account updates are a core daily operation for call center representatives. Incorrect updates to credit limits or balances directly impact revenue, customer experience, and regulatory compliance.

**Modernization Recommendation**: Decompose into separate Account, Customer, and Card update services with explicit transaction boundaries. Extract validation rules into a reusable rules engine.

---

### #2 — CBTRN02C (Transaction Posting — POSTTRAN) — Score: 14/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 731 |
| IF Statements | 93 |
| EVALUATE Statements | 0 |
| PERFORM Calls | 62 |
| Copybook Dependencies | 6 |
| Files Accessed | 6 (DALYTRAN, TRANSACT, XREF, ACCOUNT, TCATBAL, DALYREJS) |

**Complexity (4/5)**: Dense batch logic with 93 IF statements in 731 lines — highest IF-density of any program (12.7%). Reads daily transactions, validates against XREF, posts to TRANSACT VSAM, updates account balances and category balances, and writes rejects to GDG. Multi-file I/O with referential integrity checks.

**Risk (5/5)**: This is the financial heart of the system. Every daily transaction flows through this program. It updates Account balances (CURR-BAL, CYC-CREDIT, CYC-DEBIT), Transaction Category Balances, and writes the posted transaction file. A bug here means incorrect balances for all accounts. The reject logic writes to GDG files — any misclassification sends valid transactions to rejects (or vice versa), causing reconciliation failures.

**Business Impact (5/5)**: Runs daily as POSTTRAN. All online transactions are meaningless until posted. Downstream processes (interest calculation, statements, reports) depend on correct posting. Any failure requires manual reconciliation. This is the single most business-critical batch program.

**Modernization Recommendation**: First candidate for comprehensive unit testing before any code changes. Consider event-driven architecture with individual transaction processing and idempotency guarantees.

---

### #3 — CBACT04C (Interest Calculation — INTCALC) — Score: 13/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 652 |
| IF Statements | 86 |
| EVALUATE Statements | 0 |
| PERFORM Calls | 57 |
| Copybook Dependencies | 6 |
| Files Accessed | 5 (ACCOUNT, XREF, DISCGRP, TCATBAL, TRANSACT) |

**Complexity (4/5)**: High IF-density (13.2%) implementing compound interest calculation logic. Reads disclosure group rates by account group + transaction type + category, then applies rates to category balances. Updates account balances and generates interest transaction records. Multiple nested lookups across 5 files.

**Risk (5/5)**: Directly computes and applies interest charges to customer accounts. Interest rate lookups from the Disclosure Group file determine charges. Any calculation error compounds monthly and affects all accounts. Financial regulatory exposure is high — incorrect interest charges violate Truth in Lending Act (TILA) / Regulation Z requirements.

**Business Impact (4/5)**: Runs monthly. Revenue-generating process — interest charges are a primary income source for credit card operations. Errors require statement corrections and regulatory reporting.

**Modernization Recommendation**: Extract interest calculation as a pure-function service with comprehensive test coverage against known-good calculation results. Implement decimal arithmetic with explicit rounding rules (currently uses COBOL implicit V99 decimal handling).

---

### #4 — COTRTLIC (Transaction Type List/Update/Delete — DB2) — Score: 12/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 2,098 |
| IF Statements | 99 |
| EVALUATE Statements | 32 |
| PERFORM Calls | 69 |
| Copybook Dependencies | 12 |
| CICS Commands | 12 |

**Complexity (5/5)**: Second-largest program overall. Combines CICS screen handling with embedded DB2 SQL (cursor operations, DELETE, UPDATE). 32 EVALUATE blocks handle complex state transitions between list/update/delete modes. Manages DB2 cursor lifecycle (OPEN/FETCH/CLOSE) within CICS pseudo-conversational flow.

**Risk (4/5)**: Modifies reference data (transaction types) used by transaction posting and reporting. Incorrect type data cascades to downstream batch processes. DB2 cursor management in CICS adds concurrency risk.

**Business Impact (3/5)**: Admin-only function for reference data maintenance. Changes are infrequent but affect all transaction classification.

**Modernization Recommendation**: Replace with a simple CRUD REST API backed by a relational database. The DB2 cursor pattern maps directly to a paginated list endpoint.

---

### #5 — COCRDUPC (Credit Card Update) — Score: 12/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 1,560 |
| IF Statements | 151 |
| EVALUATE Statements | 16 |
| PERFORM Calls | 26 |
| CICS Commands | 12 |

**Complexity (5/5)**: 151 IF statements — the second-highest absolute count. Extensive field validation for card number, expiration date, CVV, embossed name, and status. Multiple CICS READ and REWRITE operations. Complex screen state management.

**Risk (4/5)**: Modifies credit card master records including card status (active/inactive) and expiration dates. PCI-DSS implications — CVV display and card number handling require careful security controls during modernization.

**Business Impact (3/5)**: Regular operational function. Card updates (reissue, status change) are time-sensitive for customer service.

**Modernization Recommendation**: Implement as a card management microservice with PCI-DSS compliant data handling. Tokenize card numbers in the modernized system.

---

### #6 — COTRTUPC (Transaction Type Add/Edit — DB2) — Score: 11/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 1,702 |
| IF Statements | 108 |
| EVALUATE Statements | 26 |
| PERFORM Calls | 40 |
| CICS Commands | 12 |

**Complexity (5/5)**: Third-largest program. Similar DB2/CICS hybrid pattern as COTRTLIC. 108 IF statements with 26 EVALUATE blocks. Handles both INSERT and UPDATE SQL operations within screen-driven workflow.

**Risk (3/5)**: Creates/modifies reference data. Lower direct financial risk than master data updates, but incorrect transaction type definitions affect all categorization.

**Business Impact (3/5)**: Admin reference data maintenance.

**Modernization Recommendation**: Combine with COTRTLIC into a single Transaction Type Management service.

---

### #7 — CBSTM03A (Statement Generation) — Score: 11/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 924 |
| IF Statements | 15 |
| EVALUATE Statements | 9 |
| PERFORM Calls | 33 |
| Copybook Dependencies | 5 |
| Subroutine Calls | 12 (to CBSTM03B) |

**Complexity (4/5)**: Moderate size but architecturally complex — delegates all file I/O to CBSTM03B subroutine via 12 CALL invocations. Uses ALTER statement to dynamically modify GO TO targets (a rare and difficult-to-analyze COBOL feature). Reads from 3 VSAM files (TRANSACT, XREF, CUSTDAT) and generates both plain text and HTML statement output.

**Risk (3/5)**: Read-only against master files (no REWRITE). Output accuracy affects customer communications but does not corrupt data. The ALTER statement creates unpredictable control flow that could mask bugs.

**Business Impact (4/5)**: Monthly customer statements are a regulatory requirement and primary customer communication channel. Incorrect statements trigger complaints and regulatory scrutiny.

**Modernization Recommendation**: Replace with a template-based document generation service. Eliminate the ALTER pattern entirely — it has no modern equivalent and is widely considered an anti-pattern.

---

### #8 — COCRDLIC (Credit Card List) — Score: 11/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 1,459 |
| IF Statements | 131 |
| EVALUATE Statements | 18 |
| PERFORM Calls | 34 |
| CICS Commands | 18 |

**Complexity (4/5)**: 131 IF statements with 18 CICS commands — highest CICS command count of any program. Implements forward/backward paging via STARTBR/READNEXT/READPREV on VSAM. Complex screen state management for card selection and navigation.

**Risk (3/5)**: Read-only — lists cards and navigates to view/update screens. No direct data mutation but serves as the gateway to card updates.

**Business Impact (4/5)**: Primary card inquiry function used by all operators. Performance is critical for call center response times.

**Modernization Recommendation**: Implement as a paginated card search API. The VSAM browse pattern maps to a database cursor or keyset pagination.

---

### #9 — COPAUA0C (Process Authorization Requests — MQ) — Score: 11/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 1,026 |
| IF Statements | 52 |
| EVALUATE Statements | 10 |
| PERFORM Calls | 38 |
| CICS Commands | 12 |

**Complexity (4/5)**: Multi-technology program: CICS + MQ + IMS DL/I. Processes incoming authorization requests from MQ queues, validates against IMS database, writes authorization records, and sends response via MQ. Three distinct technology stacks in one program.

**Risk (4/5)**: Real-time authorization decisions. Writes to IMS database and DB2. An incorrect authorization (approve when should decline, or vice versa) has immediate financial impact. MQ trigger pattern requires careful error handling for message integrity.

**Business Impact (3/5)**: Part of the optional authorization module. Critical for fraud prevention when deployed.

**Modernization Recommendation**: Decompose into an event-driven authorization service. Replace MQ with a modern message broker. IMS data access can be migrated to a relational store.

---

### #10 — COBIL00C (Bill Payment) — Score: 10/15

| Metric | Value |
|:-------|------:|
| Lines of Code | 572 |
| IF Statements | 10 |
| EVALUATE Statements | 18 |
| PERFORM Calls | 38 |
| CICS Commands | 13 |

**Complexity (3/5)**: Moderate size with 18 EVALUATE blocks driving screen state. Reads account, cross-reference, and transaction files. Writes both a payment transaction and updates account balance.

**Risk (4/5)**: Directly processes customer payments. Updates account balance (REWRITE) and creates a new transaction record (WRITE). Payment amounts must be validated and applied atomically. Double-payment or missed-payment bugs have direct financial impact.

**Business Impact (3/5)**: Customer-facing payment function. Critical for cash flow and customer satisfaction but lower volume than daily transaction posting.

**Modernization Recommendation**: Implement as a payment processing service with idempotency keys to prevent duplicate payments.

---

## Migration Priority Matrix

```
                    Business Impact →
                    Low        Medium       High
              ┌──────────┬──────────┬──────────┐
    High      │ COTRTUPC │ COCRDLIC │ COACTUPC │
    ↑         │   (#6)   │   (#8)   │   (#1)   │
              ├──────────┼──────────┼──────────┤
Complexity    │ COTRTLIC │ COPAUA0C │ CBSTM03A │
    ↑         │   (#4)   │   (#9)   │   (#7)   │
              ├──────────┼──────────┼──────────┤
    Low       │ COCRDUPC │ COBIL00C │ CBTRN02C │
              │   (#5)   │  (#10)   │   (#2)   │
              │          │          │ CBACT04C │
              │          │          │   (#3)   │
              └──────────┴──────────┴──────────┘
```

### Recommended Migration Waves

| Wave | Programs | Rationale |
|:-----|:---------|:----------|
| **Wave 1 — Foundation** | CBTRN02C, CBACT04C | Highest risk + impact. Batch programs with no UI dependency. Can be modernized and tested independently. Start here to de-risk the financial engine. |
| **Wave 2 — Core UI** | COACTUPC, COBIL00C, COCRDLIC, COCRDUPC | Core online CRUD operations. Depend on Wave 1 services for data integrity. Largest code volumes — decomposition yields highest LOC reduction. |
| **Wave 3 — Reporting** | CBSTM03A/B, CBTRN03C, CORPT00C | Read-only programs generating statements and reports. Lower risk. Can run in parallel with legacy during transition. |
| **Wave 4 — Reference Data** | COTRTLIC, COTRTUPC, COBTUPDT | DB2 optional modules. Simple CRUD that maps cleanly to REST APIs. |
| **Wave 5 — Integration** | COPAUA0C, COPAUS0C/1C/2C, COACCT01, CODATE01 | MQ/IMS programs. Require messaging infrastructure modernization. Deploy after core services are stable. |
| **Wave 6 — Admin & Utility** | COUSR00-03C, COSGN00C, COMEN01C, COADM01C | Navigation and security. Replace with modern authentication and UI framework as final step. |

---

## Risk Flags for Modernization

| Flag | Programs | Description |
|:-----|:---------|:------------|
| **COMP-3 / Packed Decimal** | CBACT04C, COPAUA0C, CBEXPORT | Binary-coded decimal arithmetic. Java `BigDecimal` must match COBOL rounding behavior exactly. |
| **ALTER Statement** | CBSTM03A | Dynamic GO TO modification. No modern equivalent — requires complete restructuring. |
| **VSAM AIX** | COCRDLIC, COACTVWC, COACTUPC | Alternate index access patterns. Must be mapped to database secondary indexes. |
| **GDG Files** | CBTRN02C, TRANBKP | Generation Data Groups for versioned backups. Replace with timestamped storage or log-structured files. |
| **EBCDIC Data** | All batch | All flat files stored in EBCDIC encoding. Code page conversion required during migration. |
| **Pseudo-Conversational CICS** | All online | Programs exit and restart between user interactions. Session state passed via COMMAREA. Maps to stateless REST with session tokens. |
| **Embedded SQL** | COTRTLIC, COTRTUPC, COBTUPDT | DB2 embedded SQL with DCLGEN copybooks. Extract SQL and map to JPA/JDBC. |
| **DL/I Calls** | COPAUA0C, COPAUS0C/1C, PAUDBLOD, PAUDBUNL | IMS hierarchical database access. Requires data model flattening for relational migration. |
| **MQ Trigger** | COPAUA0C | MQ-initiated program. Replace with message consumer pattern (JMS, Kafka, etc.). |
| **PII Exposure** | CSUSR01Y, CVCUS01Y | SSN, DOB, passwords stored in plaintext. Encryption and tokenization required in target system. |
