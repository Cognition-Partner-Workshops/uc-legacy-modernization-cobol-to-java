package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.model.Account;
import com.carddemo.service.AccountService;
import com.carddemo.util.DateTimeUtil;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payments")
public class BillPaymentController {

    private final AccountService accountService;

    public BillPaymentController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public String showBillPayment(Model model, Authentication authentication) {
        populateHeader(model, authentication);
        model.addAttribute("paymentRequest", new BillPaymentRequest());
        return "bill-payment";
    }

    @PostMapping
    public String processBillPayment(@Valid @ModelAttribute("paymentRequest") BillPaymentRequest request,
                                      BindingResult result,
                                      Model model, Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        populateHeader(model, authentication);

        if (result.hasErrors()) {
            return "bill-payment";
        }

        try {
            Account account = accountService.getAccount(request.getAcctId());
            // Payment reduces the balance (credit)
            BigDecimal paymentAmount = request.getAmount().negate();
            accountService.updateBalance(request.getAcctId(), paymentAmount, true);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of $" + request.getAmount() + " applied to account " +
                    String.format("%011d", request.getAcctId()) + " successfully.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("paymentRequest", request);
            return "bill-payment";
        }

        return "redirect:/payments";
    }

    private void populateHeader(Model model, Authentication authentication) {
        model.addAttribute("currentDate", DateTimeUtil.getCurrentDateFormatted());
        model.addAttribute("currentTime", DateTimeUtil.getCurrentTimeFormatted());
        model.addAttribute("programName", "COBIL00C");
        model.addAttribute("userId", authentication.getName());
    }
}
