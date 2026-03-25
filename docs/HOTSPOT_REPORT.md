# HOTSPOT REPORT - CardDemo COBOL Application

> **Generated**: 2026-03-25 | **Application**: CardDemo - Credit Card Management System
> **Purpose**: Top 10 modules prioritized by complexity, risk, and business impact for modernization planning

---

## 1. Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension          | Weight | Criteria                                                                      |
|--------------------|--------|-------------------------------------------------------------------------------|
| **Complexity**     | 40%    | LOC, cyclomatic complexity (EVALUATE/IF nesting), copybook count, VSAM I/O count, external calls |
| **Risk**           | 30%    | Data sensitivity (PII/PCI), financial calculations, multi-file updates, error paths, REWRITE operations |
| **Business Impact**| 30%    | Revenue criticality, user-facing frequency, downstream dependencies, batch cycle position |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Impact x 0.3)

---

## 2. Top 10 Hotspot Modules

### Rank #1: COACTUPC - Account Update

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COACTUPC.cbl`                                          |
| **Lines of Code**   | 4,236 (largest program in codebase)                              |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **10/10** - 14 copybooks, deep EVALUATE/IF nesting, field-level validation for 10+ editable fields, NUMVAL-C computations, lookup table (CSLKPCDY = 1,318 lines), string processing (CSSTRPFY, CSSETATY), attribute setting |
| **Risk**            | **9/10** - REWRITE to ACCTFILE (financial data), modifies credit limits, current balance, cash limits, cycle credits/debits. Incorrect update could corrupt account financials. Handles PCI-adjacent card data via XREF. |
| **Business Impact** | **9/10** - Core account maintenance function. Every balance adjustment, credit limit change, and status update flows through this program. Directly affects customer financial state. |
| **Composite Score** | **9.4**                                                          |
| **Modernization Notes** | Decompose into smaller services: AccountViewService, AccountUpdateService, AccountValidationService. Extract validation rules into a separate rules engine. The 1,318-line lookup copybook (CSLKPCDY) should become a database table. |

---

### Rank #2: CBTRN02C - Transaction Posting (Batch)

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/CBTRN02C.cbl`                                          |
| **Lines of Code**   | 731                                                              |
| **Type**            | Batch                                                            |
| **Complexity**      | **8/10** - 6 file I/O streams (DALYTRAN, TRANSACT, XREF, DALYREJS, ACCTFILE, TCATBALF), transaction validation logic, reject handling, multi-step posting with category balance updates |
| **Risk**            | **10/10** - **Highest risk module**. Writes to transaction master (TRANSACT), rejects file (DALYREJS), category balance (TCATBALF), and account file (ACCTFILE). A bug here corrupts the financial ledger. Sets RETURN-CODE=4 on rejects affecting downstream batch. |
| **Business Impact** | **10/10** - **Core batch processing**. Every daily transaction must pass through this program. Batch cycle cannot proceed without successful posting. Feeds CBACT04C (interest), CBSTM03A (statements), CBTRN03C (reports). |
| **Composite Score** | **9.2**                                                          |
| **Modernization Notes** | Convert to Spring Batch job with chunk-oriented processing. Implement database transactions for atomicity. Add comprehensive audit logging. Separate validation from posting into distinct steps. |

---

### Rank #3: CBACT04C - Interest Calculation

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/CBACT04C.cbl`                                          |
| **Lines of Code**   | 652                                                              |
| **Type**            | Batch                                                            |
| **Complexity**      | **8/10** - 5 input files (TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT), multi-level break logic on account number, interest rate lookup by group/type/category, COMPUTE statements for interest and fees |
| **Risk**            | **10/10** - **Financial calculation engine**. REWRITE to ACCTFILE modifying account balances. Incorrect interest calculation directly impacts revenue and regulatory compliance. Rate lookup via disclosure groups adds complexity. |
| **Business Impact** | **9/10** - Revenue-generating: interest charges are a primary income source. Must be accurate for regulatory compliance (TILA, Reg Z). Runs every batch cycle. |
| **Composite Score** | **8.9**                                                          |
| **Modernization Notes** | Implement as a dedicated financial calculation service with BigDecimal precision. Externalize rate tables to database with audit trail. Add reconciliation reports. Unit test every rate/fee scenario. |

---

### Rank #4: COCRDUPC - Card Update

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COCRDUPC.cbl`                                          |
| **Lines of Code**   | 1,560                                                            |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **8/10** - 10 copybooks, field-level input editing, card name/status/expiration validation, CSSTRPFY string processing, CSSETATY attribute management, multi-step update workflow (fetch-display-edit-confirm-save) |
| **Risk**            | **8/10** - REWRITE to CARDFILE with PCI-DSS data (card number, CVV, expiration). Card status changes affect transaction processing downstream. Embossed name changes have compliance implications. |
| **Business Impact** | **8/10** - Card maintenance is a daily operational function. Card status (active/inactive) directly controls whether transactions are authorized. |
| **Composite Score** | **8.0**                                                          |
| **Modernization Notes** | PCI-DSS compliance must be maintained during migration. Card numbers need tokenization. Separate card lifecycle management from display logic. |

---

### Rank #5: CBSTM03A - Statement Generation (Driver)

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/CBSTM03A.CBL`                                          |
| **Lines of Code**   | 924                                                              |
| **Type**            | Batch                                                            |
| **Complexity**      | **8/10** - Calls CBSTM03B subroutine for all file I/O, TIOT block parsing (low-level system), dual output format (text + HTML), ALTER statement usage (self-modifying code), multi-level break on card/transaction |
| **Risk**            | **7/10** - Statement accuracy is customer-facing. Errors in statement generation lead to disputes and regulatory issues. Uses ALTER (self-modifying code) which is error-prone and hard to debug. |
| **Business Impact** | **8/10** - Customer-facing deliverable. Statements are a regulatory requirement (monthly billing). Downstream input to TXT2PDF1 for PDF generation. |
| **Composite Score** | **7.7**                                                          |
| **Modernization Notes** | **High priority for modernization** due to ALTER usage (anti-pattern). Replace with modern template engine (Thymeleaf/JasperReports). Eliminate self-modifying code. Separate data gathering from formatting. |

---

### Rank #6: COBIL00C - Bill Payment

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COBIL00C.cbl`                                          |
| **Lines of Code**   | 572                                                              |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **6/10** - Payment amount validation, account lookup, balance update, transaction record creation, confirmation workflow |
| **Risk**            | **9/10** - REWRITE to ACCTFILE (balance update) + WRITE to TRANSACT. Double-write: if either fails, data is inconsistent. Financial impact of incorrect payment posting. No explicit SYNCPOINT for atomicity. |
| **Business Impact** | **8/10** - Customer-facing payment processing. Directly affects account balances and customer satisfaction. Revenue-impacting. |
| **Composite Score** | **7.5**                                                          |
| **Modernization Notes** | Critical to implement proper transaction management (database ACID transactions). Add idempotency keys to prevent double payments. Integrate with payment gateway. |

---

### Rank #7: COTRN02C - Transaction Add (Online)

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COTRN02C.cbl`                                          |
| **Lines of Code**   | 783                                                              |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **7/10** - Multi-field validation (account, card, type, category, amount, merchant), cross-reference lookups (CXACAIX alternate index + CCXREF), confirmation workflow (Y/N), NUMVAL conversions |
| **Risk**            | **8/10** - WRITE to TRANSACT (creates financial records). Validation bypass could create invalid transactions. Uses alternate index lookup which adds I/O complexity. |
| **Business Impact** | **7/10** - Manual transaction entry for adjustments, corrections, and special entries. Lower volume than batch but operationally important. |
| **Composite Score** | **7.3**                                                          |
| **Modernization Notes** | Convert to REST POST endpoint with Jakarta Bean Validation. Implement optimistic locking. Add transaction amount limits and fraud checks. |

---

### Rank #8: COCRDLIC - Card List

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COCRDLIC.cbl`                                          |
| **Lines of Code**   | 1,459                                                            |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **8/10** - CICS STARTBR/READNEXT/ENDBR browse logic, pagination (PF7/PF8), multi-record display (10 cards per page), cross-reference lookup per card, selection routing to view/update programs |
| **Risk**            | **6/10** - Read-only browse, but incorrect pagination or selection routing could send users to wrong card records. Handles card numbers (PCI scope). |
| **Business Impact** | **7/10** - Primary card discovery screen. Entry point for all card operations. High usage frequency. |
| **Composite Score** | **7.1**                                                          |
| **Modernization Notes** | Convert browse logic to paginated REST API with cursor-based pagination. Card numbers should be masked in list view (show last 4 digits only). |

---

### Rank #9: COACTVWC - Account View

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COACTVWC.cbl`                                          |
| **Lines of Code**   | 941                                                              |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **7/10** - 12 copybooks (most of any view-only program), reads 4 VSAM files (ACCT, CARD, CUST, XREF), complex screen layout with multiple data sources, navigation state management |
| **Risk**            | **5/10** - Read-only, but displays PII (customer SSN, address) and financial data (balances, limits). Incorrect data display could lead to wrong decisions. |
| **Business Impact** | **8/10** - Most frequently used screen after menu. Foundation for account inquiries. Used by both regular users and admin. |
| **Composite Score** | **6.7**                                                          |
| **Modernization Notes** | Good candidate for early migration as read-only. Convert to REST GET endpoint + React/Angular component. Apply field-level masking for PII. |

---

### Rank #10: COUSR00C - User List (Admin)

| Metric              | Value / Assessment                                               |
|---------------------|------------------------------------------------------------------|
| **File**            | `app/cbl/COUSR00C.cbl`                                          |
| **Lines of Code**   | 695                                                              |
| **Type**            | Online CICS                                                      |
| **Complexity**      | **7/10** - CICS browse with pagination (10 users/page), 10-slot selection evaluation (SEL0001-SEL0010), selection routing to update/delete, user ID filtering |
| **Risk**            | **7/10** - Gateway to user management. Selection routing errors could send admin to wrong user record. Plaintext password exposure in downstream COUSR02C. |
| **Business Impact** | **6/10** - Admin-only function but controls system access. Security implications if user management is compromised. |
| **Composite Score** | **6.7**                                                          |
| **Modernization Notes** | Replace plaintext password storage with bcrypt/scrypt hashing. Implement RBAC with Spring Security. Add audit logging for all user management operations. |

---

## 3. Consolidated Ranking Table

| Rank | Program   | LOC   | Type   | Complexity | Risk | Impact | **Score** | Domain              |
|------|-----------|-------|--------|-----------|------|--------|-----------|---------------------|
|  1   | COACTUPC  | 4,236 | Online | 10        | 9    | 9      | **9.4**   | Account Update      |
|  2   | CBTRN02C  | 731   | Batch  | 8         | 10   | 10     | **9.2**   | Transaction Posting |
|  3   | CBACT04C  | 652   | Batch  | 8         | 10   | 9      | **8.9**   | Interest Calc       |
|  4   | COCRDUPC  | 1,560 | Online | 8         | 8    | 8      | **8.0**   | Card Update         |
|  5   | CBSTM03A  | 924   | Batch  | 8         | 7    | 8      | **7.7**   | Statement Gen       |
|  6   | COBIL00C  | 572   | Online | 6         | 9    | 8      | **7.5**   | Bill Payment        |
|  7   | COTRN02C  | 783   | Online | 7         | 8    | 7      | **7.3**   | Transaction Add     |
|  8   | COCRDLIC  | 1,459 | Online | 8         | 6    | 7      | **7.1**   | Card List           |
|  9   | COACTVWC  | 941   | Online | 7         | 5    | 8      | **6.7**   | Account View        |
| 10   | COUSR00C  | 695   | Online | 7         | 7    | 6      | **6.7**   | User List           |

---

## 4. Modernization Wave Recommendations

### Wave 1 - Foundation (Low Risk, High Learning)
**Target**: Read-only programs to establish patterns without financial risk

| Program   | Rationale                                                  |
|-----------|------------------------------------------------------------|
| COACTVWC  | Read-only account view. Good pattern for VSAM-to-DB mapping. |
| COCRDLIC  | Read-only card list. Establishes pagination pattern.        |
| COSGN00C  | Simple authentication. Start with Spring Security setup.    |
| COMEN01C  | Menu navigation. Establishes routing framework.             |

### Wave 2 - Core Updates (Medium Risk, High Value)
**Target**: CRUD operations with financial data

| Program   | Rationale                                                  |
|-----------|------------------------------------------------------------|
| COACTUPC  | Largest program - decompose into microservices.             |
| COCRDUPC  | Card updates with PCI compliance requirements.              |
| COTRN02C  | Online transaction entry with validation.                   |
| COBIL00C  | Payment processing - needs proper ACID transactions.        |
| COUSR*    | User management suite - replace with Spring Security.       |

### Wave 3 - Batch Processing (Highest Risk, Highest Impact)
**Target**: Financial batch processing - requires extensive testing

| Program   | Rationale                                                  |
|-----------|------------------------------------------------------------|
| CBTRN02C  | Transaction posting - core of daily batch cycle.            |
| CBACT04C  | Interest calculation - revenue and compliance critical.     |
| CBSTM03A/B| Statement generation - eliminate ALTER anti-pattern.        |
| CBTRN03C  | Reporting - replace with modern reporting framework.        |
| CBEXPORT/CBIMPORT | Data migration - convert to ETL pipeline.           |

### Wave 4 - Optional Modules (Specialized)
**Target**: DB2/IMS/MQ integrations

| Module                | Rationale                                              |
|-----------------------|--------------------------------------------------------|
| Authorization (IMS)   | Complex IMS DLI + MQ. Convert to JPA + JMS/Kafka.     |
| Transaction Type (DB2)| Already SQL-based. Most straightforward DB2-to-JPA.   |
| VSAM-MQ              | MQ patterns. Convert to Spring JMS or Kafka.           |

---

## 5. Key Technical Debt & Anti-Patterns

| Issue                          | Location(s)          | Severity | Recommendation                            |
|--------------------------------|----------------------|----------|-------------------------------------------|
| ALTER statement (self-modifying code) | CBSTM03A      | **HIGH** | Eliminate - replace with EVALUATE/IF       |
| Plaintext passwords            | CSUSR01Y, COUSR*     | **HIGH** | Hash with bcrypt/scrypt in Java            |
| No SYNCPOINT in multi-write ops| COBIL00C, COACTUPC   | **HIGH** | Use database transactions for atomicity    |
| FILLER fields (30-50% of records)| All CV* copybooks   | MEDIUM   | Remove in Java; use properly-sized POJOs   |
| Monolithic COMMAREA            | COCOM01Y             | MEDIUM   | Replace with session/JWT tokens            |
| 1,318-line lookup table in code| CSLKPCDY             | MEDIUM   | Move to database reference table           |
| Hard-coded error messages      | All CO* programs     | LOW      | Externalize to resource bundles             |
| GOTO-equivalent (PERFORM THRU) | Multiple programs    | LOW      | Refactor to structured method calls         |
