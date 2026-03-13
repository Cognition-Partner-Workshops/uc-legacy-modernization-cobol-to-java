package com.carddemo.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for the CardDemo API.
 * Defines two roles matching the COBOL CDEMO-USRTYP pattern from COCOM01Y.cpy:
 * - ADMIN (CDEMO-USRTYP-ADMIN VALUE 'A')
 * - USER  (CDEMO-USRTYP-USER  VALUE 'U')
 *
 * Admin users have access to /api/admin/** endpoints (user management, transaction type CRUD).
 * Regular users have access to standard account, card, and transaction endpoints.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole(ROLE_ADMIN)
                        // Authenticated endpoints for both ADMIN and USER
                        .requestMatchers("/api/**").hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
