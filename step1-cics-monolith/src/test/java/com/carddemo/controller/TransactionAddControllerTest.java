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
 * Functional parity tests for COTRN02C.cbl (Transaction Add Controller).
 * Verifies transaction addition with full validation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionAddControllerTest {

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
    void testTransactionAddPageDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/add").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-add"));
    }

    @Test
    void testTransactionAddWithEmptyFieldsShowsErrors() throws Exception {
        // COTRN02C VALIDATE-INPUT-DATA-FIELDS: type CD not empty, category CD not empty
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("tranTypeCd", "")
                .param("tranCatCd", "")
                .param("tranAmt", "")
                .param("tranCardNum", "")
                .session(session)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-add"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("action", "PF3")
                .session(session)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }
}
