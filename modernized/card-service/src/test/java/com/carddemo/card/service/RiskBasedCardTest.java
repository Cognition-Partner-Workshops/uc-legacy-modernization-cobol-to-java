package com.carddemo.card.service;

import com.carddemo.card.dto.UpdateCardRequest;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.common.exception.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TIER 2 — HIGH RISK: Card CRUD & Validation Rules
 * Risk factors: card data corruption, validation bypass, XREF inconsistency, stale updates
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-2")
@DisplayName("Tier 2 (High): Card CRUD & Validation Rules")
class RiskBasedCardTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository, cardXrefRepository);
    }

    // --- Optimistic Locking ---

    @Test
    @DisplayName("T2-CARD-001: Update with matching version succeeds")
    void updateCard_matchingVersion_succeeds() {
        Card card = Card.builder()
                .cardNum("4111111111111111").acctId("00000000001")
                .embossedName("JOHN DOE").activeStatus('Y')
                .expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().embossedName("JANE DOE").version(0L).build());
        assertThat(result.getEmbossedName()).isEqualTo("JANE DOE");
    }

    @Test
    @DisplayName("T2-CARD-002: Update with stale version is rejected")
    void updateCard_staleVersion_throws() {
        Card card = Card.builder()
                .cardNum("4111111111111111").version(3L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().embossedName("NEW NAME").version(1L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("modified");
    }

    // --- Embossed Name Validation (COBOL: COCRDUPC lines 822-837) ---

    @Test
    @DisplayName("T2-CARD-003: Alphabetic name with spaces is accepted")
    void updateCard_alphabeticName_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().embossedName("JOHN ALEXANDER DOE").version(0L).build());
        assertThat(result.getEmbossedName()).isEqualTo("JOHN ALEXANDER DOE");
    }

    @Test
    @DisplayName("T2-CARD-004: Name with numbers is rejected")
    void updateCard_nameWithNumbers_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().embossedName("JOHN123").version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("alphabetic");
    }

    @Test
    @DisplayName("T2-CARD-005: Name with special characters is rejected")
    void updateCard_nameWithSpecialChars_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().embossedName("JOHN@DOE").version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("alphabetic");
    }

    // --- Active Status Validation ---

    @Test
    @DisplayName("T2-CARD-006: Status 'Y' is accepted")
    void updateCard_statusY_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").activeStatus('N').version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().activeStatus('Y').version(0L).build());
        assertThat(result.getActiveStatus()).isEqualTo('Y');
    }

    @Test
    @DisplayName("T2-CARD-007: Status 'X' (invalid) is rejected")
    void updateCard_statusInvalid_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().activeStatus('X').version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("activeStatus");
    }

    // --- Expiration Date Validation (COBOL: month 1-12, year 1950-2099) ---

    @Test
    @DisplayName("T2-CARD-008: Valid month (1) is accepted")
    void updateCard_month1_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationMonth(1).version(0L).build());
        assertThat(result.getExpirationDate()).contains("-01-");
    }

    @Test
    @DisplayName("T2-CARD-009: Valid month (12) is accepted")
    void updateCard_month12_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-06-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationMonth(12).version(0L).build());
        assertThat(result.getExpirationDate()).startsWith("2025-12-");
    }

    @Test
    @DisplayName("T2-CARD-010: Month 0 (below range) is rejected")
    void updateCard_month0_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationMonth(0).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationMonth");
    }

    @Test
    @DisplayName("T2-CARD-011: Month 13 (above range) is rejected")
    void updateCard_month13_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationMonth(13).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationMonth");
    }

    @Test
    @DisplayName("T2-CARD-012: Year 1949 (below range) is rejected")
    void updateCard_year1949_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationYear(1949).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationYear");
    }

    @Test
    @DisplayName("T2-CARD-013: Year 2100 (above range) is rejected")
    void updateCard_year2100_rejected() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationYear(2100).version(0L).build()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("expirationYear");
    }

    @Test
    @DisplayName("T2-CARD-014: Year boundary 1950 is accepted")
    void updateCard_year1950_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationYear(1950).version(0L).build());
        assertThat(result.getExpirationDate()).startsWith("1950-");
    }

    @Test
    @DisplayName("T2-CARD-015: Year boundary 2099 is accepted")
    void updateCard_year2099_accepted() {
        Card card = Card.builder()
                .cardNum("4111111111111111").expirationDate("2025-12-01").version(0L).build();
        when(cardRepository.findById("4111111111111111")).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var result = cardService.updateCard("4111111111111111",
                UpdateCardRequest.builder().expirationYear(2099).version(0L).build());
        assertThat(result.getExpirationDate()).startsWith("2099-");
    }

    // --- Card List ---

    @Test
    @DisplayName("T2-CARD-016: List cards by account returns correct cards")
    void listCards_byAccount_returnsCards() {
        Card c1 = Card.builder().cardNum("4111111111111111").acctId("00000000001").activeStatus('Y').build();
        Card c2 = Card.builder().cardNum("4999999999999999").acctId("00000000001").activeStatus('Y').build();
        Page<Card> page = new PageImpl<>(List.of(c1, c2), PageRequest.of(0, 7), 2);
        when(cardRepository.findByAcctId("00000000001", PageRequest.of(0, 7))).thenReturn(page);

        var result = cardService.listCardsByAccount("00000000001", PageRequest.of(0, 7));
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("T2-CARD-017: List cards for account with no cards returns empty")
    void listCards_noCards_returnsEmpty() {
        Page<Card> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 7), 0);
        when(cardRepository.findByAcctId("99999999999", PageRequest.of(0, 7))).thenReturn(emptyPage);

        var result = cardService.listCardsByAccount("99999999999", PageRequest.of(0, 7));
        assertThat(result.getContent()).isEmpty();
    }

    // --- XREF Cross-Reference ---

    @Test
    @DisplayName("T2-CARD-018: XREF resolve returns correct customer and account")
    void resolveXref_returnsCorrectMapping() {
        CardXref xref = CardXref.builder()
                .cardNum("4111111111111111").custId("000000001").acctId("00000000001").build();
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));

        var result = cardService.resolveXref("4111111111111111");
        assertThat(result.getCustId()).isEqualTo("000000001");
        assertThat(result.getAcctId()).isEqualTo("00000000001");
    }

    @Test
    @DisplayName("T2-CARD-019: XREF resolve for unknown card throws")
    void resolveXref_unknownCard_throws() {
        when(cardXrefRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.resolveXref("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("T2-CARD-020: Get card detail for nonexistent card throws")
    void getCardDetail_notFound_throws() {
        when(cardRepository.findById("0000000000000000")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardDetail("0000000000000000"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
