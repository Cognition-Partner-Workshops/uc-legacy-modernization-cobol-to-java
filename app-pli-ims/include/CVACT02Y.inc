 /*********************************************************************/
 /* Data structure for card entity (RECLN 150)                        */
 /* Converted from COBOL copybook: app/cpy/CVACT02Y.cpy              */
 /*********************************************************************/

 DCL 1 CARD_RECORD,
       2 CARD_NUM                   CHAR(16),
       2 CARD_ACCT_ID               PIC '(11)9',
       2 CARD_CVV_CD                PIC '(3)9',
       2 CARD_EMBOSSED_NAME         CHAR(50),
       2 CARD_EXPIRAION_DATE        CHAR(10),
       2 CARD_ACTIVE_STATUS         CHAR(1),
       2 FILLER_CARD                CHAR(59);
