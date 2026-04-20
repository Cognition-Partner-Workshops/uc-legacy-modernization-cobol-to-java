//TTRN02C  JOB (CARDDEMO),'TEST TXN PROCESSING',
//         CLASS=A,MSGCLASS=X,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*
//* ================================================================
//* JCL: TTRN02C - Test driver for CBTRN02C transaction processing
//*
//* This JCL runs the CBTRN02C batch program against controlled
//* test fixture data and then verifies the output files.
//*
//* Two test scenarios:
//*   STEP010/020: Valid transactions (3 records, all should post)
//*   STEP030/040: Reject transactions (4 records, all should fail)
//* ================================================================
//*
//* ================================================================
//* STEP005 - Generate test data for valid scenario
//* ================================================================
//STEP005  EXEC PGM=GENTDATA
//STEPLIB  DD DSN=&HLQ..TEST.LOADLIB,DISP=SHR
//DALYTRAN DD DSN=&&DALYVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=350,BLKSIZE=3500)
//XREFFILE DD DSN=&&XREFVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=50,BLKSIZE=500)
//ACCTFILE DD DSN=&&ACCTVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=300,BLKSIZE=3000)
//TCATBALF DD DSN=&&TCATVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=50,BLKSIZE=500)
//SYSOUT   DD SYSOUT=*
//*
//* ================================================================
//* STEP010 - Run CBTRN02C with valid transactions
//* ================================================================
//STEP010  EXEC PGM=CBTRN02C,COND=(0,NE)
//STEPLIB  DD DSN=&HLQ..CARDDEMO.LOADLIB,DISP=SHR
//DALYTRAN DD DSN=&&DALYVLD,DISP=(OLD,DELETE)
//TRANFILE DD DSN=&&TRANVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=350,BLKSIZE=3500)
//XREFFILE DD DSN=&&XREFVLD,DISP=(OLD,DELETE)
//DALYREJS DD DSN=&&REJSVLD,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=430,BLKSIZE=4300)
//ACCTFILE DD DSN=&&ACCTVLD,DISP=(OLD,PASS)
//TCATBALF DD DSN=&&TCATVLD,DISP=(OLD,PASS)
//SYSOUT   DD SYSOUT=*
//*
//* ================================================================
//* STEP020 - Verify valid transaction outputs
//* ================================================================
//STEP020  EXEC PGM=VTRN02C,COND=(8,LT)
//STEPLIB  DD DSN=&HLQ..TEST.LOADLIB,DISP=SHR
//TRANFILE DD DSN=&&TRANVLD,DISP=(OLD,DELETE)
//DALYREJS DD DSN=&&REJSVLD,DISP=(OLD,DELETE)
//ACCTFILE DD DSN=&&ACCTVLD,DISP=(OLD,DELETE)
//TCATBALF DD DSN=&&TCATVLD,DISP=(OLD,DELETE)
//SYSOUT   DD SYSOUT=*
//*
//* ================================================================
//* STEP025 - Generate test data for reject scenario
//* ================================================================
//STEP025  EXEC PGM=GENTDATA,COND=(0,NE)
//STEPLIB  DD DSN=&HLQ..TEST.LOADLIB,DISP=SHR
//DALYTRAN DD DSN=&&DALYREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=350,BLKSIZE=3500)
//XREFFILE DD DSN=&&XREFREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=50,BLKSIZE=500)
//ACCTFILE DD DSN=&&ACCTREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=300,BLKSIZE=3000)
//TCATBALF DD DSN=&&TCATREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=50,BLKSIZE=500)
//SYSOUT   DD SYSOUT=*
//*
//* ================================================================
//* STEP030 - Run CBTRN02C with reject transactions
//* ================================================================
//STEP030  EXEC PGM=CBTRN02C,COND=(0,NE)
//STEPLIB  DD DSN=&HLQ..CARDDEMO.LOADLIB,DISP=SHR
//DALYTRAN DD DSN=&&DALYREJ,DISP=(OLD,DELETE)
//TRANFILE DD DSN=&&TRANREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=350,BLKSIZE=3500)
//XREFFILE DD DSN=&&XREFREJ,DISP=(OLD,DELETE)
//DALYREJS DD DSN=&&REJSREJ,DISP=(NEW,PASS),
//            SPACE=(TRK,(1,1)),
//            DCB=(RECFM=FB,LRECL=430,BLKSIZE=4300)
//ACCTFILE DD DSN=&&ACCTREJ,DISP=(OLD,PASS)
//TCATBALF DD DSN=&&TCATREJ,DISP=(OLD,PASS)
//SYSOUT   DD SYSOUT=*
//*
//* ================================================================
//* STEP040 - Verify reject transaction outputs
//* ================================================================
//STEP040  EXEC PGM=VTRN02C,COND=(0,NE)
//STEPLIB  DD DSN=&HLQ..TEST.LOADLIB,DISP=SHR
//TRANFILE DD DSN=&&TRANREJ,DISP=(OLD,DELETE)
//DALYREJS DD DSN=&&REJSREJ,DISP=(OLD,DELETE)
//ACCTFILE DD DSN=&&ACCTREJ,DISP=(OLD,DELETE)
//TCATBALF DD DSN=&&TCATREJ,DISP=(OLD,DELETE)
//SYSOUT   DD SYSOUT=*
//*
