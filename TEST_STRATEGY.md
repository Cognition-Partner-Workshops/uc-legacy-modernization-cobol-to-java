# CardDemo Migration Test Strategy

## 1. Overview

This document defines a four-dimensional testing strategy for validating the
migration of the CardDemo mainframe COBOL/CICS/VSAM application to Java 17+.
Every batch program, CICS transaction, and data-access path must be covered by
at least one dimension before the migrated code is considered production-ready.

### Scope

| Layer | Legacy | Target |
|-------|--------|--------|
| Batch programs | COBOL (`CB*.cbl`) via JCL | Java services / Spring Batch |
| Online transactions | CICS (`CO*.cbl`) via BMS maps | REST / gRPC endpoints |
| Data stores | VSAM KSDS, sequential files, EBCDIC | RDBMS / flat-file (UTF-8) |
| Scheduling | Control-M (`CardDemo.controlm`) | Scheduler (Airflow / cron / cloud-native) |

---

## 2. Dimension 1 — Golden-File Testing

**Goal:** Byte-for-byte (logical) equivalence of migrated output against a
known-good COBOL reference for the same input.

### Approach

1. **Capture golden references** – Parse the ASCII data files under
   `app/data/ASCII/` using their COBOL copybook layouts (`app/cpy/`) to produce
   structured JSON snapshots stored in `golden-files/`.

2. **Run the migrated Java code** with identical input and serialize its output
   to the same JSON schema.

3. **Deep-compare** the two JSON trees field-by-field, respecting:
   - Numeric precision rules (COBOL `PIC S9(10)V99` → 2 decimal places).
   - Trailing-space normalization for `PIC X` fields.
   - Signed-overpunch decoding (`{` = `+0`, `}` = `-0`, `A`–`R`, `J`–`R`).

### Golden-File Inventory

| File | Copybook | RECLN | Records | Key Field |
|------|----------|-------|---------|-----------|
| `acctdata.txt` | `CVACT01Y` | 300 | 50 | `ACCT-ID` |
| `carddata.txt` | `CVACT02Y` | 150 | 50 | `CARD-NUM` |
| `cardxref.txt` | `CVACT03Y` | 50 | 50 | `XREF-CARD-NUM` |
| `custdata.txt` | `CVCUS01Y` | 500 | 50 | `CUST-ID` |
| `dailytran.txt` | `CVTRA06Y` (= `CVTRA05Y` layout) | 350 | 300 | `DALYTRAN-ID` |
| `discgrp.txt` | `CVTRA02Y` | 50 | 51 | `DIS-ACCT-GROUP-ID` |
| `tcatbal.txt` | `CVTRA01Y` | 50 | 50 | `TRANCAT-ACCT-ID` |
| `trancatg.txt` | `CVTRA04Y` | 60 | 18 | `TRAN-TYPE-CD + TRAN-CAT-CD` |
| `trantype.txt` | `CVTRA03Y` | 60 | 7 | `TRAN-TYPE` |

### Pass Criteria

- 100 % field-level match (after normalization) for every record.
- Zero unmatched / extra records on either side.

---

## 3. Dimension 2 — Differential Testing

**Goal:** Ensure that the migrated code produces identical *behavioral* results
as the legacy code when both are fed randomized or boundary-case inputs.

### Approach

1. **Shadow execution** – Run both COBOL (via rehosting / emulation) and Java in
   parallel on the same synthetic input.

2. **Diff the outputs** – Use the same JSON comparator from Dimension 1 but
   apply it to the *output files* produced by each batch step:

   | Batch Job | Input(s) | Output(s) | Comparison Granularity |
   |-----------|----------|-----------|------------------------|
   | `POSTTRAN` (CBTRN01C) | `dailytran.txt`, account/card/xref VSAM | Updated `TRANFILE`, `ACCTFILE`, reject file | Record-level balance deltas |
   | `INTCALC` (CBACT04C) | `ACCTFILE`, `TCATBALF`, `DISCGRP` | Updated `ACCTFILE` balances | Per-account interest amount |
   | `TRANBKP` | `TRANFILE` | Backup sequential file | Byte-exact copy |
   | `COMBTRAN` | System + daily transactions | Merged `TRANFILE` | Record count + sort order |
   | `CREASTMT` (CBSTM03A/B) | `TRANFILE`, `ACCTFILE`, `CUSTFILE` | Statement report | Line-for-line text diff |
   | `CBTRN03C` | `TRANFILE`, type/category lookups | Transaction detail report | Line-for-line text diff |
   | `CBEXPORT` | All VSAM files | Export sequential file | Record-for-record with COMP/COMP-3 decode |

3. **Boundary inputs** – Auto-generate edge-case records:
   - Maximum / minimum `PIC S9(10)V99` values.
   - Empty and all-spaces `PIC X` fields.
   - Invalid card numbers, expired dates, inactive accounts.

### Pass Criteria

- Zero field-level differences for all deterministic fields.
- Timestamps and run-IDs excluded from comparison (configurable ignore list).

---

## 4. Dimension 3 — Reconciliation Testing

**Goal:** After a migrated batch run, verify that aggregate invariants and
cross-file relationships still hold.

### Approach

See `RECONCILIATION_CHECKS.md` for the full per-job specification. Summary of
check categories:

| Category | Example Check |
|----------|---------------|
| **Row-count parity** | Input daily transactions = posted + rejected |
| **Balance integrity** | Σ(transaction amounts) = Δ(account current balance) |
| **Referential integrity** | Every `XREF-CARD-NUM` exists in `CARDFILE`; every `XREF-ACCT-ID` exists in `ACCTFILE` |
| **Key uniqueness** | No duplicate `ACCT-ID`, `CARD-NUM`, `TRAN-ID` |
| **Category balance rollup** | Σ(transactions per acct + type + cat) = `TRAN-CAT-BAL` |
| **Interest calculation** | `interest = balance × rate / 365 × days` per disclosure group |
| **Statement completeness** | Every active account with transactions appears in statement output |

### Implementation

Reconciliation checks are implemented as pure functions in
`test-harness/reconciliation.py`. Each function:

1. Loads the relevant golden-file JSON (or post-run output).
2. Computes the expected aggregate.
3. Asserts equality within a tolerance (default `±0.01` for monetary amounts).

### Pass Criteria

- All reconciliation assertions pass.
- A summary report is emitted listing each check, expected vs. actual, and
  PASS / FAIL status.

---

## 5. Dimension 4 — Contract Testing

**Goal:** Guarantee that the migrated Java services honor the same data
contracts (record layouts, field semantics, file formats) as the legacy COBOL
programs.

### Approach

1. **Copybook-as-schema** – Each COBOL copybook under `app/cpy/` is the
   canonical contract. The test harness parses these into machine-readable field
   descriptors (name, PIC clause, offset, length, type).

2. **Contract assertions**:

   | Contract | Assertion |
   |----------|-----------|
   | Record length | Java output record byte-length = copybook RECLN |
   | Field encoding | Numeric fields match PIC precision; alpha fields are left-justified, space-padded |
   | Key ordering | VSAM KSDS replacement must maintain ascending primary-key order |
   | REDEFINES fidelity | `CVEXPORT.cpy` multi-record layout: correct `EXPORT-REC-TYPE` dispatches to correct sub-layout |
   | OCCURS handling | `CVEXPORT.cpy` `OCCURS 3 TIMES` for address lines, `OCCURS 2 TIMES` for phone numbers |
   | Signed numeric | COBOL sign-overpunch on `PIC S9(n)V99` fields decoded correctly |
   | COMP / COMP-3 | Binary and packed-decimal fields in export layout round-trip without loss |

3. **Schema-drift detection** – If a copybook is updated, the test harness
   automatically flags any Java DTO that no longer matches (field count, total
   length, type mapping).

### Pass Criteria

- Zero schema violations.
- All REDEFINES and OCCURS patterns exercise at least one record in the golden
  file set.

---

## 6. Test Execution Matrix

Each batch job must pass all applicable dimensions:

| Job | Golden-File | Differential | Reconciliation | Contract |
|-----|:-----------:|:------------:|:--------------:|:--------:|
| POSTTRAN (CBTRN01C/02C) | ✓ | ✓ | ✓ | ✓ |
| INTCALC (CBACT04C) | ✓ | ✓ | ✓ | ✓ |
| TRANBKP | ✓ | ✓ | ✓ | — |
| COMBTRAN | ✓ | ✓ | ✓ | — |
| CREASTMT (CBSTM03A/B) | ✓ | ✓ | ✓ | ✓ |
| CBTRN03C (Report) | ✓ | ✓ | — | ✓ |
| CBEXPORT | ✓ | ✓ | ✓ | ✓ |
| CBIMPORT | ✓ | ✓ | ✓ | ✓ |
| File loaders (ACCTFILE, CARDFILE, etc.) | ✓ | — | ✓ | ✓ |

---

## 7. Tooling

| Component | Location | Purpose |
|-----------|----------|---------|
| Copybook parser | `test-harness/copybook_parser.py` | Parse COBOL PIC clauses → field descriptors |
| Record parser | `test-harness/record_parser.py` | Slice fixed-width ASCII records into dicts |
| Golden-file generator | `test-harness/generate_golden_files.py` | Produce `golden-files/*.json` |
| JSON comparator | `test-harness/comparator.py` | Deep field-level diff with normalization |
| Reconciliation suite | `test-harness/reconciliation.py` | Aggregate invariant checks |
| Contract validator | `test-harness/contract_validator.py` | Copybook-vs-output schema checks |

---

## 8. CI Integration

```
┌─────────────┐     ┌──────────────┐     ┌──────────────────┐
│ golden-file  │────▶│ differential │────▶│ reconciliation    │
│ generation   │     │ comparison   │     │ checks            │
└─────────────┘     └──────────────┘     └──────────────────┘
                                                │
                                                ▼
                                         ┌──────────────┐
                                         │ contract     │
                                         │ validation   │
                                         └──────────────┘
```

All four stages are gated: a failure in any dimension blocks the pipeline.

---

## 9. Glossary

| Term | Definition |
|------|------------|
| **RECLN** | Record length in bytes as declared by the copybook |
| **PIC clause** | COBOL data description that defines field type, size, and precision |
| **Sign overpunch** | EBCDIC convention where the sign is encoded in the last digit's zone bits; in ASCII data, `{` = `+0`, `}` = `-0`, `A`–`I` = `+1`–`+9`, `J`–`R` = `-1`–`-9` |
| **COMP-3** | Packed-decimal storage: each byte holds two digits, last nibble is sign |
| **KSDS** | VSAM Key-Sequenced Data Set — primary indexed file organization |
| **Golden file** | A snapshot of known-correct output used as a comparison baseline |
| **Reconciliation** | Post-run aggregate checks validating cross-file consistency |
