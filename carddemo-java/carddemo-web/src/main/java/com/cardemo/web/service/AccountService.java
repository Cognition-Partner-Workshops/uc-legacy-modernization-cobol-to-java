package com.cardemo.web.service;

import com.cardemo.common.repository.AccountRepository;
import org.springframework.stereotype.Service;

/**
 * Account service replacing business logic from COACTVWC.cbl and COACTUPC.cbl.
 *
 * TODO: Implement account view (read VSAM) from COACTVWC.cbl
 * TODO: Implement account update (rewrite VSAM) from COACTUPC.cbl
 * TODO: Validate account status transitions
 */
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // TODO: Implement account CRUD from COACTVWC.cbl and COACTUPC.cbl
}
