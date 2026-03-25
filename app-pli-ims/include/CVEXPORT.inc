 /*********************************************************************/
 /* Multi-record export layout (RECLN 500)                            */
 /* Converted from COBOL copybook: app/cpy/CVEXPORT.cpy              */
 /*********************************************************************/

 DCL 1 EXPORT_RECORD,
       2 EXPORT_REC_TYPE             CHAR(1),
       2 EXPORT_TIMESTAMP            CHAR(26),
       2 EXPORT_SEQUENCE_NUM         FIXED BIN(31),
       2 EXPORT_BRANCH_ID            CHAR(4),
       2 EXPORT_REGION_CODE          CHAR(5),
       2 EXPORT_RECORD_DATA          CHAR(460);

 /* Customer export overlay */
 DCL 1 EXPORT_CUSTOMER_DATA BASED(ADDR(EXPORT_RECORD_DATA)),
       2 EXP_CUST_ID                 FIXED BIN(31),
       2 EXP_CUST_FIRST_NAME         CHAR(25),
       2 EXP_CUST_MIDDLE_NAME        CHAR(25),
       2 EXP_CUST_LAST_NAME          CHAR(25),
       2 EXP_CUST_ADDR_LINE(3)       CHAR(50),
       2 EXP_CUST_ADDR_STATE_CD      CHAR(2),
       2 EXP_CUST_ADDR_COUNTRY_CD    CHAR(3),
       2 EXP_CUST_ADDR_ZIP           CHAR(10),
       2 EXP_CUST_PHONE_NUM(2)       CHAR(15),
       2 EXP_CUST_SSN                PIC '(9)9',
       2 EXP_CUST_GOVT_ISSUED_ID     CHAR(20),
       2 EXP_CUST_DOB_YYYY_MM_DD     CHAR(10),
       2 EXP_CUST_EFT_ACCOUNT_ID     CHAR(10),
       2 EXP_CUST_PRI_CARD_HOLDER_IND CHAR(1),
       2 EXP_CUST_FICO               FIXED DEC(3),
       2 FILLER_EXP_CUST             CHAR(134);

 /* Account export overlay */
 DCL 1 EXPORT_ACCOUNT_DATA BASED(ADDR(EXPORT_RECORD_DATA)),
       2 EXP_ACCT_ID                 PIC '(11)9',
       2 EXP_ACCT_ACTIVE_STATUS      CHAR(1),
       2 EXP_ACCT_CURR_BAL           FIXED DEC(12,2),
       2 EXP_ACCT_CREDIT_LIMIT       FIXED DEC(12,2),
       2 EXP_ACCT_CASH_CREDIT_LIMIT  FIXED DEC(12,2),
       2 EXP_ACCT_OPEN_DATE          CHAR(10),
       2 EXP_ACCT_EXPIRAION_DATE     CHAR(10),
       2 EXP_ACCT_REISSUE_DATE       CHAR(10),
       2 EXP_ACCT_CURR_CYC_CREDIT    FIXED DEC(12,2),
       2 EXP_ACCT_CURR_CYC_DEBIT     FIXED BIN(31),
       2 EXP_ACCT_ADDR_ZIP           CHAR(10),
       2 EXP_ACCT_GROUP_ID           CHAR(10),
       2 FILLER_EXP_ACCT             CHAR(352);

 /* Transaction export overlay */
 DCL 1 EXPORT_TRANSACTION_DATA BASED(ADDR(EXPORT_RECORD_DATA)),
       2 EXP_TRAN_ID                 CHAR(16),
       2 EXP_TRAN_TYPE_CD            CHAR(2),
       2 EXP_TRAN_CAT_CD             PIC '(4)9',
       2 EXP_TRAN_SOURCE             CHAR(10),
       2 EXP_TRAN_DESC               CHAR(100),
       2 EXP_TRAN_AMT                FIXED DEC(11,2),
       2 EXP_TRAN_MERCHANT_ID        FIXED BIN(31),
       2 EXP_TRAN_MERCHANT_NAME      CHAR(50),
       2 EXP_TRAN_MERCHANT_CITY      CHAR(50),
       2 EXP_TRAN_MERCHANT_ZIP       CHAR(10),
       2 EXP_TRAN_CARD_NUM           CHAR(16),
       2 EXP_TRAN_ORIG_TS            CHAR(26),
       2 EXP_TRAN_PROC_TS            CHAR(26),
       2 FILLER_EXP_TRAN             CHAR(140);

 /* Card cross-reference export overlay */
 DCL 1 EXPORT_CARD_XREF_DATA BASED(ADDR(EXPORT_RECORD_DATA)),
       2 EXP_XREF_CARD_NUM           CHAR(16),
       2 EXP_XREF_CUST_ID            PIC '(9)9',
       2 EXP_XREF_ACCT_ID            FIXED BIN(31),
       2 FILLER_EXP_XREF             CHAR(427);

 /* Card record export overlay */
 DCL 1 EXPORT_CARD_DATA BASED(ADDR(EXPORT_RECORD_DATA)),
       2 EXP_CARD_NUM                CHAR(16),
       2 EXP_CARD_ACCT_ID            FIXED BIN(31),
       2 EXP_CARD_CVV_CD             FIXED BIN(15),
       2 EXP_CARD_EMBOSSED_NAME      CHAR(50),
       2 EXP_CARD_EXPIRAION_DATE     CHAR(10),
       2 EXP_CARD_ACTIVE_STATUS      CHAR(1),
       2 FILLER_EXP_CARD             CHAR(373);
