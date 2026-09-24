#!/usr/bin/env bash
# Build the Java port, run the four golden-harness scenarios from the pristine
# fixtures, normalise every output with the harness' own tools and compare
# against harness/golden with compare.py.
#
#   usage: scripts/run-parity.sh HARNESS_DIR OUT_DIR
#
# HARNESS_DIR is the extracted harness (contains compare.py, tools/harness.py,
# fixtures/ascii and golden/). OUT_DIR receives the candidate tree laid out like
# golden/ plus a work/ directory with the raw record files.
set -euo pipefail

[[ $# -eq 2 ]] || { echo "usage: $0 HARNESS_DIR OUT_DIR" >&2; exit 2; }
HARNESS="$(cd "$1" && pwd)"
OUT="$2"
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"
PROJECT="$(cd "$(dirname "$0")/.." && pwd)"

PY="python3 $HARNESS/tools/harness.py"
FIXTURES="$HARNESS/fixtures/ascii"
WORK="$OUT/work"
JAR="$PROJECT/target/carddemo-batch.jar"
INTCALC_PARM="2022071800"

echo "== build"
(cd "$PROJECT" && ./mvnw -q -DskipTests package)

run_java() {
  local scenario="$1" max_rc="$2"; shift 2
  local dir="$WORK/$scenario" gold="$OUT/$scenario"
  echo "== run $scenario"
  local rc=0
  java -jar "$JAR" "$@" >"$dir/stdout.raw" 2>"$dir/stderr.raw" || rc=$?
  $PY stdout "$dir/stdout.raw" "$gold/stdout.txt"
  [[ -s "$dir/stderr.raw" ]] && sed 's/^/   stderr: /' "$dir/stderr.raw" >&2
  printf '%d\n' "$rc" >"$gold/return-code.txt"
  [[ $rc -le $max_rc ]] || { echo "$scenario failed (rc=$rc)" >&2; exit 1; }
}

# load FIXTURE RECLEN DEST [to-fixed options]  (indexed datasets are plain fixed-record files)
load() {
  $PY to-fixed "$FIXTURES/$1" "$3" --reclen "$2" "${@:4}"
}

rm -rf "$WORK"
for s in CBACT01C CBACT04C CBTRN02C CBACT04C-after-CBTRN02C; do
  rm -rf "$OUT/$s"
  mkdir -p "$WORK/$s" "$OUT/$s"
done

# ---------------------------------------------------------------- CBACT01C
W="$WORK/CBACT01C"; G="$OUT/CBACT01C"
load acctdata.txt 300 "$W/ACCTFILE"
run_java CBACT01C 0 CBACT01C \
  --dd ACCTFILE="$W/ACCTFILE" --dd OUTFILE="$W/OUTFILE" \
  --dd ARRYFILE="$W/ARRYFILE" --dd VBRCFILE="$W/VBRCFILE"
$PY normalize "$W/OUTFILE"  "$G/OUTFILE.txt"  --reclen 107
$PY normalize "$W/ARRYFILE" "$G/ARRYFILE.txt" --reclen 110
$PY normalize "$W/VBRCFILE" "$G/VBRCFILE.txt" --variable

# ---------------------------------------------------------------- CBACT04C
W="$WORK/CBACT04C"; G="$OUT/CBACT04C"
load acctdata.txt 300 "$W/ACCTFILE"
load tcatbal.txt    50 "$W/TCATBALF"
load cardxref.txt   50 "$W/XREFFILE" --pad
load discgrp.txt    50 "$W/DISCGRP"
run_java CBACT04C 0 CBACT04C --parm "$INTCALC_PARM" \
  --dd TCATBALF="$W/TCATBALF" --dd XREFFILE="$W/XREFFILE" --dd DISCGRP="$W/DISCGRP" \
  --dd ACCTFILE="$W/ACCTFILE" --dd TRANSACT="$W/TRANSACT"
$PY normalize "$W/TRANSACT" "$G/TRANSACT.txt" --reclen 350
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300

# ---------------------------------------------------------------- CBTRN02C
W="$WORK/CBTRN02C"; G="$OUT/CBTRN02C"
load dailytran.txt 350 "$W/DALYTRAN"
load acctdata.txt  300 "$W/ACCTFILE"
load tcatbal.txt    50 "$W/TCATBALF"
load cardxref.txt   50 "$W/XREFFILE" --pad
run_java CBTRN02C 4 CBTRN02C \
  --dd DALYTRAN="$W/DALYTRAN" --dd XREFFILE="$W/XREFFILE" --dd ACCTFILE="$W/ACCTFILE" \
  --dd TCATBALF="$W/TCATBALF" --dd TRANFILE="$W/TRANFILE" --dd DALYREJS="$W/DALYREJS"
$PY normalize "$W/TRANFILE" "$G/TRANFILE.txt" --reclen 350
$PY normalize "$W/DALYREJS" "$G/DALYREJS.txt" --reclen 430
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300
$PY normalize "$W/TCATBALF" "$G/TCATBALF.txt" --reclen 50

# ------------------------------------------------- CBACT04C after CBTRN02C
# Interest run over the balances and accounts just posted by CBTRN02C (this
# arm's own outputs, mirroring how the golden run chains its own results).
W="$WORK/CBACT04C-after-CBTRN02C"; G="$OUT/CBACT04C-after-CBTRN02C"
$PY to-fixed "$OUT/CBTRN02C/TCATBALF.txt" "$W/TCATBALF" --reclen 50
$PY to-fixed "$OUT/CBTRN02C/ACCTFILE.txt" "$W/ACCTFILE" --reclen 300
load cardxref.txt 50 "$W/XREFFILE" --pad
load discgrp.txt  50 "$W/DISCGRP"
run_java CBACT04C-after-CBTRN02C 0 CBACT04C --parm "$INTCALC_PARM" \
  --dd TCATBALF="$W/TCATBALF" --dd XREFFILE="$W/XREFFILE" --dd DISCGRP="$W/DISCGRP" \
  --dd ACCTFILE="$W/ACCTFILE" --dd TRANSACT="$W/TRANSACT"
$PY normalize "$W/TRANSACT" "$G/TRANSACT.txt" --reclen 350
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300

echo "== compare"
python3 "$HARNESS/compare.py" "$OUT"
