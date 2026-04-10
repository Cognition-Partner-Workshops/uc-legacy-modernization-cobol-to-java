package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.model.entity.CardXref;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
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
 * Mirrors COCRDSLC.cbl - Credit Card View/Select.
 */
@Controller
public class CardSelectController {

    private static final String PGM_NAME = "COCRDSLC";
    private static final String TRAN_ID = "CC02";

    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public CardSelectController(CardRepository cardRepository, CardXrefRepository cardXrefRepository) {
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    @GetMapping("/card/select")
    public String showCardSelect(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("card", null);
        model.addAttribute("xref", null);

        if (commarea.getCardNum() != null && !commarea.getCardNum().isBlank()) {
            loadCardData(commarea.getCardNum(), model);
        }

        return "card-select";
    }

    @PostMapping("/card/select")
    public String processCardSelect(@RequestParam(defaultValue = "") String cardNum,
                                    @RequestParam(defaultValue = "") String action,
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
            model.addAttribute("xref", null);
            return "card-select";
        }

        commarea.setCardNum(cardNum.trim());
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        loadCardData(cardNum.trim(), model);
        return "card-select";
    }

    private void loadCardData(String cardNum, Model model) {
        Optional<Card> cardOpt = cardRepository.findById(cardNum);
        if (cardOpt.isPresent()) {
            model.addAttribute("card", cardOpt.get());
            Optional<CardXref> xrefOpt = cardXrefRepository.findByXrefCardNum(cardNum);
            xrefOpt.ifPresent(x -> model.addAttribute("xref", x));
            model.addAttribute("errorMessage", "");
        } else {
            model.addAttribute("card", null);
            model.addAttribute("xref", null);
            model.addAttribute("errorMessage", "Card not found...");
        }
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Credit Card View");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
