package com.shmoney.debt.dto;

import com.shmoney.debt.entity.DebtTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DebtTransactionMapper {

    @Mapping(target = "counterpartyId", source = "counterparty.id")
    @Mapping(target = "counterpartyName", source = "counterparty.name")
    @Mapping(target = "walletId", source = "wallet.id")
    @Mapping(target = "currencyCode", source = "currency.code")
    DebtTransactionResponse toResponse(DebtTransaction transaction);
}
