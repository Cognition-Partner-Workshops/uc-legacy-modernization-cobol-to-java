# CardDemo Hotspot Report

> **System**: AWS CardDemo -- Mainframe Credit Card Management System
> **Generated**: 2026-03-25
> **Purpose**: Identify the top 10 modules prioritized by complexity, risk, and business impact for modernization planning.

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension        | Weight | Criteria                                                                   |
|------------------|--------|----------------------------------------------------------------------------|
| **Complexity**   | 40%    | Lines of code, number of COPY includes, CICS commands, file I/O operations, EVALUATE/PERFORM nesting, number of VSAM files accessed |
| **Risk**         | 30%    | Data sensitivity (PII/PCI), write operations, multi-file updates, error handling gaps, abend potential, security concerns |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, downstream dependencies, batch cycle position, regulatory exposure |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 4,236  | Largest program in the entire codebase                   |
| **Complexity**    | 10/10  | 15 copybooks, 13+ EXEC CICS commands, COPY REPLACING, multi-file READ/REWRITE, complex validation logic |
| **Risk**          | 9/10   | Writes to ACCTDATA (balance changes), reads 5 VSAM files, contains inline validation with CSLKPCDY (1,318-line lookup table), ABEND handler |
| **Business Impact**| 9/10  | Core account management -- balance and limit changes directly affect customer credit |
| **Composite Score**| **9.4** |                                                          |
| **Copybooks**     | CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY |
| **VSAM Files**    | ACCTDATA(R/U), CARDXREF(R), CUSTDATA(R), CARDDATA(R), USRSEC(R) |
| **BMS Map**       | COACTUP (512 lines -- largest map)                       |

**Modernization Concerns**:
- Monolithic 4,200-line program must be decomposed into smaller service methods
- COPY REPLACING pattern for UI attribute setting needs a different approach in Java
- CSLKPCDY (US ZIP/state validation) should become an external reference data service
- Multi-file transactional updates lack ACID guarantees in VSAM -- modernize with DB transactions

---

### Rank 2: CBTRN02C -- Transaction Posting

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 731    | Core batch posting engine                                |
| **Complexity**    | 8/10   | 5 copybooks, 6 SELECT/ASSIGN files, complex posting logic with running balance updates |
| **Risk**          | 10/10  | **Highest-risk write program**: simultaneously updates TRANSACT, ACCTDATA, and TCATBALF; generates DALYREJS for rejected records; financial accuracy is paramount |
| **Business Impact**| 10/10 | **Critical path**: all daily transactions flow through this program; errors cascade to interest calculation and statements |
| **Composite Score**| **9.2** |                                                          |
| **Copybooks**     | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y        |
| **Files**         | DALYTRAN(R), TRANFILE(R/W), XREFFILE(R), DALYREJS(W), ACCTFILE(R/U), TCATBALF(R/U) |
| **JCL**           | POSTTRAN.jcl                                              |

**Modernization Concerns**:
- Must implement as a Spring Batch job with chunk-oriented processing and rollback
- Multi-file atomic updates require proper database transaction boundaries
- Rejection logic needs to be preserved exactly (functional equivalence critical)
- Daily batch window timing -- this job blocks the entire downstream cycle

---

### Rank 3: COCRDUPC -- Card Update

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 1,560  | Second-largest online program                            |
| **Complexity**    | 9/10   | 12 copybooks, CSSTRPFY string parsing, CICS HANDLE ABEND, multi-file reads, card status changes |
| **Risk**          | 8/10   | Writes to CARDDATA (PCI-sensitive card details), card status changes affect downstream transaction processing |
| **Business Impact**| 8/10  | Card lifecycle management -- activation, deactivation, name changes affect cardholder experience |
| **Composite Score**| **8.4** |                                                          |
| **Copybooks**     | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| **VSAM Files**    | CARDDATA(R/U), CUSTDATA(R)                               |
| **BMS Map**       | COCRDUP (172 lines)                                       |

**Modernization Concerns**:
- Card number (PAN) handling requires PCI-DSS compliance in modernized system
- CVV display/update needs tokenization or masking
- CSSTRPFY inline string parsing should be replaced with standard Java utilities

---

### Rank 4: COCRDLIC -- Card List

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 1,459  | Third-largest online program                             |
| **Complexity**    | 8/10   | CICS STARTBR/READNEXT/READPREV pagination, 3 XCTL targets, complex browse logic with forward/backward scrolling |
| **Risk**          | 5/10   | Read-only browse -- lower write risk, but pagination bugs could expose wrong data |
| **Business Impact**| 7/10  | Primary card lookup screen -- gateway to card detail and update |
| **Composite Score**| **6.8** |                                                          |
| **Copybooks**     | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY |
| **VSAM Files**    | CARDDATA(Browse)                                          |
| **BMS Map**       | COCRDLI (344 lines)                                       |

**Modernization Concerns**:
- CICS browse (STARTBR/READNEXT/READPREV) maps to paginated REST API with cursor-based pagination
- XCTL to 3 different programs requires careful routing in the modernized UI
- 3270 page-up/page-down UX needs redesign for web interface

---

### Rank 5: CBSTM03A -- Statement Generation (Main)

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 924    | Statement generation main driver                         |
| **Complexity**    | 8/10   | 13 CALLs to CBSTM03B subroutine, 4 copybooks, multi-file reads, HTML + text output generation, control break logic |
| **Risk**          | 7/10   | Generates customer-facing financial statements; inaccuracy has regulatory and reputational risk |
| **Business Impact**| 9/10  | Customer statements are a core regulatory deliverable; directly customer-visible output |
| **Composite Score**| **8.0** |                                                          |
| **Copybooks**     | COSTM01, CVACT03Y, CUSTREC, CVACT01Y                    |
| **Files**         | TRNXFILE(R), XREFFILE(R), ACCTFILE(R), CUSTFILE(R), STMTFILE(W), HTMLFILE(W) |
| **JCL**           | CREASTMT.JCL (multi-step: SORT -> IDCAMS -> CBSTM03A)   |

**Modernization Concerns**:
- 13 calls to CBSTM03B indicate complex formatting logic -- consider template engine (Thymeleaf/FreeMarker)
- Dual output (text + HTML) should become a single modern template with multiple renderers
- Control break processing (per-card, per-account subtotals) maps to grouped Stream operations
- The SORT pre-step should become an ORDER BY clause in the query

---

### Rank 6: CBACT04C -- Interest Calculation

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 652    | Financial calculation engine                             |
| **Complexity**    | 7/10   | 5 copybooks, 5 file SELECTs, interest rate lookup by group/type/category, running balance updates |
| **Risk**          | 9/10   | **Financial accuracy critical**: calculates interest on all accounts; writes interest transactions to SYSTRAN and updates TCATBALF balances |
| **Business Impact**| 9/10  | Revenue-generating process -- incorrect interest calculation is a regulatory violation |
| **Composite Score**| **8.2** |                                                          |
| **Copybooks**     | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y        |
| **Files**         | TCATBALF(R/U), XREFFILE(R), ACCTFILE(R/U), DISCGRP(R), TRANSACT(W) |
| **JCL**           | INTCALC.jcl (PARM='2022071800' -- date parameter)       |

**Modernization Concerns**:
- Interest calculation rules must be exhaustively tested (equivalence testing critical)
- DISCGRP lookup (group + type + category -> rate) should become a database JOIN
- The PARM date should become a configurable parameter in Spring Batch
- Results must be auditable -- consider an interest calculation audit trail

---

### Rank 7: COACTVWC -- Account View

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 941    | Account detail display                                   |
| **Complexity**    | 7/10   | 13 copybooks, CSSTRPFY, reads from 4 VSAM files, CICS HANDLE ABEND, complex screen population |
| **Risk**          | 5/10   | Read-only (no writes), but displays sensitive customer and account data |
| **Business Impact**| 7/10  | Primary account inquiry -- highest-traffic screen for customer service |
| **Composite Score**| **6.4** |                                                          |
| **Copybooks**     | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY |
| **VSAM Files**    | ACCTDATA(R), CARDXREF(R), CUSTDATA(R), CARDDATA(R)       |
| **BMS Map**       | COACTVW (378 lines)                                       |

**Modernization Concerns**:
- 4-file read aggregation maps to a single JPA query with joins
- Account + customer + card composite view should become a DTO/ViewModel
- PII masking needed for customer SSN and card PAN in the modernized UI

---

### Rank 8: COCRDSLC -- Card Detail View

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 887    | Card detail display with customer info                   |
| **Complexity**    | 7/10   | 12 copybooks, CSSTRPFY, CICS HANDLE ABEND/ABEND pair, reads CARDDATA + CUSTDATA |
| **Risk**          | 6/10   | Displays PCI-sensitive card data (PAN, CVV, expiration); abend handler adds risk |
| **Business Impact**| 6/10  | Card inquiry screen used by customer service              |
| **Composite Score**| **6.4** |                                                          |
| **Copybooks**     | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDSL, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| **VSAM Files**    | CARDDATA(R), CUSTDATA(R)                                  |
| **BMS Map**       | COCRDSL (157 lines)                                       |

**Modernization Concerns**:
- PCI-DSS: Card PAN must be masked (show only last 4 digits)
- CVV should never be displayed in the modernized system
- The ABEND/HANDLE ABEND pattern should become proper try-catch exception handling

---

### Rank 9: COTRN02C -- Transaction Add

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 783    | New transaction entry screen                             |
| **Complexity**    | 7/10   | 10 copybooks, CALLs CSUTLDTC for date conversion, multi-file validation (account + card + xref), CICS WRITE to TRANSACT |
| **Risk**          | 8/10   | Creates new financial transactions; writes to TRANSACT master; validation errors could create bad data |
| **Business Impact**| 8/10  | Manual transaction entry -- used for adjustments, corrections, and special entries |
| **Composite Score**| **7.6** |                                                          |
| **Copybooks**     | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA |
| **VSAM Files**    | TRANSACT(W), ACCTDATA(R), CARDXREF(R/Browse)             |
| **BMS Map**       | COTRN02 (307 lines)                                       |

**Modernization Concerns**:
- Date validation via CSUTLDTC call should use Java `LocalDate` parsing
- Multi-file validation (account exists + card exists + xref valid) becomes JPA queries with proper error messages
- Transaction write should be wrapped in a database transaction
- Need to preserve the STARTBR/READPREV pattern for "most recent card" lookup

---

### Rank 10: CBTRN03C -- Transaction Report Generation

| Metric            | Value  | Notes                                                    |
|-------------------|--------|----------------------------------------------------------|
| **Lines of Code** | 649    | Batch report writer                                      |
| **Complexity**    | 7/10   | 5 copybooks, 6 file SELECTs, control break logic (by account, by type, by category), formatted report output with subtotals |
| **Risk**          | 5/10   | Read-only from transaction files; writes report output only |
| **Business Impact**| 7/10  | Transaction reports are used for reconciliation and audit; regulatory requirement |
| **Composite Score**| **6.4** |                                                          |
| **Copybooks**     | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y        |
| **Files**         | TRANFILE(R), CARDXREF(R), TRANTYPE(R), TRANCATG(R), DATEPARM(R), TRANREPT(W) |
| **JCL**           | TRANREPT.jcl (SORT pre-step + CBTRN03C)                  |

**Modernization Concerns**:
- Control break report logic should become a JasperReports or similar reporting framework
- CVTRA07Y defines the print layout -- should become a report template
- SORT pre-step merges into an SQL ORDER BY
- DATEPARM file should become a Spring Batch job parameter

---

## Summary: Hotspot Ranking

| Rank | Module    | LOC   | Complexity | Risk | Business Impact | Composite | Category           |
|------|-----------|-------|:----------:|:----:|:---------------:|:---------:|---------------------|
| 1    | COACTUPC  | 4,236 | 10         | 9    | 9               | **9.4**   | Online / Account    |
| 2    | CBTRN02C  | 731   | 8          | 10   | 10              | **9.2**   | Batch / Transactions|
| 3    | COCRDUPC  | 1,560 | 9          | 8    | 8               | **8.4**   | Online / Cards      |
| 4    | CBACT04C  | 652   | 7          | 9    | 9               | **8.2**   | Batch / Financial   |
| 5    | CBSTM03A  | 924   | 8          | 7    | 9               | **8.0**   | Batch / Statements  |
| 6    | COTRN02C  | 783   | 7          | 8    | 8               | **7.6**   | Online / Transactions|
| 7    | COCRDLIC  | 1,459 | 8          | 5    | 7               | **6.8**   | Online / Cards      |
| 8    | COACTVWC  | 941   | 7          | 5    | 7               | **6.4**   | Online / Account    |
| 9    | COCRDSLC  | 887   | 7          | 6    | 6               | **6.4**   | Online / Cards      |
| 10   | CBTRN03C  | 649   | 7          | 5    | 7               | **6.4**   | Batch / Reporting   |

---

## Migration Priority Recommendations

### Wave 1 -- High Risk, High Impact (Migrate First)

| Module    | Rationale                                                           |
|-----------|---------------------------------------------------------------------|
| CBTRN02C  | Highest combined risk + impact; financial data integrity is critical; batch cycle bottleneck |
| CBACT04C  | Revenue-generating interest calculation; must be bit-for-bit accurate |
| CBSTM03A  | Customer-facing output; regulatory requirement; complex formatting  |

**Strategy**: Convert to Spring Batch jobs with comprehensive equivalence testing. Run legacy and modern in parallel for at least 2 cycles before cutover.

### Wave 2 -- High Complexity, Medium Risk (Migrate Second)

| Module    | Rationale                                                           |
|-----------|---------------------------------------------------------------------|
| COACTUPC  | Largest program -- decomposition yields highest ROI; core account ops |
| COCRDUPC  | PCI-sensitive card operations; second-largest online program        |
| COTRN02C  | Transaction entry with multi-file validation                        |

**Strategy**: Decompose into Spring MVC controllers + service layer. Implement PCI-DSS controls (PAN masking, CVV elimination). Use Strangler Fig to route individual functions.

### Wave 3 -- Medium Complexity (Migrate Third)

| Module    | Rationale                                                           |
|-----------|---------------------------------------------------------------------|
| COCRDLIC  | Complex browse logic but read-only; good candidate for REST pagination |
| COACTVWC  | Read-only aggregation; straightforward JPA query conversion         |
| COCRDSLC  | Card detail view; relatively self-contained                        |
| CBTRN03C  | Reporting; can be replaced with modern reporting framework          |

**Strategy**: Implement as REST endpoints with JPA repositories. Use the Strangler Fig pattern to incrementally replace 3270 screens with web UI components.

---

## Cross-Cutting Modernization Risks

| Risk Area                | Affected Modules                | Mitigation                                          |
|--------------------------|---------------------------------|-----------------------------------------------------|
| **PCI-DSS Compliance**   | COCRDUPC, COCRDSLC, COCRDLIC, CBEXPORT | Implement tokenization, mask PAN, eliminate CVV display |
| **PII Data Protection**  | COACTUPC, COACTVWC, all customer-facing | Encrypt SSN/DOB at rest and in transit; implement RBAC |
| **Financial Accuracy**   | CBTRN02C, CBACT04C, COBIL00C    | Exhaustive parallel-run testing; BigDecimal for all currency |
| **Plaintext Passwords**  | COSGN00C, COUSR00C-03C          | Implement bcrypt hashing; eliminate plaintext storage |
| **VSAM-to-RDBMS Migration** | All 31 core programs         | Map VSAM KSDS to tables; handle FILLER/padding removal |
| **CICS Session State**   | All 17 online programs (COMMAREA) | Replace with HTTP session or JWT claims             |
| **Batch Window Constraints** | CLOSEFIL -> POSTTRAN -> ... -> OPENFIL | Replace with database-native batch (no file close needed) |
| **EBCDIC/ASCII Encoding** | CBEXPORT, CBIMPORT             | Handle character set conversion in data migration   |
