# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Purpose:** Identify the top 10 highest-risk modules for modernization prioritization

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale each):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting), number of PERFORM branches, number of files accessed, COPY inclusions, use of REDEFINES/ALTER/GO TO |
| **Risk** | 35% | Financial data mutation, multi-file I/O with REWRITE/DELETE, error handling gaps, tight coupling to other modules, use of legacy constructs (ALTER, GO TO, COMP-3) |
| **Business Impact** | 30% | Criticality to daily operations, revenue impact, regulatory exposure (PII/PCI), number of downstream dependents |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update (Online)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 10/10 | 4,236 lines — largest program in the codebase. Extensive input validation (SSN, phone, dates, FICO score). Deep nested EVALUATE/IF logic. 11+ copybook inclusions including CSLKPCDY (1,318-line lookup table). REDEFINES for SSN, phone number parsing. Date validation via CSUTLDWY. |
| **Risk** | 10/10 | Mutates ACCTDATA and CUSTDATA via REWRITE. Handles PII: SSN, DOB, phone, address. PCI-adjacent: credit limits, balances. Single program handles both account AND customer updates — blast radius is enormous. |
| **Business Impact** | 9/10 | Core account management function. Every account change flows through this program. Regulatory audit trail implications. |
| **Composite** | **9.7** | |

**Modernization Concerns:**
- Must decompose into separate Account and Customer update services
- PII handling requires encryption in target platform
- Input validation logic should become a shared validation library
- 88-level condition names (50+) need careful mapping to Java enums/constants

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 731 lines. Accesses 6 files simultaneously (DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTDATA, TCATBAL). Multi-step validation pipeline. Creates/updates records across 3 VSAM files in a single run. DB2-format timestamp generation. |
| **Risk** | 10/10 | Core financial posting — writes transactions to master, updates account balances, updates category balances. Reject handling writes to DALYREJS. A bug here means incorrect balances across all accounts. No transaction rollback mechanism. |
| **Business Impact** | 10/10 | Daily batch cycle critical path. Every transaction in the system passes through this program. Financial accuracy depends entirely on this module. |
| **Composite** | **9.3** | |

**Modernization Concerns:**
- Must implement proper database transactions with rollback in Java
- Reject handling should become a dead-letter queue pattern
- Multi-file update atomicity is a major risk during conversion
- Balance update logic is the most financially sensitive code in the system

---

### Rank 3: CBACT04C — Interest Calculation (Batch)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 652 lines. Reads 5 files (TCATBAL, XREFFILE, DISCGRP, ACCTDATA, TRANSACT). Complex financial computation: interest rate lookup by disclosure group, interest computation, fee calculation. REWRITE to account file. |
| **Risk** | 9/10 | Direct financial impact — calculates and applies interest charges. Updates account balances. Creates new transaction records for interest charges. Rate lookup logic is business-critical. |
| **Business Impact** | 10/10 | Revenue-generating process. Interest calculation accuracy is auditable. Regulatory compliance (Truth in Lending Act). |
| **Composite** | **9.0** | |

**Modernization Concerns:**
- Financial precision: COMP-3 and S9(10)V99 must map to Java BigDecimal
- Interest rate lookup logic must be preserved exactly
- Need comprehensive regression testing with known-good calculation results
- Consider extracting interest rate rules into a configurable rules engine

---

### Rank 4: COTRTLIC — Transaction Type List/Delete (DB2, Optional)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 9/10 | 2,098 lines. Embedded SQL (DB2 cursors, FETCH, DELETE). CICS BMS screen handling with pagination. Complex screen navigation with PF keys. Dual-technology: CICS + DB2. |
| **Risk** | 7/10 | DELETE operations on transaction type reference data. DB2 SYNCPOINT for commit control. Configuration data changes affect all transaction processing. |
| **Business Impact** | 6/10 | Configuration management — less frequent but impacts transaction categorization system-wide. |
| **Composite** | **7.4** | |

**Modernization Concerns:**
- DB2 embedded SQL must convert to JPA/JDBC
- CICS pseudo-conversational pattern needs complete redesign for web
- Cursor-based pagination maps to Spring Data pagination
- SYNCPOINT logic maps to @Transactional boundaries

---

### Rank 5: COTRTUPC — Transaction Type Add/Update (DB2, Optional)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 9/10 | 1,702 lines. Embedded SQL (INSERT, UPDATE, SELECT). CICS + DB2 dual technology. Complex input validation. COPY of CSSETATY with REPLACING clause (conditional compilation). |
| **Risk** | 7/10 | INSERT/UPDATE to DB2 tables. SYNCPOINT commit control. Validation logic determines what transaction types are allowed. |
| **Business Impact** | 6/10 | Configuration management for transaction types. |
| **Composite** | **7.4** | |

**Modernization Concerns:**
- Same DB2/CICS concerns as COTRTLIC
- REPLACING clause in COPY statement is an unusual construct
- Input validation should become a shared service

---

### Rank 6: COCRDUPC — Card Update (Online)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 1,560 lines. Multi-screen workflow. Date validation via CSUTLDWY. Multiple VSAM file access (CARDDATA, CARDXREF). Complex 88-level validation flags. |
| **Risk** | 8/10 | Mutates credit card data (REWRITE). PCI-DSS scope: card numbers, CVV, expiration dates, active status. Card activation/deactivation is security-critical. |
| **Business Impact** | 8/10 | Card lifecycle management. Incorrect updates could activate/deactivate cards improperly. |
| **Composite** | **8.0** | |

**Modernization Concerns:**
- PCI-DSS compliance: card data must be encrypted/tokenized in Java
- CVV should never be stored in plaintext in the target system
- Card status changes need audit logging

---

### Rank 7: COCRDLIC — Card List (Online)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 1,459 lines. Paginated browse with STARTBR/READNEXT/READPREV CICS commands. Complex page-forward/page-back logic. Multiple screen selection processing (select card for view/update). |
| **Risk** | 5/10 | Read-only display, but card numbers shown on screen (PCI scope). Pagination bugs could skip or duplicate records. |
| **Business Impact** | 7/10 | Primary card lookup interface. Downstream navigation to card view/update. |
| **Composite** | **6.7** | |

**Modernization Concerns:**
- CICS STARTBR/READNEXT pagination must become database cursor or offset pagination
- Card number masking required in web UI (show last 4 digits only)
- Selection processing pattern needs redesign for web paradigm

---

### Rank 8: CBSTM03A — Statement Generation (Batch)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 924 lines. Uses ALTER and GO TO (legacy control flow — hardest to convert). Calls CBSTM03B subroutine repeatedly. Generates dual output: plain text + HTML. 2D array processing. Mainframe control block addressing. |
| **Risk** | 7/10 | ALTER statement modifies program flow at runtime — extremely difficult to trace and test. Customer-facing output (statements). Multi-format generation. |
| **Business Impact** | 8/10 | Customer statements are a regulatory requirement. Billing accuracy. Customer-facing document. |
| **Composite** | **7.7** | |

**Modernization Concerns:**
- **ALTER/GO TO is the #1 conversion risk** — must be refactored to structured control flow before or during conversion
- Dual-format output (text + HTML) should become a template engine (Thymeleaf, FreeMarker)
- CALL to CBSTM03B subroutine should become a service dependency
- Consider using a reporting framework (JasperReports) in Java

---

### Rank 9: COPAUS0C — Authorization Summary (IMS/DB2/MQ, Optional)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 8/10 | 1,032 lines. Triple-technology: CICS + DB2 + MQ. Complex message processing. Screen display with DB2 cursor-based data. |
| **Risk** | 7/10 | Authorization decisions affect transaction approval. MQ message handling must be reliable. DB2 queries for pending authorizations. |
| **Business Impact** | 7/10 | Fraud detection and authorization workflow. |
| **Composite** | **7.3** | |

**Modernization Concerns:**
- Three middleware dependencies (CICS, DB2, MQ) all need replacement
- MQ messaging maps to JMS or Spring Cloud Stream
- Authorization logic is security-critical

---

### Rank 10: COACTVWC — Account View (Online)

| Metric | Score | Details |
|--------|-------|---------|
| **Complexity** | 7/10 | 941 lines. Reads from 3 VSAM files (ACCTDATA, CARDXREF, CUSTDATA). Screen population logic. Navigation to COACTUPC for updates. |
| **Risk** | 5/10 | Read-only, but displays sensitive financial data (balances, credit limits). Entry point to account update workflow. |
| **Business Impact** | 8/10 | Primary account inquiry screen. Most-used function by customer service representatives. |
| **Composite** | **6.6** | |

**Modernization Concerns:**
- Multi-file join logic must become a database JOIN or service aggregation
- Sensitive data display needs role-based access control
- High usage frequency means performance is critical in the target platform

---

## Summary Ranking Table

| Rank | Program | Lines | Complexity | Risk | Biz Impact | Composite | Domain |
|------|---------|-------|-----------|------|-----------|-----------|--------|
| 1 | COACTUPC | 4,236 | 10 | 10 | 9 | **9.7** | Account Update |
| 2 | CBTRN02C | 731 | 8 | 10 | 10 | **9.3** | Transaction Posting |
| 3 | CBACT04C | 652 | 8 | 9 | 10 | **9.0** | Interest Calculation |
| 4 | COCRDUPC | 1,560 | 8 | 8 | 8 | **8.0** | Card Update |
| 5 | CBSTM03A | 924 | 8 | 7 | 8 | **7.7** | Statement Generation |
| 6 | COTRTLIC | 2,098 | 9 | 7 | 6 | **7.4** | Tran Type List (DB2) |
| 7 | COTRTUPC | 1,702 | 9 | 7 | 6 | **7.4** | Tran Type Update (DB2) |
| 8 | COPAUS0C | 1,032 | 8 | 7 | 7 | **7.3** | Auth Summary (MQ/DB2) |
| 9 | COCRDLIC | 1,459 | 8 | 5 | 7 | **6.7** | Card List |
| 10 | COACTVWC | 941 | 7 | 5 | 8 | **6.6** | Account View |

---

## Recommended Modernization Sequencing

### Wave 1 — Foundation (Low risk, high learning)
1. **COACTVWC** (Account View) — read-only, good first CICS→REST conversion
2. **COCRDLIC** (Card List) — pagination pattern, teaches VSAM→DB migration
3. **Utility programs** (CSUTLDTC, COBSWAIT) — small, isolated

### Wave 2 — Core Batch (High value, moderate risk)
4. **CBTRN03C** (Transaction Report) — read-only batch, introduces batch framework
5. **CBSTM03A/B** (Statement Generation) — requires ALTER refactoring first
6. **CBEXPORT/CBIMPORT** — data migration utilities, useful for testing

### Wave 3 — Financial Core (Highest risk, requires extensive testing)
7. **CBTRN02C** (Transaction Posting) — critical path, needs parallel-run testing
8. **CBACT04C** (Interest Calculation) — financial precision, needs calculation verification
9. **COBIL00C** (Bill Payment) — financial transaction creation

### Wave 4 — Full CRUD (Complex UI + data mutation)
10. **COACTUPC** (Account Update) — decompose first, then convert
11. **COCRDUPC** (Card Update) — PCI compliance in target
12. **COUSR00C-03C** (User Management) — auth/security redesign

### Wave 5 — Optional Modules (Multi-technology)
13. **DB2 programs** (COTRTLIC, COTRTUPC) — embedded SQL conversion
14. **MQ programs** (COPAUS0C, COACCT01, CODATE01) — messaging redesign
15. **IMS programs** (DBUNLDGS, PAUDBLOD, PAUDBUNL) — hierarchical→relational
