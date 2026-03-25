       IDENTIFICATION DIVISION.
       PROGRAM-ID. ADD2NUMS.
       AUTHOR. DEVIN.
      *
      * Batch program to add pairs of numbers read from a flat file.
      * Input:  Sequential file with two 7-char numeric fields per line
      *         Format: 9(5)V99 <space> 9(5)V99
      * Output: Display of each addition and a summary record count.
      *
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT INPUT-FILE ASSIGN TO 'ADDINPUT'
               ORGANIZATION IS LINE SEQUENTIAL
               FILE STATUS IS WS-FILE-STATUS.
      *
       DATA DIVISION.
       FILE SECTION.
       FD INPUT-FILE.
       01 INPUT-RECORD.
           05 IR-NUM1       PIC 9(5)V99.
           05 FILLER        PIC X(1).
           05 IR-NUM2       PIC 9(5)V99.
      *
       WORKING-STORAGE SECTION.
       01 WS-FILE-STATUS    PIC XX.
       01 WS-EOF-FLAG       PIC X VALUE 'N'.
           88 END-OF-FILE   VALUE 'Y'.
       01 WS-RESULT         PIC 9(6)V99 VALUE ZEROS.
       01 WS-DISPLAY-NUM1   PIC Z(4)9.99.
       01 WS-DISPLAY-NUM2   PIC Z(4)9.99.
       01 WS-DISPLAY-RES    PIC Z(5)9.99.
       01 WS-REC-COUNT      PIC 9(5) VALUE ZEROS.
       01 WS-INPUT-LEN      PIC 9(3) VALUE ZEROS.
      *
       PROCEDURE DIVISION.
       0000-MAIN.
           OPEN INPUT INPUT-FILE
           IF WS-FILE-STATUS NOT = '00'
               DISPLAY 'ERROR OPENING INPUT FILE: ' WS-FILE-STATUS
               STOP RUN
           END-IF
           DISPLAY '--- BATCH ADDITION START ---'
           PERFORM 1000-PROCESS-RECORDS
               UNTIL END-OF-FILE
           DISPLAY '--- BATCH COMPLETE ---'
           DISPLAY 'RECORDS PROCESSED: ' WS-REC-COUNT
           CLOSE INPUT-FILE
           STOP RUN.
      *
       1000-PROCESS-RECORDS.
           READ INPUT-FILE
               AT END
                   SET END-OF-FILE TO TRUE
               NOT AT END
                   MOVE FUNCTION LENGTH(
                       FUNCTION TRIM(INPUT-RECORD))
                       TO WS-INPUT-LEN
                   IF WS-INPUT-LEN > 0
                       ADD 1 TO WS-REC-COUNT
                       ADD IR-NUM1 TO IR-NUM2
                           GIVING WS-RESULT
                       MOVE IR-NUM1 TO WS-DISPLAY-NUM1
                       MOVE IR-NUM2 TO WS-DISPLAY-NUM2
                       MOVE WS-RESULT TO WS-DISPLAY-RES
                       DISPLAY 'REC ' WS-REC-COUNT
                           ': ' WS-DISPLAY-NUM1
                           ' + ' WS-DISPLAY-NUM2
                           ' = ' WS-DISPLAY-RES
                   END-IF
           END-READ.
