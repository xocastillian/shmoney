package com.shmoney.debt.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.service.CurrencyService;
import com.shmoney.currency.service.ExchangeRateService;
import com.shmoney.debt.dto.DebtCounterpartyCreateRequest;
import com.shmoney.debt.dto.DebtCounterpartyUpdateRequest;
import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtCounterpartyStatus;
import com.shmoney.debt.exception.DebtCounterpartyNotFoundException;
import com.shmoney.debt.exception.InvalidDebtCounterpartyException;
import com.shmoney.debt.repository.DebtCounterpartyRepository;
import com.shmoney.settings.service.AppSettingsProvider;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional
public class DebtCounterpartyService {
    
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    
    private final DebtCounterpartyRepository counterpartyRepository;
    private final UserService userService;
    private final CurrencyService currencyService;
    private final AppSettingsProvider appSettingsProvider;
    private final ExchangeRateService exchangeRateService;
    
    public DebtCounterpartyService(DebtCounterpartyRepository counterpartyRepository,
                                   UserService userService,
                                   CurrencyService currencyService,
                                   AppSettingsProvider appSettingsProvider,
                                   ExchangeRateService exchangeRateService) {
        this.counterpartyRepository = counterpartyRepository;
        this.userService = userService;
        this.currencyService = currencyService;
        this.appSettingsProvider = appSettingsProvider;
        this.exchangeRateService = exchangeRateService;
    }
    
    public DebtCounterparty create(Long userId, DebtCounterpartyCreateRequest request) {
        String name = sanitizeName(request.name());
        User owner = userService.getById(userId);
        Currency currency = resolveMainCurrency();
        
        DebtCounterparty counterparty = new DebtCounterparty();
        counterparty.setUser(owner);
        counterparty.setName(name);
        counterparty.setColor(request.color());
        counterparty.setCurrency(currency);
        counterparty.setOwedToMe(ZERO);
        counterparty.setIOwe(ZERO);
        counterparty.setStatus(DebtCounterpartyStatus.ACTIVE);
        
        return counterpartyRepository.save(counterparty);
    }
    
    @Transactional(readOnly = true)
    public List<DebtCounterparty> list(Long userId, DebtCounterpartyStatus status) {
        if (status == null) {
            return counterpartyRepository.findAllByUserIdOrderByIdAsc(userId);
        }
        
        return counterpartyRepository.findAllByUserIdAndStatusOrderByIdAsc(userId, status);
    }
    
    @Transactional(readOnly = true)
    public DebtCounterparty getOwned(Long userId, Long id) {
        return counterpartyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DebtCounterpartyNotFoundException(id));
    }
    
    public DebtCounterparty update(Long userId, Long id, DebtCounterpartyUpdateRequest request) {
        DebtCounterparty counterparty = getOwned(userId, id);
        
        if (request.name() != null) {
            counterparty.setName(sanitizeName(request.name()));
        }
        
        if (request.color() != null) {
            counterparty.setColor(request.color());
        }
        
        return counterpartyRepository.save(counterparty);
    }
    
    public DebtCounterparty archive(Long userId, Long id) {
        DebtCounterparty counterparty = getOwned(userId, id);
        counterparty.setStatus(DebtCounterpartyStatus.ARCHIVED);
        
        return counterpartyRepository.save(counterparty);
    }
    
    public DebtCounterparty restore(Long userId, Long id) {
        DebtCounterparty counterparty = getOwned(userId, id);
        counterparty.setStatus(DebtCounterpartyStatus.ACTIVE);
        
        return counterpartyRepository.save(counterparty);
    }
    
    public void recalculateForNewCurrency(String newCurrencyCode) {
        Currency newCurrency = currencyService.getActiveByCode(newCurrencyCode);
        List<DebtCounterparty> counterparties = counterpartyRepository.findAll();
        
        for (DebtCounterparty counterparty : counterparties) {
            String previousCode = counterparty.getCurrency().getCode();
            BigDecimal owed = convert(counterparty.getOwedToMe(), previousCode, newCurrency.getCode());
            BigDecimal iOwe = convert(counterparty.getIOwe(), previousCode, newCurrency.getCode());
            counterparty.setCurrency(newCurrency);
            counterparty.setOwedToMe(owed);
            counterparty.setIOwe(iOwe);
            counterpartyRepository.save(counterparty);
        }
    }
    
    private Currency resolveMainCurrency() {
        String code = appSettingsProvider.getOrCreate().getMainCurrency();
        
        return currencyService.getActiveByCode(code);
    }
    
    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidDebtCounterpartyException("Название контрагента не может быть пустым");
        }
        
        return name.trim();
    }
    
    private BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null) return ZERO;
        
        if (from.equalsIgnoreCase(to)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        
        return exchangeRateService.convert(amount, from, to);
    }
}
