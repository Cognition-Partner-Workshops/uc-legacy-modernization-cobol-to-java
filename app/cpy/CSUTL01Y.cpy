      ******************************************************************
      * Shared copybook for CSUTLDTC date utility parameter structure.
      * Used when calling the CSUTLDTC date validation subroutine.
      *
      * Usage: COPY CSUTL01Y.
      * Place in WORKING-STORAGE SECTION at the 01 level.
      ******************************************************************
       01 CSUTLDTC-PARM.
          05 CSUTLDTC-DATE                   PIC X(10).
          05 CSUTLDTC-DATE-FORMAT            PIC X(10).
          05 CSUTLDTC-RESULT.
             10 CSUTLDTC-RESULT-SEV-CD       PIC X(04).
             10 FILLER                       PIC X(11).
             10 CSUTLDTC-RESULT-MSG-NUM      PIC X(04).
             10 CSUTLDTC-RESULT-MSG          PIC X(61).
