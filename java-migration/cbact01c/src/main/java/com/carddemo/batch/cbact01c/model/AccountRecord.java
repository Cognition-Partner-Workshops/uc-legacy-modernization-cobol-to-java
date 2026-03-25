package com.carddemo.batch.cbact01c.model;

import java.math.BigDecimal;

/**
 * Maps to COBOL copybook CVACT01Y — the 300-byte account master record.
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
        String acctActiveStatus,
        BigDecimal acctCurrBal,
        BigDecimal acctCreditLimit,
        BigDecimal acctCashCreditLimit,
        String acctOpenDate,
        String acctExpirationDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctAddrZip,
        String acctGroupId
) {

    /** Total record length in the mainframe fixed-width file. */
    public static final int RECORD_LENGTH = 300;

    // Field widths matching the COBOL PIC clauses
    private static final int ACCT_ID_LEN = 11;
    private static final int STATUS_LEN = 1;
    private static final int SIGNED_DECIMAL_LEN = 12; // S9(10)V99 zoned decimal
    private static final int DATE_LEN = 10;
    private static final int ZIP_LEN = 10;
    private static final int GROUP_LEN = 10;

    /**
     * Parse a single fixed-width line (300 chars padded) from the ASCII account
     * data file.  COBOL zoned-decimal sign convention: the last byte of a signed
     * field uses an overpunch character ({@code '{'} = +0, {@code 'A'}-{@code 'I'}
     * = +1..+9, {@code '}'} = -0, {@code 'J'}-{@code 'R'} = -1..-9).
     */
    public static AccountRecord parse(String line) {
        if (line.length() < RECORD_LENGTH) {
            line = padRight(line, RECORD_LENGTH);
        }

        int pos = 0;

        long acctId = Long.parseLong(line.substring(pos, pos + ACCT_ID_LEN).trim());
        pos += ACCT_ID_LEN;

        String status = line.substring(pos, pos + STATUS_LEN);
        pos += STATUS_LEN;

        BigDecimal currBal = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal creditLimit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal cashCreditLimit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        String openDate = line.substring(pos, pos + DATE_LEN);
        pos += DATE_LEN;

        String expirationDate = line.substring(pos, pos + DATE_LEN);
        pos += DATE_LEN;

        String reissueDate = line.substring(pos, pos + DATE_LEN);
        pos += DATE_LEN;

        BigDecimal cycCredit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal cycDebit = parseSignedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        String zip = line.substring(pos, pos + ZIP_LEN).trim();
        pos += ZIP_LEN;

        String groupId = line.substring(pos, pos + GROUP_LEN).trim();

        return new AccountRecord(
                acctId, status, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                cycCredit, cycDebit, zip, groupId
        );
    }

    /**
     * Decode a COBOL zoned-decimal field with trailing overpunch sign.
     * The field represents PIC S9(10)V99 — 10 integer digits + 2 decimal digits,
     * stored as 12 display characters with the sign embedded in the last byte.
     */
    public static BigDecimal parseSignedDecimal(String line, int offset, int length) {
        String raw = line.substring(offset, offset + length);
        char lastChar = raw.charAt(raw.length() - 1);

        int sign = 1;
        int lastDigit;

        if (lastChar >= '0' && lastChar <= '9') {
            lastDigit = lastChar - '0';
        } else if (lastChar == '{') {
            lastDigit = 0;
        } else if (lastChar == '}') {
            lastDigit = 0;
            sign = -1;
        } else if (lastChar >= 'A' && lastChar <= 'I') {
            lastDigit = lastChar - 'A' + 1;
        } else if (lastChar >= 'J' && lastChar <= 'R') {
            lastDigit = lastChar - 'J' + 1;
            sign = -1;
        } else {
            lastDigit = 0;
        }

        String digits = raw.substring(0, raw.length() - 1) + lastDigit;
        // Insert decimal point: S9(10)V99 → 10 integer + 2 fractional
        String intPart = digits.substring(0, digits.length() - 2);
        String fracPart = digits.substring(digits.length() - 2);
        BigDecimal value = new BigDecimal(intPart + "." + fracPart);
        return sign < 0 ? value.negate() : value;
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s;
        }
        return s + " ".repeat(len - s.length());
    }
}
