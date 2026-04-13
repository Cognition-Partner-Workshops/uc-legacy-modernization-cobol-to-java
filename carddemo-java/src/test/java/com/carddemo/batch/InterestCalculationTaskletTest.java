package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.DiscountGroup;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.DiscountGroupRepository;
import com.carddemo.repository.TransactionCategoryBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterestCalculationTaskletTest {

    @Mock
    private TransactionCategoryBalanceRepository tranCatBalanceRepository;

    @Mock
    private DiscountGroupRepository discountGroupRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @InjectMocks
    private InterestCalculationTasklet tasklet;

    private TransactionCategoryBalance balance;
    private Account account;
    private DiscountGroup discountGroup;

    @BeforeEach
    void setUp() {
        balance = new TransactionCategoryBalance();
        balance.setAcctId(100001L);
        balance.setTypeCd("01");
        balance.setCatCd(5000);
        balance.setBalance(new BigDecimal("1200.00"));

        account = new Account();
        account.setAcctId(100001L);
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setGroupId("A000000000");

        discountGroup = new DiscountGroup();
        discountGroup.setAcctGroupId("A000000000");
        discountGroup.setTranTypeCd("01");
        discountGroup.setTranCatCd(5000);
        discountGroup.setInterestRate(new BigDecimal("0.18"));
    }

    @Test
    void executeCalculatesInterestAndUpdatesAccountBalance() throws Exception {
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(discountGroupRepository.findById(any())).thenReturn(Optional.of(discountGroup));

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        // Monthly rate = 0.18 / 12 = 0.015, interest = 1200.00 * 0.015 = 18.00
        assertEquals(new BigDecimal("5018.00"), saved.getCurrentBalance());
    }

    @Test
    void executeSkipsZeroBalanceEntries() throws Exception {
        balance.setBalance(BigDecimal.ZERO);
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).findById(any());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeSkipsNegativeBalanceEntries() throws Exception {
        balance.setBalance(new BigDecimal("-500.00"));
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeSkipsWhenAccountNotFound() throws Exception {
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));
        when(accountRepository.findById(100001L)).thenReturn(Optional.empty());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeSkipsWhenInterestRateIsZero() throws Exception {
        discountGroup.setInterestRate(BigDecimal.ZERO);
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(discountGroupRepository.findById(any())).thenReturn(Optional.of(discountGroup));

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeUsesZeroRateWhenDiscountGroupNotFound() throws Exception {
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(discountGroupRepository.findById(any())).thenReturn(Optional.empty());

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeWithEmptyBalances_doesNothing() throws Exception {
        when(tranCatBalanceRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void executeUsesDefaultGroupIdWhenAccountGroupIsNull() throws Exception {
        account.setGroupId(null);
        when(tranCatBalanceRepository.findAll()).thenReturn(List.of(balance));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(discountGroupRepository.findById(any())).thenReturn(Optional.of(discountGroup));

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository).save(any(Account.class));
    }
}
