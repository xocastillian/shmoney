package com.shmoney.settings.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateAppSettingsRequest(
        @Size(min = 2, max = 8) String language,
        @Pattern(regexp = "^[A-Za-z]{3}$") String mainCurrency
) {
}
