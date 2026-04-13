package com.carddemo.batch;

import com.carddemo.entity.Account;
import com.carddemo.entity.CardXref;
import com.carddemo.entity.Transaction;
import com.carddemo.repository.AccountRepository;
import com.carddemo.repository.CardXrefRepository;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementGenerationTaskletTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    private StatementGenerationTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new StatementGenerationTasklet(accountRepository, cardXrefRepository, transactionRepository);
    }

    @AfterEach
    void cleanUp() throws Exception {
        Path stmtDir = Path.of("statements");
        if (Files.exists(stmtDir)) {
            Files.walk(stmtDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void executeGeneratesTextAndHtmlStatements() throws Exception {
        Account account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("2500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));

        CardXref xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setAcctId(100001L);

        Transaction txn = new Transaction();
        txn.setCardNum("4111111111111111");
        txn.setTranId("TXN001");
        txn.setAmount(new BigDecimal("50.00"));
        txn.setDescription("Coffee Shop");
        txn.setOrigTimestamp("2026-01-15T10:30:00");

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(cardXrefRepository.findByAcctId(100001L)).thenReturn(List.of(xref));
        when(transactionRepository.findByCardNum("4111111111111111")).thenReturn(List.of(txn));

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File stmtDir = new File("statements");
        assertTrue(stmtDir.exists());

        File[] txtFiles = stmtDir.listFiles((dir, name) -> name.endsWith(".txt") && name.startsWith("stmt_100001"));
        File[] htmlFiles = stmtDir.listFiles((dir, name) -> name.endsWith(".html") && name.startsWith("stmt_100001"));
        assertNotNull(txtFiles);
        assertNotNull(htmlFiles);
        assertTrue(txtFiles.length > 0, "Text statement should be generated");
        assertTrue(htmlFiles.length > 0, "HTML statement should be generated");
    }

    @Test
    void executeTextStatementContainsAccountInfo() throws Exception {
        Account account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("2500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(cardXrefRepository.findByAcctId(100001L)).thenReturn(Collections.emptyList());

        tasklet.execute(stepContribution, chunkContext);

        File stmtDir = new File("statements");
        File[] txtFiles = stmtDir.listFiles((dir, name) -> name.endsWith(".txt") && name.startsWith("stmt_100001"));
        assertNotNull(txtFiles);
        String content = Files.readString(txtFiles[0].toPath());
        assertTrue(content.contains("CARDEMO ACCOUNT STATEMENT"));
        assertTrue(content.contains("100001"));
        assertTrue(content.contains("2,500.00"));
    }

    @Test
    void executeHtmlStatementContainsTableStructure() throws Exception {
        Account account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("2500.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(cardXrefRepository.findByAcctId(100001L)).thenReturn(Collections.emptyList());

        tasklet.execute(stepContribution, chunkContext);

        File stmtDir = new File("statements");
        File[] htmlFiles = stmtDir.listFiles((dir, name) -> name.endsWith(".html") && name.startsWith("stmt_100001"));
        assertNotNull(htmlFiles);
        String content = Files.readString(htmlFiles[0].toPath());
        assertTrue(content.contains("<html>"));
        assertTrue(content.contains("<table"));
        assertTrue(content.contains("Account Statement - 100001"));
    }

    @Test
    void executeWithNoAccounts_doesNotGenerateFiles() throws Exception {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
    }

    @Test
    void executeProcessesMultipleAccounts() throws Exception {
        Account account1 = new Account();
        account1.setAcctId(100001L);
        account1.setActiveStatus("Y");
        account1.setCurrentBalance(new BigDecimal("2500.00"));
        account1.setCreditLimit(new BigDecimal("10000.00"));

        Account account2 = new Account();
        account2.setAcctId(100002L);
        account2.setActiveStatus("Y");
        account2.setCurrentBalance(new BigDecimal("3000.00"));
        account2.setCreditLimit(new BigDecimal("15000.00"));

        when(accountRepository.findAll()).thenReturn(List.of(account1, account2));
        when(cardXrefRepository.findByAcctId(anyLong())).thenReturn(Collections.emptyList());

        tasklet.execute(stepContribution, chunkContext);

        File stmtDir = new File("statements");
        File[] txtFiles = stmtDir.listFiles((dir, name) -> name.endsWith(".txt"));
        assertNotNull(txtFiles);
        assertEquals(2, txtFiles.length);
    }
}
