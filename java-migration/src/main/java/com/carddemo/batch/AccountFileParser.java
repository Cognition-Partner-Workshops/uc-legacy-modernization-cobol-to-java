package com.carddemo.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the fixed-width ASCII account data file (ACCTDATA) into
 * {@link AccountRecord} instances.
 *
 * The input file mirrors the VSAM KSDS used by CBACT01C, presented in
 * ASCII with zoned-decimal sign overpunch encoding.
 *
 * Field layout from CVACT01Y (300-byte record):
 * <pre>
 * Offset  Length  Field                    PIC
 * 0       11      ACCT-ID                  9(11)
 * 11       1      ACCT-ACTIVE-STATUS       X(01)
 * 12      12      ACCT-CURR-BAL            S9(10)V99
 * 24      12      ACCT-CREDIT-LIMIT        S9(10)V99
 * 36      12      ACCT-CASH-CREDIT-LIMIT   S9(10)V99
 * 48      10      ACCT-OPEN-DATE           X(10)
 * 58      10      ACCT-EXPIRAION-DATE      X(10)
 * 68      10      ACCT-REISSUE-DATE        X(10)
 * 78      12      ACCT-CURR-CYC-CREDIT     S9(10)V99
 * 90      12      ACCT-CURR-CYC-DEBIT      S9(10)V99
 * 102     10      ACCT-ADDR-ZIP            X(10)
 * 112     10      ACCT-GROUP-ID            X(10)
 * 122    178      FILLER                   X(178)
 * </pre>
 */
public final class AccountFileParser {

    private AccountFileParser() {
        // utility class
    }

    /**
     * Parse all account records from the given file.
     */
    public static List<AccountRecord> parse(Path inputFile) throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // Pad to full record length if needed
                if (line.length() < AccountRecord.RECORD_LENGTH) {
                    line = line + " ".repeat(AccountRecord.RECORD_LENGTH - line.length());
                }
                records.add(parseLine(line));
            }
        }
        return records;
    }

    private static AccountRecord parseLine(String line) {
        long acctId = Long.parseLong(line.substring(0, 11).trim());
        String activeStatus = line.substring(11, 12);
        var currBal = CobolDecimalUtils.parseZonedDecimal(line.substring(12, 24), 2);
        var creditLimit = CobolDecimalUtils.parseZonedDecimal(line.substring(24, 36), 2);
        var cashCreditLimit = CobolDecimalUtils.parseZonedDecimal(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58);
        String expirationDate = line.substring(58, 68);
        String reissueDate = line.substring(68, 78);
        var currCycCredit = CobolDecimalUtils.parseZonedDecimal(line.substring(78, 90), 2);
        var currCycDebit = CobolDecimalUtils.parseZonedDecimal(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112);
        String groupId = line.substring(112, 122);

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
}
