package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.Account;
import com.carddemo.entity.CreditCard;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private Account createTestAccount() {
        Account account = new Account();
        account.setAcctId(1L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("10000.00"));
        account.setCreditLimit(new BigDecimal("20000.00"));
        account.setCashCreditLimit(new BigDecimal("5000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        account.setGroupId("A000000000");
        return account;
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void viewAccount_returnsAccountView() throws Exception {
        Account account = createTestAccount();
        when(accountService.viewAccount(1L)).thenReturn(account);
        when(accountService.getCardsForAccount(1L)).thenReturn(List.of());

        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-view"))
                .andExpect(model().attributeExists("account"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void editAccount_returnsUpdateForm() throws Exception {
        Account account = createTestAccount();
        when(accountService.viewAccount(1L)).thenReturn(account);

        mockMvc.perform(get("/accounts/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("account-update"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("accountForm"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void updateAccount_redirectsToAccountView() throws Exception {
        Account account = createTestAccount();
        when(accountService.updateAccount(eq(1L), any(Account.class))).thenReturn(account);

        mockMvc.perform(post("/accounts/1")
                        .with(csrf())
                        .param("activeStatus", "Y")
                        .param("creditLimit", "25000.00"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts/1"));
    }

    @Test
    void viewAccount_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/accounts/1"))
                .andExpect(status().is3xxRedirection());
    }
}
