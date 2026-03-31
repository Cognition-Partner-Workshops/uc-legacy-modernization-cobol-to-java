package com.carddemo.controller.api;

import com.carddemo.dto.BillPaymentRequest;
import com.carddemo.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentApiController {

    private final AccountService accountService;

    public PaymentApiController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody BillPaymentRequest request) {
        accountService.getAccount(request.getAcctId());
        BigDecimal paymentAmount = request.getAmount().negate();
        accountService.updateBalance(request.getAcctId(), paymentAmount, true);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Payment of $" + request.getAmount() + " applied to account " +
                        String.format("%011d", request.getAcctId()) + " successfully."
        ));
    }
}
