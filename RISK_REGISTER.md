# Oracle Forms HRMS — Migration Risk Register

> Top 10 Forms-specific migration risks with likelihood, impact, and mitigation strategies.  
> Source: `ts-plsql-oracle-forms-legacy-codebase`

---

## Risk Severity Matrix

| | **Low Impact** | **Medium Impact** | **High Impact** | **Critical Impact** |
|---|---|---|---|---|
| **Very Likely** | Medium | High | Critical | Critical |
| **Likely** | Low | Medium | High | Critical |
| **Possible** | Low | Medium | Medium | High |
| **Unlikely** | Low | Low | Medium | Medium |

---

## Risk Summary

| ID | Risk | Likelihood | Impact | Severity | Phase Affected |
|----|------|-----------|--------|----------|----------------|
| R-01 | Validation drift between client PLL and server PL/SQL | Very Likely | High | **Critical** | Phase 2a, 2b |
| R-02 | Circular dependency (PKG_EMPLOYEE ↔ PKG_PAYROLL) blocks incremental migration | Very Likely | High | **Critical** | Phase 2a, 3a |
| R-03 | Payroll calculation discrepancies during parallel-run validation | Likely | Critical | **Critical** | Phase 3a |
| R-04 | Forms session/global variable state lost in stateless architecture | Likely | High | **High** | Phase 2a, 2b, 3b |
| R-05 | LOV dynamic WHERE clauses produce different results in REST endpoints | Likely | Medium | **Medium** | Phase 2a |
| R-06 | Master-detail cascading delete/update behavior divergence | Possible | High | **Medium** | Phase 2a, 3a |
| R-07 | Form trigger execution order not replicated in Java lifecycle | Likely | High | **High** | Phase 2a, 2b, 3a |
| R-08 | Hard-coded configuration values missed during extraction | Very Likely | Medium | **High** | Phase 3a, 3c |
| R-09 | Security vulnerabilities carried forward or incompletely remediated | Possible | Critical | **High** | Phase 1 |
| R-10 | Data migration and cutover integrity failures | Possible | Critical | **High** | Phase 4 |

---

## Detailed Risk Analysis

### R-01: Validation Drift Between Client PLL and Server PL/SQL

**Severity: CRITICAL**

#### Description

The legacy system implements validation in two independent layers that have diverged over time:

- **Client-side:** `HRMS_VALIDATION_LIB.pll` — runs in the Oracle Forms applet (lines 1–136)
- **Server-side:** `PKG_VALIDATION` — runs in the database (48 lines spec)

The PLL library header explicitly documents this drift:

> *"KNOWN ISSUE: Some validations in this library have drifted from the server-side PKG_VALIDATION. The client-side allows values that the server rejects and vice versa."*

**Specific drift examples from the codebase:**

| Validation | PLL (Client) | PKG_VALIDATION (Server) | Conflict |
|---|---|---|---|
| Email format | Allows `user@domain` (no TLD check) | Requires `user@domain.tld` (TLD mandatory) | Client accepts invalid emails that server rejects on COMMIT |
| Phone format | Accepts 10–11 digits | Accepts 10 digits only | 11-digit numbers pass client but fail server |
| Salary range | Checks `MIN_SALARY <= val <= MAX_SALARY` from cached grade | Checks against live DB with ±10% tolerance band | Different thresholds |
| Date range | Allows hire date up to 180 days in future | `TRG_EMP_BEFORE_INSERT` limits to 90 days | Trigger rejects what form allows |

#### Likelihood: Very Likely

This is a confirmed, documented issue. Every field with dual validation is affected.

#### Impact: High

- Users will experience validation failures at unexpected points in the new system
- Data that passed the old system's client-side check may be rejected by the new unified validation
- Existing database records may violate the new stricter rules

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Audit all validation rules** — Create a spreadsheet mapping every PLL validation to its PKG_VALIDATION counterpart | Migration Lead | Phase 0 |
| **Choose canonical rule** — For each divergence, decide which rule is correct (usually the stricter one) | Business Analyst + Migration Lead | Phase 0 |
| **Single validation layer** — Implement all validation in the Java service layer (Jakarta Bean Validation annotations + custom validators); no separate client-side rules | Backend Dev | Phase 2a |
| **Data remediation script** — Identify existing records that violate the new canonical rules; generate a report for business decision on correction | DBA | Phase 2a |
| **Gradual enforcement** — Initially log violations without rejecting; switch to strict mode after data cleanup | Backend Dev | Phase 2a → 3a |

---

### R-02: Circular Dependency (PKG_EMPLOYEE ↔ PKG_PAYROLL) Blocks Incremental Migration

**Severity: CRITICAL**

#### Description

`PKG_EMPLOYEE` and `PKG_PAYROLL` have a bidirectional dependency:

- `PKG_EMPLOYEE.create_employee()` calls `PKG_PAYROLL.create_salary_record()` (PKG_EMPLOYEE.pkb lines 273–281):
  > *"CIRCULAR DEPENDENCY: PKG_EMPLOYEE calls PKG_PAYROLL here. PKG_PAYROLL also references PKG_EMPLOYEE. This circular dependency is a known tech debt."*

- `PKG_PAYROLL.calculate_employee_pay()` calls `PKG_EMPLOYEE.get_employee()` to retrieve employee details

This means neither package can be migrated to Java independently without maintaining a bridge to the legacy PL/SQL for the other package.

#### Likelihood: Very Likely

This is a structural constraint in the existing codebase. Any migration plan must address it.

#### Impact: High

- Cannot migrate Employee module without also handling salary creation
- Cannot migrate Payroll module without also handling employee lookups
- Coexistence period requires a shim/adapter layer adding complexity and potential failure points

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Dependency inversion** — Introduce a `SalaryService` interface that both `EmployeeService` and `PayrollService` depend on (see MODULE_ORDERING.md Section 3) | Architect | Phase 2a |
| **Temporary shim** — Implement `LegacySalaryServiceImpl` that calls `PKG_PAYROLL` via JDBC for salary operations during the Employee migration phase | Backend Dev | Phase 2a |
| **Replace shim** — When Payroll migrates in Phase 3a, replace the shim with `PayrollSalaryServiceImpl` | Backend Dev | Phase 3a |
| **Integration tests** — Create tests that validate the Employee → Salary flow end-to-end with both shim and real implementations | QA | Phase 2a, 3a |

---

### R-03: Payroll Calculation Discrepancies During Parallel-Run Validation

**Severity: CRITICAL**

#### Description

The payroll module has multiple embedded calculation issues that make bit-for-bit comparison between legacy and new systems difficult:

1. **Hard-coded 2024 tax brackets** (PKG_PAYROLL.pkb lines 6–14):
   ```
   c_ss_wage_base_2024   CONSTANT NUMBER := 168600;
   c_ss_rate             CONSTANT NUMBER := 0.062;
   c_medicare_rate       CONSTANT NUMBER := 0.0145;
   c_standard_deduction_single CONSTANT NUMBER := 14600;
   c_standard_deduction_married CONSTANT NUMBER := 29200;
   ```
   The new system will use database-driven configuration. If legacy constants aren't perfectly replicated for the comparison year, every calculation will differ.

2. **Partial commits during calculation** (PKG_PAYROLL.pkb lines 322–326): Legacy commits every 50 employees. If a failure occurs mid-batch, the old system has a partially calculated payroll. The new system (with proper transactions) would either fully succeed or fully fail — making comparison unreliable.

3. **Rounding differences**: PL/SQL `NUMBER` type vs. Java `BigDecimal` may produce different results at boundary cases depending on rounding mode.

4. **Overtime calculation ignores holidays**: Legacy bug means overtime is calculated on holidays. The new system should fix this, but then parallel-run comparisons will intentionally differ.

#### Likelihood: Likely

Exact numeric agreement across two different platforms with different arithmetic implementations is inherently difficult.

#### Impact: Critical

Payroll errors directly affect employee compensation, tax filings, and regulatory compliance. Even a $0.01 discrepancy per employee across 200 employees accumulates.

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Replicate constants exactly** — For parallel-run period, configure the new system with identical 2024 constants; switch to DB-driven values only after cutover | Backend Dev | Phase 3a |
| **Match rounding mode** — Use `BigDecimal.ROUND_HALF_UP` to match Oracle NUMBER arithmetic | Backend Dev | Phase 3a |
| **Document known differences** — Create an "expected differences" register for intentional bug fixes (overtime/holidays) so parallel-run comparisons can exclude them | Migration Lead | Phase 3a |
| **Tolerance-based comparison** — Allow $0.01 tolerance per employee per pay element in automated reconciliation | QA | Phase 3a |
| **Phased cutover** — Run one department on new system first; expand after 2 successful pay periods | Project Manager | Phase 3a |
| **Finance sign-off** — Require CFO/Payroll Manager approval before each cutover expansion | Project Manager | Phase 3a |

---

### R-04: Forms Session/Global Variable State Lost in Stateless Architecture

**Severity: HIGH**

#### Description

Oracle Forms maintains state via global variables (`:GLOBAL.xxx`), package-level variables, and the Forms Runtime session. The legacy HRMS uses these extensively:

- **`PKG_EMPLOYEE` package variables** (PKG_EMPLOYEE.pkb lines 13–17):
  ```
  g_current_user     VARCHAR2(100);
  g_current_emp_id   NUMBER;
  g_debug_mode       BOOLEAN := FALSE;
  ```

- **Forms globals** used in `HRMS_COMMON_LIB.pll`:
  - `:GLOBAL.user_id` — current user ID
  - `:GLOBAL.session_id` — current Forms session
  - `:GLOBAL.emp_id` — current employee being viewed

- **PKG_SECURITY session state**: The `USER_SESSIONS` table and `is_session_valid()` function rely on DB-side session management with 30-minute timeout.

In a stateless Spring Boot + React architecture:
- No server-side session (JWT is stateless)
- No package variables persisting between requests
- No global variables shared across forms/modules

#### Likelihood: Likely

Every Forms application relies on some form of session state. The HRMS uses it moderately but in critical paths (auth, audit, employee context).

#### Impact: High

- User context (`g_current_user`, `g_current_emp_id`) must be passed per-request
- Multi-form workflows that depend on shared globals will break
- Audit logging that reads package variables will miss context

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **JWT claims for user context** — Embed `userId`, `empId`, `roles` in JWT token; extract via Spring Security `@AuthenticationPrincipal` | Backend Dev | Phase 1 |
| **React Context for client state** — Replace `:GLOBAL.*` with React Context providers (`AuthContext`, `EmployeeContext`) | Frontend Dev | Phase 2a |
| **Audit interceptor** — Use Spring AOP `@Before` advice to populate audit fields from JWT before every service call (replacing `g_current_user` reads) | Backend Dev | Phase 0 |
| **Inventory all globals** — Catalog every `:GLOBAL.xxx` and package variable usage; map each to its JWT claim, React context, or request parameter equivalent | Migration Lead | Phase 0 |

---

### R-05: LOV Dynamic WHERE Clauses Produce Different Results in REST Endpoints

**Severity: MEDIUM**

#### Description

Oracle Forms LOVs (List of Values) support dynamic WHERE clauses that change at runtime based on form context. The HRMS uses several LOVs with context-dependent filtering:

- **LOV_JOB_TITLES**: Filtered by `GRADE_ID` matching the employee's current job grade range
- **LOV_MANAGERS**: Filtered by `DEPT_ID` to show only managers in the same department hierarchy
- **LOV_DEPARTMENTS**: May be filtered by location or business unit based on user's access level

In Forms, the developer sets `SET_BLOCK_PROPERTY(..., DEFAULT_WHERE, ...)` or uses record group queries with bind variables (`:BLOCK.ITEM`). In the React/REST architecture, these dynamic filters must be passed as query parameters.

#### Likelihood: Likely

All LOVs with dynamic WHERE clauses require refactoring.

#### Impact: Medium

- LOVs may show too many or too few options if filtering is incomplete
- Users may select invalid combinations (e.g., job title not valid for the grade)
- Cascading LOV dependencies (department → location → manager) are complex to replicate

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Catalog LOV queries** — Extract the SQL for all 8 LOVs and their dynamic WHERE clauses from the Forms XML | Migration Lead | Phase 0 |
| **Parameterized REST endpoints** — Implement LOV endpoints with optional filter parameters: `GET /api/departments?locationCode=X&active=true` | Backend Dev | Phase 2a |
| **Cascading dependency rules** — Document which LOVs depend on other LOV selections and implement cascading invalidation in React (`useEffect` on dependency change triggers re-fetch) | Frontend Dev | Phase 2a |
| **Acceptance tests** — Create test scenarios for each LOV with various filter combinations | QA | Phase 2a |

---

### R-06: Master-Detail Cascading Delete/Update Behavior Divergence

**Severity: MEDIUM**

#### Description

Oracle Forms master-detail relationships have specific cascading behaviors configured in the relation properties:

- **HRMS_EMPLOYEE**: `EMP_SALARY_REL` — `DeleteRecordBehavior="Cascading"` means deleting an employee cascades to salary records
- **HRMS_PAYROLL**: `PERIOD_RUN_REL` — cascading from pay period to payroll runs
- **HRMS_PERFORMANCE**: `CYCLE_REVIEW_REL`, `REVIEW_GOAL_REL` — two levels of cascading

However, the database triggers (`TRG_EMP_INSTEAD_OF_DELETE`) **prevent actual deletion** — they enforce soft delete. This creates a conflict: the Forms UI thinks it can cascade-delete, but the trigger converts it to a status update.

In the new system, the JPA cascade configuration must match the *actual* behavior (soft delete), not the Forms-declared behavior (cascade delete).

#### Likelihood: Possible

This only manifests during delete operations, which are rare in HR systems (most records are soft-deleted).

#### Impact: High

- Configuring JPA `CascadeType.REMOVE` to match Forms would bypass the soft-delete pattern
- Not configuring cascade would leave orphaned detail records
- Inconsistent behavior between old and new systems during coexistence

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Soft delete pattern** — Implement `SoftDeletable` interface and override `SimpleJpaRepository.delete()` to set `activeFlag='N'` instead of `DELETE` | Backend Dev | Phase 0 |
| **Cascade audit** — For each master-detail relation, determine actual cascade behavior (considering triggers) vs. declared behavior | Migration Lead | Phase 0 |
| **No JPA cascade deletes** — Do not use `CascadeType.REMOVE`; handle all deletion logic explicitly in service layer | Backend Dev | Phase 2a |
| **Orphan cleanup** — Write a data integrity checker that identifies detail records whose master has been soft-deleted | DBA | Phase 2a |

---

### R-07: Form Trigger Execution Order Not Replicated in Java Lifecycle

**Severity: HIGH**

#### Description

Oracle Forms triggers fire in a specific, well-defined order. The HRMS relies on this ordering:

**Example: Creating a new employee record in HRMS_EMPLOYEE**

```
1. WHEN-VALIDATE-ITEM (each field as user tabs through)
2. PRE-INSERT (block-level) — generates EMP_NUMBER, sets audit columns
3. INSERT row into EMPLOYEES table
4. Database trigger TRG_EMP_BEFORE_INSERT fires — validates, sets audit columns (again)
5. POST-INSERT (block-level) — not used, but would fire here
6. POST-DATABASE-COMMIT — not used, but would fire here
7. ON-ERROR — if any step fails
```

In the Java/React architecture, the equivalent flow is:

```
1. React form validation (onBlur / onSubmit)
2. REST API call (POST /api/employees)
3. Spring @Valid validation (Jakarta Bean Validation)
4. EmployeeService.createEmployee() — business logic
5. JPA @PrePersist callback — sets audit columns
6. Database INSERT (trigger fires if still present)
7. @PostPersist callback (optional)
8. @ControllerAdvice handles exceptions
```

The risk is that business logic that depends on Forms trigger ordering (e.g., PRE-INSERT setting a value that POST-QUERY later reads) may not work in the new order.

#### Likelihood: Likely

The HRMS has 12+ trigger types across 6 forms. At least some will have ordering dependencies.

#### Impact: High

- Silent data corruption if validation runs in wrong order
- Duplicate audit column setting (both Forms PRE-INSERT and DB trigger set `CREATED_BY`)
- Logic that depends on PRE-INSERT running before the DB trigger may fail

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Trigger execution map** — For each form, document the complete trigger firing sequence for CREATE, UPDATE, DELETE operations | Migration Lead | Phase 0 |
| **Identify ordering dependencies** — Flag any trigger that reads a value set by a prior trigger in the sequence | Migration Lead | Phase 0 |
| **Service method orchestration** — Implement each operation's logic as an explicit sequence of steps in the service method (not relying on callback ordering) | Backend Dev | Phase 2a |
| **Remove duplicate triggers** — Once Java handles audit columns via `@CreatedBy`/`@LastModifiedBy`, disable the corresponding DB triggers to avoid double-setting | DBA | Phase 2a (carefully, after validation) |
| **Integration tests** — Test CREATE/UPDATE/DELETE for each entity verifying all fields are correctly populated | QA | Phase 2a |

---

### R-08: Hard-Coded Configuration Values Missed During Extraction

**Severity: HIGH**

#### Description

The legacy codebase contains numerous hard-coded values scattered across PL/SQL packages, form triggers, and PLL libraries. These values must all be externalized to configuration in the new system.

**Known hard-coded values from the codebase:**

| Location | Value | Description |
|---|---|---|
| PKG_PAYROLL.pkb line 6 | `168600` | Social Security wage base (2024) |
| PKG_PAYROLL.pkb line 7 | `0.062` | Social Security tax rate |
| PKG_PAYROLL.pkb line 8 | `0.0145` | Medicare tax rate |
| PKG_PAYROLL.pkb line 9 | `0.009` | Additional Medicare rate |
| PKG_PAYROLL.pkb line 10 | `200000` | Additional Medicare threshold |
| PKG_PAYROLL.pkb lines 11–13 | `14600`, `29200`, `4300` | Standard deductions and allowance |
| PKG_SECURITY.pkb line 7 | `HR$ystem_3ncrypt10n_K3y_2024!!` | Encryption key (CRITICAL) |
| PKG_SECURITY.pkb line 8 | `30` | Session timeout (minutes) |
| PKG_REPORTING (referenced) | `October 1` | Fiscal year start |
| PKG_NOTIFICATION (referenced) | Legacy SMTP server address | SMTP configuration |
| PKG_INTEGRATION (referenced) | FTP credentials | Cleartext FTP credentials in SYSTEM_PARAMETERS |
| PKG_SECURITY.has_permission | Job grade thresholds | Hard-coded RBAC logic |
| HRMS_VALIDATION_LIB.pll | Future hire date limit: 180 days | Different from trigger's 90-day limit |
| TRG_EMP_BEFORE_INSERT | Future hire date limit: 90 days | Different from PLL's 180-day limit |

#### Likelihood: Very Likely

With 7,000+ lines of PL/SQL and 1,500+ lines of Forms XML, it is nearly certain that some hard-coded values will be missed during initial migration.

#### Impact: Medium

- Tax calculations with wrong constants produce incorrect pay
- Security parameters that aren't externalized create configuration drift
- Missed values may not surface until a specific code path is exercised

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Automated constant extraction** — Use `grep`/`ripgrep` to scan all PL/SQL for `CONSTANT`, literal numbers, and string literals; review each | Migration Lead | Phase 0 |
| **Configuration inventory** — Create a master spreadsheet of every extracted constant with its source, current value, and target configuration location | Migration Lead | Phase 0 |
| **Externalize to config** — Use `application.yml` + `@ConfigurationProperties` for non-sensitive values; secrets manager for sensitive values | Backend Dev | Phase 1–3 |
| **Configuration validation tests** — Write tests that fail if a known configuration value is hard-coded instead of read from config | QA | Ongoing |
| **Code review checklist** — Add "no hard-coded configuration values" to the PR review checklist | Tech Lead | Ongoing |

---

### R-09: Security Vulnerabilities Carried Forward or Incompletely Remediated

**Severity: HIGH**

#### Description

The legacy `PKG_SECURITY` has multiple documented security vulnerabilities. The migration is an opportunity to fix all of them, but incomplete remediation could create a false sense of security.

**Documented vulnerabilities in PKG_SECURITY.pkb:**

| Vulnerability | Location | Severity | Risk if Carried Forward |
|---|---|---|---|
| **MD5 password hashing** | Lines 14–24 (body) | Critical | Rainbow table attacks; regulatory non-compliance |
| **Hard-coded encryption key** | Line 7 (body) | Critical | SSN/PII exposure if source code is compromised |
| **No account lockout** | Lines 27–29 (body) | High | Brute-force attacks succeed eventually |
| **Timing attack on password comparison** | Lines 48–50 (body) | Medium | Side-channel attack reveals password length |
| **Cleartext password transmission** | HRMS_LOGIN.xml | High | Network sniffing captures passwords |
| **No 2FA/MFA** | System-wide | Medium | Single factor authentication |
| **FTP credentials in cleartext** | PKG_INTEGRATION / SYSTEM_PARAMETERS | High | Database breach exposes FTP access |
| **SQL injection** | PKG_EMPLOYEE.pkb lines 465–471 | Critical | Arbitrary data access/modification |

#### Likelihood: Possible

If the migration team focuses on functional parity rather than security improvements, vulnerabilities may be inadvertently replicated.

#### Impact: Critical

Security breaches in an HRMS affect PII (SSN, salary, bank accounts) for all employees.

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Security requirements document** — Explicitly list every vulnerability and its remediation | Security Lead | Phase 0 |
| **BCrypt for passwords** — Replace MD5 with BCrypt (`BCryptPasswordEncoder` in Spring Security); force password reset for all users at cutover | Backend Dev | Phase 1 |
| **Secrets management** — Move encryption key to a secrets manager (Vault, AWS Secrets Manager); rotate key | DevOps | Phase 1 |
| **Account lockout** — Implement 5-attempt lockout with progressive backoff in `AuthService` | Backend Dev | Phase 1 |
| **HTTPS only** — Enforce TLS for all traffic; disable HTTP | DevOps | Phase 0 |
| **Parameterized queries** — Ensure all database queries use JPA/Hibernate parameterized queries (eliminates SQL injection by design) | Backend Dev | All phases |
| **Security penetration test** — Engage a third party to test the new system before Phase 4 cutover | Security Lead | Phase 3 |
| **MFA implementation** — Add TOTP-based 2FA using Spring Security's MFA support | Backend Dev | Phase 1 (optional: post-cutover) |

---

### R-10: Data Migration and Cutover Integrity Failures

**Severity: HIGH**

#### Description

During the cutover from legacy Oracle Forms to the new Java/React system, data integrity must be maintained. Specific risks include:

1. **Dual-write conflicts**: During coexistence, both legacy Forms and new React UI write to the same tables. Concurrent edits to the same record could produce inconsistencies if locking is not coordinated.

2. **Schema evolution**: The new system may require schema changes (new columns, renamed columns, changed data types). These changes must be backward-compatible with the legacy Forms during coexistence.

3. **Soft-delete semantics**: The legacy trigger (`TRG_EMP_INSTEAD_OF_DELETE`) implements soft delete, but Forms users see a "record deleted" message. The new system must replicate this UX.

4. **Sequence gaps**: If the new system uses JPA sequence allocation with `allocationSize > 1`, sequence values will have gaps that may confuse reconciliation scripts.

5. **Audit trail continuity**: The `AUDIT_LOG` table must have continuous entries from both legacy and new systems. Different audit field formats could break reporting.

6. **Password migration**: Moving from MD5 to BCrypt means existing password hashes are invalid. Users must reset passwords.

7. **In-flight transactions**: Leave requests in PENDING status, payroll runs in CALCULATING status, performance reviews in DRAFT status — all must be handled during cutover.

#### Likelihood: Possible

With careful planning, most of these can be avoided, but the complexity of a 42-table schema with 200+ triggers makes surprises likely.

#### Impact: Critical

Data integrity failures in an HRMS can affect payroll processing, legal compliance, and employee trust.

#### Mitigation

| Action | Owner | Timeline |
|---|---|---|
| **Optimistic locking** — Add `@Version` column to all entities; both legacy Forms and new system check version before update | Backend Dev + DBA | Phase 0 |
| **Backward-compatible migrations** — Use Flyway with additive-only migrations during coexistence (add columns, don't rename or drop) | DBA | All phases |
| **Pre-cutover freeze** — Define a data freeze window for each module cutover (e.g., no leave requests during Leave cutover weekend) | Project Manager | Each phase |
| **In-flight transaction handler** — Write a script to identify all in-flight transactions before each phase cutover; either complete them in legacy or migrate their state | DBA + Backend Dev | Each phase |
| **Password reset campaign** — Notify users 2 weeks before auth cutover; force BCrypt password reset at first login on new system | Project Manager + Backend Dev | Phase 1 |
| **Reconciliation scripts** — For each table, write a row-count and checksum reconciliation script; run daily during coexistence | DBA | Phase 2–4 |
| **Cutover rehearsal** — Perform a full cutover on a staging environment before production cutover | Entire Team | Phase 4 (2 weeks before production) |

---

## Risk Monitoring Schedule

| Frequency | Activity | Owner |
|---|---|---|
| **Weekly** | Review risk register in sprint planning; update likelihood/impact based on new information | Scrum Master |
| **Per phase gate** | Formal risk review before proceeding to next phase; verify all mitigations for current phase are in place | Migration Lead |
| **Monthly** | Stakeholder risk report — escalate any risks that have moved to CRITICAL | Project Manager |
| **Ad hoc** | Immediate escalation if any risk materializes (e.g., payroll discrepancy detected) | Any team member |

---

## Risk Ownership

| Role | Risks Owned | Responsibility |
|---|---|---|
| **Migration Lead** | R-01, R-02, R-05, R-07, R-08 | Technical risk assessment and mitigation planning |
| **Backend Dev Lead** | R-03, R-04, R-06 | Implementation of technical mitigations |
| **Security Lead** | R-09 | Security remediation and validation |
| **DBA** | R-10 | Data integrity, schema management, reconciliation |
| **Project Manager** | R-03 (cutover), R-10 (cutover) | Stakeholder communication and cutover planning |
