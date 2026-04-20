#!/bin/bash
# ================================================================
# run_all_tests.sh - CardDemo Unit Test Orchestration Script
#
# Compiles and runs all test drivers and extracted business logic
# subprograms using GnuCOBOL (cobc).
#
# Prerequisites:
#   - GnuCOBOL installed (cobc command available)
#   - Run from the repository root directory
#
# Usage:
#   ./tests/scripts/run_all_tests.sh
#
# Exit codes:
#   0 - All tests passed
#   1 - Compilation errors
#   2 - One or more test suites failed
# ================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
TEST_DIR="$REPO_ROOT/tests"
DRIVER_DIR="$TEST_DIR/drivers"
APP_CBL_DIR="$REPO_ROOT/app/cbl"
APP_CPY_DIR="$REPO_ROOT/app/cpy"
BUILD_DIR="$TEST_DIR/build"
DATA_DIR="$TEST_DIR/data"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

TOTAL_SUITES=0
PASSED_SUITES=0
FAILED_SUITES=0
OVERALL_RC=0

# ================================================================
# Helper functions
# ================================================================
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_header() {
    echo ""
    echo "=============================================="
    echo "$1"
    echo "=============================================="
}

# ================================================================
# Check prerequisites
# ================================================================
check_prerequisites() {
    log_header "CHECKING PREREQUISITES"

    if ! command -v cobc &> /dev/null; then
        log_error "GnuCOBOL (cobc) is not installed."
        log_info "Install with: sudo apt-get install gnucobol"
        exit 1
    fi

    COBC_VERSION=$(cobc --version 2>&1 | head -1)
    log_info "COBOL Compiler: $COBC_VERSION"
    log_info "Repository root: $REPO_ROOT"
    log_info "Copybook path: $APP_CPY_DIR"
}

# ================================================================
# Prepare build directory
# ================================================================
prepare_build() {
    log_header "PREPARING BUILD ENVIRONMENT"

    mkdir -p "$BUILD_DIR"
    rm -f "$BUILD_DIR"/*

    log_info "Build directory: $BUILD_DIR"
}

# ================================================================
# Compile a COBOL program
# Arguments:
#   $1 - Source file path
#   $2 - Output module name (optional, for subprograms)
#   $3 - Extra flags (optional, e.g. "-x" for executables)
# ================================================================
compile_cobol() {
    local SRC="$1"
    local BASENAME=$(basename "$SRC" .cbl)
    local BASENAME_UPPER=$(basename "$SRC" .CBL)
    local EXTRA_FLAGS="${3:--x}"

    log_info "Compiling $BASENAME ..."

    cobc $EXTRA_FLAGS -I "$APP_CPY_DIR" -o "$BUILD_DIR/$BASENAME" "$SRC" 2>&1
    local RC=$?

    if [ $RC -ne 0 ]; then
        log_error "Compilation of $BASENAME failed (RC=$RC)"
        return 1
    fi

    log_info "  -> $BUILD_DIR/$BASENAME compiled successfully"
    return 0
}

# ================================================================
# Compile a subprogram (shared object / module)
# ================================================================
compile_subprogram() {
    local SRC="$1"
    local BASENAME=$(basename "$SRC" .cbl)

    log_info "Compiling subprogram $BASENAME ..."

    cobc -m -I "$APP_CPY_DIR" -o "$BUILD_DIR/$BASENAME.so" "$SRC" 2>&1
    local RC=$?

    if [ $RC -ne 0 ]; then
        log_error "Compilation of subprogram $BASENAME failed (RC=$RC)"
        return 1
    fi

    log_info "  -> $BUILD_DIR/$BASENAME.so compiled successfully"
    return 0
}

# ================================================================
# Run a test driver
# Arguments:
#   $1 - Test driver executable name
#   $2 - Description
# ================================================================
run_test_driver() {
    local DRIVER="$1"
    local DESC="$2"

    TOTAL_SUITES=$((TOTAL_SUITES + 1))

    log_header "RUNNING: $DESC"

    export COB_LIBRARY_PATH="$BUILD_DIR"
    cd "$BUILD_DIR"

    "./$DRIVER" 2>&1
    local RC=$?

    cd "$REPO_ROOT"

    if [ $RC -eq 0 ]; then
        PASSED_SUITES=$((PASSED_SUITES + 1))
        log_info "Suite PASSED (RC=$RC)"
    else
        FAILED_SUITES=$((FAILED_SUITES + 1))
        OVERALL_RC=2
        log_error "Suite FAILED (RC=$RC)"
    fi

    return $RC
}

# ================================================================
# MAIN EXECUTION
# ================================================================
main() {
    log_header "CARDDEMO UNIT TEST SUITE"
    echo "Starting test execution at $(date)"

    check_prerequisites
    prepare_build

    # ============================================================
    # Phase 1: Compile all subprograms
    # ============================================================
    log_header "PHASE 1: COMPILING SUBPROGRAMS"

    compile_subprogram "$APP_CBL_DIR/COSGN00B.cbl" || exit 1
    compile_subprogram "$APP_CBL_DIR/COBIL00B.cbl" || exit 1

    # ============================================================
    # Phase 2: Compile all test drivers
    # ============================================================
    log_header "PHASE 2: COMPILING TEST DRIVERS"

    compile_cobol "$DRIVER_DIR/TSGN00C.cbl" || exit 1
    compile_cobol "$DRIVER_DIR/TBIL00C.cbl" || exit 1

    # ============================================================
    # Phase 3: Run test suites
    # ============================================================
    log_header "PHASE 3: RUNNING TEST SUITES"

    # Authentication tests
    run_test_driver "TSGN00C" "Authentication Business Logic Tests" || true

    # Bill payment tests
    run_test_driver "TBIL00C" "Bill Payment Business Logic Tests" || true

    # ============================================================
    # Final Summary
    # ============================================================
    log_header "FINAL TEST SUMMARY"
    echo "Test suites run    : $TOTAL_SUITES"
    echo "Test suites passed : $PASSED_SUITES"
    echo "Test suites failed : $FAILED_SUITES"
    echo ""
    echo "Finished at $(date)"

    if [ $OVERALL_RC -eq 0 ]; then
        log_info "ALL TESTS PASSED"
    else
        log_error "SOME TESTS FAILED"
    fi

    exit $OVERALL_RC
}

main "$@"
