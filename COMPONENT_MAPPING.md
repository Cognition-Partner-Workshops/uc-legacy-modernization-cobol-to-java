# Oracle Forms to Java/React Component Mapping

> Comprehensive mapping of every Oracle Forms element in the HRMS legacy application  
> to its Java (Spring Boot) / React equivalent.  
> Source: `ts-plsql-oracle-forms-legacy-codebase`

---

## Table of Contents

1. [Forms Module → Application Structure](#1-forms-module--application-structure)
2. [Data Blocks → JPA Entities & REST Resources](#2-data-blocks--jpa-entities--rest-resources)
3. [Form Items → React UI Components](#3-form-items--react-ui-components)
4. [Triggers → Java Service Methods & React Event Handlers](#4-triggers--java-service-methods--react-event-handlers)
5. [LOVs & Record Groups → REST Endpoints & Select Components](#5-lovs--record-groups--rest-endpoints--select-components)
6. [Canvases, Windows & Tab Pages → React Routes & Layouts](#6-canvases-windows--tab-pages--react-routes--layouts)
7. [Alerts → Dialog/Modal Components](#7-alerts--dialogmodal-components)
8. [Menu Module → Navigation Components](#8-menu-module--navigation-components)
9. [PL/SQL Libraries → Java Shared Libraries](#9-plsql-libraries--java-shared-libraries)
10. [PL/SQL Packages → Spring Services](#10-plsql-packages--spring-services)
11. [Database Objects → JPA/Hibernate Mappings](#11-database-objects--jpahibernate-mappings)
12. [Built-in Forms Functions → Java/React Equivalents](#12-built-in-forms-functions--javareact-equivalents)

---

## 1. Forms Module → Application Structure

| Oracle Form Module | React Application Route | Spring Boot Controller | Description |
|---|---|---|---|
| `HRMS_LOGIN.fmb` | `/login` | `AuthController` | Authentication page |
| `HRMS_MENU.fmb` | `/` (shell layout) | N/A (client-side routing) | Main navigation shell |
| `HRMS_EMPLOYEE.fmb` | `/employees`, `/employees/:id` | `EmployeeController` | Employee maintenance |
| `HRMS_PAYROLL.fmb` | `/payroll`, `/payroll/runs/:id` | `PayrollController` | Payroll processing |
| `HRMS_LEAVE.fmb` | `/leave`, `/leave/new`, `/leave/approvals` | `LeaveController` | Leave management |
| `HRMS_PERFORMANCE.fmb` | `/performance`, `/performance/reviews/:id` | `PerformanceController` | Performance reviews |
| `HRMS_REPORTS.fmb` (referenced) | `/reports/:reportType` | `ReportController` | Report launcher |

### Attached Libraries → Shared Modules

| Oracle PLL | Java Package | React Module |
|---|---|---|
| `HRMS_COMMON_LIB.pll` | `com.hrms.common` | `src/shared/hooks/`, `src/shared/utils/` |
| `HRMS_VALIDATION_LIB.pll` | `com.hrms.validation` | `src/shared/validation/` |

### Menu Module → App Shell

| Oracle MMB | React Component | Description |
|---|---|---|
| `HRMS_MENU.mmb` → `MAIN_MENUBAR` | `<AppShell>` + `<Sidebar>` / `<TopNav>` | Application layout with navigation |

---

## 2. Data Blocks → JPA Entities & REST Resources

### HRMS_EMPLOYEE.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `EMPLOYEE` | `HRMS.EMPLOYEES` | `Employee.java` | `GET/POST/PUT /api/employees` | Master |
| `SALARY` | `HRMS.SALARY_RECORDS` | `SalaryRecord.java` | `GET /api/employees/{id}/salary-history` | Detail (of EMPLOYEE) |
| `DEPENDENTS` (referenced) | `HRMS.EMPLOYEE_DEPENDENTS` | `EmployeeDependent.java` | `GET/POST/PUT/DELETE /api/employees/{id}/dependents` | Detail (of EMPLOYEE) |
| `EMERGENCY_CONTACTS` (referenced) | `HRMS.EMERGENCY_CONTACTS` | `EmergencyContact.java` | `GET/POST/PUT/DELETE /api/employees/{id}/emergency-contacts` | Detail (of EMPLOYEE) |
| `EMP_HISTORY` (referenced) | `HRMS.EMPLOYEE_HISTORY` | `EmployeeHistory.java` | `GET /api/employees/{id}/history` | Detail (of EMPLOYEE) |

**Master-Detail Relationships:**

| Forms Relation | JPA Mapping | React Pattern |
|---|---|---|
| `EMP_SALARY_REL` (EMPLOYEE → SALARY) | `@OneToMany(mappedBy = "employee")` on `Employee` | Tab panel loads salary records when employee selected |
| `DeleteRecordBehavior="Cascading"` on SALARY | `cascade = CascadeType.ALL, orphanRemoval = true` | Delete confirmation dialog |

### HRMS_PAYROLL.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `PAY_PERIOD` | `HRMS.PAY_PERIODS` | `PayPeriod.java` | `GET /api/payroll/periods` | Master |
| `PAYROLL_RUN` | `HRMS.PAYROLL_RUNS` | `PayrollRun.java` | `GET/POST /api/payroll/runs` | Detail (of PAY_PERIOD) |
| `PAYROLL_DETAIL` (referenced) | `HRMS.PAYROLL_DETAILS` | `PayrollDetail.java` | `GET /api/payroll/runs/{id}/details` | Detail (of PAYROLL_RUN) |
| `PAYSLIP_SUMMARY` (referenced) | (derived from PAYROLL_DETAILS) | `PayslipSummaryDTO.java` | `GET /api/payroll/runs/{id}/payslips` | Summary view |

**Master-Detail Relationships:**

| Forms Relation | JPA Mapping |
|---|---|
| `PERIOD_RUN_REL` (PAY_PERIOD → PAYROLL_RUN) | `@OneToMany(mappedBy = "payPeriod")` on `PayPeriod` |

### HRMS_LEAVE.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `LEAVE_REQUEST` | `HRMS.LEAVE_REQUESTS` | `LeaveRequest.java` | `GET /api/leave/requests` | Query block |
| `NEW_REQUEST` | (None — control block) | `LeaveRequestDTO.java` | `POST /api/leave/requests` | Input form |
| `LEAVE_BALANCE` | `HRMS.LEAVE_BALANCES` | `LeaveBalance.java` | `GET /api/leave/balances` | Display block |
| `PENDING_APPROVAL` (referenced) | `HRMS.LEAVE_REQUESTS` (filtered) | `LeaveRequest.java` | `GET /api/leave/approvals` | Query block |
| `TEAM_CAL` (referenced) | (derived query) | `TeamCalendarDTO.java` | `GET /api/leave/calendar` | Display block |

### HRMS_PERFORMANCE.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `REVIEW_CYCLE` | `HRMS.REVIEW_CYCLES` | `ReviewCycle.java` | `GET /api/performance/cycles` | Master |
| `PERFORMANCE_REVIEW` | `HRMS.PERFORMANCE_REVIEWS` | `PerformanceReview.java` | `GET/PUT /api/performance/reviews` | Detail (of REVIEW_CYCLE) |
| `PERFORMANCE_GOAL` | `HRMS.PERFORMANCE_GOALS` | `PerformanceGoal.java` | `GET/POST/PUT /api/performance/reviews/{id}/goals` | Detail (of PERFORMANCE_REVIEW) |

**Master-Detail Relationships:**

| Forms Relation | JPA Mapping |
|---|---|
| `CYCLE_REVIEW_REL` (REVIEW_CYCLE → PERFORMANCE_REVIEW) | `@OneToMany(mappedBy = "reviewCycle")` on `ReviewCycle` |
| `REVIEW_GOAL_REL` (PERFORMANCE_REVIEW → PERFORMANCE_GOAL) | `@OneToMany(mappedBy = "performanceReview")` on `PerformanceReview` |

### HRMS_LOGIN.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `LOGIN` | (None — control block) | `LoginRequestDTO.java` | `POST /api/auth/login` | Input form |

### HRMS_MENU.xml

| Forms Data Block | Base Table | JPA Entity | REST Resource | Block Type |
|---|---|---|---|---|
| `MENU_CONTROL` | (None — control block) | N/A | `GET /api/auth/permissions` | Navigation control |

---

## 3. Form Items → React UI Components

### HRMS_EMPLOYEE.xml — EMPLOYEE Block Items

| Forms Item | Item Type | Forms Properties | React Component | Props/Notes |
|---|---|---|---|---|
| `EMP_ID` | Text Field | `Visible="No"`, `PrimaryKey="Yes"` | N/A (state variable) | Hidden; used as route param `:id` |
| `EMP_NUMBER` | Text Field | `InsertAllowed="No"`, `UpdateAllowed="No"` | `<TextField disabled>` | Read-only after creation; auto-generated |
| `FIRST_NAME` | Text Field | `Required="Yes"`, `CaseRestriction="Upper"` | `<TextField required inputProps={{style: {textTransform: 'uppercase'}}} />` | Validation: required |
| `LAST_NAME` | Text Field | `Required="Yes"`, `CaseRestriction="Upper"` | `<TextField required inputProps={{style: {textTransform: 'uppercase'}}} />` | Validation: required |
| `DATE_OF_BIRTH` | Text Field | `FormatMask="MM/DD/YYYY"` | `<DatePicker format="MM/dd/yyyy" />` | MUI X Date Picker |
| `GENDER` | List Item | `ListStyle="Poplist"`, values: M/F/O | `<Select>` with options Male/Female/Other | Controlled component |
| `MARITAL_STATUS` | List Item | `ListStyle="Poplist"`, values: SINGLE/MARRIED/DIVORCED/WIDOWED | `<Select>` with 4 options | Controlled component |
| `EMAIL` | Text Field | `CaseRestriction="Lower"` | `<TextField type="email" />` | Validation: email format (fix drift) |
| `PHONE_WORK` | Text Field | | `<TextField>` with phone mask | `react-imask` or `<PatternFormat>` |
| `PHONE_MOBILE` | Text Field | | `<TextField>` with phone mask | `react-imask` or `<PatternFormat>` |
| `ADDRESS_LINE1` | Text Field | | `<TextField>` | Standard text input |
| `ADDRESS_LINE2` | Text Field | | `<TextField>` | Standard text input |
| `CITY` | Text Field | | `<TextField>` | Standard text input |
| `STATE_PROVINCE` | Text Field | | `<TextField>` or `<Select>` (US states) | Could be enhanced to dropdown |
| `POSTAL_CODE` | Text Field | | `<TextField>` with zip mask | Pattern validation |
| `HIRE_DATE` | Text Field | `Required="Yes"`, `FormatMask="MM/DD/YYYY"` | `<DatePicker required />` | Validation: not >90 days future |
| `DEPT_ID` | Text Field | `Required="Yes"`, `LOV="LOV_DEPARTMENTS"` | `<Autocomplete>` backed by `/api/departments` | LOV replacement |
| `DEPT_NAME_DISP` | Display Item | `DatabaseItem="No"` | Inline text next to `DEPT_ID` selector | Auto-populated on selection |
| `JOB_ID` | Text Field | `Required="Yes"`, `LOV="LOV_JOB_TITLES"` | `<Autocomplete>` backed by `/api/job-titles` | LOV replacement |
| `JOB_TITLE_DISP` | Display Item | `DatabaseItem="No"` | Inline text next to `JOB_ID` selector | Auto-populated on selection |
| `MANAGER_EMP_ID` | Text Field | `LOV="LOV_MANAGERS"` | `<Autocomplete>` backed by `/api/employees?active=true` | LOV replacement |
| `MANAGER_NAME_DISP` | Display Item | `DatabaseItem="No"` | Inline text next to `MANAGER_EMP_ID` selector | Auto-populated on selection |
| `LOCATION_CODE` | Text Field | `LOV="LOV_LOCATIONS"` | `<Autocomplete>` backed by `/api/locations` | LOV replacement |
| `EMPLOYMENT_TYPE` | List Item | `ListStyle="Poplist"`, 4 values | `<Select>` with 4 options | FULL_TIME/PART_TIME/CONTRACT/INTERN |
| `EMPLOYMENT_STATUS` | List Item | `UpdateAllowed="No"`, 4 values | `<Chip>` or read-only `<Select>` | Changed via lifecycle operations only |
| `TERMINATION_DATE` | Text Field | `UpdateAllowed="No"` | `<DatePicker disabled>` | Read-only; set via termination process |
| `ACTIVE_FLAG` | Text Field | `Visible="No"` | N/A (managed by backend) | Soft delete flag |
| `CREATED_BY` | Text Field | `Visible="No"` | N/A (audit metadata) | Set by backend `@CreatedBy` |
| `CREATED_DATE` | Text Field | `Visible="No"` | N/A (audit metadata) | Set by backend `@CreatedDate` |
| `MODIFIED_BY` | Text Field | `Visible="No"` | N/A (audit metadata) | Set by backend `@LastModifiedBy` |
| `MODIFIED_DATE` | Text Field | `Visible="No"` | N/A (audit metadata) | Set by backend `@LastModifiedDate` |

### HRMS_EMPLOYEE.xml — SALARY Block Items

| Forms Item | Item Type | Forms Properties | React Component | Props/Notes |
|---|---|---|---|---|
| `SALARY_ID` | (hidden) | `PrimaryKey="Yes"` | N/A (state) | Hidden key |
| `EMP_ID` | (hidden) | FK | N/A | Derived from parent |
| `EFFECTIVE_DATE` | Text Field | `FormatMask="MM/DD/YYYY"`, `UpdateAllowed="No"` | `<DatePicker disabled>` | Read-only in list |
| `END_DATE` | Text Field | `FormatMask="MM/DD/YYYY"` | `<DatePicker disabled>` | Read-only |
| `BASE_SALARY` | Text Field | `FormatMask="$999,999,990.00"` | `<NumericFormat prefix="$" thousandSeparator>` | Currency display |
| `CHANGE_REASON` | Text Field | | `<TextField>` or `<Chip>` | Descriptive label |
| `CHANGE_PCT` | Text Field | `FormatMask="990.00%"` | `<NumericFormat suffix="%" />` | Percentage display |

### HRMS_LOGIN.xml — LOGIN Block Items

| Forms Item | Item Type | Forms Properties | React Component | Props/Notes |
|---|---|---|---|---|
| `COMPANY_LOGO` | Image | `ImageFormat="GIF"` | `<img>` or `<Avatar>` | Static asset |
| `USERNAME` | Text Field | `Required="Yes"` | `<TextField required autoFocus>` | Email-based login |
| `PASSWORD` | Text Field | `ConcealData="Yes"`, `Required="Yes"` | `<TextField type="password" required>` | Masked input |
| `ERROR_MSG` | Display Item | `ForegroundColor="red"`, `FontWeight="Bold"` | `<Alert severity="error">` | Conditional render |
| `BTN_LOGIN` | Push Button | `Label="Login"` | `<Button variant="contained" type="submit">` | Form submit |

### HRMS_LEAVE.xml — NEW_REQUEST Block Items

| Forms Item | Item Type | Forms Properties | React Component | Props/Notes |
|---|---|---|---|---|
| `NR_LEAVE_TYPE_ID` | Text Field | `LOV="LOV_LEAVE_TYPES"` | `<Select>` backed by `/api/leave/types` | Required |
| `NR_LEAVE_TYPE_DISP` | Display Item | | Auto-populated label | Inline text |
| `NR_START_DATE` | Text Field | `FormatMask="MM/DD/YYYY"` | `<DatePicker>` | Required |
| `NR_END_DATE` | Text Field | `FormatMask="MM/DD/YYYY"` | `<DatePicker>` | Required; validation: >= start |
| `NR_HALF_DAY` | Check Box | `CheckBoxMapping="Y,N"` | `<Checkbox>` / `<Switch>` | Boolean toggle |
| `NR_REASON` | Text Field | `MultiLine="Yes"`, `MaximumLength="500"` | `<TextField multiline rows={3} maxLength={500}>` | Optional |
| `NR_CALC_DAYS` | Display Item | | `<Typography>` (computed) | Auto-calculated from date range |
| `NR_BALANCE_DISP` | Display Item | | `<Typography>` (from API) | Fetched from balance endpoint |
| `BTN_SUBMIT` | Push Button | `Label="Submit Request"` | `<Button type="submit">` | Calls POST endpoint |

### HRMS_LEAVE.xml — LEAVE_REQUEST Block (List View)

| Forms Item | Item Type | React Component | Props/Notes |
|---|---|---|---|
| `REQUEST_ID` | (hidden) | N/A | Row key |
| `LEAVE_TYPE_NAME_DISP` | Display Item | `<TableCell>` | Joined from LEAVE_TYPES |
| `START_DATE` | Text Field | `<TableCell>` formatted date | `dayjs(date).format('MM/DD/YYYY')` |
| `END_DATE` | Text Field | `<TableCell>` formatted date | Same |
| `TOTAL_DAYS` | Text Field | `<TableCell>` | Numeric |
| `STATUS` | Text Field | `<Chip color={statusColor}>` | Color-coded badge |
| `REASON` | Text Field | `<TableCell>` | Truncated with tooltip |
| `BTN_CANCEL_REQUEST` | Push Button | `<IconButton>` or `<Button size="small">` | Conditional: only PENDING/APPROVED |

### HRMS_PAYROLL.xml Items

| Forms Item (Block.Item) | React Component | Props/Notes |
|---|---|---|
| `PAY_PERIOD.PERIOD_NAME` | `<TableCell>` | Data grid column |
| `PAY_PERIOD.PERIOD_START_DATE` | `<TableCell>` formatted date | |
| `PAY_PERIOD.PERIOD_END_DATE` | `<TableCell>` formatted date | |
| `PAY_PERIOD.PAY_DATE` | `<TableCell>` formatted date | |
| `PAY_PERIOD.STATUS` | `<Chip>` | Color-coded |
| `PAYROLL_RUN.RUN_TYPE` | `<TableCell>` | |
| `PAYROLL_RUN.RUN_DATE` | `<TableCell>` formatted datetime | `FormatMask="MM/DD/YYYY HH24:MI"` |
| `PAYROLL_RUN.STATUS` | `<Chip>` | Color-coded |
| `PAYROLL_RUN.EMPLOYEE_COUNT` | `<TableCell>` numeric | |
| `PAYROLL_RUN.TOTAL_GROSS` | `<TableCell>` currency | `Intl.NumberFormat('en-US', {style:'currency'})` |
| `PAYROLL_RUN.TOTAL_NET` | `<TableCell>` currency | Same |
| `BTN_CREATE_RUN` | `<Button>` | `POST /api/payroll/runs` |
| `BTN_CALCULATE` | `<Button>` | `POST /api/payroll/runs/{id}/calculate` |
| `BTN_APPROVE` | `<Button>` | `POST /api/payroll/runs/{id}/approve` |

### HRMS_PERFORMANCE.xml Items

| Forms Item (Block.Item) | React Component | Props/Notes |
|---|---|---|
| `REVIEW_CYCLE.CYCLE_NAME` | `<TableCell>` | |
| `REVIEW_CYCLE.CYCLE_YEAR` | `<TableCell>` | |
| `REVIEW_CYCLE.START_DATE` | `<TableCell>` formatted date | |
| `REVIEW_CYCLE.END_DATE` | `<TableCell>` formatted date | |
| `REVIEW_CYCLE.STATUS` | `<Chip>` | |
| `PERFORMANCE_REVIEW.EMP_NAME_DISP` | `<TableCell>` | POST-QUERY populated → API join |
| `PERFORMANCE_REVIEW.STATUS` | `<Chip>` | |
| `PERFORMANCE_REVIEW.OVERALL_RATING` | `<Rating>` (MUI) | 1.0–5.0 scale |
| `PERFORMANCE_REVIEW.SELF_ASSESSMENT` | `<TextField multiline>` or rich text editor | CLOB → long text |
| `PERFORMANCE_REVIEW.MANAGER_ASSESSMENT` | `<TextField multiline>` or rich text editor | CLOB → long text |
| `PERFORMANCE_GOAL.GOAL_TITLE` | `<TextField>` | |
| `PERFORMANCE_GOAL.GOAL_CATEGORY` | `<Select>` | BUSINESS/DEVELOPMENT/LEADERSHIP |
| `PERFORMANCE_GOAL.WEIGHT_PCT` | `<TextField type="number">` | Percentage |
| `PERFORMANCE_GOAL.PROGRESS_PCT` | `<LinearProgress>` + label | Visual progress bar |
| `PERFORMANCE_GOAL.STATUS` | `<Chip>` | |

### HRMS_MENU.xml Items

| Forms Item (Block.Item) | React Component | Props/Notes |
|---|---|---|
| `MENU_CONTROL.WELCOME_TEXT` | `<Typography variant="h4">` | Static welcome header |
| `MENU_CONTROL.USER_INFO` | `<Typography>` | Populated from auth context |
| `BTN_EMPLOYEES` | `<Card>` + `<CardActionArea>` (dashboard tile) | `onClick={() => navigate('/employees')}` |
| `BTN_PAYROLL` | `<Card>` + `<CardActionArea>` | Permission-gated |
| `BTN_LEAVE` | `<Card>` + `<CardActionArea>` | |
| `BTN_PERFORMANCE` | `<Card>` + `<CardActionArea>` | |
| `BTN_REPORTS` | `<Card>` + `<CardActionArea>` | Permission-gated |
| `BTN_LOGOUT` | `<Button>` or `<IconButton>` in AppBar | Calls `POST /api/auth/logout` |

---

## 4. Triggers → Java Service Methods & React Event Handlers

### Form-Level Triggers

| Forms Trigger | Fires When | Java Equivalent | React Equivalent |
|---|---|---|---|
| `WHEN-NEW-FORM-INSTANCE` | Form opens | N/A (handled by initial data fetch) | `useEffect(() => { fetchData() }, [])` in page component |
| `ON-ERROR` | Oracle error occurs | `@ControllerAdvice` + `@ExceptionHandler` | `axios` interceptor or React Error Boundary |
| `KEY-EXIT` | User presses Exit key | N/A | `useBeforeUnload()` hook / `<Prompt>` for unsaved changes |

### Block-Level Triggers

| Forms Trigger | Source | Java Equivalent | React Equivalent |
|---|---|---|---|
| `PRE-INSERT` (EMPLOYEE) | Before record insert | `@PrePersist` JPA callback or `EmployeeService.create()` | N/A (server-side) |
| `PRE-UPDATE` (EMPLOYEE) | Before record update | `@PreUpdate` JPA callback or `AuditingEntityListener` | N/A (server-side) |
| `POST-QUERY` (EMPLOYEE) | After each record fetched | `JOIN FETCH` in JPA query or `@Transient` + DTO mapping | Data transformation in API response / React `useMemo` |
| `POST-QUERY` (LEAVE_REQUEST) | After each record fetched | JOIN in repository query | API returns joined data |
| `POST-QUERY` (PERFORMANCE_REVIEW) | After each record fetched | JOIN in repository query | API returns joined data |
| `WHEN-VALIDATE-ITEM` (EMPLOYEE) | Field loses focus | `@Valid` + Jakarta Bean Validation annotations | `react-hook-form` field-level validation / `onBlur` handler |

### Item-Level Triggers

| Forms Trigger | Item | Java Equivalent | React Equivalent |
|---|---|---|---|
| `WHEN-BUTTON-PRESSED` (BTN_LOGIN) | Login button click | `AuthService.authenticate()` | `onSubmit` form handler → `POST /api/auth/login` |
| `WHEN-BUTTON-PRESSED` (BTN_SUBMIT) | Leave submit click | `LeaveService.submitRequest()` | `onSubmit` form handler → `POST /api/leave/requests` |
| `WHEN-BUTTON-PRESSED` (BTN_CANCEL_REQUEST) | Cancel leave click | `LeaveService.cancelRequest()` | `onClick` → confirmation dialog → `PUT /api/leave/requests/{id}/cancel` |
| `WHEN-BUTTON-PRESSED` (BTN_CREATE_RUN) | Create payroll run | `PayrollService.createRun()` | `onClick` → `POST /api/payroll/runs` |
| `WHEN-BUTTON-PRESSED` (BTN_CALCULATE) | Calculate payroll | `PayrollService.calculate()` | `onClick` → `POST /api/payroll/runs/{id}/calculate` (async) |
| `WHEN-BUTTON-PRESSED` (BTN_APPROVE) | Approve payroll | `PayrollService.approve()` | `onClick` → confirmation → `POST /api/payroll/runs/{id}/approve` |
| `WHEN-BUTTON-PRESSED` (BTN_EMPLOYEES) | Nav to employee module | N/A | `navigate('/employees')` |
| `WHEN-BUTTON-PRESSED` (BTN_LOGOUT) | Logout | `AuthService.logout()` | `onClick` → `POST /api/auth/logout` → redirect `/login` |
| `KEY-NEXT-ITEM` (LOGIN block) | Tab/Enter on password field | N/A | Form `onSubmit` handles Enter key natively |

### Validation Trigger Mapping (WHEN-VALIDATE-ITEM on EMPLOYEE)

| Validated Item | Forms Validation Logic | Java Validation | React Validation |
|---|---|---|---|
| `EMPLOYEE.EMAIL` | `PKG_VALIDATION.validate_email_format()` | `@Email` annotation on `Employee.email` | `react-hook-form` pattern: `/^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/` |
| `EMPLOYEE.HIRE_DATE` | `> SYSDATE + 90` check | Custom `@FutureWithinDays(90)` validator | `maxDate={addDays(new Date(), 90)}` on DatePicker |
| `EMPLOYEE.DEPT_ID` | SELECT from DEPARTMENTS with ACTIVE_FLAG check | `@DepartmentExists` custom constraint | Autocomplete only shows active departments |
| `EMPLOYEE.JOB_ID` | SELECT from JOB_TITLES with ACTIVE_FLAG check | `@JobTitleExists` custom constraint | Autocomplete only shows active job titles |

---

## 5. LOVs & Record Groups → REST Endpoints & Select Components

| LOV Name | Record Group | SQL Query | REST Endpoint | React Component |
|---|---|---|---|---|
| `LOV_DEPARTMENTS` | `RG_DEPARTMENTS` | `SELECT DEPT_ID, DEPT_CODE, DEPT_NAME, COST_CENTER FROM DEPARTMENTS WHERE ACTIVE_FLAG='Y' ORDER BY DEPT_NAME` | `GET /api/departments?active=true` | `<Autocomplete options={departments} getOptionLabel={d => d.deptName} />` |
| `LOV_JOB_TITLES` | `RG_JOB_TITLES` | `SELECT j.JOB_ID, j.JOB_CODE, j.JOB_TITLE, g.GRADE_NAME FROM JOB_TITLES j JOIN JOB_GRADES g ON j.GRADE_ID=g.GRADE_ID WHERE j.ACTIVE_FLAG='Y'` | `GET /api/job-titles?active=true` | `<Autocomplete options={jobTitles} getOptionLabel={j => j.jobTitle} />` |
| `LOV_MANAGERS` | `RG_MANAGERS` | `SELECT EMP_ID, EMP_NUMBER, FIRST_NAME||' '||LAST_NAME FROM EMPLOYEES WHERE EMPLOYMENT_STATUS='ACTIVE'` | `GET /api/employees?status=ACTIVE&fields=id,number,name` | `<Autocomplete options={managers} getOptionLabel={m => m.name} />` |
| `LOV_LOCATIONS` | `RG_LOCATIONS` | `SELECT LOCATION_CODE, LOCATION_NAME, CITY, STATE_PROVINCE FROM LOCATIONS WHERE ACTIVE_FLAG='Y'` | `GET /api/locations?active=true` | `<Autocomplete options={locations} getOptionLabel={l => l.locationName} />` |
| `LOV_LEAVE_TYPES` | `RG_LEAVE_TYPES` | `SELECT LEAVE_TYPE_ID, LEAVE_TYPE_CODE, LEAVE_TYPE_NAME FROM LEAVE_TYPES WHERE ACTIVE_FLAG='Y'` | `GET /api/leave/types?active=true` | `<Select options={leaveTypes} />` |

### Column Mapping → Return Value Binding

| LOV | Forms Column Mapping | React Equivalent |
|---|---|---|
| `LOV_DEPARTMENTS` | `DEPT_ID → EMPLOYEE.DEPT_ID`, `DEPT_NAME → EMPLOYEE.DEPT_NAME_DISP` | `onChange={(e, dept) => { setValue('deptId', dept.id); }}` — display name auto-resolves from selected object |
| `LOV_JOB_TITLES` | `JOB_ID → EMPLOYEE.JOB_ID`, `JOB_TITLE → EMPLOYEE.JOB_TITLE_DISP` | Same pattern — object selection provides both ID and label |
| `LOV_MANAGERS` | `EMP_ID → EMPLOYEE.MANAGER_EMP_ID`, `MANAGER_NAME → EMPLOYEE.MANAGER_NAME_DISP` | Same pattern |
| `LOV_LOCATIONS` | `LOCATION_CODE → EMPLOYEE.LOCATION_CODE` | `onChange={(e, loc) => { setValue('locationCode', loc.code); }}` |
| `LOV_LEAVE_TYPES` | `LEAVE_TYPE_ID → NEW_REQUEST.NR_LEAVE_TYPE_ID`, `LEAVE_TYPE_NAME → NEW_REQUEST.NR_LEAVE_TYPE_DISP` | `onChange={(e) => { setValue('leaveTypeId', e.target.value); }}` |

---

## 6. Canvases, Windows & Tab Pages → React Routes & Layouts

### HRMS_EMPLOYEE.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_MAIN` | Tab Canvas | `<Tabs>` + `<TabPanel>` (MUI) |
| `TP_PERSONAL` | Tab Page ("Personal Information") | `<TabPanel value={0}>` → `<PersonalInfoForm>` |
| `TP_JOB` | Tab Page ("Job & Compensation") | `<TabPanel value={1}>` → `<JobCompensationForm>` |
| `TP_DEPENDENTS` | Tab Page ("Dependents") | `<TabPanel value={2}>` → `<DependentsList>` |
| `TP_HISTORY` | Tab Page ("Employment History") | `<TabPanel value={3}>` → `<EmploymentHistoryTable>` |
| `CVS_TOOLBAR` | Horizontal Toolbar Canvas | `<AppBar position="static">` with action buttons |
| `WIN_EMPLOYEE` | Document Window (720x550) | Page layout component with max-width container |

### HRMS_LEAVE.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_MAIN` | Tab Canvas | `<Tabs>` + `<TabPanel>` |
| `TP_MY_REQUESTS` | Tab Page ("My Requests") | `<TabPanel>` → `<LeaveRequestsTable>` |
| `TP_NEW_REQUEST` | Tab Page ("Submit Request") | `<TabPanel>` → `<NewLeaveRequestForm>` |
| `TP_APPROVALS` | Tab Page ("Pending Approvals") | `<TabPanel>` → `<PendingApprovalsTable>` |
| `TP_CALENDAR` | Tab Page ("Team Calendar") | `<TabPanel>` → `<TeamCalendar>` (FullCalendar.io) |
| `WIN_LEAVE` | Document Window (720x520) | Page layout component |

### HRMS_PAYROLL.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_MAIN` | Tab Canvas | `<Tabs>` + `<TabPanel>` |
| `TP_PERIODS` | Tab Page ("Pay Periods") | `<TabPanel>` → `<PayPeriodsTable>` |
| `TP_RUNS` | Tab Page ("Payroll Runs") | `<TabPanel>` → `<PayrollRunsTable>` |
| `TP_DETAILS` | Tab Page ("Pay Details") | `<TabPanel>` → `<PayDetailsTable>` |
| `WIN_PAYROLL` | Document Window (770x560) | Page layout component |

### HRMS_PERFORMANCE.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_MAIN` | Tab Canvas | `<Tabs>` + `<TabPanel>` |
| `TP_CYCLES` | Tab Page ("Review Cycles") | `<TabPanel>` → `<ReviewCyclesTable>` |
| `TP_REVIEWS` | Tab Page ("My Reviews") | `<TabPanel>` → `<MyReviewsList>` |
| `TP_GOALS` | Tab Page ("Goals") | `<TabPanel>` → `<GoalsTracker>` |
| `WIN_PERFORMANCE` | Document Window (770x560) | Page layout component |

### HRMS_LOGIN.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_LOGIN` | Content Canvas (700x300) | Centered `<Card>` with `<CardContent>` |
| `WIN_LOGIN` | Dialog Window (700x320, non-resizable) | Full-page centered layout; no chrome |

### HRMS_MENU.xml

| Forms Element | Type | React Equivalent |
|---|---|---|
| `CVS_MAIN` | Content Canvas (740x400) | Dashboard grid layout |
| `WIN_MAIN` | Document Window (760x420) | `<AppShell>` wrapper (sidebar + content area) |
| `MENU_MAIN` (MenuModule) | Menu bar with 4 menus | `<Sidebar>` with `<List>` items or `<AppBar>` with `<Menu>` |

---

## 7. Alerts → Dialog/Modal Components

| Forms Alert | Alert Style | React Equivalent | Trigger Context |
|---|---|---|---|
| `ALT_CONFIRM_EXIT` (EMPLOYEE) | Caution; 3 buttons (Save/Discard/Cancel) | `<Dialog>` with 3 `<Button>` actions | Unsaved changes on navigation |
| `ALT_CONFIRM_DELETE` (EMPLOYEE) | Stop; 2 buttons (Yes/No) | `<Dialog>` with destructive action styling | Employee delete attempt |
| `ALT_CONFIRM_CANCEL` (LEAVE) | Caution; 2 buttons (Yes/No) | `<Dialog>` with confirmation prompt | Leave request cancellation |

### React Implementation Pattern

```
// Shared confirmation dialog hook
const { confirm, ConfirmDialog } = useConfirmDialog();

// Usage
const handleCancel = async () => {
  const confirmed = await confirm({
    title: 'Confirm Cancellation',
    message: 'Are you sure you want to cancel this leave request?',
    confirmLabel: 'Yes',
    cancelLabel: 'No',
    severity: 'warning',
  });
  if (confirmed) { /* proceed */ }
};
```

---

## 8. Menu Module → Navigation Components

### Top-Level Menu Bar (MAIN_MENUBAR from HRMS_MENU.mmb.sql)

| Forms Menu | Menu Items | React Component | Permission Gate |
|---|---|---|---|
| **File** | Save, Save & Exit, Print, Exit | N/A (individual page actions) | Per-page |
| **Edit** | Clear/Duplicate/Delete/Insert Record | Per-form action buttons | Per-entity |
| **Query** | Enter Query, Execute Query, Cancel, Count, Fetch Next | Search bar + filters + pagination | N/A |
| **Navigate** | First/Prev/Next/Last Record, Prev/Next Block | Pagination controls (`<TablePagination>`) | N/A |
| **Modules** | Employee, Payroll, Leave, Performance, Reports, Admin | `<Sidebar>` `<NavLink>` items | `PKG_SECURITY.has_permission` → `usePermission()` hook |
| **Admin** | Change Password, System Parameters, User Management | Settings section in sidebar | `has_permission('ADMIN', 'VIEW')` |
| **Help** | Contents, About, Support | Footer links or help menu | N/A |

### Module Menu Items → Sidebar Navigation

| Forms MenuItem | Command | React Route | Permission Check |
|---|---|---|---|
| `MI_EMPLOYEES` | `OPEN_FORM('HRMS_EMPLOYEE')` | `/employees` | Always visible |
| `MI_PAYROLL` | `OPEN_FORM('HRMS_PAYROLL')` | `/payroll` | `has_permission(empId, 'PAYROLL', 'VIEW')` |
| `MI_LEAVE` | `OPEN_FORM('HRMS_LEAVE')` | `/leave` | Always visible |
| `MI_PERFORMANCE` | `OPEN_FORM('HRMS_PERFORMANCE')` | `/performance` | Always visible |
| `MI_REPORTS` | `OPEN_FORM('HRMS_REPORTS')` | `/reports` | `has_permission(empId, 'REPORTS', 'VIEW')` |
| `MI_ADMIN` | `OPEN_FORM('HRMS_ADMIN')` | `/admin` | `has_permission(empId, 'ADMIN', 'VIEW')` |
| `MI_CHANGE_PWD` | `SHOW_WINDOW('WIN_CHANGE_PWD')` | Modal dialog from user menu | Always visible |
| `MI_LOGOUT` | `PKG_SECURITY.logout(); EXIT_FORM` | User menu → Logout | Always visible |
| `MI_ABOUT` | `MESSAGE('HRMS v4.2')` | About dialog in help menu | Always visible |

---

## 9. PL/SQL Libraries → Java Shared Libraries

### HRMS_COMMON_LIB.pll.sql

| PLL Procedure/Function | Java Equivalent | React Equivalent |
|---|---|---|
| `handle_error(p_module, p_location)` | `@ControllerAdvice` + `ErrorService.logAndThrow()` | Axios response interceptor + `toast.error()` |
| `toolbar_save` | N/A (REST PUT/POST) | Form `onSubmit` handler |
| `toolbar_clear` | N/A | Form `reset()` method |
| `toolbar_query` | N/A (search is always available) | Search/filter component |
| `toolbar_first/prev/next/last` | Pagination params in API | `<TablePagination>` component |
| `toolbar_insert` | N/A (navigate to create form) | `navigate('/employees/new')` |
| `toolbar_delete` | `DELETE /api/employees/{id}` | Delete button with confirmation |
| `toolbar_exit` | N/A | `navigate(-1)` or close tab |
| `format_date(p_date)` | `DateTimeFormatter.ofPattern("MM/dd/yyyy")` | `dayjs(date).format('MM/DD/YYYY')` |
| `format_datetime(p_date)` | `DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")` | `dayjs(date).format('MM/DD/YYYY HH:mm:ss')` |
| `get_current_user` | `SecurityContextHolder.getContext().getAuthentication()` | `useAuth()` hook |
| `get_session_id` | JWT token (stateless) | `useAuth()` hook returns token |
| `check_session` | Spring Security filter chain (automatic) | Axios interceptor checks 401 |
| `refresh_lov(p_lov_name)` | N/A (React Query auto-refetch) | `queryClient.invalidateQueries(['departments'])` |

### HRMS_VALIDATION_LIB.pll.sql

| PLL Function | Java Equivalent | React Equivalent |
|---|---|---|
| `validate_email(p_email)` | `@Email` Jakarta annotation | `react-hook-form` pattern rule |
| `validate_phone(p_phone)` | Custom `@USPhone` annotation | Pattern: `/^\d{10,11}$/` on stripped input |
| `validate_ssn(p_ssn)` | Custom `@SSN` annotation | Pattern validation + segment checks |
| `validate_date_not_future(p_date)` | `@PastOrPresent` Jakarta annotation | `maxDate={new Date()}` on DatePicker |
| `validate_salary_range(p_salary, p_grade_id)` | `SalaryValidator.validate(salary, gradeId)` | Async validation via `GET /api/job-grades/{id}/salary-range` |

---

## 10. PL/SQL Packages → Spring Services

| PL/SQL Package | Spring Service Class | Spring Repository | Key Methods |
|---|---|---|---|
| `PKG_COMMON` | `CommonService.java` | N/A (utility) | `logError()`, `getParam()`, `businessDaysBetween()`, `formatCurrency()` |
| `PKG_AUDIT` | `AuditService.java` | `AuditLogRepository` | `logAction()`, `purgeOldRecords()`, `getChangeHistory()` |
| `PKG_VALIDATION` | `ValidationService.java` | N/A | `validateDateRange()`, `validateSalaryForGrade()`, `validateEmailFormat()` |
| `PKG_SECURITY` | `AuthService.java` + `SecurityConfig.java` | `UserSessionRepository`, `EmployeeRepository` | `authenticate()`, `logout()`, `isSessionValid()`, `hasPermission()` |
| `PKG_EMPLOYEE` | `EmployeeService.java` | `EmployeeRepository`, `EmployeeHistoryRepository` | `createEmployee()`, `updateEmployee()`, `transferEmployee()`, `terminateEmployee()` |
| `PKG_PAYROLL` | `PayrollService.java` + `TaxCalculationService.java` | `SalaryRecordRepository`, `PayrollRunRepository`, `PayrollDetailRepository` | `createSalaryRecord()`, `calculatePayroll()`, `calculateFederalTax()` |
| `PKG_LEAVE` | `LeaveService.java` | `LeaveRequestRepository`, `LeaveBalanceRepository` | `submitLeaveRequest()`, `approveRequest()`, `runMonthlyAccrual()` |
| `PKG_PERFORMANCE` | `PerformanceService.java` | `ReviewCycleRepository`, `PerformanceReviewRepository`, `GoalRepository` | `createReviewCycle()`, `submitSelfAssessment()`, `addGoal()` |
| `PKG_REPORTING` | `ReportService.java` | Custom `@Query` methods | `headcountReport()`, `compensationSummary()`, `turnoverReport()` |
| `PKG_NOTIFICATION` | `NotificationService.java` | `NotificationQueueRepository` | `sendNotification()`, `processQueue()`, `retryFailed()` |
| `PKG_INTEGRATION` | `IntegrationService.java` | N/A (external API calls) | `generateGlJournal()`, `exportBenefitsFeed()`, `importTimeAttendance()` |

### Package Variable → Spring Equivalent

| PL/SQL Package Variable | Spring Equivalent |
|---|---|
| `PKG_EMPLOYEE.g_current_user` | `SecurityContextHolder.getContext().getAuthentication().getName()` |
| `PKG_EMPLOYEE.g_current_emp_id` | Custom `@CurrentEmployeeId` annotation resolving from JWT claims |
| `PKG_EMPLOYEE.g_debug_mode` | `logging.level.com.hrms.employee=DEBUG` in application.yml |
| `PKG_SECURITY.c_encryption_key` (hard-coded) | Environment variable / secrets manager (`HRMS_ENCRYPTION_KEY`) |
| `PKG_SECURITY.c_session_timeout_min` | `server.servlet.session.timeout=30m` or JWT expiry claim |
| `PKG_PAYROLL.c_ss_wage_base_2024` (hard-coded) | `TAX_BRACKETS` table query or `@ConfigurationProperties` |

---

## 11. Database Objects → JPA/Hibernate Mappings

### Tables → JPA Entities

| Oracle Table | JPA Entity | Key Annotations |
|---|---|---|
| `HRMS.EMPLOYEES` | `Employee` | `@Entity`, `@Table(name="employees")`, `@Audited` (Envers) |
| `HRMS.DEPARTMENTS` | `Department` | `@Entity`, self-referencing `@ManyToOne` for `parentDeptId` |
| `HRMS.LOCATIONS` | `Location` | `@Entity`, `@Id` on `locationCode` (natural key) |
| `HRMS.JOB_GRADES` | `JobGrade` | `@Entity` |
| `HRMS.JOB_TITLES` | `JobTitle` | `@Entity`, `@ManyToOne` to `JobGrade` |
| `HRMS.EMPLOYEE_HISTORY` | `EmployeeHistory` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.EMPLOYEE_DEPENDENTS` | `EmployeeDependent` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.EMERGENCY_CONTACTS` | `EmergencyContact` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.SALARY_RECORDS` | `SalaryRecord` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.PAY_ELEMENTS` | `PayElement` | `@Entity` |
| `HRMS.EMPLOYEE_PAY_ELEMENTS` | `EmployeePayElement` | `@Entity`, composite FK to Employee + PayElement |
| `HRMS.PAY_PERIODS` | `PayPeriod` | `@Entity` |
| `HRMS.PAYROLL_RUNS` | `PayrollRun` | `@Entity`, `@ManyToOne` to `PayPeriod` |
| `HRMS.PAYROLL_DETAILS` | `PayrollDetail` | `@Entity`, `@ManyToOne` to `PayrollRun` + `Employee` |
| `HRMS.TAX_BRACKETS` | `TaxBracket` | `@Entity` |
| `HRMS.EMPLOYEE_TAX_INFO` | `EmployeeTaxInfo` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.EMPLOYEE_BANK_ACCOUNTS` | `EmployeeBankAccount` | `@Entity`, `@ManyToOne` to `Employee` |
| `HRMS.LEAVE_TYPES` | `LeaveType` | `@Entity` |
| `HRMS.LEAVE_BALANCES` | `LeaveBalance` | `@Entity`, computed `available` → `@Formula` or `@Transient` |
| `HRMS.LEAVE_REQUESTS` | `LeaveRequest` | `@Entity`, `@ManyToOne` to `Employee` + `LeaveType` |
| `HRMS.LEAVE_ACCRUAL_LOG` | `LeaveAccrualLog` | `@Entity` |
| `HRMS.HOLIDAYS` | `Holiday` | `@Entity` |
| `HRMS.REVIEW_CYCLES` | `ReviewCycle` | `@Entity` |
| `HRMS.PERFORMANCE_REVIEWS` | `PerformanceReview` | `@Entity`, CLOB fields → `@Lob` |
| `HRMS.PERFORMANCE_GOALS` | `PerformanceGoal` | `@Entity` |
| `HRMS.AUDIT_LOG` | `AuditLog` | `@Entity` |
| `HRMS.SYSTEM_PARAMETERS` | `SystemParameter` | `@Entity` (or Spring `@ConfigurationProperties`) |
| `HRMS.NOTIFICATION_QUEUE` | `Notification` | `@Entity` |
| `HRMS.USER_SESSIONS` | `UserSession` | `@Entity` (or replaced by JWT — stateless) |
| `HRMS.LOOKUP_VALUES` | `LookupValue` | `@Entity` |

### Sequences → ID Generation

| Oracle Sequence | JPA Strategy |
|---|---|
| `SEQ_EMPLOYEE` | `@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq")` |
| `SEQ_SALARY`, `SEQ_PAY_PERIOD`, etc. | Same pattern per entity |
| `generate_emp_number()` (MAX+1 race condition) | `@PrePersist` callback with DB sequence-based generation (eliminates race condition) |

### Triggers → JPA Callbacks / Event Listeners

| Oracle Trigger | JPA Equivalent |
|---|---|
| `TRG_EMP_BEFORE_INSERT` | `@PrePersist` on `Employee` entity + `AuditingEntityListener` |
| `TRG_EMP_BEFORE_UPDATE` | `@PreUpdate` on `Employee` entity + `AuditingEntityListener` |
| `TRG_EMP_INSTEAD_OF_DELETE` | `SoftDeleteRepository` pattern: override `delete()` to set `activeFlag='N'` |
| `TRG_AUDIT_*` | `@EntityListeners(AuditingEntityListener.class)` + Hibernate Envers `@Audited` |

### Views → JPA Projections or Named Queries

| Oracle View | JPA Equivalent |
|---|---|
| `VW_ACTIVE_EMPLOYEES` | `EmployeeRepository.findAllActive()` with `@Query` |
| `VW_ORG_HIERARCHY` | Recursive CTE in `@Query` or `EmployeeService.getOrgChart()` |
| `VW_EMPLOYEE_COMPENSATION` | DTO projection joining Employee + SalaryRecord |
| `VW_LEAVE_SUMMARY` | DTO projection from LeaveBalance aggregation |
| `VW_PAYROLL_LATEST` | `PayrollRunRepository.findLatestByPeriod()` |

---

## 12. Built-in Forms Functions → Java/React Equivalents

| Oracle Forms Built-in | Purpose | Java/React Equivalent |
|---|---|---|
| `COMMIT_FORM` | Save all pending changes | `repository.save()` / form `onSubmit` |
| `EXECUTE_QUERY` | Fetch records from DB | `repository.findAll(spec)` / `useQuery()` |
| `ENTER_QUERY` | Enter query-by-example mode | Search/filter panel |
| `CLEAR_FORM(NO_VALIDATE)` | Discard changes | `form.reset()` |
| `EXIT_FORM` | Close current form | `navigate(-1)` or `navigate('/')` |
| `OPEN_FORM('X', ACTIVATE, SESSION)` | Open sub-form sharing session | `navigate('/x')` (SPA routing, shared auth context) |
| `CREATE_RECORD` | Insert blank row | Navigate to `/entity/new` or open inline form |
| `DELETE_RECORD` | Delete current record | `DELETE /api/entity/{id}` + optimistic UI update |
| `FIRST_RECORD` / `LAST_RECORD` | Navigate to first/last row | Pagination: page 1 / last page |
| `NEXT_RECORD` / `PREVIOUS_RECORD` | Navigate records | Table row selection or pagination |
| `SET_BLOCK_PROPERTY(..., DEFAULT_WHERE)` | Set default query filter | Default query params in `useQuery` |
| `SET_BLOCK_PROPERTY(..., INSERT_ALLOWED, PROPERTY_FALSE)` | Disable inserts | `<Button disabled={!canCreate}>` |
| `SET_WINDOW_PROPERTY(FORMS_MDI_WINDOW, TITLE)` | Set window title | `document.title = ...` or `useDocumentTitle()` |
| `SET_MENU_ITEM_PROPERTY(..., ENABLED, PROPERTY_FALSE)` | Disable menu item | `<NavLink>` with `disabled` prop based on `usePermission()` |
| `SHOW_ALERT('X')` | Show confirmation dialog | `useConfirmDialog()` custom hook |
| `MESSAGE('text')` | Status bar message | `toast.info('text')` or `<Snackbar>` |
| `POPULATE_GROUP('RG_X')` | Refresh record group for LOV | `queryClient.invalidateQueries(['lov-x'])` |
| `GO_BLOCK('X')` | Navigate to data block | Tab change: `setActiveTab(tabIndex)` |
| `GO_ITEM('X.Y')` | Move cursor to specific item | `inputRef.current.focus()` |
| `GET_APPLICATION_PROPERTY(USERNAME)` | Get Forms session user | `useAuth().user.username` |
| `GET_APPLICATION_PROPERTY(CLIENT_HOST)` | Get client IP | Server reads from request headers |
| `:GLOBAL.variable` | Global form variable | React Context or Redux store |
| `:SYSTEM.FORM_STATUS` | Dirty check | `react-hook-form` `formState.isDirty` |
| `:SYSTEM.TRIGGER_ITEM` | Current item being validated | `react-hook-form` field name in validation rule |
| `:SYSTEM.CURSOR_ITEM` | Current focused item | `document.activeElement` |
| `:SYSTEM.MODE` | Normal/Enter-Query mode | N/A (search is separate UI) |
| `RAISE FORM_TRIGGER_FAILURE` | Abort current operation | `throw new ValidationException()` / `setError()` in form |
| `SYNCHRONIZE` | Force UI refresh | N/A (React renders automatically) |
| `WEB.SHOW_DOCUMENT` | Open URL in browser | `window.open(url)` |
| `RUN_PRODUCT` | Launch Oracle Reports | Navigate to report page / trigger report generation API |
