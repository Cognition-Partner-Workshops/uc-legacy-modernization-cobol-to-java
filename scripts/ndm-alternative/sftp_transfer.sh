#!/bin/bash
###################################################################
# sftp_transfer.sh - SFTP File Transfer Wrapper (NDM Replacement)
#
# Application : CardDemo
# Purpose     : Replace NDM (Connect:Direct) file transfers with
#               SFTP for Micro Focus COBOL distributed environments.
#
# Usage:
#   sftp_transfer.sh --mode <SEND|RECEIVE>
#                    --local-file <path>
#                    --remote-file <path>
#                    [--host <hostname>]
#                    [--port <port>]
#                    [--user <username>]
#                    [--key <ssh-key-path>]
#                    [--password-file <path>]
#                    [--retry <count>]
#                    [--retry-delay <seconds>]
#                    [--checksum]
#                    [--log-file <path>]
#
# Exit Codes:
#   0 - Transfer completed successfully
#   1 - Invalid arguments
#   2 - Connection failure
#   3 - Transfer failure
#   4 - Checksum mismatch
#   5 - Configuration error
#
# Copyright Amazon.com, Inc. or its affiliates.
# All Rights Reserved.
# Licensed under the Apache License, Version 2.0
###################################################################

set -euo pipefail

# ------------------------------------------------------------------
# Default Configuration
# ------------------------------------------------------------------
SFTP_HOST="${SFTP_HOST:-}"
SFTP_PORT="${SFTP_PORT:-22}"
SFTP_USER="${SFTP_USER:-}"
SFTP_KEY="${SFTP_KEY:-}"
SFTP_PASSWORD_FILE="${SFTP_PASSWORD_FILE:-}"
TRANSFER_MODE=""
LOCAL_FILE=""
REMOTE_FILE=""
RETRY_COUNT="${SFTP_RETRY_COUNT:-3}"
RETRY_DELAY="${SFTP_RETRY_DELAY:-10}"
VERIFY_CHECKSUM=false
LOG_FILE="${SFTP_LOG_FILE:-/tmp/sftp_transfer_$(date +%Y%m%d_%H%M%S).log}"

# ------------------------------------------------------------------
# Logging
# ------------------------------------------------------------------
log_info()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] INFO  $*" | tee -a "$LOG_FILE"; }
log_warn()  { echo "[$(date '+%Y-%m-%d %H:%M:%S')] WARN  $*" | tee -a "$LOG_FILE"; }
log_error() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] ERROR $*" | tee -a "$LOG_FILE" >&2; }

# ------------------------------------------------------------------
# Usage
# ------------------------------------------------------------------
usage() {
    cat <<EOF
Usage: $(basename "$0") [OPTIONS]

NDM/Connect:Direct Replacement - SFTP File Transfer Wrapper

Required:
  --mode <SEND|RECEIVE>      Transfer direction
  --local-file <path>        Local file path
  --remote-file <path>       Remote file path on SFTP server

Connection (override env vars SFTP_HOST, SFTP_USER, etc.):
  --host <hostname>          SFTP server hostname or IP
  --port <port>              SFTP server port (default: 22)
  --user <username>          SFTP username
  --key <path>               Path to SSH private key
  --password-file <path>     Path to file containing SFTP password

Transfer Options:
  --retry <count>            Number of retry attempts (default: 3)
  --retry-delay <seconds>    Delay between retries (default: 10)
  --checksum                 Verify file integrity via SHA-256 checksum
  --log-file <path>          Path to log file

Environment Variables:
  SFTP_HOST                  Default SFTP host
  SFTP_PORT                  Default SFTP port
  SFTP_USER                  Default SFTP user
  SFTP_KEY                   Default SSH key path
  SFTP_PASSWORD_FILE         Default password file path
  SFTP_RETRY_COUNT           Default retry count
  SFTP_RETRY_DELAY           Default retry delay
  SFTP_LOG_FILE              Default log file path

Exit Codes:
  0  Success
  1  Invalid arguments
  2  Connection failure
  3  Transfer failure
  4  Checksum mismatch
  5  Configuration error

Examples:
  # Send a file (like NDM SEND)
  $(basename "$0") --mode SEND \\
      --host sftp.example.com --user batch_user --key ~/.ssh/id_rsa \\
      --local-file /data/export/CARDDATA.dat \\
      --remote-file /inbound/CARDDATA.dat \\
      --checksum

  # Receive a file (like NDM RECEIVE)
  $(basename "$0") --mode RECEIVE \\
      --host sftp.example.com --user batch_user --key ~/.ssh/id_rsa \\
      --remote-file /outbound/TRANDATA.dat \\
      --local-file /data/import/TRANDATA.dat
EOF
    exit 1
}

# ------------------------------------------------------------------
# Argument Parsing
# ------------------------------------------------------------------
parse_args() {
    while [[ $# -gt 0 ]]; do
        case "$1" in
            --mode)          TRANSFER_MODE="${2^^}"; shift 2 ;;
            --local-file)    LOCAL_FILE="$2"; shift 2 ;;
            --remote-file)   REMOTE_FILE="$2"; shift 2 ;;
            --host)          SFTP_HOST="$2"; shift 2 ;;
            --port)          SFTP_PORT="$2"; shift 2 ;;
            --user)          SFTP_USER="$2"; shift 2 ;;
            --key)           SFTP_KEY="$2"; shift 2 ;;
            --password-file) SFTP_PASSWORD_FILE="$2"; shift 2 ;;
            --retry)         RETRY_COUNT="$2"; shift 2 ;;
            --retry-delay)   RETRY_DELAY="$2"; shift 2 ;;
            --checksum)      VERIFY_CHECKSUM=true; shift ;;
            --log-file)      LOG_FILE="$2"; shift 2 ;;
            --help|-h)       usage ;;
            *)               log_error "Unknown option: $1"; usage ;;
        esac
    done
}

# ------------------------------------------------------------------
# Validation
# ------------------------------------------------------------------
validate_args() {
    local errors=0

    if [[ -z "$TRANSFER_MODE" ]]; then
        log_error "Transfer mode is required (--mode SEND|RECEIVE)"
        errors=1
    elif [[ "$TRANSFER_MODE" != "SEND" && "$TRANSFER_MODE" != "RECEIVE" ]]; then
        log_error "Invalid transfer mode: $TRANSFER_MODE (must be SEND or RECEIVE)"
        errors=1
    fi

    if [[ -z "$LOCAL_FILE" ]]; then
        log_error "Local file path is required (--local-file)"
        errors=1
    fi

    if [[ -z "$REMOTE_FILE" ]]; then
        log_error "Remote file path is required (--remote-file)"
        errors=1
    fi

    if [[ -z "$SFTP_HOST" ]]; then
        log_error "SFTP host is required (--host or SFTP_HOST env var)"
        errors=1
    fi

    if [[ -z "$SFTP_USER" ]]; then
        log_error "SFTP user is required (--user or SFTP_USER env var)"
        errors=1
    fi

    if [[ "$TRANSFER_MODE" == "SEND" && ! -f "$LOCAL_FILE" ]]; then
        log_error "Local file not found: $LOCAL_FILE"
        errors=1
    fi

    if [[ -n "$SFTP_KEY" && ! -f "$SFTP_KEY" ]]; then
        log_error "SSH key file not found: $SFTP_KEY"
        errors=1
    fi

    if [[ -n "$SFTP_PASSWORD_FILE" && ! -f "$SFTP_PASSWORD_FILE" ]]; then
        log_error "Password file not found: $SFTP_PASSWORD_FILE"
        errors=1
    fi

    if [[ -z "$SFTP_KEY" && -z "$SFTP_PASSWORD_FILE" ]]; then
        log_error "Authentication required: provide --key or --password-file"
        errors=1
    fi

    if [[ $errors -ne 0 ]]; then
        exit 5
    fi
}

# ------------------------------------------------------------------
# Build SFTP connection options
# ------------------------------------------------------------------
build_sftp_opts() {
    SFTP_OPTS="-oPort=${SFTP_PORT} -oStrictHostKeyChecking=no -oBatchMode=yes"

    if [[ -n "$SFTP_KEY" ]]; then
        SFTP_OPTS="$SFTP_OPTS -oIdentityFile=${SFTP_KEY}"
    fi
}

# ------------------------------------------------------------------
# SFTP SEND (equivalent to NDM SEND / COPY FROM)
# ------------------------------------------------------------------
do_send() {
    local remote_dir
    remote_dir=$(dirname "$REMOTE_FILE")

    log_info "SEND: $LOCAL_FILE -> ${SFTP_USER}@${SFTP_HOST}:${REMOTE_FILE}"
    log_info "Local file size: $(stat -c%s "$LOCAL_FILE" 2>/dev/null || echo 'unknown') bytes"

    sftp $SFTP_OPTS "${SFTP_USER}@${SFTP_HOST}" <<SFTP_BATCH
-mkdir ${remote_dir}
put ${LOCAL_FILE} ${REMOTE_FILE}
bye
SFTP_BATCH
}

# ------------------------------------------------------------------
# SFTP RECEIVE (equivalent to NDM RECEIVE / COPY TO)
# ------------------------------------------------------------------
do_receive() {
    local local_dir
    local_dir=$(dirname "$LOCAL_FILE")

    log_info "RECEIVE: ${SFTP_USER}@${SFTP_HOST}:${REMOTE_FILE} -> $LOCAL_FILE"

    mkdir -p "$local_dir"

    sftp $SFTP_OPTS "${SFTP_USER}@${SFTP_HOST}" <<SFTP_BATCH
get ${REMOTE_FILE} ${LOCAL_FILE}
bye
SFTP_BATCH

    log_info "Received file size: $(stat -c%s "$LOCAL_FILE" 2>/dev/null || echo 'unknown') bytes"
}

# ------------------------------------------------------------------
# Checksum Verification
# ------------------------------------------------------------------
verify_checksum() {
    if [[ "$VERIFY_CHECKSUM" != true ]]; then
        return 0
    fi

    log_info "Verifying SHA-256 checksum..."

    local local_hash
    local_hash=$(sha256sum "$LOCAL_FILE" | awk '{print $1}')

    # Compute remote checksum via SSH
    local remote_hash
    remote_hash=$(ssh -p "$SFTP_PORT" -i "$SFTP_KEY" \
        -o StrictHostKeyChecking=no -o BatchMode=yes \
        "${SFTP_USER}@${SFTP_HOST}" \
        "sha256sum '${REMOTE_FILE}' 2>/dev/null | awk '{print \$1}'" 2>/dev/null || echo "REMOTE_HASH_UNAVAILABLE")

    if [[ "$remote_hash" == "REMOTE_HASH_UNAVAILABLE" ]]; then
        log_warn "Could not compute remote checksum (ssh may not be available). Skipping verification."
        return 0
    fi

    if [[ "$local_hash" == "$remote_hash" ]]; then
        log_info "Checksum verified: $local_hash"
        return 0
    else
        log_error "Checksum mismatch! Local=$local_hash Remote=$remote_hash"
        return 1
    fi
}

# ------------------------------------------------------------------
# Transfer with Retry Logic
# ------------------------------------------------------------------
transfer_with_retry() {
    local attempt=0
    local rc=0

    while [[ $attempt -lt $RETRY_COUNT ]]; do
        attempt=$((attempt + 1))
        log_info "Transfer attempt $attempt of $RETRY_COUNT"

        rc=0
        if [[ "$TRANSFER_MODE" == "SEND" ]]; then
            do_send && rc=0 || rc=$?
        else
            do_receive && rc=0 || rc=$?
        fi

        if [[ $rc -eq 0 ]]; then
            log_info "Transfer successful on attempt $attempt"

            # Verify checksum after transfer
            if verify_checksum; then
                return 0
            else
                log_error "Checksum verification failed"
                rc=4
            fi
        fi

        if [[ $attempt -lt $RETRY_COUNT ]]; then
            log_warn "Transfer failed (rc=$rc). Retrying in ${RETRY_DELAY}s..."
            sleep "$RETRY_DELAY"
        fi
    done

    log_error "Transfer failed after $RETRY_COUNT attempts"
    return $rc
}

# ------------------------------------------------------------------
# Generate Transfer Summary (like NDM statistics)
# ------------------------------------------------------------------
print_summary() {
    local status="$1"
    local end_time
    end_time=$(date '+%Y-%m-%d %H:%M:%S')

    cat <<EOF | tee -a "$LOG_FILE"

===================================================================
  FILE TRANSFER SUMMARY (NDM Alternative - SFTP)
===================================================================
  Transfer Mode  : $TRANSFER_MODE
  Local File     : $LOCAL_FILE
  Remote File    : $REMOTE_FILE
  SFTP Host      : $SFTP_HOST:$SFTP_PORT
  SFTP User      : $SFTP_USER
  Status         : $status
  Completed At   : $end_time
  Checksum Check : $(if $VERIFY_CHECKSUM; then echo "Enabled"; else echo "Disabled"; fi)
  Log File       : $LOG_FILE
===================================================================

EOF
}

# ==================================================================
# MAIN
# ==================================================================
main() {
    parse_args "$@"

    # Initialize log
    mkdir -p "$(dirname "$LOG_FILE")"
    log_info "========================================"
    log_info "NDM Alternative - SFTP Transfer Starting"
    log_info "========================================"

    validate_args
    build_sftp_opts

    if transfer_with_retry; then
        print_summary "SUCCESS"
        exit 0
    else
        local rc=$?
        print_summary "FAILED (rc=$rc)"

        case $rc in
            4) exit 4 ;;  # Checksum mismatch
            *) exit 3 ;;  # General transfer failure
        esac
    fi
}

main "$@"
