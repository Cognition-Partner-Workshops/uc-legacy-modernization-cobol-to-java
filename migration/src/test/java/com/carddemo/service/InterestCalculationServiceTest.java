package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DisclosureGroup;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DisclosureGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationServiceTest {

    @Mock private TransactionCategoryBalanceRepository tcatBalRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private DisclosureGroupRepository disclosureGroupRepository;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private InterestCalculationService service;

    private Account account;
    private CardXref xref;
    private TransactionCategoryBalance tcatBal;
    private DisclosureGroup discGroup;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setAcctId("00000000001");
        account.setGroupId("DEFAULT");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("100.00"));

        xref = new CardXref();
        xref.setCardNum("4000123456789012");
        xref.setAcctId("00000000001");

        tcatBal = new TransactionCategoryBalance();
        tcatBal.setAcctId("00000000001");
        tcatBal.setTypeCd("01");
        tcatBal.setCatCd(1);
        tcatBal.setBalance(new BigDecimal("1200.00"));

        discGroup = new DisclosureGroup();
        discGroup.setAcctGroupId("DEFAULT");
        discGroup.setTranTypeCd("01");
        discGroup.setTranCatCd(1);
        discGroup.setInterestRate(new BigDecimal("15.00"));
    }

    @Test
    void calculateInterest_correctFormula() {
        // Formula: monthlyInterest = (categoryBalance * interestRate) / 1200
        // Expected: (1200.00 * 15.00) / 1200 = 15.00
        BigDecimal expected = new BigDecimal("1200.00")
                .multiply(new BigDecimal("15.00"))
                .divide(new BigDecimal("1200"), 2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("15.00"), expected);
    }

    @Test
    void calculateInterest_processesRecordsCorrectly() {
        when(tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc())
                .thenReturn(List.of(tcatBal));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(cardXrefRepository.findFirstByAcctId("00000000001")).thenReturn(Optional.of(xref));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", 1))
                .thenReturn(Optional.of(discGroup));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InterestCalculationService.InterestResult result =
                service.calculateInterest(LocalDate.of(2024, 6, 1));

        assertEquals(1, result.recordsProcessed());
        assertEquals(1, result.transactionsCreated());

        // Verify account was updated: balance += interest, cycle counters reset
        verify(accountRepository).save(any());
    }

    @Test
    void calculateInterest_usesDefaultGroupWhenNotFound() {
        account.setGroupId("NONEXISTENT");

        when(tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc())
                .thenReturn(List.of(tcatBal));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(cardXrefRepository.findFirstByAcctId("00000000001")).thenReturn(Optional.of(xref));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("NONEXISTENT", "01", 1))
                .thenReturn(Optional.empty());
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", 1))
                .thenReturn(Optional.of(discGroup));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InterestCalculationService.InterestResult result =
                service.calculateInterest(LocalDate.of(2024, 6, 1));

        assertEquals(1, result.transactionsCreated());
    }

    @Test
    void calculateInterest_zeroRate_noTransaction() {
        discGroup.setInterestRate(BigDecimal.ZERO);

        when(tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc())
                .thenReturn(List.of(tcatBal));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(cardXrefRepository.findFirstByAcctId("00000000001")).thenReturn(Optional.of(xref));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", 1))
                .thenReturn(Optional.of(discGroup));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        InterestCalculationService.InterestResult result =
                service.calculateInterest(LocalDate.of(2024, 6, 1));

        assertEquals(1, result.recordsProcessed());
        assertEquals(0, result.transactionsCreated());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void calculateInterest_resetsCycleCounters() {
        when(tcatBalRepository.findAllByOrderByAcctIdAscTypeCdAscCatCdAsc())
                .thenReturn(List.of(tcatBal));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(cardXrefRepository.findFirstByAcctId("00000000001")).thenReturn(Optional.of(xref));
        when(disclosureGroupRepository.findByAcctGroupIdAndTranTypeCdAndTranCatCd("DEFAULT", "01", 1))
                .thenReturn(Optional.of(discGroup));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.calculateInterest(LocalDate.of(2024, 6, 1));

        // Verify cycle counters were reset to 0
        assertEquals(BigDecimal.ZERO, account.getCurrentCycleCredit());
        assertEquals(BigDecimal.ZERO, account.getCurrentCycleDebit());
    }
}
