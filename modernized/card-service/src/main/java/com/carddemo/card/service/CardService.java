package com.carddemo.card.service;

import com.carddemo.card.dto.*;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import com.carddemo.common.dto.PageResponse;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardService(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    public PageResponse<CardListItemDto> listCardsByAccount(String acctId, Pageable pageable) {
        Page<Card> page = cardRepository.findByAcctId(acctId, pageable);
        List<CardListItemDto> content = page.getContent().stream()
                .map(c -> CardListItemDto.builder()
                        .acctId(c.getAcctId())
                        .cardNum(c.getCardNum())
                        .activeStatus(c.getActiveStatus())
                        .build())
                .collect(Collectors.toList());
        return new PageResponse<>(content, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public CardDto getCardDetail(String cardNum) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNum", cardNum));
        return toDto(card);
    }

    @Transactional
    public CardDto updateCard(String cardNum, UpdateCardRequest request) {
        Card card = cardRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "cardNum", cardNum));

        if (request.getVersion() != null && !request.getVersion().equals(card.getVersion())) {
            throw new ValidationException("Card " + cardNum + " was modified by another transaction");
        }

        if (request.getEmbossedName() != null) {
            validateEmbossedName(request.getEmbossedName());
            card.setEmbossedName(request.getEmbossedName());
        }
        if (request.getActiveStatus() != null) {
            validateActiveStatus(request.getActiveStatus());
            card.setActiveStatus(request.getActiveStatus());
        }
        if (request.getExpirationMonth() != null || request.getExpirationYear() != null) {
            String currentExpDate = card.getExpirationDate() != null ? card.getExpirationDate() : "2000-01-01";
            String[] parts = currentExpDate.split("-");
            int year = request.getExpirationYear() != null ? request.getExpirationYear() : Integer.parseInt(parts[0]);
            int month = request.getExpirationMonth() != null ? request.getExpirationMonth() : Integer.parseInt(parts[1]);
            int day = parts.length > 2 ? Integer.parseInt(parts[2]) : 1;

            validateExpirationMonth(month);
            validateExpirationYear(year);

            card.setExpirationDate(String.format("%04d-%02d-%02d", year, month, day));
        }

        try {
            card = cardRepository.save(card);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ValidationException("Card " + cardNum + " was modified by another transaction");
        }
        return toDto(card);
    }

    public CardXrefDto resolveXref(String cardNum) {
        CardXref xref = cardXrefRepository.findById(cardNum)
                .orElseThrow(() -> new ResourceNotFoundException("CardXref", "cardNum", cardNum));
        return CardXrefDto.builder()
                .cardNum(xref.getCardNum())
                .custId(xref.getCustId())
                .acctId(xref.getAcctId())
                .build();
    }

    public Optional<CardXrefDto> resolveXrefByAccount(String acctId) {
        return cardXrefRepository.findByAcctId(acctId)
                .map(xref -> CardXrefDto.builder()
                        .cardNum(xref.getCardNum())
                        .custId(xref.getCustId())
                        .acctId(xref.getAcctId())
                        .build());
    }

    private void validateEmbossedName(String name) {
        if (!name.matches("[a-zA-Z ]+")) {
            throw new ValidationException("embossedName must contain only alphabetic characters and spaces");
        }
    }

    private void validateActiveStatus(Character status) {
        if (status != 'Y' && status != 'N') {
            throw new ValidationException("activeStatus must be 'Y' or 'N'");
        }
    }

    private void validateExpirationMonth(int month) {
        if (month < 1 || month > 12) {
            throw new ValidationException("expirationMonth must be between 1 and 12");
        }
    }

    private void validateExpirationYear(int year) {
        if (year < 1950 || year > 2099) {
            throw new ValidationException("expirationYear must be between 1950 and 2099");
        }
    }

    private CardDto toDto(Card c) {
        return CardDto.builder()
                .cardNum(c.getCardNum())
                .acctId(c.getAcctId())
                .cvvCd(c.getCvvCd())
                .embossedName(c.getEmbossedName())
                .expirationDate(c.getExpirationDate())
                .activeStatus(c.getActiveStatus())
                .version(c.getVersion())
                .build();
    }
}
