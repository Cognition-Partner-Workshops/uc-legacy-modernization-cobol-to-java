      ******************************************************************
      * Program     : COSGN00B.CBL
      * Application : CardDemo
      * Type        : Batch COBOL Subprogram (Business Logic)
      * Function    : Authentication business logic extracted from
      *               COSGN00C for unit testing. Contains NO CICS
      *               statements. All I/O is handled via parameters.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COSGN00B.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-VARIABLES.
         05 WS-USER-ID-UPPER            PIC X(08).
         05 WS-USER-PWD-UPPER           PIC X(08).

      *----------------------------------------------------------------*
      *                        LINKAGE SECTION
      *----------------------------------------------------------------*
       LINKAGE SECTION.

       01 LK-USER-ID                    PIC X(08).
       01 LK-USER-PWD                   PIC X(08).

       COPY CSUSR01Y.

       01 LK-USER-FOUND-FLAG            PIC X(01).
       01 LK-RESULT-CODE                PIC 9(02).
       01 LK-USER-TYPE                  PIC X(01).
       01 LK-MESSAGE                    PIC X(80).

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION USING LK-USER-ID
                                LK-USER-PWD
                                SEC-USER-DATA
                                LK-USER-FOUND-FLAG
                                LK-RESULT-CODE
                                LK-USER-TYPE
                                LK-MESSAGE.
       MAIN-PARA.

           MOVE 00 TO LK-RESULT-CODE
           MOVE SPACES TO LK-MESSAGE
           MOVE SPACES TO LK-USER-TYPE

           EVALUATE TRUE
               WHEN LK-USER-ID = SPACES OR LOW-VALUES
                   MOVE 01 TO LK-RESULT-CODE
                   MOVE 'Please enter User ID ...' TO LK-MESSAGE
                   GOBACK
               WHEN LK-USER-PWD = SPACES OR LOW-VALUES
                   MOVE 02 TO LK-RESULT-CODE
                   MOVE 'Please enter Password ...' TO LK-MESSAGE
                   GOBACK
               WHEN OTHER
                   CONTINUE
           END-EVALUATE

           MOVE FUNCTION UPPER-CASE(LK-USER-ID) TO
                           WS-USER-ID-UPPER
           MOVE FUNCTION UPPER-CASE(LK-USER-PWD) TO
                           WS-USER-PWD-UPPER

           EVALUATE LK-USER-FOUND-FLAG
               WHEN 'Y'
                   IF SEC-USR-PWD = WS-USER-PWD-UPPER
                       MOVE 00 TO LK-RESULT-CODE
                       MOVE SEC-USR-TYPE TO LK-USER-TYPE
                       MOVE 'Sign-on successful.' TO LK-MESSAGE
                   ELSE
                       MOVE 03 TO LK-RESULT-CODE
                       MOVE 'Wrong Password. Try again ...'
                                                  TO LK-MESSAGE
                   END-IF
               WHEN 'N'
                   MOVE 04 TO LK-RESULT-CODE
                   MOVE 'User not found. Try again ...'
                                              TO LK-MESSAGE
               WHEN 'E'
                   MOVE 05 TO LK-RESULT-CODE
                   MOVE 'Unable to verify the User ...'
                                              TO LK-MESSAGE
               WHEN OTHER
                   MOVE 05 TO LK-RESULT-CODE
                   MOVE 'Unable to verify the User ...'
                                              TO LK-MESSAGE
           END-EVALUATE.

           GOBACK.
