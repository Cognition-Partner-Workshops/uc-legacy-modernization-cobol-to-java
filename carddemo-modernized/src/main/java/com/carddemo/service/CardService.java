package com.carddemo.service;

import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Card;
import com.carddemo.repository.cassandra.CardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Card service - modernized business logic from COBOL programs:
 *
 *   COCRDLIC.cbl  -> listCards()        (Credit Card List - CCLI transaction)
 *   COCRDSLC.cbl  -> getCardDetails()   (Credit Card View - CCDL transaction)
 *   COCRDUPC.cbl  -> updateCard()       (Credit Card Update - CCUP transaction)
 *
 * Legacy flow (COCRDLIC):
 *   1. STARTBR on CARDDAT file
 *   2. READNEXT in loop (up to 7 records per page)
 *   3. Populate BMS map fields for each card
 *   4. Handle PF7 (page back) / PF8 (page forward) navigation
 *   5. Selection 'S' transfers control to COCRDSLC for detail view
 *
 * Modernized: Reactive streams replace VSAM browse operations
 */
@Service
public class CardService {

    private static final Logger log = LoggerFactory.getLogger(CardService.class);

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public Flux<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Mono<Card> getCardByNumber(String cardNumber) {
        log.debug("Viewing card details: {}", cardNumber);
        return cardRepository.findById(cardNumber)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Card not found: " + cardNumber)));
    }

    public Flux<Card> getCardsByAccountId(String accountId) {
        log.debug("Listing cards for account: {}", accountId);
        return cardRepository.findByAccountId(accountId);
    }

    public Mono<Card> updateCard(String cardNumber, Card cardUpdate) {
        log.debug("Updating card: {}", cardNumber);
        return cardRepository.findById(cardNumber)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "Card not found: " + cardNumber)))
                .flatMap(existing -> {
                    if (cardUpdate.getEmbossedName() != null) {
                        existing.setEmbossedName(cardUpdate.getEmbossedName());
                    }
                    if (cardUpdate.getActiveStatus() != null) {
                        existing.setActiveStatus(cardUpdate.getActiveStatus());
                    }
                    if (cardUpdate.getExpirationDate() != null) {
                        existing.setExpirationDate(cardUpdate.getExpirationDate());
                    }
                    return cardRepository.save(existing);
                });
    }

    public Mono<Card> createCard(Card card) {
        log.debug("Creating card for account: {}", card.getAccountId());
        return cardRepository.save(card);
    }
}
