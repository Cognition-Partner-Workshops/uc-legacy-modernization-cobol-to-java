package com.carddemo.batch.job;

import com.carddemo.batch.model.AccountRecord;
import com.carddemo.batch.model.CardXrefRecord;
import com.carddemo.batch.model.TranCatBalRecord;
import com.carddemo.batch.model.TransactionRecord;
import com.carddemo.batch.util.TimestampUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java equivalent of COBOL program CBTRN02C.CBL - Transaction Posting.
 *
 * Reads daily transaction records, validates each transaction against
 * the card cross-reference and account files, then posts valid transactions
 * to the transaction master file. Invalid transactions are written to
 * a reject file with validation failure reasons.
 *
 * Validation rules (from COBOL 1500-VALIDATE-TRAN):
 *   - Card number must exist in cross-reference file (reason 100)
 *   - Account record must exist for the card's account (reason 101)
 *   - Transaction must not exceed credit limit (reason 102)
 *   - Transaction must not be after account expiration (reason 103)
 *
 * On successful posting (from COBOL 2000-POST-TRANSACTION):
 *   - Updates transaction category balance (creates or updates)
 *   - Updates account current balance and cycle credits/debits
 *   - Writes transaction to the transaction master file
 */
public class TransactionPostingJob {

    private final Map<String, CardXrefRecord> xrefByCardNum;
    private final Map<Long, AccountRecord> accountsByAcctId;
    private final Map<String, TranCatBalRecord> tranCatBalByKey;

    private final List<TransactionRecord> postedTransactions = new ArrayList<>();
    private final List<RejectedTransaction> rejectedTransactions = new ArrayList<>();

    private int transactionCount = 0;
    private int rejectCount = 0;

    public TransactionPostingJob(
            Map<String, CardXrefRecord> xrefByCardNum,
            Map<Long, AccountRecord> accountsByAcctId,
            Map<String, TranCatBalRecord> tranCatBalByKey) {
        this.xrefByCardNum = xrefByCardNum;
        this.accountsByAcctId = accountsByAcctId;
        this.tranCatBalByKey = tranCatBalByKey != null ? tranCatBalByKey : new HashMap<>();
    }

    /**
     * Execute the batch job - equivalent to COBOL PROCEDURE DIVISION main loop.
     */
    public int execute(List<TransactionRecord> dailyTransactions) {
        System.out.println("START OF EXECUTION OF PROGRAM CBTRN02C (Java)");

        for (TransactionRecord dailyTran : dailyTransactions) {
            transactionCount++;

            ValidationResult validation = validateTransaction(dailyTran);
            if (validation.isValid()) {
                postTransaction(dailyTran);
            } else {
                rejectCount++;
                rejectedTransactions.add(new RejectedTransaction(dailyTran,
                        validation.failureReason(), validation.failureDescription()));
            }
        }

        System.out.println("TRANSACTIONS PROCESSED :" + transactionCount);
        System.out.println("TRANSACTIONS REJECTED  :" + rejectCount);

        if (rejectCount > 0) {
            System.out.println("END OF EXECUTION OF PROGRAM CBTRN02C (Java)");
            return 4; // COBOL RETURN-CODE 4 when rejects exist
        }

        System.out.println("END OF EXECUTION OF PROGRAM CBTRN02C (Java)");
        return 0;
    }

    /**
     * Validate a daily transaction - equivalent to COBOL 1500-VALIDATE-TRAN.
     */
    ValidationResult validateTransaction(TransactionRecord dailyTran) {
        // 1500-A-LOOKUP-XREF: Check card number exists in cross-reference
        CardXrefRecord xref = xrefByCardNum.get(dailyTran.getCardNum());
        if (xref == null) {
            return new ValidationResult(100, "INVALID CARD NUMBER FOUND");
        }

        // 1500-B-LOOKUP-ACCT: Check account exists
        AccountRecord account = accountsByAcctId.get(xref.getAcctId());
        if (account == null) {
            return new ValidationResult(101, "ACCOUNT RECORD NOT FOUND");
        }

        // Check credit limit: COMPUTE WS-TEMP-BAL = ACCT-CURR-CYC-CREDIT
        //                      - ACCT-CURR-CYC-DEBIT + DALYTRAN-AMT
        BigDecimal tempBal = account.getCurrCycCredit()
                .subtract(account.getCurrCycDebit())
                .add(dailyTran.getAmount());

        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            return new ValidationResult(102, "OVERLIMIT TRANSACTION");
        }

        // Check expiration: IF ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS (1:10)
        String tranDate = dailyTran.getOrigTimestamp();
        if (tranDate != null && tranDate.length() >= 10) {
            tranDate = tranDate.substring(0, 10);
        }
        if (account.getExpirationDate() != null && tranDate != null
                && account.getExpirationDate().compareTo(tranDate) < 0) {
            return new ValidationResult(103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        return ValidationResult.VALID;
    }

    /**
     * Post a valid transaction - equivalent to COBOL 2000-POST-TRANSACTION.
     */
    private void postTransaction(TransactionRecord dailyTran) {
        CardXrefRecord xref = xrefByCardNum.get(dailyTran.getCardNum());
        AccountRecord account = accountsByAcctId.get(xref.getAcctId());

        // Build the posted transaction record
        TransactionRecord posted = new TransactionRecord();
        posted.setTranId(dailyTran.getTranId());
        posted.setTypeCd(dailyTran.getTypeCd());
        posted.setCatCd(dailyTran.getCatCd());
        posted.setSource(dailyTran.getSource());
        posted.setDescription(dailyTran.getDescription());
        posted.setAmount(dailyTran.getAmount());
        posted.setMerchantId(dailyTran.getMerchantId());
        posted.setMerchantName(dailyTran.getMerchantName());
        posted.setMerchantCity(dailyTran.getMerchantCity());
        posted.setMerchantZip(dailyTran.getMerchantZip());
        posted.setCardNum(dailyTran.getCardNum());
        posted.setOrigTimestamp(dailyTran.getOrigTimestamp());
        posted.setProcTimestamp(TimestampUtil.getDb2FormatTimestamp());

        // 2700-UPDATE-TCATBAL: Update transaction category balance
        updateTranCatBal(xref.getAcctId(), dailyTran);

        // 2800-UPDATE-ACCOUNT-REC: Update account balances
        account.setCurrBal(account.getCurrBal().add(dailyTran.getAmount()));
        if (dailyTran.getAmount().compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrCycCredit(account.getCurrCycCredit().add(dailyTran.getAmount()));
        } else {
            account.setCurrCycDebit(account.getCurrCycDebit().add(dailyTran.getAmount()));
        }

        // 2900-WRITE-TRANSACTION-FILE
        postedTransactions.add(posted);
    }

    /**
     * Update transaction category balance - equivalent to COBOL 2700-UPDATE-TCATBAL.
     */
    private void updateTranCatBal(long acctId, TransactionRecord dailyTran) {
        String key = String.format("%011d%2s%04d", acctId, dailyTran.getTypeCd(), dailyTran.getCatCd());

        TranCatBalRecord catBal = tranCatBalByKey.get(key);
        if (catBal == null) {
            // 2700-A-CREATE-TCATBAL-REC
            catBal = new TranCatBalRecord(acctId, dailyTran.getTypeCd(),
                    dailyTran.getCatCd(), dailyTran.getAmount());
            tranCatBalByKey.put(key, catBal);
        } else {
            // 2700-B-UPDATE-TCATBAL-REC
            catBal.setBalance(catBal.getBalance().add(dailyTran.getAmount()));
        }
    }

    public List<TransactionRecord> getPostedTransactions() { return postedTransactions; }
    public List<RejectedTransaction> getRejectedTransactions() { return rejectedTransactions; }
    public int getTransactionCount() { return transactionCount; }
    public int getRejectCount() { return rejectCount; }
    public Map<String, TranCatBalRecord> getTranCatBalByKey() { return tranCatBalByKey; }
    public Map<Long, AccountRecord> getAccountsByAcctId() { return accountsByAcctId; }

    /**
     * Holds a rejected transaction with its failure reason.
     */
    public record RejectedTransaction(
            TransactionRecord transaction,
            int failureReason,
            String failureDescription) {}

    /**
     * Result of transaction validation.
     */
    record ValidationResult(int failureReason, String failureDescription) {
        static final ValidationResult VALID = new ValidationResult(0, "");

        boolean isValid() { return failureReason == 0; }
    }
}
