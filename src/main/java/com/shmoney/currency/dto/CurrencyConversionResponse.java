package com.shmoney.currency.dto;

import java.math.BigDecimal;

public record CurrencyConversionResponse(
        BigDecimal amount,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal rate,
        BigDecimal convertedAmount
) {
}
