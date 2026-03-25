//DBDGEN   JOB (CARDDEMO),'DBD GENERATION',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : DBDGEN.JCL
//* Application: CardDemo
//* Function  : Generate all DBD (Database Descriptor) definitions
//*             for CardDemo IMS databases
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//*-------------------------------------------------------------------
//* STEP 1: Generate DBUSRSEC DBD (User Security)
//*-------------------------------------------------------------------
//USRSEC   EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBUSRSEC'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBUSRSEC),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
//*
//*-------------------------------------------------------------------
//* STEP 2: Generate DBACCTDT DBD (Account Data)
//*-------------------------------------------------------------------
//ACCTDT   EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBACCTDT'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBACCTDT),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
//*
//*-------------------------------------------------------------------
//* STEP 3: Generate DBCARDDT DBD (Card Data)
//*-------------------------------------------------------------------
//CARDDT   EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBCARDDT'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBCARDDT),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
//*
//*-------------------------------------------------------------------
//* STEP 4: Generate DBCUSTDT DBD (Customer Data)
//*-------------------------------------------------------------------
//CUSTDT   EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBCUSTDT'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBCUSTDT),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
//*
//*-------------------------------------------------------------------
//* STEP 5: Generate DBTRANSC DBD (Transaction Data)
//*-------------------------------------------------------------------
//TRANSC   EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBTRANSC'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBTRANSC),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
//*
//*-------------------------------------------------------------------
//* STEP 6: Generate DBCXREF DBD (Card Cross-Reference)
//*-------------------------------------------------------------------
//CXREF    EXEC PGM=DFSRRC00,
//         PARM='ULU,DBDGEN,DBCXREF'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//IMS      DD DSN=IMS.DBDLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(DBCXREF),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(80,(100,100))
//SYSUT1   DD UNIT=SYSDA,SPACE=(1024,(100,50))
