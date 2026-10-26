#!/usr/bin/env bash
# Parity check for the CBACT03C Java port against the original COBOL compiled
# with GnuCOBOL.
#
#   usage: java/scripts/run-parity.sh [OUT_DIR]
#
# For every scenario the COBOL program and the Java port are run against the
# same XREFFILE data and SYSOUT plus the process exit status are compared
# byte-for-byte. OUT_DIR (default java/target/parity) must be new or empty; it
# receives <scenario>/{cobol,java}/{sysout,rc} and the summary is printed at
# the end.
set -euo pipefail

PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$PROJECT/.." && pwd)"
OUT="${1:-$PROJECT/target/parity}"
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"
if [[ -n "${1:-}" && -n "$(ls -A "$OUT")" ]]; then
  echo "OUT_DIR $OUT is not empty; refusing to clear it" >&2; exit 2
fi
JAR="$PROJECT/target/carddemo-batch.jar"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
ASCII_FIXTURE="$REPO/app/data/ASCII/cardxref.txt"
EBCDIC_FIXTURE="$REPO/app/data/EBCDIC/AWS.M2.CARDDEMO.CARDXREF.PS"
RECLEN=50

command -v cobc >/dev/null || { echo "cobc (GnuCOBOL) is required: sudo apt-get install -y gnucobol3" >&2; exit 2; }
command -v iconv >/dev/null || { echo "iconv is required" >&2; exit 2; }
command -v mvn  >/dev/null || { echo "mvn is required" >&2; exit 2; }

echo "== build Java"
(cd "$PROJECT" && mvn -q -DskipTests package)

echo "== compile COBOL (GnuCOBOL $(cobc --version | head -1 | awk '{print $NF}'))"
BIN="$OUT/bin"; rm -rf "$BIN"; mkdir -p "$BIN"
cobc -x -I "$REPO/app/cpy" -o "$BIN/cbact03c" "$REPO/app/cbl/CBACT03C.cbl"
cobc -x -o "$BIN/loadksds" "$PROJECT/scripts/cobol/LOADKSDS.cbl"
cobc -m -o "$BIN/CEE3ABD.so" "$PROJECT/scripts/cobol/CEE3ABD.cbl"
export COB_LIBRARY_PATH="$BIN"

# Materialise the KSDS content. The EBCDIC mainframe extract is the authoritative
# 50-byte fixed-record unload (the ASCII rendering has its trailing spaces
# stripped, so it is only checked against the extract). Convert it from code
# page 037 to get the records the Java port reads, then load them into a
# GnuCOBOL INDEXED file for the COBOL run.
DATA="$OUT/data"; rm -rf "$DATA"; mkdir -p "$DATA"
iconv -f CP037 -t ISO-8859-1 "$EBCDIC_FIXTURE" >"$DATA/cardxref.fixed"
SIZE=$(stat -c %s "$DATA/cardxref.fixed")
[[ $((SIZE % RECLEN)) -eq 0 ]] || { echo "EBCDIC fixture is not a multiple of $RECLEN bytes" >&2; exit 1; }
echo "   fixture: $((SIZE / RECLEN)) records of $RECLEN bytes (from EBCDIC extract, CP037 -> ASCII)"
{ fold -w "$RECLEN" "$DATA/cardxref.fixed"; echo; } | sed 's/ *$//' >"$DATA/cardxref-from-ebcdic.txt"
if cmp -s <(sed 's/ *$//' "$ASCII_FIXTURE") "$DATA/cardxref-from-ebcdic.txt"; then
  echo "   ASCII fixture app/data/ASCII/cardxref.txt matches the EBCDIC extract record for record"
else
  echo "ASCII fixture differs from the EBCDIC extract after CP037 normalisation" >&2; exit 1
fi
# Reverse-ordered copy: a KSDS returns records in key order whatever order they were loaded in.
fold -w "$RECLEN" "$DATA/cardxref.fixed" | tac | tr -d '\n' >"$DATA/cardxref-reversed.fixed"
: >"$DATA/empty.fixed"

load_ksds() { # SRC DEST
  rm -f "$2"
  DD_INFILE="$1" DD_OUTKSDS="$2" "$BIN/loadksds" >/dev/null
}
load_ksds "$DATA/cardxref.fixed"          "$DATA/cardxref.ksds"
load_ksds "$DATA/cardxref-reversed.fixed" "$DATA/cardxref-reversed.ksds"
load_ksds "$DATA/empty.fixed"             "$DATA/empty.ksds"

FAIL=0
compare() { # SCENARIO COBOL_XREFFILE JAVA_XREFFILE
  local s="$1" cob_dd="$2" java_dd="$3" d="$OUT/$1"
  rm -rf "$d"; mkdir -p "$d/cobol" "$d/java"
  local rc=0
  DD_XREFFILE="$cob_dd" "$BIN/cbact03c" >"$d/cobol/sysout" 2>"$d/cobol/stderr" || rc=$?
  echo "$rc" >"$d/cobol/rc"
  rc=0
  "$JAVA" -jar "$JAR" CBACT03C --dd XREFFILE="$java_dd" >"$d/java/sysout" 2>"$d/java/stderr" || rc=$?
  echo "$rc" >"$d/java/rc"
  for f in sysout rc; do
    if cmp -s "$d/cobol/$f" "$d/java/$f"; then
      printf '%-28s %-7s MATCH   %6d bytes\n' "$s" "$f" "$(stat -c %s "$d/java/$f")"
    else
      printf '%-28s %-7s DIFF\n' "$s" "$f"; FAIL=1
      diff "$d/cobol/$f" "$d/java/$f" | head -20 || true
    fi
  done
}

echo "== compare (COBOL vs Java: SYSOUT and exit status)"
compare cardxref           "$DATA/cardxref.ksds"          "$DATA/cardxref.fixed"
compare cardxref-reversed  "$DATA/cardxref-reversed.ksds" "$DATA/cardxref-reversed.fixed"
compare empty-file         "$DATA/empty.ksds"             "$DATA/empty.fixed"
compare missing-file       "$DATA/no-such.ksds"           "$DATA/no-such.fixed"

echo "== reconcile record counts (cardxref)"
IN=$((SIZE / RECLEN))
COB_LINES=$(grep -c . "$OUT/cardxref/cobol/sysout")
JAVA_LINES=$(grep -c . "$OUT/cardxref/java/sysout")
echo "   records in: $IN; SYSOUT lines: cobol=$COB_LINES java=$JAVA_LINES (expected $((2 + 2 * IN)): start + 2 per record + end)"
[[ $COB_LINES -eq $((2 + 2 * IN)) && $JAVA_LINES -eq $((2 + 2 * IN)) ]] || FAIL=1

if [[ $FAIL -eq 0 ]]; then
  echo "SUMMARY: all compared outputs match"
else
  echo "SUMMARY: differences found" >&2
  exit 1
fi
