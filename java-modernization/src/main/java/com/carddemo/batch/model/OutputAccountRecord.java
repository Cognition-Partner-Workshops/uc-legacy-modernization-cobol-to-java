package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the OUT-ACCT-REC file-descriptor in CBACT01C.
 * <p>
 * COBOL layout (all DISPLAY unless noted):
 * <pre>
 *  05  OUT-ACCT-ID                PIC 9(11)
 *  05  OUT-ACCT-ACTIVE-STATUS     PIC X(01)
 *  05  OUT-ACCT-CURR-BAL          PIC S9(10)V99
 *  05  OUT-ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *  05  OUT-ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  05  OUT-ACCT-OPEN-DATE         PIC X(10)
 *  05  OUT-ACCT-EXPIRAION-DATE    PIC X(10)
 *  05  OUT-ACCT-REISSUE-DATE      PIC X(10)
 *  05  OUT-ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *  05  OUT-ACCT-CURR-CYC-DEBIT    PIC S9(10)V99 USAGE IS COMP-3
 *  05  OUT-ACCT-GROUP-ID          PIC X(10)
 * </pre>
 */
public record OutputAccountRecord(
        long acctId,
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpiraionDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctGroupId
) {

    /**
     * Format as a fixed-width text line that mirrors the COBOL WRITE output.
     * <p>
     * The COMP-3 field is represented here as a plain signed decimal string
     * for readability and test comparison purposes.
     */
    public String toFixedWidth() {
        var sb = new StringBuilder();
        sb.append(String.format("%011d", acctId));
        sb.append(acctActiveStatus);
        sb.append(formatSignedZoned(acctCurrBal));
        sb.append(formatSignedZoned(acctCreditLimit));
        sb.append(formatSignedZoned(acctCashCreditLimit));
        sb.append(String.format("%-10s", acctOpenDate));
        sb.append(String.format("%-10s", acctExpiraionDate));
        sb.append(String.format("%-10s", acctReissueDate));
        sb.append(formatSignedZoned(acctCurrCycCredit));
        sb.append(formatComp3Display(acctCurrCycDebit));
        sb.append(String.format("%-10s", acctGroupId));
        return sb.toString();
    }

    /**
     * Format a BigDecimal as a COBOL-style signed zoned decimal
     * PIC S9(10)V99 (12-character ASCII representation with trailing sign overpunch).
     */
    public static String formatSignedZoned(BigDecimal value) {
        boolean negative = value.signum() < 0;
        long cents = value.abs().movePointRight(2).longValueExact();
        String digits = String.format("%012d", cents);

        int lastDigit = digits.charAt(11) - '0';
        char signChar;
        if (negative) {
            signChar = lastDigit == 0 ? '}' : (char) ('J' + lastDigit - 1);
        } else {
            signChar = lastDigit == 0 ? '{' : (char) ('A' + lastDigit - 1);
        }
        return digits.substring(0, 11) + signChar;
    }

    /**
     * Format a COMP-3 (packed decimal) field as a readable signed string.
     * In the real mainframe this would be binary packed; here we output a
     * human-readable representation for comparison purposes: sign + 12 digits
     * with implied V99 decimal.
     */
    public static String formatComp3Display(BigDecimal value) {
        boolean negative = value.signum() < 0;
        long cents = value.abs().movePointRight(2).longValueExact();
        String digits = String.format("%012d", cents);
        return (negative ? "-" : "+") + digits;
    }
}
