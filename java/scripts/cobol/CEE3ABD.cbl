      ******************************************************************
      * CEE3ABD - parity-harness stub for the z/OS Language Environment
      * abend service, which GnuCOBOL does not provide. Terminates the
      * run unit with the requested abend code as the process exit code
      * (POSIX keeps the low 8 bits: 999 -> 231).
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID.    CEE3ABD.
       DATA DIVISION.
       LINKAGE SECTION.
       01  ABCODE                   PIC S9(9) BINARY.
       01  TIMING                   PIC S9(9) BINARY.
       PROCEDURE DIVISION USING ABCODE, TIMING.
           STOP RUN RETURNING ABCODE.
