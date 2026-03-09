package com.carddemo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TranCatBalanceId composite key.
 * Validates equals/hashCode for JPA @IdClass usage.
 */
class TranCatBalanceIdTest {

    @Test
    void testEqualsAndHashCode() {
        TranCatBalanceId id1 = new TranCatBalanceId(12345678901L, "PR", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(12345678901L, "PR", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testNotEquals() {
        TranCatBalanceId id1 = new TranCatBalanceId(12345678901L, "PR", 1);
        TranCatBalanceId id2 = new TranCatBalanceId(12345678901L, "PR", 2);

        assertNotEquals(id1, id2);
    }

    @Test
    void testDefaultConstructor() {
        TranCatBalanceId id = new TranCatBalanceId();
        assertNull(id.getTrancatAcctId());
        assertNull(id.getTrancatTypeCd());
        assertNull(id.getTrancatCd());
    }
}
