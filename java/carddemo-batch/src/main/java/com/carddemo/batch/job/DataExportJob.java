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
 * Batch job replacing COBOL program CBEXPORT / JCL job CBEXPORT.
 * Exports data from VSAM files to flat files.
 *
 * Original COBOL: app/cbl/CBEXPORT.cbl
 * JCL Job: CBEXPORT
 */
@Configuration
public class DataExportJob {

    @Bean
    public Job dataExportBatchJob(JobRepository jobRepository,
                                  Step dataExportStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(dataExportStep)
                .build();
    }

    @Bean
    public Step dataExportStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataExportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: Implement - migrated from COBOL program CBEXPORT
                    throw new UnsupportedOperationException(
                            "TODO: Implement - migrated from COBOL program CBEXPORT");
                }, transactionManager)
                .build();
    }
}
