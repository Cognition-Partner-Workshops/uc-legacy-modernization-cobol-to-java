# CardDemo Migration Test Strategy

## Overview

This document defines a four-dimensional testing strategy for validating the migration of
the CardDemo mainframe COBOL/CICS/VSAM application to a Java-based target platform. Each
dimension addresses a distinct class of migration risk: data fidelity, behavioural
equivalence, aggregate consistency, and interface stability.

---

## 1. Golden-File Testing

**Goal:** Prove that every fixed-length record in the legacy ASCII data files can be parsed
into a structured representation and that the migrated system produces byte-equivalent
output for the same inputs.

### Approach

| Step | Description |
|------|-------------|
| 1 | Parse each ASCII data file (`acctdata.txt`, `carddata.txt`, `custdata.txt`, `cardxref.txt`, `dailytran.txt`, `trantype.txt`, `trancatg.txt`, `tcatbal.txt`, `discgrp.txt`) using the COBOL copybook layout that defines its record structure. |
| 2 | Emit one JSON file per data file into `golden-files/`. Each JSON file is an array of record objects with named fields matching the copybook field names. |
| 3 | During CI the migrated Java code reads the same ASCII inputs and writes its output. The test harness compares the Java output field-by-field against the golden JSON. |
| 4 | Differences are reported at the field level with record index, field name, expected value, and actual value. |

### File ↔ Copybook ↔ Golden-File Mapping

| ASCII Data File | Copybook | Record Length | Golden File |
|----------------|----------|--------------|-------------|
| `acctdata.txt` | `CVACT01Y.cpy` (ACCOUNT-RECORD) | 300 | `golden-files/acctdata.json` |
| `carddata.txt` | `CVACT02Y.cpy` (CARD-RECORD) | 150 | `golden-files/carddata.json` |
| `custdata.txt` | `CVCUS01Y.cpy` (CUSTOMER-RECORD) | 500 | `golden-files/custdata.json` |
| `cardxref.txt` | `CVACT03Y.cpy` (CARD-XREF-RECORD) | 50 | `golden-files/cardxref.json` |
| `dailytran.txt` | `CVTRA06Y.cpy` (DALYTRAN-RECORD) | 350 | `golden-files/dailytran.json` |
| `trantype.txt` | `CVTRA03Y.cpy` (TRAN-TYPE-RECORD) | 60 | `golden-files/trantype.json` |
| `trancatg.txt` | `CVTRA04Y.cpy` (TRAN-CAT-RECORD) | 60 | `golden-files/trancatg.json` |
| `tcatbal.txt` | `CVTRA01Y.cpy` (TRAN-CAT-BAL-RECORD) | 50 | `golden-files/tcatbal.json` |
| `discgrp.txt` | `CVTRA02Y.cpy` (DIS-GROUP-RECORD) | 50 | `golden-files/discgrp.json` |

### Signed-Numeric Convention

Legacy ASCII files encode signed numeric fields using the EBCDIC zoned-decimal trailing
sign convention (overpunch). The parser in `test-harness/` handles the following mapping:

| Positive | `{` = 0, `A` = 1, `B` = 2, `C` = 3, `D` = 4, `E` = 5, `F` = 6, `G` = 7, `H` = 8, `I` = 9 |
|----------|---|
| **Negative** | `}` = 0, `J` = 1, `K` = 2, `L` = 3, `M` = 4, `N` = 5, `O` = 6, `P` = 7, `Q` = 8, `R` = 9 |

### Pass/Fail Criteria

- **Pass:** Every field in every record matches the golden reference exactly (after
  trimming trailing spaces on alphanumeric fields and normalizing numeric precision to the
  copybook-defined scale).
- **Fail:** Any field-level mismatch.

---

## 2. Differential Testing

**Goal:** Run the same business scenario through both the legacy COBOL programs (or a
reference emulation) and the migrated Java code, then compare all outputs.

### Approach

1. **Define Canonical Scenarios** — one per batch job:

   | Scenario | Legacy Program | Input Files | Output Files |
   |----------|---------------|-------------|--------------|
   | Post Daily Transactions | `CBTRN02C` | `dailytran.txt`, `cardxref.txt`, `acctdata.txt`, `tcatbal.txt` | Updated `acctdata`, updated `tcatbal`, `DALYREJS` rejects |
   | Interest Calculation | `CBACT04C` | `tcatbal.txt`, `cardxref.txt`, `acctdata.txt`, `discgrp.txt` | Updated `acctdata`, system transactions |
   | Transaction Report | `CBTRN03C` | `transact`, `cardxref.txt`, `trantype.txt`, `trancatg.txt` | Formatted report file |
   | Statement Generation | `CBSTM03A` | `transact`, `cardxref.txt`, `acctdata.txt`, `custdata.txt` | Statement text + HTML |
   | Data Export | `CBEXPORT` | `custdata`, `acctdata`, `cardxref`, `transact`, `carddata` | Multi-record export file (CVEXPORT layout) |
   | Data Import | `CBIMPORT` | Export file | Normalized customer, account, xref, transaction files |

2. **Execute Both Paths** — run the COBOL batch (or capture its known-good output) and the
   Java equivalent with identical inputs.

3. **Diff Outputs** — use the test-harness comparison functions to produce a structured
   diff of every output file. The diff operates at the record/field level using the
   appropriate copybook layout, not raw byte comparison.

4. **Report** — generate a per-scenario summary: total records, matched, mismatched, with
   field-level detail for every mismatch.

### Pass/Fail Criteria

- **Pass:** Zero mismatches across all output files for all scenarios.
- **Fail:** Any field-level or record-count difference.

---

## 3. Reconciliation Testing

**Goal:** Validate aggregate invariants that must hold after each batch job completes.
These are domain-specific business rules that catch systemic errors (e.g., dropped
transactions, balance drift) even when individual records appear correct.

### Approach

Each reconciliation check is an assertion over aggregated data. Checks are defined in
`RECONCILIATION_CHECKS.md` and implemented in `test-harness/reconciliation.py`.

### Check Categories

| Category | Example |
|----------|---------|
| **Row-Count** | Daily transaction input count = posted count + rejected count |
| **Balance** | Sum of all `TRAN-CAT-BAL` records for an account = `ACCT-CURR-BAL` |
| **Cross-Reference Integrity** | Every card in `cardxref.txt` exists in `carddata.txt` and references a valid account in `acctdata.txt` |
| **Referential Completeness** | Every `DALYTRAN-CARD-NUM` maps to a `XREF-CARD-NUM` (or the transaction is in the rejects file) |
| **Idempotency** | Re-running a batch job with the same input produces identical output |
| **Export/Import Round-Trip** | Exporting then importing yields files identical to the originals |

### Pass/Fail Criteria

- **Pass:** Every reconciliation assertion evaluates to `True`.
- **Fail:** Any assertion failure, reported with the check name, expected value, actual
  value, and the set of records that contributed to the discrepancy.

---

## 4. Contract Testing

**Goal:** Ensure the migrated Java system honours the same external interface contracts as
the COBOL original — file layouts, CICS transaction codes, screen maps, and inter-program
communication areas.

### Approach

| Contract Type | Validation Method |
|---------------|-------------------|
| **File Layout** | For every VSAM/sequential file, assert that the Java output record length matches the copybook `RECLN` and that field positions/types align with the copybook `PIC` clauses. |
| **CICS Transaction Codes** | Verify that the Java equivalent exposes the same transaction identifiers (`CC00`, `CA00`, `CB00`, etc.) and accepts the same input screen fields. |
| **BMS Screen Maps** | Parse the BMS map source (`app/bms/`) and assert that every named field has a corresponding element in the Java UI layer with matching length and data type. |
| **Commarea / Data Structures** | For each copybook used in inter-program calls (`COCOM01Y`, `COADM02Y`, etc.), assert that the Java DTO has matching field names, types, and byte offsets. |
| **JCL DD Names** | For each batch job, assert that the Java batch configuration references the same logical file identifiers (DD names) from the JCL. |
| **Signed Numeric Encoding** | Verify that `PIC S9(n)V99` fields in Java produce the same zoned-decimal encoding as COBOL when writing output files. |

### Implementation

Contract tests are defined as declarative JSON schemas derived from the copybooks.
The test harness in `test-harness/contracts.py` loads these schemas and validates
Java-produced output files against them.

Schema properties checked:
- Total record length in bytes
- Field start position (0-based byte offset)
- Field length in bytes
- Field type (`alphanumeric`, `numeric_display`, `numeric_signed`, `numeric_comp3`)
- Decimal scale (for `PIC V99` fields)

### Pass/Fail Criteria

- **Pass:** Every output file conforms to its contract schema — correct record length,
  correct field positions and types.
- **Fail:** Any structural violation (wrong record length, field at wrong offset, wrong
  data type).

---

## Test Execution Summary

```
┌──────────────────────┐
│   Golden-File Tests  │  "Does the parser read legacy data correctly?"
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│  Differential Tests  │  "Does the Java code produce the same output as COBOL?"
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│ Reconciliation Tests │  "Do the aggregate business invariants still hold?"
└──────────┬───────────┘
           │
┌──────────▼───────────┐
│   Contract Tests     │  "Does the Java system honour the same file/API contracts?"
└──────────────────────┘
```

### Execution Order

1. **Golden-File** tests run first — they validate the test infrastructure itself.
2. **Contract** tests run next — they catch structural issues before comparing data.
3. **Differential** tests run with known-good COBOL outputs as the reference.
4. **Reconciliation** tests run last — they validate business invariants on the
   differential test outputs.

### Tooling

| Component | Location | Purpose |
|-----------|----------|---------|
| Copybook Parser | `test-harness/copybook_parser.py` | Parse COBOL PIC clauses into field definitions |
| Record Parser | `test-harness/record_parser.py` | Split fixed-width ASCII lines into named fields |
| Golden-File Generator | `test-harness/generate_golden_files.py` | Produce JSON golden references from ASCII data |
| Comparison Engine | `test-harness/comparator.py` | Field-level diff between expected and actual records |
| Reconciliation Runner | `test-harness/reconciliation.py` | Execute aggregate consistency checks |
| Contract Validator | `test-harness/contracts.py` | Validate output files against copybook-derived schemas |
