package com.carddemo.service;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Card;
import com.carddemo.entity.CardXref;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private CardService service;

    private Card card;
    private CardXref xref;

    @BeforeEach
    void setUp() {
        card = new Card();
        card.setCardNum("4000123456789012");
        card.setAcctId("00000000001");
        card.setCvvCode(123);
        card.setEmbossedName("John Doe");
        card.setExpirationDate(LocalDate.of(2025, 12, 31));
        card.setActiveStatus("Y");

        xref = new CardXref();
        xref.setCardNum("4000123456789012");
        xref.setCustId(1L);
        xref.setAcctId("00000000001");
    }

    @Test
    void getCard_found() {
        when(cardRepository.findById("4000123456789012")).thenReturn(Optional.of(card));

        Card result = service.getCard("4000123456789012");
        assertEquals("4000123456789012", result.getCardNum());
        assertEquals("John Doe", result.getEmbossedName());
    }

    @Test
    void getCard_notFound_throwsException() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());
        assertThrows(CardNotFoundException.class, () -> service.getCard("9999999999999999"));
    }

    @Test
    void getCardsByAccount() {
        when(cardRepository.findByAcctId("00000000001")).thenReturn(List.of(card));

        List<Card> result = service.getCardsByAccount("00000000001");
        assertEquals(1, result.size());
        assertEquals("4000123456789012", result.get(0).getCardNum());
    }

    @Test
    void updateCard_updatesFields() {
        when(cardRepository.findById("4000123456789012")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest();
        request.setEmbossedName("Jane Doe");
        request.setActiveStatus("N");

        Card result = service.updateCard("4000123456789012", request);

        assertEquals("Jane Doe", result.getEmbossedName());
        assertEquals("N", result.getActiveStatus());
    }

    @Test
    void updateCard_partialUpdate() {
        when(cardRepository.findById("4000123456789012")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CardUpdateRequest request = new CardUpdateRequest();
        request.setActiveStatus("N");

        Card result = service.updateCard("4000123456789012", request);

        assertEquals("John Doe", result.getEmbossedName());
        assertEquals("N", result.getActiveStatus());
    }
}
