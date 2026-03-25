 /*********************************************************************/
 /* Set attribute utility - field validation attribute setting         */
 /* Converted from COBOL copybook: app/cpy/CSSETATY.cpy              */
 /* In IMS/MFS, field attributes are set via MFS MOD definitions      */
 /* rather than programmatically. This include provides a procedure   */
 /* to set error indicators for field validation.                     */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 /* SET_FIELD_ERROR: Procedure to mark a field as in error
    In IMS/PL/I, we track field error states in flags rather than
    setting BMS attributes directly. The MFS MOD handles display. */

 DCL 1 FIELD_ERROR_FLAGS,
       2 FLD_ERR_COUNT         FIXED BIN(15) INIT(0),
       2 FLD_ERR_FLAG(50)      CHAR(1) INIT((50)' ');

 SET_FIELD_ERROR: PROC(FIELD_INDEX);
   DCL FIELD_INDEX FIXED BIN(15);
   FLD_ERR_FLAG(FIELD_INDEX) = 'E';
   FLD_ERR_COUNT = FLD_ERR_COUNT + 1;
 END SET_FIELD_ERROR;

 CLEAR_FIELD_ERRORS: PROC;
   DCL I FIXED BIN(15);
   DO I = 1 TO 50;
     FLD_ERR_FLAG(I) = ' ';
   END;
   FLD_ERR_COUNT = 0;
 END CLEAR_FIELD_ERRORS;
