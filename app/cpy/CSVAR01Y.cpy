      ******************************************************************
      * Shared copybook for common working storage variables used
      * across all online CICS programs.
      *
      * Usage: COPY CSVAR01Y.
      * Place within the 01 WS-VARIABLES group after program-specific
      * fields (WS-PGMNAME, WS-TRANID, etc.).
      ******************************************************************
         05 WS-MESSAGE                 PIC X(80) VALUE SPACES.
         05 WS-ERR-FLG                 PIC X(01) VALUE 'N'.
           88 ERR-FLG-ON                         VALUE 'Y'.
           88 ERR-FLG-OFF                        VALUE 'N'.
         05 WS-RESP-CD                 PIC S9(09) COMP VALUE ZEROS.
         05 WS-REAS-CD                 PIC S9(09) COMP VALUE ZEROS.
