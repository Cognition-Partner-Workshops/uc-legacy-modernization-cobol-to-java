package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.TranCatBalRecord;
import com.carddemo.batch.model.TransactionRecord;
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
import java.util.Map;

/**
 * Java equivalent of COBOL program CBTRN02C.
 * Posts records from the daily transaction file:
 *   1. Reads daily transactions sequentially
 *   2. Validates each transaction (card xref lookup, account lookup)
 *   3. Posts valid transactions to the transaction master file
 *   4. Updates account balances and transaction category balances
 *   5. Writes rejected transactions to a reject file
 */
public class TransactionPostingProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingProcessor.class);

    private final Path dailyTranFile;
    private final Path transactOutputFile;
    private final Path rejectFile;
    private final Map<String, CardXrefRecord> xrefByCardNum;
    private final Map<Long, AccountRecord> accountMap;
    private final Map<String, TranCatBalRecord> tranCatBalMap;

    private int transactionCount;
    private int rejectCount;

    public TransactionPostingProcessor(
            Path dailyTranFile,
            Path transactOutputFile,
            Path rejectFile,
            Map<String, CardXrefRecord> xrefByCardNum,
            Map<Long, AccountRecord> accountMap,
            Map<String, TranCatBalRecord> tranCatBalMap) {
        this.dailyTranFile = dailyTranFile;
        this.transactOutputFile = transactOutputFile;
        this.rejectFile = rejectFile;
        this.xrefByCardNum = xrefByCardNum;
        this.accountMap = accountMap;
        this.tranCatBalMap = tranCatBalMap;
        this.transactionCount = 0;
        this.rejectCount = 0;
    }

    /**
     * Execute the transaction posting batch job.
     *
     * @return list of successfully posted transactions
     */
    public List<TransactionRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBTRN02C (TransactionPostingProcessor)");
        List<TransactionRecord> postedTransactions = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(dailyTranFile);
             BufferedWriter tranWriter = Files.newBufferedWriter(transactOutputFile);
             BufferedWriter rejectWriter = Files.newBufferedWriter(rejectFile)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                transactionCount++;
                TransactionRecord dailyTran = parseDailyTransaction(line);
                ValidationResult validation = validateTransaction(dailyTran);

                if (validation.isValid()) {
                    postTransaction(dailyTran, tranWriter);
                    postedTransactions.add(dailyTran);
                } else {
                    rejectCount++;
                    writeRejectRecord(rejectWriter, line, validation);
                }
            }
        }

        log.info("TRANSACTIONS PROCESSED :{}", transactionCount);
        log.info("TRANSACTIONS REJECTED  :{}", rejectCount);
        log.info("END OF EXECUTION OF PROGRAM CBTRN02C");
        return postedTransactions;
    }

    /**
     * Validate a transaction (1500-VALIDATE-TRAN).
     * Checks: card xref exists, account exists and is active.
     */
    ValidationResult validateTransaction(TransactionRecord tran) {
        // 1500-A-LOOKUP-XREF
        CardXrefRecord xref = xrefByCardNum.get(tran.getTranCardNum());
        if (xref == null) {
            return new ValidationResult(1001, "Card number not found in cross-reference file");
        }

        // 1500-B-LOOKUP-ACCT
        AccountRecord account = accountMap.get(xref.getXrefAcctId());
        if (account == null) {
            return new ValidationResult(1002, "Account not found for card cross-reference");
        }

        if (!"Y".equalsIgnoreCase(account.getAcctActiveStatus())) {
            return new ValidationResult(1003, "Account is not active");
        }

        return new ValidationResult(0, "");
    }

    /**
     * Post a valid transaction (2000-POST-TRANSACTION).
     * Updates account balance and transaction category balance.
     */
    void postTransaction(TransactionRecord tran, BufferedWriter writer) throws IOException {
        // Add processing timestamp
        tran.setTranProcTs(DateFormatUtil.getDb2FormatTimestamp());

        // Write to transaction master
        writeTransactionRecord(writer, tran);

        // Update account balance
        CardXrefRecord xref = xrefByCardNum.get(tran.getTranCardNum());
        if (xref != null) {
            AccountRecord account = accountMap.get(xref.getXrefAcctId());
            if (account != null) {
                updateAccountBalance(account, tran);
                updateTranCatBalance(xref.getXrefAcctId(), tran);
            }
        }
    }

    /**
     * Update account balance based on transaction (2000-A-UPDATE-ACCT).
     */
    void updateAccountBalance(AccountRecord account, TransactionRecord tran) {
        BigDecimal amount = tran.getTranAmt();
        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setAcctCurrCycDebit(account.getAcctCurrCycDebit().add(amount));
        } else {
            account.setAcctCurrCycCredit(account.getAcctCurrCycCredit().add(amount.abs()));
        }
        account.setAcctCurrBal(account.getAcctCurrBal().add(amount));
    }

    /**
     * Update transaction category balance (2000-B-UPDATE-TCATBAL).
     */
    void updateTranCatBalance(long acctId, TransactionRecord tran) {
        String key = String.format("%011d%2s%04d", acctId, tran.getTranTypeCd(), tran.getTranCatCd());
        TranCatBalRecord catBal = tranCatBalMap.get(key);
        if (catBal != null) {
            catBal.setTranCatBal(catBal.getTranCatBal().add(tran.getTranAmt()));
        } else {
            // Create new category balance record
            TranCatBalRecord newCatBal = new TranCatBalRecord();
            newCatBal.setTrancatAcctId(acctId);
            newCatBal.setTrancatTypeCd(tran.getTranTypeCd());
            newCatBal.setTrancatCd(tran.getTranCatCd());
            newCatBal.setTranCatBal(tran.getTranAmt());
            tranCatBalMap.put(key, newCatBal);
        }
    }

    /**
     * Parse a daily transaction record from a fixed-width line.
     */
    static TransactionRecord parseDailyTransaction(String line) {
        TransactionRecord rec = new TransactionRecord();
        rec.setTranId(safeSubstring(line, 0, 16));
        rec.setTranTypeCd(safeSubstring(line, 16, 18));
        rec.setTranCatCd(parseInt(line, 18, 22));
        rec.setTranSource(safeSubstring(line, 22, 32));
        rec.setTranDesc(safeSubstring(line, 32, 132));
        rec.setTranAmt(parseDecimal(line, 132, 144));
        rec.setTranMerchantId(parseLong(line, 144, 153));
        rec.setTranMerchantName(safeSubstring(line, 153, 203));
        rec.setTranMerchantCity(safeSubstring(line, 203, 253));
        rec.setTranMerchantZip(safeSubstring(line, 253, 263));
        rec.setTranCardNum(safeSubstring(line, 263, 279));
        rec.setTranOrigTs(safeSubstring(line, 279, 305));
        rec.setTranProcTs(safeSubstring(line, 305, 331));
        return rec;
    }

    private void writeTransactionRecord(BufferedWriter writer, TransactionRecord txn) throws IOException {
        writer.write(String.format("%-16s%-2s%04d%-10s%-100s%+012.2f%09d%-50s%-50s%-10s%-16s%-26s%-26s",
                txn.getTranId(),
                txn.getTranTypeCd(),
                txn.getTranCatCd(),
                txn.getTranSource(),
                txn.getTranDesc(),
                txn.getTranAmt(),
                txn.getTranMerchantId(),
                txn.getTranMerchantName(),
                txn.getTranMerchantCity(),
                txn.getTranMerchantZip(),
                txn.getTranCardNum(),
                txn.getTranOrigTs(),
                txn.getTranProcTs()));
        writer.newLine();
    }

    private void writeRejectRecord(BufferedWriter writer, String originalLine,
                                   ValidationResult validation) throws IOException {
        writer.write(String.format("%s|%04d|%s",
                originalLine,
                validation.failReasonCode(),
                validation.failReasonDesc()));
        writer.newLine();
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

    private static BigDecimal parseDecimal(String line, int start, int end) {
        String s = safeSubstring(line, start, end);
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public int getRejectCount() {
        return rejectCount;
    }

    /**
     * Validation result record - replaces WS-VALIDATION-TRAILER in COBOL.
     */
    public record ValidationResult(int failReasonCode, String failReasonDesc) {
        public boolean isValid() {
            return failReasonCode == 0;
        }
    }
}
