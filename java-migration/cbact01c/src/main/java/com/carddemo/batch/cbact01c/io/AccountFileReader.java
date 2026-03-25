package com.carddemo.batch.cbact01c.io;

import com.carddemo.batch.cbact01c.model.AccountRecord;
import com.carddemo.batch.cbact01c.util.CobolDecimalParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads fixed-length account records from a sequential file.
 * <p>
 * Mirrors COBOL: SELECT ACCTFILE-FILE ASSIGN TO ACCTFILE
 *                ORGANIZATION IS INDEXED, ACCESS MODE IS SEQUENTIAL
 * <p>
 * In the Java migration, we read line-by-line from a flat file (the ASCII
 * export of the VSAM KSDS). Each line is a 300-byte fixed-length record.
 *
 * COBOL field positions within the 300-byte record (CVACT01Y):
 * <pre>
 *   Offset  Length  Field
 *   0       11      ACCT-ID               PIC 9(11)
 *   11      1       ACCT-ACTIVE-STATUS    PIC X(01)
 *   12      12      ACCT-CURR-BAL         PIC S9(10)V99
 *   24      12      ACCT-CREDIT-LIMIT     PIC S9(10)V99
 *   36      12      ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
 *   48      10      ACCT-OPEN-DATE        PIC X(10)
 *   58      10      ACCT-EXPIRAION-DATE   PIC X(10)
 *   68      10      ACCT-REISSUE-DATE     PIC X(10)
 *   78      12      ACCT-CURR-CYC-CREDIT  PIC S9(10)V99
 *   90      12      ACCT-CURR-CYC-DEBIT   PIC S9(10)V99
 *   102     10      ACCT-ADDR-ZIP         PIC X(10)
 *   112     10      ACCT-GROUP-ID         PIC X(10)
 *   122     178     FILLER
 * </pre>
 */
public class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path inputFile) throws IOException {
        this.reader = Files.newBufferedReader(inputFile);
    }

    /**
     * Reads the next account record. Returns empty when EOF is reached.
     * Mirrors COBOL: READ ACCTFILE-FILE INTO ACCOUNT-RECORD.
     */
    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return Optional.empty();
        }

        // Pad to full record length if needed
        if (line.length() < AccountRecord.RECORD_LENGTH) {
            line = line + " ".repeat(AccountRecord.RECORD_LENGTH - line.length());
        }

        return Optional.of(parseRecord(line));
    }

    private AccountRecord parseRecord(String line) {
        long acctId = Long.parseLong(line.substring(0, 11));
        String activeStatus = line.substring(11, 12);
        var currBal = CobolDecimalParser.parse(line.substring(12, 24), 2);
        var creditLimit = CobolDecimalParser.parse(line.substring(24, 36), 2);
        var cashCreditLimit = CobolDecimalParser.parse(line.substring(36, 48), 2);
        String openDate = line.substring(48, 58);
        String expirationDate = line.substring(58, 68);
        String reissueDate = line.substring(68, 78);
        var currCycCredit = CobolDecimalParser.parse(line.substring(78, 90), 2);
        var currCycDebit = CobolDecimalParser.parse(line.substring(90, 102), 2);
        String addrZip = line.substring(102, 112);
        String groupId = line.substring(112, 122);

        return new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expirationDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        );
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
