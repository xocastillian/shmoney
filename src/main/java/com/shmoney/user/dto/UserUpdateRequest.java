package com.shmoney.user.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 30) String role,
        Boolean subscriptionActive
) {
}
