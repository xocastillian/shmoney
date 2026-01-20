package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtTransactionDirection;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DebtTransactionResponse(
        Long id,
        Long counterpartyId,
        String counterpartyName,
        Long walletId,
        DebtTransactionDirection direction,
        BigDecimal amount,
        String currencyCode,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime createdAt
) {
}
