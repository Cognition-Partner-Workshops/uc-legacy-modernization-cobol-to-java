package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * Bill Payment Service - converted from COBOL program COBIL00C.cbl
 * Original: CICS Bill Payment screen
 * Handles credit card bill payments.
 */
@Service
public class BillPaymentService {

    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS");

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
     * Process a bill payment - equivalent to COBIL00C PROCESS-ENTER-KEY.
     *
     * @param acctId        the account ID
     * @param paymentAmount the payment amount (positive value)
     * @return the created payment transaction
     */
    @Transactional
    public Transaction processPayment(Long acctId, BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        Optional<Account> accountOpt = accountRepository.findById(acctId);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Account not found: " + acctId);
        }

        Account account = accountOpt.get();

        // Find card number for the account via cross-reference
        var xrefs = cardXrefRepository.findByAcctId(acctId);
        if (xrefs.isEmpty()) {
            throw new IllegalArgumentException("No card found for account: " + acctId);
        }
        CardXref xref = xrefs.get(0);

        // Create payment transaction
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        Transaction payment = new Transaction();
        payment.setTranId(UUID.randomUUID().toString().substring(0, 16));
        payment.setTypeCd("BP");
        payment.setCatCd(5000);
        payment.setSource("ONLINE");
        payment.setDescription("Bill Payment");
        payment.setAmount(paymentAmount.negate()); // Payments reduce balance
        payment.setCardNum(xref.getCardNum());
        payment.setOrigTimestamp(timestamp);
        payment.setProcTimestamp(timestamp);

        transactionRepository.save(payment);

        // Update account balance
        BigDecimal currentBal = account.getCurrentBalance() != null
                ? account.getCurrentBalance() : BigDecimal.ZERO;
        account.setCurrentBalance(currentBal.subtract(paymentAmount));

        BigDecimal cycCredit = account.getCurrentCycleCredit() != null
                ? account.getCurrentCycleCredit() : BigDecimal.ZERO;
        account.setCurrentCycleCredit(cycCredit.add(paymentAmount));

        accountRepository.save(account);

        return payment;
    }
}
