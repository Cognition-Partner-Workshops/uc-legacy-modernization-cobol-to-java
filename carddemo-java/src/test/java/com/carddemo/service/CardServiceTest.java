package com.carddemo.service;

import com.carddemo.entity.CardXref;
import com.carddemo.entity.CreditCard;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CreditCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardService cardService;

    private CreditCard testCard;

    @BeforeEach
    void setUp() {
        testCard = new CreditCard();
        testCard.setCardNum("4000123456789010");
        testCard.setAcctId(1L);
        testCard.setCvvCode(123);
        testCard.setEmbossedName("John Doe");
        testCard.setExpirationDate("2025-12-31");
        testCard.setActiveStatus("Y");
    }

    @Test
    void listCards_forAccount_returnsList() {
        when(creditCardRepository.findByAcctId(1L)).thenReturn(List.of(testCard));

        List<CreditCard> result = cardService.listCards(1L);

        assertEquals(1, result.size());
        assertEquals("4000123456789010", result.get(0).getCardNum());
    }

    @Test
    void searchCards_byCardNumber_returnsMatchingCard() {
        when(creditCardRepository.findById("4000123456789010")).thenReturn(Optional.of(testCard));

        List<CreditCard> result = cardService.searchCards("4000123456789010", null);

        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getEmbossedName());
    }

    @Test
    void searchCards_byAccountId_returnsCards() {
        when(creditCardRepository.findByAcctId(1L)).thenReturn(List.of(testCard));

        List<CreditCard> result = cardService.searchCards(null, 1L);

        assertEquals(1, result.size());
    }

    @Test
    void searchCards_noParams_returnsEmptyList() {
        List<CreditCard> result = cardService.searchCards(null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCardDetail_existingCard_returnsCard() {
        when(creditCardRepository.findById("4000123456789010")).thenReturn(Optional.of(testCard));

        CreditCard result = cardService.getCardDetail("4000123456789010");

        assertNotNull(result);
        assertEquals(1L, result.getAcctId());
        assertEquals(123, result.getCvvCode());
    }

    @Test
    void getCardDetail_nonExistentCard_throwsException() {
        when(creditCardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cardService.getCardDetail("9999999999999999"));
    }

    @Test
    void updateCard_updateExpiry_saves() {
        when(creditCardRepository.findById("4000123456789010")).thenReturn(Optional.of(testCard));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(testCard);

        CreditCard updates = new CreditCard();
        updates.setExpirationDate("2027-06-30");

        CreditCard result = cardService.updateCard("4000123456789010", updates);

        assertEquals("2027-06-30", testCard.getExpirationDate());
        verify(creditCardRepository).save(testCard);
    }

    @Test
    void updateCard_updateStatus_saves() {
        when(creditCardRepository.findById("4000123456789010")).thenReturn(Optional.of(testCard));
        when(creditCardRepository.save(any(CreditCard.class))).thenReturn(testCard);

        CreditCard updates = new CreditCard();
        updates.setActiveStatus("N");

        cardService.updateCard("4000123456789010", updates);

        assertEquals("N", testCard.getActiveStatus());
    }

    @Test
    void getCardXref_existingXref_returnsXref() {
        CardXref xref = new CardXref();
        xref.setCardNum("4000123456789010");
        xref.setCustId(1L);
        xref.setAcctId(1L);
        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(xref));

        CardXref result = cardService.getCardXref("4000123456789010");

        assertNotNull(result);
        assertEquals(1L, result.getCustId());
    }
}
