 /*********************************************************************/
 /* Data structure for account entity (RECLN 300)                     */
 /* Converted from COBOL copybook: app/cpy/CVACT01Y.cpy              */
 /*********************************************************************/

 DCL 1 ACCOUNT_RECORD,
       2 ACCT_ID                    PIC '(11)9',
       2 ACCT_ACTIVE_STATUS         CHAR(1),
       2 ACCT_CURR_BAL              FIXED DEC(12,2),
       2 ACCT_CREDIT_LIMIT          FIXED DEC(12,2),
       2 ACCT_CASH_CREDIT_LIMIT     FIXED DEC(12,2),
       2 ACCT_OPEN_DATE             CHAR(10),
       2 ACCT_EXPIRAION_DATE        CHAR(10),
       2 ACCT_REISSUE_DATE          CHAR(10),
       2 ACCT_CURR_CYC_CREDIT       FIXED DEC(12,2),
       2 ACCT_CURR_CYC_DEBIT        FIXED DEC(12,2),
       2 ACCT_ADDR_ZIP              CHAR(10),
       2 ACCT_GROUP_ID              CHAR(10),
       2 FILLER_ACCT                CHAR(178);
