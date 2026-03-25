package com.cardemo.service;

import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.model.TransactionCategoryBalanceKey;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import com.cardemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Transaction Service - converted from COBOL programs COTRN00C, COTRN01C, COTRN02C
 * Original: CICS Transaction List, View, and Add screens
 * Also incorporates batch transaction posting logic from CBTRN02C.
 */
@Service
public class TransactionService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tranCatBalRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository,
                              AccountRepository accountRepository,
                              TransactionCategoryBalanceRepository tranCatBalRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tranCatBalRepository = tranCatBalRepository;
    }

    /**
     * List transactions for a card - equivalent to COTRN00C (Transaction List).
     */
    public Page<Transaction> listTransactions(String cardNum, Pageable pageable) {
        return transactionRepository.findByCardNum(cardNum, pageable);
    }

    /**
     * List all transactions.
     */
    public List<Transaction> listAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * View transaction details - equivalent to COTRN01C (Transaction View).
     */
    public Optional<Transaction> viewTransaction(String tranId) {
        return transactionRepository.findById(tranId);
    }

    /**
     * Add a new transaction - equivalent to COTRN02C (Transaction Add).
     * Includes validation logic from CBTRN02C batch program's 1500-VALIDATE-TRAN paragraph.
     */
    @Transactional
    public Transaction addTransaction(Transaction transaction) {
        // Validate card exists in cross-reference (1500-A-LOOKUP-XREF)
        Optional<CardXref> xrefOpt = cardXrefRepository.findByCardNum(transaction.getCardNum());
        if (xrefOpt.isEmpty()) {
            throw new IllegalArgumentException(
                    "Card number not found in cross-reference: " + transaction.getCardNum());
        }

        CardXref xref = xrefOpt.get();

        // Validate account exists (1500-B-LOOKUP-ACCT)
        if (accountRepository.findById(xref.getAcctId()).isEmpty()) {
            throw new IllegalArgumentException(
                    "Account not found for card: " + transaction.getCardNum());
        }

        // Set processing timestamp
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        transaction.setProcTimestamp(timestamp);
        if (transaction.getOrigTimestamp() == null || transaction.getOrigTimestamp().isBlank()) {
            transaction.setOrigTimestamp(timestamp);
        }

        // Save transaction (2000-POST-TRANSACTION)
        Transaction saved = transactionRepository.save(transaction);

        // Update category balance (2000-POST-TRANSACTION -> update TCATBAL)
        updateCategoryBalance(xref.getAcctId(), transaction);

        // Update account balance (2000-POST-TRANSACTION -> update account)
        updateAccountBalance(xref.getAcctId(), transaction);

        return saved;
    }

    /**
     * Update transaction category balance - equivalent to CBTRN02C 2000-POST-TRANSACTION.
     */
    private void updateCategoryBalance(Long acctId, Transaction transaction) {
        TransactionCategoryBalanceKey key = new TransactionCategoryBalanceKey(
                acctId, transaction.getTypeCd(), transaction.getCatCd());

        Optional<TransactionCategoryBalance> existing = tranCatBalRepository.findById(key);
        if (existing.isPresent()) {
            TransactionCategoryBalance bal = existing.get();
            BigDecimal currentBal = bal.getBalance() != null ? bal.getBalance() : BigDecimal.ZERO;
            bal.setBalance(currentBal.add(transaction.getAmount()));
            tranCatBalRepository.save(bal);
        } else {
            TransactionCategoryBalance newBal = new TransactionCategoryBalance();
            newBal.setAcctId(acctId);
            newBal.setTypeCd(transaction.getTypeCd());
            newBal.setCatCd(transaction.getCatCd());
            newBal.setBalance(transaction.getAmount());
            tranCatBalRepository.save(newBal);
        }
    }

    /**
     * Update account balance after transaction posting.
     */
    private void updateAccountBalance(Long acctId, Transaction transaction) {
        accountRepository.findById(acctId).ifPresent(account -> {
            BigDecimal amount = transaction.getAmount();
            BigDecimal currentBal = account.getCurrentBalance() != null
                    ? account.getCurrentBalance() : BigDecimal.ZERO;
            account.setCurrentBalance(currentBal.add(amount));

            if (amount.compareTo(BigDecimal.ZERO) >= 0) {
                BigDecimal cycCredit = account.getCurrentCycleCredit() != null
                        ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
                account.setCurrentCycleCredit(cycCredit.add(amount));
            } else {
                BigDecimal cycDebit = account.getCurrentCycleDebit() != null
                        ? account.getCurrentCycleDebit() : BigDecimal.ZERO;
                account.setCurrentCycleDebit(cycDebit.add(amount.abs()));
            }

            accountRepository.save(account);
        });
    }
}
