package com.carddemo.batch;

import com.carddemo.model.entity.Transaction;
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
 * Boundary and risk-based tests for CBTRN03C.cbl (Transaction Report Batch Job).
 * Covers: report generation with transactions, empty transaction table,
 * large transaction set, transactions with various types and amounts.
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionReportJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("transactionReportBatchJob")
    private Job transactionReportBatchJob;

    @Autowired
    private TransactionRepository transactionRepository;

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Transaction report job completes successfully with seed data")
    void testJobCompletesWithSeedData() throws Exception {
        JobExecution execution = jobLauncher.run(transactionReportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    @DisplayName("Transaction report job completes with empty transaction table")
    void testJobCompletesWithEmptyTable() throws Exception {
        List<Transaction> saved = transactionRepository.findAll();
        transactionRepository.deleteAll();

        try {
            JobExecution execution = jobLauncher.run(transactionReportBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Report job should complete with no transactions");
        } finally {
            transactionRepository.saveAll(saved);
        }
    }

    // ===== BOUNDARY TESTS =====

    @Test
    @DisplayName("Transaction report processes transactions with various amounts")
    void testReportWithVariousAmounts() throws Exception {
        // Add transactions with boundary amounts
        Transaction zerAmt = new Transaction();
        zerAmt.setTranId("RPT_TEST_000001");
        zerAmt.setTranTypeCd("01");
        zerAmt.setTranCatCd(1);
        zerAmt.setTranAmt(BigDecimal.ZERO);
        zerAmt.setTranDesc("Zero Amount");
        transactionRepository.save(zerAmt);

        Transaction negAmt = new Transaction();
        negAmt.setTranId("RPT_TEST_000002");
        negAmt.setTranTypeCd("02");
        negAmt.setTranCatCd(1);
        negAmt.setTranAmt(new BigDecimal("-999.99"));
        negAmt.setTranDesc("Negative Amount");
        transactionRepository.save(negAmt);

        Transaction largeAmt = new Transaction();
        largeAmt.setTranId("RPT_TEST_000003");
        largeAmt.setTranTypeCd("01");
        largeAmt.setTranCatCd(1);
        largeAmt.setTranAmt(new BigDecimal("9999999999.99"));
        largeAmt.setTranDesc("Large Amount");
        transactionRepository.save(largeAmt);

        try {
            JobExecution execution = jobLauncher.run(transactionReportBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Report should handle various transaction amounts");
        } finally {
            transactionRepository.deleteById("RPT_TEST_000001");
            transactionRepository.deleteById("RPT_TEST_000002");
            transactionRepository.deleteById("RPT_TEST_000003");
        }
    }

    @Test
    @DisplayName("Transaction report handles transactions with null fields")
    void testReportWithNullFields() throws Exception {
        Transaction nullFields = new Transaction();
        nullFields.setTranId("RPT_TEST_000004");
        nullFields.setTranTypeCd(null);
        nullFields.setTranCatCd(null);
        nullFields.setTranAmt(null);
        nullFields.setTranDesc(null);
        nullFields.setTranCardNum(null);
        transactionRepository.save(nullFields);

        try {
            JobExecution execution = jobLauncher.run(transactionReportBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Report should handle null transaction fields");
        } finally {
            transactionRepository.deleteById("RPT_TEST_000004");
        }
    }

    // ===== RISK-BASED TESTS =====

    @Test
    @DisplayName("Transaction report handles all transaction types")
    void testReportWithAllTransactionTypes() throws Exception {
        String[] types = {"01", "02", "03", "04", "05", "06", "07"};
        for (int i = 0; i < types.length; i++) {
            Transaction t = new Transaction();
            t.setTranId(String.format("RPT_TEST_%06d", 10 + i));
            t.setTranTypeCd(types[i]);
            t.setTranCatCd(1);
            t.setTranAmt(new BigDecimal("100.00"));
            t.setTranDesc("Type " + types[i] + " transaction");
            transactionRepository.save(t);
        }

        try {
            JobExecution execution = jobLauncher.run(transactionReportBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Report should handle all transaction types");
        } finally {
            for (int i = 0; i < types.length; i++) {
                transactionRepository.deleteById(String.format("RPT_TEST_%06d", 10 + i));
            }
        }
    }

    @Test
    @DisplayName("Transaction report is idempotent — running twice doesn't change data")
    void testReportIdempotent() throws Exception {
        long countBefore = transactionRepository.count();

        jobLauncher.run(transactionReportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());

        long countAfterFirst = transactionRepository.count();
        assertEquals(countBefore, countAfterFirst,
            "Report job should not modify transaction data");

        jobLauncher.run(transactionReportBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis() + 1).toJobParameters());

        long countAfterSecond = transactionRepository.count();
        assertEquals(countBefore, countAfterSecond,
            "Running report twice should not change data");
    }
}
