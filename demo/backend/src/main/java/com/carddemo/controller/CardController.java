package com.carddemo.controller;

import com.carddemo.model.Card;
import com.carddemo.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<Card>> listCards(
            @RequestParam(required = false) String accountId) {
        return ResponseEntity.ok(cardService.listCards(accountId));
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<Card> getCard(@PathVariable String cardNumber) {
        return cardService.getCard(cardNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{cardNumber}")
    public ResponseEntity<?> updateCard(
            @PathVariable String cardNumber,
            @RequestBody Card card) {
        try {
            Card updated = cardService.updateCard(cardNumber, card);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
