package com.shmoney.currency.dto;

import java.math.BigDecimal;

public record ExchangeRateResponse(
        String sourceCurrency,
        String targetCurrency,
        BigDecimal rate
) {
}
