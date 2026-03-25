package com.carddemo.batch.job;

import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.DisclosureGroupRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.common.model.Account;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.DisclosureGroup;
import com.carddemo.common.model.Transaction;
import com.carddemo.common.model.TransactionCategoryBalance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for InterestCalculationJob (CBACT04C).
 * Verifies that the Java batch job calculates interest equivalently to the COBOL program.
 *
 * COBOL formula: COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
 */
@SpringBatchTest
@SpringBootTest(classes = com.carddemo.batch.CardDemoBatchApplication.class)
@ActiveProfiles("test")
class InterestCalculationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("interestCalculationBatchJob")
    private Job interestCalculationBatchJob;

    @Autowired
    private TransactionCategoryBalanceRepository tcatBalRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(interestCalculationBatchJob);
        transactionRepository.deleteAll();
        tcatBalRepository.deleteAll();
        disclosureGroupRepository.deleteAll();
        cardCrossReferenceRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("CBACT04C Parity: Interest calculated correctly using formula (balance * rate) / 1200")
    void testInterestCalculation() throws Exception {
        // Set up account with group
        Account account = new Account();
        account.setAccountId(11111111101L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setGroupId("PREMIUM");
        accountRepository.save(account);

        CardCrossReference xref = new CardCrossReference();
        xref.setCardNumber("4222222222222222");
        xref.setCustomerId(111111111L);
        xref.setAccountId(11111111101L);
        cardCrossReferenceRepository.save(xref);

        // Disclosure group with 18% annual interest rate
        DisclosureGroup dg = new DisclosureGroup();
        dg.setAccountGroupId("PREMIUM");
        dg.setTransactionTypeCode("01");
        dg.setTransactionCategoryCode("0001");
        dg.setInterestRate(new BigDecimal("18.00"));
        disclosureGroupRepository.save(dg);

        // Transaction category balance of $1200
        TransactionCategoryBalance tcatBal = new TransactionCategoryBalance();
        tcatBal.setAccountId(11111111101L);
        tcatBal.setTypeCode("01");
        tcatBal.setCategoryCode("0001");
        tcatBal.setBalance(new BigDecimal("1200.00"));
        tcatBalRepository.save(tcatBal);

        // Execute
        JobParameters params = new JobParametersBuilder()
                .addString("parmDate", "2025-06-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Verify interest transaction was written
        // COBOL: (1200.00 * 18.00) / 1200 = 18.00
        List<Transaction> transactions = transactionRepository.findAll();
        assertFalse(transactions.isEmpty(), "Interest transaction should be created");

        Transaction interestTran = transactions.get(0);
        assertEquals("01", interestTran.getTypeCode());
        assertEquals("0005", interestTran.getCategoryCode());
        assertEquals("System", interestTran.getSource());
        assertTrue(interestTran.getDescription().contains("Int. for a/c"));

        BigDecimal expectedInterest = new BigDecimal("1200.00")
                .multiply(new BigDecimal("18.00"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);
        assertEquals(0, expectedInterest.compareTo(interestTran.getAmount()),
                "Interest should be (1200 * 18) / 1200 = 18.00");
    }

    @Test
    @DisplayName("CBACT04C Parity: Falls back to DEFAULT disclosure group when specific not found")
    void testDefaultGroupFallback() throws Exception {
        Account account = new Account();
        account.setAccountId(22222222201L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("3000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setGroupId("UNKNOWNGRP");
        accountRepository.save(account);

        CardCrossReference xref = new CardCrossReference();
        xref.setCardNumber("4333333333333333");
        xref.setCustomerId(222222222L);
        xref.setAccountId(22222222201L);
        cardCrossReferenceRepository.save(xref);

        // Only DEFAULT group exists (no UNKNOWNGRP)
        DisclosureGroup defaultDg = new DisclosureGroup();
        defaultDg.setAccountGroupId("DEFAULT");
        defaultDg.setTransactionTypeCode("02");
        defaultDg.setTransactionCategoryCode("0002");
        defaultDg.setInterestRate(new BigDecimal("12.00"));
        disclosureGroupRepository.save(defaultDg);

        TransactionCategoryBalance tcatBal = new TransactionCategoryBalance();
        tcatBal.setAccountId(22222222201L);
        tcatBal.setTypeCode("02");
        tcatBal.setCategoryCode("0002");
        tcatBal.setBalance(new BigDecimal("600.00"));
        tcatBalRepository.save(tcatBal);

        JobParameters params = new JobParametersBuilder()
                .addString("parmDate", "2025-06-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Should use DEFAULT rate: (600 * 12) / 1200 = 6.00
        List<Transaction> transactions = transactionRepository.findAll();
        assertFalse(transactions.isEmpty(), "Interest transaction should be created using DEFAULT group");
        assertEquals(0, new BigDecimal("6.00").compareTo(transactions.get(0).getAmount()),
                "Interest should be (600 * 12) / 1200 = 6.00 using DEFAULT group rate");
    }
}
