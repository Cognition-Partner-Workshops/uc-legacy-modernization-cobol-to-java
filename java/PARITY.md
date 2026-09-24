# Parity report: Java port of CBACT01C / CBACT04C / CBTRN02C

Java 21 / Maven port of the three CardDemo batch programs, written from the
COBOL sources, copybooks, JCL and the golden-output harness only.

## Running

```
cd java
./mvnw -q verify                       # build gate (compiles, runs JUnit 5 tests, packages the jar)
scripts/run-parity.sh HARNESS_DIR OUT_DIR   # build + 4 scenarios + tools/harness.py + compare.py
```

One launcher, one entry point per program:

```
java -jar target/carddemo-batch.jar CBACT01C --dd ACCTFILE=... --dd OUTFILE=... --dd ARRYFILE=... --dd VBRCFILE=...
java -jar target/carddemo-batch.jar CBACT04C --parm 2022071800 --dd TCATBALF=... --dd XREFFILE=... --dd ACCTFILE=... --dd DISCGRP=... --dd TRANSACT=...
java -jar target/carddemo-batch.jar CBTRN02C --dd DALYTRAN=... --dd TRANFILE=... --dd XREFFILE=... --dd DALYREJS=... --dd ACCTFILE=... --dd TCATBALF=...
```

* `--dd DDNAME=path` binds a JCL DD name to a headerless fixed-length record
  file (the harness's `to-fixed` output). KSDS data sets opened I-O are
  rewritten to the same path in primary-key order on close; `TRANFILE` is
  created in key order.
* `--parm` is the JCL PARM (CBACT04C reads the first 10 characters as `PARM-DATE`).
* `--now "yyyy-MM-dd HH:mm:ss"` overrides `FUNCTION CURRENT-DATE`; the default
  is the harness's frozen clock `2022-07-18 00:00:00`.
* DISPLAY output goes to stdout; the process exit status is the COBOL
  `RETURN-CODE` (CBTRN02C exits 4 when it rejected anything).

## Final `compare.py` report (verbatim)

```
Parity report: /tmp/parity-out vs /home/ubuntu/harness/harness/golden

FILE                                     STATUS     GOLDEN     CAND   DIFFS
CBACT01C/ARRYFILE.txt                    MATCH          50       50       0
CBACT01C/OUTFILE.txt                     MATCH          50       50       0
CBACT01C/VBRCFILE.txt                    MATCH         100      100       0
CBACT01C/return-code.txt                 MATCH           1        1       0
CBACT01C/stdout.txt                      MATCH         752      752       0
CBACT04C-after-CBTRN02C/ACCTFILE.txt     MATCH          50       50       0
CBACT04C-after-CBTRN02C/TRANSACT.txt     MATCH          50       50       0
CBACT04C-after-CBTRN02C/return-code.txt  MATCH           1        1       0
CBACT04C-after-CBTRN02C/stdout.txt       MATCH         302      302       0
CBACT04C/ACCTFILE.txt                    MATCH          50       50       0
CBACT04C/TRANSACT.txt                    MATCH          50       50       0
CBACT04C/return-code.txt                 MATCH           1        1       0
CBACT04C/stdout.txt                      MATCH         152      152       0
CBTRN02C/ACCTFILE.txt                    MATCH          50       50       0
CBTRN02C/DALYREJS.txt                    MATCH          38       38       0
CBTRN02C/TCATBALF.txt                    MATCH         100      100       0
CBTRN02C/TRANFILE.txt                    MATCH         262      262       0
CBTRN02C/return-code.txt                 MATCH           1        1       0
CBTRN02C/stdout.txt                      MATCH          54       54       0

SUMMARY: 19/19 files match
```

## `./mvnw -q verify` result

Exit status 0, no output (quiet mode). Surefire: 13 tests run, 0 failures,
0 errors, 0 skipped (`NumericEncodingTest` 4, `Cbact04cTest` 3, `Cbtrn02cTest` 6).

Environment note: Maven Central (`repo.maven.apache.org`) returned HTTP 403
from the build machine, so `~/.m2/settings.xml` mirrors `central` to
`https://maven-central.storage-download.googleapis.com/maven2/` and the
wrapper's Maven distribution was fetched with
`MVNW_REPOURL=https://maven-central.storage-download.googleapis.com/maven2`.
The committed `mvnw`/`maven-wrapper.properties` are the stock ones; on a
network with Central access no override is needed.

## What had to be inferred or guessed

* **Zoned-decimal sign representation.** The ASCII fixtures and golden files
  encode `PIC S9` fields with EBCDIC overpunch characters (`{`=+0, `A`-`I`=+1..+9,
  `}`=-0, `J`-`R`=-1..-9), consistent with the harness's `-fsign=EBCDIC`. The
  port reads plain digits, `+` overpunch and `-` overpunch, and always writes
  the EBCDIC overpunch form. Values are held as scaled `long` (cents / hundredths
  of a percent), and stores truncate high-order digits like a COBOL MOVE.
* **COMP-3 layout.** Standard packed decimal: two digits per byte, trailing sign
  nibble `C` (positive) / `D` (negative). Verified against `OUTFILE`/`ARRYFILE`
  bytes in the golden output.
* **Variable-length sequential file (`VBRCFILE`).** The golden `--variable`
  splitter expects a 4-byte prefix per record: 2-byte big-endian length then two
  zero bytes (GnuCOBOL `COB_VARSEQ_FORMAT=0`). The port writes exactly that.
* **`WRITE ... FROM` of a record area shorter than the FD.** CBACT01C's
  `OUT-ACCT-RECORD`/`ARRAY-RECORD` working-storage items are 107/110 bytes; the
  parts not assigned by the program (`INITIALIZE`d or `VALUE`d) were reproduced
  as INITIALIZE semantics (spaces for `X`, zeros for `9`/COMP-3).
* **CBACT01C literal moves.** The program stores constants (`2525.00` when cycle
  debit is zero; `1005.00`/`1525.00`/`-1025.00`/`-2500.00` in the array
  record) that have no business meaning; they were carried over verbatim.
* **Interest arithmetic.** `COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200`
  without `ROUNDED`, target `S9(09)V99`: implemented as exact integer
  arithmetic on scaled longs with truncation toward zero, then modulo 10^11.
* **Last account in CBACT04C.** `1050-UPDATE-ACCOUNT` is only performed when
  the account changes; the loop exits on EOF before the final group is flushed,
  so the last account's balance is never rewritten (the ELSE branch at EOF is
  unreachable because the loop condition is checked first). The port keeps this
  behaviour because the golden `ACCTFILE` shows the last account unchanged.
* **CBTRN02C validation order.** The credit-limit check and the expiry check are
  both evaluated on a found account; a failing expiry check overwrites an
  earlier `102 OVERLIMIT` with `103`. The xref lookup gates the account lookup.
  The over-limit test is `ACCT-CREDIT-LIMIT >= CYC-CREDIT - CYC-DEBIT + AMT`
  (temp balance truncated to `S9(09)V99`).
* **Reject record.** 350-byte daily transaction followed by `PIC 9(04)` reason
  and `PIC X(76)` description (430 bytes total).
* **`MOVE '05' TO TRAN-CAT-CD` (PIC 9(04))** produces `0005`, confirmed by the
  golden `TRANSACT` records.
* **DB2 timestamp.** `yyyy-MM-dd-HH.mm.ss.ffffff` from the frozen clock, with
  the microseconds fixed at `000000`.
* **ACCESS RANDOM/SEQUENTIAL KSDS files** are modelled as an in-memory
  `TreeMap` keyed on the primary key (byte order == ASCII order for these
  keys); the alternate index `XREFFIL1` (account id) is a linear scan. Files are
  rewritten in key order on close, matching the harness's IDCAMS-style unload.
* **Abend paths** (`9999-ABEND-PROGRAM`, `CEE3ABD` with code 999) are
  implemented as an `Abend` exception mapped to exit status `999 & 0xFF`; none
  of the golden scenarios exercise them, so the exact status is a guess.

## Harness rules not satisfied

None. All 19 files listed in `golden/manifest.json` match (as reported by
`compare.py`); `harness/golden` and `compare.py` were not modified.
