package com.aws.carddemo.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.containers.PostgreSQLContainer;
import org.flywaydb.core.Flyway;

class FlywayPostgresTest {
  static boolean dockerAvailable() {
    try { return new ProcessBuilder("docker", "info").redirectErrorStream(true).start().waitFor() == 0; }
    catch (Exception e) { return false; }
  }
  @Test @EnabledIf("dockerAvailable")
  void postgresContainerCanBeStarted() {
    try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")) {
      postgres.start();
      assertTrue(postgres.isRunning());
      Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
          .load().migrate();
      assertTrue(Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
          .load().info().current().getVersion().toString().equals("3"));
    }
  }
}
