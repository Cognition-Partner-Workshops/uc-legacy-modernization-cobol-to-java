package com.carddemo.customer.controller;

import com.carddemo.customer.model.Customer;
import com.carddemo.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for Customer Service (CRUD operations).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        Customer c1 = new Customer();
        c1.setCustId(1L);
        c1.setCustFirstName("John");
        c1.setCustLastName("Doe");
        c1.setCustAddrLine1("123 Main St");
        c1.setCustAddrStateCd("NY");
        c1.setCustAddrZip("10001");
        c1.setCustSsn("123456789");
        c1.setCustFicoCreditScore((short) 750);
        customerRepository.save(c1);

        Customer c2 = new Customer();
        c2.setCustId(2L);
        c2.setCustFirstName("Jane");
        c2.setCustLastName("Smith");
        c2.setCustFicoCreditScore((short) 650);
        customerRepository.save(c2);
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("List all customers returns 200")
    void testListCustomers() throws Exception {
        mockMvc.perform(get("/api/customers"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get customer by valid ID returns 200")
    void testGetCustomerById() throws Exception {
        mockMvc.perform(get("/api/customers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custId").value(1))
            .andExpect(jsonPath("$.custFirstName").value("John"));
    }

    @Test
    @DisplayName("Create new customer returns 200")
    void testCreateCustomer() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":99,\"custFirstName\":\"Test\",\"custLastName\":\"User\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custId").value(99));
    }

    // ===== BOUNDARY TESTS - NON-EXISTENT CUSTOMER =====

    @Test
    @DisplayName("Get non-existent customer returns 404")
    void testGetNonExistentCustomer() throws Exception {
        mockMvc.perform(get("/api/customers/99999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get customer with ID 0 returns 404")
    void testGetCustomerIdZero() throws Exception {
        mockMvc.perform(get("/api/customers/0"))
            .andExpect(status().isNotFound());
    }

    // ===== BOUNDARY TESTS - UPDATE =====

    @Test
    @DisplayName("Update existing customer returns 200")
    void testUpdateCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":1,\"custFirstName\":\"Updated\",\"custLastName\":\"Name\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custFirstName").value("Updated"));
    }

    @Test
    @DisplayName("Update non-existent customer returns 404")
    void testUpdateNonExistentCustomer() throws Exception {
        mockMvc.perform(put("/api/customers/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":99999,\"custFirstName\":\"Test\",\"custLastName\":\"User\"}"))
            .andExpect(status().isNotFound());
    }

    // ===== BOUNDARY TESTS - DELETE =====

    @Test
    @DisplayName("Delete existing customer returns 204")
    void testDeleteCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/2"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete non-existent customer returns 404")
    void testDeleteNonExistentCustomer() throws Exception {
        mockMvc.perform(delete("/api/customers/99999"))
            .andExpect(status().isNotFound());
    }

    // ===== BOUNDARY TESTS - SPECIAL VALUES =====

    @Test
    @DisplayName("Create customer with minimum data")
    void testCreateCustomerMinData() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":100}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custId").value(100));
    }

    @Test
    @DisplayName("Create customer with max FICO score")
    void testCreateCustomerMaxFico() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":101,\"custFirstName\":\"Max\",\"custLastName\":\"Score\",\"custFicoCreditScore\":850}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custFicoCreditScore").value(850));
    }

    @Test
    @DisplayName("Create customer with zero FICO score")
    void testCreateCustomerZeroFico() throws Exception {
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"custId\":102,\"custFirstName\":\"Zero\",\"custLastName\":\"Score\",\"custFicoCreditScore\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.custFicoCreditScore").value(0));
    }
}
