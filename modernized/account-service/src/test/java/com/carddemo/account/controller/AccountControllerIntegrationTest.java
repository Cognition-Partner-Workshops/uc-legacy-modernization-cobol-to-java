package com.carddemo.account.controller;

import com.carddemo.account.dto.BalanceAdjustmentRequest;
import com.carddemo.account.dto.UpdateAccountRequest;
import com.carddemo.account.model.Account;
import com.carddemo.account.repository.AccountRepository;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AccountControllerIntegrationTest {

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
        registry.add("spring.flyway.schemas", () -> "account");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "account");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String authToken;

    @BeforeEach
    void setUp() {
        accountRepository.deleteAll();
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
    void getAccount_returnsCorrectData() throws Exception {
        Account account = Account.builder()
                .acctId("00000000001")
                .activeStatus('Y')
                .currBal(new BigDecimal("1500.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .openDate("2020-01-15")
                .expirationDate("2025-12-31")
                .addrZip("10001")
                .groupId("GROUP001")
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .version(0L)
                .build();
        accountRepository.save(account);

        mockMvc.perform(get("/api/accounts/00000000001")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.acctId").value("00000000001"))
                .andExpect(jsonPath("$.activeStatus").value("Y"))
                .andExpect(jsonPath("$.currBal").value(1500.00))
                .andExpect(jsonPath("$.creditLimit").value(5000.00));
    }

    @Test
    void updateAccount_verifiesAllFieldsUpdated() throws Exception {
        Account account = Account.builder()
                .acctId("00000000002")
                .activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .currBal(BigDecimal.ZERO)
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .openDate("2020-01-01")
                .version(0L)
                .build();
        accountRepository.save(account);

        UpdateAccountRequest request = UpdateAccountRequest.builder()
                .activeStatus('N')
                .creditLimit(new BigDecimal("8000.00"))
                .cashCreditLimit(new BigDecimal("2000.00"))
                .openDate("2021-06-15")
                .expirationDate("2026-12-31")
                .groupId("NEWGRP")
                .addrZip("90210")
                .version(0L)
                .build();

        mockMvc.perform(put("/api/accounts/00000000002")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeStatus").value("N"))
                .andExpect(jsonPath("$.creditLimit").value(8000.00))
                .andExpect(jsonPath("$.cashCreditLimit").value(2000.00))
                .andExpect(jsonPath("$.groupId").value("NEWGRP"));
    }

    @Test
    void updateAccount_staleVersion_returns409() throws Exception {
        Account account = Account.builder()
                .acctId("00000000003")
                .activeStatus('Y')
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .currBal(BigDecimal.ZERO)
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .version(0L)
                .build();
        accountRepository.save(account);

        UpdateAccountRequest firstUpdate = UpdateAccountRequest.builder()
                .activeStatus('N')
                .version(0L)
                .build();

        mockMvc.perform(put("/api/accounts/00000000003")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUpdate)))
                .andExpect(status().isOk());

        UpdateAccountRequest staleUpdate = UpdateAccountRequest.builder()
                .activeStatus('Y')
                .version(0L)
                .build();

        mockMvc.perform(put("/api/accounts/00000000003")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staleUpdate)))
                .andExpect(status().isConflict());
    }

    @Test
    void balanceAdjustment_preservesPrecision() throws Exception {
        Account account = Account.builder()
                .acctId("00000000004")
                .activeStatus('Y')
                .currBal(new BigDecimal("1000.50"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(BigDecimal.ZERO)
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .version(0L)
                .build();
        accountRepository.save(account);

        BalanceAdjustmentRequest request = BalanceAdjustmentRequest.builder()
                .amount(new BigDecimal("0.01"))
                .description("Penny test")
                .build();

        mockMvc.perform(patch("/api/accounts/00000000004/balance")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currBal").value(1000.51));
    }

    @Test
    void unauthenticatedRequest_returns401() throws Exception {
        mockMvc.perform(get("/api/accounts/00000000001"))
                .andExpect(status().isUnauthorized());
    }
}
