package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtTransactionDirection;

import java.time.OffsetDateTime;

public record DebtTransactionFilter(
        Long counterpartyId,
        DebtTransactionDirection direction,
        OffsetDateTime from,
        OffsetDateTime to
) {
}
