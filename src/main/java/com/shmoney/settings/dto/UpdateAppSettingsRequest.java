package com.shmoney.settings.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAppSettingsRequest(
        @NotBlank String language
) {
}
