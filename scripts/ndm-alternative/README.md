# NDM (Connect:Direct) Alternative for Micro Focus COBOL

## Overview

NDM (Network Data Mover), also known as **Connect:Direct**, is a mainframe-native file transfer solution that does not run natively in Micro Focus COBOL distributed environments (Linux/Windows). This module provides a production-ready **SFTP-based alternative** that replaces NDM file transfer functionality while maintaining the same batch workflow patterns.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    MAINFRAME (Original)                         │
│                                                                 │
│  JCL Job ──► NDM (Connect:Direct) ──► Remote System            │
│    │              │                                             │
│    │         PNODE/SNODE                                        │
│    │         Process Defs                                       │
│    ▼                                                            │
│  COBOL Programs (file I/O only)                                 │
└─────────────────────────────────────────────────────────────────┘
                          │
                    Migration Path
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│               MICRO FOCUS COBOL (Distributed)                   │
│                                                                 │
│  run_batch_transfer.sh ──► sftp_transfer.sh ──► Remote System   │
│    (JCL replacement)         (NDM replacement)     (SFTP)       │
│    │                              │                             │
│    │                         SSH Key Auth                        │
│    │                         Retry Logic                         │
│    │                         Checksum Verify                     │
│    ▼                                                            │
│  COBOL Programs (file I/O unchanged)                            │
│    │                                                            │
│    ├── CBEXPORT  (existing - exports data to flat files)        │
│    ├── CBIMPORT  (existing - imports data from flat files)      │
│    └── CBSFTP01  (new - invokes SFTP from COBOL via SYSTEM)     │
└─────────────────────────────────────────────────────────────────┘
```

## Components

### 1. `sftp_transfer.sh` — SFTP Transfer Wrapper (NDM Replacement)

A standalone shell script that replaces NDM Connect:Direct for file transfers.

| NDM Concept | SFTP Equivalent |
|---|---|
| NDM SEND / COPY FROM | `sftp_transfer.sh --mode SEND` |
| NDM RECEIVE / COPY TO | `sftp_transfer.sh --mode RECEIVE` |
| PNODE (Primary Node) | `--host` / `SFTP_HOST` env var |
| SNODE (Secondary Node) | Local machine (the script host) |
| SNODEID (Authentication) | `--key` (SSH key) or `--password-file` |
| NDM Process retry | `--retry` / `--retry-delay` |
| NDM Statistics | `--log-file` + transfer summary |
| NDM Checkpointing | `--checksum` (SHA-256 integrity check) |

**Usage:**
```bash
# Send a file (like NDM SEND)
./sftp_transfer.sh \
    --mode SEND \
    --host sftp.example.com \
    --user batch_user \
    --key ~/.ssh/sftp_key \
    --local-file /data/export/CUSTDATA.dat \
    --remote-file /inbound/CUSTDATA.dat \
    --checksum --retry 3

# Receive a file (like NDM RECEIVE)
./sftp_transfer.sh \
    --mode RECEIVE \
    --host sftp.example.com \
    --user batch_user \
    --key ~/.ssh/sftp_key \
    --remote-file /outbound/DALYTRAN.dat \
    --local-file /data/import/DALYTRAN.dat
```

### 2. `sftp_transfer.conf.template` — Configuration Template

Replaces NDM node and process definitions. Copy to `sftp_transfer.conf` and update for your environment.

### 3. `run_batch_transfer.sh` — Batch Orchestration Script (JCL Replacement)

Replaces the mainframe JCL that orchestrated NDM file transfers. Runs the full workflow:

1. **Export** — Execute CBEXPORT COBOL program to extract data
2. **Send** — Transfer exported files via SFTP (replaces NDM SEND)
3. **Receive** — Retrieve inbound files via SFTP (replaces NDM RECEIVE)
4. **Import** — Execute CBIMPORT COBOL program to load received data

**Usage:**
```bash
# Run full batch workflow
./run_batch_transfer.sh --config sftp_transfer.conf

# Export only (skip transfers)
./run_batch_transfer.sh --export-only

# Dry run (show what would happen)
./run_batch_transfer.sh --dry-run
```

### 4. `CBSFTP01.cbl` — COBOL Program with SFTP Integration

Demonstrates how to invoke SFTP transfers directly from a Micro Focus COBOL program using the `CALL 'SYSTEM'` interface. This is useful when:

- Transfer logic must be embedded in the COBOL program
- Multiple transfers need to be coordinated with COBOL business logic
- Transfer results must be checked before proceeding with processing

The program supports batching multiple transfers (SEND/RECEIVE) in a single run, similar to NDM multi-step processes.

## Migration Guide: NDM to SFTP

### Step 1: Identify NDM Usage

Search your JCL for NDM/Connect:Direct references:
```
EXEC PGM=DMBATCH       (Connect:Direct batch interface)
EXEC PGM=DMCHLST       (Connect:Direct change list)
SUBMIT PROCESS=         (NDM process submission)
```

### Step 2: Map NDM Processes to SFTP

For each NDM Process, create a corresponding SFTP transfer call:

**Original NDM JCL:**
```jcl
//STEP01   EXEC PGM=DMBATCH
//SYSIN    DD *
  SUBMIT MAXDELAY=UNLIMITED PROCESS=CARDDEMO-SEND -
    SNODE=REMOTE.SERVER -
    SNODEID=(BATCHUSR,PASSWORD) -
    &LOCAL.DSN='AWS.M2.CARDDEMO.EXPORT.DATA' -
    &REMOTE.DSN='/data/inbound/EXPORT.dat'
/*
```

**SFTP Replacement:**
```bash
./sftp_transfer.sh \
    --mode SEND \
    --host remote.server.com \
    --user batchusr \
    --key /opt/carddemo/keys/sftp_key \
    --local-file /opt/carddemo/data/export/EXPORT.dat \
    --remote-file /data/inbound/EXPORT.dat \
    --checksum --retry 3
```

### Step 3: Choose Integration Approach

| Approach | When to Use | Files |
|---|---|---|
| **Script-driven** (Recommended) | COBOL programs only do file I/O; transfer is orchestrated externally | `run_batch_transfer.sh` + `sftp_transfer.sh` |
| **COBOL-driven** | Transfer logic must be inside COBOL program | `CBSFTP01.cbl` calls `sftp_transfer.sh` |

**Recommendation:** Use the script-driven approach. Keep COBOL programs focused on data processing (READ/WRITE to local files) and let the shell orchestration handle file transfers. This is cleaner, easier to maintain, and allows changing the transfer method without modifying COBOL code.

### Step 4: Set Up Authentication

Replace NDM SNODEID credentials with SSH key-based authentication:

```bash
# Generate SSH key pair for batch transfers
ssh-keygen -t rsa -b 4096 -f /opt/carddemo/keys/sftp_batch_key -N ""

# Copy public key to SFTP server
ssh-copy-id -i /opt/carddemo/keys/sftp_batch_key.pub batch_user@sftp.example.com
```

### Step 5: Schedule with cron (Replaces Mainframe Scheduler)

```cron
# Daily batch transfer at 2:00 AM (replaces mainframe scheduler job)
0 2 * * * /opt/carddemo/scripts/ndm-alternative/run_batch_transfer.sh --config /opt/carddemo/conf/sftp_transfer.conf >> /var/log/carddemo/cron.log 2>&1
```

## Other Alternatives to NDM

While SFTP is the most common and recommended replacement, other options include:

| Alternative | Best For | Notes |
|---|---|---|
| **SFTP** (this module) | Most use cases | Simple, secure, widely supported |
| **Connect:Direct for Unix/Linux** | Maintaining NDM protocol compatibility | Licensed product from IBM/Sterling |
| **IBM MQ MFT** | Organizations already using IBM MQ | Reliable, auditable, enterprise-grade |
| **GoAnywhere MFT** | Enterprise managed file transfer | GUI-based, scheduling, compliance |
| **AWS Transfer Family** | Cloud-native deployments | Managed SFTP/FTPS/FTP service |
| **rsync over SSH** | Large files with incremental updates | Efficient for partial transfers |

## Directory Structure

```
scripts/ndm-alternative/
├── README.md                       # This file
├── sftp_transfer.sh                # SFTP transfer wrapper (NDM replacement)
├── sftp_transfer.conf.template     # Configuration template
└── run_batch_transfer.sh           # Batch orchestration (JCL replacement)

app/cbl/
└── CBSFTP01.cbl                    # COBOL program with SFTP integration
```
