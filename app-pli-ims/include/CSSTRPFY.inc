 /*********************************************************************/
 /* Store PF Key - Maps IMS input key to common area key code         */
 /* Converted from COBOL copybook: app/cpy/CSSTRPFY.cpy              */
 /* In IMS, the function key is communicated via the MFS MID          */
 /* rather than via EIBAID. This procedure maps IMS key values        */
 /* to the common CCARD_AID field for application logic.              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 STORE_PFKEY: PROC(IMS_KEY);
   DCL IMS_KEY CHAR(5);

   SELECT(IMS_KEY);
     WHEN('ENTER') CCARD_AID = CCARD_AID_ENTER;
     WHEN('CLEAR') CCARD_AID = CCARD_AID_CLEAR;
     WHEN('PA1  ') CCARD_AID = CCARD_AID_PA1;
     WHEN('PA2  ') CCARD_AID = CCARD_AID_PA2;
     WHEN('PFK01') CCARD_AID = CCARD_AID_PFK01;
     WHEN('PFK02') CCARD_AID = CCARD_AID_PFK02;
     WHEN('PFK03') CCARD_AID = CCARD_AID_PFK03;
     WHEN('PFK04') CCARD_AID = CCARD_AID_PFK04;
     WHEN('PFK05') CCARD_AID = CCARD_AID_PFK05;
     WHEN('PFK06') CCARD_AID = CCARD_AID_PFK06;
     WHEN('PFK07') CCARD_AID = CCARD_AID_PFK07;
     WHEN('PFK08') CCARD_AID = CCARD_AID_PFK08;
     WHEN('PFK09') CCARD_AID = CCARD_AID_PFK09;
     WHEN('PFK10') CCARD_AID = CCARD_AID_PFK10;
     WHEN('PFK11') CCARD_AID = CCARD_AID_PFK11;
     WHEN('PFK12') CCARD_AID = CCARD_AID_PFK12;
     WHEN('PFK13') CCARD_AID = CCARD_AID_PFK01;
     WHEN('PFK14') CCARD_AID = CCARD_AID_PFK02;
     WHEN('PFK15') CCARD_AID = CCARD_AID_PFK03;
     WHEN('PFK16') CCARD_AID = CCARD_AID_PFK04;
     WHEN('PFK17') CCARD_AID = CCARD_AID_PFK05;
     WHEN('PFK18') CCARD_AID = CCARD_AID_PFK06;
     WHEN('PFK19') CCARD_AID = CCARD_AID_PFK07;
     WHEN('PFK20') CCARD_AID = CCARD_AID_PFK08;
     WHEN('PFK21') CCARD_AID = CCARD_AID_PFK09;
     WHEN('PFK22') CCARD_AID = CCARD_AID_PFK10;
     WHEN('PFK23') CCARD_AID = CCARD_AID_PFK11;
     WHEN('PFK24') CCARD_AID = CCARD_AID_PFK12;
     OTHERWISE ;
   END;
 END STORE_PFKEY;
