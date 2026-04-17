package com.carddemo.batch.model;

import java.math.BigDecimal;

/**
 * Java equivalent of the CVACT01Y copybook ACCOUNT-RECORD layout.
 * <p>
 * COBOL record length: 300 bytes (FB).
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
        String acctExpiraionDate,
        String acctReissueDate,
        BigDecimal acctCurrCycCredit,
        BigDecimal acctCurrCycDebit,
        String acctAddrZip,
        String acctGroupId
) {

    /** Total record length in the flat file. */
    public static final int RECORD_LENGTH = 300;

    /** Length of signed zoned-decimal field PIC S9(10)V99. */
    private static final int SIGNED_DECIMAL_LEN = 12;

    /**
     * Parse a 300-byte fixed-length ASCII line into an {@code AccountRecord}.
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

        BigDecimal currBal = parseSignedZonedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal creditLimit = parseSignedZonedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal cashCreditLimit = parseSignedZonedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expiraionDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currCycCredit = parseSignedZonedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        BigDecimal currCycDebit = parseSignedZonedDecimal(line, pos, SIGNED_DECIMAL_LEN);
        pos += SIGNED_DECIMAL_LEN;

        String addrZip = line.substring(pos, pos + 10);
        pos += 10;

        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    /**
     * Decode an ASCII zoned-decimal field with an embedded trailing sign.
     * <p>
     * In the ASCII representation used by the sample data the last byte
     * encodes both the sign and the units digit:
     * <ul>
     *   <li>Positive: {@code {=0  A=1  B=2  C=3  D=4  E=5  F=6  G=7  H=8  I=9}</li>
     *   <li>Negative: {@code }=0  J=1  K=2  L=3  M=4  N=5  O=6  P=7  Q=8  R=9}</li>
     * </ul>
     * The implied decimal point (V99) means the last two digit positions
     * represent cents.
     */
    public static BigDecimal parseSignedZonedDecimal(String line, int offset, int length) {
        String raw = line.substring(offset, offset + length);
        char lastChar = raw.charAt(length - 1);

        int sign = 1;
        int lastDigit;

        if (lastChar == '{') {
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
            lastDigit = lastChar - '0';
        }

        String digits = raw.substring(0, length - 1) + lastDigit;
        BigDecimal value = new BigDecimal(digits).movePointLeft(2);
        return sign < 0 ? value.negate() : value;
    }
}
