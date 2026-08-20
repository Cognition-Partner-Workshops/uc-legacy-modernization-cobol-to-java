package com.aws.carddemo.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aws.carddemo.domain.Card;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.web.card.CardService;
import com.aws.carddemo.web.common.WebTypes.Context;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CardServiceTest {
  private static final Context CONTEXT =
      new Context("", "", "", "", "USER001", "U", 1L, "4000000000000001");

  @Test
  void expiryMonthYearRoundTripPersistsNormalizedDate() {
    Card card = card("2025-01-17");
    CardRepository repository = mock(CardRepository.class);
    when(repository.findById(card.getCardNum())).thenReturn(Optional.of(card));
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result =
        new CardService(repository)
            .update(
                card.getCardNum(),
                new CardService.CardUpdateRequest(
                    "UPDATED NAME",
                    "Y",
                    null,
                    "12",
                    "2030",
                    new CardService.CardPreImage("ORIGINAL NAME", "Y", "2025-01-17", "01", "2025"),
                    true,
                    CONTEXT));

    assertEquals("", result.message());
    assertEquals("2030-12-17", card.getExpiraionDate());
  }

  @Test
  void expiryMonthYearNoChangeUsesNormalizedPreImageAndReportsNoChange() {
    Card card = card("2025-01-17");
    CardRepository repository = mock(CardRepository.class);
    when(repository.findById(card.getCardNum())).thenReturn(Optional.of(card));

    var result =
        new CardService(repository)
            .update(
                card.getCardNum(),
                new CardService.CardUpdateRequest(
                    "ORIGINAL NAME",
                    "Y",
                    null,
                    "01",
                    "2025",
                    new CardService.CardPreImage("ORIGINAL NAME", "Y", "2025-01-17", "01", "2025"),
                    true,
                    CONTEXT));

    assertEquals("No change detected with respect to values fetched.", result.message());
    verify(repository, never()).save(any());
  }

  private static Card card(String expirationDate) {
    Card card = new Card();
    card.setCardNum("4000000000000001");
    card.setAcctId(1L);
    card.setEmbossedName("ORIGINAL NAME");
    card.setActiveStatus("Y");
    card.setExpiraionDate(expirationDate);
    return card;
  }
}
