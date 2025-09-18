package com.shmoney.auth.dto;

import java.time.OffsetDateTime;

public record AuthResponse(
        String accessToken,
        OffsetDateTime accessTokenExpiresAt,
        String refreshToken,
        OffsetDateTime refreshTokenExpiresAt
) {
}
