package com.cardemo.service;

import com.cardemo.model.Card;
import com.cardemo.model.CardXref;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Card Service - converted from COBOL programs COCRDLIC.cbl, COCRDSLC.cbl, COCRDUPC.cbl
 * Original: CICS Credit Card List, View, and Update screens
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List cards for an account - equivalent to COCRDLIC (Card List).
     */
    public List<Card> listCardsByAccount(Long acctId) {
        return cardRepository.findByCardAcctId(acctId);
    }

    /**
     * View card details - equivalent to COCRDSLC (Card View).
     */
    public Optional<Card> viewCard(String cardNum) {
        return cardRepository.findById(cardNum);
    }

    /**
     * Update card details - equivalent to COCRDUPC (Card Update).
     */
    @Transactional
    public Optional<Card> updateCard(String cardNum, Card updatedData) {
        return cardRepository.findById(cardNum).map(existing -> {
            if (updatedData.getEmbossedName() != null) {
                existing.setEmbossedName(updatedData.getEmbossedName());
            }
            if (updatedData.getExpirationDate() != null) {
                existing.setExpirationDate(updatedData.getExpirationDate());
            }
            if (updatedData.getActiveStatus() != null) {
                existing.setActiveStatus(updatedData.getActiveStatus());
            }
            return cardRepository.save(existing);
        });
    }

    /**
     * Get cross-reference data for a card.
     */
    public Optional<CardXref> getCardXref(String cardNum) {
        return cardXrefRepository.findByCardNum(cardNum);
    }

    /**
     * List all cross-references for an account.
     */
    public List<CardXref> listXrefsByAccount(Long acctId) {
        return cardXrefRepository.findByAcctId(acctId);
    }
}
