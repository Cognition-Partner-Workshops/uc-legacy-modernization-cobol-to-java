 /*********************************************************************/
 /* Lookup code repository - Phone area codes and state codes         */
 /* Converted from COBOL copybook: app/cpy/CSLKPCDY.cpy              */
 /* In PL/I, the 88-level condition names with VALUE lists are        */
 /* replaced by a validation function using a lookup table approach.  */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL WS_US_PHONE_AREA_CODE_TO_EDIT CHAR(3);

 /* US State codes */
 DCL VALID_STATE_CODES(51) CHAR(2) INIT(
     'AL','AK','AZ','AR','CA','CO','CT','DE','FL','GA',
     'HI','ID','IL','IN','IA','KS','KY','LA','ME','MD',
     'MA','MI','MN','MS','MO','MT','NE','NV','NH','NJ',
     'NM','NY','NC','ND','OH','OK','OR','PA','RI','SC',
     'SD','TN','TX','UT','VT','VA','WA','WV','WI','WY',
     'DC');

 VALIDATE_PHONE_AREA_CODE: PROC(AREA_CODE) RETURNS(BIT(1));
   DCL AREA_CODE CHAR(3);
   DCL CODE_NUM  FIXED BIN(15);
   DCL VALID     BIT(1) INIT('0'B);

   /* Basic validation: must be numeric and 3 digits */
   IF VERIFY(AREA_CODE, '0123456789') ^= 0 THEN
     RETURN('0'B);

   CODE_NUM = AREA_CODE;

   /* Valid range: 200-999, first digit cannot be 0 or 1 */
   IF CODE_NUM >= 200 & CODE_NUM <= 999 THEN
     VALID = '1'B;

   RETURN(VALID);
 END VALIDATE_PHONE_AREA_CODE;

 VALIDATE_STATE_CODE: PROC(STATE_CD) RETURNS(BIT(1));
   DCL STATE_CD CHAR(2);
   DCL I FIXED BIN(15);

   DO I = 1 TO 51;
     IF STATE_CD = VALID_STATE_CODES(I) THEN
       RETURN('1'B);
   END;
   RETURN('0'B);
 END VALIDATE_STATE_CODE;
