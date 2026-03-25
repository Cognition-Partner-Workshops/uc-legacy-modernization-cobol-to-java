package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL FD OUT-ACCT-REC — the flat output record written to OUTFILE.
 * <p>
 * COBOL layout:
 * <pre>
 *   05 OUT-ACCT-ID                 PIC 9(11)
 *   05 OUT-ACCT-ACTIVE-STATUS      PIC X(01)
 *   05 OUT-ACCT-CURR-BAL           PIC S9(10)V99
 *   05 OUT-ACCT-CREDIT-LIMIT       PIC S9(10)V99
 *   05 OUT-ACCT-CASH-CREDIT-LIMIT  PIC S9(10)V99
 *   05 OUT-ACCT-OPEN-DATE          PIC X(10)
 *   05 OUT-ACCT-EXPIRAION-DATE     PIC X(10)
 *   05 OUT-ACCT-REISSUE-DATE       PIC X(10)
 *   05 OUT-ACCT-CURR-CYC-CREDIT    PIC S9(10)V99
 *   05 OUT-ACCT-CURR-CYC-DEBIT     PIC S9(10)V99  USAGE IS COMP-3
 *   05 OUT-ACCT-GROUP-ID           PIC X(10)
 * </pre>
 */
public record OutputAccountRecord(
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
) {

    /**
     * Format as a pipe-delimited line for the output file.
     * In the COBOL program the debit field uses COMP-3 (packed decimal),
     * which is a binary encoding. In our Java port we use a human-readable
     * pipe-delimited format instead.
     */
    public String toOutputLine() {
        return String.join("|",
                String.format("%011d", acctId),
                activeStatus,
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                openDate,
                expirationDate,
                reissueDate,
                currCycCredit.toPlainString(),
                currCycDebit.toPlainString(),
                groupId);
    }
}
