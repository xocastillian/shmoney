package com.shmoney.category.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String color,
        String icon,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SubcategoryResponse> subcategories
) {
}
