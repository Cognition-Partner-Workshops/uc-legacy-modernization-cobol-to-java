package com.carddemo.batch.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Maps to COBOL copybook CVACT01Y.cpy — ACCOUNT-RECORD (300 bytes).
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

    /** Total record length in the COBOL flat file. */
    public static final int RECORD_LENGTH = 300;

    /**
     * Parse a 300-byte fixed-length ASCII line into an {@code AccountRecord}.
     * Zoned-decimal sign encoding: last byte {@code '{'} = +0 … {@code 'I'} = +9,
     * {@code '}'} = -0 … {@code 'R'} = -9.
     */
    public static AccountRecord parse(String line) {
        Objects.requireNonNull(line, "line must not be null");

        // Pad to at least RECORD_LENGTH so substring calls are safe
        String padded = line.length() >= RECORD_LENGTH
                ? line
                : String.format("%-" + RECORD_LENGTH + "s", line);

        int pos = 0;
        long acctId = Long.parseLong(padded.substring(pos, pos + 11).trim());
        pos += 11;

        String activeStatus = padded.substring(pos, pos + 1);
        pos += 1;

        BigDecimal currBal = parseZonedDecimal(padded, pos, 12, 2);
        pos += 12;

        BigDecimal creditLimit = parseZonedDecimal(padded, pos, 12, 2);
        pos += 12;

        BigDecimal cashCreditLimit = parseZonedDecimal(padded, pos, 12, 2);
        pos += 12;

        String openDate = padded.substring(pos, pos + 10);
        pos += 10;

        String expiraionDate = padded.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = padded.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currCycCredit = parseZonedDecimal(padded, pos, 12, 2);
        pos += 12;

        BigDecimal currCycDebit = parseZonedDecimal(padded, pos, 12, 2);
        pos += 12;

        String addrZip = padded.substring(pos, pos + 10).trim();
        pos += 10;

        String groupId = padded.substring(pos, pos + 10).trim();

        return new AccountRecord(
                acctId, activeStatus,
                currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit,
                addrZip, groupId
        );
    }

    /**
     * Decode a COBOL zoned-decimal (DISPLAY) field from ASCII text.
     * Handles trailing-sign overpunch characters used in mainframe-to-ASCII
     * file transfers (e.g. {@code '{'} = +0, {@code '}'} = -0).
     *
     * @param line   the full record line
     * @param offset start position of the field
     * @param len    total character length of the field (including sign byte)
     * @param scale  implied decimal places (V99 → 2)
     * @return the decoded {@link BigDecimal} value
     */
    static BigDecimal parseZonedDecimal(String line, int offset, int len, int scale) {
        String raw = line.substring(offset, offset + len);
        char lastChar = raw.charAt(len - 1);
        String digits = raw.substring(0, len - 1);

        int lastDigit;
        boolean negative;

        switch (lastChar) {
            // Positive overpunch: { = +0, A–I = +1 … +9
            case '{' -> { lastDigit = 0; negative = false; }
            case 'A' -> { lastDigit = 1; negative = false; }
            case 'B' -> { lastDigit = 2; negative = false; }
            case 'C' -> { lastDigit = 3; negative = false; }
            case 'D' -> { lastDigit = 4; negative = false; }
            case 'E' -> { lastDigit = 5; negative = false; }
            case 'F' -> { lastDigit = 6; negative = false; }
            case 'G' -> { lastDigit = 7; negative = false; }
            case 'H' -> { lastDigit = 8; negative = false; }
            case 'I' -> { lastDigit = 9; negative = false; }
            // Negative overpunch: } = -0, J–R = -1 … -9
            case '}' -> { lastDigit = 0; negative = true; }
            case 'J' -> { lastDigit = 1; negative = true; }
            case 'K' -> { lastDigit = 2; negative = true; }
            case 'L' -> { lastDigit = 3; negative = true; }
            case 'M' -> { lastDigit = 4; negative = true; }
            case 'N' -> { lastDigit = 5; negative = true; }
            case 'O' -> { lastDigit = 6; negative = true; }
            case 'P' -> { lastDigit = 7; negative = true; }
            case 'Q' -> { lastDigit = 8; negative = true; }
            case 'R' -> { lastDigit = 9; negative = true; }
            // Plain digit (no overpunch)
            default -> {
                if (Character.isDigit(lastChar)) {
                    lastDigit = lastChar - '0';
                    negative = false;
                } else {
                    throw new IllegalArgumentException(
                            "Unrecognised zoned-decimal sign character: '" + lastChar + "'");
                }
            }
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits).movePointLeft(scale);
        return negative ? value.negate() : value;
    }
}
