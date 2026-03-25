 /*********************************************************************/
 /* Date and time working storage                                     */
 /* Converted from COBOL copybook: app/cpy/CSDAT01Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 WS_DATE_TIME,
       2 WS_CURDATE_DATA,
         3 WS_CURDATE,
           4 WS_CURDATE_YEAR        PIC '(4)9',
           4 WS_CURDATE_MONTH       PIC '(2)9',
           4 WS_CURDATE_DAY         PIC '(2)9',
         3 WS_CURTIME,
           4 WS_CURTIME_HOURS       PIC '(2)9',
           4 WS_CURTIME_MINUTE      PIC '(2)9',
           4 WS_CURTIME_SECOND      PIC '(2)9',
           4 WS_CURTIME_MILSEC      PIC '(2)9',
       2 WS_CURDATE_MM_DD_YY        CHAR(8),
       2 WS_CURTIME_HH_MM_SS        CHAR(8),
       2 WS_TIMESTAMP                CHAR(26);
