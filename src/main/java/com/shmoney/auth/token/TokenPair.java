package com.shmoney.auth.token;

import java.time.OffsetDateTime;

public record TokenPair(
        String accessToken,
        OffsetDateTime accessTokenExpiresAt,
        String refreshToken,
        OffsetDateTime refreshTokenExpiresAt
) {
}
