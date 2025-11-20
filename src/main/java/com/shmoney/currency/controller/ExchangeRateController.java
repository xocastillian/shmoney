package com.shmoney.currency.controller;

import com.shmoney.currency.dto.CurrencyConversionResponse;
import com.shmoney.currency.dto.ExchangeRateResponse;
import com.shmoney.currency.service.ExchangeRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Exchange Rates")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/exchange-rates")
@Validated
public class ExchangeRateController {
    
    private final ExchangeRateService exchangeRateService;
    
    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }
    
    @Operation(summary = "Получить курс между валютами")
    @GetMapping
    public ExchangeRateResponse getRate(@RequestParam @NotBlank String from,
                                        @RequestParam @NotBlank String to) {
        BigDecimal rate = exchangeRateService.getRate(from, to);

        return new ExchangeRateResponse(from.toUpperCase(), to.toUpperCase(), rate);
    }

    @Operation(summary = "Курсы всех валют к базовой")
    @GetMapping("/all")
    public java.util.List<ExchangeRateResponse> getAllTo(@RequestParam(defaultValue = "KZT") @NotBlank String to) {
        return exchangeRateService.getAllTo(to);
    }
    
    @Operation(summary = "Конвертировать сумму между валютами")
    @GetMapping("/convert")
    public CurrencyConversionResponse convert(@RequestParam @NotBlank String from,
                                              @RequestParam @NotBlank String to,
                                              @RequestParam @DecimalMin(value = "0", inclusive = false) BigDecimal amount) {
        BigDecimal rate = exchangeRateService.getRate(from, to);
        BigDecimal converted = exchangeRateService.convert(amount, from, to);
        
        return new CurrencyConversionResponse(
                amount.setScale(2, java.math.RoundingMode.HALF_UP),
                from.toUpperCase(),
                to.toUpperCase(),
                rate,
                converted
        );
    }
}
