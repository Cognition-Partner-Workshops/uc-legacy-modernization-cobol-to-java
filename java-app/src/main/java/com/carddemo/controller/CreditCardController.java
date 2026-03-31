package com.carddemo.controller;

import com.carddemo.dto.CardUpdateRequest;
import com.carddemo.model.CardData;
import com.carddemo.service.CardService;
import com.carddemo.util.DateTimeUtil;
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

@Controller
@RequestMapping("/cards")
public class CreditCardController {

    private final CardService cardService;

    public CreditCardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping("/list")
    public String listCards(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDLIC");
        List<CardData> cards = cardService.getAllCards();
        model.addAttribute("cards", cards);
        return "card-list";
    }

    @GetMapping("/view")
    public String showCardView(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDSLC");
        return "card-detail";
    }

    @GetMapping("/view/{cardNum}")
    public String viewCard(@PathVariable String cardNum, Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDSLC");
        try {
            CardData card = cardService.getCard(cardNum);
            model.addAttribute("card", card);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "card-detail";
    }

    @PostMapping("/view")
    public String viewCardBySearch(@RequestParam("cardNum") String cardNum,
                                   Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDSLC");
        try {
            CardData card = cardService.getCard(cardNum);
            model.addAttribute("card", card);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "card-detail";
    }

    @GetMapping("/update")
    public String showCardUpdate(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDUPC");
        return "card-update";
    }

    @GetMapping("/update/{cardNum}")
    public String showCardUpdateForm(@PathVariable String cardNum,
                                      Model model, Authentication authentication) {
        populateHeader(model, authentication, "COCRDUPC");
        try {
            CardData card = cardService.getCard(cardNum);
            model.addAttribute("card", card);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "card-update";
    }

    @PostMapping("/update")
    public String updateCard(@ModelAttribute CardUpdateRequest updateRequest,
                             RedirectAttributes redirectAttributes) {
        try {
            cardService.updateCard(updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Card updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cards/update/" + updateRequest.getCardNum();
    }

    private void populateHeader(Model model, Authentication authentication, String programName) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", programName);
        model.addAttribute("userId", authentication.getName());
    }
}
