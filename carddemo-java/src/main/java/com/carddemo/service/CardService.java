package com.carddemo.service;

import com.carddemo.entity.CardXref;
import com.carddemo.entity.CreditCard;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CreditCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CardService {

    private final CreditCardRepository creditCardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CreditCardRepository creditCardRepository,
                       CardXrefRepository cardXrefRepository) {
        this.creditCardRepository = creditCardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * List cards for an account - mirrors COCRDLIC.cbl
     */
    public List<CreditCard> listCards(Long acctId) {
        return creditCardRepository.findByAcctId(acctId);
    }

    public Page<CreditCard> listCards(Long acctId, int page, int size) {
        List<CreditCard> cards = creditCardRepository.findByAcctId(acctId);
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), cards.size());
        return new org.springframework.data.domain.PageImpl<>(
                cards.subList(start, end), pageable, cards.size());
    }

    /**
     * Search cards by card number or account ID - mirrors COCRDSLC.cbl
     */
    public List<CreditCard> searchCards(String cardNum, Long acctId) {
        if (cardNum != null && !cardNum.trim().isEmpty()) {
            return creditCardRepository.findById(cardNum.trim())
                    .map(List::of)
                    .orElse(List.of());
        }
        if (acctId != null) {
            return creditCardRepository.findByAcctId(acctId);
        }
        return List.of();
    }

    /**
     * Get card detail - mirrors COCRDSLC.cbl detail view
     */
    public CreditCard getCardDetail(String cardNum) {
        return creditCardRepository.findById(cardNum)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNum));
    }

    /**
     * Update card - mirrors COCRDUPC.cbl
     */
    @Transactional
    public CreditCard updateCard(String cardNum, CreditCard updatedFields) {
        CreditCard existing = creditCardRepository.findById(cardNum)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNum));

        if (updatedFields.getActiveStatus() != null) {
            existing.setActiveStatus(updatedFields.getActiveStatus());
        }
        if (updatedFields.getExpirationDate() != null) {
            existing.setExpirationDate(updatedFields.getExpirationDate());
        }
        if (updatedFields.getEmbossedName() != null) {
            existing.setEmbossedName(updatedFields.getEmbossedName());
        }

        return creditCardRepository.save(existing);
    }

    /**
     * Look up cross-reference for a card
     */
    public CardXref getCardXref(String cardNum) {
        return cardXrefRepository.findById(cardNum)
                .orElseThrow(() -> new IllegalArgumentException("Card cross-reference not found: " + cardNum));
    }
}
