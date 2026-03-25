# DATA DICTIONARY

## HRMS Business Entities by Domain

> **Schema**: `HRMS`
> **Database**: Oracle Database 19c
> **Total Tables**: 42 | **Total Views**: 6 | **Total Sequences**: 25
> **Patterns**: Surrogate keys via sequences, soft deletes (`ACTIVE_FLAG`), audit columns on every table (`CREATED_BY`, `CREATED_DATE`, `MODIFIED_BY`, `MODIFIED_DATE`)

---

## Table of Contents

1. [Core / Organization Domain](#1-core--organization-domain)
2. [Employee Domain](#2-employee-domain)
3. [Payroll & Compensation Domain](#3-payroll--compensation-domain)
4. [Leave & Absence Domain](#4-leave--absence-domain)
5. [Performance Management Domain](#5-performance-management-domain)
6. [System & Infrastructure Domain](#6-system--infrastructure-domain)
7. [Views](#7-views)
8. [Seed Data Summary](#8-seed-data-summary)

---

## 1. Core / Organization Domain

### 1.1 LOCATIONS

Physical office locations where employees work.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| LOCATION_CODE | VARCHAR2(10) | **PK** | Short code (e.g., `HQ`, `CHI`, `SF`) |
| LOCATION_NAME | VARCHAR2(100) | NOT NULL | Full name |
| ADDRESS_LINE1 | VARCHAR2(200) | | Street address |
| CITY | VARCHAR2(50) | | City |
| STATE_PROVINCE | VARCHAR2(50) | | State/province |
| POSTAL_CODE | VARCHAR2(20) | | ZIP/postal code |
| COUNTRY_CODE | VARCHAR2(3) | | ISO country code |
| PHONE | VARCHAR2(20) | | Office phone |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | Soft delete flag |
| CREATED_BY | VARCHAR2(30) | | Audit: creator |
| CREATED_DATE | DATE | | Audit: creation timestamp |
| MODIFIED_BY | VARCHAR2(30) | | Audit: last modifier |
| MODIFIED_DATE | DATE | | Audit: last modification |

**Seed Data**: 3 locations (HQ - New York, CHI - Chicago, SF - San Francisco)

---

### 1.2 DEPARTMENTS

Organizational units forming a hierarchical tree via `PARENT_DEPT_ID`.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| DEPT_ID | NUMBER(10) | **PK** (SEQ_DEPARTMENT) | Surrogate key |
| DEPT_CODE | VARCHAR2(10) | UNIQUE, NOT NULL | Short code (e.g., `HR`, `FIN`, `IT`) |
| DEPT_NAME | VARCHAR2(100) | NOT NULL | Department name |
| COST_CENTER | VARCHAR2(20) | | GL cost center code (e.g., `CC-1100`) |
| PARENT_DEPT_ID | NUMBER(10) | **FK** → DEPARTMENTS | Self-referential hierarchy |
| MANAGER_EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Department head |
| LOCATION_CODE | VARCHAR2(10) | **FK** → LOCATIONS | Default office |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | Soft delete flag |
| CREATED_BY / CREATED_DATE | | | Audit columns |
| MODIFIED_BY / MODIFIED_DATE | | | Audit columns |

**Seed Data**: 10 departments — Executive Office, HR, Finance & Accounting, IT, IT-Development, IT-Operations, Sales, Marketing, Operations, Legal & Compliance

**Hierarchy**:
```
Executive Office (1)
├── Human Resources (10)
├── Finance & Accounting (20)
├── Information Technology (30)
│   ├── IT - Development (31)
│   └── IT - Operations (32)
├── Sales (40)
├── Marketing (50)
├── Operations (60)
└── Legal & Compliance (70)
```

---

### 1.3 JOB_GRADES

Salary band definitions defining min/max compensation for each grade level.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| GRADE_ID | NUMBER(10) | **PK** | Grade identifier |
| GRADE_NAME | VARCHAR2(50) | NOT NULL | Display name |
| GRADE_LEVEL | NUMBER(2) | NOT NULL | Numeric level for sorting/comparison |
| MIN_SALARY | NUMBER(12,2) | NOT NULL | Minimum annual salary |
| MAX_SALARY | NUMBER(12,2) | NOT NULL, CHK > MIN | Maximum annual salary |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | Soft delete flag |
| Audit columns | | | Standard |

**Seed Data** (10 grades):

| Grade | Name | Level | Min Salary | Max Salary |
|-------|------|-------|-----------|-----------|
| 1 | Entry Level | 1 | $35,000 | $55,000 |
| 2 | Junior | 2 | $45,000 | $70,000 |
| 3 | Mid-Level | 3 | $60,000 | $90,000 |
| 4 | Senior | 4 | $80,000 | $120,000 |
| 5 | Lead | 5 | $95,000 | $145,000 |
| 6 | Manager | 6 | $110,000 | $170,000 |
| 7 | Senior Manager | 7 | $130,000 | $200,000 |
| 8 | Director | 8 | $160,000 | $250,000 |
| 9 | VP | 9 | $200,000 | $350,000 |
| 10 | C-Suite | 10 | $300,000 | $600,000 |

---

### 1.4 JOB_TITLES

Position definitions mapped to grades and EEO categories.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| JOB_ID | NUMBER(10) | **PK** (SEQ_JOB_TITLE) | Surrogate key |
| JOB_CODE | VARCHAR2(20) | UNIQUE, NOT NULL | Short code (e.g., `SR-DEV`, `MGR-PAY`) |
| JOB_TITLE | VARCHAR2(100) | NOT NULL | Full title |
| GRADE_ID | NUMBER(10) | **FK** → JOB_GRADES | Associated salary band |
| EEO_CATEGORY | VARCHAR2(10) | | EEO reporting category |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | Soft delete flag |
| Audit columns | | | Standard |

**Seed Data**: 26 job titles across grades 1-10 (Intern through CEO)

---

## 2. Employee Domain

### 2.1 EMPLOYEES

Central employee master record. The most-referenced table in the system.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| EMP_ID | NUMBER(10) | **PK** (SEQ_EMPLOYEE) | Surrogate key |
| EMP_NUMBER | VARCHAR2(20) | **UNIQUE**, NOT NULL | Business key `EMP-NNNNNN` |
| FIRST_NAME | VARCHAR2(50) | NOT NULL | Stored as UPPER |
| LAST_NAME | VARCHAR2(50) | NOT NULL | Stored as UPPER |
| EMAIL | VARCHAR2(100) | | Stored as LOWER |
| PHONE_WORK | VARCHAR2(20) | | Work phone |
| PHONE_MOBILE | VARCHAR2(20) | | Mobile phone |
| HIRE_DATE | DATE | NOT NULL | Original/rehire date |
| TERMINATION_DATE | DATE | | Set on termination |
| TERMINATION_REASON | VARCHAR2(50) | | VOLUNTARY / INVOLUNTARY |
| DEPT_ID | NUMBER(10) | **FK** → DEPARTMENTS | Current department |
| JOB_ID | NUMBER(10) | **FK** → JOB_TITLES | Current position |
| MANAGER_EMP_ID | NUMBER(10) | **FK** → EMPLOYEES (self) | Reporting manager |
| LOCATION_CODE | VARCHAR2(10) | **FK** → LOCATIONS | Work location |
| EMPLOYMENT_TYPE | VARCHAR2(20) | CHECK | FULL_TIME, PART_TIME, CONTRACT |
| EMPLOYMENT_STATUS | VARCHAR2(20) | CHECK | ACTIVE, TERMINATED, ON_LEAVE, SUSPENDED |
| GENDER | CHAR(1) | | M, F, O |
| DATE_OF_BIRTH | DATE | | Birth date |
| MARITAL_STATUS | VARCHAR2(20) | | SINGLE, MARRIED, DIVORCED |
| SSN_ENCRYPTED | RAW(256) | | AES-256 encrypted SSN |
| ADDRESS_LINE1 | VARCHAR2(200) | | Mailing address |
| ADDRESS_LINE2 | VARCHAR2(200) | | |
| CITY | VARCHAR2(50) | | |
| STATE_PROVINCE | VARCHAR2(50) | | |
| POSTAL_CODE | VARCHAR2(20) | | |
| COUNTRY_CODE | VARCHAR2(3) | | |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | Soft delete flag |
| Audit columns | | | Standard |

**Key Relationships**: Referenced by nearly every table. Self-referential via MANAGER_EMP_ID for org hierarchy.

**Seed Data**: 25 employees across Executive (3), HR (3), Finance (5), IT (8), Sales (4), Operations (1), Marketing (1)

---

### 2.2 EMPLOYEE_HISTORY

Change tracking for transfers, promotions, terminations, and rehires. Uses autonomous transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| HIST_ID | NUMBER(10) | **PK** (SEQ_EMP_HISTORY) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Employee reference |
| CHANGE_TYPE | VARCHAR2(20) | NOT NULL | HIRE, TRANSFER, PROMOTION, TERMINATION, REHIRE |
| EFFECTIVE_DATE | DATE | NOT NULL | When the change took effect |
| OLD_DEPT_ID / NEW_DEPT_ID | NUMBER(10) | | Department change |
| OLD_JOB_ID / NEW_JOB_ID | NUMBER(10) | | Job change |
| OLD_MANAGER_ID / NEW_MANAGER_ID | NUMBER(10) | | Manager change |
| OLD_SALARY / NEW_SALARY | NUMBER(12,2) | | Salary change |
| OLD_LOCATION / NEW_LOCATION | VARCHAR2(10) | | Location change |
| REASON_CODE | VARCHAR2(50) | | Change reason |
| COMMENTS | VARCHAR2(4000) | | Free-text notes |
| Audit columns | | | Standard |

---

### 2.3 EMPLOYEE_DEPENDENTS

Dependent information for benefits enrollment.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| DEPENDENT_ID | NUMBER(10) | **PK** (SEQ_DEPENDENT) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Parent employee |
| FIRST_NAME | VARCHAR2(50) | NOT NULL | |
| LAST_NAME | VARCHAR2(50) | NOT NULL | |
| RELATIONSHIP | VARCHAR2(20) | | SPOUSE, CHILD, DOMESTIC_PARTNER |
| DATE_OF_BIRTH | DATE | | |
| GENDER | CHAR(1) | | |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

### 2.4 EMERGENCY_CONTACTS

Priority-ordered emergency contact list per employee.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| CONTACT_ID | NUMBER(10) | **PK** (SEQ_EMERGENCY) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Parent employee |
| CONTACT_NAME | VARCHAR2(100) | NOT NULL | |
| RELATIONSHIP | VARCHAR2(20) | | |
| PHONE_PRIMARY | VARCHAR2(20) | NOT NULL | |
| PHONE_SECONDARY | VARCHAR2(20) | | |
| PRIORITY_ORDER | NUMBER(2) | | 1 = first contact |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

## 3. Payroll & Compensation Domain

### 3.1 SALARY_RECORDS

Effective-dated salary history. Only one record should have `ACTIVE_FLAG='Y'` per employee at any time.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| SALARY_ID | NUMBER(10) | **PK** (SEQ_SALARY) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Employee reference |
| EFFECTIVE_DATE | DATE | NOT NULL | Salary start date |
| END_DATE | DATE | | Salary end date (NULL = current) |
| BASE_SALARY | NUMBER(12,2) | NOT NULL, > 0 | Annual base salary |
| CURRENCY_CODE | VARCHAR2(3) | DEFAULT 'USD' | |
| PAY_FREQUENCY | VARCHAR2(20) | DEFAULT 'MONTHLY' | WEEKLY, BIWEEKLY, SEMIMONTHLY, MONTHLY |
| SALARY_BASIS | VARCHAR2(20) | | ANNUAL, HOURLY |
| CHANGE_REASON | VARCHAR2(50) | | NEW_HIRE, PROMOTION, MERIT, REHIRE |
| CHANGE_PCT | NUMBER(5,2) | | Percentage change from previous |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

### 3.2 PAY_ELEMENTS

Master definition of earning types, deductions, benefits, and taxes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| ELEMENT_ID | NUMBER(10) | **PK** (SEQ_PAY_ELEMENT) | Surrogate key |
| ELEMENT_CODE | VARCHAR2(20) | UNIQUE, NOT NULL | Short code |
| ELEMENT_NAME | VARCHAR2(100) | NOT NULL | Display name |
| ELEMENT_TYPE | VARCHAR2(20) | CHECK | EARNING, DEDUCTION, BENEFIT, TAX |
| CALCULATION_TYPE | VARCHAR2(20) | | FLAT, PERCENTAGE |
| DEFAULT_AMOUNT | NUMBER(12,2) | | Default flat amount |
| DEFAULT_PERCENTAGE | NUMBER(5,2) | | Default percentage |
| PRETAX_FLAG | CHAR(1) | | Y = pre-tax deduction |
| GL_ACCOUNT_CODE | VARCHAR2(30) | | General Ledger account |
| PRIORITY_ORDER | NUMBER(3) | | Calculation order |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Seed Data**: 8 elements — Base Pay, Overtime, Federal Tax, State Tax, Social Security, Medicare, 401(k), Health Insurance

---

### 3.3 EMPLOYEE_PAY_ELEMENTS

Employee-specific assignments of deductions and benefits (e.g., 401k election, health plan).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| EMP_ELEMENT_ID | NUMBER(10) | **PK** (SEQ_EMP_PAY_ELEMENT) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| ELEMENT_ID | NUMBER(10) | **FK** → PAY_ELEMENTS | |
| EFFECTIVE_DATE | DATE | NOT NULL | Start date |
| END_DATE | DATE | | End date (NULL = ongoing) |
| AMOUNT | NUMBER(12,2) | | Override flat amount |
| PERCENTAGE | NUMBER(5,2) | | Override percentage |
| OVERRIDE_AMOUNT | NUMBER(12,2) | | Manual override |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

### 3.4 PAY_PERIODS

Pay period definitions (monthly or biweekly).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| PERIOD_ID | NUMBER(10) | **PK** (SEQ_PAY_PERIOD) | Surrogate key |
| PERIOD_NAME | VARCHAR2(50) | NOT NULL | Display name (e.g., `2024-01 (Jan)`) |
| PAY_FREQUENCY | VARCHAR2(20) | | MONTHLY, BIWEEKLY |
| PERIOD_START_DATE | DATE | NOT NULL | |
| PERIOD_END_DATE | DATE | NOT NULL | |
| PAY_DATE | DATE | NOT NULL | Adjusted for weekends |
| STATUS | VARCHAR2(20) | | OPEN, CLOSED |
| CLOSED_BY | VARCHAR2(30) | | Who closed the period |
| CLOSED_DATE | DATE | | When closed |
| Audit columns | | | Standard |

---

### 3.5 PAYROLL_RUNS

Payroll processing run header with status workflow: PENDING → CALCULATING → CALCULATED → APPROVED → REVERSED.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| RUN_ID | NUMBER(10) | **PK** (SEQ_PAYROLL_RUN) | Surrogate key |
| PERIOD_ID | NUMBER(10) | **FK** → PAY_PERIODS | Associated period |
| RUN_TYPE | VARCHAR2(20) | | REGULAR, SUPPLEMENTAL, BONUS |
| RUN_DATE | DATE | | Processing date |
| STATUS | VARCHAR2(20) | | PENDING, CALCULATING, CALCULATED, APPROVED, ERROR, REVERSED |
| EMPLOYEE_COUNT | NUMBER(6) | | Employees processed |
| ERROR_COUNT | NUMBER(6) | | Employees with errors |
| TOTAL_GROSS | NUMBER(15,2) | | Sum of earnings |
| TOTAL_DEDUCTIONS | NUMBER(15,2) | | Sum of deductions + taxes |
| TOTAL_NET | NUMBER(15,2) | | Net pay total |
| SUBMITTED_BY / SUBMITTED_DATE | | | Who submitted |
| APPROVED_BY / APPROVED_DATE | | | Who approved |
| Audit columns | | | Standard |

---

### 3.6 PAYROLL_DETAILS

Line-item payroll calculation per employee per run.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| DETAIL_ID | NUMBER(15) | **PK** (SEQ_PAYROLL_DETAIL) | Surrogate key |
| RUN_ID | NUMBER(10) | **FK** → PAYROLL_RUNS | Parent run |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Employee |
| ELEMENT_ID | NUMBER(10) | **FK** → PAY_ELEMENTS | Pay element |
| ELEMENT_TYPE | VARCHAR2(20) | | EARNING, DEDUCTION, TAX, BENEFIT, ERROR |
| AMOUNT | NUMBER(12,2) | | Amount (negative for deductions) |
| STATUS | VARCHAR2(20) | | CALCULATED, ERROR, REVERSED |
| ERROR_MESSAGE | VARCHAR2(4000) | | Error detail if status=ERROR |
| Audit columns | | | Standard |

---

### 3.7 TAX_BRACKETS

Federal and state tax bracket definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| BRACKET_ID | NUMBER(10) | **PK** (SEQ_TAX_BRACKET) | Surrogate key |
| TAX_YEAR | NUMBER(4) | NOT NULL | |
| TAX_TYPE | VARCHAR2(20) | | FEDERAL, STATE |
| JURISDICTION | VARCHAR2(10) | | State code (NULL for federal) |
| FILING_STATUS | VARCHAR2(30) | | SINGLE, MARRIED_JOINT, etc. |
| BRACKET_MIN | NUMBER(12,2) | | Lower bound |
| BRACKET_MAX | NUMBER(12,2) | | Upper bound |
| TAX_RATE | NUMBER(6,4) | | Marginal rate |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Note**: PKG_PAYROLL.calculate_federal_tax uses hard-coded 2024 brackets instead of this table.

---

### 3.8 EMPLOYEE_TAX_INFO

W-4 information per employee per tax year.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| TAX_INFO_ID | NUMBER(10) | **PK** | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| TAX_YEAR | NUMBER(4) | NOT NULL | |
| FILING_STATUS | VARCHAR2(30) | | SINGLE, MARRIED_JOINT, MARRIED_SEPARATE |
| FEDERAL_ALLOWANCES | NUMBER(2) | | W-4 allowances |
| STATE_CODE | VARCHAR2(3) | | Tax state |
| STATE_ALLOWANCES | NUMBER(2) | | State allowances |
| ADDITIONAL_FED_WH | NUMBER(8,2) | | Additional federal withholding |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

### 3.9 EMPLOYEE_BANK_ACCOUNTS

Direct deposit information.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| BANK_ACCOUNT_ID | NUMBER(10) | **PK** | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| BANK_NAME | VARCHAR2(100) | | |
| ROUTING_NUMBER | VARCHAR2(9) | | ABA routing number |
| ACCOUNT_NUMBER | VARCHAR2(20) | | Encrypted in production |
| ACCOUNT_TYPE | VARCHAR2(10) | | CHECKING, SAVINGS |
| DEPOSIT_TYPE | VARCHAR2(10) | | FULL, PERCENTAGE, FLAT |
| DEPOSIT_AMOUNT | NUMBER(12,2) | | Amount/percentage for partial deposit |
| PRIORITY_ORDER | NUMBER(2) | | For split deposits |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

---

## 4. Leave & Absence Domain

### 4.1 LEAVE_TYPES

Leave type definitions with accrual configuration.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| LEAVE_TYPE_ID | NUMBER(10) | **PK** (SEQ_LEAVE_TYPE) | Surrogate key |
| LEAVE_TYPE_CODE | VARCHAR2(10) | UNIQUE, NOT NULL | Short code (PTO, SICK, COMP, FMLA, JURY, BEREAV) |
| LEAVE_TYPE_NAME | VARCHAR2(50) | NOT NULL | Display name |
| ACCRUAL_FLAG | CHAR(1) | | Y = accrual-based, N = entitlement-based |
| ACCRUAL_RATE | NUMBER(5,3) | | Days accrued per period |
| ACCRUAL_FREQUENCY | VARCHAR2(20) | | MONTHLY, BIWEEKLY |
| MAX_BALANCE | NUMBER(5,1) | | Maximum accrual cap |
| CARRYOVER_MAX | NUMBER(5,1) | | Max days carried to next year |
| CARRYOVER_EXPIRY | NUMBER(2) | | Months until carryover expires |
| REQUIRES_APPROVAL | CHAR(1) | | Y = manager approval needed |
| MIN_TENURE_DAYS | NUMBER(4) | | Minimum employment days before eligible |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Seed Data** (6 leave types):

| Code | Name | Accrual | Rate/Mo | Max | Carryover | Approval | Min Tenure |
|------|------|---------|---------|-----|-----------|----------|-----------|
| PTO | Paid Time Off | Yes | 1.25 days | 20 | 5 days (3 mo expiry) | Yes | 0 days |
| SICK | Sick Leave | Yes | 0.833 days | 10 | 10 days (no expiry) | Yes | 0 days |
| COMP | Compensatory Time | No | — | — | 0 | Yes | 90 days |
| FMLA | Family Medical Leave | No | — | — | 0 | Yes | 365 days |
| JURY | Jury Duty | No | — | — | 0 | No (auto) | 0 days |
| BEREAV | Bereavement | No | — | — | 0 | No (auto) | 0 days |

---

### 4.2 LEAVE_BALANCES

Per-employee per-year leave balance tracking. Includes a virtual column `AVAILABLE`.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| BALANCE_ID | NUMBER(10) | **PK** (SEQ_LEAVE_BALANCE) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| LEAVE_TYPE_ID | NUMBER(10) | **FK** → LEAVE_TYPES | |
| CALENDAR_YEAR | NUMBER(4) | NOT NULL | |
| OPENING_BALANCE | NUMBER(5,1) | DEFAULT 0 | Beginning of year |
| ACCRUED | NUMBER(5,1) | DEFAULT 0 | Monthly accrual running total |
| USED | NUMBER(5,1) | DEFAULT 0 | Approved/taken days |
| ADJUSTMENT | NUMBER(5,1) | DEFAULT 0 | Manual adjustments |
| PENDING | NUMBER(5,1) | DEFAULT 0 | Days in pending requests |
| CARRYOVER_FROM_PREV | NUMBER(5,1) | DEFAULT 0 | Carried from previous year |
| CARRYOVER_EXPIRY_DT | DATE | | When carryover expires |
| **AVAILABLE** | NUMBER | **Virtual** | `OPENING_BALANCE + ACCRUED - USED + ADJUSTMENT - PENDING` |
| Audit columns | | | Standard |

**Unique Constraint**: (EMP_ID, LEAVE_TYPE_ID, CALENDAR_YEAR)

---

### 4.3 LEAVE_REQUESTS

Leave request with approval workflow: PENDING → APPROVED/REJECTED → TAKEN/CANCELLED.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| REQUEST_ID | NUMBER(10) | **PK** (SEQ_LEAVE_REQUEST) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Requestor |
| LEAVE_TYPE_ID | NUMBER(10) | **FK** → LEAVE_TYPES | |
| START_DATE | DATE | NOT NULL | |
| END_DATE | DATE | NOT NULL | |
| TOTAL_DAYS | NUMBER(4,1) | | Business days (0.5 for half-day) |
| HALF_DAY_FLAG | CHAR(1) | DEFAULT 'N' | |
| HALF_DAY_PERIOD | VARCHAR2(10) | | AM, PM |
| STATUS | VARCHAR2(20) | | PENDING, APPROVED, REJECTED, CANCELLED, TAKEN |
| REASON | VARCHAR2(500) | | Request reason |
| APPROVER_EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Manager |
| APPROVAL_DATE | DATE | | |
| APPROVAL_COMMENTS | VARCHAR2(500) | | |
| CANCEL_REASON | VARCHAR2(500) | | |
| CANCELLED_DATE | DATE | | |
| Audit columns | | | Standard |

---

### 4.4 LEAVE_ACCRUAL_LOG

Audit trail for monthly accrual batch processing.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| ACCRUAL_ID | NUMBER(10) | **PK** (SEQ_LEAVE_ACCRUAL) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| LEAVE_TYPE_ID | NUMBER(10) | **FK** → LEAVE_TYPES | |
| ACCRUAL_DATE | DATE | | Processing date |
| ACCRUAL_AMOUNT | NUMBER(5,3) | | Days accrued |
| Audit columns | | | Standard |

---

### 4.5 HOLIDAYS

Company and location-specific holiday calendar.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| HOLIDAY_ID | NUMBER(10) | **PK** (SEQ_HOLIDAY) | Surrogate key |
| HOLIDAY_DATE | DATE | NOT NULL | |
| HOLIDAY_NAME | VARCHAR2(100) | NOT NULL | |
| LOCATION_CODE | VARCHAR2(10) | | NULL = all locations |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Seed Data**: 11 US holidays for 2024 (New Year's, MLK, Presidents', Memorial, Independence, Labor, Columbus, Veterans, Thanksgiving, Day After Thanksgiving, Christmas)

---

## 5. Performance Management Domain

### 5.1 REVIEW_CYCLES

Annual review cycle definitions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| CYCLE_ID | NUMBER(10) | **PK** (SEQ_REVIEW_CYCLE) | Surrogate key |
| CYCLE_NAME | VARCHAR2(100) | NOT NULL | e.g., "2024 Annual Review" |
| CYCLE_YEAR | NUMBER(4) | NOT NULL | |
| START_DATE | DATE | NOT NULL | Review period start |
| END_DATE | DATE | NOT NULL | Review period end |
| SELF_REVIEW_DUE | DATE | | Self-assessment deadline |
| MANAGER_REVIEW_DUE | DATE | | Manager review deadline |
| STATUS | VARCHAR2(20) | | DRAFT, OPEN, CLOSED |
| Audit columns | | | Standard |

---

### 5.2 PERFORMANCE_REVIEWS

Individual performance review records.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| REVIEW_ID | NUMBER(10) | **PK** (SEQ_PERF_REVIEW) | Surrogate key |
| CYCLE_ID | NUMBER(10) | **FK** → REVIEW_CYCLES | Parent cycle |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Reviewee |
| REVIEWER_EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | Manager/reviewer |
| REVIEW_TYPE | VARCHAR2(20) | | ANNUAL, MID_YEAR, PROBATION |
| STATUS | VARCHAR2(20) | | NOT_STARTED, SELF_REVIEW, MANAGER_REVIEW, COMPLETED, ACKNOWLEDGED |
| OVERALL_RATING | NUMBER(2,1) | CHK 1.0-5.0 | Numeric rating |
| RATING_LABEL | VARCHAR2(30) | | Exceptional / Exceeds / Meets / Needs Improvement / Unsatisfactory |
| SELF_ASSESSMENT | CLOB | | Employee self-review |
| MANAGER_ASSESSMENT | CLOB | | Manager narrative |
| STRENGTHS | CLOB | | |
| AREAS_FOR_IMPROVEMENT | CLOB | | |
| DEVELOPMENT_PLAN | CLOB | | |
| EMPLOYEE_COMMENTS | CLOB | | Post-review comments |
| EMPLOYEE_ACK_DATE | DATE | | Acknowledgement date |
| Audit columns | | | Standard |

**Rating Scale**:

| Range | Label |
|-------|-------|
| 4.5 - 5.0 | Exceptional |
| 3.5 - 4.4 | Exceeds Expectations |
| 2.5 - 3.4 | Meets Expectations |
| 1.5 - 2.4 | Needs Improvement |
| 1.0 - 1.4 | Unsatisfactory |

---

### 5.3 PERFORMANCE_GOALS

Individual goals linked to reviews.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| GOAL_ID | NUMBER(10) | **PK** (SEQ_PERF_GOAL) | Surrogate key |
| REVIEW_ID | NUMBER(10) | **FK** → PERFORMANCE_REVIEWS | Parent review |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| GOAL_TITLE | VARCHAR2(200) | NOT NULL | |
| GOAL_DESCRIPTION | CLOB | | |
| GOAL_CATEGORY | VARCHAR2(20) | | BUSINESS, DEVELOPMENT, PERSONAL |
| WEIGHT_PCT | NUMBER(3) | | Weight for overall rating |
| TARGET_DATE | DATE | | |
| STATUS | VARCHAR2(20) | | NOT_STARTED, IN_PROGRESS, COMPLETED, CANCELLED |
| PROGRESS_PCT | NUMBER(3) | DEFAULT 0 | 0-100 |
| COMMENTS | CLOB | | Progress notes |
| Audit columns | | | Standard |

---

## 6. System & Infrastructure Domain

### 6.1 AUDIT_LOG

Centralized audit trail for all DML operations.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| LOG_ID | NUMBER(15) | **PK** (SEQ_AUDIT) | Surrogate key |
| TABLE_NAME | VARCHAR2(30) | NOT NULL | Affected table |
| RECORD_ID | NUMBER(15) | NOT NULL | Primary key of affected record |
| ACTION | VARCHAR2(10) | NOT NULL | INSERT, UPDATE, DELETE |
| ACTION_DATE | DATE | DEFAULT SYSDATE | |
| ACTION_BY | VARCHAR2(30) | | User who performed action |
| IP_ADDRESS | VARCHAR2(45) | | From SYS_CONTEXT |
| SESSION_ID | VARCHAR2(100) | | From SYS_CONTEXT |

---

### 6.2 SYSTEM_PARAMETERS

Key-value configuration store.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| PARAM_ID | NUMBER(10) | **PK** | Surrogate key |
| CATEGORY | VARCHAR2(50) | NOT NULL | Grouping category |
| PARAM_NAME | VARCHAR2(100) | NOT NULL | Parameter name |
| PARAM_VALUE | VARCHAR2(4000) | | String value |
| PARAM_TYPE | VARCHAR2(20) | | STRING, NUMBER, DATE, BOOLEAN |
| DESCRIPTION | VARCHAR2(500) | | |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Unique Constraint**: (CATEGORY, PARAM_NAME)

**Seed Data**: SESSION_TIMEOUT_MINUTES=30, MAX_LOGIN_ATTEMPTS=5, PASSWORD_EXPIRY_DAYS=90, SMTP_SERVER=smtp.internal.company.com, FISCAL_YEAR_START_MONTH=10

---

### 6.3 NOTIFICATION_QUEUE

Async notification queue for email, SMS, and in-app notifications.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| NOTIFICATION_ID | NUMBER(15) | **PK** (SEQ_NOTIFICATION) | Surrogate key |
| RECIPIENT_EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| NOTIFICATION_TYPE | VARCHAR2(10) | | EMAIL, SMS, IN_APP |
| SUBJECT | VARCHAR2(200) | | |
| BODY | CLOB | | |
| STATUS | VARCHAR2(20) | | PENDING, SENT, FAILED |
| RETRY_COUNT | NUMBER(2) | DEFAULT 0 | |
| SENT_DATE | DATE | | |
| ERROR_MESSAGE | VARCHAR2(4000) | | |
| Audit columns | | | Standard |

---

### 6.4 USER_SESSIONS

Active session tracking for session management and timeout.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| SESSION_ID | NUMBER(15) | **PK** (SEQ_SESSION) | Surrogate key |
| EMP_ID | NUMBER(10) | **FK** → EMPLOYEES | |
| USERNAME | VARCHAR2(30) | | |
| LOGIN_TIME | DATE | | |
| LAST_ACTIVITY | DATE | | Updated on each request |
| LOGOUT_TIME | DATE | | |
| STATUS | VARCHAR2(10) | | ACTIVE, EXPIRED, LOGGED_OUT |
| IP_ADDRESS | VARCHAR2(45) | | |
| MACHINE_NAME | VARCHAR2(100) | | |

---

### 6.5 LOOKUP_VALUES

Generic reference data for dropdowns and validation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| LOOKUP_ID | NUMBER(10) | **PK** | Surrogate key |
| LOOKUP_TYPE | VARCHAR2(50) | NOT NULL | Category (e.g., GENDER, MARITAL_STATUS, COUNTRY) |
| LOOKUP_CODE | VARCHAR2(20) | NOT NULL | |
| LOOKUP_VALUE | VARCHAR2(100) | NOT NULL | Display value |
| DISPLAY_ORDER | NUMBER(3) | | Sort order |
| ACTIVE_FLAG | CHAR(1) | DEFAULT 'Y' | |
| Audit columns | | | Standard |

**Unique Constraint**: (LOOKUP_TYPE, LOOKUP_CODE)

**Seed Data**: 12 lookup values across GENDER, MARITAL_STATUS, EMPLOYMENT_TYPE, EMPLOYMENT_STATUS

---

## 7. Views

| View | Purpose | Key Columns |
|------|---------|-------------|
| `VW_ACTIVE_EMPLOYEES` | Active employee directory | EMP_ID, EMP_NUMBER, name, DEPT_NAME, JOB_TITLE, GRADE_NAME, LOCATION_NAME, STATUS |
| `VW_ORG_HIERARCHY` | Hierarchical org chart (CONNECT BY) | LEVEL, EMP_ID, name, DEPT_NAME, MANAGER_NAME |
| `VW_EMPLOYEE_COMPENSATION` | Current salary with compa-ratio | EMP_ID, name, BASE_SALARY, GRADE_MIN/MAX, COMPA_RATIO |
| `VW_LEAVE_SUMMARY` | Leave balance overview | EMP_ID, LEAVE_TYPE_NAME, ACCRUED, USED, AVAILABLE |
| `VW_PAYROLL_LATEST` | Most recent payroll details | EMP_ID, PERIOD_NAME, GROSS, DEDUCTIONS, NET |
| `VW_PENDING_APPROVALS` | Pending leave requests | REQUEST_ID, EMPLOYEE_NAME, LEAVE_TYPE, dates, APPROVER |

---

## 8. Seed Data Summary

### Reference Data (`01_reference_data.sql`)

| Entity | Count | Examples |
|--------|-------|---------|
| Locations | 3 | HQ (New York), CHI (Chicago), SF (San Francisco) |
| Job Grades | 10 | Entry Level through C-Suite ($35k - $600k) |
| Departments | 10 | Executive, HR, Finance, IT, IT-Dev, IT-Ops, Sales, Marketing, Operations, Legal |
| Job Titles | 26 | CEO, CFO, CIO, VPs, Directors, Managers, Seniors, Mid-level, Juniors, Interns |
| Leave Types | 6 | PTO, Sick, Compensatory, FMLA, Jury Duty, Bereavement |
| Holidays | 11 | 2024 US federal holidays |
| Pay Elements | 8 | Base Pay, Overtime, Fed Tax, State Tax, SS, Medicare, 401k, Health Insurance |
| Tax Brackets | 7 | 2024 Federal brackets for Single filing status |
| Lookup Values | 12 | Gender, Marital Status, Employment Type, Employment Status |
| System Parameters | 5 | Session timeout, login attempts, password expiry, SMTP, fiscal year |

### Employee Data (`02_employee_data.sql`)

| Department | Count | Roles |
|-----------|-------|-------|
| Executive | 3 | CEO, CFO, CIO |
| Human Resources | 3 | VP HR, HR Specialist, HR Assistant |
| Finance & Accounting | 5 | VP Finance, Payroll Manager, Sr Accountant, Accountant, Accounting Clerk |
| Information Technology | 8 | Director IT, Dev Manager, Sr Developer, Sr DBA, 2x Developer, QA Analyst, Jr Developer |
| Sales | 4 | VP Sales, Sales Manager, Sr Sales Rep, Sales Rep |
| Operations | 1 | Operations Manager |
| Marketing | 1 | Marketing staff |
| **Total** | **25** | |

Each employee has one corresponding SALARY_RECORDS entry.
