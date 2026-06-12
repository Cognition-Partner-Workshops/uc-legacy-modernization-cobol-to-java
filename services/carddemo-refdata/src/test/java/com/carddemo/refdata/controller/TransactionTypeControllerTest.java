package com.carddemo.refdata.controller;

import com.carddemo.refdata.entity.TransactionType;
import com.carddemo.refdata.repository.TransactionTypeRepository;
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
class TransactionTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionTypeRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void listTypes_empty() throws Exception {
        mockMvc.perform(get("/reference/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAndGetType() throws Exception {
        TransactionType tt = new TransactionType("SA", "Sale");
        mockMvc.perform(post("/reference/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.typeCode", is("SA")))
                .andExpect(jsonPath("$.description", is("Sale")));

        mockMvc.perform(get("/reference/types/SA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeCode", is("SA")));
    }

    @Test
    void createDuplicate_returns409() throws Exception {
        repository.save(new TransactionType("SA", "Sale"));
        TransactionType dup = new TransactionType("SA", "Sale Again");
        mockMvc.perform(post("/reference/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    void getNotFound() throws Exception {
        mockMvc.perform(get("/reference/types/ZZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateType() throws Exception {
        repository.save(new TransactionType("SA", "Sale"));
        TransactionType update = new TransactionType("SA", "Updated Sale");
        mockMvc.perform(put("/reference/types/SA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description", is("Updated Sale")));
    }

    @Test
    void updateNotFound() throws Exception {
        TransactionType update = new TransactionType("ZZ", "Nope");
        mockMvc.perform(put("/reference/types/ZZ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteType() throws Exception {
        repository.save(new TransactionType("SA", "Sale"));
        mockMvc.perform(delete("/reference/types/SA"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/reference/types/SA"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete("/reference/types/ZZ"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInvalid_blankCode() throws Exception {
        TransactionType tt = new TransactionType("", "No Code");
        mockMvc.perform(post("/reference/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tt)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvalid_codeToLong() throws Exception {
        TransactionType tt = new TransactionType("ABC", "Too long code");
        mockMvc.perform(post("/reference/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tt)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listTypes_returnsAll() throws Exception {
        repository.save(new TransactionType("SA", "Sale"));
        repository.save(new TransactionType("RE", "Return"));
        mockMvc.perform(get("/reference/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
}
