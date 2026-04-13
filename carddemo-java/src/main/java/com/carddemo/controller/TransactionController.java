package com.carddemo.controller;

import com.carddemo.dto.TransactionForm;
import com.carddemo.entity.Transaction;
import com.carddemo.service.TransactionService;
import org.springframework.data.domain.Page;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Transaction Controller - maps to COTRN00C/CT00 + COTRN01C/CT01 + COTRN02C/CT02
 * Screens from app/bms/COTRN00.bms, COTRN01.bms, COTRN02.bms
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    private static final DateTimeFormatter TS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(required = false) String cardNum,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   Authentication authentication,
                                   Model model) {
        if (cardNum != null && !cardNum.trim().isEmpty()) {
            Page<Transaction> transactions = transactionService.listTransactions(cardNum, page, size);
            model.addAttribute("transactions", transactions);
            model.addAttribute("cardNum", cardNum);
        }
        model.addAttribute("tranName", "CT00");
        model.addAttribute("pgmName", "COTRN00C");
        return "transaction-list";
    }

    @GetMapping("/{cardNum}/{tranId}")
    public String viewTransaction(@PathVariable String cardNum,
                                  @PathVariable String tranId,
                                  Model model) {
        try {
            Transaction transaction = transactionService.viewTransaction(cardNum, tranId);
            model.addAttribute("transaction", transaction);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        model.addAttribute("tranName", "CT01");
        model.addAttribute("pgmName", "COTRN01C");
        return "transaction-view";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("transactionForm", new TransactionForm());
        model.addAttribute("tranName", "CT02");
        model.addAttribute("pgmName", "COTRN02C");
        return "transaction-add";
    }

    @PostMapping
    public String addTransaction(@ModelAttribute TransactionForm form,
                                 RedirectAttributes redirectAttributes) {
        try {
            Transaction transaction = new Transaction();
            transaction.setCardNum(form.getCardNum());
            transaction.setTypeCd(form.getTypeCd());
            transaction.setCatCd(form.getCatCd());
            transaction.setSource(form.getSource());
            transaction.setDescription(form.getDescription());
            transaction.setAmount(form.getAmount());
            transaction.setMerchantId(form.getMerchantId());
            transaction.setMerchantName(form.getMerchantName());
            transaction.setMerchantCity(form.getMerchantCity());
            transaction.setMerchantZip(form.getMerchantZip());
            transaction.setOrigTimestamp(LocalDateTime.now().format(TS_FORMATTER));

            Transaction saved = transactionService.addTransaction(transaction);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Transaction added successfully: " + saved.getTranId());
            return "redirect:/transactions?cardNum=" + saved.getCardNum();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transactions/add";
        }
    }
}
