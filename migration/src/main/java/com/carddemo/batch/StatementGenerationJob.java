package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.service.StatementGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Replaces CREASTMT JCL / CBSTM03A.CBL monthly batch job.
 * Generates statements for all accounts.
 */
@Configuration
public class StatementGenerationJob {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationJob.class);

    private final AccountRepository accountRepository;
    private final StatementGenerationService statementGenerationService;

    public StatementGenerationJob(AccountRepository accountRepository,
                                   StatementGenerationService statementGenerationService) {
        this.accountRepository = accountRepository;
        this.statementGenerationService = statementGenerationService;
    }

    @Bean
    public Job generateStatementsJob(JobRepository jobRepository, Step generateStatementsStep) {
        return new JobBuilder("generateStatementsJob", jobRepository)
                .start(generateStatementsStep)
                .build();
    }

    @Bean
    public Step generateStatementsStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("generateStatementsStep", jobRepository)
                .tasklet(statementGenerationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet statementGenerationTasklet() {
        return (contribution, chunkContext) -> {
            List<Account> accounts = accountRepository.findAll();
            for (Account account : accounts) {
                statementGenerationService.generateStatement(account.getAcctId());
            }
            log.info("Statements generated for {} accounts", accounts.size());
            return RepeatStatus.FINISHED;
        };
    }
}
