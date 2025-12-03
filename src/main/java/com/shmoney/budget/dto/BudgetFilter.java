package com.shmoney.budget.dto;

import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetStatus;
import com.shmoney.budget.entity.BudgetType;
import java.time.OffsetDateTime;

public record BudgetFilter(
        BudgetStatus status,
        BudgetPeriodType periodType,
        BudgetType budgetType,
        OffsetDateTime from,
        OffsetDateTime to
) {
}
