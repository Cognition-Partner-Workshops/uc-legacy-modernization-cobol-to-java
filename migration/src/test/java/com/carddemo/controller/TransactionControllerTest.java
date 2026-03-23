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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void viewTransaction_returnsDetailView() throws Exception {
        Transaction tran = new Transaction();
        tran.setTranId("TEST0001");
        tran.setTypeCd("01");
        tran.setCatCd(1);
        tran.setAmount(new BigDecimal("100.00"));
        tran.setDescription("Test purchase");
        tran.setOrigTimestamp(LocalDateTime.now());

        when(transactionService.getTransaction("TEST0001")).thenReturn(tran);

        mockMvc.perform(get("/transactions/TEST0001"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-detail"))
                .andExpect(model().attributeExists("transaction"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void addTransactionForm_returnsAddView() throws Exception {
        mockMvc.perform(get("/transactions/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("transaction-add"));
    }

    @Test
    void viewTransaction_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/transactions/TEST0001"))
                .andExpect(status().is3xxRedirection());
    }
}
