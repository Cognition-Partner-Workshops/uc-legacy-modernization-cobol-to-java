#!/bin/bash
# =============================================================================
# Test runner for ADD2NUMS batch COBOL program
# Compiles the program, runs each test case, and compares output to expected.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$SCRIPT_DIR/../../samples/batch-add"
TEST_DIR="$SCRIPT_DIR"
BUILD_DIR="$SCRIPT_DIR/build"

PASS=0
FAIL=0
ERRORS=""

# Clean and create build directory
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"

# --- Compile ---
echo "=== Compiling ADD2NUMS.cbl ==="
if ! cobc -x -o "$BUILD_DIR/ADD2NUMS" "$SRC_DIR/ADD2NUMS.cbl"; then
    echo "FATAL: Compilation failed"
    exit 1
fi
echo "Compilation OK"
echo ""

# --- Run test cases ---
for INPUT_FILE in "$TEST_DIR"/tc*.input; do
    TC_NAME=$(basename "$INPUT_FILE" .input)
    EXPECTED_FILE="$TEST_DIR/${TC_NAME}.expected"
    ACTUAL_FILE="$BUILD_DIR/${TC_NAME}.actual"

    if [ ! -f "$EXPECTED_FILE" ]; then
        echo "SKIP $TC_NAME — no .expected file"
        continue
    fi

    echo "--- $TC_NAME ---"

    # Set the input file via environment variable (GnuCOBOL file assignment)
    export ADDINPUT="$INPUT_FILE"
    "$BUILD_DIR/ADD2NUMS" > "$ACTUAL_FILE" 2>&1 || true

    # Compare actual vs expected
    if diff -u "$EXPECTED_FILE" "$ACTUAL_FILE" > "$BUILD_DIR/${TC_NAME}.diff" 2>&1; then
        echo "  PASS"
        PASS=$((PASS + 1))
    else
        echo "  FAIL"
        cat "$BUILD_DIR/${TC_NAME}.diff"
        FAIL=$((FAIL + 1))
        ERRORS="$ERRORS  - $TC_NAME\n"
    fi
    echo ""
done

# --- Summary ---
TOTAL=$((PASS + FAIL))
echo "==========================================="
echo "  RESULTS: $PASS/$TOTAL passed, $FAIL failed"
echo "==========================================="

if [ $FAIL -gt 0 ]; then
    echo ""
    echo "Failed tests:"
    echo -e "$ERRORS"
    exit 1
fi

exit 0
