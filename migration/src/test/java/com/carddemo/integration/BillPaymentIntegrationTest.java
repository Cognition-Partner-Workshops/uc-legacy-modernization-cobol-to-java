package com.carddemo.integration;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountNotFoundException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.service.BillPaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for BillPaymentService.
 * Verifies COBOL COBIL00C.cbl bill payment logic against real H2 database.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BillPaymentIntegrationTest {

    @Autowired
    private BillPaymentService billPaymentService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Bill payment reduces account balance (COBIL00C)")
    void shouldReduceAccountBalance_onPayment() {
        Account account = accountRepository.findById("00000000001").orElseThrow();
        BigDecimal originalBalance = account.getCurrentBalance();
        BigDecimal paymentAmount = new BigDecimal("500.00");

        Transaction paymentTran = billPaymentService.processPayment("00000000001", paymentAmount);

        assertNotNull(paymentTran);
        assertNotNull(paymentTran.getTranId());
        assertEquals("02", paymentTran.getTypeCd(), "Payment type should be '02'");
        assertTrue(paymentTran.getDescription().contains("Bill Payment"));

        // Verify balance was reduced
        Account updated = accountRepository.findById("00000000001").orElseThrow();
        BigDecimal expectedBalance = originalBalance.subtract(paymentAmount);
        assertEquals(0, expectedBalance.compareTo(updated.getCurrentBalance()),
                "Balance should be reduced by payment amount");
    }

    @Test
    @DisplayName("Bill payment creates negative transaction record (COBIL00C)")
    void shouldCreateNegativeTransactionRecord() {
        BigDecimal paymentAmount = new BigDecimal("200.00");

        Transaction paymentTran = billPaymentService.processPayment("00000000001", paymentAmount);

        assertEquals(0, paymentAmount.negate().compareTo(paymentTran.getAmount()),
                "Transaction amount should be negative (payment reduces balance)");
        assertEquals("Online", paymentTran.getSource());
    }

    @Test
    @DisplayName("Bill payment for nonexistent account throws exception")
    void shouldThrow_whenAccountNotFound() {
        assertThrows(AccountNotFoundException.class,
                () -> billPaymentService.processPayment("99999999999", new BigDecimal("100.00")));
    }
}
