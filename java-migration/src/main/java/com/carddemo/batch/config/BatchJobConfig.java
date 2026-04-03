package com.carddemo.batch.config;

import com.carddemo.batch.job.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring Batch configuration for CardDemo batch jobs.
 * Each COBOL batch program maps to a Step within the cardDemoBatchJob.
 */
@Configuration
public class BatchJobConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchJobConfig.class);

    @Value("${carddemo.data.dir:./data}")
    private String dataDir;

    @Bean
    public Job accountFileJob(JobRepository jobRepository, Step accountFileStep) {
        return new JobBuilder("accountFileJob", jobRepository)
                .start(accountFileStep)
                .build();
    }

    @Bean
    public Step accountFileStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Path base = Paths.get(dataDir);
            AccountFileProcessor processor = new AccountFileProcessor(
                    base.resolve("acctdata.txt"),
                    base.resolve("output/acctout.txt"),
                    base.resolve("output/arryout.txt"),
                    base.resolve("output/vbrcout.txt"));
            processor.execute();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("accountFileStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Job cardFileJob(JobRepository jobRepository, Step cardFileStep) {
        return new JobBuilder("cardFileJob", jobRepository)
                .start(cardFileStep)
                .build();
    }

    @Bean
    public Step cardFileStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Path base = Paths.get(dataDir);
            CardFileProcessor processor = new CardFileProcessor(base.resolve("carddata.txt"));
            processor.execute();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("cardFileStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Job xrefFileJob(JobRepository jobRepository, Step xrefFileStep) {
        return new JobBuilder("xrefFileJob", jobRepository)
                .start(xrefFileStep)
                .build();
    }

    @Bean
    public Step xrefFileStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Path base = Paths.get(dataDir);
            CrossReferenceFileProcessor processor =
                    new CrossReferenceFileProcessor(base.resolve("cardxref.txt"));
            processor.execute();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("xrefFileStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }

    @Bean
    public Job customerFileJob(JobRepository jobRepository, Step customerFileStep) {
        return new JobBuilder("customerFileJob", jobRepository)
                .start(customerFileStep)
                .build();
    }

    @Bean
    public Step customerFileStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        Tasklet tasklet = (contribution, chunkContext) -> {
            Path base = Paths.get(dataDir);
            CustomerFileProcessor processor =
                    new CustomerFileProcessor(base.resolve("custdata.txt"));
            processor.execute();
            return RepeatStatus.FINISHED;
        };
        return new StepBuilder("customerFileStep", jobRepository)
                .tasklet(tasklet, txManager)
                .build();
    }
}
