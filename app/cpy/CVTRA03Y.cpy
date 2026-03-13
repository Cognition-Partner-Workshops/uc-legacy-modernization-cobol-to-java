      *****************************************************************         
      * Transaction type record (TRANTYPE file, RECLN = 60).
      * Key: 2-char type code (e.g., 'SA' = sale, 'RE' = return).
      * Stores the type code and its 50-char description.
      * Used for type lookups in reporting (CBTRN03C) and
      * transaction display screens (COTRN00C/01C).
      *****************************************************************         
       01  TRAN-TYPE-RECORD.                                                    
           05  TRAN-TYPE                               PIC X(02).               
           05  TRAN-TYPE-DESC                          PIC X(50).               
           05  FILLER                                  PIC X(08).               
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:16:00 CDT
      *
