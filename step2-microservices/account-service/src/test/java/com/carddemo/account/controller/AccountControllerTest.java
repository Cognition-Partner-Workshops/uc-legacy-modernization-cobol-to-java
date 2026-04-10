package com.carddemo.account.controller;

import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for Account Service (CRUD operations).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
        Account acct = new Account();
        acct.setAcctId(1L);
        acct.setAcctActiveStatus("Y");
        acct.setAcctCurrBal(new BigDecimal("1000.00"));
        acct.setAcctCreditLimit(new BigDecimal("5000.00"));
        acct.setAcctOpenDate("2020-01-15");
        acct.setAcctExpiraionDate("2025-12-31");
        accountRepository.save(acct);

        Account acct2 = new Account();
        acct2.setAcctId(2L);
        acct2.setAcctActiveStatus("N");
        acct2.setAcctCurrBal(new BigDecimal("500.00"));
        acct2.setAcctCreditLimit(new BigDecimal("2000.00"));
        accountRepository.save(acct2);
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("List all accounts returns 200")
    void testListAccounts() throws Exception {
        mockMvc.perform(get("/api/accounts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get account by valid ID returns 200")
    void testGetAccountById() throws Exception {
        mockMvc.perform(get("/api/accounts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(1))
            .andExpect(jsonPath("$.acctActiveStatus").value("Y"));
    }

    @Test
    @DisplayName("Create new account returns 200")
    void testCreateAccount() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":99,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":0,\"acctCreditLimit\":1000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctId").value(99));
    }

    // ===== BOUNDARY TESTS - NON-EXISTENT ACCOUNT =====

    @Test
    @DisplayName("Get non-existent account returns 404")
    void testGetNonExistentAccount() throws Exception {
        mockMvc.perform(get("/api/accounts/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get account with ID 0 returns 404")
    void testGetAccountIdZero() throws Exception {
        mockMvc.perform(get("/api/accounts/0"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get account with very large ID returns 404")
    void testGetVeryLargeAccountId() throws Exception {
        mockMvc.perform(get("/api/accounts/99999999999"))
            .andExpect(status().isNotFound());
    }

    // ===== BOUNDARY TESTS - UPDATE =====

    @Test
    @DisplayName("Update existing account returns 200")
    void testUpdateExistingAccount() throws Exception {
        mockMvc.perform(put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":1,\"acctActiveStatus\":\"N\",\"acctCurrBal\":2000,\"acctCreditLimit\":5000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctActiveStatus").value("N"));
    }

    @Test
    @DisplayName("Update non-existent account returns 404")
    void testUpdateNonExistentAccount() throws Exception {
        mockMvc.perform(put("/api/accounts/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":99999,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":0,\"acctCreditLimit\":1000}"))
            .andExpect(status().isNotFound());
    }

    // ===== BOUNDARY TESTS - BALANCE VALUES =====

    @Test
    @DisplayName("Create account with zero balance")
    void testCreateAccountZeroBalance() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":100,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":0,\"acctCreditLimit\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctCurrBal").value(0));
    }

    @Test
    @DisplayName("Create account with negative balance")
    void testCreateAccountNegativeBalance() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":101,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":-500.50,\"acctCreditLimit\":1000}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctCurrBal").value(-500.50));
    }

    @Test
    @DisplayName("Create account with very large credit limit")
    void testCreateAccountLargeCreditLimit() throws Exception {
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"acctId\":102,\"acctActiveStatus\":\"Y\",\"acctCurrBal\":0,\"acctCreditLimit\":9999999999.99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctCreditLimit").value(9999999999.99));
    }

    // ===== RISK-BASED TESTS - INACTIVE ACCOUNT =====

    @Test
    @DisplayName("Inactive account (status=N) is retrievable")
    void testInactiveAccountRetrievable() throws Exception {
        mockMvc.perform(get("/api/accounts/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.acctActiveStatus").value("N"));
    }
}
