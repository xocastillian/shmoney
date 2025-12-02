package com.shmoney.transaction.category.dto;

import com.shmoney.transaction.category.entity.CategoryTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CategoryTransactionUpdateRequest(
        @Positive Long walletId,
        Long categoryId,
        CategoryTransactionType type,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @PastOrPresent OffsetDateTime occurredAt,
        @Size(max = 255) String description
) {
}
