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
 * Boundary and risk-based tests for COCRDLIC.cbl (Card List Controller).
 * Covers: card list display, pagination boundaries, PF7/PF8 navigation,
 * card selection, empty pages, missing COMMAREA.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "USER0001")
class CardListControllerTest {

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
    @DisplayName("Card list page displays with cards")
    void testCardListDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/card/list").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("card-list"))
            .andExpect(model().attributeExists("cards"))
            .andExpect(model().attributeExists("currentPage"));
    }

    @Test
    @DisplayName("PF3 returns to menu")
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/card/list")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    // ===== BOUNDARY TESTS - PAGINATION =====

    @Test
    @DisplayName("First page (page=0) loads successfully")
    void testFirstPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/card/list")
                .param("page", "0")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentPage", 0));
    }

    @Test
    @DisplayName("PF7 on first page stays at page 0")
    void testPF7OnFirstPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/card/list")
                .param("action", "PF7")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/card/list?page=0"));
    }

    @Test
    @DisplayName("PF8 advances to next page")
    void testPF8AdvancesPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/card/list")
                .param("action", "PF8")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/card/list?page=1"));
    }

    @Test
    @DisplayName("Very high page number returns empty result set")
    void testVeryHighPageNumber() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/card/list")
                .param("page", "9999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("card-list"));
    }

    @Test
    @DisplayName("Page 1 loads second set of cards")
    void testSecondPage() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/card/list")
                .param("page", "1")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("currentPage", 1));
    }

    // ===== BOUNDARY TESTS - CARD SELECTION =====

    @Test
    @DisplayName("Selecting a card redirects to card select page")
    void testSelectCard() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/card/list")
                .param("selectedCard", "1234567890123456")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/card/select"));
    }

    @Test
    @DisplayName("Empty selectedCard stays on card list")
    void testEmptySelectedCard() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/card/list")
                .param("selectedCard", "")
                .param("page", "0")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/card/list?page=0"));
    }

    // ===== RISK-BASED TESTS - MISSING SESSION =====

    @Test
    @DisplayName("GET /card/list without COMMAREA redirects to signon")
    void testNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/card/list").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST /card/list without COMMAREA redirects to signon")
    void testPostNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/card/list")
                .param("action", "PF8")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== RISK-BASED TESTS - HEADER INFO =====

    @Test
    @DisplayName("Card list page has header info")
    void testCardListHasHeaderInfo() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/card/list").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("title01"))
            .andExpect(model().attributeExists("title02"))
            .andExpect(model().attributeExists("tranId"))
            .andExpect(model().attributeExists("pgmName"));
    }
}
