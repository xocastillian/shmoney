package com.shmoney.transaction.category.dto;

import com.shmoney.transaction.category.entity.CategoryTransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CategoryTransactionResponse(
        Long id,
        Long walletId,
        String walletName,
        Long categoryId,
        String categoryName,
        CategoryTransactionType type,
        BigDecimal amount,
        String currencyCode,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
