package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.TransactionIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Interest calculation batch job - replaces CBACT04C.
 * Original COBOL: reads all accounts, calculates monthly interest on current balance,
 * creates interest transaction, updates account balance.
 */
@Configuration
public class InterestCalculationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJobConfig.class);
    private static final BigDecimal MONTHLY_INTEREST_RATE = new BigDecimal("0.015"); // 1.5% monthly

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;

    public InterestCalculationJobConfig(AccountRepository accountRepository,
                                         TransactionRepository transactionRepository,
                                         TransactionIdGenerator idGenerator) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
    }

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository, Step calculateInterestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(calculateInterestStep)
                .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("calculateInterestStep", jobRepository)
                .<Account, Account>chunk(50, transactionManager)
                .reader(accountReader())
                .processor(interestProcessor())
                .writer(interestWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> accountReader() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReader")
                .repository(accountRepository)
                .methodName("findAll")
                .sorts(Map.of("accountId", Sort.Direction.ASC))
                .pageSize(50)
                .build();
    }

    @Bean
    public ItemProcessor<Account, Account> interestProcessor() {
        return account -> {
            if (!"Y".equals(account.getActiveStatus())) {
                return null; // Skip inactive accounts
            }
            if (account.getCurrentBalance().compareTo(BigDecimal.ZERO) <= 0) {
                return null; // No interest on zero/negative balance
            }
            return account;
        };
    }

    @Bean
    public ItemWriter<Account> interestWriter() {
        return accounts -> {
            for (Account account : accounts) {
                BigDecimal interest = account.getCurrentBalance()
                        .multiply(MONTHLY_INTEREST_RATE)
                        .setScale(2, RoundingMode.HALF_UP);

                // Create interest transaction
                Transaction interestTran = new Transaction();
                interestTran.setTransactionId(idGenerator.generateNextId());
                interestTran.setTypeCode("IC");
                interestTran.setCategoryCode(9000);
                interestTran.setSource("BATCH");
                interestTran.setDescription("Monthly Interest Charge");
                interestTran.setAmount(interest);
                interestTran.setOriginatedTs(LocalDateTime.now());
                interestTran.setProcessedTs(LocalDateTime.now());
                transactionRepository.save(interestTran);

                // Update account balance
                account.setCurrentBalance(account.getCurrentBalance().add(interest));
                accountRepository.save(account);

                log.info("Applied interest {} to account {}", interest, account.getAccountId());
            }
        };
    }
}
