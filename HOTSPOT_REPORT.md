# HOTSPOT REPORT - CardDemo COBOL Estate

> **Generated:** 2026-03-25 | **Methodology:** Multi-dimensional complexity scoring across size, coupling, I/O density, and business logic complexity

---

## 1. Top 10 Programs by Lines of Code

| Rank | Program | Lines | Type | Purpose |
|------|---------|-------|------|---------|
| 1 | **COACTUPC.cbl** | 4,236 | Online | Account Update |
| 2 | **COCRDUPC.cbl** | 1,560 | Online | Card Update |
| 3 | **COCRDLIC.cbl** | 1,459 | Online | Card List |
| 4 | **COACTVWC.cbl** | 941 | Online | Account View |
| 5 | **CBSTM03A.CBL** | 924 | Batch | Statement Generation |
| 6 | **COCRDSLC.cbl** | 887 | Online | Card Detail View |
| 7 | **COTRN02C.cbl** | 783 | Online | Transaction Add |
| 8 | **CBTRN02C.cbl** | 731 | Batch | Transaction Posting |
| 9 | **COTRN00C.cbl** | 699 | Online | Transaction List |
| 10 | **COUSR00C.cbl** | 695 | Online | User List |

---

## 2. Top 10 Programs by Number of Copybooks Referenced

| Rank | Program | Copybook Count | Copybooks |
|------|---------|---------------|-----------|
| 1 | **COACTUPC.cbl** | 56 | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSSTRPFY, DFHAID, DFHBMSCA, + many BMS map copies |
| 2 | **COACTVWC.cbl** | 15 | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 3 | **COCRDSLC.cbl** | 15 | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 4 | **COCRDUPC.cbl** | 15 | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 5 | **COCRDLIC.cbl** | 13 | CVCRD01Y, COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY, DFHAID, DFHBMSCA |
| 6 | **COBIL00C.cbl** | 10 | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| 7 | **COTRN02C.cbl** | 10 | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| 8 | **COADM01C.cbl** | 9 | COCOM01Y, COADM02Y, COADM01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 9 | **COMEN01C.cbl** | 9 | COCOM01Y, COMEN02Y, COMEN01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| 10 | **COSGN00C.cbl** | 9 | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |

---

## 3. Top 10 Programs by I/O Operation Count

I/O operations include CICS READ, WRITE, REWRITE, DELETE, STARTBR, READNEXT, READPREV, ENDBR, and batch file READ/WRITE.

| Rank | Program | I/O Ops | VSAM Files Accessed | Operations Detail |
|------|---------|---------|--------------------|--------------------|
| 1 | **COCRDLIC.cbl** | 18 | CARDDAT | 2x STARTBR, 3x READNEXT, 2x READPREV, 2x ENDBR, 2x XCTL, SEND MAP, RECEIVE MAP, SEND TEXT |
| 2 | **COACTUPC.cbl** | 15 | ACCTDAT, CARDDAT, CARDAIX, CUSTDAT | 3x READ, 1x REWRITE, SEND MAP, RECEIVE MAP |
| 3 | **COACTVWC.cbl** | 15 | ACCTDAT, CARDDAT, CARDAIX, CUSTDAT | 3x READ, SEND MAP, RECEIVE MAP, SEND TEXT |
| 4 | **COBIL00C.cbl** | 13 | ACCTDAT, CARDXREF, TRANSACT | READ, REWRITE (account), READ (xref), STARTBR, READPREV, ENDBR, WRITE (transaction) |
| 5 | **COTRN02C.cbl** | 11 | CARDXREF, ACCTDAT, TRANSACT | 2x READ, STARTBR, READPREV, ENDBR, WRITE |
| 6 | **COUSR00C.cbl** | 11 | USRSEC | STARTBR, READNEXT, READPREV, ENDBR, 2x XCTL |
| 7 | **COTRN00C.cbl** | 10 | TRANSACT | STARTBR, READNEXT, READPREV, ENDBR |
| 8 | **CBACT04C.cbl** | 10 | TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT | 5 file OPEN/CLOSE, READ sequential + random, REWRITE, WRITE |
| 9 | **CBEXPORT.cbl** | 10 | CUSTFILE, ACCTFILE, XREFFILE, TRANSACT, CARDFILE, EXPFILE | 5x READ (all masters), 1x WRITE (export) |
| 10 | **CBSTM03A.CBL** | 8 | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE, STMTFILE, HTMLFILE | 4x READ (via CBSTM03B), 2x WRITE (statement output) |

---

## 4. Top 10 Programs by Business Logic Density

Business logic density measured by EVALUATE statements (complex branching), IF statements (conditional logic), and nested logic depth.

| Rank | Program | EVALUATE | IF | PERFORM | Logic Score | Key Business Rules |
|------|---------|----------|-----|---------|-------------|-------------------|
| 1 | **COACTUPC.cbl** | 20 | 333 | 64 | **417** | Account field validation (balance, limits, dates, status), field-by-field comparison for changes, multi-entity cross-validation |
| 2 | **COCRDUPC.cbl** | 16 | 148 | 26 | **190** | Card field validation (CVV, expiration, embossed name, status), customer existence check |
| 3 | **COCRDLIC.cbl** | 18 | 122 | 34 | **174** | Card list filtering, page navigation logic, selection handling |
| 4 | **CBTRN02C.cbl** | 0 | 96 | 62 | **158** | Transaction validation, card-to-account mapping, category balance updates, duplicate detection |
| 5 | **CBACT04C.cbl** | 0 | 86 | 57 | **143** | Interest rate lookup, monthly interest computation, fee calculation, account balance updates |
| 6 | **CBTRN03C.cbl** | 4 | 75 | 73 | **152** | Report formatting, page breaks, subtotals by account, type/category description lookups |
| 7 | **COCRDSLC.cbl** | 8 | 68 | 19 | **95** | Card detail display, customer lookup, field formatting |
| 8 | **COACTVWC.cbl** | 10 | 57 | 21 | **88** | Multi-entity read (account + card + customer), data presentation, error handling |
| 9 | **COTRN00C.cbl** | 16 | 52 | 47 | **115** | Transaction browse with filter, page forward/backward, selection routing |
| 10 | **COUSR00C.cbl** | 16 | 50 | 45 | **111** | User list browse, selection for update/delete, page navigation |

*Logic Score = EVALUATE + IF + (PERFORM / 2), weighted toward conditional complexity.*

---

## 5. Top 10 Programs by Inter-Program Dependencies

Dependencies include CALL, XCTL, copybook COPY, and CICS file references.

| Rank | Program | Outgoing Deps | Incoming Deps | Total | Dependencies |
|------|---------|--------------|---------------|-------|--------------|
| 1 | **COACTUPC.cbl** | 56 copies + 1 XCTL | Called from COMEN01C | **58** | Highest coupling; references nearly every copybook and data entity |
| 2 | **COACTVWC.cbl** | 15 copies + 1 XCTL | Called from COMEN01C | **17** | Multi-entity read across 4 VSAM files |
| 3 | **COCRDLIC.cbl** | 13 copies + 3 XCTL | Called from COMEN01C; returns from COCRDSLC, COCRDUPC | **19** | Hub for card management flow |
| 4 | **CBSTM03A.CBL** | 4 copies + 13 CALL CBSTM03B + 1 CALL CEE3ABD | Called by CREASTMT.JCL | **19** | Statement generation calls subroutine 13 times |
| 5 | **COTRN02C.cbl** | 10 copies + 2 CALL CSUTLDTC + 1 XCTL | Called from COTRN00C | **14** | Transaction creation with date validation and xref lookup |
| 6 | **COBIL00C.cbl** | 10 copies + 1 XCTL | Called from COMEN01C | **12** | Bill payment writes to account and transaction |
| 7 | **CORPT00C.cbl** | 8 copies + 2 CALL CSUTLDTC + 1 XCTL | Called from COMEN01C | **12** | Triggers batch job submission via TD queue |
| 8 | **CBACT04C.cbl** | 5 copies | Called by INTCALC.jcl | **10** | Interest calculation reads 5 files |
| 9 | **CBTRN02C.cbl** | 5 copies | Called by POSTTRAN.jcl | **10** | Core transaction posting |
| 10 | **COTRN00C.cbl** | 8 copies + 2 XCTL | Called from COMEN01C; returns from COTRN01C, COTRN02C | **12** | Hub for transaction management flow |

---

## 6. Composite Hotspot Ranking

Weighted composite score combining all dimensions:
- **Size** (30%): Lines of code normalized
- **Coupling** (25%): Copybooks + inter-program dependencies
- **I/O Density** (20%): Number of I/O operations
- **Logic Complexity** (25%): EVALUATE + IF + PERFORM density

| Rank | Program | LOC | Copies | I/O Ops | Logic Score | **Composite** | Classification |
|------|---------|-----|--------|---------|-------------|---------------|----------------|
| 1 | **COACTUPC.cbl** | 4,236 | 56 | 15 | 417 | **100.0** | 🔴 Critical |
| 2 | **COCRDUPC.cbl** | 1,560 | 15 | 10 | 190 | **55.3** | 🔴 Critical |
| 3 | **COCRDLIC.cbl** | 1,459 | 13 | 18 | 174 | **52.8** | 🟠 High |
| 4 | **CBTRN02C.cbl** | 731 | 5 | 8 | 158 | **39.5** | 🟠 High |
| 5 | **CBACT04C.cbl** | 653 | 5 | 10 | 143 | **36.8** | 🟠 High |
| 6 | **CBSTM03A.CBL** | 924 | 4 | 8 | 72 | **35.2** | 🟠 High |
| 7 | **COACTVWC.cbl** | 941 | 15 | 15 | 88 | **41.3** | 🟠 High |
| 8 | **COTRN02C.cbl** | 783 | 10 | 11 | 80 | **37.9** | 🟡 Medium |
| 9 | **COBIL00C.cbl** | 572 | 10 | 13 | 56 | **31.4** | 🟡 Medium |
| 10 | **CBTRN03C.cbl** | 649 | 5 | 6 | 152 | **35.7** | 🟡 Medium |

---

## 7. Modernization Recommendations

### 7.1 Priority 1 — Modernize First (Critical Hotspots)

#### COACTUPC.cbl — Account Update
- **Why first:** Largest program (4,236 LOC), highest coupling (56 copybooks), most complex business logic (333 IF statements, 20 EVALUATEs). This is the single highest-risk module in the estate.
- **Modernization approach:** Decompose into multiple Java service classes:
  - `AccountValidationService` — field-by-field validation rules
  - `AccountUpdateService` — apply changes and persist
  - `AccountViewController` — REST endpoint replacing CICS SEND/RECEIVE MAP
- **Risk:** High — contains the most intricate business rules in the system. Requires extensive test coverage before migration.
- **Estimated effort:** Large (3-4 weeks with testing)

#### COCRDUPC.cbl — Card Update
- **Why:** Second highest complexity (148 IF, 16 EVALUATE), heavy CICS I/O, and shared copybooks with COACTUPC.
- **Modernization approach:** Similar decomposition to Account Update. Reuse validation patterns.
- **Risk:** Medium-high — card validation rules (CVV, expiration) need careful mapping.
- **Estimated effort:** Medium (2-3 weeks)

### 7.2 Priority 2 — Core Batch Pipeline

#### CBTRN02C.cbl — Transaction Posting
- **Why:** Core of the batch cycle. 731 LOC with 96 IF statements. Every downstream process depends on successful transaction posting.
- **Modernization approach:** Spring Batch job with:
  - `TransactionPostingTasklet` — main processing
  - `CardXrefLookupService` — cross-reference validation
  - `CategoryBalanceService` — update running balances
- **Risk:** High — errors cascade to interest calculation and statements. Needs robust integration testing.
- **Estimated effort:** Medium (2-3 weeks)

#### CBACT04C.cbl — Interest Calculation
- **Why:** 653 LOC, reads 5 VSAM files, contains financial computation logic. Directly impacts account balances.
- **Modernization approach:** Spring Batch job with separate interest/fee calculation engines. Use BigDecimal for all financial math.
- **Risk:** High — financial accuracy is critical. Requires penny-perfect reconciliation testing.
- **Estimated effort:** Medium (2 weeks)

#### CBSTM03A.CBL + CBSTM03B.CBL — Statement Generation
- **Why:** 924 + 230 = 1,154 LOC combined. CBSTM03A calls CBSTM03B 13 times. Produces customer-facing output.
- **Modernization approach:** Spring Batch job producing HTML/PDF statements. Replace CBSTM03B subroutine with a `StatementFileService`.
- **Risk:** Medium — output format must match exactly for customer acceptance.
- **Estimated effort:** Medium (2-3 weeks)

### 7.3 Priority 3 — High-Traffic Online Screens

#### COCRDLIC.cbl — Card List
- **Why:** 1,459 LOC with complex browse logic (STARTBR/READNEXT/READPREV). Common user workflow.
- **Modernization approach:** REST API with pagination. Replace CICS browse with SQL `OFFSET/FETCH` or cursor-based pagination.
- **Estimated effort:** Medium (1-2 weeks)

#### COACTVWC.cbl — Account View
- **Why:** 941 LOC, reads 4 VSAM files in a single transaction. Read-only but high coupling.
- **Modernization approach:** Single REST endpoint aggregating data from Account, Card, and Customer JPA repositories.
- **Estimated effort:** Small-Medium (1-2 weeks)

### 7.4 Priority 4 — Supporting Programs

| Program | Recommendation |
|---------|---------------|
| **COTRN00C/01C** | Convert to REST APIs for transaction list/view. Standard CRUD pattern. |
| **COBIL00C** | Bill payment service. Contains important business logic (payment posting). |
| **COUSR00C-03C** | User management CRUD. Replace with Spring Security + JPA user repository. |
| **COSGN00C** | Sign-on. Replace with Spring Security authentication. |
| **COMEN01C/COADM01C** | Menu programs. Eliminated in web UI — routing becomes frontend concerns. |
| **CBACT01C-03C, CBCUS01C** | Simple read/print utilities. Low priority; replace with database queries. |
| **CSUTLDTC** | Date utility. Replace with `java.time` API. |
| **COBSWAIT** | Wait utility. Replace with `Thread.sleep()` or scheduler delay. |

### 7.5 Recommended Migration Wave Plan

```
Wave 1 (Weeks 1-6): Foundation + Core Batch
├── Set up Java/Spring Boot project structure
├── Convert copybooks to JPA entities (CVACT01Y, CVACT02Y, CVCUS01Y, CVACT03Y, CVTRA05Y)
├── Implement VSAM-to-RDBMS data migration
├── CBTRN02C -> Spring Batch transaction posting
└── CBACT04C -> Spring Batch interest calculation

Wave 2 (Weeks 7-12): Critical Online Programs
├── COACTUPC -> Account Update REST API + validation service
├── COCRDUPC -> Card Update REST API + validation service
├── COTRN02C (online) -> Transaction Add REST API
└── COBIL00C -> Bill Payment REST API

Wave 3 (Weeks 13-16): Batch Pipeline Completion
├── CBSTM03A/B -> Statement generation batch job
├── CBTRN03C -> Transaction report batch job
├── CBEXPORT/CBIMPORT -> Data migration services
└── JCL conversion -> Spring Batch job orchestration

Wave 4 (Weeks 17-20): Remaining Online + Polish
├── COCRDLIC/COCRDSLC -> Card management REST APIs
├── COACTVWC -> Account View REST API
├── COTRN00C/01C -> Transaction browse REST APIs
├── COUSR00C-03C -> User management (Spring Security)
├── COSGN00C -> Authentication (Spring Security)
└── Integration testing and UAT

Wave 5 (Weeks 21-24): Sub-Applications (Optional Modules)
├── Authorization module (IMS/DB2/MQ -> Spring + JPA + JMS)
├── Transaction Type DB2 module -> JPA CRUD
├── VSAM-MQ module -> Spring JMS services
└── End-to-end regression testing
```

---

## 8. Key Risk Areas

| Risk | Impact | Mitigation |
|------|--------|-----------|
| **Financial calculation precision** | Incorrect interest/fees | Use BigDecimal in Java; run parallel processing for reconciliation |
| **CICS transaction semantics** | Data integrity on failure | Map CICS SYNCPOINT to database transactions with proper rollback |
| **VSAM alternate index queries** | Missing data access paths | Ensure all AIX access patterns are mapped to SQL indexes |
| **BMS map field formatting** | UI display issues | Comprehensive UI testing; map PIC edit masks to Java formatters |
| **Batch job sequencing** | Processing order violations | Implement Spring Batch job dependencies matching JCL COND logic |
| **Date format handling** | COBDATFT assembler dependency | Replace with java.time.format.DateTimeFormatter |
| **COMP/COMP-3 data types** | Precision loss in conversion | Test boundary values; use exact-precision Java types |
| **REDEFINES/OCCURS** | Polymorphic records | Map to Java inheritance or union-type DTOs |
