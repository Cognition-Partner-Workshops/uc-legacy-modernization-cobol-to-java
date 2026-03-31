package com.carddemo.exception;

public class CardNotFoundException extends CardDemoException {
    public CardNotFoundException(String cardNum) {
        super("Card not found: " + cardNum);
    }
}
