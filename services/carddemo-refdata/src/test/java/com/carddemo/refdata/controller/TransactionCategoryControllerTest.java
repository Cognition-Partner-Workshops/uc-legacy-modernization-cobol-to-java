package com.carddemo.refdata.controller;

import com.carddemo.refdata.entity.TransactionCategory;
import com.carddemo.refdata.repository.TransactionCategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionCategoryRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void listCategories_empty() throws Exception {
        mockMvc.perform(get("/reference/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAndGetCategory() throws Exception {
        TransactionCategory cat = new TransactionCategory("SA", 1001, "Online Sale");
        mockMvc.perform(post("/reference/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCode", is("SA")))
                .andExpect(jsonPath("$.categoryCode", is(1001)))
                .andExpect(jsonPath("$.description", is("Online Sale")));

        mockMvc.perform(get("/reference/categories/SA/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Online Sale")));
    }

    @Test
    void createDuplicate_returns409() throws Exception {
        repository.save(new TransactionCategory("SA", 1001, "Online Sale"));
        TransactionCategory dup = new TransactionCategory("SA", 1001, "Duplicate");
        mockMvc.perform(post("/reference/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    void getNotFound() throws Exception {
        mockMvc.perform(get("/reference/categories/ZZ/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory() throws Exception {
        repository.save(new TransactionCategory("SA", 1001, "Online Sale"));
        TransactionCategory update = new TransactionCategory("SA", 1001, "Updated Sale");
        mockMvc.perform(put("/reference/categories/SA/1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated Sale")));
    }

    @Test
    void updateNotFound() throws Exception {
        TransactionCategory update = new TransactionCategory("ZZ", 9999, "Nope");
        mockMvc.perform(put("/reference/categories/ZZ/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCategory() throws Exception {
        repository.save(new TransactionCategory("SA", 1001, "Online Sale"));
        mockMvc.perform(delete("/reference/categories/SA/1001"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/reference/categories/SA/1001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete("/reference/categories/ZZ/9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInvalid_codeOverflow() throws Exception {
        TransactionCategory cat = new TransactionCategory("SA", 10000, "Overflow");
        mockMvc.perform(post("/reference/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvalid_blankTypeCode() throws Exception {
        TransactionCategory cat = new TransactionCategory("", 1001, "No type");
        mockMvc.perform(post("/reference/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cat)))
                .andExpect(status().isBadRequest());
    }
}
