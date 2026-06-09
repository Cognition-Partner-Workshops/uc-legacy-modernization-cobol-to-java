package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.repository.AccountRepository;
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
import java.util.Map;

@Configuration
public class InterestCalculationJobConfig {

    private static final BigDecimal MONTHLY_RATE = new BigDecimal("0.015");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;

    public InterestCalculationJobConfig(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        AccountRepository accountRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountRepository = accountRepository;
    }

    @Bean
    public Job interestCalculationJob(Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(RepositoryItemReader<Account> accountReader,
                                        ItemProcessor<Account, Account> interestProcessor,
                                        ItemWriter<Account> accountWriter) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .<Account, Account>chunk(100, transactionManager)
                .reader(accountReader)
                .processor(interestProcessor)
                .writer(accountWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> accountReader() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("accountReader")
                .repository(accountRepository)
                .methodName("findAll")
                .sorts(Map.of("acctId", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Account, Account> interestProcessor() {
        return account -> {
            if (account.getCurrentBalance() != null
                    && account.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal interest = account.getCurrentBalance()
                        .multiply(MONTHLY_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
                account.setCurrentBalance(account.getCurrentBalance().add(interest));
            }
            return account;
        };
    }

    @Bean
    public ItemWriter<Account> accountWriter() {
        return chunk -> {
            for (Account account : chunk) {
                accountRepository.save(account);
            }
        };
    }
}
