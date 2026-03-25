package com.cardemo.batch;

import com.cardemo.model.Account;
import com.cardemo.model.Card;
import com.cardemo.model.CardXref;
import com.cardemo.model.Customer;
import com.cardemo.model.Transaction;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import com.cardemo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Importer - converted from COBOL program CBIMPORT.cbl (CBIMPORT)
 * 
 * Original: Batch Import program
 * Imports data from a sequential export file into VSAM files
 * (ACCTFILE, CUSTFILE, CARDFILE, XREFFILE, TRANFILE).
 * Uses the CVEXPORT.cpy record layout for parsing.
 * 
 * Import record types:
 * - 'A' = Account record -> ACCTFILE
 * - 'C' = Customer record -> CUSTFILE
 * - 'R' = Card record -> CARDFILE
 * - 'X' = Cross-reference record -> XREFFILE
 * - 'T' = Transaction record -> TRANFILE
 */
@Component
public class DataImporter {

    private static final Logger log = LoggerFactory.getLogger(DataImporter.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public DataImporter(AccountRepository accountRepository,
                        CustomerRepository customerRepository,
                        CardRepository cardRepository,
                        CardXrefRepository cardXrefRepository,
                        TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Import records from export data.
     * Equivalent to CBIMPORT main logic.
     */
    @Transactional
    public ImportResult importRecords(List<DataExporter.ExportRecord> records) {
        ImportResult result = new ImportResult();

        for (DataExporter.ExportRecord record : records) {
            try {
                switch (record.recordType) {
                    case "A":
                        importAccount(record.data);
                        result.accountCount++;
                        break;
                    case "C":
                        importCustomer(record.data);
                        result.customerCount++;
                        break;
                    case "R":
                        importCard(record.data);
                        result.cardCount++;
                        break;
                    case "X":
                        importXref(record.data);
                        result.xrefCount++;
                        break;
                    case "T":
                        importTransaction(record.data);
                        result.transactionCount++;
                        break;
                    default:
                        log.warn("Unknown record type: {}", record.recordType);
                        result.errors++;
                }
            } catch (Exception e) {
                log.error("Error importing record seq {}: {}", record.sequenceNum, e.getMessage());
                result.errors++;
            }
        }

        log.info("Import complete. Accounts: {}, Customers: {}, Cards: {}, Xrefs: {}, Transactions: {}, Errors: {}",
                result.accountCount, result.customerCount, result.cardCount,
                result.xrefCount, result.transactionCount, result.errors);

        return result;
    }

    private void importAccount(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length >= 5) {
            Account acct = new Account();
            acct.setAcctId(Long.parseLong(parts[1]));
            acct.setActiveStatus(parts[2]);
            acct.setCurrentBalance(new BigDecimal(parts[3]));
            acct.setCreditLimit(new BigDecimal(parts[4]));
            accountRepository.save(acct);
        }
    }

    private void importCustomer(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length >= 7) {
            Customer cust = new Customer();
            cust.setCustId(Long.parseLong(parts[1]));
            cust.setFirstName(parts[2]);
            cust.setMiddleName(parts[3]);
            cust.setLastName(parts[4]);
            cust.setAddrStateCd(parts[5]);
            cust.setFicoCreditScore(Integer.parseInt(parts[6]));
            customerRepository.save(cust);
        }
    }

    private void importCard(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length >= 5) {
            Card card = new Card();
            card.setCardNum(parts[1]);
            card.setCardAcctId(Long.parseLong(parts[2]));
            card.setEmbossedName(parts[3]);
            card.setActiveStatus(parts[4]);
            cardRepository.save(card);
        }
    }

    private void importXref(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length >= 4) {
            CardXref xref = new CardXref();
            xref.setCardNum(parts[1]);
            xref.setCustId(Long.parseLong(parts[2]));
            xref.setAcctId(Long.parseLong(parts[3]));
            cardXrefRepository.save(xref);
        }
    }

    private void importTransaction(String data) {
        String[] parts = data.split("\\|", -1);
        if (parts.length >= 7) {
            Transaction tran = new Transaction();
            tran.setTranId(parts[1]);
            tran.setTypeCd(parts[2]);
            tran.setCatCd(Integer.parseInt(parts[3]));
            tran.setCardNum(parts[4]);
            tran.setAmount(new BigDecimal(parts[5]));
            tran.setDescription(parts[6]);
            transactionRepository.save(tran);
        }
    }

    public static class ImportResult {
        public int accountCount = 0;
        public int customerCount = 0;
        public int cardCount = 0;
        public int xrefCount = 0;
        public int transactionCount = 0;
        public int errors = 0;
    }
}
