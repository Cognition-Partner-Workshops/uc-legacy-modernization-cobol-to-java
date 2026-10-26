      ******************************************************************
      * LOADKSDS - parity-harness utility (not part of CardDemo).
      * Loads a fixed 150-byte record sequential file (DD_INFILE) into
      * a GnuCOBOL indexed file keyed on the first 16 bytes (DD_OUTKSDS)
      * so that CBACT02C can be run against a real INDEXED dataset.
      * Stands in for IDCAMS REPRO into the CARDDATA VSAM KSDS.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    LOADKSDS.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS  IS IN-STATUS.
           SELECT OUT-FILE ASSIGN TO OUTKSDS
                  ORGANIZATION IS INDEXED
                  ACCESS MODE  IS RANDOM
                  RECORD KEY   IS OUT-CARD-NUM
                  FILE STATUS  IS OUT-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC                   PIC X(150).
       FD  OUT-FILE.
       01  OUT-REC.
           05 OUT-CARD-NUM          PIC X(16).
           05 OUT-CARD-DATA         PIC X(134).
       WORKING-STORAGE SECTION.
       01  IN-STATUS                PIC XX.
       01  OUT-STATUS               PIC XX.
       01  EOF-FLAG                 PIC X VALUE 'N'.
       01  REC-COUNT                PIC 9(9) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE
           IF IN-STATUS NOT = '00'
               DISPLAY 'LOADKSDS: OPEN INFILE STATUS ' IN-STATUS
               STOP RUN RETURNING 12
           END-IF
           OPEN OUTPUT OUT-FILE
           IF OUT-STATUS NOT = '00'
               DISPLAY 'LOADKSDS: OPEN OUTKSDS STATUS ' OUT-STATUS
               STOP RUN RETURNING 12
           END-IF
           PERFORM UNTIL EOF-FLAG = 'Y'
               READ IN-FILE
                   AT END MOVE 'Y' TO EOF-FLAG
                   NOT AT END
                       MOVE IN-REC TO OUT-REC
                       WRITE OUT-REC
                       IF OUT-STATUS NOT = '00'
                           DISPLAY 'LOADKSDS: write failed, status '
                                   OUT-STATUS ' key ' OUT-CARD-NUM
                           STOP RUN RETURNING 12
                       END-IF
                       ADD 1 TO REC-COUNT
               END-READ
           END-PERFORM
           CLOSE IN-FILE OUT-FILE
           DISPLAY 'LOADKSDS: loaded ' REC-COUNT ' records'
           STOP RUN RETURNING 0.
