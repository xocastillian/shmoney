package com.shmoney.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletCreateRequest(
        @NotBlank @Size(max = 50) String name,
        @NotBlank @Size(max = 10) String currencyCode,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal balance,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        @Positive Long ownerId
) {
}
