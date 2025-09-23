package com.shmoney.currency.repository;

import com.shmoney.currency.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    
    Optional<Currency> findByCodeIgnoreCase(String code);

    List<Currency> findAllByActiveTrueOrderByCodeAsc();
}
