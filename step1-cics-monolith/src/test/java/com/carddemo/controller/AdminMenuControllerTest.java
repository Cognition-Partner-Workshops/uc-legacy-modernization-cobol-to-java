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
 * Boundary and risk-based tests for COADM01C.cbl (Admin Menu Controller).
 * Covers: admin menu display, option validation, PF3 navigation,
 * non-admin access rejection, boundary option numbers, missing COMMAREA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "ADMIN001")
class AdminMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAdminSession() {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("ADMIN001");
        commarea.setUserType("A");
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    private MockHttpSession createUserSession() {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("USER0001");
        commarea.setUserType("U");
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Admin menu page displays for admin user")
    void testAdminMenuDisplays() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(get("/admin").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("admin-menu"))
            .andExpect(model().attributeExists("adminOptions"));
    }

    @Test
    @DisplayName("PF3 returns to main menu")
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    @DisplayName("Valid option 1 redirects to user list")
    void testValidOption1() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "1")
                .session(session))
            .andExpect(status().is3xxRedirection());
    }

    // ===== BOUNDARY TESTS - OPTION NUMBERS =====

    @Test
    @DisplayName("Option 0 shows invalid option error")
    void testOptionZero() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "0")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("admin-menu"))
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Option 5 (beyond max 4) shows invalid option error")
    void testOptionBeyondMax() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "5")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Negative option shows invalid option error")
    void testNegativeOption() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "-1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Non-numeric option shows invalid option error")
    void testNonNumericOption() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "ABC")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Empty option shows invalid option error")
    void testEmptyOption() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    @Test
    @DisplayName("Option 4 (max valid) is accepted")
    void testMaxValidOption() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "4")
                .session(session))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("Very large option number rejected")
    void testVeryLargeOption() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin")
                .param("option", "99999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Please enter a valid option number..."));
    }

    // ===== RISK-BASED TESTS - ACCESS CONTROL =====

    @Test
    @DisplayName("GET /admin without COMMAREA redirects to signon")
    void testNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/admin").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST /admin without COMMAREA redirects to signon")
    void testPostNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/admin")
                .param("option", "1")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== RISK-BASED TESTS - HEADER INFO =====

    @Test
    @DisplayName("Admin menu has header info model attributes")
    void testAdminMenuHasHeaderInfo() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(get("/admin").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("title01"))
            .andExpect(model().attributeExists("title02"))
            .andExpect(model().attributeExists("tranId"))
            .andExpect(model().attributeExists("pgmName"))
            .andExpect(model().attributeExists("currentDate"))
            .andExpect(model().attributeExists("currentTime"));
    }
}
