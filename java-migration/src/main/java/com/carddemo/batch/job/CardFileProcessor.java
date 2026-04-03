package com.carddemo.batch.job;

import com.carddemo.batch.model.CardRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of COBOL program CBACT02C.
 * Reads and prints the card data file (VSAM KSDS).
 */
public class CardFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(CardFileProcessor.class);

    private final Path inputFile;

    public CardFileProcessor(Path inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Execute the batch job - reads all card records and returns them.
     *
     * @return list of CardRecords read from the file
     */
    public List<CardRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBACT02C (CardFileProcessor)");
        List<CardRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                CardRecord record = parseCardRecord(line);
                records.add(record);
                log.debug("{}", record);
            }
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT02C - read {} records", records.size());
        return records;
    }

    /**
     * Parse a fixed-width card record line into a CardRecord.
     */
    public static CardRecord parseCardRecord(String line) {
        CardRecord rec = new CardRecord();
        rec.setCardNum(safeSubstring(line, 0, 16));
        rec.setCardAcctId(parseLong(line, 16, 27));
        rec.setCardCvvCd(parseInt(line, 27, 30));
        rec.setCardEmbossedName(safeSubstring(line, 30, 80));
        rec.setCardExpirationDate(safeSubstring(line, 80, 90));
        rec.setCardActiveStatus(safeSubstring(line, 90, 91));
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

    private static int parseInt(String line, int start, int end) {
        String s = safeSubstring(line, start, end);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
