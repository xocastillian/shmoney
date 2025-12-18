package com.shmoney.wallet.dto;

import com.shmoney.wallet.entity.DebetOrCredit;
import com.shmoney.wallet.entity.WalletStatus;
import com.shmoney.wallet.entity.WalletType;
import java.time.OffsetDateTime;

public record WalletResponse(
        Long id,
        Long ownerId,
        String name,
        String currencyCode,
        String color,
        WalletType type,
        DebetOrCredit debetOrCredit,
        WalletStatus status,
        java.math.BigDecimal balance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
