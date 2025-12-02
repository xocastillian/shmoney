package com.shmoney.budget.dto;

import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetStatus;
import com.shmoney.budget.entity.BudgetType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BudgetResponse(
        Long id,
        String name,
        BudgetPeriodType periodType,
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        BudgetType budgetType,
        String currencyCode,
        BigDecimal amountLimit,
        BigDecimal spentAmount,
        BigDecimal percentSpent,
        BudgetStatus status,
        OffsetDateTime closedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<Long> categoryIds
) {
}
