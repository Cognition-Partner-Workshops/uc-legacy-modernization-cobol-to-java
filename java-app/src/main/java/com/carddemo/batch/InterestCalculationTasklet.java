package com.carddemo.batch;

import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.DisclosureGroup;
import com.carddemo.model.Transaction;
import com.carddemo.model.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InterestCalculationTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationTasklet.class);
    private static final String DEFAULT_GROUP = "DEFAULT";

    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationTasklet(TransactionCategoryBalanceRepository tcatBalRepository,
                                      AccountRepository accountRepository,
                                      CardXrefRepository cardXrefRepository,
                                      DisclosureGroupRepository disclosureGroupRepository,
                                      TransactionRepository transactionRepository) {
        this.tcatBalRepository = tcatBalRepository;
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<TransactionCategoryBalance> allBalances =
                tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();
        log.info("Processing {} transaction category balance records", allBalances.size());

        // Group by account
        Map<Long, List<TransactionCategoryBalance>> balancesByAccount = new HashMap<>();
        for (TransactionCategoryBalance bal : allBalances) {
            balancesByAccount.computeIfAbsent(bal.getAcctId(), k -> new java.util.ArrayList<>()).add(bal);
        }

        int accountsProcessed = 0;

        for (Map.Entry<Long, List<TransactionCategoryBalance>> entry : balancesByAccount.entrySet()) {
            Long acctId = entry.getKey();
            List<TransactionCategoryBalance> balances = entry.getValue();

            try {
                // Read account data (CBACT04C line 203)
                Optional<Account> accountOpt = accountRepository.findById(acctId);
                if (accountOpt.isEmpty()) {
                    log.warn("Account not found for interest calculation: {}", acctId);
                    continue;
                }
                Account account = accountOpt.get();

                BigDecimal totalInterest = BigDecimal.ZERO;

                for (TransactionCategoryBalance bal : balances) {
                    // Look up disclosure group interest rate (CBACT04C lines 210-213)
                    BigDecimal interestRate = getInterestRate(
                            account.getGroupId(), bal.getTypeCd(), bal.getCatCd());

                    if (interestRate != null && interestRate.compareTo(BigDecimal.ZERO) > 0) {
                        // Compute monthly interest: (balance * rate) / 1200 (CBACT04C lines 464-465)
                        BigDecimal monthlyInterest = bal.getBalance()
                                .multiply(interestRate)
                                .divide(BigDecimal.valueOf(1200), 2, RoundingMode.HALF_UP);
                        totalInterest = totalInterest.add(monthlyInterest);
                    }
                }

                if (totalInterest.compareTo(BigDecimal.ZERO) != 0) {
                    // Write interest transaction (CBACT04C lines 473-515)
                    Long maxId = transactionRepository.findMaxTranId();
                    String tranId = String.format("%016d", maxId + 1);
                    String timestamp = DateTimeUtil.generateTransactionTimestamp();

                    Transaction interestTran = Transaction.builder()
                            .tranId(tranId)
                            .typeCd("05")
                            .catCd(1)
                            .source("SYSTEM")
                            .description("Monthly interest charge")
                            .amount(totalInterest)
                            .merchantId(0L)
                            .merchantName("CARDDEMO SYSTEM")
                            .merchantCity("")
                            .merchantZip("")
                            .cardNum(getFirstCardForAccount(acctId))
                            .origTimestamp(timestamp)
                            .procTimestamp(timestamp)
                            .build();
                    transactionRepository.save(interestTran);

                    // Update account (CBACT04C lines 350-370)
                    account.setCurrBal(account.getCurrBal().add(totalInterest));
                    account.setCurrCycCredit(BigDecimal.ZERO);
                    account.setCurrCycDebit(BigDecimal.ZERO);
                    accountRepository.save(account);

                    accountsProcessed++;
                }
            } catch (Exception e) {
                log.error("Error calculating interest for account {}: {}", acctId, e.getMessage());
            }
        }

        log.info("Interest calculation complete. Accounts processed: {}", accountsProcessed);
        return RepeatStatus.FINISHED;
    }

    private BigDecimal getInterestRate(String groupId, String typeCd, Integer catCd) {
        // Try account's group first
        Optional<DisclosureGroup> disc = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(groupId, typeCd, catCd);
        if (disc.isPresent()) {
            return disc.get().getInterestRate();
        }
        // Fall back to DEFAULT group (CBACT04C lines 436-439)
        disc = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(DEFAULT_GROUP, typeCd, catCd);
        return disc.map(DisclosureGroup::getInterestRate).orElse(BigDecimal.ZERO);
    }

    private String getFirstCardForAccount(Long acctId) {
        List<CardXref> xrefs = cardXrefRepository.findByAcctId(acctId);
        return xrefs.isEmpty() ? "0000000000000000" : xrefs.get(0).getCardNum();
    }
}
