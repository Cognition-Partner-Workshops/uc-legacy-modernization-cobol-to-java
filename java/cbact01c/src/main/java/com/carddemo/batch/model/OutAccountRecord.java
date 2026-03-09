package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to the COBOL FD OUT-FILE record (LRECL=107 from JCL).
 *
 * <pre>
 *   05  OUT-ACCT-ID                PIC 9(11)
 *   05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *   05  OUT-ACCT-CURR-BAL          PIC S9(10)V99   (12 bytes display)
 *   05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99   (12 bytes display)
 *   05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   (12 bytes display)
 *   05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *   05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *   05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *   05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   (12 bytes display)
 *   05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   COMP-3 (7 bytes)
 *   05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 *
 * Total = 11+1+12+12+12+10+10+10+12+7+10 = 107 bytes
 */
public record OutAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currBal,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expiraionDate,
        String reissueDate,
        BigDecimal currCycCredit,
        BigDecimal currCycDebit,
        String groupId
) {
}
