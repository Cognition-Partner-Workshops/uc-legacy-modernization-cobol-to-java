package com.carddemo.controller;

import com.carddemo.model.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Functional parity tests for COSGN00C.cbl (Signon Controller).
 * Verifies signon flow: show screen, authenticate, redirect based on user type.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @BeforeEach
    void setUp() {
        // Seed data is loaded via Flyway V2__seed_data.sql
    }

    @Test
    void testSignonPageDisplays() throws Exception {
        mockMvc.perform(get("/signon"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"));
    }

    @Test
    void testAdminSignonRedirectsToAdminMenu() throws Exception {
        // COSGN00C: If SEC-USR-TYPE = 'A', XCTL to COADM01C (admin menu)
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "ADMIN001")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void testUserSignonRedirectsToMainMenu() throws Exception {
        // COSGN00C: If SEC-USR-TYPE = 'U', XCTL to COMEN01C (main menu)
        mockMvc.perform(post("/signon")
                .param("userId", "USER0001")
                .param("password", "USER0001")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    void testWrongPasswordShowsError() throws Exception {
        // COSGN00C: Wrong password → show error message
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "WRONGPWD")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void testUserNotFoundShowsError() throws Exception {
        // COSGN00C: RESP=13 (NOTFND) → show "User not found" error
        mockMvc.perform(post("/signon")
                .param("userId", "NONEXIST")
                .param("password", "ANYTHING")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attributeExists("errorMessage"));
    }
}
