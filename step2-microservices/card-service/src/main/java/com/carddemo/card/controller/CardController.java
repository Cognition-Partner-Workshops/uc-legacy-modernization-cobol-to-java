package com.carddemo.card.controller;

import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardController(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @GetMapping
    public List<Card> listCards() {
        return cardRepository.findAll();
    }

    @GetMapping("/{cardNum}")
    public ResponseEntity<Card> getCard(@PathVariable String cardNum) {
        return cardRepository.findById(cardNum)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/account/{acctId}")
    public List<Card> getCardsByAccount(@PathVariable Long acctId) {
        return cardRepository.findByCardAcctId(acctId);
    }

    @GetMapping("/xref/{cardNum}")
    public ResponseEntity<CardXref> getXref(@PathVariable String cardNum) {
        return cardXrefRepository.findByXrefCardNum(cardNum)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Card> createCard(@RequestBody Card card) {
        return ResponseEntity.ok(cardRepository.save(card));
    }

    @PutMapping("/{cardNum}")
    public ResponseEntity<Card> updateCard(@PathVariable String cardNum, @RequestBody Card card) {
        if (!cardRepository.existsById(cardNum)) {
            return ResponseEntity.notFound().build();
        }
        card.setCardNum(cardNum);
        return ResponseEntity.ok(cardRepository.save(card));
    }
}
