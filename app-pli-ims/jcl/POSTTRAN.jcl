//POSTTRAN JOB (CARDDEMO),'POST DAILY TRANSACTIONS',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : POSTTRAN.JCL
//* Application: CardDemo
//* Function  : Post daily transactions via IMS BMP region
//*             Reads daily transaction file, validates, and posts
//*             to IMS transaction database. Updates account balances.
//* Converted from CICS batch JCL: app/jcl/POSTTRAN
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//POSTING  EXEC PGM=DFSRRC00,
//         PARM='BMP,CBTRN02C,PCBTRN02,,,,,,,,,,N,N'
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
//* Input: Daily transaction file
//*
//DALYTRAN DD DSN=CARDDEMO.DAILYTRAN.FILE,DISP=SHR
//*
//* Output: Rejected transactions
//*
//DALYREJS DD DSN=CARDDEMO.DAILYREJ.FILE,
//         DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,SPACE=(CYL,(5,2)),
//         DCB=(RECFM=FB,LRECL=350,BLKSIZE=35000)
//*
//* Database Datasets
//*
//DBUSRSEC DD DSN=CARDDEMO.IMS.DBUSRSEC,DISP=SHR
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//DBCARDDT DD DSN=CARDDEMO.IMS.DBCARDDT,DISP=SHR
//DBCUSTDT DD DSN=CARDDEMO.IMS.DBCUSTDT,DISP=SHR
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
