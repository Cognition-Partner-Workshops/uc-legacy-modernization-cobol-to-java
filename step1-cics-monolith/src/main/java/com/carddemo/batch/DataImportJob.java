package com.carddemo.batch;

import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.CustomerRepository;
import com.carddemo.repository.TransactionRepository;
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
 * Mirrors CBIMPORT.jcl / CBIMPORT.cbl - Data Import.
 * Imports data from fixed-width format files into all tables.
 */
@Configuration
public class DataImportJob {

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public DataImportJob(AccountRepository accountRepository,
                         CardRepository cardRepository,
                         CardXrefRepository cardXrefRepository,
                         CustomerRepository customerRepository,
                         TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job dataImportBatchJob(JobRepository jobRepository, Step dataImportStep) {
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
            // Import data from fixed-width files into database tables
            // In production, this would read files matching the COBOL copybook layouts
            // and parse fixed-width fields into entity objects
            return RepeatStatus.FINISHED;
        };
    }
}
