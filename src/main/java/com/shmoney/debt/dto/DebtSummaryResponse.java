package com.shmoney.debt.dto;

import java.math.BigDecimal;
import java.util.List;

public record DebtSummaryResponse(
        String currencyCode,
        BigDecimal totalOwedToMe,
        BigDecimal totalIOwe,
        List<DebtCounterpartySummary> counterparties
) {
}
