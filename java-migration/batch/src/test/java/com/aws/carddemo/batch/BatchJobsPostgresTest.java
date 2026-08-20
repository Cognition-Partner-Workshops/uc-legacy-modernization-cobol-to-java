package com.aws.carddemo.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.aws.carddemo.dataload.SeedDataLoader;
import com.aws.carddemo.domain.Account;
import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardXrefId;
import com.aws.carddemo.domain.CardXrefRepository;
import com.aws.carddemo.domain.DailyTransaction;
import com.aws.carddemo.domain.DailyTransactionRejectRepository;
import com.aws.carddemo.domain.DailyTransactionRepository;
import com.aws.carddemo.domain.DisclosureGroup;
import com.aws.carddemo.domain.DisclosureGroupId;
import com.aws.carddemo.domain.DisclosureGroupRepository;
import com.aws.carddemo.domain.TranCategoryBalanceId;
import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.Transaction;
import com.aws.carddemo.domain.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(
    classes = BatchApplication.class,
    properties = {
      "spring.main.web-application-type=none",
      "spring.batch.job.enabled=false",
      "spring.jpa.hibernate.ddl-auto=validate"
    })
@ContextConfiguration(initializers = BatchJobsPostgresTest.Initializer.class)
@Testcontainers
@EnabledIf("dockerAvailable")
class BatchJobsPostgresTest {
  private static final Path DATA_ROOT = Path.of("../../app/data").toAbsolutePath().normalize();
  private static final Path OUTPUT = Path.of("target/test-output").toAbsolutePath().normalize();

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired SeedDataLoader loader;
  @Autowired JobLauncher jobLauncher;
  @Autowired Job postTransactionsJob;
  @Autowired Job interestCalculationJob;
  @Autowired Job dailyTransactionValidateJob;
  @Autowired Job transactionReportJob;
  @Autowired Job createStatementsJob;
  @Autowired DailyTransactionRepository dailyTransactions;
  @Autowired DailyTransactionRejectRepository rejects;
  @Autowired CardXrefRepository xrefs;
  @Autowired AccountRepository accounts;
  @Autowired TranCategoryBalanceRepository balances;
  @Autowired DisclosureGroupRepository disclosureGroups;
  @Autowired TransactionRepository transactions;

  @BeforeAll
  static void start() {
    if (!POSTGRES.isRunning()) {
      POSTGRES.start();
    }
  }

  static boolean dockerAvailable() {
    try {
      return new ProcessBuilder("docker", "info").start().waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }

  @Test
  void jobsReconcileAndProduceReports() throws Exception {
    loader.loadAll();
    Map<Long, Account> beforeAccounts = new HashMap<>();
    accounts.findAll().forEach(account -> beforeAccounts.put(account.getAcctId(), copy(account)));
    Map<TranCategoryBalanceId, BigDecimal> beforeBalances = new HashMap<>();
    balances
        .findAll()
        .forEach(
            balance ->
                beforeBalances.put(
                    balance.getId(),
                    balance.getTranCatBal() == null ? BigDecimal.ZERO : balance.getTranCatBal()));
    Map<Long, BigDecimal> accountDeltas = new HashMap<>();
    Map<TranCategoryBalanceId, BigDecimal> balanceDeltas = new HashMap<>();
    int expectedRejected = 0;
    for (DailyTransaction daily : dailyTransactions.findByTranIdGreaterThanOrderByTranId("")) {
      var xref = xrefs.findById(new CardXrefId(daily.getCardNum())).orElse(null);
      if (xref == null) {
        expectedRejected++;
        continue;
      }
      Account account = beforeAccounts.get(xref.getAcctId());
      if (account == null) {
        expectedRejected++;
        continue;
      }
      BigDecimal amount = daily.getAmt();
      BigDecimal temp =
          value(account.getCurrCycCredit())
              .subtract(value(account.getCurrCycDebit()))
              .add(value(amount));
      int reason = 0;
      if (value(account.getCreditLimit()).compareTo(temp) < 0) {
        reason = 102;
      }
      if (account.getExpiraionDate() != null
          && !account.getExpiraionDate().isBlank()
          && account.getExpiraionDate().compareTo(daily.getOrigTs().substring(0, 10)) < 0) {
        reason = 103;
      }
      if (reason != 0) {
        expectedRejected++;
        continue;
      }
      accountDeltas.merge(xref.getAcctId(), value(amount), BigDecimal::add);
      TranCategoryBalanceId balanceId =
          new TranCategoryBalanceId(xref.getAcctId(), daily.getTypeCd(), daily.getCatCd());
      balanceDeltas.merge(balanceId, value(amount), BigDecimal::add);
      // CBTRN02C rewrites ACCOUNT-FILE before reading the next daily transaction.
      // Overlimit validation is therefore stateful across records within one run.
      if (amount.signum() >= 0) {
        account.setCurrCycCredit(value(account.getCurrCycCredit()).add(value(amount)));
      } else {
        account.setCurrCycDebit(value(account.getCurrCycDebit()).add(value(amount)));
      }
    }
    JobExecution posting = run(postTransactionsJob, "posting-" + System.nanoTime());
    assertTrue(posting.getExitStatus().getExitCode().contains("REJECTIONS"));
    assertTrue(posting.getExitStatus().getExitDescription().contains("return-code=4"));
    assertEquals(38, expectedRejected);
    assertEquals(262, 300 - expectedRejected);
    assertEquals(expectedRejected, posting.getExecutionContext().getInt("rejectedCount"));
    assertEquals(300 - expectedRejected, posting.getExecutionContext().getInt("processedCount"));
    assertEquals(expectedRejected, rejects.count());
    assertEquals(new BigDecimal("1094.10"), accountDeltas.get(1L));
    assertEquals(new BigDecimal("1576.97"), accountDeltas.get(2L));
    assertEquals(
        new BigDecimal("1164.87"), balanceDeltas.get(new TranCategoryBalanceId(1L, "01", 1)));
    assertEquals(
        new BigDecimal("-763.00"), balanceDeltas.get(new TranCategoryBalanceId(2L, "03", 1)));
    for (Long acctId : accountDeltas.keySet()) {
      BigDecimal expected =
          value(beforeAccounts.get(acctId).getCurrBal()).add(accountDeltas.get(acctId));
      assertEquals(expected, accounts.findById(acctId).orElseThrow().getCurrBal());
    }
    for (var entry : balanceDeltas.entrySet()) {
      BigDecimal expected =
          value(beforeBalances.getOrDefault(entry.getKey(), BigDecimal.ZERO)).add(entry.getValue());
      assertEquals(expected, balances.findById(entry.getKey()).orElseThrow().getTranCatBal());
    }

    JobExecution validation = run(dailyTransactionValidateJob, "validation-" + System.nanoTime());
    assertEquals("COMPLETED", validation.getExitStatus().getExitCode());
    assertEquals(300, validation.getExecutionContext().getInt("checkedCount"));
    assertEquals(0, validation.getExecutionContext().getInt("missingXrefCount"));
    assertEquals(0, validation.getExecutionContext().getInt("missingAccountCount"));
    Account interestAccountBefore = copy(accounts.findById(1L).orElseThrow());
    BigDecimal expectedInterest = BigDecimal.ZERO.setScale(2);
    java.util.List<BigDecimal> expectedInterestRows = new java.util.ArrayList<>();
    for (var balance : balances.findAll()) {
      if (!balance.getId().getAcctId().equals(1L)) {
        continue;
      }
      DisclosureGroup group =
          disclosureGroups
              .findById(
                  new DisclosureGroupId(
                      interestAccountBefore.getGroupId(),
                      balance.getId().getTypeCd(),
                      balance.getId().getCatCd()))
              .orElseGet(
                  () ->
                      disclosureGroups
                          .findById(
                              new DisclosureGroupId(
                                  "DEFAULT",
                                  balance.getId().getTypeCd(),
                                  balance.getId().getCatCd()))
                          .orElseThrow());
      BigDecimal rate = value(group.getIntRate());
      if (rate.signum() != 0) {
        BigDecimal amount =
            value(balance.getTranCatBal())
                .multiply(rate)
                .divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        expectedInterestRows.add(amount);
        expectedInterest = expectedInterest.add(amount);
      }
    }
    assertTrue(!expectedInterestRows.isEmpty());
    JobExecution interest =
        run(
            interestCalculationJob,
            "interest-" + System.nanoTime(),
            new JobParametersBuilder().addString("runDate", "2022071800"));
    assertEquals("COMPLETED", interest.getExitStatus().getExitCode());
    Account interestAccountAfter = accounts.findById(1L).orElseThrow();
    assertEquals(
        value(interestAccountBefore.getCurrBal()).add(expectedInterest),
        interestAccountAfter.getCurrBal());
    java.util.List<Transaction> generatedInterest =
        transactions.findAll().stream()
            .filter(t -> ("Int. for a/c " + 1L).equals(t.getTranDesc()))
            .toList();
    assertEquals(expectedInterestRows.size(), generatedInterest.size());
    for (BigDecimal amount : expectedInterestRows) {
      assertTrue(generatedInterest.stream().anyMatch(t -> amount.compareTo(t.getAmt()) == 0));
    }
    assertTrue(
        accounts.findAll().stream().allMatch(a -> value(a.getCurrCycCredit()).signum() == 0));
    assertTrue(accounts.findAll().stream().allMatch(a -> value(a.getCurrCycDebit()).signum() == 0));

    Files.createDirectories(OUTPUT);
    JobExecution report =
        run(
            transactionReportJob,
            "report-" + System.nanoTime(),
            new JobParametersBuilder()
                .addString("startDate", "1900-01-01")
                .addString("endDate", "2999-12-31")
                .addString("reportPath", OUTPUT.resolve("report.txt").toString()));
    assertEquals("COMPLETED", report.getExitStatus().getExitCode());
    String reportText = Files.readString(OUTPUT.resolve("report.txt"));
    assertTrue(reportText.contains("Daily Transaction Report"));
    assertTrue(reportText.contains("Transaction ID"));
    assertTrue(reportText.contains("Grand Total"));
    assertTrue(reportText.contains("0000000000683580"));

    JobExecution statements =
        run(
            createStatementsJob,
            "statement-" + System.nanoTime(),
            new JobParametersBuilder()
                .addString("statementPath", OUTPUT.resolve("statements.txt").toString())
                .addString("statementHtmlPath", OUTPUT.resolve("statements.html").toString()));
    assertEquals("COMPLETED", statements.getExitStatus().getExitCode());
    String plain = Files.readString(OUTPUT.resolve("statements.txt"));
    String html = Files.readString(OUTPUT.resolve("statements.html"));
    assertTrue(plain.contains("START OF STATEMENT"));
    assertTrue(plain.contains("TRANSACTION SUMMARY"));
    assertTrue(html.contains("<!DOCTYPE html>"));
    assertTrue(html.contains("Current Balance"));
    assertTrue(html.contains("FICO Score"));
    assertTrue(html.contains("TRANSACTION SUMMARY"));
    assertTrue(html.contains("END OF STATEMENT"));
    assertTrue(transactions.count() > 0);
  }

  private JobExecution run(Job job, String id) throws Exception {
    return run(job, id, new JobParametersBuilder());
  }

  private JobExecution run(Job job, String id, JobParametersBuilder builder) throws Exception {
    return jobLauncher.run(job, builder.addString("run.id", id).toJobParameters());
  }

  private Account copy(Account account) {
    Account copy = new Account();
    copy.setAcctId(account.getAcctId());
    copy.setCurrBal(account.getCurrBal());
    copy.setCreditLimit(account.getCreditLimit());
    copy.setCurrCycCredit(account.getCurrCycCredit());
    copy.setCurrCycDebit(account.getCurrCycDebit());
    copy.setExpiraionDate(account.getExpiraionDate());
    return copy;
  }

  private BigDecimal value(BigDecimal value) {
    return value == null ? BigDecimal.ZERO.setScale(2) : value;
  }

  public static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
      TestPropertyValues.of(
              "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
              "spring.datasource.username=" + POSTGRES.getUsername(),
              "spring.datasource.password=" + POSTGRES.getPassword(),
              "carddemo.data-root=" + DATA_ROOT)
          .applyTo(context.getEnvironment());
    }
  }
}
