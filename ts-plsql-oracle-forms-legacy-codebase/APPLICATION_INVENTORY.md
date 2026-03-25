# APPLICATION INVENTORY

## HRMS Oracle Forms / PL/SQL Estate

> **System**: Human Resources Management System (HRMS)
> **Platform**: Oracle Forms 12c + Oracle Database 19c + Oracle WebLogic
> **Schema**: `HRMS`
> **Estimated Size**: ~7 000 lines SQL/PL/SQL, ~1 400 lines Forms XML, 40+ source files
> **Concurrent Users**: ~200 across 3 regional offices

---

## 1. Layer Architecture Overview

| Layer | Technology | Artifact Count |
|-------|-----------|---------------|
| **Presentation** | Oracle Forms 12c XML exports | 6 forms |
| **Shared UI Logic** | PL/SQL Libraries (PLL) | 2 libraries |
| **Navigation** | Menu Module (MMB) | 1 menu |
| **Business Logic** | PL/SQL Packages | 11 packages (22 files) |
| **Enforcement** | Database Triggers | 6 triggers (2 files) |
| **Data Access** | Views | 6 views |
| **Schema** | Tables, Sequences | 42 tables, 25 sequences |
| **Seed Data** | SQL Insert Scripts | 2 scripts |

---

## 2. Forms XML Exports (Presentation Layer)

Located in `forms/xml-exports/`

| # | File | Form Name | Purpose | Lines | Data Blocks | LOVs | Tab Pages | Attached Libraries |
|---|------|-----------|---------|-------|-------------|------|-----------|-------------------|
| 1 | `HRMS_LOGIN.xml` | HRMS_LOGIN | Authentication / session creation | 131 | 1 (LOGIN) | 0 | 0 | HRMS_COMMON_LIB |
| 2 | `HRMS_MENU.xml` | HRMS_MENU | MDI parent, module navigation | 176 | 1 (NAVIGATION) | 0 | 0 | HRMS_COMMON_LIB |
| 3 | `HRMS_EMPLOYEE.xml` | HRMS_EMPLOYEE | Employee master-detail maintenance | 539 | 5 (EMPLOYEE, SALARY, DEPENDENTS, EMERGENCY_CONTACTS, EMP_HISTORY) | 8 (Department, Job Title, Manager, Location, Status, Gender, Marital, Country) | 4 (Personal, Job & Compensation, Dependents, History) | HRMS_COMMON_LIB, HRMS_VALIDATION_LIB |
| 4 | `HRMS_PAYROLL.xml` | HRMS_PAYROLL | Payroll processing | 167 | 4 (PAY_PERIOD, PAYROLL_RUN, PAYROLL_DETAIL, PAYSLIP_SUMMARY) | 3 (Period, Run Type, Employee) | 3 (Pay Periods, Payroll Runs, Pay Details) | HRMS_COMMON_LIB |
| 5 | `HRMS_LEAVE.xml` | HRMS_LEAVE | Leave request & approval | 220 | 5 (LEAVE_REQUEST, NEW_REQUEST, LEAVE_BALANCE, PENDING_APPROVAL, TEAM_CAL) | 3 (Leave Type, Employee, Half-Day Period) | 4 (My Requests, Submit Request, Approvals, Team Calendar) | HRMS_COMMON_LIB |
| 6 | `HRMS_PERFORMANCE.xml` | HRMS_PERFORMANCE | Performance reviews & goals | 132 | 4 (REVIEW_CYCLE, PERFORMANCE_REVIEW, PERFORMANCE_GOAL, REVIEW_DETAIL) | 0 | 3 (Review Cycles, My Reviews, Goals) | HRMS_COMMON_LIB |

### Common Form Triggers

All forms implement the following trigger pattern:

| Trigger | Purpose |
|---------|---------|
| `WHEN-NEW-FORM-INSTANCE` | Session validation (`PKG_SECURITY.is_session_valid`), permission check, LOV initialization |
| `ON-ERROR` | Centralized error handling via `HRMS_COMMON_LIB.handle_error` |
| `KEY-EXIT` | Controlled form exit / logout |
| `PRE-INSERT` | Primary key generation via sequences |
| `PRE-UPDATE` | Audit column population (`MODIFIED_BY`, `MODIFIED_DATE`) |
| `POST-QUERY` | Populate display-only fields, format data |
| `WHEN-VALIDATE-ITEM` | Field-level client-side validation |

---

## 3. PL/SQL Libraries (Shared UI Logic Layer)

Located in `forms/libraries/`

| # | File | Library Name | Lines | Functions/Procedures | Purpose |
|---|------|-------------|-------|---------------------|---------|
| 1 | `HRMS_COMMON_LIB.pll.sql` | HRMS_COMMON_LIB | 152 | 6: `handle_error`, `toolbar_save`, `toolbar_query`, `format_date`, `get_current_user`, `check_session`, `refresh_lov` | Shared error handling, toolbar actions, date formatting, session management, LOV refresh |
| 2 | `HRMS_VALIDATION_LIB.pll.sql` | HRMS_VALIDATION_LIB | 136 | 5: `validate_email`, `validate_phone`, `validate_ssn`, `validate_date_not_future`, `validate_salary_range` | Client-side field validation (attached to HRMS_EMPLOYEE) |

---

## 4. Menu Module

Located in `forms/menus/`

| # | File | Module Name | Lines | Menu Items |
|---|------|-------------|-------|------------|
| 1 | `HRMS_MENU.mmb.sql` | HRMS_MENU | 61 | File (Save, Exit), Edit (Cut, Copy, Paste), Query (Enter, Execute, Cancel), Navigate (First/Prev/Next/Last), Modules (Employees, Payroll, Leave, Performance, Reports), Admin (Users, Parameters), Help (Contents, About) |

---

## 5. PL/SQL Packages (Business Logic Layer)

Located in `plsql/packages/`

### 5.1 Package Summary

| # | Package | Spec (.pks) | Body (.pkb) | Layer | Dependencies | Called By |
|---|---------|-------------|-------------|-------|-------------|----------|
| 1 | `PKG_COMMON` | 122 lines | 284 lines | Infrastructure | None (base) | All packages |
| 2 | `PKG_AUDIT` | 33 lines | 73 lines | Infrastructure | None (base) | All packages |
| 3 | `PKG_VALIDATION` | 48 lines | 126 lines | Business Rules | PKG_COMMON | Forms, PKG_EMPLOYEE, PKG_PAYROLL |
| 4 | `PKG_NOTIFICATION` | 43 lines | 178 lines | Infrastructure | PKG_COMMON | PKG_EMPLOYEE, PKG_LEAVE, PKG_PAYROLL, PKG_PERFORMANCE |
| 5 | `PKG_SECURITY` | 64 lines | 238 lines | Security | PKG_COMMON, PKG_AUDIT | All forms, PKG_EMPLOYEE |
| 6 | `PKG_EMPLOYEE` | 193 lines | 967 lines | Core Business | PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION, **PKG_PAYROLL** | Forms, PKG_LEAVE, PKG_PERFORMANCE, PKG_REPORTING, PKG_INTEGRATION |
| 7 | `PKG_PAYROLL` | 165 lines | 898 lines | Core Business | **PKG_EMPLOYEE**, PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION | Forms, PKG_REPORTING, PKG_INTEGRATION |
| 8 | `PKG_LEAVE` | 129 lines | 674 lines | Core Business | PKG_EMPLOYEE, PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION | HRMS_LEAVE form, batch scheduler |
| 9 | `PKG_PERFORMANCE` | 98 lines | 321 lines | Core Business | PKG_EMPLOYEE, PKG_COMMON, PKG_AUDIT, PKG_NOTIFICATION | HRMS_PERFORMANCE form |
| 10 | `PKG_REPORTING` | 64 lines | 208 lines | Reporting | PKG_EMPLOYEE, PKG_PAYROLL, PKG_COMMON | HRMS_REPORTS form, Oracle Reports, batch |
| 11 | `PKG_INTEGRATION` | 51 lines | 214 lines | Integration | PKG_COMMON, PKG_PAYROLL, PKG_EMPLOYEE | Batch scheduler (nightly GL, weekly benefits) |

### 5.2 Package Detail

#### PKG_COMMON (Infrastructure)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `log_error` | Procedure | Autonomous-transaction error logging |
| `log_info` | Procedure | Autonomous-transaction info logging |
| `get_param` | Function | Read SYSTEM_PARAMETERS value |
| `get_param_number` | Function | Read numeric parameter |
| `get_param_date` | Function | Read date parameter |
| `set_param` | Procedure | Write SYSTEM_PARAMETERS value |
| `business_days_between` | Function | Count business days between dates |
| `add_business_days` | Function | Add N business days to date |
| `get_fiscal_year` | Function | Return fiscal year (Oct 1 start) |
| `get_fiscal_quarter` | Function | Return fiscal quarter |
| `format_phone` | Function | Format phone number display |
| `format_ssn_masked` | Function | Mask SSN (XXX-XX-1234) |
| `format_currency` | Function | Format number as currency |
| `format_name` | Function | Format name display |
| `is_valid_email` | Function | Email regex validation |
| `is_valid_phone` | Function | Phone format validation |
| `is_valid_ssn` | Function | SSN format validation |

#### PKG_AUDIT (Infrastructure)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `log_action` | Procedure | Centralized DML audit logging (autonomous transaction) |
| `purge_old_records` | Procedure | Delete audit records older than N days |
| `get_change_history` | Function | Retrieve change history for a record |

#### PKG_SECURITY (Security)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `authenticate` | Function | Login, password verification, session creation |
| `logout` | Procedure | Session termination |
| `is_session_valid` | Function | Check session status and timeout (30 min) |
| `has_permission` | Function | Grade-based RBAC check |
| `encrypt_ssn` | Function | AES-256 SSN encryption |
| `decrypt_ssn` | Function | AES-256 SSN decryption |
| `hash_password` | Function | Password hashing (MD5 - vulnerability) |
| `change_password` | Procedure | Password update with complexity rules |

#### PKG_VALIDATION (Business Rules)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `validate_date_range` | Procedure | Ensure start < end |
| `validate_salary_for_grade` | Procedure | Salary within JOB_GRADES min/max |
| `validate_email_format` | Function | Delegates to PKG_COMMON.is_valid_email |
| `validate_phone_format` | Function | Delegates to PKG_COMMON.is_valid_phone |
| `validate_emp_number_format` | Function | Regex `^EMP-\d{6}$` |
| `is_future_date` | Function | Date > SYSDATE check |
| `is_business_day` | Function | Checks weekends + HOLIDAYS table |
| `validate_required_fields` | Procedure | NOT NULL column check for EMPLOYEES |

#### PKG_NOTIFICATION (Infrastructure)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `send_notification` | Procedure | Queue notification (autonomous transaction) |
| `process_queue` | Procedure | Send pending notifications via UTL_SMTP |
| `retry_failed` | Procedure | Retry failed notifications |
| `cancel_notification` | Procedure | Cancel pending notification |

#### PKG_EMPLOYEE (Core Business)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `create_employee` | Function | Full employee creation with salary, notifications |
| `update_employee` | Procedure | Partial update (NVL pattern) |
| `get_employee` | Function | Fetch employee record by ID |
| `get_employee_by_number` | Function | Fetch employee by EMP_NUMBER |
| `search_employees` | Procedure | Dynamic search with cursor (SQL injection risk) |
| `transfer_employee` | Procedure | Department/job/location transfer |
| `promote_employee` | Procedure | Job change with salary update |
| `terminate_employee` | Procedure | Status change, auto-cancel leave, end salary |
| `rehire_employee` | Procedure | Reactivate terminated employee |
| `get_direct_reports` | Function | List direct reports for manager |
| `get_org_chart` | Function | Recursive org hierarchy (CONNECT BY) |
| `get_headcount_by_dept` | Function | Department headcount |
| `get_tenure_years` | Function | Calculate employee tenure |
| `is_active` | Function | Check active employment status |
| `validate_employee` | Function | Basic field validation |
| `emp_exists` | Function | Existence check |
| `generate_emp_number` | Function | Generate EMP-NNNNNN (race condition) |
| `set_session_context` | Procedure | Set global session variables |

#### PKG_PAYROLL (Core Business)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `create_salary_record` | Procedure | New salary with end-dating of current |
| `get_current_salary` | Function | Active salary lookup |
| `get_salary_as_of` | Function | Point-in-time salary |
| `create_pay_periods` | Procedure | Generate monthly/biweekly periods for year |
| `close_pay_period` | Procedure | Close period (FOR UPDATE) |
| `get_current_period` | Function | Current open period |
| `create_payroll_run` | Function | Create payroll run record |
| `calculate_payroll` | Procedure | Process all active employees (cursor loop) |
| `calculate_employee_pay` | Procedure | Single employee pay calculation |
| `approve_payroll` | Procedure | Approve calculated run |
| `reverse_payroll` | Procedure | Reverse a run |
| `calculate_federal_tax` | Function | Progressive bracket calculation (hard-coded 2024) |
| `calculate_state_tax` | Function | Flat rate by state (simplified) |
| `calculate_fica` | Function | Social Security with wage base cap |
| `calculate_medicare` | Function | Medicare with additional high-earner tax |
| `get_payslip` | Procedure | Payslip cursor for run/employee |
| `get_ytd_earnings` | Function | Year-to-date gross earnings |
| `generate_pay_register` | Procedure | CSV file via UTL_FILE |

#### PKG_LEAVE (Core Business)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `submit_leave_request` | Function | Request with balance/overlap/tenure checks |
| `approve_leave_request` | Procedure | Approve with balance transfer (FOR UPDATE) |
| `reject_leave_request` | Procedure | Reject with balance release |
| `cancel_leave_request` | Procedure | Cancel with balance restore |
| `get_leave_balance` | Function | Available balance calculation |
| `adjust_leave_balance` | Procedure | Manual balance adjustment |
| `initialize_balances` | Procedure | Create balance records for year |
| `run_monthly_accrual` | Procedure | Batch accrual for all employees |
| `process_carryover` | Procedure | Year-end carryover processing |
| `expire_carryover` | Procedure | Remove expired carryover (double-expire bug) |
| `get_pending_requests` | Procedure | Cursor for manager's pending approvals |
| `get_team_calendar` | Procedure | Cursor for team leave calendar |
| `calculate_business_days` | Function | Business days excluding holidays |
| `check_leave_overlap` | Function | Overlapping leave detection |

#### PKG_PERFORMANCE (Core Business)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `create_review_cycle` | Function | New annual review cycle |
| `open_review_cycle` | Procedure | Transition DRAFT to OPEN |
| `close_review_cycle` | Procedure | Close cycle |
| `create_review` | Function | Initiate review for employee |
| `submit_self_assessment` | Procedure | Employee self-review submission |
| `submit_manager_review` | Procedure | Manager rating (1.0-5.0) with labels |
| `acknowledge_review` | Procedure | Employee acknowledgement |
| `add_goal` | Function | Add goal to review |
| `update_goal_progress` | Procedure | Update goal progress/status |
| `get_team_reviews` | Procedure | Cursor for manager's team reviews |
| `get_rating_distribution` | Function | Rating distribution by cycle/dept |
| `generate_reviews_for_cycle` | Procedure | Batch-create reviews for all active employees |

#### PKG_REPORTING (Reporting)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `headcount_report` | Procedure | Headcount by dept/location with demographics |
| `compensation_summary` | Procedure | Salary analysis with compa-ratio |
| `turnover_report` | Procedure | Termination analysis with voluntary/involuntary split |
| `new_hires_report` | Procedure | Recent hires listing |
| `leave_utilization_report` | Procedure | Leave balance utilization by dept |
| `payroll_summary_report` | Procedure | Payroll totals by department |
| `eeo_compliance_report` | Procedure | EEO gender distribution by job category |
| `refresh_reporting_tables` | Procedure | Nightly refresh of denormalized RPT_* tables |

#### PKG_INTEGRATION (Integration)

| Subprogram | Type | Description |
|-----------|------|-------------|
| `generate_gl_journal` | Procedure | GL journal flat file (pipe-delimited) via UTL_FILE |
| `export_benefits_feed` | Procedure | ADP-format fixed-width benefits file via UTL_FILE |
| `import_time_attendance` | Procedure | CSV time data import via UTL_FILE |
| `sync_org_structure` | Procedure | LDAP/AD org sync (placeholder) |
| `get_integration_status` | Function | Check integration status from SYSTEM_PARAMETERS |

---

## 6. Database Triggers (Enforcement Layer)

Located in `plsql/triggers/`

### File: `trg_employees.sql` (131 lines)

| # | Trigger | Table | Timing | Event | Purpose |
|---|---------|-------|--------|-------|---------|
| 1 | `TRG_EMP_BEFORE_INSERT` | EMPLOYEES | BEFORE | INSERT | Auto-generate EMP_ID (sequence), set CREATED_BY/DATE, default ACTIVE_FLAG |
| 2 | `TRG_EMP_BEFORE_UPDATE` | EMPLOYEES | BEFORE | UPDATE | Set MODIFIED_BY/DATE, track status changes |
| 3 | `TRG_EMP_INSTEAD_OF_DELETE` | EMPLOYEES | INSTEAD OF | DELETE | Soft delete: sets ACTIVE_FLAG='N' instead of physical delete |

### File: `trg_audit.sql` (85 lines)

| # | Trigger | Table | Timing | Event | Purpose |
|---|---------|-------|--------|-------|---------|
| 4 | `TRG_SALARY_AUDIT` | SALARY_RECORDS | AFTER | INSERT, UPDATE | Log salary changes to AUDIT_LOG |
| 5 | `TRG_LEAVE_REQUEST_AUDIT` | LEAVE_REQUESTS | AFTER | INSERT, UPDATE | Log leave request changes to AUDIT_LOG |
| 6 | `TRG_DEPARTMENT_AUDIT` | DEPARTMENTS | AFTER | UPDATE | Log department changes to AUDIT_LOG |

---

## 7. Database Views (Data Access Layer)

Located in `schema/views/hrms_views.sql` (160 lines)

| # | View | Base Tables | Purpose |
|---|------|-------------|---------|
| 1 | `VW_ACTIVE_EMPLOYEES` | EMPLOYEES, DEPARTMENTS, JOB_TITLES, JOB_GRADES, LOCATIONS | Active employee directory with department, job, grade, location details |
| 2 | `VW_ORG_HIERARCHY` | EMPLOYEES, DEPARTMENTS, JOB_TITLES | Hierarchical org chart using CONNECT BY PRIOR |
| 3 | `VW_EMPLOYEE_COMPENSATION` | EMPLOYEES, SALARY_RECORDS, JOB_TITLES, JOB_GRADES, DEPARTMENTS | Current compensation with compa-ratio calculation |
| 4 | `VW_LEAVE_SUMMARY` | LEAVE_BALANCES, LEAVE_TYPES, EMPLOYEES | Leave balance summary with available days |
| 5 | `VW_PAYROLL_LATEST` | PAYROLL_RUNS, PAY_PERIODS, PAYROLL_DETAILS, EMPLOYEES | Most recent payroll run details |
| 6 | `VW_PENDING_APPROVALS` | LEAVE_REQUESTS, EMPLOYEES, LEAVE_TYPES | Pending leave requests awaiting manager approval |

---

## 8. Schema Objects (Data Layer)

### 8.1 Tables (42 total)

Located in `schema/tables/`

#### Core Domain (`01_core_tables.sql` - 221 lines)

| # | Table | Columns | Key Constraints | Purpose |
|---|-------|---------|----------------|---------|
| 1 | `DEPARTMENTS` | DEPT_ID, DEPT_CODE, DEPT_NAME, COST_CENTER, PARENT_DEPT_ID, MANAGER_EMP_ID, LOCATION_CODE, ACTIVE_FLAG, audit cols | PK(DEPT_ID), FK(PARENT_DEPT_ID), FK(LOCATION_CODE) | Organizational units |
| 2 | `LOCATIONS` | LOCATION_CODE, LOCATION_NAME, ADDRESS_LINE1, CITY, STATE_PROVINCE, POSTAL_CODE, COUNTRY_CODE, PHONE, ACTIVE_FLAG, audit cols | PK(LOCATION_CODE) | Physical office locations |
| 3 | `JOB_GRADES` | GRADE_ID, GRADE_NAME, GRADE_LEVEL, MIN_SALARY, MAX_SALARY, ACTIVE_FLAG, audit cols | PK(GRADE_ID), CHK(MIN < MAX) | Salary bands |
| 4 | `JOB_TITLES` | JOB_ID, JOB_CODE, JOB_TITLE, GRADE_ID, EEO_CATEGORY, ACTIVE_FLAG, audit cols | PK(JOB_ID), FK(GRADE_ID) | Position definitions |
| 5 | `EMPLOYEES` | EMP_ID, EMP_NUMBER, FIRST_NAME, LAST_NAME, EMAIL, PHONE_WORK, PHONE_MOBILE, HIRE_DATE, TERMINATION_DATE, DEPT_ID, JOB_ID, MANAGER_EMP_ID, LOCATION_CODE, EMPLOYMENT_TYPE, EMPLOYMENT_STATUS, GENDER, DATE_OF_BIRTH, MARITAL_STATUS, SSN_ENCRYPTED, ADDRESS fields, ACTIVE_FLAG, audit cols | PK(EMP_ID), UQ(EMP_NUMBER), FK(DEPT_ID, JOB_ID, MANAGER_EMP_ID, LOCATION_CODE) | Central employee master |
| 6 | `EMPLOYEE_HISTORY` | HIST_ID, EMP_ID, CHANGE_TYPE, EFFECTIVE_DATE, OLD/NEW_DEPT_ID, OLD/NEW_JOB_ID, OLD/NEW_MANAGER_ID, OLD/NEW_SALARY, OLD/NEW_LOCATION, REASON_CODE, COMMENTS, audit cols | PK(HIST_ID), FK(EMP_ID) | Employee change tracking |
| 7 | `EMPLOYEE_DEPENDENTS` | DEPENDENT_ID, EMP_ID, FIRST_NAME, LAST_NAME, RELATIONSHIP, DATE_OF_BIRTH, GENDER, ACTIVE_FLAG, audit cols | PK(DEPENDENT_ID), FK(EMP_ID) | Employee dependents |
| 8 | `EMERGENCY_CONTACTS` | CONTACT_ID, EMP_ID, CONTACT_NAME, RELATIONSHIP, PHONE_PRIMARY, PHONE_SECONDARY, PRIORITY_ORDER, ACTIVE_FLAG, audit cols | PK(CONTACT_ID), FK(EMP_ID) | Emergency contact info |

#### Payroll Domain (`02_payroll_tables.sql` - 226 lines)

| # | Table | Purpose |
|---|-------|---------|
| 9 | `SALARY_RECORDS` | Salary history with effective dating and end-dating |
| 10 | `PAY_ELEMENTS` | Earning/deduction/benefit type definitions with GL account codes |
| 11 | `EMPLOYEE_PAY_ELEMENTS` | Employee-specific deduction/benefit assignments |
| 12 | `PAY_PERIODS` | Pay period definitions (monthly/biweekly) |
| 13 | `PAYROLL_RUNS` | Payroll run header with status workflow |
| 14 | `PAYROLL_DETAILS` | Line-item payroll calculations per employee per run |
| 15 | `TAX_BRACKETS` | Federal/state tax bracket definitions |
| 16 | `EMPLOYEE_TAX_INFO` | W-4 information (filing status, allowances) |
| 17 | `EMPLOYEE_BANK_ACCOUNTS` | Direct deposit bank account info |

#### Leave Domain (`03_leave_tables.sql` - 125 lines)

| # | Table | Purpose |
|---|-------|---------|
| 18 | `LEAVE_TYPES` | Leave type definitions with accrual rules |
| 19 | `LEAVE_BALANCES` | Per-employee per-year balance tracking (virtual column: AVAILABLE) |
| 20 | `LEAVE_REQUESTS` | Leave request with approval workflow |
| 21 | `LEAVE_ACCRUAL_LOG` | Monthly accrual history |
| 22 | `HOLIDAYS` | Company/location-specific holiday calendar |

#### Performance & System (`04_performance_tables.sql` - 183 lines)

| # | Table | Purpose |
|---|-------|---------|
| 23 | `REVIEW_CYCLES` | Annual review cycle definitions |
| 24 | `PERFORMANCE_REVIEWS` | Individual performance reviews with ratings |
| 25 | `PERFORMANCE_GOALS` | Goals linked to reviews with progress tracking |
| 26 | `AUDIT_LOG` | Centralized audit trail for all DML |
| 27 | `SYSTEM_PARAMETERS` | Key-value application configuration |
| 28 | `NOTIFICATION_QUEUE` | Async notification queue (email/SMS/in-app) |
| 29 | `USER_SESSIONS` | Active session tracking |
| 30 | `LOOKUP_VALUES` | Generic lookup/reference data |

### 8.2 Sequences (25 total)

Located in `schema/sequences/hrms_sequences.sql` (50 lines)

| Sequence | Table | Cache |
|----------|-------|-------|
| SEQ_EMPLOYEE | EMPLOYEES | 20 |
| SEQ_EMP_NUMBER | Employee number generation | **NOCACHE** (race condition risk) |
| SEQ_EMP_HISTORY | EMPLOYEE_HISTORY | 20 |
| SEQ_DEPENDENT | EMPLOYEE_DEPENDENTS | 10 |
| SEQ_EMERGENCY | EMERGENCY_CONTACTS | 10 |
| SEQ_DEPARTMENT | DEPARTMENTS | 10 |
| SEQ_JOB_TITLE | JOB_TITLES | 10 |
| SEQ_SALARY | SALARY_RECORDS | 20 |
| SEQ_PAY_ELEMENT | PAY_ELEMENTS | 10 |
| SEQ_EMP_PAY_ELEMENT | EMPLOYEE_PAY_ELEMENTS | 20 |
| SEQ_PAY_PERIOD | PAY_PERIODS | 10 |
| SEQ_PAYROLL_RUN | PAYROLL_RUNS | 10 |
| SEQ_PAYROLL_DETAIL | PAYROLL_DETAILS | 100 |
| SEQ_LEAVE_TYPE | LEAVE_TYPES | 5 |
| SEQ_LEAVE_BALANCE | LEAVE_BALANCES | 20 |
| SEQ_LEAVE_REQUEST | LEAVE_REQUESTS | 20 |
| SEQ_LEAVE_ACCRUAL | LEAVE_ACCRUAL_LOG | 50 |
| SEQ_HOLIDAY | HOLIDAYS | 5 |
| SEQ_REVIEW_CYCLE | REVIEW_CYCLES | 5 |
| SEQ_PERF_REVIEW | PERFORMANCE_REVIEWS | 20 |
| SEQ_PERF_GOAL | PERFORMANCE_GOALS | 20 |
| SEQ_AUDIT | AUDIT_LOG | 100 |
| SEQ_NOTIFICATION | NOTIFICATION_QUEUE | 50 |
| SEQ_SESSION | USER_SESSIONS | 20 |
| SEQ_TAX_BRACKET | TAX_BRACKETS | 10 |

---

## 9. Seed Data Scripts

Located in `data/seed/`

| # | File | Lines | Content |
|---|------|-------|---------|
| 1 | `01_reference_data.sql` | 204 | 3 locations, 10 job grades, 10 departments, 26 job titles, 6 leave types, 11 holidays, 8 pay elements, 7 tax bracket rows, 12 lookup values, 5 system parameters |
| 2 | `02_employee_data.sql` | 173 | 25 employees across all departments, 25 salary records |

---

## 10. Cross-Reference: Forms-to-Package Calls

| Form | Packages Called |
|------|---------------|
| HRMS_LOGIN | PKG_SECURITY (authenticate, is_session_valid) |
| HRMS_MENU | PKG_SECURITY (has_permission, logout, is_session_valid) |
| HRMS_EMPLOYEE | PKG_SECURITY (is_session_valid, has_permission), PKG_EMPLOYEE (generate_emp_number), PKG_VALIDATION (validate_email_format) |
| HRMS_PAYROLL | PKG_SECURITY (is_session_valid, has_permission), PKG_PAYROLL (create_payroll_run, calculate_payroll, approve_payroll) |
| HRMS_LEAVE | PKG_SECURITY (is_session_valid), PKG_LEAVE (submit_leave_request, cancel_leave_request) |
| HRMS_PERFORMANCE | PKG_SECURITY (is_session_valid) |
