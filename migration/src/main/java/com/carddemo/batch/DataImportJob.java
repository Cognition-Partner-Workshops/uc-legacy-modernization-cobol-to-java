package com.carddemo.batch;

import com.carddemo.service.DataMigrationService;
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

/**
 * Replaces CBIMPORT JCL - Data import batch job.
 */
@Configuration
public class DataImportJob {

    private static final Logger log = LoggerFactory.getLogger(DataImportJob.class);

    private final DataMigrationService dataMigrationService;

    public DataImportJob(DataMigrationService dataMigrationService) {
        this.dataMigrationService = dataMigrationService;
    }

    @Bean
    public Job dataImportJob(JobRepository jobRepository, Step dataImportStep) {
        return new JobBuilder("dataImportJob", jobRepository)
                .start(dataImportStep)
                .build();
    }

    @Bean
    public Step dataImportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataImportStep", jobRepository)
                .tasklet(dataImportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet dataImportTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Data import job started - use REST API to provide data");
            return RepeatStatus.FINISHED;
        };
    }
}
