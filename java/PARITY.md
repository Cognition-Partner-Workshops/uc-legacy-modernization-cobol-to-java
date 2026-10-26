# CBACT02C parity report (KAN-62)

`CBACT02C` reads the card master file (DD `CARDFILE`, a VSAM KSDS keyed on `CARD-NUM`) from start to end. It prints each 150-byte `CARD-RECORD` to SYSOUT. The Java port is `carddemo.programs.Cbact02c`. Run it with:

```
java -jar java/target/carddemo-batch.jar CBACT02C --dd CARDFILE=<path to 150-byte fixed records>
```

## How parity is checked

`java/scripts/run-parity.sh` does the following:

1. Builds the jar with Maven.
2. Compiles the original `app/cbl/CBACT02C.cbl` with GnuCOBOL (`cobc -x`).
3. Builds the test inputs:
   - Removes the line breaks from `app/data/ASCII/carddata.txt` to get 50 fixed 150-byte records.
   - Converts the EBCDIC extract `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS` from code page 037 and checks that it is byte-identical to the ASCII fixture.
   - Loads each record set into a real GnuCOBOL `INDEXED` file (Berkeley DB) with `scripts/cobol/LOADKSDS.cbl`. This plays the role of `IDCAMS REPRO` into the KSDS.
4. Runs COBOL against the indexed file and Java against the same records, for each scenario.
5. Compares SYSOUT and the process exit code byte-for-byte with `cmp`.

`CEE3ABD` is the Language Environment abend service. It is not part of GnuCOBOL, so `scripts/cobol/CEE3ABD.cbl` stands in for it: it ends the run with `STOP RUN RETURNING ABCODE`. ABCODE is 999, so the process exits with 999 mod 256 = 231. The Java `Main` exits with code 999 on an `Abend`, and the operating system reports that as 231 too.

| Scenario | What it covers |
|---|---|
| `carddata` | All 50 fixture records, loaded in file order |
| `carddata-reversed` | The same records loaded in reverse order. A KSDS still returns them in key order. |
| `carddata-ebcdic` | Records taken from the EBCDIC mainframe extract after CP037-to-ASCII conversion |
| `empty-file` | A dataset with zero records: start/end messages only, RC 0 |
| `missing-file` | `CARDFILE` points at a dataset that does not exist: OPEN status 35, `ERROR OPENING CARDFILE`, `FILE STATUS IS: NNNN0035`, abend 999 |

## Results

GnuCOBOL 3.1.2 and OpenJDK 21 on Ubuntu 22.04:

```
== compare (COBOL vs Java: SYSOUT and exit status)
carddata                     sysout  MATCH     7626 bytes
carddata                     rc      MATCH        2 bytes
carddata-reversed            sysout  MATCH     7626 bytes
carddata-reversed            rc      MATCH        2 bytes
carddata-ebcdic              sysout  MATCH     7626 bytes
carddata-ebcdic              rc      MATCH        2 bytes
empty-file                   sysout  MATCH       76 bytes
empty-file                   rc      MATCH        2 bytes
missing-file                 sysout  MATCH      104 bytes
missing-file                 rc      MATCH        4 bytes
SUMMARY: all compared outputs match
```

Record count reconciliation: 50 records in, 50 `CARD-RECORD` lines out (52 SYSOUT lines including the start and end messages). CBACT02C writes no datasets, so SYSOUT and the return code are the only outputs to compare.

## Unit tests

`mvn -q -f java/pom.xml verify` runs JUnit 5 tests for:

- **Codecs**: zoned-decimal overpunch signs, COMP-3 packing, `INITIALIZE` compared with `MOVE 0`, and how `9910-DISPLAY-IO-STATUS` formats a file status.
- **Record layout**: the `CVACT02Y` field offsets, decoding fixture records, and the 150-byte length check.
- **Program logic**: reading in key order, an empty file, a missing file (status 35), a corrupt or short file (status 30), and the abend code.
- **Command line**: the CLI wiring and argument checks.

## Deliberate deviations

- **Dataset format.** GnuCOBOL stores `INDEXED` files as Berkeley DB, and z/OS stores them as VSAM. The Java port does not read either native format. It reads the KSDS content as a flat file of fixed 150-byte records, which is the `REPRO` unload form, and orders it by the 16-byte key (unsigned byte compare) in memory. Both programs are fed the same records.
- **Errors after a successful OPEN.** If the flat file length is not a multiple of 150, the Java port reports it at OPEN with status 30. On VSAM, a damaged dataset would usually fail on OPEN or READ with a VSAM-specific status. The parity scenarios do not exercise this path.
- **CLOSE.** CLOSE on the in-memory dataset cannot fail, so the `ERROR CLOSING CARDFILE` branch has nothing to trigger it. The code keeps it so the structure matches the COBOL.
- **Spring Batch / scheduler (ticket acceptance criteria).** KAN-62 asks for a Spring Batch job wired into the migrated scheduler. Those depend on KAN-17 (Spring Boot 3.5 / Java 21 multi-module project) and KAN-19 (Spring Batch job framework), and both are still To Do. This port is a plain Java 21 module. `Cbact02c` implements `BatchProgram.run(JobContext)`, using the DD-name contract and return codes, so a Spring Batch `Tasklet` can wrap it once KAN-19 exists. READCARD has no GDG outputs, so the versioned-output and restart criteria do not apply to it.
