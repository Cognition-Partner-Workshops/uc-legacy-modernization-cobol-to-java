package com.aws.carddemo.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.dataload.SeedDataLoader;
import com.aws.carddemo.web.security.TokenService;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
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
@Import(WebPostgresIntegrationTest.FailureControllerConfiguration.class)
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("dockerAvailable")
class WebPostgresIntegrationTest {
  @TestConfiguration
  static class FailureControllerConfiguration {
    @Bean
    FailureController failureController() {
      return new FailureController();
    }
  }

  @RestController
  static class FailureController {
    @GetMapping("/api/test/failure")
    String failure() {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "test failure");
    }
  }

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired MockMvc mvc;
  @Autowired SeedDataLoader loader;
  @Autowired TokenService tokens;
  @Autowired com.aws.carddemo.domain.CardRepository cardRepository;

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
    mvc.perform(
            get("/api/accounts/1")
                .param("userId", "ATTACKER")
                .with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.account.acctId").value(1))
        .andExpect(jsonPath("$.data.account.currBal").exists())
        .andExpect(jsonPath("$.data.customer.firstName").exists())
        .andExpect(jsonPath("$.context.userId").value("USER001"))
        .andExpect(jsonPath("$.context.userType").value("U"));
  }

  @Test
  void cardListUsesSevenRowWindows() throws Exception {
    loader.loadAll();
    mvc.perform(
            get("/api/cards").param("page", "0").with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.pageSize").value(7))
        .andExpect(jsonPath("$.data.cards.length()").value(7));
    mvc.perform(
            get("/api/cards").param("page", "1").with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cards.length()").value(7));
  }

  @Test
  void transactionTypeQueryParametersBindWithCompiledParameterMetadata() throws Exception {
    loader.loadAll();
    mvc.perform(
            get("/api/admin/transaction-types")
                .param("page", "0")
                .param("direction", "FORWARD")
                .with(bearer(tokens.issue("ADMIN001", "ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data.length()").value(7))
        .andExpect(jsonPath("$.data[0].typeCd").exists());
  }

  @Test
  void handlerFailureSurfacesAsServerErrorInsteadOfUnauthorized() throws Exception {
    mvc.perform(get("/api/test/failure").with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isInternalServerError());
  }

  @Test
  void cardListFilterBySeededAccountReturnsOnlyThatAccountsCards() throws Exception {
    loader.loadAll();
    mvc.perform(
            get("/api/cards")
                .param("accountId", "1")
                .with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cards.length()").value(cardRepository.findByAcctId(1L).size()))
        .andExpect(jsonPath("$.data.cards[0].acctId").value(1));
  }

  @Test
  void transactionAddPersistsAndBillPaymentUpdatesAccount() throws Exception {
    loader.loadAll();
    String userToken = tokens.issue("USER001", "ROLE_USER");
    String transaction =
        """
        {
          "accountId":"1","typeCode":"01","categoryCode":"1","source":"ONLINE",
          "description":"WEB ADD","amount":"+00000100.00","origDate":"2022-01-01",
          "procDate":"2022-01-01","merchantId":"123456789","merchantName":"MERCHANT",
          "merchantCity":"CITY","merchantZip":"12345","confirmValue":"Y","confirm":true,
          "context":{"userId":"USER001","userType":"U"}
        }
        """;
    mvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transaction)
                .with(bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.amt").value(100.0));
    mvc.perform(
            post("/api/bill-payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"accountId":1,"confirm":true,"context":{"userId":"USER001","userType":"U"}}
                    """)
                .with(bearer(userToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.remainingBalance").value(0.0));
  }

  @Test
  void adminUserRoundTripPersistsAgainstUsrsec() throws Exception {
    loader.loadAll();
    String adminToken = tokens.issue("ADMIN001", "ROLE_ADMIN");
    mvc.perform(
            post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"firstName":"Test","lastName":"User","userId":"WEBUSR01",
                     "password":"PASSWORD","userType":"U"}
                    """)
                .with(bearer(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.secUsrId").value("WEBUSR01"));
    mvc.perform(
            put("/api/admin/users/WEBUSR01")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"firstName":"Updated","lastName":"User","password":"PASSWORD",
                     "userType":"U","confirm":true}
                    """)
                .with(bearer(adminToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.secUsrFname").value("Updated"));
    mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                    "/api/admin/users/WEBUSR01")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"confirm\":true}")
                .with(bearer(adminToken)))
        .andExpect(status().isOk());
  }

  @Test
  void reportEndpointSubmitsExistingBatchJob() throws Exception {
    loader.loadAll();
    mvc.perform(
            post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"reportType":"monthly","startDate":"2022-01-01",
                     "endDate":"2022-12-31","confirm":true}
                    """)
                .with(bearer(tokens.issue("USER001", "ROLE_USER"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    Path output = Path.of("target/output/transaction-report.txt");
    for (int attempt = 0; attempt < 50 && !Files.exists(output); attempt++) {
      Thread.sleep(100);
    }
    org.junit.jupiter.api.Assertions.assertTrue(Files.exists(output));
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
