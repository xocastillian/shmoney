package com.shmoney.budget.dto;

import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BudgetUpdateRequest(
        @Size(max = 100) String name,
        BudgetPeriodType periodType,
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        BudgetType budgetType,
        List<Long> categoryIds,
        @Size(max = 10) @Pattern(regexp = "^[A-Za-z]{3}$") String currencyCode,
        @DecimalMin(value = "0.01", inclusive = true) BigDecimal amountLimit
) {
}
