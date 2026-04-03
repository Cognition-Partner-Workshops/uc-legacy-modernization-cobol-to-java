package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.DisclosureGroupRecord;
import com.carddemo.batch.model.TranCatBalRecord;
import com.carddemo.batch.model.TransactionRecord;
import com.carddemo.batch.util.DateFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of COBOL program CBACT04C.
 * Interest calculator batch program that:
 *   1. Reads transaction category balance records sequentially
 *   2. For each account, looks up account data, cross-reference, and disclosure group
 *   3. Computes monthly interest = (balance * rate) / 1200
 *   4. Writes interest transaction records to output file
 *   5. Updates account balances
 */
public class InterestCalculator {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculator.class);
    private static final String DEFAULT_GROUP = "DEFAULT";

    private final List<TranCatBalRecord> tranCatBalRecords;
    private final Map<Long, AccountRecord> accountMap;
    private final Map<Long, CardXrefRecord> xrefByAcctId;
    private final Map<String, DisclosureGroupRecord> disclosureGroupMap;
    private final Path transactionOutputFile;
    private final String parmDate;

    private int recordCount;
    private int tranIdSuffix;

    public InterestCalculator(
            List<TranCatBalRecord> tranCatBalRecords,
            Map<Long, AccountRecord> accountMap,
            Map<Long, CardXrefRecord> xrefByAcctId,
            Map<String, DisclosureGroupRecord> disclosureGroupMap,
            Path transactionOutputFile,
            String parmDate) {
        this.tranCatBalRecords = tranCatBalRecords;
        this.accountMap = accountMap;
        this.xrefByAcctId = xrefByAcctId;
        this.disclosureGroupMap = disclosureGroupMap;
        this.transactionOutputFile = transactionOutputFile;
        this.parmDate = parmDate;
        this.recordCount = 0;
        this.tranIdSuffix = 0;
    }

    /**
     * Execute the interest calculation batch job.
     *
     * @return list of generated interest transaction records
     */
    public List<TransactionRecord> execute() throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBACT04C (InterestCalculator)");
        List<TransactionRecord> generatedTransactions = new ArrayList<>();

        try (BufferedWriter tranWriter = Files.newBufferedWriter(transactionOutputFile)) {
            long lastAcctNum = -1;
            boolean firstTime = true;
            BigDecimal totalInt = BigDecimal.ZERO;
            AccountRecord currentAccount = null;

            for (TranCatBalRecord catBal : tranCatBalRecords) {
                recordCount++;
                log.debug("{}", catBal);

                if (catBal.getTrancatAcctId() != lastAcctNum) {
                    if (!firstTime && currentAccount != null) {
                        updateAccount(currentAccount, totalInt);
                    } else {
                        firstTime = false;
                    }

                    totalInt = BigDecimal.ZERO;
                    lastAcctNum = catBal.getTrancatAcctId();

                    currentAccount = accountMap.get(catBal.getTrancatAcctId());
                    if (currentAccount == null) {
                        log.warn("ACCOUNT NOT FOUND: {}", catBal.getTrancatAcctId());
                        continue;
                    }

                    CardXrefRecord xref = xrefByAcctId.get(catBal.getTrancatAcctId());
                    if (xref == null) {
                        log.warn("XREF NOT FOUND FOR ACCOUNT: {}", catBal.getTrancatAcctId());
                    }
                }

                if (currentAccount == null) {
                    continue;
                }

                DisclosureGroupRecord discGroup = getInterestRate(
                        currentAccount.getAcctGroupId(),
                        catBal.getTrancatTypeCd(),
                        catBal.getTrancatCd());

                if (discGroup != null && discGroup.getDisIntRate().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal monthlyInt = computeInterest(catBal.getTranCatBal(), discGroup.getDisIntRate());
                    totalInt = totalInt.add(monthlyInt);

                    CardXrefRecord xref = xrefByAcctId.get(catBal.getTrancatAcctId());
                    String cardNum = xref != null ? xref.getXrefCardNum() : "";

                    TransactionRecord txn = createInterestTransaction(
                            currentAccount, monthlyInt, cardNum);
                    generatedTransactions.add(txn);
                    writeTransactionRecord(tranWriter, txn);
                }
            }

            // Update the last account
            if (currentAccount != null) {
                updateAccount(currentAccount, totalInt);
            }
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT04C - processed {} records, generated {} transactions",
                recordCount, generatedTransactions.size());
        return generatedTransactions;
    }

    /**
     * Look up disclosure group interest rate (1200-GET-INTEREST-RATE).
     * Falls back to DEFAULT group if specific group not found.
     */
    DisclosureGroupRecord getInterestRate(String acctGroupId, String tranTypeCd, int tranCatCd) {
        String key = buildDiscGroupKey(acctGroupId, tranTypeCd, tranCatCd);
        DisclosureGroupRecord record = disclosureGroupMap.get(key);

        if (record == null) {
            log.debug("DISCLOSURE GROUP RECORD MISSING - TRY WITH DEFAULT GROUP CODE");
            String defaultKey = buildDiscGroupKey(DEFAULT_GROUP, tranTypeCd, tranCatCd);
            record = disclosureGroupMap.get(defaultKey);
        }

        if (record == null) {
            log.warn("ERROR READING DEFAULT DISCLOSURE GROUP for key: {}", key);
        }

        return record;
    }

    /**
     * Compute monthly interest (1300-COMPUTE-INTEREST).
     * Formula: monthlyInterest = (balance * rate) / 1200
     */
    BigDecimal computeInterest(BigDecimal balance, BigDecimal rate) {
        return balance.multiply(rate)
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Update account balances after interest calculation (1050-UPDATE-ACCOUNT).
     */
    void updateAccount(AccountRecord account, BigDecimal totalInterest) {
        account.setAcctCurrBal(account.getAcctCurrBal().add(totalInterest));
        account.setAcctCurrCycCredit(BigDecimal.ZERO);
        account.setAcctCurrCycDebit(BigDecimal.ZERO);
        log.debug("Updated account {} balance to {}", account.getAcctId(), account.getAcctCurrBal());
    }

    /**
     * Create an interest transaction record (1300-B-WRITE-TX).
     */
    TransactionRecord createInterestTransaction(AccountRecord account, BigDecimal monthlyInt, String cardNum) {
        tranIdSuffix++;
        TransactionRecord txn = new TransactionRecord();
        txn.setTranId(parmDate + String.format("%06d", tranIdSuffix));
        txn.setTranTypeCd("01");
        txn.setTranCatCd(5);
        txn.setTranSource("System");
        txn.setTranDesc("Int. for a/c " + account.getAcctId());
        txn.setTranAmt(monthlyInt);
        txn.setTranMerchantId(0);
        txn.setTranMerchantName("");
        txn.setTranMerchantCity("");
        txn.setTranMerchantZip("");
        txn.setTranCardNum(cardNum);
        String timestamp = DateFormatUtil.getDb2FormatTimestamp();
        txn.setTranOrigTs(timestamp);
        txn.setTranProcTs(timestamp);
        return txn;
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

    private static String buildDiscGroupKey(String groupId, String typeCd, int catCd) {
        String paddedGroup = groupId == null ? "" : groupId.trim();
        return String.format("%-10s%-2s%04d", paddedGroup, typeCd, catCd);
    }

    public int getRecordCount() {
        return recordCount;
    }
}
