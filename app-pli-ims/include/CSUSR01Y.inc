 /*********************************************************************/
 /* User security record structure                                    */
 /* Converted from COBOL copybook: app/cpy/CSUSR01Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 SEC_USER_DATA,
       2 SEC_USR_ID              CHAR(8),
       2 SEC_USR_FNAME           CHAR(20),
       2 SEC_USR_LNAME           CHAR(20),
       2 SEC_USR_PWD             CHAR(8),
       2 SEC_USR_TYPE            CHAR(1),
       2 SEC_USR_FILLER          CHAR(23);
