package com.carddemo.controller;

import com.carddemo.model.CardXref;
import com.carddemo.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public Page<CardXref> listCards(Pageable pageable) {
        return cardService.findAll(pageable);
    }

    @GetMapping("/{cardNum}")
    public CardXref getCard(@PathVariable String cardNum) {
        return cardService.findByCardNum(cardNum);
    }

    @GetMapping(params = {"custId", "!acctId"})
    public List<CardXref> getCardsByCustomer(@RequestParam long custId) {
        return cardService.findByCustomerId(custId);
    }

    @GetMapping(params = {"acctId", "!custId"})
    public List<CardXref> getCardsByAccount(@RequestParam long acctId) {
        return cardService.findByAccountId(acctId);
    }

    @PostMapping
    public ResponseEntity<CardXref> createCard(@Valid @RequestBody CardXref card) {
        CardXref created = cardService.create(card);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{cardNum}")
    public CardXref updateCard(@PathVariable String cardNum, @Valid @RequestBody CardXref card) {
        return cardService.update(cardNum, card);
    }
}
