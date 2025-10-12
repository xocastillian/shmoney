package com.shmoney.wallet.dto;

import com.shmoney.wallet.entity.WalletType;
import java.time.OffsetDateTime;

public record WalletResponse(
        Long id,
        Long ownerId,
        String name,
        String currencyCode,
        String color,
        WalletType type,
        java.math.BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
