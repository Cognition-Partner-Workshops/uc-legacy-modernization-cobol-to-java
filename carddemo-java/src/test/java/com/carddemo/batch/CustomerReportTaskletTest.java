package com.carddemo.batch;

import com.carddemo.entity.Customer;
import com.carddemo.repository.CustomerRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerReportTaskletTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    private CustomerReportTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new CustomerReportTasklet(customerRepository);
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

    @Test
    void executeGeneratesCustomerReport() throws Exception {
        Customer customer = new Customer();
        customer.setCustId(1001L);
        customer.setFirstName("John");
        customer.setMiddleName("Q");
        customer.setLastName("Smith");
        customer.setStateCode("NY");
        customer.setZipCode("10001");

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File reportDir = new File("reports");
        assertTrue(reportDir.exists());
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("customer_report_"));
        assertNotNull(reportFiles);
        assertTrue(reportFiles.length > 0);

        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("CARDEMO CUSTOMER LISTING REPORT"));
        assertTrue(content.contains("John"));
        assertTrue(content.contains("Smith"));
        assertTrue(content.contains("NY"));
        assertTrue(content.contains("10001"));
    }

    @Test
    void executeReportContainsTotalCount() throws Exception {
        Customer c1 = new Customer();
        c1.setCustId(1001L);
        c1.setFirstName("John");
        c1.setMiddleName("Q");
        c1.setLastName("Smith");
        c1.setStateCode("NY");
        c1.setZipCode("10001");

        Customer c2 = new Customer();
        c2.setCustId(1002L);
        c2.setFirstName("Jane");
        c2.setMiddleName("A");
        c2.setLastName("Doe");
        c2.setStateCode("CA");
        c2.setZipCode("90001");

        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        tasklet.execute(stepContribution, chunkContext);

        File reportDir = new File("reports");
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("customer_report_"));
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("TOTAL CUSTOMERS: 2"));
    }

    @Test
    void executeWithNoCustomers_generatesEmptyReport() throws Exception {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertEquals(RepeatStatus.FINISHED, status);
        File reportDir = new File("reports");
        assertTrue(reportDir.exists());
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("customer_report_"));
        assertNotNull(reportFiles);
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("TOTAL CUSTOMERS: 0"));
    }

    @Test
    void executeReportContainsHeaderColumns() throws Exception {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        tasklet.execute(stepContribution, chunkContext);

        File reportDir = new File("reports");
        File[] reportFiles = reportDir.listFiles((dir, name) -> name.startsWith("customer_report_"));
        String content = Files.readString(reportFiles[0].toPath());
        assertTrue(content.contains("CUST ID"));
        assertTrue(content.contains("FIRST NAME"));
        assertTrue(content.contains("LAST NAME"));
    }
}
