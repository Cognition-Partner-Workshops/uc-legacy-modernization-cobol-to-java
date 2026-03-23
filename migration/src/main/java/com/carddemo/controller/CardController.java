package com.carddemo.controller;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.entity.Card;
import com.carddemo.service.CardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Replaces CCLI (card list), CCDL (card detail), CCUP (card update) transactions.
 */
@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public String listCards(@RequestParam(required = false) String acctId, Model model) {
        List<Card> cards;
        if (acctId != null && !acctId.isBlank()) {
            cards = cardService.getCardsByAccount(acctId);
            model.addAttribute("acctId", acctId);
        } else {
            cards = cardService.getAllCards();
        }
        model.addAttribute("cards", cards);
        return "card-list";
    }

    @GetMapping("/{cardNum}")
    public String cardDetail(@PathVariable String cardNum, Model model) {
        Card card = cardService.getCard(cardNum);
        model.addAttribute("card", card);
        return "card-detail";
    }

    @GetMapping("/{cardNum}/edit")
    public String editCard(@PathVariable String cardNum, Model model) {
        Card card = cardService.getCard(cardNum);
        model.addAttribute("card", card);
        model.addAttribute("updateRequest", new CardUpdateRequest());
        return "card-update";
    }

    @PostMapping("/{cardNum}/edit")
    public String updateCard(@PathVariable String cardNum,
                             @ModelAttribute CardUpdateRequest updateRequest,
                             RedirectAttributes redirectAttributes) {
        cardService.updateCard(cardNum, updateRequest);
        redirectAttributes.addFlashAttribute("message", "Card updated successfully");
        return "redirect:/cards/" + cardNum;
    }
}
