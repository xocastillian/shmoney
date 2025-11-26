package com.shmoney.transaction.category.dto;

import com.shmoney.transaction.category.entity.CategoryTransactionType;

import java.time.OffsetDateTime;

public record CategoryTransactionFilter(
        Long walletId,
        Long categoryId,
        Long subcategoryId,
        CategoryTransactionType type,
        OffsetDateTime from,
        OffsetDateTime to
) {
}
