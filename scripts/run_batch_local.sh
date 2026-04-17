#!/bin/bash
##############################################################################
# run_batch_local.sh - Run CardDemo batch programs locally using GnuCOBOL
#
# Prerequisites:
#   - Run build_local.sh first to compile the programs
#   - GnuCOBOL runtime libraries in LD_LIBRARY_PATH
#
# Usage: ./scripts/run_batch_local.sh
##############################################################################
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_DIR="$REPO_ROOT/build"
DATA_DIR="$REPO_ROOT/build/data"
ASCII_DIR="$REPO_ROOT/app/data/ASCII"

export LD_LIBRARY_PATH="${LD_LIBRARY_PATH:-/usr/local/lib}"
export COB_LIBRARY_PATH="$BUILD_DIR"
COBC="${COBC:-cobc}"

# Check build directory
if [ ! -f "$BUILD_DIR/cbtrn02c" ]; then
    echo "ERROR: Build artifacts not found. Run ./scripts/build_local.sh first."
    exit 1
fi

mkdir -p "$DATA_DIR"

echo "============================================"
echo " CardDemo Local Batch Execution"
echo "============================================"
echo ""

##############################################################################
# Helper: generate, compile, and run a flat-to-indexed loader
##############################################################################
load_indexed_file() {
    local tag="$1" reclen="$2" keylen="$3" flatfile="$4" idxfile="$5"
    local datalen=$((reclen - keylen))
    local src="/tmp/loader_${tag}.cbl"
    local bin="$BUILD_DIR/loader_${tag}"

    # Generate COBOL source using printf to avoid heredoc issues
    printf '       IDENTIFICATION DIVISION.\n' > "$src"
    printf '       PROGRAM-ID. LOADER.\n' >> "$src"
    printf '       ENVIRONMENT DIVISION.\n' >> "$src"
    printf '       INPUT-OUTPUT SECTION.\n' >> "$src"
    printf '       FILE-CONTROL.\n' >> "$src"
    printf '           SELECT FLAT-FILE ASSIGN TO FLATFILE\n' >> "$src"
    printf '               ORGANIZATION IS LINE SEQUENTIAL\n' >> "$src"
    printf '               FILE STATUS IS WS-FS.\n' >> "$src"
    printf '           SELECT IDX-FILE ASSIGN TO IDXFILE\n' >> "$src"
    printf '               ORGANIZATION IS INDEXED\n' >> "$src"
    printf '               ACCESS MODE IS RANDOM\n' >> "$src"
    printf '               RECORD KEY IS FD-KEY\n' >> "$src"
    printf '               FILE STATUS IS WS-IS.\n' >> "$src"
    printf '       DATA DIVISION.\n' >> "$src"
    printf '       FILE SECTION.\n' >> "$src"
    printf '       FD FLAT-FILE.\n' >> "$src"
    printf '       01 FLAT-REC              PIC X(%d).\n' "$reclen" >> "$src"
    printf '       FD IDX-FILE.\n' >> "$src"
    printf '       01 IDX-REC.\n' >> "$src"
    printf '           05 FD-KEY            PIC X(%d).\n' "$keylen" >> "$src"
    printf '           05 FD-DATA           PIC X(%d).\n' "$datalen" >> "$src"
    printf '       WORKING-STORAGE SECTION.\n' >> "$src"
    printf '       01 WS-FS                 PIC X(02).\n' >> "$src"
    printf '       01 WS-IS                 PIC X(02).\n' >> "$src"
    printf '       01 WS-N                  PIC 9(09) VALUE 0.\n' >> "$src"
    printf '       01 WS-EOF                PIC X VALUE '"'"'N'"'"'.\n' >> "$src"
    printf '       PROCEDURE DIVISION.\n' >> "$src"
    printf '           OPEN INPUT FLAT-FILE\n' >> "$src"
    printf '           OPEN OUTPUT IDX-FILE\n' >> "$src"
    printf '           PERFORM UNTIL WS-EOF = '"'"'Y'"'"'\n' >> "$src"
    printf '               READ FLAT-FILE INTO IDX-REC\n' >> "$src"
    printf '               AT END MOVE '"'"'Y'"'"' TO WS-EOF\n' >> "$src"
    printf '               NOT AT END\n' >> "$src"
    printf '                   WRITE IDX-REC\n' >> "$src"
    printf '                   ADD 1 TO WS-N\n' >> "$src"
    printf '               END-READ\n' >> "$src"
    printf '           END-PERFORM\n' >> "$src"
    printf '           CLOSE FLAT-FILE\n' >> "$src"
    printf '           CLOSE IDX-FILE\n' >> "$src"
    printf '           DISPLAY '"'"'  %s: '"'"' WS-N '"'"' records'"'"'\n' "$tag" >> "$src"
    printf '           GOBACK.\n' >> "$src"

    "$COBC" -x -std=ibm-strict -o "$bin" "$src" 2>/dev/null
    FLATFILE="$flatfile" IDXFILE="$idxfile" "$bin"
}

##############################################################################
# Step 1: Load ASCII data into indexed (BDB) files
##############################################################################
echo "--- Step 1: Loading sample data into indexed files ---"
echo ""

load_indexed_file "acct"     300 11 "$ASCII_DIR/acctdata.txt"  "$DATA_DIR/acctfile.dat"
load_indexed_file "xref"      50 16 "$ASCII_DIR/cardxref.txt"  "$DATA_DIR/xreffile.dat"
load_indexed_file "card"     150 16 "$ASCII_DIR/carddata.txt"  "$DATA_DIR/cardfile.dat"
load_indexed_file "cust"     500  9 "$ASCII_DIR/custdata.txt"  "$DATA_DIR/custfile.dat"
load_indexed_file "tcatbal"   50 17 "$ASCII_DIR/tcatbal.txt"   "$DATA_DIR/tcatbalf.dat"
load_indexed_file "trantype"  60  2 "$ASCII_DIR/trantype.txt"  "$DATA_DIR/trantype.dat"
load_indexed_file "trancatg"  60  4 "$ASCII_DIR/trancatg.txt"  "$DATA_DIR/trancatg.dat"
load_indexed_file "discgrp"   50  2 "$ASCII_DIR/discgrp.txt"   "$DATA_DIR/discgrp.dat"

echo ""

##############################################################################
# Step 2: Run CBTRN02C - Core Transaction Posting
##############################################################################
echo "--- Step 2: Running CBTRN02C (Transaction Posting) ---"
echo ""

DALYTRAN="$ASCII_DIR/dailytran.txt" \
TRANFILE="$DATA_DIR/transact.dat" \
XREFFILE="$DATA_DIR/xreffile.dat" \
DALYREJS="$DATA_DIR/rejects.dat" \
ACCTFILE="$DATA_DIR/acctfile.dat" \
TCATBALF="$DATA_DIR/tcatbalf.dat" \
"$BUILD_DIR/cbtrn02c"

echo ""

##############################################################################
# Step 3: Run CBACT01C - Account Display
##############################################################################
echo "--- Step 3: Running CBACT01C (Account Display) ---"
echo ""

ACCTFILE="$DATA_DIR/acctfile.dat" \
CARDFILE="$DATA_DIR/cardfile.dat" \
XREFFILE="$DATA_DIR/xreffile.dat" \
CUSTFILE="$DATA_DIR/custfile.dat" \
CARDAIX="$DATA_DIR/cardaix.dat" \
"$BUILD_DIR/cbact01c" 2>&1 | head -60 || true

echo ""
echo "============================================"
echo " Batch Execution Complete"
echo "============================================"
echo ""
echo "Output files in: $DATA_DIR/"
ls -la "$DATA_DIR/" 2>/dev/null || true
