package com.shmoney.currency.dto;

public record CurrencyResponse(
        Long id,
        String code,
        String name,
        int decimalPrecision,
        boolean active
) {
}
