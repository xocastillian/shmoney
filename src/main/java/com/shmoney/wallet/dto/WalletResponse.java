package com.shmoney.wallet.dto;

import java.time.OffsetDateTime;

public record WalletResponse(
        Long id,
        Long ownerId,
        String name,
        String currencyCode,
        java.math.BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
