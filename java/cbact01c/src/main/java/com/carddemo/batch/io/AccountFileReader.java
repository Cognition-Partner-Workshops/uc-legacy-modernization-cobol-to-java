package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.CobolDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Reads account records from a fixed-length text file in the COBOL
 * zoned-decimal format used by the CardDemo sample data.
 *
 * <p>Each line corresponds to one 300-byte ACCOUNT-RECORD (CVACT01Y.cpy).
 * In the ASCII sample files, trailing spaces may be trimmed, so we
 * pad to the expected length as needed.
 *
 * <p>Field layout (offsets are 0-based character positions):
 * <pre>
 *   0-10   ACCT-ID               PIC 9(11)
 *  11-11   ACCT-ACTIVE-STATUS    PIC X(01)
 *  12-23   ACCT-CURR-BAL         PIC S9(10)V99  (12 chars with overpunch)
 *  24-35   ACCT-CREDIT-LIMIT     PIC S9(10)V99
 *  36-47   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  48-57   ACCT-OPEN-DATE        PIC X(10)
 *  58-67   ACCT-EXPIRAION-DATE   PIC X(10)
 *  68-77   ACCT-REISSUE-DATE     PIC X(10)
 *  78-89   ACCT-CURR-CYC-CREDIT  PIC S9(10)V99
 *  90-101  ACCT-CURR-CYC-DEBIT   PIC S9(10)V99
 * 102-111  ACCT-ADDR-ZIP         PIC X(10)
 * 112-121  ACCT-GROUP-ID         PIC X(10)
 * 122-299  FILLER                PIC X(178)
 * </pre>
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    /**
     * Reads the next account record from the file.
     *
     * @return an {@link Optional} containing the parsed record, or empty at EOF
     * @throws IOException on I/O error
     */
    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(parseLine(line));
    }

    /**
     * Reads all account records into a list (convenience method).
     */
    public List<AccountRecord> readAll() throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        Optional<AccountRecord> rec;
        while ((rec = readNext()).isPresent()) {
            records.add(rec.get());
        }
        return records;
    }

    /**
     * Parses a single fixed-length line into an {@link AccountRecord}.
     */
    public static AccountRecord parseLine(String line) {
        // Pad line to at least 122 characters to handle trimmed trailing spaces
        String padded = padRight(line, AccountRecord.RECORD_LENGTH);

        long acctId = Long.parseLong(padded.substring(0, 11));
        String activeStatus = padded.substring(11, 12);
        BigDecimal currBal = CobolDecimalParser.parseSignedDecimal(padded.substring(12, 24), 2);
        BigDecimal creditLimit = CobolDecimalParser.parseSignedDecimal(padded.substring(24, 36), 2);
        BigDecimal cashCreditLimit = CobolDecimalParser.parseSignedDecimal(padded.substring(36, 48), 2);
        String openDate = padded.substring(48, 58);
        String expiraionDate = padded.substring(58, 68);
        String reissueDate = padded.substring(68, 78);
        BigDecimal currCycCredit = CobolDecimalParser.parseSignedDecimal(padded.substring(78, 90), 2);
        BigDecimal currCycDebit = CobolDecimalParser.parseSignedDecimal(padded.substring(90, 102), 2);
        String addrZip = padded.substring(102, 112).trim();
        String groupId = padded.substring(112, 122);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s;
        }
        return s + " ".repeat(length - s.length());
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
