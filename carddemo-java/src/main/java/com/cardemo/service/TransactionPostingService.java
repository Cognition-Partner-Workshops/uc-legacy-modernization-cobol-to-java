package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import com.cardemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Java equivalent of COBOL program CBTRN02C.
 * Posts records from daily transaction file, validates and processes them.
 */
public class TransactionPostingService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000");

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository categoryBalanceRepository;

    private int transactionCount;
    private int rejectCount;

    public TransactionPostingService(TransactionRepository transactionRepository,
                                      CardXrefRepository cardXrefRepository,
                                      AccountRepository accountRepository,
                                      TransactionCategoryBalanceRepository categoryBalanceRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.categoryBalanceRepository = categoryBalanceRepository;
        this.transactionCount = 0;
        this.rejectCount = 0;
    }

    /**
     * Result of transaction validation.
     */
    public static class ValidationResult {
        private final int failReasonCode;
        private final String failReasonDescription;

        public ValidationResult(int failReasonCode, String failReasonDescription) {
            this.failReasonCode = failReasonCode;
            this.failReasonDescription = failReasonDescription;
        }

        public static ValidationResult success() {
            return new ValidationResult(0, "");
        }

        public int getFailReasonCode() {
            return failReasonCode;
        }

        public String getFailReasonDescription() {
            return failReasonDescription;
        }

        public boolean isValid() {
            return failReasonCode == 0;
        }
    }

    /**
     * Validates a daily transaction.
     * Equivalent of COBOL 1500-VALIDATE-TRAN.
     */
    public ValidationResult validateTransaction(Transaction dailyTransaction) {
        // 1500-A: Lookup card number in cross-reference
        Optional<CardXref> xrefOpt =
                cardXrefRepository.findByCardNumber(dailyTransaction.getCardNumber());
        if (xrefOpt.isEmpty()) {
            return new ValidationResult(100, "INVALID CARD NUMBER FOUND");
        }

        CardXref xref = xrefOpt.get();

        // 1500-B: Lookup account
        Optional<Account> accountOpt =
                accountRepository.findByAccountId(xref.getAccountId());
        if (accountOpt.isEmpty()) {
            return new ValidationResult(101, "ACCOUNT RECORD NOT FOUND");
        }

        Account account = accountOpt.get();

        // Check credit limit: cyclicCredit - cyclicDebit + transactionAmount <= creditLimit
        BigDecimal tempBalance = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(dailyTransaction.getAmount());

        if (account.getCreditLimit().compareTo(tempBalance) < 0) {
            return new ValidationResult(102, "OVERLIMIT TRANSACTION");
        }

        // Check expiration: account expiration >= transaction origin timestamp date portion
        String expirationDate = account.getExpirationDate();
        String transactionDate = dailyTransaction.getOriginTimestamp();
        if (transactionDate != null && transactionDate.length() >= 10) {
            transactionDate = transactionDate.substring(0, 10);
        }
        if (expirationDate != null && transactionDate != null
                && expirationDate.compareTo(transactionDate) < 0) {
            return new ValidationResult(103,
                    "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
        }

        return ValidationResult.success();
    }

    /**
     * Posts a validated transaction.
     * Equivalent of COBOL 2000-POST-TRANSACTION.
     */
    public void postTransaction(Transaction dailyTransaction) {
        Optional<CardXref> xrefOpt =
                cardXrefRepository.findByCardNumber(dailyTransaction.getCardNumber());
        if (xrefOpt.isEmpty()) {
            throw new IllegalStateException("Card xref not found for posting");
        }

        CardXref xref = xrefOpt.get();
        Optional<Account> accountOpt =
                accountRepository.findByAccountId(xref.getAccountId());
        if (accountOpt.isEmpty()) {
            throw new IllegalStateException("Account not found for posting");
        }

        Account account = accountOpt.get();

        // Create the transaction record
        Transaction posted = new Transaction();
        posted.setTransactionId(dailyTransaction.getTransactionId());
        posted.setTypeCode(dailyTransaction.getTypeCode());
        posted.setCategoryCode(dailyTransaction.getCategoryCode());
        posted.setSource(dailyTransaction.getSource());
        posted.setDescription(dailyTransaction.getDescription());
        posted.setAmount(dailyTransaction.getAmount());
        posted.setMerchantId(dailyTransaction.getMerchantId());
        posted.setMerchantName(dailyTransaction.getMerchantName());
        posted.setMerchantCity(dailyTransaction.getMerchantCity());
        posted.setMerchantZip(dailyTransaction.getMerchantZip());
        posted.setCardNumber(dailyTransaction.getCardNumber());
        posted.setOriginTimestamp(dailyTransaction.getOriginTimestamp());
        posted.setProcessedTimestamp(generateTimestamp());

        // Update category balance (2700)
        updateCategoryBalance(xref.getAccountId(), dailyTransaction);

        // Update account record (2800)
        updateAccountRecord(account, dailyTransaction.getAmount());

        // Write transaction (2900)
        transactionRepository.save(posted);
        transactionCount++;
    }

    /**
     * Processes a daily transaction (validate + post or reject).
     * Main processing loop equivalent.
     */
    public boolean processDailyTransaction(Transaction dailyTransaction) {
        ValidationResult result = validateTransaction(dailyTransaction);
        if (result.isValid()) {
            postTransaction(dailyTransaction);
            return true;
        } else {
            rejectCount++;
            return false;
        }
    }

    /**
     * Updates the transaction category balance.
     * Equivalent of COBOL 2700-UPDATE-TCATBAL.
     */
    void updateCategoryBalance(long accountId, Transaction transaction) {
        Optional<TransactionCategoryBalance> existing =
                categoryBalanceRepository.findByKey(
                        accountId,
                        transaction.getTypeCode(),
                        transaction.getCategoryCode());

        if (existing.isPresent()) {
            TransactionCategoryBalance balance = existing.get();
            balance.setBalance(balance.getBalance().add(transaction.getAmount()));
            categoryBalanceRepository.update(balance);
        } else {
            TransactionCategoryBalance newBalance = new TransactionCategoryBalance();
            newBalance.setAccountId(accountId);
            newBalance.setTypeCode(transaction.getTypeCode());
            newBalance.setCategoryCode(transaction.getCategoryCode());
            newBalance.setBalance(transaction.getAmount());
            categoryBalanceRepository.save(newBalance);
        }
    }

    /**
     * Updates account balances after posting.
     * Equivalent of COBOL 2800-UPDATE-ACCOUNT-REC.
     */
    void updateAccountRecord(Account account, BigDecimal transactionAmount) {
        account.setCurrentBalance(
                account.getCurrentBalance().add(transactionAmount));
        if (transactionAmount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrentCycleCredit(
                    account.getCurrentCycleCredit().add(transactionAmount));
        } else {
            account.setCurrentCycleDebit(
                    account.getCurrentCycleDebit().add(transactionAmount));
        }
        accountRepository.update(account);
    }

    String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    public int getTransactionCount() {
        return transactionCount;
    }

    public int getRejectCount() {
        return rejectCount;
    }

    public int getReturnCode() {
        return rejectCount > 0 ? 4 : 0;
    }
}
