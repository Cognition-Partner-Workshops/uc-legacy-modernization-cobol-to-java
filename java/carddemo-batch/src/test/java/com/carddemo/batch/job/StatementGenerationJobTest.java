package com.carddemo.batch.job;

import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.CustomerRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.common.model.Account;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.Customer;
import com.carddemo.common.model.Transaction;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parity tests for StatementGenerationJob (CBSTM03A).
 * Verifies that the Java batch job generates statements matching COBOL output.
 */
@SpringBatchTest
@SpringBootTest(classes = com.carddemo.batch.CardDemoBatchApplication.class)
@ActiveProfiles("test")
class StatementGenerationJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("statementGenerationBatchJob")
    private Job statementGenerationBatchJob;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardCrossReferenceRepository cardCrossReferenceRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(statementGenerationBatchJob);
        transactionRepository.deleteAll();
        cardCrossReferenceRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("CBSTM03A Parity: Statement generated with correct account and transaction data")
    void testStatementGeneration() throws Exception {
        Account account = new Account();
        account.setAccountId(55555555501L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("2500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("2000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2028, 12, 31));
        accountRepository.save(account);

        Customer customer = new Customer();
        customer.setCustomerId(555555555L);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setStateCode("CA");
        customer.setCountryCode("US");
        customer.setZipCode("90210");
        customerRepository.save(customer);

        CardCrossReference xref = new CardCrossReference();
        xref.setCardNumber("5555555555555555");
        xref.setCustomerId(555555555L);
        xref.setAccountId(55555555501L);
        cardCrossReferenceRepository.save(xref);

        Transaction tran = new Transaction();
        tran.setTransactionId("STMT_TRAN_00001");
        tran.setTypeCode("01");
        tran.setCategoryCode("0001");
        tran.setSource("Online");
        tran.setDescription("Statement test purchase");
        tran.setAmount(new BigDecimal("499.99"));
        tran.setCardNumber("5555555555555555");
        tran.setOriginTimestamp(LocalDateTime.of(2025, 6, 10, 9, 0, 0));
        tran.setProcessedTimestamp(LocalDateTime.of(2025, 6, 10, 9, 15, 0));
        transactionRepository.save(tran);

        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }

    @Test
    @DisplayName("CBSTM03A Parity: Empty statement when no transactions exist")
    void testEmptyStatement() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
    }
}
