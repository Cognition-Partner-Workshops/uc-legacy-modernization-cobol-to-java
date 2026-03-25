package com.cardemo.batch;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.DailyTransaction;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.model.TransactionCategoryBalanceKey;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DailyTransactionRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Processor - converted from COBOL programs CBTRN01C and CBTRN02C
 * 
 * CBTRN01C: Transaction File Read - reads all records from TRANSACT file
 * CBTRN02C: Transaction Posting - validates and posts daily transactions
 *   - 1000-DALYTRAN-GET-NEXT: Read daily transaction file sequentially
 *   - 1500-VALIDATE-TRAN: Validate transaction (lookup XREF, check account)
 *   - 2000-POST-TRANSACTION: Post transaction to TRANSACT file, update balances
 */
@Component
public class TransactionProcessor {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final TransactionRepository transactionRepository;
    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tranCatBalRepository;

    public TransactionProcessor(TransactionRepository transactionRepository,
                                DailyTransactionRepository dailyTransactionRepository,
                                CardXrefRepository cardXrefRepository,
                                AccountRepository accountRepository,
                                TransactionCategoryBalanceRepository tranCatBalRepository) {
        this.transactionRepository = transactionRepository;
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tranCatBalRepository = tranCatBalRepository;
    }

    /**
     * Read all transaction records - equivalent to CBTRN01C.
     * Original: Sequential read of TRANSACT VSAM file.
     */
    public int readAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        int count = 0;
        for (Transaction tran : transactions) {
            log.info("Tran ID: {}, Card: {}, Type: {}, Amount: {}",
                    tran.getTranId(), tran.getCardNum(),
                    tran.getTypeCd(), tran.getAmount());
            count++;
        }
        log.info("Total transaction records read: {}", count);
        return count;
    }

    /**
     * Post daily transactions - equivalent to CBTRN02C.
     * Reads daily transaction file, validates, and posts to transaction file.
     */
    @Transactional
    public PostingResult postDailyTransactions() {
        PostingResult result = new PostingResult();
        List<DailyTransaction> dailyTransactions = dailyTransactionRepository.findAll();

        for (DailyTransaction daily : dailyTransactions) {
            try {
                // 1500-VALIDATE-TRAN
                ValidationResult validation = validateTransaction(daily);
                if (!validation.valid) {
                    log.warn("Transaction {} rejected: {}", daily.getTranId(), validation.reason);
                    result.rejected++;
                    continue;
                }

                // 2000-POST-TRANSACTION
                postTransaction(daily, validation.acctId);
                result.posted++;

                log.info("Transaction {} posted for card {} amount {}",
                        daily.getTranId(), daily.getCardNum(), daily.getAmount());

            } catch (Exception e) {
                log.error("Error processing transaction {}: {}",
                        daily.getTranId(), e.getMessage());
                result.errors++;
            }
        }

        log.info("Transaction posting complete. Posted: {}, Rejected: {}, Errors: {}",
                result.posted, result.rejected, result.errors);
        return result;
    }

    /**
     * Validate a daily transaction - equivalent to 1500-VALIDATE-TRAN paragraph.
     */
    private ValidationResult validateTransaction(DailyTransaction daily) {
        ValidationResult result = new ValidationResult();

        // 1500-A-LOOKUP-XREF: Check cross-reference
        Optional<CardXref> xrefOpt = cardXrefRepository.findByCardNum(daily.getCardNum());
        if (xrefOpt.isEmpty()) {
            result.valid = false;
            result.reason = "Card not found in cross-reference: " + daily.getCardNum();
            return result;
        }

        Long acctId = xrefOpt.get().getAcctId();

        // 1500-B-LOOKUP-ACCT: Check account exists
        Optional<Account> accountOpt = accountRepository.findById(acctId);
        if (accountOpt.isEmpty()) {
            result.valid = false;
            result.reason = "Account not found: " + acctId;
            return result;
        }

        result.valid = true;
        result.acctId = acctId;
        return result;
    }

    /**
     * Post a validated transaction - equivalent to 2000-POST-TRANSACTION paragraph.
     */
    private void postTransaction(DailyTransaction daily, Long acctId) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);

        // Write to TRANSACT file
        Transaction tran = new Transaction();
        tran.setTranId(daily.getTranId());
        tran.setTypeCd(daily.getTypeCd());
        tran.setCatCd(daily.getCatCd());
        tran.setSource(daily.getSource());
        tran.setDescription(daily.getDescription());
        tran.setAmount(daily.getAmount());
        tran.setMerchantId(daily.getMerchantId());
        tran.setMerchantName(daily.getMerchantName());
        tran.setMerchantCity(daily.getMerchantCity());
        tran.setMerchantZip(daily.getMerchantZip());
        tran.setCardNum(daily.getCardNum());
        tran.setOrigTimestamp(daily.getOrigTimestamp() != null ? daily.getOrigTimestamp() : timestamp);
        tran.setProcTimestamp(timestamp);
        transactionRepository.save(tran);

        // Update TCATBAL
        updateCategoryBalance(acctId, daily);

        // Update account balance
        updateAccountBalance(acctId, daily.getAmount());
    }

    /**
     * Update transaction category balance after posting.
     */
    private void updateCategoryBalance(Long acctId, DailyTransaction daily) {
        TransactionCategoryBalanceKey key = new TransactionCategoryBalanceKey(
                acctId, daily.getTypeCd(), daily.getCatCd());

        Optional<TransactionCategoryBalance> existing = tranCatBalRepository.findById(key);
        if (existing.isPresent()) {
            TransactionCategoryBalance bal = existing.get();
            BigDecimal currentBal = bal.getBalance() != null ? bal.getBalance() : BigDecimal.ZERO;
            bal.setBalance(currentBal.add(daily.getAmount()));
            tranCatBalRepository.save(bal);
        } else {
            TransactionCategoryBalance newBal = new TransactionCategoryBalance();
            newBal.setAcctId(acctId);
            newBal.setTypeCd(daily.getTypeCd());
            newBal.setCatCd(daily.getCatCd());
            newBal.setBalance(daily.getAmount());
            tranCatBalRepository.save(newBal);
        }
    }

    /**
     * Update account balance after posting.
     */
    private void updateAccountBalance(Long acctId, BigDecimal amount) {
        accountRepository.findById(acctId).ifPresent(account -> {
            BigDecimal currentBal = account.getCurrentBalance() != null
                    ? account.getCurrentBalance() : BigDecimal.ZERO;
            account.setCurrentBalance(currentBal.add(amount));

            if (amount != null && amount.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal cycCredit = account.getCurrentCycleCredit() != null
                        ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
                account.setCurrentCycleCredit(cycCredit.add(amount));
            } else if (amount != null) {
                BigDecimal cycDebit = account.getCurrentCycleDebit() != null
                        ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
                account.setCurrentCycleDebit(cycDebit.add(amount.abs()));
            }

            accountRepository.save(account);
        });
    }

    private static class ValidationResult {
        boolean valid;
        String reason;
        Long acctId;
    }

    public static class PostingResult {
        public int posted = 0;
        public int rejected = 0;
        public int errors = 0;
    }
}
