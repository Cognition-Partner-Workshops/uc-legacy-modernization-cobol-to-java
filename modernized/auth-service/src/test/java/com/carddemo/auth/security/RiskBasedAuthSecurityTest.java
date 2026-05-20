package com.carddemo.auth.security;

import com.carddemo.auth.dto.LoginRequest;
import com.carddemo.auth.model.User;
import com.carddemo.auth.repository.UserRepository;
import com.carddemo.auth.service.AuthService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * TIER 1 — CRITICAL RISK: Authentication & Security
 * Risk factors: unauthorized access, credential bypass, token forgery, privilege escalation
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-1")
@DisplayName("Tier 1 (Critical): Authentication & Security")
class RiskBasedAuthSecurityTest {

    @Mock
    private UserRepository userRepository;

    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        tokenProvider = new JwtTokenProvider(
                "CardDemoSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits!!",
                3600000
        );
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider);
    }

    // --- Authentication Tests ---

    @Test
    @DisplayName("T1-AUTH-001: Valid admin login returns JWT with correct claims")
    void validAdminLogin_returnsTokenWithAdminRole() {
        User admin = User.builder()
                .userId("admin01").firstName("System").lastName("Admin")
                .passwordHash(passwordEncoder.encode("password"))
                .userType("A").build();
        when(userRepository.findById("admin01")).thenReturn(Optional.of(admin));

        var response = authService.login(new LoginRequest("admin01", "password"));

        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo("admin01");
        assertThat(response.getUserType()).isEqualTo("A");
        assertThat(tokenProvider.getUserIdFromToken(response.getToken())).isEqualTo("admin01");
        assertThat(tokenProvider.getRoleFromToken(response.getToken())).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("T1-AUTH-002: Valid regular user login returns JWT with USER role")
    void validUserLogin_returnsTokenWithUserRole() {
        User user = User.builder()
                .userId("user0001").firstName("John").lastName("Smith")
                .passwordHash(passwordEncoder.encode("password"))
                .userType("U").build();
        when(userRepository.findById("user0001")).thenReturn(Optional.of(user));

        var response = authService.login(new LoginRequest("user0001", "password"));

        assertThat(response.getUserType()).isEqualTo("U");
        assertThat(tokenProvider.getRoleFromToken(response.getToken())).isEqualTo("USER");
    }

    @Test
    @DisplayName("T1-AUTH-003: Wrong password is rejected")
    void wrongPassword_throwsBadCredentials() {
        User user = User.builder()
                .userId("user0001")
                .passwordHash(passwordEncoder.encode("correctpass"))
                .userType("U").build();
        when(userRepository.findById("user0001")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user0001", "wrongpass")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("T1-AUTH-004: Nonexistent user is rejected")
    void nonexistentUser_throwsBadCredentials() {
        when(userRepository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("ghost", "password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("T1-AUTH-005: Empty password is rejected")
    void emptyPassword_throwsBadCredentials() {
        User user = User.builder()
                .userId("user0001")
                .passwordHash(passwordEncoder.encode("password"))
                .userType("U").build();
        when(userRepository.findById("user0001")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user0001", "")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("T1-AUTH-006: SQL injection attempt in userId is harmless")
    void sqlInjectionInUserId_rejected() {
        when(userRepository.findById("' OR 1=1--")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("' OR 1=1--", "password")))
                .isInstanceOf(BadCredentialsException.class);
    }

    // --- JWT Token Tests ---

    @Test
    @DisplayName("T1-JWT-001: Tampered token is rejected")
    void tamperedToken_failsValidation() {
        String token = tokenProvider.generateToken("user0001", "U");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(tokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    @DisplayName("T1-JWT-002: Expired token is rejected")
    void expiredToken_failsValidation() {
        JwtTokenProvider shortLived = new JwtTokenProvider(
                "CardDemoSecretKeyForJWTTokenGenerationMustBeAtLeast256Bits!!", 0);
        String token = shortLived.generateToken("user0001", "U");

        assertThat(shortLived.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("T1-JWT-003: Token signed with different secret is rejected")
    void wrongSecretToken_failsValidation() {
        JwtTokenProvider otherProvider = new JwtTokenProvider(
                "DifferentSecretKeyForJWTTokenGenMustBeAtLeast256BitsLong!!", 3600000);
        String token = otherProvider.generateToken("user0001", "U");

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("T1-JWT-004: Completely malformed token is rejected")
    void malformedToken_failsValidation() {
        assertThat(tokenProvider.validateToken("not.a.jwt")).isFalse();
        assertThat(tokenProvider.validateToken("")).isFalse();
        assertThat(tokenProvider.validateToken("random-string")).isFalse();
    }

    @Test
    @DisplayName("T1-JWT-005: Admin token carries ADMIN role, not USER")
    void adminTokenRole_isAdmin() {
        String token = tokenProvider.generateToken("admin01", "A");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("T1-JWT-006: Regular user token carries USER role, not ADMIN")
    void userTokenRole_isUser() {
        String token = tokenProvider.generateToken("user0001", "U");
        assertThat(tokenProvider.getRoleFromToken(token)).isEqualTo("USER");
    }
}
