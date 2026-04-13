package com.carddemo.repository;

import com.carddemo.entity.CreditCard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CreditCardRepositoryTest {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @BeforeEach
    void setUp() {
        creditCardRepository.deleteAll();

        CreditCard card1 = new CreditCard();
        card1.setCardNum("4000123456789010");
        card1.setAcctId(1L);
        card1.setCvvCode(123);
        card1.setEmbossedName("John Doe");
        card1.setExpirationDate("2025-12-31");
        card1.setActiveStatus("Y");
        creditCardRepository.save(card1);

        CreditCard card2 = new CreditCard();
        card2.setCardNum("4000123456789020");
        card2.setAcctId(1L);
        card2.setCvvCode(456);
        card2.setEmbossedName("Jane Doe");
        card2.setExpirationDate("2026-06-30");
        card2.setActiveStatus("Y");
        creditCardRepository.save(card2);

        CreditCard card3 = new CreditCard();
        card3.setCardNum("4000123456789030");
        card3.setAcctId(2L);
        card3.setCvvCode(789);
        card3.setEmbossedName("Bob Smith");
        card3.setExpirationDate("2024-03-15");
        card3.setActiveStatus("N");
        creditCardRepository.save(card3);
    }

    @Test
    void save_andFindById_returnsCard() {
        Optional<CreditCard> found = creditCardRepository.findById("4000123456789010");

        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getEmbossedName());
        assertEquals(1L, found.get().getAcctId());
    }

    @Test
    void findByAcctId_returnsCardsForAccount() {
        List<CreditCard> cardsForAcct1 = creditCardRepository.findByAcctId(1L);
        List<CreditCard> cardsForAcct2 = creditCardRepository.findByAcctId(2L);

        assertEquals(2, cardsForAcct1.size());
        assertEquals(1, cardsForAcct2.size());
    }

    @Test
    void findByAcctId_noCards_returnsEmptyList() {
        List<CreditCard> cards = creditCardRepository.findByAcctId(999L);
        assertTrue(cards.isEmpty());
    }

    @Test
    void delete_removesCard() {
        creditCardRepository.deleteById("4000123456789010");
        assertFalse(creditCardRepository.findById("4000123456789010").isPresent());
    }

    @Test
    void update_modifiesCard() {
        CreditCard card = creditCardRepository.findById("4000123456789030").orElseThrow();
        card.setActiveStatus("Y");
        creditCardRepository.save(card);

        CreditCard updated = creditCardRepository.findById("4000123456789030").orElseThrow();
        assertEquals("Y", updated.getActiveStatus());
    }
}
