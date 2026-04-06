package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.carddemo.batch.model.AccountRecord.*;

/**
 * Reads account records from a fixed-width flat file that mirrors the
 * COBOL VSAM KSDS account dataset (ACCTFILE).
 * <p>
 * Each line is {@value AccountRecord#RECORD_LENGTH} characters wide.
 * Signed numeric fields use COBOL zoned-decimal overpunch encoding.
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    /**
     * Reads the next account record from the file.
     *
     * @return the parsed {@link AccountRecord}, or {@code null} at end-of-file
     * @throws IOException              on I/O error
     * @throws IllegalArgumentException if a line is too short or contains bad data
     */
    public AccountRecord readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null; // EOF
        }
        // Pad short lines (some data files may trim trailing spaces)
        if (line.length() < RECORD_LENGTH) {
            line = padRight(line, RECORD_LENGTH);
        }
        return parseLine(line);
    }

    /**
     * Reads all records from the file into a list.
     */
    public List<AccountRecord> readAll() throws IOException {
        var records = new ArrayList<AccountRecord>();
        AccountRecord rec;
        while ((rec = readNext()) != null) {
            records.add(rec);
        }
        return records;
    }

    /**
     * Parses a single fixed-width line into an {@link AccountRecord}.
     * This is intentionally package-visible so tests can exercise it directly.
     */
    static AccountRecord parseLine(String line) {
        String acctId = line.substring(ACCT_ID_OFF, ACCT_ID_OFF + ACCT_ID_LEN);
        String activeStatus = line.substring(ACTIVE_STATUS_OFF, ACTIVE_STATUS_OFF + ACTIVE_STATUS_LEN);

        BigDecimal currBal = parseZoned(line, CURR_BAL_OFF, CURR_BAL_LEN);
        BigDecimal creditLimit = parseZoned(line, CREDIT_LIMIT_OFF, CREDIT_LIMIT_LEN);
        BigDecimal cashCreditLimit = parseZoned(line, CASH_CREDIT_LIMIT_OFF, CASH_CREDIT_LIMIT_LEN);

        String openDate = line.substring(OPEN_DATE_OFF, OPEN_DATE_OFF + OPEN_DATE_LEN);
        String expirationDate = line.substring(EXPIRATION_DATE_OFF, EXPIRATION_DATE_OFF + EXPIRATION_DATE_LEN);
        String reissueDate = line.substring(REISSUE_DATE_OFF, REISSUE_DATE_OFF + REISSUE_DATE_LEN);

        BigDecimal currCycCredit = parseZoned(line, CURR_CYC_CREDIT_OFF, CURR_CYC_CREDIT_LEN);
        BigDecimal currCycDebit = parseZoned(line, CURR_CYC_DEBIT_OFF, CURR_CYC_DEBIT_LEN);

        String addrZip = line.substring(ADDR_ZIP_OFF, ADDR_ZIP_OFF + ADDR_ZIP_LEN);
        String groupId = line.substring(GROUP_ID_OFF, GROUP_ID_OFF + GROUP_ID_LEN);

        return new AccountRecord(
                acctId, activeStatus,
                currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit,
                addrZip, groupId
        );
    }

    private static BigDecimal parseZoned(String line, int offset, int length) {
        String raw = line.substring(offset, offset + length);
        return ZonedDecimalParser.parse(raw, 2);
    }

    private static String padRight(String s, int width) {
        return s + " ".repeat(width - s.length());
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
