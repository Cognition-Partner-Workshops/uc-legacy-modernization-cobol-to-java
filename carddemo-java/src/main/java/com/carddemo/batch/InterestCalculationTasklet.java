package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.DiscountGroup;
import com.carddemo.entity.DiscountGroupId;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DiscountGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Interest Calculation - from CBACT04C.cbl + INTCALC.jcl
 * Read tran_cat_balances, look up discount_groups for interest rate,
 * calculate interest, update account balance
 */
@Component
public class InterestCalculationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationTasklet.class);

    private final TransactionCategoryBalanceRepository tranCatBalanceRepository;
    private final DiscountGroupRepository discountGroupRepository;
    private final AccountRepository accountRepository;

    public InterestCalculationTasklet(TransactionCategoryBalanceRepository tranCatBalanceRepository,
                                      DiscountGroupRepository discountGroupRepository,
                                      AccountRepository accountRepository) {
        this.tranCatBalanceRepository = tranCatBalanceRepository;
        this.discountGroupRepository = discountGroupRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<TransactionCategoryBalance> balances = tranCatBalanceRepository.findAll();
        int processedCount = 0;

        for (TransactionCategoryBalance balance : balances) {
            if (balance.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            Optional<Account> accountOpt = accountRepository.findById(balance.getAcctId());
            if (accountOpt.isEmpty()) {
                continue;
            }

            Account account = accountOpt.get();
            String groupId = account.getGroupId() != null ? account.getGroupId() : "A000000000";

            DiscountGroupId dgId = new DiscountGroupId(groupId, balance.getTypeCd(), balance.getCatCd());
            Optional<DiscountGroup> dgOpt = discountGroupRepository.findById(dgId);

            BigDecimal interestRate = dgOpt.map(DiscountGroup::getInterestRate)
                    .orElse(BigDecimal.ZERO);

            if (interestRate.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);
                BigDecimal interest = balance.getBalance().multiply(monthlyRate)
                        .setScale(2, RoundingMode.HALF_UP);

                account.setCurrentBalance(account.getCurrentBalance().add(interest));
                accountRepository.save(account);
                processedCount++;

                log.debug("Interest {} applied to account {} for type/cat {}/{}",
                        interest, account.getAcctId(), balance.getTypeCd(), balance.getCatCd());
            }
        }

        log.info("Interest calculation complete: {} accounts processed", processedCount);
        return RepeatStatus.FINISHED;
    }
}
