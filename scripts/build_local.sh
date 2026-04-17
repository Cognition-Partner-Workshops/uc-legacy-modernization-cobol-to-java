#!/bin/bash
##############################################################################
# build_local.sh - Build CardDemo COBOL programs locally using GnuCOBOL
#
# Prerequisites:
#   - GnuCOBOL 3.2+ with BDB indexed file support (cobc)
#   - Build from source: https://ftp.gnu.org/gnu/gnucobol/gnucobol-3.2.tar.xz
#     ./configure --with-db --prefix=/usr/local && make -j$(nproc) && sudo make install && sudo ldconfig
#
# Usage: ./scripts/build_local.sh
##############################################################################
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_DIR="$REPO_ROOT/build"
CPY_DIR="$REPO_ROOT/app/cpy"
CBL_DIR="$REPO_ROOT/app/cbl"
COBC="${COBC:-cobc}"

# Check cobc is available
if ! command -v "$COBC" &>/dev/null; then
    echo "ERROR: cobc (GnuCOBOL compiler) not found. Install GnuCOBOL 3.2+ with BDB support."
    exit 1
fi

# Check indexed file support
if ! "$COBC" --info 2>&1 | grep -q "indexed file handler.*BDB\|indexed file handler.*VBISAM"; then
    echo "WARNING: cobc does not have indexed file support (BDB/VBISAM)."
    echo "  Batch programs using indexed files will not work at runtime."
    echo "  Build GnuCOBOL from source with --with-db flag."
fi

mkdir -p "$BUILD_DIR"

echo "============================================"
echo " CardDemo Local Build"
echo " Compiler: $($COBC --version 2>&1 | head -1)"
echo "============================================"
echo ""

# Batch programs (standalone executables)
BATCH_PROGRAMS=(
    CBACT01C CBACT02C CBACT03C CBCUS01C
    CBTRN01C CBTRN02C CBTRN03C
    CBEXPORT CBIMPORT
    COBSWAIT
)

# Subprograms (compiled as shared modules)
SUB_PROGRAMS=(
    CBACT04C CSUTLDTC
)

# Programs with .CBL extension (mixed case)
CBL_UPPER_MAIN=(CBSTM03A)
CBL_UPPER_SUB=(CBSTM03B)

success=0
fail=0
skip=0

echo "--- Compiling batch programs (executables) ---"
for prog in "${BATCH_PROGRAMS[@]}"; do
    src="$CBL_DIR/${prog}.cbl"
    out="$BUILD_DIR/$(echo "$prog" | tr '[:upper:]' '[:lower:]')"
    if [ ! -f "$src" ]; then
        echo "  SKIP  $prog (source not found)"
        skip=$((skip+1))
        continue
    fi
    if $COBC -x -I "$CPY_DIR" -std=ibm-strict -o "$out" "$src" 2>/dev/null; then
        echo "  OK    $prog -> $(basename "$out")"
        success=$((success+1))
    else
        echo "  FAIL  $prog"
        fail=$((fail+1))
    fi
done

echo ""
echo "--- Compiling subprograms (shared modules) ---"
for prog in "${SUB_PROGRAMS[@]}"; do
    src="$CBL_DIR/${prog}.cbl"
    out="$BUILD_DIR/$(echo "$prog" | tr '[:upper:]' '[:lower:]').so"
    if [ ! -f "$src" ]; then
        echo "  SKIP  $prog (source not found)"
        skip=$((skip+1))
        continue
    fi
    if $COBC -m -I "$CPY_DIR" -std=ibm-strict -o "$out" "$src" 2>/dev/null; then
        echo "  OK    $prog -> $(basename "$out")"
        success=$((success+1))
    else
        echo "  FAIL  $prog"
        fail=$((fail+1))
    fi
done

echo ""
echo "--- Compiling .CBL programs ---"
for prog in "${CBL_UPPER_MAIN[@]}"; do
    src="$CBL_DIR/${prog}.CBL"
    out="$BUILD_DIR/$(echo "$prog" | tr '[:upper:]' '[:lower:]')"
    if $COBC -x -I "$CPY_DIR" -std=ibm-strict -o "$out" "$src" 2>/dev/null; then
        echo "  OK    $prog -> $(basename "$out")"
        success=$((success+1))
    else
        echo "  FAIL  $prog"
        fail=$((fail+1))
    fi
done
for prog in "${CBL_UPPER_SUB[@]}"; do
    src="$CBL_DIR/${prog}.CBL"
    out="$BUILD_DIR/$(echo "$prog" | tr '[:upper:]' '[:lower:]').so"
    if $COBC -m -I "$CPY_DIR" -std=ibm-strict -o "$out" "$src" 2>/dev/null; then
        echo "  OK    $prog -> $(basename "$out")"
        success=$((success+1))
    else
        echo "  FAIL  $prog"
        fail=$((fail+1))
    fi
done

echo ""
echo "============================================"
echo " Build Summary"
echo "  Compiled:  $success"
echo "  Failed:    $fail"
echo "  Skipped:   $skip"
echo "  Output:    $BUILD_DIR/"
echo "============================================"
echo ""

# Note about CICS programs
echo "NOTE: Online (CICS) programs (CO*.cbl) are not compiled locally"
echo "  because they require CICS API headers (DFHBMSCA) and BMS-generated"
echo "  copybooks that are only available in a mainframe environment."

exit $fail
