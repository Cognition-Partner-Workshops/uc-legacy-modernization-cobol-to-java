package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Replaces COBIL00C.cbl - Bill payment processing.
 */
@Service
public class BillPaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CardXrefRepository cardXrefRepository;

    public BillPaymentService(AccountRepository accountRepository,
                               TransactionRepository transactionRepository,
                               CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Process a bill payment.
     * Reads account by ID, applies payment (reduces balance), updates account record.
     */
    @Transactional
    public Transaction processPayment(String acctId, BigDecimal paymentAmount) {
        Account account = accountRepository.findById(acctId)
                .orElseThrow(() -> new AccountNotFoundException(acctId));

        // Payment reduces balance (negative amount)
        BigDecimal negativeAmount = paymentAmount.negate();
        account.setCurrentBalance(account.getCurrentBalance().add(negativeAmount));
        account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(negativeAmount));
        accountRepository.save(account);

        // Get a card number for this account
        String cardNum = cardXrefRepository.findFirstByAcctId(acctId)
                .map(xref -> xref.getCardNum())
                .orElse("");

        // Create a payment transaction record
        Transaction paymentTran = new Transaction();
        paymentTran.setTranId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        paymentTran.setTypeCd("02");
        paymentTran.setCatCd(1);
        paymentTran.setSource("Online");
        paymentTran.setDescription("Bill Payment for a/c " + acctId);
        paymentTran.setAmount(negativeAmount);
        paymentTran.setMerchantId(0L);
        paymentTran.setMerchantName("Payment");
        paymentTran.setMerchantCity("");
        paymentTran.setMerchantZip("");
        paymentTran.setCardNum(cardNum);
        paymentTran.setOrigTimestamp(LocalDateTime.now());
        paymentTran.setProcTimestamp(LocalDateTime.now());

        return transactionRepository.save(paymentTran);
    }
}
