 /*********************************************************************/
 /* Working storage for DATE related code                             */
 /* Converted from COBOL copybook: app/cpy/CSUTLDWY.cpy              */
 /*********************************************************************/

 DCL 1 WS_EDIT_DATE_AREA,
       2 WS_EDIT_DATE_CCYYMMDD,
         3 WS_EDIT_DATE_CCYY,
           4 WS_EDIT_DATE_CC        CHAR(2),
           4 WS_EDIT_DATE_YY        CHAR(2),
         3 WS_EDIT_DATE_MM          CHAR(2),
         3 WS_EDIT_DATE_DD          CHAR(2),
       2 WS_EDIT_DATE_BINARY        FIXED BIN(31),
       2 WS_CURRENT_DATE,
         3 WS_CURRENT_DATE_YYYYMMDD CHAR(8),
         3 WS_CURRENT_DATE_BINARY   FIXED BIN(31),
       2 WS_EDIT_DATE_FLGS          CHAR(3),
       2 WS_EDIT_YEAR_FLG           CHAR(1),
       2 WS_EDIT_MONTH_FLG          CHAR(1),
       2 WS_EDIT_DAY_FLG            CHAR(1),
       2 WS_DATE_FORMAT             CHAR(8) INIT('YYYYMMDD'),
       2 WS_DATE_VALIDATION_RESULT,
         3 WS_SEVERITY              CHAR(4),
         3 WS_MSG_NO                CHAR(4),
         3 WS_RESULT                CHAR(15),
         3 WS_DATE                  CHAR(10),
         3 WS_DATE_FMT              CHAR(10);

 /* Numeric redefinitions */
 DCL WS_EDIT_DATE_CC_N      FIXED BIN(15) BASED(ADDR(WS_EDIT_DATE_CC));
 DCL WS_EDIT_DATE_YY_N      FIXED BIN(15) BASED(ADDR(WS_EDIT_DATE_YY));
 DCL WS_EDIT_DATE_CCYY_N    FIXED BIN(15) BASED(ADDR(WS_EDIT_DATE_CCYY));
 DCL WS_EDIT_DATE_MM_N       FIXED BIN(15) BASED(ADDR(WS_EDIT_DATE_MM));
 DCL WS_EDIT_DATE_DD_N       FIXED BIN(15) BASED(ADDR(WS_EDIT_DATE_DD));
 DCL WS_SEVERITY_N           FIXED BIN(15) BASED(ADDR(WS_SEVERITY));

 /* Date validation flags */
 DCL INPUT_ERROR             BIT(1) INIT('0'B);
 DCL WS_RETURN_MSG           CHAR(75) INIT(' ');
 DCL WS_RETURN_MSG_OFF       BIT(1) INIT('1'B);
 DCL WS_EDIT_VARIABLE_NAME   CHAR(30) INIT(' ');
 DCL WS_DIV_BY               FIXED BIN(31);
 DCL WS_DIVIDEND             FIXED BIN(31);
 DCL WS_REMAINDER            FIXED BIN(31);
