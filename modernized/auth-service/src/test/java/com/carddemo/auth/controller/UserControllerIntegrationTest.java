package com.carddemo.auth.controller;

import com.carddemo.auth.dto.CreateUserRequest;
import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.dto.UpdateUserRequest;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("carddemo")
            .withUsername("carddemo")
            .withPassword("carddemo")
            .withInitScript("init-test-schema.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.schemas", () -> "auth");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "auth");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        User admin = User.builder()
                .userId("ADMIN001")
                .firstName("Admin")
                .lastName("User")
                .passwordHash(passwordEncoder.encode("PASSWORD"))
                .userType("A")
                .build();
        userRepository.save(admin);

        User regularUser = User.builder()
                .userId("USER0001")
                .firstName("Regular")
                .lastName("User")
                .passwordHash(passwordEncoder.encode("PASSWORD"))
                .userType("U")
                .build();
        userRepository.save(regularUser);

        adminToken = obtainToken("ADMIN001", "PASSWORD");
        userToken = obtainToken("USER0001", "PASSWORD");
    }

    private String obtainToken(String userId, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(userId, password))))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void listUsers_asAdmin_returnsAll() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listUsers_asRegularUser_returns403() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_asAdmin_succeeds() throws Exception {
        CreateUserRequest request = new CreateUserRequest("NEWUSER1", "New", "User", "pass123", "U");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("NEWUSER1"))
                .andExpect(jsonPath("$.firstName").value("New"));
    }

    @Test
    void getUser_returnsUser() throws Exception {
        mockMvc.perform(get("/api/users/ADMIN001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.firstName").value("Admin"));
    }

    @Test
    void updateUser_asAdmin_succeeds() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setFirstName("Updated");

        mockMvc.perform(put("/api/users/USER0001")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void deleteUser_asAdmin_succeeds() throws Exception {
        mockMvc.perform(delete("/api/users/USER0001")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_asRegularUser_returns403() throws Exception {
        CreateUserRequest request = new CreateUserRequest("NOAUTH", "No", "Auth", "pass", "U");

        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }
}
