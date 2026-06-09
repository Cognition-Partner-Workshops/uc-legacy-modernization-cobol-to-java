package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.model.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
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
public class PostTransactionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public PostTransactionJobConfig(JobRepository jobRepository,
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
    public Job postTransactionJob(Step postTransactionStep) {
        return new JobBuilder("postTransactionJob", jobRepository)
                .start(postTransactionStep)
                .build();
    }

    @Bean
    public Step postTransactionStep(RepositoryItemReader<Transaction> transactionReader,
                                    ItemProcessor<Transaction, Transaction> postTransactionProcessor,
                                    ItemWriter<Transaction> postTransactionWriter) {
        return new StepBuilder("postTransactionStep", jobRepository)
                .<Transaction, Transaction>chunk(100, transactionManager)
                .reader(transactionReader)
                .processor(postTransactionProcessor)
                .writer(postTransactionWriter)
                .build();
    }

    @Bean
    public RepositoryItemReader<Transaction> transactionReader() {
        return new RepositoryItemReaderBuilder<Transaction>()
                .name("transactionReader")
                .repository(transactionRepository)
                .methodName("findByPostedFalse")
                .sorts(Map.of("tranId", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> postTransactionProcessor() {
        return transaction -> {
            var cardOpt = cardXrefRepository.findById(transaction.getCardNum());
            if (cardOpt.isPresent()) {
                var card = cardOpt.get();
                var accountOpt = accountRepository.findById(card.getAcctId());
                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    if (transaction.getTranAmount() != null) {
                        java.math.BigDecimal balance = account.getCurrentBalance() != null
                                ? account.getCurrentBalance() : java.math.BigDecimal.ZERO;
                        account.setCurrentBalance(balance.add(transaction.getTranAmount()));
                    }
                    accountRepository.save(account);
                }
            }
            transaction.setPosted(true);
            return transaction;
        };
    }

    @Bean
    public ItemWriter<Transaction> postTransactionWriter() {
        return chunk -> {
            for (Transaction t : chunk) {
                transactionRepository.save(t);
            }
        };
    }
}
