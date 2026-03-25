 /*********************************************************************/
 /* Data structure for disclosure group (RECLN 50)                    */
 /* Converted from COBOL copybook: app/cpy/CVTRA02Y.cpy              */
 /*********************************************************************/

 DCL 1 DIS_GROUP_RECORD,
       2 DIS_GROUP_KEY,
         3 DIS_ACCT_GROUP_ID        CHAR(10),
         3 DIS_TRAN_TYPE_CD         CHAR(2),
         3 DIS_TRAN_CAT_CD          PIC '(4)9',
       2 DIS_INT_RATE               FIXED DEC(6,2),
       2 FILLER_DIS                 CHAR(28);
