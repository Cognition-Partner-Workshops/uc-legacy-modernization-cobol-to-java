      *****************************************************************
      * COBDATFT - GnuCOBOL stand-in for app/asm/COBDATFT.asm, used
      * only by java/scripts/run-parity.sh so the original CBACT01C can
      * run off-mainframe. Statement-for-statement translation of the
      * assembler (same byte moves, same partial update of the output
      * date, no date validation). Not part of the Java port.
      *****************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COBDATFT.
       DATA DIVISION.
       LINKAGE SECTION.
       COPY CODATECN.
       PROCEDURE DIVISION USING CODATECN-REC.
           EVALUATE CODATECN-TYPE
               WHEN '1'
                   IF CODATECN-INP-DATE(5:1) = '-'
                      OR CODATECN-OUTTYPE = '2'
                       PERFORM 9000-INVALID
                   ELSE
                       MOVE CODATECN-INP-DATE(1:4)
                         TO CODATECN-0UT-DATE(1:4)
                       MOVE '-' TO CODATECN-0UT-DATE(5:1)
                       MOVE CODATECN-INP-DATE(5:2)
                         TO CODATECN-0UT-DATE(6:2)
                       MOVE '-' TO CODATECN-0UT-DATE(8:1)
                       MOVE CODATECN-INP-DATE(7:2)
                         TO CODATECN-0UT-DATE(9:2)
                   END-IF
               WHEN '2'
                   IF CODATECN-OUTTYPE = '1'
                       PERFORM 9000-INVALID
                   ELSE
                       MOVE CODATECN-INP-DATE(1:4)
                         TO CODATECN-0UT-DATE(1:4)
                       MOVE CODATECN-INP-DATE(6:2)
                         TO CODATECN-0UT-DATE(5:2)
                       MOVE CODATECN-INP-DATE(9:2)
                         TO CODATECN-0UT-DATE(7:2)
                   END-IF
               WHEN OTHER
                   PERFORM 9000-INVALID
           END-EVALUATE
           MOVE 0 TO RETURN-CODE
           GOBACK.
       9000-INVALID.
           MOVE 'INVALID INPUT' TO CODATECN-ERROR-MSG.
