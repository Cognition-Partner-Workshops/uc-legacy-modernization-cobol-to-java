package com.carddemo.viewer;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "carddemo.statements.dir=src/test/resources/fixtures")
class StatementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexListsStatements() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(containsString("Ava Morgan")))
                .andExpect(content().string(containsString("Noah Lee")));
    }

    @Test
    void detailRendersCustomerAndTransactions() throws Exception {
        mockMvc.perform(get("/statements/00000000101"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(content().string(containsString("Ava Morgan")))
                .andExpect(content().string(containsString("Test purchase")))
                .andExpect(content().string(containsString("-$123.45")))
                .andExpect(content().string(containsString("Testville NY USA 10001")));
    }

    @Test
    void unknownAccountReturnsNotFoundPage() throws Exception {
        mockMvc.perform(get("/statements/99999999999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(content().string(containsString("Statement not found")));
    }

    @Test
    void jsonCollectionEndpointReturnsContractData() throws Exception {
        mockMvc.perform(get("/api/statements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].accountId").value("00000000101"))
                .andExpect(jsonPath("$[0].totalAmount").value(-123.45))
                .andExpect(jsonPath("$[0].currentBalance").value(100.00));
    }

    @Test
    void jsonDetailEndpointReturnsContractData() throws Exception {
        mockMvc.perform(get("/api/statements/00000000202"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.firstName").value("Noah"))
                .andExpect(jsonPath("$.transactions").isEmpty())
                .andExpect(jsonPath("$.totalAmount").value(0.00));
    }

    @Test
    void unknownJsonAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/statements/99999999999")).andExpect(status().isNotFound());
    }
}
