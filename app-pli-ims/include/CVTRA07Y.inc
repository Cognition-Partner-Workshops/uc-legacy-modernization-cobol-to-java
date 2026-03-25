 /*********************************************************************/
 /* Reporting data structures for transaction report                  */
 /* Converted from COBOL copybook: app/cpy/CVTRA07Y.cpy              */
 /*********************************************************************/

 DCL 1 REPORT_NAME_HEADER,
       2 REPT_SHORT_NAME           CHAR(38) INIT('DALYREPT'),
       2 REPT_LONG_NAME            CHAR(41)
         INIT('Daily Transaction Report'),
       2 REPT_DATE_HEADER          CHAR(12) INIT('Date Range: '),
       2 REPT_START_DATE           CHAR(10) INIT(' '),
       2 FILLER_RPT_TO             CHAR(4)  INIT(' to '),
       2 REPT_END_DATE             CHAR(10) INIT(' ');

 DCL 1 TRANSACTION_DETAIL_REPORT,
       2 TRAN_REPORT_TRANS_ID      CHAR(16),
       2 FILLER_RPT1               CHAR(1) INIT(' '),
       2 TRAN_REPORT_ACCOUNT_ID    CHAR(11),
       2 FILLER_RPT2               CHAR(1) INIT(' '),
       2 TRAN_REPORT_TYPE_CD       CHAR(2),
       2 FILLER_RPT3               CHAR(1) INIT('-'),
       2 TRAN_REPORT_TYPE_DESC     CHAR(15),
       2 FILLER_RPT4               CHAR(1) INIT(' '),
       2 TRAN_REPORT_CAT_CD        PIC '(4)9',
       2 FILLER_RPT5               CHAR(1) INIT('-'),
       2 TRAN_REPORT_CAT_DESC      CHAR(29),
       2 FILLER_RPT6               CHAR(1) INIT(' '),
       2 TRAN_REPORT_SOURCE        CHAR(10),
       2 FILLER_RPT7               CHAR(4) INIT(' '),
       2 TRAN_REPORT_AMT           PIC '-ZZZ,ZZZ,ZZ9V.99',
       2 FILLER_RPT8               CHAR(2) INIT(' ');

 DCL TRANSACTION_HEADER_1 CHAR(114)
     INIT('Transaction ID   Account ID  Transaction Type   Tran Category                        Tran Source          Amount');

 DCL TRANSACTION_HEADER_2 CHAR(133) INIT((133)'-');

 DCL 1 REPORT_PAGE_TOTALS,
       2 FILLER_PGT1               CHAR(11) INIT('Page Total'),
       2 FILLER_PGT2               CHAR(86) INIT((86)'.'),
       2 REPT_PAGE_TOTAL           PIC '+ZZZ,ZZZ,ZZ9V.99';

 DCL 1 REPORT_ACCOUNT_TOTALS,
       2 FILLER_ACT1               CHAR(13) INIT('Account Total'),
       2 FILLER_ACT2               CHAR(84) INIT((84)'.'),
       2 REPT_ACCOUNT_TOTAL        PIC '+ZZZ,ZZZ,ZZ9V.99';

 DCL 1 REPORT_GRAND_TOTALS,
       2 FILLER_GRT1               CHAR(11) INIT('Grand Total'),
       2 FILLER_GRT2               CHAR(86) INIT((86)'.'),
       2 REPT_GRAND_TOTAL          PIC '+ZZZ,ZZZ,ZZ9V.99';
