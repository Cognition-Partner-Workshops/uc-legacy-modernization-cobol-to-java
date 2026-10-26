# CBACT01C parity: GnuCOBOL vs Java 21 port

Ticket: [KAN-57](https://partner-workshops-2.atlassian.net/browse/KAN-57). The original program
`app/cbl/CBACT01C.cbl` and the Java port `carddemo.programs.Cbact01c` are run on the same account
data, and every output is compared byte for byte with `cmp`.

## Reproduce

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 PATH=$JAVA_HOME/bin:$PATH
sudo apt-get install -y gnucobol3 maven openjdk-21-jdk-headless   # if missing
java/scripts/run-parity.sh                  # report in java/target/parity/parity-report.txt
java/scripts/run-parity.sh --update-golden  # also refresh src/test/resources/golden/
```

How the script works:

1. It compiles `CBACT01C.cbl` with `cobc -x -std=ibm -fsign=EBCDIC -I app/cpy`, linked with two
   GnuCOBOL stand-ins for z/OS-only modules (in `java/scripts/cobol/`):
   * `COBDATFT.cbl`: a line-by-line translation of `app/asm/COBDATFT.asm`. It does the same
     positional byte moves on `CODATECN-REC`.
   * `CEE3ABD.cbl`: the LE abend service. It sets `RETURN-CODE` to the abend code and ends the
     run unit.
2. `LOADKSDS.cbl` loads each line-sequential fixture into a GnuCOBOL indexed file. This file is
   the COBOL program's `ACCTFILE`, and it plays the part of the IDCAMS REPRO in
   `app/jcl/ACCTFILE.jcl`. The same program also writes the flat 300-byte unload that the Java
   port reads as its `ACCTFILE`.
3. Both programs run with the DD names from `READACCT.jcl`: `ACCTFILE`, `OUTFILE`, `ARRYFILE`
   and `VBRCFILE`. The script then compares `OUTFILE`, `ARRYFILE`, `VBRCFILE`, SYSOUT (stdout)
   and the process return code.

`Cbact01cGoldenTest` runs the Java side of the same five scenarios on every
`mvn -f java/pom.xml verify`. It checks the output against the captured GnuCOBOL output in
`src/test/resources/golden/`, so CI catches regressions without needing `cobc`.

## Scenarios

| Scenario | ACCTFILE input | What it exercises |
|---|---|---|
| `acctdata` | `app/data/ASCII/acctdata.txt` (50 accounts) | Production-like data: all three outputs and the full DISPLAY dump |
| `edge-cases` | `src/test/resources/fixtures/acct-edge.txt` (5 accounts, unsorted) | KSDS key ordering. Negative balances and a negative zero (`...0}`). Debit = 0 versus debit <> 0 (the "sticky" OUT debit). Maximum 9s values. Reissue dates that are not `YYYY-MM-DD` (`20250101  `, `2027/01/01`), plus a blank one |
| `empty-acctfile` | 0 records | Immediate EOF: empty outputs, RC 0 |
| `abend-missing-acctfile` | dataset absent | Open failure, status 35, `9910`/`9999` paths, abend 999 |
| `abend-outfile-open` | edge fixture, OUTFILE in a missing directory | OUTFILE open failure, status 30, abend 999 |

## Results (GnuCOBOL 3.1.2, OpenJDK 21.0.x)

```
SCENARIO                   OUTPUT    STATUS      COBOL_B     JAVA_B
acctdata                   OUTFILE   MATCH          5350       5350
acctdata                   ARRYFILE  MATCH          5500       5500
acctdata                   VBRCFILE  MATCH          2950       2950
acctdata                   SYSOUT    MATCH         41426      41426
acctdata                   RC        MATCH             2          2
edge-cases                 OUTFILE   MATCH           535        535
edge-cases                 ARRYFILE  MATCH           550        550
edge-cases                 VBRCFILE  MATCH           295        295
edge-cases                 SYSOUT    MATCH          4211       4211
edge-cases                 RC        MATCH             2          2
empty-acctfile             OUTFILE   MATCH             0          0
empty-acctfile             ARRYFILE  MATCH             0          0
empty-acctfile             VBRCFILE  MATCH             0          0
empty-acctfile             SYSOUT    MATCH            76         76
empty-acctfile             RC        MATCH             2          2
abend-missing-acctfile     OUTFILE   ABSENT            -          -
abend-missing-acctfile     ARRYFILE  ABSENT            -          -
abend-missing-acctfile     VBRCFILE  ABSENT            -          -
abend-missing-acctfile     SYSOUT    MATCH           104        104
abend-missing-acctfile     RC        MATCH             4          4
abend-outfile-open         OUTFILE   ABSENT            -          -
abend-outfile-open         ARRYFILE  ABSENT            -          -
abend-outfile-open         VBRCFILE  ABSENT            -          -
abend-outfile-open         SYSOUT    MATCH           105        105
abend-outfile-open         RC        MATCH             4          4

PARITY: all compared outputs match
```

In the `RC` rows, `2` and `4` are the byte sizes of the files `0\n` and `231\n`. The return code is
0 on normal completion and 231 after an abend: abend code 999 mod 256, which is the code the shell
sees from both GnuCOBOL and the JVM.

`mvn -q -f java/pom.xml verify` passes: 50 JUnit tests, 0 failures. They cover the codec
(`CodecTest`, `FileIoTest`), the `COBDATFT` replacement (`DateFormatConverterTest`), the program
logic (`Cbact01cTest`), the CLI (`MainTest`) and the golden files (`Cbact01cGoldenTest`).

## COBOL behaviours reproduced on purpose

* **Sticky `OUT-ACCT-CURR-CYC-DEBIT`.** `1300-POPUL-ACCT-RECORD` assigns 2525.00 only when the
  account's debit is zero, and it has no ELSE branch. On any other record the field keeps its
  earlier value. Before the first such assignment that value is the FD buffer's initial binary
  zeros, which GnuCOBOL allocates as `x'00'`. The Java FD record areas start the same way:
  `RecordArea.FD_FILL`.
* **`INITIALIZE ARR-ARRAY-REC`.** Occurrences 4 and 5 hold unsigned ASCII zeros
  (`000000000000`, with no over-punch) and packed `+0`. `ARR-FILLER` holds spaces.
* **Negative zero.** A balance of `00000000000}` is copied with its sign kept, and DISPLAY shows
  it as `000000000000-`. `IF ACCT-CURR-CYC-DEBIT EQUAL TO ZERO` treats it as zero.
* **DISPLAY format.** A signed numeric is printed as all its digits followed by `+` or `-`.
  `DISPLAY ACCOUNT-RECORD` prints the raw 300 bytes, trailing spaces included.
* **Variable records.** `VBRCFILE` gets one 12-byte and one 39-byte record per account. Each
  record has GnuCOBOL's 4-byte header in front: the 2-byte big-endian length, then `x'0000'`.
* **KSDS order.** Records are read in ascending order of unsigned key bytes, whatever order the
  unload file has.
* **Error messages.** These are copied word for word, including `ERROR OPENING ACCTFILE`, which
  is shown without the status, and `IO-STATUS-04` formatting such as `NNNN0035`.

## Deliberate deviations

* **ACCTFILE format.** The Java port reads the KSDS as its unloaded image: fixed 300-byte
  records in any order. It does not read a VSAM cluster or a BDB file. A short file, or one
  with a duplicate key, is rejected with status 30. GnuCOBOL would return 35 for a missing
  file, and the Java port does the same.
* **Date conversion.** `DateFormatConverter` replaces `COBDATFT` and uses `java.time` with
  strict `uuuu-MM-dd` and `uuuuMMdd` formats. If the input is not a valid date, it falls back to
  the assembler's positional byte moves. The output is therefore always the same as the
  assembler's, for example `20250101  ` becomes `202510  `.
* **The assembler's error path.** `MVC COERMSG,=C'INVALID INPUT'` copies 38 bytes from a
  13-byte literal, so on z/OS the last 25 bytes come from whatever follows it in the literal
  pool. The Java port and the GnuCOBOL stand-in both pad with spaces instead. CBACT01C always
  calls the routine with type 2 and out-type 2, so this path cannot be reached from this program.
* **Implicit close.** At `GOBACK`, CBACT01C never closes `OUTFILE`, `ARRYFILE` or `VBRCFILE`.
  GnuCOBOL closes them implicitly and writes `libcob: warning: implicit CLOSE` to stderr. Java
  closes them silently. stderr is not part of the compared outputs.
* **Encoding.** All data is ASCII, matching the `app/data/ASCII` fixtures, with
  EBCDIC-convention sign over-punch. Code-page conversion to and from EBCDIC is left to the
  data-migration story. Packed fields are binary and do not depend on the code page.
* **Spring Batch, restartability and GDG outputs.** The ticket's acceptance criteria assume the
  Spring Batch framework stories that KAN-57 depends on, and those are not in `main` yet. This
  port is a plain Java step with the same DD contract and return codes, so it can be wrapped
  later as a Tasklet. CBACT01C writes no GDG or reject outputs.
