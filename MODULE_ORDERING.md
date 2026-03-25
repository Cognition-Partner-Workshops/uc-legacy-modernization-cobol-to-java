# Oracle Forms HRMS — Module Migration Ordering

> Safe migration sequence respecting package dependencies, data coupling, and business risk.  
> Source: `ts-plsql-oracle-forms-legacy-codebase`

---

## Table of Contents

1. [Package Dependency Graph](#1-package-dependency-graph)
2. [Dependency Analysis](#2-dependency-analysis)
3. [Circular Dependency Resolution](#3-circular-dependency-resolution)
4. [Migration Phase Plan](#4-migration-phase-plan)
5. [Detailed Phase Breakdown](#5-detailed-phase-breakdown)
6. [Parallel Migration Opportunities](#6-parallel-migration-opportunities)
7. [Rollback Strategy per Phase](#7-rollback-strategy-per-phase)

---

## 1. Package Dependency Graph

```
Legend:  ──▶ depends on     ◀──▶ circular dependency

                    ┌─────────────┐
                    │  PKG_AUDIT  │   (no dependencies — base)
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  PKG_COMMON │   (no dependencies — base)
                    └──────┬──────┘
                           │
              ┌────────────┼────────────────────┐
              │            │                    │
     ┌────────▼──────┐  ┌─▼──────────────┐  ┌──▼────────────────┐
     │ PKG_VALIDATION │  │ PKG_NOTIFICATION│  │   PKG_SECURITY    │
     │  (PKG_COMMON)  │  │  (PKG_COMMON)  │  │(PKG_COMMON,       │
     └────────────────┘  └────────┬───────┘  │ PKG_AUDIT)        │
                                  │          └────────┬───────────┘
                                  │                   │
              ┌───────────────────┼───────────────────┤
              │                   │                   │
     ┌────────▼──────────────────────────────────────────┐
     │                   PKG_EMPLOYEE                     │
     │  (PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION,        │
     │   PKG_PAYROLL ◀──▶ circular)                      │
     └────────┬──────────────────────────────┬───────────┘
              │                              │
              │                    ┌─────────▼───────────┐
              │                    │    PKG_PAYROLL       │
              │                    │  (PKG_EMPLOYEE ◀──▶, │
              │                    │   PKG_COMMON,        │
              │                    │   PKG_AUDIT,         │
              │                    │   PKG_NOTIFICATION)  │
              │                    └─────────┬────────────┘
              │                              │
     ┌────────▼────────┐           ┌─────────▼────────────┐
     │    PKG_LEAVE     │           │   PKG_INTEGRATION    │
     │  (PKG_EMPLOYEE,  │           │  (PKG_COMMON,        │
     │   PKG_COMMON,    │           │   PKG_PAYROLL,       │
     │   PKG_AUDIT,     │           │   PKG_EMPLOYEE)      │
     │   PKG_NOTIFICATION)          └──────────────────────┘
     └────────┬─────────┘
              │
     ┌────────▼────────────┐
     │  PKG_PERFORMANCE    │
     │  (PKG_EMPLOYEE,     │
     │   PKG_COMMON,       │
     │   PKG_AUDIT,        │
     │   PKG_NOTIFICATION) │
     └─────────────────────┘

     ┌──────────────────────┐
     │   PKG_REPORTING      │
     │  (PKG_EMPLOYEE,      │
     │   PKG_PAYROLL,       │
     │   PKG_COMMON)        │
     └──────────────────────┘
```

---

## 2. Dependency Analysis

### Dependency Matrix

Each cell shows whether the **row** package depends on the **column** package.

| Package ↓ depends on → | AUDIT | COMMON | VALIDATION | NOTIFICATION | SECURITY | EMPLOYEE | PAYROLL | LEAVE | PERFORMANCE | REPORTING | INTEGRATION |
|---|---|---|---|---|---|---|---|---|---|---|---|
| **PKG_AUDIT** | — | | | | | | | | | | |
| **PKG_COMMON** | | — | | | | | | | | | |
| **PKG_VALIDATION** | | **Y** | — | | | | | | | | |
| **PKG_NOTIFICATION** | | **Y** | | — | | | | | | | |
| **PKG_SECURITY** | **Y** | **Y** | | | — | | | | | | |
| **PKG_EMPLOYEE** | **Y** | **Y** | | **Y** | | — | **Y** (circular) | | | | |
| **PKG_PAYROLL** | **Y** | **Y** | | **Y** | | **Y** (circular) | — | | | | |
| **PKG_LEAVE** | **Y** | **Y** | | **Y** | | **Y** | | — | | | |
| **PKG_PERFORMANCE** | **Y** | **Y** | | **Y** | | **Y** | | | — | | |
| **PKG_REPORTING** | | **Y** | | | | **Y** | **Y** | | | — | |
| **PKG_INTEGRATION** | | **Y** | | | | **Y** | **Y** | | | | — |

### Topological Sort (ignoring circular dependency)

Resolving `PKG_EMPLOYEE ↔ PKG_PAYROLL` by treating them as a single unit:

1. **Tier 0 (no dependencies):** `PKG_AUDIT`, `PKG_COMMON`
2. **Tier 1 (depends on Tier 0 only):** `PKG_VALIDATION`, `PKG_NOTIFICATION`, `PKG_SECURITY`
3. **Tier 2 (depends on Tier 0 + Tier 1):** `PKG_EMPLOYEE` + `PKG_PAYROLL` (coupled unit)
4. **Tier 3 (depends on Tier 2):** `PKG_LEAVE`, `PKG_PERFORMANCE`, `PKG_INTEGRATION`
5. **Tier 4 (depends on everything):** `PKG_REPORTING`

### Coupling Metrics

| Package Pair | Coupling Type | Strength | Notes |
|---|---|---|---|
| PKG_EMPLOYEE ↔ PKG_PAYROLL | **Circular** | **High** | Employee calls Payroll for salary on hire; Payroll calls Employee for data |
| PKG_EMPLOYEE → PKG_NOTIFICATION | Unidirectional | Medium | Lifecycle events trigger notifications |
| PKG_PAYROLL → PKG_NOTIFICATION | Unidirectional | Medium | Payroll completion triggers notifications |
| PKG_REPORTING → PKG_EMPLOYEE + PKG_PAYROLL | Read-only | Low | Reporting only reads data; no writes |
| PKG_INTEGRATION → PKG_EMPLOYEE + PKG_PAYROLL | Read + Write | Medium | Imports time/attendance; exports GL/benefits |
| All → PKG_COMMON | Utility | Low | Shared utilities; easy to replace |
| All → PKG_AUDIT | Cross-cutting | Low | Audit logging; replaceable with AOP/Spring Events |

---

## 3. Circular Dependency Resolution

### The Problem

```
PKG_EMPLOYEE.create_employee() → calls PKG_PAYROLL.create_salary_record()
PKG_PAYROLL.calculate_employee_pay() → calls PKG_EMPLOYEE.get_employee()
```

In the legacy system, `PKG_EMPLOYEE` body references `PKG_PAYROLL` (lines 273-281 of `PKG_EMPLOYEE.pkb`), and `PKG_PAYROLL` body references `PKG_EMPLOYEE`. This circular dependency makes it impossible to migrate one without the other.

### Resolution Strategy: Dependency Inversion

Introduce a shared interface layer that breaks the cycle:

```
                    BEFORE                              AFTER

  ┌──────────────┐          ┌──────────────┐     ┌──────────────┐    ┌──────────────────┐
  │ PKG_EMPLOYEE │◀────────▶│ PKG_PAYROLL  │     │ PKG_EMPLOYEE │───▶│ SalaryService    │
  └──────────────┘          └──────────────┘     └──────────────┘    │ (interface)       │
                                                                     └────────▲──────────┘
                                                 ┌──────────────┐             │
                                                 │ PKG_PAYROLL  │─────────────┘
                                                 └──────────────┘
```

**In Java:**

```java
// Shared interface — no circular dependency
public interface SalaryService {
    SalaryRecord createSalaryRecord(Long empId, BigDecimal baseSalary, ...);
    SalaryRecord getCurrentSalary(Long empId);
}

// EmployeeService depends on SalaryService (interface), not PayrollService
@Service
public class EmployeeService {
    private final SalaryService salaryService;  // injected
}

// PayrollServiceImpl implements SalaryService
@Service
public class PayrollServiceImpl implements SalaryService {
    private final EmployeeRepository employeeRepository;  // reads employee data directly
}
```

**Migration implication:** The `SalaryService` interface must be defined in Phase 2 *before* either `EmployeeService` or `PayrollService` is fully implemented. During the coexistence period, a shim implementation can delegate to the legacy `PKG_PAYROLL` via a database call.

---

## 4. Migration Phase Plan

```
Phase 0       Phase 1         Phase 2a / 2b        Phase 3a / 3b / 3c      Phase 4
─────────────────────────────────────────────────────────────────────────────────────
Foundation    Auth &          Employee + Leave     Payroll +               Reporting +
              Shared Infra    (parallel possible)  Performance +           Cutover
                                                   Integration

Weeks 1-4     Weeks 5-10      Weeks 11-20          Weeks 21-32             Weeks 33-38

PKG_AUDIT     PKG_SECURITY    PKG_EMPLOYEE (2a)    PKG_PAYROLL (3a)        PKG_REPORTING
PKG_COMMON    PKG_VALIDATION  PKG_LEAVE (2b)       PKG_PERFORMANCE (3b)    Decommission
              PKG_NOTIFICATION SalaryService I/F    PKG_INTEGRATION (3c)    Legacy Forms
              HRMS_LOGIN      HRMS_EMPLOYEE                                Data migration
              HRMS_MENU       HRMS_LEAVE            HRMS_PAYROLL           final cutover
                                                    HRMS_PERFORMANCE
```

---

## 5. Detailed Phase Breakdown

### Phase 0: Foundation (Weeks 1–4)

**Objective:** Establish the new technology stack, CI/CD pipeline, and migrate base utility packages.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_AUDIT` | 33 lines (spec) | `AuditService` + Spring AOP | 1 week | Base package; no dependencies. Replace with Spring Events + AOP `@Around` advice |
| `PKG_COMMON` | Full utility package | `CommonService` + `DateUtils` | 1 week | Shared utilities; needed by everything. Straight port with Java idioms |
| CI/CD Pipeline | N/A | GitHub Actions / Jenkins | 1 week | Build, test, deploy pipeline for new services |
| Database migration | Oracle DDL scripts | Flyway/Liquibase scripts | 1 week | Version-control schema; dual-write adapter for coexistence |
| API Gateway | N/A | Spring Cloud Gateway / Kong | Included | Route requests to legacy Forms or new services based on module |

**Exit Criteria:**
- [ ] `AuditService` passes unit tests
- [ ] `CommonService` passes unit tests matching legacy PKG_COMMON behavior
- [ ] CI/CD pipeline deploys to staging
- [ ] API gateway routes to both legacy WebLogic and new Spring Boot

---

### Phase 1: Authentication & Shared Infrastructure (Weeks 5–10)

**Objective:** Replace the authentication layer and establish shared services. This is the prerequisite for all subsequent phases.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_SECURITY` | 238 lines (body) | `AuthService` + Spring Security + OAuth 2.0 | 2 weeks | **Must be first** — all modules depend on auth. Replace MD5 with BCrypt. Add account lockout. Remove hard-coded encryption key. |
| `PKG_VALIDATION` | 48 lines (spec) | `ValidationService` + Jakarta Bean Validation | 1 week | Unify client-side PLL and server-side validation |
| `PKG_NOTIFICATION` | 43 lines (spec) | `NotificationService` + Spring Mail | 1 week | Replace hard-coded SMTP; add rate limiting |
| `HRMS_LOGIN.fmb` | 131 lines (XML) | React login page | 1 week | JWT-based; replaced login form |
| `HRMS_MENU.fmb` | 176 lines (XML) | React App Shell (`<Sidebar>`, `<TopNav>`) | 1 week | Role-based navigation from `has_permission` API |
| `HRMS_COMMON_LIB.pll` | 152 lines | `src/shared/hooks/`, `src/shared/utils/` | Included | Client-side utilities become React hooks/utils |
| `HRMS_VALIDATION_LIB.pll` | 136 lines | `src/shared/validation/` | Included | Single validation source eliminates drift |

**Dependencies:** Phase 0 complete.

**Coexistence Mechanism:**
- New auth service issues JWT tokens
- Legacy forms receive a session shim: on JWT login, a corresponding `USER_SESSIONS` row is created so `PKG_SECURITY.is_session_valid()` still works for un-migrated forms
- API gateway checks JWT for new services, passes through to WebLogic for legacy forms

**Exit Criteria:**
- [ ] JWT-based login functional with BCrypt password hashing
- [ ] Account lockout after 5 failed attempts
- [ ] Legacy forms still work via session shim
- [ ] Notification service sends email via configurable SMTP
- [ ] React app shell renders with role-based sidebar

---

### Phase 2a: Employee Management (Weeks 11–16)

**Objective:** Migrate the largest and most complex form. This unlocks all downstream modules.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_EMPLOYEE` | 967 lines (body) | `EmployeeService` + `EmployeeController` | 3 weeks | Fix race condition in `generate_emp_number`. Fix SQL injection in `search_employees`. Resolve circular dep via `SalaryService` interface. |
| `SalaryService` interface | (extracted) | `SalaryService.java` (interface) | Included | Dependency inversion — shared interface for Employee/Payroll |
| `SalaryService` shim | N/A | `LegacySalaryServiceImpl` | Included | Temporary impl that calls `PKG_PAYROLL` via JDBC during coexistence |
| `HRMS_EMPLOYEE.fmb` | 539 lines (XML) | React Employee module (4 tabs, search, detail) | 3 weeks | 5 data blocks → components; 8 LOVs → Autocomplete endpoints; master-detail → parent/child routes |
| Employee DB triggers | `trg_employees.sql` (131 lines) | JPA lifecycle callbacks + `SoftDeleteRepository` | Included | `TRG_EMP_BEFORE_INSERT` → `@PrePersist`; `TRG_EMP_INSTEAD_OF_DELETE` → soft delete pattern |

**Dependencies:** Phase 1 complete (auth, validation, notification).

**Coexistence Mechanism:**
- New Employee React UI is available alongside legacy form
- Both write to the same `EMPLOYEES` table
- API gateway routes `/employees` to new service; legacy form URL unchanged
- Dual-write is safe because triggers still fire on direct DB writes

**Exit Criteria:**
- [ ] Employee CRUD via REST API + React UI
- [ ] All 8 LOVs replaced with API-backed dropdowns
- [ ] `generate_emp_number` race condition eliminated
- [ ] `search_employees` SQL injection eliminated
- [ ] Master-detail Employee → Salary displays correctly
- [ ] Lifecycle operations (transfer, promote, terminate, rehire) functional

---

### Phase 2b: Leave Management (Weeks 11–16, parallel with 2a)

**Objective:** Migrate the self-contained leave module. Can proceed in parallel with Employee since it only reads employee data.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_LEAVE` | 129 lines (spec) + body | `LeaveService` | 2 weeks | Fix half-day overlap detection. Fix carryover double-expiry. Improve holiday detection. |
| `HRMS_LEAVE.fmb` | 220 lines (XML) | React Leave module (4 tabs) | 2 weeks | Self-service portal; approval workflow; team calendar |
| Leave accrual batch | `run_monthly_accrual`, `process_carryover` | Spring Batch jobs | Included | Replace DBMS_SCHEDULER with Spring `@Scheduled` + Batch |

**Dependencies:** Phase 1 complete. Reads `PKG_EMPLOYEE` data (can use existing legacy package during coexistence or the new `EmployeeService` if Phase 2a is complete).

**Coexistence Mechanism:**
- New leave portal coexists with legacy form
- Same `LEAVE_REQUESTS`, `LEAVE_BALANCES` tables
- Approval notifications sent via new `NotificationService`

**Exit Criteria:**
- [ ] Leave request submission, approval, cancellation via React UI
- [ ] Half-day overlap detection fixed
- [ ] Carryover double-expiry fixed
- [ ] Monthly accrual batch runs via Spring Batch
- [ ] Team calendar functional

---

### Phase 3a: Payroll Processing (Weeks 21–28)

**Objective:** Big-bang rewrite of the payroll calculation engine. This is the highest-risk phase.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_PAYROLL` | 898 lines (body) | `PayrollService` + `TaxCalculationService` + Spring Batch | 4 weeks | Externalize tax brackets. Replace cursor loops with chunk processing. Fix partial commits. Fix overtime/holiday calculation. |
| `SalaryService` real impl | `LegacySalaryServiceImpl` (shim) | `PayrollSalaryServiceImpl` | Included | Replace the Phase 2a shim with real implementation |
| `HRMS_PAYROLL.fmb` | 167 lines (XML) | React Payroll module (3 tabs) | 2 weeks | Period management, run creation/calculation/approval, detail view |
| Tax bracket config | Hard-coded constants | `TAX_BRACKETS` table + `@ConfigurationProperties` | Included | Year-agnostic; configurable per tax year |

**Dependencies:** Phase 2a complete (Employee service must be operational because Payroll depends on employee data).

**Validation Strategy:**
- **3 months of parallel payroll runs** (new system calculates alongside legacy)
- Compare output cent-by-cent: gross pay, each deduction, net pay
- Discrepancies logged and investigated before cutover
- Phased cutover: run one department on new system first, then expand

**Exit Criteria:**
- [ ] Payroll calculation produces identical results to legacy for 3 consecutive pay periods
- [ ] Tax brackets externalized and configurable
- [ ] Bulk processing replaces cursor loops (10x throughput improvement target)
- [ ] Transaction boundaries correct (all-or-nothing per payroll run)
- [ ] `SalaryService` shim replaced with real implementation

---

### Phase 3b: Performance Reviews (Weeks 21–26, parallel with 3a)

**Objective:** Migrate the self-contained performance module.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_PERFORMANCE` | 98 lines (spec) + body | `PerformanceService` | 2 weeks | Straightforward port; add rich text for assessments |
| `HRMS_PERFORMANCE.fmb` | 132 lines (XML) | React Performance module (3 tabs) | 2 weeks | Cycle → Review → Goal nested UI |

**Dependencies:** Phase 2a complete (reads employee data).

**Exit Criteria:**
- [ ] Review cycle management functional
- [ ] Self-assessment and manager review submission
- [ ] Goal tracking with progress visualization
- [ ] Rating display and calibration view

---

### Phase 3c: External Integrations (Weeks 25–32, starts after 3a begins)

**Objective:** Replace flat-file integrations with API-based integrations.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_INTEGRATION` | 51 lines (spec) + body | `IntegrationService` + Spring Integration | 3 weeks | Replace UTL_FILE with REST/SFTP. Replace cleartext FTP creds with secrets manager. Add retry logic. |
| GL journal export | Flat file via UTL_FILE | REST API to GL system (or SFTP with encryption) | 1 week | Coordinate with finance team |
| Benefits feed (ADP) | ADP-specific flat file format | ADP API or configurable format adapter | 1 week | Abstract vendor format |
| Time & attendance import | File import | REST API from T&A system | 1 week | Coordinate with T&A vendor |

**Dependencies:** Phase 3a in progress or complete (integration exports payroll data).

**Exit Criteria:**
- [ ] GL journal export via API or encrypted SFTP
- [ ] Benefits feed via API or configurable format
- [ ] Time & attendance import via REST API
- [ ] Credentials in secrets manager (not cleartext DB)
- [ ] Retry logic with exponential backoff on all integrations

---

### Phase 4: Reporting & Cutover (Weeks 33–38)

**Objective:** Migrate reporting (last, because it reads from all other modules) and decommission legacy.

| Component | Source | Target | Effort | Notes |
|---|---|---|---|---|
| `PKG_REPORTING` | 64 lines (spec) + body | `ReportService` + JasperReports / embedded analytics | 3 weeks | Replace denormalized tables with real-time queries or fast-refresh materialized views. Externalize fiscal year config. |
| Legacy decommission | All Forms + WebLogic | Remove API gateway legacy routes | 1 week | After all users migrated |
| Data migration validation | N/A | Verification scripts | 1 week | Row counts, checksum validation, reconciliation reports |
| User acceptance testing | N/A | UAT environment | 1 week | Full regression by business users |

**Dependencies:** All previous phases complete.

**Exit Criteria:**
- [ ] All 7 reports functional in new system
- [ ] Real-time data (no stale denormalized tables)
- [ ] Fiscal year configuration externalized
- [ ] Legacy Forms decommissioned
- [ ] WebLogic server powered down
- [ ] All users on new React application

---

## 6. Parallel Migration Opportunities

Based on the dependency analysis, the following phases can be executed in parallel:

```
Sequential │ Phase 0 ──▶ Phase 1 ──┬──▶ Phase 3a (Payroll) ──┬──▶ Phase 4
           │                       │                          │
Parallel   │                       ├──▶ Phase 2a (Employee)   │
           │                       │     (required by 3a)     │
           │                       │                          │
           │                       ├──▶ Phase 2b (Leave)      │
           │                       │     (parallel with 2a)   │
           │                       │                          │
           │                       ├──▶ Phase 3b (Performance) │
           │                       │     (parallel with 3a)   │
           │                       │                          │
           │                       └──▶ Phase 3c (Integration) ┘
           │                            (starts mid-3a)
```

### Team Allocation for Parallel Execution

| Team | Phase 2 (Weeks 11–20) | Phase 3 (Weeks 21–32) |
|---|---|---|
| **Team A** (3 developers) | Employee Management (2a) | Payroll Processing (3a) |
| **Team B** (2 developers) | Leave Management (2b) | Performance Reviews (3b) + Integrations (3c) |

With two teams, the **critical path reduces from 38 weeks to ~32 weeks**.

---

## 7. Rollback Strategy per Phase

| Phase | Rollback Mechanism | Max Rollback Time |
|---|---|---|
| Phase 0 (Foundation) | Remove new services from API gateway; legacy untouched | Immediate |
| Phase 1 (Auth) | Revert API gateway routing to legacy login; session shim bidirectional | 1 hour |
| Phase 2a (Employee) | Route `/employees` back to legacy form via API gateway | 1 hour |
| Phase 2b (Leave) | Route `/leave` back to legacy form | 1 hour |
| Phase 3a (Payroll) | Switch payroll runs back to legacy `PKG_PAYROLL`; both systems share same DB | 2 hours |
| Phase 3b (Performance) | Route `/performance` back to legacy form | 1 hour |
| Phase 3c (Integration) | Revert to flat-file integration scripts; re-enable FTP jobs | 4 hours |
| Phase 4 (Reporting + Cutover) | Re-enable legacy WebLogic; restore API gateway routes | 4 hours (requires WebLogic restart) |

**Key principle:** During coexistence, both systems share the same Oracle database. The API gateway is the single routing control point. Rollback = change routing rules.

---

## 8. Migration Decision Checklist

Before proceeding from one phase to the next, verify:

- [ ] All exit criteria for the current phase are met
- [ ] Regression tests pass for migrated modules
- [ ] Performance benchmarks meet or exceed legacy system
- [ ] User acceptance testing sign-off obtained
- [ ] Rollback procedure tested and documented
- [ ] Monitoring and alerting configured for new services
- [ ] Legacy module marked as deprecated (not yet decommissioned)
- [ ] Data integrity verified between old and new systems
