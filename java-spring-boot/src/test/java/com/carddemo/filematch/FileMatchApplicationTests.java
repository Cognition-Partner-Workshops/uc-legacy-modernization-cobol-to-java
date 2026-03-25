package com.carddemo.filematch;

import com.carddemo.filematch.model.Account;
import com.carddemo.filematch.model.ProcessingSummary;
import com.carddemo.filematch.model.Transaction;
import com.carddemo.filematch.service.AccountFileMatchService;
import com.carddemo.filematch.service.TransactionFileMatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class FileMatchApplicationTests {

    @Autowired
    private AccountFileMatchService accountService;

    @Autowired
    private TransactionFileMatchService transactionService;

    @Test
    void contextLoads() {
    }

    @Test
    void testAccountFileProcessing() throws Exception {
        ProcessingSummary summary = accountService.processAccountFile(
                new ClassPathResource("data/accounts.csv"));

        assertNotNull(summary);
        assertEquals("CBACT01C", summary.getProgramName());
        assertEquals(10, summary.getRecordsRead());
        // 7 accounts have status 'Y', 3 have status 'N'
        assertEquals(7, summary.getRecordsMatched());
        assertEquals(3, summary.getRecordsRejected());
        assertEquals("COMPLETED", summary.getStatus());

        List<Account> matched = accountService.getMatchedAccounts();
        assertEquals(7, matched.size());
        assertTrue(matched.stream().allMatch(a -> "Y".equals(a.getAcctActiveStatus())));
    }

    @Test
    void testTransactionFileProcessing() throws Exception {
        ProcessingSummary summary = transactionService.processTransactionFile(
                new ClassPathResource("data/transactions.csv"));

        assertNotNull(summary);
        assertEquals("CBTRN02C", summary.getProgramName());
        assertEquals(10, summary.getRecordsRead());
        // 6 transactions have type 'SA', 2 have 'CR', 1 has 'FE', 1 has 'PA'
        assertEquals(6, summary.getRecordsMatched());
        assertEquals(4, summary.getRecordsRejected());
        assertEquals("COMPLETED", summary.getStatus());

        List<Transaction> matched = transactionService.getMatchedTransactions();
        assertEquals(6, matched.size());
        assertTrue(matched.stream().allMatch(t -> "SA".equals(t.getTranTypeCd())));
    }
}
