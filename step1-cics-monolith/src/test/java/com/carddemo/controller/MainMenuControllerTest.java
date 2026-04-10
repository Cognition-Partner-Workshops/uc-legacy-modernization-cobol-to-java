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
 * Functional parity tests for COMEN01C.cbl (Main Menu Controller).
 * Verifies menu display and navigation to sub-programs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MainMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAuthenticatedSession(String userId, String userType) {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setCdemoUserId(userId);
        commarea.setCdemoUserType(userType);
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    @Test
    void testMainMenuDisplaysForUser() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        mockMvc.perform(get("/menu").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("main-menu"))
            .andExpect(model().attributeExists("menuOptions"));
    }

    @Test
    void testMenuOptionNavigatesToAccountView() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        // COMEN01C: Select option 1 → XCTL to account view
        mockMvc.perform(post("/menu")
                .param("selection", "01")
                .session(session)
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void testPF3ExitsToSignon() throws Exception {
        MockHttpSession session = createAuthenticatedSession("USER0001", "U");

        // COMEN01C: PF3 → exit to signon
        mockMvc.perform(post("/menu")
                .param("action", "PF3")
                .session(session)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }
}
