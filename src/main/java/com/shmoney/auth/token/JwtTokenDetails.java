package com.shmoney.auth.token;

import java.time.OffsetDateTime;

public record JwtTokenDetails(
        Long userId,
        String email,
        String role,
        OffsetDateTime expiresAt
) {
}
