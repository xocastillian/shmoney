package com.shmoney.currency.controller;

import com.shmoney.currency.dto.CurrencyMapper;
import com.shmoney.currency.dto.CurrencyResponse;
import com.shmoney.currency.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {
    
    private final CurrencyService currencyService;
    private final CurrencyMapper currencyMapper;
    
    public CurrencyController(CurrencyService currencyService, CurrencyMapper currencyMapper) {
        this.currencyService = currencyService;
        this.currencyMapper = currencyMapper;
    }
    
    @GetMapping
    public List<CurrencyResponse> getActiveCurrencies() {
        return currencyService.getActiveCurrencies().stream()
                .map(currencyMapper::toResponse)
                .toList();
    }
}
