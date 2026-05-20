package com.carddemo.transaction.batch;

import com.carddemo.transaction.model.DailyTransaction;
import com.carddemo.transaction.model.RejectedTransaction;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.model.TransactionCategoryBalance;
import com.carddemo.transaction.repository.DailyTransactionRepository;
import com.carddemo.transaction.repository.RejectedTransactionRepository;
import com.carddemo.transaction.repository.TransactionCategoryBalanceRepository;
import com.carddemo.transaction.repository.TransactionRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class TransactionPostingJobConfig {

    private final DailyTransactionRepository dailyTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final RejectedTransactionRepository rejectedTransactionRepository;
    private final TransactionCategoryBalanceRepository tranCatBalanceRepository;

    public TransactionPostingJobConfig(
            DailyTransactionRepository dailyTransactionRepository,
            TransactionRepository transactionRepository,
            RejectedTransactionRepository rejectedTransactionRepository,
            TransactionCategoryBalanceRepository tranCatBalanceRepository) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.rejectedTransactionRepository = rejectedTransactionRepository;
        this.tranCatBalanceRepository = tranCatBalanceRepository;
    }

    @Bean
    public Job transactionPostingJob(JobRepository jobRepository, Step postTransactionsStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(postTransactionsStep)
                .build();
    }

    @Bean
    public Step postTransactionsStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager) {
        return new StepBuilder("postTransactionsStep", jobRepository)
                .<DailyTransaction, PostingResult>chunk(10, transactionManager)
                .reader(dailyTransactionReader())
                .processor(transactionPostingProcessor())
                .writer(postingResultWriter())
                .build();
    }

    @Bean
    public ItemReader<DailyTransaction> dailyTransactionReader() {
        return new ListItemReader<>(dailyTransactionRepository.findAll());
    }

    @Bean
    public ItemProcessor<DailyTransaction, PostingResult> transactionPostingProcessor() {
        return dailyTxn -> {
            String procTs = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSSS"));

            Transaction posted = Transaction.builder()
                    .tranId(dailyTxn.getTranId())
                    .typeCd(dailyTxn.getTypeCd())
                    .catCd(dailyTxn.getCatCd())
                    .source(dailyTxn.getSource())
                    .description(dailyTxn.getDescription())
                    .amount(dailyTxn.getAmount())
                    .merchantId(dailyTxn.getMerchantId())
                    .merchantName(dailyTxn.getMerchantName())
                    .merchantCity(dailyTxn.getMerchantCity())
                    .merchantZip(dailyTxn.getMerchantZip())
                    .cardNum(dailyTxn.getCardNum())
                    .origTs(dailyTxn.getOrigTs())
                    .procTs(procTs)
                    .build();

            return PostingResult.valid(posted, dailyTxn.getTypeCd(), dailyTxn.getCatCd(),
                    dailyTxn.getCardNum(), dailyTxn.getAmount());
        };
    }

    @Bean
    public ItemWriter<PostingResult> postingResultWriter() {
        return items -> {
            for (PostingResult result : items) {
                if (result.isValid()) {
                    transactionRepository.save(result.getTransaction());
                } else {
                    rejectedTransactionRepository.save(RejectedTransaction.builder()
                            .tranId(result.getRejectedTranId())
                            .cardNum(result.getRejectedCardNum())
                            .amount(result.getRejectedAmount())
                            .reasonCode(result.getRejectReasonCode())
                            .reasonDesc(result.getRejectReasonDesc())
                            .rejectedAt(LocalDateTime.now())
                            .build());
                }
            }
        };
    }
}
