package com.shmoney.wallet.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WalletUpdateRequest(
        @Size(max = 50) String name,
        @Size(max = 10) String currencyCode,
        @Positive Long ownerId
) {
}
