package com.carddemo.controller;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.Transaction;
import com.carddemo.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Replaces CT00 (transaction list), CT01 (transaction view), CT02 (transaction add).
 */
@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String acctId,
                                   @RequestParam(required = false) String cardNum,
                                   Model model) {
        Page<Transaction> transactions;
        if (cardNum != null && !cardNum.isBlank()) {
            transactions = transactionService.getTransactionsByCard(cardNum,
                    PageRequest.of(page, size, Sort.by("origTimestamp").descending()));
            model.addAttribute("cardNum", cardNum);
        } else if (acctId != null && !acctId.isBlank()) {
            transactions = transactionService.getTransactionsByAccount(acctId,
                    PageRequest.of(page, size, Sort.by("origTimestamp").descending()));
            model.addAttribute("acctId", acctId);
        } else {
            transactions = transactionService.getTransactions(
                    PageRequest.of(page, size, Sort.by("origTimestamp").descending()));
        }
        model.addAttribute("transactions", transactions);
        return "transaction-list";
    }

    @GetMapping("/{tranId}")
    public String viewTransaction(@PathVariable String tranId, Model model) {
        Transaction transaction = transactionService.getTransaction(tranId);
        model.addAttribute("transaction", transaction);
        return "transaction-detail";
    }

    @GetMapping("/add")
    public String addTransactionForm(Model model) {
        model.addAttribute("transactionRequest", new TransactionRequest());
        return "transaction-add";
    }

    @PostMapping("/add")
    public String addTransaction(@ModelAttribute TransactionRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            Transaction transaction = transactionService.addTransaction(request);
            redirectAttributes.addFlashAttribute("message", "Transaction added successfully");
            return "redirect:/transactions/" + transaction.getTranId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/transactions/add";
        }
    }
}
