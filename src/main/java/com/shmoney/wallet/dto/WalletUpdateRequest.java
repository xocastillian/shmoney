package com.shmoney.wallet.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WalletUpdateRequest(
        @Size(max = 50) String name,
        @Size(max = 10) String currencyCode,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        @Positive Long ownerId
) {
}
