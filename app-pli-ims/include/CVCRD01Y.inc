 /*********************************************************************/
 /* Card work areas and AID key definitions                           */
 /* Converted from COBOL copybook: app/cpy/CVCRD01Y.cpy              */
 /*********************************************************************/

 DCL 1 CC_WORK_AREAS,
       2 CC_WORK_AREA,
         3 CCARD_AID                CHAR(5),
         3 CCARD_NEXT_PROG          CHAR(8),
         3 CCARD_NEXT_MAPSET        CHAR(7),
         3 CCARD_NEXT_MAP           CHAR(7),
         3 CCARD_ERROR_MSG          CHAR(75),
         3 CCARD_RETURN_MSG         CHAR(75),
         3 CC_ACCT_ID               CHAR(11) INIT(' '),
         3 CC_CARD_NUM              CHAR(16) INIT(' '),
         3 CC_CUST_ID               CHAR(9)  INIT(' ');

 /* AID key constants */
 DCL CCARD_AID_ENTER    CHAR(5) INIT('ENTER');
 DCL CCARD_AID_CLEAR    CHAR(5) INIT('CLEAR');
 DCL CCARD_AID_PA1      CHAR(5) INIT('PA1  ');
 DCL CCARD_AID_PA2      CHAR(5) INIT('PA2  ');
 DCL CCARD_AID_PFK01    CHAR(5) INIT('PFK01');
 DCL CCARD_AID_PFK02    CHAR(5) INIT('PFK02');
 DCL CCARD_AID_PFK03    CHAR(5) INIT('PFK03');
 DCL CCARD_AID_PFK04    CHAR(5) INIT('PFK04');
 DCL CCARD_AID_PFK05    CHAR(5) INIT('PFK05');
 DCL CCARD_AID_PFK06    CHAR(5) INIT('PFK06');
 DCL CCARD_AID_PFK07    CHAR(5) INIT('PFK07');
 DCL CCARD_AID_PFK08    CHAR(5) INIT('PFK08');
 DCL CCARD_AID_PFK09    CHAR(5) INIT('PFK09');
 DCL CCARD_AID_PFK10    CHAR(5) INIT('PFK10');
 DCL CCARD_AID_PFK11    CHAR(5) INIT('PFK11');
 DCL CCARD_AID_PFK12    CHAR(5) INIT('PFK12');
