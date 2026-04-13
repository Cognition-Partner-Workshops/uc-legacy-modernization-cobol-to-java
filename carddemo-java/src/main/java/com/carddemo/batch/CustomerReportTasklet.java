package com.carddemo.batch;

import com.carddemo.entity.Customer;
import com.carddemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Customer Report - from CBCUS01C.cbl
 * Read customers, generate customer listing report
 */
@Component
public class CustomerReportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(CustomerReportTasklet.class);

    private final CustomerRepository customerRepository;

    public CustomerReportTasklet(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<Customer> customers = customerRepository.findAll();

        Path reportDir = Paths.get("reports");
        try {
            Files.createDirectories(reportDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path reportFile = reportDir.resolve("customer_report_" + timestamp + ".txt");

            try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile.toFile()))) {
                writer.println("=".repeat(80));
                writer.println("CARDEMO CUSTOMER LISTING REPORT");
                writer.printf("Generated: %s%n", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                writer.println("=".repeat(80));
                writer.println();

                writer.printf("%-10s %-25s %-25s %-25s %-2s %-10s%n",
                        "CUST ID", "FIRST NAME", "MIDDLE NAME", "LAST NAME", "ST", "ZIP");
                writer.println("-".repeat(80));

                for (Customer cust : customers) {
                    writer.printf("%-10d %-25s %-25s %-25s %-2s %-10s%n",
                            cust.getCustId(), cust.getFirstName(), cust.getMiddleName(),
                            cust.getLastName(), cust.getStateCode(), cust.getZipCode());
                }

                writer.println("-".repeat(80));
                writer.printf("TOTAL CUSTOMERS: %d%n", customers.size());
            }

            log.info("Customer report generated: {} ({} customers)", reportFile, customers.size());
        } catch (IOException e) {
            log.error("Failed to generate customer report", e);
            throw new RuntimeException("Failed to generate customer report", e);
        }

        return RepeatStatus.FINISHED;
    }
}
