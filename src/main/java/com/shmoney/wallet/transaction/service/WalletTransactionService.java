package com.shmoney.wallet.transaction.service;

import com.shmoney.currency.entity.Currency;
import com.shmoney.currency.service.ExchangeRateService;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.repository.WalletRepository;
import com.shmoney.wallet.transaction.entity.WalletTransaction;
import com.shmoney.wallet.transaction.repository.WalletTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class WalletTransactionService {
    
    private static final int AMOUNT_SCALE = 2;
    private static final int RATE_SCALE = 6;
    
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletRepository walletRepository;
    private final ExchangeRateService exchangeRateService;
    
    public WalletTransactionService(WalletTransactionRepository walletTransactionRepository,
                                    WalletRepository walletRepository,
                                    ExchangeRateService exchangeRateService) {
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletRepository = walletRepository;
        this.exchangeRateService = exchangeRateService;
    }
    
    public WalletTransaction create(Wallet fromWallet,
                                    Wallet toWallet,
                                    BigDecimal amount,
                                    OffsetDateTime executedAt,
                                    String description) {
        if (fromWallet.getId().equals(toWallet.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same wallet");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        
        Currency sourceCurrency = fromWallet.getCurrency();
        Currency targetCurrency = toWallet.getCurrency();
        
        BigDecimal normalizedAmount = amount.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        BigDecimal rate;
        BigDecimal targetAmount;
        
        if (sourceCurrency.getId().equals(targetCurrency.getId())) {
            rate = BigDecimal.ONE;
        } else {
            rate = exchangeRateService.getRate(sourceCurrency.getCode(), targetCurrency.getCode());
        }
        
        rate = rate.setScale(RATE_SCALE, RoundingMode.HALF_UP);
        
        if (sourceCurrency.getId().equals(targetCurrency.getId())) {
            targetAmount = normalizedAmount;
        } else {
            targetAmount = normalizedAmount
                    .multiply(rate)
                    .setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        }
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setFromWallet(fromWallet);
        transaction.setToWallet(toWallet);
        transaction.setSourceCurrency(sourceCurrency);
        transaction.setTargetCurrency(targetCurrency);
        transaction.setSourceAmount(normalizedAmount);
        transaction.setTargetAmount(targetAmount);
        transaction.setExchangeRate(rate);
        transaction.setDescription(description);
        transaction.setExecutedAt(executedAt);

        updateBalances(fromWallet, toWallet, normalizedAmount, targetAmount);

        return walletTransactionRepository.save(transaction);
    }

    private void updateBalances(Wallet fromWallet,
                                Wallet toWallet,
                                BigDecimal sourceAmount,
                                BigDecimal targetAmount) {
        BigDecimal sourceBalance = fromWallet.getBalance() == null ? BigDecimal.ZERO : fromWallet.getBalance();
        BigDecimal targetBalance = toWallet.getBalance() == null ? BigDecimal.ZERO : toWallet.getBalance();

        fromWallet.setBalance(sourceBalance.subtract(sourceAmount));
        toWallet.setBalance(targetBalance.add(targetAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
    }
    
    @Transactional(readOnly = true)
    public List<WalletTransaction> getAll() {
        return walletTransactionRepository.findAllByOrderByExecutedAtDesc();
    }
    
    @Transactional(readOnly = true)
    public List<WalletTransaction> getByWallet(Long walletId) {
        return walletTransactionRepository.findAllByFromWalletIdOrToWalletIdOrderByExecutedAtDesc(walletId, walletId);
    }
}
