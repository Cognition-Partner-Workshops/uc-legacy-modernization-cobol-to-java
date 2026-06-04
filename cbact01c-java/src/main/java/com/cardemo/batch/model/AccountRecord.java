package com.cardemo.batch.model;

import java.math.BigDecimal;

/**
 * Maps to CVACT01Y.cpy ACCOUNT-RECORD (300-byte VSAM record).
 * Field lengths match the COBOL PIC definitions exactly.
 */
public record AccountRecord(
        long acctId,                    // PIC 9(11)
        String activeStatus,            // PIC X(01)
        BigDecimal currBal,             // PIC S9(10)V99
        BigDecimal creditLimit,         // PIC S9(10)V99
        BigDecimal cashCreditLimit,     // PIC S9(10)V99
        String openDate,                // PIC X(10)
        String expirationDate,          // PIC X(10)  (note: COBOL typo "EXPIRAION")
        String reissueDate,             // PIC X(10)
        BigDecimal currCycCredit,       // PIC S9(10)V99
        BigDecimal currCycDebit,        // PIC S9(10)V99
        String addrZip,                 // PIC X(10)
        String groupId                  // PIC X(10)
) {
    public static final int RECORD_LENGTH = 300;

    public static AccountRecord parse(String line) {
        if (line.length() < 122) {
            throw new IllegalArgumentException(
                    "Account record too short: " + line.length() + " chars (need at least 122)");
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
        String addrZip = line.substring(pos, pos + 10);
        pos += 10;
        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(acctId, activeStatus, currBal, creditLimit,
                cashCreditLimit, openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId);
    }

    private static BigDecimal parseSignedDecimal(String raw, int scale) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return BigDecimal.ZERO.setScale(scale);
        }
        return new BigDecimal(trimmed).setScale(scale, java.math.RoundingMode.HALF_UP);
    }

    public String format() {
        return String.format("%-11d%-1s%12s%12s%12s%-10s%-10s%-10s%12s%12s%-10s%-10s",
                acctId, activeStatus,
                formatSignedDecimal(currBal),
                formatSignedDecimal(creditLimit),
                formatSignedDecimal(cashCreditLimit),
                openDate, expirationDate, reissueDate,
                formatSignedDecimal(currCycCredit),
                formatSignedDecimal(currCycDebit),
                addrZip, groupId);
    }

    private static String formatSignedDecimal(BigDecimal val) {
        String str = val.toPlainString();
        if (str.length() > 12) {
            return str.substring(0, 12);
        }
        return String.format("%12s", str);
    }
}
