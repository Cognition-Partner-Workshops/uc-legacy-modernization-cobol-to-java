package com.carddemo.batch;

import com.carddemo.model.entity.Account;
import com.carddemo.model.entity.CardXref;
import com.carddemo.model.entity.DisclosureGroup;
import com.carddemo.model.entity.Transaction;
import com.carddemo.model.entity.TransactionCatBal;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Mirrors INTCALC.jcl / CBACT04C.cbl - Interest Calculation.
 * Read TCATBAL sequentially, for each account change: lookup account data,
 * lookup XREF, lookup disclosure group for interest rate, compute monthly
 * interest = (balance * rate) / 1200, accumulate total interest, update
 * account balance, write interest transaction.
 */
@Configuration
public class InterestCalculationJob {

    private final TransactionCatBalRepository transactionCatBalRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdService transactionIdService;

    public InterestCalculationJob(TransactionCatBalRepository transactionCatBalRepository,
                                  AccountRepository accountRepository,
                                  CardXrefRepository cardXrefRepository,
                                  DisclosureGroupRepository disclosureGroupRepository,
                                  TransactionRepository transactionRepository,
                                  TransactionIdService transactionIdService) {
        this.transactionCatBalRepository = transactionCatBalRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
        this.transactionIdService = transactionIdService;
    }

    @Bean
    public Job interestCalculationBatchJob(JobRepository jobRepository, Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(interestCalculationTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet interestCalculationTasklet() {
        return (contribution, chunkContext) -> {
            List<TransactionCatBal> catBals = transactionCatBalRepository
                .findAllByOrderByTrancatAcctIdAscTrancatTypeCdAscTrancatCdAsc();

            long currentAcctId = -1;
            BigDecimal totalInterest = BigDecimal.ZERO;
            Account currentAccount = null;

            for (TransactionCatBal catBal : catBals) {
                long acctId = catBal.getTrancatAcctId();

                // Account change - flush accumulated interest
                if (acctId != currentAcctId) {
                    if (currentAccount != null && totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                        applyInterest(currentAccount, totalInterest);
                    }
                    currentAcctId = acctId;
                    totalInterest = BigDecimal.ZERO;
                    currentAccount = accountRepository.findById(acctId).orElse(null);
                }

                if (currentAccount == null) continue;

                // Lookup disclosure group for interest rate
                String groupId = currentAccount.getAcctGroupId();
                if (groupId == null || groupId.isBlank()) continue;

                Optional<DisclosureGroup> discGroupOpt = disclosureGroupRepository
                    .findByDisAcctGroupIdAndDisTranTypeCdAndDisTranCatCd(
                        groupId.trim(),
                        catBal.getTrancatTypeCd(),
                        catBal.getTrancatCd()
                    );

                if (discGroupOpt.isEmpty()) continue;

                BigDecimal rate = discGroupOpt.get().getDisIntRate();
                if (rate == null || rate.compareTo(BigDecimal.ZERO) == 0) continue;

                // Compute monthly interest = (balance * rate) / 1200
                BigDecimal balance = catBal.getTranCatBal();
                if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal monthlyInterest = balance.multiply(rate)
                    .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

                totalInterest = totalInterest.add(monthlyInterest);
            }

            // Flush last account
            if (currentAccount != null && totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                applyInterest(currentAccount, totalInterest);
            }

            return RepeatStatus.FINISHED;
        };
    }

    private void applyInterest(Account account, BigDecimal totalInterest) {
        // Update account balance
        BigDecimal currentBal = account.getAcctCurrBal() != null ? account.getAcctCurrBal() : BigDecimal.ZERO;
        account.setAcctCurrBal(currentBal.add(totalInterest));
        accountRepository.save(account);

        // Write interest transaction
        String nextTranId = transactionIdService.generateNextTranId();
        Transaction tran = new Transaction();
        tran.setTranId(nextTranId);
        tran.setTranTypeCd("01");
        tran.setTranCatCd(5);
        tran.setTranSource("INTEREST");
        tran.setTranDesc("Monthly Interest Charge");
        tran.setTranAmt(totalInterest);
        tran.setTranOrigTs(LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")));
        tran.setTranProcTs(tran.getTranOrigTs());

        // Get card number from XREF
        Optional<CardXref> xref = cardXrefRepository.findByXrefAcctId(account.getAcctId());
        xref.ifPresent(x -> tran.setTranCardNum(x.getXrefCardNum()));

        transactionRepository.save(tran);
    }

}
