package com.shmoney.wallet.dto;

import com.shmoney.wallet.entity.Wallet;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WalletMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Wallet toEntity(WalletCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "color", ignore = true)
    void updateEntity(WalletUpdateRequest request, @MappingTarget Wallet wallet);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "currencyCode", source = "currency.code")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "debetOrCredit", source = "debetOrCredit")
    WalletResponse toResponse(Wallet wallet);
}
