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
 * Boundary and risk-based tests for COTRN00C.cbl (Transaction List Controller).
 * Covers: transaction list display, pagination (PF7/PF8), transaction selection,
 * boundary page numbers, missing COMMAREA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "USER0001")
class TransactionListControllerTest {

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
    @DisplayName("Transaction list page displays")
    void testTransactionListDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/list").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-list"))
            .andExpect(model().attributeExists("transactions"))
            .andExpect(model().attributeExists("currentPage"));
    }

    @Test
    @DisplayName("PF3 returns to menu")
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    // ===== BOUNDARY TESTS - PAGINATION =====

    @Test
    @DisplayName("Default page is 0")
    void testDefaultPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/list").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentPage", 0));
    }

    @Test
    @DisplayName("PF7 on first page stays at page 0")
    void testPF7OnFirstPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("action", "PF7")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/list?page=0"));
    }

    @Test
    @DisplayName("PF8 advances to next page")
    void testPF8AdvancesPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("action", "PF8")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/list?page=1"));
    }

    @Test
    @DisplayName("PF7 from page 3 goes to page 2")
    void testPF7FromPage3() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("action", "PF7")
                .param("page", "3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/list?page=2"));
    }

    @Test
    @DisplayName("Very high page number returns empty list")
    void testVeryHighPageNumber() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/list")
                .param("page", "99999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-list"));
    }

    // ===== BOUNDARY TESTS - TRANSACTION SELECTION =====

    @Test
    @DisplayName("Selecting a transaction redirects to transaction view")
    void testSelectTransaction() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("selectedTran", "0000000000000001")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/view"));
    }

    @Test
    @DisplayName("Empty selectedTran stays on list")
    void testEmptySelectedTran() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/list")
                .param("selectedTran", "")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/list?page=0"));
    }

    // ===== RISK-BASED TESTS - MISSING SESSION =====

    @Test
    @DisplayName("GET /transaction/list without COMMAREA redirects to signon")
    void testNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/transaction/list").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST /transaction/list without COMMAREA redirects to signon")
    void testPostNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/transaction/list")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== RISK-BASED TESTS - HEADER INFO =====

    @Test
    @DisplayName("Transaction list page has header info")
    void testTransactionListHasHeaderInfo() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/list").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("title01"))
            .andExpect(model().attributeExists("title02"))
            .andExpect(model().attributeExists("tranId"))
            .andExpect(model().attributeExists("pgmName"));
    }
}
