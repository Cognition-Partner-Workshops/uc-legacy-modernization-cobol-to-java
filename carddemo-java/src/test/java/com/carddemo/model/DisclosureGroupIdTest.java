package com.carddemo.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DisclosureGroupId composite key.
 * Validates equals/hashCode for JPA @IdClass usage.
 */
class DisclosureGroupIdTest {

    @Test
    void testEqualsAndHashCode() {
        DisclosureGroupId id1 = new DisclosureGroupId("GROUP001", "PR", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("GROUP001", "PR", 1);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testNotEquals() {
        DisclosureGroupId id1 = new DisclosureGroupId("GROUP001", "PR", 1);
        DisclosureGroupId id2 = new DisclosureGroupId("GROUP002", "PR", 1);

        assertNotEquals(id1, id2);
    }
}
