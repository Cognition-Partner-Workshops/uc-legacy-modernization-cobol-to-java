package com.carddemo.card.controller;

import com.carddemo.card.dto.UpdateCardRequest;
import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
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
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CardControllerIntegrationTest {

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
        registry.add("spring.flyway.schemas", () -> "card");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "card");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private String authToken;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        cardXrefRepository.deleteAll();
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
    void listCardsByAccount_returnsPaginated() throws Exception {
        Card card = Card.builder()
                .cardNum("4111111111111111")
                .acctId("00000000001")
                .activeStatus('Y')
                .embossedName("JOHN DOE")
                .expirationDate("2025-12-01")
                .cvvCd(123)
                .version(0L)
                .build();
        cardRepository.save(card);

        mockMvc.perform(get("/api/cards")
                        .param("acctId", "00000000001")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].cardNum").value("4111111111111111"));
    }

    @Test
    void getCardDetail_returnsCard() throws Exception {
        Card card = Card.builder()
                .cardNum("4222222222222222")
                .acctId("00000000001")
                .activeStatus('Y')
                .embossedName("JANE DOE")
                .expirationDate("2026-06-01")
                .cvvCd(456)
                .version(0L)
                .build();
        cardRepository.save(card);

        mockMvc.perform(get("/api/cards/4222222222222222")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("JANE DOE"))
                .andExpect(jsonPath("$.cvvCd").value(456));
    }

    @Test
    void updateCard_validData_succeeds() throws Exception {
        Card card = Card.builder()
                .cardNum("4333333333333333")
                .acctId("00000000001")
                .activeStatus('Y')
                .embossedName("JOHN DOE")
                .expirationDate("2025-12-01")
                .version(0L)
                .build();
        cardRepository.save(card);

        UpdateCardRequest request = UpdateCardRequest.builder()
                .embossedName("UPDATED NAME")
                .activeStatus('N')
                .expirationMonth(3)
                .expirationYear(2027)
                .version(0L)
                .build();

        mockMvc.perform(put("/api/cards/4333333333333333")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.embossedName").value("UPDATED NAME"))
                .andExpect(jsonPath("$.activeStatus").value("N"));
    }

    @Test
    void updateCard_invalidName_returns400() throws Exception {
        Card card = Card.builder()
                .cardNum("4444444444444444")
                .acctId("00000000001")
                .activeStatus('Y')
                .embossedName("JOHN DOE")
                .version(0L)
                .build();
        cardRepository.save(card);

        UpdateCardRequest request = UpdateCardRequest.builder()
                .embossedName("JOHN123")
                .version(0L)
                .build();

        mockMvc.perform(put("/api/cards/4444444444444444")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void xrefLookup_returnsCorrectMapping() throws Exception {
        CardXref xref = CardXref.builder()
                .cardNum("4555555555555555")
                .custId("000000001")
                .acctId("00000000001")
                .build();
        cardXrefRepository.save(xref);

        mockMvc.perform(get("/api/cards/xref/4555555555555555")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.custId").value("000000001"))
                .andExpect(jsonPath("$.acctId").value("00000000001"));
    }
}
