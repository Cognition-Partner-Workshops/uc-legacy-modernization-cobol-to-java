# CBACT01C Java Migration

Standalone Java (17+, no dependencies) migration of the CardDemo batch
program `app/cbl/CBACT01C.cbl` — the account master file reader.

## Contents

| Path | Purpose |
|------|---------|
| `src/CBACT01C.java` | Java port: parses EBCDIC fixed-width (300-byte) account records per the `CVACT01Y` copybook — zoned decimal with sign overpunch and COMP-3 packed decimal — replicates all DISPLAY output and writes OUTFILE / ARRYFILE / VBRCFILE. |
| `parity/cobol/CBACT01C.cbl` | The original program, unchanged except the VSAM KSDS input is declared sequential so GnuCOBOL can run it. |
| `parity/cobol/COBDATFT.cbl` | GnuCOBOL stand-in for the `COBDATFT` assembler date-format routine. |
| `run_parity.sh` | End-to-end functional-equivalence harness. |

## Run the Java program

```bash
javac -d out java-migration/src/CBACT01C.java
java -cp out CBACT01C app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS /tmp/out ebcdic
```

## Prove parity

```bash
./java-migration/run_parity.sh
```

The harness compiles the original COBOL with GnuCOBOL (`-fsign=EBCDIC`),
runs it on an ASCII rendering of the same EBCDIC data, runs the Java port
directly on the EBCDIC file, and diffs stdout plus all three output files
byte for byte. Expected result:

```
stdout (DISPLAY output): IDENTICAL
OUTFILE: IDENTICAL (5350 bytes)
ARRYFILE: IDENTICAL (5500 bytes)
VBRCFILE: IDENTICAL (2950 bytes)
PARITY PASSED: Java output matches COBOL byte for byte.
```
