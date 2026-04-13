package com.carddemo.batch;

import com.carddemo.entity.*;
import com.carddemo.repository.*;
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

/**
 * Data Export - from CBEXPORT.cbl + CBEXPORT.jcl
 * Read all tables, write to export file using CVEXPORT.cpy format
 */
@Component
public class DataExportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DataExportTasklet.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CreditCardRepository creditCardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public DataExportTasklet(AccountRepository accountRepository,
                             CustomerRepository customerRepository,
                             CreditCardRepository creditCardRepository,
                             CardXrefRepository cardXrefRepository,
                             TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.creditCardRepository = creditCardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        Path exportDir = Paths.get("exports");
        try {
            Files.createDirectories(exportDir);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path exportFile = exportDir.resolve("carddemo_export_" + timestamp + ".dat");

            try (PrintWriter writer = new PrintWriter(new FileWriter(exportFile.toFile()))) {
                // Export accounts (record type 'A')
                for (Account acct : accountRepository.findAll()) {
                    writer.printf("A|%d|%s|%.2f|%.2f|%.2f|%s|%s|%s|%.2f|%.2f|%s|%s%n",
                            acct.getAcctId(), acct.getActiveStatus(),
                            acct.getCurrentBalance(), acct.getCreditLimit(), acct.getCashCreditLimit(),
                            acct.getOpenDate(), acct.getExpirationDate(), acct.getReissueDate(),
                            acct.getCurrentCycleCredit(), acct.getCurrentCycleDebit(),
                            acct.getAddressZip(), acct.getGroupId());
                }

                // Export customers (record type 'C')
                for (Customer cust : customerRepository.findAll()) {
                    writer.printf("C|%d|%s|%s|%s|%s|%s%n",
                            cust.getCustId(), cust.getFirstName(), cust.getLastName(),
                            cust.getStateCode(), cust.getZipCode(), cust.getPhone1());
                }

                // Export cards (record type 'D')
                for (CreditCard card : creditCardRepository.findAll()) {
                    writer.printf("D|%s|%d|%d|%s|%s|%s%n",
                            card.getCardNum(), card.getAcctId(), card.getCvvCode(),
                            card.getEmbossedName(), card.getExpirationDate(), card.getActiveStatus());
                }

                // Export card xrefs (record type 'X')
                for (CardXref xref : cardXrefRepository.findAll()) {
                    writer.printf("X|%s|%d|%d%n", xref.getCardNum(), xref.getCustId(), xref.getAcctId());
                }

                // Export transactions (record type 'T')
                for (Transaction txn : transactionRepository.findAll()) {
                    writer.printf("T|%s|%s|%s|%d|%.2f|%s%n",
                            txn.getCardNum(), txn.getTranId(), txn.getTypeCd(),
                            txn.getCatCd(), txn.getAmount(), txn.getDescription());
                }
            }

            log.info("Data export complete: {}", exportFile);
        } catch (IOException e) {
            log.error("Failed to export data", e);
            throw new RuntimeException("Failed to export data", e);
        }

        return RepeatStatus.FINISHED;
    }
}
