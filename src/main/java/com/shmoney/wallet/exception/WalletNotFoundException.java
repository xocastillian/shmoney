package com.shmoney.wallet.exception;

public class WalletNotFoundException extends RuntimeException {
    
    public WalletNotFoundException(Long id) {
        super("Wallet not found for id=" + id);
    }
}
