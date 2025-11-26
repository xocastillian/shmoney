package com.shmoney.wallet.transaction.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.service.WalletService;
import com.shmoney.wallet.transaction.dto.WalletTransactionMapper;
import com.shmoney.wallet.transaction.dto.WalletTransactionRequest;
import com.shmoney.wallet.transaction.dto.WalletTransactionResponse;
import com.shmoney.wallet.transaction.dto.WalletTransactionUpdateRequest;
import com.shmoney.wallet.transaction.entity.WalletTransaction;
import com.shmoney.wallet.transaction.service.WalletTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Wallet Transactions")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/wallet-transactions")
public class WalletTransactionController {
    
    private final WalletTransactionService walletTransactionService;
    private final WalletService walletService;
    private final WalletTransactionMapper walletTransactionMapper;
    private final CurrentUserProvider currentUserProvider;
    
    public WalletTransactionController(WalletTransactionService walletTransactionService,
                                       WalletService walletService,
                                       WalletTransactionMapper walletTransactionMapper,
                                       CurrentUserProvider currentUserProvider) {
        this.walletTransactionService = walletTransactionService;
        this.walletService = walletService;
        this.walletTransactionMapper = walletTransactionMapper;
        this.currentUserProvider = currentUserProvider;
    }
    
    @Operation(summary = "Создать транзакцию между кошельками")
    @PostMapping
    public ResponseEntity<WalletTransactionResponse> create(@Valid @RequestBody WalletTransactionRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        Wallet fromWallet = walletService.getById(request.fromWalletId());
        Wallet toWallet = walletService.getById(request.toWalletId());
        ensureCanTransfer(current, fromWallet, toWallet);
        
        WalletTransaction transaction = walletTransactionService.create(
                fromWallet,
                toWallet,
                request.amount(),
                request.executedAt(),
                request.description());
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletTransactionMapper.toResponse(transaction));
    }
    
    @Operation(summary = "Получить список транзакций (все для ADMIN, либо по кошельку)")
    @GetMapping
    public List<WalletTransactionResponse> getTransactions(@RequestParam(required = false) Long walletId) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        List<WalletTransaction> transactions;
        
        if (walletId == null) {
            ensureAdmin(current);
            transactions = walletTransactionService.getAll();
        } else {
            Wallet wallet = walletService.getById(walletId);
            ensureCanAccess(current, wallet);
            transactions = walletTransactionService.getByWallet(walletId);
        }
        
        return transactions.stream()
                .map(walletTransactionMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Получить транзакцию по id")
    @GetMapping("/{id}")
    public WalletTransactionResponse getTransaction(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        WalletTransaction transaction = walletTransactionService.getById(id);
        ensureCanAccess(current, transaction);
        return walletTransactionMapper.toResponse(transaction);
    }

    @Operation(summary = "Удалить транзакцию между кошельками")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        WalletTransaction transaction = walletTransactionService.getById(id);
        ensureCanAccess(current, transaction);
        walletTransactionService.delete(transaction);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновить транзакцию между кошельками")
    @PatchMapping("/{id}")
    public WalletTransactionResponse update(@PathVariable Long id,
                                            @Valid @RequestBody WalletTransactionUpdateRequest request) {
        AuthenticatedUser current = currentUserProvider.requireCurrentUser();
        WalletTransaction transaction = walletTransactionService.getById(id);
        ensureCanAccess(current, transaction);

        Wallet fromWallet = request.fromWalletId() == null
                ? transaction.getFromWallet()
                : walletService.getById(request.fromWalletId());
        Wallet toWallet = request.toWalletId() == null
                ? transaction.getToWallet()
                : walletService.getById(request.toWalletId());

        ensureCanTransfer(current, fromWallet, toWallet);

        WalletTransaction updated = walletTransactionService.update(transaction, request, fromWallet, toWallet);
        return walletTransactionMapper.toResponse(updated);
    }
    
    private void ensureCanTransfer(AuthenticatedUser current, Wallet fromWallet, Wallet toWallet) {
        boolean isAdmin = currentUserProvider.isAdmin(current);
        
        if (!isAdmin && !fromWallet.getOwner().getId().equals(current.id())) {
            throw new AccessDeniedException("Forbidden");
        }
        
        if (!isAdmin && !toWallet.getOwner().getId().equals(current.id())) {
            throw new AccessDeniedException("Forbidden");
        }
    }
    
    private void ensureCanAccess(AuthenticatedUser current, Wallet wallet) {
        if (!wallet.getOwner().getId().equals(current.id()) && !currentUserProvider.isAdmin(current)) {
            throw new AccessDeniedException("Forbidden");
        }
    }

    private void ensureCanAccess(AuthenticatedUser current, WalletTransaction transaction) {
        boolean isAdmin = currentUserProvider.isAdmin(current);
        if (isAdmin) {
            return;
        }
        Long currentId = current.id();
        boolean ownsFrom = transaction.getFromWallet().getOwner().getId().equals(currentId);
        boolean ownsTo = transaction.getToWallet().getOwner().getId().equals(currentId);
        if (!ownsFrom && !ownsTo) {
            throw new AccessDeniedException("Forbidden");
        }
    }
    
    private void ensureAdmin(AuthenticatedUser current) {
        if (!currentUserProvider.isAdmin(current)) throw new AccessDeniedException("Forbidden");
    }
}
