package com.carddemo.controller;

import com.carddemo.config.SecurityConfig;
import com.carddemo.entity.CreditCard;
import com.carddemo.repository.UserSecurityRepository;
import com.carddemo.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserSecurityRepository userSecurityRepository;

    private CreditCard createTestCard() {
        CreditCard card = new CreditCard();
        card.setCardNum("4000123456789010");
        card.setAcctId(1L);
        card.setCvvCode(123);
        card.setEmbossedName("John Doe");
        card.setExpirationDate("2025-12-31");
        card.setActiveStatus("Y");
        return card;
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void listCards_returnsCardList() throws Exception {
        CreditCard card = createTestCard();
        when(cardService.listCards(eq(1L), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/cards").param("acctId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-list"))
                .andExpect(model().attributeExists("cards"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void searchCards_withParams_returnsResults() throws Exception {
        CreditCard card = createTestCard();
        when(cardService.searchCards(eq("4000123456789010"), isNull()))
                .thenReturn(List.of(card));

        mockMvc.perform(get("/cards/search").param("cardNum", "4000123456789010"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-search"))
                .andExpect(model().attributeExists("cards"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void updateCard_redirectsToCardView() throws Exception {
        CreditCard card = createTestCard();
        when(cardService.updateCard(eq("4000123456789010"), any(CreditCard.class)))
                .thenReturn(card);

        mockMvc.perform(post("/cards/4000123456789010")
                        .with(csrf())
                        .param("activeStatus", "N")
                        .param("expirationDate", "2027-12-31"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/4000123456789010"));
    }

    @Test
    @WithMockUser(username = "USER0001", roles = {"USER"})
    void viewCard_returnsCardSearch() throws Exception {
        CreditCard card = createTestCard();
        when(cardService.getCardDetail("4000123456789010")).thenReturn(card);

        mockMvc.perform(get("/cards/4000123456789010"))
                .andExpect(status().isOk())
                .andExpect(view().name("card-search"))
                .andExpect(model().attributeExists("card"));
    }
}
