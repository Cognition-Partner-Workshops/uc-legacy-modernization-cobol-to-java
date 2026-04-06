package com.suitecrm.account.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String id) {
        super("Account not found with id: " + id);
    }
}
