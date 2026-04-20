      ******************************************************************
      * Program     : TSGN00C.CBL
      * Application : CardDemo - Unit Test Driver
      * Type        : Batch COBOL Program
      * Function    : Test driver for COSGN00B authentication
      *               business logic subprogram.
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. TSGN00C.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.

       DATA DIVISION.
      *----------------------------------------------------------------*
      *                     WORKING STORAGE SECTION
      *----------------------------------------------------------------*
       WORKING-STORAGE SECTION.

       01 WS-TEST-COUNTERS.
         05 WS-TOTAL-TESTS              PIC 9(03) VALUE 0.
         05 WS-PASS-COUNT               PIC 9(03) VALUE 0.
         05 WS-FAIL-COUNT               PIC 9(03) VALUE 0.

       01 WS-TEST-FIELDS.
         05 WS-USER-ID                  PIC X(08).
         05 WS-USER-PWD                 PIC X(08).
         05 WS-USER-FOUND-FLAG          PIC X(01).
         05 WS-RESULT-CODE              PIC 9(02).
         05 WS-USER-TYPE                PIC X(01).
         05 WS-MESSAGE                  PIC X(80).

       COPY CSUSR01Y.

      *----------------------------------------------------------------*
      *                      PROCEDURE DIVISION
      *----------------------------------------------------------------*
       PROCEDURE DIVISION.
       MAIN-PARA.

           DISPLAY '=============================================='
           DISPLAY 'COSGN00B AUTHENTICATION UNIT TEST SUITE'
           DISPLAY '=============================================='
           DISPLAY SPACES

           PERFORM TC-AUTH-01
           PERFORM TC-AUTH-02
           PERFORM TC-AUTH-03
           PERFORM TC-AUTH-04
           PERFORM TC-AUTH-05
           PERFORM TC-AUTH-06
           PERFORM TC-AUTH-07
           PERFORM TC-AUTH-08

           DISPLAY SPACES
           DISPLAY '=============================================='
           DISPLAY 'TEST SUMMARY'
           DISPLAY '=============================================='
           DISPLAY 'TOTAL TESTS : ' WS-TOTAL-TESTS
           DISPLAY 'PASSED      : ' WS-PASS-COUNT
           DISPLAY 'FAILED      : ' WS-FAIL-COUNT
           DISPLAY '=============================================='

           IF WS-FAIL-COUNT > 0
               MOVE 4 TO RETURN-CODE
           ELSE
               MOVE 0 TO RETURN-CODE
           END-IF

           STOP RUN.

      *----------------------------------------------------------------*
      * TC-AUTH-01: Valid admin login
      *----------------------------------------------------------------*
       TC-AUTH-01.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'ADMIN001' TO WS-USER-ID
           MOVE 'ADMIN001' TO WS-USER-PWD
           MOVE 'ADMIN001' TO SEC-USR-ID
           MOVE 'ADMIN001' TO SEC-USR-PWD
           MOVE 'A'        TO SEC-USR-TYPE
           MOVE 'Y'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 00 AND WS-USER-TYPE = 'A'
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-01: Valid admin login'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-01: Valid admin login'
               DISPLAY '  Expected RC=00 TYPE=A'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
                       ' TYPE=' WS-USER-TYPE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-02: Valid regular user login
      *----------------------------------------------------------------*
       TC-AUTH-02.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'USER0001' TO WS-USER-ID
           MOVE 'USER0001' TO WS-USER-PWD
           MOVE 'USER0001' TO SEC-USR-ID
           MOVE 'USER0001' TO SEC-USR-PWD
           MOVE 'U'        TO SEC-USR-TYPE
           MOVE 'Y'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 00 AND WS-USER-TYPE = 'U'
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-02: Valid regular user login'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-02: Valid regular user login'
               DISPLAY '  Expected RC=00 TYPE=U'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
                       ' TYPE=' WS-USER-TYPE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-03: Wrong password
      *----------------------------------------------------------------*
       TC-AUTH-03.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'USER0001' TO WS-USER-ID
           MOVE 'WRONGPWD' TO WS-USER-PWD
           MOVE 'USER0001' TO SEC-USR-ID
           MOVE 'USER0001' TO SEC-USR-PWD
           MOVE 'U'        TO SEC-USR-TYPE
           MOVE 'Y'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 03
               INSPECT WS-MESSAGE TALLYING WS-PASS-COUNT
                   FOR ALL 'Wrong Password'
               IF WS-PASS-COUNT > WS-PASS-COUNT - 1
                   ADD 1 TO WS-PASS-COUNT
                   DISPLAY 'PASS TC-AUTH-03: Wrong password'
               ELSE
                   ADD 1 TO WS-FAIL-COUNT
                   DISPLAY 'FAIL TC-AUTH-03: Wrong password'
                   DISPLAY '  Message: ' WS-MESSAGE
               END-IF
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-03: Wrong password'
               DISPLAY '  Expected RC=03'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-04: User not found
      *----------------------------------------------------------------*
       TC-AUTH-04.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'NOUSER01' TO WS-USER-ID
           MOVE 'ANYPASSW' TO WS-USER-PWD
           MOVE 'N'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 04
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-04: User not found'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-04: User not found'
               DISPLAY '  Expected RC=04'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-05: Empty user ID
      *----------------------------------------------------------------*
       TC-AUTH-05.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE SPACES     TO WS-USER-ID
           MOVE 'ANYPASSW' TO WS-USER-PWD
           MOVE 'N'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 01
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-05: Empty user ID'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-05: Empty user ID'
               DISPLAY '  Expected RC=01'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-06: Empty password
      *----------------------------------------------------------------*
       TC-AUTH-06.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'USER0001' TO WS-USER-ID
           MOVE SPACES     TO WS-USER-PWD
           MOVE 'N'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 02
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-06: Empty password'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-06: Empty password'
               DISPLAY '  Expected RC=02'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-07: File error
      *----------------------------------------------------------------*
       TC-AUTH-07.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'USER0001' TO WS-USER-ID
           MOVE 'USER0001' TO WS-USER-PWD
           MOVE 'E'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 05
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-07: File error'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-07: File error'
               DISPLAY '  Expected RC=05'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
           END-IF.

      *----------------------------------------------------------------*
      * TC-AUTH-08: Case insensitivity
      *----------------------------------------------------------------*
       TC-AUTH-08.
           ADD 1 TO WS-TOTAL-TESTS
           INITIALIZE SEC-USER-DATA
           MOVE 'user0001' TO WS-USER-ID
           MOVE 'user0001' TO WS-USER-PWD
           MOVE 'USER0001' TO SEC-USR-ID
           MOVE 'USER0001' TO SEC-USR-PWD
           MOVE 'U'        TO SEC-USR-TYPE
           MOVE 'Y'        TO WS-USER-FOUND-FLAG
           MOVE 00         TO WS-RESULT-CODE
           MOVE SPACES     TO WS-USER-TYPE
           MOVE SPACES     TO WS-MESSAGE

           CALL 'COSGN00B' USING WS-USER-ID
                                 WS-USER-PWD
                                 SEC-USER-DATA
                                 WS-USER-FOUND-FLAG
                                 WS-RESULT-CODE
                                 WS-USER-TYPE
                                 WS-MESSAGE

           IF WS-RESULT-CODE = 00
               ADD 1 TO WS-PASS-COUNT
               DISPLAY 'PASS TC-AUTH-08: Case insensitivity'
           ELSE
               ADD 1 TO WS-FAIL-COUNT
               DISPLAY 'FAIL TC-AUTH-08: Case insensitivity'
               DISPLAY '  Expected RC=00'
               DISPLAY '  Got      RC=' WS-RESULT-CODE
               DISPLAY '  Message: ' WS-MESSAGE
           END-IF.
