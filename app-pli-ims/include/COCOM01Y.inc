 /*********************************************************************/
 /* Communication area for CardDemo application programs              */
 /* Converted from COBOL copybook: app/cpy/COCOM01Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 CARDDEMO_COMMAREA,
       2 CDEMO_GENERAL_INFO,
         3 CDEMO_FROM_TRANID        CHAR(4),
         3 CDEMO_FROM_PROGRAM       CHAR(8),
         3 CDEMO_TO_TRANID          CHAR(4),
         3 CDEMO_TO_PROGRAM         CHAR(8),
         3 CDEMO_USER_ID            CHAR(8),
         3 CDEMO_USER_TYPE          CHAR(1),
         3 CDEMO_PGM_CONTEXT        PIC '9',
       2 CDEMO_CUSTOMER_INFO,
         3 CDEMO_CUST_ID            PIC '(9)9',
         3 CDEMO_CUST_FNAME         CHAR(25),
         3 CDEMO_CUST_MNAME         CHAR(25),
         3 CDEMO_CUST_LNAME         CHAR(25),
       2 CDEMO_ACCOUNT_INFO,
         3 CDEMO_ACCT_ID            PIC '(11)9',
         3 CDEMO_ACCT_STATUS        CHAR(1),
       2 CDEMO_CARD_INFO,
         3 CDEMO_CARD_NUM           PIC '(16)9',
       2 CDEMO_MORE_INFO,
         3 CDEMO_LAST_MAP           CHAR(7),
         3 CDEMO_LAST_MAPSET        CHAR(7);

 /* Constants for user type */
 DCL CDEMO_USRTYP_ADMIN     CHAR(1) INIT('A');
 DCL CDEMO_USRTYP_USER      CHAR(1) INIT('U');

 /* Constants for program context */
 DCL CDEMO_PGM_ENTER        FIXED BIN(15) INIT(0);
 DCL CDEMO_PGM_REENTER      FIXED BIN(15) INIT(1);
