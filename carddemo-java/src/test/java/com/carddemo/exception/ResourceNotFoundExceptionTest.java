package com.carddemo.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException.
 */
class ResourceNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Account", "12345");
        assertEquals("Account not found with id: 12345", ex.getMessage());
        assertEquals("Account", ex.getResourceType());
        assertEquals("12345", ex.getResourceId());
    }
}
