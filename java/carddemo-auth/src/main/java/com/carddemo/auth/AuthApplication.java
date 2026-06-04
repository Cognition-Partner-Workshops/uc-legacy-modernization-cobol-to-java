package com.carddemo.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CardDemo Identity &amp; Access service.
 *
 * <p>This Spring Boot application is the Phase 1 modernization of the CardDemo
 * sign-on and user-management mainframe programs. It replaces:
 * <ul>
 *   <li>{@code COSGN00C} (CICS transaction {@code CC00}) - the sign-on entry
 *       point of the entire legacy application.</li>
 *   <li>{@code COUSR00C}/{@code COUSR01C}/{@code COUSR02C}/{@code COUSR03C} -
 *       the VSAM {@code USRSEC} user maintenance programs.</li>
 *   <li>{@code COADM01C} - the admin menu that dispatched to the above.</li>
 * </ul>
 * The flat-file {@code USRSEC} VSAM dataset (keyed on {@code SEC-USR-ID}) is
 * replaced by a relational {@code users} table, and the COMMAREA trust flags
 * ({@code CDEMO-USER-ID}/{@code CDEMO-USER-TYPE}) are replaced by JWT claims.
 */
@SpringBootApplication
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
