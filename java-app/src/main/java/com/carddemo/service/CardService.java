package com.carddemo.service;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.model.CardData;
import com.carddemo.model.CardXref;
import com.carddemo.repository.CardDataRepository;
import com.carddemo.repository.CardXrefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardDataRepository cardDataRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardDataRepository cardDataRepository,
                       CardXrefRepository cardXrefRepository) {
        this.cardDataRepository = cardDataRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    public List<CardData> getAllCards() {
        return cardDataRepository.findAllByOrderByCardNumAsc();
    }

    public CardData getCard(String cardNum) {
        return cardDataRepository.findById(cardNum)
                .orElseThrow(() -> new CardNotFoundException(cardNum));
    }

    public Optional<CardData> findByCardNum(String cardNum) {
        return cardDataRepository.findById(cardNum);
    }

    public List<CardData> findByAcctId(Long acctId) {
        return cardDataRepository.findByAcctId(acctId);
    }

    public Optional<CardXref> findXrefByCardNum(String cardNum) {
        return cardXrefRepository.findById(cardNum);
    }

    public List<CardXref> findXrefsByAcctId(Long acctId) {
        return cardXrefRepository.findByAcctId(acctId);
    }

    @Transactional
    public CardData updateCard(CardUpdateRequest request) {
        CardData card = getCard(request.getCardNum());

        if (request.getEmbossedName() != null) {
            card.setEmbossedName(request.getEmbossedName());
        }
        if (request.getExpirationDate() != null) {
            card.setExpirationDate(request.getExpirationDate());
        }
        if (request.getActiveStatus() != null) {
            card.setActiveStatus(request.getActiveStatus());
        }

        return cardDataRepository.save(card);
    }
}
