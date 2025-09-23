package com.shmoney.currency.repository;

import com.shmoney.currency.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    Optional<ExchangeRate> findTopByBaseCurrencyCodeIgnoreCaseAndTargetCurrencyCodeIgnoreCaseOrderByFetchedAtDesc(
            String baseCode,
            String targetCode
    );
    
    Optional<ExchangeRate> findTopByBaseCurrencyCodeIgnoreCaseAndTargetCurrencyCodeIgnoreCaseAndFetchedAtAfter(
            String baseCode,
            String targetCode,
            OffsetDateTime threshold
    );
}
