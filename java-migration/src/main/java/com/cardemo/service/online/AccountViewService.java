package com.cardemo.service.online;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Account View Service - migrated from COBOL program COACTVWC.cbl.
 * Handles viewing account details, associated cards, and customer info.
 * Original: CICS program with TRANID CAVW, reads ACCTDAT/CARDDAT/CUSTDAT/CARDXREF.
 */
@Service
public class AccountViewService {

    private static final Logger log = LoggerFactory.getLogger(AccountViewService.class);

    private final AccountRepository accountRepository;
    private final CardRepository cardRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public AccountViewService(AccountRepository accountRepository,
                              CardRepository cardRepository,
                              CardXrefRepository cardXrefRepository,
                              CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardRepository = cardRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * View account details by account ID.
     * Migrated from 9000-READ-ACCT and 2000-PROCESS-INPUTS paragraphs.
     *
     * @param acctIdStr the account ID string
     * @return map of account details including account, cards, and customer info
     */
    public Map<String, Object> viewAccount(String acctIdStr) {
        if (acctIdStr == null || acctIdStr.isBlank()) {
            throw new AccountViewException("Account number not provided");
        }

        String trimmed = acctIdStr.trim();
        long acctId;
        try {
            acctId = Long.parseLong(trimmed);
        } catch (NumberFormatException e) {
            throw new AccountViewException("Account number must be a non zero 11 digit number");
        }

        if (acctId == 0) {
            throw new AccountViewException("Account number must be a non zero 11 digit number");
        }

        // Read card xref by account - equivalent to CICS READ on CXACAIX
        List<CardXrefRecord> xrefs = cardXrefRepository.findByXrefAcctId(acctId);
        if (xrefs.isEmpty()) {
            throw new AccountViewException("Did not find this account in account card xref file");
        }

        // Read account master - equivalent to CICS READ on ACCTDAT
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            throw new AccountViewException("Did not find this account in account master file");
        }

        // Read customer master - equivalent to CICS READ on CUSTDAT
        CardXrefRecord xref = xrefs.get(0);
        Optional<CustomerRecord> custOpt = customerRepository.findById(xref.getXrefCustId());
        if (custOpt.isEmpty()) {
            throw new AccountViewException("Did not find associated customer in master file");
        }

        // Read cards for this account
        List<CardRecord> cards = cardRepository.findByCardAcctId(acctId);

        Map<String, Object> result = new HashMap<>();
        result.put("account", acctOpt.get());
        result.put("customer", custOpt.get());
        result.put("cards", cards);
        result.put("xref", xref);

        log.info("Account {} viewed successfully", acctId);
        return result;
    }

    public static class AccountViewException extends RuntimeException {
        public AccountViewException(String message) {
            super(message);
        }
    }
}
