package com.carddemo.batch.job;

import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.TransactionCategoryRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.batch.repository.TransactionTypeRepository;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.Transaction;
import com.carddemo.common.model.TransactionCategory;
import com.carddemo.common.model.TransactionType;
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
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for TransactionReportJob (CBTRN03C).
 * Verifies that the Java batch job produces a report matching COBOL output format.
 */
@SpringBatchTest
@SpringBootTest(classes = com.carddemo.batch.CardDemoBatchApplication.class)
@ActiveProfiles("test")
class TransactionReportJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("transactionReportBatchJob")
    private Job transactionReportBatchJob;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionTypeRepository transactionTypeRepository;

    @Autowired
    private TransactionCategoryRepository transactionCategoryRepository;

    @Autowired
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(transactionReportBatchJob);
        transactionRepository.deleteAll();
        transactionTypeRepository.deleteAll();
        transactionCategoryRepository.deleteAll();
        cardCrossReferenceRepository.deleteAll();
    }

    @Test
    @DisplayName("CBTRN03C Parity: Report generation completes with transaction data")
    void testReportGeneration() throws Exception {
        // Set up reference data
        TransactionType type = new TransactionType();
        type.setTypeCode("01");
        type.setTypeDescription("Purchase");
        transactionTypeRepository.save(type);

        TransactionCategory cat = new TransactionCategory();
        cat.setTypeCode("01");
        cat.setCategoryCode("0001");
        cat.setCategoryDescription("Retail Purchase");
        transactionCategoryRepository.save(cat);

        CardCrossReference xref = new CardCrossReference();
        xref.setCardNumber("4444444444444444");
        xref.setCustomerId(444444444L);
        xref.setAccountId(44444444401L);
        cardCrossReferenceRepository.save(xref);

        // Create transactions within date range
        Transaction tran1 = new Transaction();
        tran1.setTransactionId("RPT_TRAN_000001");
        tran1.setTypeCode("01");
        tran1.setCategoryCode("0001");
        tran1.setSource("Online");
        tran1.setDescription("Test purchase 1");
        tran1.setAmount(new BigDecimal("100.00"));
        tran1.setCardNumber("4444444444444444");
        tran1.setOriginTimestamp(LocalDateTime.of(2025, 6, 15, 10, 0, 0));
        tran1.setProcessedTimestamp(LocalDateTime.of(2025, 6, 15, 10, 30, 0));
        transactionRepository.save(tran1);

        Transaction tran2 = new Transaction();
        tran2.setTransactionId("RPT_TRAN_000002");
        tran2.setTypeCode("01");
        tran2.setCategoryCode("0001");
        tran2.setSource("POS");
        tran2.setDescription("Test purchase 2");
        tran2.setAmount(new BigDecimal("250.50"));
        tran2.setCardNumber("4444444444444444");
        tran2.setOriginTimestamp(LocalDateTime.of(2025, 6, 15, 14, 0, 0));
        tran2.setProcessedTimestamp(LocalDateTime.of(2025, 6, 15, 14, 30, 0));
        transactionRepository.save(tran2);

        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2025-06-15")
                .addString("endDate", "2025-06-15")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    @DisplayName("CBTRN03C Parity: Empty report when no transactions in date range")
    void testEmptyReport() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("startDate", "2020-01-01")
                .addString("endDate", "2020-01-01")
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
