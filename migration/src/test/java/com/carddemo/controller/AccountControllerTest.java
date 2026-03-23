package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.Account;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void viewAccount_returnsAccountView() throws Exception {
        Account account = new Account();
        account.setAcctId("00000000001");
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("5000.00"));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));

        when(accountService.getAccount("00000000001")).thenReturn(account);

        mockMvc.perform(get("/accounts/00000000001"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-view"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = "USER")
    void editAccount_returnsUpdateForm() throws Exception {
        Account account = new Account();
        account.setAcctId("00000000001");
        account.setActiveStatus("Y");

        when(accountService.getAccount("00000000001")).thenReturn(account);

        mockMvc.perform(get("/accounts/00000000001/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-update"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    void viewAccount_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/accounts/00000000001"))
                .andExpect(status().is3xxRedirection());
    }
}
