package com.carddemo.repository;

import com.carddemo.entity.Transaction;
import com.carddemo.entity.TransactionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();

        Transaction txn1 = new Transaction();
        txn1.setCardNum("4000123456789010");
        txn1.setTranId("0000000000000001");
        txn1.setTypeCd("01");
        txn1.setCatCd(1);
        txn1.setAmount(new BigDecimal("100.00"));
        txn1.setDescription("Purchase at Store A");
        txn1.setOrigTimestamp("2024-01-15 10:30:00.000000");
        transactionRepository.save(txn1);

        Transaction txn2 = new Transaction();
        txn2.setCardNum("4000123456789010");
        txn2.setTranId("0000000000000002");
        txn2.setTypeCd("01");
        txn2.setCatCd(1);
        txn2.setAmount(new BigDecimal("250.00"));
        txn2.setDescription("Purchase at Store B");
        txn2.setOrigTimestamp("2024-06-20 14:15:00.000000");
        transactionRepository.save(txn2);

        Transaction txn3 = new Transaction();
        txn3.setCardNum("4000123456789020");
        txn3.setTranId("0000000000000003");
        txn3.setTypeCd("02");
        txn3.setCatCd(2);
        txn3.setAmount(new BigDecimal("75.50"));
        txn3.setDescription("Online purchase");
        txn3.setOrigTimestamp("2024-03-10 08:00:00.000000");
        transactionRepository.save(txn3);
    }

    @Test
    void findById_existingTransaction_returnsTransaction() {
        TransactionId id = new TransactionId("4000123456789010", "0000000000000001");
        Optional<Transaction> found = transactionRepository.findById(id);

        assertTrue(found.isPresent());
        assertEquals(new BigDecimal("100.00"), found.get().getAmount());
        assertEquals("Purchase at Store A", found.get().getDescription());
    }

    @Test
    void findByCardNum_returnsTransactionsForCard() {
        List<Transaction> txns = transactionRepository.findByCardNum("4000123456789010");
        assertEquals(2, txns.size());
    }

    @Test
    void findByCardNum_paged_returnsPagedResults() {
        Page<Transaction> page = transactionRepository.findByCardNum("4000123456789010", PageRequest.of(0, 1));
        assertEquals(2, page.getTotalElements());
        assertEquals(1, page.getContent().size());
    }

    @Test
    void findByCardNumAndDateRange_returnsFilteredResults() {
        List<Transaction> txns = transactionRepository.findByCardNumAndDateRange(
                "4000123456789010",
                "2024-01-01 00:00:00.000000",
                "2024-03-31 23:59:59.999999");

        assertEquals(1, txns.size());
        assertEquals("0000000000000001", txns.get(0).getTranId());
    }

    @Test
    void findByCardNum_noTransactions_returnsEmptyList() {
        List<Transaction> txns = transactionRepository.findByCardNum("9999999999999999");
        assertTrue(txns.isEmpty());
    }

    @Test
    void save_andDelete_works() {
        TransactionId id = new TransactionId("4000123456789010", "0000000000000001");
        transactionRepository.deleteById(id);
        assertFalse(transactionRepository.findById(id).isPresent());
    }
}
