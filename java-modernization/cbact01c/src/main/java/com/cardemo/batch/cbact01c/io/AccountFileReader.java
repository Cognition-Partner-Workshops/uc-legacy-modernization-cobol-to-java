package com.cardemo.batch.cbact01c.io;

import com.cardemo.batch.cbact01c.model.AccountRecord;
import com.cardemo.batch.cbact01c.util.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static com.cardemo.batch.cbact01c.model.AccountRecord.*;

/**
 * Reads fixed-width account records from an ASCII flat file that mirrors
 * the VSAM KSDS layout defined by copybook CVACT01Y (300 bytes per record).
 */
public final class AccountFileReader implements AutoCloseable, Iterable<AccountRecord> {

    private final BufferedReader reader;

    public AccountFileReader(Path inputFile) throws IOException {
        this.reader = Files.newBufferedReader(inputFile);
    }

    public static AccountRecord parseLine(String line) {
        String padded = padRight(line, RECORD_LENGTH);

        long acctId = Long.parseLong(
                padded.substring(ACCT_ID_OFF, ACCT_ID_OFF + ACCT_ID_LEN));
        char activeStatus = padded.charAt(ACTIVE_STATUS_OFF);
        BigDecimal currBal = parseZoned(padded, CURR_BAL_OFF, CURR_BAL_LEN);
        BigDecimal creditLimit = parseZoned(padded, CREDIT_LIMIT_OFF, CREDIT_LIMIT_LEN);
        BigDecimal cashCreditLimit = parseZoned(padded, CASH_CREDIT_LIMIT_OFF, CASH_CREDIT_LIMIT_LEN);
        String openDate = padded.substring(OPEN_DATE_OFF, OPEN_DATE_OFF + OPEN_DATE_LEN);
        String expirationDate = padded.substring(EXPIRATION_DATE_OFF, EXPIRATION_DATE_OFF + EXPIRATION_DATE_LEN);
        String reissueDate = padded.substring(REISSUE_DATE_OFF, REISSUE_DATE_OFF + REISSUE_DATE_LEN);
        BigDecimal currCycCredit = parseZoned(padded, CURR_CYC_CREDIT_OFF, CURR_CYC_CREDIT_LEN);
        BigDecimal currCycDebit = parseZoned(padded, CURR_CYC_DEBIT_OFF, CURR_CYC_DEBIT_LEN);
        String addrZip = padded.substring(ADDR_ZIP_OFF, ADDR_ZIP_OFF + ADDR_ZIP_LEN);
        String groupId = padded.substring(GROUP_ID_OFF, GROUP_ID_OFF + GROUP_ID_LEN);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId);
    }

    public Stream<AccountRecord> stream() {
        return reader.lines()
                .filter(line -> !line.isBlank())
                .map(AccountFileReader::parseLine);
    }

    @Override
    public Iterator<AccountRecord> iterator() {
        return new Iterator<>() {
            private String nextLine = advance();

            private String advance() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.isBlank()) return line;
                    }
                    return null;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public boolean hasNext() {
                return nextLine != null;
            }

            @Override
            public AccountRecord next() {
                if (nextLine == null) throw new NoSuchElementException();
                AccountRecord rec = parseLine(nextLine);
                nextLine = advance();
                return rec;
            }
        };
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    private static BigDecimal parseZoned(String line, int offset, int length) {
        return ZonedDecimalParser.parse(line.substring(offset, offset + length), 2);
    }

    private static String padRight(String s, int width) {
        if (s.length() >= width) return s;
        return s + " ".repeat(width - s.length());
    }
}
