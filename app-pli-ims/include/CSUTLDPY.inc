 /*********************************************************************/
 /* Procedure Division code for DATE related validation               */
 /* Converted from COBOL copybook: app/cpy/CSUTLDPY.cpy              */
 /* This include provides date validation procedures for PL/I         */
 /*********************************************************************/

 EDIT_DATE_CCYYMMDD: PROC RETURNS(BIT(1));
   DCL DATE_VALID BIT(1) INIT('1'B);

   /* Validate year */
   IF WS_EDIT_DATE_CCYY = '' | WS_EDIT_DATE_CCYY = ' ' THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     RETURN(DATE_VALID);
   END;

   IF VERIFY(WS_EDIT_DATE_CCYY, '0123456789') ^= 0 THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ' must be 4 digit number.';
     RETURN(DATE_VALID);
   END;

   /* Check century is 19 or 20 */
   IF WS_EDIT_DATE_CC ^= '19' & WS_EDIT_DATE_CC ^= '20' THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ' : Century is not valid.';
     RETURN(DATE_VALID);
   END;

   /* Validate month */
   IF WS_EDIT_DATE_MM = '' | WS_EDIT_DATE_MM = ' ' THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ' : Month must be supplied.';
     RETURN(DATE_VALID);
   END;

   IF VERIFY(WS_EDIT_DATE_MM, '0123456789') ^= 0 THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ': Month must be a number between 1 and 12.';
     RETURN(DATE_VALID);
   END;

   DCL MM_NUM FIXED BIN(15);
   MM_NUM = WS_EDIT_DATE_MM;
   IF MM_NUM < 1 | MM_NUM > 12 THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ': Month must be a number between 1 and 12.';
     RETURN(DATE_VALID);
   END;

   /* Validate day */
   IF WS_EDIT_DATE_DD = '' | WS_EDIT_DATE_DD = ' ' THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ' : Day must be supplied.';
     RETURN(DATE_VALID);
   END;

   IF VERIFY(WS_EDIT_DATE_DD, '0123456789') ^= 0 THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ':day must be a number between 1 and 31.';
     RETURN(DATE_VALID);
   END;

   DCL DD_NUM FIXED BIN(15);
   DD_NUM = WS_EDIT_DATE_DD;
   IF DD_NUM < 1 | DD_NUM > 31 THEN DO;
     INPUT_ERROR = '1'B;
     DATE_VALID = '0'B;
     WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                     ':day must be a number between 1 and 31.';
     RETURN(DATE_VALID);
   END;

   /* Check 31-day month validity */
   IF DD_NUM = 31 THEN DO;
     IF MM_NUM ^= 1 & MM_NUM ^= 3 & MM_NUM ^= 5 &
        MM_NUM ^= 7 & MM_NUM ^= 8 & MM_NUM ^= 10 &
        MM_NUM ^= 12 THEN DO;
       INPUT_ERROR = '1'B;
       DATE_VALID = '0'B;
       WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                       ':Cannot have 31 days in this month.';
       RETURN(DATE_VALID);
     END;
   END;

   /* Check February */
   IF MM_NUM = 2 THEN DO;
     IF DD_NUM > 29 THEN DO;
       INPUT_ERROR = '1'B;
       DATE_VALID = '0'B;
       WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
                       ':Cannot have 30 days in this month.';
       RETURN(DATE_VALID);
     END;
     IF DD_NUM = 29 THEN DO;
       DCL YYYY_NUM FIXED BIN(31);
       DCL DIV_BY   FIXED BIN(31);
       DCL REMAIND  FIXED BIN(31);
       YYYY_NUM = WS_EDIT_DATE_CCYY;
       IF MOD(YYYY_NUM, 400) = 0 THEN ;
       ELSE IF MOD(YYYY_NUM, 100) = 0 THEN DO;
         INPUT_ERROR = '1'B;
         DATE_VALID = '0'B;
         WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
           ':Not a leap year.Cannot have 29 days in this month.';
         RETURN(DATE_VALID);
       END;
       ELSE IF MOD(YYYY_NUM, 4) ^= 0 THEN DO;
         INPUT_ERROR = '1'B;
         DATE_VALID = '0'B;
         WS_RETURN_MSG = WS_EDIT_VARIABLE_NAME ||
           ':Not a leap year.Cannot have 29 days in this month.';
         RETURN(DATE_VALID);
       END;
     END;
   END;

   RETURN(DATE_VALID);
 END EDIT_DATE_CCYYMMDD;
