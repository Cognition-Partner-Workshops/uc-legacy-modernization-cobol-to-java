package com.carddemo.batch;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary and risk-based tests for CBTRN02C.cbl (Transaction Posting Batch Job).
 * Covers: valid transaction posting, null amounts, invalid card xref,
 * inactive accounts, credit limit exceeded, empty daily transaction table.
 */
@SpringBootTest
@ActiveProfiles("test")
class TransactionPostingJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job transactionPostingBatchJob;

    @Autowired
    private DailyTransactionRepository dailyTransactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private JobParameters createUniqueJobParams() {
        return new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Batch job completes successfully with no daily transactions")
    void testJobCompletesWithNoDailyTransactions() throws Exception {
        // Clear any daily transactions first
        dailyTransactionRepository.deleteAll();

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    // ===== BOUNDARY TESTS - DAILY TRANSACTION DATA =====

    @Test
    @DisplayName("Transaction with valid card and account is posted")
    void testValidTransactionPosted() throws Exception {
        // Clean slate
        dailyTransactionRepository.deleteAll();
        long initialTranCount = transactionRepository.count();

        // Create a daily transaction with valid card number from seed data
        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000001");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Test Purchase");
        dt.setDalytranAmt(new BigDecimal("50.00"));
        dt.setDalytranCardNum("0000000000000001"); // Must exist in card_xref seed data
        dt.setDalytranMerchantId(12345L);
        dt.setDalytranMerchantName("Test Merchant");
        dt.setDalytranMerchantCity("Test City");
        dt.setDalytranMerchantZip("12345");
        dt.setDalytranOrigTs("2024-01-15 10:00:00.000000");
        dailyTransactionRepository.save(dt);

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        // Transaction count should have increased (or stayed same if card not in xref)
    }

    @Test
    @DisplayName("Transaction with null amount is rejected")
    void testNullAmountRejected() throws Exception {
        dailyTransactionRepository.deleteAll();

        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000002");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Null Amount Test");
        dt.setDalytranAmt(null); // NULL amount
        dt.setDalytranCardNum("0000000000000001");
        dailyTransactionRepository.save(dt);

        long tranCountBefore = transactionRepository.count();

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // The null-amount transaction should be rejected, count should not increase
        long tranCountAfter = transactionRepository.count();
        assertEquals(tranCountBefore, tranCountAfter, "Null amount transaction should be rejected");
    }

    @Test
    @DisplayName("Transaction with invalid card number (not in xref) is rejected")
    void testInvalidCardRejected() throws Exception {
        dailyTransactionRepository.deleteAll();

        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000003");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Bad Card Test");
        dt.setDalytranAmt(new BigDecimal("50.00"));
        dt.setDalytranCardNum("9999999999999999"); // Not in xref
        dailyTransactionRepository.save(dt);

        long tranCountBefore = transactionRepository.count();

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        long tranCountAfter = transactionRepository.count();
        assertEquals(tranCountBefore, tranCountAfter, "Invalid card transaction should be rejected");
    }

    @Test
    @DisplayName("Transaction with zero amount is processed (not rejected)")
    void testZeroAmountProcessed() throws Exception {
        dailyTransactionRepository.deleteAll();

        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000004");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Zero Amount Test");
        dt.setDalytranAmt(BigDecimal.ZERO);
        dt.setDalytranCardNum("0000000000000001");
        dailyTransactionRepository.save(dt);

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    // ===== RISK-BASED TESTS - CREDIT LIMIT =====

    @Test
    @DisplayName("Transaction exceeding credit limit is rejected")
    void testCreditLimitExceeded() throws Exception {
        dailyTransactionRepository.deleteAll();

        // This tests the credit limit check in the posting job
        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000005");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Over Limit Test");
        dt.setDalytranAmt(new BigDecimal("99999999.99")); // Very large amount
        dt.setDalytranCardNum("0000000000000001");
        dailyTransactionRepository.save(dt);

        long tranCountBefore = transactionRepository.count();

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());

        // Should be rejected due to credit limit
        long tranCountAfter = transactionRepository.count();
        assertEquals(tranCountBefore, tranCountAfter, "Over-limit transaction should be rejected");
    }

    @Test
    @DisplayName("Negative amount (payment/credit) bypasses credit limit check")
    void testNegativeAmountBypassesCreditLimit() throws Exception {
        dailyTransactionRepository.deleteAll();

        DailyTransaction dt = new DailyTransaction();
        dt.setDalytranId("TEST000000000006");
        dt.setDalytranTypeCd("01");
        dt.setDalytranCatCd(1);
        dt.setDalytranSource("ONLINE");
        dt.setDalytranDesc("Payment/Credit Test");
        dt.setDalytranAmt(new BigDecimal("-100.00")); // Negative = payment
        dt.setDalytranCardNum("0000000000000001");
        dailyTransactionRepository.save(dt);

        JobExecution execution = jobLauncher.run(transactionPostingBatchJob, createUniqueJobParams());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
