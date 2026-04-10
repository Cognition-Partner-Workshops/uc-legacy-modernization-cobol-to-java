package com.carddemo.transaction.controller;

import com.carddemo.transaction.model.Transaction;
import com.carddemo.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for Transaction Service (CRUD operations).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        Transaction t1 = new Transaction();
        t1.setTranId("0000000000000001");
        t1.setTranTypeCd("01");
        t1.setTranCatCd(1);
        t1.setTranDesc("Purchase");
        t1.setTranAmt(new BigDecimal("100.00"));
        t1.setTranCardNum("1234567890123456");
        t1.setTranMerchantId(12345L);
        t1.setTranMerchantName("Amazon");
        t1.setTranOrigTs("2024-01-15 10:00:00.000000");
        transactionRepository.save(t1);

        Transaction t2 = new Transaction();
        t2.setTranId("0000000000000002");
        t2.setTranTypeCd("02");
        t2.setTranCatCd(2);
        t2.setTranDesc("Payment");
        t2.setTranAmt(new BigDecimal("-50.00"));
        t2.setTranCardNum("1234567890123456");
        transactionRepository.save(t2);
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("List all transactions returns 200")
    void testListTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Get transaction by valid ID returns 200")
    void testGetTransactionById() throws Exception {
        mockMvc.perform(get("/api/transactions/0000000000000001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranId").value("0000000000000001"))
            .andExpect(jsonPath("$.tranDesc").value("Purchase"));
    }

    @Test
    @DisplayName("Get transactions by card number")
    void testGetTransactionsByCard() throws Exception {
        mockMvc.perform(get("/api/transactions/card/1234567890123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Create new transaction returns 200")
    void testCreateTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"0000000000000003\",\"tranTypeCd\":\"01\",\"tranCatCd\":1,\"tranDesc\":\"New Purchase\",\"tranAmt\":75.00,\"tranCardNum\":\"1234567890123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranId").value("0000000000000003"));
    }

    // ===== BOUNDARY TESTS - NON-EXISTENT TRANSACTION =====

    @Test
    @DisplayName("Get non-existent transaction returns 404")
    void testGetNonExistentTransaction() throws Exception {
        mockMvc.perform(get("/api/transactions/9999999999999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get transactions by non-existent card returns empty list")
    void testGetTransactionsByNonExistentCard() throws Exception {
        mockMvc.perform(get("/api/transactions/card/0000000000000000"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    // ===== BOUNDARY TESTS - AMOUNT VALUES =====

    @Test
    @DisplayName("Create transaction with zero amount")
    void testCreateTransactionZeroAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"0000000000000010\",\"tranTypeCd\":\"01\",\"tranCatCd\":1,\"tranAmt\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranAmt").value(0));
    }

    @Test
    @DisplayName("Create transaction with negative amount (payment)")
    void testCreateTransactionNegativeAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"0000000000000011\",\"tranTypeCd\":\"02\",\"tranCatCd\":1,\"tranAmt\":-999.99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranAmt").value(-999.99));
    }

    @Test
    @DisplayName("Create transaction with very large amount")
    void testCreateTransactionLargeAmount() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tranId\":\"0000000000000012\",\"tranTypeCd\":\"01\",\"tranCatCd\":1,\"tranAmt\":99999999.99}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranAmt").value(99999999.99));
    }

    // ===== RISK-BASED TESTS - TRANSACTION ORDERING =====

    @Test
    @DisplayName("Transactions by card are returned in descending ID order")
    void testTransactionsByCardOrdering() throws Exception {
        mockMvc.perform(get("/api/transactions/card/1234567890123456"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].tranId").value("0000000000000002"))
            .andExpect(jsonPath("$[1].tranId").value("0000000000000001"));
    }

    // ===== RISK-BASED TESTS - PAYMENT VS PURCHASE =====

    @Test
    @DisplayName("Payment transaction (negative amount) is stored correctly")
    void testPaymentTransactionStored() throws Exception {
        mockMvc.perform(get("/api/transactions/0000000000000002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tranAmt").value(-50.00))
            .andExpect(jsonPath("$.tranTypeCd").value("02"));
    }
}
