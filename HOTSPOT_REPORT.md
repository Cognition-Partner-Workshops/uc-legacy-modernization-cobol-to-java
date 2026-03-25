# Hotspot Report -- CardDemo COBOL Codebase

> Top 10 modules prioritized by **complexity**, **risk**, and **business impact** for modernization planning.
> Scoring: each dimension rated 1-5 (5 = highest). Composite = Complexity + Risk + Business Impact.

---

## Hotspot Summary

| Rank | Module | Lines | Composite | Complexity | Risk | Biz Impact | Classification |
|------|--------|-------|-----------|------------|------|------------|----------------|
| 1 | COACTUPC | 4,236 | 14 | 5 | 5 | 4 | Online / Account Update |
| 2 | CBTRN02C | 731 | 13 | 4 | 5 | 4 | Batch / Transaction Posting |
| 3 | CBACT04C | 652 | 13 | 4 | 5 | 4 | Batch / Interest Calculation |
| 4 | COBIL00C | 572 | 12 | 3 | 4 | 5 | Online / Bill Payment |
| 5 | COCRDLIC | 1,459 | 12 | 4 | 4 | 4 | Online / Card List Browse |
| 6 | COCRDUPC | 1,560 | 12 | 4 | 4 | 4 | Online / Card Update |
| 7 | CBSTM03A | 924 | 12 | 4 | 4 | 4 | Batch / Statement Generation |
| 8 | COTRN02C | 783 | 11 | 3 | 4 | 4 | Online / Transaction Add |
| 9 | CBEXPORT | 582 | 11 | 4 | 4 | 3 | Batch / Data Export |
| 10 | COSGN00C | 260 | 11 | 2 | 5 | 4 | Online / Authentication |

---

## Detailed Analysis

### 1. COACTUPC -- Account Update (Rank 1, Score 14)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 5 | **Largest program in the codebase at 4,236 lines.** 38 COPY REPLACING invocations for CSSETATY (attribute byte setting). Multi-entity update: reads/writes ACCTDAT, reads/writes CUSTDAT, reads CXACAIX. Complex 9600-WRITE-PROCESSING paragraph with nested REWRITE operations on two files. Extensive input validation with field-level cursor positioning. |
| Risk | 5 | Modifies two master files (accounts and customers) in a single transaction. No explicit SYNCPOINT -- partial update risk if CICS task fails mid-write. 38 repetitive COPY REPLACING blocks create maintenance fragility. Any regression corrupts core financial records. |
| Biz Impact | 4 | Core account and customer maintenance. Every account modification (address, status, limits) flows through this program. High user-facing visibility. |

**Modernization Notes:**
- Extract account-update and customer-update into separate service methods
- Replace COPY REPLACING pattern with a reusable field-attribute utility
- Add transactional integrity (database transaction wrapping both updates)
- Consider splitting into Account Update and Customer Update microservices

---

### 2. CBTRN02C -- Transaction Posting (Rank 2, Score 13)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 731 lines. Multi-file batch processing: reads DALYTRAN sequentially, validates against CCXREF/ACCTDAT, writes to TRANSACT (indexed) and DALYREJS (rejects), updates TCATBAL (category balances). Contains 1500-VALIDATE-TRAN with multi-condition validation logic. Running counters for processed/rejected transactions. |
| Risk | 5 | **Core financial posting engine.** Any bug directly corrupts the transaction master file. Updates category balances that feed interest calculations. Reject file logic must be bulletproof -- lost transactions mean financial discrepancies. Multiple file dependencies create failure-cascade risk. |
| Biz Impact | 4 | Processes every daily transaction. The central hub connecting external transaction feeds to the internal ledger. Downstream impacts: interest calculation, reporting, statements all depend on correct posting. |

**Modernization Notes:**
- Map to Spring Batch job with chunk-oriented processing
- Implement idempotent posting with transaction IDs to prevent duplicates
- Add detailed audit logging for each posted/rejected transaction
- Consider event-driven architecture (post event -> update balances asynchronously)

---

### 3. CBACT04C -- Interest Calculation (Rank 3, Score 13)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 652 lines. Multi-step financial computation: reads TCATBAL sequentially, joins with DISCGRP for interest rates, computes interest per category, computes fees, accumulates per-account totals, writes interest transaction records, updates account balances. Account-break logic (detect when ACCT-ID changes). |
| Risk | 5 | **Direct financial calculation.** Incorrect interest rates or rounding errors compound across all accounts. Reads from 4 files (TCATBAL, CCXREF, ACCTDAT, DISCGRP), writes to 2 (TRANSACT, ACCTDAT). Account balance updates here affect all downstream operations. Regulatory compliance implications. |
| Biz Impact | 4 | Revenue-generating process. Interest charges are the primary revenue stream for credit card operations. Must be accurate to the cent. Subject to regulatory audit. |

**Modernization Notes:**
- Implement with BigDecimal for precise financial arithmetic
- Add configurable rounding rules (ROUND_HALF_UP for banking)
- Create separate interest-rate lookup service
- Implement reconciliation checks (pre/post balance verification)

---

### 4. COBIL00C -- Bill Payment (Rank 4, Score 12)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 3 | 572 lines. Moderate complexity but multi-step workflow: looks up account via CXACAIX, reads current balance, generates new transaction ID (STARTBR HIGH-VALUES + READPREV to find max ID, increment), creates payment transaction record, updates account balance. Confirmation flow (Y/N). |
| Risk | 4 | Creates financial transactions and updates account balances. The MAX-ID generation pattern (browse to end of file, read previous, add 1) is a concurrency risk -- two simultaneous payments could generate duplicate IDs. |
| Biz Impact | 5 | **Highest business impact.** Direct customer-facing payment function. Payment failures or double-charges have immediate customer impact and potential regulatory consequences. |

**Modernization Notes:**
- Replace MAX-ID browse pattern with database sequence or UUID
- Wrap payment + balance update in atomic transaction
- Add duplicate payment detection
- Implement payment confirmation with idempotency key

---

### 5. COCRDLIC -- Card List Browse (Rank 5, Score 12)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 1,459 lines. Complex CICS browse logic: STARTBR/READNEXT/READPREV with page-forward, page-backward, first-page, and last-page navigation. Maintains browse position state across pseudo-conversational transactions. Two separate browse routines (9000-READ-FORWARD, 9100-READ-BACKWARDS) with end-of-file detection. |
| Risk | 4 | Browse state management across pseudo-conversational CICS is error-prone. Incorrect ENDBR handling can leave file cursors open. Page navigation edge cases (empty result sets, single-page results, position after delete). |
| Biz Impact | 4 | Primary card lookup interface. Users must be able to find cards reliably to perform any card operation. Gateway to card view and card update functions. |

**Modernization Notes:**
- Replace with paginated REST endpoint using cursor-based pagination
- Implement server-side search/filter capabilities
- Add card number masking for PCI compliance in the UI layer

---

### 6. COCRDUPC -- Card Update (Rank 6, Score 12)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 1,560 lines. Similar pattern to COACTUPC but for card entity. READ-for-update + REWRITE pattern. Input validation for card fields (expiration date, status, embossed name). Multi-step flow: fetch data, display, accept changes, validate, confirm, write. |
| Risk | 4 | Modifies card master data. Incorrect card status changes could enable/disable cards improperly. Card data is PCI-sensitive. |
| Biz Impact | 4 | Card lifecycle management. Activating, deactivating, and updating cards is core operational functionality. |

**Modernization Notes:**
- Implement PCI DSS compliant data handling (encryption at rest, tokenization)
- Add audit trail for all card status changes
- Separate card data update from card status management

---

### 7. CBSTM03A -- Statement Generation (Rank 7, Score 12)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 924 lines. Multi-file join logic: reads transaction file (keyed by card+tran-id), joins with XREF to get customer/account, reads customer and account data for header. Calls CBSTM03B as sub-program. Generates both text and HTML output. ALTER statement usage (GO TO ... ALTERED) makes control flow hard to follow. |
| Risk | 4 | ALTER statement is considered an anti-pattern -- it modifies GO TO targets at runtime, making static analysis unreliable. Multi-file joins without error recovery could produce incomplete statements. Generates customer-facing documents. |
| Biz Impact | 4 | Customer statements are a regulatory requirement. Incorrect statements cause customer complaints and potential compliance issues. HTML output suggests modernization path toward digital statements. |

**Modernization Notes:**
- Replace ALTER/GO-TO pattern with structured method calls
- Split CBSTM03A/B into a single statement service with template engine
- Generate statements as PDF using a modern template framework
- Implement statement archival and retrieval service

---

### 8. COTRN02C -- Transaction Add (Rank 8, Score 11)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 3 | 783 lines. Input validation for transaction fields (amount, type, category, merchant). Cross-file validation: verifies account exists (ACCTDAT), verifies card exists (CCXREF/CXACAIX). Confirmation workflow. Transaction ID generation. |
| Risk | 4 | Creates financial transaction records. Input validation must be thorough -- accepting invalid transactions corrupts the ledger. Cross-file lookups add failure points. |
| Biz Impact | 4 | Manual transaction entry (used by operators). Important for adjustments, corrections, and manual postings that cannot go through the automated daily feed. |

**Modernization Notes:**
- Implement as REST POST endpoint with request validation
- Add authorization checks (role-based access for manual transaction creation)
- Implement maker-checker workflow for high-value transactions

---

### 9. CBEXPORT -- Data Export (Rank 9, Score 11)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | 582 lines. Reads 5 separate VSAM files (customer, account, xref, transaction, card) and writes a single polymorphic sequential file using the CVEXPORT copybook's REDEFINES structure. Multi-record-type output with header fields (record type, timestamp, sequence number, branch ID, region code). COMP and COMP-3 packed fields in export format. |
| Risk | 4 | Data migration utility. Incorrect export corrupts downstream import. COMP/COMP-3 fields require careful byte-level handling during modernization. The REDEFINES pattern means the same memory area represents different record types -- error-prone in conversion. |
| Biz Impact | 3 | Branch migration tool. Not part of daily operations but critical for modernization and data migration scenarios. |

**Modernization Notes:**
- Replace with database-to-database migration or ETL tool
- Convert COMP/COMP-3 packed decimal fields to standard numeric types
- Implement record-type discrimination using polymorphism or tagged unions
- Add checksum/validation for data integrity verification

---

### 10. COSGN00C -- Sign-on / Authentication (Rank 10, Score 11)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 2 | 260 lines. Relatively simple: accepts user ID and password, reads USRSEC file, compares credentials, routes to appropriate menu based on user type. |
| Risk | 5 | **Security gateway for the entire application.** Plain-text password comparison (SEC-USR-PWD stored in clear text in USRSEC VSAM). No password hashing, no account lockout, no session timeout enforcement at this level. Single point of entry -- any bypass compromises all functions. |
| Biz Impact | 4 | Every user interaction begins here. Authentication failures lock users out of all application functions. Must be modernized to meet current security standards. |

**Modernization Notes:**
- **Critical:** Implement password hashing (bcrypt/scrypt/argon2)
- Add account lockout after N failed attempts
- Implement session management with JWT or server-side sessions
- Add multi-factor authentication support
- Consider integration with enterprise identity provider (LDAP/SAML/OIDC)

---

## Risk Heat Map

```
                    Low Business Impact    Med Business Impact    High Business Impact
                   +--------------------+---------------------+---------------------+
High Complexity    |                    | CBEXPORT             | COACTUPC            |
                   |                    |                     | COCRDLIC, COCRDUPC  |
                   |                    |                     | CBSTM03A            |
                   +--------------------+---------------------+---------------------+
Med Complexity     |                    | COTRN02C            | CBTRN02C, CBACT04C  |
                   |                    |                     | COBIL00C            |
                   +--------------------+---------------------+---------------------+
Low Complexity     |                    |                     | COSGN00C            |
                   +--------------------+---------------------+---------------------+
```

---

## Modernization Priority Recommendations

### Wave 1 -- Immediate (Security & Financial Core)
1. **COSGN00C** -- Plain-text passwords are a critical security vulnerability
2. **CBTRN02C** -- Core posting engine; must be correct before any other migration
3. **CBACT04C** -- Interest calculation; revenue and compliance critical

### Wave 2 -- High Value (Customer-Facing Operations)
4. **COBIL00C** -- Customer payment function with concurrency risks
5. **COTRN02C** -- Manual transaction entry
6. **COACTUPC** -- Largest program; account/customer update

### Wave 3 -- Operational (Card Management & Reporting)
7. **COCRDLIC** -- Card browsing (complex CICS browse logic)
8. **COCRDUPC** -- Card updates (PCI compliance needs)
9. **CBSTM03A** -- Statement generation (ALTER anti-pattern)

### Wave 4 -- Migration Tooling
10. **CBEXPORT** -- Data export (needed for migration itself; COMP/COMP-3 handling)

---

## Complexity Drivers Summary

| Driver | Affected Modules | Modernization Concern |
|--------|-----------------|----------------------|
| Multi-file CICS READ/REWRITE | COACTUPC, COBIL00C, COCRDUPC | Transactional integrity across multiple tables |
| CICS pseudo-conversational browse | COCRDLIC, COTRN00C, COUSR00C | Stateless pagination design |
| Sequential-to-indexed posting | CBTRN02C | Batch-to-streaming or chunk processing |
| Financial arithmetic (PIC S9V99) | CBACT04C, COBIL00C, CBTRN02C | BigDecimal precision, rounding rules |
| ALTER GO-TO | CBSTM03A | Control flow restructuring |
| COPY REPLACING (x38) | COACTUPC | Template/utility method extraction |
| Plain-text passwords | COSGN00C | Password hashing, IAM integration |
| COMP/COMP-3 packed decimal | CBEXPORT, CBIMPORT | Byte-level data conversion |
| MAX-ID browse pattern | COBIL00C, COTRN02C | Database sequence/UUID generation |
| COMMAREA navigation | All 17 online programs | REST API routing, session management |
