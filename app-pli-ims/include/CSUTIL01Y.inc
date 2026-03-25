 /*********************************************************************/
 /* Include     : CSUTIL01Y.INC                                       */
 /* Application : CardDemo                                            */
 /* Function    : Shared utility procedures for online programs       */
 /*               Eliminates duplicate logic across all online PL/I   */
 /*               programs (POPULATE_HEADER_INFO, SEND_MAP_COMMON)    */
 /*********************************************************************/
 /* Copyright Amazon.com, Inc. or its affiliates.                     */
 /* All Rights Reserved.                                              */
 /* Licensed under the Apache License, Version 2.0                    */
 /*********************************************************************/

 /*********************************************************************/
 /* POPULATE_HEADER_INFO                                              */
 /* Populates standard header fields in the output message:           */
 /*   - Transaction name, program name                                */
 /*   - Application titles (from COTTL01Y include)                    */
 /*   - Current date (MM/DD/YY) and time (HH:MM:SS)                  */
 /* Prerequisites:                                                    */
 /*   - OUT_TRNNAME, OUT_TITLE01, OUT_TITLE02, OUT_PGMNAME,          */
 /*     OUT_CURDATE, OUT_CURTIME must be declared in OUTPUT_MSG       */
 /*   - WS_TRANID, WS_PGMNAME must be declared as working storage    */
 /*   - COTTL01Y include must be included for CCDA_TITLE01/02         */
 /*********************************************************************/
 POPULATE_HEADER_INFO: PROC;
   DCL CURRENT_TS CHAR(17);
   CURRENT_TS = DATETIME();
   OUT_TRNNAME = WS_TRANID;
   OUT_TITLE01 = CCDA_TITLE01;
   OUT_TITLE02 = CCDA_TITLE02;
   OUT_PGMNAME = WS_PGMNAME;
   OUT_CURDATE = SUBSTR(CURRENT_TS, 5, 2) || '/' ||
                 SUBSTR(CURRENT_TS, 7, 2) || '/' ||
                 SUBSTR(CURRENT_TS, 3, 2);
   OUT_CURTIME = SUBSTR(CURRENT_TS, 9, 2) || ':' ||
                 SUBSTR(CURRENT_TS, 11, 2) || ':' ||
                 SUBSTR(CURRENT_TS, 13, 2);
 END POPULATE_HEADER_INFO;

 /*********************************************************************/
 /* RETURN_TO_PREV_SCREEN                                            */
 /* Switches to a different IMS transaction via ALT PCB.              */
 /* Uses CHNG to set destination, ISRT to send COMMAREA, PURG to     */
 /* complete the message switch.                                      */
 /* Prerequisites:                                                    */
 /*   - ALT_PCB_PTR must be declared                                  */
 /*   - CARDDEMO_COMMAREA must be populated                           */
 /*   - TARGET_TRANID must be set before calling                      */
 /*********************************************************************/
 RETURN_TO_PREV_SCREEN: PROC(TARGET_TRANID);
   DCL TARGET_TRANID CHAR(4);
   CALL PLITDLI(CHNG, ALT_PCB_PTR, TARGET_TRANID);
   CALL PLITDLI(ISRT, ALT_PCB_PTR, CARDDEMO_COMMAREA);
   CALL PLITDLI(PURG, ALT_PCB_PTR);
 END RETURN_TO_PREV_SCREEN;
