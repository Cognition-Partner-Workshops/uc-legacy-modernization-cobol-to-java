 /*********************************************************************/
 /* Data structure for transaction record (RECLN 350)                 */
 /* Converted from COBOL copybook: app/cpy/CVTRA05Y.cpy              */
 /*********************************************************************/

 DCL 1 TRAN_RECORD,
       2 TRAN_ID                        CHAR(16),
       2 TRAN_TYPE_CD                   CHAR(2),
       2 TRAN_CAT_CD                    PIC '(4)9',
       2 TRAN_SOURCE                    CHAR(10),
       2 TRAN_DESC                      CHAR(100),
       2 TRAN_AMT                       FIXED DEC(11,2),
       2 TRAN_MERCHANT_ID               PIC '(9)9',
       2 TRAN_MERCHANT_NAME             CHAR(50),
       2 TRAN_MERCHANT_CITY             CHAR(50),
       2 TRAN_MERCHANT_ZIP              CHAR(10),
       2 TRAN_CARD_NUM                  CHAR(16),
       2 TRAN_ORIG_TS                   CHAR(26),
       2 TRAN_PROC_TS                   CHAR(26),
       2 FILLER_TRAN                    CHAR(20);
