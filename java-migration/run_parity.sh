#!/usr/bin/env bash
# Parity harness: proves the Java migration of CBACT01C is functionally
# equivalent to the original COBOL program.
#
#   COBOL side: java-migration/parity/cobol/CBACT01C.cbl (identical to
#     app/cbl/CBACT01C.cbl except the VSAM KSDS input is declared as a
#     flat sequential file, since GnuCOBOL has no VSAM) is compiled with
#     GnuCOBOL and run against an ASCII rendering of the EBCDIC account
#     master file, using -fsign=EBCDIC so zoned sign overpunch matches
#     the mainframe encoding.
#   Java side: java-migration/src/CBACT01C.java is run directly against
#     the original EBCDIC file app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS.
#
# The harness then diffs stdout (all DISPLAY output) and the three output
# files (OUTFILE / ARRYFILE / VBRCFILE) byte for byte.
set -euo pipefail
cd "$(dirname "$0")/.."

EBCDIC_DATA=app/data/EBCDIC/AWS.M2.CARDDEMO.ACCTDATA.PS
WORK=java-migration/parity/work
rm -rf "$WORK"
mkdir -p "$WORK/cobol-out" "$WORK/java-out" "$WORK/classes"

echo "== 1. Convert EBCDIC account master to ASCII for the GnuCOBOL run =="
python3 - "$EBCDIC_DATA" "$WORK/acctdata.ascii" <<'EOF'
import sys
data = open(sys.argv[1], 'rb').read()
open(sys.argv[2], 'wb').write(data.decode('cp037').encode('latin-1'))
EOF

echo "== 2. Compile and run the original COBOL (GnuCOBOL) =="
cobc -x -I app/cpy/ -fsign=EBCDIC \
     -o "$WORK/cbact01c" \
     java-migration/parity/cobol/CBACT01C.cbl \
     java-migration/parity/cobol/COBDATFT.cbl
(
  export DD_ACCTFILE="$PWD/$WORK/acctdata.ascii"
  export DD_OUTFILE="$PWD/$WORK/cobol-out/OUTFILE"
  export DD_ARRYFILE="$PWD/$WORK/cobol-out/ARRYFILE"
  export DD_VBRCFILE="$PWD/$WORK/cobol-out/VBRCFILE"
  "$WORK/cbact01c" > "$WORK/cobol-out/stdout.txt"
)

echo "== 3. Compile and run the Java migration against the EBCDIC file =="
javac -d "$WORK/classes" java-migration/src/CBACT01C.java
java -cp "$WORK/classes" CBACT01C "$EBCDIC_DATA" "$WORK/java-out" ebcdic \
     > "$WORK/java-out/stdout.txt"

echo "== 4. Diff COBOL vs Java =="
status=0
diff "$WORK/cobol-out/stdout.txt" "$WORK/java-out/stdout.txt" \
  && echo "stdout (DISPLAY output): IDENTICAL" || status=1
for f in OUTFILE ARRYFILE VBRCFILE; do
  if cmp "$WORK/cobol-out/$f" "$WORK/java-out/$f"; then
    echo "$f: IDENTICAL ($(stat -c%s "$WORK/cobol-out/$f") bytes)"
  else
    status=1
  fi
done

if [ "$status" -eq 0 ]; then
  echo "PARITY PASSED: Java output matches COBOL byte for byte."
else
  echo "PARITY FAILED"
fi
exit "$status"
