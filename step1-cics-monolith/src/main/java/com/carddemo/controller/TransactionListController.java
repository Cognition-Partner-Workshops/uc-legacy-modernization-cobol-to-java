package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import com.carddemo.model.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
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
 * Mirrors COTRN00C.cbl - Transaction List.
 */
@Controller
public class TransactionListController {

    private static final String PGM_NAME = "COTRN00C";
    private static final String TRAN_ID = "CT00";
    private static final int PAGE_SIZE = 10;

    private final TransactionRepository transactionRepository;

    public TransactionListController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/transaction/list")
    public String showTransactionList(@RequestParam(defaultValue = "0") int page,
                                      Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        commarea.setPgmContext(1);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        populateHeaderInfo(model);
        Page<Transaction> transactions = transactionRepository.findAllByOrderByTranIdDesc(
            PageRequest.of(page, PAGE_SIZE));
        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("errorMessage", "");
        return "tran-list";
    }

    @PostMapping("/transaction/list")
    public String processTransactionList(@RequestParam(defaultValue = "") String action,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "") String selectedTran,
                                         Model model, HttpSession session) {
        CardDemoCommarea commarea = getCommarea(session);
        if (commarea == null) return "redirect:/signon";

        if ("PF3".equals(action)) return "redirect:/menu";
        if ("PF7".equals(action)) return "redirect:/transaction/list?page=" + Math.max(0, page - 1);
        if ("PF8".equals(action)) return "redirect:/transaction/list?page=" + (page + 1);

        if (!selectedTran.isBlank()) {
            commarea.setCt02TrnSelected(selectedTran.trim());
            session.setAttribute("CARDDEMO_COMMAREA", commarea);
            return "redirect:/transaction/view";
        }

        return "redirect:/transaction/list?page=" + page;
    }

    private CardDemoCommarea getCommarea(HttpSession session) {
        return (CardDemoCommarea) session.getAttribute("CARDDEMO_COMMAREA");
    }

    private void populateHeaderInfo(Model model) {
        LocalDateTime now = LocalDateTime.now();
        model.addAttribute("title01", "AWS CardDemo");
        model.addAttribute("title02", "Transaction List");
        model.addAttribute("tranId", TRAN_ID);
        model.addAttribute("pgmName", PGM_NAME);
        model.addAttribute("currentDate", now.format(DateTimeFormatter.ofPattern("MM/dd/yy")));
        model.addAttribute("currentTime", now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }
}
