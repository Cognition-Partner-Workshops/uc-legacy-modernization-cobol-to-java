package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL OUT-ACCT-REC structure written to OUTFILE.
 *
 * <pre>
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
 *
 * Note: The COBOL version stores CURR-CYC-DEBIT as COMP-3 (packed decimal).
 * In Java we use BigDecimal uniformly.
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
     * Build an output record from an input account record, applying the
     * COBOL business rules from paragraph 1300-POPUL-ACCT-RECORD:
     * <ul>
     *   <li>Reissue date is reformatted from YYYY-MM-DD to YYYYMMDD</li>
     *   <li>If cycle debit is zero, substitute 2525.00</li>
     * </ul>
     */
    public static OutputAccountRecord fromAccountRecord(AccountRecord input,
                                                        String formattedReissueDate) {
        BigDecimal cycDebit = input.currCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? new BigDecimal("2525.00")
                : input.currCycDebit();

        return new OutputAccountRecord(
                input.acctId(),
                input.activeStatus(),
                input.currBal(),
                input.creditLimit(),
                input.cashCreditLimit(),
                input.openDate(),
                input.expirationDate(),
                formattedReissueDate,
                input.currCycCredit(),
                cycDebit,
                input.groupId()
        );
    }
}
