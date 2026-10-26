# CBACT03C parity report (KAN-63)

`CBACT03C` reads the card cross-reference file (DD `XREFFILE`, a VSAM KSDS keyed on `XREF-CARD-NUM`) from start to end and prints each 50-byte `CARD-XREF-RECORD` to SYSOUT. The Java port is `carddemo.programs.Cbact03c`. Run it with:

```
java -jar java/target/carddemo-batch.jar CBACT03C --dd XREFFILE=<path to 50-byte fixed records>
```

The COBOL program displays every record **twice**: once inside `1000-XREFFILE-GET-NEXT` when the READ returns status `00`, and again in the main `PERFORM UNTIL` loop. This is almost certainly an upstream oversight (CBACT02C, the sibling card reader, prints once), but the port reproduces it so SYSOUT matches byte-for-byte. 50 records give 102 SYSOUT lines.

## How parity is checked

`java/scripts/run-parity.sh` does the following:

1. Builds the jar with Maven (set `JAVA_HOME` to a Java 21 JDK; the script runs the jar with `$JAVA_HOME/bin/java`).
2. Compiles the original `app/cbl/CBACT03C.cbl` with GnuCOBOL (`cobc -x`).
3. Builds the test inputs:
   - Converts the EBCDIC extract `app/data/EBCDIC/AWS.M2.CARDDEMO.CARDXREF.PS` (2500 bytes = 50 x 50) from code page 037. This is the authoritative fixed-record unload.
   - Checks it against `app/data/ASCII/cardxref.txt` record for record. That text file has its trailing `FILLER` spaces stripped (36 characters per line), so it cannot be used directly as the 50-byte fixture.
   - Loads each record set into a real GnuCOBOL `INDEXED` file (Berkeley DB) with `scripts/cobol/LOADKSDS.cbl`. This plays the role of `IDCAMS REPRO` into the KSDS.
4. Runs COBOL against the indexed file and Java against the same records, for each scenario.
5. Compares SYSOUT and the process exit code byte-for-byte with `cmp`, and reconciles the SYSOUT line count against the input record count.

`CEE3ABD` is the Language Environment abend service. It is not part of GnuCOBOL, so `scripts/cobol/CEE3ABD.cbl` stands in for it: it ends the run with `STOP RUN RETURNING ABCODE`. ABCODE is 999, so the process exits with 999 mod 256 = 231. The Java `Main` exits with code 999 on an `Abend`, and the operating system reports that as 231 too.

| Scenario | What it covers |
|---|---|
| `cardxref` | All 50 records from the EBCDIC extract, loaded in file order |
| `cardxref-reversed` | The same records loaded in reverse order. A KSDS still returns them in key order. |
| `empty-file` | A dataset with zero records: start/end messages only, RC 0 |
| `missing-file` | `XREFFILE` points at a dataset that does not exist: OPEN status 35, `ERROR OPENING XREFFILE`, `FILE STATUS IS: NNNN0035`, abend 999 |

## Results

GnuCOBOL 3.1.2 and OpenJDK 21 on Ubuntu 22.04:

```
== compare (COBOL vs Java: SYSOUT and exit status)
cardxref                     sysout  MATCH     5176 bytes
cardxref                     rc      MATCH        2 bytes
cardxref-reversed            sysout  MATCH     5176 bytes
cardxref-reversed            rc      MATCH        2 bytes
empty-file                   sysout  MATCH       76 bytes
empty-file                   rc      MATCH        2 bytes
missing-file                 sysout  MATCH      104 bytes
missing-file                 rc      MATCH        4 bytes
== reconcile record counts (cardxref)
   records in: 50; SYSOUT lines: cobol=102 java=102 (expected 102: start + 2 per record + end)
SUMMARY: all compared outputs match
```

CBACT03C writes no datasets, so SYSOUT and the return code are the only outputs to compare.

## Unit tests

`JAVA_HOME=<jdk21> mvn -q -f java/pom.xml verify` runs 25 JUnit 5 tests for:

- **Codecs**: zoned-decimal overpunch signs, COMP-3 packing, `INITIALIZE` compared with `MOVE 0`, and how `9910-DISPLAY-IO-STATUS` formats a file status.
- **Record layout**: the `CVACT03Y` field offsets, decoding a fixture record, and the 50-byte length.
- **Program logic**: the EBCDIC/ASCII fixture agreement, reading in key order with the double DISPLAY, an empty file, a missing file (status 35), a corrupt or short file (status 30), a duplicate key in the unload (status 30), and the abend code.
- **Command line**: the CLI wiring, argument checks, DD names that don't depend on the locale, and failing the run when SYSOUT cannot be written.

## Deliberate deviations

- **Dataset format.** GnuCOBOL stores `INDEXED` files as Berkeley DB, and z/OS stores them as VSAM. The Java port does not read either native format. It reads the KSDS content as a flat file of fixed 50-byte records, which is the `REPRO` unload form, and orders it by the 16-byte key (unsigned byte compare) in memory. Both programs are fed the same records.
- **Errors after a successful OPEN.** If the flat file length is not a multiple of 50, or two records share a key, the Java port reports it at OPEN with status 30. On VSAM, a damaged dataset would usually fail on OPEN or READ with a VSAM-specific status. The parity scenarios do not exercise this path.
- **Memory.** The sorted key index and the records are held in memory, so heap size limits how large a cross-reference file the port can handle. A very large file would need an external sort, or an unload that is already in key order.
- **Lost SYSOUT.** A Java `PrintStream` does not throw on write errors. If SYSOUT cannot be written (for example, the disk is full), `Main` checks `checkError()` and ends the run with RC 12 instead of RC 0.
- **CLOSE.** CLOSE on the in-memory dataset cannot fail, so the `ERROR CLOSING XREFFILE` branch has nothing to trigger it. The code keeps it so the structure matches the COBOL.
- **Spring Batch / scheduler (ticket acceptance criteria).** KAN-63 asks for a Spring Batch job wired into the migrated scheduler. Those depend on KAN-17 (Spring Boot 3.5 / Java 21 multi-module project) and KAN-19 (Spring Batch job framework), which are still To Do. This port is a plain Java 21 module. `Cbact03c` implements `BatchProgram.run(JobContext)`, using the DD-name contract and return codes, so a Spring Batch `Tasklet` can wrap it once KAN-19 exists. In `app/scheduler/CardDemo.ca7`, READXREF is triggered by the completion of READCUST (SCHID 030); that trigger will be reproduced when the scheduler flow is migrated. READXREF has no GDG outputs, so the versioned-output and restart criteria do not apply to it.
- **Shared runtime with KAN-62.** `carddemo.cobol`, `JobContext`, `Main`, `Abend`, `BatchProgram`, `pom.xml` and `scripts/cobol/CEE3ABD.cbl` are copied byte-for-byte from the CBACT02C branch (PR #245) so the two PRs merge cleanly; only `Main` (program registry and usage) and the per-program files differ.
