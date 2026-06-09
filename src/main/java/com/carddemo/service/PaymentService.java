package com.carddemo.service;

import com.carddemo.dto.PaymentRequest;
import com.carddemo.dto.PaymentResponse;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PaymentService {

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(CardXrefRepository cardXrefRepository,
                          AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        CardXref card = cardXrefRepository.findById(request.cardNum())
                .orElseThrow(() -> new ResourceNotFoundException("Card", request.cardNum()));

        Account account = accountRepository.findById(card.getAcctId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", card.getAcctId()));

        account.setCurrentBalance(account.getCurrentBalance().add(request.amount()));
        account.setCurrentCycleDebit(account.getCurrentCycleDebit().add(request.amount()));
        accountRepository.save(account);

        String now = LocalDateTime.now().format(TS_FORMAT);
        String tranId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Transaction transaction = new Transaction();
        transaction.setTranId(tranId);
        transaction.setTranTypeCode("05");
        transaction.setTranCategoryCode(1);
        transaction.setTranSource("ONLINE");
        transaction.setTranDescription(request.description() != null ? request.description() : "Bill Payment");
        transaction.setTranAmount(request.amount());
        transaction.setMerchantId(0L);
        transaction.setMerchantName("PAYMENT");
        transaction.setMerchantCity("");
        transaction.setMerchantZip("");
        transaction.setCardNum(request.cardNum());
        transaction.setOriginTimestamp(now);
        transaction.setProcessedTimestamp(now);
        transactionRepository.save(transaction);

        return new PaymentResponse(tranId, request.cardNum(), request.amount(), "APPROVED", now);
    }
}
