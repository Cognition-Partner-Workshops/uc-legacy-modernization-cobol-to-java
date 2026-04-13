package com.carddemo.batch;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
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
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataExportTaskletTest {

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

    private DataExportTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new DataExportTasklet(accountRepository, customerRepository,
                creditCardRepository, cardXrefRepository, transactionRepository);
    }

    @AfterEach
    void cleanUp() throws Exception {
        Path exportDir = Path.of("exports");
        if (Files.exists(exportDir)) {
            Files.walk(exportDir)
                    .sorted(java.util.Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    void executeExportsAccountRecords() throws Exception {
        Account account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("3000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2027, 12, 31));
        account.setReissueDate(LocalDate.of(2025, 1, 1));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("300.00"));
        account.setAddressZip("10001");
        account.setGroupId("A000000000");

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());
        when(creditCardRepository.findAll()).thenReturn(Collections.emptyList());
        when(cardXrefRepository.findAll()).thenReturn(Collections.emptyList());
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File exportDir = new File("exports");
        assertTrue(exportDir.exists());
        File[] exportFiles = exportDir.listFiles((dir, name) -> name.startsWith("carddemo_export_"));
        assertNotNull(exportFiles);
        assertTrue(exportFiles.length > 0);

        String content = Files.readString(exportFiles[0].toPath());
        assertTrue(content.startsWith("A|"));
        assertTrue(content.contains("100001"));
        assertTrue(content.contains("5000.00"));
    }

    @Test
    void executeExportsAllRecordTypes() throws Exception {
        Account account = new Account();
        account.setAcctId(100001L);
        account.setActiveStatus("Y");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCreditLimit(new BigDecimal("10000.00"));
        account.setCashCreditLimit(new BigDecimal("3000.00"));
        account.setOpenDate(LocalDate.of(2020, 1, 1));
        account.setExpirationDate(LocalDate.of(2027, 12, 31));
        account.setReissueDate(LocalDate.of(2025, 1, 1));
        account.setCurrentCycleCredit(new BigDecimal("200.00"));
        account.setCurrentCycleDebit(new BigDecimal("300.00"));
        account.setAddressZip("10001");
        account.setGroupId("A000000000");

        Customer customer = new Customer();
        customer.setCustId(1001L);
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setStateCode("NY");
        customer.setZipCode("10001");
        customer.setPhone1("5551234567");

        CreditCard card = new CreditCard();
        card.setCardNum("4111111111111111");
        card.setAcctId(100001L);
        card.setCvvCode(123);
        card.setEmbossedName("JOHN SMITH");
        card.setExpirationDate("2027-12-31");
        card.setActiveStatus("Y");

        CardXref xref = new CardXref();
        xref.setCardNum("4111111111111111");
        xref.setCustId(1001L);
        xref.setAcctId(100001L);

        Transaction txn = new Transaction();
        txn.setCardNum("4111111111111111");
        txn.setTranId("TXN001");
        txn.setTypeCd("01");
        txn.setCatCd(5000);
        txn.setAmount(new BigDecimal("100.00"));
        txn.setDescription("Test purchase");

        when(accountRepository.findAll()).thenReturn(List.of(account));
        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(creditCardRepository.findAll()).thenReturn(List.of(card));
        when(cardXrefRepository.findAll()).thenReturn(List.of(xref));
        when(transactionRepository.findAll()).thenReturn(List.of(txn));

        tasklet.execute(stepContribution, chunkContext);

        File exportDir = new File("exports");
        File[] exportFiles = exportDir.listFiles((dir, name) -> name.startsWith("carddemo_export_"));
        String content = Files.readString(exportFiles[0].toPath());

        assertTrue(content.contains("A|100001|"));
        assertTrue(content.contains("C|1001|"));
        assertTrue(content.contains("D|4111111111111111|"));
        assertTrue(content.contains("X|4111111111111111|"));
        assertTrue(content.contains("T|4111111111111111|"));
    }

    @Test
    void executeWithEmptyTables_createsEmptyExportFile() throws Exception {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());
        when(creditCardRepository.findAll()).thenReturn(Collections.emptyList());
        when(cardXrefRepository.findAll()).thenReturn(Collections.emptyList());
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File exportDir = new File("exports");
        assertTrue(exportDir.exists());
        File[] exportFiles = exportDir.listFiles((dir, name) -> name.startsWith("carddemo_export_"));
        assertNotNull(exportFiles);
        String content = Files.readString(exportFiles[0].toPath());
        assertTrue(content.isEmpty());
    }
}
