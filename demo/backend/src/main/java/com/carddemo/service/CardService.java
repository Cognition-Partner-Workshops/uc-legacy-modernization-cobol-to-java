package com.carddemo.service;

import com.carddemo.model.Card;
import com.carddemo.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    // Equivalent to 1200-SCREEN-ARRAY-INIT + 9000-READ-FORWARD in COCRDLIC.cbl
    public List<Card> listCards(String accountId) {
        if (accountId != null && !accountId.isBlank()) {
            validateAccountId(accountId);
            return cardRepository.findByAccountId(accountId);
        }
        return cardRepository.findAll();
    }

    // Equivalent to 9000-READ-DATA + 9100-GETCARD-BYACCTCARD in COCRDSLC.cbl
    public Optional<Card> getCard(String cardNumber) {
        validateCardNumber(cardNumber);
        return cardRepository.findById(cardNumber);
    }

    // Equivalent to 2000-DECIDE-ACTION + 9100-UPDATE-CARD in COCRDUPC.cbl
    public Card updateCard(String cardNumber, Card updated) {
        validateCardNumber(cardNumber);

        Card existing = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNumber));

        // Equivalent to 1230-EDIT-NAME in COCRDUPC.cbl
        if (updated.getEmbossedName() != null && !updated.getEmbossedName().isBlank()) {
            existing.setEmbossedName(updated.getEmbossedName().trim());
        }

        // Equivalent to 1240-EDIT-CARDSTATUS in COCRDUPC.cbl
        // Active status must be Y or N (FLG-YES-NO-VALID VALUES 'Y', 'N')
        if (updated.getActiveStatus() != null) {
            validateActiveStatus(updated.getActiveStatus());
            existing.setActiveStatus(updated.getActiveStatus());
        }

        // Equivalent to 1250-EDIT-EXPIRY-MON + 1260-EDIT-EXPIRY-YEAR in COCRDUPC.cbl
        if (updated.getExpirationDate() != null) {
            validateExpirationDate(updated.getExpirationDate());
            existing.setExpirationDate(updated.getExpirationDate());
        }

        return cardRepository.save(existing);
    }

    // Equivalent to 2210-EDIT-ACCOUNT in COCRDSLC.cbl / 1210-EDIT-ACCOUNT in COCRDUPC.cbl
    // Account ID must be numeric, 11 digits (PIC 9(11))
    private void validateAccountId(String accountId) {
        if (accountId == null || !accountId.matches("\\d{1,11}")) {
            throw new IllegalArgumentException(
                    "Account ID must be numeric, up to 11 digits");
        }
    }

    // Equivalent to 2220-EDIT-CARD in COCRDSLC.cbl / 1220-EDIT-CARD in COCRDUPC.cbl
    // Card number must be numeric, 16 digits (PIC X(16))
    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException(
                    "Card number must be exactly 16 digits");
        }
    }

    // Equivalent to 1240-EDIT-CARDSTATUS in COCRDUPC.cbl
    // FLG-YES-NO-VALID VALUES 'Y', 'N'
    private void validateActiveStatus(String status) {
        if (!"Y".equals(status) && !"N".equals(status)) {
            throw new IllegalArgumentException(
                    "Active status must be 'Y' or 'N'");
        }
    }

    // Equivalent to 1250-EDIT-EXPIRY-MON + 1260-EDIT-EXPIRY-YEAR in COCRDUPC.cbl
    // VALID-MONTH VALUES 1 THRU 12, VALID-YEAR VALUES 1950 THRU 2099
    private void validateExpirationDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException(
                    "Expiration date must be in YYYY-MM-DD format");
        }
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        if (year < 1950 || year > 2099) {
            throw new IllegalArgumentException(
                    "Year must be between 1950 and 2099");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 01 and 12");
        }
    }
}
