package com.shmoney.wallet.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.wallet.dto.*;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.entity.WalletStatus;
import com.shmoney.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Tag(name = "Wallets")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    
    private final WalletService walletService;
    private final WalletMapper walletMapper;
    private final CurrentUserProvider currentUserProvider;
    
    public WalletController(WalletService walletService,
                            WalletMapper walletMapper,
                            CurrentUserProvider currentUserProvider) {
        this.walletService = walletService;
        this.walletMapper = walletMapper;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Создать кошелек")
    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody WalletCreateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Long ownerId = resolveOwnerId(request.ownerId(), current);
        Wallet wallet = walletMapper.toEntity(request);
        Wallet created = walletService.create(ownerId, wallet, request.currencyCode(), request.balance());
        WalletResponse response = walletMapper.toResponse(created);
        
        return ResponseEntity
                .created(URI.create("/api/wallets/" + response.id()))
                .body(response);
    }
    
    @Operation(summary = "Получить кошелек по id")
    @GetMapping("/{id}")
    public WalletResponse getById(@PathVariable Long id) {
        Wallet wallet = walletService.getById(id);
        ensureCanAccess(wallet);
        
        return walletMapper.toResponse(wallet);
    }
    
    @Operation(summary = "Список кошельков (только свои)")
    @GetMapping
    public List<WalletResponse> getAll() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        List<Wallet> wallets = walletService.getByOwner(current.id());

        return wallets.stream()
                .map(walletMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Общий баланс по валютам")
    @GetMapping("/balances")
    public List<WalletCurrencyBalanceResponse> getBalancesByCurrency() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        List<WalletService.CurrencyBalance> balances = walletService.getCurrencyBalancesForOwner(current.id());
        
        return balances.stream()
                .map(balance -> new WalletCurrencyBalanceResponse(balance.currencyCode(), balance.totalBalance()))
                .toList();
    }
    
    @Operation(summary = "Обновить кошелек")
    @PatchMapping("/{id}")
    public WalletResponse update(@PathVariable Long id, @Valid @RequestBody WalletUpdateRequest request) {
        Wallet existing = walletService.getById(id);
        AuthenticatedUser current = ensureCanAccess(existing);
        Long ownerId = resolveOwnerIdForUpdate(request.ownerId(), current, existing);
        walletMapper.updateEntity(request, existing);
        Wallet updated = walletService.update(
                existing,
                ownerId,
                request.currencyCode(),
                request.type(),
                request.balance(),
                request.color()
        );
        
        return walletMapper.toResponse(updated);
    }
    
    @Operation(summary = "Изменить статус кошелька")
    @PatchMapping("/{id}/status")
    public WalletResponse updateStatus(@PathVariable Long id,
                                       @Valid @RequestBody WalletStatusUpdateRequest request) {
        Wallet wallet = walletService.getById(id);
        ensureCanAccess(wallet);
        Wallet updated = walletService.updateStatus(id, request.status());
        return walletMapper.toResponse(updated);
    }
    
    private AuthenticatedUser ensureCanAccess(Wallet wallet) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        boolean isOwner = wallet.getOwner().getId().equals(current.id());
        
        if (!isOwner) throw new AccessDeniedException("Forbidden");
        
        return current;
    }
    
    private Long resolveOwnerId(Long requestedOwnerId, AuthenticatedUser current) {
        if (requestedOwnerId == null || requestedOwnerId.equals(current.id())) {
            return current.id();
        }
        
        throw new AccessDeniedException("Forbidden");
    }
    
    private Long resolveOwnerIdForUpdate(Long requestedOwnerId,
                                         AuthenticatedUser current,
                                         Wallet existing) {
        Long currentOwnerId = existing.getOwner().getId();
        
        if (!currentOwnerId.equals(current.id())) throw new AccessDeniedException("Forbidden");
        
        if (requestedOwnerId == null || requestedOwnerId.equals(currentOwnerId)) {
            return null;
        }
        
        throw new AccessDeniedException("Forbidden");
    }
}
