package com.carddemo.exception;

public class CustomerNotFoundException extends CardDemoException {
    public CustomerNotFoundException(Long custId) {
        super("Customer not found: " + custId);
    }
}
