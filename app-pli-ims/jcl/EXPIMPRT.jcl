//EXPIMPRT JOB (CARDDEMO),'EXPORT/IMPORT DATA',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : EXPIMPRT.JCL
//* Application: CardDemo
//* Function  : Export and Import CardDemo data via IMS BMP
//*             Step 1: Export all databases to sequential file
//*             Step 2: Import from sequential file to databases
//*             Run steps independently as needed (condition codes)
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//*-------------------------------------------------------------------
//* STEP 1: EXPORT - Read all IMS databases, write sequential file
//*-------------------------------------------------------------------
//EXPORT   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBEXPORT,PCBEXPRT,,,,,,,,,,N,N'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//*
//* Database Datasets (input for export)
//*
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//DBCARDDT DD DSN=CARDDEMO.IMS.DBCARDDT,DISP=SHR
//DBCUSTDT DD DSN=CARDDEMO.IMS.DBCUSTDT,DISP=SHR
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//* Output: Export file
//*
//EXPFILE  DD DSN=CARDDEMO.EXPORT.FILE,
//         DISP=(NEW,CATLG,DELETE),
//         UNIT=SYSDA,SPACE=(CYL,(20,10)),
//         DCB=(RECFM=FB,LRECL=500,BLKSIZE=50000)
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
//*
//*-------------------------------------------------------------------
//* STEP 2: IMPORT - Read sequential file, load IMS databases
//*         (Only runs if EXPORT step completes with RC=0)
//*-------------------------------------------------------------------
//IMPORT   EXEC PGM=DFSRRC00,
//         PARM='BMP,CBIMPORT,PCBMPRT,,,,,,,,,,N,N',
//         COND=(0,NE,EXPORT)
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//         DD DSN=CARDDEMO.PLI.LOADLIB,DISP=SHR
//DFSRESLB DD DSN=IMS.SDFSRESL,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//         DD DSN=IMS.PSBLIB,DISP=SHR
//DFSVSAMP DD *
  4096,8
  VSRBF=4096,4
/*
//*
//* Database Datasets (output for import)
//*
//DBACCTDT DD DSN=CARDDEMO.IMS.DBACCTDT,DISP=SHR
//DBCARDDT DD DSN=CARDDEMO.IMS.DBCARDDT,DISP=SHR
//DBCUSTDT DD DSN=CARDDEMO.IMS.DBCUSTDT,DISP=SHR
//DBTRANSC DD DSN=CARDDEMO.IMS.DBTRANSC,DISP=SHR
//DBCXREF  DD DSN=CARDDEMO.IMS.DBCXREF,DISP=SHR
//*
//* Input: Import file
//*
//IMPFILE  DD DSN=CARDDEMO.EXPORT.FILE,DISP=SHR
//*
//SYSPRINT DD SYSOUT=*
//SYSOUT   DD SYSOUT=*
//SYSUDUMP DD SYSOUT=*
