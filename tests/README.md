# CardDemo Unit Test Infrastructure

## Overview

This directory contains unit test infrastructure for the CardDemo mainframe application. Since the codebase is pure COBOL/CICS/VSAM with zero existing test infrastructure, we use two approaches:

1. **CICS Programs** (COSGN00C, COBIL00C): Extract pure business logic into callable subprograms (no CICS dependencies), then write batch COBOL test drivers that exercise them.
2. **Batch Programs** (CBTRN02C): Create controlled test data files, run the program against known inputs, and verify outputs with a verification program.

## Directory Structure

```
tests/
├── drivers/          # COBOL test driver and verification programs
│   ├── TSGN00C.cbl   # Authentication test driver (8 test cases)
│   ├── TBIL00C.cbl   # Bill payment test driver (8 test cases)
│   ├── VTRN02C.cbl   # Transaction processing verifier (14 test cases)
│   └── GENTDATA.cbl   # Test data generator for CBTRN02C
├── data/             # Test fixture data files (generated at runtime)
├── expected/         # Expected output files for comparison
├── jcl/              # JCL to run test jobs on mainframe
│   └── TTRN02C.jcl   # Transaction processing test JCL
├── scripts/          # Shell scripts to orchestrate tests
│   └── run_all_tests.sh  # Main test runner (GnuCOBOL)
└── README.md         # This file
```

### Extracted Business Logic Subprograms

Located in `app/cbl/`:

| Program    | Source     | Description |
|:-----------|:-----------|:------------|
| COSGN00B   | COSGN00C   | Authentication logic (no CICS) |
| COBIL00B   | COBIL00C   | Bill payment logic (no CICS) |

## Test Case Inventory

### Authentication Tests (TSGN00C → COSGN00B)

| Test Case   | Description                | Expected Result |
|:------------|:---------------------------|:----------------|
| TC-AUTH-01  | Valid admin login           | RC=00, TYPE='A' |
| TC-AUTH-02  | Valid regular user login    | RC=00, TYPE='U' |
| TC-AUTH-03  | Wrong password              | RC=03, 'Wrong Password' |
| TC-AUTH-04  | User not found              | RC=04, 'User not found' |
| TC-AUTH-05  | Empty user ID               | RC=01, 'Please enter User ID' |
| TC-AUTH-06  | Empty password              | RC=02, 'Please enter Password' |
| TC-AUTH-07  | File error                  | RC=05, 'Unable to verify' |
| TC-AUTH-08  | Case insensitivity          | RC=00 (uppercases input) |

### Bill Payment Tests (TBIL00C → COBIL00B)

| Test Case   | Description                | Expected Result |
|:------------|:---------------------------|:----------------|
| TC-PAY-01   | Successful payment         | RC=00, TRAN-AMT=$500, BAL=0 |
| TC-PAY-02   | Zero balance rejection     | RC=02, 'nothing to pay' |
| TC-PAY-03   | Negative balance           | RC=02 (balance <= 0) |
| TC-PAY-04   | Empty account ID           | RC=01 |
| TC-PAY-05   | Confirm='N' (cancelled)    | RC=06 |
| TC-PAY-06   | Invalid confirm value      | RC=04, 'Invalid value' |
| TC-PAY-07   | Transaction ID generation  | TRAN-ID incremented by 1 |
| TC-PAY-08   | Account not found          | RC=03 |

### Transaction Processing Tests (VTRN02C verifying CBTRN02C)

**Valid Transaction Scenario:**

| Test Case   | Description                     | Verification |
|:------------|:--------------------------------|:-------------|
| TC-TXN-01   | Transaction posted              | 3 records in TRANFILE |
| TC-TXN-02   | Account balance updated         | ACCT-CURR-BAL correct |
| TC-TXN-05   | Category balance created        | TCATBALF records exist |
| TC-TXN-06   | No rejects                      | DALYREJS empty |

**Reject Transaction Scenario:**

| Test Case   | Description                     | Verification |
|:------------|:--------------------------------|:-------------|
| TC-TXN-08   | Invalid card rejected           | Reject code 0100 |
| TC-TXN-09   | Missing account rejected        | Reject code 0101 |
| TC-TXN-10   | Overlimit rejected              | Reject code 0102 |
| TC-TXN-11   | Expired account rejected        | Reject code 0103 |
| TC-TXN-13   | No transactions posted          | TRANFILE empty |

## How to Run the Tests

### Prerequisites

- **GnuCOBOL** (cobc) installed
  ```bash
  sudo apt-get install gnucobol
  ```

### Running All Tests (GnuCOBOL / Linux)

From the repository root:

```bash
./tests/scripts/run_all_tests.sh
```

The script will:
1. Verify GnuCOBOL is installed
2. Compile all extracted business logic subprograms
3. Compile all test drivers
4. Run each test suite
5. Display pass/fail summary
6. Exit with 0 if all pass, non-zero otherwise

### Running on Mainframe

1. Upload test programs to your mainframe COBOL source library
2. Compile all programs (COSGN00B, COBIL00B, TSGN00C, TBIL00C, GENTDATA, VTRN02C)
3. For authentication/bill payment tests: Run TSGN00C and TBIL00C as batch jobs
4. For transaction processing tests: Submit `tests/jcl/TTRN02C.jcl`

### Running Individual Test Suites

```bash
# Build directory setup
mkdir -p tests/build
CPY=app/cpy

# Authentication tests only
cobc -m -I $CPY -o tests/build/COSGN00B.so app/cbl/COSGN00B.cbl
cobc -x -I $CPY -o tests/build/TSGN00C tests/drivers/TSGN00C.cbl
cd tests/build && COB_LIBRARY_PATH=. ./TSGN00C

# Bill payment tests only
cobc -m -I $CPY -o tests/build/COBIL00B.so app/cbl/COBIL00B.cbl
cobc -x -I $CPY -o tests/build/TBIL00C tests/drivers/TBIL00C.cbl
cd tests/build && COB_LIBRARY_PATH=. ./TBIL00C
```

## Approach

### Extracted Business Logic Pattern

CICS programs contain EXEC CICS statements that cannot execute in a batch environment. To make them testable, we:

1. **Extract** the pure business logic (validation, computation, flow control) into a new subprogram (suffix "B" for "Business logic")
2. **Replace** CICS I/O operations with LINKAGE SECTION parameters
3. **Preserve** the same copybook data structures for compatibility
4. **Test** via batch COBOL drivers that set up parameters and call the subprogram

Example: `COSGN00C.cbl` (CICS) → `COSGN00B.cbl` (testable business logic)

### Batch Program Testing Pattern

Batch programs like CBTRN02C already use file I/O (no CICS), so they can be tested as-is:

1. **Generate** controlled input files with known data (GENTDATA program)
2. **Run** the batch program against the test data
3. **Verify** output files match expected results (VTRN02C verifier)

### Test Output Format

Each test driver produces standardized DISPLAY output:
```
PASS TC-AUTH-01: Valid admin login
FAIL TC-AUTH-03: Wrong password
  Expected RC=03
  Got      RC=00
```

Summary at end:
```
TOTAL TESTS : 008
PASSED      : 007
FAILED      : 001
```

Return codes: 0 = all pass, 4 = failures detected.
