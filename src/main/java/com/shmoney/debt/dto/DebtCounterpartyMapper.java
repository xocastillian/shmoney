package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtCounterparty;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DebtCounterpartyMapper {

    @Mapping(target = "currencyCode", source = "currency.code")
    @Mapping(target = "iOwe", source = "IOwe")
    DebtCounterpartyResponse toResponse(DebtCounterparty counterparty);
}
