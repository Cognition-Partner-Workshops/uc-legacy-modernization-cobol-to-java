      ******************************************************************
      * Program     : TBIL00C.CBL
      * Application : CardDemo - Unit Test Driver
      * Type        : Batch COBOL Program
      * Function    : Test driver for COBIL00B bill payment
      *               business logic subprogram.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. TBIL00C.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-TEST-COUNTERS.
         05 WS-TOTAL-TESTS              PIC 9(03) VALUE 0.
         05 WS-PASS-COUNT               PIC 9(03) VALUE 0.
         05 WS-FAIL-COUNT               PIC 9(03) VALUE 0.

       01 WS-TEST-FIELDS.
         05 WS-ACCT-ID                  PIC 9(11).
         05 WS-ACCT-FOUND-FLAG          PIC X(01).
         05 WS-CONFIRM-FLAG             PIC X(01).
         05 WS-CURR-TRAN-ID             PIC X(16).
         05 WS-XREF-FOUND-FLAG          PIC X(01).
         05 WS-RESULT-CODE              PIC 9(02).
         05 WS-MESSAGE                  PIC X(80).

       COPY CVACT01Y.

       COPY CVACT03Y.

       COPY CVTRA05Y.

       01 WS-UPDATED-ACCOUNT.
         05 WS-UPD-ACCT-ID              PIC 9(11).
         05 WS-UPD-ACCT-ACTIVE-STATUS   PIC X(01).
         05 WS-UPD-ACCT-CURR-BAL        PIC S9(10)V99.
         05 WS-UPD-ACCT-CREDIT-LIMIT    PIC S9(10)V99.
         05 WS-UPD-ACCT-CASH-CREDIT     PIC S9(10)V99.
         05 WS-UPD-ACCT-OPEN-DATE       PIC X(10).
         05 WS-UPD-ACCT-EXPIRY-DATE     PIC X(10).
         05 WS-UPD-ACCT-REISSUE-DATE    PIC X(10).
         05 WS-UPD-ACCT-CYC-CREDIT      PIC S9(10)V99.
         05 WS-UPD-ACCT-CYC-DEBIT       PIC S9(10)V99.
         05 WS-UPD-ACCT-ADDR-ZIP        PIC X(10).
         05 WS-UPD-ACCT-GROUP-ID        PIC X(10).
         05 WS-UPD-FILLER               PIC X(178).

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

           DISPLAY '=============================================='
           DISPLAY 'COBIL00B BILL PAYMENT UNIT TEST SUITE'
           DISPLAY '=============================================='
           DISPLAY SPACES

           PERFORM TC-PAY-01
           PERFORM TC-PAY-02
           PERFORM TC-PAY-03
           PERFORM TC-PAY-04
           PERFORM TC-PAY-05
           PERFORM TC-PAY-06
           PERFORM TC-PAY-07
           PERFORM TC-PAY-08

           DISPLAY SPACES
           DISPLAY '=============================================='
           DISPLAY 'TEST SUMMARY'
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
      * TC-PAY-01: Successful payment
      *----------------------------------------------------------------*
       TC-PAY-01.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE 500.00       TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 1000.00      TO ACCT-CASH-CREDIT-LIMIT
           MOVE '2020-01-01' TO ACCT-OPEN-DATE
           MOVE '2027-12-31' TO ACCT-EXPIRAION-DATE
           MOVE '2025-01-01' TO ACCT-REISSUE-DATE
           MOVE 0            TO ACCT-CURR-CYC-CREDIT
           MOVE 0            TO ACCT-CURR-CYC-DEBIT
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE '4000123456789010' TO XREF-CARD-NUM
           MOVE 123456789    TO XREF-CUST-ID
           MOVE 12345678901  TO XREF-ACCT-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 00 AND
              TRAN-AMT = 500.00 AND
              TRAN-TYPE-CD = '02' AND
              WS-UPD-ACCT-CURR-BAL = 0
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-01: Successful payment'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-01: Successful payment'
               DISPLAY '  RC=' WS-RESULT-CODE
                       ' TRAN-AMT=' TRAN-AMT
               DISPLAY '  UPD-BAL=' WS-UPD-ACCT-CURR-BAL
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-02: Zero balance rejection
      *----------------------------------------------------------------*
       TC-PAY-02.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE 0            TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 02
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-02: Zero balance rejection'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-02: Zero balance rejection'
               DISPLAY '  Expected RC=02'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-03: Negative balance
      *----------------------------------------------------------------*
       TC-PAY-03.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE -100.00      TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 02
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-03: Negative balance'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-03: Negative balance'
               DISPLAY '  Expected RC=02'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-04: Empty account ID
      *----------------------------------------------------------------*
       TC-PAY-04.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE ZEROS        TO WS-ACCT-ID
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 01
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-04: Empty account ID'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-04: Empty account ID'
               DISPLAY '  Expected RC=01'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-05: Confirm='N' (payment cancelled)
      *----------------------------------------------------------------*
       TC-PAY-05.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE 500.00       TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'N'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 06
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-05: Confirm N - cancelled'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-05: Confirm N - cancelled'
               DISPLAY '  Expected RC=06'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-06: Invalid confirm value
      *----------------------------------------------------------------*
       TC-PAY-06.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE 500.00       TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'X'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 04
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-06: Invalid confirm value'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-06: Invalid confirm value'
               DISPLAY '  Expected RC=04'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-07: Transaction ID generation
      *----------------------------------------------------------------*
       TC-PAY-07.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 12345678901  TO WS-ACCT-ID
           MOVE 12345678901  TO ACCT-ID
           MOVE 'Y'          TO ACCT-ACTIVE-STATUS
           MOVE 250.00       TO ACCT-CURR-BAL
           MOVE 5000.00      TO ACCT-CREDIT-LIMIT
           MOVE 1000.00      TO ACCT-CASH-CREDIT-LIMIT
           MOVE '2020-01-01' TO ACCT-OPEN-DATE
           MOVE '2027-12-31' TO ACCT-EXPIRAION-DATE
           MOVE 'Y'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE '4000123456789010' TO XREF-CARD-NUM
           MOVE 123456789    TO XREF-CUST-ID
           MOVE 12345678901  TO XREF-ACCT-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 00 AND
              TRAN-ID = '0000000000000006'
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-07: Transaction ID generation'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-07: Transaction ID generation'
               DISPLAY '  Expected TRAN-ID=0000000000000006'
               DISPLAY '  Got      TRAN-ID=' TRAN-ID
               DISPLAY '  RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-PAY-08: Account not found
      *----------------------------------------------------------------*
       TC-PAY-08.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE ACCOUNT-RECORD
           INITIALIZE CARD-XREF-RECORD
           INITIALIZE TRAN-RECORD
           INITIALIZE WS-UPDATED-ACCOUNT

           MOVE 99999999999  TO WS-ACCT-ID
           MOVE 'N'          TO WS-ACCT-FOUND-FLAG
           MOVE 'Y'          TO WS-CONFIRM-FLAG
           MOVE '0000000000000005' TO WS-CURR-TRAN-ID
           MOVE 'Y'          TO WS-XREF-FOUND-FLAG
           MOVE 00           TO WS-RESULT-CODE
           MOVE SPACES       TO WS-MESSAGE

           CALL 'COBIL00B' USING WS-ACCT-ID
                                 ACCOUNT-RECORD
                                 WS-ACCT-FOUND-FLAG
                                 WS-CONFIRM-FLAG
                                 WS-CURR-TRAN-ID
                                 CARD-XREF-RECORD
                                 WS-XREF-FOUND-FLAG
                                 WS-RESULT-CODE
                                 TRAN-RECORD
                                 WS-UPDATED-ACCOUNT
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 03
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-PAY-08: Account not found'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-PAY-08: Account not found'
               DISPLAY '  Expected RC=03'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.
