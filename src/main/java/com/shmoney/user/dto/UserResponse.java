package com.shmoney.user.dto;

import com.shmoney.wallet.dto.WalletResponse;
import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
    Long id,
    String name,
    String email,
    String role,
    boolean subscriptionActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime lastLoginAt,
    List<WalletResponse> wallets
) {
}
