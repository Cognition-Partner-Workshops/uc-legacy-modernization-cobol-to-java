package com.carddemo.controller;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.service.BillPaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Replaces CB00 bill payment transaction (COBIL00C.cbl).
 */
@Controller
@RequestMapping("/billpay")
public class BillPaymentController {

    private final BillPaymentService billPaymentService;

    public BillPaymentController(BillPaymentService billPaymentService) {
        this.billPaymentService = billPaymentService;
    }

    @GetMapping
    public String billPaymentForm(Model model) {
        model.addAttribute("paymentRequest", new BillPaymentRequest());
        return "bill-payment";
    }

    @PostMapping
    public String processPayment(@ModelAttribute BillPaymentRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            billPaymentService.processPayment(request.getAcctId(), request.getAmount());
            redirectAttributes.addFlashAttribute("message",
                    "Payment of $" + request.getAmount() + " applied to account " + request.getAcctId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/billpay";
    }
}
