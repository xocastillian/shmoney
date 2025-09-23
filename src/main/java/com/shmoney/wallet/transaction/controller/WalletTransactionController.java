package com.shmoney.wallet.transaction.controller;

import com.shmoney.auth.security.AuthenticatedUser;
import com.shmoney.auth.security.CurrentUserProvider;
import com.shmoney.wallet.entity.Wallet;
import com.shmoney.wallet.service.WalletService;
import com.shmoney.wallet.transaction.dto.WalletTransactionMapper;
import com.shmoney.wallet.transaction.dto.WalletTransactionRequest;
import com.shmoney.wallet.transaction.dto.WalletTransactionResponse;
import com.shmoney.wallet.transaction.entity.WalletTransaction;
import com.shmoney.wallet.transaction.service.WalletTransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    private void ensureAdmin(AuthenticatedUser current) {
        if (!currentUserProvider.isAdmin(current)) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
