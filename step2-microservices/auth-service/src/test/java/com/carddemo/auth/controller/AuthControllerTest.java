package com.carddemo.auth.controller;

import com.carddemo.auth.model.UserSecurity;
import com.carddemo.auth.repository.UserSecurityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for Auth Service (login, user CRUD).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    @BeforeEach
    void setUp() {
        userSecurityRepository.deleteAll();
        UserSecurity admin = new UserSecurity();
        admin.setSecUsrId("ADMIN001");
        admin.setSecUsrFname("Admin");
        admin.setSecUsrLname("User");
        admin.setSecUsrPwd("ADMIN001");
        admin.setSecUsrType("A");
        userSecurityRepository.save(admin);

        UserSecurity user = new UserSecurity();
        user.setSecUsrId("USER0001");
        user.setSecUsrFname("Regular");
        user.setSecUsrLname("User");
        user.setSecUsrPwd("USER0001");
        user.setSecUsrType("U");
        userSecurityRepository.save(user);
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Valid admin login returns 200 with user info")
    void testValidAdminLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"ADMIN001\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("ADMIN001"))
            .andExpect(jsonPath("$.userType").value("A"));
    }

    @Test
    @DisplayName("Valid user login returns 200 with user info")
    void testValidUserLogin() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"USER0001\",\"password\":\"USER0001\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value("USER0001"))
            .andExpect(jsonPath("$.userType").value("U"));
    }

    @Test
    @DisplayName("List users returns all users")
    void testListUsers() throws Exception {
        mockMvc.perform(get("/api/auth/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    // ===== BOUNDARY TESTS - EMPTY/NULL CREDENTIALS =====

    @Test
    @DisplayName("Null userId returns 401")
    void testNullUserId() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    @Test
    @DisplayName("Null password returns 401")
    void testNullPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    @Test
    @DisplayName("Empty userId returns 401")
    void testEmptyUserId() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    @Test
    @DisplayName("Empty password returns 401")
    void testEmptyPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    @Test
    @DisplayName("Blank userId (spaces only) returns 401")
    void testBlankUserId() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"   \",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    @Test
    @DisplayName("Both null returns 401")
    void testBothNull() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Missing credentials"));
    }

    // ===== BOUNDARY TESTS - WRONG CREDENTIALS =====

    @Test
    @DisplayName("Non-existent user returns 401")
    void testNonExistentUser() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"NOUSER01\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Wrong password returns 401")
    void testWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"WRONG123\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Wrong password"));
    }

    // ===== BOUNDARY TESTS - SPECIAL CHARACTERS =====

    @Test
    @DisplayName("Special characters in userId - not found")
    void testSpecialCharsUserId() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"!@#$%^&*\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    @DisplayName("Very long userId - not found")
    void testVeryLongUserId() throws Exception {
        String longId = "A".repeat(500);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"" + longId + "\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("User not found"));
    }

    // ===== RISK-BASED TESTS - USER CRUD =====

    @Test
    @DisplayName("Create new user via POST /api/auth/users")
    void testCreateUser() throws Exception {
        mockMvc.perform(post("/api/auth/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"secUsrId\":\"NEWUSR01\",\"secUsrPwd\":\"NEWPWD01\",\"secUsrType\":\"U\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.secUsrId").value("NEWUSR01"));
    }

    @Test
    @DisplayName("Update existing user via PUT")
    void testUpdateUser() throws Exception {
        mockMvc.perform(put("/api/auth/users/ADMIN001")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"secUsrId\":\"ADMIN001\",\"secUsrPwd\":\"NEWPWD01\",\"secUsrType\":\"A\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.secUsrPwd").value("NEWPWD01"));
    }

    @Test
    @DisplayName("Update non-existent user returns 404")
    void testUpdateNonExistentUser() throws Exception {
        mockMvc.perform(put("/api/auth/users/NOUSER01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"secUsrId\":\"NOUSER01\",\"secUsrPwd\":\"PWD\",\"secUsrType\":\"U\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete existing user returns 204")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/auth/users/USER0001"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete non-existent user returns 404")
    void testDeleteNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/auth/users/NOUSER01"))
            .andExpect(status().isNotFound());
    }

    // ===== RISK-BASED TESTS - NULL PASSWORD IN DB =====

    @Test
    @DisplayName("User with null password in DB returns wrong password on login")
    void testNullPasswordInDb() throws Exception {
        UserSecurity nullPwdUser = new UserSecurity();
        nullPwdUser.setSecUsrId("NULLPWD1");
        nullPwdUser.setSecUsrPwd(null);
        nullPwdUser.setSecUsrType("U");
        userSecurityRepository.save(nullPwdUser);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"NULLPWD1\",\"password\":\"ANYTHING\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Wrong password"));
    }
}
