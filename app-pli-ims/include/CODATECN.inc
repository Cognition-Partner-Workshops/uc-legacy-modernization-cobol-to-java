 /*********************************************************************/
 /* Date conversion record structure                                  */
 /* Converted from COBOL copybook: app/cpy/CODATECN.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 CODATECN_REC,
       2 CODATECN_IN_REC,
         3 CODATECN_TYPE            CHAR(1),
         3 CODATECN_INP_DATE        CHAR(20),
       2 CODATECN_OUT_REC,
         3 CODATECN_OUTTYPE         CHAR(1),
         3 CODATECN_OUT_DATE        CHAR(20),
       2 CODATECN_ERROR_MSG         CHAR(38);

 /* Input type constants */
 DCL YYYYMMDD_IN     CHAR(1) INIT('1');
 DCL YYYY_MM_DD_IN   CHAR(1) INIT('2');

 /* Output type constants */
 DCL YYYY_MM_DD_OP   CHAR(1) INIT('1');
 DCL YYYYMMDD_OP     CHAR(1) INIT('2');
