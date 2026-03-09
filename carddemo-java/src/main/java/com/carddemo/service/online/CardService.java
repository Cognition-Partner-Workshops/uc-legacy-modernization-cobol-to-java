package com.carddemo.service.online;

import com.carddemo.dto.CardDto;
import com.carddemo.exception.ResourceNotFoundException;
import com.carddemo.model.Card;
import com.carddemo.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Card service replacing COBOL programs COCRDLIC, COCRDSLC, and COCRDUPC.
 *
 * <p>COCRDLIC (Card List, CICS txn CCLI):
 * - Browses CARDDAT by account ID (EXEC CICS STARTBR/READNEXT)
 * - Displays paginated list of cards
 *
 * <p>COCRDSLC (Card Detail, CICS txn CCDL):
 * - Reads CARDDAT by card number (EXEC CICS READ RIDFLD)
 *
 * <p>COCRDUPC (Card Update, CICS txn CCUP):
 * - Reads CARDDAT for update, rewrites after validation
 */
@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    /**
     * List cards by account - replaces COCRDLIC STARTBR/READNEXT loop.
     */
    @Transactional(readOnly = true)
    public List<CardDto> getCardsByAccount(Long accountId) {
        return cardRepository.findByCardAcctId(accountId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get card detail - replaces COCRDSLC READ paragraph.
     */
    @Transactional(readOnly = true)
    public CardDto getCard(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardNum));
        return mapToDto(card);
    }

    /**
     * Update card - replaces COCRDUPC PROCESS-ENTER-KEY paragraph.
     */
    @Transactional
    public CardDto updateCard(String cardNum, CardDto request) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", cardNum));

        if (request.getCardActiveStatus() != null) {
            card.setCardActiveStatus(request.getCardActiveStatus());
        }
        if (request.getCardEmbossedName() != null) {
            card.setCardEmbossedName(request.getCardEmbossedName());
        }
        if (request.getCardExpirationDate() != null) {
            card.setCardExpirationDate(request.getCardExpirationDate());
        }

        Card saved = cardRepository.save(card);
        return mapToDto(saved);
    }

    private CardDto mapToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setCardNum(card.getCardNum());
        dto.setCardAcctId(card.getCardAcctId());
        dto.setCardCvvCd(card.getCardCvvCd());
        dto.setCardEmbossedName(card.getCardEmbossedName());
        dto.setCardExpirationDate(card.getCardExpirationDate());
        dto.setCardActiveStatus(card.getCardActiveStatus());
        return dto;
    }
}
