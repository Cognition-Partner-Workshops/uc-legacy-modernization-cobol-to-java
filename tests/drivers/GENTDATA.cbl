      ******************************************************************
      * Program     : GENTDATA.CBL
      * Application : CardDemo - Test Data Generator
      * Type        : Batch COBOL Program
      * Function    : Generates test fixture data files for CBTRN02C
      *               transaction processing tests.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. GENTDATA.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT DALYTRAN-FILE ASSIGN TO DALYTRAN
                  ORGANIZATION IS SEQUENTIAL
                  ACCESS MODE  IS SEQUENTIAL
                  FILE STATUS  IS WS-DALY-STATUS.

           SELECT XREF-FILE ASSIGN TO XREFFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-XREF-CARD-NUM
                  FILE STATUS  IS WS-XREF-STATUS.

           SELECT ACCOUNT-FILE ASSIGN TO ACCTFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-ACCT-ID
                  FILE STATUS  IS WS-ACCT-STATUS.

           SELECT TCATBAL-FILE ASSIGN TO TCATBALF
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS FD-TCAT-KEY
                  FILE STATUS  IS WS-TCAT-STATUS.

       DATA DIVISION.
       FILE SECTION.

       FD  DALYTRAN-FILE.
       01  FD-DALY-REC                   PIC X(350).

       FD  XREF-FILE.
       01  FD-XREF-REC.
           05 FD-XREF-CARD-NUM          PIC X(16).
           05 FD-XREF-REST              PIC X(34).

       FD  ACCOUNT-FILE.
       01  FD-ACCT-REC.
           05 FD-ACCT-ID                PIC 9(11).
           05 FD-ACCT-REST              PIC X(289).

       FD  TCATBAL-FILE.
       01  FD-TCAT-REC.
           05 FD-TCAT-KEY.
              10 FD-TCAT-ACCT-ID        PIC 9(11).
              10 FD-TCAT-TYPE-CD        PIC X(02).
              10 FD-TCAT-CD             PIC 9(04).
           05 FD-TCAT-REST              PIC X(33).

       WORKING-STORAGE SECTION.

       01 WS-FILE-STATUSES.
         05 WS-DALY-STATUS              PIC X(02).
         05 WS-XREF-STATUS              PIC X(02).
         05 WS-ACCT-STATUS              PIC X(02).
         05 WS-TCAT-STATUS              PIC X(02).

       01 WS-GEN-MODE                   PIC X(01) VALUE 'V'.
           88 WS-MODE-VALID             VALUE 'V'.
           88 WS-MODE-REJECT            VALUE 'R'.

       COPY CVTRA06Y.

       COPY CVACT03Y.

       COPY CVACT01Y.

       COPY CVTRA01Y.

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

           ACCEPT WS-GEN-MODE FROM ENVIRONMENT 'GEN_MODE'

           IF WS-MODE-VALID
               DISPLAY 'Generating VALID transaction test data...'
               PERFORM GEN-VALID-DATA
           ELSE
               DISPLAY 'Generating REJECT transaction test data...'
               PERFORM GEN-REJECT-DATA
           END-IF

           DISPLAY 'Test data generation complete.'
           STOP RUN.

      *----------------------------------------------------------------*
      * GEN-VALID-DATA - Generate valid transaction scenario files
      *----------------------------------------------------------------*
       GEN-VALID-DATA.

           PERFORM GEN-XREF-FILE
           PERFORM GEN-ACCT-FILE-VALID
           PERFORM GEN-TCATBAL-EMPTY
           PERFORM GEN-DALYTRAN-VALID.

      *----------------------------------------------------------------*
      * GEN-REJECT-DATA - Generate reject transaction scenario files
      *----------------------------------------------------------------*
       GEN-REJECT-DATA.

           PERFORM GEN-XREF-FILE
           PERFORM GEN-ACCT-FILE-REJECT
           PERFORM GEN-TCATBAL-EMPTY
           PERFORM GEN-DALYTRAN-REJECT.

      *----------------------------------------------------------------*
      * GEN-XREF-FILE - Cross-reference file (CVACT03Y, 50 bytes)
      *----------------------------------------------------------------*
       GEN-XREF-FILE.
           OPEN OUTPUT XREF-FILE
           IF WS-XREF-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open XREF file: '
                       WS-XREF-STATUS
               STOP RUN
           END-IF

           INITIALIZE CARD-XREF-RECORD
           MOVE '4000123456789010' TO XREF-CARD-NUM
           MOVE 100000001         TO XREF-CUST-ID
           MOVE 12345678901       TO XREF-ACCT-ID
           WRITE FD-XREF-REC FROM CARD-XREF-RECORD

           INITIALIZE CARD-XREF-RECORD
           MOVE '4000123456789020' TO XREF-CARD-NUM
           MOVE 100000002         TO XREF-CUST-ID
           MOVE 12345678902       TO XREF-ACCT-ID
           WRITE FD-XREF-REC FROM CARD-XREF-RECORD

           INITIALIZE CARD-XREF-RECORD
           MOVE '4000123456789030' TO XREF-CARD-NUM
           MOVE 100000003         TO XREF-CUST-ID
           MOVE 12345678903       TO XREF-ACCT-ID
           WRITE FD-XREF-REC FROM CARD-XREF-RECORD

           CLOSE XREF-FILE
           DISPLAY 'XREF file created.'.

      *----------------------------------------------------------------*
      * GEN-ACCT-FILE-VALID - Account file for valid scenario
      *----------------------------------------------------------------*
       GEN-ACCT-FILE-VALID.
           OPEN OUTPUT ACCOUNT-FILE
           IF WS-ACCT-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open ACCT file: '
                       WS-ACCT-STATUS
               STOP RUN
           END-IF

           INITIALIZE ACCOUNT-RECORD
           MOVE 12345678901       TO ACCT-ID
           MOVE 'Y'               TO ACCT-ACTIVE-STATUS
           MOVE 200.00            TO ACCT-CURR-BAL
           MOVE 1000.00           TO ACCT-CREDIT-LIMIT
           MOVE 500.00            TO ACCT-CASH-CREDIT-LIMIT
           MOVE '2020-01-01'      TO ACCT-OPEN-DATE
           MOVE '2027-12-31'      TO ACCT-EXPIRAION-DATE
           MOVE '2025-01-01'      TO ACCT-REISSUE-DATE
           MOVE 100.00            TO ACCT-CURR-CYC-CREDIT
           MOVE 50.00             TO ACCT-CURR-CYC-DEBIT
           MOVE '12345     '      TO ACCT-ADDR-ZIP
           MOVE 'GRP001    '      TO ACCT-GROUP-ID
           WRITE FD-ACCT-REC FROM ACCOUNT-RECORD

           CLOSE ACCOUNT-FILE
           DISPLAY 'Account file (valid) created.'.

      *----------------------------------------------------------------*
      * GEN-ACCT-FILE-REJECT - Account files for reject scenarios
      *----------------------------------------------------------------*
       GEN-ACCT-FILE-REJECT.
           OPEN OUTPUT ACCOUNT-FILE
           IF WS-ACCT-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open ACCT file: '
                       WS-ACCT-STATUS
               STOP RUN
           END-IF

           INITIALIZE ACCOUNT-RECORD
           MOVE 12345678902       TO ACCT-ID
           MOVE 'Y'               TO ACCT-ACTIVE-STATUS
           MOVE 490.00            TO ACCT-CURR-BAL
           MOVE 500.00            TO ACCT-CREDIT-LIMIT
           MOVE 250.00            TO ACCT-CASH-CREDIT-LIMIT
           MOVE '2020-01-01'      TO ACCT-OPEN-DATE
           MOVE '2027-12-31'      TO ACCT-EXPIRAION-DATE
           MOVE '2025-01-01'      TO ACCT-REISSUE-DATE
           MOVE 490.00            TO ACCT-CURR-CYC-CREDIT
           MOVE 0                 TO ACCT-CURR-CYC-DEBIT
           MOVE '12345     '      TO ACCT-ADDR-ZIP
           MOVE 'GRP001    '      TO ACCT-GROUP-ID
           WRITE FD-ACCT-REC FROM ACCOUNT-RECORD

           INITIALIZE ACCOUNT-RECORD
           MOVE 12345678903       TO ACCT-ID
           MOVE 'Y'               TO ACCT-ACTIVE-STATUS
           MOVE 0                 TO ACCT-CURR-BAL
           MOVE 1000.00           TO ACCT-CREDIT-LIMIT
           MOVE 500.00            TO ACCT-CASH-CREDIT-LIMIT
           MOVE '2015-01-01'      TO ACCT-OPEN-DATE
           MOVE '2020-01-01'      TO ACCT-EXPIRAION-DATE
           MOVE '2019-01-01'      TO ACCT-REISSUE-DATE
           MOVE 0                 TO ACCT-CURR-CYC-CREDIT
           MOVE 0                 TO ACCT-CURR-CYC-DEBIT
           MOVE '12345     '      TO ACCT-ADDR-ZIP
           MOVE 'GRP001    '      TO ACCT-GROUP-ID
           WRITE FD-ACCT-REC FROM ACCOUNT-RECORD

           CLOSE ACCOUNT-FILE
           DISPLAY 'Account file (reject) created.'.

      *----------------------------------------------------------------*
      * GEN-TCATBAL-EMPTY - Empty transaction category balance file
      *----------------------------------------------------------------*
       GEN-TCATBAL-EMPTY.
           OPEN OUTPUT TCATBAL-FILE
           IF WS-TCAT-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open TCATBAL file: '
                       WS-TCAT-STATUS
               STOP RUN
           END-IF

           CLOSE TCATBAL-FILE
           DISPLAY 'TCATBAL file (empty) created.'.

      *----------------------------------------------------------------*
      * GEN-DALYTRAN-VALID - Valid daily transactions
      *----------------------------------------------------------------*
       GEN-DALYTRAN-VALID.
           OPEN OUTPUT DALYTRAN-FILE
           IF WS-DALY-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open DALYTRAN file: '
                       WS-DALY-STATUS
               STOP RUN
           END-IF

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000001' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'TEST PURCHASE - VALID $100'
                                  TO DALYTRAN-DESC
           MOVE 100.00            TO DALYTRAN-AMT
           MOVE 111111111         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT A'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY A'     TO DALYTRAN-MERCHANT-CITY
           MOVE '10001     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789010' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-10.00.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000002' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'TEST PURCHASE - VALID $500'
                                  TO DALYTRAN-DESC
           MOVE 500.00            TO DALYTRAN-AMT
           MOVE 222222222         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT B'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY B'     TO DALYTRAN-MERCHANT-CITY
           MOVE '20002     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789010' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-10.05.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000003' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5002              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'TEST REFUND - CREDIT $50'
                                  TO DALYTRAN-DESC
           MOVE -50.00            TO DALYTRAN-AMT
           MOVE 333333333         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT C'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY C'     TO DALYTRAN-MERCHANT-CITY
           MOVE '30003     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789010' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-10.10.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           CLOSE DALYTRAN-FILE
           DISPLAY 'DALYTRAN (valid) file created with 3 records.'.

      *----------------------------------------------------------------*
      * GEN-DALYTRAN-REJECT - Reject scenario daily transactions
      *----------------------------------------------------------------*
       GEN-DALYTRAN-REJECT.
           OPEN OUTPUT DALYTRAN-FILE
           IF WS-DALY-STATUS NOT = '00'
               DISPLAY 'ERROR: Cannot open DALYTRAN file: '
                       WS-DALY-STATUS
               STOP RUN
           END-IF

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000010' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'REJECT TEST - INVALID CARD'
                                  TO DALYTRAN-DESC
           MOVE 50.00             TO DALYTRAN-AMT
           MOVE 444444444         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT D'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY D'     TO DALYTRAN-MERCHANT-CITY
           MOVE '40004     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '9999999999999999' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-11.00.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000011' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'REJECT TEST - ACCT NOT FOUND'
                                  TO DALYTRAN-DESC
           MOVE 75.00             TO DALYTRAN-AMT
           MOVE 555555555         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT E'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY E'     TO DALYTRAN-MERCHANT-CITY
           MOVE '50005     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789010' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-11.05.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000012' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'REJECT TEST - OVERLIMIT'
                                  TO DALYTRAN-DESC
           MOVE 999.00            TO DALYTRAN-AMT
           MOVE 666666666         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT F'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY F'     TO DALYTRAN-MERCHANT-CITY
           MOVE '60006     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789020' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-11.10.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           INITIALIZE DALYTRAN-RECORD
           MOVE '0000000000000013' TO DALYTRAN-ID
           MOVE '01'              TO DALYTRAN-TYPE-CD
           MOVE 5001              TO DALYTRAN-CAT-CD
           MOVE 'POS TERM  '      TO DALYTRAN-SOURCE
           MOVE 'REJECT TEST - EXPIRED ACCOUNT'
                                  TO DALYTRAN-DESC
           MOVE 25.00             TO DALYTRAN-AMT
           MOVE 777777777         TO DALYTRAN-MERCHANT-ID
           MOVE 'TEST MERCHANT G'
                                  TO DALYTRAN-MERCHANT-NAME
           MOVE 'TEST CITY G'     TO DALYTRAN-MERCHANT-CITY
           MOVE '70007     '      TO DALYTRAN-MERCHANT-ZIP
           MOVE '4000123456789030' TO DALYTRAN-CARD-NUM
           MOVE '2026-04-20-11.15.00.000000'
                                  TO DALYTRAN-ORIG-TS
           WRITE FD-DALY-REC FROM DALYTRAN-RECORD

           CLOSE DALYTRAN-FILE
           DISPLAY 'DALYTRAN (reject) file created with 4 records.'.
