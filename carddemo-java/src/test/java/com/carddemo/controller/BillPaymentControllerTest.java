package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.BillPaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BillPaymentController.class)
@Import(SecurityConfig.class)
class BillPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillPaymentService billPaymentService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void showPaymentForm_returnsForm() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(view().name("bill-payment"))
                .andExpect(model().attributeExists("paymentForm"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void processPayment_redirectsToPayments() throws Exception {
        Transaction txn = new Transaction();
        txn.setTranId("0000000000000001");
        when(billPaymentService.processPayment(eq(1L), any(BigDecimal.class))).thenReturn(txn);

        mockMvc.perform(post("/payments")
                        .with(csrf())
                        .param("acctId", "1")
                        .param("amount", "500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/payments"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void processPayment_error_redirectsWithError() throws Exception {
        when(billPaymentService.processPayment(eq(1L), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Account not active"));

        mockMvc.perform(post("/payments")
                        .with(csrf())
                        .param("acctId", "1")
                        .param("amount", "500.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
