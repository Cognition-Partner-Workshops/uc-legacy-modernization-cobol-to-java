package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Card;
import com.carddemo.repository.CardRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mirrors COCRDLIC.cbl - Credit Card List.
 */
@Controller
public class CardListController {

    private static final String PGM_NAME = "COCRDLIC";
    private static final String TRAN_ID = "CC01";
    private static final int PAGE_SIZE = 10;

    private final CardRepository cardRepository;

    public CardListController(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @GetMapping("/card/list")
    public String showCardList(@RequestParam(defaultValue = "0") int page,
                               Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        Page<Card> cards = cardRepository.findAllByOrderByCardNumAsc(PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("cards", cards);
        model.addAttribute("currentPage", page);
        model.addAttribute("errorMessage", "");
        return "card-list";
    }

    @PostMapping("/card/list")
    public String processCardList(@RequestParam(defaultValue = "") String action,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "") String selectedCard,
                                  Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) {
            return "redirect:/menu";
        }
        if ("PF7".equals(action)) {
            return "redirect:/card/list?page=" + Math.max(0, page - 1);
        }
        if ("PF8".equals(action)) {
            return "redirect:/card/list?page=" + (page + 1);
        }

        if (!selectedCard.isBlank()) {
            commarea.setCardNum(selectedCard.trim());
            session.setAttribute("CARDDEMO_COMMAREA", commarea);
            return "redirect:/card/select";
        }

        return "redirect:/card/list?page=" + page;
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Credit Card List");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
