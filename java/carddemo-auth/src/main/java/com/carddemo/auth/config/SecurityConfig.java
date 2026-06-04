package com.carddemo.auth.config;

import com.carddemo.auth.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Stateless JWT security configuration.
 *
 * <p>Encodes the access policy that the legacy {@code COADM01C} admin menu and
 * the sign-on program enforced procedurally:
 * <ul>
 *   <li>{@code POST /api/auth/login} is public - it is the entry point,
 *       replacing transaction {@code CC00} / {@code COSGN00C}.</li>
 *   <li>The user-management API ({@code /api/users/**}, replacing
 *       {@code COUSR00C}-{@code COUSR03C}, reachable only through the admin
 *       menu) requires the {@code ADMIN} role.</li>
 *   <li>Every other request requires a valid JWT.</li>
 * </ul>
 * Sessions are stateless: identity comes from the token, not a CICS
 * pseudo-conversation or COMMAREA.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                // Missing/invalid token -> 401 (default would be 403).
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * BCrypt password encoder - the heart of the Phase 1 security uplift.
     * Replaces the plaintext {@code SEC-USR-PWD} comparison in {@code COSGN00C}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
