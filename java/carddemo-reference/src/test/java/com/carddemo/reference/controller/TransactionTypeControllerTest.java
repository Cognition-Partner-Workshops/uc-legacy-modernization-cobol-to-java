package com.carddemo.reference.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.reference.dto.TransactionTypeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * MockMvc integration tests for {@link TransactionTypeController}, covering the
 * behaviour migrated from {@code COTRTLIC}, {@code COTRTUPC} and
 * {@code COBTUPDT}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TransactionTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void resetDatabase() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void listReturnsSeededTypes() throws Exception {
        mockMvc.perform(get("/api/transaction-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(greaterThanOrEqualTo(7))));
    }

    @Test
    void searchFiltersByDescription() throws Exception {
        mockMvc.perform(get("/api/transaction-types").param("search", "Payment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].typeCode", is("02")));
    }

    @Test
    void getSingleTypeIncludesCategories() throws Exception {
        mockMvc.perform(get("/api/transaction-types/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode", is("01")))
                .andExpect(jsonPath("$.description", is("Purchase")))
                .andExpect(jsonPath("$.categories", hasSize(5)));
    }

    @Test
    void getMissingTypeReturns404() throws Exception {
        mockMvc.perform(get("/api/transaction-types/ZZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createValidType() throws Exception {
        TransactionTypeDto dto = new TransactionTypeDto("09", "Promotional credit");
        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCode", is("09")));
    }

    @Test
    void createDuplicateTypeReturns409() throws Exception {
        TransactionTypeDto dto = new TransactionTypeDto("01", "Duplicate");
        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createInvalidTypeCodeReturns400() throws Exception {
        TransactionTypeDto dto = new TransactionTypeDto("ABC", "Too long code");
        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBlankDescriptionReturns400() throws Exception {
        TransactionTypeDto dto = new TransactionTypeDto("08", "  ");
        mockMvc.perform(post("/api/transaction-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateTypeDescription() throws Exception {
        TransactionTypeDto dto = new TransactionTypeDto("05", "Refund (updated)");
        mockMvc.perform(put("/api/transaction-types/05")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Refund (updated)")));
    }

    @Test
    void deleteTypeWithCategoriesReturns409() throws Exception {
        mockMvc.perform(delete("/api/transaction-types/01"))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteTypeWithoutCategoriesSucceeds() throws Exception {
        // Type 07 has one category; remove it first, then the type deletes cleanly.
        mockMvc.perform(delete("/api/transaction-types/07/categories/0001"))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/transaction-types/07"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/transaction-types/07"))
                .andExpect(status().isNotFound());
    }

    @Test
    void batchAppliesInsertUpdateDelete() throws Exception {
        // Remove type 05's only category so it can be deleted (ON DELETE RESTRICT).
        mockMvc.perform(delete("/api/transaction-types/05/categories/0001"))
                .andExpect(status().isNoContent());

        String json = "["
                + "{\"action\":\"INSERT\",\"typeCode\":\"10\",\"description\":\"Fee\"},"
                + "{\"action\":\"UPDATE\",\"typeCode\":\"02\",\"description\":\"Payment v2\"},"
                + "{\"action\":\"DELETE\",\"typeCode\":\"05\"}"
                + "]";
        mockMvc.perform(post("/api/transaction-types/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transaction-types/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Fee")));
        mockMvc.perform(get("/api/transaction-types/02"))
                .andExpect(jsonPath("$.description", is("Payment v2")));
        mockMvc.perform(get("/api/transaction-types/05"))
                .andExpect(status().isNotFound());
    }

    @Test
    void batchRollsBackOnFailure() throws Exception {
        // Second op fails (deleting type 01 which has categories); the INSERT of
        // type 11 must be rolled back, mirroring COBTUPDT all-or-nothing semantics.
        String json = "["
                + "{\"action\":\"INSERT\",\"typeCode\":\"11\",\"description\":\"Temp\"},"
                + "{\"action\":\"DELETE\",\"typeCode\":\"01\"}"
                + "]";
        mockMvc.perform(post("/api/transaction-types/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/transaction-types/11"))
                .andExpect(status().isNotFound());
    }
}
