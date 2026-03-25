 /*********************************************************************/
 /* Admin Menu Options                                                */
 /* Converted from COBOL copybook: app/cpy/COADM02Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL CDEMO_ADMIN_OPT_COUNT   FIXED BIN(15) INIT(6);

 DCL 1 CDEMO_ADMIN_OPT(9),
       2 CDEMO_ADMIN_OPT_NUM      PIC '99',
       2 CDEMO_ADMIN_OPT_NAME     CHAR(35),
       2 CDEMO_ADMIN_OPT_PGMNAME  CHAR(8);

 /* Admin option initialization procedure:
    CDEMO_ADMIN_OPT(1) = ('01','User List (Security)               ','COUSR00C');
    CDEMO_ADMIN_OPT(2) = ('02','User Add (Security)                ','COUSR01C');
    CDEMO_ADMIN_OPT(3) = ('03','User Update (Security)             ','COUSR02C');
    CDEMO_ADMIN_OPT(4) = ('04','User Delete (Security)             ','COUSR03C');
    CDEMO_ADMIN_OPT(5) = ('05','Transaction Type List/Update (Db2) ','COTRTLIC');
    CDEMO_ADMIN_OPT(6) = ('06','Transaction Type Maintenance (Db2) ','COTRTUPC');
 */
