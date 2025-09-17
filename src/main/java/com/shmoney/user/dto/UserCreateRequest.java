package com.shmoney.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 30, min = 2) String name,
        @NotBlank @Email @Size(max = 30, min = 2) String email,
        @NotBlank @Size(max = 255, min = 6) String password,
        @NotBlank @Size(max = 30) String role,
        boolean subscriptionActive
) {
}
