package com.carddemo.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String acctId) {
        super("Account not found: " + acctId);
    }
}
