package com.carddemo.service;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces COCRDLIC.cbl (card list), COCRDSLC.cbl (card detail),
 * and COCRDUPC.cbl (card update).
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardRepository cardRepository,
                       CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List cards by account ID.
     * Matches COCRDLIC logic: query CardXref by account, then look up each card.
     */
    public List<Card> getCardsByAccount(String acctId) {
        return cardRepository.findByAcctId(acctId);
    }

    /**
     * Get card cross-references by account ID.
     */
    public List<CardXref> getCardXrefsByAccount(String acctId) {
        return cardXrefRepository.findByAcctId(acctId);
    }

    /**
     * Get card detail by card number.
     * Matches COCRDSLC logic: read card by card number.
     */
    public Card getCard(String cardNum) {
        return cardRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));
    }

    /**
     * Look up card cross-reference by card number.
     */
    public CardXref getCardXref(String cardNum) {
        return cardXrefRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));
    }

    /**
     * Update card fields.
     * Matches COCRDUPC logic: modify card fields (activate/deactivate, etc.).
     */
    @Transactional
    public Card updateCard(String cardNum, CardUpdateRequest request) {
        Card card = getCard(cardNum);

        if (request.getEmbossedName() != null) {
            card.setEmbossedName(request.getEmbossedName());
        }
        if (request.getExpirationDate() != null) {
            card.setExpirationDate(request.getExpirationDate());
        }
        if (request.getActiveStatus() != null) {
            card.setActiveStatus(request.getActiveStatus());
        }

        return cardRepository.save(card);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }
}
