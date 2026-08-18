# carddemo-java

Java 17 port of the CardDemo COBOL statement generator `app/cbl/CBSTM03A.CBL`
(with its I/O subroutine `app/cbl/CBSTM03B.CBL`), plus the parity pipeline that
compares its output with a GnuCOBOL reference run of the original program.

`app/` is the read-only source of truth: copybooks in `app/cpy` define the record
layouts, `app/jcl/CREASTMT.JCL` defines the transaction SORT/OUTREC reshape
(reproduced in `JclTransactionSort`), and `app/data/ASCII` provides the input data.

## Run the whole pipeline

```bash
cd carddemo-java
./run-parity.sh              # COBOL reference run + mvn package + parity comparison
SKIP_COBOL_REF=1 ./run-parity.sh   # reuse the existing target/cobol-ref output
```

Or directly:

```bash
mvn package
java -jar target/carddemo-java-1.0.0-SNAPSHOT.jar <repo-root>
```

## Output convention

The COBOL program writes fixed-block files with no line terminators: STMTFILE is
a stream of 80-byte records, HTMLFILE a stream of 100-byte records. The Java port
writes the same records one per line terminated by `\n`, which keeps the files
readable and diffable. The comparator normalises both sides to a list of
fixed-width records (80 for text, 100 for HTML) before diffing, so parity is
judged on record content, never on line-terminator style. Stripping the newlines
from the Java concatenated files yields the COBOL files byte for byte.

## Artifacts

| Path | Content |
| --- | --- |
| `target/cobol-ref/statement.txt` / `.html` | GnuCOBOL reference run (whole run, undelimited) |
| `target/parity/cobol/<accountId>.txt` / `.html` | reference output split per account |
| `target/parity/java/<accountId>.txt` / `.html` | Java output per account |
| `target/parity/java/statement.txt` / `.html` | Java output for the whole run |
| `target/parity/parity-summary.json` | parity summary, per the frozen contract in `docs/parity-contract.md` |

`firstDiffLine` is 1-based over the concatenated text-then-HTML record sequence of
an account, so an HTML-only difference reports `textRecordCount + htmlLineNumber`.

## Tests

```bash
mvn test
```

Covers zoned-decimal overpunch decoding, the `9(9).99-` and `Z(9).99-` edit masks,
`STRING ... DELIMITED BY` name/address concatenation, record parsing for each
copybook layout including the JCL reshape and sort, the parity comparator, and an
end-to-end fixture asserting the full text statement of the first account.
