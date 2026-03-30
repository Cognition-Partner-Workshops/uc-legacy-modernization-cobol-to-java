#!/bin/bash
###################################################################
# run_batch_transfer.sh - Batch Transfer Orchestration Script
#                         (JCL Replacement for NDM Jobs)
#
# Application : CardDemo
# Purpose     : Replaces the mainframe JCL that invoked NDM
#               Connect:Direct processes. This script orchestrates
#               the full batch data transfer workflow:
#
#               1. Export data from CardDemo files (COBOL program)
#               2. Send exported files to remote system (SFTP)
#               3. Receive inbound files from remote system (SFTP)
#               4. Import received data into CardDemo (COBOL program)
#
# NDM JCL Equivalent:
#   This script replaces JCL steps like:
#     //STEP01  EXEC PGM=DMWBATCH  (Connect:Direct batch interface)
#     //SYSIN   DD *
#       SUBMIT PROCESS=CARDDEMO_SEND ...
#     //STEP02  EXEC PGM=DMWBATCH
#     //SYSIN   DD *
#       SUBMIT PROCESS=CARDDEMO_RECV ...
#
# Usage:
#   run_batch_transfer.sh [--config <config-file>]
#                         [--export-only | --transfer-only | --import-only]
#                         [--dry-run]
#
# Copyright Amazon.com, Inc. or its affiliates.
# All Rights Reserved.
# Licensed under the Apache License, Version 2.0
###################################################################

set -euo pipefail

# ------------------------------------------------------------------
# Configuration
# ------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_FILE="${SCRIPT_DIR}/sftp_transfer.conf"
DRY_RUN=false
RUN_EXPORT=true
RUN_TRANSFER=true
RUN_IMPORT=true

# CardDemo paths (adjust for your Micro Focus COBOL installation)
CARDDEMO_HOME="${CARDDEMO_HOME:-/opt/carddemo}"
COBOL_BIN="${CARDDEMO_HOME}/bin"
DATA_DIR="${CARDDEMO_HOME}/data"
EXPORT_DIR="${DATA_DIR}/export"
IMPORT_DIR="${DATA_DIR}/import"
ARCHIVE_DIR="${DATA_DIR}/archive"
LOG_DIR="${CARDDEMO_HOME}/logs"
SFTP_SCRIPT="${SCRIPT_DIR}/sftp_transfer.sh"

# Batch Job tracking
JOB_ID="BATCH$(date +%Y%m%d%H%M%S)"
JOB_LOG="${LOG_DIR}/batch_transfer_${JOB_ID}.log"
STEP_NUM=0
STEP_RC=0
MAX_RC=0

# ------------------------------------------------------------------
# Logging (replaces JCL SYSOUT)
# ------------------------------------------------------------------
log_info()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$JOB_ID] INFO  $*" | tee -a "$JOB_LOG"; }
log_warn()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$JOB_ID] WARN  $*" | tee -a "$JOB_LOG"; }
log_error() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] [$JOB_ID] ERROR $*" | tee -a "$JOB_LOG" >&2; }

log_step_header() {
    STEP_NUM=$((STEP_NUM + 1))
    local step_name="$1"
    cat <<EOF | tee -a "$JOB_LOG"

===================================================================
  STEP ${STEP_NUM}: ${step_name}
  Job: ${JOB_ID}  Time: $(date '+%Y-%m-%d %H:%M:%S')
===================================================================
EOF
}

log_step_footer() {
    local rc="$1"
    STEP_RC=$rc
    if [[ $rc -gt $MAX_RC ]]; then
        MAX_RC=$rc
    fi
    cat <<EOF | tee -a "$JOB_LOG"
-------------------------------------------------------------------
  STEP ${STEP_NUM} RC = ${rc}  (MAX RC = ${MAX_RC})
-------------------------------------------------------------------
EOF
}

# ------------------------------------------------------------------
# Usage
# ------------------------------------------------------------------
usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

Batch Transfer Orchestration - NDM/JCL Replacement

Options:
  --config <file>      Path to SFTP config file (default: sftp_transfer.conf)
  --export-only        Only run the COBOL export step
  --transfer-only      Only run SFTP transfers (skip export/import)
  --import-only        Only run the COBOL import step
  --dry-run            Show what would be done without executing
  --help               Show this help

Environment:
  CARDDEMO_HOME        CardDemo installation directory (default: /opt/carddemo)
EOF
    exit 0
}

# ------------------------------------------------------------------
# Argument Parsing
# ------------------------------------------------------------------
parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --config)        CONFIG_FILE="$2"; shift 2 ;;
            --export-only)   RUN_EXPORT=true; RUN_TRANSFER=false; RUN_IMPORT=false; shift ;;
            --transfer-only) RUN_EXPORT=false; RUN_TRANSFER=true; RUN_IMPORT=false; shift ;;
            --import-only)   RUN_EXPORT=false; RUN_TRANSFER=false; RUN_IMPORT=true; shift ;;
            --dry-run)       DRY_RUN=true; shift ;;
            --help|-h)       usage ;;
            *)               log_error "Unknown option: $1"; usage ;;
        esac
    done
}

# ------------------------------------------------------------------
# Initialization (replaces JCL JOB card setup)
# ------------------------------------------------------------------
initialize() {
    log_info "========================================================"
    log_info "CardDemo Batch Transfer Job: $JOB_ID"
    log_info "========================================================"
    log_info "CARDDEMO_HOME : $CARDDEMO_HOME"
    log_info "Config File   : $CONFIG_FILE"
    log_info "Dry Run       : $DRY_RUN"
    log_info "========================================================"

    # Load SFTP configuration
    if [[ -f "$CONFIG_FILE" ]]; then
        log_info "Loading config from $CONFIG_FILE"
        set -a
        # shellcheck disable=SC1090
        source "$CONFIG_FILE"
        set +a
    else
        log_warn "Config file not found: $CONFIG_FILE"
        log_warn "Using environment variables for SFTP configuration"
    fi

    # Create required directories
    mkdir -p "$EXPORT_DIR" "$IMPORT_DIR" "$ARCHIVE_DIR" "$LOG_DIR"
}

# ------------------------------------------------------------------
# STEP: Export Data (replaces CBEXPORT JCL step)
# ------------------------------------------------------------------
step_export_data() {
    log_step_header "EXPORT - Run CBEXPORT to extract CardDemo data"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY RUN] Would execute: ${COBOL_BIN}/CBEXPORT"
        log_step_footer 0
        return
    fi

    if [[ ! -x "${COBOL_BIN}/CBEXPORT" ]]; then
        log_warn "CBEXPORT not found at ${COBOL_BIN}/CBEXPORT"
        log_warn "Skipping export step - using pre-existing export files"
        log_step_footer 0
        return
    fi

    # Set up file assignments for Micro Focus COBOL
    export CUSTFILE="${DATA_DIR}/CUSTDATA.dat"
    export ACCTFILE="${DATA_DIR}/ACCTDATA.dat"
    export XREFFILE="${DATA_DIR}/CARDXREF.dat"
    export TRANSACT="${DATA_DIR}/TRANSACT.dat"
    export CARDFILE="${DATA_DIR}/CARDDATA.dat"
    export EXPFILE="${EXPORT_DIR}/CARDDEMO_EXPORT.dat"

    "${COBOL_BIN}/CBEXPORT" 2>&1 | tee -a "$JOB_LOG"
    local rc=${PIPESTATUS[0]}

    if [[ $rc -eq 0 ]]; then
        log_info "Export completed successfully"
        log_info "Export file: $EXPFILE"
        log_info "Export size: $(stat -c%s "$EXPFILE" 2>/dev/null || echo 'N/A') bytes"
    else
        log_error "Export failed with RC=$rc"
    fi

    log_step_footer $rc
}

# ------------------------------------------------------------------
# STEP: Send Exported Files (replaces NDM SEND Process)
# ------------------------------------------------------------------
step_send_files() {
    log_step_header "SEND - Transfer exported files via SFTP (NDM SEND replacement)"

    local files_to_send=(
        "CARDDEMO_EXPORT.dat"
        "CUSTDATA.dat"
        "ACCTDATA.dat"
    )

    local send_rc=0

    for filename in "${files_to_send[@]}"; do
        local local_path="${EXPORT_DIR}/${filename}"
        local remote_path="${REMOTE_INBOUND_DIR:-/data/inbound/carddemo}/${filename}"

        if [[ ! -f "$local_path" ]]; then
            log_warn "File not found, skipping: $local_path"
            continue
        fi

        log_info "Sending: $local_path -> $remote_path"

        if [[ "$DRY_RUN" == true ]]; then
            log_info "[DRY RUN] Would send $filename"
            continue
        fi

        "$SFTP_SCRIPT" \
            --mode SEND \
            --local-file "$local_path" \
            --remote-file "$remote_path" \
            --checksum \
            --log-file "${LOG_DIR}/sftp_send_${JOB_ID}.log" \
            2>&1 | tee -a "$JOB_LOG"

        local rc=${PIPESTATUS[0]}
        if [[ $rc -ne 0 ]]; then
            log_error "Failed to send $filename (RC=$rc)"
            send_rc=$rc
        else
            log_info "Successfully sent $filename"
            # Archive the sent file
            cp "$local_path" "${ARCHIVE_DIR}/${filename}.$(date +%Y%m%d%H%M%S)"
        fi
    done

    log_step_footer $send_rc
}

# ------------------------------------------------------------------
# STEP: Receive Inbound Files (replaces NDM RECEIVE Process)
# ------------------------------------------------------------------
step_receive_files() {
    log_step_header "RECEIVE - Retrieve inbound files via SFTP (NDM RECEIVE replacement)"

    local files_to_receive=(
        "DALYTRAN.dat"
    )

    local recv_rc=0

    for filename in "${files_to_receive[@]}"; do
        local local_path="${IMPORT_DIR}/${filename}"
        local remote_path="${REMOTE_OUTBOUND_DIR:-/data/outbound/carddemo}/${filename}"

        log_info "Receiving: $remote_path -> $local_path"

        if [[ "$DRY_RUN" == true ]]; then
            log_info "[DRY RUN] Would receive $filename"
            continue
        fi

        "$SFTP_SCRIPT" \
            --mode RECEIVE \
            --local-file "$local_path" \
            --remote-file "$remote_path" \
            --checksum \
            --log-file "${LOG_DIR}/sftp_recv_${JOB_ID}.log" \
            2>&1 | tee -a "$JOB_LOG"

        local rc=${PIPESTATUS[0]}
        if [[ $rc -ne 0 ]]; then
            log_error "Failed to receive $filename (RC=$rc)"
            recv_rc=$rc
        else
            log_info "Successfully received $filename"
            log_info "File size: $(stat -c%s "$local_path" 2>/dev/null || echo 'N/A') bytes"
        fi
    done

    log_step_footer $recv_rc
}

# ------------------------------------------------------------------
# STEP: Import Received Data (replaces CBIMPORT JCL step)
# ------------------------------------------------------------------
step_import_data() {
    log_step_header "IMPORT - Run CBIMPORT to load received data"

    if [[ "$DRY_RUN" == true ]]; then
        log_info "[DRY RUN] Would execute: ${COBOL_BIN}/CBIMPORT"
        log_step_footer 0
        return
    fi

    if [[ ! -x "${COBOL_BIN}/CBIMPORT" ]]; then
        log_warn "CBIMPORT not found at ${COBOL_BIN}/CBIMPORT"
        log_warn "Skipping import step"
        log_step_footer 0
        return
    fi

    # Set up file assignments for Micro Focus COBOL
    export IMPFILE="${IMPORT_DIR}/DALYTRAN.dat"
    export TRANSACT="${DATA_DIR}/TRANSACT.dat"

    "${COBOL_BIN}/CBIMPORT" 2>&1 | tee -a "$JOB_LOG"
    local rc=${PIPESTATUS[0]}

    if [[ $rc -eq 0 ]]; then
        log_info "Import completed successfully"
    else
        log_error "Import failed with RC=$rc"
    fi

    log_step_footer $rc
}

# ------------------------------------------------------------------
# Job Summary (replaces JCL JESMSGLG)
# ------------------------------------------------------------------
print_job_summary() {
    cat <<EOF | tee -a "$JOB_LOG"

*******************************************************************
*  BATCH TRANSFER JOB SUMMARY
*******************************************************************
*  Job ID    : $JOB_ID
*  Started   : $(head -1 "$JOB_LOG" 2>/dev/null | grep -oP '\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}' || echo 'N/A')
*  Completed : $(date '+%Y-%m-%d %H:%M:%S')
*  Steps Run : $STEP_NUM
*  Max RC    : $MAX_RC
*  Status    : $(if [[ $MAX_RC -eq 0 ]]; then echo "SUCCESS"; elif [[ $MAX_RC -le 4 ]]; then echo "WARNING"; else echo "FAILED"; fi)
*  Job Log   : $JOB_LOG
*******************************************************************

EOF
}

# ==================================================================
# MAIN
# ==================================================================
main() {
    parse_args "$@"
    initialize

    # Execute steps based on flags (similar to JCL COND parameter)

    if [[ "$RUN_EXPORT" == true ]]; then
        step_export_data
        if [[ $MAX_RC -gt 4 ]]; then
            log_error "Export failed. Skipping subsequent steps."
            print_job_summary
            exit $MAX_RC
        fi
    fi

    if [[ "$RUN_TRANSFER" == true ]]; then
        step_send_files
        step_receive_files
        if [[ $MAX_RC -gt 4 ]]; then
            log_error "Transfer failed. Skipping import."
            print_job_summary
            exit $MAX_RC
        fi
    fi

    if [[ "$RUN_IMPORT" == true ]]; then
        step_import_data
    fi

    print_job_summary

    exit $MAX_RC
}

main "$@"
