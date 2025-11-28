package com.shmoney.user.dto;

import com.shmoney.wallet.dto.WalletResponse;
import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
    Long id,
    Long telegramUserId,
    String telegramUsername,
    String telegramLanguageCode,
    boolean subscriptionActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<WalletResponse> wallets
) {
}
