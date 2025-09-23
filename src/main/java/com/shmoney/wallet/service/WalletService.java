package com.shmoney.wallet.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.service.CurrencyService;
import com.shmoney.user.entity.User;
import com.shmoney.user.service.UserService;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.exception.WalletNotFoundException;
import com.shmoney.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    
    public Wallet create(Long ownerId, Wallet wallet, String currencyCode) {
        wallet.setId(null);
        wallet.setOwner(resolveOwner(ownerId));
        wallet.setCurrency(resolveCurrency(currencyCode));
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
        return walletRepository.findAllByOwnerId(ownerId);
    }
    
    @Transactional(readOnly = true)
    public List<Wallet> getAll() {
        return walletRepository.findAll();
    }
    
    public Wallet update(Wallet wallet, Long ownerId, String currencyCode) {
        if (ownerId != null) {
            wallet.setOwner(resolveOwner(ownerId));
        }
        if (currencyCode != null && !currencyCode.isBlank()) {
            wallet.setCurrency(resolveCurrency(currencyCode));
        }
        return walletRepository.save(wallet);
    }
    
    public void delete(Long id) {
        if (!walletRepository.existsById(id)) {
            throw new WalletNotFoundException(id);
        }
        walletRepository.deleteById(id);
    }
    
    private User resolveOwner(Long ownerId) {
        return userService.getById(ownerId);
    }

    private Currency resolveCurrency(String code) {
        return currencyService.getActiveByCode(code.trim());
    }
}
