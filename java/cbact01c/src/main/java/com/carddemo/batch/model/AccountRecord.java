package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to the COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300 bytes).
 *
 * <pre>
 *  05  ACCT-ID                  PIC 9(11)
 *  05  ACCT-ACTIVE-STATUS       PIC X(01)
 *  05  ACCT-CURR-BAL            PIC S9(10)V99
 *  05  ACCT-CREDIT-LIMIT        PIC S9(10)V99
 *  05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99
 *  05  ACCT-OPEN-DATE           PIC X(10)
 *  05  ACCT-EXPIRAION-DATE      PIC X(10)
 *  05  ACCT-REISSUE-DATE        PIC X(10)
 *  05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99
 *  05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99
 *  05  ACCT-ADDR-ZIP            PIC X(10)
 *  05  ACCT-GROUP-ID            PIC X(10)
 *  05  FILLER                   PIC X(178)
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

    /** Total fixed-width record length in the COBOL data file. */
    public static final int RECORD_LENGTH = 300;

    /**
     * Parse a fixed-width line (matching the COBOL ACCOUNT-RECORD layout)
     * into an {@code AccountRecord}.
     * <p>
     * The COBOL signed-numeric fields use a trailing overpunch sign convention
     * where the last digit of a PIC S9(n)V99 field encodes the sign.
     * '{' = +0, 'A'-'I' = +1..+9, '}' = -0, 'J'-'R' = -1..-9.
     */
    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }

        int pos = 0;

        long acctId = Long.parseLong(line.substring(pos, pos + 11).trim());
        pos += 11;

        String activeStatus = line.substring(pos, pos + 1);
        pos += 1;

        BigDecimal currBal = parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal creditLimit = parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal cashCreditLimit = parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expirationDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currCycCredit = parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal currCycDebit = parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String addrZip = line.substring(pos, pos + 10).trim();
        pos += 10;

        String groupId = line.substring(pos, pos + 10).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    /**
     * Decode a COBOL zoned-decimal field with trailing overpunch sign.
     * <p>
     * The last character encodes the sign and the units digit:
     * <ul>
     *   <li>{@code {  } → +0</li>
     *   <li>{@code A-I} → +1 .. +9</li>
     *   <li>{@code }  } → -0</li>
     *   <li>{@code J-R} → -1 .. -9</li>
     * </ul>
     *
     * @param raw    the raw fixed-width string
     * @param scale  number of implied decimal places
     * @return the decoded value
     */
    public static BigDecimal parseSignedDecimal(String raw, int scale) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        char last = raw.charAt(raw.length() - 1);
        String digits = raw.substring(0, raw.length() - 1);
        int sign = 1;
        int lastDigit;

        if (last >= '0' && last <= '9') {
            lastDigit = last - '0';
        } else if (last == '{') {
            lastDigit = 0;
        } else if (last >= 'A' && last <= 'I') {
            lastDigit = last - 'A' + 1;
        } else if (last == '}') {
            lastDigit = 0;
            sign = -1;
        } else if (last >= 'J' && last <= 'R') {
            lastDigit = last - 'J' + 1;
            sign = -1;
        } else {
            lastDigit = 0;
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits.trim()).movePointLeft(scale);
        return sign < 0 ? value.negate() : value;
    }
}
