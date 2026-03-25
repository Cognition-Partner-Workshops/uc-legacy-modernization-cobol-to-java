 /*********************************************************************/
 /* Data structure for transaction category balance (RECLN 50)        */
 /* Converted from COBOL copybook: app/cpy/CVTRA01Y.cpy              */
 /*********************************************************************/

 DCL 1 TRAN_CAT_BAL_RECORD,
       2 TRAN_CAT_KEY,
         3 TRANCAT_ACCT_ID          PIC '(11)9',
         3 TRANCAT_TYPE_CD          CHAR(2),
         3 TRANCAT_CD               PIC '(4)9',
       2 TRAN_CAT_BAL               FIXED DEC(11,2),
       2 FILLER_TRANCAT             CHAR(22);
