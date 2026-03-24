package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads account records from a fixed-width flat file (ASCII representation
 * of the COBOL VSAM KSDS ACCTFILE).
 *
 * The file has 300-byte records matching the CVACT01Y.cpy layout.
 * Signed numeric fields use zoned-decimal encoding where the last byte
 * carries the sign:
 * <ul>
 *   <li>{@code {} = +0, {@code A} = +1, ... {@code I} = +9 (positive)</li>
 *   <li>{@code }} = -0, {@code J} = -1, ... {@code R} = -9 (negative)</li>
 * </ul>
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path filePath) throws IOException {
        this.reader = Files.newBufferedReader(filePath);
    }

    /**
     * Reads the next account record from the file.
     *
     * @return the next AccountRecord, or empty if EOF
     * @throws IOException on I/O error
     */
    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return Optional.empty();
        }
        // Pad or trim to exactly 300 characters
        line = padRight(line, AccountRecord.RECORD_LENGTH);
        return Optional.of(parseLine(line));
    }

    /**
     * Reads all records from the file.
     */
    public List<AccountRecord> readAll() throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        Optional<AccountRecord> rec;
        while ((rec = readNext()).isPresent()) {
            records.add(rec.get());
        }
        return records;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Parses a single 300-character line into an AccountRecord.
     *
     * Field layout (offset, length):
     * <pre>
     *   0-10   (11) ACCT-ID           PIC 9(11)
     *  11-11    (1) ACCT-ACTIVE-STATUS PIC X(01)
     *  12-23   (12) ACCT-CURR-BAL     PIC S9(10)V99
     *  24-35   (12) ACCT-CREDIT-LIMIT PIC S9(10)V99
     *  36-47   (12) ACCT-CASH-CREDIT  PIC S9(10)V99
     *  48-57   (10) ACCT-OPEN-DATE    PIC X(10)
     *  58-67   (10) ACCT-EXPIRAION-DATE PIC X(10)
     *  68-77   (10) ACCT-REISSUE-DATE PIC X(10)
     *  78-89   (12) ACCT-CURR-CYC-CREDIT PIC S9(10)V99
     *  90-101  (12) ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
     * 102-111  (10) ACCT-ADDR-ZIP     PIC X(10)
     * 112-121  (10) ACCT-GROUP-ID     PIC X(10)
     * 122-299 (178) FILLER            PIC X(178)
     * </pre>
     */
    static AccountRecord parseLine(String line) {
        String acctId = line.substring(0, 11);
        String activeStatus = line.substring(11, 12);
        BigDecimal currBal = parseSignedDecimal(line.substring(12, 24), 2);
        BigDecimal creditLimit = parseSignedDecimal(line.substring(24, 36), 2);
        BigDecimal cashCreditLimit = parseSignedDecimal(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58).trim();
        String expiraionDate = line.substring(58, 68).trim();
        String reissueDate = line.substring(68, 78).trim();
        BigDecimal currCycCredit = parseSignedDecimal(line.substring(78, 90), 2);
        BigDecimal currCycDebit = parseSignedDecimal(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112).trim();
        String groupId = line.substring(112, 122).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    /**
     * Parses a COBOL zoned-decimal field (PIC S9(n)V9(scale)) from its
     * ASCII representation.
     *
     * The last character encodes both the sign and the last digit:
     * <ul>
     *   <li>Positive: { = 0, A = 1, B = 2, C = 3, D = 4,
     *                   E = 5, F = 6, G = 7, H = 8, I = 9</li>
     *   <li>Negative: } = 0, J = 1, K = 2, L = 3, M = 4,
     *                   N = 5, O = 6, P = 7, Q = 8, R = 9</li>
     * </ul>
     *
     * @param field the raw field string
     * @param scale number of implied decimal places (V99 means scale = 2)
     * @return the parsed BigDecimal value
     */
    static BigDecimal parseSignedDecimal(String field, int scale) {
        if (field == null || field.isBlank()) {
            return BigDecimal.ZERO;
        }

        char lastChar = field.charAt(field.length() - 1);
        String digits = field.substring(0, field.length() - 1);
        int lastDigit;
        boolean negative;

        switch (lastChar) {
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
            default -> {
                // If the last char is a plain digit (no sign encoding),
                // treat as positive
                if (Character.isDigit(lastChar)) {
                    lastDigit = Character.getNumericValue(lastChar);
                    negative = false;
                } else {
                    throw new IllegalArgumentException(
                            "Invalid zoned-decimal sign character: '" + lastChar + "' in field: " + field);
                }
            }
        }

        String fullDigits = digits + lastDigit;
        BigDecimal value = new BigDecimal(fullDigits)
                .movePointLeft(scale)
                .round(MathContext.UNLIMITED);

        return negative ? value.negate() : value;
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s.substring(0, length);
        }
        return s + " ".repeat(length - s.length());
    }
}
