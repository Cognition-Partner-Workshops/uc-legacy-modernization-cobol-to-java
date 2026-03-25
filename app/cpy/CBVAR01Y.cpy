      ******************************************************************
      * Shared copybook for common batch program working storage
      * variables: IO status, application result codes, end-of-file
      * flag, abend/timing fields, and DB2 timestamp structure.
      *
      * Usage: COPY CBVAR01Y.
      * Place in WORKING-STORAGE SECTION after file status variables.
      ******************************************************************
       01  IO-STATUS.
           05  IO-STAT1            PIC X.
           05  IO-STAT2            PIC X.
       01  TWO-BYTES-BINARY        PIC 9(4) BINARY.
       01  TWO-BYTES-ALPHA         REDEFINES TWO-BYTES-BINARY.
           05  TWO-BYTES-LEFT      PIC X.
           05  TWO-BYTES-RIGHT     PIC X.
       01  IO-STATUS-04.
           05  IO-STATUS-0401      PIC 9   VALUE 0.
           05  IO-STATUS-0403      PIC 999 VALUE 0.

       01  APPL-RESULT             PIC S9(9)   COMP.
           88  APPL-AOK            VALUE 0.
           88  APPL-EOF            VALUE 16.

       01  END-OF-FILE             PIC X(01)    VALUE 'N'.
       01  ABCODE                  PIC S9(9) BINARY.
       01  TIMING                  PIC S9(9) BINARY.
      * T I M E S T A M P   D B 2  X(26)   EEEE-MM-DD-UU.MM.SS.HH0000
       01  COBOL-TS.
           05 COB-YYYY                  PIC X(04).
           05 COB-MM                    PIC X(02).
           05 COB-DD                    PIC X(02).
           05 COB-HH                    PIC X(02).
           05 COB-MIN                   PIC X(02).
           05 COB-SS                    PIC X(02).
           05 COB-MIL                   PIC X(02).
           05 COB-REST                  PIC X(05).
       01  DB2-FORMAT-TS                PIC X(26).
       01  FILLER REDEFINES DB2-FORMAT-TS.
           06 DB2-YYYY                  PIC X(004).
           06 DB2-STREEP-1              PIC X.
           06 DB2-MM                    PIC X(002).
           06 DB2-STREEP-2              PIC X.
           06 DB2-DD                    PIC X(002).
           06 DB2-STREEP-3              PIC X.
           06 DB2-HH                    PIC X(002).
           06 DB2-DOT-1                 PIC X.
           06 DB2-MIN                   PIC X(002).
           06 DB2-DOT-2                 PIC X.
           06 DB2-SS                    PIC X(002).
           06 DB2-DOT-3                 PIC X.
           06 DB2-MIL                   PIC 9(002).
           06 DB2-REST                  PIC X(04).
