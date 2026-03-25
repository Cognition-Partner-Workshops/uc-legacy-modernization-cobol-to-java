package com.carddemo.service;

import com.carddemo.dto.TransactionCreateDTO;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.TransactionIdGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * Transaction service - consolidates COTRN00C (list), COTRN01C (view), COTRN02C (add).
 * COTRN02C performs multi-file validation (card exists, account active, xref valid).
 * This service encapsulates all transaction business logic.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionIdGenerator idGenerator;

    public TransactionService(TransactionRepository transactionRepository,
                              CardRepository cardRepository,
                              AccountRepository accountRepository,
                              CardXrefRepository cardXrefRepository,
                              TransactionIdGenerator idGenerator) {
        this.transactionRepository = transactionRepository;
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * List transactions with pagination - replaces COTRN00C's STARTBR/READNEXT/READPREV.
     */
    public Page<Transaction> listTransactions(Pageable pageable) {
        return transactionRepository.findAllByOrderByTransactionIdDesc(pageable);
    }

    /**
     * Get transaction by ID - replaces COTRN01C's READ operation.
     */
    public Transaction getTransaction(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));
    }

    /**
     * Create a new transaction - replaces COTRN02C's validation and WRITE logic.
     * Validates card exists, card is active, cross-reference exists, account is active.
     */
    @Transactional
    public Transaction createTransaction(TransactionCreateDTO dto) {
        Card card = cardRepository.findById(dto.getCardNumber())
                .orElseThrow(() -> new BusinessValidationException(
                        "Card not found: " + dto.getCardNumber()));

        if (!"Y".equals(card.getActiveStatus())) {
            throw new BusinessValidationException("Card is not active: " + dto.getCardNumber());
        }

        CardXref xref = cardXrefRepository.findByCardNumber(dto.getCardNumber())
                .orElseThrow(() -> new BusinessValidationException(
                        "No cross-reference found for card: " + dto.getCardNumber()));

        Account account = accountRepository.findById(xref.getAccountId())
                .orElseThrow(() -> new BusinessValidationException(
                        "Account not found for card: " + dto.getCardNumber()));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new BusinessValidationException("Account is not active: " + account.getAccountId());
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(idGenerator.generateNextId());
        transaction.setTypeCode(dto.getTypeCode());
        transaction.setCategoryCode(dto.getCategoryCode());
        transaction.setSource(dto.getSource());
        transaction.setDescription(dto.getDescription());
        transaction.setAmount(dto.getAmount());
        transaction.setMerchantId(dto.getMerchantId());
        transaction.setMerchantName(dto.getMerchantName());
        transaction.setMerchantCity(dto.getMerchantCity());
        transaction.setMerchantZip(dto.getMerchantZip());
        transaction.setCardNumber(dto.getCardNumber());
        transaction.setOriginatedTs(LocalDateTime.now());
        transaction.setProcessedTs(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }
}
