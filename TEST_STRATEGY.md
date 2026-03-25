# CardDemo Migration Test Strategy

This document defines the four-dimensional testing approach used to validate the
migration of the CardDemo COBOL/CICS/VSAM mainframe application to Java.

---

## 1. Golden-File Testing

**Purpose:** Establish a known-correct baseline from the legacy system's data files
so that every field parsed by the Java replacement can be compared byte-for-byte
against an authoritative reference.

### How It Works

1. Each ASCII flat file shipped in `app/data/ASCII/` is parsed according to its
   COBOL copybook layout (found in `app/cpy/`).
2. The parser emits one JSON file per data source into the `golden-files/`
   directory.  Every record is a JSON object whose keys match the copybook
   field names and whose values are trimmed, typed representations of the
   original fixed-width data.
3. Tests load the golden JSON, run the equivalent Java loader / DTO mapper, and
   assert field-level equality.

### Golden-File Inventory

| Source File | Copybook | Golden File | Record Length |
|---|---|---|---|
| `acctdata.txt` | `CVACT01Y.cpy` (ACCOUNT-RECORD) | `acctdata.json` | 300 |
| `carddata.txt` | `CVACT02Y.cpy` (CARD-RECORD) | `carddata.json` | 150 |
| `custdata.txt` | `CVCUS01Y.cpy` (CUSTOMER-RECORD) | `custdata.json` | 500 |
| `cardxref.txt` | `CVACT03Y.cpy` (CARD-XREF-RECORD) | `cardxref.json` | 50 |
| `dailytran.txt` | `CVTRA05Y.cpy` (TRAN-RECORD) | `dailytran.json` | 350 |
| `trantype.txt` | `CVTRA03Y.cpy` (TRAN-TYPE-RECORD) | `trantype.json` | 60 |
| `trancatg.txt` | `CVTRA04Y.cpy` (TRAN-CAT-RECORD) | `trancatg.json` | 60 |
| `tcatbal.txt` | `CVTRA01Y.cpy` (TRAN-CAT-BAL-RECORD) | `tcatbal.json` | 50 |
| `discgrp.txt` | `CVTRA02Y.cpy` (DIS-GROUP-RECORD) | `discgrp.json` | 50 |

### Acceptance Criteria

- Every field in the golden JSON must match the Java-parsed output exactly
  (after trimming trailing spaces for PIC X fields).
- Signed numeric fields (PIC S9...V99) must agree on sign and decimal placement.
- FILLER bytes are captured in golden files for completeness but excluded from
  comparison by default.

---

## 2. Differential Testing

**Purpose:** Run both the legacy COBOL batch programs and the new Java batch jobs
against the same input data set and compare their outputs record-by-record.

### Approach

```
                     +------------------+
  Input Data ------->| COBOL Batch Job  |-------> Legacy Output
       |             +------------------+
       |
       +------------>| Java Batch Job   |-------> Migrated Output
                     +------------------+

                  diff(Legacy Output, Migrated Output) == 0
```

1. **Identical Inputs:** Both systems consume the same ASCII data files from
   `app/data/ASCII/`.
2. **Capture Legacy Output:** If a mainframe environment is available, run the
   JCL job and capture the output datasets.  Otherwise, use the golden-file
   JSON as the proxy for legacy output.
3. **Run Java Job:** Execute the equivalent Spring Batch (or scheduled) job.
4. **Field-Level Diff:** The `test-harness/comparator.py` module performs a
   structured diff that:
   - Ignores whitespace-only differences in PIC X fields.
   - Compares numeric fields with configurable tolerance (default `0.00`).
   - Reports mismatches per field, per record, with row index.

### Jobs Covered

| COBOL Program | JCL Job | Java Equivalent | Key Outputs |
|---|---|---|---|
| `CBTRN02C` | `POSTTRAN.jcl` | Transaction Posting Service | Updated TRANSACT, TCATBAL, DALYREJS |
| `CBACT04C` | `INTCALC.jcl` | Interest Calculation Service | Updated ACCTDATA, new SYSTRAN records |
| `CBSTM03A/B` | `CREASTMT.JCL` | Statement Generation Service | STATEMNT.PS, STATEMNT.HTML |
| `CBTRN03C` | `TRANREPT.jcl` | Transaction Report Service | Report output |
| SORT + IDCAMS | `COMBTRAN.jcl` | Transaction Combine Service | Merged TRANSACT VSAM |
| REPRO + IDCAMS | `TRANBKP.jcl` | Transaction Backup Service | Backup sequential file |

### Tolerance Rules

| Field Type | Default Tolerance | Notes |
|---|---|---|
| PIC X (alphanumeric) | Exact (after trim) | Trailing spaces ignored |
| PIC 9 (unsigned integer) | Exact | Leading zeros preserved in comparison |
| PIC S9(n)V99 (signed decimal) | +/- 0.00 | Configurable per-job |
| Timestamps (PIC X(26)) | Exact | Unless marked volatile |

---

## 3. Reconciliation Testing

**Purpose:** Validate business-rule invariants that must hold true after each
batch job completes, regardless of whether the output was produced by COBOL or
Java.

### Reconciliation Dimensions

| Dimension | Description |
|---|---|
| **Record Counts** | Input record count must reconcile with output counts (accepted + rejected). |
| **Control Totals** | Sum of monetary fields (e.g., transaction amounts) must balance before and after processing. |
| **Referential Integrity** | Every card in `cardxref` must reference a valid account in `acctdata` and a valid customer in `custdata`. |
| **State Consistency** | After posting, every account's balance must equal its prior balance plus the net of posted transactions. |

### Implementation

The `test-harness/reconciliation.py` module provides check functions that can
be called after any batch run.  Each check returns a structured result:

```python
{
    "check": "record_count_balance",
    "job": "POSTTRAN",
    "passed": true,
    "expected": {"input": 300, "accepted": 295, "rejected": 5},
    "actual": {"input": 300, "accepted": 295, "rejected": 5}
}
```

See `RECONCILIATION_CHECKS.md` for the full per-job specification.

---

## 4. Contract Testing

**Purpose:** Ensure that the Java service APIs and data models honour the
implicit contracts defined by the COBOL copybooks and CICS transaction
interfaces.

### What Constitutes a Contract

In the legacy system, contracts are implicit:

| Legacy Artefact | Contract It Defines |
|---|---|
| **Copybook** (`.cpy`) | Field names, types, sizes, and offsets for a data record. |
| **BMS Map** (`.bms`) | Screen field names, positions, and lengths for CICS 3270 UI. |
| **JCL DD statements** | Input/output dataset names, record formats, and key structures. |
| **CICS CSD definitions** | Transaction IDs, program names, and file bindings. |

### Contract Test Categories

#### 4.1 Data-Model Contracts (Copybook Fidelity)

For every copybook, assert that the corresponding Java DTO/entity:

- Has a field for each non-FILLER copybook item.
- Uses the correct Java type mapping:

| COBOL PIC | Java Type | Example |
|---|---|---|
| `PIC X(n)` | `String` (length n) | `ACCT-ACTIVE-STATUS` -> `String` |
| `PIC 9(n)` | `long` or `int` | `ACCT-ID` -> `long` |
| `PIC S9(n)V99` | `BigDecimal` (scale 2) | `ACCT-CURR-BAL` -> `BigDecimal` |

- Serialises to / deserialises from the fixed-width format at the correct
  byte offsets.

#### 4.2 API Contracts (REST / Service Interface)

If the Java migration exposes REST endpoints:

- Request/response schemas must include all copybook fields.
- HTTP status codes and error payloads are documented.
- OpenAPI specs (if generated) are validated against golden-file data.

#### 4.3 Batch I/O Contracts (JCL Parity)

For each JCL job, the Java equivalent must:

- Accept the same input file formats (record length, key positions).
- Produce output files with identical record layouts.
- Handle the same error/reject conditions (e.g., DALYREJS in POSTTRAN).

### Automation

Contract tests are implemented as schema-validation functions in
`test-harness/contracts.py`.  They parse the copybook definitions and
verify that a given Java class or JSON schema covers all required fields
with correct types and sizes.

---

## Directory Layout

```
project-root/
  TEST_STRATEGY.md              <-- This document
  RECONCILIATION_CHECKS.md      <-- Per-job reconciliation specs
  golden-files/
    acctdata.json
    carddata.json
    custdata.json
    cardxref.json
    dailytran.json
    trantype.json
    trancatg.json
    tcatbal.json
    discgrp.json
  test-harness/
    __init__.py
    copybook_parser.py          <-- Parse fixed-width data via copybook layouts
    golden_file_generator.py    <-- Generate golden JSON from ASCII data files
    comparator.py               <-- Differential comparison engine
    reconciliation.py           <-- Reconciliation check functions
    contracts.py                <-- Contract validation utilities
    conftest.py                 <-- Shared pytest fixtures
    test_golden_files.py        <-- Golden-file round-trip tests
    test_reconciliation.py      <-- Reconciliation check tests
    test_contracts.py           <-- Contract validation tests
```

---

## Running the Test Harness

```bash
# Generate golden-file JSON from ASCII data
python -m test-harness.golden_file_generator

# Run all tests
python -m pytest test-harness/ -v

# Run only golden-file tests
python -m pytest test-harness/test_golden_files.py -v

# Run only reconciliation checks
python -m pytest test-harness/test_reconciliation.py -v

# Run only contract tests
python -m pytest test-harness/test_contracts.py -v
```
