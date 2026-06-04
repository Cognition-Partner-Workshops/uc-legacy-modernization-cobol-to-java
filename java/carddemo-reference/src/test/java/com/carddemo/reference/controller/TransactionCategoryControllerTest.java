package com.carddemo.reference.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carddemo.reference.dto.TransactionCategoryDto;
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
 * MockMvc integration tests for {@link TransactionCategoryController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TransactionCategoryControllerTest {

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
    void listCategoriesForType() throws Exception {
        mockMvc.perform(get("/api/transaction-types/01/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].categoryCode", is("0001")));
    }

    @Test
    void listCategoriesForMissingTypeReturns404() throws Exception {
        mockMvc.perform(get("/api/transaction-types/ZZ/categories"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCategory() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "0009", "New category");
        mockMvc.perform(post("/api/transaction-types/02/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCode", is("02")))
                .andExpect(jsonPath("$.categoryCode", is("0009")));
    }

    @Test
    void createCategoryForMissingTypeReturns404() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "0001", "Orphan");
        mockMvc.perform(post("/api/transaction-types/ZZ/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createDuplicateCategoryReturns409() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "0001", "Dup");
        mockMvc.perform(post("/api/transaction-types/01/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void createCategoryInvalidCodeReturns400() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "1", "Bad code");
        mockMvc.perform(post("/api/transaction-types/01/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategoryDescription() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "0001", "Updated draft");
        mockMvc.perform(put("/api/transaction-types/01/categories/0001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated draft")));
    }

    @Test
    void updateMissingCategoryReturns404() throws Exception {
        TransactionCategoryDto dto = new TransactionCategoryDto(null, "9999", "Nope");
        mockMvc.perform(put("/api/transaction-types/01/categories/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory() throws Exception {
        mockMvc.perform(delete("/api/transaction-types/01/categories/0005"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/transaction-types/01/categories"))
                .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void deleteMissingCategoryReturns404() throws Exception {
        mockMvc.perform(delete("/api/transaction-types/01/categories/9999"))
                .andExpect(status().isNotFound());
    }
}
