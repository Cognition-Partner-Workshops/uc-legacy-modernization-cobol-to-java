package com.carddemo.batch;

import com.carddemo.model.Account;
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
 * Spring Batch job replacing JCL INTCALC.jcl and COBOL program CBACT04C (653 lines).
 *
 * <p>Original JCL flow (INTCALC.jcl):
 * <pre>
 * //INTCALC  JOB ...
 * //STEP01   EXEC PGM=CBACT04C
 * //  DD TCATBALF -> tran_cat_balances (input)
 * //  DD DISCGRP  -> disclosure_groups (lookup - interest rates)
 * //  DD ACCTDAT  -> accounts (update with interest)
 * //  DD TRANSACT -> transactions (write interest transactions)
 * </pre>
 *
 * <p>CBACT04C logic (653 lines):
 * 1. For each account, read category balances from TCATBALF
 * 2. For each category, lookup interest rate from DISCGRP by (group_id, type_cd, cat_cd)
 * 3. Compute interest: balance * rate / 12 (monthly)
 * 4. Add interest amount to account balance in ACCTDAT
 * 5. Write interest transaction to TRANSACT
 *
 * <p>Phase 2 will implement the full calculation logic.
 */
@Configuration
public class InterestCalculationJobConfig {

    @Bean
    public Job interestCalculationJob(JobRepository jobRepository,
                                       Step calculateInterestStep) {
        return new JobBuilder("interestCalculationJob", jobRepository)
                .start(calculateInterestStep)
                .build();
    }

    @Bean
    public Step calculateInterestStep(JobRepository jobRepository,
                                       PlatformTransactionManager transactionManager) {
        return new StepBuilder("calculateInterestStep", jobRepository)
                .tasklet(interestCalculationTasklet(), transactionManager)
                .build();
    }

    /**
     * Interest calculation tasklet - stub for Phase 2 implementation.
     *
     * <p>Phase 2 algorithm (from CBACT04C):
     * <pre>
     * FOR each account:
     *   total_interest = 0
     *   FOR each tran_cat_balance WHERE acct_id = account.acct_id:
     *     disclosure = LOOKUP disclosure_groups(account.group_id, balance.type_cd, balance.cat_cd)
     *     interest = balance.bal_amt * disclosure.interest_rate / 1200
     *     total_interest += interest
     *   END FOR
     *   account.curr_bal += total_interest
     *   account.curr_cyc_credit += total_interest (if positive)
     *   WRITE interest transaction (type='IN', amount=total_interest)
     * END FOR
     * </pre>
     */
    @Bean
    public Tasklet interestCalculationTasklet() {
        return (contribution, chunkContext) -> {
            // Phase 2: implement full interest calculation from CBACT04C
            return RepeatStatus.FINISHED;
        };
    }
}
