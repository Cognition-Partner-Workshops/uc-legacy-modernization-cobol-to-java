//MFSGEN   JOB (CARDDEMO),'MFS FORMAT GENERATION',
//         CLASS=A,MSGCLASS=H,MSGLEVEL=(1,1),
//         NOTIFY=&SYSUID
//*********************************************************************
//* JCL       : MFSGEN.JCL
//* Application: CardDemo
//* Function  : Generate MFS (Message Format Service) control blocks
//*             from MFS source definitions
//*********************************************************************
//* Copyright Amazon.com, Inc. or its affiliates.
//* All Rights Reserved.
//* Licensed under the Apache License, Version 2.0
//*********************************************************************
//*
//*-------------------------------------------------------------------
//* PROC for MFS generation
//*-------------------------------------------------------------------
//MFSCOMP  PROC MBR=
//*
//MFS      EXEC PGM=DFSUNUB0,
//         PARM='LINECT=55,COMP'
//STEPLIB  DD DSN=IMS.SDFSRESL,DISP=SHR
//SYSLIB   DD DSN=IMS.SDFSMAC,DISP=SHR
//SYSIN    DD DSN=CARDDEMO.MFS.SOURCE(&MBR),DISP=SHR
//SYSPRINT DD SYSOUT=*
//REFIN    DD DUMMY
//REFOUT   DD DUMMY
//REFRD    DD DUMMY
//SEQBLKS  DD DSN=&&SEQBLK,DISP=(NEW,PASS),
//         UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT1   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT2   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//SYSUT3   DD DSN=CARDDEMO.MFS.FMTLIB,DISP=SHR
//SYSUT4   DD UNIT=SYSDA,SPACE=(CYL,(1,1))
//*
//         PEND
//*
//*-------------------------------------------------------------------
//* Generate all MFS formats
//*-------------------------------------------------------------------
//COSGN00  EXEC MFSCOMP,MBR=COSGN00
//COMEN01  EXEC MFSCOMP,MBR=COMEN01
//COADM01  EXEC MFSCOMP,MBR=COADM01
//COACTVW  EXEC MFSCOMP,MBR=COACTVW
//COACTUP  EXEC MFSCOMP,MBR=COACTUP
//COCRDLI  EXEC MFSCOMP,MBR=COCRDLI
//COCRDSL  EXEC MFSCOMP,MBR=COCRDSL
//COCRDUP  EXEC MFSCOMP,MBR=COCRDUP
//COTRN00  EXEC MFSCOMP,MBR=COTRN00
//COTRN01  EXEC MFSCOMP,MBR=COTRN01
//COTRN02  EXEC MFSCOMP,MBR=COTRN02
//CORPT00  EXEC MFSCOMP,MBR=CORPT00
//COBIL00  EXEC MFSCOMP,MBR=COBIL00
//COUSR00  EXEC MFSCOMP,MBR=COUSR00
//COUSR01  EXEC MFSCOMP,MBR=COUSR01
//COUSR02  EXEC MFSCOMP,MBR=COUSR02
//COUSR03  EXEC MFSCOMP,MBR=COUSR03
