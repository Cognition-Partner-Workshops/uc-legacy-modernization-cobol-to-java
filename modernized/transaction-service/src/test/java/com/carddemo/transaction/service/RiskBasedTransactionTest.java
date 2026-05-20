package com.carddemo.transaction.service;

import com.carddemo.common.exception.ResourceNotFoundException;
import com.carddemo.transaction.dto.AddTransactionRequest;
import com.carddemo.transaction.dto.TransactionDto;
import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * TIER 1 — CRITICAL RISK: Financial Transaction Processing
 * Risk factors: monetary precision loss, duplicate transactions, data corruption
 */
@ExtendWith(MockitoExtension.class)
@Tag("risk-tier-1")
@DisplayName("Tier 1 (Critical): Financial Transaction Processing")
class RiskBasedTransactionTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    // --- BigDecimal Precision (highest financial risk) ---

    @Test
    @DisplayName("T1-TXN-001: Transaction amount preserves decimal precision")
    void addTransaction_preservesDecimalPrecision() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("1234.56"))
                .description("Precision test")
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getAmount()).isEqualByComparingTo("1234.56");
    }

    @Test
    @DisplayName("T1-TXN-002: Small fractional amount (pennies) is preserved")
    void addTransaction_preservesPennies() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("0.01"))
                .description("Penny test")
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getAmount()).isEqualByComparingTo("0.01");
    }

    @Test
    @DisplayName("T1-TXN-003: Large amount near NUMERIC(11,2) limit is preserved")
    void addTransaction_preservesLargeAmount() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("999999999.99"))
                .description("Max amount test")
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getAmount()).isEqualByComparingTo("999999999.99");
    }

    @Test
    @DisplayName("T1-TXN-004: Negative amount (refund/payment) is preserved")
    void addTransaction_preservesNegativeAmount() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("05").catCd(1)
                .amount(new BigDecimal("-500.00"))
                .description("Payment credit")
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getAmount()).isEqualByComparingTo("-500.00");
    }

    // --- Transaction ID Generation ---

    @Test
    @DisplayName("T1-TXN-005: Transaction ID is exactly 16 characters")
    void addTransaction_generatesCorrectLengthId() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("100.00"))
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getTranId()).hasSize(16);
    }

    @Test
    @DisplayName("T1-TXN-006: Two transactions get different IDs (no duplicates)")
    void addTransaction_uniqueIds() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("100.00"))
                .build();

        TransactionDto result1 = transactionService.addTransaction(request);
        TransactionDto result2 = transactionService.addTransaction(request);

        assertThat(result1.getTranId()).isNotEqualTo(result2.getTranId());
    }

    // --- Processing Timestamp ---

    @Test
    @DisplayName("T1-TXN-007: Processing timestamp is set on transaction add")
    void addTransaction_setsProcessingTimestamp() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .amount(new BigDecimal("50.00"))
                .build();

        TransactionDto result = transactionService.addTransaction(request);
        assertThat(result.getProcTs()).isNotNull();
        assertThat(result.getProcTs()).isNotBlank();
    }

    // --- Transaction Retrieval ---

    @Test
    @DisplayName("T1-TXN-008: Get nonexistent transaction throws ResourceNotFoundException")
    void getTransaction_notFound_throws() {
        when(transactionRepository.findByTranId("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransaction("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("T1-TXN-009: List transactions by card returns correct pagination")
    void listTransactions_paginatesCorrectly() {
        Transaction txn = Transaction.builder()
                .tranId("TXN0000000000001")
                .cardNum("4111111111111111")
                .amount(new BigDecimal("125.50"))
                .typeCd("01").catCd(1)
                .build();
        Page<Transaction> page = new PageImpl<>(List.of(txn), PageRequest.of(0, 20), 1);
        when(transactionRepository.findByCardNum("4111111111111111", PageRequest.of(0, 20)))
                .thenReturn(page);

        var result = transactionService.listTransactions("4111111111111111", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getAmount()).isEqualByComparingTo("125.50");
    }

    @Test
    @DisplayName("T1-TXN-010: List transactions for card with no transactions returns empty")
    void listTransactions_emptyResult() {
        Page<Transaction> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(transactionRepository.findByCardNum("9999999999999999", PageRequest.of(0, 20)))
                .thenReturn(emptyPage);

        var result = transactionService.listTransactions("9999999999999999", PageRequest.of(0, 20));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // --- All merchant fields preserved ---

    @Test
    @DisplayName("T1-TXN-011: All merchant fields are stored and returned")
    void addTransaction_allFieldsPreserved() {
        when(transactionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AddTransactionRequest request = AddTransactionRequest.builder()
                .cardNum("4111111111111111")
                .typeCd("01").catCd(1)
                .source("POS")
                .description("Grocery purchase")
                .amount(new BigDecimal("125.50"))
                .merchantId("M00000001")
                .merchantName("Fresh Foods Market")
                .merchantCity("New York")
                .merchantZip("10001")
                .build();

        TransactionDto result = transactionService.addTransaction(request);

        assertThat(result.getSource()).isEqualTo("POS");
        assertThat(result.getDescription()).isEqualTo("Grocery purchase");
        assertThat(result.getMerchantId()).isEqualTo("M00000001");
        assertThat(result.getMerchantName()).isEqualTo("Fresh Foods Market");
        assertThat(result.getMerchantCity()).isEqualTo("New York");
        assertThat(result.getMerchantZip()).isEqualTo("10001");
        assertThat(result.getCardNum()).isEqualTo("4111111111111111");
    }
}
