package com.carddemo.batch;

import com.carddemo.entity.Transaction;
import com.carddemo.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionReportTaskletTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    private TransactionReportTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new TransactionReportTasklet(transactionRepository);
    }

    @AfterEach
    void cleanUp() throws Exception {
        Path reportDir = Path.of("reports");
        if (Files.exists(reportDir)) {
            Files.walk(reportDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    private void setupJobParameters(String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getJobParameters()).thenReturn(params);
    }

    @Test
    void executeGeneratesReportWithTransactions() throws Exception {
        Transaction txn = new Transaction();
        txn.setCardNum("4111111111111111");
        txn.setTranId("TXN001");
        txn.setTypeCd("01");
        txn.setSource("ONLINE");
        txn.setAmount(new BigDecimal("100.00"));
        txn.setDescription("Test purchase");
        txn.setOrigTimestamp("2026-01-15T10:30:00");

        when(transactionRepository.findAll()).thenReturn(List.of(txn));
        setupJobParameters("", "");

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File reportDir = new File("reports");
        assertTrue(reportDir.exists());
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("transaction_report_"));
        assertNotNull(reportFiles);
        assertTrue(reportFiles.length > 0);

        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("CARDEMO TRANSACTION REPORT"));
        assertTrue(content.contains("4111111111111111"));
        assertTrue(content.contains("100.00"));
    }

    @Test
    void executeWithDateRangeFiltersTransactions() throws Exception {
        Transaction inRange = new Transaction();
        inRange.setCardNum("4111111111111111");
        inRange.setTranId("TXN001");
        inRange.setTypeCd("01");
        inRange.setAmount(new BigDecimal("100.00"));
        inRange.setOrigTimestamp("2026-01-15T10:30:00");

        Transaction outOfRange = new Transaction();
        outOfRange.setCardNum("4222222222222222");
        outOfRange.setTranId("TXN002");
        outOfRange.setTypeCd("01");
        outOfRange.setAmount(new BigDecimal("200.00"));
        outOfRange.setOrigTimestamp("2025-06-01T10:30:00");

        when(transactionRepository.findAll()).thenReturn(List.of(inRange, outOfRange));
        setupJobParameters("2026-01-01", "2026-12-31");

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File reportDir = new File("reports");
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("transaction_report_"));
        assertNotNull(reportFiles);
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("4111111111111111"));
        assertFalse(content.contains("4222222222222222"));
    }

    @Test
    void executeWithEmptyTransactions_generatesEmptyReport() throws Exception {
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
        setupJobParameters("", "");

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File reportDir = new File("reports");
        assertTrue(reportDir.exists());
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("transaction_report_"));
        assertNotNull(reportFiles);
        assertTrue(reportFiles.length > 0);
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("TRANSACTION COUNT: 0"));
    }

    @Test
    void executeReportContainsGrandTotal() throws Exception {
        Transaction txn1 = new Transaction();
        txn1.setCardNum("4111111111111111");
        txn1.setTranId("TXN001");
        txn1.setTypeCd("01");
        txn1.setAmount(new BigDecimal("100.00"));

        Transaction txn2 = new Transaction();
        txn2.setCardNum("4222222222222222");
        txn2.setTranId("TXN002");
        txn2.setTypeCd("01");
        txn2.setAmount(new BigDecimal("250.00"));

        when(transactionRepository.findAll()).thenReturn(List.of(txn1, txn2));
        setupJobParameters("", "");

        tasklet.execute(stepContribution, chunkContext);

        File reportDir = new File("reports");
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("transaction_report_"));
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("GRAND TOTAL"));
        assertTrue(content.contains("TRANSACTION COUNT: 2"));
    }
}
