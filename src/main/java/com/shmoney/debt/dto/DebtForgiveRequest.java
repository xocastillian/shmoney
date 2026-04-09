package com.shmoney.debt.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record DebtForgiveRequest(
        @NotNull @PastOrPresent OffsetDateTime occurredAt,
        @Size(max = 255) String description
) {
}
