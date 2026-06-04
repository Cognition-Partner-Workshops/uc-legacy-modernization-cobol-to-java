package com.carddemo.auth.security;

import com.carddemo.auth.model.UserType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link JwtTokenProvider}, the COMMAREA-replacement token mint.
 */
class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-test-secret-test-secret-test-secret-1234567890";

    private final JwtTokenProvider provider =
            new JwtTokenProvider(SECRET, 3_600_000L);

    @Test
    void generatesTokenCarryingUserIdAndTypeClaims() {
        String token = provider.generateToken("ADMIN001", UserType.ADMIN);

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUserId(token)).isEqualTo("ADMIN001");
        assertThat(provider.getUserType(token)).isEqualTo(UserType.ADMIN);
    }

    @Test
    void rejectsTamperedToken() {
        String token = provider.generateToken("USER0001", UserType.USER);

        assertThat(provider.validateToken(token + "tampered")).isFalse();
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider other = new JwtTokenProvider(
                "another-secret-another-secret-another-secret-0987654321", 3_600_000L);
        String foreignToken = other.generateToken("ADMIN001", UserType.ADMIN);

        assertThat(provider.validateToken(foreignToken)).isFalse();
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, -1_000L);
        String expired = shortLived.generateToken("USER0001", UserType.USER);

        assertThat(shortLived.validateToken(expired)).isFalse();
    }
}
