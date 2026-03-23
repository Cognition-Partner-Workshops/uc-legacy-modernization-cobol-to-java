package com.carddemo.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String cardNum) {
        super("Card not found: " + cardNum);
    }
}
