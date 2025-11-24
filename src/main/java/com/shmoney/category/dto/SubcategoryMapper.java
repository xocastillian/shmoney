package com.shmoney.category.dto;

import com.shmoney.category.entity.Subcategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubcategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Subcategory toEntity(SubcategoryCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", ignore = true)
    void updateEntity(SubcategoryUpdateRequest request, @MappingTarget Subcategory subcategory);

    SubcategoryResponse toResponse(Subcategory subcategory);
}
