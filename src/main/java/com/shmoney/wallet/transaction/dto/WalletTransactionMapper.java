package com.shmoney.wallet.transaction.dto;

import com.shmoney.wallet.transaction.entity.WalletTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletTransactionMapper {

    @Mapping(target = "fromWalletId", source = "fromWallet.id")
    @Mapping(target = "fromWalletName", source = "fromWallet.name")
    @Mapping(target = "toWalletId", source = "toWallet.id")
    @Mapping(target = "toWalletName", source = "toWallet.name")
    @Mapping(target = "sourceCurrencyCode", source = "sourceCurrency.code")
    @Mapping(target = "targetCurrencyCode", source = "targetCurrency.code")
    WalletTransactionResponse toResponse(WalletTransaction transaction);
}
