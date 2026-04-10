package com.carddemo.card.controller;

import com.carddemo.card.model.Card;
import com.carddemo.card.model.CardXref;
import com.carddemo.card.repository.CardRepository;
import com.carddemo.card.repository.CardXrefRepository;
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
 * Boundary and risk-based tests for Card Service (CRUD operations, xref lookup).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @BeforeEach
    void setUp() {
        cardXrefRepository.deleteAll();
        cardRepository.deleteAll();

        Card card = new Card();
        card.setCardNum("1234567890123456");
        card.setCardAcctId(1L);
        card.setCardCvvCd((short) 123);
        card.setCardEmbossedName("TEST USER");
        card.setCardExpiraionDate("2025-12-31");
        card.setCardActiveStatus("Y");
        cardRepository.save(card);

        Card card2 = new Card();
        card2.setCardNum("9876543210987654");
        card2.setCardAcctId(1L);
        card2.setCardActiveStatus("N");
        cardRepository.save(card2);

        CardXref xref = new CardXref();
        xref.setXrefCardNum("1234567890123456");
        xref.setXrefCustId(1L);
        xref.setXrefAcctId(1L);
        cardXrefRepository.save(xref);
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("List all cards returns 200")
    void testListCards() throws Exception {
        mockMvc.perform(get("/api/cards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get card by valid card number returns 200")
    void testGetCardByNumber() throws Exception {
        mockMvc.perform(get("/api/cards/1234567890123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardNum").value("1234567890123456"))
            .andExpect(jsonPath("$.cardActiveStatus").value("Y"));
    }

    @Test
    @DisplayName("Get cards by account ID returns matching cards")
    void testGetCardsByAccount() throws Exception {
        mockMvc.perform(get("/api/cards/account/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get xref by card number returns 200")
    void testGetXref() throws Exception {
        mockMvc.perform(get("/api/cards/xref/1234567890123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.xrefAcctId").value(1));
    }

    // ===== BOUNDARY TESTS - NON-EXISTENT CARD =====

    @Test
    @DisplayName("Get non-existent card returns 404")
    void testGetNonExistentCard() throws Exception {
        mockMvc.perform(get("/api/cards/0000000000000000"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get xref for non-existent card returns 404")
    void testGetXrefNonExistent() throws Exception {
        mockMvc.perform(get("/api/cards/xref/0000000000000000"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get cards by non-existent account returns empty list")
    void testGetCardsByNonExistentAccount() throws Exception {
        mockMvc.perform(get("/api/cards/account/99999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ===== BOUNDARY TESTS - CREATE/UPDATE =====

    @Test
    @DisplayName("Create new card returns 200")
    void testCreateCard() throws Exception {
        mockMvc.perform(post("/api/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardNum\":\"1111222233334444\",\"cardAcctId\":2,\"cardActiveStatus\":\"Y\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardNum").value("1111222233334444"));
    }

    @Test
    @DisplayName("Update existing card returns 200")
    void testUpdateCard() throws Exception {
        mockMvc.perform(put("/api/cards/1234567890123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardNum\":\"1234567890123456\",\"cardAcctId\":1,\"cardActiveStatus\":\"N\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardActiveStatus").value("N"));
    }

    @Test
    @DisplayName("Update non-existent card returns 404")
    void testUpdateNonExistentCard() throws Exception {
        mockMvc.perform(put("/api/cards/0000000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"cardNum\":\"0000000000000000\",\"cardAcctId\":1,\"cardActiveStatus\":\"Y\"}"))
            .andExpect(status().isNotFound());
    }

    // ===== RISK-BASED TESTS - INACTIVE CARD =====

    @Test
    @DisplayName("Inactive card is still retrievable")
    void testInactiveCardRetrievable() throws Exception {
        mockMvc.perform(get("/api/cards/9876543210987654"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cardActiveStatus").value("N"));
    }
}
