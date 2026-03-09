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
 * Reads the VSAM KSDS account master file (sequential access).
 *
 * <p>In the modernized environment, this reads from a fixed-length text file
 * where each line represents one 300-byte account record in COBOL
 * zoned-decimal / alphanumeric display format.
 *
 * <p>Corresponds to COBOL:
 * <pre>
 * SELECT ACCTFILE-FILE ASSIGN TO ACCTFILE
 *        ORGANIZATION IS INDEXED
 *        ACCESS MODE  IS SEQUENTIAL
 *        RECORD KEY   IS FD-ACCT-ID
 * </pre>
 */
public class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    /**
     * Reads the next account record, returning empty at end-of-file.
     * Corresponds to COBOL: READ ACCTFILE-FILE INTO ACCOUNT-RECORD.
     */
    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return Optional.empty();
        }
        // Pad line to at least RECORD_LENGTH to handle short lines
        line = padRight(line, AccountRecord.RECORD_LENGTH);
        return Optional.of(parseLine(line));
    }

    /**
     * Convenience method to read all records at once.
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
     * Parses a single fixed-length line into an AccountRecord.
     *
     * Field layout (CVACT01Y.cpy):
     * <pre>
     * Offset  Length  Field
     *   0      11     ACCT-ID              PIC 9(11)
     *  11       1     ACCT-ACTIVE-STATUS   PIC X(01)
     *  12      12     ACCT-CURR-BAL        PIC S9(10)V99
     *  24      12     ACCT-CREDIT-LIMIT    PIC S9(10)V99
     *  36      12     ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
     *  48      10     ACCT-OPEN-DATE       PIC X(10)
     *  58      10     ACCT-EXPIRAION-DATE  PIC X(10)
     *  68      10     ACCT-REISSUE-DATE    PIC X(10)
     *  78      12     ACCT-CURR-CYC-CREDIT PIC S9(10)V99
     *  90      12     ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
     * 102      10     ACCT-ADDR-ZIP        PIC X(10)
     * 112      10     ACCT-GROUP-ID        PIC X(10)
     * 122     178     FILLER               PIC X(178)
     * </pre>
     */
    static AccountRecord parseLine(String line) {
        int pos = 0;

        long acctId = CobolDecimalParser.parseUnsignedLong(line.substring(pos, pos + 11));
        pos += 11;

        String activeStatus = line.substring(pos, pos + 1);
        pos += 1;

        BigDecimal currBal = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal creditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal cashCreditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expirationDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        BigDecimal currCycCredit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        BigDecimal currCycDebit = CobolDecimalParser.parseSignedDecimal(line.substring(pos, pos + 12), 2);
        pos += 12;

        String addrZip = line.substring(pos, pos + 10);
        pos += 10;

        String groupId = line.substring(pos, pos + 10);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    private static String padRight(String s, int len) {
        if (s.length() >= len) {
            return s;
        }
        return s + " ".repeat(len - s.length());
    }
}
