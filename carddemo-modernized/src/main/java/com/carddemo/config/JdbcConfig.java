package com.carddemo.config;

import org.springframework.context.annotation.Configuration;

/**
 * JDBC/ODBC configuration for relational database access.
 *
 * Replaces legacy DB2 connectivity from the mainframe environment.
 * The legacy application used embedded SQL (EXEC SQL) in COBOL programs
 * like COTRTLIC and COTRTUPC to access DB2 tables.
 *
 * This configuration supports multiple connectivity options:
 *
 * 1. JDBC (Direct):
 *    - H2 (development): jdbc:h2:mem:carddemo
 *    - PostgreSQL: jdbc:postgresql://host:5432/carddemo
 *    - MySQL: jdbc:mysql://host:3306/carddemo
 *    - Oracle: jdbc:oracle:thin:@host:1521:carddemo
 *    - DB2 (migration): jdbc:db2://host:50000/carddemo
 *
 * 2. ODBC (via JDBC-ODBC Bridge):
 *    - Configure ODBC DSN on the host system
 *    - Use driver: sun.jdbc.odbc.JdbcOdbcDriver (legacy)
 *    - Or use a third-party JDBC-ODBC bridge library
 *    - URL format: jdbc:odbc:CardDemoDSN
 *
 * Spring Boot auto-configures the DataSource and JdbcTemplate
 * beans from application.yml properties.
 */
@Configuration
public class JdbcConfig {
    // Spring Boot auto-configuration handles DataSource and JdbcTemplate setup.
    // Custom configuration can be added here for ODBC bridge setup,
    // connection pooling, or multi-datasource scenarios.
}
