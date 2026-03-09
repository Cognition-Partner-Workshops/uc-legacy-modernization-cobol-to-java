package com.carddemo.config;

import com.carddemo.service.online.CardDemoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration replacing COBOL COSGN00C sign-on program and RACF.
 *
 * <p>Original COBOL behavior:
 * - COSGN00C reads USRSEC VSAM file by user ID
 * - Compares plain-text password (SEC-USR-PWD)
 * - Routes admin users (SEC-USR-TYPE='A') to admin menu (COADM01C)
 * - Routes regular users (SEC-USR-TYPE='U') to user menu (COMEN01C)
 *
 * <p>Java replacement:
 * - BCrypt password hashing (no more plain-text)
 * - Role-based access: ROLE_ADMIN for type 'A', ROLE_USER for type 'U'
 * - Stateless session with HTTP Basic auth (to be replaced with JWT in Phase 2)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CardDemoUserDetailsService userDetailsService;

    public SecurityConfig(CardDemoUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                // Admin endpoints (replaces COADM01C menu access check)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Batch job endpoints
                .requestMatchers("/api/batch/**").hasRole("ADMIN")
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .httpBasic(basic -> {})
            .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
