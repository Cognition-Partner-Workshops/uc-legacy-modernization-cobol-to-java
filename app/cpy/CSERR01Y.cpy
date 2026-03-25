      ******************************************************************
      * Shared copybook for CICS file error message structure.
      * Used in working storage to format file operation error messages.
      *
      * Usage: COPY CSERR01Y.
      * Place within a group-level item at the 05 level.
      ******************************************************************
         05  WS-FILE-ERROR-MESSAGE.
           10  FILLER                              PIC X(12)
                                                   VALUE 'File Error: '.
           10  ERROR-OPNAME                        PIC X(8)
                                                   VALUE SPACES.
           10  FILLER                              PIC X(4)
                                                   VALUE ' on '.
           10  ERROR-FILE                          PIC X(9)
                                                   VALUE SPACES.
           10  FILLER                              PIC X(15)
                                                   VALUE
                                                   ' returned RESP '.
           10  ERROR-RESP                          PIC X(10)
                                                   VALUE SPACES.
           10  FILLER                              PIC X(7)
                                                   VALUE ',RESP2 '.
           10  ERROR-RESP2                         PIC X(10)
                                                   VALUE SPACES.
           10  FILLER                              PIC X(5)
                                                   VALUE SPACES.
