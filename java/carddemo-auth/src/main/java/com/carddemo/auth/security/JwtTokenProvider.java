package com.carddemo.auth.security;

import com.carddemo.auth.model.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Creates and validates the JWTs that replace the COMMAREA identity flags.
 *
 * <p>In the legacy system, {@code COSGN00C} placed {@code CDEMO-USER-ID} and
 * {@code CDEMO-USER-TYPE} into the shared COMMAREA, and every downstream online
 * program trusted those fields. This provider issues a signed token carrying
 * the same identity as claims ({@code sub} = user ID, {@code userType} = role),
 * so trust is cryptographically verifiable rather than implicit shared memory.
 */
@Component
public class JwtTokenProvider {

    /** JWT claim holding the legacy {@code CDEMO-USER-TYPE} value. */
    public static final String CLAIM_USER_TYPE = "userType";

    private final SecretKey signingKey;
    private final long validityMillis;

    public JwtTokenProvider(
            @Value("${carddemo.jwt.secret}") String secret,
            @Value("${carddemo.jwt.expiration-ms}") long validityMillis) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.validityMillis = validityMillis;
    }

    /**
     * Issues a signed JWT for an authenticated user.
     *
     * @param userId   the authenticated user ID (becomes the {@code sub} claim)
     * @param userType the user's authority level (becomes the {@code userType} claim)
     * @return a compact, signed JWT string
     */
    public String generateToken(String userId, UserType userType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityMillis);
        return Jwts.builder()
                .subject(userId)
                .claim(CLAIM_USER_TYPE, userType.name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates the signature and expiry of a token.
     *
     * @param token the compact JWT string
     * @return {@code true} if the token is well-formed, correctly signed and unexpired
     */
    public boolean validateToken(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /** @return the user ID ({@code sub} claim) from a valid token. */
    public String getUserId(String token) {
        return parse(token).getSubject();
    }

    /** @return the {@link UserType} ({@code userType} claim) from a valid token. */
    public UserType getUserType(String token) {
        String value = parse(token).get(CLAIM_USER_TYPE, String.class);
        return UserType.valueOf(value);
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
