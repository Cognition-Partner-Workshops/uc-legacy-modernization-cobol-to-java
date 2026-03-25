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

/**
 * Reads a fixed-width account data file (ASCII format) and produces
 * {@link AccountRecord} instances.
 *
 * COBOL equivalent: SELECT ACCTFILE-FILE ... ORGANIZATION IS INDEXED.
 *
 * Layout (from CVACT01Y copybook — 300 bytes per record):
 * <pre>
 *   Offset  Len  Field                    PIC
 *   0       11   ACCT-ID                  9(11)
 *   11       1   ACCT-ACTIVE-STATUS       X(1)
 *   12      12   ACCT-CURR-BAL            S9(10)V99  (overpunch)
 *   24      12   ACCT-CREDIT-LIMIT        S9(10)V99  (overpunch)
 *   36      12   ACCT-CASH-CREDIT-LIMIT   S9(10)V99  (overpunch)
 *   48      10   ACCT-OPEN-DATE           X(10)
 *   58      10   ACCT-EXPIRAION-DATE      X(10)
 *   68      10   ACCT-REISSUE-DATE        X(10)
 *   78      12   ACCT-CURR-CYC-CREDIT     S9(10)V99  (overpunch)
 *   90      12   ACCT-CURR-CYC-DEBIT      S9(10)V99  (overpunch)
 *   102     10   ACCT-ADDR-ZIP            X(10)
 *   112     10   ACCT-GROUP-ID            X(10)
 *   122    178   FILLER                   X(178)
 * </pre>
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path inputFile) throws IOException {
        this.reader = Files.newBufferedReader(inputFile);
    }

    /**
     * Read all records from the file.
     */
    public List<AccountRecord> readAll() throws IOException {
        var records = new ArrayList<AccountRecord>();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            records.add(parseLine(line));
        }
        return records;
    }

    /**
     * Parse a single fixed-width line into an {@link AccountRecord}.
     */
    static AccountRecord parseLine(String line) {
        // Pad to 300 if shorter
        if (line.length() < 300) {
            line = String.format("%-300s", line);
        }

        long acctId = CobolDecimalParser.parseUnsignedNumeric(line.substring(0, 11));
        String activeStatus = line.substring(11, 12);
        BigDecimal currBal = CobolDecimalParser.parseSignedDecimal(line.substring(12, 24), 2);
        BigDecimal creditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(24, 36), 2);
        BigDecimal cashCreditLimit = CobolDecimalParser.parseSignedDecimal(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58).strip();
        String expirationDate = line.substring(58, 68).strip();
        String reissueDate = line.substring(68, 78).strip();
        BigDecimal currCycCredit = CobolDecimalParser.parseSignedDecimal(line.substring(78, 90), 2);
        BigDecimal currCycDebit = CobolDecimalParser.parseSignedDecimal(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112).strip();
        String groupId = line.substring(112, 122).strip();

        return new AccountRecord(
                acctId,
                activeStatus,
                currBal,
                creditLimit,
                cashCreditLimit,
                openDate,
                expirationDate,
                reissueDate,
                currCycCredit,
                currCycDebit,
                addrZip,
                groupId
        );
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
