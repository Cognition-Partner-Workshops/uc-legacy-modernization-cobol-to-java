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

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Data Import - from CBIMPORT.cbl + CBIMPORT.jcl
 * Read export file, parse by record type, insert into appropriate tables
 */
@Component
public class DataImportTasklet implements Tasklet {

    private static final Logger log = LoggerFactory.getLogger(DataImportTasklet.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CreditCardRepository creditCardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public DataImportTasklet(AccountRepository accountRepository,
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
        String importFile = chunkContext.getStepContext().getJobParameters()
                .getOrDefault("importFile", "").toString();

        if (importFile.isEmpty()) {
            log.warn("No import file specified");
            return RepeatStatus.FINISHED;
        }

        Path filePath = Paths.get(importFile);
        int importedCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;

                String recordType = parts[0];
                switch (recordType) {
                    case "A" -> importAccount(parts);
                    case "C" -> importCustomer(parts);
                    case "D" -> importCreditCard(parts);
                    case "X" -> importCardXref(parts);
                    case "T" -> importTransaction(parts);
                    default -> log.warn("Unknown record type: {}", recordType);
                }
                importedCount++;
            }
        } catch (Exception e) {
            log.error("Failed to import data from: {}", importFile, e);
            throw new RuntimeException("Failed to import data", e);
        }

        log.info("Data import complete: {} records imported from {}", importedCount, importFile);
        return RepeatStatus.FINISHED;
    }

    private void importAccount(String[] parts) {
        Account acct = new Account();
        acct.setAcctId(Long.parseLong(parts[1]));
        acct.setActiveStatus(parts[2]);
        acct.setCurrentBalance(new BigDecimal(parts[3]));
        acct.setCreditLimit(new BigDecimal(parts[4]));
        acct.setCashCreditLimit(new BigDecimal(parts[5]));
        accountRepository.save(acct);
    }

    private void importCustomer(String[] parts) {
        Customer cust = new Customer();
        cust.setCustId(Long.parseLong(parts[1]));
        cust.setFirstName(parts[2]);
        cust.setLastName(parts[3]);
        customerRepository.save(cust);
    }

    private void importCreditCard(String[] parts) {
        CreditCard card = new CreditCard();
        card.setCardNum(parts[1]);
        card.setAcctId(Long.parseLong(parts[2]));
        card.setCvvCode(Integer.parseInt(parts[3]));
        card.setEmbossedName(parts[4]);
        card.setExpirationDate(parts[5]);
        card.setActiveStatus(parts[6]);
        creditCardRepository.save(card);
    }

    private void importCardXref(String[] parts) {
        CardXref xref = new CardXref();
        xref.setCardNum(parts[1]);
        xref.setCustId(Long.parseLong(parts[2]));
        xref.setAcctId(Long.parseLong(parts[3]));
        cardXrefRepository.save(xref);
    }

    private void importTransaction(String[] parts) {
        Transaction txn = new Transaction();
        txn.setCardNum(parts[1]);
        txn.setTranId(parts[2]);
        txn.setTypeCd(parts[3]);
        txn.setCatCd(Integer.parseInt(parts[4]));
        txn.setAmount(new BigDecimal(parts[5]));
        txn.setDescription(parts.length > 6 ? parts[6] : "");
        transactionRepository.save(txn);
    }
}
