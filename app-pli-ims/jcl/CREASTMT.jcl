//CREASTMT JOB (CARDDEMO),'GENERATE STATEMENTS',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : CREASTMT.JCL
//* Application: CardDemo
//* Function  : Generate customer statements
//*             Step 1: Extract transactions per account (CBSTM03A)
//*             Step 2: Format and print statements (CBSTM03B)
//* Converted from CICS batch JCL: app/jcl/CREASTMT
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//*-------------------------------------------------------------------
//* STEP 1: Extract statement data using IMS BMP
//*-------------------------------------------------------------------
//EXTRACT  EXEC PGM=DFSRRC00,
//         PARM='BMP,CBSTM03A,PCBSTM3A,,,,,,,,,,N,N'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//*
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//*
//STMTFILE DD DSN=CARDDEMO.STATEMENT.FILE,
//         DISP=(NEW,PASS,DELETE),
//         UNIT=SYSDA,SPACE=(CYL,(10,5)),
//         DCB=(RECFM=FB,LRECL=350,BLKSIZE=35000)
//*
//* Database Datasets
//*
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//DBCUSTDT DD DSN=CARDDEMO.IMS.DBCUSTDT,DISP=SHR
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 2: Format and print statements (no IMS needed)
//*-------------------------------------------------------------------
//PRINT    EXEC PGM=CBSTM03B,
//         COND=(4,LT,EXTRACT)
//STEPLIB  DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//STMTFILE DD DSN=CARDDEMO.STATEMENT.FILE,DISP=(OLD,DELETE)
//RPTFILE  DD SYSOUT=*,
//         DCB=(RECFM=FBA,LRECL=133,BLKSIZE=13300)
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
