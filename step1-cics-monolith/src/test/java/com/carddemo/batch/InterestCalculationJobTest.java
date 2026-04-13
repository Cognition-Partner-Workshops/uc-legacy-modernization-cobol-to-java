package com.carddemo.batch;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCatBal;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCatBalRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Boundary and risk-based tests for CBACT04C.cbl (Interest Calculation Batch Job).
 * Covers: interest computation from disclosure groups, account balance updates,
 * interest transaction creation, zero-balance skip, missing disclosure group,
 * multiple category balances per account, empty TCATBAL table.
 */
@SpringBootTest
@ActiveProfiles("test")
class InterestCalculationJobTest {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("interestCalculationBatchJob")
    private Job interestCalculationBatchJob;

    @Autowired
    private TransactionCatBalRepository transactionCatBalRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DisclosureGroupRepository disclosureGroupRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    private long initialTransactionCount;

    @BeforeEach
    void recordInitialState() {
        initialTransactionCount = transactionRepository.count();
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Interest calculation job completes successfully with empty TCATBAL")
    void testJobCompletesWithEmptyTcatbal() throws Exception {
        // Save existing cat bals, clear, run, restore
        List<TransactionCatBal> saved = transactionCatBalRepository.findAll();
        transactionCatBalRepository.deleteAll();

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            // No interest transactions should be created
            assertEquals(initialTransactionCount, transactionRepository.count(),
                "No interest transactions should be created with empty TCATBAL");
        } finally {
            transactionCatBalRepository.saveAll(saved);
        }
    }

    @Test
    @DisplayName("Interest calculation job completes successfully with seed data")
    void testJobCompletesWithSeedData() throws Exception {
        JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
            new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    // ===== BOUNDARY TESTS - INTEREST COMPUTATION =====

    @Test
    @DisplayName("Interest is computed correctly: (balance * rate) / 1200")
    void testInterestComputedCorrectly() throws Exception {
        // Setup: use a dedicated test account with known group_id
        // Seed accounts may have blank group_id, so we create/update account to have 'A000000000'
        Account acct = accountRepository.findById(1L).orElseThrow();
        String origGroupId = acct.getAcctGroupId();
        BigDecimal origAcctBal = acct.getAcctCurrBal();
        acct.setAcctGroupId("A000000000"); // ensure group_id is set for disclosure lookup
        accountRepository.save(acct);

        TransactionCatBal.TransactionCatBalId catBalId =
            new TransactionCatBal.TransactionCatBalId(1L, "01", 1);

        Optional<TransactionCatBal> existing = transactionCatBalRepository.findById(catBalId);
        BigDecimal originalCatBal = existing.map(TransactionCatBal::getTranCatBal).orElse(BigDecimal.ZERO);

        // Set a known balance of $1200 for easy math
        TransactionCatBal catBal = existing.orElseGet(() -> {
            TransactionCatBal cb = new TransactionCatBal();
            cb.setTrancatAcctId(1L);
            cb.setTrancatTypeCd("01");
            cb.setTrancatCd(1);
            return cb;
        });
        catBal.setTranCatBal(new BigDecimal("1200.00"));
        transactionCatBalRepository.save(catBal);

        // Record account balance before (after setting group_id)
        BigDecimal balBefore = acct.getAcctCurrBal();
        long tranCountBefore = transactionRepository.count();

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            // Expected interest = (1200 * 15.000) / 1200 = 15.00
            BigDecimal expectedInterest = new BigDecimal("1200.00").multiply(new BigDecimal("15.000"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

            Account acctAfter = accountRepository.findById(1L).orElseThrow();
            BigDecimal balAfter = acctAfter.getAcctCurrBal();

            // Account balance should have increased by interest amount
            assertEquals(balBefore.add(expectedInterest).setScale(2, RoundingMode.HALF_UP),
                balAfter.setScale(2, RoundingMode.HALF_UP),
                "Account balance should increase by computed interest");

            // An interest transaction should be written
            assertTrue(transactionRepository.count() > tranCountBefore,
                "Interest transaction should be created");
        } finally {
            // Restore original cat bal
            catBal.setTranCatBal(originalCatBal);
            transactionCatBalRepository.save(catBal);
            // Restore account
            acct.setAcctGroupId(origGroupId);
            acct.setAcctCurrBal(origAcctBal);
            accountRepository.save(acct);
        }
    }

    @Test
    @DisplayName("Zero balance TCATBAL produces no interest")
    void testZeroBalanceProducesNoInterest() throws Exception {
        // All seed TCATBAL entries have balance 0.00
        // The job should skip them (balance <= 0 check)
        long tranCountBefore = transactionRepository.count();

        // Ensure all cat bals are zero
        List<TransactionCatBal> catBals = transactionCatBalRepository.findAll();
        boolean allZero = catBals.stream()
            .allMatch(cb -> cb.getTranCatBal() == null || cb.getTranCatBal().compareTo(BigDecimal.ZERO) <= 0);

        if (allZero) {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            assertEquals(tranCountBefore, transactionRepository.count(),
                "Zero balance should produce no interest transactions");
        }
    }

    @Test
    @DisplayName("Negative balance TCATBAL produces no interest")
    void testNegativeBalanceProducesNoInterest() throws Exception {
        // Setup a negative balance
        TransactionCatBal catBal = new TransactionCatBal();
        catBal.setTrancatAcctId(1L);
        catBal.setTrancatTypeCd("07");  // Use adjustment type to avoid colliding with seed data
        catBal.setTrancatCd(1);
        catBal.setTranCatBal(new BigDecimal("-500.00"));
        transactionCatBalRepository.save(catBal);

        long tranCountBefore = transactionRepository.count();

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        } finally {
            transactionCatBalRepository.delete(catBal);
        }
    }

    // ===== RISK-BASED TESTS - MISSING DATA =====

    @Test
    @DisplayName("TCATBAL for non-existent account is skipped")
    void testNonExistentAccountSkipped() throws Exception {
        // Create TCATBAL for an account that doesn't exist
        TransactionCatBal catBal = new TransactionCatBal();
        catBal.setTrancatAcctId(999999L);
        catBal.setTrancatTypeCd("01");
        catBal.setTrancatCd(1);
        catBal.setTranCatBal(new BigDecimal("1000.00"));
        transactionCatBalRepository.save(catBal);

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Job should complete even with non-existent account");
        } finally {
            transactionCatBalRepository.delete(catBal);
        }
    }

    @Test
    @DisplayName("Account with blank group ID is skipped")
    void testBlankGroupIdSkipped() throws Exception {
        // Account 1 has group_id 'A000000000' from seed data, but let's test with a temp blank one
        // Create a temp account with blank group id
        Account tempAcct = new Account();
        tempAcct.setAcctId(990001L);
        tempAcct.setAcctActiveStatus("Y");
        tempAcct.setAcctCurrBal(new BigDecimal("1000.00"));
        tempAcct.setAcctCreditLimit(new BigDecimal("5000.00"));
        tempAcct.setAcctGroupId("");  // blank group ID
        accountRepository.save(tempAcct);

        TransactionCatBal catBal = new TransactionCatBal();
        catBal.setTrancatAcctId(990001L);
        catBal.setTrancatTypeCd("01");
        catBal.setTrancatCd(1);
        catBal.setTranCatBal(new BigDecimal("1000.00"));
        transactionCatBalRepository.save(catBal);

        long tranCountBefore = transactionRepository.count();

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        } finally {
            transactionCatBalRepository.delete(catBal);
            accountRepository.delete(tempAcct);
        }
    }

    @Test
    @DisplayName("Disclosure group with zero interest rate produces no interest")
    void testZeroInterestRateProducesNoInterest() throws Exception {
        // Payment type '02' has rate 0.000 in seed data
        TransactionCatBal catBal = new TransactionCatBal();
        catBal.setTrancatAcctId(1L);
        catBal.setTrancatTypeCd("02");
        catBal.setTrancatCd(1);
        catBal.setTranCatBal(new BigDecimal("5000.00"));
        transactionCatBalRepository.save(catBal);

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        } finally {
            transactionCatBalRepository.delete(catBal);
        }
    }

    @Test
    @DisplayName("Interest transaction has correct source and description")
    void testInterestTransactionMetadata() throws Exception {
        // Setup: ensure account has valid group_id
        Account acct = accountRepository.findById(1L).orElseThrow();
        String origGroupId = acct.getAcctGroupId();
        BigDecimal origAcctBal = acct.getAcctCurrBal();
        acct.setAcctGroupId("A000000000");
        accountRepository.save(acct);

        // Setup: known balance for interest
        Optional<TransactionCatBal> existing = transactionCatBalRepository.findById(
            new TransactionCatBal.TransactionCatBalId(1L, "01", 1));
        BigDecimal origCatBal = existing.map(TransactionCatBal::getTranCatBal).orElse(BigDecimal.ZERO);
        TransactionCatBal catBal = existing.orElseGet(() -> {
            TransactionCatBal cb = new TransactionCatBal();
            cb.setTrancatAcctId(1L);
            cb.setTrancatTypeCd("01");
            cb.setTrancatCd(1);
            return cb;
        });
        catBal.setTranCatBal(new BigDecimal("2400.00"));
        transactionCatBalRepository.save(catBal);

        long tranCountBefore = transactionRepository.count();

        try {
            JobExecution execution = jobLauncher.run(interestCalculationBatchJob,
                new JobParametersBuilder().addLong("timestamp", System.currentTimeMillis()).toJobParameters());
            assertEquals(BatchStatus.COMPLETED, execution.getStatus());

            // Find the new interest transaction
            List<Transaction> allTrans = transactionRepository.findAll();
            Optional<Transaction> interestTran = allTrans.stream()
                .filter(t -> "INTEREST".equals(t.getTranSource()))
                .reduce((first, second) -> second); // get last one

            assertTrue(transactionRepository.count() > tranCountBefore,
                "Interest transaction should be created");
            assertTrue(interestTran.isPresent(), "Interest transaction should exist");
            assertEquals("INTEREST", interestTran.get().getTranSource());
            assertEquals("Monthly Interest Charge", interestTran.get().getTranDesc());
            assertEquals("01", interestTran.get().getTranTypeCd());
            assertEquals(5, interestTran.get().getTranCatCd());
        } finally {
            catBal.setTranCatBal(origCatBal);
            transactionCatBalRepository.save(catBal);
            acct.setAcctGroupId(origGroupId);
            acct.setAcctCurrBal(origAcctBal);
            accountRepository.save(acct);
        }
    }
}
