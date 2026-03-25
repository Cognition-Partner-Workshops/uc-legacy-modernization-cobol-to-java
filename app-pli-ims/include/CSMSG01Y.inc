 /*********************************************************************/
 /* Common messages                                                   */
 /* Converted from COBOL copybook: app/cpy/CSMSG01Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 CCDA_COMMON_MESSAGES,
       2 CCDA_MSG_THANK_YOU    CHAR(50)
         INIT('Thank you for using CardDemo application...      '),
       2 CCDA_MSG_INVALID_KEY  CHAR(50)
         INIT('Invalid key pressed. Please see below...         ');
