package com.carddemo.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y -- 300-byte account master record.
 */
public record AccountRecord(
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
        String addressZip,
        String groupId
) {

    private static final int RECORD_LENGTH = 300;

    /**
     * Parse a fixed-length (300-char) account record line that mirrors the
     * CVACT01Y copybook layout in display (zoned-decimal) format.
     *
     * Field offsets (0-based):
     *   ACCT-ID               0..10   PIC 9(11)
     *   ACCT-ACTIVE-STATUS   11..11   PIC X(01)
     *   ACCT-CURR-BAL        12..23   PIC S9(10)V99  (display, 12 chars)
     *   ACCT-CREDIT-LIMIT    24..35   PIC S9(10)V99
     *   ACCT-CASH-CREDIT-LIM 36..47   PIC S9(10)V99
     *   ACCT-OPEN-DATE       48..57   PIC X(10)
     *   ACCT-EXPIRAION-DATE  58..67   PIC X(10)
     *   ACCT-REISSUE-DATE    68..77   PIC X(10)
     *   ACCT-CURR-CYC-CREDIT 78..89   PIC S9(10)V99
     *   ACCT-CURR-CYC-DEBIT  90..101  PIC S9(10)V99
     *   ACCT-ADDR-ZIP       102..111  PIC X(10)
     *   ACCT-GROUP-ID       112..121  PIC X(10)
     *   FILLER              122..299  PIC X(178)
     */
    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }

        long acctId = Long.parseLong(line.substring(0, 11).trim());
        String activeStatus = line.substring(11, 12);
        BigDecimal currBal = parseSignedDecimal(line.substring(12, 24));
        BigDecimal creditLimit = parseSignedDecimal(line.substring(24, 36));
        BigDecimal cashCreditLimit = parseSignedDecimal(line.substring(36, 48));
        String openDate = line.substring(48, 58);
        String expirationDate = line.substring(58, 68);
        String reissueDate = line.substring(68, 78);
        BigDecimal currCycCredit = parseSignedDecimal(line.substring(78, 90));
        BigDecimal currCycDebit = parseSignedDecimal(line.substring(90, 102));
        String addressZip = line.substring(102, 112);
        String groupId = line.substring(112, 122);

        return new AccountRecord(acctId, activeStatus, currBal, creditLimit,
                cashCreditLimit, openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addressZip, groupId);
    }

    /**
     * Parse a COBOL S9(10)V99 display-numeric field (12 characters) into a
     * BigDecimal with scale 2.  Handles leading sign (+/-) or trailing sign
     * conventions, as well as plain unsigned digits.
     */
    private static BigDecimal parseSignedDecimal(String raw) {
        String s = raw.trim();
        if (s.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }

        boolean negative = false;
        if (s.startsWith("-")) {
            negative = true;
            s = s.substring(1);
        } else if (s.startsWith("+")) {
            s = s.substring(1);
        } else if (s.endsWith("-")) {
            negative = true;
            s = s.substring(0, s.length() - 1);
        } else if (s.endsWith("+")) {
            s = s.substring(0, s.length() - 1);
        }

        if (s.contains(".")) {
            BigDecimal val = new BigDecimal(s);
            return negative ? val.negate() : val;
        }

        if (s.length() <= 2) {
            s = "0".repeat(3 - s.length()) + s;
        }
        String intPart = s.substring(0, s.length() - 2);
        String decPart = s.substring(s.length() - 2);
        BigDecimal val = new BigDecimal(intPart + "." + decPart);
        return negative ? val.negate() : val;
    }
}
