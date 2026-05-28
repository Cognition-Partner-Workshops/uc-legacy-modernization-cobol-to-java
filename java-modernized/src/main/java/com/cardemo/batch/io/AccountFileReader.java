package com.cardemo.batch.io;

import com.cardemo.batch.model.AccountRecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads fixed-width account records from a flat file (replaces VSAM KSDS sequential read).
 */
public final class AccountFileReader implements AutoCloseable {

    private final BufferedReader reader;

    public AccountFileReader(Path filePath) throws IOException {
        this.reader = Files.newBufferedReader(filePath);
    }

    public AccountRecord readNext() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return AccountRecord.parse(line);
    }

    public List<AccountRecord> readAll() throws IOException {
        List<AccountRecord> records = new ArrayList<>();
        AccountRecord record;
        while ((record = readNext()) != null) {
            records.add(record);
        }
        return records;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
