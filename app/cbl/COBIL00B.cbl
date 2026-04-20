      ******************************************************************
      * Program     : COBIL00B.CBL
      * Application : CardDemo
      * Type        : Batch COBOL Subprogram (Business Logic)
      * Function    : Bill payment business logic extracted from
      *               COBIL00C for unit testing. Contains NO CICS
      *               statements. All I/O is handled via parameters.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COBIL00B.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-VARIABLES.
         05 WS-TRAN-ID-NUM              PIC 9(16) VALUE ZEROS.
         05 WS-CURR-BAL                  PIC S9(10)V99.

       01 WS-TIMESTAMP.
         05 WS-TIMESTAMP-DATE           PIC X(10).
         05 WS-TIMESTAMP-FILLER         PIC X(01) VALUE '-'.
         05 WS-TIMESTAMP-TIME           PIC X(08).
         05 WS-TIMESTAMP-TM-MS6         PIC 9(06).

       01 WS-CURDATE-DATA.
         05 WS-CURDATE-YEAR             PIC X(04).
         05 WS-CURDATE-MONTH            PIC X(02).
         05 WS-CURDATE-DAY              PIC X(02).
         05 WS-CURTIME-HOURS            PIC X(02).
         05 WS-CURTIME-MINUTE           PIC X(02).
         05 WS-CURTIME-SECOND           PIC X(02).
         05 WS-CURDATE-REST             PIC X(05).

      *----------------------------------------------------------------*
      *                        LINKAGE SECTION
      *----------------------------------------------------------------*
       LINKAGE SECTION.

       01 LK-ACCT-ID                    PIC 9(11).

       COPY CVACT01Y.

       01 LK-ACCT-FOUND-FLAG            PIC X(01).
       01 LK-CONFIRM-FLAG               PIC X(01).
       01 LK-CURR-TRAN-ID               PIC X(16).

       COPY CVACT03Y.

       01 LK-XREF-FOUND-FLAG            PIC X(01).
       01 LK-RESULT-CODE                PIC 9(02).

       COPY CVTRA05Y.

       01 LK-UPDATED-ACCOUNT.
         05 LK-UPD-ACCT-ID              PIC 9(11).
         05 LK-UPD-ACCT-ACTIVE-STATUS   PIC X(01).
         05 LK-UPD-ACCT-CURR-BAL        PIC S9(10)V99.
         05 LK-UPD-ACCT-CREDIT-LIMIT    PIC S9(10)V99.
         05 LK-UPD-ACCT-CASH-CREDIT     PIC S9(10)V99.
         05 LK-UPD-ACCT-OPEN-DATE       PIC X(10).
         05 LK-UPD-ACCT-EXPIRY-DATE     PIC X(10).
         05 LK-UPD-ACCT-REISSUE-DATE    PIC X(10).
         05 LK-UPD-ACCT-CYC-CREDIT      PIC S9(10)V99.
         05 LK-UPD-ACCT-CYC-DEBIT       PIC S9(10)V99.
         05 LK-UPD-ACCT-ADDR-ZIP        PIC X(10).
         05 LK-UPD-ACCT-GROUP-ID        PIC X(10).
         05 LK-UPD-FILLER               PIC X(178).

       01 LK-MESSAGE                    PIC X(80).

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION USING LK-ACCT-ID
                                ACCOUNT-RECORD
                                LK-ACCT-FOUND-FLAG
                                LK-CONFIRM-FLAG
                                LK-CURR-TRAN-ID
                                CARD-XREF-RECORD
                                LK-XREF-FOUND-FLAG
                                LK-RESULT-CODE
                                TRAN-RECORD
                                LK-UPDATED-ACCOUNT
                                LK-MESSAGE.
       MAIN-PARA.

           MOVE 00 TO LK-RESULT-CODE
           MOVE SPACES TO LK-MESSAGE
           INITIALIZE TRAN-RECORD

           IF LK-ACCT-ID = ZEROS OR
              LK-ACCT-ID = SPACES OR LOW-VALUES
               MOVE 01 TO LK-RESULT-CODE
               MOVE 'Acct ID can NOT be empty...' TO LK-MESSAGE
               GOBACK
           END-IF

           EVALUATE LK-CONFIRM-FLAG
               WHEN 'Y'
               WHEN 'y'
                   CONTINUE
               WHEN 'N'
               WHEN 'n'
                   MOVE 06 TO LK-RESULT-CODE
                   MOVE 'Payment cancelled by user.' TO LK-MESSAGE
                   GOBACK
               WHEN SPACES
               WHEN LOW-VALUES
                   CONTINUE
               WHEN OTHER
                   MOVE 04 TO LK-RESULT-CODE
                   MOVE 'Invalid value. Valid values are (Y/N)...'
                                        TO LK-MESSAGE
                   GOBACK
           END-EVALUATE

           IF LK-ACCT-FOUND-FLAG = 'N'
               MOVE 03 TO LK-RESULT-CODE
               MOVE 'Account ID NOT found...' TO LK-MESSAGE
               GOBACK
           END-IF

           IF ACCT-CURR-BAL <= ZEROS
               MOVE 02 TO LK-RESULT-CODE
               MOVE 'You have nothing to pay...' TO LK-MESSAGE
               GOBACK
           END-IF

           IF LK-CONFIRM-FLAG = 'Y' OR 'y'

               IF LK-XREF-FOUND-FLAG = 'N'
                   MOVE 05 TO LK-RESULT-CODE
                   MOVE 'Account XREF NOT found...' TO LK-MESSAGE
                   GOBACK
               END-IF

               MOVE LK-CURR-TRAN-ID TO WS-TRAN-ID-NUM
               ADD 1 TO WS-TRAN-ID-NUM
               INITIALIZE TRAN-RECORD
               MOVE WS-TRAN-ID-NUM       TO TRAN-ID
               MOVE '02'                 TO TRAN-TYPE-CD
               MOVE 2                    TO TRAN-CAT-CD
               MOVE 'POS TERM'           TO TRAN-SOURCE
               MOVE 'BILL PAYMENT - ONLINE' TO TRAN-DESC
               MOVE ACCT-CURR-BAL        TO TRAN-AMT
               MOVE XREF-CARD-NUM        TO TRAN-CARD-NUM
               MOVE 999999999            TO TRAN-MERCHANT-ID
               MOVE 'BILL PAYMENT'       TO TRAN-MERCHANT-NAME
               MOVE 'N/A'                TO TRAN-MERCHANT-CITY
               MOVE 'N/A'                TO TRAN-MERCHANT-ZIP

               PERFORM GET-CURRENT-TIMESTAMP
               MOVE WS-TIMESTAMP         TO TRAN-ORIG-TS
               MOVE WS-TIMESTAMP         TO TRAN-PROC-TS

               MOVE ACCOUNT-RECORD TO LK-UPDATED-ACCOUNT
               COMPUTE LK-UPD-ACCT-CURR-BAL =
                   ACCT-CURR-BAL - TRAN-AMT

               MOVE 00 TO LK-RESULT-CODE
               MOVE 'Payment successful.' TO LK-MESSAGE
           ELSE
               MOVE 00 TO LK-RESULT-CODE
               MOVE 'Confirm to make a bill payment...'
                                          TO LK-MESSAGE
           END-IF.

           GOBACK.

      *----------------------------------------------------------------*
      *                      GET-CURRENT-TIMESTAMP
      *----------------------------------------------------------------*
       GET-CURRENT-TIMESTAMP.

           MOVE FUNCTION CURRENT-DATE TO WS-CURDATE-DATA

           INITIALIZE WS-TIMESTAMP
           STRING WS-CURDATE-YEAR '-'
                  WS-CURDATE-MONTH '-'
                  WS-CURDATE-DAY
                  DELIMITED BY SIZE
                  INTO WS-TIMESTAMP-DATE
           STRING WS-CURTIME-HOURS ':'
                  WS-CURTIME-MINUTE ':'
                  WS-CURTIME-SECOND
                  DELIMITED BY SIZE
                  INTO WS-TIMESTAMP-TIME
           MOVE ZEROS TO WS-TIMESTAMP-TM-MS6.
