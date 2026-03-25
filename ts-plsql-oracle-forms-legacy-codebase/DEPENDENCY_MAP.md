# DEPENDENCY MAP

## HRMS Multi-Layer Call Graph & Circular Dependency Analysis

> **System**: Human Resources Management System (HRMS)
> **Architecture**: Oracle Forms 12c → PLL Libraries → PL/SQL Packages → Database Objects
> **Total Artifacts**: 6 forms, 2 PLL libraries, 1 menu, 11 packages, 6 triggers, 6 views, 42 tables, 25 sequences

---

## Table of Contents

1. [Architecture Layers](#1-architecture-layers)
2. [Layer 1: Forms → PLL Libraries](#2-layer-1-forms--pll-libraries)
3. [Layer 2: Forms → PL/SQL Packages](#3-layer-2-forms--plsql-packages)
4. [Layer 3: PLL Libraries → PL/SQL Packages](#4-layer-3-pll-libraries--plsql-packages)
5. [Layer 4: Package → Package Dependencies](#5-layer-4-package--package-dependencies)
6. [Layer 5: Packages → Tables](#6-layer-5-packages--tables)
7. [Layer 6: Triggers → Tables & Packages](#7-layer-6-triggers--tables--packages)
8. [Layer 7: Views → Tables](#8-layer-7-views--tables)
9. [Circular Dependencies](#9-circular-dependencies)
10. [Full Dependency Matrix](#10-full-dependency-matrix)
11. [Topological Sort & Build Order](#11-topological-sort--build-order)
12. [Orphan & Dead Code Analysis](#12-orphan--dead-code-analysis)

---

## 1. Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐       │
│  │HRMS_LOGIN│ │HRMS_MENU │ │HRMS_EMP  │ │HRMS_PAY  │       │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘       │
│  ┌────┴─────┐ ┌────┴─────┐                                  │
│  │HRMS_LEAVE│ │HRMS_PERF │   ┌──────────────────┐           │
│  └────┬─────┘ └────┬─────┘   │  HRMS_MENU.mmb   │           │
│       │             │         └────────┬─────────┘           │
├───────┼─────────────┼──────────────────┼─────────────────────┤
│       │    SHARED UI LOGIC LAYER       │                     │
│       │  ┌─────────────────────────┐   │                     │
│       ├──┤  HRMS_COMMON_LIB.pll   ├───┘                     │
│       │  └─────────────────────────┘                         │
│       │  ┌─────────────────────────┐                         │
│       ├──┤ HRMS_VALIDATION_LIB.pll│  (HRMS_EMPLOYEE only)   │
│       │  └─────────────────────────┘                         │
├───────┼──────────────────────────────────────────────────────┤
│       │       BUSINESS LOGIC LAYER (PL/SQL Packages)         │
│       │                                                      │
│  ┌────┴──────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │PKG_SECURITY│ │PKG_EMPLOYEE│ │PKG_PAYROLL│ │PKG_LEAVE │   │
│  └────┬──────┘  └─────┬─────┘ └─────┬─────┘ └────┬─────┘   │
│       │          ┌────┴──────┐       │             │         │
│       │          │   ⟲ CIRCULAR ⟲   │             │         │
│       │          └───────────┘       │             │         │
│  ┌────┴────────┐ ┌──────────┐ ┌─────┴──────┐               │
│  │PKG_PERF    │ │PKG_REPORT│ │PKG_INTEGR  │                │
│  └────┬───────┘ └────┬─────┘ └────┬───────┘                │
│       │              │             │                         │
│  ┌────┴──────────────┴─────────────┴───────┐                │
│  │       INFRASTRUCTURE PACKAGES            │                │
│  │  PKG_COMMON  PKG_AUDIT  PKG_NOTIFICATION │                │
│  │  PKG_VALIDATION                          │                │
│  └──────────────────┬──────────────────────┘                │
├─────────────────────┼────────────────────────────────────────┤
│                ENFORCEMENT LAYER                             │
│  ┌──────────────────┴──────────────────┐                    │
│  │ TRG_EMP_*  TRG_SALARY_AUDIT         │                    │
│  │ TRG_LEAVE_REQUEST_AUDIT              │                    │
│  │ TRG_DEPARTMENT_AUDIT                 │                    │
│  └──────────────────┬──────────────────┘                    │
├─────────────────────┼────────────────────────────────────────┤
│              DATA ACCESS LAYER (Views)                       │
│  VW_ACTIVE_EMPLOYEES  VW_ORG_HIERARCHY                      │
│  VW_EMPLOYEE_COMPENSATION  VW_LEAVE_SUMMARY                 │
│  VW_PAYROLL_LATEST  VW_PENDING_APPROVALS                    │
├─────────────────────┼────────────────────────────────────────┤
│                  DATA LAYER                                  │
│  42 Tables  │  25 Sequences                                 │
└─────────────┴────────────────────────────────────────────────┘
```

---

## 2. Layer 1: Forms → PLL Libraries

Every form attaches `HRMS_COMMON_LIB`. Only `HRMS_EMPLOYEE` also attaches `HRMS_VALIDATION_LIB`.

| Form | HRMS_COMMON_LIB | HRMS_VALIDATION_LIB |
|------|:---------------:|:-------------------:|
| HRMS_LOGIN | Yes | — |
| HRMS_MENU | Yes | — |
| HRMS_EMPLOYEE | Yes | Yes |
| HRMS_PAYROLL | Yes | — |
| HRMS_LEAVE | Yes | — |
| HRMS_PERFORMANCE | Yes | — |

```
HRMS_LOGIN ──────────────┐
HRMS_MENU ───────────────┤
HRMS_PAYROLL ────────────┼──→ HRMS_COMMON_LIB
HRMS_LEAVE ──────────────┤
HRMS_PERFORMANCE ────────┘
                         
HRMS_EMPLOYEE ───────────┼──→ HRMS_COMMON_LIB
                         └──→ HRMS_VALIDATION_LIB
```

---

## 3. Layer 2: Forms → PL/SQL Packages

Direct package calls from form triggers and code blocks.

### HRMS_LOGIN
```
HRMS_LOGIN
  ├──→ PKG_SECURITY.authenticate
  └──→ PKG_SECURITY.is_session_valid
```

### HRMS_MENU
```
HRMS_MENU
  ├──→ PKG_SECURITY.is_session_valid
  ├──→ PKG_SECURITY.has_permission
  └──→ PKG_SECURITY.logout
```

### HRMS_EMPLOYEE
```
HRMS_EMPLOYEE
  ├──→ PKG_SECURITY.is_session_valid
  ├──→ PKG_SECURITY.has_permission
  ├──→ PKG_EMPLOYEE.generate_emp_number
  └──→ PKG_VALIDATION.validate_email_format
```

### HRMS_PAYROLL
```
HRMS_PAYROLL
  ├──→ PKG_SECURITY.is_session_valid
  ├──→ PKG_SECURITY.has_permission
  ├──→ PKG_PAYROLL.create_payroll_run
  ├──→ PKG_PAYROLL.calculate_payroll
  └──→ PKG_PAYROLL.approve_payroll
```

### HRMS_LEAVE
```
HRMS_LEAVE
  ├──→ PKG_SECURITY.is_session_valid
  ├──→ PKG_LEAVE.submit_leave_request
  └──→ PKG_LEAVE.cancel_leave_request
```

### HRMS_PERFORMANCE
```
HRMS_PERFORMANCE
  └──→ PKG_SECURITY.is_session_valid
```

### Summary Matrix

| Form | PKG_SECURITY | PKG_EMPLOYEE | PKG_PAYROLL | PKG_LEAVE | PKG_VALIDATION |
|------|:------------:|:------------:|:-----------:|:---------:|:--------------:|
| HRMS_LOGIN | authenticate, is_session_valid | — | — | — | — |
| HRMS_MENU | is_session_valid, has_permission, logout | — | — | — | — |
| HRMS_EMPLOYEE | is_session_valid, has_permission | generate_emp_number | — | — | validate_email_format |
| HRMS_PAYROLL | is_session_valid, has_permission | — | create_payroll_run, calculate_payroll, approve_payroll | — | — |
| HRMS_LEAVE | is_session_valid | — | — | submit_leave_request, cancel_leave_request | — |
| HRMS_PERFORMANCE | is_session_valid | — | — | — | — |

---

## 4. Layer 3: PLL Libraries → PL/SQL Packages

### HRMS_COMMON_LIB
```
HRMS_COMMON_LIB
  ├──→ PKG_SECURITY.is_session_valid     (via check_session)
  ├──→ PKG_SECURITY.get_current_user     (via get_current_user)
  └──→ (Built-in Forms APIs: MESSAGE, COMMIT_FORM, EXECUTE_QUERY, etc.)
```

### HRMS_VALIDATION_LIB
```
HRMS_VALIDATION_LIB
  └──→ (Client-side regex only — no direct package calls)
       NOTE: Validation logic DIVERGES from PKG_VALIDATION / PKG_COMMON
```

---

## 5. Layer 4: Package → Package Dependencies

### Dependency Graph

```
PKG_COMMON ←──────────────────────────────────── (base, no deps)
PKG_AUDIT ←───────────────────────────────────── (base, no deps)

PKG_VALIDATION ──────→ PKG_COMMON

PKG_NOTIFICATION ────→ PKG_COMMON

PKG_SECURITY ────────→ PKG_COMMON
                 ────→ PKG_AUDIT

PKG_EMPLOYEE ────────→ PKG_COMMON
                 ────→ PKG_AUDIT
                 ────→ PKG_NOTIFICATION
                 ────→ PKG_PAYROLL        ← ⚠ CIRCULAR
                 (calls PKG_SECURITY.has_permission indirectly via Forms)

PKG_PAYROLL ─────────→ PKG_COMMON
                 ────→ PKG_AUDIT
                 ────→ PKG_NOTIFICATION
                 ────→ PKG_EMPLOYEE       ← ⚠ CIRCULAR

PKG_LEAVE ───────────→ PKG_COMMON
                 ────→ PKG_AUDIT
                 ────→ PKG_NOTIFICATION
                 ────→ PKG_EMPLOYEE (reads EMPLOYEES table)

PKG_PERFORMANCE ─────→ PKG_COMMON
                 ────→ PKG_AUDIT
                 ────→ PKG_NOTIFICATION
                 ────→ PKG_EMPLOYEE (reads EMPLOYEES table)

PKG_REPORTING ───────→ PKG_COMMON
                 ────→ PKG_EMPLOYEE
                 ────→ PKG_PAYROLL

PKG_INTEGRATION ─────→ PKG_COMMON
                 ────→ PKG_PAYROLL
                 ────→ PKG_EMPLOYEE
```

### Adjacency List (directed: A → B means "A depends on B")

```
PKG_COMMON       → (none)
PKG_AUDIT        → (none)
PKG_VALIDATION   → PKG_COMMON
PKG_NOTIFICATION → PKG_COMMON
PKG_SECURITY     → PKG_COMMON, PKG_AUDIT
PKG_EMPLOYEE     → PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION, PKG_PAYROLL
PKG_PAYROLL      → PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION, PKG_EMPLOYEE
PKG_LEAVE        → PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION, PKG_EMPLOYEE
PKG_PERFORMANCE  → PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION, PKG_EMPLOYEE
PKG_REPORTING    → PKG_COMMON, PKG_EMPLOYEE, PKG_PAYROLL
PKG_INTEGRATION  → PKG_COMMON, PKG_PAYROLL, PKG_EMPLOYEE
```

### In-Degree / Out-Degree

| Package | In-Degree (depended on by) | Out-Degree (depends on) |
|---------|:-------------------------:|:-----------------------:|
| PKG_COMMON | 10 | 0 |
| PKG_AUDIT | 5 | 0 |
| PKG_NOTIFICATION | 5 | 1 |
| PKG_EMPLOYEE | 6 | 4 |
| PKG_PAYROLL | 4 | 4 |
| PKG_SECURITY | 1 (via forms) | 2 |
| PKG_VALIDATION | 1 (via forms) | 1 |
| PKG_LEAVE | 0 | 4 |
| PKG_PERFORMANCE | 0 | 4 |
| PKG_REPORTING | 0 | 3 |
| PKG_INTEGRATION | 0 | 3 |

---

## 6. Layer 5: Packages → Tables

### PKG_COMMON
| Operation | Table |
|-----------|-------|
| INSERT | AUDIT_LOG (via log_error, log_info) |
| SELECT | SYSTEM_PARAMETERS |
| UPDATE | SYSTEM_PARAMETERS (via set_param) |
| SELECT | HOLIDAYS (via business_days_between) |

### PKG_AUDIT
| Operation | Table |
|-----------|-------|
| INSERT | AUDIT_LOG |
| DELETE | AUDIT_LOG (via purge_old_records) |
| SELECT | AUDIT_LOG (via get_change_history) |

### PKG_SECURITY
| Operation | Table |
|-----------|-------|
| SELECT | EMPLOYEES |
| INSERT | USER_SESSIONS |
| SELECT/UPDATE | USER_SESSIONS |
| INSERT | AUDIT_LOG (via PKG_AUDIT) |

### PKG_VALIDATION
| Operation | Table |
|-----------|-------|
| SELECT | JOB_GRADES |
| SELECT | HOLIDAYS |
| SELECT | EMPLOYEES |

### PKG_NOTIFICATION
| Operation | Table |
|-----------|-------|
| INSERT | NOTIFICATION_QUEUE |
| SELECT/UPDATE | NOTIFICATION_QUEUE |

### PKG_EMPLOYEE
| Operation | Table |
|-----------|-------|
| SELECT/INSERT/UPDATE | EMPLOYEES |
| INSERT | EMPLOYEE_HISTORY |
| SELECT | DEPARTMENTS |
| SELECT | JOB_TITLES |
| SELECT | JOB_GRADES |
| SELECT | SALARY_RECORDS (via get_employee) |
| UPDATE | SALARY_RECORDS (via terminate_employee) |
| UPDATE | EMPLOYEE_PAY_ELEMENTS (via terminate_employee) |
| UPDATE | LEAVE_REQUESTS (via terminate_employee) |

### PKG_PAYROLL
| Operation | Table |
|-----------|-------|
| SELECT/INSERT/UPDATE | SALARY_RECORDS |
| SELECT/INSERT | PAY_PERIODS |
| SELECT/INSERT/UPDATE | PAYROLL_RUNS |
| SELECT/INSERT/UPDATE | PAYROLL_DETAILS |
| SELECT | EMPLOYEE_TAX_INFO |
| SELECT | EMPLOYEE_PAY_ELEMENTS |
| SELECT | PAY_ELEMENTS |
| SELECT | EMPLOYEES (via cursor) |

### PKG_LEAVE
| Operation | Table |
|-----------|-------|
| SELECT | EMPLOYEES |
| SELECT | LEAVE_TYPES |
| SELECT/INSERT/UPDATE | LEAVE_REQUESTS |
| SELECT/UPDATE/INSERT | LEAVE_BALANCES |
| INSERT | LEAVE_ACCRUAL_LOG |
| SELECT | HOLIDAYS |

### PKG_PERFORMANCE
| Operation | Table |
|-----------|-------|
| INSERT/UPDATE | REVIEW_CYCLES |
| SELECT/INSERT/UPDATE | PERFORMANCE_REVIEWS |
| INSERT/UPDATE | PERFORMANCE_GOALS |
| SELECT | EMPLOYEES |

### PKG_REPORTING
| Operation | Table |
|-----------|-------|
| SELECT | EMPLOYEES |
| SELECT | DEPARTMENTS |
| SELECT | LOCATIONS |
| SELECT | JOB_TITLES |
| SELECT | JOB_GRADES |
| SELECT | SALARY_RECORDS |
| SELECT | LEAVE_BALANCES |
| SELECT | LEAVE_TYPES |
| SELECT | PAYROLL_DETAILS |
| SELECT | PAYROLL_RUNS |
| SELECT | PAY_PERIODS |

### PKG_INTEGRATION
| Operation | Table |
|-----------|-------|
| SELECT | PAYROLL_DETAILS |
| SELECT | PAYROLL_RUNS |
| SELECT | PAY_PERIODS |
| SELECT | PAY_ELEMENTS |
| SELECT | EMPLOYEES |
| SELECT | DEPARTMENTS |
| SELECT | EMPLOYEE_DEPENDENTS |

### Table Reference Counts (how many packages reference each table)

| Table | Package Count | Packages |
|-------|:------------:|----------|
| EMPLOYEES | 9 | All except PKG_NOTIFICATION |
| AUDIT_LOG | 3 | PKG_COMMON, PKG_AUDIT, PKG_SECURITY |
| DEPARTMENTS | 4 | PKG_EMPLOYEE, PKG_REPORTING, PKG_INTEGRATION, PKG_LEAVE (indirect) |
| SALARY_RECORDS | 3 | PKG_EMPLOYEE, PKG_PAYROLL, PKG_REPORTING |
| PAYROLL_DETAILS | 3 | PKG_PAYROLL, PKG_REPORTING, PKG_INTEGRATION |
| LEAVE_BALANCES | 2 | PKG_LEAVE, PKG_REPORTING |
| LEAVE_REQUESTS | 2 | PKG_LEAVE, PKG_EMPLOYEE |
| NOTIFICATION_QUEUE | 1 | PKG_NOTIFICATION |
| USER_SESSIONS | 1 | PKG_SECURITY |
| SYSTEM_PARAMETERS | 1 | PKG_COMMON |
| REVIEW_CYCLES | 1 | PKG_PERFORMANCE |
| PERFORMANCE_REVIEWS | 1 | PKG_PERFORMANCE |
| PERFORMANCE_GOALS | 1 | PKG_PERFORMANCE |

---

## 7. Layer 6: Triggers → Tables & Packages

| Trigger | Fires On | Reads | Writes | Calls Package |
|---------|----------|-------|--------|--------------|
| TRG_EMP_BEFORE_INSERT | EMPLOYEES (BEFORE INSERT) | SEQ_EMPLOYEE | EMPLOYEES (:NEW) | — |
| TRG_EMP_BEFORE_UPDATE | EMPLOYEES (BEFORE UPDATE) | — | EMPLOYEES (:NEW) | — |
| TRG_EMP_INSTEAD_OF_DELETE | EMPLOYEES (INSTEAD OF DELETE) | — | EMPLOYEES (sets ACTIVE_FLAG='N') | — |
| TRG_SALARY_AUDIT | SALARY_RECORDS (AFTER INSERT/UPDATE) | :OLD, :NEW | AUDIT_LOG | — |
| TRG_LEAVE_REQUEST_AUDIT | LEAVE_REQUESTS (AFTER INSERT/UPDATE) | :OLD, :NEW | AUDIT_LOG | — |
| TRG_DEPARTMENT_AUDIT | DEPARTMENTS (AFTER UPDATE) | :OLD, :NEW | AUDIT_LOG | — |

**Note**: Triggers duplicate some audit logic also handled by PKG_AUDIT.log_action — a source of validation drift.

---

## 8. Layer 7: Views → Tables

| View | Source Tables |
|------|-------------|
| VW_ACTIVE_EMPLOYEES | EMPLOYEES, DEPARTMENTS, JOB_TITLES, JOB_GRADES, LOCATIONS |
| VW_ORG_HIERARCHY | EMPLOYEES, DEPARTMENTS, JOB_TITLES |
| VW_EMPLOYEE_COMPENSATION | EMPLOYEES, SALARY_RECORDS, JOB_TITLES, JOB_GRADES, DEPARTMENTS |
| VW_LEAVE_SUMMARY | LEAVE_BALANCES, LEAVE_TYPES, EMPLOYEES |
| VW_PAYROLL_LATEST | PAYROLL_RUNS, PAY_PERIODS, PAYROLL_DETAILS, EMPLOYEES |
| VW_PENDING_APPROVALS | LEAVE_REQUESTS, EMPLOYEES, LEAVE_TYPES |

---

## 9. Circular Dependencies

### 9.1 PKG_EMPLOYEE ↔ PKG_PAYROLL (CONFIRMED)

This is the **only circular dependency** in the codebase and is explicitly documented in both package headers.

**Forward Path (PKG_EMPLOYEE → PKG_PAYROLL)**:
- `PKG_EMPLOYEE.create_employee` → `PKG_PAYROLL.create_salary_record` (line 275)
- `PKG_EMPLOYEE.promote_employee` → `PKG_PAYROLL.create_salary_record` (line 617)
- `PKG_EMPLOYEE.rehire_employee` → `PKG_PAYROLL.create_salary_record` (line 778)

**Reverse Path (PKG_PAYROLL → PKG_EMPLOYEE)**:
- `PKG_PAYROLL.calculate_payroll` reads EMPLOYEES table (cursor loop, line 296)
- `PKG_PAYROLL.calculate_employee_pay` reads EMPLOYEES (indirectly via get_salary_as_of)
- `PKG_PAYROLL` declared dependency on `PKG_EMPLOYEE` in spec header (line 6)

**Impact**:
- Oracle resolves this at compile time because packages are compiled spec-first; bodies can reference other specs.
- Risk: If either package spec is invalidated, the other becomes invalid, causing cascading recompilation.
- This is the single largest technical debt item from an architecture perspective.

**Recommended Resolution**:
- Extract salary management into a separate `PKG_SALARY` package.
- `PKG_EMPLOYEE` → `PKG_SALARY` for salary creation.
- `PKG_PAYROLL` → `PKG_SALARY` for salary reads.
- Neither `PKG_EMPLOYEE` nor `PKG_PAYROLL` reference each other.

### 9.2 No Other Circular Dependencies

All remaining package dependencies form a directed acyclic graph (DAG) after removing the PKG_EMPLOYEE ↔ PKG_PAYROLL cycle.

```
Topological layers (excluding circular pair):

Layer 0 (bases):     PKG_COMMON, PKG_AUDIT
Layer 1:             PKG_VALIDATION, PKG_NOTIFICATION, PKG_SECURITY
Layer 2:             PKG_EMPLOYEE ⟲ PKG_PAYROLL  (circular — must compile specs first)
Layer 3:             PKG_LEAVE, PKG_PERFORMANCE
Layer 4:             PKG_REPORTING, PKG_INTEGRATION
```

---

## 10. Full Dependency Matrix

`D` = Direct dependency, `I` = Indirect dependency, `C` = Circular

| Depends On → | COMMON | AUDIT | VALID | NOTIF | SECURITY | EMPLOYEE | PAYROLL | LEAVE | PERF | REPORT | INTEG |
|-------------|:------:|:-----:|:-----:|:-----:|:--------:|:--------:|:-------:|:-----:|:----:|:------:|:-----:|
| **PKG_COMMON** | — | | | | | | | | | | |
| **PKG_AUDIT** | | — | | | | | | | | | |
| **PKG_VALIDATION** | D | | — | | | | | | | | |
| **PKG_NOTIFICATION** | D | | | — | | | | | | | |
| **PKG_SECURITY** | D | D | | | — | | | | | | |
| **PKG_EMPLOYEE** | D | D | | D | | — | **C** | | | | |
| **PKG_PAYROLL** | D | D | | D | | **C** | — | | | | |
| **PKG_LEAVE** | D | D | | D | | D | | — | | | |
| **PKG_PERFORMANCE** | D | D | | D | | D | | | — | | |
| **PKG_REPORTING** | D | | | | | D | D | | | — | |
| **PKG_INTEGRATION** | D | | | | | D | D | | | | — |

---

## 11. Topological Sort & Build Order

The correct compilation order for the HRMS schema:

```sql
-- Phase 1: Schema objects (no dependencies)
-- Tables: 01_core_tables.sql → 02_payroll_tables.sql → 03_leave_tables.sql → 04_performance_tables.sql
-- Sequences: hrms_sequences.sql
-- Views: hrms_views.sql

-- Phase 2: Package specs (order matters for forward references)
@plsql/packages/PKG_COMMON.pks
@plsql/packages/PKG_AUDIT.pks
@plsql/packages/PKG_VALIDATION.pks
@plsql/packages/PKG_NOTIFICATION.pks
@plsql/packages/PKG_SECURITY.pks
@plsql/packages/PKG_EMPLOYEE.pks       -- spec only (references PKG_PAYROLL spec)
@plsql/packages/PKG_PAYROLL.pks        -- spec only (references PKG_EMPLOYEE spec)
@plsql/packages/PKG_LEAVE.pks
@plsql/packages/PKG_PERFORMANCE.pks
@plsql/packages/PKG_REPORTING.pks
@plsql/packages/PKG_INTEGRATION.pks

-- Phase 3: Package bodies (order matters for circular deps)
@plsql/packages/PKG_COMMON.pkb
@plsql/packages/PKG_AUDIT.pkb
@plsql/packages/PKG_VALIDATION.pkb
@plsql/packages/PKG_NOTIFICATION.pkb
@plsql/packages/PKG_SECURITY.pkb
@plsql/packages/PKG_EMPLOYEE.pkb       -- body references PKG_PAYROLL.create_salary_record
@plsql/packages/PKG_PAYROLL.pkb        -- body references EMPLOYEES table
@plsql/packages/PKG_LEAVE.pkb
@plsql/packages/PKG_PERFORMANCE.pkb
@plsql/packages/PKG_REPORTING.pkb
@plsql/packages/PKG_INTEGRATION.pkb

-- Phase 4: Triggers
@plsql/triggers/trg_employees.sql
@plsql/triggers/trg_audit.sql

-- Phase 5: Seed data
@data/seed/01_reference_data.sql
@data/seed/02_employee_data.sql
```

---

## 12. Orphan & Dead Code Analysis

### Packages with No Form Callers

These packages are only called by other packages or batch jobs, never directly from a form:

| Package | Called By |
|---------|----------|
| PKG_COMMON | All other packages (infrastructure) |
| PKG_AUDIT | All business packages (infrastructure) |
| PKG_NOTIFICATION | PKG_EMPLOYEE, PKG_LEAVE, PKG_PAYROLL, PKG_PERFORMANCE |
| PKG_REPORTING | Referenced by HRMS_REPORTS form (not in XML exports) and batch jobs |
| PKG_INTEGRATION | Batch scheduler only |

### Forms Not in XML Exports

The menu module (`HRMS_MENU.mmb.sql`) references a **Reports** button (`BTN_REPORTS`), but there is no `HRMS_REPORTS.xml` in the codebase. This form is either:
- Not yet developed
- Stored as a binary `.fmb` only (not exported to XML)
- Replaced by Oracle Reports (`.rdf`) files

### Incomplete / Placeholder Code

| Location | Description |
|----------|-------------|
| `PKG_INTEGRATION.import_time_attendance` | Reads file but parsing is TODO — `v_imported` increments but no DB updates |
| `PKG_INTEGRATION.sync_org_structure` | Placeholder — logs message only |
| `PKG_EMPLOYEE.terminate_employee` | Three TODO comments for COBRA, access revocation, final pay |
| `PKG_REPORTING.refresh_reporting_tables` | Placeholder — logs message only |

### Duplicated Logic

| Logic | Location 1 | Location 2 | Drift Risk |
|-------|-----------|-----------|-----------|
| Email validation | `HRMS_VALIDATION_LIB.validate_email` (client, rejects subdomains) | `PKG_COMMON.is_valid_email` (server, accepts subdomains) | **HIGH** — Different regex patterns |
| Audit logging | `TRG_SALARY_AUDIT`, `TRG_LEAVE_REQUEST_AUDIT`, `TRG_DEPARTMENT_AUDIT` | `PKG_AUDIT.log_action` (called by all packages) | **MEDIUM** — Double audit entries |
| Employee number generation | `TRG_EMP_BEFORE_INSERT` (sets EMP_ID from sequence) | `PKG_EMPLOYEE.generate_emp_number` (MAX+1 pattern) | **HIGH** — Different strategies for ID vs. number |
| Business day calculation | `PKG_COMMON.business_days_between` | `PKG_LEAVE.calculate_business_days` | **LOW** — Similar logic, PKG_LEAVE adds location-aware holidays |
