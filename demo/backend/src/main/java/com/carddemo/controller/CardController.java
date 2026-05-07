package com.carddemo.controller;

import com.carddemo.model.Card;
import com.carddemo.service.CardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    // Equivalent to COCRDLIC.cbl 7-row page display
    private static final int DEFAULT_PAGE_SIZE = 7;

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // Equivalent to 1200-SCREEN-ARRAY-INIT + 9000-READ-FORWARD in COCRDLIC.cbl
    // Supports both paged (page=0&size=7) and non-paged (no params) access.
    @GetMapping
    public ResponseEntity<?> listCards(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        // Equivalent to 2200-EDIT-MAP-INPUTS in COCRDSLC.cbl
        // Both account filter blank → return all cards

        // Paged response (equivalent to PF7/PF8 navigation in COCRDLIC.cbl)
        if (page != null) {
            int pageSize = (size != null) ? size : DEFAULT_PAGE_SIZE;
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<Card> result = cardService.listCardsPaged(accountId, pageable);
            return ResponseEntity.ok(Map.of(
                    "content", result.getContent(),
                    "totalPages", result.getTotalPages(),
                    "totalElements", result.getTotalElements(),
                    "page", result.getNumber(),
                    "size", result.getSize()
            ));
        }

        // Non-paged (backward compatible)
        return ResponseEntity.ok(cardService.listCards(accountId));
    }

    // Equivalent to 9000-READ-DATA in COCRDSLC.cbl (single key lookup)
    @GetMapping("/{cardNumber}")
    public ResponseEntity<Card> getCard(@PathVariable String cardNumber) {
        return cardService.getCard(cardNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Equivalent to 9100-GETCARD-BYACCTCARD in COCRDSLC.cbl (composite key lookup)
    // COBOL RIDFLD = ACCT-ID + CARD-NUM for VSAM KSDS read.
    @GetMapping("/{cardNumber}/verify")
    public ResponseEntity<?> getCardByAccountAndCard(
            @PathVariable String cardNumber,
            @RequestParam String accountId) {
        return cardService.getCardByAccountAndCard(accountId, cardNumber)
                .map(card -> ResponseEntity.ok((Object) card))
                .orElse(ResponseEntity.notFound().build());
    }

    // Equivalent to 2000-DECIDE-ACTION + 9100-UPDATE-CARD + 9200-WRITE-PROCESSING in COCRDUPC.cbl
    // confirmed param: Equivalent to PF5 confirmation requirement in 2000-DECIDE-ACTION.
    @PutMapping("/{cardNumber}")
    public ResponseEntity<?> updateCard(
            @PathVariable String cardNumber,
            @RequestBody Card card,
            @RequestParam(defaultValue = "true") boolean confirmed) {
        Card updated = cardService.updateCard(cardNumber, card, confirmed);
        return ResponseEntity.ok(updated);
    }
}
