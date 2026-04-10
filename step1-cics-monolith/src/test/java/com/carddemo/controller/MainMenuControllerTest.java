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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for COMEN01C.cbl (Main Menu Controller).
 * Covers: menu display, navigation, boundary option numbers, admin-only access,
 * missing COMMAREA, invalid options, AID key handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "USER0001")
class MainMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAuthenticatedSession(String userId, String userType) {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId(userId);
        commarea.setUserType(userType);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Main menu displays for authenticated user")
    void testMainMenuDisplaysForUser() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(get("/menu").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attributeExists("menuOptions"));
    }

    @Test
    @DisplayName("Option 1 navigates to Account View (COACTVWC)")
    void testMenuOptionNavigatesToAccountView() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "1")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account/view"));
    }

    @Test
    @DisplayName("PF3 exits to signon")
    void testPF3ExitsToSignon() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== BOUNDARY TESTS - OPTION NUMBER BOUNDARIES =====

    @Test
    @DisplayName("Option 0 is below valid range")
    void testOptionZeroBelowRange() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "0")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Option 12 is above valid range (max is 11)")
    void testOptionAboveRange() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "12")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Option -1 is invalid (negative)")
    void testNegativeOption() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "-1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Non-numeric option shows error")
    void testNonNumericOption() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "ABC")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Empty option shows error")
    void testEmptyOption() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Option with spaces is trimmed and processed")
    void testOptionWithSpaces() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", " 1 ")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/account/view"));
    }

    // ===== BOUNDARY TESTS - VALID OPTION RANGE (1-11) =====

    @Test
    @DisplayName("Option 1 - minimum valid option")
    void testMinimumValidOption() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "1")
                .session(session))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Option 6 - Transaction List")
    void testOptionSixTransactionList() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "6")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/list"));
    }

    @Test
    @DisplayName("Option 8 - Transaction Add")
    void testOptionEightTransactionAdd() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "8")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/add"));
    }

    // ===== RISK-BASED TESTS - MISSING COMMAREA / SESSION =====

    @Test
    @DisplayName("GET /menu without COMMAREA redirects to signon")
    void testMenuWithoutCommareaRedirectsToSignon() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/menu").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST /menu without COMMAREA redirects to signon")
    void testPostMenuWithoutCommareaRedirectsToSignon() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/menu")
                .param("option", "1")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== RISK-BASED TESTS - ADMIN MENU DISPLAY =====

    @Test
    @DisplayName("Admin user can access menu")
    void testAdminCanAccessMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession("ADMIN001", "A");

        mockMvc.perform(get("/menu").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"));
    }

    @Test
    @DisplayName("Option 11 targets uninstalled program COPAUS0C - shows error")
    void testUninstalledProgramShowsError() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "11")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"));
    }

    @Test
    @DisplayName("Very large option number is rejected")
    void testVeryLargeOptionNumber() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "999999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Special characters in option are rejected")
    void testSpecialCharsInOption() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(post("/menu")
                .param("option", "!@#")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }
}
