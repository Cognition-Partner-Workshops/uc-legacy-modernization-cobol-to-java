package com.carddemo.batch;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DailyTransaction;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCatBal;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionCatBalRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionIdService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Mirrors POSTTRAN.jcl / CBTRN02C.cbl - Transaction Posting.
 * Read daily transactions sequentially, validate card via XREF, validate account
 * (check credit limit, check expiration), if valid: update TCATBAL, update account
 * balance, write to TRANSACT file; if invalid: write to rejects file.
 */
@Configuration
public class TransactionPostingJob {

    private final DailyTransactionRepository dailyTransactionRepository;
    private final CardXrefRepository cardXrefRepository;
    private final AccountRepository accountRepository;
    private final TransactionCatBalRepository transactionCatBalRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdService transactionIdService;

    public TransactionPostingJob(DailyTransactionRepository dailyTransactionRepository,
                                 CardXrefRepository cardXrefRepository,
                                 AccountRepository accountRepository,
                                 TransactionCatBalRepository transactionCatBalRepository,
                                 TransactionRepository transactionRepository,
                                 TransactionIdService transactionIdService) {
        this.dailyTransactionRepository = dailyTransactionRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.accountRepository = accountRepository;
        this.transactionCatBalRepository = transactionCatBalRepository;
        this.transactionRepository = transactionRepository;
        this.transactionIdService = transactionIdService;
    }

    @Bean
    public Job transactionPostingBatchJob(JobRepository jobRepository, Step transactionPostingStep) {
        return new JobBuilder("transactionPostingJob", jobRepository)
                .start(transactionPostingStep)
                .build();
    }

    @Bean
    public Step transactionPostingStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("transactionPostingStep", jobRepository)
                .tasklet(transactionPostingTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet transactionPostingTasklet() {
        return (contribution, chunkContext) -> {
            List<DailyTransaction> dailyTransactions =
                dailyTransactionRepository.findAllByOrderByDalytranIdAsc();

            int processed = 0;
            int rejected = 0;

            for (DailyTransaction dt : dailyTransactions) {
                // Validate card via XREF
                Optional<CardXref> xref = cardXrefRepository.findByXrefCardNum(dt.getDalytranCardNum());
                if (xref.isEmpty()) {
                    rejected++;
                    continue;
                }

                // Validate account
                long acctId = xref.get().getXrefAcctId();
                Optional<Account> acctOpt = accountRepository.findById(acctId);
                if (acctOpt.isEmpty()) {
                    rejected++;
                    continue;
                }

                Account account = acctOpt.get();

                // Check active status
                if (!"Y".equals(account.getAcctActiveStatus())) {
                    rejected++;
                    continue;
                }

                // Reject if amount is null
                if (dt.getDalytranAmt() == null) {
                    rejected++;
                    continue;
                }

                // Null-safe balance defaults
                BigDecimal acctBal = account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO;
                BigDecimal creditLimit = account.getAcctCreditLimit() != null ? account.getAcctCreditLimit() : BigDecimal.ZERO;

                // Check credit limit for purchases
                if (dt.getDalytranAmt().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal newBal = acctBal.add(dt.getDalytranAmt());
                    if (newBal.compareTo(creditLimit) > 0) {
                        rejected++;
                        continue;
                    }
                }

                // Valid - update TCATBAL
                TransactionCatBal.TransactionCatBalId catBalId =
                    new TransactionCatBal.TransactionCatBalId(acctId, dt.getDalytranTypeCd(), dt.getDalytranCatCd());

                Optional<TransactionCatBal> catBalOpt = transactionCatBalRepository.findById(catBalId);
                if (catBalOpt.isPresent()) {
                    TransactionCatBal catBal = catBalOpt.get();
                    BigDecimal currentCatBal = catBal.getTranCatBal() != null ? catBal.getTranCatBal() : BigDecimal.ZERO;
                    catBal.setTranCatBal(currentCatBal.add(dt.getDalytranAmt()));
                    transactionCatBalRepository.save(catBal);
                } else {
                    TransactionCatBal newCatBal = new TransactionCatBal();
                    newCatBal.setTrancatAcctId(acctId);
                    newCatBal.setTrancatTypeCd(dt.getDalytranTypeCd());
                    newCatBal.setTrancatCd(dt.getDalytranCatCd());
                    newCatBal.setTranCatBal(dt.getDalytranAmt());
                    transactionCatBalRepository.save(newCatBal);
                }

                // Update account balance
                account.setAcctCurrBal(acctBal.add(dt.getDalytranAmt()));
                accountRepository.save(account);

                // Write to TRANSACT file — generate ID and save atomically
                Transaction tran = new Transaction();
                tran.setTranTypeCd(dt.getDalytranTypeCd());
                tran.setTranCatCd(dt.getDalytranCatCd());
                tran.setTranSource(dt.getDalytranSource());
                tran.setTranDesc(dt.getDalytranDesc());
                tran.setTranAmt(dt.getDalytranAmt());
                tran.setTranCardNum(dt.getDalytranCardNum());
                tran.setTranMerchantId(dt.getDalytranMerchantId());
                tran.setTranMerchantName(dt.getDalytranMerchantName());
                tran.setTranMerchantCity(dt.getDalytranMerchantCity());
                tran.setTranMerchantZip(dt.getDalytranMerchantZip());
                tran.setTranOrigTs(dt.getDalytranOrigTs());
                tran.setTranProcTs(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")));
                transactionIdService.generateIdAndSave(tran);

                processed++;
            }

            return RepeatStatus.FINISHED;
        };
    }

}
