 /*********************************************************************/
 /* Transaction altered layout for reporting (statement processing)   */
 /* Converted from COBOL copybook: app/cpy/COSTM01.CPY               */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 TRNX_RECORD,
       2 TRNX_KEY,
         3 TRNX_CARD_NUM           CHAR(16),
         3 TRNX_ID                 CHAR(16),
       2 TRNX_REST,
         3 TRNX_TYPE_CD            CHAR(2),
         3 TRNX_CAT_CD             PIC '(4)9',
         3 TRNX_SOURCE             CHAR(10),
         3 TRNX_DESC               CHAR(100),
         3 TRNX_AMT                FIXED DEC(11,2),
         3 TRNX_MERCHANT_ID        PIC '(9)9',
         3 TRNX_MERCHANT_NAME      CHAR(50),
         3 TRNX_MERCHANT_CITY      CHAR(50),
         3 TRNX_MERCHANT_ZIP       CHAR(10),
         3 TRNX_ORIG_TS            CHAR(26),
         3 TRNX_PROC_TS            CHAR(26),
         3 FILLER_TRNX             CHAR(20);
