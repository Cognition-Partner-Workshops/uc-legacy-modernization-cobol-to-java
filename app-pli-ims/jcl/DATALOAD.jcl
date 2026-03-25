//DATALOAD JOB (CARDDEMO),'LOAD ALL DATA FILES',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : DATALOAD.JCL
//* Application: CardDemo
//* Function  : Initial data load for all IMS databases
//*             Loads accounts, cards, customers, cross-references,
//*             and user security data from sequential files.
//* Converted from CICS VSAM load JCL (ACCTFILE, CARDFILE, etc.)
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//*-------------------------------------------------------------------
//* STEP 1: Load Account Data
//*-------------------------------------------------------------------
//LDACCT   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBACT01C,PCBACT01,,,,,,,,,,N,N'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//ACCTDATA DD DSN=CARDDEMO.DATA.ACCTDATA,DISP=SHR
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 2: Load Card Data
//*-------------------------------------------------------------------
//LDCARD   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBACT02C,PCBACT02,,,,,,,,,,N,N',
//         COND=(4,LT,LDACCT)
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//CARDDATA DD DSN=CARDDEMO.DATA.CARDDATA,DISP=SHR
//DBCARDDT DD DSN=CARDDEMO.IMS.DBCARDDT,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 3: Load Card Cross-Reference Data
//*-------------------------------------------------------------------
//LDXREF   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBACT03C,PCBACT03,,,,,,,,,,N,N',
//         COND=(4,LT,LDCARD)
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//XREFDATA DD DSN=CARDDEMO.DATA.XREFDATA,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 4: Load Customer Data
//*-------------------------------------------------------------------
//LDCUST   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBCUS01C,PCBCUS01,,,,,,,,,,N,N',
//         COND=(4,LT,LDXREF)
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//CUSTDATA DD DSN=CARDDEMO.DATA.CUSTDATA,DISP=SHR
//DBCUSTDT DD DSN=CARDDEMO.IMS.DBCUSTDT,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 5: Load User Security Data
//*-------------------------------------------------------------------
//LDUSRSEC EXEC PGM=DFSRRC00,
//         PARM='DLI,CBUSRSEC,PCBUSRSC,,,,,,,,,,N,N',
//         COND=(4,LT,LDCUST)
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//USRSECDT DD DSN=CARDDEMO.DATA.USRSEC,DISP=SHR
//DBUSRSEC DD DSN=CARDDEMO.IMS.DBUSRSEC,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
