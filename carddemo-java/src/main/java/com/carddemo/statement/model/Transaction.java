package com.carddemo.statement.model;

import java.math.BigDecimal;

public record Transaction(String transactionId, String description, BigDecimal amount, String cardNumber) {}
