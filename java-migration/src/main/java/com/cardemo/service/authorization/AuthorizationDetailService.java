package com.cardemo.service.authorization;

import com.cardemo.model.AccountRecord;
import com.cardemo.model.CardXrefRecord;
import com.cardemo.repository.AccountRepository;
import com.cardemo.repository.CardXrefRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authorization Detail Service - migrated from COBOL program COPAUS1C.cbl.
 * Detail View of individual Authorization Messages.
 * Original: CICS COBOL IMS BMS Program that displays pending authorization
 *           detail for a selected authorization key.
 */
@Service
public class AuthorizationDetailService {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationDetailService.class);

    private final AccountRepository accountRepository;
    private final CardXrefRepository cardXrefRepository;

    public AuthorizationDetailService(AccountRepository accountRepository,
                                      CardXrefRepository cardXrefRepository) {
        this.accountRepository = accountRepository;
        this.cardXrefRepository = cardXrefRepository;
    }

    /**
     * Get authorization detail for a specific account.
     * Migrated from GATHER-AUTH-DETAILS paragraph.
     *
     * @param acctId the account ID
     * @return account details for the authorization view
     */
    public AccountRecord getAuthorizationDetail(long acctId) {
        Optional<AccountRecord> acctOpt = accountRepository.findById(acctId);
        if (acctOpt.isEmpty()) {
            log.warn("Account {} not found for auth detail view", acctId);
            return null;
        }
        log.info("Retrieved auth detail for account {}", acctId);
        return acctOpt.get();
    }

    /**
     * Look up account via card cross-reference.
     * Migrated from READ-XREF-VSAM paragraph.
     *
     * @param cardNum card number to look up
     * @return the cross-reference record or null
     */
    public CardXrefRecord lookupCardXref(String cardNum) {
        Optional<CardXrefRecord> xrefOpt = cardXrefRepository.findById(cardNum);
        if (xrefOpt.isEmpty()) {
            log.warn("Card {} not found in cross-reference", cardNum);
            return null;
        }
        return xrefOpt.get();
    }
}
