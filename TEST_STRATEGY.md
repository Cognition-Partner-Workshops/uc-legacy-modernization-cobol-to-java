# Test Strategy: COBOL-to-Java Migration for CardDemo Application

## 1. Overview

This document defines the testing approach for migrating the AWS CardDemo COBOL application to Java. The strategy ensures functional equivalence between the legacy COBOL system and the new Java implementation through four complementary testing layers:

1. **Golden-File Tests** -- Capture and verify outputs against known-good reference data
2. **Differential Tests** -- Run COBOL and Java side-by-side, compare field-by-field
3. **Batch Reconciliation** -- Validate totals, counts, and checksums after each batch run
4. **Contract Tests** -- Codify fixed-width record layouts as enforceable interface contracts

---

## 2. Golden-File Tests

### 2.1 Purpose

Golden-file tests establish a baseline of known-correct output by parsing the existing ASCII data files using the field layouts defined in the COBOL copybooks. The structured JSON representations serve as the "ground truth" against which the Java implementation's output is compared.

### 2.2 Data Files and Corresponding Copybooks

| Data File | Copybook | Record Length | Key Field | Description |
|---|---|---|---|---|
| `acctdata.txt` | `CVACT01Y.cpy` | 300 bytes | `ACCT-ID` (PIC 9(11)) | Account master records |
| `carddata.txt` | `CVACT02Y.cpy` | 150 bytes | `CARD-NUM` (PIC X(16)) | Card detail records |
| `cardxref.txt` | `CVACT03Y.cpy` | 50 bytes | `XREF-CARD-NUM` (PIC X(16)) | Card-to-account cross-reference |
| `custdata.txt` | `CVCUS01Y.cpy` | 500 bytes | `CUST-ID` (PIC 9(09)) | Customer master records |
| `dailytran.txt` | `CVTRA06Y.cpy` | 350 bytes | `DALYTRAN-ID` (PIC X(16)) | Daily transaction records |
| `tcatbal.txt` | `CVTRA01Y.cpy` | 50 bytes | `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD` | Transaction category balances |
| `trancatg.txt` | `CVTRA04Y.cpy` | 60 bytes | `TRAN-TYPE-CD` + `TRAN-CAT-CD` | Transaction category types |
| `trantype.txt` | `CVTRA03Y.cpy` | 60 bytes | `TRAN-TYPE` (PIC X(02)) | Transaction type lookup |
| `discgrp.txt` | `CVTRA02Y.cpy` | 50 bytes | `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD` | Disclosure/interest rate groups |

### 2.3 Approach

1. **Parse** each ASCII data file using the PIC clause definitions from its corresponding copybook.
2. **Produce** a structured JSON file for each data file (stored in `golden-files/`).
3. **Annotate** each field with its name, PIC type, byte offset, and length.
4. **Compare** Java output against the golden JSON files using exact field-by-field matching.

### 2.4 PIC Clause Handling

| PIC Type | Description | Example | Parsing Rule |
|---|---|---|---|
| `PIC X(n)` | Alphanumeric | `PIC X(16)` | Read `n` characters, strip trailing spaces |
| `PIC 9(n)` | Unsigned numeric | `PIC 9(11)` | Read `n` characters, convert to integer |
| `PIC S9(n)V99` | Signed decimal with implied decimal | `PIC S9(10)V99` | Read `n+2` characters; last char encodes sign via zoned decimal ('{' = +0, 'A'-'I' = +1 to +9, '}' = -0, 'J'-'R' = -1 to -9); insert decimal point before last 2 digits |
| `PIC S9(n)V99 COMP-3` | Packed decimal | `PIC S9(09)V99 COMP-3` | Each byte holds two BCD digits; last nibble is sign (C=+, D=-); applies to EBCDIC export layout only |
| `PIC 9(n) COMP` | Binary integer | `PIC 9(9) COMP` | 4-byte big-endian integer; applies to EBCDIC export layout only |
| `FILLER` | Padding | `PIC X(178)` | Skip `n` bytes |

### 2.5 Golden-File Test Execution

```
# Generate golden files from COBOL data
python test-harness/cobol_test_harness/parser.py --copybook app/cpy/CVACT01Y.cpy --data app/data/ASCII/acctdata.txt --output golden-files/acctdata.json

# Compare Java output against golden file
python test-harness/cobol_test_harness/comparator.py --expected golden-files/acctdata.json --actual java-output/acctdata.json
```

---

## 3. Differential Tests

### 3.1 Purpose

Differential tests run both the COBOL and Java implementations against the same input data and compare their outputs field-by-field to detect behavioral divergence.

### 3.2 Approach

```
+------------------+       +------------------+
|   Input Data     |------>|  COBOL Program   |----> COBOL Output
|  (ASCII files)   |       |  (Mainframe/     |
|                  |       |   Emulator)      |
+------------------+       +------------------+
        |
        |                  +------------------+
        +----------------->|  Java Program    |----> Java Output
                           |  (Spring Boot /  |
                           |   Batch)         |
                           +------------------+

                           +------------------+
    COBOL Output --------->|  Field-by-Field  |----> Diff Report
    Java Output  --------->|  Comparator      |
                           +------------------+
```

### 3.3 Comparison Rules

1. **Numeric fields**: Compare as decimal values with tolerance of 0.00 (exact match by default). Configurable tolerance for floating-point conversions.
2. **Alphanumeric fields**: Compare after right-trimming trailing spaces.
3. **Date fields**: Compare as normalized `YYYY-MM-DD` strings.
4. **Timestamps**: Compare with configurable precision (default: microseconds).
5. **FILLER fields**: Ignored in comparison.

### 3.4 Diff Report Format

```json
{
  "file": "acctdata.txt",
  "record_index": 3,
  "field": "ACCT-CURR-BAL",
  "cobol_value": "1470.00",
  "java_value": "1470.01",
  "offset": 12,
  "length": 12,
  "pic": "S9(10)V99",
  "status": "MISMATCH"
}
```

---

## 4. Batch Reconciliation

### 4.1 Purpose

Batch reconciliation verifies that batch processing jobs produce correct aggregate results. After each batch run (modeled from the JCL jobs in `app/jcl/`), the following checks are performed.

### 4.2 Reconciliation Check Categories

#### 4.2.1 Record Count Validation

| Check | Source | Expected |
|---|---|---|
| Account records | `acctdata.txt` | 50 records (RECLN 300, file size 15050 / 300 = ~50 + newlines) |
| Card records | `carddata.txt` | 50 records (RECLN 150) |
| Card cross-references | `cardxref.txt` | 50 records (RECLN 50, with newlines = 37 bytes per line) |
| Customer records | `custdata.txt` | 50 records (RECLN 500) |
| Daily transactions | `dailytran.txt` | 300 records (RECLN 350) |
| Transaction category balances | `tcatbal.txt` | 50 records (RECLN 50) |
| Transaction categories | `trancatg.txt` | 18 records (RECLN 60) |
| Transaction types | `trantype.txt` | 7 records (RECLN 60) |
| Disclosure groups | `discgrp.txt` | 51 records (RECLN 50) |

#### 4.2.2 Numeric Field Sum Validation

| Summation | Source File | Field | Description |
|---|---|---|---|
| Total current balances | `acctdata.txt` | `ACCT-CURR-BAL` | Sum of all account current balances |
| Total credit limits | `acctdata.txt` | `ACCT-CREDIT-LIMIT` | Sum of all credit limits |
| Total cash credit limits | `acctdata.txt` | `ACCT-CASH-CREDIT-LIMIT` | Sum of all cash advance limits |
| Total cycle credits | `acctdata.txt` | `ACCT-CURR-CYC-CREDIT` | Sum of current cycle credits |
| Total cycle debits | `acctdata.txt` | `ACCT-CURR-CYC-DEBIT` | Sum of current cycle debits |
| Total transaction amounts | `dailytran.txt` | `DALYTRAN-AMT` | Sum of all daily transaction amounts |
| Total category balances | `tcatbal.txt` | `TRAN-CAT-BAL` | Sum of all category balances |
| Total interest rates | `discgrp.txt` | `DIS-INT-RATE` | Sum of all disclosure interest rates |

#### 4.2.3 Cross-Reference Integrity

| Check | Description |
|---|---|
| Card-to-Account | Every `XREF-ACCT-ID` in `cardxref.txt` must exist as `ACCT-ID` in `acctdata.txt` |
| Card-to-Customer | Every `XREF-CUST-ID` in `cardxref.txt` must exist as `CUST-ID` in `custdata.txt` |
| Card Xref-to-Card | Every `XREF-CARD-NUM` in `cardxref.txt` must exist as `CARD-NUM` in `carddata.txt` |
| Transaction-to-Card | Every `DALYTRAN-CARD-NUM` in `dailytran.txt` must exist as `CARD-NUM` in `carddata.txt` |
| Account-to-CatBal | Every `TRANCAT-ACCT-ID` in `tcatbal.txt` must exist as `ACCT-ID` in `acctdata.txt` |
| Card Account Link | Every `CARD-ACCT-ID` in `carddata.txt` must exist as `ACCT-ID` in `acctdata.txt` |

### 4.3 Post-Batch Verification Flow

```
1. Capture pre-batch record counts and field sums
2. Execute batch job (COBOL or Java)
3. Capture post-batch record counts and field sums
4. Validate:
   a. Record count deltas match expected changes
   b. Numeric field sum deltas are consistent
   c. All cross-references remain valid
   d. No orphaned records created
```

---

## 5. Contract Tests

### 5.1 Purpose

Contract tests codify the fixed-width record layouts from copybooks as enforceable interface contracts. Any Java class that reads or writes these records must conform to the exact byte-level layout.

### 5.2 Record Layout Contracts

#### 5.2.1 Account Record (CVACT01Y.cpy) -- RECLN 300

| Field | PIC | Offset | Length | Type | Contract Rule |
|---|---|---|---|---|---|
| `ACCT-ID` | `9(11)` | 0 | 11 | Unsigned integer | Must be exactly 11 digits, zero-padded |
| `ACCT-ACTIVE-STATUS` | `X(01)` | 11 | 1 | Char | Must be 'Y' or 'N' |
| `ACCT-CURR-BAL` | `S9(10)V99` | 12 | 12 | Signed decimal | Zoned decimal with implied decimal point |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 24 | 12 | Signed decimal | Must be >= 0 |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 36 | 12 | Signed decimal | Must be >= 0 and <= ACCT-CREDIT-LIMIT |
| `ACCT-OPEN-DATE` | `X(10)` | 48 | 10 | Date string | Format: YYYY-MM-DD |
| `ACCT-EXPIRAION-DATE` | `X(10)` | 58 | 10 | Date string | Format: YYYY-MM-DD, must be > ACCT-OPEN-DATE |
| `ACCT-REISSUE-DATE` | `X(10)` | 68 | 10 | Date string | Format: YYYY-MM-DD |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 78 | 12 | Signed decimal | Zoned decimal |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 90 | 12 | Signed decimal | Zoned decimal |
| `ACCT-ADDR-ZIP` | `X(10)` | 102 | 10 | String | ZIP code format |
| `ACCT-GROUP-ID` | `X(10)` | 112 | 10 | String | Group identifier |
| `FILLER` | `X(178)` | 122 | 178 | Padding | Must be spaces |
| **Total** | | | **300** | | |

#### 5.2.2 Transaction Record (CVTRA05Y.cpy) -- RECLN 350

| Field | PIC | Offset | Length | Type | Contract Rule |
|---|---|---|---|---|---|
| `TRAN-ID` | `X(16)` | 0 | 16 | String | Unique transaction identifier |
| `TRAN-TYPE-CD` | `X(02)` | 16 | 2 | String | Must match a valid `TRAN-TYPE` in trantype.txt |
| `TRAN-CAT-CD` | `9(04)` | 18 | 4 | Unsigned int | Must match a valid category code in trancatg.txt |
| `TRAN-SOURCE` | `X(10)` | 22 | 10 | String | Transaction source identifier |
| `TRAN-DESC` | `X(100)` | 32 | 100 | String | Transaction description |
| `TRAN-AMT` | `S9(09)V99` | 132 | 11 | Signed decimal | Zoned decimal with implied V99 |
| `TRAN-MERCHANT-ID` | `9(09)` | 143 | 9 | Unsigned int | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `X(50)` | 152 | 50 | String | Merchant name |
| `TRAN-MERCHANT-CITY` | `X(50)` | 202 | 50 | String | Merchant city |
| `TRAN-MERCHANT-ZIP` | `X(10)` | 252 | 10 | String | Merchant ZIP |
| `TRAN-CARD-NUM` | `X(16)` | 262 | 16 | String | Must match a valid card number |
| `TRAN-ORIG-TS` | `X(26)` | 278 | 26 | Timestamp | YYYY-MM-DD HH:MM:SS.ffffff |
| `TRAN-PROC-TS` | `X(26)` | 304 | 26 | Timestamp | Processing timestamp |
| `FILLER` | `X(20)` | 330 | 20 | Padding | Must be spaces |
| **Total** | | | **350** | | |

#### 5.2.3 Customer Record (CVCUS01Y.cpy) -- RECLN 500

| Field | PIC | Offset | Length | Type | Contract Rule |
|---|---|---|---|---|---|
| `CUST-ID` | `9(09)` | 0 | 9 | Unsigned int | Must be exactly 9 digits, zero-padded |
| `CUST-FIRST-NAME` | `X(25)` | 9 | 25 | String | Customer first name |
| `CUST-MIDDLE-NAME` | `X(25)` | 34 | 25 | String | Customer middle name |
| `CUST-LAST-NAME` | `X(25)` | 59 | 25 | String | Customer last name |
| `CUST-ADDR-LINE-1` | `X(50)` | 84 | 50 | String | Address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | 134 | 50 | String | Address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | 184 | 50 | String | Address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | 234 | 2 | String | US state code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | 236 | 3 | String | Country code |
| `CUST-ADDR-ZIP` | `X(10)` | 239 | 10 | String | ZIP code |
| `CUST-PHONE-NUM-1` | `X(15)` | 249 | 15 | String | Primary phone |
| `CUST-PHONE-NUM-2` | `X(15)` | 264 | 15 | String | Secondary phone |
| `CUST-SSN` | `9(09)` | 279 | 9 | Unsigned int | Social Security Number |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | 288 | 20 | String | Government-issued ID |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | 308 | 10 | Date string | Format: YYYY-MM-DD |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | 318 | 10 | String | EFT account identifier |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 328 | 1 | Char | 'Y' or 'N' |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | 329 | 3 | Unsigned int | 0-999 credit score |
| `FILLER` | `X(168)` | 332 | 168 | Padding | Must be spaces |
| **Total** | | | **500** | | |

#### 5.2.4 Card Cross-Reference Record (CVACT03Y.cpy) -- RECLN 50

| Field | PIC | Offset | Length | Type | Contract Rule |
|---|---|---|---|---|---|
| `XREF-CARD-NUM` | `X(16)` | 0 | 16 | String | Must match CARD-NUM in carddata.txt |
| `XREF-CUST-ID` | `9(09)` | 16 | 9 | Unsigned int | Must match CUST-ID in custdata.txt |
| `XREF-ACCT-ID` | `9(11)` | 25 | 11 | Unsigned int | Must match ACCT-ID in acctdata.txt |
| `FILLER` | `X(14)` | 36 | 14 | Padding | Must be spaces |
| **Total** | | | **50** | | |

### 5.3 Contract Enforcement

Contracts are enforced through:

1. **Schema validation**: Each JSON golden file includes a `_schema` section defining field offsets and lengths.
2. **Round-trip testing**: Parse a record, serialize it back to fixed-width, and verify byte-for-byte equality.
3. **Boundary testing**: Verify fields at maximum values (e.g., `PIC 9(11)` with `99999999999`).
4. **Type checking**: Ensure numeric fields contain only valid digits plus sign indicators.

---

## 6. Test Execution Plan

### 6.1 Phase 1: Baseline Capture (Pre-Migration)

1. Run the parser to generate golden-file JSON for all 9 ASCII data files.
2. Compute and record all reconciliation checksums.
3. Verify all cross-reference integrity checks pass against the COBOL data.

### 6.2 Phase 2: Unit Tests (During Migration)

1. Test individual Java record parsers against golden files.
2. Verify each Java batch job produces the same record counts and field sums.
3. Run contract tests to ensure Java DTOs match COBOL copybook layouts.

### 6.3 Phase 3: Integration Tests (Post-Migration)

1. Run the full batch processing pipeline in Java.
2. Execute differential tests comparing COBOL and Java output for each batch job.
3. Run complete reconciliation suite.

### 6.4 Phase 4: Regression Tests (Ongoing)

1. Include golden-file comparisons in CI/CD pipeline.
2. Run reconciliation checks after every batch processing change.
3. Monitor for contract violations when updating record layouts.

---

## 7. Tools and Utilities

| Tool | Location | Purpose |
|---|---|---|
| `parser.py` | `test-harness/cobol_test_harness/parser.py` | Parse fixed-width COBOL data files using copybook PIC definitions |
| `comparator.py` | `test-harness/cobol_test_harness/comparator.py` | Diff two outputs field-by-field, report mismatches |
| `reconciliation.py` | `test-harness/cobol_test_harness/reconciliation.py` | Record count, numeric sum, and cross-reference integrity checks |
| `test_parser.py` | `test-harness/tests/test_parser.py` | Unit tests for the parser |
| `test_comparator.py` | `test-harness/tests/test_comparator.py` | Unit tests for the comparator |
| `test_reconciliation.py` | `test-harness/tests/test_reconciliation.py` | Unit tests for reconciliation checks |
