# CBACT01C System Analysis

## 1. Program Overview

| Attribute     | Value                                            |
|---------------|--------------------------------------------------|
| Program ID    | `CBACT01C`                                       |
| Application   | CardDemo                                         |
| Type          | Batch COBOL program                              |
| Purpose       | Read the VSAM account master file and produce three derivative output files in different record formats |

CBACT01C is a batch extract/transform utility. It sequentially reads every
record from the indexed (VSAM KSDS) account master, applies light
transformations (date reformatting, default-value substitution, array
expansion), and fans the data out into three separate output files -- each
demonstrating a different COBOL record format: fixed-length flat, fixed-length
with OCCURS arrays, and variable-length (RECORDING MODE V).

---

## 2. Business Logic Flow

```
START
  |
  +-- Open all four files (ACCTFILE input, OUTFILE / ARRYFILE / VBRCFILE output)
  |     On any open failure -> display status, ABEND 999
  |
  +-- LOOP until EOF on ACCTFILE
  |     |
  |     +-- READ next account record into ACCOUNT-RECORD (copybook CVACT01Y)
  |     |     Status '00' -> continue processing
  |     |     Status '10' -> set EOF flag, exit loop
  |     |     Other        -> display status, ABEND 999
  |     |
  |     +-- 1100-DISPLAY-ACCT-RECORD
  |     |     Display all account fields to SYSOUT (diagnostic trace)
  |     |
  |     +-- 1300-POPUL-ACCT-RECORD  (flat output record)
  |     |     Copy most fields verbatim from ACCOUNT-RECORD -> OUT-ACCT-REC
  |     |     ACCT-REISSUE-DATE: pass through COBDATFT date formatter
  |     |       input type '2' (YYYY-MM-DD) -> output type '2' (YYYYMMDD)
  |     |     ACCT-CURR-CYC-DEBIT: if zero, substitute 2525.00
  |     |
  |     +-- 1350-WRITE-ACCT-RECORD  -> write OUT-ACCT-REC to OUTFILE
  |     |
  |     +-- 1400-POPUL-ARRAY-RECORD (array output record, 5 OCCURS slots)
  |     |     Slot 1: balance = ACCT-CURR-BAL,  debit = 1005.00
  |     |     Slot 2: balance = ACCT-CURR-BAL,  debit = 1525.00
  |     |     Slot 3: balance = -1025.00,        debit = -2500.00
  |     |     Slots 4-5: remain initialized to zero (from INITIALIZE)
  |     |
  |     +-- 1450-WRITE-ARRY-RECORD  -> write ARR-ARRAY-REC to ARRYFILE
  |     |
  |     +-- 1500-POPUL-VBRC-RECORD  (two variable-length records)
  |     |     REC1 (12 bytes): ACCT-ID + ACCT-ACTIVE-STATUS
  |     |     REC2 (39 bytes): ACCT-ID + CURR-BAL + CREDIT-LIMIT + REISSUE-YYYY
  |     |
  |     +-- 1550-WRITE-VB1-RECORD   -> write VBR-REC (len=12) to VBRCFILE
  |     +-- 1575-WRITE-VB2-RECORD   -> write VBR-REC (len=39) to VBRCFILE
  |
  +-- Close ACCTFILE (on failure -> ABEND 999)
  |
  +-- Display "END OF EXECUTION"
  |
  +-- GOBACK
END
```

---

## 3. Data Structures

### 3.1 Copybook CVACT01Y -- Account Record (300 bytes)

| COBOL Field              | PIC               | Bytes | Java Type              | Notes                              |
|--------------------------|-------------------|------:|------------------------|------------------------------------|
| `ACCT-ID`                | `9(11)`           |    11 | `long`                 | Numeric account identifier         |
| `ACCT-ACTIVE-STATUS`     | `X(01)`           |     1 | `String` (1 char)      | 'Y'/'N' active flag                |
| `ACCT-CURR-BAL`          | `S9(10)V99`       |    12 | `BigDecimal`           | Signed with 2 implied decimals     |
| `ACCT-CREDIT-LIMIT`      | `S9(10)V99`       |    12 | `BigDecimal`           | Signed with 2 implied decimals     |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`       |    12 | `BigDecimal`           | Signed with 2 implied decimals     |
| `ACCT-OPEN-DATE`         | `X(10)`           |    10 | `String`               | Date as string (YYYY-MM-DD)        |
| `ACCT-EXPIRAION-DATE`    | `X(10)`           |    10 | `String`               | Expiration date (typo preserved)   |
| `ACCT-REISSUE-DATE`      | `X(10)`           |    10 | `String`               | Reissue date (YYYY-MM-DD)         |
| `ACCT-CURR-CYC-CREDIT`   | `S9(10)V99`       |    12 | `BigDecimal`           | Current cycle credits              |
| `ACCT-CURR-CYC-DEBIT`    | `S9(10)V99`       |    12 | `BigDecimal`           | Current cycle debits               |
| `ACCT-ADDR-ZIP`          | `X(10)`           |    10 | `String`               | Not written to any output          |
| `ACCT-GROUP-ID`          | `X(10)`           |    10 | `String`               | Account group identifier           |
| `FILLER`                 | `X(178)`          |   178 | *(ignored)*            | Padding to 300-byte record         |

### 3.2 Copybook CODATECN -- Date Conversion Interface

| COBOL Field           | PIC      | Java Equivalent        | Description                               |
|-----------------------|----------|------------------------|-------------------------------------------|
| `CODATECN-TYPE`       | `X`      | `int` enum (1 or 2)    | Input format: 1=YYYYMMDD, 2=YYYY-MM-DD   |
| `CODATECN-INP-DATE`   | `X(20)`  | `String`               | Date input string                         |
| `CODATECN-OUTTYPE`    | `X`      | `int` enum (1 or 2)    | Output format: 1=YYYY-MM-DD, 2=YYYYMMDD  |
| `CODATECN-0UT-DATE`   | `X(20)`  | `String`               | Date output string (reformatted)          |
| `CODATECN-ERROR-MSG`  | `X(38)`  | `String`               | Error message (unused in this program)    |

The assembler program `COBDATFT` converts between the two date layouts.
In CBACT01C the conversion is always type 2 -> type 2, i.e.
`YYYY-MM-DD` in -> `YYYYMMDD` out.

### 3.3 Output Records

#### OUT-ACCT-REC (flat sequential)

| Field                          | PIC / USAGE             | Java Type      |
|--------------------------------|-------------------------|----------------|
| `OUT-ACCT-ID`                  | `9(11)`                 | `long`         |
| `OUT-ACCT-ACTIVE-STATUS`       | `X(01)`                 | `String`       |
| `OUT-ACCT-CURR-BAL`            | `S9(10)V99`             | `BigDecimal`   |
| `OUT-ACCT-CREDIT-LIMIT`        | `S9(10)V99`             | `BigDecimal`   |
| `OUT-ACCT-CASH-CREDIT-LIMIT`   | `S9(10)V99`             | `BigDecimal`   |
| `OUT-ACCT-OPEN-DATE`           | `X(10)`                 | `String`       |
| `OUT-ACCT-EXPIRAION-DATE`      | `X(10)`                 | `String`       |
| `OUT-ACCT-REISSUE-DATE`        | `X(10)`                 | `String`       |
| `OUT-ACCT-CURR-CYC-CREDIT`     | `S9(10)V99`             | `BigDecimal`   |
| `OUT-ACCT-CURR-CYC-DEBIT`      | `S9(10)V99 COMP-3`      | `BigDecimal`   |
| `OUT-ACCT-GROUP-ID`            | `X(10)`                 | `String`       |

> Note: `OUT-ACCT-CURR-CYC-DEBIT` uses `COMP-3` (packed-decimal), unlike
> the display-numeric source field. The value 2525.00 is substituted when the
> source debit is zero.

#### ARR-ARRAY-REC (array-structured sequential)

| Field                    | PIC / USAGE                | Occurs | Java Type        |
|--------------------------|----------------------------|--------|------------------|
| `ARR-ACCT-ID`            | `9(11)`                    | --     | `long`           |
| `ARR-ACCT-CURR-BAL(n)`   | `S9(10)V99`                | 5      | `BigDecimal[]`   |
| `ARR-ACCT-CURR-CYC-DEBIT(n)` | `S9(10)V99 COMP-3`    | 5      | `BigDecimal[]`   |
| `ARR-FILLER`             | `X(04)`                    | --     | *(padding)*      |

Slots 1-3 are populated with specific values; slots 4-5 remain zero.

#### VBRC variable-length records (two layouts per account)

**VB1 (12 bytes):** `ACCT-ID` (11) + `ACCT-ACTIVE-STATUS` (1)

**VB2 (39 bytes):** `ACCT-ID` (11) + `CURR-BAL` (12) + `CREDIT-LIMIT` (12) + `REISSUE-YYYY` (4)

---

## 4. I/O Operations

| Logical Name   | DD Name    | Organization                | Access    | Direction | Java Equivalent                         |
|----------------|------------|-----------------------------|-----------|-----------|-----------------------------------------|
| `ACCTFILE-FILE`| `ACCTFILE` | Indexed (VSAM KSDS)         | Sequential| Input     | `BufferedReader` / custom indexed reader |
| `OUT-FILE`     | `OUTFILE`  | Sequential, fixed-length    | Sequential| Output    | `BufferedWriter` / `PrintWriter`         |
| `ARRY-FILE`    | `ARRYFILE` | Sequential, fixed-length    | Sequential| Output    | `BufferedWriter` / `PrintWriter`         |
| `VBRC-FILE`    | `VBRCFILE` | Sequential, variable-length | Sequential| Output    | `BufferedWriter` (length-prefixed)       |

### File Status Codes

| Status | Meaning   | Program Action               |
|--------|-----------|------------------------------|
| `00`   | Success   | Continue                     |
| `10`   | EOF       | Set `END-OF-FILE = 'Y'`     |
| Other  | Error     | Display status, ABEND 999   |

---

## 5. Dependencies

### External Programs

| Program    | Type       | Purpose                                       | Java Replacement                  |
|------------|------------|-----------------------------------------------|-----------------------------------|
| `COBDATFT` | Assembler  | Date format conversion (YYYY-MM-DD <-> YYYYMMDD) | `DateConverter` utility class |
| `CEE3ABD`  | LE runtime | Abnormal program termination (ABEND)          | `System.exit(999)` / throw `RuntimeException` |

### Copybooks

| Copybook    | Purpose                                                |
|-------------|--------------------------------------------------------|
| `CVACT01Y`  | 300-byte account master record layout                  |
| `CODATECN`  | Date conversion input/output structure for `COBDATFT`  |

---

## 6. Edge Cases and Error Handling

| Scenario                              | COBOL Behavior                                         | Java Equivalent                                  |
|---------------------------------------|--------------------------------------------------------|--------------------------------------------------|
| File open failure (any file)          | Display error + file status, ABEND 999                 | Throw `IOException` / `RuntimeException`         |
| Read returns non-00/non-10 status     | Display "ERROR READING ACCOUNT FILE", show status, ABEND | Throw `IOException`                            |
| Write returns non-00/non-10 status    | Display write error + status, ABEND                    | Throw `IOException`                              |
| Close failure on ACCTFILE             | Display error, ABEND                                   | Throw `IOException`                              |
| `ACCT-CURR-CYC-DEBIT` = 0            | Substitute `2525.00` in output record                  | `if (debit == 0) debit = 2525.00`               |
| Non-numeric file status (stat1 = '9') | Binary-to-decimal conversion of stat2 byte, display as `NNNN` | Translate to meaningful exception message    |
| Empty input file (immediate EOF)      | Loop body never executes; close file, exit normally    | Same -- process zero records gracefully          |
| Array slots 4-5                       | Left at zero after `INITIALIZE`                        | Initialize `BigDecimal.ZERO`                     |
| Variable-length record sizing         | `WS-RECD-LEN` set to 12 or 39 before each WRITE       | Write exactly 12 or 39 characters per record type|
| Reissue date parse                    | `WS-ACCT-REISSUE-YYYY` extracted by REDEFINES overlay  | `substring(0, 4)` of the reissue date string     |
