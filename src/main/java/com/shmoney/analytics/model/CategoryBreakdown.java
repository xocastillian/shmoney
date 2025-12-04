package com.shmoney.analytics.model;

import java.math.BigDecimal;

public record CategoryBreakdown(
        Long categoryId,
        String categoryName,
        String categoryColor,
        BigDecimal amount,
        long transactionCount
) {
}
