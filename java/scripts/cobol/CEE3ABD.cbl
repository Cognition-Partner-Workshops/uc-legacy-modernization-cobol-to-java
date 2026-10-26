      *****************************************************************
      * CEE3ABD - GnuCOBOL stand-in for the Language Environment abend
      * service, used only by java/scripts/run-parity.sh. Ends the run
      * unit with RETURN-CODE = abend code (999 in CardDemo), which the
      * Java port mirrors as its process exit status.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. CEE3ABD.
       DATA DIVISION.
       LINKAGE SECTION.
       01  ABCODE                  PIC S9(9) BINARY.
       01  TIMING                  PIC S9(9) BINARY.
       PROCEDURE DIVISION USING ABCODE TIMING.
           MOVE ABCODE TO RETURN-CODE
           STOP RUN.
