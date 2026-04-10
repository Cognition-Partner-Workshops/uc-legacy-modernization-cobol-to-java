package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
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
 * Mirrors COTRN01C.cbl - Transaction View.
 */
@Controller
public class TransactionViewController {

    private static final String PGM_NAME = "COTRN01C";
    private static final String TRAN_ID = "CT01";

    private final TransactionRepository transactionRepository;

    public TransactionViewController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transaction/view")
    public String showTransactionView(Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        model.addAttribute("errorMessage", "");
        model.addAttribute("transaction", null);

        if (commarea.getCt02TrnSelected() != null && !commarea.getCt02TrnSelected().isBlank()) {
            transactionRepository.findById(commarea.getCt02TrnSelected())
                .ifPresent(t -> model.addAttribute("transaction", t));
        }

        return "tran-view";
    }

    @PostMapping("/transaction/view")
    public String processTransactionView(@RequestParam(defaultValue = "") String tranId,
                                         @RequestParam(defaultValue = "") String action,
                                         Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) return "redirect:/transaction/list";

        populateHeaderInfo(model);

        if (tranId.isBlank()) {
            model.addAttribute("errorMessage", "Please enter a Transaction ID...");
            model.addAttribute("transaction", null);
            return "tran-view";
        }

        Optional<Transaction> tranOpt = transactionRepository.findById(tranId.trim());
        if (tranOpt.isPresent()) {
            model.addAttribute("transaction", tranOpt.get());
            model.addAttribute("errorMessage", "");
        } else {
            model.addAttribute("transaction", null);
            model.addAttribute("errorMessage", "Transaction not found...");
        }

        return "tran-view";
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Transaction View");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
