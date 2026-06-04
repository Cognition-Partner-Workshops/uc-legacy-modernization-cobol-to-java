package com.carddemo.auth.controller;

import com.carddemo.auth.model.UserType;
import com.carddemo.auth.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@link UserController} - the {@code USRSEC}
 * maintenance programs ({@code COUSR00C}-{@code COUSR03C}) replacement.
 * Verifies CRUD behaviour and that the admin-only access of the legacy
 * {@code COADM01C} menu is enforced via JWT roles.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String adminAuth() {
        return "Bearer " + tokenProvider.generateToken("ADMIN001", UserType.ADMIN);
    }

    private String userAuth() {
        return "Bearer " + tokenProvider.generateToken("USER0001", UserType.USER);
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private Map<String, Object> newUser(String id) {
        Map<String, Object> body = new HashMap<>();
        body.put("userId", id);
        body.put("firstName", "Test");
        body.put("lastName", "Person");
        body.put("password", "secret12");
        body.put("userType", "USER");
        return body;
    }

    @Test
    void listUsersAsAdminReturnsSeededUsers() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void listUsersAsNonAdminReturns403() throws Exception {
        mockMvc.perform(get("/api/users").header("Authorization", userAuth()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getSingleUserReturnsItWithoutPassword() throws Exception {
        mockMvc.perform(get("/api/users/ADMIN001").header("Authorization", adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.password", nullValue()));
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        mockMvc.perform(get("/api/users/NOPE0001").header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUserPersistsAndIsLoginable() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(newUser("NEWUSR01"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSR01"))
                .andExpect(jsonPath("$.password", nullValue()));

        // The created user can sign on with the plaintext password (BCrypt verify).
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("userId", "NEWUSR01", "password", "secret12"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("NEWUSR01"));
    }

    @Test
    void createDuplicateUserReturns409() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(newUser("ADMIN001"))))
                .andExpect(status().isConflict());
    }

    @Test
    void createUserWithMissingFieldsReturns400() throws Exception {
        Map<String, Object> invalid = new HashMap<>();
        invalid.put("userId", "BADUSR01");
        invalid.put("firstName", "");
        invalid.put("lastName", "");
        invalid.put("password", "");
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserAsNonAdminReturns403() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", userAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(newUser("XUSER001"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUserChangesFields() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(newUser("UPDUSR01"))))
                .andExpect(status().isCreated());

        Map<String, Object> update = new HashMap<>();
        update.put("userId", "UPDUSR01");
        update.put("firstName", "Updated");
        update.put("lastName", "Name");
        update.put("userType", "ADMIN");

        mockMvc.perform(put("/api/users/UPDUSR01")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void updateUnknownUserReturns404() throws Exception {
        Map<String, Object> update = new HashMap<>();
        update.put("firstName", "Ghost");
        mockMvc.perform(put("/api/users/GHOST001")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUserRemovesIt() throws Exception {
        mockMvc.perform(post("/api/users")
                        .header("Authorization", adminAuth())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(newUser("DELUSR01"))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/users/DELUSR01").header("Authorization", adminAuth()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/DELUSR01").header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUnknownUserReturns404() throws Exception {
        mockMvc.perform(delete("/api/users/MISSING1").header("Authorization", adminAuth()))
                .andExpect(status().isNotFound());
    }
}
