package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MainMenuController.class)
@Import(SecurityConfig.class)
class MainMenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void getMenu_authenticatedUser_returns200() throws Exception {
        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("main-menu"))
                .andExpect(model().attributeExists("menuOptions"))
                .andExpect(model().attributeExists("isAdmin"))
                .andExpect(model().attribute("isAdmin", false));
    }

    @Test
    @WithMockUser(username = "ADMIN001", roles = {"ADMIN"})
    void getMenu_adminUser_seesAdminFlag() throws Exception {
        mockMvc.perform(get("/menu"))
                .andExpect(status().isOk())
                .andExpect(view().name("main-menu"))
                .andExpect(model().attribute("isAdmin", true));
    }

    @Test
    void getMenu_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/menu"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void getMenu_containsTranAndPgmName() throws Exception {
        mockMvc.perform(get("/menu"))
                .andExpect(model().attribute("tranName", "CM00"))
                .andExpect(model().attribute("pgmName", "COMEN01C"));
    }
}
