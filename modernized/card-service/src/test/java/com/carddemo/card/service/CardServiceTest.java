package com.carddemo.card.service;

import com.carddemo.card.dto.CardXrefDto;
import com.carddemo.card.dto.UpdateCardRequest;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, cardXrefRepository);
    }

    @Test
    void listCardsByAccount_returnsCorrectCards() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .acctId("00000000001")
                .activeStatus('Y')
                .build();
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findByAcctId("00000000001", PageRequest.of(0, 7))).thenReturn(page);

        var result = cardService.listCardsByAccount("00000000001", PageRequest.of(0, 7));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCardNum()).isEqualTo("4111111111111111");
    }

    @Test
    void getCardDetail_validCard_returnsDto() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .acctId("00000000001")
                .cvvCd(123)
                .embossedName("JOHN DOE")
                .expirationDate("2025-12-01")
                .activeStatus('Y')
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        var result = cardService.getCardDetail("4111111111111111");

        assertThat(result.getCardNum()).isEqualTo("4111111111111111");
        assertThat(result.getEmbossedName()).isEqualTo("JOHN DOE");
    }

    @Test
    void getCardDetail_invalidCard_throws() {
        when(cardRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardDetail("9999999999999999"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCard_validData_succeeds() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .acctId("00000000001")
                .embossedName("JOHN DOE")
                .activeStatus('Y')
                .expirationDate("2025-12-01")
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UpdateCardRequest request = UpdateCardRequest.builder()
                .embossedName("JANE DOE")
                .activeStatus('N')
                .expirationMonth(6)
                .expirationYear(2026)
                .version(0L)
                .build();
        var result = cardService.updateCard("4111111111111111", request);

        assertThat(result.getEmbossedName()).isEqualTo("JANE DOE");
        assertThat(result.getActiveStatus()).isEqualTo('N');
        assertThat(result.getExpirationDate()).isEqualTo("2026-06-01");
    }

    @Test
    void updateCard_nonAlphaName_throws() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        UpdateCardRequest request = UpdateCardRequest.builder()
                .embossedName("JOHN123")
                .version(0L)
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("alphabetic");
    }

    @Test
    void updateCard_invalidMonth_throws() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .expirationDate("2025-12-01")
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        UpdateCardRequest request = UpdateCardRequest.builder()
                .expirationMonth(13)
                .version(0L)
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationMonth");
    }

    @Test
    void updateCard_invalidYear_throws() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .expirationDate("2025-12-01")
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        UpdateCardRequest request = UpdateCardRequest.builder()
                .expirationYear(2100)
                .version(0L)
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationYear");
    }

    @Test
    void updateCard_invalidStatus_throws() {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .version(0L)
                .build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        UpdateCardRequest request = UpdateCardRequest.builder()
                .activeStatus('X')
                .version(0L)
                .build();

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("activeStatus");
    }

    @Test
    void resolveXref_returnsCorrectIds() {
        CardXref xref = CardXref.builder()
                .cardNum("4111111111111111")
                .custId("000000001")
                .acctId("00000000001")
                .build();
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));

        CardXrefDto result = cardService.resolveXref("4111111111111111");

        assertThat(result.getCustId()).isEqualTo("000000001");
        assertThat(result.getAcctId()).isEqualTo("00000000001");
    }
}
