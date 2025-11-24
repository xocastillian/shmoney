package com.shmoney.category.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 100) String name,
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        @Size(max = 100) String icon
) {
}
