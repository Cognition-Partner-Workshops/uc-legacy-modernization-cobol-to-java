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
 * Boundary and risk-based tests for COUSR01C.cbl (User Add Controller).
 * Covers: user creation, required field validation, duplicate user detection,
 * user type validation, admin-only access enforcement, boundary inputs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "ADMIN001")
class UserAddControllerTest {

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
    @DisplayName("User add page displays for admin")
    void testUserAddDisplays() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(get("/admin/user/add").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("user-add"));
    }

    @Test
    @DisplayName("PF3 returns to admin menu")
    void testPF3ReturnsToAdmin() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    // ===== BOUNDARY TESTS - REQUIRED FIELDS =====

    @Test
    @DisplayName("Empty userId shows required error")
    void testEmptyUserId() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "")
                .param("password", "PASSWORD")
                .param("userType", "U")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User ID is required..."));
    }

    @Test
    @DisplayName("Empty password shows required error")
    void testEmptyPassword() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "NEWUSER1")
                .param("password", "")
                .param("userType", "U")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Password is required..."));
    }

    @Test
    @DisplayName("Empty userType shows type error")
    void testEmptyUserType() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "NEWUSER1")
                .param("password", "PASSWORD")
                .param("userType", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User Type must be 'A' (Admin) or 'U' (User)..."));
    }

    @Test
    @DisplayName("Invalid userType (X) shows type error")
    void testInvalidUserType() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "NEWUSER1")
                .param("password", "PASSWORD")
                .param("userType", "X")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User Type must be 'A' (Admin) or 'U' (User)..."));
    }

    // ===== BOUNDARY TESTS - USER TYPE VALUES =====

    @Test
    @DisplayName("userType=A (admin) accepted")
    void testUserTypeAdmin() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "TESTADM1")
                .param("password", "PASSWORD")
                .param("userType", "A")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User added successfully..."));
    }

    @Test
    @DisplayName("userType=U (user) accepted")
    void testUserTypeUser() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "TESTUSR1")
                .param("password", "PASSWORD")
                .param("userType", "U")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User added successfully..."));
    }

    @Test
    @DisplayName("Lowercase userType=a accepted (uppercased)")
    void testLowercaseUserType() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "TESTLC01")
                .param("password", "PASSWORD")
                .param("userType", "a")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User added successfully..."));
    }

    // ===== BOUNDARY TESTS - DUPLICATE USER =====

    @Test
    @DisplayName("Duplicate userId shows already exists error")
    void testDuplicateUserId() throws Exception {
        MockHttpSession session = createAdminSession();

        // ADMIN001 already exists in seed data
        mockMvc.perform(post("/admin/user/add")
                .param("userId", "ADMIN001")
                .param("password", "PASSWORD")
                .param("userType", "A")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User already exists..."));
    }

    // ===== RISK-BASED TESTS - ACCESS CONTROL =====

    @Test
    @DisplayName("Non-admin user redirected from user add GET")
    void testNonAdminGetRedirects() throws Exception {
        MockHttpSession session = createUserSession();

        mockMvc.perform(get("/admin/user/add").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    @DisplayName("Non-admin user redirected from user add POST")
    void testNonAdminPostRedirects() throws Exception {
        MockHttpSession session = createUserSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "NEWUSER1")
                .param("password", "PASSWORD")
                .param("userType", "U")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    @DisplayName("No COMMAREA redirects to signon")
    void testNoCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/admin/user/add").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    // ===== BOUNDARY TESTS - SPECIAL CHARACTERS =====

    @Test
    @DisplayName("userId with special characters - uppercased and saved")
    void testSpecialCharsUserId() throws Exception {
        MockHttpSession session = createAdminSession();

        mockMvc.perform(post("/admin/user/add")
                .param("userId", "test!@#1")
                .param("password", "PASSWORD")
                .param("userType", "U")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "User added successfully..."));
    }

    @Test
    @DisplayName("Very long userId is saved (database may truncate)")
    void testVeryLongUserId() throws Exception {
        MockHttpSession session = createAdminSession();

        String longId = "A".repeat(100);
        mockMvc.perform(post("/admin/user/add")
                .param("userId", longId)
                .param("password", "PASSWORD")
                .param("userType", "U")
                .session(session))
            .andExpect(status().isOk());
    }
}
