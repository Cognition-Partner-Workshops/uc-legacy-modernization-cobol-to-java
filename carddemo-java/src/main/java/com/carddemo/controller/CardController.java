package com.carddemo.controller;

import com.carddemo.dto.CardForm;
import com.carddemo.entity.CreditCard;
import com.carddemo.service.CardService;
import org.springframework.security.core.Authentication;
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
 * Card Controller - maps to COCRDLIC/CCLI + COCRDSLC/CCDL + COCRDUPC/CCUP
 * Screens from app/bms/COCRDLI.bms, COCRDSL.bms, COCRDUP.bms
 */
@Controller
@RequestMapping("/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public String listCards(@RequestParam(required = false) Long acctId,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Authentication authentication,
                            Model model) {
        if (acctId != null) {
            model.addAttribute("cards", cardService.listCards(acctId, page, size));
            model.addAttribute("acctId", acctId);
        }
        model.addAttribute("tranName", "CCLI");
        model.addAttribute("pgmName", "COCRDLIC");
        return "card-list";
    }

    @GetMapping("/search")
    public String searchCards(@RequestParam(required = false) String cardNum,
                              @RequestParam(required = false) Long acctId,
                              Authentication authentication,
                              Model model) {
        if (cardNum != null || acctId != null) {
            List<CreditCard> results = cardService.searchCards(cardNum, acctId);
            model.addAttribute("cards", results);
        }
        model.addAttribute("tranName", "CCDL");
        model.addAttribute("pgmName", "COCRDSLC");
        return "card-search";
    }

    @GetMapping("/{cardNum}")
    public String viewCard(@PathVariable String cardNum, Model model) {
        try {
            CreditCard card = cardService.getCardDetail(cardNum);
            model.addAttribute("card", card);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tranName", "CCDL");
        model.addAttribute("pgmName", "COCRDSLC");
        return "card-search";
    }

    @GetMapping("/{cardNum}/edit")
    public String editCard(@PathVariable String cardNum, Model model) {
        try {
            CreditCard card = cardService.getCardDetail(cardNum);
            model.addAttribute("card", card);
            model.addAttribute("cardForm", new CardForm());
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tranName", "CCUP");
        model.addAttribute("pgmName", "COCRDUPC");
        return "card-update";
    }

    @PostMapping("/{cardNum}")
    public String updateCard(@PathVariable String cardNum,
                             @ModelAttribute CardForm form,
                             RedirectAttributes redirectAttributes) {
        try {
            CreditCard updatedFields = new CreditCard();
            updatedFields.setActiveStatus(form.getActiveStatus());
            updatedFields.setExpirationDate(form.getExpirationDate());
            updatedFields.setEmbossedName(form.getEmbossedName());

            cardService.updateCard(cardNum, updatedFields);
            redirectAttributes.addFlashAttribute("infoMessage", "Card updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cards/" + cardNum;
    }
}
