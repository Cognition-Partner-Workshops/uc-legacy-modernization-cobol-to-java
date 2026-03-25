/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.authorization;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.model.CustomerRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authorization Summary Service - migrated from COBOL program COPAUS0C.cbl.
 * Summary View of Authorization Messages with pagination.
 * Original: CICS COBOL IMS BMS Program that displays pending authorization
 *           summaries by account, with PF7/PF8 paging and drill-down to details.
 */
@Service
public class AuthorizationSummaryService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationSummaryService.class);
    private static final int PAGE_SIZE = 5;

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;
    private final CustomerRepository customerRepository;

    public AuthorizationSummaryService(AccountRepository accountRepository,
                                       CardXrefRepository cardXrefRepository,
                                       CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Get account details for authorization summary view.
     * Migrated from GATHER-ACCOUNT-DETAILS paragraph.
     *
     * @param acctId the account ID
     * @return account details or null if not found
     */
    public AccountRecord getAccountDetails(long acctId) {
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            log.warn("Account {} not found", acctId);
            return null;
        }
        return acctOpt.get();
    }

    /**
     * Get customer details for authorization summary.
     * Migrated from READ-CUST-VSAM paragraph.
     *
     * @param custId the customer ID
     * @return customer record or null if not found
     */
    public CustomerRecord getCustomerDetails(long custId) {
        Optional<CustomerRecord> custOpt = customerRepository.findById(custId);
        if (custOpt.isEmpty()) {
            log.warn("Customer {} not found", custId);
            return null;
        }
        return custOpt.get();
    }
}
