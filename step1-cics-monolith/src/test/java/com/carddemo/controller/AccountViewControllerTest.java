package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Functional parity tests for COACTVWC.cbl (Account View Controller).
 * Verifies account lookup by ID and display.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAuthenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setCdemoUserId("USER0001");
        commarea.setCdemoUserType("U");
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    @Test
    void testAccountViewPageDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/account/view").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"));
    }

    @Test
    void testAccountViewByIdFound() throws Exception {
        // COACTVWC: Enter account ID → READ DATASET('ACCTDAT') RIDFLD(key) → display account data
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "1")
                .session(session)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attributeExists("account"));
    }

    @Test
    void testAccountViewByIdNotFound() throws Exception {
        // COACTVWC: DFHRESP(NOTFND) → show error
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("acctId", "99999999999")
                .session(session)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("account-view"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/account/view")
                .param("action", "PF3")
                .session(session)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }
}
