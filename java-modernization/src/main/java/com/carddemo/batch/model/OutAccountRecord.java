package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-FILE record layout (OUTFILE).
 *
 * <pre>
 *   05  OUT-ACCT-ID                PIC 9(11)
 *   05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *   05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *   05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *   05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *   05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *   05  OUT-ACCT-REISSUE-DATE      PIC X(10)    (reformatted by COBDATFT)
 *   05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *   05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99  USAGE IS COMP-3
 *   05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 */
public record OutAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {}
