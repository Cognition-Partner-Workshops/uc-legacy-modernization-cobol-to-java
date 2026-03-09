package com.carddemo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Card entity.
 * Validates field mapping from CVACT02Y.cpy CARD-RECORD layout.
 */
class CardTest {

    @Test
    void testCardCreation() {
        Card card = new Card();
        card.setCardNum("4111111111111111");
        card.setCardAcctId(12345678901L);
        card.setCardCvvCd(123);
        card.setCardEmbossedName("JOHN DOE");
        card.setCardExpirationDate("2028-12-31");
        card.setCardActiveStatus("Y");

        assertEquals("4111111111111111", card.getCardNum());
        assertEquals(12345678901L, card.getCardAcctId());
        assertEquals(123, card.getCardCvvCd());
        assertEquals("JOHN DOE", card.getCardEmbossedName());
        assertEquals("2028-12-31", card.getCardExpirationDate());
        assertEquals("Y", card.getCardActiveStatus());
    }

    @Test
    void testCardNumIs16Chars() {
        Card card = new Card();
        card.setCardNum("1234567890123456");
        assertEquals(16, card.getCardNum().length());
    }
}
