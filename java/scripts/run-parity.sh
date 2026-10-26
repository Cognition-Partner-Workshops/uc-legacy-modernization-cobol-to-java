#!/usr/bin/env bash
# Byte-for-byte parity check: original CBACT01C (GnuCOBOL) vs the Java 21 port.
#
#   usage: java/scripts/run-parity.sh [--update-golden] [WORK_DIR]
#
# For every scenario both implementations run against the same account fixture, then
# OUTFILE, ARRYFILE, VBRCFILE, SYSOUT (stdout) and the return code are compared with cmp.
# --update-golden copies the GnuCOBOL outputs into src/test/resources/golden/, which the
# JUnit golden-file test (Cbact01cGoldenTest) checks on every `mvn verify`.
#
# Requires: cobc (GnuCOBOL 3.x with an indexed-file handler), JDK 21, Maven.
set -euo pipefail

UPDATE_GOLDEN=0
if [[ "${1:-}" == "--update-golden" ]]; then UPDATE_GOLDEN=1; shift; fi

JAVA_DIR="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$JAVA_DIR/.." && pwd)"
WORK="${1:-$JAVA_DIR/target/parity}"
mkdir -p "$WORK" && WORK="$(cd "$WORK" && pwd)"
# Only clear the default target/parity, a directory this script created (marker file), or an empty one.
if [[ "$WORK" != "$JAVA_DIR/target/parity" && ! -e "$WORK/.parity-work" && -n "$(ls -A "$WORK")" ]]; then
  echo "refusing to use non-empty WORK_DIR $WORK (not created by run-parity.sh)" >&2; exit 2
fi
GOLDEN="$JAVA_DIR/src/test/resources/golden"
JAR="$JAVA_DIR/target/carddemo-batch.jar"
COBC_FLAGS=(-std=ibm -fsign=EBCDIC -I "$REPO/app/cpy")

for tool in cobc java mvn cmp; do
  command -v "$tool" >/dev/null || { echo "missing required tool: $tool" >&2; exit 2; }
done

find "$WORK" -mindepth 1 -delete
mkdir -p "$WORK/bin"
touch "$WORK/.parity-work"

echo "== build Java port"
mvn -q -f "$JAVA_DIR/pom.xml" -DskipTests package

echo "== compile COBOL ($(cobc --version | head -1))"
cobc -x "${COBC_FLAGS[@]}" -o "$WORK/bin/CBACT01C" \
  "$REPO/app/cbl/CBACT01C.cbl" "$JAVA_DIR/scripts/cobol/COBDATFT.cbl" "$JAVA_DIR/scripts/cobol/CEE3ABD.cbl"
cobc -x "${COBC_FLAGS[@]}" -o "$WORK/bin/LOADKSDS" "$JAVA_DIR/scripts/cobol/LOADKSDS.cbl"

# GnuCOBOL default; the Java port writes the same 4-byte record header.
export COB_VARSEQ_FORMAT=0
OUTPUTS=(OUTFILE ARRYFILE VBRCFILE SYSOUT RC)
REPORT="$WORK/parity-report.txt"
FAILED=0
SCENARIOS=()
printf '%-26s %-9s %-8s %10s %10s\n' SCENARIO OUTPUT STATUS COBOL_B JAVA_B | tee "$REPORT"

# scenario NAME FIXTURE [OUTFILE_OVERRIDE]
#   FIXTURE: line-sequential 300-byte account records, or "-" for "ACCTFILE does not exist".
#   OUTFILE_OVERRIDE: path (relative to each side's dir) to force an OUTFILE open failure.
scenario() {
  local name="$1" fixture="$2" outfile="${3:-OUTFILE}"
  local dir="$WORK/$name"
  mkdir -p "$dir/cobol" "$dir/java"
  local ksds="$dir/ACCTFILE.ksds" flat="$dir/ACCTFILE.flat"
  if [[ "$fixture" == "-" ]]; then
    ksds="$dir/missing.ksds"; flat="$dir/missing.flat"
  else
    DD_INFILE="$fixture" DD_KSDSFILE="$ksds" DD_FLATFILE="$flat" "$WORK/bin/LOADKSDS" >"$dir/load.log"
  fi

  local rc=0
  (cd "$dir/cobol" && DD_ACCTFILE="$ksds" DD_OUTFILE="$outfile" DD_ARRYFILE=ARRYFILE DD_VBRCFILE=VBRCFILE \
     "$WORK/bin/CBACT01C" >SYSOUT 2>stderr.txt) || rc=$?
  echo "$rc" >"$dir/cobol/RC"
  if [[ $rc -eq 126 || $rc -eq 127 ]]; then
    echo "COBOL executable could not be run in scenario $name (rc=$rc)" >&2; exit 2
  fi

  rc=0
  (cd "$dir/java" && java -jar "$JAR" CBACT01C --dd ACCTFILE="$flat" --dd OUTFILE="$outfile" \
     --dd ARRYFILE=ARRYFILE --dd VBRCFILE=VBRCFILE >SYSOUT 2>stderr.txt) || rc=$?
  echo "$rc" >"$dir/java/RC"

  local out status cb jb
  for out in "${OUTPUTS[@]}"; do
    if [[ ! -e "$dir/cobol/$out" && ! -e "$dir/java/$out" ]]; then
      status="ABSENT"; cb="-"; jb="-"
    elif [[ -e "$dir/cobol/$out" && -e "$dir/java/$out" ]] && cmp -s "$dir/cobol/$out" "$dir/java/$out"; then
      status="MATCH"; cb=$(wc -c <"$dir/cobol/$out"); jb=$(wc -c <"$dir/java/$out")
    else
      status="DIFF"; FAILED=1
      cb=$( [[ -e "$dir/cobol/$out" ]] && wc -c <"$dir/cobol/$out" || echo none)
      jb=$( [[ -e "$dir/java/$out" ]] && wc -c <"$dir/java/$out" || echo none)
    fi
    printf '%-26s %-9s %-8s %10s %10s\n' "$name" "$out" "$status" "$cb" "$jb" | tee -a "$REPORT"
  done

  SCENARIOS+=("$name")
}

# Goldens are refreshed only from a fully matching run, so a broken COBOL run cannot replace them.
update_golden() {
  local name out
  for name in "${SCENARIOS[@]}"; do
    rm -rf "${GOLDEN:?}/$name"
    mkdir -p "$GOLDEN/$name"
    for out in "${OUTPUTS[@]}"; do
      if [[ -e "$WORK/$name/cobol/$out" ]]; then cp "$WORK/$name/cobol/$out" "$GOLDEN/$name/$out"; fi
    done
  done
  echo "golden files updated in $GOLDEN"
}

scenario acctdata               "$REPO/app/data/ASCII/acctdata.txt"
scenario edge-cases             "$JAVA_DIR/src/test/resources/fixtures/acct-edge.txt"
scenario empty-acctfile         /dev/null
scenario abend-missing-acctfile -
scenario abend-outfile-open     "$JAVA_DIR/src/test/resources/fixtures/acct-edge.txt" no-such-dir/OUTFILE

echo
if [[ $FAILED -eq 0 ]]; then
  echo "PARITY: all compared outputs match" | tee -a "$REPORT"
  if [[ $UPDATE_GOLDEN -eq 1 ]]; then update_golden; fi
else
  echo "PARITY: MISMATCH (see $WORK)" | tee -a "$REPORT"
  if [[ $UPDATE_GOLDEN -eq 1 ]]; then echo "golden files NOT updated" >&2; fi
  exit 1
fi
