package com.carddemo.service;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.TransactionValidationException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Transaction;
import com.carddemo.model.TransactionCategoryBalance;
import com.carddemo.model.TransactionCategoryBalanceId;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.DateTimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TransactionService {

    private static final int PAGE_SIZE = 10;

    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tcatBalRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository,
                              AccountRepository accountRepository,
                              TransactionCategoryBalanceRepository tcatBalRepository) {
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.tcatBalRepository = tcatBalRepository;
    }

    public Page<Transaction> getTransactions(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return transactionRepository.findAllByOrderByTranIdDesc(pageable);
    }

    public Page<Transaction> getTransactionsByCardNum(String cardNum, int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        return transactionRepository.findByCardNum(cardNum, pageable);
    }

    public Optional<Transaction> findById(String tranId) {
        return transactionRepository.findById(tranId);
    }

    @Transactional
    public Transaction addTransaction(TransactionAddRequest request) {
        // Validate card exists in xref (CBTRN02C lines 380-392)
        CardXref xref = cardXrefRepository.findById(request.getCardNum())
                .orElseThrow(() -> new TransactionValidationException(100,
                        "Card number not found in cross-reference: " + request.getCardNum()));

        // Validate account exists (CBTRN02C lines 393-422)
        Account account = accountRepository.findById(xref.getAcctId())
                .orElseThrow(() -> new TransactionValidationException(101,
                        "Account not found for card: " + request.getCardNum()));

        // Check credit limit (CBTRN02C lines 403-413)
        BigDecimal newBalance = account.getCurrBal().add(request.getAmount());
        if (newBalance.compareTo(account.getCreditLimit()) > 0) {
            throw new TransactionValidationException(102,
                    "Transaction would exceed credit limit. Current balance: " +
                    account.getCurrBal() + ", Credit limit: " + account.getCreditLimit());
        }

        // Check account expiration (CBTRN02C lines 414-420)
        if (DateTimeUtil.isDateExpired(account.getExpirationDate())) {
            throw new TransactionValidationException(103,
                    "Account has expired: " + account.getExpirationDate());
        }

        // Generate transaction ID
        Long maxId = transactionRepository.findMaxTranId();
        String tranId = String.format("%016d", maxId + 1);

        String timestamp = DateTimeUtil.generateTransactionTimestamp();

        Transaction transaction = Transaction.builder()
                .tranId(tranId)
                .typeCd(request.getTypeCd())
                .catCd(request.getCatCd())
                .source(request.getSource() != null ? request.getSource() : "ONLINE")
                .description(request.getDescription())
                .amount(request.getAmount())
                .merchantId(request.getMerchantId() != null ? request.getMerchantId() : 0L)
                .merchantName(request.getMerchantName())
                .merchantCity(request.getMerchantCity())
                .merchantZip(request.getMerchantZip())
                .cardNum(request.getCardNum())
                .origTimestamp(timestamp)
                .procTimestamp(timestamp)
                .build();

        transaction = transactionRepository.save(transaction);

        // Update transaction category balance (CBTRN02C lines 467-542)
        updateCategoryBalance(xref.getAcctId(), request.getTypeCd(),
                request.getCatCd(), request.getAmount());

        // Update account balances (CBTRN02C lines 545-560)
        account.setCurrBal(newBalance);
        if (request.getAmount().signum() >= 0) {
            account.setCurrCycDebit(account.getCurrCycDebit().add(request.getAmount()));
        } else {
            account.setCurrCycCredit(account.getCurrCycCredit().add(request.getAmount().abs()));
        }
        accountRepository.save(account);

        return transaction;
    }

    private void updateCategoryBalance(Long acctId, String typeCd, Integer catCd, BigDecimal amount) {
        TransactionCategoryBalanceId id = new TransactionCategoryBalanceId(acctId, typeCd, catCd);
        TransactionCategoryBalance tcatBal = tcatBalRepository.findById(id)
                .orElse(TransactionCategoryBalance.builder()
                        .acctId(acctId)
                        .typeCd(typeCd)
                        .catCd(catCd)
                        .balance(BigDecimal.ZERO)
                        .build());

        tcatBal.setBalance(tcatBal.getBalance().add(amount));
        tcatBalRepository.save(tcatBal);
    }
}
