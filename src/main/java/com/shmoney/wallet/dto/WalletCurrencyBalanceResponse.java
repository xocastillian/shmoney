package com.shmoney.wallet.dto;

import java.math.BigDecimal;

public record WalletCurrencyBalanceResponse(
        String currencyCode,
        BigDecimal totalBalance
) {
}
