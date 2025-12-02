package com.shmoney.transaction.category.dto;

import com.shmoney.transaction.category.entity.CategoryTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CategoryTransactionCreateRequest(
        @Positive Long walletId,
        @NotNull Long categoryId,
        @NotNull CategoryTransactionType type,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @NotNull @PastOrPresent OffsetDateTime occurredAt,
        @Size(max = 255) String description
) {
}
