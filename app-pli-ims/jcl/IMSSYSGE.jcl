//IMSSYSGE JOB (CARDDEMO),'IMS SYSGEN STAGE1',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : IMSSYSGE.JCL
//* Application: CardDemo
//* Function  : IMS System Generation (SYSGEN) Stage 1
//*             Assembles the IMS SYSGEN macros to define
//*             applications and transactions
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//SYSGEN   EXEC PGM=ASMA90,
//         PARM='OBJECT,NODECK'
//STEPLIB  DD DSN=SYS1.LINKLIB,DISP=SHR
//SYSLIB   DD DSN=IMS.ADFSMAC,DISP=SHR
//         DD DSN=SYS1.MACLIB,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.IMS.SOURCE(IMSSYSGE),DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSLIN   DD DSN=&&OBJMOD,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//*
//*-------------------------------------------------------------------
//* Stage 2: Link-edit the SYSGEN output
//*-------------------------------------------------------------------
//LKED     EXEC PGM=IEWL,
//         PARM='LIST,LET,XREF',
//         COND=(8,LT,SYSGEN)
//SYSLIB   DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIN   DD DSN=&&OBJMOD,DISP=(OLD,DELETE)
//SYSLMOD  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSPRINT DD SYSOUT=*
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
