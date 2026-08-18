#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
ROOT="${1:-$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)}"
ROOT="$(CDPATH= cd -- "$ROOT" && pwd)"
REF_DIR="$ROOT/carddemo-java/cobol-ref"
OUT_DIR="$ROOT/carddemo-java/target/cobol-ref"
BUILD_DIR="$OUT_DIR/build"
DATA_DIR="$OUT_DIR/data"
INDEX_DIR="$OUT_DIR/index"

rm -rf "$OUT_DIR"
mkdir -p "$BUILD_DIR" "$DATA_DIR" "$INDEX_DIR"

python3 "$REF_DIR/prepare_data.py" "$ROOT/app/data/ASCII" "$DATA_DIR"

cobc -ftab-width=1 -I "$ROOT/app/cpy" -x -o "$BUILD_DIR/LOADIDX" \
    "$REF_DIR/LOADIDX.cbl"
cobc -ftab-width=1 -I "$ROOT/app/cpy" -m -o "$BUILD_DIR/CBSTM03B.so" \
    "$REF_DIR/CBSTM03B.cbl"
cobc -ftab-width=1 -I "$ROOT/app/cpy" -x -o "$BUILD_DIR/CBSTM03A" \
    "$REF_DIR/CBSTM03A.cbl"

TRNXIN="$DATA_DIR/trxfl.dat" \
XREFIN="$DATA_DIR/xref.dat" \
CUSTIN="$DATA_DIR/cust.dat" \
ACCTIN="$DATA_DIR/acct.dat" \
TRNXFILE="$INDEX_DIR/trxfl.bdb" \
XREFFILE="$INDEX_DIR/xref.bdb" \
CUSTFILE="$INDEX_DIR/cust.bdb" \
ACCTFILE="$INDEX_DIR/acct.bdb" \
COB_LIBRARY_PATH="$BUILD_DIR" \
LD_LIBRARY_PATH="$BUILD_DIR${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
    "$BUILD_DIR/LOADIDX"

STMTFILE="$OUT_DIR/statement.txt" \
HTMLFILE="$OUT_DIR/statement.html" \
TRNXFILE="$INDEX_DIR/trxfl.bdb" \
XREFFILE="$INDEX_DIR/xref.bdb" \
CUSTFILE="$INDEX_DIR/cust.bdb" \
ACCTFILE="$INDEX_DIR/acct.bdb" \
COB_LIBRARY_PATH="$BUILD_DIR" \
LD_LIBRARY_PATH="$BUILD_DIR${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}" \
    "$BUILD_DIR/CBSTM03A"
