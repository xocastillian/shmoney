package com.shmoney.wallet.dto;

import com.shmoney.wallet.entity.WalletStatus;
import jakarta.validation.constraints.NotNull;

public record WalletStatusUpdateRequest(
        @NotNull WalletStatus status
) {
}
