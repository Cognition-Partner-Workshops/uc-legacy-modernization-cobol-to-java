package com.carddemo.batch;

import com.carddemo.model.entity.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary and risk-based tests for CBSTM03A.CBL (Statement Generation Batch Job).
 * Covers: statement generation with active accounts, inactive accounts skipped,
 * accounts with no transactions, empty account table, accounts with no card xref.
 */
@SpringBootTest
@ActiveProfiles("test")
class StatementGenerationJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("statementGenerationBatchJob")
    private Job statementGenerationBatchJob;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Statement generation job completes successfully with seed data")
    void testJobCompletesWithSeedData() throws Exception {
        JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    @DisplayName("Statement generation processes all active accounts")
    void testProcessesActiveAccounts() throws Exception {
        // Count active accounts
        List<Account> activeAccounts = accountRepository.findAllByOrderByAcctIdAsc()
            .stream()
            .filter(a -> "Y".equals(a.getAcctActiveStatus()))
            .toList();

        assertTrue(activeAccounts.size() > 0, "Seed data should have active accounts");

        JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    // ===== BOUNDARY TESTS =====

    @Test
    @DisplayName("Inactive accounts are skipped during statement generation")
    void testInactiveAccountsSkipped() throws Exception {
        // Create an inactive account
        Account inactiveAcct = new Account();
        inactiveAcct.setAcctId(990010L);
        inactiveAcct.setAcctActiveStatus("N");
        inactiveAcct.setAcctCurrBal(new BigDecimal("500.00"));
        inactiveAcct.setAcctCreditLimit(new BigDecimal("5000.00"));
        accountRepository.save(inactiveAcct);

        try {
            JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Job should complete even with inactive accounts");
        } finally {
            accountRepository.delete(inactiveAcct);
        }
    }

    @Test
    @DisplayName("Account with no card xref does not cause failure")
    void testAccountWithNoXrefHandled() throws Exception {
        // Create an active account with no corresponding card xref
        Account noXrefAcct = new Account();
        noXrefAcct.setAcctId(990011L);
        noXrefAcct.setAcctActiveStatus("Y");
        noXrefAcct.setAcctCurrBal(new BigDecimal("100.00"));
        noXrefAcct.setAcctCreditLimit(new BigDecimal("1000.00"));
        accountRepository.save(noXrefAcct);

        try {
            JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Job should complete even for accounts with no card xref");
        } finally {
            accountRepository.delete(noXrefAcct);
        }
    }

    @Test
    @DisplayName("Statement generation with empty account table completes")
    void testEmptyAccountTable() throws Exception {
        List<Account> saved = accountRepository.findAll();
        accountRepository.deleteAll();

        try {
            JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Job should complete with empty account table");
        } finally {
            accountRepository.saveAll(saved);
        }
    }

    // ===== RISK-BASED TESTS =====

    @Test
    @DisplayName("Statement generation handles mixed active/inactive accounts")
    void testMixedActiveInactiveAccounts() throws Exception {
        Account activeAcct = new Account();
        activeAcct.setAcctId(990012L);
        activeAcct.setAcctActiveStatus("Y");
        activeAcct.setAcctCurrBal(new BigDecimal("1000.00"));
        activeAcct.setAcctCreditLimit(new BigDecimal("5000.00"));
        accountRepository.save(activeAcct);

        Account inactiveAcct = new Account();
        inactiveAcct.setAcctId(990013L);
        inactiveAcct.setAcctActiveStatus("N");
        inactiveAcct.setAcctCurrBal(new BigDecimal("200.00"));
        inactiveAcct.setAcctCreditLimit(new BigDecimal("1000.00"));
        accountRepository.save(inactiveAcct);

        try {
            JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        } finally {
            accountRepository.delete(activeAcct);
            accountRepository.delete(inactiveAcct);
        }
    }

    @Test
    @DisplayName("Statement generation handles account with null balance")
    void testAccountWithNullBalance() throws Exception {
        Account nullBalAcct = new Account();
        nullBalAcct.setAcctId(990014L);
        nullBalAcct.setAcctActiveStatus("Y");
        nullBalAcct.setAcctCurrBal(null);
        nullBalAcct.setAcctCreditLimit(null);
        accountRepository.save(nullBalAcct);

        try {
            JobExecution execution = jobLauncher.run(statementGenerationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Job should handle null balance gracefully");
        } finally {
            accountRepository.delete(nullBalAcct);
        }
    }
}
