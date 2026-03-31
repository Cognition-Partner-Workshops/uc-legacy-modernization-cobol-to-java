package com.carddemo.exception;

public class AccountNotFoundException extends CardDemoException {
    public AccountNotFoundException(Long acctId) {
        super("Account not found: " + acctId);
    }
}
