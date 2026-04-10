package com.carddemo.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional parity tests for CSUTLDTC.cbl (Date Validation Utility).
 */
class DateValidationServiceTest {

    private final DateValidationService service = new DateValidationService();

    @Test
    void testValidDate() {
        assertTrue(service.isValidDate("2024-01-15"));
    }

    @Test
    void testInvalidDateFormat() {
        assertFalse(service.isValidDate("01-15-2024"));
    }

    @Test
    void testEmptyDate() {
        assertFalse(service.isValidDate(""));
    }

    @Test
    void testNullDate() {
        assertFalse(service.isValidDate(null));
    }

    @Test
    void testLeapYearDate() {
        assertTrue(service.isValidDate("2024-02-29"));
    }

    @Test
    void testInvalidLeapYearDate() {
        assertFalse(service.isValidDate("2023-02-29"));
    }

    @Test
    void testInvalidMonth() {
        assertFalse(service.isValidDate("2024-13-01"));
    }

    @Test
    void testInvalidDay() {
        assertFalse(service.isValidDate("2024-01-32"));
    }
}
