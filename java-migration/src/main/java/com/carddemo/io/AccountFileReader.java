package com.carddemo.io;

import com.carddemo.model.AccountRecord;
import com.carddemo.util.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads COBOL-format account records from an ASCII fixed-width file.
 *
 * Mirrors the COBOL READ ACCTFILE-FILE INTO ACCOUNT-RECORD operation.
 * The file is read sequentially (matching COBOL ACCESS MODE IS SEQUENTIAL).
 *
 * Field positions (0-based offsets, derived from CVACT01Y.cpy):
 *   0-10   (11) ACCT-ID              PIC 9(11)
 *  11-11    (1) ACCT-ACTIVE-STATUS   PIC X(01)
 *  12-23   (12) ACCT-CURR-BAL        PIC S9(10)V99
 *  24-35   (12) ACCT-CREDIT-LIMIT    PIC S9(10)V99
 *  36-47   (12) ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *  48-57   (10) ACCT-OPEN-DATE       PIC X(10)
 *  58-67   (10) ACCT-EXPIRAION-DATE  PIC X(10)
 *  68-77   (10) ACCT-REISSUE-DATE    PIC X(10)
 *  78-89   (12) ACCT-CURR-CYC-CREDIT PIC S9(10)V99
 *  90-101  (12) ACCT-CURR-CYC-DEBIT  PIC S9(10)V99
 * 102-111  (10) ACCT-ADDR-ZIP        PIC X(10)
 * 112-121  (10) ACCT-GROUP-ID        PIC X(10)
 * 122-299 (178) FILLER
 */
public class AccountFileReader implements Iterable<AccountRecord>, AutoCloseable {

    private final BufferedReader reader;
    private String nextLine;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
        advance();
    }

    private void advance() {
        try {
            nextLine = reader.readLine();
            // Skip blank lines
            while (nextLine != null && nextLine.isBlank()) {
                nextLine = reader.readLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Parse a single fixed-width line into an AccountRecord.
     */
    public static AccountRecord parseLine(String line) {
        // Pad line to minimum required length
        String padded = line.length() >= 122
                ? line
                : String.format("%-300s", line);

        long acctId = Long.parseLong(padded.substring(0, 11).trim());
        String activeStatus = padded.substring(11, 12).trim();
        BigDecimal currBal = ZonedDecimalParser.decode(padded.substring(12, 24));
        BigDecimal creditLimit = ZonedDecimalParser.decode(padded.substring(24, 36));
        BigDecimal cashCreditLimit = ZonedDecimalParser.decode(padded.substring(36, 48));
        String openDate = padded.substring(48, 58).trim();
        String expirationDate = padded.substring(58, 68).trim();
        String reissueDate = padded.substring(68, 78).trim();
        BigDecimal currCycCredit = ZonedDecimalParser.decode(padded.substring(78, 90));
        BigDecimal currCycDebit = ZonedDecimalParser.decode(padded.substring(90, 102));
        String addrZip = padded.substring(102, 112).trim();
        String groupId = padded.substring(112, 122).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    @Override
    public Iterator<AccountRecord> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return nextLine != null;
            }

            @Override
            public AccountRecord next() {
                if (nextLine == null) {
                    throw new NoSuchElementException("No more account records");
                }
                AccountRecord record = parseLine(nextLine);
                advance();
                return record;
            }
        };
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
