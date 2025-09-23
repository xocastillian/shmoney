package com.shmoney.currency.dto;

import com.shmoney.currency.entity.Currency;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {
    
    CurrencyResponse toResponse(Currency currency);
}
