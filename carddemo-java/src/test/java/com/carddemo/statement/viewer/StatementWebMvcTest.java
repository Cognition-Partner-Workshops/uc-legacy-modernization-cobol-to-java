package com.carddemo.statement.viewer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({StatementController.class, StatementViewController.class})
class StatementWebMvcTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    StatementRepository repository;

    private final Statement statement = new Statement(
            "00000000011",
            new Customer("John", "Doe", List.of("123 Main Street", "Apt 4B")),
            "****-****-****-1234",
            List.of(new Transaction("TX1", "PURCHASE", new BigDecimal("-12.50"))),
            new BigDecimal("-12.50"));

    @Test
    void rendersIndexPage() throws Exception {
        when(repository.findAll()).thenReturn(List.of(statement));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(content().string(Matchers.containsString("00000000011")));
    }

    @Test
    void rendersDetailPage() throws Exception {
        when(repository.findByAccountId("00000000011")).thenReturn(Optional.of(statement));

        mockMvc.perform(get("/statements/00000000011"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail"))
                .andExpect(content().string(Matchers.containsString("John Doe")));
    }

    @Test
    void servesApiListAndDetail() throws Exception {
        when(repository.findAll()).thenReturn(List.of(statement));
        when(repository.findByAccountId("00000000011")).thenReturn(Optional.of(statement));

        mockMvc.perform(get("/api/statements"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().string(Matchers.containsString("\"accountId\":\"00000000011\"")));
        mockMvc.perform(get("/api/statements/00000000011"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("\"totalAmount\":-12.50")));
    }

    @Test
    void returnsNotFoundForUnknownHtmlAndApiAccounts() throws Exception {
        when(repository.findByAccountId(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/statements/99999999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/statements/99999999999")).andExpect(status().isNotFound());
    }
}
