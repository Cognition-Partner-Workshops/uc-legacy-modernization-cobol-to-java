package com.carddemo.customer.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listCustomers_returnsPaginatedResults() throws Exception {
        mockMvc.perform(get("/customers").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(3)));
    }

    @Test
    void getCustomer_existingId_returnsCustomer() throws Exception {
        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Smith")))
                .andExpect(jsonPath("$.ssnMasked", is("***-**-6789")));
    }

    @Test
    void getCustomer_nonExistingId_returns404() throws Exception {
        mockMvc.perform(get("/customers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCustomerAccounts_existingCustomer_returnsLinkedAccounts() throws Exception {
        mockMvc.perform(get("/customers/1/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountId", is(10000000001L)))
                .andExpect(jsonPath("$[0].cardNumber", is("4111111111111111")));
    }

    @Test
    void getCustomerAccounts_nonExistingCustomer_returns404() throws Exception {
        mockMvc.perform(get("/customers/999/accounts"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchCustomers_byLastName_returnsMatches() throws Exception {
        mockMvc.perform(get("/customers/search").param("lastName", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].lastName", is("Smith")));
    }

    @Test
    void searchCustomers_byZip_returnsMatches() throws Exception {
        mockMvc.perform(get("/customers/search").param("zip", "90210"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].lastName", is("Doe")));
    }

    @Test
    void searchCustomers_byLastNameAndZip_returnsIntersection() throws Exception {
        mockMvc.perform(get("/customers/search")
                        .param("lastName", "Johnson")
                        .param("zip", "75001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].firstName", is("Robert")));
    }

    @Test
    void searchCustomers_noMatch_returnsEmpty() throws Exception {
        mockMvc.perform(get("/customers/search").param("lastName", "Nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void getCustomer_ssnIsMasked_neverExposesFullSsn() throws Exception {
        mockMvc.perform(get("/customers/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssnMasked", is("***-**-4321")))
                .andExpect(jsonPath("$.ssn").doesNotExist());
    }
}
