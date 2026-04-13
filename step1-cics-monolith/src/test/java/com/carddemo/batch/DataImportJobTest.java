package com.carddemo.batch;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CustomerRepository;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary and risk-based tests for CBIMPORT.cbl (Data Import Batch Job).
 * Covers: import job completion, idempotency, data integrity after import.
 * Note: The current implementation is a structural shell — the actual fixed-width
 * file I/O is stubbed. These tests verify the Spring Batch wiring and job lifecycle.
 */
@SpringBootTest
@ActiveProfiles("test")
class DataImportJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("dataImportBatchJob")
    private Job dataImportBatchJob;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Data import job completes successfully")
    void testJobCompletes() throws Exception {
        JobExecution execution = jobLauncher.run(dataImportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    @DisplayName("Data import job is idempotent — running twice doesn't corrupt data")
    void testImportIdempotent() throws Exception {
        long acctCountBefore = accountRepository.count();
        long custCountBefore = customerRepository.count();
        long tranCountBefore = transactionRepository.count();

        jobLauncher.run(dataImportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        jobLauncher.run(dataImportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis() + 1).toJobParameters());

        // Since import is a stub, counts should remain unchanged
        assertEquals(acctCountBefore, accountRepository.count(),
            "Import stub should not modify account data");
        assertEquals(custCountBefore, customerRepository.count(),
            "Import stub should not modify customer data");
        assertEquals(tranCountBefore, transactionRepository.count(),
            "Import stub should not modify transaction data");
    }

    // ===== BOUNDARY TESTS =====

    @Test
    @DisplayName("Data import job lifecycle: STARTED → COMPLETED")
    void testJobLifecycle() throws Exception {
        JobExecution execution = jobLauncher.run(dataImportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertNotNull(execution.getStartTime(), "Job should have a start time");
        assertNotNull(execution.getEndTime(), "Job should have an end time");
        assertTrue(execution.getEndTime().isAfter(execution.getStartTime()) ||
                   execution.getEndTime().equals(execution.getStartTime()),
            "End time should be >= start time");
    }

    @Test
    @DisplayName("Data import preserves existing seed data")
    void testImportPreservesSeedData() throws Exception {
        long acctCountBefore = accountRepository.count();
        assertTrue(acctCountBefore > 0, "Seed data should exist before import");

        JobExecution execution = jobLauncher.run(dataImportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        assertEquals(acctCountBefore, accountRepository.count(),
            "Import should preserve existing seed data");
    }
}
