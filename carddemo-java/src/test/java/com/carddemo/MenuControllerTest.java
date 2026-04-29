package com.carddemo;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void adminUserSeesAllMenuOptions() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("ADMIN001", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("ADMIN001")))
                .andExpect(jsonPath("$.userType", is("A")))
                .andExpect(jsonPath("$.menuOptions", hasSize(14)))
                .andExpect(jsonPath("$.menuOptions[0].label", is("User List")))
                .andExpect(jsonPath("$.menuOptions[0].cobolProgram", is("COUSR00C")))
                .andExpect(jsonPath("$.menuOptions[0].cobolTransaction", is("CU00")))
                .andExpect(jsonPath("$.menuOptions[3].label", is("User Delete")))
                .andExpect(jsonPath("$.menuOptions[4].label", is("Account View")))
                .andExpect(jsonPath("$.menuOptions[4].optionNumber", is(5)))
                .andExpect(jsonPath("$.menuOptions[13].label", is("Bill Payment")));
    }

    @Test
    void regularUserSeesOnlyRegularMenuOptions() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("USER0001", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is("USER0001")))
                .andExpect(jsonPath("$.userType", is("U")))
                .andExpect(jsonPath("$.menuOptions", hasSize(10)))
                .andExpect(jsonPath("$.menuOptions[0].label", is("Account View")))
                .andExpect(jsonPath("$.menuOptions[0].optionNumber", is(1)))
                .andExpect(jsonPath("$.menuOptions[0].cobolProgram", is("COACTVWC")))
                .andExpect(jsonPath("$.menuOptions[0].cobolTransaction", is("CAVW")))
                .andExpect(jsonPath("$.menuOptions[9].label", is("Bill Payment")));
    }

    @Test
    void unauthenticatedRequestReturns401() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void responseIncludesCobolTraceabilityMetadata() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("USER0001", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuOptions[0].cobolProgram").exists())
                .andExpect(jsonPath("$.menuOptions[0].cobolTransaction").exists())
                .andExpect(jsonPath("$.menuOptions[0].apiEndpoint").exists())
                .andExpect(jsonPath("$.menuOptions[0].httpMethod").exists())
                .andExpect(jsonPath("$.menuOptions[0].implemented").exists())
                .andExpect(jsonPath("$.menuOptions[0].implemented", is(false)));
    }

    @Test
    void adminMenuIncludesCorrectEndpoints() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("ADMIN001", "admin123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuOptions[0].apiEndpoint", is("/api/admin/users")))
                .andExpect(jsonPath("$.menuOptions[0].httpMethod", is("GET")))
                .andExpect(jsonPath("$.menuOptions[1].apiEndpoint", is("/api/admin/users")))
                .andExpect(jsonPath("$.menuOptions[1].httpMethod", is("POST")))
                .andExpect(jsonPath("$.menuOptions[2].apiEndpoint", is("/api/admin/users/{userId}")))
                .andExpect(jsonPath("$.menuOptions[2].httpMethod", is("PUT")))
                .andExpect(jsonPath("$.menuOptions[3].apiEndpoint", is("/api/admin/users/{userId}")))
                .andExpect(jsonPath("$.menuOptions[3].httpMethod", is("DELETE")));
    }

    @Test
    void invalidCredentialsReturns401() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("USER0001", "wrongpassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularMenuTransactionCodesMatchCobol() throws Exception {
        mockMvc.perform(get("/api/menu").with(httpBasic("USER0001", "user123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menuOptions[0].cobolTransaction", is("CAVW")))
                .andExpect(jsonPath("$.menuOptions[1].cobolTransaction", is("CAUP")))
                .andExpect(jsonPath("$.menuOptions[2].cobolTransaction", is("CCLI")))
                .andExpect(jsonPath("$.menuOptions[3].cobolTransaction", is("CCDL")))
                .andExpect(jsonPath("$.menuOptions[4].cobolTransaction", is("CCUP")))
                .andExpect(jsonPath("$.menuOptions[5].cobolTransaction", is("CT00")))
                .andExpect(jsonPath("$.menuOptions[6].cobolTransaction", is("CT01")))
                .andExpect(jsonPath("$.menuOptions[7].cobolTransaction", is("CT02")))
                .andExpect(jsonPath("$.menuOptions[8].cobolTransaction", is("CR00")))
                .andExpect(jsonPath("$.menuOptions[9].cobolTransaction", is("CB00")));
    }
}
