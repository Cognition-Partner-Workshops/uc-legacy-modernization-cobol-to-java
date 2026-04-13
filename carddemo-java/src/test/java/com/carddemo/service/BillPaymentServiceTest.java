package com.carddemo.service;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillPaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardXrefRepository cardXrefRepository;

    @InjectMocks
    private BillPaymentService billPaymentService;

    private Account activeAccount;
    private Account inactiveAccount;

    @BeforeEach
    void setUp() {
        activeAccount = new Account();
        activeAccount.setAcctId(1L);
        activeAccount.setActiveStatus("Y");
        activeAccount.setCurrentBalance(new BigDecimal("5000.00"));
        activeAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        activeAccount.setCurrentCycleDebit(BigDecimal.ZERO);

        inactiveAccount = new Account();
        inactiveAccount.setAcctId(2L);
        inactiveAccount.setActiveStatus("N");
        inactiveAccount.setCurrentBalance(new BigDecimal("3000.00"));
        inactiveAccount.setCurrentCycleCredit(BigDecimal.ZERO);
        inactiveAccount.setCurrentCycleDebit(BigDecimal.ZERO);
    }

    @Test
    void processPayment_successfulPayment_reducesBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        CardXref xref = new CardXref();
        xref.setCardNum("4000123456789010");
        when(cardXrefRepository.findByAcctId(1L)).thenReturn(List.of(xref));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);

        Transaction result = billPaymentService.processPayment(1L, new BigDecimal("1000.00"));

        assertNotNull(result);
        assertEquals(new BigDecimal("4000.00"), activeAccount.getCurrentBalance());
        assertEquals(new BigDecimal("1000.00"), activeAccount.getCurrentCycleCredit());
        verify(accountRepository).save(activeAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void processPayment_inactiveAccount_throwsException() {
        when(accountRepository.findById(2L)).thenReturn(Optional.of(inactiveAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> billPaymentService.processPayment(2L, new BigDecimal("500.00")));
        assertTrue(ex.getMessage().contains("not active"));
    }

    @Test
    void processPayment_exceedsBalance_throwsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> billPaymentService.processPayment(1L, new BigDecimal("10000.00")));
        assertTrue(ex.getMessage().contains("exceeds"));
    }

    @Test
    void processPayment_zeroAmount_throwsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThrows(IllegalArgumentException.class,
                () -> billPaymentService.processPayment(1L, BigDecimal.ZERO));
    }

    @Test
    void processPayment_negativeAmount_throwsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));

        assertThrows(IllegalArgumentException.class,
                () -> billPaymentService.processPayment(1L, new BigDecimal("-100.00")));
    }

    @Test
    void processPayment_nonExistentAccount_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> billPaymentService.processPayment(999L, new BigDecimal("100.00")));
    }

    @Test
    void processPayment_createsTransactionRecord() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(activeAccount));
        when(cardXrefRepository.findByAcctId(1L)).thenReturn(List.of());
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenReturn(activeAccount);

        Transaction result = billPaymentService.processPayment(1L, new BigDecimal("200.00"));

        assertNotNull(result);
        assertEquals("Bill Payment", result.getDescription());
        assertEquals("01", result.getTypeCd());
        assertNotNull(result.getTranId());
    }
}
