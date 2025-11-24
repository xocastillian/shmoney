package com.shmoney.category.dto;

import java.time.OffsetDateTime;

public record SubcategoryResponse(
        Long id,
        String name,
        String color,
        String icon,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
