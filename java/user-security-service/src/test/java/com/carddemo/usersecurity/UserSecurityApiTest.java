package com.carddemo.usersecurity;

import static org.hamcrest.Matchers.hasSize;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserSecurityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signonSucceedsForAdmin() throws Exception {
        mockMvc.perform(post("/api/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"ADMIN001\",\"password\":\"PASSWORD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value("ADMIN001"))
                .andExpect(jsonPath("$.user.userType").value("A"))
                .andExpect(jsonPath("$.admin").value(true))
                .andExpect(jsonPath("$.nextProgram").value("COADM01C"));
    }

    @Test
    void signonSucceedsForRegularUser() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"user0002\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.userId").value("USER0002"))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.nextProgram").value("COMEN01C"));
    }

    @Test
    void signonWithWrongPasswordReturns401() throws Exception {
        mockMvc.perform(post("/api/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"ADMIN001\",\"password\":\"NOPE\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Wrong Password. Try again ..."));
    }

    @Test
    void signonWithUnknownUserReturns404() throws Exception {
        mockMvc.perform(post("/api/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"GHOST001\",\"password\":\"PASSWORD\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User ID NOT found..."));
    }

    @Test
    void signonWithBlankUserIdReturns400() throws Exception {
        mockMvc.perform(post("/api/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"\",\"password\":\"PASSWORD\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please enter User ID ..."));
    }

    @Test
    void listReturnsTenSeededUsers() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.content[0].userId").value("ADMIN001"))
                .andExpect(jsonPath("$.content[0].firstName").value("MARGARET"));
    }

    @Test
    void listHonoursPagination() throws Exception {
        mockMvc.perform(get("/api/users").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].userId").value("USER0001"));
    }

    @Test
    void getUserReturnsSingleRecordAndNeverThePassword() throws Exception {
        mockMvc.perform(get("/api/users/ADMIN003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("ADMIN003"))
                .andExpect(jsonPath("$.lastName").value("WHITMORE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        mockMvc.perform(get("/api/users/GHOST001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User ID NOT found..."));
    }

    @Test
    void crudLifecycle() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"APITST1\",\"firstName\":\"API\",\"lastName\":\"TESTER\","
                                + "\"password\":\"PWD00001\",\"userType\":\"U\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("APITST1"));

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"APITST1\",\"firstName\":\"API\",\"lastName\":\"TESTER\","
                                + "\"password\":\"PWD00001\",\"userType\":\"U\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User ID already exist..."));

        mockMvc.perform(put("/api/users/APITST1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"UPDATED\",\"lastName\":\"TESTER\","
                                + "\"password\":\"PWD00002\",\"userType\":\"A\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UPDATED"))
                .andExpect(jsonPath("$.userType").value("A"));

        mockMvc.perform(post("/api/signon").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"APITST1\",\"password\":\"PWD00002\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(true));

        mockMvc.perform(delete("/api/users/APITST1")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/APITST1")).andExpect(status().isNotFound());
    }

    @Test
    void createRejectsFieldsLongerThanThePicClauses() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"TOOLONGUSERID\",\"firstName\":\"API\",\"lastName\":\"TESTER\","
                                + "\"password\":\"PWD00001\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User ID must be at most 8 characters"));
    }

    @Test
    void createRejectsEmptyFirstNameLikeCousr01c() throws Exception {
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"APITST2\",\"firstName\":\"\",\"lastName\":\"TESTER\","
                                + "\"password\":\"PWD00001\",\"userType\":\"U\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("First Name can NOT be empty..."));
    }

    @Test
    void updateOfUnknownUserReturns404() throws Exception {
        mockMvc.perform(put("/api/users/GHOST001").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\","
                                + "\"password\":\"PWD00001\",\"userType\":\"U\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOfUnknownUserReturns404() throws Exception {
        mockMvc.perform(delete("/api/users/GHOST001"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User ID NOT found..."));
    }
}
