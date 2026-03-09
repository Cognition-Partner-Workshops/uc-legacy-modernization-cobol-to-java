package com.carddemo.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LoginRequest DTO.
 */
class LoginRequestTest {

    @Test
    void testLoginRequestCreation() {
        LoginRequest req = new LoginRequest("ADMIN001", "PASSWORD");
        assertEquals("ADMIN001", req.getUserId());
        assertEquals("PASSWORD", req.getPassword());
    }

    @Test
    void testLoginRequestSetters() {
        LoginRequest req = new LoginRequest();
        req.setUserId("USER0001");
        req.setPassword("PASS1234");
        assertEquals("USER0001", req.getUserId());
        assertEquals("PASS1234", req.getPassword());
    }
}
