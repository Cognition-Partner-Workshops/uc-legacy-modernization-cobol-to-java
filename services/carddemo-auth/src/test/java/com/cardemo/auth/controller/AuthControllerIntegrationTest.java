package com.cardemo.auth.controller;

import com.cardemo.auth.dto.CreateUserRequest;
import com.cardemo.auth.dto.LoginRequest;
import com.cardemo.auth.entity.User;
import com.cardemo.auth.entity.User.UserType;
import com.cardemo.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = new User("ADMIN01", "Admin", "User", passwordEncoder.encode("admin123"), UserType.ADMIN);
        User regular = new User("USER01", "Regular", "User", passwordEncoder.encode("user123"), UserType.USER);
        userRepository.save(admin);
        userRepository.save(regular);
    }

    @Test
    void login_withValidCredentials_returnsJwt() throws Exception {
        LoginRequest request = new LoginRequest("ADMIN01", "admin123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.userId").value("ADMIN01"))
                .andExpect(jsonPath("$.userType").value("ADMIN"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("ADMIN01", "wrongpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsers_asAdmin_returnsAllUsers() throws Exception {
        String token = loginAndGetToken("ADMIN01", "admin123");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUsers_asRegularUser_returnsOnlyOwnProfile() throws Exception {
        String token = loginAndGetToken("USER01", "user123");

        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value("USER01"));
    }

    @Test
    void createUser_asAdmin_succeeds() throws Exception {
        String token = loginAndGetToken("ADMIN01", "admin123");
        CreateUserRequest request = new CreateUserRequest("NEWUSR", "New", "User", "pass123", "USER");

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSR"));
    }

    @Test
    void createUser_asRegularUser_returns403() throws Exception {
        String token = loginAndGetToken("USER01", "user123");
        CreateUserRequest request = new CreateUserRequest("NEWUSR", "New", "User", "pass123", "USER");

        mockMvc.perform(post("/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateUser_ownProfile_succeeds() throws Exception {
        String token = loginAndGetToken("USER01", "user123");

        mockMvc.perform(put("/users/USER01")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void updateUser_otherProfile_asRegularUser_returns403() throws Exception {
        String token = loginAndGetToken("USER01", "user123");

        mockMvc.perform(put("/users/ADMIN01")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_asAdmin_succeeds() throws Exception {
        String token = loginAndGetToken("ADMIN01", "admin123");

        mockMvc.perform(delete("/users/USER01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_asRegularUser_returns403() throws Exception {
        String token = loginAndGetToken("USER01", "user123");

        mockMvc.perform(delete("/users/ADMIN01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    private String loginAndGetToken(String userId, String password) throws Exception {
        LoginRequest request = new LoginRequest(userId, password);
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}
