package com.carddemo.batch;

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

/**
 * Spring Batch job replacing JCL TRANREPT.jcl and COBOL program CBTRN03C.
 *
 * <p>CBTRN03C generates a formatted transaction report with:
 * - Summary by transaction type
 * - Summary by transaction category
 * - Detail lines sorted by date
 *
 * <p>Phase 2 will implement the full report generation logic.
 */
@Configuration
public class TransactionReportJobConfig {

    @Bean
    public Job transactionReportJob(JobRepository jobRepository,
                                     Step generateTransactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(generateTransactionReportStep)
                .build();
    }

    @Bean
    public Step generateTransactionReportStep(JobRepository jobRepository,
                                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("generateTransactionReportStep", jobRepository)
                .tasklet(transactionReportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet transactionReportTasklet() {
        return (contribution, chunkContext) -> {
            // Phase 2: implement transaction report generation from CBTRN03C
            return RepeatStatus.FINISHED;
        };
    }
}
