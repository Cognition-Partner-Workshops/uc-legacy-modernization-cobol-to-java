package com.aws.carddemo.web.card;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {
  private final CardService service;

  public CardController(CardService service) {
    this.service = service;
  }

  @GetMapping
  public Response<CardService.CardPage> list(
      @RequestParam(name = "accountId", required = false) Long accountId,
      @RequestParam(name = "cardNumber", required = false) String cardNumber,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "direction", defaultValue = "FORWARD") String direction) {
    return service.list(
        new CardService.CardListRequest(
            accountId,
            cardNumber,
            page,
            direction,
            new Context("", "COCRDLIC", "", "CCRD", null, "U", accountId, cardNumber)));
  }

  @GetMapping("/{cardNumber}")
  public Response<com.aws.carddemo.domain.Card> detail(
      @PathVariable("cardNumber") String cardNumber) {
    return service.detail(
        cardNumber, new Context("", "COCRDSLC", "", "CCRD", null, "U", null, cardNumber));
  }

  @PutMapping("/{cardNumber}")
  public Response<com.aws.carddemo.domain.Card> update(
      @PathVariable("cardNumber") String cardNumber,
      @RequestBody CardService.CardUpdateRequest request) {
    return service.update(cardNumber, request);
  }
}
