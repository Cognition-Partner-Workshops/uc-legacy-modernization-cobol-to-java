package com.carddemo.card.controller;

import com.carddemo.card.dto.*;
import com.carddemo.card.service.CardService;
import com.carddemo.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<CardListItemDto>> listCards(
            @RequestParam String acctId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "7") int size) {
        return ResponseEntity.ok(cardService.listCardsByAccount(acctId, PageRequest.of(page, size)));
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<CardDto> getCardDetail(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCardDetail(cardNum));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<CardDto> updateCard(@PathVariable String cardNum,
                                               @Valid @RequestBody UpdateCardRequest request) {
        return ResponseEntity.ok(cardService.updateCard(cardNum, request));
    }

    @GetMapping("/xref/{cardNum}")
    public ResponseEntity<CardXrefDto> getXref(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.resolveXref(cardNum));
    }

    @GetMapping("/xref/by-account/{acctId}")
    public ResponseEntity<CardXrefDto> getXrefByAccount(@PathVariable String acctId) {
        return ResponseEntity.ok(
                cardService.resolveXrefByAccount(acctId)
                        .orElseThrow(() -> new com.carddemo.common.exception.ResourceNotFoundException(
                                "CardXref", "acctId", acctId)));
    }
}
