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
import com.cardemo.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Exporter - converted from COBOL program CBEXPORT.cbl (CBEXPORT)
 * 
 * Original: Batch Export program
 * Exports data from VSAM files (ACCTFILE, CUSTFILE, CARDFILE, XREFFILE, TRANFILE)
 * to a sequential export file using the CVEXPORT.cpy record layout.
 * 
 * Export record types:
 * - 'A' = Account record
 * - 'C' = Customer record
 * - 'R' = Card record
 * - 'X' = Cross-reference record
 * - 'T' = Transaction record
 */
@Component
public class DataExporter {

    private static final Logger log = LoggerFactory.getLogger(DataExporter.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;

    public DataExporter(AccountRepository accountRepository,
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
     * Export all data to a list of export records.
     * Equivalent to CBEXPORT main logic.
     */
    public ExportResult exportAll() {
        ExportResult result = new ExportResult();
        int seqNum = 0;
        String timestamp = DateUtil.getCurrentTimestamp();

        // Export accounts (Record type 'A')
        for (Account acct : accountRepository.findAll()) {
            ExportRecord rec = new ExportRecord();
            rec.recordType = "A";
            rec.timestamp = timestamp;
            rec.sequenceNum = ++seqNum;
            rec.data = String.format("ACCT|%d|%s|%s|%s",
                    acct.getAcctId(),
                    acct.getActiveStatus(),
                    acct.getCurrentBalance(),
                    acct.getCreditLimit());
            result.records.add(rec);
            result.accountCount++;
        }

        // Export customers (Record type 'C')
        for (Customer cust : customerRepository.findAll()) {
            ExportRecord rec = new ExportRecord();
            rec.recordType = "C";
            rec.timestamp = timestamp;
            rec.sequenceNum = ++seqNum;
            rec.data = String.format("CUST|%d|%s|%s|%s|%s|%s",
                    cust.getCustId(),
                    safe(cust.getFirstName()),
                    safe(cust.getMiddleName()),
                    safe(cust.getLastName()),
                    safe(cust.getAddrStateCd()),
                    cust.getFicoCreditScore());
            result.records.add(rec);
            result.customerCount++;
        }

        // Export cards (Record type 'R')
        for (Card card : cardRepository.findAll()) {
            ExportRecord rec = new ExportRecord();
            rec.recordType = "R";
            rec.timestamp = timestamp;
            rec.sequenceNum = ++seqNum;
            rec.data = String.format("CARD|%s|%d|%s|%s",
                    card.getCardNum(),
                    card.getCardAcctId(),
                    safe(card.getEmbossedName()),
                    card.getActiveStatus());
            result.records.add(rec);
            result.cardCount++;
        }

        // Export cross-references (Record type 'X')
        for (CardXref xref : cardXrefRepository.findAll()) {
            ExportRecord rec = new ExportRecord();
            rec.recordType = "X";
            rec.timestamp = timestamp;
            rec.sequenceNum = ++seqNum;
            rec.data = String.format("XREF|%s|%d|%d",
                    xref.getCardNum(),
                    xref.getCustId(),
                    xref.getAcctId());
            result.records.add(rec);
            result.xrefCount++;
        }

        // Export transactions (Record type 'T')
        for (Transaction tran : transactionRepository.findAll()) {
            ExportRecord rec = new ExportRecord();
            rec.recordType = "T";
            rec.timestamp = timestamp;
            rec.sequenceNum = ++seqNum;
            rec.data = String.format("TRAN|%s|%s|%d|%s|%s|%s",
                    tran.getTranId(),
                    tran.getTypeCd(),
                    tran.getCatCd(),
                    tran.getCardNum(),
                    tran.getAmount(),
                    safe(tran.getDescription()));
            result.records.add(rec);
            result.transactionCount++;
        }

        log.info("Export complete. Accounts: {}, Customers: {}, Cards: {}, Xrefs: {}, Transactions: {}",
                result.accountCount, result.customerCount, result.cardCount,
                result.xrefCount, result.transactionCount);

        return result;
    }

    private static String safe(String value) {
        return value != null ? value.trim() : "";
    }

    public static class ExportResult {
        public List<ExportRecord> records = new ArrayList<>();
        public int accountCount = 0;
        public int customerCount = 0;
        public int cardCount = 0;
        public int xrefCount = 0;
        public int transactionCount = 0;
    }

    public static class ExportRecord {
        public String recordType;
        public String timestamp;
        public int sequenceNum;
        public String data;
    }
}
