package com.cardemo.controller;

import com.cardemo.model.Card;
import com.cardemo.model.CardXref;
import com.cardemo.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Card Controller - converted from COBOL programs COCRDLIC, COCRDSLC, COCRDUPC
 * Original: CICS Credit Card List, View, and Update screens
 * Replaces BMS maps COCRDLI/COCRDSL/COCRDUP with REST endpoints.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * GET /api/cards?acctId={acctId}
     * List cards for an account - replaces COCRDLIC SEND-CRDLST-SCREEN.
     */
    @GetMapping
    public ResponseEntity<List<Card>> listCards(@RequestParam Long acctId) {
        return ResponseEntity.ok(cardService.listCardsByAccount(acctId));
    }

    /**
     * GET /api/cards/{cardNum}
     * View card details - replaces COCRDSLC SEND-CRDSEL-SCREEN.
     */
    @GetMapping("/{cardNum}")
    public ResponseEntity<Card> viewCard(@PathVariable String cardNum) {
        return cardService.viewCard(cardNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/cards/{cardNum}
     * Update card details - replaces COCRDUPC PROCESS-ENTER-KEY.
     */
    @PutMapping("/{cardNum}")
    public ResponseEntity<Card> updateCard(@PathVariable String cardNum,
                                           @RequestBody Card updatedData) {
        return cardService.updateCard(cardNum, updatedData)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/cards/{cardNum}/xref
     * Get card cross-reference data.
     */
    @GetMapping("/{cardNum}/xref")
    public ResponseEntity<CardXref> getCardXref(@PathVariable String cardNum) {
        return cardService.getCardXref(cardNum)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
