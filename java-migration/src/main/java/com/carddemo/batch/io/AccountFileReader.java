package com.carddemo.batch.io;

import com.carddemo.batch.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the ASCII account data file (acctdata.txt), parsing each 300-byte
 * fixed-length record into an {@link AccountRecord}.
 */
public final class AccountFileReader {

    private AccountFileReader() {
    }

    public static List<AccountRecord> readAll(Path accountFile) throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(accountFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < AccountRecord.RECORD_LENGTH) {
                    line = padRight(line, AccountRecord.RECORD_LENGTH);
                }
                records.add(parseLine(line));
            }
        }
        return records;
    }

    private static AccountRecord parseLine(String line) {
        int pos = 0;

        String acctId = line.substring(pos, pos += AccountRecord.ACCT_ID_LEN);
        String status = line.substring(pos, pos += AccountRecord.ACTIVE_STATUS_LEN);
        BigDecimal currBal = parseDecimal(line, pos, pos += AccountRecord.DECIMAL_FIELD_LEN);
        BigDecimal creditLimit = parseDecimal(line, pos, pos += AccountRecord.DECIMAL_FIELD_LEN);
        BigDecimal cashCredit = parseDecimal(line, pos, pos += AccountRecord.DECIMAL_FIELD_LEN);
        String openDate = line.substring(pos, pos += AccountRecord.DATE_LEN);
        String expDate = line.substring(pos, pos += AccountRecord.DATE_LEN);
        String reissueDate = line.substring(pos, pos += AccountRecord.DATE_LEN);
        BigDecimal cycCredit = parseDecimal(line, pos, pos += AccountRecord.DECIMAL_FIELD_LEN);
        BigDecimal cycDebit = parseDecimal(line, pos, pos += AccountRecord.DECIMAL_FIELD_LEN);
        String zip = line.substring(pos, pos += AccountRecord.ZIP_LEN);
        String groupId = line.substring(pos, pos + AccountRecord.GROUP_ID_LEN);

        return new AccountRecord(
                acctId, status, currBal, creditLimit, cashCredit,
                openDate, expDate, reissueDate, cycCredit, cycDebit,
                zip, groupId
        );
    }

    private static BigDecimal parseDecimal(String line, int start, int end) {
        return CobolDecimalParser.parseZonedDecimal(line.substring(start, end), 2);
    }

    private static String padRight(String s, int len) {
        return s + " ".repeat(len - s.length());
    }
}
