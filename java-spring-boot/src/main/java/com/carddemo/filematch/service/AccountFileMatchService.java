package com.carddemo.filematch.service;

import com.carddemo.filematch.model.Account;
import com.carddemo.filematch.model.ProcessingSummary;
import com.carddemo.filematch.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Account file-match processing service.
 *
 * Java equivalent of COBOL program CBACT01C:
 *   - Opens the account file (CSV instead of VSAM KSDS)
 *   - Reads each record
 *   - Matches records by active status
 *   - Writes matched records to the database (instead of sequential output file)
 *   - Displays a processing summary
 */
@Service
public class AccountFileMatchService {

    private static final Logger log = LoggerFactory.getLogger(AccountFileMatchService.class);

    private final CsvService csvService;
    private final AccountRepository accountRepository;

    @Value("${app.match-criteria.account-active-status:Y}")
    private String matchActiveStatus;

    public AccountFileMatchService(CsvService csvService, AccountRepository accountRepository) {
        this.csvService = csvService;
        this.accountRepository = accountRepository;
    }

    /**
     * Process accounts from a CSV input stream.
     * Reads all records, matches by active status, persists matched records to DB.
     *
     * Equivalent to COBOL CBACT01C PROCEDURE DIVISION:
     *   PERFORM 0000-ACCTFILE-OPEN
     *   PERFORM UNTIL END-OF-FILE = 'Y'
     *       PERFORM 1000-ACCTFILE-GET-NEXT
     *   END-PERFORM
     *   PERFORM 9000-ACCTFILE-CLOSE
     */
    public ProcessingSummary processAccountFile(InputStream inputStream) throws IOException {
        log.info("START OF EXECUTION OF PROGRAM CBACT01C (Java)");
        log.info("Match criteria: ACCT-ACTIVE-STATUS = '{}'", matchActiveStatus);

        List<Account> allAccounts = csvService.readAccountsCsv(inputStream);
        List<Account> matchedAccounts = new ArrayList<>();
        int rejectCount = 0;

        for (Account acct : allAccounts) {
            // Equivalent to: IF ACCT-ACTIVE-STATUS = WS-MATCH-CRITERIA
            if (matchActiveStatus.equals(acct.getAcctActiveStatus())) {
                matchedAccounts.add(acct);
                log.debug("MATCHED: {}", acct);
            } else {
                rejectCount++;
                log.debug("REJECTED: {} (status='{}')", acct.getAcctId(),
                        acct.getAcctActiveStatus());
            }
        }

        // Persist matched records to database (replaces WRITE OUT-ACCT-REC)
        accountRepository.saveAll(matchedAccounts);
        log.info("Persisted {} matched accounts to database", matchedAccounts.size());

        log.info("END OF EXECUTION OF PROGRAM CBACT01C (Java)");

        return new ProcessingSummary(
                "CBACT01C",
                allAccounts.size(),
                matchedAccounts.size(),
                rejectCount,
                "ACCT-ACTIVE-STATUS = '" + matchActiveStatus + "'",
                "COMPLETED"
        );
    }

    /**
     * Process accounts from a classpath resource file.
     */
    public ProcessingSummary processAccountFile(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            return processAccountFile(is);
        }
    }

    /**
     * Query matched accounts from the database by active status.
     */
    public List<Account> getMatchedAccounts() {
        return accountRepository.findByAcctActiveStatus(matchActiveStatus);
    }

    /**
     * Query all persisted accounts.
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
