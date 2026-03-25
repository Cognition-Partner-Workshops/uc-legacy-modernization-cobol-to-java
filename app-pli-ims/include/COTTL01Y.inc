 /*********************************************************************/
 /* Screen title definitions                                          */
 /* Converted from COBOL copybook: app/cpy/COTTL01Y.cpy              */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 DCL 1 CCDA_SCREEN_TITLE,
       2 CCDA_TITLE01     CHAR(40)
         INIT('      AWS Mainframe Modernization       '),
       2 CCDA_TITLE02     CHAR(40)
         INIT('              CardDemo                  '),
       2 CCDA_THANK_YOU   CHAR(40)
         INIT('Thank you for using CCDA application... ');
