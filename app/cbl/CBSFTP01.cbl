       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CBSFTP01.
       AUTHOR.        CARDDEMO TEAM.
      ******************************************************************
      * Program     : CBSFTP01.CBL
      * Application : CardDemo
      * Type        : BATCH COBOL Program (Micro Focus Compatible)
      * Function    : File Transfer via SFTP - NDM Replacement
      *
      * Description : Demonstrates how to replace NDM (Connect:Direct)
      *               file transfers in a Micro Focus COBOL environment
      *               by invoking an external SFTP shell script through
      *               the SYSTEM interface.
      *
      *               The COBOL program handles:
      *               - Building the transfer command dynamically
      *               - Invoking the SFTP wrapper script
      *               - Checking return codes
      *               - Logging transfer results
      *
      * NDM Mapping :
      *   NDM SEND  -> SFTP PUT  (mode=SEND)
      *   NDM RECV  -> SFTP GET  (mode=RECEIVE)
      *   NDM PNODE -> SFTP_HOST / SFTP_USER env vars
      *   NDM SNODE -> local machine (this program)
      *
      ******************************************************************
      * Copyright Amazon.com, Inc. or its affiliates.
      * All Rights Reserved.
      *
      * Licensed under the Apache License, Version 2.0 (the "License").
      * You may not use this file except in compliance with the License.
      * You may obtain a copy of the License at
      *
      *    http://www.apache.org/licenses/LICENSE-2.0
      *
      * Unless required by applicable law or agreed to in writing,
      * software distributed under the License is distributed on an
      * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
      * either express or implied. See the License for the specific
      * language governing permissions and limitations under the License
      ******************************************************************
       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       REPOSITORY.
           FUNCTION ALL INTRINSIC.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TRANSFER-LOG ASSIGN TO XFERLOG
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-LOG-STATUS.

       DATA DIVISION.
       FILE SECTION.

       FD  TRANSFER-LOG.
       01  LOG-RECORD                            PIC X(256).

       WORKING-STORAGE SECTION.

      * ---------------------------------------------------------
      * Transfer Configuration (replaces NDM Process parameters)
      * ---------------------------------------------------------
       01  WS-TRANSFER-CONFIG.
           05  WS-SFTP-SCRIPT-PATH.
               10  FILLER       PIC X(80) VALUE
                   '/opt/carddemo/scripts/sftp_transfer.sh'.
           05  WS-SFTP-HOST                      PIC X(64).
           05  WS-SFTP-PORT                      PIC X(05) VALUE
               '22'.
           05  WS-SFTP-USER                      PIC X(32).
           05  WS-SFTP-KEY-PATH                  PIC X(128).
           05  WS-TRANSFER-MODE                  PIC X(07).
               88  WS-MODE-SEND                  VALUE 'SEND'.
               88  WS-MODE-RECEIVE               VALUE 'RECEIVE'.
           05  WS-LOCAL-FILE-PATH                PIC X(256).
           05  WS-REMOTE-FILE-PATH               PIC X(256).
           05  WS-VERIFY-CHECKSUM                PIC X(01).
               88  WS-CHECKSUM-YES               VALUE 'Y'.
               88  WS-CHECKSUM-NO                VALUE 'N'.

      * ---------------------------------------------------------
      * Command Buffer for SYSTEM call
      * ---------------------------------------------------------
       01  WS-COMMAND-BUFFER                     PIC X(1024).
       01  WS-COMMAND-LENGTH                     PIC 9(04) VALUE 0.

      * ---------------------------------------------------------
      * Return Codes and Status
      * ---------------------------------------------------------
       01  WS-RETURN-CODES.
           05  WS-SYSTEM-RC                      PIC S9(04) VALUE 0.
               88  WS-TRANSFER-SUCCESS           VALUE 0.
               88  WS-TRANSFER-BAD-ARGS          VALUE 1.
               88  WS-TRANSFER-CONN-FAIL         VALUE 2.
               88  WS-TRANSFER-XFER-FAIL         VALUE 3.
               88  WS-TRANSFER-CHECKSUM-FAIL     VALUE 4.
               88  WS-TRANSFER-CONFIG-ERR        VALUE 5.
           05  WS-PROGRAM-RC                     PIC S9(04) VALUE 0.
           05  WS-LOG-STATUS                     PIC X(02).
               88  WS-LOG-OK                     VALUE '00'.

      * ---------------------------------------------------------
      * Timestamp / Logging
      * ---------------------------------------------------------
       01  WS-TIMESTAMP-FIELDS.
           05  WS-CURRENT-DATE.
               10  WS-CURR-YEAR                  PIC 9(04).
               10  WS-CURR-MONTH                 PIC 9(02).
               10  WS-CURR-DAY                   PIC 9(02).
           05  WS-CURRENT-TIME.
               10  WS-CURR-HOUR                  PIC 9(02).
               10  WS-CURR-MINUTE                PIC 9(02).
               10  WS-CURR-SECOND                PIC 9(02).
               10  WS-CURR-HUNDREDTH             PIC 9(02).

       01  WS-FORMATTED-TIMESTAMP               PIC X(19).
       01  WS-LOG-MESSAGE                        PIC X(256).

      * ---------------------------------------------------------
      * Transfer Request Table
      * Allows batching multiple transfers in one program run,
      * similar to NDM multi-step Processes.
      * ---------------------------------------------------------
       01  WS-TRANSFER-TABLE.
           05  WS-MAX-TRANSFERS                  PIC 9(02) VALUE 10.
           05  WS-TRANSFER-COUNT                 PIC 9(02) VALUE 0.
           05  WS-TRANSFER-ENTRY OCCURS 10 TIMES.
               10  WS-XFER-MODE                  PIC X(07).
               10  WS-XFER-LOCAL-FILE            PIC X(256).
               10  WS-XFER-REMOTE-FILE           PIC X(256).
               10  WS-XFER-STATUS                PIC X(01).
                   88  WS-XFER-PENDING           VALUE 'P'.
                   88  WS-XFER-OK                VALUE 'S'.
                   88  WS-XFER-FAILED            VALUE 'F'.
               10  WS-XFER-RC                    PIC S9(04).

      * Working index
       01  WS-IDX                                PIC 9(02) VALUE 0.
       01  WS-TRANSFERS-OK                       PIC 9(02) VALUE 0.
       01  WS-TRANSFERS-FAILED                   PIC 9(02) VALUE 0.

       PROCEDURE DIVISION.

      *****************************************************************
       0000-MAIN-PROCESSING.
      *****************************************************************
           PERFORM 1000-INITIALIZE
           PERFORM 2000-SETUP-TRANSFERS
           PERFORM 3000-EXECUTE-TRANSFERS
           PERFORM 4000-REPORT-RESULTS
           PERFORM 9000-FINALIZE
           GOBACK.

      *****************************************************************
       1000-INITIALIZE.
      *****************************************************************
           DISPLAY 'CBSFTP01: NDM Alternative - SFTP Transfer Program'
           DISPLAY 'CBSFTP01: Starting file transfer processing'

           PERFORM 1100-GET-TIMESTAMP
           PERFORM 1200-OPEN-LOG

           PERFORM 1300-LOG-START.

      *****************************************************************
       1100-GET-TIMESTAMP.
      *****************************************************************
           ACCEPT WS-CURRENT-DATE FROM DATE YYYYMMDD
           ACCEPT WS-CURRENT-TIME FROM TIME

           STRING WS-CURR-YEAR '-' WS-CURR-MONTH '-' WS-CURR-DAY
                  ' '
                  WS-CURR-HOUR ':' WS-CURR-MINUTE ':' WS-CURR-SECOND
               DELIMITED BY SIZE
               INTO WS-FORMATTED-TIMESTAMP
           END-STRING.

      *****************************************************************
       1200-OPEN-LOG.
      *****************************************************************
           OPEN EXTEND TRANSFER-LOG
           IF NOT WS-LOG-OK
               OPEN OUTPUT TRANSFER-LOG
               IF NOT WS-LOG-OK
                   DISPLAY 'CBSFTP01: WARNING - Cannot open log file'
                   DISPLAY 'CBSFTP01: Log status: ' WS-LOG-STATUS
               END-IF
           END-IF.

      *****************************************************************
       1300-LOG-START.
      *****************************************************************
           MOVE SPACES TO WS-LOG-MESSAGE
           STRING '=========================================='
               DELIMITED BY SIZE INTO WS-LOG-MESSAGE
           END-STRING
           PERFORM 8000-WRITE-LOG

           MOVE SPACES TO WS-LOG-MESSAGE
           STRING WS-FORMATTED-TIMESTAMP
                  ' CBSFTP01: Transfer session started'
               DELIMITED BY SIZE INTO WS-LOG-MESSAGE
           END-STRING
           PERFORM 8000-WRITE-LOG.

      *****************************************************************
       2000-SETUP-TRANSFERS.
      *****************************************************************
      *    Load connection configuration from environment variables.
      *    In production, these would be set by the orchestration
      *    script (run_batch_transfer.sh) or sourced from config.
      *
      *    This replaces NDM PNODE/SNODE configuration.
      * ---------------------------------------------------------
           ACCEPT WS-SFTP-HOST FROM ENVIRONMENT 'SFTP_HOST'
           ACCEPT WS-SFTP-PORT FROM ENVIRONMENT 'SFTP_PORT'
           ACCEPT WS-SFTP-USER FROM ENVIRONMENT 'SFTP_USER'
           ACCEPT WS-SFTP-KEY-PATH FROM ENVIRONMENT 'SFTP_KEY'

           IF WS-SFTP-HOST = SPACES
               DISPLAY 'CBSFTP01: ERROR - SFTP_HOST not set'
               MOVE 5 TO WS-PROGRAM-RC
               PERFORM 9000-FINALIZE
               GOBACK
           END-IF

           IF WS-SFTP-USER = SPACES
               DISPLAY 'CBSFTP01: ERROR - SFTP_USER not set'
               MOVE 5 TO WS-PROGRAM-RC
               PERFORM 9000-FINALIZE
               GOBACK
           END-IF

           DISPLAY 'CBSFTP01: SFTP Host: ' TRIM(WS-SFTP-HOST)
           DISPLAY 'CBSFTP01: SFTP User: ' TRIM(WS-SFTP-USER)

      *    Set up the transfer requests.
      *    In a real scenario these could come from a control file,
      *    database table, or be hard-coded for a specific batch job.
      * ---------------------------------------------------------

      *    Transfer 1: SEND exported customer data
           ADD 1 TO WS-TRANSFER-COUNT
           MOVE 'SEND' TO WS-XFER-MODE(WS-TRANSFER-COUNT)
           MOVE '/opt/carddemo/data/export/CUSTDATA.dat'
               TO WS-XFER-LOCAL-FILE(WS-TRANSFER-COUNT)
           MOVE '/data/inbound/carddemo/CUSTDATA.dat'
               TO WS-XFER-REMOTE-FILE(WS-TRANSFER-COUNT)
           SET WS-XFER-PENDING(WS-TRANSFER-COUNT) TO TRUE
           MOVE 0 TO WS-XFER-RC(WS-TRANSFER-COUNT)

      *    Transfer 2: SEND exported account data
           ADD 1 TO WS-TRANSFER-COUNT
           MOVE 'SEND' TO WS-XFER-MODE(WS-TRANSFER-COUNT)
           MOVE '/opt/carddemo/data/export/ACCTDATA.dat'
               TO WS-XFER-LOCAL-FILE(WS-TRANSFER-COUNT)
           MOVE '/data/inbound/carddemo/ACCTDATA.dat'
               TO WS-XFER-REMOTE-FILE(WS-TRANSFER-COUNT)
           SET WS-XFER-PENDING(WS-TRANSFER-COUNT) TO TRUE
           MOVE 0 TO WS-XFER-RC(WS-TRANSFER-COUNT)

      *    Transfer 3: RECEIVE daily transaction feed
           ADD 1 TO WS-TRANSFER-COUNT
           MOVE 'RECEIVE' TO WS-XFER-MODE(WS-TRANSFER-COUNT)
           MOVE '/opt/carddemo/data/import/DALYTRAN.dat'
               TO WS-XFER-LOCAL-FILE(WS-TRANSFER-COUNT)
           MOVE '/data/outbound/carddemo/DALYTRAN.dat'
               TO WS-XFER-REMOTE-FILE(WS-TRANSFER-COUNT)
           SET WS-XFER-PENDING(WS-TRANSFER-COUNT) TO TRUE
           MOVE 0 TO WS-XFER-RC(WS-TRANSFER-COUNT)

           DISPLAY 'CBSFTP01: Configured '
                   WS-TRANSFER-COUNT ' transfer(s)'.

      *****************************************************************
       3000-EXECUTE-TRANSFERS.
      *****************************************************************
           DISPLAY 'CBSFTP01: Executing transfers...'

           PERFORM VARYING WS-IDX FROM 1 BY 1
               UNTIL WS-IDX > WS-TRANSFER-COUNT
               PERFORM 3100-EXECUTE-SINGLE-TRANSFER
           END-PERFORM.

      *****************************************************************
       3100-EXECUTE-SINGLE-TRANSFER.
      *****************************************************************
           DISPLAY 'CBSFTP01: Transfer ' WS-IDX ' of '
                   WS-TRANSFER-COUNT
           DISPLAY 'CBSFTP01:   Mode  : '
                   TRIM(WS-XFER-MODE(WS-IDX))
           DISPLAY 'CBSFTP01:   Local : '
                   TRIM(WS-XFER-LOCAL-FILE(WS-IDX))
           DISPLAY 'CBSFTP01:   Remote: '
                   TRIM(WS-XFER-REMOTE-FILE(WS-IDX))

      *    Build the SFTP command
           PERFORM 3200-BUILD-COMMAND

      *    Execute the command via SYSTEM call
           CALL 'SYSTEM' USING WS-COMMAND-BUFFER
               RETURNING WS-SYSTEM-RC
           END-CALL

           MOVE WS-SYSTEM-RC TO WS-XFER-RC(WS-IDX)

           IF WS-TRANSFER-SUCCESS
               SET WS-XFER-OK(WS-IDX) TO TRUE
               ADD 1 TO WS-TRANSFERS-OK
               DISPLAY 'CBSFTP01:   Result: SUCCESS'
           ELSE
               SET WS-XFER-FAILED(WS-IDX) TO TRUE
               ADD 1 TO WS-TRANSFERS-FAILED
               DISPLAY 'CBSFTP01:   Result: FAILED (RC='
                       WS-SYSTEM-RC ')'
               PERFORM 3300-LOG-FAILURE
           END-IF.

      *****************************************************************
       3200-BUILD-COMMAND.
      *****************************************************************
           MOVE SPACES TO WS-COMMAND-BUFFER

           STRING
               TRIM(WS-SFTP-SCRIPT-PATH)
               ' --mode '   TRIM(WS-XFER-MODE(WS-IDX))
               ' --host '   TRIM(WS-SFTP-HOST)
               ' --port '   TRIM(WS-SFTP-PORT)
               ' --user '   TRIM(WS-SFTP-USER)
               ' --key '    TRIM(WS-SFTP-KEY-PATH)
               ' --local-file '  TRIM(WS-XFER-LOCAL-FILE(WS-IDX))
               ' --remote-file ' TRIM(WS-XFER-REMOTE-FILE(WS-IDX))
               ' --checksum'
               X'00'
               DELIMITED BY SIZE
               INTO WS-COMMAND-BUFFER
           END-STRING.

      *****************************************************************
       3300-LOG-FAILURE.
      *****************************************************************
           MOVE SPACES TO WS-LOG-MESSAGE
           STRING WS-FORMATTED-TIMESTAMP
                  ' FAILED: Transfer ' WS-IDX
                  ' Mode=' TRIM(WS-XFER-MODE(WS-IDX))
                  ' RC=' WS-SYSTEM-RC
               DELIMITED BY SIZE INTO WS-LOG-MESSAGE
           END-STRING
           PERFORM 8000-WRITE-LOG.

      *****************************************************************
       4000-REPORT-RESULTS.
      *****************************************************************
           DISPLAY ' '
           DISPLAY '==========================================='
           DISPLAY ' CBSFTP01: TRANSFER SUMMARY'
           DISPLAY '==========================================='
           DISPLAY '  Total Transfers : ' WS-TRANSFER-COUNT
           DISPLAY '  Successful      : ' WS-TRANSFERS-OK
           DISPLAY '  Failed          : ' WS-TRANSFERS-FAILED
           DISPLAY '==========================================='

           PERFORM VARYING WS-IDX FROM 1 BY 1
               UNTIL WS-IDX > WS-TRANSFER-COUNT
               DISPLAY '  [' WS-IDX '] '
                       TRIM(WS-XFER-MODE(WS-IDX))
                       ' -> Status: ' WS-XFER-STATUS(WS-IDX)
                       ' RC: ' WS-XFER-RC(WS-IDX)
           END-PERFORM

           DISPLAY '==========================================='
           DISPLAY ' '

      *    Log summary
           MOVE SPACES TO WS-LOG-MESSAGE
           STRING WS-FORMATTED-TIMESTAMP
                  ' SUMMARY: Total=' WS-TRANSFER-COUNT
                  ' OK=' WS-TRANSFERS-OK
                  ' FAILED=' WS-TRANSFERS-FAILED
               DELIMITED BY SIZE INTO WS-LOG-MESSAGE
           END-STRING
           PERFORM 8000-WRITE-LOG.

      *****************************************************************
       8000-WRITE-LOG.
      *****************************************************************
           IF WS-LOG-OK
               MOVE WS-LOG-MESSAGE TO LOG-RECORD
               WRITE LOG-RECORD
           END-IF.

      *****************************************************************
       9000-FINALIZE.
      *****************************************************************
           IF WS-LOG-OK
               MOVE SPACES TO WS-LOG-MESSAGE
               STRING WS-FORMATTED-TIMESTAMP
                      ' CBSFTP01: Transfer session ended'
                   DELIMITED BY SIZE INTO WS-LOG-MESSAGE
               END-STRING
               PERFORM 8000-WRITE-LOG
               CLOSE TRANSFER-LOG
           END-IF

      *    Set program return code based on transfer results
           IF WS-TRANSFERS-FAILED > 0
               MOVE 8 TO WS-PROGRAM-RC
           ELSE
               MOVE 0 TO WS-PROGRAM-RC
           END-IF

           DISPLAY 'CBSFTP01: Program complete. RC='
                   WS-PROGRAM-RC
           MOVE WS-PROGRAM-RC TO RETURN-CODE.
