package com.shmoney.wallet.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.wallet.dto.WalletCreateRequest;
import com.shmoney.wallet.dto.WalletMapper;
import com.shmoney.wallet.dto.WalletResponse;
import com.shmoney.wallet.dto.WalletUpdateRequest;
import com.shmoney.wallet.entity.Wallet;
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
    
    @Operation(summary = "Список кошельков (свои или все для ADMIN)")
    @GetMapping
    public List<WalletResponse> getAll() {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        List<Wallet> wallets = currentUserProvider.isAdmin(current)
                ? walletService.getAll()
                : walletService.getByOwner(current.id());
        
        return wallets.stream()
                .map(walletMapper::toResponse)
                .toList();
    }
    
    @Operation(summary = "Обновить кошелек")
    @PatchMapping("/{id}")
    public WalletResponse update(@PathVariable Long id, @Valid @RequestBody WalletUpdateRequest request) {
        Wallet existing = walletService.getById(id);
        AuthenticatedUser current = ensureCanAccess(existing);
        Long ownerId = resolveOwnerIdForUpdate(request.ownerId(), current, existing);
        walletMapper.updateEntity(request, existing);
        Wallet updated = walletService.update(existing, ownerId, request.currencyCode());

        return walletMapper.toResponse(updated);
    }
    
    @Operation(summary = "Удалить кошелек")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Wallet wallet = walletService.getById(id);
        ensureCanAccess(wallet);
        walletService.delete(id);
        
        return ResponseEntity.noContent().build();
    }
    
    private AuthenticatedUser ensureCanAccess(Wallet wallet) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        boolean isOwner = wallet.getOwner().getId().equals(current.id());
        
        if (!isOwner && !currentUserProvider.isAdmin(current)) throw new AccessDeniedException("Forbidden");
        
        return current;
    }
    
    private Long resolveOwnerId(Long requestedOwnerId, AuthenticatedUser current) {
        if (requestedOwnerId == null) return current.id();
        if (currentUserProvider.isAdmin(current)) return requestedOwnerId;
        if (!requestedOwnerId.equals(current.id())) throw new AccessDeniedException("Forbidden");
        
        return requestedOwnerId;
    }
    
    private Long resolveOwnerIdForUpdate(Long requestedOwnerId,
                                         AuthenticatedUser current,
                                         Wallet existing) {
        if (requestedOwnerId == null) return null;
        if (currentUserProvider.isAdmin(current)) return requestedOwnerId;
        
        Long currentOwnerId = existing.getOwner().getId();
        
        if (!requestedOwnerId.equals(currentOwnerId)) throw new AccessDeniedException("Forbidden");
        
        return requestedOwnerId;
    }
}
