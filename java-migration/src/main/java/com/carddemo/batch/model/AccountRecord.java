package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps the CVACT01Y copybook — the 300-byte account record layout.
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                   PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS        PIC X(01).
 *     05  ACCT-CURR-BAL             PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT         PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT    PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE            PIC X(10).
 *     05  ACCT-EXPIRAION-DATE       PIC X(10).
 *     05  ACCT-REISSUE-DATE         PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT      PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT       PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP             PIC X(10).
 *     05  ACCT-GROUP-ID             PIC X(10).
 *     05  FILLER                    PIC X(178).
 * </pre>
 */
public record AccountRecord(
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
        String addrZip,
        String groupId
) {

    /** Total record length in the fixed-width representation. */
    public static final int RECORD_LENGTH = 300;

    // Field widths matching the COBOL PIC clauses
    private static final int W_ACCT_ID            = 11;
    private static final int W_ACTIVE_STATUS       = 1;
    private static final int W_SIGNED_DECIMAL      = 12; // S9(10)V99 → sign + 10 + 2
    private static final int W_DATE                = 10;
    private static final int W_ZIP                 = 10;
    private static final int W_GROUP_ID            = 10;

    /**
     * Parses a fixed-width line into an {@code AccountRecord}.
     * The line layout mirrors the CVACT01Y copybook field order.
     */
    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }
        int pos = 0;

        long acctId = Long.parseLong(line.substring(pos, pos += W_ACCT_ID).trim());
        String activeStatus = line.substring(pos, pos += W_ACTIVE_STATUS);
        BigDecimal currBal = parseSignedDecimal(line.substring(pos, pos += W_SIGNED_DECIMAL));
        BigDecimal creditLimit = parseSignedDecimal(line.substring(pos, pos += W_SIGNED_DECIMAL));
        BigDecimal cashCreditLimit = parseSignedDecimal(line.substring(pos, pos += W_SIGNED_DECIMAL));
        String openDate = line.substring(pos, pos += W_DATE).trim();
        String expirationDate = line.substring(pos, pos += W_DATE).trim();
        String reissueDate = line.substring(pos, pos += W_DATE).trim();
        BigDecimal currCycCredit = parseSignedDecimal(line.substring(pos, pos += W_SIGNED_DECIMAL));
        BigDecimal currCycDebit = parseSignedDecimal(line.substring(pos, pos += W_SIGNED_DECIMAL));
        String addrZip = line.substring(pos, pos += W_ZIP).trim();
        String groupId = line.substring(pos, pos += W_GROUP_ID).trim();

        return new AccountRecord(acctId, activeStatus, currBal, creditLimit,
                cashCreditLimit, openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId);
    }

    /**
     * Formats this record back to the 300-byte fixed-width layout.
     */
    public String toFixedWidth() {
        StringBuilder sb = new StringBuilder(RECORD_LENGTH);
        sb.append(String.format("%0" + W_ACCT_ID + "d", acctId));
        sb.append(activeStatus);
        sb.append(formatSignedDecimal(currBal));
        sb.append(formatSignedDecimal(creditLimit));
        sb.append(formatSignedDecimal(cashCreditLimit));
        sb.append(String.format("%-" + W_DATE + "s", openDate));
        sb.append(String.format("%-" + W_DATE + "s", expirationDate));
        sb.append(String.format("%-" + W_DATE + "s", reissueDate));
        sb.append(formatSignedDecimal(currCycCredit));
        sb.append(formatSignedDecimal(currCycDebit));
        sb.append(String.format("%-" + W_ZIP + "s", addrZip));
        sb.append(String.format("%-" + W_GROUP_ID + "s", groupId));
        // Pad FILLER to reach 300
        while (sb.length() < RECORD_LENGTH) {
            sb.append(' ');
        }
        return sb.toString();
    }

    /**
     * Parses a COBOL-style signed decimal string (S9(10)V99) into a BigDecimal.
     * Expects a 12-character field: optional sign indicator followed by digits,
     * with the last two digits representing the fractional part.
     */
    static BigDecimal parseSignedDecimal(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) {
            return BigDecimal.ZERO;
        }
        boolean negative = s.startsWith("-");
        if (s.startsWith("+") || s.startsWith("-")) {
            s = s.substring(1);
        }
        // Remove leading zeros but keep at least one digit
        s = s.replaceFirst("^0+(?=\\d)", "");
        if (s.length() < 3) {
            s = String.format("%03d", Long.parseLong(s));
        }
        String intPart = s.substring(0, s.length() - 2);
        String fracPart = s.substring(s.length() - 2);
        BigDecimal value = new BigDecimal(intPart + "." + fracPart);
        return negative ? value.negate() : value;
    }

    /**
     * Formats a BigDecimal as a 12-character COBOL-style signed decimal (S9(10)V99).
     */
    static String formatSignedDecimal(BigDecimal value) {
        boolean negative = value.signum() < 0;
        BigDecimal abs = value.abs();
        long unscaled = abs.movePointRight(2).longValue();
        String digits = String.format("%012d", unscaled);
        return (negative ? "-" : "+") + digits.substring(1); // sign + 11 digits = 12 chars
    }
}
