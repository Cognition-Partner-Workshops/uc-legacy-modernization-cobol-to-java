 /*********************************************************************/
 /* Data structure for transaction category type (RECLN 60)           */
 /* Converted from COBOL copybook: app/cpy/CVTRA04Y.cpy              */
 /*********************************************************************/

 DCL 1 TRAN_CAT_RECORD,
       2 TRAN_CAT_KEY,
         3 TRAN_TYPE_CD             CHAR(2),
         3 TRAN_CAT_CD              PIC '(4)9',
       2 TRAN_CAT_TYPE_DESC         CHAR(50),
       2 FILLER_TRANCAT_TYPE        CHAR(4);
