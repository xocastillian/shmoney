package com.shmoney.debt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DebtCounterpartyCreateRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 16) String color
) {
}
