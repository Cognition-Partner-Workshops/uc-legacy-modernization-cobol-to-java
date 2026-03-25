package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;

import java.math.BigDecimal;

/**
 * Parses fixed-width COBOL account records from ASCII data files.
 *
 * <p>Handles signed zoned decimal fields that use EBCDIC overpunch
 * encoding on the last byte (e.g., {@code PIC S9(10)V99}).
 */
public final class CobolDataParser {

    private CobolDataParser() {}

    /**
     * EBCDIC overpunch map for the last byte of a signed zoned decimal field.
     * <pre>
     *   '{' = +0, 'A' = +1, ..., 'I' = +9   (positive)
     *   '}' = -0, 'J' = -1, ..., 'R' = -9   (negative)
     * </pre>
     */
    private static int overpunchDigit(char c) {
        return switch (c) {
            case '{' -> 0;
            case 'A' -> 1;
            case 'B' -> 2;
            case 'C' -> 3;
            case 'D' -> 4;
            case 'E' -> 5;
            case 'F' -> 6;
            case 'G' -> 7;
            case 'H' -> 8;
            case 'I' -> 9;
            case '}' -> 0;
            case 'J' -> 1;
            case 'K' -> 2;
            case 'L' -> 3;
            case 'M' -> 4;
            case 'N' -> 5;
            case 'O' -> 6;
            case 'P' -> 7;
            case 'Q' -> 8;
            case 'R' -> 9;
            default -> Character.getNumericValue(c);
        };
    }

    private static boolean isNegativeOverpunch(char c) {
        return c == '}' || (c >= 'J' && c <= 'R');
    }

    /**
     * Parse a signed zoned decimal field (e.g., PIC S9(10)V99) from a
     * fixed-width substring.  The last character may carry an overpunch sign.
     *
     * @param raw           the raw field string
     * @param decimalPlaces number of implied decimal places (V99 = 2)
     * @return parsed BigDecimal value
     */
    public static BigDecimal parseSignedDecimal(String raw, int decimalPlaces) {
        if (raw == null || raw.isBlank()) {
            return BigDecimal.ZERO;
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO;
        }

        char lastChar = trimmed.charAt(trimmed.length() - 1);
        boolean negative = isNegativeOverpunch(lastChar);
        int lastDigit = overpunchDigit(lastChar);

        // Build the numeric string: all chars except last + the decoded last digit
        StringBuilder sb = new StringBuilder(trimmed.length());
        for (int i = 0; i < trimmed.length() - 1; i++) {
            char ch = trimmed.charAt(i);
            if (Character.isDigit(ch)) {
                sb.append(ch);
            }
        }
        sb.append(lastDigit);

        BigDecimal value = new BigDecimal(sb.toString());

        // Apply implied decimal point
        if (decimalPlaces > 0) {
            value = value.movePointLeft(decimalPlaces);
        }

        if (negative) {
            value = value.negate();
        }

        return value;
    }

    /**
     * Parse a single 300-byte account record line from the ASCII data file.
     *
     * <p>Field layout from CVACT01Y:
     * <pre>
     * Offset  Len  Field                   PIC
     * 0       11   ACCT-ID                 9(11)
     * 11       1   ACCT-ACTIVE-STATUS      X(01)
     * 12      12   ACCT-CURR-BAL           S9(10)V99
     * 24      12   ACCT-CREDIT-LIMIT       S9(10)V99
     * 36      12   ACCT-CASH-CREDIT-LIMIT  S9(10)V99
     * 48      10   ACCT-OPEN-DATE          X(10)
     * 58      10   ACCT-EXPIRAION-DATE     X(10)
     * 68      10   ACCT-REISSUE-DATE       X(10)
     * 78      12   ACCT-CURR-CYC-CREDIT    S9(10)V99
     * 90      12   ACCT-CURR-CYC-DEBIT     S9(10)V99
     * 102     10   ACCT-ADDR-ZIP           X(10)
     * 112     10   ACCT-GROUP-ID           X(10)
     * 122    178   FILLER                  X(178)
     * </pre>
     */
    public static AccountRecord parseLine(String line) {
        // Pad short lines to full record length
        String padded = line.length() < AccountRecord.RECORD_LENGTH
                ? String.format("%-" + AccountRecord.RECORD_LENGTH + "s", line)
                : line;

        long acctId = Long.parseLong(padded.substring(0, 11).trim());
        String activeStatus = padded.substring(11, 12);
        BigDecimal currBal = parseSignedDecimal(padded.substring(12, 24), 2);
        BigDecimal creditLimit = parseSignedDecimal(padded.substring(24, 36), 2);
        BigDecimal cashCreditLimit = parseSignedDecimal(padded.substring(36, 48), 2);
        String openDate = padded.substring(48, 58).trim();
        String expirationDate = padded.substring(58, 68).trim();
        String reissueDate = padded.substring(68, 78).trim();
        BigDecimal currCycCredit = parseSignedDecimal(padded.substring(78, 90), 2);
        BigDecimal currCycDebit = parseSignedDecimal(padded.substring(90, 102), 2);
        String addrZip = padded.substring(102, 112).trim();
        String groupId = padded.substring(112, 122).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }
}
