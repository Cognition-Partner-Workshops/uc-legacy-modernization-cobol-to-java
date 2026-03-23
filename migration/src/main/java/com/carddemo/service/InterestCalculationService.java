package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Replaces CBACT04C.cbl - Interest calculator program.
 * Replicates the exact logic from the COBOL batch program.
 */
@Service
public class InterestCalculationService {

    private static final Logger log = LoggerFactory.getLogger(InterestCalculationService.class);
    private static final BigDecimal TWELVE_HUNDRED = new BigDecimal("1200");

    private final TransactionCategoryBalanceRepository tcatBalRepository;
    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    public InterestCalculationService(TransactionCategoryBalanceRepository tcatBalRepository,
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

    /**
     * Calculate interest for all accounts.
     * Matches CBACT04C.cbl main loop logic:
     * 1. Read TransactionCategoryBalance records sequentially ordered by acctId
     * 2. For each new account, update previous account's balance, read new account data
     * 3. Look up interest rate from DisclosureGroup
     * 4. Compute monthly interest: (categoryBalance * interestRate) / 1200
     * 5. Write interest transaction record
     * 6. After all records: update last account
     */
    @Transactional
    public InterestResult calculateInterest(LocalDate processingDate) {
        List<TransactionCategoryBalance> allBalances =
                tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc();

        String lastAcctNum = "";
        BigDecimal totalInterest = BigDecimal.ZERO;
        boolean firstTime = true;
        Account currentAccount = null;
        String currentCardNum = null;
        AtomicInteger tranIdSuffix = new AtomicInteger(0);
        int recordCount = 0;

        for (TransactionCategoryBalance tcatBal : allBalances) {
            recordCount++;

            // When we encounter a new account
            if (!tcatBal.getAcctId().equals(lastAcctNum)) {
                if (!firstTime && currentAccount != null) {
                    // 1050-UPDATE-ACCOUNT: Update previous account's balance
                    updateAccount(currentAccount, totalInterest);
                } else {
                    firstTime = false;
                }

                totalInterest = BigDecimal.ZERO;
                lastAcctNum = tcatBal.getAcctId();

                // 1100-GET-ACCT-DATA: Read account record
                currentAccount = accountRepository.findById(tcatBal.getAcctId()).orElse(null);
                if (currentAccount == null) {
                    log.warn("Account not found: {}", tcatBal.getAcctId());
                    continue;
                }

                // 1110-GET-XREF-DATA: Read CardXref by account ID to get card number
                Optional<CardXref> xrefOpt = cardXrefRepository.findFirstByAcctId(tcatBal.getAcctId());
                currentCardNum = xrefOpt.map(CardXref::getCardNum).orElse("");
            }

            if (currentAccount == null) {
                continue;
            }

            // 1200-GET-INTEREST-RATE: Look up interest rate from DisclosureGroup
            BigDecimal interestRate = getInterestRate(
                    currentAccount.getGroupId(), tcatBal.getTypeCd(), tcatBal.getCatCd());

            if (interestRate.compareTo(BigDecimal.ZERO) != 0) {
                // 1300-COMPUTE-INTEREST: monthlyInterest = (categoryBalance * interestRate) / 1200
                BigDecimal monthlyInterest = tcatBal.getBalance()
                        .multiply(interestRate)
                        .divide(TWELVE_HUNDRED, 2, RoundingMode.HALF_UP);

                totalInterest = totalInterest.add(monthlyInterest);

                // 1300-B-WRITE-TX: Write interest transaction record
                writeInterestTransaction(processingDate, tranIdSuffix,
                        currentAccount.getAcctId(), currentCardNum, monthlyInterest);
            }
        }

        // Update the last account after processing all records
        if (currentAccount != null) {
            updateAccount(currentAccount, totalInterest);
        }

        log.info("Interest calculation complete. Records processed: {}", recordCount);
        return new InterestResult(recordCount, tranIdSuffix.get());
    }

    /**
     * Look up interest rate from DisclosureGroup.
     * Matches CBACT04C.cbl 1200-GET-INTEREST-RATE:
     * - Try with account's group ID first
     * - If not found, try with 'DEFAULT' group ID
     */
    private BigDecimal getInterestRate(String groupId, String typeCd, Integer catCd) {
        Optional<DisclosureGroup> discGroup = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd(groupId, typeCd, catCd);

        if (discGroup.isPresent()) {
            return discGroup.get().getInterestRate();
        }

        // 1200-A-GET-DEFAULT-INT-RATE: Try with DEFAULT group
        Optional<DisclosureGroup> defaultGroup = disclosureGroupRepository
                .findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", typeCd, catCd);

        return defaultGroup.map(DisclosureGroup::getInterestRate).orElse(BigDecimal.ZERO);
    }

    /**
     * Update account balance with accumulated interest and reset cycle counters.
     * Matches CBACT04C.cbl 1050-UPDATE-ACCOUNT.
     */
    private void updateAccount(Account account, BigDecimal totalInterest) {
        account.setCurrentBalance(account.getCurrentBalance().add(totalInterest));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(account);
    }

    /**
     * Write an interest transaction record.
     * Matches CBACT04C.cbl 1300-B-WRITE-TX:
     * - Type '01', Category '05', Source 'System'
     * - Description: 'Int. for a/c {acctId}'
     */
    private void writeInterestTransaction(LocalDate processingDate, AtomicInteger tranIdSuffix,
                                           String acctId, String cardNum, BigDecimal monthlyInterest) {
        int suffix = tranIdSuffix.incrementAndGet();
        String tranId = processingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + String.format("%06d", suffix);

        Transaction interestTran = new Transaction();
        interestTran.setTranId(tranId);
        interestTran.setTypeCd("01");
        interestTran.setCatCd(5);
        interestTran.setSource("System");
        interestTran.setDescription("Int. for a/c " + acctId);
        interestTran.setAmount(monthlyInterest);
        interestTran.setMerchantId(0L);
        interestTran.setMerchantName("");
        interestTran.setMerchantCity("");
        interestTran.setMerchantZip("");
        interestTran.setCardNum(cardNum);
        interestTran.setOrigTimestamp(LocalDateTime.now());
        interestTran.setProcTimestamp(LocalDateTime.now());

        transactionRepository.save(interestTran);
    }

    public record InterestResult(int recordsProcessed, int transactionsCreated) {}
}
