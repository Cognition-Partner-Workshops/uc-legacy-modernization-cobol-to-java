package com.cardemo.service.online;

import com.cardemo.common.DateTimeUtil;
import com.cardemo.model.AccountRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Bill Payment Service - migrated from COBOL program COBIL00C.cbl.
 * Handles bill payment processing for credit card accounts.
 * Original: CICS program with TRANID CB00, reads/rewrites ACCTDAT, writes TRANSACT.
 */
@Service
public class BillPaymentService {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public BillPaymentService(AccountRepository accountRepository,
                              TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Process a bill payment - migrated from PROCESS-ENTER-KEY paragraph.
     * Validates payment amount, updates account balance, creates transaction record.
     */
    @Transactional
    public TransactionRecord processBillPayment(long acctId, BigDecimal paymentAmount,
                                                String cardNum) {
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BillPaymentException("Payment amount must be greater than zero...");
        }

        // Read account for update - equivalent to CICS READ with UPDATE on ACCTDAT
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            throw new BillPaymentException("Account not found...");
        }

        AccountRecord account = acctOpt.get();

        // Update account balance - reduce by payment amount
        BigDecimal newBalance = account.getAcctCurrBal().subtract(paymentAmount);
        account.setAcctCurrBal(newBalance);

        // Update cycle credit
        BigDecimal newCycCredit = account.getAcctCurrCycCredit().add(paymentAmount);
        account.setAcctCurrCycCredit(newCycCredit);

        accountRepository.save(account);

        // Create transaction record for the payment
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(generatePaymentTranId());
        tran.setTranTypeCd("BP");
        tran.setTranCatCd(5001);
        tran.setTranSource("ONLINE");
        tran.setTranDesc("Bill Payment");
        tran.setTranAmt(paymentAmount.negate()); // Payment is negative (credit)
        tran.setTranCardNum(cardNum);
        tran.setTranMerchantId(0);
        tran.setTranMerchantName("BILL PAYMENT");
        tran.setTranMerchantCity("");
        tran.setTranMerchantZip("");

        String timestamp = DateTimeUtil.getCurrentTimestamp();
        tran.setTranOrigTs(timestamp);
        tran.setTranProcTs(timestamp);

        TransactionRecord saved = transactionRepository.save(tran);
        log.info("Bill payment of {} processed for account {}", paymentAmount, acctId);
        return saved;
    }

    private String generatePaymentTranId() {
        return String.format("%016d", System.currentTimeMillis() % 10000000000000000L);
    }

    public static class BillPaymentException extends RuntimeException {
        public BillPaymentException(String message) {
            super(message);
        }
    }
}
