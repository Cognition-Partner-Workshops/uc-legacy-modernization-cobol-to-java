package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public BillPaymentService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository,
                              CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Process bill payment - mirrors COBIL00C.cbl
     * Validates account, reduces balance, creates transaction record
     */
    @Transactional
    public Transaction processPayment(Long acctId, BigDecimal amount) {
        // Validate account exists and is active
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + acctId));

        if (!"Y".equals(account.getActiveStatus())) {
            throw new IllegalArgumentException("Account is not active: " + acctId);
        }

        // Validate payment amount
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        if (amount.compareTo(account.getCurrentBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds current balance");
        }

        // Reduce balance
        BigDecimal newBalance = account.getCurrentBalance().subtract(amount);
        account.setCurrentBalance(newBalance);
        account.setCurrentCycleCredit(
                account.getCurrentCycleCredit().add(amount));
        accountRepository.save(account);

        // Create payment transaction record
        String cardNum = cardXrefRepository.findByAcctId(acctId)
                .stream()
                .findFirst()
                .map(xref -> xref.getCardNum())
                .orElse("0000000000000000");

        Transaction paymentTxn = new Transaction();
        paymentTxn.setCardNum(cardNum);
        paymentTxn.setTranId(String.format("%016d", System.currentTimeMillis() % 10000000000000000L));
        paymentTxn.setTypeCd("01");
        paymentTxn.setCatCd(5);
        paymentTxn.setSource("ONLINE");
        paymentTxn.setDescription("Bill Payment");
        paymentTxn.setAmount(amount.negate());
        paymentTxn.setMerchantId(0L);
        paymentTxn.setMerchantName("PAYMENT");
        paymentTxn.setMerchantCity("");
        paymentTxn.setMerchantZip("");

        String now = LocalDateTime.now().format(TS_FORMATTER);
        paymentTxn.setOrigTimestamp(now);
        paymentTxn.setProcTimestamp(now);

        return transactionRepository.save(paymentTxn);
    }
}
