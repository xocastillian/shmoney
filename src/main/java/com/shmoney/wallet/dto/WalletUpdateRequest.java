package com.shmoney.wallet.dto;

import com.shmoney.wallet.entity.DebetOrCredit;
import com.shmoney.wallet.entity.WalletType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record WalletUpdateRequest(
        @Size(max = 50) String name,
        @Size(max = 10) String currencyCode,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        @DecimalMin(value = "0", inclusive = true) BigDecimal balance,
        WalletType type,
        DebetOrCredit debetOrCredit,
        @Positive Long ownerId
) {
}
