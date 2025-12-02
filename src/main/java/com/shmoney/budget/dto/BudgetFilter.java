package com.shmoney.budget.dto;

import com.shmoney.budget.entity.BudgetStatus;
import java.time.OffsetDateTime;

public record BudgetFilter(
        BudgetStatus status,
        OffsetDateTime from,
        OffsetDateTime to
) {
}
