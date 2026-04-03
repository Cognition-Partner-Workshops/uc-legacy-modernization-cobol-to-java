package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.util.DateFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Java equivalent of COBOL program CBACT01C.
 * Reads the account VSAM file and writes processed records to multiple output files:
 *   - OUTFILE: selected account fields with date reformatting
 *   - ARRYFILE: array-style records with balance repetitions
 *   - VBRCFILE: variable-length records (two record types per account)
 */
public class AccountFileProcessor {

    private static final Logger log = LoggerFactory.getLogger(AccountFileProcessor.class);
    private static final BigDecimal DEFAULT_DEBIT = new BigDecimal("2525.00");

    private final Path inputFile;
    private final Path outFile;
    private final Path arryFile;
    private final Path vbrcFile;

    public AccountFileProcessor(Path inputFile, Path outFile, Path arryFile, Path vbrcFile) {
        this.inputFile = inputFile;
        this.outFile = outFile;
        this.arryFile = arryFile;
        this.vbrcFile = vbrcFile;
    }

    /**
     * Execute the batch job - reads accounts and writes to three output files.
     *
     * @return list of AccountRecords processed
     */
    public List<AccountRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C (AccountFileProcessor)");
        List<AccountRecord> records = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile);
             BufferedWriter outWriter = Files.newBufferedWriter(outFile);
             BufferedWriter arryWriter = Files.newBufferedWriter(arryFile);
             BufferedWriter vbrcWriter = Files.newBufferedWriter(vbrcFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                AccountRecord record = parseAccountRecord(line);
                records.add(record);
                displayAccountRecord(record);
                writeOutRecord(outWriter, record);
                writeArryRecord(arryWriter, record);
                writeVbrcRecords(vbrcWriter, record);
            }
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT01C - processed {} records", records.size());
        return records;
    }

    /**
     * Parse a fixed-width account record line into an AccountRecord.
     */
    public static AccountRecord parseAccountRecord(String line) {
        AccountRecord rec = new AccountRecord();
        rec.setAcctId(parseLong(line, 0, 11));
        rec.setAcctActiveStatus(safeSubstring(line, 11, 12));
        rec.setAcctCurrBal(parseDecimal(line, 12, 24));
        rec.setAcctCreditLimit(parseDecimal(line, 24, 36));
        rec.setAcctCashCreditLimit(parseDecimal(line, 36, 48));
        rec.setAcctOpenDate(safeSubstring(line, 48, 58));
        rec.setAcctExpirationDate(safeSubstring(line, 58, 68));
        rec.setAcctReissueDate(safeSubstring(line, 68, 78));
        rec.setAcctCurrCycCredit(parseDecimal(line, 78, 90));
        rec.setAcctCurrCycDebit(parseDecimal(line, 90, 102));
        rec.setAcctAddrZip(safeSubstring(line, 102, 112));
        rec.setAcctGroupId(safeSubstring(line, 112, 122));
        return rec;
    }

    private void displayAccountRecord(AccountRecord rec) {
        log.debug("ACCT-ID                 :{}", rec.getAcctId());
        log.debug("ACCT-ACTIVE-STATUS      :{}", rec.getAcctActiveStatus());
        log.debug("ACCT-CURR-BAL           :{}", rec.getAcctCurrBal());
        log.debug("ACCT-CREDIT-LIMIT       :{}", rec.getAcctCreditLimit());
        log.debug("ACCT-CASH-CREDIT-LIMIT  :{}", rec.getAcctCashCreditLimit());
        log.debug("ACCT-OPEN-DATE          :{}", rec.getAcctOpenDate());
        log.debug("ACCT-EXPIRAION-DATE     :{}", rec.getAcctExpirationDate());
        log.debug("ACCT-REISSUE-DATE       :{}", rec.getAcctReissueDate());
        log.debug("ACCT-CURR-CYC-CREDIT    :{}", rec.getAcctCurrCycCredit());
        log.debug("ACCT-CURR-CYC-DEBIT     :{}", rec.getAcctCurrCycDebit());
        log.debug("ACCT-GROUP-ID           :{}", rec.getAcctGroupId());
        log.debug("-------------------------------------------------");
    }

    /**
     * Write the output account record (1300-POPUL-ACCT-RECORD / 1350-WRITE-ACCT-RECORD).
     * Includes date conversion equivalent to CALL 'COBDATFT'.
     */
    private void writeOutRecord(BufferedWriter writer, AccountRecord rec) throws IOException {
        String reissueDate = DateFormatUtil.convertToCompactDate(rec.getAcctReissueDate());
        BigDecimal debit = rec.getAcctCurrCycDebit().compareTo(BigDecimal.ZERO) == 0
                ? DEFAULT_DEBIT : rec.getAcctCurrCycDebit();

        writer.write(String.format("%011d%s%s%s%s%s%s%s%s%s%s",
                rec.getAcctId(),
                padRight(rec.getAcctActiveStatus(), 1),
                formatDecimal(rec.getAcctCurrBal()),
                formatDecimal(rec.getAcctCreditLimit()),
                formatDecimal(rec.getAcctCashCreditLimit()),
                padRight(rec.getAcctOpenDate(), 10),
                padRight(rec.getAcctExpirationDate(), 10),
                padRight(reissueDate, 10),
                formatDecimal(rec.getAcctCurrCycCredit()),
                formatDecimal(debit),
                padRight(rec.getAcctGroupId(), 10)));
        writer.newLine();
    }

    /**
     * Write the array-style record (1400-POPUL-ARRAY-RECORD / 1450-WRITE-ARRY-RECORD).
     * Replicates balance values into array slots with fixed debit values.
     */
    private void writeArryRecord(BufferedWriter writer, AccountRecord rec) throws IOException {
        BigDecimal bal = rec.getAcctCurrBal();
        writer.write(String.format("%011d%s%s%s%s%s%s",
                rec.getAcctId(),
                formatDecimal(bal), formatDecimal(new BigDecimal("1005.00")),
                formatDecimal(bal), formatDecimal(new BigDecimal("1525.00")),
                formatDecimal(new BigDecimal("-1025.00")), formatDecimal(new BigDecimal("-2500.00"))));
        writer.newLine();
    }

    /**
     * Write variable-length records (1500-POPUL-VBRC-RECORD).
     * Two records per account: short status record and longer balance record.
     */
    private void writeVbrcRecords(BufferedWriter writer, AccountRecord rec) throws IOException {
        // VB1: short record (acct-id + active-status)
        writer.write(String.format("%011d%s",
                rec.getAcctId(),
                padRight(rec.getAcctActiveStatus(), 1)));
        writer.newLine();

        // VB2: longer record (acct-id + balances + reissue year)
        String reissueYear = "";
        if (rec.getAcctReissueDate() != null && rec.getAcctReissueDate().length() >= 4) {
            reissueYear = rec.getAcctReissueDate().substring(0, 4);
        }
        writer.write(String.format("%011d%s%s%s",
                rec.getAcctId(),
                formatDecimal(rec.getAcctCurrBal()),
                formatDecimal(rec.getAcctCreditLimit()),
                padRight(reissueYear, 4)));
        writer.newLine();
    }

    private static String formatDecimal(BigDecimal value) {
        return String.format("%+013.2f", value);
    }

    private static String padRight(String s, int width) {
        if (s == null) return " ".repeat(width);
        if (s.length() >= width) return s.substring(0, width);
        return s + " ".repeat(width - s.length());
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

    private static BigDecimal parseDecimal(String line, int start, int end) {
        String s = safeSubstring(line, start, end);
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
