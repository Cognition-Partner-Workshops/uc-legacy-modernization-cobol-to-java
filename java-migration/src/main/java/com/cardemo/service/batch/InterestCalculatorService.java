/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.batch;

import com.cardemo.common.DateTimeUtil;
import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.DisclosureGroupRecord;
import com.cardemo.model.TransactionCategoryBalanceRecord;
import com.cardemo.model.TransactionRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DisclosureGroupRepository;
import com.cardemo.repository.TransactionCategoryBalanceRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Interest Calculator - migrated from COBOL batch program CBACT04C.cbl.
 * Calculates interest charges based on transaction category balances,
 * disclosure group interest rates, and updates account balances.
 * Original: Reads TCATBALF, XREFFILE, DISCGRP, ACCTFILE; writes TRANSACT.
 */
@Service
public class InterestCalculatorService {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculatorService.class);

    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculatorService(TransactionCategoryBalanceRepository tcatBalRepository,
                                     CardXrefRepository cardXrefRepository,
                                     DisclosureGroupRepository disclosureGroupRepository,
                                     AccountRepository accountRepository,
                                     TransactionRepository transactionRepository) {
        this.tcatBalRepository = tcatBalRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Calculate interest for all accounts - migrated from main PROCEDURE DIVISION.
     * Iterates through transaction category balances, looks up interest rates,
     * computes interest, and updates account records.
     *
     * @return number of records processed
     */
    @Transactional
    public int calculateInterest() {
        log.info("START OF EXECUTION OF PROGRAM CBACT04C (InterestCalculator)");

        List<TransactionCategoryBalanceRecord> catBalRecords = tcatBalRepository.findAll();
        int recordCount = 0;
        long lastAcctNum = -1;
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TransactionCategoryBalanceRecord catBal : catBalRecords) {
            recordCount++;
            log.info("Processing: {}", catBal);

            // When account changes, update the previous account
            if (catBal.getTrancatAcctId() != lastAcctNum) {
                if (lastAcctNum > 0) {
                    updateAccountBalance(lastAcctNum, totalInterest);
                }
                totalInterest = BigDecimal.ZERO;
                lastAcctNum = catBal.getTrancatAcctId();
            }

            // Look up interest rate from disclosure group
            Optional<AccountRecord> acctOpt = accountRepository.findById(catBal.getTrancatAcctId());
            if (acctOpt.isEmpty()) {
                log.warn("Account {} not found, skipping", catBal.getTrancatAcctId());
                continue;
            }

            AccountRecord account = acctOpt.get();
            String groupId = account.getAcctGroupId();

            List<DisclosureGroupRecord> discGroups = disclosureGroupRepository
                    .findByDisAcctGroupIdAndDisTranTypeCd(groupId, catBal.getTrancatTypeCd());

            if (discGroups.isEmpty()) {
                log.info("No disclosure group found for group {} type {}", groupId, catBal.getTrancatTypeCd());
                continue;
            }

            BigDecimal interestRate = discGroups.get(0).getDisIntRate();
            if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // Compute monthly interest: balance * (rate / 1200)
            BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
            BigDecimal monthlyInterest = catBal.getTranCatBal().multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            totalInterest = totalInterest.add(monthlyInterest);

            // Create interest transaction
            if (monthlyInterest.compareTo(BigDecimal.ZERO) != 0) {
                createInterestTransaction(catBal, monthlyInterest, recordCount);
            }
        }

        // Update last account
        if (lastAcctNum > 0) {
            updateAccountBalance(lastAcctNum, totalInterest);
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT04C. Records processed: {}", recordCount);
        return recordCount;
    }

    /**
     * Update account balance with calculated interest.
     * Migrated from 1050-UPDATE-ACCOUNT paragraph.
     */
    private void updateAccountBalance(long acctId, BigDecimal totalInterest) {
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isPresent()) {
            AccountRecord account = acctOpt.get();
            BigDecimal newBalance = account.getAcctCurrBal().add(totalInterest);
            account.setAcctCurrBal(newBalance);
            account.setAcctCurrCycCredit(BigDecimal.ZERO);
            account.setAcctCurrCycDebit(BigDecimal.ZERO);
            accountRepository.save(account);
            log.info("Account {} updated. Interest: {}, New Balance: {}",
                    acctId, totalInterest, newBalance);
        }
    }

    /**
     * Create an interest charge transaction.
     * Migrated from 1300-COMPUTE-INTEREST and 1500-WRITE-TRANSACTION paragraphs.
     */
    private void createInterestTransaction(TransactionCategoryBalanceRecord catBal,
                                           BigDecimal interest, int suffix) {
        TransactionRecord tran = new TransactionRecord();
        tran.setTranId(String.format("%010d%06d", catBal.getTrancatAcctId(), suffix));
        tran.setTranTypeCd(catBal.getTrancatTypeCd());
        tran.setTranCatCd(catBal.getTrancatCd());
        tran.setTranSource("INTEREST");
        tran.setTranDesc("Monthly Interest Charge");
        tran.setTranAmt(interest);
        tran.setTranMerchantId(0);
        tran.setTranMerchantName("INTEREST CHARGE");
        tran.setTranMerchantCity("");
        tran.setTranMerchantZip("");
        tran.setTranCardNum("");

        String timestamp = DateTimeUtil.getCurrentTimestamp();
        tran.setTranOrigTs(timestamp);
        tran.setTranProcTs(timestamp);

        transactionRepository.save(tran);
    }
}
