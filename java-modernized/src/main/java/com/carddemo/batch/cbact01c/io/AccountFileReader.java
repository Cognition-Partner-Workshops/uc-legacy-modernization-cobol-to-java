package com.carddemo.batch.cbact01c.io;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.service.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads account master records from the CardDemo ASCII data file, parsing
 * the fixed-width format defined by the CVACT01Y copybook.
 *
 * <p>Field layout (300 bytes total):
 * <pre>
 *  Offset  Length  Field                    PIC
 *  0       11      ACCT-ID                  9(11)
 *  11      1       ACCT-ACTIVE-STATUS       X(01)
 *  12      12      ACCT-CURR-BAL            S9(10)V99
 *  24      12      ACCT-CREDIT-LIMIT        S9(10)V99
 *  36      12      ACCT-CASH-CREDIT-LIMIT   S9(10)V99
 *  48      10      ACCT-OPEN-DATE           X(10)
 *  58      10      ACCT-EXPIRAION-DATE      X(10)
 *  68      10      ACCT-REISSUE-DATE        X(10)
 *  78      12      ACCT-CURR-CYC-CREDIT     S9(10)V99
 *  90      12      ACCT-CURR-CYC-DEBIT      S9(10)V99
 *  102     10      ACCT-ADDR-ZIP            X(10)
 *  112     10      ACCT-GROUP-ID            X(10)
 *  122     178     FILLER                   X(178)
 * </pre>
 */
public final class AccountFileReader {

    private AccountFileReader() {
        // utility class
    }

    /**
     * Reads all account records from the given file path.
     *
     * @param path path to the account data file (ASCII fixed-width)
     * @return list of parsed account records
     * @throws IOException if the file cannot be read
     */
    public static List<AccountRecord> readAll(Path path) throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // Pad to full record length if needed (trailing spaces may be trimmed)
                line = padRight(line, AccountRecord.RECORD_LENGTH);
                records.add(parseLine(line));
            }
        }
        return records;
    }

    /**
     * Parses a single account record from a line.
     *
     * @param line the line to parse (must be at least 122 characters)
     * @return the parsed account record
     */
    public static AccountRecord parseLine(String line) {
        long acctId = Long.parseLong(line.substring(0, 11));
        String activeStatus = line.substring(11, 12);
        BigDecimal currBal = ZonedDecimalParser.parse(line.substring(12, 24), 2);
        BigDecimal creditLimit = ZonedDecimalParser.parse(line.substring(24, 36), 2);
        BigDecimal cashCreditLimit = ZonedDecimalParser.parse(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58).trim();
        String expirationDate = line.substring(58, 68).trim();
        String reissueDate = line.substring(68, 78).trim();
        BigDecimal currCycCredit = ZonedDecimalParser.parse(line.substring(78, 90), 2);
        BigDecimal currCycDebit = ZonedDecimalParser.parse(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112).trim();
        String groupId = line.substring(112, 122).trim();

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    private static String padRight(String s, int length) {
        if (s.length() >= length) {
            return s;
        }
        return s + " ".repeat(length - s.length());
    }
}
