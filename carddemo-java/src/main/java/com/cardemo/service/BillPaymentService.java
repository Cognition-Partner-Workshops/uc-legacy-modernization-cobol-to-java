package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Java equivalent of COBOL program COBIL00C.
 * Bill Payment - pays account balance in full and creates a transaction record.
 */
public class BillPaymentService {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000");

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                               CardXrefRepository cardXrefRepository,
                               TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Result of a bill payment operation.
     */
    public static class PaymentResult {

        public enum Status {
            SUCCESS,
            ACCOUNT_NOT_FOUND,
            NOTHING_TO_PAY,
            INVALID_ACCOUNT_ID,
            XREF_NOT_FOUND,
            SYSTEM_ERROR
        }

        private final Status status;
        private final String message;
        private final BigDecimal amountPaid;

        private PaymentResult(Status status, String message, BigDecimal amountPaid) {
            this.status = status;
            this.message = message;
            this.amountPaid = amountPaid;
        }

        public static PaymentResult success(BigDecimal amountPaid) {
            return new PaymentResult(Status.SUCCESS,
                    "Bill payment processed successfully", amountPaid);
        }

        public static PaymentResult accountNotFound() {
            return new PaymentResult(Status.ACCOUNT_NOT_FOUND,
                    "Account ID NOT found...", null);
        }

        public static PaymentResult nothingToPay() {
            return new PaymentResult(Status.NOTHING_TO_PAY,
                    "You have nothing to pay...", null);
        }

        public static PaymentResult invalidAccountId() {
            return new PaymentResult(Status.INVALID_ACCOUNT_ID,
                    "Acct ID can NOT be empty...", null);
        }

        public static PaymentResult xrefNotFound() {
            return new PaymentResult(Status.XREF_NOT_FOUND,
                    "Account ID NOT found...", null);
        }

        public static PaymentResult systemError(String message) {
            return new PaymentResult(Status.SYSTEM_ERROR, message, null);
        }

        public Status getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public BigDecimal getAmountPaid() {
            return amountPaid;
        }

        public boolean isSuccessful() {
            return status == Status.SUCCESS;
        }
    }

    /**
     * Validates an account ID for bill payment.
     * Equivalent of the initial validation in PROCESS-ENTER-KEY.
     */
    public PaymentResult validateAccount(String accountIdStr) {
        if (accountIdStr == null || accountIdStr.isBlank()) {
            return PaymentResult.invalidAccountId();
        }

        long accountId;
        try {
            accountId = Long.parseLong(accountIdStr.trim());
        } catch (NumberFormatException e) {
            return PaymentResult.invalidAccountId();
        }

        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return PaymentResult.accountNotFound();
        }

        Account account = accountOpt.get();
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentResult.nothingToPay();
        }

        return null; // No validation error
    }

    /**
     * Processes a bill payment for the given account.
     * Pays the full current balance and creates a transaction.
     * Equivalent of COBOL PROCESS-ENTER-KEY when CONF-PAY-YES.
     *
     * @param accountIdStr the account ID string
     * @return PaymentResult indicating success or failure
     */
    public PaymentResult processPayment(String accountIdStr) {
        if (accountIdStr == null || accountIdStr.isBlank()) {
            return PaymentResult.invalidAccountId();
        }

        long accountId;
        try {
            accountId = Long.parseLong(accountIdStr.trim());
        } catch (NumberFormatException e) {
            return PaymentResult.invalidAccountId();
        }

        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            return PaymentResult.accountNotFound();
        }

        Account account = accountOpt.get();
        if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
            return PaymentResult.nothingToPay();
        }

        // Look up card cross-reference
        Optional<CardXref> xrefOpt = cardXrefRepository.findByAccountId(accountId);
        if (xrefOpt.isEmpty()) {
            return PaymentResult.xrefNotFound();
        }

        CardXref xref = xrefOpt.get();

        // Get next transaction ID
        long nextTranId = getNextTransactionId();

        // Create bill payment transaction
        BigDecimal paymentAmount = account.getCurrentBalance();

        Transaction billPayment = createBillPaymentTransaction(
                nextTranId, paymentAmount, xref.getCardNumber());

        // Write transaction
        transactionRepository.save(billPayment);

        // Update account balance: balance = balance - paymentAmount
        account.setCurrentBalance(
                account.getCurrentBalance().subtract(paymentAmount));
        accountRepository.update(account);

        return PaymentResult.success(paymentAmount);
    }

    /**
     * Creates a bill payment transaction record.
     * Mirrors the COBOL logic for initializing the TRAN-RECORD.
     */
    Transaction createBillPaymentTransaction(long transactionId,
                                              BigDecimal amount,
                                              String cardNumber) {
        Transaction tx = new Transaction();
        tx.setTransactionId(String.format("%016d", transactionId));
        tx.setTypeCode("02");
        tx.setCategoryCode(2);
        tx.setSource("POS TERM");
        tx.setDescription("BILL PAYMENT - ONLINE");
        tx.setAmount(amount);
        tx.setCardNumber(cardNumber);
        tx.setMerchantId(999999999L);
        tx.setMerchantName("BILL PAYMENT");
        tx.setMerchantCity("N/A");
        tx.setMerchantZip("N/A");

        String timestamp = generateTimestamp();
        tx.setOriginTimestamp(timestamp);
        tx.setProcessedTimestamp(timestamp);

        return tx;
    }

    /**
     * Gets the next transaction ID by finding the last one and incrementing.
     * Equivalent of COBOL STARTBR/READPREV TRANSACT-FILE logic.
     */
    long getNextTransactionId() {
        Optional<Transaction> lastTx = transactionRepository.findLastTransaction();
        if (lastTx.isPresent()) {
            try {
                return Long.parseLong(lastTx.get().getTransactionId().trim()) + 1;
            } catch (NumberFormatException e) {
                return 1;
            }
        }
        return 1;
    }

    String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }
}
