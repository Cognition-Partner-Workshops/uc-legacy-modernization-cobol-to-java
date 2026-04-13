package com.carddemo.service;

import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionId;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Transaction testTransaction;
    private CardXref testXref;

    @BeforeEach
    void setUp() {
        testTransaction = new Transaction();
        testTransaction.setCardNum("4000123456789010");
        testTransaction.setTranId("0000000000000001");
        testTransaction.setTypeCd("01");
        testTransaction.setCatCd(1);
        testTransaction.setSource("POS TERM");
        testTransaction.setDescription("Test purchase");
        testTransaction.setAmount(new BigDecimal("100.00"));
        testTransaction.setMerchantId(1L);
        testTransaction.setMerchantName("Test Merchant");
        testTransaction.setMerchantCity("Test City");
        testTransaction.setMerchantZip("12345");
        testTransaction.setOrigTimestamp("2024-01-15 10:30:00.000000");

        testXref = new CardXref();
        testXref.setCardNum("4000123456789010");
        testXref.setCustId(1L);
        testXref.setAcctId(1L);
    }

    @Test
    void listTransactions_returnsPagedResults() {
        Page<Transaction> page = new PageImpl<>(List.of(testTransaction));
        when(transactionRepository.findByCardNum(eq("4000123456789010"), any(Pageable.class)))
                .thenReturn(page);

        Page<Transaction> result = transactionService.listTransactions("4000123456789010", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test purchase", result.getContent().get(0).getDescription());
    }

    @Test
    void viewTransaction_existing_returnsTransaction() {
        TransactionId id = new TransactionId("4000123456789010", "0000000000000001");
        when(transactionRepository.findById(id)).thenReturn(Optional.of(testTransaction));

        Transaction result = transactionService.viewTransaction("4000123456789010", "0000000000000001");

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals("POS TERM", result.getSource());
    }

    @Test
    void viewTransaction_nonExistent_throwsException() {
        TransactionId id = new TransactionId("4000123456789010", "9999999999999999");
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.viewTransaction("4000123456789010", "9999999999999999"));
    }

    @Test
    void addTransaction_validCard_createsTransaction() {
        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        Transaction newTxn = new Transaction();
        newTxn.setCardNum("4000123456789010");
        newTxn.setTypeCd("01");
        newTxn.setCatCd(1);
        newTxn.setAmount(new BigDecimal("50.00"));
        newTxn.setDescription("New purchase");

        Transaction result = transactionService.addTransaction(newTxn);

        assertNotNull(result);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void addTransaction_invalidCard_throwsException() {
        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        Transaction newTxn = new Transaction();
        newTxn.setCardNum("9999999999999999");

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.addTransaction(newTxn));
    }

    @Test
    void addTransaction_generatesTransactionIdIfMissing() {
        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction newTxn = new Transaction();
        newTxn.setCardNum("4000123456789010");
        newTxn.setAmount(new BigDecimal("25.00"));

        Transaction result = transactionService.addTransaction(newTxn);

        assertNotNull(result.getTranId());
        assertFalse(result.getTranId().trim().isEmpty());
    }

    @Test
    void listTransactions_byCardNum_returnsList() {
        when(transactionRepository.findByCardNum("4000123456789010"))
                .thenReturn(List.of(testTransaction));

        List<Transaction> result = transactionService.listTransactions("4000123456789010");

        assertEquals(1, result.size());
    }
}
