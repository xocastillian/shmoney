package com.shmoney.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record UserUpdateRequest(
        @Size(max = 30, min = 2) String name,
        @Email @Size(max = 30, min = 2) String email,
        @Size(max = 255, min = 6) String password,
        @Size(max = 30) String role,
        Boolean subscriptionActive,
        OffsetDateTime lastLoginAt
) {
}
