package com.carddemo.service;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.dto.TransactionRequest;
import com.carddemo.exception.BusinessValidationException;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.Transaction;
import com.carddemo.repository.cassandra.AccountRepository;
import com.carddemo.repository.cassandra.TransactionRepository;
import com.carddemo.repository.jdbc.TransactionCategoryJdbcRepository;
import com.carddemo.repository.jdbc.TransactionTypeJdbcRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Transaction service - modernized business logic from COBOL programs:
 *
 *   COTRN00C.cbl  -> listTransactions()    (Transaction List - CT00 transaction)
 *   COTRN01C.cbl  -> getTransaction()      (Transaction View - CT01 transaction)
 *   COTRN02C.cbl  -> createTransaction()   (Transaction Add - CT02 transaction)
 *   COBIL00C.cbl  -> processBillPayment()  (Bill Payment - CB00 transaction)
 *   CBTRN02C.cbl  -> batch transaction posting logic
 *   CBTRN03C.cbl  -> generateReport()      (Transaction Report)
 *
 * Legacy flow (COTRN00C - Transaction List):
 *   1. STARTBR DATASET('TRANSACT') with RIDFLD (starting transaction ID)
 *   2. READNEXT loop (10 records per page)
 *   3. Populate BMS map rows (TRNID01O-TRNID10O, AMT0001O-AMT0010O, etc.)
 *   4. Handle PF7 (READPREV) / PF8 (READNEXT) pagination
 *   5. Selection 'S' -> XCTL to COTRN01C for detail view
 *
 * Legacy flow (COBIL00C - Bill Payment):
 *   1. Receive account ID and payment amount from screen
 *   2. Read account from ACCTDAT
 *   3. Validate payment amount
 *   4. Create payment transaction in TRANSACT
 *   5. Update account balance in ACCTDAT
 *
 * Modernized: Reactive composition with Cassandra (transactions) + JDBC (reference data)
 */
@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionTypeJdbcRepository transactionTypeJdbcRepository;
    private final TransactionCategoryJdbcRepository transactionCategoryJdbcRepository;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              TransactionTypeJdbcRepository transactionTypeJdbcRepository,
                              TransactionCategoryJdbcRepository transactionCategoryJdbcRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.transactionTypeJdbcRepository = transactionTypeJdbcRepository;
        this.transactionCategoryJdbcRepository = transactionCategoryJdbcRepository;
    }

    public Flux<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Mono<Transaction> getTransactionById(String transactionId) {
        log.debug("Viewing transaction: {}", transactionId);
        return transactionRepository.findById(transactionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Transaction not found: " + transactionId)));
    }

    public Flux<Transaction> getTransactionsByCardNumber(String cardNumber) {
        log.debug("Listing transactions for card: {}", cardNumber);
        return transactionRepository.findByCardNumber(cardNumber);
    }

    public Mono<Transaction> createTransaction(TransactionRequest request) {
        log.debug("Creating transaction for card: {}", request.getCardNumber());

        // Validate transaction type exists (via JDBC - mirrors legacy DB2 lookup)
        if (transactionTypeJdbcRepository.findByTypeCode(request.getTypeCode()).isEmpty()) {
            return Mono.error(new BusinessValidationException(
                    "Invalid transaction type code: " + request.getTypeCode()));
        }

        // Validate transaction category exists
        if (transactionCategoryJdbcRepository
                .findByTypeCodeAndCategoryCode(request.getTypeCode(), request.getCategoryCode()).isEmpty()) {
            return Mono.error(new BusinessValidationException(
                    "Invalid transaction category for type " + request.getTypeCode()
                            + ": " + request.getCategoryCode()));
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setTypeCode(request.getTypeCode());
        transaction.setCategoryCode(request.getCategoryCode());
        transaction.setSource(request.getSource());
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setMerchantId(request.getMerchantId());
        transaction.setMerchantName(request.getMerchantName());
        transaction.setMerchantCity(request.getMerchantCity());
        transaction.setMerchantZip(request.getMerchantZip());
        transaction.setCardNumber(request.getCardNumber());
        transaction.setOriginTimestamp(Instant.now());

        return transactionRepository.save(transaction);
    }

    public Mono<Transaction> processBillPayment(BillPaymentRequest request) {
        log.debug("Processing bill payment for account: {}", request.getAccountId());

        return accountRepository.findById(request.getAccountId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Account not found: " + request.getAccountId())))
                .flatMap(account -> {
                    if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new BusinessValidationException(
                                "Payment amount must be greater than zero"));
                    }

                    // Update account balance (debit the payment)
                    BigDecimal newBalance = account.getCurrentBalance()
                            .subtract(request.getAmount());
                    account.setCurrentBalance(newBalance);
                    account.setCurrentCycleCredit(
                            account.getCurrentCycleCredit().add(request.getAmount()));

                    return accountRepository.save(account)
                            .flatMap(savedAccount -> {
                                // Create payment transaction record
                                Transaction payment = new Transaction();
                                payment.setTransactionId(generateTransactionId());
                                payment.setTypeCode("PM");
                                payment.setCategoryCode(8001);
                                payment.setSource("ONLINE");
                                payment.setDescription("Bill Payment - Account " + request.getAccountId());
                                payment.setAmount(request.getAmount());
                                payment.setOriginTimestamp(Instant.now());
                                payment.setProcessedTimestamp(Instant.now());
                                return transactionRepository.save(payment);
                            });
                });
    }

    public Flux<Transaction> generateReport(String typeCode) {
        log.debug("Generating transaction report for type: {}", typeCode);
        if (typeCode != null && !typeCode.isBlank()) {
            return transactionRepository.findByTypeCode(typeCode);
        }
        return transactionRepository.findAll();
    }

    private String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
