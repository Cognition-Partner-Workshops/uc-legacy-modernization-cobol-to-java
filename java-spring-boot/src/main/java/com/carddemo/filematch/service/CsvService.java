package com.carddemo.filematch.service;

import com.carddemo.filematch.model.Account;
import com.carddemo.filematch.model.Transaction;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for reading/writing CSV files.
 * Replaces COBOL sequential and VSAM file I/O operations:
 *   - OPEN INPUT / OPEN OUTPUT
 *   - READ ... INTO ... AT END
 *   - WRITE record
 *   - CLOSE
 */
@Service
public class CsvService {

    private static final Logger log = LoggerFactory.getLogger(CsvService.class);

    // ── Account CSV ──────────────────────────────────────────────────────

    private static final String[] ACCOUNT_CSV_HEADER = {
            "ACCT_ID", "ACCT_ACTIVE_STATUS", "ACCT_CURR_BAL", "ACCT_CREDIT_LIMIT",
            "ACCT_CASH_CREDIT_LIMIT", "ACCT_OPEN_DATE", "ACCT_EXPIRATION_DATE",
            "ACCT_REISSUE_DATE", "ACCT_CURR_CYC_CREDIT", "ACCT_CURR_CYC_DEBIT",
            "ACCT_ADDR_ZIP", "ACCT_GROUP_ID"
    };

    /**
     * Read accounts from a CSV input stream.
     * Equivalent to COBOL: OPEN INPUT ACCTFILE-FILE / READ ACCTFILE-FILE INTO ACCOUNT-RECORD
     */
    public List<Account> readAccountsCsv(InputStream inputStream) throws IOException {
        List<Account> accounts = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext(); // skip header
            if (header == null) {
                return accounts;
            }
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 12) {
                    log.warn("Skipping malformed account row: {}", String.join(",", line));
                    continue;
                }
                Account acct = new Account();
                acct.setAcctId(line[0].trim());
                acct.setAcctActiveStatus(line[1].trim());
                acct.setAcctCurrBal(parseBigDecimal(line[2]));
                acct.setAcctCreditLimit(parseBigDecimal(line[3]));
                acct.setAcctCashCreditLimit(parseBigDecimal(line[4]));
                acct.setAcctOpenDate(line[5].trim());
                acct.setAcctExpirationDate(line[6].trim());
                acct.setAcctReissueDate(line[7].trim());
                acct.setAcctCurrCycCredit(parseBigDecimal(line[8]));
                acct.setAcctCurrCycDebit(parseBigDecimal(line[9]));
                acct.setAcctAddrZip(line[10].trim());
                acct.setAcctGroupId(line[11].trim());
                accounts.add(acct);
            }
        } catch (CsvValidationException e) {
            throw new IOException("CSV validation error reading accounts", e);
        }
        log.info("Read {} account records from CSV", accounts.size());
        return accounts;
    }

    /**
     * Write accounts to a CSV output stream.
     * Equivalent to COBOL: OPEN OUTPUT OUT-FILE / WRITE OUT-ACCT-REC
     */
    public void writeAccountsCsv(List<Account> accounts, OutputStream outputStream)
            throws IOException {
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(ACCOUNT_CSV_HEADER);
            for (Account acct : accounts) {
                writer.writeNext(new String[]{
                        acct.getAcctId(),
                        acct.getAcctActiveStatus(),
                        formatBigDecimal(acct.getAcctCurrBal()),
                        formatBigDecimal(acct.getAcctCreditLimit()),
                        formatBigDecimal(acct.getAcctCashCreditLimit()),
                        acct.getAcctOpenDate(),
                        acct.getAcctExpirationDate(),
                        acct.getAcctReissueDate(),
                        formatBigDecimal(acct.getAcctCurrCycCredit()),
                        formatBigDecimal(acct.getAcctCurrCycDebit()),
                        acct.getAcctAddrZip(),
                        acct.getAcctGroupId()
                });
            }
        }
        log.info("Wrote {} account records to CSV", accounts.size());
    }

    // ── Transaction CSV ──────────────────────────────────────────────────

    private static final String[] TRANSACTION_CSV_HEADER = {
            "TRAN_ID", "TRAN_TYPE_CD", "TRAN_CAT_CD", "TRAN_SOURCE", "TRAN_DESC",
            "TRAN_AMT", "TRAN_MERCHANT_ID", "TRAN_MERCHANT_NAME",
            "TRAN_MERCHANT_CITY", "TRAN_MERCHANT_ZIP", "TRAN_CARD_NUM",
            "TRAN_ORIG_TS", "TRAN_PROC_TS"
    };

    /**
     * Read transactions from a CSV input stream.
     * Equivalent to COBOL: READ DALYTRAN-FILE INTO DALYTRAN-RECORD
     */
    public List<Transaction> readTransactionsCsv(InputStream inputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext(); // skip header
            if (header == null) {
                return transactions;
            }
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 13) {
                    log.warn("Skipping malformed transaction row: {}", String.join(",", line));
                    continue;
                }
                Transaction tran = new Transaction();
                tran.setTranId(line[0].trim());
                tran.setTranTypeCd(line[1].trim());
                tran.setTranCatCd(parseInteger(line[2]));
                tran.setTranSource(line[3].trim());
                tran.setTranDesc(line[4].trim());
                tran.setTranAmt(parseBigDecimal(line[5]));
                tran.setTranMerchantId(line[6].trim());
                tran.setTranMerchantName(line[7].trim());
                tran.setTranMerchantCity(line[8].trim());
                tran.setTranMerchantZip(line[9].trim());
                tran.setTranCardNum(line[10].trim());
                tran.setTranOrigTs(line[11].trim());
                tran.setTranProcTs(line[12].trim());
                transactions.add(tran);
            }
        } catch (CsvValidationException e) {
            throw new IOException("CSV validation error reading transactions", e);
        }
        log.info("Read {} transaction records from CSV", transactions.size());
        return transactions;
    }

    /**
     * Write transactions to a CSV output stream.
     * Equivalent to COBOL: WRITE FD-TRANFILE-REC
     */
    public void writeTransactionsCsv(List<Transaction> transactions, OutputStream outputStream)
            throws IOException {
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.writeNext(TRANSACTION_CSV_HEADER);
            for (Transaction tran : transactions) {
                writer.writeNext(new String[]{
                        tran.getTranId(),
                        tran.getTranTypeCd(),
                        String.valueOf(tran.getTranCatCd()),
                        tran.getTranSource(),
                        tran.getTranDesc(),
                        formatBigDecimal(tran.getTranAmt()),
                        tran.getTranMerchantId(),
                        tran.getTranMerchantName(),
                        tran.getTranMerchantCity(),
                        tran.getTranMerchantZip(),
                        tran.getTranCardNum(),
                        tran.getTranOrigTs(),
                        tran.getTranProcTs()
                });
            }
        }
        log.info("Wrote {} transaction records to CSV", transactions.size());
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid decimal value '{}', defaulting to 0", value);
            return BigDecimal.ZERO;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value '{}', defaulting to 0", value);
            return 0;
        }
    }

    private String formatBigDecimal(BigDecimal value) {
        return value != null ? value.toPlainString() : "0.00";
    }
}
