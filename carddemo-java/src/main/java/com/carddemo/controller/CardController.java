package com.carddemo.controller;

import com.carddemo.dto.CardUpdateDTO;
import com.carddemo.entity.Card;
import com.carddemo.service.CardService;
import com.carddemo.service.CardService.CardDetailWithCustomer;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Card controller - consolidates COCRDLIC (list), COCRDSLC (view), COCRDUPC (update).
 * Three COBOL programs (1460 + 888 + 1561 lines) consolidated into three REST endpoints.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<Page<Card>> listCards(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.listCardsByAccount(accountId, PageRequest.of(page, size)));
    }

    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardDetailWithCustomer> getCard(@PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getCardDetail(cardNumber));
    }

    @PutMapping("/{cardNumber}")
    public ResponseEntity<Card> updateCard(@PathVariable String cardNumber,
                                           @Valid @RequestBody CardUpdateDTO dto) {
        return ResponseEntity.ok(cardService.updateCard(cardNumber, dto));
    }
}
