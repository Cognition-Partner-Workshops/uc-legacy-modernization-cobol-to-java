package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionPostingServiceTest {

    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionCategoryBalanceRepository tcatBalRepository;

    @InjectMocks
    private TransactionPostingService service;

    private Transaction dailyTran;
    private CardXref xref;
    private Account account;

    @BeforeEach
    void setUp() {
        dailyTran = new Transaction();
        dailyTran.setTranId("TEST000000000001");
        dailyTran.setCardNum("4000123456789012");
        dailyTran.setTypeCd("01");
        dailyTran.setCatCd(1);
        dailyTran.setAmount(new BigDecimal("100.00"));
        dailyTran.setOrigTimestamp(LocalDateTime.of(2024, 1, 15, 10, 30));
        dailyTran.setSource("POS TERM");
        dailyTran.setDescription("Test purchase");
        dailyTran.setMerchantId(0L);
        dailyTran.setMerchantName("Test Merchant");
        dailyTran.setMerchantCity("Test City");
        dailyTran.setMerchantZip("12345");

        xref = new CardXref();
        xref.setCardNum("4000123456789012");
        xref.setCustId(1L);
        xref.setAcctId("00000000001");

        account = new Account();
        account.setAcctId("00000000001");
        account.setActiveStatus("Y");
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCurrentBalance(new BigDecimal("500.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("100.00"));
        account.setExpirationDate(LocalDate.of(2025, 12, 31));
    }

    @Test
    void validateTransaction_invalidCard_returns100() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.empty());

        TransactionPostingService.ValidationResult result = service.validateTransaction(dailyTran);

        assertEquals(TransactionPostingService.REJECT_INVALID_CARD, result.reasonCode());
        assertEquals("INVALID CARD NUMBER FOUND", result.reasonDescription());
    }

    @Test
    void validateTransaction_accountNotFound_returns101() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.empty());

        TransactionPostingService.ValidationResult result = service.validateTransaction(dailyTran);

        assertEquals(TransactionPostingService.REJECT_ACCOUNT_NOT_FOUND, result.reasonCode());
        assertEquals("ACCOUNT RECORD NOT FOUND", result.reasonDescription());
    }

    @Test
    void validateTransaction_overlimit_returns102() {
        account.setCreditLimit(new BigDecimal("150.00"));
        account.setCurrentCycleCredit(new BigDecimal("100.00"));
        account.setCurrentCycleDebit(new BigDecimal("0.00"));

        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        TransactionPostingService.ValidationResult result = service.validateTransaction(dailyTran);

        assertEquals(TransactionPostingService.REJECT_OVERLIMIT, result.reasonCode());
        assertEquals("OVERLIMIT TRANSACTION", result.reasonDescription());
    }

    @Test
    void validateTransaction_expired_returns103() {
        account.setExpirationDate(LocalDate.of(2023, 1, 1));

        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        TransactionPostingService.ValidationResult result = service.validateTransaction(dailyTran);

        assertEquals(TransactionPostingService.REJECT_EXPIRED, result.reasonCode());
        assertEquals("TRANSACTION RECEIVED AFTER ACCT EXPIRATION", result.reasonDescription());
    }

    @Test
    void validateTransaction_valid_returns0() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        TransactionPostingService.ValidationResult result = service.validateTransaction(dailyTran);

        assertEquals(0, result.reasonCode());
        assertNull(result.reasonDescription());
    }

    @Test
    void postTransaction_updatesBalancesCorrectly_creditAmount() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(tcatBalRepository.findByAcctIdAndTypeCdAndCatCd("00000000001", "01", 1))
                .thenReturn(Optional.empty());
        when(tcatBalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.postTransaction(dailyTran);

        // Verify account balance was updated: 500 + 100 = 600
        assertEquals(new BigDecimal("600.00"), account.getCurrentBalance());
        // Amount >= 0, so currentCycleCredit should be updated: 200 + 100 = 300
        assertEquals(new BigDecimal("300.00"), account.getCurrentCycleCredit());
    }

    @Test
    void postTransaction_updatesBalancesCorrectly_debitAmount() {
        dailyTran.setAmount(new BigDecimal("-50.00"));
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(tcatBalRepository.findByAcctIdAndTypeCdAndCatCd("00000000001", "01", 1))
                .thenReturn(Optional.empty());
        when(tcatBalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.postTransaction(dailyTran);

        // Verify account balance was updated: 500 + (-50) = 450
        assertEquals(new BigDecimal("450.00"), account.getCurrentBalance());
        // Amount < 0, so currentCycleDebit should be updated: 100 + (-50) = 50
        assertEquals(new BigDecimal("50.00"), account.getCurrentCycleDebit());
    }

    @Test
    void processDailyTransactions_separatesValidAndRejected() {
        Transaction validTran = dailyTran;
        Transaction invalidTran = new Transaction();
        invalidTran.setTranId("TEST000000000002");
        invalidTran.setCardNum("9999999999999999");
        invalidTran.setAmount(new BigDecimal("50.00"));
        invalidTran.setTypeCd("01");
        invalidTran.setCatCd(1);
        invalidTran.setOrigTimestamp(LocalDateTime.now());
        invalidTran.setMerchantId(0L);
        invalidTran.setMerchantName("");
        invalidTran.setMerchantCity("");
        invalidTran.setMerchantZip("");

        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(tcatBalRepository.findByAcctIdAndTypeCdAndCatCd(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(tcatBalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TransactionPostingService.PostingResult result =
                service.processDailyTransactions(List.of(validTran, invalidTran));

        assertEquals(2, result.processedCount());
        assertEquals(1, result.rejectedCount());
        assertEquals(1, result.rejects().size());
        assertEquals(100, result.rejects().get(0).reasonCode());
    }
}
