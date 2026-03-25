package com.carddemo.service;

import com.carddemo.dto.CardUpdateDTO;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Customer;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.util.DateUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Card service - consolidates COCRDLIC (list), COCRDSLC (view), COCRDUPC (update).
 * All three COBOL programs share identical card lookup logic via CARDDAT and CARDAIX.
 * This service provides shared methods eliminating that duplication.
 */
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public CardService(CardRepository cardRepository,
                       CardXrefRepository cardXrefRepository,
                       CustomerRepository customerRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * List cards by account - replaces COCRDLIC's STARTBR on CARDAIX alternate index.
     */
    public Page<Card> listCardsByAccount(Long accountId, Pageable pageable) {
        return cardRepository.findByAccountId(accountId, pageable);
    }

    /**
     * Get card detail with customer info - replaces COCRDSLC's multi-file reads.
     */
    public CardDetailWithCustomer getCardDetail(String cardNumber) {
        Card card = findCardOrThrow(cardNumber);

        Customer customer = null;
        CardXref xref = cardXrefRepository.findByCardNumber(cardNumber).orElse(null);
        if (xref != null) {
            customer = customerRepository.findById(xref.getCustomerId()).orElse(null);
        }

        return new CardDetailWithCustomer(card, customer);
    }

    /**
     * Update a card - replaces COCRDUPC's READ/REWRITE with validation.
     */
    @Transactional
    public Card updateCard(String cardNumber, CardUpdateDTO dto) {
        Card card = findCardOrThrow(cardNumber);

        if (dto.getEmbossedName() != null) card.setEmbossedName(dto.getEmbossedName());
        if (dto.getActiveStatus() != null) card.setActiveStatus(dto.getActiveStatus());
        if (dto.getExpirationDate() != null) card.setExpirationDate(DateUtils.parseDate(dto.getExpirationDate()));

        return cardRepository.save(card);
    }

    private Card findCardOrThrow(String cardNumber) {
        return cardRepository.findById(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardNumber));
    }

    public record CardDetailWithCustomer(Card card, Customer customer) {}
}
