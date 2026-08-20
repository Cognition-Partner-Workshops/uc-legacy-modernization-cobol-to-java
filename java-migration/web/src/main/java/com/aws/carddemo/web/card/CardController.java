package com.aws.carddemo.web.card;

import com.aws.carddemo.web.common.WebTypes.Context;
import com.aws.carddemo.web.common.WebTypes.Response;
import org.springframework.security.core.Authentication;
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
      @RequestParam(name = "direction", defaultValue = "FORWARD") String direction,
      Authentication authentication) {
    return service.list(
        new CardService.CardListRequest(
            accountId,
            cardNumber,
            page,
            direction,
            context("COCRDLIC", accountId, cardNumber, authentication)));
  }

  @GetMapping("/{cardNumber}")
  public Response<com.aws.carddemo.domain.Card> detail(
      @PathVariable("cardNumber") String cardNumber, Authentication authentication) {
    return service.detail(cardNumber, context("COCRDSLC", null, cardNumber, authentication));
  }

  @PutMapping("/{cardNumber}")
  public Response<com.aws.carddemo.domain.Card> update(
      @PathVariable("cardNumber") String cardNumber,
      @RequestBody CardService.CardUpdateRequest request,
      Authentication authentication) {
    return service.update(cardNumber, request);
  }

  private static Context context(
      String program, Long accountId, String cardNumber, Authentication authentication) {
    String userId = authentication == null ? "" : authentication.getName();
    String userType =
        authentication != null
                && authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            ? "A"
            : "U";
    return new Context("", program, "", "CCRD", userId, userType, accountId, cardNumber);
  }
}
