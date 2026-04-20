# CBACT01C Migration Notes — COBOL to Java 17+

## Overview

| Attribute | COBOL | Java |
|---|---|---|
| Program | `CBACT01C.cbl` | `com.carddemo.batch.Cbact01c` |
| Copybooks | `CVACT01Y.cpy`, `CODATECN.cpy` | `AccountRecord` record, `DateConverter` |
| External call | `CALL 'COBDATFT'` (assembler) | `DateConverter.convert()` |
| Abend handler | `CALL 'CEE3ABD'` | `RuntimeException` / `System.exit(1)` |
| Build | JCL / mainframe compile | Maven 3 + JDK 17 |

---

## File I/O Mapping

### Input

| COBOL DD Name | Organization | Java Equivalent |
|---|---|---|
| `ACCTFILE` | VSAM KSDS, sequential access | `BufferedReader` over fixed-width text file |

### Outputs

| COBOL DD Name | Record Format | Bytes/Rec | Java Equivalent |
|---|---|---|---|
| `OUTFILE` | Fixed sequential | 107 | `FixedWidthWriter.writeOutRecord()` |
| `ARRYFILE` | Fixed sequential | 110 | `FixedWidthWriter.writeArrayRecord()` |
| `VBRCFILE` | Variable-length (VB) | 12 or 39 | Newline-delimited byte records |

---

## Field Mappings — CVACT01Y (Input: ACCOUNT-RECORD, 300 bytes)

| COBOL Field | PIC | Offset | Len | Java Field | Java Type |
|---|---|---|---|---|---|
| `ACCT-ID` | `9(11)` | 0 | 11 | `acctId` | `String` |
| `ACCT-ACTIVE-STATUS` | `X(01)` | 11 | 1 | `acctActiveStatus` | `String` |
| `ACCT-CURR-BAL` | `S9(10)V99` | 12 | 12 | `acctCurrBal` | `BigDecimal` |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 24 | 12 | `acctCreditLimit` | `BigDecimal` |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 36 | 12 | `acctCashCreditLimit` | `BigDecimal` |
| `ACCT-OPEN-DATE` | `X(10)` | 48 | 10 | `acctOpenDate` | `String` |
| `ACCT-EXPIRAION-DATE` | `X(10)` | 58 | 10 | `acctExpiraionDate` | `String` |
| `ACCT-REISSUE-DATE` | `X(10)` | 68 | 10 | `acctReissueDate` | `String` |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 78 | 12 | `acctCurrCycCredit` | `BigDecimal` |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 90 | 12 | `acctCurrCycDebit` | `BigDecimal` |
| `ACCT-ADDR-ZIP` | `X(10)` | 102 | 10 | `acctAddrZip` | `String` |
| `ACCT-GROUP-ID` | `X(10)` | 112 | 10 | `acctGroupId` | `String` |
| `FILLER` | `X(178)` | 122 | 178 | *(not mapped)* | — |

Signed numeric fields use **zoned-decimal** encoding with ASCII trailing overpunch:
`{`=+0 `A`–`I`=+1 to +9 · `}`=−0 `J`–`R`=−1 to −9.
Parsed/formatted by `ZonedDecimalUtil`.

---

## Field Mappings — OUT-ACCT-REC (OUTFILE, 107 bytes)

| COBOL Field | PIC / Usage | Offset | Len | Notes |
|---|---|---|---|---|
| `OUT-ACCT-ID` | `9(11)` | 0 | 11 | Direct copy |
| `OUT-ACCT-ACTIVE-STATUS` | `X(01)` | 11 | 1 | Direct copy |
| `OUT-ACCT-CURR-BAL` | `S9(10)V99` | 12 | 12 | Zoned decimal |
| `OUT-ACCT-CREDIT-LIMIT` | `S9(10)V99` | 24 | 12 | Zoned decimal |
| `OUT-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 36 | 12 | Zoned decimal |
| `OUT-ACCT-OPEN-DATE` | `X(10)` | 48 | 10 | Direct copy |
| `OUT-ACCT-EXPIRAION-DATE` | `X(10)` | 58 | 10 | Direct copy |
| `OUT-ACCT-REISSUE-DATE` | `X(10)` | 68 | 10 | **Converted** from `YYYY-MM-DD` → `YYYYMMDD` (padded to 10) |
| `OUT-ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 78 | 12 | Zoned decimal |
| `OUT-ACCT-CURR-CYC-DEBIT` | `S9(10)V99 COMP-3` | 90 | 7 | **Packed decimal**; if source = 0 → replaced with `2525.00` |
| `OUT-ACCT-GROUP-ID` | `X(10)` | 97 | 10 | Direct copy |

---

## Field Mappings — ARR-ARRAY-REC (ARRYFILE, 110 bytes)

| COBOL Field | PIC / Usage | Offset | Len | Notes |
|---|---|---|---|---|
| `ARR-ACCT-ID` | `9(11)` | 0 | 11 | Direct copy |
| `ARR-ACCT-BAL` | OCCURS 5 TIMES | 11 | 95 | 5 × (12 zoned + 7 packed) = 19 per slot |
| — Slot 1 balance | `S9(10)V99` | 11 | 12 | = `ACCT-CURR-BAL` |
| — Slot 1 debit | `S9(10)V99 COMP-3` | 23 | 7 | Fixed `1005.00` |
| — Slot 2 balance | `S9(10)V99` | 30 | 12 | = `ACCT-CURR-BAL` |
| — Slot 2 debit | `S9(10)V99 COMP-3` | 42 | 7 | Fixed `1525.00` |
| — Slot 3 balance | `S9(10)V99` | 49 | 12 | Fixed `−1025.00` |
| — Slot 3 debit | `S9(10)V99 COMP-3` | 61 | 7 | Fixed `−2500.00` |
| — Slots 4–5 | | 68 | 38 | Initialized to zeros |
| `ARR-FILLER` | `X(04)` | 106 | 4 | Spaces |

---

## Field Mappings — VBRC Records (VBRCFILE, variable-length)

### VB1 Record (12 bytes, `WS-RECD-LEN = 12`)

| Field | PIC | Len | Notes |
|---|---|---|---|
| `VB1-ACCT-ID` | `9(11)` | 11 | Direct copy |
| `VB1-ACCT-ACTIVE-STATUS` | `X(01)` | 1 | Direct copy |

### VB2 Record (39 bytes, `WS-RECD-LEN = 39`)

| Field | PIC | Len | Notes |
|---|---|---|---|
| `VB2-ACCT-ID` | `9(11)` | 11 | Direct copy |
| `VB2-ACCT-CURR-BAL` | `S9(10)V99` | 12 | Zoned decimal |
| `VB2-ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 | Zoned decimal |
| `VB2-ACCT-REISSUE-YYYY` | `X(04)` | 4 | First 4 chars of `ACCT-REISSUE-DATE` |

---

## Business Logic Transformations

### 1. Date Conversion (COBDATFT replacement)

The COBOL program calls assembler routine `COBDATFT` via the `CODATECN` copybook to
convert the reissue date from `YYYY-MM-DD` (type 2 input) to `YYYYMMDD` (type 2 output).

Java replacement: `DateConverter.convert(date, "2", "2")` strips hyphens and right-pads to 10 characters.

### 2. Default Debit Substitution

```
IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO
    MOVE 2525.00 TO OUT-ACCT-CURR-CYC-DEBIT
END-IF
```

When the source cycle debit is zero, the output record's `OUT-ACCT-CURR-CYC-DEBIT` is
set to `2525.00` instead. Non-zero values pass through unchanged.

### 3. Array Record Population

Slots 1–3 are populated with a mix of actual and hard-coded values; slots 4–5 remain zero-initialized.
This mirrors the original COBOL `1400-POPUL-ARRAY-RECORD` paragraph exactly.

---

## Encoding Notes

| COBOL Usage | Java Utility | Description |
|---|---|---|
| DISPLAY (default) | `ZonedDecimalUtil` | ASCII zoned decimal with trailing overpunch sign |
| COMP-3 | `PackedDecimalUtil` | Packed BCD — two digits per byte, sign in last nibble |

---

## Data File Reference

| File | Path | Records | Record Length |
|---|---|---|---|
| Sample input | `app/data/ASCII/acctdata.txt` | 50 | 300 bytes |
| Test resource | `java-app/src/test/resources/acctdata.txt` | 50 | 300 bytes |

---

## How to Build and Test

```bash
cd java-app
mvn clean verify
```

Run standalone:

```bash
java -jar target/cbact01c-batch-1.0.0.jar <acctfile> <outfile> <arryfile> <vbrcfile>
```
