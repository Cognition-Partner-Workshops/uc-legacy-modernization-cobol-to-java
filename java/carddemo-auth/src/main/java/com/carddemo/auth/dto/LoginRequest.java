package com.carddemo.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sign-on request payload.
 *
 * <p>Replaces the {@code COSGN0A} BMS map input fields ({@code USERIDI} /
 * {@code PASSWDI}) read by {@code COSGN00C}. As in the legacy program, the
 * legacy fields were limited to 8 characters each.
 *
 * @param userId   the user ID (legacy {@code SEC-USR-ID})
 * @param password the plaintext password supplied at sign-on (verified against
 *                 the stored BCrypt hash, never persisted)
 */
public record LoginRequest(
        @NotBlank(message = "User ID can NOT be empty...")
        @Size(max = 8, message = "User ID must be at most 8 characters")
        String userId,

        @NotBlank(message = "Password can NOT be empty...")
        String password
) {
}
