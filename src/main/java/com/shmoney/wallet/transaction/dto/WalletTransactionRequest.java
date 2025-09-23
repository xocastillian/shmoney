package com.shmoney.wallet.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WalletTransactionRequest(
        @NotNull Long fromWalletId,
        @NotNull Long toWalletId,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        String description,
        OffsetDateTime executedAt
) {
}
