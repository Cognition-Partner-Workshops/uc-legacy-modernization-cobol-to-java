package com.carddemo.auth.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "CardDemoSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits!!",
                3600000
        );
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        String token = tokenProvider.generateToken("USER0001", "U");
        assertThat(token).isNotBlank();
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = tokenProvider.generateToken("ADMIN001", "A");
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo("ADMIN001");
    }

    @Test
    void getRoleFromToken_shouldReturnAdminForTypeA() {
        String token = tokenProvider.generateToken("ADMIN001", "A");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    void getRoleFromToken_shouldReturnUserForTypeU() {
        String token = tokenProvider.generateToken("USER0001", "U");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("USER");
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertThat(tokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "CardDemoSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits!!",
                0
        );
        String token = shortLived.generateToken("USER0001", "U");
        assertThat(shortLived.validateToken(token)).isFalse();
    }
}
