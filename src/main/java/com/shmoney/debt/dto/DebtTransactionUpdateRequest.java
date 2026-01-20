package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtTransactionDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DebtTransactionUpdateRequest(
        @Positive Long counterpartyId,
        @Positive Long walletId,
        DebtTransactionDirection direction,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @Positive Long currencyId,
        @PastOrPresent OffsetDateTime occurredAt,
        @Size(max = 255) String description
) {
}
