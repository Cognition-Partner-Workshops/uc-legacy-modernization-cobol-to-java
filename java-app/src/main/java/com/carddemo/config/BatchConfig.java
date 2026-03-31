package com.carddemo.config;

import com.carddemo.batch.InterestCalculationTasklet;
import com.carddemo.batch.StatementGenerationTasklet;
import com.carddemo.batch.TransactionPostingTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job transactionPostingJob(TransactionPostingTasklet tasklet) {
        Step step = new StepBuilder("postTransactionsStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Job interestCalculationJob(InterestCalculationTasklet tasklet) {
        Step step = new StepBuilder("calculateInterestStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    public Job statementGenerationJob(StatementGenerationTasklet tasklet) {
        Step step = new StepBuilder("generateStatementsStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(step)
                .build();
    }
}
