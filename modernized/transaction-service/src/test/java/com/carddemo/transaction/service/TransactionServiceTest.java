package com.carddemo.transaction.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void addTransaction_generatesIdAndSetsTimestamp() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01")
                .catCd(1)
                .amount(new BigDecimal("100.50"))
                .description("Test purchase")
                .build();

        TransactionDto result = transactionService.addTransaction(request);

        assertThat(result.getTranId()).isNotNull();
        assertThat(result.getTranId()).hasSize(16);
        assertThat(result.getProcTs()).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo("100.50");
    }

    @Test
    void listTransactions_paginatesCorrectly() {
        Transaction txn = Transaction.builder()
                .tranId("0000000000000001")
                .cardNum("4111111111111111")
                .amount(new BigDecimal("50.00"))
                .build();
        Page<Transaction> page = new PageImpl<>(List.of(txn));
        when(transactionRepository.findByCardNum("4111111111111111", PageRequest.of(0, 20)))
                .thenReturn(page);

        var result = transactionService.listTransactions("4111111111111111", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getTransaction_notFound_throws() {
        when(transactionRepository.findByTranId("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
