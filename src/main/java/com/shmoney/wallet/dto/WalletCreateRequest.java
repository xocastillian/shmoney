package com.shmoney.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WalletCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 10) String currencyCode,
        @Positive Long ownerId
) {
}
