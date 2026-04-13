package com.carddemo.batch;

import com.carddemo.entity.DailyTransaction;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.DailyTransactionRepository;
import com.carddemo.repository.TransactionRepository;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionValidationTaskletTest {

    @Mock
    private DailyTransactionRepository dailyTransactionRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @InjectMocks
    private TransactionValidationTasklet tasklet;

    private DailyTransaction validDaily;
    private DailyTransaction invalidDaily;

    @BeforeEach
    void setUp() {
        validDaily = new DailyTransaction();
        validDaily.setCardNum("4111111111111111");
        validDaily.setTranId("TXN001");
        validDaily.setTypeCd("01");
        validDaily.setCatCd(5000);
        validDaily.setSource("ONLINE");
        validDaily.setDescription("Test purchase");
        validDaily.setAmount(new BigDecimal("100.00"));
        validDaily.setMerchantId(12345L);
        validDaily.setMerchantName("Test Store");
        validDaily.setMerchantCity("New York");
        validDaily.setMerchantZip("10001");
        validDaily.setOrigTimestamp("2026-01-15T10:30:00.000000");
        validDaily.setProcTimestamp("2026-01-15T10:30:01.000000");

        invalidDaily = new DailyTransaction();
        invalidDaily.setCardNum("9999999999999999");
        invalidDaily.setTranId("TXN002");
        invalidDaily.setTypeCd("02");
        invalidDaily.setCatCd(6000);
        invalidDaily.setAmount(new BigDecimal("50.00"));
    }

    @Test
    void executeWithValidCard_savesTransaction() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(validDaily));
        when(cardXrefRepository.existsById("4111111111111111")).thenReturn(true);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals("4111111111111111", saved.getCardNum());
        assertEquals("TXN001", saved.getTranId());
        assertEquals("01", saved.getTypeCd());
        assertEquals(5000, saved.getCatCd());
        assertEquals("ONLINE", saved.getSource());
        assertEquals("Test purchase", saved.getDescription());
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
        assertEquals(12345L, saved.getMerchantId());
        assertEquals("Test Store", saved.getMerchantName());
        assertEquals("New York", saved.getMerchantCity());
        assertEquals("10001", saved.getMerchantZip());
    }

    @Test
    void executeWithInvalidCard_rejectsTransaction() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(invalidDaily));
        when(cardXrefRepository.existsById("9999999999999999")).thenReturn(false);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void executeWithMixedTransactions_validatesCorrectly() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(Arrays.asList(validDaily, invalidDaily));
        when(cardXrefRepository.existsById("4111111111111111")).thenReturn(true);
        when(cardXrefRepository.existsById("9999999999999999")).thenReturn(false);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void executeWithEmptyDailyTransactions_doesNothing() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void executeCopiesAllFieldsFromDailyToTransaction() throws Exception {
        when(dailyTransactionRepository.findAll()).thenReturn(List.of(validDaily));
        when(cardXrefRepository.existsById("4111111111111111")).thenReturn(true);

        tasklet.execute(stepContribution, chunkContext);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction txn = captor.getValue();
        assertEquals(validDaily.getOrigTimestamp(), txn.getOrigTimestamp());
        assertEquals(validDaily.getProcTimestamp(), txn.getProcTimestamp());
    }
}
