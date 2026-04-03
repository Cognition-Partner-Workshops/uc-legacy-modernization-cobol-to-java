package com.carddemo.batch.job;

import com.carddemo.batch.model.CustomerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of COBOL program CBCUS01C.
 * Reads and prints the customer data file (VSAM KSDS).
 */
public class CustomerFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(CustomerFileProcessor.class);

    private final Path inputFile;

    public CustomerFileProcessor(Path inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Execute the batch job - reads all customer records and returns them.
     *
     * @return list of CustomerRecords read from the file
     */
    public List<CustomerRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBCUS01C (CustomerFileProcessor)");
        List<CustomerRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                CustomerRecord record = parseCustomerRecord(line);
                records.add(record);
                log.debug("{}", record);
            }
        }

        log.info("END OF EXECUTION OF PROGRAM CBCUS01C - read {} records", records.size());
        return records;
    }

    /**
     * Parse a fixed-width customer record line into a CustomerRecord.
     */
    public static CustomerRecord parseCustomerRecord(String line) {
        CustomerRecord rec = new CustomerRecord();
        rec.setCustId(parseLong(line, 0, 9));
        rec.setCustFirstName(safeSubstring(line, 9, 34));
        rec.setCustMiddleName(safeSubstring(line, 34, 59));
        rec.setCustLastName(safeSubstring(line, 59, 84));
        rec.setCustAddrLine1(safeSubstring(line, 84, 134));
        rec.setCustAddrLine2(safeSubstring(line, 134, 184));
        rec.setCustAddrLine3(safeSubstring(line, 184, 234));
        rec.setCustAddrStateCd(safeSubstring(line, 234, 236));
        rec.setCustAddrCountryCd(safeSubstring(line, 236, 239));
        rec.setCustAddrZip(safeSubstring(line, 239, 249));
        rec.setCustPhoneNum1(safeSubstring(line, 249, 264));
        rec.setCustPhoneNum2(safeSubstring(line, 264, 279));
        rec.setCustSsn(parseLong(line, 279, 288));
        rec.setCustGovtIssuedId(safeSubstring(line, 288, 308));
        rec.setCustDobYyyyMmDd(safeSubstring(line, 308, 318));
        rec.setCustEftAccountId(safeSubstring(line, 318, 328));
        rec.setCustPriCardHolderInd(safeSubstring(line, 328, 329));
        rec.setCustFicoCreditScore(parseInt(line, 329, 332));
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
