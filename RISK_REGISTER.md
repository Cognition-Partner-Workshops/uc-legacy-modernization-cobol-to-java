# CardDemo Risk Register

## Overview

This document catalogs the top risks associated with modernizing the CardDemo mainframe COBOL/CICS application to Java/Spring Boot, along with probability, impact, mitigation strategies, and contingency plans. Risks are organized by category and ranked by overall severity (Probability x Impact).

---

## Risk Scoring Framework

| Score | Probability | Impact |
|-------|------------|--------|
| 5 | Almost Certain (>90%) | Catastrophic -- data loss, financial misstatement, extended outage |
| 4 | Likely (60--90%) | Major -- significant rework, deadline slip > 4 weeks, production degradation |
| 3 | Possible (30--60%) | Moderate -- rework < 2 weeks, partial feature delay, workaround available |
| 2 | Unlikely (10--30%) | Minor -- localized issue, < 1 week rework, minimal user impact |
| 1 | Rare (<10%) | Negligible -- cosmetic, easily fixed, no user impact |

**Severity** = Probability x Impact (range 1--25)

| Severity Range | Classification | Action |
|---------------|---------------|--------|
| 15--25 | **Critical** | Immediate mitigation required; escalate to leadership |
| 8--14 | **High** | Active mitigation plan; monitor weekly |
| 4--7 | **Medium** | Mitigation planned; monitor monthly |
| 1--3 | **Low** | Accept or monitor as needed |

---

## Risk Summary Table

| ID | Risk | Category | Prob | Impact | Severity | Phase |
|----|------|----------|------|--------|----------|-------|
| R01 | Financial calculation discrepancy (interest/posting) | Technical | 4 | 5 | **20 Critical** | 5 |
| R02 | Data migration loss or corruption | Data | 3 | 5 | **15 Critical** | 0, 3 |
| R03 | COBOL business rule misinterpretation | Technical | 4 | 4 | **16 Critical** | 3, 4 |
| R04 | Dual-write consistency failure during transition | Technical | 3 | 4 | **12 High** | 3, 4 |
| R05 | Mainframe SME availability | Organizational | 4 | 3 | **12 High** | All |
| R06 | Performance degradation in batch processing | Technical | 3 | 4 | **12 High** | 5 |
| R07 | VSAM-to-RDBMS data model mismatch | Data | 3 | 3 | **9 High** | 0, 3 |
| R08 | CICS transaction semantics lost in translation | Technical | 3 | 3 | **9 High** | 4 |
| R09 | Scope creep from optional modules | Organizational | 3 | 3 | **9 High** | 6 |
| R10 | Security regression (auth downgrade) | Security | 2 | 5 | **10 High** | 1 |
| R11 | Batch job orchestration timing failures | Technical | 3 | 3 | **9 High** | 5 |
| R12 | Team skill gap (COBOL-to-Java) | Organizational | 3 | 3 | **9 High** | All |
| R13 | Incomplete test coverage for edge cases | Quality | 4 | 2 | **8 High** | All |
| R14 | Anti-corruption layer complexity | Technical | 3 | 2 | **6 Medium** | 1--5 |
| R15 | Regulatory compliance gap | Compliance | 2 | 4 | **8 High** | 5, 6 |
| R16 | Extended parallel-run costs | Financial | 3 | 2 | **6 Medium** | 4, 5 |
| R17 | Third-party dependency changes | Technical | 2 | 2 | **4 Medium** | All |
| R18 | Rollback failure during cutover | Operational | 2 | 5 | **10 High** | 6 |
| R19 | Character encoding data corruption (EBCDIC/ASCII) | Data | 2 | 3 | **6 Medium** | 0, 2 |
| R20 | Stakeholder confidence loss | Organizational | 2 | 3 | **6 Medium** | All |

---

## Detailed Risk Analysis

### R01: Financial Calculation Discrepancy (Interest/Posting)

**Category**: Technical
**Probability**: 4 (Likely) | **Impact**: 5 (Catastrophic) | **Severity**: 20 (Critical)
**Phase**: 5 (Batch Processing)

**Description**: COBOL uses packed decimal arithmetic (PIC S9(09)V99) with specific rounding behavior. Java floating-point or incorrect BigDecimal usage could produce interest calculations that differ by fractions of a cent, accumulating to material discrepancies across thousands of accounts.

**Root Cause Analysis**:
- CBACT04C computes interest: `COMPUTE WS-MONTHLY-INT = (balance * rate) / 12`
- COBOL packed decimal truncates differently than Java `double`
- CBTRN02C updates account balances with accumulated transaction amounts
- Rounding differences compound over billing cycles

**Mitigation Strategies**:
1. **Mandatory**: Use `java.math.BigDecimal` with `RoundingMode.HALF_UP` and scale of 2 for all financial calculations
2. **Mandatory**: 30-day parallel run comparing every account balance to the cent
3. Create a COBOL-to-Java arithmetic test harness with 10,000+ test cases covering boundary values
4. Document every COMPUTE statement in CBACT04C and CBTRN02C with its Java equivalent and verify rounding behavior
5. Engage an auditor to validate calculation parity before mainframe decommission

**Contingency Plan**: If discrepancies are found, revert to mainframe batch processing. Investigate root cause using the reconciliation logs. Fix Java calculations and restart 30-day parallel run.

**Owner**: Tech Lead + Finance
**Review Frequency**: Weekly during Phase 5

---

### R02: Data Migration Loss or Corruption

**Category**: Data
**Probability**: 3 (Possible) | **Impact**: 5 (Catastrophic) | **Severity**: 15 (Critical)
**Phase**: 0 (Foundation), 3 (Account/Card)

**Description**: Migrating data from VSAM fixed-length records to PostgreSQL tables risks field misalignment, character encoding issues (EBCDIC vs. ASCII), or silent truncation. FILLER fields may contain data that is not mapped.

**Root Cause Analysis**:
- VSAM records use fixed-length fields with implicit decimal positions (PIC S9(10)V99 = 12 digits, implied decimal)
- FILLER fields in copybooks (e.g., CVACT01Y has 178-byte FILLER, CVCUS01Y has 168-byte FILLER) may contain undocumented data
- EBCDIC-to-ASCII conversion for packed decimal and signed numeric fields requires precise handling
- Record lengths: ACCTDAT=300, CARDDAT=150, CUSTDAT=500, TRANSACT=350 -- any misalignment corrupts all subsequent fields

**Mitigation Strategies**:
1. **Mandatory**: Validate record counts before and after migration (source VSAM vs. target table)
2. **Mandatory**: Checksum validation -- hash all non-FILLER field values and compare
3. Inspect FILLER fields for non-space content before discarding
4. Create field-level data profiling reports (min/max/null counts/distinct values) for both source and target
5. Use the ASCII data files in `app/data/ASCII/` as ground truth for encoding validation
6. Implement reversible migration: keep VSAM files untouched during transition

**Contingency Plan**: Restore VSAM files from backup. Fix migration scripts and re-run. Never delete VSAM source data until 90 days post-decommission.

**Owner**: DBA + Data Team
**Review Frequency**: Weekly during Phases 0 and 3

---

### R03: COBOL Business Rule Misinterpretation

**Category**: Technical
**Probability**: 4 (Likely) | **Impact**: 4 (Major) | **Severity**: 16 (Critical)
**Phase**: 3 (Account Mgmt), 4 (Transactions)

**Description**: COACTUPC (4,237 lines) contains extensive field-level validation logic with complex conditional paths. Misinterpreting or omitting a validation rule during conversion results in accepting invalid data or rejecting valid data.

**Root Cause Analysis**:
- COACTUPC validates: SSN (3-part decomposition), phone numbers (area code + number), dates (cross-field checks: open < expiry, reissue <= expiry), credit limits (cash <= total), state codes, FICO ranges
- COBOL EVALUATE statements with WHEN OTHER clauses may handle edge cases not immediately obvious
- 88-level condition names encode business rules implicitly (e.g., `88 CDEMO-USRTYP-ADMIN VALUE 'A'`)
- Batch programs (CBTRN02C) have validation extensibility comments ("ADD MORE VALIDATIONS HERE") suggesting undocumented future rules

**Mitigation Strategies**:
1. **Mandatory**: Line-by-line walkthrough of COACTUPC with a mainframe SME, documenting every validation rule in a decision table
2. Create comprehensive test cases covering every EVALUATE/IF branch in COACTUPC (aim for 100% branch coverage of the original COBOL)
3. Use COBOL code analysis tools to extract branch/condition coverage requirements
4. Implement validation rules as a separate, testable Java class (`AccountValidator`) with unit tests per rule
5. Run both COBOL and Java validation against the same 1,000+ test records and compare results field by field

**Contingency Plan**: If validation discrepancies are found in production, add the missing rule to Java and deploy a hotfix. Log all validation failures with full input data for post-mortem analysis.

**Owner**: Tech Lead + Mainframe SME
**Review Frequency**: Weekly during Phases 3 and 4

---

### R04: Dual-Write Consistency Failure During Transition

**Category**: Technical
**Probability**: 3 (Possible) | **Impact**: 4 (Major) | **Severity**: 12 (High)
**Phase**: 3 (Account/Card), 4 (Transactions)

**Description**: During the strangler fig transition, the ACL dual-writes to both VSAM and PostgreSQL. Network failures, timing issues, or transaction boundary mismatches could cause the two stores to diverge.

**Mitigation Strategies**:
1. Designate one store as the **system of record** (SOR) at all times -- VSAM in Phase 3, database in Phase 4+
2. Implement async reconciliation job running every hour comparing SOR with secondary
3. Use change-data-capture (CDC) for near-real-time sync rather than synchronous dual-write where possible
4. Accept eventual consistency with a maximum divergence window of 1 hour
5. Alert immediately when reconciliation detects any discrepancy

**Contingency Plan**: If stores diverge, the SOR is authoritative. Re-sync secondary from SOR. Investigate root cause before resuming dual-write.

**Owner**: Tech Lead + DBA
**Review Frequency**: Daily during active dual-write phases

---

### R05: Mainframe SME Availability

**Category**: Organizational
**Probability**: 4 (Likely) | **Impact**: 3 (Moderate) | **Severity**: 12 (High)
**Phase**: All

**Description**: COBOL and CICS expertise is scarce and aging. Key knowledge about CardDemo's business rules, batch job dependencies, and operational procedures may reside in one or two individuals who could become unavailable.

**Mitigation Strategies**:
1. **Mandatory**: Conduct knowledge transfer sessions in Phase 0 and document all tribal knowledge
2. Record all code walkthrough sessions for future reference
3. Create detailed decision tables for every COBOL program's business logic
4. Cross-train at least two Java developers on basic COBOL reading and CICS concepts
5. Engage mainframe SME on a retained basis (part-time) throughout the project, not just the early phases
6. Prioritize extraction of complex programs (COACTUPC, CBTRN02C, CBACT04C) while SME is available

**Contingency Plan**: If SME becomes unavailable, use recorded sessions and documented decision tables. Engage external COBOL consulting firm as backup. Pause high-risk phases until SME coverage is restored.

**Owner**: Project Manager
**Review Frequency**: Monthly

---

### R06: Performance Degradation in Batch Processing

**Category**: Technical
**Probability**: 3 (Possible) | **Impact**: 4 (Major) | **Severity**: 12 (High)
**Phase**: 5 (Batch Processing)

**Description**: Mainframe batch jobs (CBTRN02C, CBACT04C) process sequential VSAM files with optimized I/O. The Java equivalent reading from PostgreSQL may not achieve the same throughput within the nightly batch window.

**Root Cause Analysis**:
- VSAM sequential reads are highly optimized on z/OS with large buffers
- CBTRN02C reads DALYTRAN sequentially and writes TRANSACT -- simple sequential I/O
- CBACT04C reads TCATBAL sequentially, grouped by account -- relies on physical sort order
- PostgreSQL may require index scans instead of sequential access for equivalent operations
- Spring Batch overhead (chunk processing, transaction management) adds latency per record

**Mitigation Strategies**:
1. Benchmark early: create a prototype Spring Batch job with production-volume test data in Phase 0
2. Tune Spring Batch chunk size (start with 1000, adjust based on benchmarks)
3. Use `JdbcCursorItemReader` (streaming) instead of `JdbcPagingItemReader` (paging) for sequential access
4. Pre-sort data in database (ORDER BY account_id) to match COBOL sequential access patterns
5. Use database connection pooling (HikariCP) with sufficient pool size for batch operations
6. Consider partitioned processing: split accounts across multiple Spring Batch partitions for parallelism

**Contingency Plan**: If batch window is exceeded, implement parallel partitioned processing. If still insufficient, extend the batch window or move to real-time/near-real-time event processing for transaction posting.

**Owner**: Tech Lead + DBA
**Review Frequency**: Weekly during Phase 5

---

### R07: VSAM-to-RDBMS Data Model Mismatch

**Category**: Data
**Probability**: 3 (Possible) | **Impact**: 3 (Moderate) | **Severity**: 9 (High)
**Phase**: 0 (Schema Design), 3 (Account/Card)

**Description**: VSAM files use composite keys, alternate indexes (AIX), and fixed-length records that don't map cleanly to relational tables. The CARDAIX and CXACAIX alternate indexes encode implicit relationships that must become explicit foreign keys.

**Root Cause Analysis**:
- CARDAIX: alternate index on CARDDAT by CARD-ACCT-ID -- becomes FK from `cards` to `accounts`
- CXACAIX: alternate index on CCXREF by XREF-ACCT-ID -- becomes FK from `card_xref` to `accounts`
- TCATBAL uses a composite key (account ID + transaction type + category code) not obvious from the copybook
- TRANSACT KSDS key is TRAN-ID (16 bytes) -- auto-increment in COBOL via HIGH-VALUES read

**Mitigation Strategies**:
1. Model the relational schema carefully during Phase 0 with DBA and mainframe SME
2. Document every VSAM file's key structure, alternate indexes, and access patterns
3. Create an Entity-Relationship Diagram (ERD) and validate it against all COBOL program access patterns
4. Prototype the most complex queries (account view with joined customer/card/xref) and verify performance
5. Consider denormalization where COBOL programs do multi-file joins that would be expensive in SQL

**Contingency Plan**: Adjust schema design based on performance profiling. Add database indexes or materialized views as needed.

**Owner**: DBA + Tech Lead
**Review Frequency**: Weekly during Phase 0

---

### R08: CICS Transaction Semantics Lost in Translation

**Category**: Technical
**Probability**: 3 (Possible) | **Impact**: 3 (Moderate) | **Severity**: 9 (High)
**Phase**: 4 (Transaction/Payment)

**Description**: CICS provides implicit transaction management: each CICS task is a unit of work with automatic rollback on ABEND. COBIL00C uses READ FOR UPDATE (pessimistic locking) which doesn't directly translate to Spring's optimistic concurrency default.

**Root Cause Analysis**:
- CICS READ with UPDATE option locks the VSAM record until REWRITE or task end
- COBIL00C: reads account FOR UPDATE -> creates transaction -> rewrites account balance
- COTRN02C: reads HIGH-VALUES for last ID -> writes new record (relies on VSAM key ordering)
- CICS pseudo-conversational model (RETURN TRANSID) has no direct REST equivalent
- CICS SYNCPOINT provides 2-phase commit across multiple VSAM files

**Mitigation Strategies**:
1. Use `@Transactional` with `SELECT ... FOR UPDATE` for payment processing (matching CICS locking semantics)
2. Implement optimistic locking with version columns for lower-risk read/update cycles
3. Replace CICS pseudo-conversational model with stateless REST + database state
4. Document every EXEC CICS SYNCPOINT and EXEC CICS ABEND to ensure equivalent Java exception handling
5. Load-test concurrent payment processing to verify no lost updates or double payments

**Contingency Plan**: If concurrency issues arise, switch affected operations to pessimistic locking. Add distributed locking (Redis) if database-level locking is insufficient.

**Owner**: Tech Lead
**Review Frequency**: Weekly during Phase 4

---

### R09: Scope Creep from Optional Modules

**Category**: Organizational
**Probability**: 3 (Possible) | **Impact**: 3 (Moderate) | **Severity**: 9 (High)
**Phase**: 6

**Description**: The optional modules (authorization/IMS/DB2/MQ, transaction type DB2, VSAM-MQ) introduce additional complexity. If stakeholders insist on migrating all optional modules simultaneously, it could delay the overall timeline.

**Mitigation Strategies**:
1. Clearly classify optional modules as Phase 6 scope in the project charter
2. Define explicit inclusion/exclusion criteria for each optional module
3. Allow optional modules to be dropped or deferred without impacting core migration
4. Estimate optional module effort separately from core migration budget
5. Implement optional modules as separate microservices that can be enabled/disabled independently

**Contingency Plan**: Defer optional modules to a follow-up project phase. Core CardDemo functions work without them.

**Owner**: Project Manager + Product Owner
**Review Frequency**: Monthly

---

### R10: Security Regression (Authentication Downgrade)

**Category**: Security
**Probability**: 2 (Unlikely) | **Impact**: 5 (Catastrophic) | **Severity**: 10 (High)
**Phase**: 1 (Identity & Access)

**Description**: The current system stores plaintext passwords in USRSEC VSAM file. During migration, a misconfigured Spring Security setup could introduce vulnerabilities (weak JWT signing, exposed endpoints, missing CSRF protection).

**Root Cause Analysis**:
- Current COBOL auth (COSGN00C) compares plaintext: `IF WS-USER-ID = SEC-USR-ID AND WS-USER-PWD = SEC-USR-PWD`
- Migration must upgrade to bcrypt-hashed passwords -- transition period may have mixed auth
- JWT token signing key management is a new concern not present in mainframe
- CICS security is network-perimeter based (LU6.2 sessions); REST APIs are exposed on HTTP

**Mitigation Strategies**:
1. **Mandatory**: Hash all passwords with bcrypt during initial data load (never store plaintext in PostgreSQL)
2. **Mandatory**: Penetration test the identity-service before go-live
3. Use strong JWT signing (RS256 with key rotation) not HS256 with static secret
4. Implement rate limiting on login endpoint to prevent brute-force attacks
5. Enable HTTPS-only access to all REST APIs
6. Add OWASP security headers and CSRF protection to the web frontend
7. Conduct security review of Spring Security configuration before Phase 1 go-live

**Contingency Plan**: If a security vulnerability is discovered, immediately revert to CICS-based authentication and remediate.

**Owner**: Security Team + Tech Lead
**Review Frequency**: Weekly during Phase 1, then monthly

---

### R11: Batch Job Orchestration Timing Failures

**Category**: Technical
**Probability**: 3 (Possible) | **Impact**: 3 (Moderate) | **Severity**: 9 (High)
**Phase**: 5 (Batch Processing)

**Description**: The mainframe batch cycle has strict ordering enforced by JCL job dependencies (CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> OPENFIL). Missequencing in Spring Batch could cause data integrity issues.

**Mitigation Strategies**:
1. Model the batch cycle as a Spring Batch flow with explicit step dependencies (not independent jobs)
2. Implement step-level exit codes matching COBOL return codes (0=success, 4=warnings, 8+=failure)
3. Add conditional flow: if POSTTRAN returns 8+, abort the entire cycle
4. Eliminate CLOSEFIL/OPENFIL steps (database doesn't require file locking) but add equivalent quiesce logic if needed
5. Monitor batch execution time per step and alert if any step exceeds 2x expected duration

**Contingency Plan**: If orchestration fails, re-run the batch cycle from the beginning. Spring Batch restart capability ensures steps that completed successfully are not re-executed.

**Owner**: Tech Lead + Ops
**Review Frequency**: Weekly during Phase 5

---

### R12: Team Skill Gap (COBOL-to-Java)

**Category**: Organizational
**Probability**: 3 (Possible) | **Impact**: 3 (Moderate) | **Severity**: 9 (High)
**Phase**: All

**Description**: Java developers may struggle to understand COBOL patterns (WORKING-STORAGE, PERFORM VARYING, REDEFINES, 88-levels, condition names). This slows down accurate translation and increases the risk of misinterpreting business logic.

**Mitigation Strategies**:
1. Provide COBOL literacy training for Java developers (2-day bootcamp covering key patterns)
2. Create a COBOL-to-Java pattern reference guide mapping common constructs
3. Pair each Java developer with a mainframe SME for the first month
4. Use automated COBOL analysis tools to generate flowcharts and call graphs
5. Start with simpler programs (COSGN00C, COUSR01C) to build team confidence before tackling COACTUPC

**Contingency Plan**: Engage specialist COBOL modernization consultants for the most complex programs. Use automated conversion tools (Blu Age, TSRI) as a starting point for complex programs.

**Owner**: Tech Lead + HR
**Review Frequency**: Monthly

---

### R13: Incomplete Test Coverage for Edge Cases

**Category**: Quality
**Probability**: 4 (Likely) | **Impact**: 2 (Minor) | **Severity**: 8 (High)
**Phase**: All

**Description**: COBOL programs handle many edge cases implicitly (numeric overflow, space-padded strings, signed zero). Without exhaustive test cases, edge case differences between COBOL and Java may surface in production.

**Root Cause Analysis**:
- COBOL PIC S9(09)V99 fields silently truncate on overflow; Java BigDecimal throws ArithmeticException
- COBOL string comparisons are space-padded; Java String.equals() is not
- COBOL EVALUATE with no matching WHEN falls through silently; Java switch without default may miss cases
- COBOL condition names (88-level) encode business rules that may not be captured in standard unit tests

**Mitigation Strategies**:
1. Generate test data from the ASCII sample files in `app/data/ASCII/`
2. Create boundary-value test cases for every numeric field (zero, max PIC value, negative, overflow)
3. Test string fields with leading/trailing spaces, all-spaces, and LOW-VALUES
4. Achieve minimum 90% code branch coverage on all Java services
5. Use property-based testing (e.g., jqwik) for financial calculations to explore edge cases automatically

**Contingency Plan**: Maintain a production incident log mapping each defect to the missing test case. Build regression suite incrementally from production issues.

**Owner**: QA Lead
**Review Frequency**: Weekly

---

### R14: Anti-Corruption Layer Complexity

**Category**: Technical
**Probability**: 3 (Possible) | **Impact**: 2 (Minor) | **Severity**: 6 (Medium)
**Phase**: 1--5

**Description**: The ACL must translate between CICS/COMMAREA/VSAM and REST/JWT/RDBMS. As more services are extracted, the ACL becomes more complex and harder to maintain. There is risk of the ACL becoming a permanent fixture rather than a transitional component.

**Mitigation Strategies**:
1. Design ACL to be eliminated phase by phase -- each phase should reduce ACL surface area
2. Track ACL routes and set a metric: percentage of traffic going through ACL vs. direct to Java services
3. Set a hard deadline for ACL removal (Phase 6 Week 5) and enforce it
4. Keep ACL logic stateless and thin -- no business logic in the ACL
5. Document every ACL translation rule so removal is straightforward

**Contingency Plan**: If ACL becomes too complex, simplify by accelerating migration of the programs it supports.

**Owner**: Tech Lead
**Review Frequency**: Monthly

---

### R15: Regulatory Compliance Gap

**Category**: Compliance
**Probability**: 2 (Unlikely) | **Impact**: 4 (Major) | **Severity**: 8 (High)
**Phase**: 5 (Batch), 6 (Decommission)

**Description**: Credit card processing is subject to PCI-DSS, SOX, and other regulations. The modernized system must meet all compliance requirements that the mainframe currently satisfies (audit logging, data encryption, access controls, data retention).

**Mitigation Strategies**:
1. Conduct a compliance gap analysis during Phase 0 comparing mainframe controls to target platform
2. Implement audit logging from Day 1 (Spring AOP or event sourcing)
3. Encrypt card numbers (CARD-NUM) at rest using database column encryption (AES-256)
4. Implement row-level security for multi-tenant access patterns
5. Engage compliance officer early and include them in all go/no-go decision gates
6. Document the entire migration process for SOX audit trail

**Contingency Plan**: If compliance gaps are found during audit, halt migration and remediate. Maintain mainframe as fallback until compliance is achieved.

**Owner**: Compliance Officer + Tech Lead
**Review Frequency**: Quarterly, plus at each go/no-go gate

---

### R16: Extended Parallel-Run Costs

**Category**: Financial
**Probability**: 3 (Possible) | **Impact**: 2 (Minor) | **Severity**: 6 (Medium)
**Phase**: 4 (14-day parallel), 5 (30-day parallel)

**Description**: Running both mainframe and cloud infrastructure during parallel-run periods increases operational costs. If parallel runs must be extended due to reconciliation failures, costs escalate.

**Mitigation Strategies**:
1. Budget for 2x the planned parallel-run duration as contingency
2. Use cloud spot/preemptible instances for non-critical parallel-run workloads
3. Automate reconciliation to minimize manual effort
4. Define clear exit criteria for parallel runs to avoid open-ended extensions
5. Track daily parallel-run costs and report to stakeholders weekly

**Contingency Plan**: If parallel-run costs exceed 150% of budget, escalate to executive sponsor for decision: extend with additional funding or accept remaining risk and cut over.

**Owner**: Project Manager + Finance
**Review Frequency**: Weekly during parallel-run phases

---

### R17: Third-Party Dependency Changes

**Category**: Technical
**Probability**: 2 (Unlikely) | **Impact**: 2 (Minor) | **Severity**: 4 (Medium)
**Phase**: All

**Description**: The target stack (Spring Boot 3.x, Spring Batch, Spring Security, PostgreSQL) may release breaking changes during the 9--12 month migration. Dependency version conflicts could cause build failures.

**Mitigation Strategies**:
1. Pin all dependency versions in Maven POM (no SNAPSHOT or LATEST)
2. Use Dependabot or Renovate for controlled dependency updates
3. Evaluate Spring Boot LTS release schedule and align with a stable version
4. Maintain a separate dependency update sprint every 3 months
5. Include dependency scanning in CI/CD pipeline (OWASP dependency-check)

**Contingency Plan**: If a critical dependency has a breaking change, pin to the last working version and schedule an upgrade sprint.

**Owner**: Tech Lead
**Review Frequency**: Monthly

---

### R18: Rollback Failure During Cutover

**Category**: Operational
**Probability**: 2 (Unlikely) | **Impact**: 5 (Catastrophic) | **Severity**: 10 (High)
**Phase**: 6 (Decommission)

**Description**: After mainframe decommission, if a critical defect is discovered, restarting the mainframe CICS region and VSAM files may fail due to data staleness or configuration drift.

**Mitigation Strategies**:
1. Keep mainframe in cold standby for 90 days post-decommission
2. Take a full VSAM backup immediately before decommission
3. Document the exact mainframe restart procedure and test it during Phase 6 Week 5
4. Maintain CICS CSD (resource definitions) and JCL libraries in version control
5. Test mainframe restart from cold standby during a non-production window before final decommission

**Contingency Plan**: If rollback is needed: restore VSAM from backup, restart CICS region, repoint API Gateway to mainframe. Accept that data created since decommission will need manual reconciliation.

**Owner**: Ops + Mainframe Team
**Review Frequency**: Weekly during Phase 6

---

### R19: Character Encoding Data Corruption (EBCDIC/ASCII)

**Category**: Data
**Probability**: 2 (Unlikely) | **Impact**: 3 (Moderate) | **Severity**: 6 (Medium)
**Phase**: 0 (Data Migration), 2 (Export/Import)

**Description**: Mainframe data is stored in EBCDIC. The ASCII conversion in `app/data/ASCII/` may have edge cases (accented characters in customer names, packed decimal fields misinterpreted as character data).

**Mitigation Strategies**:
1. Use the provided ASCII data files as the migration source (already converted)
2. Validate packed decimal fields (PIC S9(n)V99) are correctly unpacked to numeric values
3. Test with customer names containing special characters (apostrophes, hyphens, accented letters)
4. Verify COMP and COMP-3 fields are correctly converted to Java numeric types
5. Create a character-by-character comparison tool for spot-checking migrated records

**Contingency Plan**: If encoding issues are found, create a custom field-level converter for the affected fields. Re-migrate only the affected records.

**Owner**: Data Team
**Review Frequency**: Weekly during Phase 0

---

### R20: Stakeholder Confidence Loss

**Category**: Organizational
**Probability**: 2 (Unlikely) | **Impact**: 3 (Moderate) | **Severity**: 6 (Medium)
**Phase**: All

**Description**: If early phases encounter visible issues (login failures, data discrepancies, performance problems), stakeholders may lose confidence in the migration and push for cancellation or a different approach.

**Mitigation Strategies**:
1. Deliver Phase 1 (login + user management) quickly and successfully to build confidence
2. Provide weekly status reports with metrics (records migrated, reconciliation results, test coverage)
3. Demo working features at the end of each phase
4. Be transparent about issues and show mitigation progress
5. Maintain a public risk dashboard showing risk status and trends

**Contingency Plan**: If confidence drops significantly, conduct a project health review with an independent assessor. Present options (continue, pivot, pause) with data-backed recommendations.

**Owner**: Project Manager
**Review Frequency**: Monthly

---

## Risk Heat Map

```
Impact
  5 |  R18      R10       R02       R01
    |                               
  4 |  R15                R03
    |           R04  R06
  3 |  R19,R20  R07,R08   R05
    |           R09,R11
    |           R12
  2 |  R17      R14,R16   R13
    |
  1 |
    +----------------------------------------
       1        2         3         4        5
                    Probability
```

---

## Risk Monitoring & Escalation

### Monitoring Cadence

| Severity | Review Frequency | Escalation Path |
|----------|-----------------|-----------------|
| Critical (15--25) | Weekly | Tech Lead -> Project Manager -> Executive Sponsor |
| High (8--14) | Bi-weekly | Tech Lead -> Project Manager |
| Medium (4--7) | Monthly | Tech Lead |
| Low (1--3) | Quarterly | Team Lead |

### Key Risk Indicators (KRIs)

| KRI | Threshold | Action |
|-----|-----------|--------|
| Reconciliation discrepancy count | > 0 per day | Investigate immediately; pause dual-write if persistent |
| Parallel-run match rate | < 100% | Investigate every mismatch; do not proceed to next phase |
| Batch job duration | > 150% of mainframe | Performance tuning sprint |
| Security scan findings (critical) | > 0 | Block deployment until resolved |
| Team velocity (story points) | < 70% of plan | Assess blockers; consider additional resources |
| SME availability | < 50% of planned hours | Escalate to Project Manager; engage backup SME |
