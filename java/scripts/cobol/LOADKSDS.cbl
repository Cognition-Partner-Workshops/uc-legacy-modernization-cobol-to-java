      *****************************************************************
      * LOADKSDS - parity-harness loader (stands in for the IDCAMS
      * REPRO in app/jcl/ACCTFILE.jcl). Reads a line-sequential ASCII
      * fixture of 300-byte account records (INFILE) and writes
      *   KSDSFILE - GnuCOBOL indexed file keyed on bytes 1-11, the
      *              ACCTFILE DD of the original CBACT01C
      *   FLATFILE - the same records as a fixed 300-byte sequential
      *              unload, the ACCTFILE DD of the Java port
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. LOADKSDS.
       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT IN-FILE ASSIGN TO INFILE
                  ORGANIZATION IS LINE SEQUENTIAL
                  FILE STATUS IS IN-STATUS.
           SELECT KSDS-FILE ASSIGN TO KSDSFILE
                  ORGANIZATION IS INDEXED
                  ACCESS MODE IS RANDOM
                  RECORD KEY IS KSDS-KEY
                  FILE STATUS IS KSDS-STATUS.
           SELECT FLAT-FILE ASSIGN TO FLATFILE
                  ORGANIZATION IS SEQUENTIAL
                  FILE STATUS IS FLAT-STATUS.
       DATA DIVISION.
       FILE SECTION.
       FD  IN-FILE.
       01  IN-REC                  PIC X(300).
       FD  KSDS-FILE.
       01  KSDS-REC.
           05  KSDS-KEY            PIC X(11).
           05  KSDS-DATA           PIC X(289).
       FD  FLAT-FILE.
       01  FLAT-REC                PIC X(300).
       WORKING-STORAGE SECTION.
       01  IN-STATUS               PIC XX.
       01  KSDS-STATUS             PIC XX.
       01  FLAT-STATUS             PIC XX.
       01  REC-COUNT               PIC 9(9) VALUE 0.
       PROCEDURE DIVISION.
           OPEN INPUT IN-FILE OUTPUT KSDS-FILE FLAT-FILE
           IF IN-STATUS NOT = '00' OR KSDS-STATUS NOT = '00'
              OR FLAT-STATUS NOT = '00'
               DISPLAY 'LOADKSDS OPEN FAILED ' IN-STATUS ' '
                       KSDS-STATUS ' ' FLAT-STATUS
               MOVE 12 TO RETURN-CODE
               STOP RUN
           END-IF
           PERFORM UNTIL IN-STATUS NOT = '00'
               READ IN-FILE
               IF IN-STATUS = '00'
                   MOVE IN-REC TO KSDS-REC FLAT-REC
                   WRITE KSDS-REC
                   IF KSDS-STATUS NOT = '00'
                       DISPLAY 'LOADKSDS WRITE FAILED ' KSDS-STATUS
                               ' KEY ' KSDS-KEY
                       MOVE 12 TO RETURN-CODE
                       STOP RUN
                   END-IF
                   WRITE FLAT-REC
                   ADD 1 TO REC-COUNT
               END-IF
           END-PERFORM
           CLOSE IN-FILE KSDS-FILE FLAT-FILE
           DISPLAY 'LOADKSDS RECORDS LOADED: ' REC-COUNT
           GOBACK.
