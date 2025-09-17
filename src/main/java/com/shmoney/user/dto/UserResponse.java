package com.shmoney.user.dto;

import java.time.OffsetDateTime;

public record UserResponse(
    Long id,
    String name,
    String email,
    String role,
    boolean subscriptionActive,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    OffsetDateTime lastLoginAt
) {
}

