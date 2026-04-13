package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for COACTVWC.cbl (Account View Controller).
 * Covers: account lookup, boundary account IDs, non-existent IDs, non-numeric IDs,
 * empty IDs, max-length IDs, COMMAREA session handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "USER0001")
class AccountViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAuthenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("USER0001");
        commarea.setUserType("U");
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Account view page displays")
    void testAccountViewPageDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/account/view").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"));
    }

    @Test
    @DisplayName("Account lookup by valid ID returns account data")
    void testAccountViewByIdFound() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attribute("account", notNullValue()));
    }

    @Test
    @DisplayName("Account lookup by non-existent ID shows error")
    void testAccountViewByIdNotFound() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "99999999999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attribute("errorMessage", "Account not found..."));
    }

    @Test
    @DisplayName("PF3 returns to menu")
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    // ===== BOUNDARY TESTS - ACCOUNT ID BOUNDARIES =====

    @Test
    @DisplayName("Minimum valid account ID (1) returns data")
    void testMinimumAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("account", notNullValue()));
    }

    @Test
    @DisplayName("Account ID 50 (max seed data) returns data")
    void testMaxSeedAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "50")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("account", notNullValue()));
    }

    @Test
    @DisplayName("Account ID 51 (just beyond seed data) returns not found")
    void testJustBeyondSeedAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "51")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account not found..."));
    }

    @Test
    @DisplayName("Empty account ID shows error message")
    void testEmptyAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attribute("errorMessage", "Please enter an Account ID..."));
    }

    @Test
    @DisplayName("Account ID with leading zeros parses correctly")
    void testAccountIdWithLeadingZeros() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "0001")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("account", notNullValue()));
    }

    @Test
    @DisplayName("Non-numeric account ID shows error")
    void testNonNumericAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "ABC")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attribute("errorMessage", "Account ID must be numeric..."));
    }

    @Test
    @DisplayName("Decimal account ID rejected as non-numeric")
    void testDecimalAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1.5")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account ID must be numeric..."));
    }

    @Test
    @DisplayName("Negative account ID is parsed but not found")
    void testNegativeAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "-1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account not found..."));
    }

    @Test
    @DisplayName("Zero account ID returns not found")
    void testZeroAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "0")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account not found..."));
    }

    @Test
    @DisplayName("Account ID with spaces is trimmed")
    void testAccountIdWithSpaces() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", " 1 ")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("account", notNullValue()));
    }

    @Test
    @DisplayName("Special characters in account ID rejected")
    void testSpecialCharsInAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "!@#$")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account ID must be numeric..."));
    }

    @Test
    @DisplayName("SQL injection in account ID rejected")
    void testSqlInjectionAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1; DROP TABLE account;")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account ID must be numeric..."));
    }

    @Test
    @DisplayName("Very large account ID returns not found (no overflow)")
    void testVeryLargeAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "99999999999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account not found..."));
    }

    // ===== RISK-BASED TESTS - MISSING SESSION =====

    @Test
    @DisplayName("GET /account/view without COMMAREA redirects to signon")
    void testAccountViewWithoutCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/account/view").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST /account/view without COMMAREA redirects to signon")
    void testPostAccountViewWithoutCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== RISK-BASED TESTS - COMMAREA WITH PRE-SET ACCT ID =====

    @Test
    @DisplayName("GET with COMMAREA acctId pre-set loads that account")
    void testGetWithPreSetAcctId() throws Exception {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("USER0001");
        commarea.setUserType("U");
        commarea.setAcctId(5);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);

        mockMvc.perform(get("/account/view").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attribute("account", notNullValue()));
    }
}
