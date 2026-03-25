      ******************************************************************
      * PROGRAM     : CBHELLO.CBL
      * Application : CardDemo
      * Type        : BATCH COBOL Program
      * FUNCTION    : Demonstrates account deactivation by
      *               displaying the account before and after
      *               deactivation with status and balance changes.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CBHELLO.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       01  WS-CUSTOMER-NAME           PIC X(30)
                                      VALUE 'JOHN DOE'.
       01  WS-ACCOUNT-ID              PIC 9(11)
                                      VALUE 12345678901.
       01  WS-CURRENT-BALANCE         PIC S9(10)V99
                                      VALUE 5000.50.
       01  WS-CREDIT-LIMIT            PIC S9(10)V99
                                      VALUE 10000.00.
       01  WS-AVAILABLE-CREDIT        PIC S9(10)V99
                                      VALUE ZEROS.
       01  WS-DISPLAY-BALANCE         PIC Z(9)9.99.
       01  WS-DISPLAY-CREDIT          PIC Z(9)9.99.
       01  WS-DISPLAY-AVAILABLE       PIC Z(9)9.99.
       01  WS-ACCOUNT-STATUS          PIC X(10)
                                      VALUE SPACES.

       PROCEDURE DIVISION.
       0000-MAIN-PROCESS.
           DISPLAY '================================================'.
           DISPLAY '  CARDDEMO - ACCOUNT SUMMARY REPORT'.
           DISPLAY '================================================'.
           DISPLAY SPACES.

           PERFORM 1000-CALCULATE-AVAILABLE-CREDIT.
           PERFORM 2000-DETERMINE-ACCOUNT-STATUS.
           PERFORM 3000-DISPLAY-ACCOUNT-DETAILS.

           DISPLAY SPACES.
           DISPLAY '--- DEACTIVATING ACCOUNT ---'.
           DISPLAY SPACES.

           PERFORM 4000-DEACTIVATE-ACCOUNT.
           PERFORM 3000-DISPLAY-ACCOUNT-DETAILS.

           DISPLAY SPACES.
           DISPLAY '================================================'.
           DISPLAY '  END OF REPORT'.
           DISPLAY '================================================'.

           STOP RUN.

      *---------------------------------------------------------------*
       1000-CALCULATE-AVAILABLE-CREDIT.
           SUBTRACT WS-CURRENT-BALANCE FROM WS-CREDIT-LIMIT
               GIVING WS-AVAILABLE-CREDIT.
           EXIT.

      *---------------------------------------------------------------*
       2000-DETERMINE-ACCOUNT-STATUS.
           EVALUATE TRUE
               WHEN WS-CURRENT-BALANCE > WS-CREDIT-LIMIT
                   MOVE 'OVERLIMIT' TO WS-ACCOUNT-STATUS
               WHEN WS-CURRENT-BALANCE > 0
                   MOVE 'ACTIVE' TO WS-ACCOUNT-STATUS
               WHEN WS-CURRENT-BALANCE = 0
                   MOVE 'ZERO BAL' TO WS-ACCOUNT-STATUS
               WHEN OTHER
                   MOVE 'CREDIT' TO WS-ACCOUNT-STATUS
           END-EVALUATE.
           EXIT.

      *---------------------------------------------------------------*
       4000-DEACTIVATE-ACCOUNT.
           MOVE 'INACTIVE' TO WS-ACCOUNT-STATUS.
           MOVE ZEROS TO WS-CURRENT-BALANCE.
           MOVE ZEROS TO WS-CREDIT-LIMIT.
           MOVE ZEROS TO WS-AVAILABLE-CREDIT.
           MOVE ZEROS TO WS-DISPLAY-BALANCE.
           MOVE ZEROS TO WS-DISPLAY-CREDIT.
           MOVE ZEROS TO WS-DISPLAY-AVAILABLE.
           EXIT.

      *---------------------------------------------------------------*
       3000-DISPLAY-ACCOUNT-DETAILS.
           MOVE WS-CURRENT-BALANCE  TO WS-DISPLAY-BALANCE.
           MOVE WS-CREDIT-LIMIT     TO WS-DISPLAY-CREDIT.
           MOVE WS-AVAILABLE-CREDIT TO WS-DISPLAY-AVAILABLE.

           DISPLAY '  CUSTOMER NAME    : ' WS-CUSTOMER-NAME.
           DISPLAY '  ACCOUNT ID       : ' WS-ACCOUNT-ID.
           DISPLAY '  CURRENT BALANCE  : $' WS-DISPLAY-BALANCE.
           DISPLAY '  CREDIT LIMIT     : $' WS-DISPLAY-CREDIT.
           DISPLAY '  AVAILABLE CREDIT : $' WS-DISPLAY-AVAILABLE.
           DISPLAY '  ACCOUNT STATUS   : ' WS-ACCOUNT-STATUS.
           EXIT.
