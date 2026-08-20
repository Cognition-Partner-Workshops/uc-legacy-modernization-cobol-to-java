package com.aws.carddemo.web.security;

import com.aws.carddemo.domain.UsrsecRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder legacyPasswordEncoder() {
    return new LegacyPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UsrsecRepository users) {
    return username ->
        users
            .findBySecUsrId(username.toUpperCase())
            .map(
                user ->
                    org.springframework.security.core.userdetails.User.withUsername(
                            user.getSecUsrId())
                        .password(user.getSecUsrPwd())
                        .authorities(
                            user.getSecUsrType().equalsIgnoreCase("A") ? "ROLE_ADMIN" : "ROLE_USER")
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException(username));
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, TokenService tokens) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            sessions ->
                sessions.sessionCreationPolicy(
                    org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions.authenticationEntryPoint(
                    (request, response, exception) -> response.sendError(401, "Unauthorized")))
        .authorizeHttpRequests(
            requests ->
                requests
                    .requestMatchers("/api/signon", "/actuator/health")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/api/menu",
                        "/api/accounts/**",
                        "/api/cards/**",
                        "/api/transactions/**",
                        "/api/bill-payments/**",
                        "/api/reports/**")
                    .hasRole("USER")
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            new JwtAuthenticationFilter(tokens), UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
