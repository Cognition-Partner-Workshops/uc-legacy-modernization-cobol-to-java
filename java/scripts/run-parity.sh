#!/usr/bin/env bash
# Parity check for the CBACT02C Java port against the original COBOL compiled
# with GnuCOBOL.
#
#   usage: java/scripts/run-parity.sh [OUT_DIR]
#
# For every scenario the COBOL program and the Java port are run against the
# same CARDFILE data and SYSOUT plus the process exit status are compared
# byte-for-byte. OUT_DIR (default java/target/parity) receives
# <scenario>/{cobol,java}/{sysout,rc} and the summary is printed at the end.
set -euo pipefail

PROJECT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$PROJECT/.." && pwd)"
OUT="${1:-$PROJECT/target/parity}"
mkdir -p "$OUT"
OUT="$(cd "$OUT" && pwd)"
JAR="$PROJECT/target/carddemo-batch.jar"
FIXTURE="$REPO/app/data/ASCII/carddata.txt"
EBCDIC_FIXTURE="$REPO/app/data/EBCDIC/AWS.M2.CARDDEMO.CARDDATA.PS"
RECLEN=150

command -v cobc >/dev/null || { echo "cobc (GnuCOBOL) is required: sudo apt-get install -y gnucobol3" >&2; exit 2; }
command -v iconv >/dev/null || { echo "iconv is required" >&2; exit 2; }
command -v mvn  >/dev/null || { echo "mvn is required" >&2; exit 2; }

echo "== build Java"
(cd "$PROJECT" && mvn -q -DskipTests package)

echo "== compile COBOL (GnuCOBOL $(cobc --version | head -1 | awk '{print $NF}'))"
BIN="$OUT/bin"; rm -rf "$BIN"; mkdir -p "$BIN"
cobc -x -I "$REPO/app/cpy" -o "$BIN/cbact02c" "$REPO/app/cbl/CBACT02C.cbl"
cobc -x -o "$BIN/loadksds" "$PROJECT/scripts/cobol/LOADKSDS.cbl"
cobc -m -o "$BIN/CEE3ABD.so" "$PROJECT/scripts/cobol/CEE3ABD.cbl"
export COB_LIBRARY_PATH="$BIN"

# Materialise the KSDS content: strip the line delimiters from the ASCII
# fixture to get 150-byte fixed records (the form the Java port reads), then
# load those records into a GnuCOBOL INDEXED file for the COBOL run.
DATA="$OUT/data"; rm -rf "$DATA"; mkdir -p "$DATA"
tr -d '\n' <"$FIXTURE" >"$DATA/carddata.fixed"
SIZE=$(stat -c %s "$DATA/carddata.fixed")
[[ $((SIZE % RECLEN)) -eq 0 ]] || { echo "fixture is not a multiple of $RECLEN bytes" >&2; exit 1; }
echo "   fixture: $((SIZE / RECLEN)) records of $RECLEN bytes"
# Reverse-ordered copy: a KSDS returns records in key order whatever order they were loaded in.
tac "$FIXTURE" | tr -d '\n' >"$DATA/carddata-reversed.fixed"
: >"$DATA/empty.fixed"
# EBCDIC mainframe extract, normalised from code page 037; must equal the ASCII fixture.
iconv -f CP037 -t ISO-8859-1 "$EBCDIC_FIXTURE" >"$DATA/carddata-ebcdic.fixed"
if cmp -s "$DATA/carddata.fixed" "$DATA/carddata-ebcdic.fixed"; then
  echo "   EBCDIC fixture (CP037 -> ASCII) is identical to the ASCII fixture"
else
  echo "EBCDIC fixture differs from the ASCII fixture after CP037 normalisation" >&2; exit 1
fi

load_ksds() { # SRC DEST
  rm -f "$2"
  DD_INFILE="$1" DD_OUTKSDS="$2" "$BIN/loadksds" >/dev/null
}
load_ksds "$DATA/carddata.fixed"          "$DATA/carddata.ksds"
load_ksds "$DATA/carddata-reversed.fixed" "$DATA/carddata-reversed.ksds"
load_ksds "$DATA/carddata-ebcdic.fixed"   "$DATA/carddata-ebcdic.ksds"
load_ksds "$DATA/empty.fixed"             "$DATA/empty.ksds"

FAIL=0
compare() { # SCENARIO COBOL_CARDFILE JAVA_CARDFILE
  local s="$1" cob_dd="$2" java_dd="$3" d="$OUT/$1"
  rm -rf "$d"; mkdir -p "$d/cobol" "$d/java"
  local rc=0
  DD_CARDFILE="$cob_dd" "$BIN/cbact02c" >"$d/cobol/sysout" 2>"$d/cobol/stderr" || rc=$?
  echo "$rc" >"$d/cobol/rc"
  rc=0
  java -jar "$JAR" CBACT02C --dd CARDFILE="$java_dd" >"$d/java/sysout" 2>"$d/java/stderr" || rc=$?
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
compare carddata           "$DATA/carddata.ksds"          "$DATA/carddata.fixed"
compare carddata-reversed  "$DATA/carddata-reversed.ksds" "$DATA/carddata-reversed.fixed"
compare carddata-ebcdic    "$DATA/carddata-ebcdic.ksds"   "$DATA/carddata-ebcdic.fixed"
compare empty-file         "$DATA/empty.ksds"             "$DATA/empty.fixed"
compare missing-file       "$DATA/no-such.ksds"           "$DATA/no-such.fixed"

if [[ $FAIL -eq 0 ]]; then
  echo "SUMMARY: all compared outputs match"
else
  echo "SUMMARY: differences found" >&2
  exit 1
fi
