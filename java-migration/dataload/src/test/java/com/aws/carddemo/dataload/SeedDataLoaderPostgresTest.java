package com.aws.carddemo.dataload;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.aws.carddemo.domain.AccountRepository;
import com.aws.carddemo.domain.CardRepository;
import com.aws.carddemo.domain.CustomerRepository;
import com.aws.carddemo.domain.DailyTransactionRepository;
import com.aws.carddemo.domain.DisclosureGroupRepository;
import com.aws.carddemo.domain.TranCategoryBalanceRepository;
import com.aws.carddemo.domain.TransactionCategoryRepository;
import com.aws.carddemo.domain.TransactionTypeRepository;
import com.aws.carddemo.domain.UsrsecRepository;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
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
    classes = DataloadApplication.class,
    properties = {
      "spring.main.web-application-type=none",
      "spring.jpa.hibernate.ddl-auto=validate"
    })
@ContextConfiguration(initializers = SeedDataLoaderPostgresTest.Initializer.class)
@Testcontainers
@EnabledIf("dockerAvailable")
class SeedDataLoaderPostgresTest {
  private static final Path DATA_ROOT = Path.of("../../app/data").toAbsolutePath().normalize();

  @Container
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired SeedDataLoader loader;
  @Autowired UsrsecRepository usrsecRepository;
  @Autowired CustomerRepository customerRepository;
  @Autowired AccountRepository accountRepository;
  @Autowired CardRepository cardRepository;
  @Autowired DailyTransactionRepository dailyTransactionRepository;
  @Autowired DisclosureGroupRepository disclosureGroupRepository;
  @Autowired TranCategoryBalanceRepository tranCategoryBalanceRepository;
  @Autowired TransactionCategoryRepository transactionCategoryRepository;
  @Autowired TransactionTypeRepository transactionTypeRepository;

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
  void loadsEveryFixedWidthSeedWithSamplesAndExactCounts() {
    int expectedTotal = 0;
    for (SeedDataset dataset : SeedDataset.values()) {
      expectedTotal += sourceCount(dataset);
    }
    assertEquals(expectedTotal, loader.loadAll());
    assertEquals(sourceCount(SeedDataset.USRSEC), usrsecRepository.count());
    assertEquals(sourceCount(SeedDataset.CUSTOMER), customerRepository.count());
    assertEquals(sourceCount(SeedDataset.ACCOUNT), accountRepository.count());
    assertEquals(sourceCount(SeedDataset.CARD), cardRepository.count());
    assertEquals(sourceCount(SeedDataset.DAILY_TRANSACTION), dailyTransactionRepository.count());
    assertEquals(sourceCount(SeedDataset.DISCLOSURE_GROUP), disclosureGroupRepository.count());
    assertEquals(
        sourceCount(SeedDataset.TRAN_CATEGORY_BALANCE), tranCategoryBalanceRepository.count());
    assertEquals(
        sourceCount(SeedDataset.TRANSACTION_CATEGORY), transactionCategoryRepository.count());
    assertEquals(sourceCount(SeedDataset.TRANSACTION_TYPE), transactionTypeRepository.count());

    var customer = customerRepository.findById(1).orElseThrow();
    assertEquals("Immanuel", customer.getFirstName());
    assertEquals("Kessler", customer.getLastName());

    var account = accountRepository.findById(1L).orElseThrow();
    assertEquals("Y", account.getActiveStatus());
    assertEquals("194.00", account.getCurrBal().toPlainString());

    var positive = dailyTransactionRepository.findById("0000000000683580").orElseThrow();
    assertEquals("504.77", positive.getAmt().toPlainString());
    var negative = dailyTransactionRepository.findById("0000000001774260").orElseThrow();
    assertEquals("-919.00", negative.getAmt().toPlainString());

    var user = usrsecRepository.findById("ADMIN001").orElseThrow();
    assertEquals("MARGARET", user.getSecUsrFname());
    assertEquals("GOLD", user.getSecUsrLname());
    assertEquals("A", user.getSecUsrType());

    assertEquals(expectedTotal, loader.loadAll());
    assertEquals(50, accountRepository.count());
    assertEquals(300, dailyTransactionRepository.count());
  }

  @Test
  void accountOnlyReloadRefusesPopulatedDependentsButFullReloadStillWorks() {
    loader.loadAll();

    IllegalStateException failure =
        assertThrows(IllegalStateException.class, () -> loader.load(SeedDataset.ACCOUNT));
    assertEquals(
        "Cannot reload account independently while dependent rows exist; use --dataset=all",
        failure.getMessage());
    assertEquals(50, accountRepository.count());

    loader.loadAll();
    assertEquals(50, accountRepository.count());
    assertEquals(300, dailyTransactionRepository.count());
  }

  private int sourceCount(SeedDataset dataset) {
    try {
      return FixedWidthReader.read(dataset.resolve(DATA_ROOT), dataset.recordLength()).size();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to count " + dataset + " source records", e);
    }
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
