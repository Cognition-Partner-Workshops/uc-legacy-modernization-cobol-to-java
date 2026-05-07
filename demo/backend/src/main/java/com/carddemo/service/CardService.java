package com.carddemo.service;

import com.carddemo.model.Card;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;

    public CardService(CardRepository cardRepository, AccountRepository accountRepository) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
    }

    // Equivalent to 1200-SCREEN-ARRAY-INIT + 9000-READ-FORWARD in COCRDLIC.cbl
    // COCRDLIC uses a 7-row page display; we support pagination via Pageable.
    public Page<Card> listCardsPaged(String accountId, Pageable pageable) {
        // Equivalent to 2200-EDIT-MAP-INPUTS in COCRDSLC.cbl:
        // Both account and card filters blank → return all cards (valid scenario)
        if (accountId != null && !accountId.isBlank()) {
            validateAccountId(accountId);
            return cardRepository.findByAccountId(accountId, pageable);
        }
        return cardRepository.findAll(pageable);
    }

    // Non-paged version for backward compatibility
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

    // Equivalent to 9100-GETCARD-BYACCTCARD in COCRDSLC.cbl
    // Composite key lookup: verify that the card belongs to the stated account.
    // In COBOL, RIDFLD is built from ACCT-ID + CARD-NUM for VSAM KSDS read.
    public Optional<Card> getCardByAccountAndCard(String accountId, String cardNumber) {
        validateAccountId(accountId);
        validateCardNumber(cardNumber);
        Optional<Card> card = cardRepository.findById(cardNumber);
        if (card.isPresent() && !card.get().getAccountId().equals(accountId)) {
            throw new IllegalArgumentException(
                    "Card " + cardNumber + " does not belong to account " + accountId);
        }
        return card;
    }

    // Equivalent to 2000-DECIDE-ACTION + 9100-UPDATE-CARD + 9200-WRITE-PROCESSING in COCRDUPC.cbl
    // @Transactional: Equivalent to EXEC CICS READ UPDATE (record locking) in 9200-WRITE-PROCESSING.
    // JPA @Version on Card entity handles 9300-CHECK-CHANGE-IN-REC (optimistic concurrency).
    @Transactional
    public Card updateCard(String cardNumber, Card updated, boolean confirmed) {
        validateCardNumber(cardNumber);

        // Equivalent to 2000-DECIDE-ACTION PF5 check in COCRDUPC.cbl
        // COBOL requires PF5 to confirm the update; we require a confirmation flag.
        if (!confirmed) {
            throw new IllegalStateException(
                    "Update must be confirmed. Set confirmed=true to proceed.");
        }

        Card existing = cardRepository.findById(cardNumber)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardNumber));

        // Equivalent to D-07: Verify account exists before card operations.
        // In COBOL, cross-file validation ensures the account referenced by
        // the card exists in the accounts VSAM file.
        if (existing.getAccountId() != null) {
            if (!accountRepository.existsById(existing.getAccountId())) {
                throw new IllegalArgumentException(
                        "Account " + existing.getAccountId() + " does not exist");
            }
        }

        // Equivalent to 1230-EDIT-NAME in COCRDUPC.cbl
        // COBOL uses INSPECT CONVERTING to enforce alphabetic + space only.
        if (updated.getEmbossedName() != null && !updated.getEmbossedName().isBlank()) {
            validateEmbossedName(updated.getEmbossedName());
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

        // JPA @Version handles optimistic locking automatically on save().
        // If another transaction modified the record, OptimisticLockException is thrown,
        // equivalent to 9300-CHECK-CHANGE-IN-REC detecting field-level changes.
        return cardRepository.save(existing);
    }

    // Backward-compatible overload (defaults confirmed=true for direct API calls)
    @Transactional
    public Card updateCard(String cardNumber, Card updated) {
        return updateCard(cardNumber, updated, true);
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

    // Equivalent to 1230-EDIT-NAME in COCRDUPC.cbl
    // COBOL paragraph uses INSPECT CONVERTING to check name contains only
    // alphabetic characters and spaces. Rejects numeric/special characters.
    private void validateEmbossedName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Name on card is required");
        }
        if (!name.trim().matches("^[A-Za-z ]+$")) {
            throw new IllegalArgumentException(
                    "Name on card must contain only alphabetic characters and spaces");
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
    // Also enforces V-09: EXPDAY field in COCRDUP.bms is DRK,PROT with value "01".
    // Day component must always be "01" (first of month convention from COBOL).
    private void validateExpirationDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException(
                    "Expiration date must be in YYYY-MM-DD format");
        }
        String[] parts = date.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);
        int day = Integer.parseInt(parts[2]);
        if (year < 1950 || year > 2099) {
            throw new IllegalArgumentException(
                    "Year must be between 1950 and 2099");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 01 and 12");
        }
        // Equivalent to EXPDAY field (DRK,PROT, value "01") in COCRDUP.bms
        // COBOL screen has day as a dark protected field always set to "01".
        if (day != 1) {
            throw new IllegalArgumentException(
                    "Day must be 01 (first of month, per COBOL EXPDAY convention)");
        }
    }
}
