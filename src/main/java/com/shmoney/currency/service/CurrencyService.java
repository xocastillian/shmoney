package com.shmoney.currency.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.exception.CurrencyNotFoundException;
import com.shmoney.currency.repository.CurrencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CurrencyService {
    
    private final CurrencyRepository currencyRepository;
    
    public CurrencyService(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }
    
    public List<Currency> getActiveCurrencies() {
        return currencyRepository.findAllByActiveTrueOrderByCodeAsc();
    }
    
    public Currency getActiveByCode(String code) {
        Currency currency = getByCode(code);
        if (!currency.isActive()) {
            throw new CurrencyNotFoundException(code);
        }
        return currency;
    }
    
    public Currency getByCode(String code) {
        return currencyRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new CurrencyNotFoundException(code));
    }
}
