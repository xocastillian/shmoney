package com.shmoney.currency.service;

import com.shmoney.config.CacheConfig;
import com.shmoney.currency.client.ExchangeRateClient;
import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.entity.ExchangeRate;
import com.shmoney.currency.repository.ExchangeRateRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ExchangeRateService {
    
    private static final String SOURCE_PROVIDER = "ExchangeRate.host";
    private static final String BASE_PIVOT_CODE = "USD";
    private static final Duration RATE_TTL = Duration.ofHours(12);
    private static final int RATE_SCALE = 6;
    private static final int AMOUNT_SCALE = 2;
    
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyService currencyService;
    private final ExchangeRateClient exchangeRateClient;
    
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository,
                               CurrencyService currencyService,
                               ExchangeRateClient exchangeRateClient) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyService = currencyService;
        this.exchangeRateClient = exchangeRateClient;
    }
    
    @Cacheable(value = CacheConfig.EXCHANGE_RATE_CACHE,
            key = "#sourceCurrency.toUpperCase() + '_' + #targetCurrency.toUpperCase()")
    public BigDecimal getRate(String sourceCurrency, String targetCurrency) {
        Objects.requireNonNull(sourceCurrency, "sourceCurrency");
        Objects.requireNonNull(targetCurrency, "targetCurrency");
        String from = sourceCurrency.toUpperCase();
        String to = targetCurrency.toUpperCase();
        if (from.equals(to)) {
            return BigDecimal.ONE.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        Currency source = currencyService.getActiveByCode(from);
        Currency target = currencyService.getActiveByCode(to);
        Currency pivot = currencyService.getActiveByCode(BASE_PIVOT_CODE);
        BigDecimal pivotToSource = resolvePivotRate(pivot, source);
        BigDecimal pivotToTarget = resolvePivotRate(pivot, target);
        return pivotToTarget.divide(pivotToSource, RATE_SCALE, RoundingMode.HALF_UP);
    }
    
    public BigDecimal convert(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        Objects.requireNonNull(amount, "amount");
        BigDecimal rate = getRate(sourceCurrency, targetCurrency);
        return amount.multiply(rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }
    
    private BigDecimal resolvePivotRate(Currency pivot, Currency target) {
        if (pivot.getCode().equalsIgnoreCase(target.getCode())) {
            return BigDecimal.ONE.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        }
        OffsetDateTime threshold = OffsetDateTime.now().minus(RATE_TTL);
        return exchangeRateRepository
                .findTopByBaseCurrencyCodeIgnoreCaseAndTargetCurrencyCodeIgnoreCaseAndFetchedAtAfter(
                        pivot.getCode(),
                        target.getCode(),
                        threshold
                )
                .map(ExchangeRate::getRate)
                .orElseGet(() -> refreshRates(pivot, target.getCode()));
    }
    
    private BigDecimal refreshRates(Currency pivot, String requestedTargetCode) {
        List<Currency> activeCurrencies = currencyService.getActiveCurrencies();
        Map<String, Currency> currencyByCode = activeCurrencies.stream()
                .collect(Collectors.toMap(c -> c.getCode().toUpperCase(), c -> c));
        List<String> symbols = activeCurrencies.stream()
                .map(currency -> currency.getCode().toUpperCase())
                .filter(code -> !code.equalsIgnoreCase(pivot.getCode()))
                .toList();
        ExchangeRateClient.ExchangeRateResponse response = exchangeRateClient.fetchLatest(pivot.getCode(), symbols);
        OffsetDateTime fetchedAt = OffsetDateTime.now();
        BigDecimal result = null;
        for (Map.Entry<String, BigDecimal> entry : response.rates().entrySet()) {
            String targetCode = entry.getKey().toUpperCase();
            Currency target = currencyByCode.get(targetCode);
            if (target == null) {
                continue;
            }
            BigDecimal rateValue = entry.getValue().setScale(RATE_SCALE, RoundingMode.HALF_UP);
            ExchangeRate rate = new ExchangeRate();
            rate.setBaseCurrency(pivot);
            rate.setTargetCurrency(target);
            rate.setRate(rateValue);
            rate.setFetchedAt(fetchedAt);
            rate.setSource(SOURCE_PROVIDER);
            exchangeRateRepository.save(rate);
            if (targetCode.equals(requestedTargetCode.toUpperCase())) {
                result = rateValue;
            }
        }
        if (result == null) {
            throw new IllegalStateException("Requested currency " + requestedTargetCode + " not returned by provider");
        }
        return result;
    }
}
