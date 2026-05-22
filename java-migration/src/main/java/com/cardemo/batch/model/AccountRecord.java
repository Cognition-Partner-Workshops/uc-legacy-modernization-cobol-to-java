package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y — the 300-byte account master record.
 *
 * <pre>
 * 01  ACCOUNT-RECORD.
 *     05  ACCT-ID                 PIC 9(11).
 *     05  ACCT-ACTIVE-STATUS      PIC X(01).
 *     05  ACCT-CURR-BAL           PIC S9(10)V99.
 *     05  ACCT-CREDIT-LIMIT       PIC S9(10)V99.
 *     05  ACCT-CASH-CREDIT-LIMIT  PIC S9(10)V99.
 *     05  ACCT-OPEN-DATE          PIC X(10).
 *     05  ACCT-EXPIRAION-DATE     PIC X(10).
 *     05  ACCT-REISSUE-DATE       PIC X(10).
 *     05  ACCT-CURR-CYC-CREDIT    PIC S9(10)V99.
 *     05  ACCT-CURR-CYC-DEBIT     PIC S9(10)V99.
 *     05  ACCT-ADDR-ZIP           PIC X(10).
 *     05  ACCT-GROUP-ID           PIC X(10).
 *     05  FILLER                  PIC X(178).
 * </pre>
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

    public static final int RECORD_LENGTH = 300;

    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = String.format("%-" + RECORD_LENGTH + "s", line);
        }
        int pos = 0;
        long acctId = Long.parseLong(line.substring(pos, pos + 11).trim()); pos += 11;
        String activeStatus = line.substring(pos, pos + 1); pos += 1;
        BigDecimal currBal = parseSignedDecimal(line, pos, 12); pos += 12;
        BigDecimal creditLimit = parseSignedDecimal(line, pos, 12); pos += 12;
        BigDecimal cashCreditLimit = parseSignedDecimal(line, pos, 12); pos += 12;
        String openDate = line.substring(pos, pos + 10); pos += 10;
        String expirationDate = line.substring(pos, pos + 10); pos += 10;
        String reissueDate = line.substring(pos, pos + 10); pos += 10;
        BigDecimal currCycCredit = parseSignedDecimal(line, pos, 12); pos += 12;
        BigDecimal currCycDebit = parseSignedDecimal(line, pos, 12); pos += 12;
        String addressZip = line.substring(pos, pos + 10); pos += 10;
        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addressZip, groupId
        );
    }

    /**
     * Parses a COBOL-style signed decimal field (PIC S9(10)V99)
     * stored as a zoned-decimal display string.
     */
    private static BigDecimal parseSignedDecimal(String line, int offset, int length) {
        String raw = line.substring(offset, offset + length).trim();
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        boolean negative = raw.startsWith("-");
        String digits = raw.replace("+", "").replace("-", "");
        if (digits.contains(".")) {
            BigDecimal value = new BigDecimal(digits);
            return negative ? value.negate() : value;
        }
        if (digits.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal value = new BigDecimal(digits).movePointLeft(2);
        return negative ? value.negate() : value;
    }

    public String formatDisplay() {
        return String.format(
                """
                ACCT-ID                 :%011d
                ACCT-ACTIVE-STATUS      :%s
                ACCT-CURR-BAL           :%s
                ACCT-CREDIT-LIMIT       :%s
                ACCT-CASH-CREDIT-LIMIT  :%s
                ACCT-OPEN-DATE          :%s
                ACCT-EXPIRAION-DATE     :%s
                ACCT-REISSUE-DATE       :%s
                ACCT-CURR-CYC-CREDIT    :%s
                ACCT-CURR-CYC-DEBIT     :%s
                ACCT-GROUP-ID           :%s
                -------------------------------------------------""",
                acctId, activeStatus,
                formatCobolDecimal(currentBalance),
                formatCobolDecimal(creditLimit),
                formatCobolDecimal(cashCreditLimit),
                openDate, expirationDate, reissueDate,
                formatCobolDecimal(currentCycleCredit),
                formatCobolDecimal(currentCycleDebit),
                groupId
        );
    }

    private static String formatCobolDecimal(BigDecimal value) {
        boolean neg = value.signum() < 0;
        BigDecimal abs = value.abs().setScale(2);
        String digits = abs.toPlainString().replace(".", "");
        String padded = "0".repeat(Math.max(0, 12 - digits.length())) + digits;
        return (neg ? "-" : "+") + padded;
    }
}
