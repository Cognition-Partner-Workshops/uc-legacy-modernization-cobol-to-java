package com.carddemo.exception;

public class AccountExpiredException extends RuntimeException {
    public AccountExpiredException(String acctId) {
        super("Account has expired: " + acctId);
    }
}
