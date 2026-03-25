package com.cardemo.batch;

import com.cardemo.model.Account;
import com.cardemo.model.DisclosureGroup;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.DisclosureGroupRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Interest Calculator - converted from COBOL program CBACT04C.cbl
 * 
 * Original: Batch Interest Calculation job
 * Reads through accounts and their transaction category balances,
 * looks up disclosure group interest rates, calculates monthly interest,
 * and updates account balances.
 * 
 * COBOL logic flow:
 * 1. OPEN-ACCT-FILE (1000-ACCTFILE-GET-NEXT)
 * 2. For each account, READ transaction category balances (2000-LOOKUP-TCATBAL)
 * 3. For each balance, LOOKUP disclosure group interest rate (3000-LOOKUP-DISCGRP)
 * 4. Calculate monthly interest = balance * (annual_rate / 1200)
 * 5. Update account balance (4000-UPDATE-ACCOUNT)
 */
@Component
public class InterestCalculator {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculator.class);
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("1200");

    private final AccountRepository accountRepository;
    private final TransactionCategoryBalanceRepository tranCatBalRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;

    public InterestCalculator(AccountRepository accountRepository,
                              TransactionCategoryBalanceRepository tranCatBalRepository,
                              DisclosureGroupRepository disclosureGroupRepository) {
        this.accountRepository = accountRepository;
        this.tranCatBalRepository = tranCatBalRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
    }

    /**
     * Run the interest calculation batch job.
     * Equivalent to CBACT04C main PROCEDURE DIVISION logic.
     */
    @Transactional
    public InterestResult calculateInterest() {
        InterestResult result = new InterestResult();

        List<Account> accounts = accountRepository.findAll();

        for (Account account : accounts) {
            if (!"Y".equals(account.getActiveStatus())) {
                continue;
            }

            BigDecimal totalInterest = BigDecimal.ZERO;
            String groupId = account.getGroupId() != null ? account.getGroupId().trim() : "";

            // 2000-LOOKUP-TCATBAL: Read category balances for this account
            List<TransactionCategoryBalance> balances =
                    tranCatBalRepository.findByAcctIdOrderByAcctIdAscTypeCdAscCatCdAsc(
                            account.getAcctId());

            for (TransactionCategoryBalance catBal : balances) {
                BigDecimal balance = catBal.getBalance() != null
                        ? catBal.getBalance() : BigDecimal.ZERO;

                if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                // 3000-LOOKUP-DISCGRP: Look up interest rate
                BigDecimal interestRate = lookupInterestRate(
                        groupId, catBal.getTypeCd(), catBal.getCatCd());

                if (interestRate.compareTo(BigDecimal.ZERO) > 0) {
                    // Calculate monthly interest = balance * (rate / 1200)
                    BigDecimal monthlyInterest = balance
                            .multiply(interestRate)
                            .divide(MONTHS_IN_YEAR, 2, RoundingMode.HALF_UP);

                    totalInterest = totalInterest.add(monthlyInterest);

                    log.debug("Account {}: Type={}, Cat={}, Balance={}, Rate={}, Interest={}",
                            account.getAcctId(), catBal.getTypeCd(), catBal.getCatCd(),
                            balance, interestRate, monthlyInterest);
                }
            }

            // 4000-UPDATE-ACCOUNT: Update account with interest
            if (totalInterest.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentBal = account.getCurrentBalance() != null
                        ? account.getCurrentBalance() : BigDecimal.ZERO;
                account.setCurrentBalance(currentBal.add(totalInterest));

                // Reset cycle counters
                account.setCurrentCycleCredit(BigDecimal.ZERO);
                account.setCurrentCycleDebit(BigDecimal.ZERO);

                accountRepository.save(account);

                result.accountsProcessed++;
                result.totalInterestApplied = result.totalInterestApplied.add(totalInterest);

                log.info("Account {}: Interest {} applied. New balance: {}",
                        account.getAcctId(), totalInterest, account.getCurrentBalance());
            }
        }

        log.info("Interest calculation complete. Accounts: {}, Total Interest: {}",
                result.accountsProcessed, result.totalInterestApplied);

        return result;
    }

    /**
     * Look up interest rate from disclosure group.
     * Equivalent to 3000-LOOKUP-DISCGRP paragraph.
     */
    private BigDecimal lookupInterestRate(String groupId, String typeCd, Integer catCd) {
        return disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(groupId, typeCd, catCd)
                .map(DisclosureGroup::getInterestRate)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Result of interest calculation batch run.
     */
    public static class InterestResult {
        public int accountsProcessed = 0;
        public BigDecimal totalInterestApplied = BigDecimal.ZERO;
    }
}
