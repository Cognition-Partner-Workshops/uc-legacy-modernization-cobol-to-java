package com.carddemo.service;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.TransactionIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Bill payment service - replaces COBIL00C.
 * Original COBOL: reads ACCTDAT, CXACAIX, generates transaction ID via STARTBR/READPREV,
 * creates a payment transaction, updates account balance.
 * This service consolidates the account-card resolution logic shared with COTRN02C.
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;

    public BillPaymentService(AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionRepository transactionRepository,
                              TransactionIdGenerator idGenerator) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public Transaction processPayment(BillPaymentRequest request) {
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", request.getAccountId()));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new BusinessValidationException("Account is not active: " + account.getAccountId());
        }

        if (account.getCurrentBalance().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("Account has no outstanding balance");
        }

        List<CardXref> xrefs = cardXrefRepository.findByAccountId(request.getAccountId());
        if (xrefs.isEmpty()) {
            throw new BusinessValidationException("No cards found for account: " + request.getAccountId());
        }

        String cardNumber = xrefs.get(0).getCardNumber();

        // Create payment transaction
        Transaction payment = new Transaction();
        payment.setTransactionId(idGenerator.generateNextId());
        payment.setTypeCode("BP");
        payment.setCategoryCode(5000);
        payment.setSource("ONLINE");
        payment.setDescription("Bill Payment");
        payment.setAmount(request.getAmount().negate()); // Payment is a credit (negative amount)
        payment.setCardNumber(cardNumber);
        payment.setOriginatedTs(LocalDateTime.now());
        payment.setProcessedTs(LocalDateTime.now());

        transactionRepository.save(payment);

        // Update account balance
        account.setCurrentBalance(account.getCurrentBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        return payment;
    }
}
