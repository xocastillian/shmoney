package com.shmoney.analytics.dto;

import java.math.BigDecimal;

public record CategoryAnalyticsItem(
        Long categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal amount,
        BigDecimal percent,
        long transactionCount
) {
}
