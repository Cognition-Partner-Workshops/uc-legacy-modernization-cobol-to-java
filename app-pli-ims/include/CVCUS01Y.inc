 /*********************************************************************/
 /* Data structure for Customer entity (RECLN 500)                    */
 /* Converted from COBOL copybook: app/cpy/CVCUS01Y.cpy              */
 /* Note: CUSTREC.cpy was a duplicate - consolidated here             */
 /*********************************************************************/

 DCL 1 CUSTOMER_RECORD,
       2 CUST_ID                        PIC '(9)9',
       2 CUST_FIRST_NAME                CHAR(25),
       2 CUST_MIDDLE_NAME               CHAR(25),
       2 CUST_LAST_NAME                 CHAR(25),
       2 CUST_ADDR_LINE_1               CHAR(50),
       2 CUST_ADDR_LINE_2               CHAR(50),
       2 CUST_ADDR_LINE_3               CHAR(50),
       2 CUST_ADDR_STATE_CD             CHAR(2),
       2 CUST_ADDR_COUNTRY_CD           CHAR(3),
       2 CUST_ADDR_ZIP                  CHAR(10),
       2 CUST_PHONE_NUM_1               CHAR(15),
       2 CUST_PHONE_NUM_2               CHAR(15),
       2 CUST_SSN                        PIC '(9)9',
       2 CUST_GOVT_ISSUED_ID            CHAR(20),
       2 CUST_DOB_YYYY_MM_DD            CHAR(10),
       2 CUST_EFT_ACCOUNT_ID            CHAR(10),
       2 CUST_PRI_CARD_HOLDER_IND       CHAR(1),
       2 CUST_FICO_CREDIT_SCORE         PIC '(3)9',
       2 FILLER_CUST                    CHAR(168);
