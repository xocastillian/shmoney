package com.shmoney.wallet.transaction.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record WalletTransactionResponse(
        Long id,
        Long fromWalletId,
        String fromWalletName,
        Long toWalletId,
        String toWalletName,
        BigDecimal sourceAmount,
        String sourceCurrencyCode,
        BigDecimal targetAmount,
        String targetCurrencyCode,
        BigDecimal exchangeRate,
        String description,
        OffsetDateTime executedAt,
        OffsetDateTime createdAt
) {
}
