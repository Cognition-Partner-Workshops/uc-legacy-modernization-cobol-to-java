package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL OUT-ACCT-REC file descriptor — a flattened account record
 * written to the sequential output file.
 *
 * <pre>
 * FD OUT-FILE.
 * 01 OUT-ACCT-REC.
 *    05  OUT-ACCT-ID                PIC 9(11).
 *    05  OUT-ACCT-ACTIVE-STATUS     PIC X(01).
 *    05  OUT-ACCT-CURR-BAL          PIC S9(10)V99.
 *    05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99.
 *    05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99.
 *    05  OUT-ACCT-OPEN-DATE         PIC X(10).
 *    05  OUT-ACCT-EXPIRAION-DATE    PIC X(10).
 *    05  OUT-ACCT-REISSUE-DATE      PIC X(10).
 *    05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99.
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
 * </pre>
 */
public record OutputAccountRecord(
        long acctId,
        String activeStatus,
        BigDecimal currentBalance,
        BigDecimal creditLimit,
        BigDecimal cashCreditLimit,
        String openDate,
        String expirationDate,
        String reissueDate,
        BigDecimal currentCycleCredit,
        BigDecimal currentCycleDebit,
        String groupId
) {

    public String toDelimitedLine() {
        return String.join("|",
                String.format("%011d", acctId),
                activeStatus,
                currentBalance.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currentCycleCredit.toPlainString(),
                currentCycleDebit.toPlainString(),
                groupId
        );
    }
}
