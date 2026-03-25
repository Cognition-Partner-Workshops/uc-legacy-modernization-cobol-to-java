# CardDemo Hotspot Report -- Top 10 Modules by Modernization Priority

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across code complexity, migration risk, and business impact  
> **Purpose:** Prioritize modules for modernization effort, testing investment, and team allocation

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Factors Considered |
|-----------|-------:|-------------------|
| **Complexity** | 35% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting), number of COPY statements, CICS commands, file I/O operations, CALL depth |
| **Risk** | 35% | Data mutation scope (R vs RW on how many files), financial calculation involvement, security sensitivity, multi-file transactions, error handling patterns |
| **Business Impact** | 30% | Revenue-critical path, user-facing frequency, regulatory/compliance exposure, downstream dependencies |

**Priority Score** = (Complexity x 0.35) + (Risk x 0.35) + (Business Impact x 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update
**Priority Score: 9.4** | Lines: 4,236 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 10 | Largest program in the codebase (4,236 lines). 40+ COPY CSSETATY REPLACING directives (dynamic attribute setting). 15+ EXEC CICS commands. Reads/writes ACCTDATA and CUSTDATA. Complex COMMAREA state management. HANDLE ABEND with custom recovery. |
| Risk | 9 | Writes to two master files (Account + Customer) in a single transaction. Field-level validation with 30+ attribute-setting patterns. Any conversion error directly corrupts account data. |
| Business Impact | 9 | Account updates are core to customer service operations. Errors affect balances, credit limits, and customer data integrity. Directly impacts regulatory reporting. |

**Migration Concerns:**
- The 40+ `COPY CSSETATY REPLACING` blocks implement dynamic field-level attribute control (protected/unprotected, highlighted/normal) -- requires careful UI framework mapping
- Dual-file update (Account + Customer) needs transaction management in Java (Spring `@Transactional`)
- At 4,236 lines, this program alone is ~20% of the entire online codebase

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)
**Priority Score: 8.8** | Lines: 731 | Type: Batch

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 8 | Reads DALYTRAN (daily transactions), validates against CARDXREF, updates TRANSACT master, updates ACCTDATA balances, updates TCATBAL category balances. Multi-file sequential + VSAM I/O with error handling. |
| Risk | 10 | Core financial posting engine. Writes to 3 master files (TRANSACT, ACCTDATA, TCATBAL) and a reject file. Incorrect posting means financial data corruption. This is the single most critical batch job. |
| Business Impact | 9 | Daily transaction posting is the heartbeat of the credit card system. Every purchase, payment, and adjustment flows through this program. Batch cycle cannot proceed without it. |

**Migration Concerns:**
- Maps to a Spring Batch job with `ItemReader`/`ItemProcessor`/`ItemWriter` pattern
- Must preserve exact posting logic (balance updates, category balance accumulation)
- Reject handling must be preserved -- rejected transactions go to DALYREJS
- Must support restart/recovery if batch fails mid-stream

---

### Rank 3: CBACT04C -- Interest Calculation
**Priority Score: 8.5** | Lines: 652 | Type: Batch

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 7 | Reads 5 VSAM files (ACCTDATA, CARDXREF, TCATBAL, DISCGRP, TRANSACT). Complex financial arithmetic with `S9(10)V99` decimal operations. Rate lookup by account group + transaction category. |
| Risk | 10 | Financial calculation directly affecting customer bills. Decimal precision errors compound over millions of accounts. Regulatory audit trail requirements. Interest rates must match disclosure terms. |
| Business Impact | 9 | Interest revenue is the primary income stream for credit card issuers. Calculation errors have direct financial and legal consequences. |

**Migration Concerns:**
- All arithmetic must use `BigDecimal` in Java -- never `double`/`float`
- Rate lookup logic (account group → disclosure group → rate) must be preserved exactly
- Rounding rules must match COBOL `ROUNDED` behavior (HALF-UP vs HALF-EVEN)
- Need comprehensive test cases with known expected outputs for validation

---

### Rank 4: COBIL00C -- Bill Payment
**Priority Score: 8.3** | Lines: 572 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 7 | Reads account balance, processes payment, creates transaction record, updates account balance. Uses ASKTIME/FORMATTIME for timestamps. STARTBR/READPREV for transaction ID generation. |
| Risk | 9 | Financial transaction: creates a payment record and reduces account balance. Multi-step operation (read balance → validate → write transaction → update balance) without explicit 2-phase commit. |
| Business Impact | 9 | Bill payment is a revenue-critical customer action. Payment processing errors affect customer satisfaction and regulatory compliance. |

**Migration Concerns:**
- The payment flow must be atomic -- in Java, wrap in `@Transactional`
- Transaction ID generation uses STARTBR/READPREV pattern (get last ID, increment) -- race condition risk in concurrent Java environment; use database sequences instead
- CICS ASKTIME → `java.time.Instant.now()`
- Must handle partial failure scenarios (payment record written but balance not updated)

---

### Rank 5: COCRDUPC -- Card Update
**Priority Score: 7.8** | Lines: 1,560 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 9 | Second-largest online program (1,560 lines). Complex screen handling with CSSTRPFY (strip/format). Multiple EXEC CICS commands (12+). Reads CARDDATA and CUSTDATA, writes CARDDATA. |
| Risk | 8 | Card data updates affect PCI-DSS sensitive fields (card number, CVV, expiration). Card status changes (active/inactive) directly impact transaction authorization. |
| Business Impact | 7 | Card management is essential but lower frequency than transaction operations. Errors could block legitimate card usage. |

**Migration Concerns:**
- PCI-DSS compliance: card data must be encrypted at rest and masked in UI
- CSSTRPFY copy-replacing pattern needs utility class in Java
- Field-level validation and attribute management (similar to COACTUPC but smaller scale)

---

### Rank 6: COCRDLIC -- Card List
**Priority Score: 7.5** | Lines: 1,459 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 8 | Third-largest online program. Implements forward/backward paging over VSAM using STARTBR/READNEXT/READPREV/ENDBR. Context-sensitive: admin sees all cards, regular user sees only own cards. XCTL to detail/update screens. |
| Risk | 7 | Read-only data access but complex cursor/browse logic. Incorrect paging could skip records or show wrong cards to wrong users. |
| Business Impact | 7 | Card listing is the entry point for all card management. Used frequently by both regular users and administrators. |

**Migration Concerns:**
- VSAM browse (STARTBR/READNEXT/READPREV/ENDBR) → JPA paginated query with `Pageable`
- Admin vs regular user filtering → Spring Security role-based query filtering
- The 3 XCTL targets (menu, detail, update) → URL routing with parameters

---

### Rank 7: CBSTM03A -- Statement Generation
**Priority Score: 7.4** | Lines: 924 | Type: Batch

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 8 | Reads 4 VSAM files. Produces dual-format output (plain text + HTML). 14 CALL statements to CBSTM03B subroutine for file I/O. Complex report formatting with page breaks, account totals, grand totals. |
| Risk | 7 | Statement accuracy is legally required. Format must match regulatory requirements. Customer-facing output -- errors damage trust. |
| Business Impact | 7 | Monthly statements are a regulatory requirement. Statements are the primary customer communication about their account activity. |

**Migration Concerns:**
- CBSTM03A + CBSTM03B should be migrated together as a unit
- Report generation → consider Apache PDFBox or JasperReports for modern output
- HTML generation logic is hand-coded (string concatenation) -- replace with a template engine
- The SORT step in CREASTMT.JCL (re-key by card+tran ID) must be preserved in the Spring Batch job

---

### Rank 8: COTRN02C -- Transaction Add (Online)
**Priority Score: 7.2** | Lines: 783 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 7 | CALL to CSUTLDTC for date validation. Reads ACCTDATA and CARDXREF for validation. Writes to TRANSACT. Uses STARTBR/READPREV for transaction ID generation. |
| Risk | 8 | Creates financial transaction records. Incorrect validation could allow invalid transactions. Transaction ID uniqueness must be guaranteed. |
| Business Impact | 7 | Manual transaction entry supports customer service adjustments, credits, and corrections. |

**Migration Concerns:**
- Date validation (CSUTLDTC → CEEDAYS) → `java.time.LocalDate.parse()` with validation
- Transaction ID generation (same STARTBR/READPREV pattern as COBIL00C) → database sequence
- Account/card-xref validation → JPA repository lookups with proper error messages

---

### Rank 9: COACTVWC -- Account View
**Priority Score: 7.0** | Lines: 941 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 7 | Reads 4 VSAM files (Account, Card, Card-Xref, Customer). Complex screen assembly with CSSTRPFY formatting. HANDLE ABEND with error page display. |
| Risk | 6 | Read-only -- no data mutation risk. However, incorrect display of balances or card data could mislead users. Privacy concern: shows customer PII. |
| Business Impact | 8 | Most frequently accessed screen in the system. Every user interaction starts with viewing account details. High visibility. |

**Migration Concerns:**
- Read-only but touches 4 entities → single JPA query with joins or multiple service calls
- CSSTRPFY formatting → Java `String.format()` or a display utility class
- HANDLE ABEND → try/catch with error page rendering

---

### Rank 10: COSGN00C -- Signon
**Priority Score: 6.9** | Lines: 260 | Type: Online CICS

| Dimension | Score | Justification |
|-----------|------:|---------------|
| Complexity | 5 | Small program (260 lines). Reads USRSEC file, validates credentials, routes to appropriate menu based on user type. Uses EXEC CICS ASSIGN for terminal info. |
| Risk | 9 | Authentication gateway for the entire system. Plaintext password comparison. No lockout mechanism, no session timeout, no password complexity enforcement. |
| Business Impact | 7 | Every user interaction begins here. Security vulnerability affects the entire application. |

**Migration Concerns:**
- **Critical:** Replace plaintext password storage with bcrypt/scrypt hashing
- Add session management (JWT or HTTP session with timeout)
- Add failed login lockout, password complexity rules, audit logging
- EXEC CICS ASSIGN (terminal ID) → HTTP request headers (IP, user-agent)
- Two XCTL targets (COMEN01C for users, COADM01C for admins) → role-based redirect after login

---

## Summary Ranking Table

| Rank | Program | Score | Lines | Type | Primary Risk Factor |
|------|---------|------:|------:|------|---------------------|
| 1 | COACTUPC | 9.4 | 4,236 | Online | Extreme complexity + dual-file writes |
| 2 | CBTRN02C | 8.8 | 731 | Batch | Core financial posting, 3 file writes |
| 3 | CBACT04C | 8.5 | 652 | Batch | Interest calculation precision |
| 4 | COBIL00C | 8.3 | 572 | Online | Financial payment transaction |
| 5 | COCRDUPC | 7.8 | 1,560 | Online | PCI-DSS card data mutation |
| 6 | COCRDLIC | 7.5 | 1,459 | Online | Complex paging + role-based filtering |
| 7 | CBSTM03A | 7.4 | 924 | Batch | Dual-format regulatory output |
| 8 | COTRN02C | 7.2 | 783 | Online | Financial record creation |
| 9 | COACTVWC | 7.0 | 941 | Online | High-traffic 4-file read |
| 10 | COSGN00C | 6.9 | 260 | Online | Authentication security gaps |

---

## Risk Heat Map

```
                    Low Business Impact ◄──────────► High Business Impact
                    │                                                    │
High Risk           │  COCRDUPC(5)        CBACT04C(3)   CBTRN02C(2)     │
                    │  COCRDLIC(6)        COBIL00C(4)                   │
                    │                     COTRN02C(8)                   │
                    │                     COSGN00C(10)                  │
                    │                                                    │
                    │                                                    │
Medium Risk         │                     CBSTM03A(7)                   │
                    │                     COACTVWC(9)                   │
                    │                                                    │
                    │                                                    │
Low Risk            │  COUSR00-03C        COMEN01C                      │
                    │  CBACT01-03C        COADM01C                      │
                    │  CBCUS01C           COTRN00C                      │
                    │  COBSWAIT           COTRN01C                      │
                    │                                                    │
High Complexity ◄───┴────────────────────────────────────────────────────┘
```

---

## Recommended Actions

### Immediate (Before Migration Starts)
1. **Create comprehensive test suites for Ranks 1-4** -- these programs require 90%+ test coverage before conversion
2. **Document the exact decimal arithmetic rules** in CBACT04C and CBTRN02C -- capture COBOL ROUNDED behavior
3. **Inventory all CICS COMMAREA fields** actually used vs. defined -- COCOM01Y has more fields than most programs use
4. **Security audit COSGN00C** -- plaintext passwords must be addressed in the modernized architecture

### During Migration
5. **Migrate COACTUPC last among online programs** -- it's the most complex and benefits from lessons learned on simpler programs
6. **Migrate CBTRN02C and CBACT04C together** -- they share files and run in sequence in the batch cycle
7. **Create parallel-run comparison framework** for financial programs -- run COBOL and Java side-by-side and compare outputs
8. **Use strangler fig pattern** -- migrate read-only screens first (Wave 5 in DEPENDENCY_MAP.md), then writes

### Testing Strategy
9. **Ranks 1-3 require parallel-run testing** (COBOL output vs Java output comparison) for at least 2 billing cycles
10. **Ranks 4-7 require regression test packs** built from production data samples (anonymized)
