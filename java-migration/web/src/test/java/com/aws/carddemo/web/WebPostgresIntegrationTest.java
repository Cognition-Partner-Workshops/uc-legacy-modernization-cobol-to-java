package com.aws.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.dataload.SeedDataLoader;
import com.aws.carddemo.web.security.TokenService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    classes = WebApplication.class,
    properties = {
      "spring.main.web-application-type=servlet",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.flyway.enabled=true"
    })
@AutoConfigureMockMvc
@ContextConfiguration(initializers = WebPostgresIntegrationTest.Initializer.class)
@Testcontainers
@EnabledIf("dockerAvailable")
class WebPostgresIntegrationTest {
  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired MockMvc mvc;
  @Autowired SeedDataLoader loader;
  @Autowired TokenService tokens;

  @BeforeAll
  static void start() {
    if (!POSTGRES.isRunning()) POSTGRES.start();
  }

  static boolean dockerAvailable() {
    try {
      return new ProcessBuilder("docker", "info").start().waitFor() == 0;
    } catch (Exception exception) {
      return false;
    }
  }

  @Test
  void seededAccountViewReturnsRealValues() throws Exception {
    loader.loadAll();
    mvc.perform(get("/api/accounts/1").with(bearer(tokens.issue("ADMIN001", "ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.account.acctId").value(1))
        .andExpect(jsonPath("$.data.account.currBal").exists())
        .andExpect(jsonPath("$.data.customer.firstName").exists());
  }

  @Test
  void cardListUsesSevenRowWindows() throws Exception {
    loader.loadAll();
    mvc.perform(
            get("/api/cards")
                .param("page", "0")
                .with(bearer(tokens.issue("ADMIN001", "ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageSize").value(7))
        .andExpect(jsonPath("$.data.cards.length()").value(7));
    mvc.perform(
            get("/api/cards")
                .param("page", "1")
                .with(bearer(tokens.issue("ADMIN001", "ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cards.length()").value(7));
  }

  private static RequestPostProcessor bearer(String token) {
    return request -> {
      request.addHeader("Authorization", "Bearer " + token);
      return request;
    };
  }

  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
      POSTGRES.start();
      TestPropertyValues.of(
              "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
              "spring.datasource.username=" + POSTGRES.getUsername(),
              "spring.datasource.password=" + POSTGRES.getPassword(),
              "carddemo.data-root="
                  + java.nio.file.Path.of("../../app/data").toAbsolutePath().normalize())
          .applyTo(context.getEnvironment());
    }
  }
}
