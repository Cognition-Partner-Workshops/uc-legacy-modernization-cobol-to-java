package com.carddemo.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Batch job replacing COBOL program CBTRN03C / JCL job TRANREPT.
 * Generates transaction reports.
 *
 * Original COBOL: app/cbl/CBTRN03C.cbl
 * JCL Job: TRANREPT
 */
@Configuration
public class TransactionReportJob {

    @Bean
    public Job transactionReportBatchJob(JobRepository jobRepository,
                                         Step transactionReportStep) {
        return new JobBuilder("transactionReportJob", jobRepository)
                .start(transactionReportStep)
                .build();
    }

    @Bean
    public Step transactionReportStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionReportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: Implement - migrated from COBOL program CBTRN03C / JCL TRANREPT
                    throw new UnsupportedOperationException(
                            "TODO: Implement - migrated from COBOL program CBTRN03C");
                }, transactionManager)
                .build();
    }
}
