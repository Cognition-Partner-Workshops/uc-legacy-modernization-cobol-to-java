      *****************************************************************         
      * Transaction record layout (TRANSACT file, RECLN = 350).
      * Key: TRAN-ID (16-char generated ID). Contains type code,
      * category, source, free-text description, signed amount,
      * merchant details, card number, and origination/processing
      * timestamps. Used by COTRN00C/01C/02C online screens and
      * CBTRN01C/02C/03C batch posting and reporting programs.
      *****************************************************************         
       01  TRAN-RECORD.                                                         
           05  TRAN-ID                                 PIC X(16).               
           05  TRAN-TYPE-CD                            PIC X(02).               
           05  TRAN-CAT-CD                             PIC 9(04).               
           05  TRAN-SOURCE                             PIC X(10).               
           05  TRAN-DESC                               PIC X(100).              
           05  TRAN-AMT                                PIC S9(09)V99.           
           05  TRAN-MERCHANT-ID                        PIC 9(09).               
           05  TRAN-MERCHANT-NAME                      PIC X(50).               
           05  TRAN-MERCHANT-CITY                      PIC X(50).               
           05  TRAN-MERCHANT-ZIP                       PIC X(10).               
           05  TRAN-CARD-NUM                           PIC X(16).               
           05  TRAN-ORIG-TS                            PIC X(26).               
           05  TRAN-PROC-TS                            PIC X(26).               
           05  FILLER                                  PIC X(20).               
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:16:01 CDT
      *
