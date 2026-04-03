package com.carddemo.batch.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardRecordTest {

    @Test
    void gettersAndSettersWork() {
        CardRecord rec = new CardRecord();
        rec.setCardNum("4111111111111111");
        rec.setCardAcctId(12345678901L);
        rec.setCardCvvCd(123);
        rec.setCardEmbossedName("JOHN DOE");
        rec.setCardExpirationDate("2025-12-31");
        rec.setCardActiveStatus("Y");

        assertEquals("4111111111111111", rec.getCardNum());
        assertEquals(12345678901L, rec.getCardAcctId());
        assertEquals(123, rec.getCardCvvCd());
        assertEquals("JOHN DOE", rec.getCardEmbossedName());
        assertEquals("2025-12-31", rec.getCardExpirationDate());
        assertEquals("Y", rec.getCardActiveStatus());
    }

    @Test
    void toStringContainsCardNum() {
        CardRecord rec = new CardRecord();
        rec.setCardNum("4222222222222222");
        assertTrue(rec.toString().contains("4222222222222222"));
    }
}
