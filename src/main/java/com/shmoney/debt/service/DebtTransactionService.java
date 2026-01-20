package com.shmoney.debt.service;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.currency.service.ExchangeRateService;
import com.shmoney.debt.dto.DebtTransactionCreateRequest;
import com.shmoney.debt.dto.DebtTransactionFilter;
import com.shmoney.debt.dto.DebtTransactionUpdateRequest;
import com.shmoney.debt.entity.DebtCounterparty;
import com.shmoney.debt.entity.DebtTransaction;
import com.shmoney.debt.entity.DebtTransactionDirection;
import com.shmoney.debt.exception.DebtCounterpartyNotFoundException;
import com.shmoney.debt.exception.DebtTransactionNotFoundException;
import com.shmoney.debt.exception.InvalidDebtTransactionException;
import com.shmoney.debt.repository.DebtCounterpartyRepository;
import com.shmoney.debt.repository.DebtTransactionRepository;
import com.shmoney.debt.repository.DebtTransactionSpecifications;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.repository.WalletRepository;
import com.shmoney.wallet.service.WalletService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
public class DebtTransactionService {
    
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    
    private final DebtTransactionRepository transactionRepository;
    private final DebtCounterpartyRepository counterpartyRepository;
    private final WalletService walletService;
    private final WalletRepository walletRepository;
    private final ExchangeRateService exchangeRateService;
    
    public DebtTransactionService(DebtTransactionRepository transactionRepository,
                                  DebtCounterpartyRepository counterpartyRepository,
                                  WalletService walletService,
                                  WalletRepository walletRepository,
                                  ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.walletService = walletService;
        this.walletRepository = walletRepository;
        this.exchangeRateService = exchangeRateService;
    }
    
    public DebtTransaction create(AuthenticatedUser currentUser, DebtTransactionCreateRequest request) {
        Wallet wallet = requireWalletOwner(currentUser.id(), request.walletId());
        DebtCounterparty counterparty = counterpartyRepository
                .findByIdAndUserId(request.counterpartyId(), currentUser.id())
                .orElseThrow(() -> new DebtCounterpartyNotFoundException(request.counterpartyId()));
        
        if (!wallet.getCurrency().getId().equals(request.currencyId())) {
            throw new InvalidDebtTransactionException("Валюта операции должна совпадать с валютой кошелька");
        }
        
        DebtTransaction transaction = new DebtTransaction();
        transaction.setUser(wallet.getOwner());
        transaction.setCounterparty(counterparty);
        transaction.setWallet(wallet);
        transaction.setDirection(request.direction());
        transaction.setAmount(normalizeAmount(request.amount()));
        transaction.setCurrency(wallet.getCurrency());
        transaction.setDescription(request.description());
        transaction.setOccurredAt(request.occurredAt());
        
        DebtTransaction saved = transactionRepository.save(transaction);
        applyWalletDelta(wallet, saved.getDirection(), saved.getAmount());
        updateAggregates(counterparty, saved.getDirection(), saved.getAmount(), wallet.getCurrency().getCode());

        return saved;
    }
    
    @Transactional(readOnly = true)
    public Page<DebtTransaction> getPage(Long userId, DebtTransactionFilter filter, Pageable pageable) {
        Specification<DebtTransaction> specification = Specification
                .where(DebtTransactionSpecifications.belongsToUser(userId))
                .and(DebtTransactionSpecifications.hasCounterparty(filter.counterpartyId()))
                .and(DebtTransactionSpecifications.hasDirection(filter.direction()))
                .and(DebtTransactionSpecifications.occurredAfter(filter.from()))
                .and(DebtTransactionSpecifications.occurredBefore(filter.to()));
        
        return transactionRepository.findAll(specification, pageable);
    }
    
    @Transactional(readOnly = true)
    public DebtTransaction getOwnedById(Long userId, Long id) {
        return transactionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new DebtTransactionNotFoundException(id));
    }

    public DebtTransaction update(AuthenticatedUser currentUser, Long id, DebtTransactionUpdateRequest request) {
        DebtTransaction existing = getOwnedById(currentUser.id(), id);
        Wallet originalWallet = existing.getWallet();
        DebtTransactionDirection originalDirection = existing.getDirection();
        BigDecimal originalAmount = existing.getAmount();
        DebtCounterparty originalCounterparty = existing.getCounterparty();

        Wallet targetWallet = request.walletId() == null
                ? originalWallet
                : requireWalletOwner(currentUser.id(), request.walletId());
        DebtCounterparty targetCounterparty = request.counterpartyId() == null
                ? originalCounterparty
                : counterpartyRepository.findByIdAndUserId(request.counterpartyId(), currentUser.id())
                        .orElseThrow(() -> new DebtCounterpartyNotFoundException(request.counterpartyId()));

        if (request.currencyId() != null && !targetWallet.getCurrency().getId().equals(request.currencyId())) {
            throw new InvalidDebtTransactionException("Валюта операции должна совпадать с валютой кошелька");
        }

        DebtTransactionDirection direction = request.direction() == null ? existing.getDirection() : request.direction();
        BigDecimal amount = request.amount() == null ? existing.getAmount() : normalizeAmount(request.amount());

        existing.setWallet(targetWallet);
        existing.setUser(targetWallet.getOwner());
        existing.setCounterparty(targetCounterparty);
        existing.setDirection(direction);
        existing.setAmount(amount);
        existing.setCurrency(targetWallet.getCurrency());
        if (request.description() != null) {
            existing.setDescription(request.description());
        }
        if (request.occurredAt() != null) {
            existing.setOccurredAt(request.occurredAt());
        }

        DebtTransaction saved = transactionRepository.save(existing);
        revertWalletDelta(originalWallet, originalDirection, originalAmount);
        applyWalletDelta(targetWallet, saved.getDirection(), saved.getAmount());
        recalculateAggregates(originalCounterparty);
        if (!originalCounterparty.getId().equals(targetCounterparty.getId())) {
            recalculateAggregates(targetCounterparty);
        }
        return saved;
    }

    public void delete(AuthenticatedUser currentUser, Long id) {
        DebtTransaction transaction = getOwnedById(currentUser.id(), id);
        revertWalletDelta(transaction.getWallet(), transaction.getDirection(), transaction.getAmount());
        transactionRepository.delete(transaction);
        recalculateAggregates(transaction.getCounterparty());
    }
    
    public Pageable buildPageable(int page, int size) {
        int resolvedSize = size <= 0 ? 50 : Math.min(size, 100);
        int resolvedPage = Math.max(page, 0);
        
        Sort sort = Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id"));
        
        return PageRequest.of(resolvedPage, resolvedSize, sort);
    }
    
    private Wallet requireWalletOwner(Long userId, Long walletId) {
        Wallet wallet = walletService.getById(walletId);
        
        if (!wallet.getOwner().getId().equals(userId)) {
            throw new InvalidDebtTransactionException("Кошелек не принадлежит пользователю");
        }
        
        return wallet;
    }

    private void applyWalletDelta(Wallet wallet, DebtTransactionDirection direction, BigDecimal amount) {
        BigDecimal delta = amount;
        if (direction == DebtTransactionDirection.LENT) {
            delta = delta.negate();
        }
        updateWalletBalance(wallet, delta);
    }

    private void revertWalletDelta(Wallet wallet, DebtTransactionDirection direction, BigDecimal amount) {
        BigDecimal delta = direction == DebtTransactionDirection.LENT ? amount : amount.negate();
        updateWalletBalance(wallet, delta);
    }

    private void updateWalletBalance(Wallet wallet, BigDecimal delta) {
        BigDecimal currentBalance = wallet.getBalance() == null
                ? BigDecimal.ZERO
                : wallet.getBalance();
        BigDecimal updated = currentBalance.add(delta);
        wallet.setBalance(updated);
        walletRepository.save(wallet);
    }
    
    private void updateAggregates(DebtCounterparty counterparty,
                                  DebtTransactionDirection direction,
                                  BigDecimal amount,
                                  String walletCurrency) {
        BigDecimal converted = convert(amount, walletCurrency, counterparty.getCurrency().getCode());
        BigDecimal owedToMe = valueOrZero(counterparty.getOwedToMe());
        BigDecimal iOwe = valueOrZero(counterparty.getIOwe());

        if (direction == DebtTransactionDirection.LENT) {
            if (iOwe.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal updatedIowe = iOwe.subtract(converted);
                if (updatedIowe.compareTo(BigDecimal.ZERO) < 0) {
                    iOwe = BigDecimal.ZERO;
                    owedToMe = owedToMe.add(updatedIowe.abs());
                } else {
                    iOwe = updatedIowe;
                }
            } else {
                owedToMe = owedToMe.add(converted);
            }
        } else {
            if (owedToMe.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal updatedOwed = owedToMe.subtract(converted);
                if (updatedOwed.compareTo(BigDecimal.ZERO) < 0) {
                    owedToMe = BigDecimal.ZERO;
                    iOwe = iOwe.add(updatedOwed.abs());
                } else {
                    owedToMe = updatedOwed;
                }
            } else {
                iOwe = iOwe.add(converted);
            }
        }

        counterparty.setOwedToMe(owedToMe);
        counterparty.setIOwe(iOwe);
        counterpartyRepository.save(counterparty);
    }

    private void recalculateAggregates(DebtCounterparty counterparty) {
        var transactions = transactionRepository.findAllByCounterpartyId(counterparty.getId());
        BigDecimal totalLent = BigDecimal.ZERO;
        BigDecimal totalBorrowed = BigDecimal.ZERO;
        String targetCurrency = counterparty.getCurrency().getCode();
        for (DebtTransaction tx : transactions) {
            BigDecimal converted = convert(tx.getAmount(), tx.getCurrency().getCode(), targetCurrency);
            if (tx.getDirection() == DebtTransactionDirection.LENT) {
                totalLent = totalLent.add(converted);
            } else {
                totalBorrowed = totalBorrowed.add(converted);
            }
        }
        BigDecimal owedToMe = totalLent.subtract(totalBorrowed);
        BigDecimal iOwe = totalBorrowed.subtract(totalLent);
        counterparty.setOwedToMe(owedToMe.compareTo(BigDecimal.ZERO) < 0 ? ZERO : owedToMe);
        counterparty.setIOwe(iOwe.compareTo(BigDecimal.ZERO) < 0 ? ZERO : iOwe);
        counterpartyRepository.save(counterparty);
    }
    
    private BigDecimal convert(BigDecimal amount, String from, String to) {
        if (amount == null) return ZERO;
        
        if (from.equalsIgnoreCase(to)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        
        return exchangeRateService.convert(amount, from, to);
    }
    
    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDebtTransactionException("Сумма операции должна быть больше нуля");
        }
        
        return value.setScale(2, RoundingMode.HALF_UP);
    }
    
    private void ensureNonNegative(BigDecimal value, String message) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidDebtTransactionException(message);
        }
    }
    
    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
