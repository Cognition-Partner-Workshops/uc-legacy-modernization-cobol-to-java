package com.carddemo.controller;

import com.carddemo.model.Card;
import com.carddemo.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Card controller - replaces COBOL programs:
 *   COCRDLIC.cbl (CCLI transaction) -> GET  /api/cards
 *   COCRDSLC.cbl (CCDL transaction) -> GET  /api/cards/{cardNumber}
 *   COCRDUPC.cbl (CCUP transaction) -> PUT  /api/cards/{cardNumber}
 *
 * Legacy CICS flow (COCRDLIC - Card List):
 *   1. STARTBR DATASET('CARDDAT')
 *   2. READNEXT loop (7 cards per page)
 *   3. User selects 'S' to view details -> XCTL to COCRDSLC
 *   4. PF7/PF8 for pagination (READPREV/READNEXT)
 *
 * Modernized: Reactive REST endpoints replace BMS screen navigation
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public Flux<Card> listCards(@RequestParam(required = false) String accountId) {
        if (accountId != null && !accountId.isBlank()) {
            return cardService.getCardsByAccountId(accountId);
        }
        return cardService.getAllCards();
    }

    @GetMapping("/{cardNumber}")
    public Mono<Card> viewCard(@PathVariable String cardNumber) {
        return cardService.getCardByNumber(cardNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Card> createCard(@Valid @RequestBody Card card) {
        return cardService.createCard(card);
    }

    @PutMapping("/{cardNumber}")
    public Mono<Card> updateCard(@PathVariable String cardNumber,
                                 @Valid @RequestBody Card cardUpdate) {
        return cardService.updateCard(cardNumber, cardUpdate);
    }
}
