package com.carddemo.statement.viewer;

import java.math.BigDecimal;

public record Transaction(String transactionId, String description, BigDecimal amount) {}
