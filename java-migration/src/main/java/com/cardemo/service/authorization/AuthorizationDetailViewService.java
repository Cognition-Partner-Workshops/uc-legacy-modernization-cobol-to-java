/**
 * @author Pradeep Sahu @ Cognizant
 */
package com.cardemo.service.authorization;

import com.cardemo.model.AccountRecord;
import com.cardemo.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authorization Detail View Service - migrated from COBOL program COPAUS2C.cbl.
 * Provides detailed view of individual authorization records with
 * approval/decline information.
 * Original: CICS COBOL IMS BMS Program for viewing individual
 *           pending authorization detail records.
 */
@Service
public class AuthorizationDetailViewService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationDetailViewService.class);

    private final AccountRepository accountRepository;

    public AuthorizationDetailViewService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * View detailed authorization information for an account.
     *
     * @param acctId the account ID
     * @return the account record or null if not found
     */
    public AccountRecord viewAuthorizationDetail(long acctId) {
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            log.warn("Account {} not found for authorization detail view", acctId);
            return null;
        }
        return acctOpt.get();
    }
}
