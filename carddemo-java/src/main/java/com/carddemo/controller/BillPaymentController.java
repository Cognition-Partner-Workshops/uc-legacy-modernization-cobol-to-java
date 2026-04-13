package com.carddemo.controller;

import com.carddemo.dto.PaymentForm;
import com.carddemo.service.BillPaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Bill Payment Controller - maps to COBIL00C/CB00
 * Screen from app/bms/COBIL00.bms
 */
@Controller
@RequestMapping("/payments")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @GetMapping
    public String showPaymentForm(Authentication authentication, Model model) {
        model.addAttribute("paymentForm", new PaymentForm());
        model.addAttribute("tranName", "CB00");
        model.addAttribute("pgmName", "COBIL00C");
        return "bill-payment";
    }

    @PostMapping
    public String processPayment(@ModelAttribute PaymentForm form,
                                 RedirectAttributes redirectAttributes) {
        try {
            billPaymentService.processPayment(form.getAcctId(), form.getAmount());
            redirectAttributes.addFlashAttribute("infoMessage", "Payment processed successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/payments";
    }
}
