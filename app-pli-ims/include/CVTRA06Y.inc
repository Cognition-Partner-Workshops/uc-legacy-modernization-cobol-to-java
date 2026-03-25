 /*********************************************************************/
 /* Data structure for daily transaction record (RECLN 350)           */
 /* Converted from COBOL copybook: app/cpy/CVTRA06Y.cpy              */
 /* Note: Structure mirrors CVTRA05Y (TRAN_RECORD) with DALYTRAN     */
 /* prefix. Consider using CVTRA05Y.inc directly if no distinction    */
 /* is needed between online and daily transaction records.           */
 /*********************************************************************/

 DCL 1 DALYTRAN_RECORD,
       2 DALYTRAN_ID                    CHAR(16),
       2 DALYTRAN_TYPE_CD               CHAR(2),
       2 DALYTRAN_CAT_CD                PIC '(4)9',
       2 DALYTRAN_SOURCE                CHAR(10),
       2 DALYTRAN_DESC                  CHAR(100),
       2 DALYTRAN_AMT                   FIXED DEC(11,2),
       2 DALYTRAN_MERCHANT_ID           PIC '(9)9',
       2 DALYTRAN_MERCHANT_NAME         CHAR(50),
       2 DALYTRAN_MERCHANT_CITY         CHAR(50),
       2 DALYTRAN_MERCHANT_ZIP          CHAR(10),
       2 DALYTRAN_CARD_NUM              CHAR(16),
       2 DALYTRAN_ORIG_TS               CHAR(26),
       2 DALYTRAN_PROC_TS               CHAR(26),
       2 FILLER_DALYTRAN                CHAR(20);
