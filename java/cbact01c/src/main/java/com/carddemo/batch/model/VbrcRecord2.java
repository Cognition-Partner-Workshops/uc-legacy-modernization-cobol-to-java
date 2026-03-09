package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL variable-length record type 2 (long record, 39 bytes).
 *
 * <pre>
 *  05  VB2-ACCT-ID                PIC 9(11)
 *  05  VB2-ACCT-CURR-BAL          PIC S9(10)V99   (12 bytes)
 *  05  VB2-ACCT-CREDIT-LIMIT      PIC S9(10)V99   (12 bytes)
 *  05  VB2-ACCT-REISSUE-YYYY      PIC X(04)
 * </pre>
 *
 * Total: 11 + 12 + 12 + 4 = 39 bytes.
 */
public record VbrcRecord2(
        long acctId,
        BigDecimal currBal,
        BigDecimal creditLimit,
        String reissueYear
) {
    public static final int RECORD_LENGTH = 39;

    /**
     * Build from an {@link AccountRecord}.
     * The reissue year is extracted from the YYYY-MM-DD reissue date
     * (first 4 characters), matching the COBOL logic:
     * {@code MOVE WS-ACCT-REISSUE-YYYY TO VB2-ACCT-REISSUE-YYYY}.
     */
    public static VbrcRecord2 fromAccountRecord(AccountRecord acct) {
        String reissueYear = acct.reissueDate().substring(0, 4);
        return new VbrcRecord2(
                acct.acctId(),
                acct.currBal(),
                acct.creditLimit(),
                reissueYear
        );
    }

    public String toFixedWidth() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        sb.append(formatSigned(currBal, 10, 2));
        sb.append(formatSigned(creditLimit, 10, 2));
        sb.append(String.format("%-4s", reissueYear));
        return sb.toString();
    }

    private static String formatSigned(BigDecimal value, int intDigits, int decDigits) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        long unscaled = abs.movePointRight(decDigits).longValue();
        int totalDigits = intDigits + decDigits;
        String digits = String.format("%0" + totalDigits + "d", unscaled);

        char lastChar = digits.charAt(digits.length() - 1);
        int lastDigit = lastChar - '0';
        char overpunch;
        if (negative) {
            overpunch = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            overpunch = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }
        return digits.substring(0, digits.length() - 1) + overpunch;
    }
}
