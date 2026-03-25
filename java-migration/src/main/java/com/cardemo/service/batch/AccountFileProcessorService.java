package com.cardemo.service.batch;

import com.cardemo.model.AccountRecord;
import com.cardemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Account File Processor - migrated from COBOL batch program CBACT01C.cbl.
 * Reads account file and writes data into output files in various formats.
 * Original: Reads VSAM ACCTFILE (indexed), writes to OUTFILE, ARRYFILE, VBRCFILE.
 */
@Service
public class AccountFileProcessorService {

    private static final Logger log = LoggerFactory.getLogger(AccountFileProcessorService.class);

    private final AccountRepository accountRepository;

    public AccountFileProcessorService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Process all accounts - migrated from main PROCEDURE DIVISION logic.
     * Reads each account sequentially and displays/processes the record.
     *
     * @return number of accounts processed
     */
    public int processAccountFile() {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C (AccountFileProcessor)");

        List<AccountRecord> accounts = accountRepository.findAll();
        int count = 0;

        for (AccountRecord account : accounts) {
            displayAccountRecord(account);
            count++;
        }

        log.info("END OF EXECUTION OF PROGRAM CBACT01C. Records processed: {}", count);
        return count;
    }

    /**
     * Display account record - migrated from 1100-DISPLAY-ACCT-RECORD paragraph.
     */
    private void displayAccountRecord(AccountRecord account) {
        log.info("ACCT-ID                 : {}", account.getAcctId());
        log.info("ACCT-ACTIVE-STATUS      : {}", account.getAcctActiveStatus());
        log.info("ACCT-CURR-BAL           : {}", account.getAcctCurrBal());
        log.info("ACCT-CREDIT-LIMIT       : {}", account.getAcctCreditLimit());
        log.info("ACCT-CASH-CREDIT-LIMIT  : {}", account.getAcctCashCreditLimit());
        log.info("ACCT-OPEN-DATE          : {}", account.getAcctOpenDate());
        log.info("ACCT-EXPIRAION-DATE     : {}", account.getAcctExpirationDate());
        log.info("ACCT-REISSUE-DATE       : {}", account.getAcctReissueDate());
        log.info("ACCT-CURR-CYC-CREDIT    : {}", account.getAcctCurrCycCredit());
        log.info("ACCT-CURR-CYC-DEBIT     : {}", account.getAcctCurrCycDebit());
        log.info("ACCT-GROUP-ID           : {}", account.getAcctGroupId());
        log.info("-------------------------------------------------");
    }
}
