package com.shmoney.category.dto;

import com.shmoney.category.entity.CategoryStatus;
import jakarta.validation.constraints.NotNull;

public record CategoryStatusUpdateRequest(
        @NotNull CategoryStatus status
) {
}
