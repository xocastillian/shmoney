package com.shmoney.debt.dto;

import java.math.BigDecimal;

public record DebtCounterpartySummary(
        Long id,
        String name,
        BigDecimal owedToMe,
        BigDecimal iOwe,
        BigDecimal owedToMeShare,
        BigDecimal iOweShare
) {
}
