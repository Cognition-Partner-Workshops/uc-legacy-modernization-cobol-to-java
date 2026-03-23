package com.carddemo.batch;

import com.carddemo.service.InterestCalculationService;
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

import java.time.LocalDate;

/**
 * Replaces INTCALC JCL / CBACT04C.cbl monthly batch job.
 * Calculates interest for all accounts based on category balances.
 */
@Configuration
public class InterestCalculationJob {

    private final InterestCalculationService interestCalculationService;

    public InterestCalculationJob(InterestCalculationService interestCalculationService) {
        this.interestCalculationService = interestCalculationService;
    }

    @Bean
    public Job calculateInterestJob(JobRepository jobRepository, Step calculateInterestStep) {
        return new JobBuilder("calculateInterestJob", jobRepository)
                .start(calculateInterestStep)
                .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("calculateInterestStep", jobRepository)
                .tasklet(interestCalculationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet interestCalculationTasklet() {
        return (contribution, chunkContext) -> {
            interestCalculationService.calculateInterest(LocalDate.now());
            return RepeatStatus.FINISHED;
        };
    }
}
