package com.aws.carddemo.web.reconciliation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.aws.carddemo.common.CobolDecimal;
import com.aws.carddemo.dataload.SeedDataLoader;
import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.DailyTransactionRepository;
import com.aws.carddemo.domain.TransactionRepository;
import com.aws.carddemo.web.WebApplication;
import com.aws.carddemo.web.security.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    classes = WebApplication.class,
    properties = {
      "spring.main.web-application-type=servlet",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.batch.job.enabled=false"
    })
@AutoConfigureMockMvc
@ContextConfiguration(initializers = ReconciliationAcceptanceTest.Initializer.class)
@ActiveProfiles("test")
@Testcontainers
@EnabledIf("dockerAvailable")
class ReconciliationAcceptanceTest {
  private static final Path OUTPUT = Path.of("target/reconciliation").toAbsolutePath().normalize();

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired SeedDataLoader loader;
  @Autowired JobLauncher launcher;
  @Autowired Job postTransactionsJob;
  @Autowired Job interestCalculationJob;
  @Autowired Job transactionReportJob;
  @Autowired Job createStatementsJob;
  @Autowired Job exportJob;
  @Autowired DailyTransactionRepository dailyTransactions;
  @Autowired TransactionRepository transactions;
  @Autowired AccountRepository accounts;
  @Autowired CustomerRepository customers;
  @Autowired MockMvc mvc;
  @Autowired TokenService tokens;
  @Autowired ObjectMapper objectMapper;

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
  void reconcilesBatchSequenceAndRestJourney() throws Exception {
    assertEquals(new BigDecimal("1.24"), CobolDecimal.rounded(new BigDecimal("1.235"), 2));
    assertEquals(new BigDecimal("-1.24"), CobolDecimal.rounded(new BigDecimal("-1.235"), 2));
    loader.loadAll();
    assertEquals(300, dailyTransactions.count());
    Account before = copy(accounts.findById(1L).orElseThrow());

    JobExecution posting =
        run(postTransactionsJob, "post", new JobParametersBuilder().toJobParameters());
    assertTrue(posting.getExitStatus().getExitCode().contains("REJECTIONS"));
    assertEquals(262, posting.getExecutionContext().getInt("processedCount"));
    assertEquals(38, posting.getExecutionContext().getInt("rejectedCount"));

    JobExecution interest =
        run(
            interestCalculationJob,
            "interest",
            new JobParametersBuilder().addString("runDate", "20220719").toJobParameters());
    assertEquals("COMPLETED", interest.getExitStatus().getExitCode());
    Account afterInterest = accounts.findById(1L).orElseThrow();
    assertEquals(2, afterInterest.getCurrBal().scale());
    assertEquals(2, afterInterest.getCurrCycCredit().scale());
    assertEquals(2, afterInterest.getCurrCycDebit().scale());
    assertTrue(afterInterest.getCurrBal().compareTo(before.getCurrBal()) > 0);

    Files.createDirectories(OUTPUT);
    JobExecution report =
        run(
            transactionReportJob,
            "report",
            new JobParametersBuilder()
                .addString("startDate", "1900-01-01")
                .addString("endDate", "2999-12-31")
                .addString("reportPath", OUTPUT.resolve("report.txt").toString())
                .toJobParameters());
    assertEquals("COMPLETED", report.getExitStatus().getExitCode());
    assertFixtureLines(
        "reconciliation/report-fixture.txt", Files.readString(OUTPUT.resolve("report.txt")));

    JobExecution statements =
        run(
            createStatementsJob,
            "statements",
            new JobParametersBuilder()
                .addString("statementPath", OUTPUT.resolve("statements.txt").toString())
                .addString("statementHtmlPath", OUTPUT.resolve("statements.html").toString())
                .toJobParameters());
    assertEquals("COMPLETED", statements.getExitStatus().getExitCode());
    assertFixtureLines(
        "reconciliation/statement-fixture.txt", Files.readString(OUTPUT.resolve("statements.txt")));

    JobExecution export =
        run(
            exportJob,
            "export",
            new JobParametersBuilder()
                .addString("exportPath", OUTPUT.resolve("carddemo-export.dat").toString())
                .toJobParameters());
    assertEquals("COMPLETED", export.getExitStatus().getExitCode());
    assertEquals(50, export.getExecutionContext().getInt("accountRecordsExported"));
    assertEquals(
        200 + transactions.count(), export.getExecutionContext().getInt("totalRecordsExported"));

    String token = signon();
    mvc.perform(
            post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.options.length()").isNotEmpty());
    JsonNode view = accountView(token);
    String cardNumber = view.at("/data/card/cardNum").asText();
    accountUpdate(token, view);
    assertEquals("Reconciled", customers.findById(1).orElseThrow().getFirstName());
    mvc.perform(
            get("/api/cards").param("accountId", "1").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.cards[0].acctId").value(1));
    mvc.perform(
            post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(
                    """
                    {"accountId":"1","cardNumber":"%s","typeCode":"01","categoryCode":"1",
                     "source":"ONLINE","description":"RECONCILIATION","amount":"+00000001.00",
                     "origDate":"2022-07-19","procDate":"2022-07-19","merchantId":"1",
                     "merchantName":"MERCHANT","merchantCity":"CITY","merchantZip":"12345",
                     "confirmValue":"Y","confirm":true}
                    """
                        .formatted(cardNumber)))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/bill-payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content("{\"accountId\":1,\"confirm\":true}"))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(
                    "{\"reportType\":\"monthly\",\"startDate\":\"2022-01-01\","
                        + "\"endDate\":\"2022-12-31\",\"confirm\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("SUBMITTED"));
    assertTrue(accounts.findById(1L).orElseThrow().getCurrBal().compareTo(BigDecimal.ZERO) == 0);
    assertTrue(transactions.count() > 0);
  }

  private String signon() throws Exception {
    MvcResult result =
        mvc.perform(
                post("/api/signon")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userId\":\"USER0001\",\"password\":\"PASSWORD\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return objectMapper
        .readTree(result.getResponse().getContentAsString())
        .at("/data/token")
        .asText();
  }

  private JsonNode accountView(String token) throws Exception {
    MvcResult result =
        mvc.perform(get("/api/accounts/1").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private void accountUpdate(String token, JsonNode view) throws Exception {
    JsonNode account = view.at("/data/account");
    JsonNode customer = view.at("/data/customer");
    ObjectNode body = objectMapper.createObjectNode();
    body.put("accountId", 1);
    body.put("activeStatus", text(account, "activeStatus"));
    body.set("currBal", account.get("currBal"));
    body.set("currCycCredit", account.get("currCycCredit"));
    body.set("currCycDebit", account.get("currCycDebit"));
    body.set("creditLimit", account.get("creditLimit"));
    body.set("cashCreditLimit", account.get("cashCreditLimit"));
    body.put("openDate", text(account, "openDate"));
    body.put("expiraionDate", text(account, "expiraionDate"));
    body.put("reissueDate", text(account, "reissueDate"));
    body.put("groupId", text(account, "groupId"));
    body.put("firstName", "Reconciled");
    body.put("middleName", text(customer, "middleName"));
    body.put("lastName", text(customer, "lastName"));
    body.put("addressLine1", text(customer, "addrLine1"));
    body.put("addressLine2", text(customer, "addrLine2"));
    body.put("city", text(customer, "addrLine3"));
    body.put("state", text(customer, "addrStateCd"));
    body.put("country", text(customer, "addrCountryCd"));
    body.put("zip", "27546");
    body.put("phone1", text(customer, "phoneNum1"));
    body.put("phone2", "(201)555-1212");
    body.put("eftAccountId", text(customer, "eftAccountId"));
    body.put("primaryCardHolder", text(customer, "priCardHolderInd"));
    body.put("dob", text(customer, "dobYyyyMmDd"));
    body.set("ficoScore", customer.get("ficoCreditScore"));
    ObjectNode preImage = body.deepCopy();
    preImage.put("firstName", text(customer, "firstName"));
    preImage.put("zip", text(customer, "addrZip"));
    preImage.put("phone2", text(customer, "phoneNum2"));
    preImage.remove(List.of("accountId", "confirm", "preImage", "context"));
    body.set("preImage", preImage);
    body.put("confirm", false);
    body.set(
        "context", objectMapper.createObjectNode().put("userId", "USER0001").put("userType", "U"));
    mvc.perform(
            put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body.toString()))
        .andExpect(status().isOk());
    body.put("confirm", true);
    mvc.perform(
            put("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(body.toString()))
        .andExpect(status().isOk());
  }

  private static String text(JsonNode node, String name) {
    return node.at("/" + name).asText();
  }

  private void assertFixtureLines(String resource, String actual) throws Exception {
    var fixture = getClass().getClassLoader().getResourceAsStream(resource);
    assertTrue(fixture != null, "Missing fixture resource: " + resource);
    for (String expected : new String(fixture.readAllBytes()).split("\\R")) {
      assertTrue(actual.contains(expected), "Missing fixture line: " + expected);
    }
  }

  private JobExecution run(Job job, String id, JobParameters parameters) throws Exception {
    return launcher.run(
        job,
        new JobParametersBuilder(parameters)
            .addString("testRun", id + "-" + System.nanoTime())
            .toJobParameters());
  }

  private static Account copy(Account source) {
    Account value = new Account();
    value.setAcctId(source.getAcctId());
    value.setCurrBal(source.getCurrBal());
    return value;
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
              "carddemo.data-root=" + Path.of("../../app/data").toAbsolutePath().normalize())
          .applyTo(context.getEnvironment());
    }
  }
}
