package com.shmoney.wallet.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.service.CurrencyService;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.entity.WalletType;
import com.shmoney.wallet.exception.WalletNotFoundException;
import com.shmoney.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final UserService userService;
    private final CurrencyService currencyService;
    
    public WalletService(WalletRepository walletRepository,
                         UserService userService,
                         CurrencyService currencyService) {
        this.walletRepository = walletRepository;
        this.userService = userService;
        this.currencyService = currencyService;
    }
    
    public Wallet create(Long ownerId, Wallet wallet, String currencyCode, BigDecimal initialBalance) {
        wallet.setId(null);
        wallet.setOwner(resolveOwner(ownerId));
        wallet.setCurrency(resolveCurrency(currencyCode));
        wallet.setBalance(normalizeBalance(initialBalance));
        
        if (wallet.getType() == null) wallet.setType(WalletType.CASH);
        
        return walletRepository.save(wallet);
    }
    
    @Transactional(readOnly = true)
    public Wallet getById(Long id) {
        return walletRepository
                .findById(id)
                .orElseThrow(() -> new WalletNotFoundException(id));
    }
    
    @Transactional(readOnly = true)
    public List<Wallet> getByOwner(Long ownerId) {
        return walletRepository.findAllByOwnerIdOrderByIdAsc(ownerId);
    }

    @Transactional(readOnly = true)
    public List<CurrencyBalance> getCurrencyBalancesForOwner(Long ownerId) {
        return aggregateBalances(walletRepository.findAllByOwnerIdOrderByIdAsc(ownerId));
    }
    
    public Wallet update(Wallet wallet,
                         Long ownerId,
                         String currencyCode,
                         WalletType type,
                         BigDecimal balance,
                         String color) {
        if (ownerId != null) wallet.setOwner(resolveOwner(ownerId));
        
        if (currencyCode != null && !currencyCode.isBlank()) {
            wallet.setCurrency(resolveCurrency(currencyCode));
        }
        
        if (type != null) wallet.setType(type);
        
        if (balance != null) {
            wallet.setBalance(normalizeBalance(balance));
        }
        
        if (color != null) wallet.setColor(color);
        
        return walletRepository.save(wallet);
    }
    
    public void delete(Long id) {
        if (!walletRepository.existsById(id)) throw new WalletNotFoundException(id);
        
        walletRepository.deleteById(id);
    }
    
    private User resolveOwner(Long ownerId) {
        return userService.getById(ownerId);
    }
    
    private Currency resolveCurrency(String code) {
        return currencyService.getActiveByCode(code.trim());
    }
    
    private BigDecimal normalizeBalance(BigDecimal balance) {
        BigDecimal value = balance == null ? BigDecimal.ZERO : balance;
        return value.setScale(2, RoundingMode.HALF_UP);
    }
    
    private List<CurrencyBalance> aggregateBalances(List<Wallet> wallets) {
        Map<String, BigDecimal> totals = new HashMap<>();
        for (Wallet wallet : wallets) {
            if (wallet.getCurrency() == null) {
                continue;
            }
            String code = wallet.getCurrency().getCode();
            BigDecimal walletBalance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
            totals.merge(code, walletBalance, BigDecimal::add);
        }
        
        return totals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CurrencyBalance(entry.getKey(), entry.getValue().setScale(2, RoundingMode.HALF_UP)))
                .toList();
    }
    
    public record CurrencyBalance(String currencyCode, BigDecimal totalBalance) {
    }
}
