//IMSREGN  JOB (CARDDEMO),'IMS REGION STARTUP',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : IMSREGN.JCL
//* Application: CardDemo
//* Function  : Start IMS MPP (Message Processing Program) region
//*             for CardDemo online transactions
//* Converted from CICS region startup for CardDemo
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//* This JCL starts an IMS MPP region that processes all CardDemo
//* online transactions (signon, menus, account/card/transaction
//* management, reports, billing, user administration).
//*
//*********************************************************************
//MPPREGN  EXEC PGM=DFSRRC00,
//         PARM='DLI,COSGN00C,PCOSGN00,,,,,,,,,,,Y,N,,,,,,,,,,,T'
//*
//* IMS System Libraries
//*
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//*
//* IMS Log and Checkpoint
//*
//IMSLOGR  DD DSN=IMS.LOG,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//*
//* MFS Format Library
//*
//DFSFORT  DD DSN=CARDDEMO.MFS.FMTLIB,DISP=SHR
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
