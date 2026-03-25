package com.carddemo.api.controller;

import com.carddemo.common.dto.CardDto;
import com.carddemo.api.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Card controller replacing COBOL programs:
 * - COCRDLIC (CCLI transaction) — Card List
 * - COCRDSLC (CCDL transaction) — Card Detail/Select
 * - COCRDUPC (CCUP transaction) — Card Update
 *
 * Original COBOL: app/cbl/COCRDLIC.cbl, app/cbl/COCRDSLC.cbl, app/cbl/COCRDUPC.cbl
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    /**
     * GET /api/cards — List all cards.
     * Replaces COCRDLIC (CCLI transaction).
     */
    @GetMapping
    public ResponseEntity<List<CardDto>> listCards() {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDLIC");
    }

    /**
     * GET /api/cards/{cardNumber} — View card details.
     * Replaces COCRDSLC (CCDL transaction).
     */
    @GetMapping("/{cardNumber}")
    public ResponseEntity<CardDto> getCard(@PathVariable String cardNumber) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDSLC");
    }

    /**
     * PUT /api/cards/{cardNumber} — Update card.
     * Replaces COCRDUPC (CCUP transaction).
     */
    @PutMapping("/{cardNumber}")
    public ResponseEntity<CardDto> updateCard(
            @PathVariable String cardNumber,
            @RequestBody CardDto cardDto) {
        throw new UnsupportedOperationException(
                "TODO: Implement - migrated from COBOL program COCRDUPC");
    }
}
