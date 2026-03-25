package com.cardemo.service;

import com.cardemo.model.Account;
import com.cardemo.model.CardXref;
import com.cardemo.model.DisclosureGroup;
import com.cardemo.model.Transaction;
import com.cardemo.model.TransactionCategoryBalance;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.DisclosureGroupRepository;
import com.cardemo.repository.TransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Java equivalent of COBOL program CBACT04C.
 * Interest calculator program that processes transaction category balances
 * and computes monthly interest charges.
 */
public class InterestCalculationService {

    private static final BigDecimal MONTHS_PER_YEAR = new BigDecimal("1200");
    private static final String DEFAULT_GROUP_ID = "DEFAULT";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SS0000");

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final DisclosureGroupRepository disclosureGroupRepository;
    private final TransactionRepository transactionRepository;

    private int recordCount;
    private int transactionIdSuffix;

    public InterestCalculationService(AccountRepository accountRepository,
                                       CardXrefRepository cardXrefRepository,
                                       DisclosureGroupRepository disclosureGroupRepository,
                                       TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.disclosureGroupRepository = disclosureGroupRepository;
        this.transactionRepository = transactionRepository;
        this.recordCount = 0;
        this.transactionIdSuffix = 0;
    }

    /**
     * Computes monthly interest for a given balance and interest rate.
     * Equivalent of COBOL 1300-COMPUTE-INTEREST:
     * COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
     *
     * @param categoryBalance the transaction category balance
     * @param annualInterestRate the annual interest rate (e.g., 18.00 for 18%)
     * @return the monthly interest amount
     */
    public BigDecimal computeMonthlyInterest(BigDecimal categoryBalance,
                                              BigDecimal annualInterestRate) {
        if (categoryBalance == null || annualInterestRate == null) {
            return BigDecimal.ZERO;
        }
        return categoryBalance.multiply(annualInterestRate)
                .divide(MONTHS_PER_YEAR, 2, RoundingMode.HALF_UP);
    }

    /**
     * Gets the interest rate for an account group and transaction category.
     * Falls back to DEFAULT group if account-specific rate not found.
     * Equivalent of COBOL 1200-GET-INTEREST-RATE.
     */
    public BigDecimal getInterestRate(String accountGroupId, String transactionTypeCode,
                                       int transactionCategoryCode) {
        // Try account-specific rate first
        Optional<DisclosureGroup> discGroupOpt =
                disclosureGroupRepository.findByKey(
                        accountGroupId, transactionTypeCode, transactionCategoryCode);

        if (discGroupOpt.isPresent()) {
            return discGroupOpt.get().getInterestRate();
        }

        // Fallback to default group
        Optional<DisclosureGroup> defaultGroupOpt =
                disclosureGroupRepository.findByKey(
                        DEFAULT_GROUP_ID, transactionTypeCode, transactionCategoryCode);

        return defaultGroupOpt.map(DisclosureGroup::getInterestRate)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Processes interest for a list of category balances belonging to an account.
     * Equivalent of the main processing loop in CBACT04C.
     *
     * @param accountId the account ID
     * @param categoryBalances the list of category balances
     * @param processingDate the date to use for transaction IDs (PARM-DATE)
     * @return the total interest computed for this account
     */
    public BigDecimal processAccountInterest(long accountId,
                                              List<TransactionCategoryBalance> categoryBalances,
                                              String processingDate) {
        Optional<Account> accountOpt = accountRepository.findByAccountId(accountId);
        if (accountOpt.isEmpty()) {
            throw new IllegalStateException("Account not found: " + accountId);
        }

        Account account = accountOpt.get();

        Optional<CardXref> xrefOpt = cardXrefRepository.findByAccountId(accountId);
        if (xrefOpt.isEmpty()) {
            throw new IllegalStateException("Card xref not found for account: " + accountId);
        }

        CardXref xref = xrefOpt.get();
        BigDecimal totalInterest = BigDecimal.ZERO;

        for (TransactionCategoryBalance catBal : categoryBalances) {
            recordCount++;

            BigDecimal interestRate = getInterestRate(
                    account.getGroupId(),
                    catBal.getTypeCode(),
                    catBal.getCategoryCode());

            if (interestRate.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal monthlyInterest =
                        computeMonthlyInterest(catBal.getBalance(), interestRate);
                totalInterest = totalInterest.add(monthlyInterest);

                // Write interest transaction
                writeInterestTransaction(account, xref, monthlyInterest, processingDate);
            }
        }

        // Update account: add total interest and reset cycle balances
        updateAccountWithInterest(account, totalInterest);

        return totalInterest;
    }

    /**
     * Updates the account with computed interest.
     * Equivalent of COBOL 1050-UPDATE-ACCOUNT.
     */
    void updateAccountWithInterest(Account account, BigDecimal totalInterest) {
        account.setCurrentBalance(
                account.getCurrentBalance().add(totalInterest));
        account.setCurrentCycleCredit(BigDecimal.ZERO);
        account.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.update(account);
    }

    /**
     * Writes an interest transaction record.
     * Equivalent of COBOL 1300-B-WRITE-TX.
     */
    void writeInterestTransaction(Account account, CardXref xref,
                                   BigDecimal monthlyInterest, String processingDate) {
        transactionIdSuffix++;

        Transaction interestTx = new Transaction();
        interestTx.setTransactionId(
                processingDate + String.format("%06d", transactionIdSuffix));
        interestTx.setTypeCode("01");
        interestTx.setCategoryCode(5);
        interestTx.setSource("System");
        interestTx.setDescription("Int. for a/c " + account.getAccountId());
        interestTx.setAmount(monthlyInterest);
        interestTx.setMerchantId(0);
        interestTx.setMerchantName("");
        interestTx.setMerchantCity("");
        interestTx.setMerchantZip("");
        interestTx.setCardNumber(xref.getCardNumber());

        String timestamp = generateTimestamp();
        interestTx.setOriginTimestamp(timestamp);
        interestTx.setProcessedTimestamp(timestamp);

        transactionRepository.save(interestTx);
    }

    String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMATTER);
    }

    public int getRecordCount() {
        return recordCount;
    }
}
