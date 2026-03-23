package com.carddemo.integration;

import com.carddemo.dto.AccountUpdateRequest;
import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.Card;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.service.AccountService;
import com.carddemo.service.CardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for AccountService and CardService.
 * Verifies COBOL COACTVWC/COACTUPC (account) and COCRDLIC/COCRDSLC/COCRDUPC (card) logic.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AccountCardCrudIntegrationTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CardService cardService;

    // =========================================================================
    // Account View (COACTVWC)
    // =========================================================================
    @Test
    @DisplayName("View account by ID returns all fields (COACTVWC)")
    void shouldViewAccountById() {
        Account account = accountService.getAccount("00000000001");

        assertNotNull(account);
        assertEquals("00000000001", account.getAcctId());
        assertNotNull(account.getActiveStatus());
        assertNotNull(account.getCurrentBalance());
        assertNotNull(account.getCreditLimit());
    }

    @Test
    @DisplayName("View nonexistent account throws AccountNotFoundException")
    void shouldThrow_whenAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount("99999999999"));
    }

    // =========================================================================
    // Account Update (COACTUPC)
    // =========================================================================
    @Test
    @DisplayName("Update account credit limit (COACTUPC)")
    void shouldUpdateAccountCreditLimit() {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setCreditLimit(new BigDecimal("25000.00"));

        Account updated = accountService.updateAccount("00000000001", request);

        assertEquals(0, new BigDecimal("25000.00").compareTo(updated.getCreditLimit()));
    }

    @Test
    @DisplayName("Update account active status (COACTUPC)")
    void shouldUpdateAccountActiveStatus() {
        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setActiveStatus("N");

        Account updated = accountService.updateAccount("00000000001", request);
        assertEquals("N", updated.getActiveStatus());

        // Verify persisted
        Account reread = accountService.getAccount("00000000001");
        assertEquals("N", reread.getActiveStatus());
    }

    @Test
    @DisplayName("Partial update preserves unchanged fields (COACTUPC)")
    void shouldPreserveUnchangedFields_onPartialUpdate() {
        Account original = accountService.getAccount("00000000001");
        BigDecimal originalBalance = original.getCurrentBalance();

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setZipCode("99999");

        Account updated = accountService.updateAccount("00000000001", request);

        assertEquals("99999", updated.getZipCode());
        assertEquals(0, originalBalance.compareTo(updated.getCurrentBalance()),
                "Balance should not change on partial update");
    }

    // =========================================================================
    // Card List (COCRDLIC)
    // =========================================================================
    @Test
    @DisplayName("List cards by account ID (COCRDLIC)")
    void shouldListCardsByAccount() {
        List<Card> cards = cardService.getCardsByAccount("00000000001");

        assertNotNull(cards);
        assertFalse(cards.isEmpty(), "Should have cards for account from data.sql");
        cards.forEach(card -> assertEquals("00000000001", card.getAcctId()));
    }

    // =========================================================================
    // Card Detail (COCRDSLC)
    // =========================================================================
    @Test
    @DisplayName("View card detail by card number (COCRDSLC)")
    void shouldViewCardDetail() {
        List<Card> cards = cardService.getCardsByAccount("00000000001");
        assertFalse(cards.isEmpty());

        Card card = cardService.getCard(cards.get(0).getCardNum());

        assertNotNull(card);
        assertNotNull(card.getCardNum());
        assertNotNull(card.getAcctId());
        assertNotNull(card.getActiveStatus());
    }

    @Test
    @DisplayName("View nonexistent card throws CardNotFoundException")
    void shouldThrow_whenCardNotFound() {
        assertThrows(CardNotFoundException.class,
                () -> cardService.getCard("0000000000000000"));
    }

    // =========================================================================
    // Card Update (COCRDUPC)
    // =========================================================================
    @Test
    @DisplayName("Update card embossed name (COCRDUPC)")
    void shouldUpdateCardEmbossedName() {
        List<Card> cards = cardService.getCardsByAccount("00000000001");
        assertFalse(cards.isEmpty());
        String cardNum = cards.get(0).getCardNum();

        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("NEW CARDHOLDER NAME");

        Card updated = cardService.updateCard(cardNum, request);

        assertEquals("NEW CARDHOLDER NAME", updated.getEmbossedName());
    }

    @Test
    @DisplayName("Deactivate card (COCRDUPC)")
    void shouldDeactivateCard() {
        List<Card> cards = cardService.getCardsByAccount("00000000001");
        assertFalse(cards.isEmpty());
        String cardNum = cards.get(0).getCardNum();

        CardUpdateRequest request = new CardUpdateRequest();
        request.setActiveStatus("N");

        Card updated = cardService.updateCard(cardNum, request);
        assertEquals("N", updated.getActiveStatus());

        // Verify persisted
        Card reread = cardService.getCard(cardNum);
        assertEquals("N", reread.getActiveStatus());
    }
}
