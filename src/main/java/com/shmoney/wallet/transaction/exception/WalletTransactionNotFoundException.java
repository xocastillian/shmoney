package com.shmoney.wallet.transaction.exception;

public class WalletTransactionNotFoundException extends RuntimeException {

    public WalletTransactionNotFoundException(Long id) {
        super("Wallet transaction not found: " + id);
    }
}
