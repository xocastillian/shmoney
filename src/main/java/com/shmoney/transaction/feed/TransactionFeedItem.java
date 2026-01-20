package com.shmoney.transaction.feed;

import com.shmoney.transaction.category.entity.CategoryTransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionFeedItem(
        Long id,
        String entryType,
        CategoryTransactionType categoryType,
        Long walletId,
        Long counterpartyWalletId,
        Long categoryId,
        Long debtCounterpartyId,
        String debtDirection,
        BigDecimal amount,
        String currencyCode,
        String description,
        OffsetDateTime occurredAt,
        OffsetDateTime createdAt
) {
}
