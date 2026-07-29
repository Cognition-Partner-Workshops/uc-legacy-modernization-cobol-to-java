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

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    @Autowired private MockMvc mockMvc;

    @Test void signonSuccessReturnsType() throws Exception {
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"PASSWORDA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.userType").value("A"));
    }
    @Test void signonNotFoundAndWrongPassword() throws Exception {
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"MISSING\",\"password\":\"PASSWORDA\"}")).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/auth/signon").contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"ADMIN001\",\"password\":\"WRONG\"}")).andExpect(status().isUnauthorized());
    }
    @Test void listIsSeededAndGetMissingIs404() throws Exception {
        mockMvc.perform(get("/api/users")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(10));
        mockMvc.perform(get("/api/users/NOPE")).andExpect(status().isNotFound());
    }
    @Test void crudCreateUpdateDeleteAndValidation() throws Exception {
        String create = "{\"firstName\":\"Test\",\"lastName\":\"User\",\"userId\":\"TEST0001\",\"password\":\"PASSWORD\",\"userType\":\"U\"}";
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").value("TEST0001"));
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON).content(create)).andExpect(status().isConflict());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"User\",\"userId\":\"BAD\",\"password\":\"PASSWORD\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/users/TEST0001").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Updated\",\"lastName\":\"User\",\"password\":\"PASSWORD\",\"userType\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Updated"));
        mockMvc.perform(put("/api/users/MISSING").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Updated\",\"lastName\":\"User\",\"password\":\"PASSWORD\",\"userType\":\"U\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/users/TEST0001")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/users/TEST0001")).andExpect(status().isNotFound());
    }
}
