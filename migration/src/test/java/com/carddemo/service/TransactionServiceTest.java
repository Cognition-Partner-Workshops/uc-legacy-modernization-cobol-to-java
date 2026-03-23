package com.carddemo.service;

import com.carddemo.dto.TransactionRequest;
import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.exception.AccountExpiredException;
import com.carddemo.exception.CardNotFoundException;
import com.carddemo.exception.OverLimitException;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CardXrefRepository cardXrefRepository;
    @Mock private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService service;

    private CardXref xref;
    private Account account;
    private TransactionRequest request;

    @BeforeEach
    void setUp() {
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
        account.setExpirationDate(LocalDate.of(2027, 12, 31));

        request = new TransactionRequest();
        request.setCardNum("4000123456789012");
        request.setTypeCd("01");
        request.setCatCd(1);
        request.setAmount(new BigDecimal("100.00"));
        request.setSource("Online");
        request.setDescription("Test transaction");
    }

    @Test
    void addTransaction_success() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = service.addTransaction(request);

        assertNotNull(result);
        assertEquals("01", result.getTypeCd());
        assertEquals(1, result.getCatCd());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
    }

    @Test
    void addTransaction_cardNotFound_throwsException() {
        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> service.addTransaction(request));
    }

    @Test
    void addTransaction_overLimit_throwsException() {
        account.setCreditLimit(new BigDecimal("150.00"));
        account.setCurrentCycleCredit(new BigDecimal("100.00"));
        account.setCurrentCycleDebit(new BigDecimal("0.00"));

        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThrows(OverLimitException.class, () -> service.addTransaction(request));
    }

    @Test
    void addTransaction_expired_throwsException() {
        account.setExpirationDate(LocalDate.of(2020, 1, 1));

        when(cardXrefRepository.findById("4000123456789012")).thenReturn(Optional.of(xref));
        when(accountRepository.findById("00000000001")).thenReturn(Optional.of(account));

        assertThrows(AccountExpiredException.class, () -> service.addTransaction(request));
    }

    @Test
    void getTransaction_found() {
        Transaction tran = new Transaction();
        tran.setTranId("TEST0001");
        when(transactionRepository.findById("TEST0001")).thenReturn(Optional.of(tran));

        Transaction result = service.getTransaction("TEST0001");
        assertEquals("TEST0001", result.getTranId());
    }

    @Test
    void getTransaction_notFound_throwsException() {
        when(transactionRepository.findById("NONEXIST")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getTransaction("NONEXIST"));
    }
}
