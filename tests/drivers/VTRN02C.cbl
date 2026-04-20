      ******************************************************************
      * Program     : VTRN02C.CBL
      * Application : CardDemo - Unit Test Verifier
      * Type        : Batch COBOL Program
      * Function    : Verification program for CBTRN02C transaction
      *               processing. Reads output files and validates
      *               results against expected values.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. VTRN02C.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TRAN-FILE ASSIGN TO TRANFILE
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS WS-TRAN-STATUS.

           SELECT REJSFILE ASSIGN TO DALYREJS
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS WS-REJS-STATUS.

           SELECT ACCT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-ACCT-ID
                  FILE STATUS  IS WS-ACCT-STATUS.

           SELECT TCATFILE ASSIGN TO TCATBALF
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS WS-TCAT-STATUS.

       DATA DIVISION.
       FILE SECTION.

       FD  TRAN-FILE.
       01  FD-TRAN-REC.
           05 FD-TRAN-ID                 PIC X(16).
           05 FD-TRAN-REST               PIC X(334).

       FD  REJSFILE.
       01  FD-REJS-REC.
           05 FD-REJS-TRAN-DATA         PIC X(350).
           05 FD-REJS-VALIDATION         PIC X(80).

       FD  ACCT-FILE.
       01  FD-ACCT-REC.
           05 FD-ACCT-ID                 PIC 9(11).
           05 FD-ACCT-REST               PIC X(289).

       FD  TCATFILE.
       01  FD-TCAT-REC.
           05 FD-TCAT-KEY.
              10 FD-TCAT-ACCT-ID         PIC 9(11).
              10 FD-TCAT-TYPE-CD         PIC X(02).
              10 FD-TCAT-CD              PIC 9(04).
           05 FD-TCAT-REST               PIC X(33).

       WORKING-STORAGE SECTION.

       01 WS-FILE-STATUSES.
         05 WS-TRAN-STATUS              PIC X(02).
         05 WS-REJS-STATUS              PIC X(02).
         05 WS-ACCT-STATUS              PIC X(02).
         05 WS-TCAT-STATUS              PIC X(02).

       01 WS-TEST-COUNTERS.
         05 WS-TOTAL-TESTS              PIC 9(03) VALUE 0.
         05 WS-PASS-COUNT               PIC 9(03) VALUE 0.
         05 WS-FAIL-COUNT               PIC 9(03) VALUE 0.

       01 WS-RECORD-COUNTERS.
         05 WS-TRAN-REC-COUNT           PIC 9(09) VALUE 0.
         05 WS-REJS-REC-COUNT           PIC 9(09) VALUE 0.
         05 WS-TCAT-REC-COUNT           PIC 9(09) VALUE 0.

       01 WS-EOF-FLAGS.
         05 WS-TRAN-EOF                 PIC X(01) VALUE 'N'.
         05 WS-REJS-EOF                 PIC X(01) VALUE 'N'.
         05 WS-TCAT-EOF                 PIC X(01) VALUE 'N'.

       COPY CVTRA05Y.

       COPY CVACT01Y.

       COPY CVTRA01Y.

       01 WS-REJS-RECORD.
         05 WS-REJS-TRAN-DATA           PIC X(350).
         05 WS-REJS-TRAILER.
           10 WS-REJS-REASON-CODE       PIC 9(04).
           10 WS-REJS-REASON-DESC       PIC X(76).

       01 WS-TEST-MODE                  PIC X(01) VALUE 'V'.
           88 WS-MODE-VALID             VALUE 'V'.
           88 WS-MODE-REJECT            VALUE 'R'.

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

           DISPLAY '=============================================='
           DISPLAY 'CBTRN02C TRANSACTION PROCESSING VERIFIER'
           DISPLAY '=============================================='
           DISPLAY SPACES

           ACCEPT WS-TEST-MODE FROM ENVIRONMENT 'TEST_MODE'

           IF WS-MODE-VALID
               DISPLAY 'Running VALID transaction tests...'
               PERFORM VERIFY-VALID-TRANSACTIONS
           ELSE
               DISPLAY 'Running REJECT transaction tests...'
               PERFORM VERIFY-REJECT-TRANSACTIONS
           END-IF

           DISPLAY SPACES
           DISPLAY '=============================================='
           DISPLAY 'VERIFICATION SUMMARY'
           DISPLAY '=============================================='
           DISPLAY 'TOTAL TESTS : ' WS-TOTAL-TESTS
           DISPLAY 'PASSED      : ' WS-PASS-COUNT
           DISPLAY 'FAILED      : ' WS-FAIL-COUNT
           DISPLAY '=============================================='

           IF WS-FAIL-COUNT > 0
               MOVE 4 TO RETURN-CODE
           ELSE
               MOVE 0 TO RETURN-CODE
           END-IF

           STOP RUN.

      *----------------------------------------------------------------*
      * VERIFY-VALID-TRANSACTIONS
      *----------------------------------------------------------------*
       VERIFY-VALID-TRANSACTIONS.

           PERFORM TC-TXN-01-COUNT-TRANS
           PERFORM TC-TXN-02-CHECK-ACCT-BAL
           PERFORM TC-TXN-05-CHECK-TCATBAL
           PERFORM TC-TXN-06-NO-REJECTS.

      *----------------------------------------------------------------*
      * TC-TXN-01: Transaction posted - count records
      *----------------------------------------------------------------*
       TC-TXN-01-COUNT-TRANS.
           ADD 1 TO WS-TOTAL-TESTS
           MOVE 0 TO WS-TRAN-REC-COUNT
           MOVE 'N' TO WS-TRAN-EOF

           OPEN INPUT TRAN-FILE
           IF WS-TRAN-STATUS NOT = '00'
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-01: Cannot open TRANFILE'
               DISPLAY '  File status: ' WS-TRAN-STATUS
               GO TO TC-TXN-01-EXIT
           END-IF

           PERFORM UNTIL WS-TRAN-EOF = 'Y'
               READ TRAN-FILE INTO TRAN-RECORD
               IF WS-TRAN-STATUS = '00'
                   ADD 1 TO WS-TRAN-REC-COUNT
               ELSE
                   MOVE 'Y' TO WS-TRAN-EOF
               END-IF
           END-PERFORM

           CLOSE TRAN-FILE

           IF WS-TRAN-REC-COUNT = 3
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-01: Transaction posted -'
                       ' 3 records found'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-01: Transaction posted'
               DISPLAY '  Expected 3 records'
               DISPLAY '  Got      ' WS-TRAN-REC-COUNT ' records'
           END-IF.
       TC-TXN-01-EXIT.
           EXIT.

      *----------------------------------------------------------------*
      * TC-TXN-02: Account balance updated
      *----------------------------------------------------------------*
       TC-TXN-02-CHECK-ACCT-BAL.
           ADD 1 TO WS-TOTAL-TESTS

           OPEN INPUT ACCT-FILE
           IF WS-ACCT-STATUS NOT = '00'
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-02: Cannot open ACCTFILE'
               DISPLAY '  File status: ' WS-ACCT-STATUS
               GO TO TC-TXN-02-EXIT
           END-IF

           MOVE 12345678901 TO FD-ACCT-ID
           READ ACCT-FILE INTO ACCOUNT-RECORD
           IF WS-ACCT-STATUS = '00'
               IF ACCT-CURR-BAL = 750.00
                   ADD 1 TO WS-PASS-COUNT
                   DISPLAY 'PASS TC-TXN-02: Account balance'
                           ' updated (expected 750.00)'
               ELSE
                   ADD 1 TO WS-FAIL-COUNT
                   DISPLAY 'FAIL TC-TXN-02: Account balance'
                   DISPLAY '  Expected balance: 750.00'
                   DISPLAY '  Got balance: ' ACCT-CURR-BAL
               END-IF
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-02: Account not found'
           END-IF

           CLOSE ACCT-FILE.
       TC-TXN-02-EXIT.
           EXIT.

      *----------------------------------------------------------------*
      * TC-TXN-05: Category balance created
      *----------------------------------------------------------------*
       TC-TXN-05-CHECK-TCATBAL.
           ADD 1 TO WS-TOTAL-TESTS
           MOVE 0 TO WS-TCAT-REC-COUNT
           MOVE 'N' TO WS-TCAT-EOF

           OPEN INPUT TCATFILE
           IF WS-TCAT-STATUS NOT = '00'
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-05: Cannot open TCATBALF'
               DISPLAY '  File status: ' WS-TCAT-STATUS
               GO TO TC-TXN-05-EXIT
           END-IF

           PERFORM UNTIL WS-TCAT-EOF = 'Y'
               READ TCATFILE INTO TRAN-CAT-BAL-RECORD
               IF WS-TCAT-STATUS = '00'
                   ADD 1 TO WS-TCAT-REC-COUNT
               ELSE
                   MOVE 'Y' TO WS-TCAT-EOF
               END-IF
           END-PERFORM

           CLOSE TCATFILE

           IF WS-TCAT-REC-COUNT > 0
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-05: Category balance'
                       ' created - ' WS-TCAT-REC-COUNT ' records'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-05: No category balance'
                       ' records created'
           END-IF.
       TC-TXN-05-EXIT.
           EXIT.

      *----------------------------------------------------------------*
      * TC-TXN-06: No rejects for valid transactions
      *----------------------------------------------------------------*
       TC-TXN-06-NO-REJECTS.
           ADD 1 TO WS-TOTAL-TESTS
           MOVE 0 TO WS-REJS-REC-COUNT
           MOVE 'N' TO WS-REJS-EOF

           OPEN INPUT REJSFILE
           IF WS-REJS-STATUS NOT = '00'
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-06: No rejects file'
                       ' (empty as expected)'
               GO TO TC-TXN-06-EXIT
           END-IF

           PERFORM UNTIL WS-REJS-EOF = 'Y'
               READ REJSFILE INTO WS-REJS-RECORD
               IF WS-REJS-STATUS = '00'
                   ADD 1 TO WS-REJS-REC-COUNT
               ELSE
                   MOVE 'Y' TO WS-REJS-EOF
               END-IF
           END-PERFORM

           CLOSE REJSFILE

           IF WS-REJS-REC-COUNT = 0
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-06: No rejects'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-06: Found '
                       WS-REJS-REC-COUNT ' reject records'
           END-IF.
       TC-TXN-06-EXIT.
           EXIT.

      *----------------------------------------------------------------*
      * VERIFY-REJECT-TRANSACTIONS
      *----------------------------------------------------------------*
       VERIFY-REJECT-TRANSACTIONS.

           PERFORM TC-TXN-08-INVALID-CARD
           PERFORM TC-TXN-13-NO-TRANS-POSTED.

      *----------------------------------------------------------------*
      * TC-TXN-08: Invalid card rejected + other reject codes
      *----------------------------------------------------------------*
       TC-TXN-08-INVALID-CARD.
           ADD 1 TO WS-TOTAL-TESTS
           MOVE 0 TO WS-REJS-REC-COUNT
           MOVE 'N' TO WS-REJS-EOF

           OPEN INPUT REJSFILE
           IF WS-REJS-STATUS NOT = '00'
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-08: Cannot open DALYREJS'
               DISPLAY '  File status: ' WS-REJS-STATUS
               GO TO TC-TXN-08-EXIT
           END-IF

           PERFORM UNTIL WS-REJS-EOF = 'Y'
               READ REJSFILE INTO WS-REJS-RECORD
               IF WS-REJS-STATUS = '00'
                   ADD 1 TO WS-REJS-REC-COUNT
                   DISPLAY 'REJECT #' WS-REJS-REC-COUNT
                           ' CODE=' WS-REJS-REASON-CODE
                           ' DESC=' WS-REJS-REASON-DESC
               ELSE
                   MOVE 'Y' TO WS-REJS-EOF
               END-IF
           END-PERFORM

           CLOSE REJSFILE

           IF WS-REJS-REC-COUNT = 4
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-08: All 4 reject records'
                       ' found'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-08: Expected 4 reject'
                       ' records'
               DISPLAY '  Got ' WS-REJS-REC-COUNT ' records'
           END-IF.
       TC-TXN-08-EXIT.
           EXIT.

      *----------------------------------------------------------------*
      * TC-TXN-13: No transactions posted for reject scenario
      *----------------------------------------------------------------*
       TC-TXN-13-NO-TRANS-POSTED.
           ADD 1 TO WS-TOTAL-TESTS
           MOVE 0 TO WS-TRAN-REC-COUNT
           MOVE 'N' TO WS-TRAN-EOF

           OPEN INPUT TRAN-FILE
           IF WS-TRAN-STATUS NOT = '00'
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-13: No trans file'
                       ' (empty as expected)'
               GO TO TC-TXN-13-EXIT
           END-IF

           PERFORM UNTIL WS-TRAN-EOF = 'Y'
               READ TRAN-FILE INTO TRAN-RECORD
               IF WS-TRAN-STATUS = '00'
                   ADD 1 TO WS-TRAN-REC-COUNT
               ELSE
                   MOVE 'Y' TO WS-TRAN-EOF
               END-IF
           END-PERFORM

           CLOSE TRAN-FILE

           IF WS-TRAN-REC-COUNT = 0
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-TXN-13: No transactions posted'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-TXN-13: Found '
                       WS-TRAN-REC-COUNT ' posted transactions'
           END-IF.
       TC-TXN-13-EXIT.
           EXIT.
