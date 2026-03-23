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

import java.util.Map;

/**
 * Replaces CBEXPORT JCL - Data export batch job.
 */
@Configuration
public class DataExportJob {

    private static final Logger log = LoggerFactory.getLogger(DataExportJob.class);

    private final DataMigrationService dataMigrationService;

    public DataExportJob(DataMigrationService dataMigrationService) {
        this.dataMigrationService = dataMigrationService;
    }

    @Bean
    public Job dataExportJob(JobRepository jobRepository, Step dataExportStep) {
        return new JobBuilder("dataExportJob", jobRepository)
                .start(dataExportStep)
                .build();
    }

    @Bean
    public Step dataExportStep(JobRepository jobRepository,
                                PlatformTransactionManager transactionManager) {
        return new StepBuilder("dataExportStep", jobRepository)
                .tasklet(dataExportTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet dataExportTasklet() {
        return (contribution, chunkContext) -> {
            Map<String, Object> data = dataMigrationService.exportAll();
            log.info("Data export completed. Entity types exported: {}", data.size());
            return RepeatStatus.FINISHED;
        };
    }
}
