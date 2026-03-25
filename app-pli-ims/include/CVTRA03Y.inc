 /*********************************************************************/
 /* Data structure for transaction type (RECLN 60)                    */
 /* Converted from COBOL copybook: app/cpy/CVTRA03Y.cpy              */
 /*********************************************************************/

 DCL 1 TRAN_TYPE_RECORD,
       2 TRAN_TYPE                  CHAR(2),
       2 TRAN_TYPE_DESC             CHAR(50),
       2 FILLER_TRANTYPE            CHAR(8);
