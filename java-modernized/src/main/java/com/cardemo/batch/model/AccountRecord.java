package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Corresponds to COBOL copybook CVACT01Y (ACCOUNT-RECORD, RECLN 300).
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                  PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS       PIC X(01).
 *     05  ACCT-CURR-BAL            PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT        PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT   PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE           PIC X(10).
 *     05  ACCT-EXPIRAION-DATE      PIC X(10).
 *     05  ACCT-REISSUE-DATE        PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT     PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT      PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP            PIC X(10).
 *     05  ACCT-GROUP-ID            PIC X(10).
 *     05  FILLER                   PIC X(178).
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

    public static final int RECORD_LENGTH = 300;

    private static final int ACCT_ID_LEN = 11;
    private static final int ACTIVE_STATUS_LEN = 1;
    private static final int SIGNED_DECIMAL_LEN = 12; // S9(10)V99
    private static final int DATE_LEN = 10;

    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }
        int pos = 0;

        long acctId = Long.parseLong(line.substring(pos, pos + ACCT_ID_LEN));
        pos += ACCT_ID_LEN;

        String activeStatus = line.substring(pos, pos + ACTIVE_STATUS_LEN);
        pos += ACTIVE_STATUS_LEN;

        BigDecimal currBal = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal creditLimit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal cashCreditLimit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        String openDate = line.substring(pos, pos + DATE_LEN).trim();
        pos += DATE_LEN;

        String expirationDate = line.substring(pos, pos + DATE_LEN).trim();
        pos += DATE_LEN;

        String reissueDate = line.substring(pos, pos + DATE_LEN).trim();
        pos += DATE_LEN;

        BigDecimal currCycCredit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal currCycDebit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

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
     * Parses a COBOL zoned-decimal field (PIC S9(n)V99) from ASCII representation.
     * The sign is overpunched on the last character:
     * <ul>
     *   <li>{@code {,A-I} → positive 0-9</li>
     *   <li>{@code },J-R} → negative 0-9</li>
     * </ul>
     */
    static BigDecimal parseSignedDecimal(String line, int offset, int length) {
        String raw = line.substring(offset, offset + length);
        char lastChar = raw.charAt(raw.length() - 1);
        int lastDigit;
        boolean negative;

        if (lastChar == '{') {
            lastDigit = 0;
            negative = false;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
            negative = false;
        } else if (lastChar == '}') {
            lastDigit = 0;
            negative = true;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            negative = true;
        } else {
            lastDigit = Character.getNumericValue(lastChar);
            negative = false;
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(2);
        return negative ? value.negate() : value;
    }
}
