package com.shmoney.wallet.dto;

import java.time.OffsetDateTime;

public record WalletResponse(
        Long id,
        Long ownerId,
        String name,
        String currencyCode,
        String color,
        java.math.BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
