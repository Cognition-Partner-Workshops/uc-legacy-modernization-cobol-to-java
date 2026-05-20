package com.carddemo.customer.controller;

import com.carddemo.customer.dto.CreateCustomerRequest;
import com.carddemo.customer.dto.UpdateCustomerRequest;
import com.carddemo.customer.model.Customer;
import com.carddemo.customer.repository.CustomerRepository;
import com.carddemo.customer.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CustomerControllerIntegrationTest {

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
        registry.add("spring.flyway.schemas", () -> "customer");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "customer");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String authToken;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        authToken = Jwts.builder()
                .subject("ADMIN001")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }

    @Test
    void createAndRetrieveCustomer() throws Exception {
        CreateCustomerRequest request = CreateCustomerRequest.builder()
                .custId("000000001")
                .firstName("John")
                .lastName("Doe")
                .ssn("123456789")
                .dob(LocalDate.of(1990, 5, 15))
                .addrStateCd("NY")
                .addrCountryCd("USA")
                .ficoCreditScore(750)
                .build();

        mockMvc.perform(post("/api/customers")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.custId").value("000000001"));

        mockMvc.perform(get("/api/customers/000000001")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.ssn").value("***-**-6789"));
    }

    @Test
    void ssnIsMaskedInGetResponse() throws Exception {
        Customer customer = Customer.builder()
                .custId("000000002")
                .firstName("Jane")
                .lastName("Smith")
                .ssn("987654321")
                .build();
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/000000002")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ssn").value("***-**-4321"));
    }

    @Test
    void searchBySsn_returnsCorrectCustomer() throws Exception {
        Customer customer = Customer.builder()
                .custId("000000003")
                .firstName("Bob")
                .lastName("Jones")
                .ssn("111223333")
                .build();
        customerRepository.save(customer);

        mockMvc.perform(get("/api/customers/search")
                        .param("ssn", "111223333")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custId").value("000000003"));
    }

    @Test
    void updateCustomer_works() throws Exception {
        Customer customer = Customer.builder()
                .custId("000000004")
                .firstName("Alice")
                .lastName("Brown")
                .build();
        customerRepository.save(customer);

        UpdateCustomerRequest updateReq = UpdateCustomerRequest.builder()
                .firstName("Updated")
                .build();

        mockMvc.perform(put("/api/customers/000000004")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Updated"));
    }

    @Test
    void paginatedListing_works() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Customer customer = Customer.builder()
                    .custId(String.format("%09d", i + 10))
                    .firstName("First" + i)
                    .lastName("Last" + i)
                    .build();
            customerRepository.save(customer);
        }

        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "3")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isUnauthorized());
    }
}
