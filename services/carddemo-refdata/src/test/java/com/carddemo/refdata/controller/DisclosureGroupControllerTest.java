package com.carddemo.refdata.controller;

import java.math.BigDecimal;

import com.carddemo.refdata.entity.DisclosureGroup;
import com.carddemo.refdata.repository.DisclosureGroupRepository;
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
class DisclosureGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DisclosureGroupRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void listGroups_empty() throws Exception {
        mockMvc.perform(get("/reference/disclosure-groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createAndGetGroup() throws Exception {
        DisclosureGroup dg = new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("12.50"));
        mockMvc.perform(post("/reference/disclosure-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dg)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountGroupId", is("GRP0000001")))
                .andExpect(jsonPath("$.transactionTypeCode", is("SA")))
                .andExpect(jsonPath("$.transactionCategoryCode", is(1001)))
                .andExpect(jsonPath("$.interestRate", is(12.50)));

        mockMvc.perform(get("/reference/disclosure-groups/GRP0000001/SA/1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestRate", is(12.50)));
    }

    @Test
    void createDuplicate_returns409() throws Exception {
        repository.save(new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("12.50")));
        DisclosureGroup dup = new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("15.00"));
        mockMvc.perform(post("/reference/disclosure-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dup)))
                .andExpect(status().isConflict());
    }

    @Test
    void getNotFound() throws Exception {
        mockMvc.perform(get("/reference/disclosure-groups/NOGROUP/ZZ/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateGroup() throws Exception {
        repository.save(new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("12.50")));
        DisclosureGroup update = new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("18.75"));
        mockMvc.perform(put("/reference/disclosure-groups/GRP0000001/SA/1001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interestRate", is(18.75)));
    }

    @Test
    void updateNotFound() throws Exception {
        DisclosureGroup update = new DisclosureGroup("NOGROUP", "ZZ", 0, new BigDecimal("1.00"));
        mockMvc.perform(put("/reference/disclosure-groups/NOGROUP/ZZ/0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup() throws Exception {
        repository.save(new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("12.50")));
        mockMvc.perform(delete("/reference/disclosure-groups/GRP0000001/SA/1001"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/reference/disclosure-groups/GRP0000001/SA/1001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotFound() throws Exception {
        mockMvc.perform(delete("/reference/disclosure-groups/NOGROUP/ZZ/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createInvalid_nullRate() throws Exception {
        String json = "{\"accountGroupId\":\"GRP0000001\",\"transactionTypeCode\":\"SA\","
                + "\"transactionCategoryCode\":1001}";
        mockMvc.perform(post("/reference/disclosure-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvalid_groupIdTooLong() throws Exception {
        DisclosureGroup dg = new DisclosureGroup("TOOLONGGROUP", "SA", 1001, new BigDecimal("12.50"));
        mockMvc.perform(post("/reference/disclosure-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dg)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createInvalid_rateTooManyDecimals() throws Exception {
        DisclosureGroup dg = new DisclosureGroup("GRP0000001", "SA", 1001, new BigDecimal("12.501"));
        mockMvc.perform(post("/reference/disclosure-groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dg)))
                .andExpect(status().isBadRequest());
    }
}
