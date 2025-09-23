package com.shmoney.currency.controller;

import com.shmoney.currency.dto.CurrencyMapper;
import com.shmoney.currency.dto.CurrencyResponse;
import com.shmoney.currency.service.CurrencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Currencies")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    private final CurrencyMapper currencyMapper;
    
    public CurrencyController(CurrencyService currencyService, CurrencyMapper currencyMapper) {
        this.currencyService = currencyService;
        this.currencyMapper = currencyMapper;
    }
    
    @Operation(summary = "Список активных валют")
    @GetMapping
    public List<CurrencyResponse> getActiveCurrencies() {
        return currencyService.getActiveCurrencies().stream()
                .map(currencyMapper::toResponse)
                .toList();
    }
}
