package com.shmoney.user.dto;

import com.shmoney.user.entity.User;
import com.shmoney.wallet.dto.WalletMapper;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = WalletMapper.class)
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserCreateRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "passwordHash", ignore = true)
    void updateEntity(UserUpdateRequest request, @MappingTarget User user);
    
    @Mapping(target = "wallets", source = "wallets")
    UserResponse toResponse(User user);
}
