package com.cognition.usersecurity.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void signonSuccessReturnsReadableTypeAndCode() throws Exception {
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"ADMIN001\",\"password\":\"PASSWORDA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.userTypeCode").value("A"));
    }

    @Test
    void signonPreservesCaseSensitivePassword() throws Exception {
        createUser("CASE0001", "lowerpass", "U");
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"CASE0001\",\"password\":\"LOWERPASS\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"CASE0001\",\"password\":\"lowerpass\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void signonReturnsNotFoundForUnknownUser() throws Exception {
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"MISSING\",\"password\":\"PASSWORDA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void signonReturnsUnauthorizedForWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"ADMIN001\",\"password\":\"WRONG\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listReturnsTenSeededUsersByDefault() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content[0].userType").value("ADMIN"))
                .andExpect(jsonPath("$.content[0].userTypeCode").value("A"));
    }

    @Test
    void getReturnsUser() throws Exception {
        mockMvc.perform(get("/api/users/admin001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN001"));
    }

    @Test
    void getReturnsNotFoundForMissingUser() throws Exception {
        mockMvc.perform(get("/api/users/NOPE")).andExpect(status().isNotFound());
    }

    @Test
    void createReturnsCreatedUser() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload("CREATE01", "PASSWORD", "U")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("CREATE01"))
                .andExpect(jsonPath("$.userType").value("USER"))
                .andExpect(jsonPath("$.userTypeCode").value("U"));
    }

    @Test
    void createReturnsConflictForDuplicateId() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload("DUPLIC01", "PASSWORD", "U")))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload("duplic01", "PASSWORD", "U")))
                .andExpect(status().isConflict());
    }

    @Test
    void createReturnsBadRequestForValidationFailure() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"User\",\"userId\":\"BAD\",\"password\":\"PASSWORD\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateReturnsUpdatedUser() throws Exception {
        createUser("UPDATE01", "PASSWORD", "U");
        mockMvc.perform(put("/api/users/update01").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Updated\",\"lastName\":\"User\",\"password\":\"PASSWORD\",\"userType\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"))
                .andExpect(jsonPath("$.userType").value("ADMIN"))
                .andExpect(jsonPath("$.userTypeCode").value("A"));
    }

    @Test
    void updateReturnsNotFoundForMissingUser() throws Exception {
        mockMvc.perform(put("/api/users/MISSING").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Updated\",\"lastName\":\"User\",\"password\":\"PASSWORD\",\"userType\":\"U\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReturnsBadRequestForValidationFailure() throws Exception {
        mockMvc.perform(put("/api/users/ADMIN001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"\",\"lastName\":\"User\",\"password\":\"PASSWORD\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteRemovesUser() throws Exception {
        createUser("DELETE01", "PASSWORD", "U");
        mockMvc.perform(delete("/api/users/delete01")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/DELETE01")).andExpect(status().isNotFound());
    }

    @Test
    void deleteReturnsNotFoundForMissingUser() throws Exception {
        mockMvc.perform(delete("/api/users/MISSING")).andExpect(status().isNotFound());
    }

    private void createUser(String id, String password, String type) throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload(id, password, type)))
                .andExpect(status().isCreated());
    }

    private String createPayload(String id, String password, String type) {
        return String.format("{\"firstName\":\"Test\",\"lastName\":\"User\",\"userId\":\"%s\",\"password\":\"%s\",\"userType\":\"%s\"}",
                id, password, type);
    }
}
