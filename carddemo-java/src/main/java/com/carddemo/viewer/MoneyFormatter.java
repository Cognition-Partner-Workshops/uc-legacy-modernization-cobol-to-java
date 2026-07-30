package com.carddemo.viewer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component("moneyFormatter")
public class MoneyFormatter {
    public String format(BigDecimal amount) {
        BigDecimal value = amount == null
                ? BigDecimal.ZERO.setScale(2)
                : amount.setScale(2, RoundingMode.HALF_UP);
        String prefix = value.signum() < 0 ? "-$" : "$";
        return prefix + value.abs().toPlainString();
    }
}
