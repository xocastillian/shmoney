package com.shmoney.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TelegramAuthRequest(
        @NotBlank String initData
) {
}
