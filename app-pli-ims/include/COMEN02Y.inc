 /*********************************************************************/
 /* Main Menu Options                                                 */
 /* Converted from COBOL copybook: app/cpy/COMEN02Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL CDEMO_MENU_OPT_COUNT   FIXED BIN(15) INIT(11);

 DCL 1 CDEMO_MENU_OPT(12),
       2 CDEMO_MENU_OPT_NUM      PIC '99',
       2 CDEMO_MENU_OPT_NAME     CHAR(35),
       2 CDEMO_MENU_OPT_PGMNAME  CHAR(8),
       2 CDEMO_MENU_OPT_USRTYPE  CHAR(1);

 /* Menu option initialization procedure:
    CDEMO_MENU_OPT(1)  = ('01','Account View                       ','COACTVWC','U');
    CDEMO_MENU_OPT(2)  = ('02','Account Update                     ','COACTUPC','U');
    CDEMO_MENU_OPT(3)  = ('03','Credit Card List                   ','COCRDLIC','U');
    CDEMO_MENU_OPT(4)  = ('04','Credit Card View                   ','COCRDSLC','U');
    CDEMO_MENU_OPT(5)  = ('05','Credit Card Update                 ','COCRDUPC','U');
    CDEMO_MENU_OPT(6)  = ('06','Transaction List                   ','COTRN00C','U');
    CDEMO_MENU_OPT(7)  = ('07','Transaction View                   ','COTRN01C','U');
    CDEMO_MENU_OPT(8)  = ('08','Transaction Add                    ','COTRN02C','U');
    CDEMO_MENU_OPT(9)  = ('09','Transaction Reports                ','CORPT00C','U');
    CDEMO_MENU_OPT(10) = ('10','Bill Payment                       ','COBIL00C','U');
    CDEMO_MENU_OPT(11) = ('11','Pending Authorization View         ','COPAUS0C','U');
 */
