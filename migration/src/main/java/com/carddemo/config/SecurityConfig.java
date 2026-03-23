package com.carddemo.config;

import com.carddemo.entity.UserSecurity;
import com.carddemo.repository.UserSecurityRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration replacing CICS CC00 sign-on.
 * Two roles: ADMIN (user type 'A') and USER (user type 'U').
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserSecurityRepository userSecurityRepository,
                                                  PasswordEncoder passwordEncoder) {
        return username -> {
            String upperUsername = username.toUpperCase().trim();
            UserSecurity userSecurity = userSecurityRepository.findById(upperUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            String password = userSecurity.getPassword();
            // If the stored password is not BCrypt-encoded, encode it for Spring Security
            if (!password.startsWith("$2a$") && !password.startsWith("$2b$")) {
                password = passwordEncoder.encode(password.trim().toUpperCase());
            }

            String role = userSecurity.isAdmin() ? "ADMIN" : "USER";

            return User.builder()
                    .username(userSecurity.getUserId())
                    .password(password)
                    .roles(role)
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/batch/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/menu", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/api/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            );

        return http.build();
    }
}
