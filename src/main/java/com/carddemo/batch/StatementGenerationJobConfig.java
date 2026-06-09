package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

@Configuration
public class StatementGenerationJobConfig {

    private static final Logger log = LoggerFactory.getLogger(StatementGenerationJobConfig.class);

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public StatementGenerationJobConfig(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager,
                                        AccountRepository accountRepository,
                                        CardXrefRepository cardXrefRepository,
                                        TransactionRepository transactionRepository) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job statementGenerationJob(Step statementGenerationStep) {
        return new JobBuilder("statementGenerationJob", jobRepository)
                .start(statementGenerationStep)
                .build();
    }

    @Bean
    public Step statementGenerationStep(RepositoryItemReader<Account> statementAccountReader,
                                        ItemProcessor<Account, String> statementProcessor,
                                        ItemWriter<String> statementWriter) {
        return new StepBuilder("statementGenerationStep", jobRepository)
                .<Account, String>chunk(50, transactionManager)
                .reader(statementAccountReader)
                .processor(statementProcessor)
                .writer(statementWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Account> statementAccountReader() {
        return new RepositoryItemReaderBuilder<Account>()
                .name("statementAccountReader")
                .repository(accountRepository)
                .methodName("findAll")
                .sorts(Map.of("acctId", Sort.Direction.ASC))
                .pageSize(50)
                .build();
    }

    @Bean
    public ItemProcessor<Account, String> statementProcessor() {
        return account -> {
            var cards = cardXrefRepository.findByAcctId(account.getAcctId());
            var sb = new StringBuilder();
            sb.append("=== STATEMENT FOR ACCOUNT: ").append(account.getAcctId()).append(" ===\n");
            sb.append("Balance: ").append(account.getCurrentBalance()).append("\n");
            sb.append("Credit Limit: ").append(account.getCreditLimit()).append("\n");
            sb.append("Transactions:\n");

            for (var card : cards) {
                var transactions = transactionRepository.findByCardNum(
                        card.getCardNum(),
                        org.springframework.data.domain.Pageable.unpaged());
                for (var tran : transactions) {
                    sb.append("  ").append(tran.getOriginTimestamp())
                            .append(" | ").append(tran.getTranDescription())
                            .append(" | ").append(tran.getTranAmount()).append("\n");
                }
            }
            return sb.toString();
        };
    }

    @Bean
    public ItemWriter<String> statementWriter() {
        return chunk -> {
            for (String statement : chunk) {
                log.info("Generated statement:\n{}", statement);
            }
        };
    }
}
