      ******************************************************************
      * Shared copybook for RETURN-TO-PREV-SCREEN paragraph.
      * Transfers control back to the calling program via XCTL.
      * Defaults to COSGN00C if no target program is set.
      *
      * Usage: COPY CSRTN01Y.
      * Requires: WS-TRANID, WS-PGMNAME, CARDDEMO-COMMAREA (COCOM01Y)
      ******************************************************************
       RETURN-TO-PREV-SCREEN.

           IF CDEMO-TO-PROGRAM = LOW-VALUES OR SPACES
               MOVE 'COSGN00C' TO CDEMO-TO-PROGRAM
           END-IF
           MOVE WS-TRANID    TO CDEMO-FROM-TRANID
           MOVE WS-PGMNAME   TO CDEMO-FROM-PROGRAM
           MOVE ZEROS        TO CDEMO-PGM-CONTEXT
           EXEC CICS
               XCTL PROGRAM(CDEMO-TO-PROGRAM)
               COMMAREA(CARDDEMO-COMMAREA)
           END-EXEC.
