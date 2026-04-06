package com.suitecrm.auth.engine;

import com.suitecrm.auth.entity.Currency;
import com.suitecrm.auth.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyConversionService {

    private final CurrencyRepository currencyRepository;

    public BigDecimal convertToUsd(BigDecimal amount, UUID currencyId) {
        if (amount == null || currencyId == null) return amount;
        Currency currency = currencyRepository.findById(currencyId).orElse(null);
        if (currency == null || currency.getConversionRate() == null) return amount;
        return amount.divide(currency.getConversionRate(), 6, RoundingMode.HALF_UP);
    }

    public BigDecimal convertFromUsd(BigDecimal amountUsd, UUID targetCurrencyId) {
        if (amountUsd == null || targetCurrencyId == null) return amountUsd;
        Currency currency = currencyRepository.findById(targetCurrencyId).orElse(null);
        if (currency == null || currency.getConversionRate() == null) return amountUsd;
        return amountUsd.multiply(currency.getConversionRate()).setScale(6, RoundingMode.HALF_UP);
    }

    public BigDecimal convert(BigDecimal amount, UUID fromCurrencyId, UUID toCurrencyId) {
        BigDecimal usdAmount = convertToUsd(amount, fromCurrencyId);
        return convertFromUsd(usdAmount, toCurrencyId);
    }
}
