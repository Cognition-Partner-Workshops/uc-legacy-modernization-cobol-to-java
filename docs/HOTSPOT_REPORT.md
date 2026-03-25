# CardDemo Hotspot Report

> **Generated from:** static complexity analysis of the CardDemo COBOL codebase
>
> **Purpose:** Identify and prioritize the top 10 modules by complexity, risk, and business impact for modernization planning

---

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale each):

| Dimension | Weight | Criteria |
|-----------|-------:|---------|
| **Complexity** | 40% | Lines of code, cyclomatic complexity (IF/EVALUATE branches), number of PERFORM paragraphs, number of copybook dependencies, CICS command count |
| **Risk** | 30% | Data mutation scope (files written/updated), centrality in call/navigation graph, error-handling patterns, security sensitivity |
| **Business Impact** | 30% | Revenue criticality, user-facing visibility, batch processing dependency chain position, regulatory relevance |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric | Value |
|--------|-------|
| **Lines of Code** | 4,236 |
| **IF Statements** | 164 |
| **EVALUATE Blocks** | 10 |
| **PERFORM Calls** | 61 |
| **CICS Commands** | 17 |
| **Copybook Dependencies** | 18 |
| **Complexity Score** | 10 |
| **Risk Score** | 9 |
| **Business Impact Score** | 10 |
| **Composite Score** | **9.7** |

**Why it's #1:** This is the single largest program in the codebase by a wide margin (4,236 lines -- nearly 3x the next largest core program). It has the highest cyclomatic complexity with 164 conditional branches and handles critical account data mutations (balance adjustments, credit limit changes, account status). It writes to ACCTDATA, the most business-critical VSAM file. Errors here directly affect customer account balances and credit limits.

**Modernization Concerns:**
- Extremely high branch complexity makes unit testing difficult
- Mixes UI logic (BMS screen handling) with business rules and data access
- Contains inline field validation that should be extracted to a validation service
- Multiple VSAM REWRITE operations within a single pseudo-conversational flow

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 731 |
| **IF Statements** | 48 |
| **EVALUATE Blocks** | 0 |
| **PERFORM Calls** | 61 |
| **Copybook Dependencies** | 5 (CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y) |
| **Files Written** | ACCTDATA, TRANSACT, TCATBALF |
| **Complexity Score** | 8 |
| **Risk Score** | 10 |
| **Business Impact Score** | 10 |
| **Composite Score** | **9.2** |

**Why it's #2:** This is the core financial posting engine. It reads daily transactions and posts them to account balances, the transaction master, and category balance files. It mutates three VSAM files in a single run, making it the highest-risk batch program. An error here causes incorrect account balances across the entire portfolio. It sits at the critical path of the nightly batch cycle (Step 3).

**Modernization Concerns:**
- Multi-file transactional updates without two-phase commit (relies on batch restart/rerun)
- Complex balance-update logic with category tracking
- Tightly coupled to VSAM sequential processing patterns
- Needs idempotency guarantees when converted to a modern batch framework

---

### Rank 3: COCRDUPC -- Card Update

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,560 |
| **IF Statements** | 72 |
| **EVALUATE Blocks** | 16 |
| **PERFORM Calls** | 26 |
| **CICS Commands** | 12 |
| **Copybook Dependencies** | 13 |
| **Complexity Score** | 9 |
| **Risk Score** | 8 |
| **Business Impact Score** | 8 |
| **Composite Score** | **8.4** |

**Why it's #3:** Second-largest online program with high branch complexity. Manages card data updates including sensitive fields (CVV, expiration date, active status). The 16 EVALUATE blocks indicate complex state-machine logic for screen flow. Card data mutations affect downstream transaction processing.

**Modernization Concerns:**
- Complex screen-state management (16 EVALUATE blocks)
- Sensitive data handling (CVV codes) -- needs encryption in modern target
- Validation logic interleaved with screen I/O
- Cross-reference file lookups add coupling complexity

---

### Rank 4: COCRDLIC -- Card List

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,459 |
| **IF Statements** | 59 |
| **EVALUATE Blocks** | 18 |
| **PERFORM Calls** | 33 |
| **CICS Commands** | 18 |
| **Copybook Dependencies** | 11 |
| **Complexity Score** | 9 |
| **Risk Score** | 6 |
| **Business Impact Score** | 7 |
| **Composite Score** | **7.5** |

**Why it's #4:** Most complex list/browse program with 18 EVALUATE blocks and 18 CICS commands. Implements forward/backward paging with STARTBR/READNEXT/READPREV, which is one of the hardest CICS patterns to modernize. The browse cursor management logic is intricate and error-prone.

**Modernization Concerns:**
- VSAM browse cursor management (STARTBR/READNEXT/READPREV/ENDBR)
- Paging state maintained across pseudo-conversational boundaries
- Screen array population logic (repeating groups for list display)
- Maps to a paginated REST API or server-side rendering in modern architecture

---

### Rank 5: CBACT04C -- Interest Calculation (Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 652 |
| **IF Statements** | 43 |
| **PERFORM Calls** | 56 |
| **Copybook Dependencies** | 5 (CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y) |
| **Files Written** | ACCTDATA, TCATBALF |
| **Complexity Score** | 7 |
| **Risk Score** | 9 |
| **Business Impact Score** | 9 |
| **Composite Score** | **8.2** |

**Why it's #5:** Implements the interest calculation business rules -- the core revenue-generating logic of a credit card system. Reads disclosure groups (rate tables) and applies interest to account balances by transaction category. Financial accuracy is paramount; rounding errors compound across the portfolio.

**Modernization Concerns:**
- Complex financial arithmetic with COBOL COMPUTE statements
- Decimal precision handling (S9(10)V99) must be preserved exactly
- Rate-table lookups across disclosure groups
- Must maintain bit-exact compatibility with COBOL packed-decimal math
- Regulatory audit requirements for interest calculation traceability

---

### Rank 6: CBSTM03A -- Statement Generation (Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 924 |
| **IF Statements** | 15 |
| **EVALUATE Blocks** | 5 |
| **PERFORM Calls** | 29 |
| **CALL Targets** | CBSTM03B, CEE3ABD |
| **Files Read** | TRANSACT (sorted), CARDXREF, ACCTDATA, CUSTDATA |
| **Files Written** | STATEMNT.PS, STATEMNT.HTML |
| **Complexity Score** | 7 |
| **Risk Score** | 7 |
| **Business Impact Score** | 8 |
| **Composite Score** | **7.3** |

**Why it's #6:** The most complex batch program by LOC. Generates customer-facing account statements in both plain text and HTML formats. Reads four VSAM files, calls a subroutine (CBSTM03B) for file I/O, and uses mainframe control block addressing (advanced COBOL technique). Exercises multiple modernization-relevant patterns intentionally.

**Modernization Concerns:**
- Calls subroutine CBSTM03B (two-program unit must migrate together)
- Mainframe control block addressing (TIOT traversal) -- non-portable
- Dual-format output generation (text + HTML)
- SORT pre-processing step required before execution
- Customer-facing output -- format changes are highly visible

---

### Rank 7: CBTRN03C -- Transaction Report (Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 649 |
| **IF Statements** | 38 |
| **EVALUATE Blocks** | 4 |
| **PERFORM Calls** | 72 |
| **Copybook Dependencies** | 5 (CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y) |
| **Complexity Score** | 7 |
| **Risk Score** | 6 |
| **Business Impact Score** | 7 |
| **Composite Score** | **6.7** |

**Why it's #7:** Highest PERFORM count (72) of any program, indicating highly-factored procedural logic. Generates the daily transaction report used for operational monitoring. Joins data from transactions, transaction types, and transaction categories -- a multi-file join pattern.

**Modernization Concerns:**
- 72 PERFORM calls suggest many small paragraphs (fine-grained procedure division)
- Multi-file join logic (transaction + type + category lookups)
- Report formatting with page totals, account totals, and grand totals
- Uses CVTRA07Y report layout copybook with formatted output fields

---

### Rank 8: COACTVWC -- Account View

| Metric | Value |
|--------|-------|
| **Lines of Code** | 941 |
| **IF Statements** | 28 |
| **EVALUATE Blocks** | 10 |
| **PERFORM Calls** | 18 |
| **CICS Commands** | 15 |
| **Copybook Dependencies** | 15 |
| **Complexity Score** | 7 |
| **Risk Score** | 5 |
| **Business Impact Score** | 8 |
| **Composite Score** | **6.7** |

**Why it's #8:** Most-used screen in the application (account inquiry is the primary user workflow). Has the highest copybook fan-out (15 dependencies) among online programs after COACTUPC. Reads from four VSAM files to assemble a complete account view. While read-only, its high usage frequency means any modernization defect has maximum user impact.

**Modernization Concerns:**
- Assembles data from 4 VSAM files (account, card, cross-ref, customer)
- 15 copybook dependencies create a wide compilation dependency surface
- High-traffic screen -- performance regression would be immediately noticed
- String-processing utility (CSSTRPFY) inline COPY for field formatting

---

### Rank 9: COTRN02C -- Transaction Add (Online)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 783 |
| **IF Statements** | 14 |
| **EVALUATE Blocks** | 13 |
| **PERFORM Calls** | 61 |
| **CICS Commands** | 11 |
| **CALL Targets** | CSUTLDTC |
| **Complexity Score** | 7 |
| **Risk Score** | 8 |
| **Business Impact Score** | 7 |
| **Composite Score** | **7.3** |

**Why it's #9:** The only online program that creates new financial records (transactions). Calls the date validation utility (CSUTLDTC) and writes to the TRANSACT VSAM file. It bridges the online and batch worlds -- transactions entered here flow into the nightly batch posting cycle.

**Modernization Concerns:**
- Financial record creation -- data integrity is critical
- External CALL to CSUTLDTC for date validation
- Cross-file lookups (account, cross-reference) for validation
- Entry point for data that feeds the entire batch pipeline

---

### Rank 10: COUSR00C -- User List (Admin)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 695 |
| **IF Statements** | 25 |
| **EVALUATE Blocks** | 8 |
| **PERFORM Calls** | 41 |
| **CICS Commands** | 11 |
| **Complexity Score** | 6 |
| **Risk Score** | 7 |
| **Business Impact Score** | 6 |
| **Composite Score** | **6.3** |

**Why it's #10:** Gateway to the user administration subsystem. Implements the browse/paging pattern (like COCRDLIC) for the user security file. Controls navigation to Add/Update/Delete user screens. Security administration functions are inherently high-risk -- unauthorized access or data corruption affects all system users.

**Modernization Concerns:**
- Paging/browse pattern on USRSEC VSAM file
- Security-sensitive: manages user access control records
- Navigates to three sub-screens (COUSR01C-03C) -- must migrate as a unit
- Plaintext password display -- must be remediated during modernization

---

## Hotspot Summary Table

| Rank | Program | LOC | Branches | Composite | Primary Risk |
|-----:|---------|----:|:--------:|:---------:|-------------|
| 1 | COACTUPC | 4,236 | 174 | **9.7** | Extreme complexity + account balance mutations |
| 2 | CBTRN02C | 731 | 48 | **9.2** | Multi-file financial posting (3 VSAM writes) |
| 3 | COCRDUPC | 1,560 | 88 | **8.4** | Complex state machine + sensitive card data |
| 4 | COCRDLIC | 1,459 | 77 | **7.5** | CICS browse pattern (hardest to modernize) |
| 5 | CBACT04C | 652 | 43 | **8.2** | Revenue logic + decimal precision |
| 6 | CBSTM03A | 924 | 20 | **7.3** | Multi-file joins + customer-facing output |
| 7 | CBTRN03C | 649 | 42 | **6.7** | Highest PERFORM count + multi-file joins |
| 8 | COACTVWC | 941 | 38 | **6.7** | Highest-traffic screen + 4-file assembly |
| 9 | COTRN02C | 783 | 27 | **7.3** | Financial record creation + batch pipeline entry |
| 10 | COUSR00C | 695 | 33 | **6.3** | Security admin + browse pattern |

---

## Modernization Priority Recommendations

### Phase 1 -- Critical Path (Migrate First)

| Priority | Module | Rationale |
|:--------:|--------|-----------|
| P0 | **CBTRN02C** | Core financial posting. Must be correct before anything else. Start here to establish the data migration foundation. |
| P0 | **CBACT04C** | Interest calculation. Revenue-critical and requires exact decimal parity testing. |
| P0 | **COTRN02C** | Online transaction entry. Feeds the batch pipeline. Must be migrated in sync with CBTRN02C. |

### Phase 2 -- High Complexity (Refactor & Migrate)

| Priority | Module | Rationale |
|:--------:|--------|-----------|
| P1 | **COACTUPC** | Needs decomposition into separate validation, business-logic, and persistence layers before migration. |
| P1 | **COCRDUPC** | Complex state machine. Extract validation rules into a card service. |
| P1 | **CBSTM03A + CBSTM03B** | Tightly coupled pair. Migrate together. Replace mainframe control block logic. |

### Phase 3 -- Standard Migration

| Priority | Module | Rationale |
|:--------:|--------|-----------|
| P2 | **COCRDLIC** | Browse pattern. Define the pagination API pattern that all list screens will follow. |
| P2 | **COACTVWC** | High-traffic read path. Good candidate for performance benchmarking. |
| P2 | **CBTRN03C** | Reporting. Replace with modern reporting framework (e.g., JasperReports). |
| P2 | **COUSR00C** | Security admin. Replace plaintext passwords with proper auth framework. |

### Cross-Cutting Modernization Actions

| Action | Affected Modules | Description |
|--------|-----------------|-------------|
| Extract COMMAREA | All 17 online programs | Replace COCOM01Y-based COMMAREA with session/context management |
| Replace BMS screens | All 17 online programs | Map BMS maps to web UI components (React/Angular/Thymeleaf) |
| VSAM to RDBMS | All 44 programs | Migrate 11 VSAM files to relational tables |
| Decimal parity testing | CBTRN02C, CBACT04C, COBIL00C | Validate financial calculations produce identical results |
| Password security | COUSR00C-03C, COSGN00C | Replace plaintext USRSEC passwords with hashed credentials |
| Date utility consolidation | CSUTLDTC, CORPT00C, COTRN02C | Replace CEEDAYS calls with java.time |
