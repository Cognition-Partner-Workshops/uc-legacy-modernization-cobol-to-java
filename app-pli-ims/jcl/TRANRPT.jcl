//TRANRPT  JOB (CARDDEMO),'TRANSACTION REPORT',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : TRANRPT.JCL
//* Application: CardDemo
//* Function  : Generate transaction report via IMS BMP region
//* Converted from CICS batch JCL: app/jcl/COMBTRAN (report step)
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//REPORT   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBTRN03C,PCBTRN03,,,,,,,,,,N,N'
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
//* Database Datasets
//*
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//* Output: Transaction report
//*
//RPTFILE  DD SYSOUT=*,
//         DCB=(RECFM=FBA,LRECL=133,BLKSIZE=13300)
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
