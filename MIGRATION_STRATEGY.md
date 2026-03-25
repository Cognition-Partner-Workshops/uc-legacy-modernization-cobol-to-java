# Oracle Forms HRMS Migration Strategy

> Analysis of the legacy Oracle Forms 12c / PL/SQL Human Resources Management System  
> Source: `ts-plsql-oracle-forms-legacy-codebase`  
> Target: Java 17 + Spring Boot (backend) / React (frontend)

---

## 1. Executive Summary

The HRMS application comprises **6 Oracle Forms modules**, **11 PL/SQL packages** (~7 000 lines), **42 database tables**, **15 views**, and **2 PL/SQL libraries**. It serves ~200 concurrent users across 3 regional offices on Oracle Forms 12c / WebLogic / Oracle DB 19c.

This document evaluates three migration approaches for each functional area:

| Approach | Description |
|---|---|
| **Strangler Fig** | Incrementally replace modules behind a facade; old and new coexist during transition |
| **Big-Bang Rewrite** | Complete rewrite of the module in Java/React; cut over all at once |
| **Re-Platform (APEX)** | Lift PL/SQL logic into Oracle APEX with minimal rewrite; stay on Oracle stack |

---

## 2. Functional Areas Inventory

| ID | Functional Area | Forms Module(s) | Primary Package(s) | Tables | Complexity |
|----|----------------|-----------------|---------------------|--------|------------|
| FA-01 | Authentication & Security | HRMS_LOGIN, HRMS_MENU | PKG_SECURITY, PKG_AUDIT | USER_SESSIONS, AUDIT_LOG, LOOKUP_VALUES | Medium |
| FA-02 | Employee Management | HRMS_EMPLOYEE | PKG_EMPLOYEE, PKG_VALIDATION | EMPLOYEES, EMPLOYEE_HISTORY, EMPLOYEE_DEPENDENTS, EMERGENCY_CONTACTS, DEPARTMENTS, LOCATIONS, JOB_GRADES, JOB_TITLES | High |
| FA-03 | Payroll Processing | HRMS_PAYROLL | PKG_PAYROLL, PKG_INTEGRATION | SALARY_RECORDS, PAY_ELEMENTS, EMPLOYEE_PAY_ELEMENTS, PAY_PERIODS, PAYROLL_RUNS, PAYROLL_DETAILS, TAX_BRACKETS, EMPLOYEE_TAX_INFO, EMPLOYEE_BANK_ACCOUNTS | Very High |
| FA-04 | Leave Management | HRMS_LEAVE | PKG_LEAVE | LEAVE_TYPES, LEAVE_BALANCES, LEAVE_REQUESTS, LEAVE_ACCRUAL_LOG, HOLIDAYS | Medium |
| FA-05 | Performance Reviews | HRMS_PERFORMANCE | PKG_PERFORMANCE | REVIEW_CYCLES, PERFORMANCE_REVIEWS, PERFORMANCE_GOALS | Medium |
| FA-06 | Reporting & Analytics | (HRMS_REPORTS referenced) | PKG_REPORTING | Denormalized reporting tables, views (VW_ACTIVE_EMPLOYEES, VW_ORG_HIERARCHY, VW_EMPLOYEE_COMPENSATION, VW_LEAVE_SUMMARY, VW_PAYROLL_LATEST) | Medium |
| FA-07 | External Integrations | (batch jobs) | PKG_INTEGRATION | SYSTEM_PARAMETERS, NOTIFICATION_QUEUE | High |
| FA-08 | Shared Infrastructure | HRMS_COMMON_LIB.pll, HRMS_VALIDATION_LIB.pll, HRMS_MENU.mmb | PKG_COMMON, PKG_NOTIFICATION | SYSTEM_PARAMETERS, NOTIFICATION_QUEUE | Medium |

---

## 3. Strategy Evaluation by Functional Area

### FA-01: Authentication & Security

#### Current State
- Login via `HRMS_LOGIN.xml` — username/password posted through Forms applet (cleartext)
- Session tracked in `USER_SESSIONS` table; 30-min DB-time timeout
- RBAC based on job grade (hard-coded thresholds in `PKG_SECURITY.has_permission`)
- Password hashing uses **MD5** (`DBMS_CRYPTO.HASH_MD5`)
- Encryption key **hard-coded** in package body (`HR$ystem_3ncrypt10n_K3y_2024!!`)
- No account lockout, no 2FA, no CAPTCHA

#### Strangler Fig (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — authentication is a natural boundary; new JWT/OAuth layer can front both old and new modules |
| Risk | Low — no downstream data dependencies |
| Effort | 2-3 sprints |
| Coexistence | New auth service issues JWT; legacy forms validate via a DB-linked session shim |

**Approach:** Deploy a Spring Security + OAuth 2.0 / OpenID Connect authentication service first. Legacy forms continue to call `PKG_SECURITY.authenticate` which is shimmed to also create a JWT session. New React modules use the JWT directly. This is the **first module to migrate** because every other module depends on it.

#### Big-Bang Rewrite
| Criterion | Assessment |
|---|---|
| Feasibility | High |
| Risk | Medium — requires all modules to switch simultaneously |
| Effort | 2-3 sprints |

Viable but unnecessary. The strangler fig approach achieves the same result with lower risk since auth is a natural seam.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | Medium — APEX has built-in auth but MD5/hard-coded key issues remain in PL/SQL |
| Risk | High — carries forward security vulnerabilities unless PKG_SECURITY is rewritten |
| Effort | 1-2 sprints for APEX native auth |

Not recommended. The security debt in `PKG_SECURITY` (MD5, hard-coded keys, no lockout) demands a ground-up rewrite regardless of platform.

---

### FA-02: Employee Management

#### Current State
- `HRMS_EMPLOYEE.xml`: 5 data blocks, 4 tab pages, 8 LOVs, master-detail (Employee -> Salary)
- Complex form triggers: `PRE-INSERT` (PK generation via sequence + MAX()+1), `POST-QUERY` (display item population), `WHEN-VALIDATE-ITEM` (email, date, FK validation)
- `PKG_EMPLOYEE`: 967 lines — CRUD, lifecycle (transfer/promote/terminate/rehire), org chart
- **Circular dependency** with `PKG_PAYROLL` (salary record creation on hire)
- `generate_emp_number` has a **race condition** (MAX()+1 without SELECT FOR UPDATE)
- `search_employees` has a **SQL injection** vulnerability (string concatenation)
- Trigger `TRG_EMP_INSTEAD_OF_DELETE` prevents DELETE, conflicting with Forms expectations

#### Strangler Fig (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — clear REST API surface (CRUD + lifecycle operations) |
| Risk | Medium — circular dependency with Payroll requires careful interface design |
| Effort | 4-6 sprints |
| Coexistence | New React UI calls Spring Boot API; legacy form remains operational for users not yet migrated |

**Approach:** Build `EmployeeService` as a Spring Boot REST API backed by JPA entities. Break the circular dependency by introducing a `SalaryService` interface that both Employee and Payroll depend on (dependency inversion). Migrate the 8 LOVs to REST endpoints returning JSON for React `<Select>` / `<Autocomplete>` components. The master-detail Employee -> Salary relationship maps to a parent resource with nested sub-resources.

#### Big-Bang Rewrite
| Criterion | Assessment |
|---|---|
| Feasibility | Medium — large surface area with complex form logic |
| Risk | High — 5 data blocks, 8 LOVs, multiple trigger types; easy to miss edge cases |
| Effort | 6-8 sprints |

Not recommended due to the volume of UI logic embedded in form triggers. A phased strangler approach allows iterative validation.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | High — APEX Interactive Reports/Forms map well to data blocks |
| Risk | Medium — business logic in triggers and PKG_EMPLOYEE is preserved but bugs carry forward |
| Effort | 3-4 sprints |

Viable if the organization wants to stay on Oracle. However, it preserves the race condition in `generate_emp_number`, the SQL injection in `search_employees`, and the circular dependency. These issues require code changes regardless.

---

### FA-03: Payroll Processing

#### Current State
- `HRMS_PAYROLL.xml`: 3 tab pages, 4 data blocks, master-detail (Period -> Run), action buttons (Create/Calculate/Approve)
- `PKG_PAYROLL`: 898 lines — salary management, pay period generation, payroll calculation, tax withholding (federal/state/FICA/Medicare)
- **Hard-coded 2024 tax brackets** as package constants (`c_ss_wage_base_2024 = 168600`, `c_standard_deduction_single = 14600`, etc.)
- Row-by-row cursor processing for payroll calculation (performance bottleneck)
- Partial COMMITs every 50 employees during calculation (data consistency risk)
- Overtime calculation does not account for holidays
- YTD accumulation bug for mid-year hires
- **Circular dependency** with PKG_EMPLOYEE

#### Strangler Fig
| Criterion | Assessment |
|---|---|
| Feasibility | Medium — payroll is deeply coupled to employee data and tax rules |
| Risk | High — financial calculations must be bit-for-bit accurate during coexistence |
| Effort | 6-8 sprints |

Possible but the coexistence period is risky. Dual-running payroll (old and new) for reconciliation adds significant overhead.

#### Big-Bang Rewrite (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — well-defined inputs/outputs; tax calculations are pure functions |
| Risk | Medium — requires thorough parallel-run testing before cutover |
| Effort | 6-8 sprints |
| Validation | 3+ months of parallel payroll runs comparing old vs. new output |

**Approach:** Rewrite as a dedicated `PayrollService` in Spring Boot. Externalize tax brackets to database-driven configuration (eliminating hard-coded constants). Replace cursor-based processing with Spring Batch chunk-oriented processing for scalability. Use `BigDecimal` for all monetary calculations. Implement proper transaction boundaries (all-or-nothing per payroll run, not partial commits). Validate with parallel runs against legacy system before cutover.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | Medium — APEX can host the UI, but PL/SQL calculation engine carries forward all bugs |
| Risk | Very High — hard-coded tax brackets, partial commits, and cursor performance remain |
| Effort | 2-3 sprints for UI, but 4-6 sprints to fix underlying PL/SQL issues |

Not recommended. The calculation engine needs a rewrite regardless, making APEX re-platforming redundant.

---

### FA-04: Leave Management

#### Current State
- `HRMS_LEAVE.xml`: 4 tab pages, 5 data blocks, 3 LOVs, approval workflow
- `PKG_LEAVE`: request submission, approval/rejection, balance tracking, monthly accrual batch, carryover processing
- **Half-day overlap detection bug** — overlapping leave check doesn't handle half-day flags
- **Carryover double-expiry** — batch job can expire carryover twice if run on same day
- Holiday detection uses exact date match only (not observed dates)

#### Strangler Fig (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — self-contained workflow with clear API boundaries |
| Risk | Low — leave management has minimal impact on other modules during migration |
| Effort | 3-4 sprints |
| Coexistence | New self-service portal for leave; legacy form deprecated module-by-module |

**Approach:** Build a `LeaveService` with a React self-service portal. The approval workflow maps naturally to a state machine pattern (PENDING -> APPROVED/REJECTED -> CANCELLED/TAKEN). Fix the half-day overlap bug and carryover double-expiry during migration. Expose a calendar API for the team calendar view.

#### Big-Bang Rewrite
| Criterion | Assessment |
|---|---|
| Feasibility | High |
| Risk | Low |
| Effort | 3-4 sprints |

Viable and similar effort to strangler fig. However, strangler fig allows the leave self-service portal to be deployed early for quick wins.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | Very High — APEX workflows map well to leave approval processes |
| Risk | Low — but half-day bug and carryover bug carry forward |
| Effort | 2-3 sprints |

Viable if staying on Oracle, but doesn't fix the known bugs in PKG_LEAVE without additional PL/SQL rework.

---

### FA-05: Performance Reviews

#### Current State
- `HRMS_PERFORMANCE.xml`: 3 tab pages, 4 data blocks, master-detail chains (Cycle -> Review -> Goal)
- `PKG_PERFORMANCE`: review cycle management, self/manager assessments, goal tracking, rating calibration
- Uses CLOB fields for assessments (mapped to Oracle Forms multi-line text items)
- Relatively self-contained with minimal cross-module dependencies

#### Strangler Fig (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **Very High** — cleanly bounded domain, minimal external dependencies |
| Risk | Very Low — no financial or regulatory sensitivity |
| Effort | 3-4 sprints |
| Coexistence | New performance module can operate independently while legacy runs in parallel |

**Approach:** Build a `PerformanceService` with a React UI featuring rich text editing for assessments (replacing CLOB-backed text areas). The multi-level master-detail (Cycle -> Review -> Goal) maps to nested REST resources. Goal progress tracking maps to a modern Kanban-style UI.

#### Big-Bang Rewrite
| Criterion | Assessment |
|---|---|
| Feasibility | High |
| Risk | Low |
| Effort | 3-4 sprints |

Similar effort; no strong advantage over strangler fig.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | Very High — straightforward Interactive Report + Form mapping |
| Risk | Very Low |
| Effort | 1-2 sprints |

The fastest path if staying on Oracle. Good candidate for APEX if the organization is doing a partial migration.

---

### FA-06: Reporting & Analytics

#### Current State
- `PKG_REPORTING`: 7 report procedures (headcount, compensation, turnover, new hires, leave utilization, payroll summary, EEO compliance)
- Denormalized reporting tables refreshed nightly (stale during business hours)
- Hard-coded fiscal year start (October 1)
- Oracle Reports (.rdf) referenced but not in repository
- Views: `VW_ACTIVE_EMPLOYEES`, `VW_ORG_HIERARCHY`, `VW_EMPLOYEE_COMPENSATION`, `VW_LEAVE_SUMMARY`, `VW_PAYROLL_LATEST`

#### Strangler Fig
| Criterion | Assessment |
|---|---|
| Feasibility | Medium — reports depend on data from all other modules |
| Risk | Medium — stale data issues during coexistence if some modules are migrated |
| Effort | 3-4 sprints |

Reports can be migrated incrementally, but they depend on data from all other modules.

#### Big-Bang Rewrite (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — reports are read-only queries; straightforward to re-implement |
| Risk | Low — no data mutation |
| Effort | 3-4 sprints |

**Approach:** Replace Oracle Reports with a modern reporting stack (e.g., JasperReports or embedded analytics). Replace denormalized tables with real-time database views or materialized views with fast refresh. Externalize fiscal year configuration. This module should be migrated **last** since it reads from all other modules' tables.

#### Re-Platform (APEX)
| Criterion | Assessment |
|---|---|
| Feasibility | Very High — APEX Interactive Reports are a natural fit |
| Risk | Low |
| Effort | 2-3 sprints |

Excellent APEX candidate. Interactive Reports provide filtering, export, and charting out of the box.

---

### FA-07: External Integrations

#### Current State
- `PKG_INTEGRATION`: GL journal generation, ADP benefits feed, time & attendance import, org structure sync
- Uses **UTL_FILE** for flat-file exchange (not API-based)
- **FTP credentials stored in cleartext** in SYSTEM_PARAMETERS
- ADP-specific format (vendor lock-in)
- No retry logic for failed transfers

#### Big-Bang Rewrite (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — well-defined integration points with external systems |
| Risk | Medium — requires coordination with external system owners (GL, ADP, T&A) |
| Effort | 3-4 sprints |

**Approach:** Replace UTL_FILE flat-file exchange with REST API integrations (or SFTP with encrypted credentials managed by a vault). Implement retry logic with exponential backoff. Abstract the benefits feed format to support multiple vendors (not just ADP). Use Spring Integration or Apache Camel for the integration layer. Store credentials in a secrets manager (HashiCorp Vault, AWS Secrets Manager), not in a database table.

#### Strangler Fig
Not practical — integration jobs are batch processes that run atomically; partial migration adds complexity with no benefit.

#### Re-Platform (APEX)
Preserves all security issues (cleartext FTP, no retry). Not recommended.

---

### FA-08: Shared Infrastructure

#### Current State
- `HRMS_COMMON_LIB.pll.sql`: error handling, toolbar handlers, date formatting, session management, dynamic LOV refresh
- `HRMS_VALIDATION_LIB.pll.sql`: client-side validation (email, phone, SSN, date, salary range) — **known drift** from server-side `PKG_VALIDATION`
- `PKG_COMMON`: logging, configuration parameters, date utilities, formatting, validations
- `PKG_NOTIFICATION`: email/SMS/in-app notification queue — hard-coded SMTP, no rate limiting

#### Strangler Fig (Recommended)
| Criterion | Assessment |
|---|---|
| Feasibility | **High** — these are utility layers that underpin everything |
| Risk | Low — new shared libraries are independent of legacy ones |
| Effort | 2-3 sprints (built incrementally as other modules migrate) |

**Approach:** Build shared Java libraries (`hrms-common`, `hrms-validation`, `hrms-notification`) as the foundation layer. Eliminate validation drift by having a single source of truth in the Java validation layer (replaces both PLL client-side and PKG_VALIDATION server-side). Replace PKG_NOTIFICATION with a modern messaging service (e.g., Spring Mail + Amazon SES or SendGrid). Replace SYSTEM_PARAMETERS with Spring externalized configuration (application.yml + database-backed config).

---

## 4. Recommended Strategy Summary

| Functional Area | Recommended Approach | Rationale |
|---|---|---|
| **FA-01: Auth & Security** | Strangler Fig | Natural boundary; new auth fronts both old and new modules |
| **FA-02: Employee Mgmt** | Strangler Fig | Large surface area benefits from incremental migration |
| **FA-03: Payroll** | Big-Bang Rewrite | Financial accuracy requires clean break; parallel-run validation |
| **FA-04: Leave Mgmt** | Strangler Fig | Self-contained; early self-service win |
| **FA-05: Performance** | Strangler Fig | Cleanly bounded; low risk |
| **FA-06: Reporting** | Big-Bang Rewrite | Read-only; depends on all other modules (migrate last) |
| **FA-07: Integrations** | Big-Bang Rewrite | Batch processes; security issues demand full replacement |
| **FA-08: Shared Infra** | Strangler Fig | Foundation layer built incrementally |

### Overall Pattern: **Hybrid Strangler Fig with Targeted Big-Bang Rewrites**

The dominant strategy is strangler fig for UI-facing modules (auth, employee, leave, performance), combined with big-bang rewrites for backend-heavy modules (payroll, reporting, integrations) where coexistence adds risk without benefit.

---

## 5. Technology Stack Recommendation

| Layer | Legacy | Target |
|---|---|---|
| Frontend | Oracle Forms 12c (Java applet) | React 18 + TypeScript + Material UI |
| Backend | PL/SQL packages + Forms triggers | Java 17 + Spring Boot 3 + Spring Security |
| Database | Oracle 19c (HRMS schema) | PostgreSQL 16 (or retain Oracle if licensed) |
| ORM | Direct SQL in PL/SQL | Spring Data JPA / Hibernate |
| Auth | PKG_SECURITY (MD5, hard-coded keys) | Spring Security + OAuth 2.0 / Keycloak |
| Batch | DBMS_SCHEDULER + cursor loops | Spring Batch |
| Messaging | UTL_MAIL (hard-coded SMTP) | Spring Mail + message broker (RabbitMQ) |
| Integrations | UTL_FILE flat files, FTP | REST APIs + SFTP + Spring Integration |
| Reporting | Oracle Reports (.rdf) | JasperReports / embedded dashboards |
| Config | SYSTEM_PARAMETERS table | Spring Config Server / application.yml |
| Secrets | Cleartext in DB | HashiCorp Vault / cloud secrets manager |

---

## 6. Timeline Estimate

| Phase | Duration | Modules |
|---|---|---|
| **Phase 0: Foundation** | 4 weeks | Shared libraries, DB migration scripts, CI/CD pipeline, auth service skeleton |
| **Phase 1: Auth + Shared Infra** | 6 weeks | FA-01, FA-08 — new auth service, shared Java libraries |
| **Phase 2: Employee + Leave** | 10 weeks | FA-02, FA-04 — strangler fig with React UIs |
| **Phase 3: Performance** | 6 weeks | FA-05 — strangler fig with React UI |
| **Phase 4: Payroll + Integrations** | 12 weeks | FA-03, FA-07 — big-bang rewrite with parallel runs |
| **Phase 5: Reporting + Cutover** | 6 weeks | FA-06 — reporting migration, legacy decommission |
| **Total** | **~44 weeks** | |

---

## 7. Decision Criteria Reference

The following criteria were used to evaluate each approach:

| Criterion | Weight | Description |
|---|---|---|
| **Risk** | 30% | Likelihood and impact of migration failure |
| **Coexistence Complexity** | 20% | Difficulty of running old and new systems simultaneously |
| **Effort** | 20% | Development time and team resources required |
| **Technical Debt Remediation** | 15% | Whether the approach fixes known bugs and security issues |
| **Business Continuity** | 15% | Impact on users during migration |
