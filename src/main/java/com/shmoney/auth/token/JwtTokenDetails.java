package com.shmoney.auth.token;

import java.time.OffsetDateTime;

public record JwtTokenDetails(
        Long userId,
        String displayName,
        String role,
        OffsetDateTime expiresAt
) {
}
