package com.shmoney.currency.exception;

public class CurrencyNotFoundException extends RuntimeException {
    
    public CurrencyNotFoundException(String code) {
        super("Currency not found for code=" + code);
    }
}
