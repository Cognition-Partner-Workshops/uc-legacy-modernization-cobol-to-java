//INTCALC  JOB (CARDDEMO),'INTEREST CALCULATION',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : INTCALC.JCL
//* Application: CardDemo
//* Function  : Calculate monthly interest on account balances
//*             via IMS BMP region
//* Converted from CICS batch JCL: app/jcl/INTCALC
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//INTCALC  EXEC PGM=DFSRRC00,
//         PARM='BMP,CBACT04C,PCBACT04,,,,,,,,,,N,N'
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
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
