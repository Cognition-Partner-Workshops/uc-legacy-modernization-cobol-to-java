package com.carddemo.controller.api;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.model.CardData;
import com.carddemo.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardApiController {

    private final CardService cardService;

    public CardApiController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public ResponseEntity<List<CardData>> listCards() {
        return ResponseEntity.ok(cardService.getAllCards());
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<CardData> getCard(@PathVariable String cardNum) {
        return ResponseEntity.ok(cardService.getCard(cardNum));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<CardData> updateCard(@PathVariable String cardNum, @RequestBody CardUpdateRequest request) {
        request.setCardNum(cardNum);
        return ResponseEntity.ok(cardService.updateCard(request));
    }
}
