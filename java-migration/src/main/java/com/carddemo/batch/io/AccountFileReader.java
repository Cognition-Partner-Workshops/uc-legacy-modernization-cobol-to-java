package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Reads the VSAM KSDS account file (fixed-width 300-byte records) and produces
 * {@link AccountRecord} instances.
 *
 * The COBOL file layout (CVACT01Y copybook):
 *   ACCT-ID                PIC 9(11)       positions  1-11
 *   ACCT-ACTIVE-STATUS     PIC X(01)       position  12
 *   ACCT-CURR-BAL          PIC S9(10)V99   positions 13-24  (signed display, trailing overpunch)
 *   ACCT-CREDIT-LIMIT      PIC S9(10)V99   positions 25-36
 *   ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99   positions 37-48
 *   ACCT-OPEN-DATE         PIC X(10)       positions 49-58
 *   ACCT-EXPIRAION-DATE    PIC X(10)       positions 59-68
 *   ACCT-REISSUE-DATE      PIC X(10)       positions 69-78
 *   ACCT-CURR-CYC-CREDIT   PIC S9(10)V99   positions 79-90
 *   ACCT-CURR-CYC-DEBIT    PIC S9(10)V99   positions 91-102
 *   ACCT-ADDR-ZIP          PIC X(10)       positions 103-112
 *   ACCT-GROUP-ID          PIC X(10)       positions 113-122
 *   FILLER                 PIC X(178)      positions 123-300
 */
public class AccountFileReader implements Iterable<AccountRecord>, AutoCloseable {

    private final BufferedReader reader;
    private String nextLine;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
        advance();
    }

    private void advance() throws IOException {
        nextLine = reader.readLine();
        // Skip empty trailing lines
        while (nextLine != null && nextLine.isBlank()) {
            nextLine = reader.readLine();
        }
    }

    public boolean hasNext() {
        return nextLine != null;
    }

    public AccountRecord readNext() throws IOException {
        if (nextLine == null) {
            return null;
        }
        AccountRecord record = parseLine(nextLine);
        advance();
        return record;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * Parse a single 300-byte (or wider) fixed-width line into an AccountRecord.
     */
    static AccountRecord parseLine(String line) {
        // Pad line to at least 122 chars (we only need positions 1-122 for actual data)
        String padded = line.length() < 300
                ? String.format("%-300s", line)
                : line;

        long acctId = CobolDecimalParser.parseUnsigned(padded.substring(0, 11));
        String activeStatus = padded.substring(11, 12);
        BigDecimal currBal = CobolDecimalParser.parse(padded.substring(12, 24), 2);
        BigDecimal creditLimit = CobolDecimalParser.parse(padded.substring(24, 36), 2);
        BigDecimal cashCreditLimit = CobolDecimalParser.parse(padded.substring(36, 48), 2);
        String openDate = padded.substring(48, 58);
        String expirationDate = padded.substring(58, 68);
        String reissueDate = padded.substring(68, 78);
        BigDecimal currCycCredit = CobolDecimalParser.parse(padded.substring(78, 90), 2);
        BigDecimal currCycDebit = CobolDecimalParser.parse(padded.substring(90, 102), 2);
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
                return AccountFileReader.this.hasNext();
            }

            @Override
            public AccountRecord next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                try {
                    return readNext();
                } catch (IOException e) {
                    throw new RuntimeException("Error reading account file", e);
                }
            }
        };
    }
}
