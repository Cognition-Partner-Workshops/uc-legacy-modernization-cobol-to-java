package com.carddemo.service;

import com.carddemo.dto.TransactionAddRequest;
import com.carddemo.exception.TransactionValidationException;
import com.carddemo.model.Account;
import com.carddemo.model.CardXref;
import com.carddemo.model.Transaction;
import com.carddemo.model.TransactionCategoryBalance;
import com.carddemo.model.TransactionCategoryBalanceId;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionCategoryBalanceRepository tcatBalRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account testAccount;
    private CardXref testXref;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .acctId(1L)
                .activeStatus("Y")
                .currBal(new BigDecimal("100.00"))
                .creditLimit(new BigDecimal("5000.00"))
                .cashCreditLimit(new BigDecimal("1000.00"))
                .expirationDate("2030-12-31")
                .currCycCredit(BigDecimal.ZERO)
                .currCycDebit(BigDecimal.ZERO)
                .build();

        testXref = CardXref.builder()
                .cardNum("4000123456789010")
                .custId(1L)
                .acctId(1L)
                .build();
    }

    @Test
    void getTransactions_returnsPage() {
        Page<Transaction> page = new PageImpl<>(List.of(
                Transaction.builder().tranId("0000000000000001").build()
        ));
        when(transactionRepository.findAllByOrderByTranIdDesc(any(Pageable.class))).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactions(0);

        assertEquals(1, result.getTotalElements());
        verify(transactionRepository).findAllByOrderByTranIdDesc(any(Pageable.class));
    }

    @Test
    void getTransactionsByCardNum_returnsFilteredPage() {
        Page<Transaction> page = new PageImpl<>(List.of(
                Transaction.builder().tranId("0000000000000001").cardNum("4000123456789010").build()
        ));
        when(transactionRepository.findByCardNum(anyString(), any(Pageable.class))).thenReturn(page);

        Page<Transaction> result = transactionService.getTransactionsByCardNum("4000123456789010", 0);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void addTransaction_success() {
        TransactionAddRequest request = new TransactionAddRequest();
        request.setCardNum("4000123456789010");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("50.00"));
        request.setSource("ONLINE");
        request.setDescription("Test transaction");

        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(transactionRepository.findMaxTranId()).thenReturn(0L);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
        when(tcatBalRepository.findById(any(TransactionCategoryBalanceId.class))).thenReturn(Optional.empty());
        when(tcatBalRepository.save(any(TransactionCategoryBalance.class))).thenAnswer(i -> i.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.addTransaction(request);

        assertNotNull(result);
        assertEquals("01", result.getTypeCd());
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        verify(transactionRepository).save(any(Transaction.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void addTransaction_cardNotFound_throwsReason100() {
        TransactionAddRequest request = new TransactionAddRequest();
        request.setCardNum("9999999999999999");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("50.00"));

        when(cardXrefRepository.findById("9999999999999999")).thenReturn(Optional.empty());

        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.addTransaction(request)
        );
        assertEquals(100, ex.getReasonCode());
    }

    @Test
    void addTransaction_accountNotFound_throwsReason101() {
        TransactionAddRequest request = new TransactionAddRequest();
        request.setCardNum("4000123456789010");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("50.00"));

        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.addTransaction(request)
        );
        assertEquals(101, ex.getReasonCode());
    }

    @Test
    void addTransaction_exceedsCreditLimit_throwsReason102() {
        TransactionAddRequest request = new TransactionAddRequest();
        request.setCardNum("4000123456789010");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("10000.00"));

        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.addTransaction(request)
        );
        assertEquals(102, ex.getReasonCode());
    }

    @Test
    void addTransaction_accountExpired_throwsReason103() {
        testAccount.setExpirationDate("2020-01-01");

        TransactionAddRequest request = new TransactionAddRequest();
        request.setCardNum("4000123456789010");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("50.00"));

        when(cardXrefRepository.findById("4000123456789010")).thenReturn(Optional.of(testXref));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        TransactionValidationException ex = assertThrows(
                TransactionValidationException.class,
                () -> transactionService.addTransaction(request)
        );
        assertEquals(103, ex.getReasonCode());
    }

    @Test
    void findById_returnsTransaction() {
        Transaction transaction = Transaction.builder().tranId("0000000000000001").build();
        when(transactionRepository.findById("0000000000000001")).thenReturn(Optional.of(transaction));

        Optional<Transaction> result = transactionService.findById("0000000000000001");

        assertTrue(result.isPresent());
        assertEquals("0000000000000001", result.get().getTranId());
    }
}
