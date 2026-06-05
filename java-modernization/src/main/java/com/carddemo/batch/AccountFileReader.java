package com.carddemo.batch;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.ZonedDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the fixed-width VSAM-style account file (300 bytes per record) into
 * {@link AccountRecord} objects.
 *
 * <p>Field layout from copybook CVACT01Y:</p>
 * <pre>
 *   Offset  Length  Field
 *   ------  ------  -----
 *     0       11    ACCT-ID               PIC 9(11)
 *    11        1    ACCT-ACTIVE-STATUS     PIC X(01)
 *    12       12    ACCT-CURR-BAL          PIC S9(10)V99
 *    24       12    ACCT-CREDIT-LIMIT      PIC S9(10)V99
 *    36       12    ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *    48       10    ACCT-OPEN-DATE         PIC X(10)
 *    58       10    ACCT-EXPIRAION-DATE    PIC X(10)
 *    68       10    ACCT-REISSUE-DATE      PIC X(10)
 *    78       12    ACCT-CURR-CYC-CREDIT   PIC S9(10)V99
 *    90       12    ACCT-CURR-CYC-DEBIT    PIC S9(10)V99
 *   102       10    ACCT-ADDR-ZIP          PIC X(10)
 *   112       10    ACCT-GROUP-ID          PIC X(10)
 *   122      178    FILLER                 PIC X(178)
 *   ---      ---
 *   Total:  300
 * </pre>
 */
public final class AccountFileReader {

    private AccountFileReader() {}

    public static List<AccountRecord> readAll(Path path) throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < AccountRecord.RECORD_LENGTH) {
                    continue; // skip blank/short lines
                }
                records.add(parseLine(line));
            }
        }
        return records;
    }

    static AccountRecord parseLine(String line) {
        long acctId             = Long.parseLong(line.substring(0, 11));
        String activeStatus     = line.substring(11, 12);
        BigDecimal currBal      = ZonedDecimalParser.parse(line.substring(12, 24), 2);
        BigDecimal creditLimit  = ZonedDecimalParser.parse(line.substring(24, 36), 2);
        BigDecimal cashCredit   = ZonedDecimalParser.parse(line.substring(36, 48), 2);
        String openDate         = line.substring(48, 58);
        String expirationDate   = line.substring(58, 68);
        String reissueDate      = line.substring(68, 78);
        BigDecimal cycCredit    = ZonedDecimalParser.parse(line.substring(78, 90), 2);
        BigDecimal cycDebit     = ZonedDecimalParser.parse(line.substring(90, 102), 2);
        String addrZip          = line.substring(102, 112);
        String groupId          = line.substring(112, 122);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCredit,
                openDate, expirationDate, reissueDate,
                cycCredit, cycDebit, addrZip, groupId);
    }
}
