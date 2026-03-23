package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Replaces CBTRN02C.cbl - Post records from daily transaction file.
 * Replicates the exact logic from the COBOL batch program.
 */
@Service
public class TransactionPostingService {

    private static final Logger log = LoggerFactory.getLogger(TransactionPostingService.class);

    // Rejection reason codes matching COBOL
    public static final int REJECT_INVALID_CARD = 100;
    public static final int REJECT_ACCOUNT_NOT_FOUND = 101;
    public static final int REJECT_OVERLIMIT = 102;
    public static final int REJECT_EXPIRED = 103;

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;

    public TransactionPostingService(CardXrefRepository cardXrefRepository,
                                      AccountRepository accountRepository,
                                      TransactionRepository transactionRepository,
                                      TransactionCategoryBalanceRepository tcatBalRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.tcatBalRepository = tcatBalRepository;
    }

    /**
     * Validate a daily transaction record.
     * Returns 0 if valid, or a rejection reason code (100-103).
     * Matches CBTRN02C.cbl 1500-VALIDATE-TRAN logic.
     */
    public ValidationResult validateTransaction(Transaction dailyTran) {
        // 1500-A-LOOKUP-XREF: Look up card number in CardXref
        Optional<CardXref> xrefOpt = cardXrefRepository.findById(dailyTran.getCardNum());
        if (xrefOpt.isEmpty()) {
            return new ValidationResult(REJECT_INVALID_CARD, "INVALID CARD NUMBER FOUND");
        }

        CardXref xref = xrefOpt.get();

        // 1500-B-LOOKUP-ACCT: Look up account by acctId from xref
        Optional<Account> acctOpt = accountRepository.findById(xref.getAcctId());
        if (acctOpt.isEmpty()) {
            return new ValidationResult(REJECT_ACCOUNT_NOT_FOUND, "ACCOUNT RECORD NOT FOUND");
        }

        Account account = acctOpt.get();

        // Check credit limit: ACCT-CREDIT-LIMIT >= currentCycleCredit - currentCycleDebit + tranAmount
        BigDecimal tempBal = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(dailyTran.getAmount());
        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            return new ValidationResult(REJECT_OVERLIMIT, "OVERLIMIT TRANSACTION");
        }

        // Check account expiration: ACCT-EXPIRAION-DATE >= DALYTRAN-ORIG-TS(1:10)
        if (dailyTran.getOrigTimestamp() != null && account.getExpirationDate() != null) {
            LocalDate tranDate = dailyTran.getOrigTimestamp().toLocalDate();
            if (account.getExpirationDate().isBefore(tranDate)) {
                return new ValidationResult(REJECT_EXPIRED, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION");
            }
        }

        return new ValidationResult(0, null);
    }

    /**
     * Post a valid transaction.
     * Matches CBTRN02C.cbl 2000-POST-TRANSACTION logic:
     * 1. Update TransactionCategoryBalance (create if not exists, add amount)
     * 2. Update Account (add to balance, update cycle credit/debit)
     * 3. Write Transaction record with processing timestamp
     */
    @Transactional
    public void postTransaction(Transaction dailyTran) {
        // Look up xref to get account ID
        CardXref xref = cardXrefRepository.findById(dailyTran.getCardNum())
                .orElseThrow(() -> new RuntimeException("CardXref not found for: " + dailyTran.getCardNum()));

        // 2700-UPDATE-TCATBAL: Update transaction category balance
        updateTransactionCategoryBalance(xref.getAcctId(), dailyTran.getTypeCd(),
                dailyTran.getCatCd(), dailyTran.getAmount());

        // 2800-UPDATE-ACCOUNT-REC: Update account balances
        updateAccountBalance(xref.getAcctId(), dailyTran.getAmount());

        // 2900-WRITE-TRANSACTION-FILE: Write transaction with processing timestamp
        dailyTran.setProcTimestamp(LocalDateTime.now());
        transactionRepository.save(dailyTran);
    }

    /**
     * Update TransactionCategoryBalance.
     * Matches CBTRN02C.cbl 2700-UPDATE-TCATBAL:
     * - If record exists, add amount to balance (2700-B-UPDATE-TCATBAL-REC)
     * - If not exists, create new record with amount as balance (2700-A-CREATE-TCATBAL-REC)
     */
    @Transactional
    public void updateTransactionCategoryBalance(String acctId, String typeCd,
                                                  Integer catCd, BigDecimal amount) {
        Optional<TransactionCategoryBalance> existing =
                tcatBalRepository.findByAcctIdAndTypeCdAndCatCd(acctId, typeCd, catCd);

        if (existing.isPresent()) {
            TransactionCategoryBalance tcatBal = existing.get();
            tcatBal.setBalance(tcatBal.getBalance().add(amount));
            tcatBalRepository.save(tcatBal);
        } else {
            TransactionCategoryBalance newTcatBal = new TransactionCategoryBalance();
            newTcatBal.setAcctId(acctId);
            newTcatBal.setTypeCd(typeCd);
            newTcatBal.setCatCd(catCd);
            newTcatBal.setBalance(amount);
            tcatBalRepository.save(newTcatBal);
        }
    }

    /**
     * Update account balances after posting a transaction.
     * Matches CBTRN02C.cbl 2800-UPDATE-ACCOUNT-REC:
     * - Add amount to current balance
     * - If amount >= 0, add to current cycle credit
     * - If amount < 0, add to current cycle debit
     */
    @Transactional
    public void updateAccountBalance(String acctId, BigDecimal amount) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + acctId));

        account.setCurrentBalance(account.getCurrentBalance().add(amount));

        if (amount.compareTo(BigDecimal.ZERO) >= 0) {
            account.setCurrentCycleCredit(account.getCurrentCycleCredit().add(amount));
        } else {
            account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(amount));
        }

        accountRepository.save(account);
    }

    /**
     * Process a batch of daily transactions.
     * Returns list of rejected transactions with reason codes.
     */
    @Transactional
    public PostingResult processDailyTransactions(List<Transaction> dailyTransactions) {
        int processedCount = 0;
        int rejectedCount = 0;
        List<RejectedTransaction> rejects = new ArrayList<>();

        for (Transaction tran : dailyTransactions) {
            processedCount++;
            ValidationResult validation = validateTransaction(tran);

            if (validation.reasonCode() == 0) {
                postTransaction(tran);
            } else {
                rejectedCount++;
                rejects.add(new RejectedTransaction(tran, validation.reasonCode(),
                        validation.reasonDescription()));
            }
        }

        log.info("TRANSACTIONS PROCESSED: {}", processedCount);
        log.info("TRANSACTIONS REJECTED: {}", rejectedCount);

        return new PostingResult(processedCount, rejectedCount, rejects);
    }

    public record ValidationResult(int reasonCode, String reasonDescription) {}
    public record RejectedTransaction(Transaction transaction, int reasonCode, String reasonDescription) {}
    public record PostingResult(int processedCount, int rejectedCount, List<RejectedTransaction> rejects) {}
}
