package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the OUT-ACCT-REC file descriptor — the flat output record written by CBACT01C.
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
 *    05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 COMP-3.
 *    05  OUT-ACCT-GROUP-ID          PIC X(10).
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

    private static final int W_ACCT_ID         = 11;
    private static final int W_STATUS          = 1;
    private static final int W_SIGNED_DECIMAL  = 12;
    private static final int W_DATE            = 10;
    private static final int W_GROUP_ID        = 10;

    /**
     * Serialises this record to a pipe-delimited line.
     * In the original COBOL the CURR-CYC-DEBIT field used COMP-3 (packed decimal).
     * In the Java port all numeric fields are rendered as plain text decimals.
     */
    public String toDelimited() {
        return String.join("|",
                String.format("%0" + W_ACCT_ID + "d", acctId),
                activeStatus,
                currBal.toPlainString(),
                creditLimit.toPlainString(),
                cashCreditLimit.toPlainString(),
                String.format("%-" + W_DATE + "s", openDate),
                String.format("%-" + W_DATE + "s", expirationDate),
                String.format("%-" + W_DATE + "s", reissueDate),
                currCycCredit.toPlainString(),
                currCycDebit.toPlainString(),
                String.format("%-" + W_GROUP_ID + "s", groupId)
        );
    }

    /**
     * Parses a pipe-delimited line back into an {@code OutputAccountRecord}.
     */
    public static OutputAccountRecord parseDelimited(String line) {
        String[] parts = line.split("\\|", -1);
        return new OutputAccountRecord(
                Long.parseLong(parts[0].trim()),
                parts[1],
                new BigDecimal(parts[2].trim()),
                new BigDecimal(parts[3].trim()),
                new BigDecimal(parts[4].trim()),
                parts[5].trim(),
                parts[6].trim(),
                parts[7].trim(),
                new BigDecimal(parts[8].trim()),
                new BigDecimal(parts[9].trim()),
                parts[10].trim()
        );
    }
}
