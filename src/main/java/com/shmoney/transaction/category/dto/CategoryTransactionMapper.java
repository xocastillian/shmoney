package com.shmoney.transaction.category.dto;

import com.shmoney.transaction.category.entity.CategoryTransaction;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryTransactionMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "currency", ignore = true)
    void updateEntity(CategoryTransactionUpdateRequest request, @MappingTarget CategoryTransaction transaction);

    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "walletName", source = "wallet.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "subcategoryId", source = "subcategory.id")
    @Mapping(target = "subcategoryName", source = "subcategory.name")
    @Mapping(target = "currencyCode", source = "currency.code")
    CategoryTransactionResponse toResponse(CategoryTransaction transaction);
}
