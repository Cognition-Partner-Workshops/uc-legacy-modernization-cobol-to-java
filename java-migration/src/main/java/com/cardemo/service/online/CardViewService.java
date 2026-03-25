package com.cardemo.service.online;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Credit Card View Service - migrated from COBOL program COCRDSLC.cbl.
 * Handles viewing credit card details.
 * Original: CICS program with TRANID CCDL, reads CARDDAT/ACCTDAT/CUSTDAT/CARDXREF.
 */
@Service
public class CardViewService {

    private static final Logger log = LoggerFactory.getLogger(CardViewService.class);

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardViewService(CardRepository cardRepository,
                           AccountRepository accountRepository,
                           CustomerRepository customerRepository,
                           CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * View card details by card number.
     * Migrated from PROCESS-ENTER-KEY and READ file paragraphs.
     */
    public Map<String, Object> viewCard(String cardNum) {
        if (cardNum == null || cardNum.isBlank()) {
            throw new CardViewException("Card number not provided");
        }

        Optional<CardRecord> cardOpt = cardRepository.findById(cardNum.trim());
        if (cardOpt.isEmpty()) {
            throw new CardViewException("Card not found...");
        }

        CardRecord card = cardOpt.get();

        Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(cardNum.trim());
        Optional<AccountRecord> acctOpt = accountRepository.findById(card.getCardAcctId());
        Optional<CustomerRecord> custOpt = Optional.empty();

        if (xrefOpt.isPresent()) {
            custOpt = customerRepository.findById(xrefOpt.get().getXrefCustId());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("card", card);
        acctOpt.ifPresent(a -> result.put("account", a));
        custOpt.ifPresent(c -> result.put("customer", c));
        xrefOpt.ifPresent(x -> result.put("xref", x));

        log.info("Card {} viewed successfully", cardNum);
        return result;
    }

    public static class CardViewException extends RuntimeException {
        public CardViewException(String message) {
            super(message);
        }
    }
}
