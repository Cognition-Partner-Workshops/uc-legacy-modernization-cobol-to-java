package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Mirrors COCRDUPC.cbl - Credit Card Update.
 */
@Controller
public class CardUpdateController {

    private static final String PGM_NAME = "COCRDUPC";
    private static final String TRAN_ID = "CC03";

    private final CardRepository cardRepository;

    public CardUpdateController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping("/card/update")
    public String showCardUpdate(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("card", null);

        if (commarea.getCardNum() != null && !commarea.getCardNum().isBlank()) {
            cardRepository.findById(commarea.getCardNum())
                .ifPresent(c -> model.addAttribute("card", c));
        }

        return "card-update";
    }

    @PostMapping("/card/update")
    public String processCardUpdate(@RequestParam(defaultValue = "") String cardNum,
                                    @RequestParam(defaultValue = "") String action,
                                    @RequestParam(defaultValue = "") String cardEmbossedName,
                                    @RequestParam(defaultValue = "") String cardActiveStatus,
                                    @RequestParam(defaultValue = "") String confirm,
                                    Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) {
            return "redirect:/menu";
        }

        populateHeaderInfo(model);

        if (cardNum.isBlank()) {
            model.addAttribute("errorMessage", "Please enter a Card Number...");
            model.addAttribute("card", null);
            return "card-update";
        }

        Optional<Card> cardOpt = cardRepository.findById(cardNum.trim());
        if (cardOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Card not found...");
            model.addAttribute("card", null);
            return "card-update";
        }

        Card card = cardOpt.get();

        if ("Y".equalsIgnoreCase(confirm)) {
            if (!cardEmbossedName.isBlank()) {
                card.setCardEmbossedName(cardEmbossedName.trim());
            }
            if (!cardActiveStatus.isBlank()) {
                card.setCardActiveStatus(cardActiveStatus.trim());
            }
            cardRepository.save(card);
            model.addAttribute("errorMessage", "Card updated successfully...");
        } else {
            model.addAttribute("errorMessage", "Confirm update (Y/N)...");
        }

        model.addAttribute("card", card);
        return "card-update";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Credit Card Update");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
