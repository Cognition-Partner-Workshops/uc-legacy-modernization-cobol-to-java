package com.carddemo.statement;

import java.math.BigDecimal;

public record Account(String id, BigDecimal currentBalance) {}
