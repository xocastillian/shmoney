package com.shmoney.analytics.dto;

import java.math.BigDecimal;

public record CategoryAnalyticsItem(
        Long categoryId,
        String categoryName,
        String categoryColor,
        String categoryIcon,
        BigDecimal amount,
        BigDecimal percent,
        long transactionCount
) {
}
