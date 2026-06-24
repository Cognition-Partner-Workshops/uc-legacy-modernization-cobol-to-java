package com.carddemo.batch;

import com.carddemo.batch.converter.DateConverter;
import com.carddemo.batch.exception.BatchAbendException;
import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.ArrayRecord;
import com.carddemo.batch.model.OutputAccountRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Java translation of CBACT01C.cbl — reads the account file and writes
 * transformed records into three output files (OUTFILE, ARRYFILE, VBRCFILE).
 *
 * Translates the COBOL batch pipeline:
 *   ACCTFILE (indexed VSAM) → sequential reads
 *     → OUTFILE   (output account records with date conversion + debit rule)
 *     → ARRYFILE  (array records with fixed balance/debit population)
 *     → VBRCFILE  (variable-length records: VB1 12-char, VB2 39-char)
 */
public class AccountFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(AccountFileProcessor.class);

    private static final int ABEND_CODE = 999;
    private static final BigDecimal ZERO_DEBIT_SUBSTITUTE = new BigDecimal("2525.00");
    private static final BigDecimal ARRAY_DEBIT_1 = new BigDecimal("1005.00");
    private static final BigDecimal ARRAY_DEBIT_2 = new BigDecimal("1525.00");
    private static final BigDecimal ARRAY_BAL_3 = new BigDecimal("-1025.00");
    private static final BigDecimal ARRAY_DEBIT_3 = new BigDecimal("-2500.00");

    static final int VB1_LENGTH = 12;
    static final int VB2_LENGTH = 39;

    private final Path acctFilePath;
    private final Path outFilePath;
    private final Path arryFilePath;
    private final Path vbrcFilePath;

    public AccountFileProcessor(Path acctFilePath,
                                Path outFilePath,
                                Path arryFilePath,
                                Path vbrcFilePath) {
        this.acctFilePath = acctFilePath;
        this.outFilePath = outFilePath;
        this.arryFilePath = arryFilePath;
        this.vbrcFilePath = vbrcFilePath;
    }

    public void execute() {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C");

        try (BufferedReader acctReader = openInputFile(acctFilePath);
             BufferedWriter outWriter = openOutputFile(outFilePath);
             BufferedWriter arryWriter = openOutputFile(arryFilePath);
             BufferedWriter vbrcWriter = openOutputFile(vbrcFilePath)) {

            String line;
            while ((line = readNextRecord(acctReader)) != null) {
                AccountRecord accountRecord = AccountRecord.parseFixedWidth(line);
                displayAccountRecord(accountRecord);

                OutputAccountRecord outRec = populateOutputRecord(accountRecord);
                writeOutputRecord(outWriter, outRec);

                ArrayRecord arrRec = populateArrayRecord(accountRecord);
                writeArrayRecord(arryWriter, arrRec);

                String vb1 = populateVb1Record(accountRecord);
                String vb2 = populateVb2Record(accountRecord);
                writeVariableLengthRecord(vbrcWriter, vb1);
                writeVariableLengthRecord(vbrcWriter, vb2);
            }

        } catch (BatchAbendException e) {
            throw e;
        } catch (IOException e) {
            log.error("ABENDING PROGRAM");
            throw new BatchAbendException(ABEND_CODE, "I/O error during batch processing", e);
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT01C");
    }

    OutputAccountRecord populateOutputRecord(AccountRecord acct) {
        OutputAccountRecord out = new OutputAccountRecord();
        out.setAcctId(acct.getAcctId());
        out.setActiveStatus(acct.getActiveStatus());
        out.setCurrentBalance(acct.getCurrentBalance());
        out.setCreditLimit(acct.getCreditLimit());
        out.setCashCreditLimit(acct.getCashCreditLimit());
        out.setOpenDate(acct.getOpenDate());
        out.setExpirationDate(acct.getExpirationDate());

        String reissueDateConverted = DateConverter.convert(
                acct.getReissueDate(), '2', '2');
        out.setReissueDate(reissueDateConverted);

        out.setCurrentCycleCredit(acct.getCurrentCycleCredit());

        if (acct.getCurrentCycleDebit().compareTo(BigDecimal.ZERO) == 0) {
            out.setCurrentCycleDebit(ZERO_DEBIT_SUBSTITUTE);
        } else {
            out.setCurrentCycleDebit(acct.getCurrentCycleDebit());
        }

        out.setGroupId(acct.getGroupId());
        return out;
    }

    ArrayRecord populateArrayRecord(AccountRecord acct) {
        ArrayRecord arr = new ArrayRecord();
        arr.setAcctId(acct.getAcctId());

        arr.setEntry(0, acct.getCurrentBalance(), ARRAY_DEBIT_1);
        arr.setEntry(1, acct.getCurrentBalance(), ARRAY_DEBIT_2);
        arr.setEntry(2, ARRAY_BAL_3, ARRAY_DEBIT_3);
        // Indices 3 and 4 remain initialised to zero (matching COBOL INITIALIZE)

        return arr;
    }

    String populateVb1Record(AccountRecord acct) {
        String raw = String.format("%011d", acct.getAcctId()) + acct.getActiveStatus();
        return padOrTruncate(raw, VB1_LENGTH);
    }

    String populateVb2Record(AccountRecord acct) {
        String reissueYear = extractReissueYear(acct.getReissueDate());
        String raw = String.format("%011d", acct.getAcctId())
                + formatDecimal12(acct.getCurrentBalance())
                + formatDecimal12(acct.getCreditLimit())
                + padOrTruncate(reissueYear, 4);
        return padOrTruncate(raw, VB2_LENGTH);
    }

    private void displayAccountRecord(AccountRecord rec) {
        log.info("ACCT-ID                 :{}", rec.getAcctId());
        log.info("ACCT-ACTIVE-STATUS      :{}", rec.getActiveStatus());
        log.info("ACCT-CURR-BAL           :{}", rec.getCurrentBalance());
        log.info("ACCT-CREDIT-LIMIT       :{}", rec.getCreditLimit());
        log.info("ACCT-CASH-CREDIT-LIMIT  :{}", rec.getCashCreditLimit());
        log.info("ACCT-OPEN-DATE          :{}", rec.getOpenDate());
        log.info("ACCT-EXPIRAION-DATE     :{}", rec.getExpirationDate());
        log.info("ACCT-REISSUE-DATE       :{}", rec.getReissueDate());
        log.info("ACCT-CURR-CYC-CREDIT    :{}", rec.getCurrentCycleCredit());
        log.info("ACCT-CURR-CYC-DEBIT     :{}", rec.getCurrentCycleDebit());
        log.info("ACCT-GROUP-ID           :{}", rec.getGroupId());
        log.info("-------------------------------------------------");
    }

    private BufferedReader openInputFile(Path path) {
        try {
            return Files.newBufferedReader(path);
        } catch (IOException e) {
            log.error("ERROR OPENING ACCTFILE");
            displayIoStatus(e);
            throw new BatchAbendException(ABEND_CODE, "Error opening input file: " + path, e);
        }
    }

    private BufferedWriter openOutputFile(Path path) {
        try {
            return Files.newBufferedWriter(path);
        } catch (IOException e) {
            log.error("ERROR OPENING OUTPUT FILE: {}", path);
            displayIoStatus(e);
            throw new BatchAbendException(ABEND_CODE, "Error opening output file: " + path, e);
        }
    }

    private String readNextRecord(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    private void writeOutputRecord(BufferedWriter writer, OutputAccountRecord rec)
            throws IOException {
        try {
            writer.write(rec.toDelimited("|"));
            writer.newLine();
        } catch (IOException e) {
            log.error("ACCOUNT FILE WRITE STATUS IS: ERROR");
            displayIoStatus(e);
            throw new BatchAbendException(ABEND_CODE, "Error writing output record", e);
        }
    }

    private void writeArrayRecord(BufferedWriter writer, ArrayRecord rec)
            throws IOException {
        try {
            writer.write(rec.toDelimited("|"));
            writer.newLine();
        } catch (IOException e) {
            log.error("ACCOUNT FILE WRITE STATUS IS: ERROR");
            displayIoStatus(e);
            throw new BatchAbendException(ABEND_CODE, "Error writing array record", e);
        }
    }

    private void writeVariableLengthRecord(BufferedWriter writer, String record)
            throws IOException {
        try {
            writer.write(record);
            writer.newLine();
        } catch (IOException e) {
            log.error("ACCOUNT FILE WRITE STATUS IS: ERROR");
            displayIoStatus(e);
            throw new BatchAbendException(ABEND_CODE, "Error writing VB record", e);
        }
    }

    private void displayIoStatus(IOException e) {
        log.error("FILE STATUS IS: {}", e.getMessage());
    }

    private static String extractReissueYear(String reissueDate) {
        if (reissueDate == null || reissueDate.length() < 4) {
            return "    ";
        }
        return reissueDate.substring(0, 4);
    }

    private static String formatDecimal12(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        BigDecimal scaled = value.setScale(2);
        String unscaled = scaled.unscaledValue().abs().toString();
        while (unscaled.length() < 12) {
            unscaled = "0" + unscaled;
        }
        if (scaled.signum() < 0) {
            return "-" + unscaled.substring(1);
        }
        return unscaled;
    }

    static String padOrTruncate(String s, int len) {
        if (s == null) {
            return " ".repeat(len);
        }
        if (s.length() > len) {
            return s.substring(0, len);
        }
        if (s.length() < len) {
            return s + " ".repeat(len - s.length());
        }
        return s;
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: AccountFileProcessor <acctFile> <outFile> <arryFile> <vbrcFile>");
            System.exit(1);
        }
        AccountFileProcessor processor = new AccountFileProcessor(
                Path.of(args[0]),
                Path.of(args[1]),
                Path.of(args[2]),
                Path.of(args[3]));
        processor.execute();
    }
}
