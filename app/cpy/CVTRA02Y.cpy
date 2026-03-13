      *****************************************************************         
      * Disclosure group record (DISCGRP file, RECLN = 50).
      * Key: account group ID + transaction type + category code.
      * Stores the interest rate applicable to a specific
      * transaction category for an account group. Used by
      * CBACT04C to look up rates during interest calculation.
      *****************************************************************         
       01  DIS-GROUP-RECORD.                                                    
           05  DIS-GROUP-KEY.                                                   
              10 DIS-ACCT-GROUP-ID                     PIC X(10).               
              10 DIS-TRAN-TYPE-CD                      PIC X(02).               
              10 DIS-TRAN-CAT-CD                       PIC 9(04).               
           05  DIS-INT-RATE                            PIC S9(04)V99.           
           05  FILLER                                  PIC X(28).               
      *
      * Ver: CardDemo_v1.0-15-g27d6c6f-68 Date: 2022-07-19 23:16:00 CDT
      *
