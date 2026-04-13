package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.TransactionCategoryBalance;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
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
class TransactionPostingTaskletTest {

    @Mock
    private DailyTransactionRepository dailyTransactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionCategoryBalanceRepository tranCatBalanceRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @InjectMocks
    private TransactionPostingTasklet tasklet;

    private DailyTransaction dailyTxn;
    private CardXref xref;
    private Account account;

    @BeforeEach
    void setUp() {
        dailyTxn = new DailyTransaction();
        dailyTxn.setCardNum("4111111111111111");
        dailyTxn.setTranId("TXN001");
        dailyTxn.setTypeCd("01");
        dailyTxn.setCatCd(5000);
        dailyTxn.setAmount(new BigDecimal("150.00"));

        xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setCustId(1001L);
        xref.setAcctId(100001L);

        account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("1000.00"));
        account.setCreditLimit(new BigDecimal("5000.00"));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("300.00"));
        account.setGroupId("A000000000");
    }

    @Test
    void executePostsDebitTransaction_updatesBalanceAndCycleDebit() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(tranCatBalanceRepository.findById(any())).thenReturn(Optional.empty());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(acctCaptor.capture());
        Account savedAcct = acctCaptor.getValue();
        assertEquals(new BigDecimal("1150.00"), savedAcct.getCurrentBalance());
        assertEquals(new BigDecimal("450.00"), savedAcct.getCurrentCycleDebit());
    }

    @Test
    void executePostsCreditTransaction_updatesBalanceAndCycleCredit() throws Exception {
        dailyTxn.setAmount(new BigDecimal("-75.00"));
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(tranCatBalanceRepository.findById(any())).thenReturn(Optional.empty());

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(acctCaptor.capture());
        Account savedAcct = acctCaptor.getValue();
        assertEquals(new BigDecimal("925.00"), savedAcct.getCurrentBalance());
        assertEquals(new BigDecimal("275.00"), savedAcct.getCurrentCycleCredit());
    }

    @Test
    void executeRejectsWhenCardXrefNotFound() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.empty());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).findById(any());
    }

    @Test
    void executeRejectsWhenAccountNotFound() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.empty());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void executeUpdatesExistingCategoryBalance() throws Exception {
        TransactionCategoryBalance existingBal = new TransactionCategoryBalance();
        existingBal.setAcctId(100001L);
        existingBal.setTypeCd("01");
        existingBal.setCatCd(5000);
        existingBal.setBalance(new BigDecimal("500.00"));

        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(tranCatBalanceRepository.findById(any())).thenReturn(Optional.of(existingBal));

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<TransactionCategoryBalance> balCaptor = ArgumentCaptor.forClass(TransactionCategoryBalance.class);
        verify(tranCatBalanceRepository).save(balCaptor.capture());
        assertEquals(new BigDecimal("650.00"), balCaptor.getValue().getBalance());
    }

    @Test
    void executeCreatesNewCategoryBalanceIfNotExists() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(tranCatBalanceRepository.findById(any())).thenReturn(Optional.empty());

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<TransactionCategoryBalance> balCaptor = ArgumentCaptor.forClass(TransactionCategoryBalance.class);
        verify(tranCatBalanceRepository).save(balCaptor.capture());
        TransactionCategoryBalance saved = balCaptor.getValue();
        assertEquals(100001L, saved.getAcctId());
        assertEquals("01", saved.getTypeCd());
        assertEquals(5000, saved.getCatCd());
        assertEquals(new BigDecimal("150.00"), saved.getBalance());
    }

    @Test
    void executeWithNullAmount_treatsAsZero() throws Exception {
        dailyTxn.setAmount(null);
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(dailyTxn));
        when(cardXrefRepository.findById("4111111111111111")).thenReturn(Optional.of(xref));
        when(accountRepository.findById(100001L)).thenReturn(Optional.of(account));
        when(tranCatBalanceRepository.findById(any())).thenReturn(Optional.empty());

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Account> acctCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(acctCaptor.capture());
        assertEquals(new BigDecimal("1000.00"), acctCaptor.getValue().getCurrentBalance());
    }

    @Test
    void executeWithEmptyDailyTransactions_doesNothing() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any());
        verify(tranCatBalanceRepository, never()).save(any());
    }
}
