package com.carddemo.io;

import com.carddemo.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the account data file (acctdata.txt) in its ASCII fixed-width format.
 * Each record is 300 characters corresponding to the CVACT01Y copybook layout.
 *
 * <p>Field positions (1-based):
 * <pre>
 *   1-11    ACCT-ID              PIC 9(11)
 *  12-12    ACCT-ACTIVE-STATUS   PIC X(01)
 *  13-24    ACCT-CURR-BAL        PIC S9(10)V99  (12 chars, sign overpunch)
 *  25-36    ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *  37-48    ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  49-58    ACCT-OPEN-DATE       PIC X(10)
 *  59-68    ACCT-EXPIRAION-DATE  PIC X(10)
 *  69-78    ACCT-REISSUE-DATE    PIC X(10)
 *  79-90    ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 *  91-102   ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 * 103-112   ACCT-ADDR-ZIP        PIC X(10)
 * 113-122   ACCT-GROUP-ID        PIC X(10)
 * 123-300   FILLER               PIC X(178)
 * </pre>
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    public AccountRecord readNext() throws IOException {
        final String line = reader.readLine();
        if (line == null) {
            return null;
        }
        final String padded = padToLength(line, AccountRecord.RECORD_LENGTH);
        return parseRecord(padded);
    }

    public List<AccountRecord> readAll() throws IOException {
        final List<AccountRecord> records = new ArrayList<>();
        AccountRecord record;
        while ((record = readNext()) != null) {
            records.add(record);
        }
        return records;
    }

    private AccountRecord parseRecord(String line) {
        return new AccountRecord(
                CobolFieldParser.parseUnsignedNumeric(line.substring(0, 11)),
                line.substring(11, 12),
                CobolFieldParser.parseSignedDecimal(line.substring(12, 24), 2),
                CobolFieldParser.parseSignedDecimal(line.substring(24, 36), 2),
                CobolFieldParser.parseSignedDecimal(line.substring(36, 48), 2),
                CobolFieldParser.parseAlphanumeric(line.substring(48, 58)),
                CobolFieldParser.parseAlphanumeric(line.substring(58, 68)),
                CobolFieldParser.parseAlphanumeric(line.substring(68, 78)),
                CobolFieldParser.parseSignedDecimal(line.substring(78, 90), 2),
                CobolFieldParser.parseSignedDecimal(line.substring(90, 102), 2),
                CobolFieldParser.parseAlphanumeric(line.substring(102, 112)),
                CobolFieldParser.parseAlphanumeric(line.substring(112, 122))
        );
    }

    private static String padToLength(String line, int length) {
        if (line.length() >= length) return line;
        return line + " ".repeat(length - line.length());
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
