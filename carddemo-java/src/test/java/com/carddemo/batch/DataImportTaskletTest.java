package com.carddemo.batch;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataImportTaskletTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CreditCardRepository creditCardRepository;

    @Mock
    private CardXrefRepository cardXrefRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepContext stepContext;

    private DataImportTasklet tasklet;
    private Path tempImportFile;

    @BeforeEach
    void setUp() {
        tasklet = new DataImportTasklet(accountRepository, customerRepository,
                creditCardRepository, cardXrefRepository, transactionRepository);
    }

    @AfterEach
    void cleanUp() throws Exception {
        if (tempImportFile != null && Files.exists(tempImportFile)) {
            Files.delete(tempImportFile);
        }
    }

    private void setupJobParameters(String importFile) {
        Map<String, Object> params = new HashMap<>();
        params.put("importFile", importFile);
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getJobParameters()).thenReturn(params);
    }

    @Test
    void executeWithNoImportFile_returnsFinished() throws Exception {
        setupJobParameters("");

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        verify(accountRepository, never()).save(any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void executeImportsAccountRecords() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "A|100001|Y|5000.00|10000.00|3000.00\n");
        setupJobParameters(tempImportFile.toString());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertEquals(100001L, saved.getAcctId());
        assertEquals("Y", saved.getActiveStatus());
        assertEquals(new BigDecimal("5000.00"), saved.getCurrentBalance());
        assertEquals(new BigDecimal("10000.00"), saved.getCreditLimit());
        assertEquals(new BigDecimal("3000.00"), saved.getCashCreditLimit());
    }

    @Test
    void executeImportsCustomerRecords() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "C|1001|John|Smith|NY|10001|5551234567\n");
        setupJobParameters(tempImportFile.toString());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(captor.capture());
        Customer saved = captor.getValue();
        assertEquals(1001L, saved.getCustId());
        assertEquals("John", saved.getFirstName());
        assertEquals("Smith", saved.getLastName());
    }

    @Test
    void executeImportsCreditCardRecords() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "D|4111111111111111|100001|123|JOHN SMITH|2027-12-31|Y\n");
        setupJobParameters(tempImportFile.toString());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<CreditCard> captor = ArgumentCaptor.forClass(CreditCard.class);
        verify(creditCardRepository).save(captor.capture());
        CreditCard saved = captor.getValue();
        assertEquals("4111111111111111", saved.getCardNum());
        assertEquals(100001L, saved.getAcctId());
        assertEquals(123, saved.getCvvCode());
        assertEquals("JOHN SMITH", saved.getEmbossedName());
        assertEquals("2027-12-31", saved.getExpirationDate());
        assertEquals("Y", saved.getActiveStatus());
    }

    @Test
    void executeImportsCardXrefRecords() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "X|4111111111111111|1001|100001\n");
        setupJobParameters(tempImportFile.toString());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<CardXref> captor = ArgumentCaptor.forClass(CardXref.class);
        verify(cardXrefRepository).save(captor.capture());
        CardXref saved = captor.getValue();
        assertEquals("4111111111111111", saved.getCardNum());
        assertEquals(1001L, saved.getCustId());
        assertEquals(100001L, saved.getAcctId());
    }

    @Test
    void executeImportsTransactionRecords() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "T|4111111111111111|TXN001|01|5000|100.00|Test purchase\n");
        setupJobParameters(tempImportFile.toString());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());
        Transaction saved = captor.getValue();
        assertEquals("4111111111111111", saved.getCardNum());
        assertEquals("TXN001", saved.getTranId());
        assertEquals("01", saved.getTypeCd());
        assertEquals(5000, saved.getCatCd());
        assertEquals(new BigDecimal("100.00"), saved.getAmount());
        assertEquals("Test purchase", saved.getDescription());
    }

    @Test
    void executeImportsMixedRecordTypes() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        String data = "A|100001|Y|5000.00|10000.00|3000.00\n" +
                "C|1001|John|Smith|NY|10001|5551234567\n" +
                "D|4111111111111111|100001|123|JOHN SMITH|2027-12-31|Y\n" +
                "X|4111111111111111|1001|100001\n" +
                "T|4111111111111111|TXN001|01|5000|100.00|Test\n";
        Files.writeString(tempImportFile, data);
        setupJobParameters(tempImportFile.toString());

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, times(1)).save(any(Account.class));
        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(creditCardRepository, times(1)).save(any(CreditCard.class));
        verify(cardXrefRepository, times(1)).save(any(CardXref.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void executeSkipsLinesWithLessThanTwoParts() throws Exception {
        tempImportFile = Files.createTempFile("import_", ".dat");
        Files.writeString(tempImportFile, "X\nA|100001|Y|5000.00|10000.00|3000.00\n");
        setupJobParameters(tempImportFile.toString());

        tasklet.execute(stepContribution, chunkContext);

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void executeThrowsExceptionForMissingFile() {
        setupJobParameters("/nonexistent/path/import.dat");

        assertThrows(RuntimeException.class, () -> tasklet.execute(stepContribution, chunkContext));
    }
}
