package com.carddemo.service;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountExpiredException;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.OverLimitException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Replaces COTRN00C (transaction list), COTRN01C (transaction view),
 * and COTRN02C (transaction add) - online portions.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository,
                               CardXrefRepository cardXrefRepository,
                               AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
    }

    public Page<Transaction> getTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    public Page<Transaction> getTransactionsByCard(String cardNum, Pageable pageable) {
        return transactionRepository.findByCardNum(cardNum, pageable);
    }

    public Page<Transaction> getTransactionsByAccount(String acctId, Pageable pageable) {
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        List<String> cardNums = xrefs.stream().map(CardXref::getCardNum).toList();
        if (cardNums.isEmpty()) {
            return Page.empty(pageable);
        }
        return transactionRepository.findByCardNumIn(cardNums, pageable);
    }

    public Transaction getTransaction(String tranId) {
        return transactionRepository.findById(tranId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + tranId));
    }

    /**
     * Add a new transaction - matches COTRN02C online validation logic.
     * Validates card exists in CardXref, account exists and is active,
     * checks credit limit, and checks card/account expiration.
     */
    @Transactional
    public Transaction addTransaction(TransactionRequest request) {
        // Validate card exists in CardXref (1500-A-LOOKUP-XREF)
        CardXref xref = cardXrefRepository.findById(request.getCardNum())
                .orElseThrow(() -> new CardNotFoundException(request.getCardNum()));

        // Validate account exists (1500-B-LOOKUP-ACCT)
        Account account = accountRepository.findById(xref.getAcctId())
                .orElseThrow(() -> new AccountNotFoundException(xref.getAcctId()));

        // Check credit limit: ACCT-CREDIT-LIMIT >= currentCycleCredit - currentCycleDebit + tranAmount
        BigDecimal tempBal = account.getCurrentCycleCredit()
                .subtract(account.getCurrentCycleDebit())
                .add(request.getAmount());
        if (account.getCreditLimit().compareTo(tempBal) < 0) {
            throw new OverLimitException(xref.getAcctId());
        }

        // Check account expiration: ACCT-EXPIRAION-DATE >= transaction date
        if (account.getExpirationDate() != null &&
            account.getExpirationDate().isBefore(LocalDate.now())) {
            throw new AccountExpiredException(xref.getAcctId());
        }

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTranId(generateTransactionId());
        transaction.setTypeCd(request.getTypeCd());
        transaction.setCatCd(request.getCatCd());
        transaction.setSource(request.getSource() != null ? request.getSource() : "Online");
        transaction.setDescription(request.getDescription() != null ? request.getDescription() : "");
        transaction.setAmount(request.getAmount());
        transaction.setMerchantId(request.getMerchantId() != null ? request.getMerchantId() : 0L);
        transaction.setMerchantName(request.getMerchantName() != null ? request.getMerchantName() : "");
        transaction.setMerchantCity(request.getMerchantCity() != null ? request.getMerchantCity() : "");
        transaction.setMerchantZip(request.getMerchantZip() != null ? request.getMerchantZip() : "");
        transaction.setCardNum(request.getCardNum());
        transaction.setOrigTimestamp(LocalDateTime.now());
        transaction.setProcTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
