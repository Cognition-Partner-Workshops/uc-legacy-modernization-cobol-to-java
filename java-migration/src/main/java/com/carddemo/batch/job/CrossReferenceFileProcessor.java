package com.carddemo.batch.job;

import com.carddemo.batch.model.CardXrefRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of COBOL program CBACT03C.
 * Reads and prints the account/card/customer cross-reference file (VSAM KSDS).
 */
public class CrossReferenceFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(CrossReferenceFileProcessor.class);

    private final Path inputFile;

    public CrossReferenceFileProcessor(Path inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Execute the batch job - reads all cross-reference records and returns them.
     *
     * @return list of CardXrefRecords read from the file
     */
    public List<CardXrefRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBACT03C (CrossReferenceFileProcessor)");
        List<CardXrefRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                CardXrefRecord record = parseXrefRecord(line);
                records.add(record);
                log.debug("{}", record);
            }
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT03C - read {} records", records.size());
        return records;
    }

    /**
     * Parse a fixed-width cross-reference record line into a CardXrefRecord.
     */
    public static CardXrefRecord parseXrefRecord(String line) {
        CardXrefRecord rec = new CardXrefRecord();
        rec.setXrefCardNum(safeSubstring(line, 0, 16));
        rec.setXrefCustId(parseLong(line, 16, 25));
        rec.setXrefAcctId(parseLong(line, 25, 36));
        return rec;
    }

    private static String safeSubstring(String line, int start, int end) {
        if (line.length() <= start) return "";
        return line.substring(start, Math.min(end, line.length())).trim();
    }

    private static long parseLong(String line, int start, int end) {
        String s = safeSubstring(line, start, end);
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
