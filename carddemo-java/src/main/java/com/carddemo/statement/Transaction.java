package com.carddemo.statement;

import java.math.BigDecimal;

public record Transaction(String cardNumber, String transactionId, String description,
                          BigDecimal amount) {}
