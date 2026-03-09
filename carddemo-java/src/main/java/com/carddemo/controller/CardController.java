package com.carddemo.controller;

import com.carddemo.dto.CardDto;
import com.carddemo.service.online.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Card controller replacing COBOL programs COCRDLIC, COCRDSLC, COCRDUPC.
 *
 * <p>COCRDLIC (Card List, CICS txn CCLI) -> GET /api/cards?accountId=X
 * <p>COCRDSLC (Card Detail, CICS txn CCDL) -> GET /api/cards/{cardNum}
 * <p>COCRDUPC (Card Update, CICS txn CCUP) -> PUT /api/cards/{cardNum}
 */
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Card list, view, and update (replaces COCRDLIC/COCRDSLC/COCRDUPC)")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    @Operation(summary = "List cards by account",
            description = "Browse cards for a given account. Replaces COCRDLIC (CICS txn CCLI). "
                    + "Original uses STARTBR/READNEXT on CARDDAT with AIX on CARD-ACCT-ID.")
    public ResponseEntity<List<CardDto>> listCards(
            @Parameter(description = "Account ID to filter cards")
            @RequestParam Long accountId) {
        return ResponseEntity.ok(cardService.getCardsByAccount(accountId));
    }

    @GetMapping("/{cardNum}")
    @Operation(summary = "View card details",
            description = "Retrieve card information by card number. Replaces COCRDSLC (CICS txn CCDL). "
                    + "Original reads CARDDAT by RIDFLD(CARD-NUM).")
    public ResponseEntity<CardDto> getCard(
            @Parameter(description = "Card number (CARD-NUM, PIC X(16))")
            @PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    @Operation(summary = "Update card",
            description = "Update card fields. Replaces COCRDUPC (CICS txn CCUP). "
                    + "Original reads CARDDAT for UPDATE then REWRITE.")
    public ResponseEntity<CardDto> updateCard(
            @Parameter(description = "Card number")
            @PathVariable String cardNum,
            @RequestBody CardDto request) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, request));
    }
}
