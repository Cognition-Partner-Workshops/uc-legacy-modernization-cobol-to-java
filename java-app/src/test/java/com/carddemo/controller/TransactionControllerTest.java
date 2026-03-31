package com.carddemo.controller;

import com.carddemo.model.Transaction;
import com.carddemo.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void listTransactions_returnsTransactionList() throws Exception {
        Transaction transaction = Transaction.builder()
                .tranId("0000000000000001")
                .typeCd("01")
                .catCd(1)
                .amount(new BigDecimal("100.00"))
                .cardNum("4000123456789010")
                .build();

        when(transactionService.getTransactions(anyInt()))
                .thenReturn(new PageImpl<>(List.of(transaction)));

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-list"))
                .andExpect(model().attributeExists("transactions"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void viewTransaction_found() throws Exception {
        Transaction transaction = Transaction.builder()
                .tranId("0000000000000001")
                .typeCd("01")
                .amount(new BigDecimal("100.00"))
                .build();

        when(transactionService.findById("0000000000000001"))
                .thenReturn(Optional.of(transaction));

        mockMvc.perform(get("/transactions/view/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-detail"))
                .andExpect(model().attributeExists("transaction"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void viewTransaction_notFound() throws Exception {
        when(transactionService.findById("9999999999999999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/view/9999999999999999"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-detail"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void showAddTransaction_returnsForm() throws Exception {
        mockMvc.perform(get("/transactions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-add"))
                .andExpect(model().attributeExists("transactionRequest"));
    }

    @Test
    void listTransactions_unauthenticated_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isUnauthorized());
    }
}
