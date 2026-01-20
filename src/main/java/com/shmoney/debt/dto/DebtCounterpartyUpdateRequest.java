package com.shmoney.debt.dto;

import jakarta.validation.constraints.Size;

public record DebtCounterpartyUpdateRequest(
        @Size(max = 120) String name,
        @Size(max = 16) String color
) {
}
