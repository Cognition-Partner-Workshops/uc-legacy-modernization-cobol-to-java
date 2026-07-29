      ******************************************************************
      * GnuCOBOL stand-in for the COBDATFT assembler date-format
      * routine (app/asm/COBDATFT.asm), sufficient for the CBACT01C
      * parity run: converts YYYY-MM-DD input (TYPE '2') to YYYYMMDD
      * output (OUTTYPE '2').
      ******************************************************************
       IDENTIFICATION DIVISION.
       PROGRAM-ID. COBDATFT.
       DATA DIVISION.
       LINKAGE SECTION.
       COPY CODATECN.
       PROCEDURE DIVISION USING CODATECN-REC.
           MOVE SPACES TO CODATECN-0UT-DATE.
           IF YYYY-MM-DD-IN AND YYYYMMDD-OP
               MOVE CODATECN-INP-DATE(1:4)  TO CODATECN-2O-YYYY
               MOVE CODATECN-INP-DATE(6:2)  TO CODATECN-2O-MM
               MOVE CODATECN-INP-DATE(9:2)  TO CODATECN-2O-DD
           ELSE
               MOVE 'UNSUPPORTED DATE CONVERSION' TO
                    CODATECN-ERROR-MSG
           END-IF.
           GOBACK.
