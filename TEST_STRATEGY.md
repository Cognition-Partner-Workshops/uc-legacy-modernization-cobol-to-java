# CardDemo Migration Test Strategy

## 1. Overview

This document defines the testing approach for migrating the CardDemo COBOL mainframe
credit card management system to Java. The strategy ensures functional equivalence
between the legacy COBOL implementation and the new Java target across all batch
processing, data transformations, and file I/O operations.

### Scope

| Layer | COBOL Source | Java Target |
|---|---|---|
| Transaction Posting | `CBTRN02C.cbl` | Equivalent Java batch |
| Interest Calculation | `CBACT04C.cbl` | Equivalent Java batch |
| Statement Generation | `CBSTM03A.CBL` / `CBSTM03B.CBL` | Equivalent Java batch |
| Data Export/Import | `CBEXPORT.cbl` / `CBIMPORT.cbl` | Equivalent Java batch |
| Data Loading (IDCAMS) | JCL utilities | Java file loaders |
| Transaction Combine | `COMBTRAN.jcl` (SORT) | Java sort/merge |

---

## 2. Golden-File Tests

Golden-file tests capture known-good outputs from the COBOL system as JSON reference
files, then assert the Java implementation produces identical results.

### 2.1 Programs to Capture

| Program | Input Files | Output to Capture |
|---|---|---|
| `CBTRN02C` (POSTTRAN) | `dailytran.txt`, `cardxref.txt`, `acctdata.txt`, `tcatbal.txt` | Updated `TRANSACT` VSAM, updated `ACCTDATA`, updated `TCATBALF`, `DALYREJS` rejects |
| `CBACT04C` (INTCALC) | `tcatbal.txt`, `cardxref.txt`, `acctdata.txt`, `discgrp.txt` | System transactions (`SYSTRAN`), updated `ACCTDATA` |
| `CBSTM03A` (CREASTMT) | `TRANSACT` VSAM, `cardxref.txt`, `acctdata.txt`, `custdata.txt` | Statement text file, Statement HTML file |
| `CBEXPORT` | All VSAM files | Multi-record export file (`CVEXPORT` layout) |
| `CBIMPORT` | Export file | Normalized customer, account, xref, transaction files |

### 2.2 Input Specifications

Use the sample ASCII data files in `app/data/ASCII/` as canonical inputs:

| File | Copybook | Record Length | Records |
|---|---|---|---|
| `acctdata.txt` | `CVACT01Y` | 300 | 50 |
| `carddata.txt` | `CVACT02Y` | 150 | 50 |
| `cardxref.txt` | `CVACT03Y` | 50 | 50 |
| `custdata.txt` | `CVCUS01Y` | 500 | 50 |
| `dailytran.txt` | `CVTRA06Y` | 350 | 300 |
| `discgrp.txt` | `CVTRA02Y` | 50 | 51 |
| `tcatbal.txt` | `CVTRA01Y` | 50 | 50 |
| `trancatg.txt` | `CVTRA04Y` | 60 | 18 |
| `trantype.txt` | `CVTRA03Y` | 60 | 7 |

### 2.3 Comparison Method

1. Parse both COBOL output and Java output into structured JSON using the copybook-based
   parser in `test-harness/`.
2. Diff the two JSON structures field-by-field using the comparison utility.
3. Report mismatches with: field name, copybook position, expected value, actual value.
4. Numeric fields (`PIC S9(n)V99`, `PIC 9(n)`) are compared with tolerance for
   rounding (configurable, default `0.01` for currency fields).

### 2.4 Golden File Format

Each golden file is a JSON array of record objects in `golden-files/<datafile>.json`.
Field names match the copybook data names. Example:

```json
[
  {
    "ACCT-ID": "00000000001",
    "ACCT-ACTIVE-STATUS": "Y",
    "ACCT-CURR-BAL": 1940.00,
    ...
  }
]
```

---

## 3. Differential Tests

Differential tests run the same input through both the COBOL and Java implementations
and compare their outputs.

### 3.1 Side-by-Side Execution Model

```
                    ┌──────────────┐
   Input Files ───>│  COBOL Batch  │──> COBOL Output
                    └──────────────┘
                    ┌──────────────┐
   Input Files ───>│  Java Batch   │──> Java Output
                    └──────────────┘
                           │
                    ┌──────┴───────┐
                    │  Comparator  │──> Diff Report
                    └──────────────┘
```

### 3.2 Test Scenarios

| Scenario | Description | Key Validation |
|---|---|---|
| Happy Path | All 300 daily transactions valid | All posted, balances updated |
| Invalid Card | Transaction with card not in `cardxref.txt` | Rejected with code 100 |
| Over Limit | Transaction exceeding credit limit | Rejected with code 102 |
| Expired Account | Transaction after account expiration | Rejected with code 103 |
| Missing Account | Card xref references non-existent account | Rejected with code 101 |
| Zero-Balance Interest | TCATBAL entries with zero balance | No interest transactions generated |
| Multiple Cards per Account | Account with multiple cards in xref | Correct aggregation |

### 3.3 Execution Steps

1. Initialize data files from `app/data/ASCII/` for both environments.
2. Run the COBOL batch job sequence (POSTTRAN → INTCALC → COMBTRAN → CREASTMT).
3. Run the equivalent Java batch job sequence.
4. Parse all output files from both runs into JSON.
5. Run field-by-field comparison and generate a diff report.

---

## 4. Batch Reconciliation

After each batch run, validate aggregate totals to ensure no data is lost or corrupted.

### 4.1 Record Count Checks

| Checkpoint | Formula |
|---|---|
| POSTTRAN Input/Output | `input_daily_trans == posted_trans + rejected_trans` |
| INTCALC Input/Output | `tcatbal_records_read == interest_trans_written` (one per non-zero balance) |
| COMBTRAN Merge | `sorted_output_count == backup_trans_count + system_trans_count` |
| CREASTMT Coverage | `statements_generated == distinct_cards_in_xref` |
| CBEXPORT | `total_export_records == cust_count + acct_count + card_count + xref_count + tran_count` |
| CBIMPORT | `import_records == export_records - error_records` |

### 4.2 Numeric Sum Checks

| File | Field | Validation |
|---|---|---|
| `dailytran.txt` | `DALYTRAN-AMT` | Sum of posted amounts + sum of rejected amounts = sum of all input amounts |
| `acctdata.txt` (post-run) | `ACCT-CURR-BAL` | Pre-balance + interest = post-balance (per account) |
| `tcatbal.txt` (post-run) | `TRAN-CAT-BAL` | Pre-balance + sum of posted transactions for that category = post-balance |
| INTCALC output | `TRAN-AMT` | Sum of interest transactions = sum of `(TRAN-CAT-BAL * DIS-INT-RATE) / 1200` |

### 4.3 Checksum Validation

- Compute MD5 hash of sorted record keys for each master file before and after batch.
- If only expected mutations occurred, the key set should be stable (no unexpected
  inserts or deletes in account/card/customer masters).
- New keys should only appear in transaction files and TCATBAL (for new category
  balance records created during POSTTRAN).

---

## 5. Contract Tests

Contract tests codify the file formats, record layouts, and interface agreements between
programs to prevent integration regressions.

### 5.1 File Format Contracts

| File | Format | LRECL | Key Position | Key Length |
|---|---|---|---|---|
| Account Master | FB (Fixed Block) | 300 | 0 | 11 |
| Card Data | FB | 150 | 0 | 16 |
| Card XREF | FB | 50 | 0 | 16 |
| Customer Data | FB | 500 | 0 | 9 |
| Daily Transactions | FB | 350 | 0 | 16 |
| Transaction Master | FB | 350 | 0 | 16 |
| Disclosure Groups | FB | 50 | 0 | 16 |
| Transaction Types | FB | 60 | 0 | 2 |
| Transaction Categories | FB | 60 | 0 | 6 |
| TCATBAL | FB | 50 | 0 | 17 |
| Daily Rejects | F | 430 | — | — |
| Export Data | FB | 500 | 28 | 4 |

### 5.2 Record Layout Contracts

Each copybook defines a contract. The test harness validates:

1. **Field widths**: every field in the Java output has the exact width specified by
   the PIC clause.
2. **Numeric formatting**: signed numeric fields (`PIC S9(n)V99`) use the correct
   sign representation (trailing sign for display, {A-R} for positive, {J-R} for
   negative in zoned decimal).
3. **Filler preservation**: FILLER bytes are space-filled (PIC X) or zero-filled
   (PIC 9) as appropriate.
4. **Key ordering**: VSAM KSDS outputs are sorted by their primary key.

### 5.3 Interface Contracts Between Programs

| Producer | Consumer | Contract |
|---|---|---|
| POSTTRAN (`CBTRN02C`) | COMBTRAN (SORT) | Transaction records keyed by `TRAN-ID` (16 bytes), LRECL=350 |
| POSTTRAN (`CBTRN02C`) | INTCALC (`CBACT04C`) | Updated `TCATBALF` VSAM with accumulated balances per acct/type/cat |
| INTCALC (`CBACT04C`) | COMBTRAN (SORT) | System transactions (`SYSTRAN`) in CVTRA05Y layout, LRECL=350 |
| COMBTRAN (SORT) | CREASTMT (`CBSTM03A`) | Merged+sorted transaction VSAM keyed by TRAN-ID |
| CBEXPORT | CBIMPORT | Multi-record export file using CVEXPORT layout (500 bytes), record type in byte 1 |
| All loaders (IDCAMS) | All batch programs | VSAM KSDS files matching copybook layouts |

### 5.4 Cross-Reference Integrity Contracts

These invariants must hold at all times:

1. Every `XREF-CARD-NUM` in `cardxref.txt` exists in `carddata.txt` (`CARD-NUM`).
2. Every `XREF-ACCT-ID` in `cardxref.txt` exists in `acctdata.txt` (`ACCT-ID`).
3. Every `XREF-CUST-ID` in `cardxref.txt` exists in `custdata.txt` (`CUST-ID`).
4. Every `DALYTRAN-CARD-NUM` in `dailytran.txt` should exist in `cardxref.txt`
   (validated transactions only; rejects may have invalid card numbers).
5. Every `TRANCAT-ACCT-ID` in `tcatbal.txt` exists in `acctdata.txt`.

---

## 6. Test Execution Order

```
1. Load golden files from app/data/ASCII/
2. Run contract tests (validate all file formats and layouts)
3. Run cross-reference integrity checks
4. Run golden-file comparison tests
5. Run batch reconciliation (record counts, numeric sums)
6. Run differential tests (if COBOL environment available)
```

## 7. Tooling

| Component | Location | Purpose |
|---|---|---|
| Copybook Parser | `test-harness/harness/parser.py` | Parse fixed-width files using PIC clause definitions |
| Field Comparator | `test-harness/harness/comparator.py` | Diff two outputs field-by-field |
| Reconciliation Checks | `test-harness/harness/reconciliation.py` | Record counts, sums, cross-reference validation |
| Golden File Generator | `test-harness/generate_golden_files.py` | Produce JSON golden files from ASCII data |
| Test Runner | `test-harness/tests/` | pytest-based test suite |
