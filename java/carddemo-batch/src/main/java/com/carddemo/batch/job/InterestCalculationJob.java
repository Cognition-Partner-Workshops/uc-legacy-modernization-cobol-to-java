package com.carddemo.batch.job;

import com.carddemo.batch.repository.AccountRepository;
import com.carddemo.batch.repository.CardCrossReferenceRepository;
import com.carddemo.batch.repository.DisclosureGroupRepository;
import com.carddemo.batch.repository.TransactionCategoryBalanceRepository;
import com.carddemo.batch.repository.TransactionRepository;
import com.carddemo.common.model.Account;
import com.carddemo.common.model.CardCrossReference;
import com.carddemo.common.model.DisclosureGroup;
import com.carddemo.common.model.Transaction;
import com.carddemo.common.model.TransactionCategoryBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Batch job replacing COBOL program CBACT04C / JCL job INTCALC.
 * Calculates interest on transaction category balances.
 *
 * Original COBOL: app/cbl/CBACT04C.cbl
 * JCL Job: INTCALC
 *
 * Processing logic (from COBOL):
 * 1. Read each transaction category balance record (TCATBAL / CVTRA01Y)
 * 2. For each record, look up the account to get the group ID
 * 3. Look up the disclosure group to get the interest rate
 * 4. If no specific rate found, use DEFAULT group rate
 * 5. Compute monthly interest: (balance * rate) / 1200
 * 6. Write an interest transaction record
 * 7. Accumulate total interest per account
 */
@Configuration
public class InterestCalculationJob {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationJob.class);
    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("1200");
    private static final String DEFAULT_GROUP = "DEFAULT";
    private static final String INTEREST_TYPE_CODE = "01";
    private static final String INTEREST_CAT_CODE = "0005";
    private static final DateTimeFormatter DATE_PARAM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final AccountRepository accountRepository;
    private final CardCrossReferenceRepository cardCrossReferenceRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationJob(
            TransactionCategoryBalanceRepository tcatBalRepository,
            AccountRepository accountRepository,
            CardCrossReferenceRepository cardCrossReferenceRepository,
            DisclosureGroupRepository disclosureGroupRepository,
            TransactionRepository transactionRepository) {
        this.tcatBalRepository = tcatBalRepository;
        this.accountRepository = accountRepository;
        this.cardCrossReferenceRepository = cardCrossReferenceRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
    }

    @Bean
    public Job interestCalculationBatchJob(JobRepository jobRepository,
                                           Step interestCalculationStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(interestCalculationStep)
                .build();
    }

    @Bean
    public Step interestCalculationStep(JobRepository jobRepository,
                                        PlatformTransactionManager transactionManager) {
        return new StepBuilder("interestCalculationStep", jobRepository)
                .tasklet(new InterestCalculationTasklet(), transactionManager)
                .build();
    }

    private class InterestCalculationTasklet implements Tasklet {

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            log.info("START OF EXECUTION OF PROGRAM CBACT04C (InterestCalculationJob)");

            String parmDate = (String) chunkContext.getStepContext()
                    .getJobParameters().get("parmDate");
            if (parmDate == null) {
                parmDate = LocalDate.now().format(DATE_PARAM_FORMAT);
            }
            log.info("PARM-DATE: {}", parmDate);

            List<TransactionCategoryBalance> balances =
                    tcatBalRepository.findAllByOrderByAccountIdAscTypeCodeAscCategoryCodeAsc();

            BigDecimal totalInterest = BigDecimal.ZERO;
            AtomicLong tranIdSuffix = new AtomicLong(0);
            int recordCount = 0;

            for (TransactionCategoryBalance tcatBal : balances) {
                recordCount++;
                Long accountId = tcatBal.getAccountId();

                // 1100-GET-ACCT-DATA
                Optional<Account> accountOpt = accountRepository.findById(accountId);
                if (accountOpt.isEmpty()) {
                    log.warn("Account not found: {}", accountId);
                    continue;
                }
                Account account = accountOpt.get();

                // 1110-GET-XREF-DATA
                List<CardCrossReference> xrefList =
                        cardCrossReferenceRepository.findByAccountId(accountId);
                if (xrefList.isEmpty()) {
                    log.warn("Cross-reference not found for account: {}", accountId);
                    continue;
                }
                CardCrossReference xref = xrefList.get(0);

                // 1200-GET-INTEREST-RATE
                String groupId = account.getGroupId() != null
                        ? account.getGroupId().trim() : DEFAULT_GROUP;
                BigDecimal interestRate = getInterestRate(groupId,
                        tcatBal.getTypeCode(), tcatBal.getCategoryCode());

                if (interestRate == null) {
                    log.warn("No interest rate found for group={}, type={}, cat={}",
                            groupId, tcatBal.getTypeCode(), tcatBal.getCategoryCode());
                    continue;
                }

                // 1300-COMPUTE-INTEREST: (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
                BigDecimal balance = tcatBal.getBalance() != null
                        ? tcatBal.getBalance() : BigDecimal.ZERO;
                BigDecimal monthlyInterest = balance.multiply(interestRate)
                        .divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);

                totalInterest = totalInterest.add(monthlyInterest);

                // 1300-B-WRITE-TX
                long suffix = tranIdSuffix.incrementAndGet();
                String tranId = parmDate.replace("-", "") + String.format("%08d", suffix);

                Transaction interestTran = new Transaction();
                interestTran.setTransactionId(tranId.length() > 16 ? tranId.substring(0, 16) : tranId);
                interestTran.setTypeCode(INTEREST_TYPE_CODE);
                interestTran.setCategoryCode(INTEREST_CAT_CODE);
                interestTran.setSource("System");
                interestTran.setDescription("Int. for a/c " + accountId);
                interestTran.setAmount(monthlyInterest);
                interestTran.setMerchantId("0");
                interestTran.setMerchantName("");
                interestTran.setMerchantCity("");
                interestTran.setMerchantZip("");
                interestTran.setCardNumber(xref.getCardNumber());
                interestTran.setOriginTimestamp(LocalDateTime.now());
                interestTran.setProcessedTimestamp(LocalDateTime.now());

                transactionRepository.save(interestTran);
            }

            log.info("TCATBAL RECORDS PROCESSED: {}", recordCount);
            log.info("TOTAL INTEREST CALCULATED: {}", totalInterest);
            log.info("END OF EXECUTION OF PROGRAM CBACT04C (InterestCalculationJob)");
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Gets the interest rate from the disclosure group, falling back to DEFAULT group.
     * Mirrors COBOL paragraphs 1200-GET-INTEREST-RATE and 1200-A-GET-DEFAULT-INT-RATE.
     */
    private BigDecimal getInterestRate(String groupId, String typeCode, String categoryCode) {
        Optional<DisclosureGroup> dgOpt =
                disclosureGroupRepository
                        .findByAccountGroupIdAndTransactionTypeCodeAndTransactionCategoryCode(
                                groupId, typeCode, categoryCode);

        if (dgOpt.isPresent()) {
            return dgOpt.get().getInterestRate();
        }

        // Fall back to DEFAULT group
        if (!DEFAULT_GROUP.equals(groupId)) {
            Optional<DisclosureGroup> defaultOpt =
                    disclosureGroupRepository
                            .findByAccountGroupIdAndTransactionTypeCodeAndTransactionCategoryCode(
                                    DEFAULT_GROUP, typeCode, categoryCode);
            if (defaultOpt.isPresent()) {
                return defaultOpt.get().getInterestRate();
            }
        }

        return null;
    }
}
