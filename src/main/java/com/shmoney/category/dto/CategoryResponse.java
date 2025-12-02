package com.shmoney.category.dto;

import com.shmoney.category.entity.CategoryStatus;

import java.time.OffsetDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        String icon,
        CategoryStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
