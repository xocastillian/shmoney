package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtTransactionDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DebtTransactionCreateRequest(
        @NotNull @Positive Long counterpartyId,
        @NotNull @Positive Long walletId,
        @NotNull DebtTransactionDirection direction,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @NotNull @Positive Long currencyId,
        @NotNull @PastOrPresent OffsetDateTime occurredAt,
        @Size(max = 255) String description
) {
}
