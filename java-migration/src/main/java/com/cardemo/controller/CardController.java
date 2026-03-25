/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.controller;

import com.cardemo.model.CardRecord;
import com.cardemo.service.online.CardListService;
import com.cardemo.service.online.CardViewService;
import com.cardemo.service.online.CardUpdateService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Card Controller - REST API for card operations.
 * Migrated from COBOL CICS programs COCRDLIC.cbl, COCRDUPC.cbl, COCRDSLC.cbl.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardListService cardListService;
    private final CardViewService cardViewService;
    private final CardUpdateService cardUpdateService;

    public CardController(CardListService cardListService,
                          CardViewService cardViewService,
                          CardUpdateService cardUpdateService) {
        this.cardListService = cardListService;
        this.cardViewService = cardViewService;
        this.cardUpdateService = cardUpdateService;
    }

    @GetMapping
    public ResponseEntity<?> listCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Long acctId) {
        if (acctId != null) {
            List<CardRecord> cards = cardListService.listCardsForAccount(acctId);
            return ResponseEntity.ok(cards);
        }
        return ResponseEntity.ok(cardListService.listCards(page));
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<Map<String, Object>> viewCard(@PathVariable String cardNum) {
        try {
            Map<String, Object> result = cardViewService.viewCard(cardNum);
            return ResponseEntity.ok(result);
        } catch (CardViewService.CardViewException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<Map<String, Object>> updateCard(
            @PathVariable String cardNum,
            @RequestBody CardRecord card) {
        try {
            card.setCardNum(cardNum);
            CardRecord updated = cardUpdateService.updateCard(card);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "message", "Card updated successfully",
                    "card", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()));
        }
    }
}
