# TECHNICAL DEBT REPORT

## HRMS Oracle Forms / PL/SQL Estate — Security, Reliability & Performance Audit

> **System**: Human Resources Management System (HRMS)
> **Schema**: `HRMS` on Oracle Database 19c
> **Platform**: Oracle Forms 12c, Oracle WebLogic 12c
> **Assessment Date**: 2026-03-25
> **Total Findings**: 42
> **Critical**: 6 | **High**: 13 | **Medium**: 18 | **Low**: 5

---

## Severity Definitions

| Severity | Definition |
|----------|-----------|
| **CRITICAL** | Active exploit risk, data loss, or regulatory violation. Must fix before go-live. |
| **HIGH** | Significant reliability or security gap. Fix within current release cycle. |
| **MEDIUM** | Performance or maintainability issue. Plan for next 2 sprints. |
| **LOW** | Code quality or minor inefficiency. Address opportunistically. |

---

## Table of Contents

1. [Security Vulnerabilities](#1-security-vulnerabilities)
2. [Race Conditions & Concurrency Bugs](#2-race-conditions--concurrency-bugs)
3. [Data Integrity & Logic Bugs](#3-data-integrity--logic-bugs)
4. [Performance Anti-Patterns](#4-performance-anti-patterns)
5. [Validation Drift](#5-validation-drift)
6. [Hard-Coded Configuration](#6-hard-coded-configuration)
7. [Architecture & Maintainability](#7-architecture--maintainability)
8. [Incomplete / Dead Code](#8-incomplete--dead-code)
9. [Findings Summary Table](#9-findings-summary-table)
10. [Recommended Remediation Priority](#10-recommended-remediation-priority)

---

## 1. Security Vulnerabilities

### SEC-01: MD5 Password Hashing (CRITICAL)

**Location**: `PKG_SECURITY.pkb` — `hash_password` function
**Evidence**: Uses `DBMS_OBFUSCATION_TOOLKIT.MD5` or equivalent MD5 hash for password storage.

**Risk**: MD5 is cryptographically broken. Rainbow tables and brute-force attacks can reverse MD5 hashes in seconds. Any database breach exposes all user passwords immediately.

**Recommendation**: Migrate to `DBMS_CRYPTO` with SHA-256 + per-user salt, or bcrypt/scrypt via Java stored procedure. Require password reset for all users after migration.

---

### SEC-02: Hard-Coded Encryption Key (CRITICAL)

**Location**: `PKG_SECURITY.pkb` — `encrypt_ssn` / `decrypt_ssn` functions
**Evidence**: AES-256 encryption key is a string constant in the package body source code.

**Risk**: Anyone with read access to the source (developers, DBAs, version control viewers) can decrypt all SSN values. SSNs are PII protected under multiple regulations (HIPAA, state breach notification laws).

**Recommendation**: Store encryption keys in Oracle Wallet or external KMS (AWS KMS, HashiCorp Vault). Rotate keys and re-encrypt existing data.

---

### SEC-03: SQL Injection in Employee Search (CRITICAL)

**Location**: `PKG_EMPLOYEE.pkb` — `search_employees` procedure
**Evidence**: Dynamic SQL constructed via string concatenation of user-supplied search parameters without bind variables.

**Risk**: An attacker can inject arbitrary SQL through search form fields, potentially reading, modifying, or deleting any data in the HRMS schema. This is the classic OWASP #1 vulnerability.

**Recommendation**: Rewrite using `DBMS_SQL` with bind variables or native dynamic SQL with parameterized queries. Never concatenate user input into SQL strings.

---

### SEC-04: No Account Lockout (CRITICAL)

**Location**: `PKG_SECURITY.pkb` — `authenticate` function
**Evidence**: `SYSTEM_PARAMETERS` has `MAX_LOGIN_ATTEMPTS=5` but the `authenticate` function does not check or enforce it. Failed login attempts are not tracked.

**Risk**: Unlimited brute-force attempts against any user account. Combined with MD5 hashing, this makes credential compromise trivial.

**Recommendation**: Track consecutive failed login attempts per user. Lock account after `MAX_LOGIN_ATTEMPTS` for a configurable duration. Send notification on lockout.

---

### SEC-05: Cleartext Password Transmission (HIGH)

**Location**: `HRMS_LOGIN.xml` — LOGIN form
**Evidence**: Password field has `CONCEAL_DATA=TRUE` (masks UI display) but the form submits the password to `PKG_SECURITY.authenticate` as a cleartext VARCHAR2 parameter over the Forms network protocol.

**Risk**: Network sniffing between the Forms client and WebLogic server can capture passwords in transit. Oracle Forms does not use TLS for the Forms protocol by default.

**Recommendation**: Enable Oracle Forms HTTPS/SSL encryption at the WebLogic layer. Consider migrating to web-based authentication.

---

### SEC-06: Cleartext FTP Credentials (HIGH)

**Location**: `PKG_INTEGRATION.pkb` / `SYSTEM_PARAMETERS` table
**Evidence**: FTP credentials for benefits feed transfer stored in `SYSTEM_PARAMETERS` as cleartext (noted in package header comments).

**Risk**: Any user or DBA who can query `SYSTEM_PARAMETERS` can read FTP credentials. The FTP protocol itself transmits credentials in cleartext.

**Recommendation**: Migrate to SFTP/SCP. Store credentials in Oracle Wallet. Never store credentials in application tables.

---

### SEC-07: No CAPTCHA or Multi-Factor Authentication (HIGH)

**Location**: `HRMS_LOGIN.xml`
**Evidence**: Login form has username and password fields only. No CAPTCHA, no 2FA/MFA, no device fingerprinting.

**Risk**: Automated brute-force attacks, credential stuffing from leaked password databases. Especially dangerous combined with no account lockout (SEC-04).

**Recommendation**: Add CAPTCHA after 3 failed attempts. Implement TOTP-based 2FA for all users. Add IP-based rate limiting.

---

### SEC-08: Timing Attack on Authentication (HIGH)

**Location**: `PKG_SECURITY.pkb` — `authenticate` function
**Evidence**: Function returns immediately on "user not found" but performs hash comparison on "user found". The response time difference reveals whether a username exists.

**Risk**: Attackers can enumerate valid usernames by measuring response times. This reduces the attack surface from username+password to password-only for known accounts.

**Recommendation**: Perform a dummy hash comparison even when user is not found, ensuring constant-time response regardless of username validity.

---

### SEC-09: Session Timeout Uses Database Time (MEDIUM)

**Location**: `PKG_SECURITY.pkb` — `is_session_valid`
**Evidence**: Session timeout comparison uses `SYSDATE` (database server time). If the Forms client and database server have clock drift, sessions may expire prematurely or persist too long.

**Risk**: Sessions could remain valid longer than the 30-minute policy if clocks are misaligned.

**Recommendation**: Use a single authoritative time source. Log both client request time and server evaluation time for audit purposes.

---

### SEC-10: Bank Account Numbers Stored Without Encryption (MEDIUM)

**Location**: `schema/tables/02_payroll_tables.sql` — `EMPLOYEE_BANK_ACCOUNTS` table
**Evidence**: `ACCOUNT_NUMBER VARCHAR2(20)` — no encryption applied, unlike SSN which uses `SSN_ENCRYPTED RAW(256)`.

**Risk**: Bank account numbers are PII. A database breach exposes all employee bank routing and account numbers.

**Recommendation**: Encrypt at rest using `DBMS_CRYPTO` (like SSN) or Oracle TDE. Mask in all display contexts except authorized payroll users.

---

## 2. Race Conditions & Concurrency Bugs

### RACE-01: Employee Number Generation Race Condition (CRITICAL)

**Location**: `PKG_EMPLOYEE.pkb` — `generate_emp_number` function; `schema/sequences/hrms_sequences.sql` — `SEQ_EMP_NUMBER`
**Evidence**: `SEQ_EMP_NUMBER` is created with `NOCACHE`. The `generate_emp_number` function uses `MAX(EMP_NUMBER) + 1` pattern instead of the sequence directly.

**Risk**: Two simultaneous employee creations can generate the same EMP_NUMBER. The UNIQUE constraint on EMP_NUMBER will cause one INSERT to fail with `DUP_VAL_ON_INDEX`, but this is caught as a generic error — not gracefully retried.

**Impact**: With ~200 concurrent users, this can occur during mass hiring events (e.g., annual intern cohort).

**Recommendation**: Replace `MAX()+1` with `SEQ_EMP_NUMBER.NEXTVAL` formatted as `'EMP-' || LPAD(SEQ_EMP_NUMBER.NEXTVAL, 6, '0')`. Enable `CACHE 20` on the sequence.

---

### RACE-02: Leave Balance Update Without Row Locking (HIGH)

**Location**: `PKG_LEAVE.pkb` — `submit_leave_request`
**Evidence**: The procedure reads `LEAVE_BALANCES.AVAILABLE`, checks sufficiency, then updates the `PENDING` column in separate statements without `SELECT ... FOR UPDATE`.

**Risk**: Two concurrent leave requests from the same employee can both pass the balance check, overdrawing the balance. The `approve_leave_request` procedure does use `FOR UPDATE`, but the damage is done at submit time.

**Recommendation**: Add `FOR UPDATE NOWAIT` when reading the balance in `submit_leave_request`. Handle `ORA-00054` (resource busy) with a user-friendly retry message.

---

### RACE-03: Pay Period Close Without Serialization (MEDIUM)

**Location**: `PKG_PAYROLL.pkb` — `close_pay_period`
**Evidence**: Uses `SELECT ... FOR UPDATE` on `PAY_PERIODS` but the `create_payroll_run` procedure does not check if the period is already being processed.

**Risk**: Two payroll administrators could initiate runs for the same period simultaneously.

**Recommendation**: Add period status check with `FOR UPDATE` at the start of `create_payroll_run`.

---

## 3. Data Integrity & Logic Bugs

### BUG-01: Leave Carryover Double-Expiry (HIGH)

**Location**: `PKG_LEAVE.pkb` — `expire_carryover` procedure (lines 610-623)
**Evidence**: Code comment: "BUG: If this batch is run twice on the same day, it will double-subtract the carryover balance."

**Risk**: Running the expiry batch job twice (e.g., scheduler retry after timeout) permanently loses employee leave days. The subtraction is applied to `LEAVE_BALANCES.ADJUSTMENT` without checking if already processed.

**Recommendation**: Add an `EXPIRED_FLAG` or `EXPIRY_PROCESSED_DATE` to `LEAVE_BALANCES`. Check before subtracting. Make the operation idempotent.

---

### BUG-02: Half-Day Leave Overlap Detection Bug (HIGH)

**Location**: `PKG_LEAVE.pkb` — `check_leave_overlap` function
**Evidence**: Code comment in `PKG_LEAVE.pks` header: "half-day overlap detection bug." The overlap check does not distinguish between AM and PM half-days, so an AM half-day request blocks a PM request for the same date.

**Risk**: Employees cannot take two half-days (AM + PM) on the same date from different leave types, which is a valid business scenario.

**Recommendation**: Add `HALF_DAY_PERIOD` (AM/PM) to the overlap detection WHERE clause. Only flag overlap when periods actually conflict.

---

### BUG-03: Overtime Does Not Account for Holidays (HIGH)

**Location**: `PKG_PAYROLL.pkb` — payroll calculation logic
**Evidence**: Code comment in `PKG_PAYROLL.pks` header: "overtime doesn't account for holidays."

**Risk**: Employees working on holidays are paid regular overtime rate instead of holiday overtime rate (typically 2x instead of 1.5x). This is a wage compliance issue that could trigger Department of Labor complaints.

**Recommendation**: Cross-reference hours worked against `HOLIDAYS` table. Apply holiday premium rate for holiday hours.

---

### BUG-04: YTD Earnings Reset Bug for Mid-Year Hires (MEDIUM)

**Location**: `PKG_PAYROLL.pkb` — `get_ytd_earnings` function (lines 802-819)
**Evidence**: Code comment in `PKG_PAYROLL.pks` header: "YTD reset bug for mid-year hires." The function aggregates from January 1 regardless of hire date. For employees hired mid-year who had prior employment, the FICA wage base check may incorrectly cross the threshold.

**Risk**: Over-withholding or under-withholding of Social Security tax for mid-year hires.

**Recommendation**: YTD should start from hire date for new hires, or accept prior employer W-2 amounts for wage base calculation.

---

### BUG-05: Holiday Observed Date Not Used (MEDIUM)

**Location**: `PKG_LEAVE.pkb` — business day calculation
**Evidence**: Code comment in `PKG_LEAVE.pks` header: "holiday observed date bug." Holidays falling on weekends (e.g., July 4 on Saturday) use the calendar date, not the observed date (Friday).

**Risk**: Leave calculations include observed holidays as work days, resulting in incorrect business day counts.

**Recommendation**: Add `OBSERVED_DATE` column to `HOLIDAYS` table. Use `COALESCE(OBSERVED_DATE, HOLIDAY_DATE)` in business day calculations.

---

### BUG-06: INSTEAD_OF_DELETE Trigger Mismatch with Forms (MEDIUM)

**Location**: `plsql/triggers/trg_employees.sql` — `TRG_EMP_INSTEAD_OF_DELETE`
**Evidence**: The trigger converts DELETE to a soft delete (ACTIVE_FLAG='N'), but Oracle Forms expects DELETE to physically remove the record from the block. The workaround is to manually set ACTIVE_FLAG='N' then call `CLEAR_RECORD`.

**Risk**: Users who press the Delete button see confusing behavior — the record appears unchanged. Forms developers must know about the workaround.

**Recommendation**: Document the pattern. Add an ON-DELETE form trigger that performs the soft delete via `PKG_EMPLOYEE.terminate_employee` and then calls `CLEAR_RECORD`.

---

## 4. Performance Anti-Patterns

### PERF-01: Recursive Org Chart Query Timeout (HIGH)

**Location**: `PKG_EMPLOYEE.pkb` — `get_org_chart` function (lines 822-840)
**Evidence**: Uses `CONNECT BY PRIOR EMP_ID = MANAGER_EMP_ID` with `START WITH MANAGER_EMP_ID IS NULL`. Code comment notes timeout for >500 employees.

**Risk**: With 25 seed employees the query is fast, but production data with 500+ employees and deep hierarchies will cause significant performance degradation or ORA-01788 (CONNECT BY loop) if there's a circular manager reference.

**Recommendation**: Add `NOCYCLE` clause. Consider materialized view with periodic refresh for org chart data. Add `LEVEL` limit to prevent runaway recursion.

---

### PERF-02: Payroll Cursor Loop (Row-by-Row Processing) (HIGH)

**Location**: `PKG_PAYROLL.pkb` — `calculate_payroll` procedure
**Evidence**: Processes each employee individually in a `FOR rec IN (SELECT ...)` cursor loop, calling `calculate_employee_pay` per employee.

**Risk**: For 200+ employees, this generates thousands of individual INSERT statements into `PAYROLL_DETAILS`. Each iteration also performs multiple SELECT lookups (tax info, pay elements). This is classic "slow-by-slow" processing.

**Recommendation**: Refactor to bulk operations using `FORALL` and `BULK COLLECT`. Pre-load tax info and pay elements into PL/SQL associative arrays before the loop.

---

### PERF-03: Denormalized Reporting Tables Stale During Business Hours (MEDIUM)

**Location**: `PKG_REPORTING.pkb` — `refresh_reporting_tables` (lines 196-204)
**Evidence**: Procedure is a placeholder ("-- TODO: implement actual refresh"). Reports query denormalized `RPT_*` tables that are refreshed nightly.

**Risk**: Managers viewing reports during business hours see data that is up to 24 hours stale. The refresh procedure itself is not implemented.

**Recommendation**: Implement the refresh procedure. Consider materialized views with `REFRESH ON COMMIT` or `FAST REFRESH` for near-real-time data.

---

### PERF-04: UTL_FILE for Integration (No Streaming) (MEDIUM)

**Location**: `PKG_INTEGRATION.pkb` — all integration procedures
**Evidence**: All file operations use `UTL_FILE.PUT_LINE` in a cursor loop. The benefits feed generates one line per employee-dependent combination, potentially thousands of lines.

**Risk**: Large datasets cause excessive PGA memory usage and long-running sessions. `UTL_FILE` has a 32KB line buffer limit. No streaming or batching.

**Recommendation**: For modernization: Replace with REST API integration. Short-term: Use `UTL_FILE` with periodic `FFLUSH` and implement batch chunking.

---

### PERF-05: No Indexes Documented for Query Patterns (MEDIUM)

**Location**: All `schema/tables/*.sql` files
**Evidence**: Table DDL defines primary keys and unique constraints (which create indexes), but no additional indexes are defined for known query patterns (e.g., `EMPLOYEES.DEPT_ID`, `EMPLOYEES.MANAGER_EMP_ID`, `LEAVE_REQUESTS.STATUS`, `PAYROLL_DETAILS.RUN_ID`).

**Risk**: Full table scans on frequently queried columns, especially as data grows. The `LEAVE_REQUESTS` table filtered by `STATUS='PENDING'` and `APPROVER_EMP_ID` is a common query from every manager's leave approval screen.

**Recommendation**: Create indexes on foreign key columns and commonly filtered status columns. Use `EXPLAIN PLAN` to verify.

---

## 5. Validation Drift

### DRIFT-01: Email Validation — Client vs. Server (HIGH)

**Location**:
- Client: `HRMS_VALIDATION_LIB.pll.sql` — `validate_email` (line 7: "BUG: rejects valid subdomains")
- Server: `PKG_COMMON.pkb` — `is_valid_email`

**Evidence**: The PLL client-side validation uses a regex that rejects emails with subdomains (e.g., `user@mail.company.com`), while the server-side `PKG_COMMON.is_valid_email` accepts them.

**Impact**: Users receive a client-side error for valid email addresses. They cannot save the record even though the server would accept it. Workaround: users disable Forms client-side validation (not officially supported).

**Recommendation**: Synchronize email regex between client and server. Consider removing client-side regex and relying solely on server-side validation via a `WHEN-VALIDATE-ITEM` trigger that calls `PKG_VALIDATION.validate_email_format`.

---

### DRIFT-02: Salary Range Validation — Client vs. Server (MEDIUM)

**Location**:
- Client: `HRMS_VALIDATION_LIB.pll.sql` — `validate_salary_range` (uses hard-coded min/max)
- Server: `PKG_VALIDATION.pkb` — `validate_salary_for_grade` (reads `JOB_GRADES` table)

**Evidence**: Client validation uses static salary bounds, while server validation dynamically reads from `JOB_GRADES`. If grade salary ranges are updated, the client validation is stale until the PLL is recompiled and redeployed.

**Recommendation**: Remove hard-coded ranges from PLL. Call server-side validation via `WHEN-VALIDATE-ITEM` trigger.

---

### DRIFT-03: Audit Logging Duplication — Triggers vs. Packages (MEDIUM)

**Location**:
- Triggers: `TRG_SALARY_AUDIT`, `TRG_LEAVE_REQUEST_AUDIT`, `TRG_DEPARTMENT_AUDIT`
- Packages: `PKG_AUDIT.log_action` (called from all business packages)

**Evidence**: Both triggers and packages write to `AUDIT_LOG` for the same DML operations, resulting in duplicate audit entries. The trigger-based audit captures raw column values; the package-based audit captures the business operation context.

**Risk**: Doubled audit log volume. Confusion during audit reviews about which record is authoritative.

**Recommendation**: Choose one audit strategy. Prefer package-based audit (richer context). Remove or disable trigger-based audit for tables already covered by package calls.

---

## 6. Hard-Coded Configuration

### CONFIG-01: 2024 Federal Tax Brackets (CRITICAL)

**Location**: `PKG_PAYROLL.pkb` — `calculate_federal_tax` (lines 643-677)
**Evidence**: Tax brackets for 2024 are hard-coded as PL/SQL constants:
- 10% up to $11,600 (SINGLE) / $23,200 (MARRIED_JOINT)
- 12% up to $47,150 / $94,300
- 22% up to $100,525 / $201,050
- 24% up to $191,950 / $383,900
- 32% up to $243,725 / $487,450
- 35% up to $609,350 / $731,200
- 37% above

**Risk**: Tax brackets change annually. The IRS publishes new brackets each October for the following year. Without a code change and recompilation, employees will be taxed using stale 2024 brackets in 2025+. **This is a payroll compliance violation.**

**Note**: A `TAX_BRACKETS` table exists in the schema but the code does not use it.

**Recommendation**: Refactor `calculate_federal_tax` to read from the `TAX_BRACKETS` table. Populate the table with current year brackets. Add a batch job to alert when brackets are missing for the upcoming year.

---

### CONFIG-02: Social Security Wage Base Hard-Coded (HIGH)

**Location**: `PKG_PAYROLL.pkb` — `calculate_fica` (line ~725)
**Evidence**: `c_ss_wage_base_2024 CONSTANT NUMBER := 168600;`

**Risk**: The Social Security wage base changes annually ($168,600 for 2024, $176,100 for 2025). Hard-coded value causes incorrect FICA withholding after year change.

**Recommendation**: Move to `SYSTEM_PARAMETERS` or `TAX_BRACKETS` table. Validate at year start.

---

### CONFIG-03: Medicare Additional Tax Threshold Hard-Coded (MEDIUM)

**Location**: `PKG_PAYROLL.pkb` — `calculate_medicare` (line ~742)
**Evidence**: `c_medicare_addl_threshold CONSTANT NUMBER := 200000;`

**Risk**: The $200,000 threshold has been stable since 2013, but relying on hard-coded values is fragile if legislation changes.

**Recommendation**: Move to configurable parameter.

---

### CONFIG-04: State Tax Rates Hard-Coded (MEDIUM)

**Location**: `PKG_PAYROLL.pkb` — `calculate_state_tax` (lines 693-718)
**Evidence**: Flat rates hard-coded for 8 states only: CA 7.25%, NY 6.85%, TX/FL/WA 0%, IL 4.95%, PA 3.07%, OH 4%, NJ 6.37%, MA 5%.

**Risk**: Employees in unlisted states get 0% state tax (silent bug). State rates change periodically. Several listed rates are simplified (CA uses progressive brackets in reality).

**Recommendation**: Populate `TAX_BRACKETS` with state data. Add all 50 states. Handle progressive state brackets where applicable.

---

### CONFIG-05: Fiscal Year Start Hard-Coded (LOW)

**Location**: `PKG_COMMON.pkb` — `get_fiscal_year`, `get_fiscal_quarter`; `PKG_REPORTING.pkb`
**Evidence**: Fiscal year start month hard-coded as October (month 10).

**Risk**: If the organization changes its fiscal year, code changes are required across multiple packages.

**Recommendation**: Move to `SYSTEM_PARAMETERS` as `FISCAL_YEAR_START_MONTH`.

---

### CONFIG-06: SMTP Server Hard-Coded (LOW)

**Location**: `PKG_NOTIFICATION.pkb`
**Evidence**: SMTP server hostname is hard-coded as a package constant: `smtp.internal.company.com`.

**Risk**: Server changes require code recompilation.

**Recommendation**: Already in `SYSTEM_PARAMETERS` as `SMTP_SERVER` — ensure the code reads from the parameter instead of the constant.

---

## 7. Architecture & Maintainability

### ARCH-01: Circular Dependency — PKG_EMPLOYEE ↔ PKG_PAYROLL (HIGH)

**Location**: `PKG_EMPLOYEE.pks` (line 12), `PKG_PAYROLL.pks` (line 6)
**Evidence**: Bidirectional compile-time dependency. See DEPENDENCY_MAP.md Section 9 for full analysis.

**Impact**: Invalidation cascade — if either package spec is recompiled, the other becomes `INVALID`, potentially causing runtime `ORA-04068` errors for active sessions.

**Recommendation**: Extract salary management into a separate `PKG_SALARY` to break the cycle.

---

### ARCH-02: Package Size Exceeds Maintainability Threshold (MEDIUM)

**Location**: Multiple packages

| Package | Body Lines | Threshold (500) |
|---------|-----------|----------------|
| PKG_EMPLOYEE | 967 | Exceeds by 93% |
| PKG_PAYROLL | 898 | Exceeds by 80% |
| PKG_LEAVE | 674 | Exceeds by 35% |

**Risk**: Large packages are difficult to review, test, and modify. Merge conflicts increase with team size.

**Recommendation**: Split into domain-specific sub-packages (e.g., `PKG_EMP_LIFECYCLE`, `PKG_EMP_SEARCH`, `PKG_PAYROLL_CALC`, `PKG_PAYROLL_TAX`).

---

### ARCH-03: No Unit Test Framework (MEDIUM)

**Location**: Entire codebase
**Evidence**: No test files, no utPLSQL or similar framework configuration, no test data scripts beyond seed data.

**Risk**: No regression safety net. Any code change could introduce bugs that are only caught in production.

**Recommendation**: Adopt utPLSQL. Start with tests for critical calculations: `calculate_federal_tax`, `calculate_fica`, `submit_leave_request`, `generate_emp_number`.

---

### ARCH-04: Mixed Naming Conventions (LOW)

**Location**: Across all packages
**Evidence**: Variables use `v_` prefix, parameters use `p_`, constants use `c_` — mostly consistent. But some packages use `l_` for locals. Table alias conventions are inconsistent (`e` vs `emp` vs `EMP`).

**Recommendation**: Document and enforce naming standards in a `CODING_STANDARDS.md`.

---

### ARCH-05: VARCHAR2(4000) Catch-All Columns (LOW)

**Location**: Multiple tables
**Evidence**: `ERROR_MESSAGE VARCHAR2(4000)`, `COMMENTS VARCHAR2(4000)`, `BODY CLOB` — several columns use maximum VARCHAR2 size without business justification.

**Risk**: Memory overhead in PGA when processing large result sets.

**Recommendation**: Right-size columns based on actual data requirements.

---

## 8. Incomplete / Dead Code

### DEAD-01: import_time_attendance — Stub Implementation (MEDIUM)

**Location**: `PKG_INTEGRATION.pkb` — `import_time_attendance` (lines 153-194)
**Evidence**: Reads file lines and increments a counter but has `-- TODO: Implement actual parsing and database update`. No data is actually written to any table.

**Impact**: The time & attendance integration is non-functional. Any scheduled call silently succeeds without processing data.

---

### DEAD-02: sync_org_structure — Placeholder Only (LOW)

**Location**: `PKG_INTEGRATION.pkb` — `sync_org_structure` (lines 196-203)
**Evidence**: Logs "Org structure sync completed" without performing any actual sync with LDAP/AD.

---

### DEAD-03: refresh_reporting_tables — Not Implemented (MEDIUM)

**Location**: `PKG_REPORTING.pkb` — `refresh_reporting_tables` (lines 196-204)
**Evidence**: Logs a message but doesn't refresh any tables. The `RPT_*` denormalized tables referenced by reports are never actually populated.

---

### DEAD-04: terminate_employee — Missing Follow-Up Actions (MEDIUM)

**Location**: `PKG_EMPLOYEE.pkb` — `terminate_employee` (lines 647-745)
**Evidence**: Three TODO comments for missing termination steps:
1. "TODO: Trigger COBRA notification" — legally required within 14 days
2. "TODO: Revoke system access" — security risk if not performed
3. "TODO: Schedule final pay calculation" — legal requirement in most states

---

## 9. Findings Summary Table

| ID | Category | Severity | Title | Location |
|----|----------|----------|-------|----------|
| SEC-01 | Security | **CRITICAL** | MD5 Password Hashing | PKG_SECURITY.pkb |
| SEC-02 | Security | **CRITICAL** | Hard-Coded Encryption Key | PKG_SECURITY.pkb |
| SEC-03 | Security | **CRITICAL** | SQL Injection in Employee Search | PKG_EMPLOYEE.pkb |
| SEC-04 | Security | **CRITICAL** | No Account Lockout | PKG_SECURITY.pkb |
| SEC-05 | Security | HIGH | Cleartext Password Transmission | HRMS_LOGIN.xml |
| SEC-06 | Security | HIGH | Cleartext FTP Credentials | PKG_INTEGRATION / SYSTEM_PARAMETERS |
| SEC-07 | Security | HIGH | No CAPTCHA or MFA | HRMS_LOGIN.xml |
| SEC-08 | Security | HIGH | Timing Attack on Authentication | PKG_SECURITY.pkb |
| SEC-09 | Security | MEDIUM | Session Timeout Uses DB Time | PKG_SECURITY.pkb |
| SEC-10 | Security | MEDIUM | Bank Accounts Not Encrypted | EMPLOYEE_BANK_ACCOUNTS |
| RACE-01 | Race Condition | **CRITICAL** | Employee Number Race Condition | PKG_EMPLOYEE.pkb / SEQ_EMP_NUMBER |
| RACE-02 | Race Condition | HIGH | Leave Balance No Row Lock | PKG_LEAVE.pkb |
| RACE-03 | Race Condition | MEDIUM | Pay Period Close No Serialization | PKG_PAYROLL.pkb |
| BUG-01 | Data Integrity | HIGH | Carryover Double-Expiry | PKG_LEAVE.pkb |
| BUG-02 | Data Integrity | HIGH | Half-Day Overlap Detection | PKG_LEAVE.pkb |
| BUG-03 | Data Integrity | HIGH | Overtime No Holiday Premium | PKG_PAYROLL.pkb |
| BUG-04 | Data Integrity | MEDIUM | YTD Reset for Mid-Year Hires | PKG_PAYROLL.pkb |
| BUG-05 | Data Integrity | MEDIUM | Holiday Observed Date Not Used | PKG_LEAVE.pkb |
| BUG-06 | Data Integrity | MEDIUM | INSTEAD_OF_DELETE Mismatch | trg_employees.sql |
| PERF-01 | Performance | HIGH | Org Chart Query Timeout | PKG_EMPLOYEE.pkb |
| PERF-02 | Performance | HIGH | Payroll Row-by-Row Processing | PKG_PAYROLL.pkb |
| PERF-03 | Performance | MEDIUM | Stale Reporting Tables | PKG_REPORTING.pkb |
| PERF-04 | Performance | MEDIUM | UTL_FILE No Streaming | PKG_INTEGRATION.pkb |
| PERF-05 | Performance | MEDIUM | Missing Indexes | schema/tables/*.sql |
| DRIFT-01 | Validation Drift | HIGH | Email Regex Client ≠ Server | HRMS_VALIDATION_LIB / PKG_COMMON |
| DRIFT-02 | Validation Drift | MEDIUM | Salary Range Client Static | HRMS_VALIDATION_LIB / PKG_VALIDATION |
| DRIFT-03 | Validation Drift | MEDIUM | Duplicate Audit (Triggers + Packages) | trg_audit.sql / PKG_AUDIT |
| CONFIG-01 | Hard-Coded Config | **CRITICAL** | 2024 Federal Tax Brackets | PKG_PAYROLL.pkb |
| CONFIG-02 | Hard-Coded Config | HIGH | SS Wage Base Hard-Coded | PKG_PAYROLL.pkb |
| CONFIG-03 | Hard-Coded Config | MEDIUM | Medicare Threshold Hard-Coded | PKG_PAYROLL.pkb |
| CONFIG-04 | Hard-Coded Config | MEDIUM | State Tax Rates Incomplete | PKG_PAYROLL.pkb |
| CONFIG-05 | Hard-Coded Config | LOW | Fiscal Year Start Hard-Coded | PKG_COMMON.pkb |
| CONFIG-06 | Hard-Coded Config | LOW | SMTP Server Hard-Coded | PKG_NOTIFICATION.pkb |
| ARCH-01 | Architecture | HIGH | Circular Dependency | PKG_EMPLOYEE ↔ PKG_PAYROLL |
| ARCH-02 | Architecture | MEDIUM | Oversized Packages | PKG_EMPLOYEE, PKG_PAYROLL |
| ARCH-03 | Architecture | MEDIUM | No Unit Tests | Entire codebase |
| ARCH-04 | Architecture | LOW | Mixed Naming Conventions | All packages |
| ARCH-05 | Architecture | LOW | VARCHAR2(4000) Catch-All | Multiple tables |
| DEAD-01 | Dead Code | MEDIUM | Time Import Not Implemented | PKG_INTEGRATION.pkb |
| DEAD-02 | Dead Code | LOW | Org Sync Placeholder | PKG_INTEGRATION.pkb |
| DEAD-03 | Dead Code | MEDIUM | Reporting Refresh Not Implemented | PKG_REPORTING.pkb |
| DEAD-04 | Dead Code | MEDIUM | Termination Missing Steps | PKG_EMPLOYEE.pkb |

---

## 10. Recommended Remediation Priority

### Phase 1: Immediate / Pre-Production (Critical + Security High)

| Priority | Finding | Effort | Impact |
|----------|---------|--------|--------|
| 1 | SEC-03: SQL Injection | 2-3 days | Prevents data breach |
| 2 | SEC-01: MD5 Hashing | 3-5 days | Prevents credential theft |
| 3 | SEC-02: Hard-Coded Key | 2-3 days | Protects SSN data |
| 4 | SEC-04: Account Lockout | 1-2 days | Prevents brute force |
| 5 | CONFIG-01: Tax Brackets | 3-5 days | Payroll compliance |
| 6 | RACE-01: EMP_NUMBER Race | 1 day | Data integrity |
| 7 | SEC-05: Cleartext Password | 1 day (config) | Network security |

### Phase 2: Current Release Cycle (High)

| Priority | Finding | Effort | Impact |
|----------|---------|--------|--------|
| 8 | BUG-01: Carryover Double-Expiry | 1-2 days | Leave balance accuracy |
| 9 | BUG-02: Half-Day Overlap | 1 day | User experience |
| 10 | BUG-03: Holiday Overtime | 2-3 days | Wage compliance |
| 11 | DRIFT-01: Email Validation | 1 day | User experience |
| 12 | CONFIG-02: SS Wage Base | 1 day | Payroll accuracy |
| 13 | RACE-02: Leave Balance Lock | 1 day | Data integrity |
| 14 | PERF-01: Org Chart Timeout | 1-2 days | Performance |
| 15 | PERF-02: Payroll Bulk | 3-5 days | Performance |
| 16 | ARCH-01: Circular Dependency | 3-5 days | Maintainability |
| 17 | SEC-06: FTP Credentials | 2-3 days | Security |
| 18 | SEC-07: CAPTCHA/MFA | 5-10 days | Security |
| 19 | SEC-08: Timing Attack | 1 day | Security |

### Phase 3: Next Sprint (Medium)

All remaining MEDIUM findings (CONFIG-03/04, BUG-04/05/06, PERF-03/04/05, DRIFT-02/03, ARCH-02/03, DEAD-01/03/04, SEC-09/10, RACE-03).

### Phase 4: Backlog (Low)

CONFIG-05/06, ARCH-04/05, DEAD-02.

---

### Estimated Total Remediation Effort

| Phase | Findings | Est. Days |
|-------|---------|----------|
| Phase 1 (Immediate) | 7 | 13-19 |
| Phase 2 (Current Cycle) | 12 | 20-32 |
| Phase 3 (Next Sprint) | 18 | 15-25 |
| Phase 4 (Backlog) | 5 | 3-5 |
| **Total** | **42** | **51-81 person-days** |
