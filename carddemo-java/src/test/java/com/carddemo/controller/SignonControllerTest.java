package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SignonController.class)
@Import(SecurityConfig.class)
class SignonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    @Test
    void getLogin_returns200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("signon"));
    }

    @Test
    void getLogin_withError_showsErrorMessage() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("signon"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void getLogin_withLogout_showsInfoMessage() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("signon"))
                .andExpect(model().attributeExists("infoMessage"));
    }

    @Test
    void getLogin_containsTranAndPgmName() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(model().attribute("tranName", "CC00"))
                .andExpect(model().attribute("pgmName", "COSGN00C"));
    }
}
