package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    private Transaction createTestTransaction() {
        Transaction txn = new Transaction();
        txn.setCardNum("4000123456789010");
        txn.setTranId("0000000000000001");
        txn.setTypeCd("01");
        txn.setCatCd(1);
        txn.setAmount(new BigDecimal("100.00"));
        txn.setDescription("Test purchase");
        txn.setOrigTimestamp("2024-01-15 10:30:00.000000");
        return txn;
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void listTransactions_returnsTransactionList() throws Exception {
        Transaction txn = createTestTransaction();
        when(transactionService.listTransactions(eq("4000123456789010"), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(txn)));

        mockMvc.perform(get("/transactions").param("cardNum", "4000123456789010"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-list"))
                .andExpect(model().attributeExists("transactions"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void viewTransaction_returnsDetail() throws Exception {
        Transaction txn = createTestTransaction();
        when(transactionService.viewTransaction("4000123456789010", "0000000000000001"))
                .thenReturn(txn);

        mockMvc.perform(get("/transactions/4000123456789010/0000000000000001"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-view"))
                .andExpect(model().attributeExists("transaction"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void showAddForm_returnsForm() throws Exception {
        mockMvc.perform(get("/transactions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-add"))
                .andExpect(model().attributeExists("transactionForm"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void addTransaction_redirectsToList() throws Exception {
        Transaction txn = createTestTransaction();
        when(transactionService.addTransaction(any(Transaction.class))).thenReturn(txn);

        mockMvc.perform(post("/transactions")
                        .with(csrf())
                        .param("cardNum", "4000123456789010")
                        .param("amount", "100.00")
                        .param("typeCd", "01")
                        .param("description", "Test"))
                .andExpect(status().is3xxRedirection());
    }
}
