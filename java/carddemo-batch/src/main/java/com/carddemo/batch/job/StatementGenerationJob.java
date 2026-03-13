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
 * Batch job replacing COBOL program CBSTM03A / JCL job CREASTMT.
 * Generates account statements.
 *
 * Original COBOL: app/cbl/CBSTM03A.CBL
 * JCL Job: CREASTMT
 */
@Configuration
public class StatementGenerationJob {

    @Bean
    public Job statementGenerationBatchJob(JobRepository jobRepository,
                                           Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: Implement - migrated from COBOL program CBSTM03A / JCL CREASTMT
                    throw new UnsupportedOperationException(
                            "TODO: Implement - migrated from COBOL program CBSTM03A");
                }, transactionManager)
                .build();
    }
}
