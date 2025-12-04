package com.shmoney.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AnalyticsResponse(
        AnalyticsPeriod period,
        String currencyCode,
        BigDecimal totalExpense,
        BigDecimal totalIncome,
        BigDecimal cashFlowAmount,
        BigDecimal cashFlowPercent,
        long totalExpenseTransactions,
        List<CategoryAnalyticsItem> categories,
        List<CategoryAnalyticsItem> topCategories
) {
}
