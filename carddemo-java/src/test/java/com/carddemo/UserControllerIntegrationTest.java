package com.carddemo;

import com.carddemo.entity.User;
import com.carddemo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String ADMIN_BASE = "/api/admin/users";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(new User("ADMIN001", "ADMIN", "USER",
                passwordEncoder.encode("password"), "A"));
        userRepository.save(new User("USER0001", "FIRST", "USER",
                passwordEncoder.encode("password"), "U"));
        userRepository.save(new User("USER0002", "SECOND", "USER",
                passwordEncoder.encode("password"), "U"));
    }

    @Test
    void listUsers_returnsPaginatedResults() throws Exception {
        mockMvc.perform(get(ADMIN_BASE)
                        .param("page", "0")
                        .param("size", "10")
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void addUser_succeedsWithValidData() throws Exception {
        String body = """
                {
                    "userId": "NEWUSR01",
                    "firstName": "NEW",
                    "lastName": "USER",
                    "password": "pass1234",
                    "userType": "U"
                }
                """;
        mockMvc.perform(post(ADMIN_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is("NEWUSR01")))
                .andExpect(jsonPath("$.firstName", is("NEW")))
                .andExpect(jsonPath("$.userType", is("U")));
    }

    @Test
    void addUser_failsWithDuplicateUserId_409() throws Exception {
        String body = """
                {
                    "userId": "ADMIN001",
                    "firstName": "DUPE",
                    "lastName": "USER",
                    "password": "pass1234",
                    "userType": "U"
                }
                """;
        mockMvc.perform(post(ADMIN_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isConflict());
    }

    @Test
    void updateUser_succeeds() throws Exception {
        String body = """
                {
                    "firstName": "UPDATED",
                    "lastName": "NAME",
                    "userType": "U"
                }
                """;
        mockMvc.perform(put(ADMIN_BASE + "/USER0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("UPDATED")))
                .andExpect(jsonPath("$.lastName", is("NAME")));
    }

    @Test
    void updateUser_nonExistent_returns404() throws Exception {
        String body = """
                {
                    "firstName": "GHOST",
                    "lastName": "USER",
                    "userType": "U"
                }
                """;
        mockMvc.perform(put(ADMIN_BASE + "/NOEXIST1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_succeeds() throws Exception {
        mockMvc.perform(delete(ADMIN_BASE + "/USER0002")
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete(ADMIN_BASE + "/NOEXIST1")
                        .with(httpBasic("ADMIN001", "password")))
                .andExpect(status().isNotFound());
    }

    @Test
    void nonAdminUser_gets403_onAllEndpoints() throws Exception {
        mockMvc.perform(get(ADMIN_BASE)
                        .with(httpBasic("USER0001", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post(ADMIN_BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(httpBasic("USER0001", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get(ADMIN_BASE + "/ADMIN001")
                        .with(httpBasic("USER0001", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put(ADMIN_BASE + "/ADMIN001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(httpBasic("USER0001", "password")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete(ADMIN_BASE + "/USER0002")
                        .with(httpBasic("USER0001", "password")))
                .andExpect(status().isForbidden());
    }
}
