package com.cardemo.batch;

import com.cardemo.model.Account;
import com.cardemo.model.Card;
import com.cardemo.model.CardXref;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Account Data Processor - converted from COBOL programs CBACT01C, CBACT02C, CBACT03C
 * 
 * CBACT01C: Account File Read (reads all records from ACCTFILE sequentially)
 * CBACT02C: Card Data Read (reads all records from CARDFILE sequentially)
 * CBACT03C: Card Cross-Reference Read (reads all records from XREFFILE sequentially)
 * 
 * These batch programs read VSAM files sequentially and display/report on all records.
 * Converted to service methods that read from database tables.
 */
@Component
public class AccountDataProcessor {

    private static final Logger log = LoggerFactory.getLogger(AccountDataProcessor.class);

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;

    public AccountDataProcessor(AccountRepository accountRepository,
                                CardRepository cardRepository,
                                CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Process all account records - equivalent to CBACT01C.
     * Original: READ ACCTFILE-FILE NEXT INTO ACCOUNT-RECORD loop
     * Returns count of records processed.
     */
    public int processAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        int count = 0;
        for (Account account : accounts) {
            log.info("Account ID: {}, Status: {}, Balance: {}",
                    account.getAcctId(),
                    account.getActiveStatus(),
                    account.getCurrentBalance());
            count++;
        }
        log.info("Total account records processed: {}", count);
        return count;
    }

    /**
     * Process all card records - equivalent to CBACT02C.
     * Original: READ CARDFILE-FILE NEXT INTO CARD-RECORD loop
     * Returns count of records processed.
     */
    public int processAllCards() {
        List<Card> cards = cardRepository.findAll();
        int count = 0;
        for (Card card : cards) {
            log.info("Card Num: {}, Account: {}, Status: {}",
                    card.getCardNum(),
                    card.getCardAcctId(),
                    card.getActiveStatus());
            count++;
        }
        log.info("Total card records processed: {}", count);
        return count;
    }

    /**
     * Process all card cross-reference records - equivalent to CBACT03C.
     * Original: READ XREFFILE-FILE NEXT INTO CARD-XREF-RECORD loop
     * Returns count of records processed.
     */
    public int processAllCardXrefs() {
        List<CardXref> xrefs = cardXrefRepository.findAll();
        int count = 0;
        for (CardXref xref : xrefs) {
            log.info("Card: {}, Customer: {}, Account: {}",
                    xref.getCardNum(),
                    xref.getCustId(),
                    xref.getAcctId());
            count++;
        }
        log.info("Total cross-reference records processed: {}", count);
        return count;
    }
}
