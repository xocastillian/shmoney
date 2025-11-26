package com.shmoney.wallet.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WalletTransactionUpdateRequest(
        @Positive Long fromWalletId,
        @Positive Long toWalletId,
        @DecimalMin(value = "0.00", inclusive = false) BigDecimal amount,
        @PastOrPresent OffsetDateTime executedAt,
        @Size(max = 255) String description
) {
}
