package com.carddemo.io;

import com.carddemo.model.AccountRecord;
import com.carddemo.util.CobolNumericParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Reads fixed-length (300-byte) account records from an ASCII flat file,
 * mirroring the COBOL ACCTFILE-FILE (VSAM KSDS read sequentially).
 *
 * Each line in the file represents one account record whose layout is
 * defined in copybook CVACT01Y.cpy.
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path path) throws IOException {
        this.reader = Files.newBufferedReader(path);
    }

    /**
     * Reads the next account record. Returns empty when EOF is reached.
     */
    public Optional<AccountRecord> readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return Optional.empty();
        }

        // Pad to 300 chars if shorter (matches COBOL FILLER behaviour)
        if (line.length() < AccountRecord.RECORD_LENGTH) {
            line = line + " ".repeat(AccountRecord.RECORD_LENGTH - line.length());
        }

        int pos = 0;
        long acctId = Long.parseLong(line.substring(pos, pos + 11).trim());
        pos += 11;

        String activeStatus = line.substring(pos, pos + 1);
        pos += 1;

        var currBal = CobolNumericParser.parseSignedDisplay(line.substring(pos, pos + 12), 10, 2);
        pos += 12;

        var creditLimit = CobolNumericParser.parseSignedDisplay(line.substring(pos, pos + 12), 10, 2);
        pos += 12;

        var cashCreditLimit = CobolNumericParser.parseSignedDisplay(line.substring(pos, pos + 12), 10, 2);
        pos += 12;

        String openDate = line.substring(pos, pos + 10);
        pos += 10;

        String expiraionDate = line.substring(pos, pos + 10);
        pos += 10;

        String reissueDate = line.substring(pos, pos + 10);
        pos += 10;

        var currCycCredit = CobolNumericParser.parseSignedDisplay(line.substring(pos, pos + 12), 10, 2);
        pos += 12;

        var currCycDebit = CobolNumericParser.parseSignedDisplay(line.substring(pos, pos + 12), 10, 2);
        pos += 12;

        String addrZip = line.substring(pos, pos + 10);
        pos += 10;

        String groupId = line.substring(pos, pos + 10);

        return Optional.of(new AccountRecord(
                acctId, activeStatus, currBal, creditLimit, cashCreditLimit,
                openDate, expiraionDate, reissueDate,
                currCycCredit, currCycDebit, addrZip, groupId
        ));
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
