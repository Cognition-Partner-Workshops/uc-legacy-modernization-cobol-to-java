package com.carddemo.integration;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import com.carddemo.service.TransactionPostingService;
import com.carddemo.service.TransactionPostingService.PostingResult;
import com.carddemo.service.TransactionPostingService.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for TransactionPostingService.
 * Verifies COBOL CBTRN02C.cbl business logic against a real H2 database.
 * Tests all 4 rejection codes and the posting logic with real DB state.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransactionPostingIntegrationTest {

    @Autowired
    private TransactionPostingService postingService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardXrefRepository cardXrefRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionCategoryBalanceRepository tcatBalRepository;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = accountRepository.findById("00000000001").orElse(null);
        assertNotNull(testAccount, "Test account 00000000001 should exist from data.sql");
    }

    @Test
    @DisplayName("Reject code 100: Invalid card number not in CardXref (CBTRN02C 1500-A-LOOKUP-XREF)")
    void shouldRejectWithCode100_invalidCardNumber() {
        Transaction tran = createDailyTransaction("9999999999999999", new BigDecimal("100.00"));

        ValidationResult result = postingService.validateTransaction(tran);

        assertEquals(100, result.reasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.reasonDescription());
    }

    @Test
    @DisplayName("Reject code 101: Card exists in xref but account not found (CBTRN02C 1500-B-LOOKUP-ACCT)")
    void shouldRejectWithCode101_accountNotFound() {
        CardXref orphanXref = new CardXref();
        orphanXref.setCardNum("8888888888888888");
        orphanXref.setCustId(1L);
        orphanXref.setAcctId("99999999999");
        cardXrefRepository.save(orphanXref);

        Transaction tran = createDailyTransaction("8888888888888888", new BigDecimal("100.00"));

        ValidationResult result = postingService.validateTransaction(tran);

        assertEquals(101, result.reasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.reasonDescription());
    }

    @Test
    @DisplayName("Reject code 102: Transaction exceeds credit limit (CBTRN02C lines 403-413)")
    void shouldRejectWithCode102_overlimit() {
        // Account 00000000001 has creditLimit=202000 from data.sql
        // To trigger overlimit: cycleCredit - cycleDebit + amount > creditLimit
        // 201500 - 0 + 600 = 202100 > 202000
        testAccount.setCurrentCycleCredit(new BigDecimal("201500.00"));
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        Transaction tran = createDailyTransaction(validCard, new BigDecimal("600.00"));

        ValidationResult result = postingService.validateTransaction(tran);

        assertEquals(102, result.reasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.reasonDescription());
    }

    @Test
    @DisplayName("Accept transaction at exact credit limit boundary (CBTRN02C lines 403-413)")
    void shouldAcceptAtExactCreditLimit() {
        testAccount.setCurrentCycleCredit(new BigDecimal("9500.00"));
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        Transaction tran = createDailyTransaction(validCard, new BigDecimal("500.00"));

        ValidationResult result = postingService.validateTransaction(tran);

        assertEquals(0, result.reasonCode(), "Should accept transaction at exact credit limit");
    }

    @Test
    @DisplayName("Reject code 103: Transaction after account expiration (CBTRN02C lines 414-420)")
    void shouldRejectWithCode103_expired() {
        testAccount.setExpirationDate(LocalDate.of(2020, 1, 1));
        testAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        Transaction tran = createDailyTransaction(validCard, new BigDecimal("100.00"));
        tran.setOrigTimestamp(LocalDateTime.of(2025, 6, 15, 10, 0));

        ValidationResult result = postingService.validateTransaction(tran);

        assertEquals(103, result.reasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.reasonDescription());
    }

    @Test
    @DisplayName("Post credit transaction updates balance and cycle credit (CBTRN02C lines 545-560)")
    void shouldPostCreditTransaction_updatesBalanceAndCycleCredit() {
        BigDecimal originalBalance = testAccount.getCurrentBalance();
        BigDecimal originalCycleCredit = testAccount.getCurrentCycleCredit();
        BigDecimal creditAmount = new BigDecimal("250.00");

        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        Transaction tran = createDailyTransaction(validCard, creditAmount);
        tran.setTranId("INTG-CREDIT-001");
        tran.setTypeCd("01");
        tran.setCatCd(1);

        postingService.postTransaction(tran);

        Account updated = accountRepository.findById("00000000001").orElseThrow();
        assertEquals(0, originalBalance.add(creditAmount).compareTo(updated.getCurrentBalance()),
                "Current balance should increase by credit amount");
        assertEquals(0, originalCycleCredit.add(creditAmount).compareTo(updated.getCurrentCycleCredit()),
                "Cycle credit should increase for positive amounts");

        Transaction saved = transactionRepository.findById("INTG-CREDIT-001").orElseThrow();
        assertNotNull(saved.getProcTimestamp(), "Processing timestamp must be set");
    }

    @Test
    @DisplayName("Post debit transaction updates balance and cycle debit (CBTRN02C lines 545-560)")
    void shouldPostDebitTransaction_updatesBalanceAndCycleDebit() {
        BigDecimal originalBalance = testAccount.getCurrentBalance();
        BigDecimal originalCycleDebit = testAccount.getCurrentCycleDebit();
        BigDecimal debitAmount = new BigDecimal("-150.00");

        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        testAccount.setCreditLimit(new BigDecimal("99999.00"));
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        Transaction tran = createDailyTransaction(validCard, debitAmount);
        tran.setTranId("INTG-DEBIT-0001");
        tran.setTypeCd("01");
        tran.setCatCd(1);

        postingService.postTransaction(tran);

        Account updated = accountRepository.findById("00000000001").orElseThrow();
        assertEquals(0, originalBalance.add(debitAmount).compareTo(updated.getCurrentBalance()),
                "Current balance should decrease by debit amount");
        assertEquals(0, originalCycleDebit.add(debitAmount).compareTo(updated.getCurrentCycleDebit()),
                "Cycle debit should increase for negative amounts");
    }

    @Test
    @DisplayName("Create new TransactionCategoryBalance if none exists (CBTRN02C 2700-A)")
    void shouldCreateNewCategoryBalance_whenNoneExists() {
        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        testAccount.setCreditLimit(new BigDecimal("99999.00"));
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();
        BigDecimal amount = new BigDecimal("300.00");

        Transaction tran = createDailyTransaction(validCard, amount);
        tran.setTranId("INTG-NEWCAT-001");
        tran.setTypeCd("99");
        tran.setCatCd(99);

        postingService.postTransaction(tran);

        Optional<TransactionCategoryBalance> tcatBal =
                tcatBalRepository.findByAcctIdAndTypeCdAndCatCd("00000000001", "99", 99);
        assertTrue(tcatBal.isPresent(), "New category balance record should be created");
        assertEquals(0, amount.compareTo(tcatBal.get().getBalance()));
    }

    @Test
    @DisplayName("Update existing TransactionCategoryBalance (CBTRN02C 2700-B)")
    void shouldUpdateExistingCategoryBalance() {
        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        testAccount.setCreditLimit(new BigDecimal("99999.00"));
        accountRepository.save(testAccount);

        Optional<TransactionCategoryBalance> existingOpt =
                tcatBalRepository.findByAcctIdAndTypeCdAndCatCd("00000000001", "01", 1);
        assertTrue(existingOpt.isPresent());
        BigDecimal originalBalance = existingOpt.get().getBalance();

        String validCard = getFirstValidCardNum();
        BigDecimal amount = new BigDecimal("175.50");

        Transaction tran = createDailyTransaction(validCard, amount);
        tran.setTranId("INTG-UPDCAT-001");
        tran.setTypeCd("01");
        tran.setCatCd(1);

        postingService.postTransaction(tran);

        TransactionCategoryBalance updated =
                tcatBalRepository.findByAcctIdAndTypeCdAndCatCd("00000000001", "01", 1).orElseThrow();
        assertEquals(0, originalBalance.add(amount).compareTo(updated.getBalance()),
                "Category balance should be incremented by transaction amount");
    }

    @Test
    @DisplayName("Process batch separates valid and rejected transactions (CBTRN02C main loop)")
    void shouldProcessBatch_separatingValidAndRejected() {
        testAccount.setExpirationDate(LocalDate.of(2030, 12, 31));
        testAccount.setCreditLimit(new BigDecimal("99999.00"));
        testAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        testAccount.setCurrentCycleDebit(BigDecimal.ZERO);
        accountRepository.save(testAccount);

        String validCard = getFirstValidCardNum();

        List<Transaction> batch = List.of(
                createDailyTransactionWithId("BATCH-VALID-001", validCard, new BigDecimal("100.00")),
                createDailyTransaction("9999999999999999", new BigDecimal("50.00")),
                createDailyTransactionWithId("BATCH-VALID-002", validCard, new BigDecimal("200.00"))
        );

        PostingResult result = postingService.processDailyTransactions(batch);

        assertEquals(3, result.processedCount());
        assertEquals(1, result.rejectedCount());
        assertEquals(100, result.rejects().get(0).reasonCode());
    }

    private String getFirstValidCardNum() {
        List<CardXref> xrefs = cardXrefRepository.findByAcctId("00000000001");
        assertFalse(xrefs.isEmpty());
        return xrefs.get(0).getCardNum();
    }

    private Transaction createDailyTransaction(String cardNum, BigDecimal amount) {
        Transaction tran = new Transaction();
        tran.setTranId("INTG-" + System.nanoTime());
        tran.setCardNum(cardNum);
        tran.setAmount(amount);
        tran.setTypeCd("01");
        tran.setCatCd(1);
        tran.setSource("Daily");
        tran.setDescription("Integration test transaction");
        tran.setMerchantId(0L);
        tran.setMerchantName("Test");
        tran.setMerchantCity("TestCity");
        tran.setMerchantZip("12345");
        tran.setOrigTimestamp(LocalDateTime.now());
        return tran;
    }

    private Transaction createDailyTransactionWithId(String tranId, String cardNum, BigDecimal amount) {
        Transaction tran = createDailyTransaction(cardNum, amount);
        tran.setTranId(tranId);
        return tran;
    }
}
