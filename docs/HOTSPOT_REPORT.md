# CardDemo Hotspot Report — Top 10 Migration-Critical Modules

> **Generated:** 2026-03-25 | **Methodology:** Static complexity metrics + dependency fan-out + business impact scoring  
> **Purpose:** Prioritize the modules that carry the highest risk, complexity, and business impact for the COBOL-to-Java modernization effort.

---

## Table of Contents

1. [Scoring Methodology](#1-scoring-methodology)
2. [Top 10 Hotspot Ranking](#2-top-10-hotspot-ranking)
3. [Detailed Module Profiles](#3-detailed-module-profiles)
4. [Risk Heat Map](#4-risk-heat-map)
5. [Recommendations](#5-recommendations)

---

## 1. Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension | Weight | Factors Considered |
|-----------|--------|-------------------|
| **Complexity** | 40% | Lines of code, EVALUATE/IF/PERFORM counts (cyclomatic proxy), number of COPY includes, REDEFINES, GO TO usage, nested conditionals |
| **Risk** | 35% | Data file fan-out (# VSAM files touched), program dependencies (call/XCTL fan-in & fan-out), shared copybook centrality, security-sensitive data, financial calculations |
| **Business Impact** | 25% | Revenue-path proximity, user-facing criticality, data volume, regulatory implications, batch SLA sensitivity |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.35) + (Business Impact × 0.25)

### Complexity Metrics Legend

| Metric | Description | Source |
|--------|-------------|--------|
| LOC | Lines of code (total, including comments) | `wc -l` |
| IF | Count of IF statements (branching complexity) | `grep` for `IF ` |
| EVAL | Count of EVALUATE statements (multi-way branching) | `grep` for `EVALUATE` |
| PERF | Count of PERFORM statements (procedure calls) | `grep` for `PERFORM` |
| CCI | Composite Complexity Index = (IF + EVAL×3 + PERF) / LOC × 100 | Computed |

---

## 2. Top 10 Hotspot Ranking

| Rank | Module | LOC | IF | EVAL | PERF | CCI | Complexity | Risk | Biz Impact | **Composite** |
|------|--------|-----|-----|------|------|------|-----------|------|-----------|---------------|
| **1** | **CBTRN02C.cbl** | 731 | 93 | 0 | 61 | 21.1 | **9** | **10** | **10** | **9.60** |
| **2** | **COACTUPC.cbl** | 4,236 | 168 | 20 | 64 | 6.9 | **10** | **9** | **9** | **9.40** |
| **3** | **CBACT04C.cbl** | 652 | 86 | 0 | 56 | 21.8 | **8** | **9** | **10** | **8.85** |
| **4** | **CBSTM03A.CBL** | 924 | 15 | 9 | 29 | 7.7 | **7** | **8** | **8** | **7.60** |
| **5** | **COCRDLIC.cbl** | 1,459 | 122 | 18 | 34 | 14.4 | **8** | **7** | **7** | **7.40** |
| **6** | **COCRDUPC.cbl** | 1,560 | 148 | 16 | 26 | 14.3 | **8** | **7** | **7** | **7.40** |
| **7** | **COTRN02C.cbl** | 783 | 14 | 26 | 61 | 19.5 | **7** | **7** | **8** | **7.25** |
| **8** | **CBTRN03C.cbl** | 649 | 75 | 4 | 72 | 24.8 | **8** | **6** | **7** | **7.05** |
| **9** | **CBEXPORT.cbl** | 582 | 16 | 0 | 45 | 10.5 | **6** | **7** | **6** | **6.35** |
| **10** | **COUSR00C.cbl** | 695 | 25 | 16 | 41 | 14.5 | **6** | **7** | **6** | **6.35** |

---

## 3. Detailed Module Profiles

### Rank 1: CBTRN02C.cbl — Transaction Posting (Batch)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 731 |
| **Domain** | Transaction Management — Core Batch Posting |
| **JCL Job** | POSTTRAN.jcl |
| **Complexity Score** | 9/10 |
| **Risk Score** | 10/10 |
| **Business Impact** | 10/10 |
| **Composite Score** | **9.60** |

**Why it's a hotspot:**
- **Core financial process:** This is the single most business-critical batch program. It posts daily transactions to the master file and updates account balances. Any bug here directly impacts customer accounts.
- **5-file fan-out:** Reads Daily Transactions, Account, Card XREF, Category Balance; writes to Transaction Master and updates Account balances. This is the highest data coupling in the batch subsystem.
- **High density:** 93 IF statements + 61 PERFORMs in only 731 lines gives a CCI of 21.1 — the second-highest complexity density.
- **Financial accuracy:** Balance updates must be ACID-compliant. The COBOL COMPUTE statements with PIC S9(10)V99 require exact decimal arithmetic in Java (BigDecimal).
- **Batch SLA:** Runs nightly in the critical path. Any performance regression impacts the entire batch window.

**Migration risks:**
- Transaction atomicity across 5 files must be preserved (COBOL has no explicit transaction management; VSAM provides record-level locking)
- Decimal arithmetic precision must be exactly preserved (no floating-point)
- Error handling via CEE3ABD (abnormal termination) must map to Java exception handling + rollback

**Recommended approach:**
- Map to a Spring Batch job with chunk-oriented processing
- Use `BigDecimal` for all monetary fields with `RoundingMode.HALF_UP`
- Implement database transactions spanning all 5 tables with proper rollback

---

### Rank 2: COACTUPC.cbl — Account Update (Online)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 4,236 (largest program in the codebase by 2.7×) |
| **Domain** | Account Management — Update |
| **CICS Transaction** | CA02 |
| **BMS Map** | COACTUP |
| **Complexity Score** | 10/10 |
| **Risk Score** | 9/10 |
| **Business Impact** | 9/10 |
| **Composite Score** | **9.40** |

**Why it's a hotspot:**
- **Massive size:** At 4,236 lines, it is 2.7× larger than the next biggest program. This single module represents ~20% of all online program code.
- **High branching:** 168 IF statements + 20 EVALUATE blocks create deeply nested decision trees.
- **Data coupling:** Reads from 3 VSAM files (Account, Customer, Card XREF). Any schema change ripples through extensive field-level validation.
- **12+ copybooks:** Including CVCRD01Y, CSLKPCDY (1,318-line lookup table), CSUTLDWY (date validation), CSSETATY, CSSTRPFY. The copious COPY REPLACING usage makes the effective code even larger.
- **Complex validation:** Validates phone area codes (NANPA list), state codes, ZIP prefixes, dates (via CSUTLDPY), and credit limits with extensive cross-field business rules.
- **COMMAREA navigation:** Uses CDEMO-TO-PROGRAM for dynamic XCTL return, making control flow non-linear.

**Migration risks:**
- Splitting this monolith into services requires understanding all implicit state transitions
- The COPY REPLACING pattern must be carefully unwound into Java method calls
- Field-level BMS attribute manipulation (CSSETATY) has no direct Java equivalent

**Recommended approach:**
- Decompose into AccountUpdateController + AccountValidationService + AccountDataService
- Extract the CSLKPCDY lookup table into a database reference table or enum
- Convert CSUTLDPY date validation into a reusable Java DateValidator class

---

### Rank 3: CBACT04C.cbl — Interest Calculation (Batch)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 652 |
| **Domain** | Financial — Interest Calculation |
| **JCL Job** | INTCALC.jcl |
| **Complexity Score** | 8/10 |
| **Risk Score** | 9/10 |
| **Business Impact** | 10/10 |
| **Composite Score** | **8.85** |

**Why it's a hotspot:**
- **Revenue-generating logic:** Interest calculation directly affects the bottom line. The business rules for rate application per category per disclosure group are intricate and regulation-sensitive.
- **3-file cross-reference:** Reads Account, Category Balance, and Disclosure Group files. The join logic (Account.GROUP-ID → Disclosure.GROUP-ID + TYPE + CAT → Rate) has no COBOL JOIN syntax — it's implemented procedurally.
- **High complexity density:** 86 IFs + 56 PERFORMs in 652 lines (CCI = 21.8, highest in the codebase).
- **Regulatory compliance:** Interest calculation rules are subject to consumer protection regulations. Exact reproduction of the COBOL arithmetic is mandatory.

**Migration risks:**
- The interest rate lookup involves a 3-part composite key match that must be exactly replicated
- COBOL fixed-point arithmetic with intermediate rounding must be tested against COBOL output penny-for-penny
- The batch must handle accounts with no matching disclosure group gracefully

**Recommended approach:**
- Implement as a Spring Batch Tasklet with explicit rate-lookup service
- Create comprehensive test fixtures from production COBOL output for penny-level validation
- Document all rounding rules explicitly in the Java implementation

---

### Rank 4: CBSTM03A.CBL — Statement Generation (Batch)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 924 (+ 230 in CBSTM03B sub-program) |
| **Domain** | Reporting — Customer Statements |
| **JCL Job** | CREASTMT.JCL |
| **Complexity Score** | 7/10 |
| **Risk Score** | 8/10 |
| **Business Impact** | 8/10 |
| **Composite Score** | **7.60** |

**Why it's a hotspot:**
- **Driver + sub-program pattern:** CBSTM03A calls CBSTM03B 11 times for page formatting. This call-based decomposition must be preserved.
- **Multi-file input:** Reads sorted transactions, accounts, and cross-references to produce customer statements.
- **Print-oriented output:** Uses fixed-width column formatting that must be replicated exactly for customer-facing statements.
- **JCL SORT dependency:** CREASTMT.JCL uses DFSORT to pre-sort transactions by card number before feeding to CBSTM03A.

**Migration risks:**
- The SORT step must be replaced with SQL ORDER BY or in-memory sorting
- Print formatting (column alignment, page breaks, totals) requires a PDF/report generation library

---

### Rank 5: COCRDLIC.cbl — Credit Card List (Online)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 1,459 |
| **Domain** | Card Management — List/Browse |
| **CICS Transaction** | CC01 |
| **Complexity Score** | 8/10 |
| **Risk Score** | 7/10 |
| **Business Impact** | 7/10 |
| **Composite Score** | **7.40** |

**Why it's a hotspot:**
- **Browse logic complexity:** 122 IF + 18 EVALUATE + STARTBR/READNEXT/READPREV VSAM browse operations with forward/backward paging.
- **Multi-target navigation:** XCTLs to 3 different programs (COMEN01C, COCRDSLC, COCRDUPC) depending on user action.
- **VSAM browse state management:** Maintains browse position across screen interactions using COMMAREA — this stateful pattern doesn't map naturally to stateless REST.

**Migration risks:**
- VSAM STARTBR/READNEXT pagination must be converted to SQL OFFSET/LIMIT or cursor-based pagination
- The stateful browse position stored in COMMAREA needs a server-side session or token-based approach

---

### Rank 6: COCRDUPC.cbl — Credit Card Update (Online)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 1,560 |
| **Domain** | Card Management — Update |
| **CICS Transaction** | CC03 |
| **Complexity Score** | 8/10 |
| **Risk Score** | 7/10 |
| **Business Impact** | 7/10 |
| **Composite Score** | **7.40** |

**Why it's a hotspot:**
- **Highest IF density among online programs:** 148 IF statements for field-level validation.
- **16 EVALUATE blocks:** Multi-way decision logic for update scenarios.
- **COPY REPLACING patterns:** Uses CSSETATY and CSSTRPFY with REPLACING, expanding the effective codebase significantly.
- **Sensitive data:** Updates card numbers, CVV, expiration dates — PCI-DSS implications.

**Migration risks:**
- PCI-DSS compliance requires card data encryption in the modernized system
- Validation logic must be extracted and unit-tested independently

---

### Rank 7: COTRN02C.cbl — Transaction Add (Online)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 783 |
| **Domain** | Transaction Management — Add New Transaction |
| **CICS Transaction** | CT02 |
| **Complexity Score** | 7/10 |
| **Risk Score** | 7/10 |
| **Business Impact** | 8/10 |
| **Composite Score** | **7.25** |

**Why it's a hotspot:**
- **26 EVALUATE blocks:** Highest EVALUATE count of any program — complex state machine for the add workflow.
- **Date validation calls:** CALLs CSUTLDTC twice for start/end date validation.
- **Cross-file validation:** Reads Account and Card XREF to validate the transaction before writing.
- **VSAM WRITE + browse:** Generates transaction ID via STARTBR/READPREV to find the next available key, then WRITEs the new record.
- **Direct financial impact:** Every transaction added here affects account balances in the next batch cycle.

---

### Rank 8: CBTRN03C.cbl — Transaction Report (Batch)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 649 |
| **Domain** | Reporting — Daily Transaction Report |
| **JCL Job** | TRANREPT.jcl |
| **Complexity Score** | 8/10 |
| **Risk Score** | 6/10 |
| **Business Impact** | 7/10 |
| **Composite Score** | **7.05** |

**Why it's a hotspot:**
- **Highest CCI in codebase:** 75 IFs + 4 EVALUATEs + 72 PERFORMs in 649 lines = CCI of 24.8. Extremely dense control flow.
- **Multi-level report breaks:** Account-level subtotals, page totals, and grand totals using control-break logic.
- **4-file lookups:** Reads Transactions, Transaction Types, Transaction Categories for description enrichment.
- **CVTRA07Y report layout:** Complex print formatting with edited numeric fields (PIC +ZZZ,ZZZ,ZZZ.ZZ).

---

### Rank 9: CBEXPORT.cbl — Data Export (Batch)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 582 |
| **Domain** | Data Migration — Multi-Record Export |
| **JCL Job** | CBEXPORT.jcl |
| **Complexity Score** | 6/10 |
| **Risk Score** | 7/10 |
| **Business Impact** | 6/10 |
| **Composite Score** | **6.35** |

**Why it's a hotspot:**
- **5-file fan-out:** Reads Account, Card, Customer, XREF, and Transaction files — the broadest data access in any single batch program.
- **REDEFINES-based polymorphism:** Uses CVEXPORT.cpy with REDEFINES to pack 5 different entity types into a single 500-byte record. This COBOL pattern (discriminated union) requires careful Java mapping.
- **COMP/COMP-3 fields:** The export format uses packed decimal (COMP-3) and binary (COMP) for storage optimization. These require byte-level conversion in Java.
- **Branch migration context:** Designed for data migration between branches — directly relevant to the modernization effort itself.

---

### Rank 10: COUSR00C.cbl — User List (Online)

| Attribute | Detail |
|-----------|--------|
| **Lines of Code** | 695 |
| **Domain** | User Security — Admin List |
| **CICS Transaction** | CU00 |
| **Complexity Score** | 6/10 |
| **Risk Score** | 7/10 |
| **Business Impact** | 6/10 |
| **Composite Score** | **6.35** |

**Why it's a hotspot:**
- **Security-critical:** Manages the user security records including passwords stored in plain text.
- **VSAM browse pattern:** STARTBR/READNEXT/READPREV pagination for user listing.
- **16 EVALUATE blocks:** State machine for screen navigation.
- **Admin-only access:** Only accessible to admin users, but controls who can access the entire system.

**Migration risks:**
- Password storage must be converted to hashed/salted storage (bcrypt or similar)
- User management should integrate with modern IAM (LDAP, OAuth, etc.)

---

## 4. Risk Heat Map

```
                    LOW Business Impact    MED Business Impact    HIGH Business Impact
                    ─────────────────────  ─────────────────────  ─────────────────────
HIGH Complexity  │  CBTRN03C (7.05)       COCRDLIC (7.40)        CBTRN02C (9.60)
                 │                         COCRDUPC (7.40)        COACTUPC (9.40)
                 │                                                CBACT04C (8.85)
                 │
MED Complexity   │  COUSR00C (6.35)       COTRN02C (7.25)        CBSTM03A (7.60)
                 │  CBEXPORT (6.35)
                 │
LOW Complexity   │  (remaining programs)   COBSWAIT, CSUTLDTC    COSGN00C (auth entry)
                 │                         CBACT01-03C            COMEN01C (router)
```

### Priority Quadrants

| Quadrant | Description | Modules | Action |
|----------|------------|---------|--------|
| **Q1: High Risk + High Impact** | Migrate first, most testing effort | CBTRN02C, CBACT04C, COACTUPC | Dedicated team, extensive regression testing, penny-level validation |
| **Q2: High Risk + Med Impact** | Migrate carefully, good test coverage | COCRDLIC, COCRDUPC, CBSTM03A, COTRN02C | Standard migration with focused testing |
| **Q3: Med Risk + High Impact** | Straightforward but important | COSGN00C, COMEN01C | Security review, ensure auth works correctly |
| **Q4: Low Risk + Low Impact** | Migrate last, lower effort | Read utilities (CBACT01-03C), COBSWAIT | Batch migration, minimal risk |

---

## 5. Recommendations

### 5.1 Immediate Actions

1. **CBTRN02C (Rank 1) + CBACT04C (Rank 3):** Create exhaustive test data with known COBOL outputs. These financial calculation programs require penny-level accuracy validation before any migration begins.

2. **COACTUPC (Rank 2):** Begin decomposition analysis immediately. At 4,236 lines, this monolith should be split into 3-4 Java classes during migration. Map out all validation rules as a pre-migration artifact.

3. **Password Security (CSUSR01Y):** Flag SEC-USR-PWD (plain text, 8 chars) for immediate remediation in the modernized system. Implement bcrypt hashing + minimum password complexity.

### 5.2 Migration Strategy per Hotspot

| Module | Recommended Java Target | Testing Strategy |
|--------|------------------------|-----------------|
| COACTUPC | Spring MVC Controller + Validation Service | Selenium/Playwright UI tests + unit tests per validation rule |
| CBTRN02C | Spring Batch Job (chunk processing) | Golden-file comparison: COBOL output vs Java output, penny-level |
| CBACT04C | Spring Batch Tasklet + InterestCalcService | Same as CBTRN02C — run both systems in parallel for 1 billing cycle |
| COCRDLIC | Spring MVC + JPA Pageable | Pagination boundary tests (first page, last page, empty result) |
| COCRDUPC | Spring MVC + Bean Validation | Field-level validation unit tests extracted from COBOL IF chains |
| CBSTM03A/B | Spring Batch + JasperReports/iText | Visual comparison of generated statements |
| CBTRN03C | Spring Batch + report generator | Report output diff testing |
| COTRN02C | Spring MVC Controller | Workflow state machine tests |
| CBEXPORT | Spring Batch ItemReader/Writer | Round-trip test: export → import → compare |
| COUSR00C | Spring Security + JPA | Authentication + authorization integration tests |

### 5.3 Key Technical Debt Items Found

| # | Issue | Affected Modules | Severity | Modernization Action |
|---|-------|-----------------|----------|---------------------|
| 1 | Plain-text passwords | CSUSR01Y, COSGN00C, COUSR00-03C | **Critical** | Implement password hashing (bcrypt) |
| 2 | CVV stored in clear text | CVACT02Y, COCRDUPC | **Critical** | Encrypt at rest, PCI-DSS compliance |
| 3 | SSN stored unmasked | CVCUS01Y, COACTUPC | **High** | Encrypt at rest, mask in UI |
| 4 | Hard-coded century check (19/20) | CSUTLDWY (88-level) | **Medium** | Use `java.time` API with no century restriction |
| 5 | GO TO statements | CSUTLDPY (6 instances) | **Medium** | Refactor to structured control flow |
| 6 | CEE3ABD for error handling | 10 batch programs | **Medium** | Replace with try/catch + proper logging |
| 7 | FILLER bytes in records | All copybooks | **Low** | Drop fillers; use JPA entity mapping |
| 8 | Duplicate customer copybook | CVCUS01Y vs CUSTREC | **Low** | Consolidate to single Java entity |
| 9 | COMP/COMP-3 in export format | CVEXPORT | **Low** | Use standard serialization (JSON/Avro) |
| 10 | Typo: "EXPIRAION" in field names | CVACT01Y, CVACT02Y | **Low** | Fix spelling in Java entities |
