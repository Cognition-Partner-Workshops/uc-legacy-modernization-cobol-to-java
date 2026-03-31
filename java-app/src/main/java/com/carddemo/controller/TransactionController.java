package com.carddemo.controller;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionService;
import com.carddemo.util.DateTimeUtil;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(required = false) String cardNum,
                                    Model model, Authentication authentication) {
        populateHeader(model, authentication, "COTRN00C");

        Page<Transaction> transactions;
        if (cardNum != null && !cardNum.isBlank()) {
            transactions = transactionService.getTransactionsByCardNum(cardNum, page);
            model.addAttribute("cardNum", cardNum);
        } else {
            transactions = transactionService.getTransactions(page);
        }

        model.addAttribute("transactions", transactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactions.getTotalPages());
        return "transaction-list";
    }

    @GetMapping("/view")
    public String showTransactionView(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COTRN01C");
        return "transaction-detail";
    }

    @GetMapping("/view/{tranId}")
    public String viewTransaction(@PathVariable String tranId,
                                   Model model, Authentication authentication) {
        populateHeader(model, authentication, "COTRN01C");
        transactionService.findById(tranId).ifPresentOrElse(
                t -> model.addAttribute("transaction", t),
                () -> model.addAttribute("errorMessage", "Transaction not found: " + tranId)
        );
        return "transaction-detail";
    }

    @GetMapping("/add")
    public String showAddTransaction(Model model, Authentication authentication) {
        populateHeader(model, authentication, "COTRN02C");
        model.addAttribute("transactionRequest", new TransactionAddRequest());
        return "transaction-add";
    }

    @PostMapping("/add")
    public String addTransaction(@Valid @ModelAttribute("transactionRequest") TransactionAddRequest request,
                                  BindingResult result,
                                  Model model, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        populateHeader(model, authentication, "COTRN02C");

        if (result.hasErrors()) {
            return "transaction-add";
        }

        try {
            Transaction transaction = transactionService.addTransaction(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Transaction added successfully. ID: " + transaction.getTranId());
            return "redirect:/transactions/view/" + transaction.getTranId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("transactionRequest", request);
            return "transaction-add";
        }
    }

    private void populateHeader(Model model, Authentication authentication, String programName) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", programName);
        model.addAttribute("userId", authentication.getName());
    }
}
