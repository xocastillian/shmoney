package com.shmoney.budget.dto;

import com.shmoney.budget.entity.BudgetPeriodType;
import com.shmoney.budget.entity.BudgetType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BudgetCreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull BudgetPeriodType periodType,
        OffsetDateTime periodStart,
        OffsetDateTime periodEnd,
        @NotNull BudgetType budgetType,
        @NotEmpty List<Long> categoryIds,
        @NotBlank @Size(max = 10) @Pattern(regexp = "^[A-Za-z]{3}$") String currencyCode,
        @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amountLimit
) {
}
