package com.cardemo.service.online;

import com.cardemo.model.CardRecord;
import com.cardemo.repository.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Credit Card Update Service - migrated from COBOL program COCRDUPC.cbl.
 * Handles updating credit card details.
 * Original: CICS program with TRANID CCUP, reads/rewrites CARDDAT.
 */
@Service
public class CardUpdateService {

    private static final Logger log = LoggerFactory.getLogger(CardUpdateService.class);

    private final CardRepository cardRepository;

    public CardUpdateService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * Get card for update - equivalent to CICS READ with UPDATE.
     */
    public CardRecord getCardForUpdate(String cardNum) {
        if (cardNum == null || cardNum.isBlank()) {
            throw new CardUpdateException("Card number not provided...");
        }
        Optional<CardRecord> cardOpt = cardRepository.findById(cardNum.trim());
        if (cardOpt.isEmpty()) {
            throw new CardUpdateException("Card NOT found...");
        }
        return cardOpt.get();
    }

    /**
     * Update card - migrated from REWRITE CARDDAT logic.
     */
    @Transactional
    public CardRecord updateCard(CardRecord updatedCard) {
        if (!cardRepository.existsById(updatedCard.getCardNum())) {
            throw new CardUpdateException("Card NOT found for update...");
        }
        CardRecord saved = cardRepository.save(updatedCard);
        log.info("Card {} updated successfully", saved.getCardNum());
        return saved;
    }

    public static class CardUpdateException extends RuntimeException {
        public CardUpdateException(String message) {
            super(message);
        }
    }
}
