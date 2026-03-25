 /*********************************************************************/
 /* Abend data work areas                                             */
 /* Converted from COBOL copybook: app/cpy/CSMSG02Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 ABEND_DATA,
       2 ABEND_CODE              CHAR(4) INIT(' '),
       2 ABEND_CULPRIT           CHAR(8) INIT(' '),
       2 ABEND_REASON            CHAR(50) INIT(' '),
       2 ABEND_MSG               CHAR(72) INIT(' ');
