       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADIDX.
       AUTHOR. REFERENCE-HARNESS.
      *
      * Load fixed-width line-sequential files into the indexed files used
      * by CBSTM03B. The input files are already sorted by their keys.
      *
      * GnuCOBOL's native ASCII display numeric representation uses a
      * numeric trailing byte for positive overpunch and lowercase p-y for
      * negative overpunch. Normalize only that amount byte while loading;
      * prepare_data.py retains the source record math and source encoding.
      *
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TRNX-IN ASSIGN TO "TRNXIN"
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT XREF-IN ASSIGN TO "XREFIN"
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT CUST-IN ASSIGN TO "CUSTIN"
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT ACCT-IN ASSIGN TO "ACCTIN"
                  ORGANIZATION IS LINE SEQUENTIAL.
           SELECT TRNX-OUT ASSIGN TO "TRNXFILE"
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS TRNX-OUT-KEY.
           SELECT XREF-OUT ASSIGN TO "XREFFILE"
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS XREF-OUT-KEY.
           SELECT CUST-OUT ASSIGN TO "CUSTFILE"
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS CUST-OUT-KEY.
           SELECT ACCT-OUT ASSIGN TO "ACCTFILE"
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS SEQUENTIAL
                  RECORD KEY IS ACCT-OUT-KEY.

       DATA DIVISION.
       FILE SECTION.
       FD TRNX-IN.
       01 TRNX-IN-REC.
           05 FILLER PIC X(148).
           05 TRNX-IN-AMT PIC X(11).
           05 FILLER PIC X(191).
       FD XREF-IN.
       01 XREF-IN-REC PIC X(50).
       FD CUST-IN.
       01 CUST-IN-REC PIC X(500).
       FD ACCT-IN.
       01 ACCT-IN-REC PIC X(300).

       FD TRNX-OUT.
       01 TRNX-OUT-REC.
           05 TRNX-OUT-KEY PIC X(32).
           05 FILLER PIC X(116).
           05 TRNX-OUT-AMT PIC X(11).
           05 FILLER PIC X(191).
       FD XREF-OUT.
       01 XREF-OUT-REC.
           05 XREF-OUT-KEY PIC X(16).
           05 XREF-OUT-DATA PIC X(34).
       FD CUST-OUT.
       01 CUST-OUT-REC.
           05 CUST-OUT-KEY PIC X(9).
           05 CUST-OUT-DATA PIC X(491).
       FD ACCT-OUT.
       01 ACCT-OUT-REC.
           05 ACCT-OUT-KEY PIC X(11).
           05 ACCT-OUT-DATA PIC X(289).

       WORKING-STORAGE SECTION.
       01 EOF-FLAGS.
           05 TRNX-EOF PIC X VALUE "N".
           05 XREF-EOF PIC X VALUE "N".
           05 CUST-EOF PIC X VALUE "N".
           05 ACCT-EOF PIC X VALUE "N".

       PROCEDURE DIVISION.
           OPEN INPUT TRNX-IN XREF-IN CUST-IN ACCT-IN
                OUTPUT TRNX-OUT XREF-OUT CUST-OUT ACCT-OUT
           PERFORM UNTIL TRNX-EOF = "Y"
               READ TRNX-IN
                   AT END MOVE "Y" TO TRNX-EOF
                   NOT AT END
                       MOVE TRNX-IN-REC TO TRNX-OUT-REC
                       EVALUATE TRNX-OUT-AMT (11:1)
                           WHEN "{" MOVE "0" TO TRNX-OUT-AMT (11:1)
                           WHEN "A" MOVE "1" TO TRNX-OUT-AMT (11:1)
                           WHEN "B" MOVE "2" TO TRNX-OUT-AMT (11:1)
                           WHEN "C" MOVE "3" TO TRNX-OUT-AMT (11:1)
                           WHEN "D" MOVE "4" TO TRNX-OUT-AMT (11:1)
                           WHEN "E" MOVE "5" TO TRNX-OUT-AMT (11:1)
                           WHEN "F" MOVE "6" TO TRNX-OUT-AMT (11:1)
                           WHEN "G" MOVE "7" TO TRNX-OUT-AMT (11:1)
                           WHEN "H" MOVE "8" TO TRNX-OUT-AMT (11:1)
                           WHEN "I" MOVE "9" TO TRNX-OUT-AMT (11:1)
                           WHEN "}" MOVE "p" TO TRNX-OUT-AMT (11:1)
                           WHEN "J" MOVE "q" TO TRNX-OUT-AMT (11:1)
                           WHEN "K" MOVE "r" TO TRNX-OUT-AMT (11:1)
                           WHEN "L" MOVE "s" TO TRNX-OUT-AMT (11:1)
                           WHEN "M" MOVE "t" TO TRNX-OUT-AMT (11:1)
                           WHEN "N" MOVE "u" TO TRNX-OUT-AMT (11:1)
                           WHEN "O" MOVE "v" TO TRNX-OUT-AMT (11:1)
                           WHEN "P" MOVE "w" TO TRNX-OUT-AMT (11:1)
                           WHEN "Q" MOVE "x" TO TRNX-OUT-AMT (11:1)
                           WHEN "R" MOVE "y" TO TRNX-OUT-AMT (11:1)
                       END-EVALUATE
                       WRITE TRNX-OUT-REC
               END-READ
           END-PERFORM
           PERFORM UNTIL XREF-EOF = "Y"
               READ XREF-IN
                   AT END MOVE "Y" TO XREF-EOF
                   NOT AT END
                       MOVE XREF-IN-REC TO XREF-OUT-REC
                       WRITE XREF-OUT-REC
               END-READ
           END-PERFORM
           PERFORM UNTIL CUST-EOF = "Y"
               READ CUST-IN
                   AT END MOVE "Y" TO CUST-EOF
                   NOT AT END
                       MOVE CUST-IN-REC TO CUST-OUT-REC
                       WRITE CUST-OUT-REC
               END-READ
           END-PERFORM
           PERFORM UNTIL ACCT-EOF = "Y"
               READ ACCT-IN
                   AT END MOVE "Y" TO ACCT-EOF
                   NOT AT END
                       MOVE ACCT-IN-REC TO ACCT-OUT-REC
                       WRITE ACCT-OUT-REC
               END-READ
           END-PERFORM
           CLOSE TRNX-IN XREF-IN CUST-IN ACCT-IN
                 TRNX-OUT XREF-OUT CUST-OUT ACCT-OUT
           GOBACK.
