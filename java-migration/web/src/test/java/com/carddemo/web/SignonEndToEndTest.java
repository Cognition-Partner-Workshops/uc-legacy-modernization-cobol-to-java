package com.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.domain.entity.Usrsec;
import com.carddemo.domain.repository.UsrsecRepository;
import com.carddemo.web.signon.SignonRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpSession;

@SpringBootTest
@AutoConfigureMockMvc
class SignonEndToEndTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UsrsecRepository usrsecRepository;

    @BeforeEach
    void loadUsers() {
        usrsecRepository.deleteAll();
        usrsecRepository.save(new Usrsec("ADMIN001", "MARGARET", "GOLD", "PASSWORD", "A"));
        usrsecRepository.save(new Usrsec("USER0001", "LAWRENCE", "THOMAS", "PASSWORD", "U"));
    }

    @Test
    void adminSessionReachesAdminMenu() throws Exception {
        MvcResult signon = signon("admin001", "password");

        mockMvc.perform(get("/api/admin/menu")
                        .session((MockHttpSession) signon.getRequest().getSession(false)))
                .andExpect(status().isOk());
    }

    @Test
    void userSessionIsForbiddenFromAdminMenu() throws Exception {
        MvcResult signon = signon("user0001", "password");

        mockMvc.perform(get("/api/admin/menu")
                        .session((MockHttpSession) signon.getRequest().getSession(false)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/menu"))
                .andExpect(status().isForbidden());
    }

    private MvcResult signon(String userId, String password) throws Exception {
        return mockMvc.perform(post("/api/signon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SignonRequest(userId, password))))
                .andExpect(status().isOk())
                .andReturn();
    }
}
