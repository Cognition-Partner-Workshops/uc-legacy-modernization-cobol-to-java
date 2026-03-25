      ******************************************************************
      * PROGRAM     : CBHELLO.CBL
      * Application : CardDemo
      * Type        : BATCH COBOL Program
      * FUNCTION    : Manages multiple user accounts using a COBOL
      *               table. Populates users, lists all accounts,
      *               deactivates a selected account, and re-lists.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CBHELLO.

       ENVIRONMENT DIVISION.

       DATA DIVISION.
       WORKING-STORAGE SECTION.

       01  WS-MAX-USERS               PIC 9(02) VALUE 5.
       01  WS-USER-INDEX              PIC 9(02) VALUE 0.
       01  WS-USER-COUNT              PIC 9(02) VALUE 0.
       01  WS-DISPLAY-IDX             PIC Z9.

       01  WS-USER-TABLE.
           05  WS-USER-ENTRY OCCURS 5 TIMES.
               10  WS-CUST-NAME       PIC X(30).
               10  WS-ACCT-ID         PIC 9(11).
               10  WS-CURR-BAL        PIC S9(10)V99.
               10  WS-CRED-LIMIT      PIC S9(10)V99.
               10  WS-AVAIL-CREDIT    PIC S9(10)V99.
               10  WS-ACCT-STATUS     PIC X(10).

       01  WS-DISPLAY-BALANCE         PIC -(9)9.99.
       01  WS-DISPLAY-CREDIT          PIC -(9)9.99.
       01  WS-DISPLAY-AVAILABLE       PIC -(9)9.99.

       PROCEDURE DIVISION.
       0000-MAIN-PROCESS.
           DISPLAY '================================================'.
           DISPLAY '  CARDDEMO - MULTI-USER ACCOUNT REPORT'.
           DISPLAY '================================================'.

           PERFORM 1000-POPULATE-USERS.

           DISPLAY SPACES.
           DISPLAY '--- ALL ACCOUNTS ---'.
           PERFORM 2000-LIST-ALL-USERS.

           DISPLAY SPACES.
           DISPLAY '--- DEACTIVATING ACCOUNT #3 (ALICE WONG) ---'.
           DISPLAY SPACES.
           PERFORM 3000-DEACTIVATE-USER-3.

           DISPLAY '--- UPDATED ACCOUNT LIST ---'.
           PERFORM 2000-LIST-ALL-USERS.

           DISPLAY '================================================'.
           DISPLAY '  END OF REPORT'.
           DISPLAY '================================================'.

           STOP RUN.

      *---------------------------------------------------------------*
       1000-POPULATE-USERS.
           MOVE 'JOHN DOE'          TO WS-CUST-NAME(1).
           MOVE 10000000001         TO WS-ACCT-ID(1).
           MOVE 5000.50             TO WS-CURR-BAL(1).
           MOVE 10000.00            TO WS-CRED-LIMIT(1).

           MOVE 'JANE SMITH'        TO WS-CUST-NAME(2).
           MOVE 10000000002         TO WS-ACCT-ID(2).
           MOVE 2500.75             TO WS-CURR-BAL(2).
           MOVE 8000.00             TO WS-CRED-LIMIT(2).

           MOVE 'ALICE WONG'        TO WS-CUST-NAME(3).
           MOVE 10000000003         TO WS-ACCT-ID(3).
           MOVE 7200.00             TO WS-CURR-BAL(3).
           MOVE 7000.00             TO WS-CRED-LIMIT(3).

           MOVE 'BOB MARTINEZ'      TO WS-CUST-NAME(4).
           MOVE 10000000004         TO WS-ACCT-ID(4).
           MOVE 0.00                TO WS-CURR-BAL(4).
           MOVE 5000.00             TO WS-CRED-LIMIT(4).

           MOVE 'SARA JOHNSON'      TO WS-CUST-NAME(5).
           MOVE 10000000005         TO WS-ACCT-ID(5).
           MOVE -150.25             TO WS-CURR-BAL(5).
           MOVE 12000.00            TO WS-CRED-LIMIT(5).

           MOVE WS-MAX-USERS TO WS-USER-COUNT.

           PERFORM VARYING WS-USER-INDEX FROM 1 BY 1
               UNTIL WS-USER-INDEX > WS-USER-COUNT
               PERFORM 1100-CALC-USER-STATUS
           END-PERFORM.
           EXIT.

      *---------------------------------------------------------------*
       1100-CALC-USER-STATUS.
           SUBTRACT WS-CURR-BAL(WS-USER-INDEX)
               FROM WS-CRED-LIMIT(WS-USER-INDEX)
               GIVING WS-AVAIL-CREDIT(WS-USER-INDEX).

           EVALUATE TRUE
               WHEN WS-CURR-BAL(WS-USER-INDEX) >
                   WS-CRED-LIMIT(WS-USER-INDEX)
                   MOVE 'OVERLIMIT' TO WS-ACCT-STATUS(WS-USER-INDEX)
               WHEN WS-CURR-BAL(WS-USER-INDEX) > 0
                   MOVE 'ACTIVE' TO WS-ACCT-STATUS(WS-USER-INDEX)
               WHEN WS-CURR-BAL(WS-USER-INDEX) = 0
                   MOVE 'ZERO BAL' TO WS-ACCT-STATUS(WS-USER-INDEX)
               WHEN OTHER
                   MOVE 'CREDIT' TO WS-ACCT-STATUS(WS-USER-INDEX)
           END-EVALUATE.
           EXIT.

      *---------------------------------------------------------------*
       2000-LIST-ALL-USERS.
           DISPLAY SPACES.
           DISPLAY '  #  CUSTOMER NAME          '
               '  ACCOUNT ID   BALANCE       STATUS'.
           DISPLAY '  -- ----------------------'
               '- ----------- ------------- ----------'.

           PERFORM VARYING WS-USER-INDEX FROM 1 BY 1
               UNTIL WS-USER-INDEX > WS-USER-COUNT
               PERFORM 2100-DISPLAY-USER-ROW
           END-PERFORM.

           DISPLAY SPACES.
           EXIT.

      *---------------------------------------------------------------*
       2100-DISPLAY-USER-ROW.
           MOVE WS-USER-INDEX TO WS-DISPLAY-IDX.
           MOVE WS-CURR-BAL(WS-USER-INDEX) TO WS-DISPLAY-BALANCE.

           DISPLAY '  ' WS-DISPLAY-IDX
               ' ' WS-CUST-NAME(WS-USER-INDEX)
               ' ' WS-ACCT-ID(WS-USER-INDEX)
               ' $' WS-DISPLAY-BALANCE
               ' ' WS-ACCT-STATUS(WS-USER-INDEX).
           EXIT.

      *---------------------------------------------------------------*
       3000-DEACTIVATE-USER-3.
           MOVE 'INACTIVE' TO WS-ACCT-STATUS(3).
           MOVE ZEROS      TO WS-CURR-BAL(3).
           MOVE ZEROS      TO WS-CRED-LIMIT(3).
           MOVE ZEROS      TO WS-AVAIL-CREDIT(3).
           EXIT.
