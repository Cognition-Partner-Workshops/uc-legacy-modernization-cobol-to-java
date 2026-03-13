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
 * Batch job replacing COBOL program CBIMPORT / JCL job CBIMPORT.
 * Imports data from flat files into VSAM files (database tables).
 *
 * Original COBOL: app/cbl/CBIMPORT.cbl
 * JCL Job: CBIMPORT
 */
@Configuration
public class DataImportJob {

    @Bean
    public Job dataImportBatchJob(JobRepository jobRepository,
                                  Step dataImportStep) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(dataImportStep)
                .build();
    }

    @Bean
    public Step dataImportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataImportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: Implement - migrated from COBOL program CBIMPORT
                    throw new UnsupportedOperationException(
                            "TODO: Implement - migrated from COBOL program CBIMPORT");
                }, transactionManager)
                .build();
    }
}
