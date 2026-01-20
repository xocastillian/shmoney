package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtCounterpartyStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DebtCounterpartyResponse(
        Long id,
        String name,
        String color,
        String currencyCode,
        BigDecimal owedToMe,
        BigDecimal iOwe,
        DebtCounterpartyStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
