#!/usr/bin/env bash
# Build the Java batch programs, run the four golden-harness scenarios from the
# pristine fixtures, normalize the outputs with the harness tools and compare
# them against harness/golden.
#
#   java/scripts/run-parity.sh HARNESS_DIR OUT_DIR
#
# HARNESS_DIR is the extracted harness (contains compare.py, tools/, fixtures/,
# golden/). OUT_DIR receives the candidate outputs laid out like golden/.
set -euo pipefail

[[ $# -eq 2 ]] || { echo "usage: $0 HARNESS_DIR OUT_DIR" >&2; exit 2; }
HARNESS_DIR="$(realpath "$1")"
OUT_DIR="$(realpath -m "$2")"
JAVA_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
FIXTURES="$HARNESS_DIR/fixtures/ascii"
PY="python3 $HARNESS_DIR/tools/harness.py"
WORK_DIR="$OUT_DIR/.work"
JAR="$JAVA_DIR/target/carddemo-batch.jar"

# JCL-equivalent inputs (same as the harness's run.sh).
INTCALC_PARM="2022071800"          # app/jcl/INTCALC.jcl  STEP15 PARM=
FAKE_NOW="2022-07-18 00:00:00"     # frozen clock for FUNCTION CURRENT-DATE

die() { echo "run-parity.sh: $*" >&2; exit 1; }
command -v python3 >/dev/null || die "python3 not found"
[[ -f "$HARNESS_DIR/compare.py" ]] || die "$HARNESS_DIR/compare.py not found"
export LC_ALL=C TZ=UTC

echo "== build"
(cd "$JAVA_DIR" && ./mvnw -q -DskipTests package)
[[ -f "$JAR" ]] || die "$JAR was not built"

rm -rf "$OUT_DIR"
mkdir -p "$WORK_DIR"

# load <fixture> <reclen> <dest> [to-fixed options]: pristine fixture -> record file
load() { $PY to-fixed "$FIXTURES/$1" "$3" --reclen "$2" "${@:4}"; }

# execute <PROGRAM> <max-rc> <workdir> <golden-dir> <java args...>
execute() {
  local prog="$1" max_rc="$2" dir="$3" gold="$4"; shift 4
  echo "== run $prog"
  local rc=0
  java -jar "$JAR" "$prog" --now "$FAKE_NOW" "$@" \
      >"$dir/stdout.raw" 2>"$dir/stderr.raw" || rc=$?
  $PY stdout "$dir/stdout.raw" "$gold/stdout.txt"
  [[ -s "$dir/stderr.raw" ]] && sed 's/^/   stderr: /' "$dir/stderr.raw" >&2
  printf '%d\n' "$rc" >"$gold/return-code.txt"
  [[ $rc -le $max_rc ]] || die "$prog failed (rc=$rc)"
}

# ---------------------------------------------------------------- CBACT01C
W="$WORK_DIR/CBACT01C"; G="$OUT_DIR/CBACT01C"; mkdir -p "$W" "$G"
load acctdata.txt 300 "$W/ACCTFILE"
execute CBACT01C 0 "$W" "$G" --dd "ACCTFILE=$W/ACCTFILE" --dd "OUTFILE=$W/OUTFILE" \
  --dd "ARRYFILE=$W/ARRYFILE" --dd "VBRCFILE=$W/VBRCFILE"
$PY normalize "$W/OUTFILE"  "$G/OUTFILE.txt"  --reclen 107
$PY normalize "$W/ARRYFILE" "$G/ARRYFILE.txt" --reclen 110
$PY normalize "$W/VBRCFILE" "$G/VBRCFILE.txt" --variable

# ---------------------------------------------------------------- CBACT04C
W="$WORK_DIR/CBACT04C"; G="$OUT_DIR/CBACT04C"; mkdir -p "$W" "$G"
load tcatbal.txt   50  "$W/TCATBALF"
load cardxref.txt  50  "$W/XREFFILE" --pad
load acctdata.txt  300 "$W/ACCTFILE"
load discgrp.txt   50  "$W/DISCGRP"
execute CBACT04C 0 "$W" "$G" --parm "$INTCALC_PARM" --dd "TCATBALF=$W/TCATBALF" \
  --dd "XREFFILE=$W/XREFFILE" --dd "ACCTFILE=$W/ACCTFILE" --dd "DISCGRP=$W/DISCGRP" \
  --dd "TRANSACT=$W/TRANSACT"
$PY normalize "$W/TRANSACT" "$G/TRANSACT.txt" --reclen 350
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300

# ---------------------------------------------------------------- CBTRN02C
W="$WORK_DIR/CBTRN02C"; G="$OUT_DIR/CBTRN02C"; mkdir -p "$W" "$G"
load dailytran.txt 350 "$W/DALYTRAN"
load cardxref.txt  50  "$W/XREFFILE" --pad
load acctdata.txt  300 "$W/ACCTFILE"
load tcatbal.txt   50  "$W/TCATBALF"
execute CBTRN02C 4 "$W" "$G" --dd "DALYTRAN=$W/DALYTRAN" --dd "TRANFILE=$W/TRANFILE" \
  --dd "XREFFILE=$W/XREFFILE" --dd "DALYREJS=$W/DALYREJS" --dd "ACCTFILE=$W/ACCTFILE" \
  --dd "TCATBALF=$W/TCATBALF"
$PY normalize "$W/TRANFILE" "$G/TRANFILE.txt" --reclen 350
$PY normalize "$W/DALYREJS" "$G/DALYREJS.txt" --reclen 430
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300
$PY normalize "$W/TCATBALF" "$G/TCATBALF.txt" --reclen 50

# ------------------------------------------------- CBACT04C after CBTRN02C
# Daily batch order (POSTTRAN then INTCALC): interest on the balances and
# account masters just written by CBTRN02C (this run's normalized outputs).
W="$WORK_DIR/CBACT04C-after-CBTRN02C"; G="$OUT_DIR/CBACT04C-after-CBTRN02C"
mkdir -p "$W" "$G"
FIXTURES="$OUT_DIR/CBTRN02C" load TCATBALF.txt 50  "$W/TCATBALF"
FIXTURES="$OUT_DIR/CBTRN02C" load ACCTFILE.txt 300 "$W/ACCTFILE"
load cardxref.txt  50  "$W/XREFFILE" --pad
load discgrp.txt   50  "$W/DISCGRP"
execute CBACT04C 0 "$W" "$G" --parm "$INTCALC_PARM" --dd "TCATBALF=$W/TCATBALF" \
  --dd "XREFFILE=$W/XREFFILE" --dd "ACCTFILE=$W/ACCTFILE" --dd "DISCGRP=$W/DISCGRP" \
  --dd "TRANSACT=$W/TRANSACT"
$PY normalize "$W/TRANSACT" "$G/TRANSACT.txt" --reclen 350
$PY normalize "$W/ACCTFILE" "$G/ACCTFILE.txt" --reclen 300

# ---------------------------------------------------------------- compare
echo "== compare"
python3 "$HARNESS_DIR/compare.py" "$OUT_DIR"
