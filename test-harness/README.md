# Test Harness: Legacy COBOL/CICS ↔ Modern Java Equivalence Testing

## Purpose

This test harness validates that a modernized Java system produces functionally equivalent results to the legacy CardDemo COBOL/CICS/VSAM system. It captures the legacy system's behavior as executable specifications and runs them against both implementations.

## How It Works

1. **Scenario Definition** (YAML): Business scenarios describe inputs and expected outputs
2. **Legacy Execution**: Scenarios run against the COBOL batch programs and CICS transactions
3. **Modern Execution**: Same scenarios run against the Java REST API / Spring Batch jobs
4. **Comparison**: Results are compared field-by-field with configurable tolerances
5. **Reconciliation**: Per-module checks validate internal consistency (balances, totals, counts)

## Directory Structure

```
test-harness/
├── README.md
├── scenarios/                           # Business scenario definitions (YAML)
│   ├── terminated-employee-payroll-exclusion.yaml
│   ├── overlapping-leave-requests.yaml
│   └── overtime-with-holidays.yaml
├── comparators/                         # Result comparison tools
│   └── result-comparator.py            # Enhanced comparator with tolerance & wildcard support
├── reconciliation/                      # Per-module reconciliation checks
│   └── reconciliation-checker.py       # Payroll/leave/employee count validation
└── results/                             # Execution results (populated at runtime)
    ├── legacy/                          # Results from legacy COBOL system
    └── modern/                          # Results from modern Java system
```

## Running Tests

```bash
# Compare legacy vs modern results
python test-harness/comparators/result-comparator.py \
    --legacy test-harness/results/legacy/ \
    --modern test-harness/results/modern/

# Compare with custom tolerance overrides
python test-harness/comparators/result-comparator.py \
    --legacy test-harness/results/legacy/ \
    --modern test-harness/results/modern/ \
    --tolerance-config tolerances.json

# Run reconciliation checks on modern results
python test-harness/reconciliation/reconciliation-checker.py \
    --results test-harness/results/modern/

# Compare reconciliation across both systems
python test-harness/reconciliation/reconciliation-checker.py \
    --results test-harness/results/legacy/ \
    --results test-harness/results/modern/ \
    --compare
```

## Scenario Structure

```yaml
name: scenario-name
description: What this scenario tests
module: payroll | leave | employee
legacy_program: CBTRN02C          # Legacy COBOL program
modern_endpoint: /api/payroll      # Modern REST endpoint

steps:
  - name: Description of step
    action: create_employee
    input:
      field: value
    expect:
      status: "ACTIVE"
      amount: "> 0"               # Comparison operators
      emp_number: "EMP-*"         # Wildcard matching
    tolerance:
      net_pay: 0.01               # Per-field tolerance
    save_as: variable_name         # Reference in later steps
```

## Business Scenarios

### Edge-Case Scenarios

| Scenario | File | Description |
|----------|------|-------------|
| Terminated Employee Payroll Exclusion | `terminated-employee-payroll-exclusion.yaml` | Validates terminated employees are excluded from payroll, prorated pay for termination period, and rehire inclusion |
| Overlapping Leave Requests | `overlapping-leave-requests.yaml` | Tests full/partial overlap rejection, cross-type overlap handling, balance constraints, and cancellation-then-resubmit |
| Overtime with Holidays | `overtime-with-holidays.yaml` | Tests holiday premium rates, overtime stacking with holidays, double-time for holidays worked, and gross-to-net calculation |

## Comparison Tolerances

The enhanced comparator supports multiple tolerance mechanisms:

| Type | Description | Example |
|------|-------------|---------|
| **Exact match** | Status fields, codes, flags | `employment_status: "ACTIVE"` |
| **Financial tolerance** | Currency amounts (±$0.01) | Built-in for `*_pay`, `*_tax`, `salary`, etc. |
| **Percentage tolerance** | Rate fields (±0.1%) | Built-in for `*_rate` fields |
| **Wildcard** | Auto-generated IDs, patterns | `emp_number: "EMP-*"` |
| **Regex** | Complex patterns | `transaction_id: "/^TXN-\d{8}$/"` |
| **Comparison operators** | Threshold checks | `count: ">= 1"`, `total: "> 0"` |
| **Custom** | Per-step or config file overrides | `tolerance: { net_pay: 0.05 }` |

### Tolerance Priority

1. Per-step `tolerance:` block in the scenario YAML
2. Custom `--tolerance-config` file
3. Built-in financial tolerance rules
4. Exact match (tolerance = 0)

## Reconciliation Checks

The reconciliation checker validates internal consistency per module:

### Payroll Module
- Sum of individual employee gross amounts == reported `total_gross`
- Sum of individual employee net amounts == reported `total_net`
- Per-employee: `gross_pay - total_deductions == net_pay`
- Aggregate: `total_gross - total_deductions == total_net`
- `employee_count` matches actual number of pay stubs
- Pay component sub-totals (regular + overtime + holiday + ...) == `gross_pay`

### Leave Module
- `used + remaining == total_entitled` for each leave type
- Business days between start/end dates match `total_days`
- Balance changes after approval/cancellation are consistent

### Employee Module (Cross-Module)
- All employee IDs in payroll exist in employee master
- All employee IDs in leave exist in employee master
- Terminated employees are excluded from payroll runs

## Legacy-to-Modern Mapping

| Legacy (COBOL/CICS) | Modern (Java/Spring) |
|---------------------|---------------------|
| `CBTRN02C` (Transaction Posting) | Payroll Service / Spring Batch |
| `CBACT04C` (Interest Calc) | Calculation Service |
| `COTRN02C` (Transaction Add) | Transaction REST API |
| `COACTUPC` (Account Update) | Employee REST API |
| VSAM KSDS files | JPA / Relational DB |
| COBOL WORKING-STORAGE | Java DTOs / Entities |
| JCL batch cycle | Spring Batch job chain |
